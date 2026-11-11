package com.atirath.trading.modules.auth.service;

import com.atirath.trading.common.ApiException;
import com.atirath.trading.common.OtpChannel;
import com.atirath.trading.common.OtpPurpose;
import com.atirath.trading.common.ResourceNotFoundException;
import com.atirath.trading.config.AppProperties;
import com.atirath.trading.modules.auth.dto.OtpResponse;
import com.atirath.trading.modules.auth.dto.OtpSendRequest;
import com.atirath.trading.modules.auth.dto.OtpVerifyRequest;
import com.atirath.trading.modules.auth.repository.UserAccountRepository;
import com.atirath.trading.modules.auth.service.gateway.EmailService;
import com.atirath.trading.modules.auth.service.gateway.SmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpService {

    private final EmailService emailService;
    private final SmsService smsService;
    private final AppProperties appProperties;
    private final UserAccountRepository userAccountRepository;

    // Fast in-memory cache for OTP storage with TTL (Zero DB changes)
    private final Map<String, OtpRecord> otpStore = new ConcurrentHashMap<>();
    // Short-lived verified marker after OTP verification (used e.g. by web mobile password reset)
    private final Map<String, Instant> verifiedStore = new ConcurrentHashMap<>();

    private record OtpRecord(String code, Instant expiresAt, OtpPurpose purpose, String sessionToken) {
        boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }
    }

    public OtpResponse sendOtp(OtpSendRequest request) {
        String target = request.getTarget().trim();

        if (request.getPurpose() == OtpPurpose.PASSWORD_RESET) {
            if (request.getChannel() == OtpChannel.EMAIL) {
                if (!userAccountRepository.existsByEmailIgnoreCase(target)) {
                    throw new ResourceNotFoundException("No account found with email: " + target);
                }
            } else {
                if (!userAccountRepository.existsByMobileNumber(target)) {
                    throw new ResourceNotFoundException("No account found with mobile number: " + target);
                }
            }
        }

        long expirySeconds = (request.getChannel() == OtpChannel.EMAIL)
                ? appProperties.getOtp().getExpirationSeconds()
                : appProperties.getOtp().getMobileExpirationSeconds();

        // Generate 6-digit cryptographic OTP
        String otpCode = String.valueOf(100000 + new SecureRandom().nextInt(900000));
        String sessionToken = UUID.randomUUID().toString();

        Instant expiresAt = Instant.now().plusSeconds(expirySeconds);
        String cacheKey = buildCacheKey(target, request.getPurpose());
        otpStore.put(cacheKey, new OtpRecord(otpCode, expiresAt, request.getPurpose(), sessionToken));

        // Dispatch via chosen Gateway
        if (request.getChannel() == OtpChannel.EMAIL) {
            emailService.sendEmailOtp(target, otpCode, request.getPurpose().name());
        } else {
            smsService.sendSmsOtp(target, otpCode, request.getPurpose().name());
        }

        String devOtp = appProperties.getOtp().isDevModeEnabled() ? otpCode : null;

        return OtpResponse.builder()
                .success(true)
                .message("OTP sent successfully to " + maskTarget(target))
                .expiresInSeconds(expirySeconds)
                .sessionToken(sessionToken)
                .devOtp(devOtp)
                .build();
    }

    public boolean verifyOtp(OtpVerifyRequest request) {
        String target = request.getTarget().trim();
        String cacheKey = buildCacheKey(target, request.getPurpose());

        OtpRecord record = otpStore.get(cacheKey);

        // Universal test OTP support in dev mode
        if (appProperties.getOtp().isDevModeEnabled() && "123456".equals(request.getOtpCode().trim())) {
            otpStore.remove(cacheKey);
            verifiedStore.put(cacheKey, Instant.now().plusSeconds(600));
            return true;
        }

        if (record == null) {
            throw new ApiException("No active OTP found for " + maskTarget(target) + ". Please request a new code.");
        }

        if (record.isExpired()) {
            otpStore.remove(cacheKey);
            throw new ApiException("OTP has expired. Please request a new code.");
        }

        if (!record.code().equals(request.getOtpCode().trim())) {
            throw new ApiException("Invalid OTP code. Please enter the correct 6-digit code.");
        }

        // Successfully verified -> Invalidate OTP and record short-lived verified state
        otpStore.remove(cacheKey);
        verifiedStore.put(cacheKey, Instant.now().plusSeconds(600));
        return true;
    }

    /**
     * Verifies and consumes the one-time verified OTP state for sensitive follow-up actions (e.g. password reset).
     */
    public void consumeVerifiedOtp(String target, OtpPurpose purpose) {
        String cacheKey = buildCacheKey(target.trim(), purpose);
        Instant validUntil = verifiedStore.remove(cacheKey);
        if (validUntil == null || Instant.now().isAfter(validUntil)) {
            throw new ApiException("OTP verification is required or has expired. Please verify OTP first.");
        }
    }

    private String buildCacheKey(String target, OtpPurpose purpose) {
        return target.toLowerCase() + ":" + purpose.name();
    }

    private String maskTarget(String target) {
        if (target.contains("@")) {
            int atIndex = target.indexOf('@');
            if (atIndex <= 2) return target;
            return target.charAt(0) + "***" + target.substring(atIndex - 1);
        } else if (target.length() >= 6) {
            return "******" + target.substring(target.length() - 4);
        }
        return target;
    }
}
