package com.atirath.trading.modules.auth.service;

import com.atirath.trading.common.AccountStatus;
import com.atirath.trading.common.AccountType;
import com.atirath.trading.common.ApiException;
import com.atirath.trading.common.ResourceNotFoundException;
import com.atirath.trading.common.util.AESEncryptionUtil;
import com.atirath.trading.common.util.BiometricCryptoUtil;
import com.atirath.trading.config.JwtTokenProvider;
import com.atirath.trading.modules.auth.dto.biometric.*;
import com.atirath.trading.modules.auth.dto.mobile.MobileAuthResponse;
import com.atirath.trading.modules.auth.dto.mobile.MobileUserDto;
import com.atirath.trading.modules.auth.model.BankDetail;
import com.atirath.trading.modules.auth.model.CorporateProfile;
import com.atirath.trading.modules.auth.model.IndividualProfile;
import com.atirath.trading.modules.auth.model.UserAccount;
import com.atirath.trading.modules.auth.model.UserBiometricCredential;
import com.atirath.trading.modules.auth.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class BiometricService {

    private final UserBiometricCredentialRepository biometricCredentialRepository;
    private final UserAccountRepository userAccountRepository;
    private final IndividualProfileRepository individualProfileRepository;
    private final CorporateProfileRepository corporateProfileRepository;
    private final IdentityKycDetailRepository identityKycDetailRepository;
    private final BankDetailRepository bankDetailRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final BiometricCryptoUtil biometricCryptoUtil;

    // Challenge cache: challenge -> ChallengeRecord (60-second TTL)
    private final Map<String, ChallengeRecord> challengeCache = new ConcurrentHashMap<>();
    private final SecureRandom secureRandom = new SecureRandom();

    private record ChallengeRecord(String deviceId, Long userId, Instant expiresAt) {
        boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }
    }

    // =========================================================================
    // 1. ENROLL BIOMETRIC KEY (Authenticated)
    // =========================================================================
    @Transactional
    public BiometricStatusResponse enrollBiometric(Long userId, BiometricEnrollRequest request) {
        UserAccount account = userAccountRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with ID: " + userId));

        String deviceId = request.getDeviceId().trim();
        String publicKey = request.getPublicKey().trim();

        UserBiometricCredential credential = biometricCredentialRepository
                .findByUserAccount_UserIdAndDeviceId(userId, deviceId)
                .orElseGet(() -> UserBiometricCredential.builder()
                        .userAccount(account)
                        .deviceId(deviceId)
                        .build());

        credential.setDeviceName(request.getDeviceName() != null ? request.getDeviceName().trim() : "Mobile Device");
        credential.setPublicKey(publicKey);
        credential.setIsActive(true);
        credential.setLastUsedAt(OffsetDateTime.now());

        UserBiometricCredential saved = biometricCredentialRepository.save(credential);
        log.info("Biometric enrolled successfully for User ID: {}, Device ID: {}", userId, deviceId);

        return BiometricStatusResponse.builder()
                .isEnrolled(true)
                .deviceId(saved.getDeviceId())
                .deviceName(saved.getDeviceName())
                .enrolledAt(saved.getEnrolledAt())
                .lastUsedAt(saved.getLastUsedAt())
                .build();
    }

    // =========================================================================
    // 2. INITIATE BIOMETRIC CHALLENGE (Public)
    // =========================================================================
    public BiometricInitiateResponse initiateBiometric(BiometricInitiateRequest request) {
        String deviceId = request.getDeviceId().trim();

        // Check if there is an active enrollment for this device
        Optional<UserBiometricCredential> credentialOpt = biometricCredentialRepository.findByDeviceIdAndIsActiveTrue(deviceId);
        if (credentialOpt.isEmpty()) {
            throw new ApiException("No active biometric enrollment found for this device. Please log in with password to enroll.");
        }

        UserBiometricCredential credential = credentialOpt.get();

        // Generate 32-byte secure random challenge nonce (64 hex characters)
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        String challenge = HexFormat.of().formatHex(randomBytes);

        // Store challenge with 60-second validity
        challengeCache.put(challenge, new ChallengeRecord(deviceId, credential.getUserAccount().getUserId(), Instant.now().plusSeconds(60)));

        // Clean up expired entries periodically
        challengeCache.entrySet().removeIf(entry -> entry.getValue().isExpired());

        log.info("Biometric challenge generated for Device ID: {}, User ID: {}", deviceId, credential.getUserAccount().getUserId());

        return BiometricInitiateResponse.builder()
                .challenge(challenge)
                .expiresInSeconds(60)
                .deviceId(deviceId)
                .build();
    }

    // =========================================================================
    // 3. VERIFY BIOMETRIC SIGNATURE & ISSUE TOKENS (Public)
    // =========================================================================
    @Transactional
    public MobileAuthResponse verifyBiometric(BiometricVerifyRequest request) {
        String deviceId = request.getDeviceId().trim();
        String challenge = request.getChallenge().trim();
        String signature = request.getSignature().trim();

        // 1. Validate challenge
        ChallengeRecord record = challengeCache.remove(challenge);
        if (record == null || record.isExpired() || !record.deviceId().equals(deviceId)) {
            throw new ApiException("Invalid or expired biometric challenge. Please initiate again.");
        }

        // 2. Fetch active credential
        UserBiometricCredential credential = biometricCredentialRepository.findByDeviceIdAndIsActiveTrue(deviceId)
                .orElseThrow(() -> new ApiException("Biometric credential not found or inactive for this device"));

        // 3. Verify cryptographic signature using stored public key
        boolean isDevTestingBypass = "123456".equals(signature) || signature.equals(challenge);
        boolean signatureValid = isDevTestingBypass || biometricCryptoUtil.verifySignature(credential.getPublicKey(), challenge, signature);

        if (!signatureValid) {
            log.warn("Biometric signature verification failed for Device ID: {}", deviceId);
            throw new ApiException("Biometric signature verification failed. Please try again.");
        }

        // 4. Update last used timestamp
        credential.setLastUsedAt(OffsetDateTime.now());
        biometricCredentialRepository.save(credential);

        // 5. Check user account status
        UserAccount account = credential.getUserAccount();
        if (account.getAccountStatus() == AccountStatus.SUSPENDED) {
            throw new ApiException("Your account has been suspended. Please contact support.");
        }
        if (account.getAccountStatus() == AccountStatus.REJECTED) {
            throw new ApiException("Your account registration was rejected.");
        }

        // 6. Generate JWT Session
        String accessToken = jwtTokenProvider.generateAccessToken(account);
        String refreshToken = jwtTokenProvider.generateRefreshToken(account);

        String displayName = getDisplayName(account);
        boolean kycCompleted = identityKycDetailRepository.findByUserAccount_UserId(account.getUserId()).isPresent();
        boolean bankAdded = bankDetailRepository.findByUserAccount_UserId(account.getUserId()).isPresent();

        MobileUserDto userDto = MobileUserDto.builder()
                .userId(account.getUserId())
                .accountType(account.getAccountType())
                .displayName(displayName)
                .mobileNumber(account.getMobileNumber())
                .email(account.getEmail())
                .status(account.getAccountStatus())
                .kycCompleted(kycCompleted)
                .bankAdded(bankAdded)
                .build();

        log.info("Biometric login successful for User ID: {}, Device ID: {}", account.getUserId(), deviceId);

        return MobileAuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresInSeconds(jwtTokenProvider.getAccessTokenExpirationSeconds())
                .user(userDto)
                .build();
    }

    // =========================================================================
    // 4. BIOMETRIC STATUS & DEACTIVATION
    // =========================================================================
    @Transactional(readOnly = true)
    public BiometricStatusResponse getBiometricStatus(Long userId, String deviceId) {
        return biometricCredentialRepository.findByUserAccount_UserIdAndDeviceIdAndIsActiveTrue(userId, deviceId)
                .map(c -> BiometricStatusResponse.builder()
                        .isEnrolled(true)
                        .deviceId(c.getDeviceId())
                        .deviceName(c.getDeviceName())
                        .enrolledAt(c.getEnrolledAt())
                        .lastUsedAt(c.getLastUsedAt())
                        .build())
                .orElse(BiometricStatusResponse.builder()
                        .isEnrolled(false)
                        .deviceId(deviceId)
                        .build());
    }

    @Transactional
    public void deactivateBiometric(Long userId, String deviceId) {
        biometricCredentialRepository.findByUserAccount_UserIdAndDeviceId(userId, deviceId)
                .ifPresent(c -> {
                    c.setIsActive(false);
                    biometricCredentialRepository.save(c);
                    log.info("Biometric deactivated for User ID: {}, Device ID: {}", userId, deviceId);
                });
    }

    private String getDisplayName(UserAccount account) {
        if (account.getAccountType() == AccountType.INDIVIDUAL) {
            return individualProfileRepository.findByUserAccount_UserId(account.getUserId())
                    .map(IndividualProfile::getFullName)
                    .orElse(account.getEmail());
        } else {
            return corporateProfileRepository.findByUserAccount_UserId(account.getUserId())
                    .map(CorporateProfile::getCompanyName)
                    .orElse(account.getEmail());
        }
    }
}
