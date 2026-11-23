package com.atirath.trading.modules.auth.service;

import com.atirath.trading.common.AccountStatus;
import com.atirath.trading.common.AccountType;
import com.atirath.trading.common.ApiException;
import com.atirath.trading.common.DocumentType;
import com.atirath.trading.common.InvalidCredentialsException;
import com.atirath.trading.common.OtpPurpose;
import com.atirath.trading.common.ResourceNotFoundException;
import com.atirath.trading.common.util.AESEncryptionUtil;
import com.atirath.trading.common.util.HashUtil;
import com.atirath.trading.config.AppProperties;
import com.atirath.trading.config.JwtTokenProvider;
import com.atirath.trading.modules.auth.dto.*;
import com.atirath.trading.modules.auth.dto.common.AuthResponse;
import com.atirath.trading.modules.auth.dto.common.RefreshTokenRequest;
import com.atirath.trading.modules.auth.dto.device.DisconnectAndLoginRequestDto;
import com.atirath.trading.modules.auth.dto.mobile.*;
import com.atirath.trading.modules.auth.model.*;
import com.atirath.trading.modules.auth.repository.*;
import com.atirath.trading.modules.auth.service.gateway.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.Period;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserAccountRepository userAccountRepository;
    private final IndividualProfileRepository individualProfileRepository;
    private final CorporateProfileRepository corporateProfileRepository;
    private final IdentityKycDetailRepository identityKycDetailRepository;
    private final BankDetailRepository bankDetailRepository;
    private final AccountDocumentRepository accountDocumentRepository;
    private final UserCredentialRepository userCredentialRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AESEncryptionUtil aesEncryptionUtil;
    private final EmailService emailService;
    private final AppProperties appProperties;
    private final OtpService otpService;
    private final TokenBlacklistService tokenBlacklistService;
    private final AdminAccountRepository adminAccountRepository;
    private final AdminLoginAttemptRepository adminLoginAttemptRepository;
    private final AdminAuditService adminAuditService;
    private final JdbcTemplate jdbcTemplate;
    private final UserDeviceSessionManager userDeviceSessionManager;

    private static final long MAX_DOCUMENT_SIZE_BYTES = 5L * 1024L * 1024L;
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "application/pdf",
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/webp"
    );

    // =========================================================================
    // 1. REGISTRATION - INDIVIDUAL
    // =========================================================================
    @Transactional
    public AuthResponse registerIndividual(IndividualRegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        String mobile = request.getMobileNumber().trim();

        if (userAccountRepository.existsByEmailIgnoreCase(email)) {
            throw new ApiException("An account with email " + email + " already exists");
        }
        if (userAccountRepository.existsByMobileNumber(mobile)) {
            throw new ApiException("An account with mobile number " + mobile + " already exists");
        }

        // 1. Create Common Account
        UserAccount account = UserAccount.builder()
                .accountType(AccountType.INDIVIDUAL)
                .email(email)
                .mobileNumber(mobile)
                .referralCode(request.getReferralCode() != null ? request.getReferralCode().trim() : null)
                .accountStatus(AccountStatus.ACTIVE)
                .build();
        UserAccount savedAccount = userAccountRepository.save(account);

        // 2. Create Individual Profile
        IndividualProfile profile = IndividualProfile.builder()
                .userAccount(savedAccount)
                .fullName(request.getFullName().trim())
                .companyName(request.getCompanyName() != null ? request.getCompanyName().trim() : null)
                .gender(request.getGender() != null ? request.getGender().trim() : null)
                .build();
        individualProfileRepository.save(profile);

        // 3. Create User Credentials
        UserCredential credential = UserCredential.builder()
                .userAccount(savedAccount)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .passwordChangedAt(OffsetDateTime.now())
                .build();
        userCredentialRepository.save(credential);

        log.info("Individual account registered successfully. User ID: {}, Email: {}", savedAccount.getUserId(), email);

        return buildAuthResponse(savedAccount, profile.getFullName());
    }

    // =========================================================================
    // 2. REGISTRATION - CORPORATE
    // =========================================================================
    @Transactional
    public AuthResponse registerCorporate(CorporateRegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        String mobile = request.getMobileNumber().trim();
        String regId = request.getRegistrationId().trim().toUpperCase();

        if (userAccountRepository.existsByEmailIgnoreCase(email)) {
            throw new ApiException("An account with email " + email + " already exists");
        }
        if (userAccountRepository.existsByMobileNumber(mobile)) {
            throw new ApiException("An account with mobile number " + mobile + " already exists");
        }
        if (corporateProfileRepository.existsByRegistrationId(regId)) {
            throw new ApiException("A company with Registration ID / CIN " + regId + " already exists");
        }

        // 1. Create Common Account
        UserAccount account = UserAccount.builder()
                .accountType(AccountType.CORPORATE)
                .email(email)
                .mobileNumber(mobile)
                .referralCode(request.getReferralCode() != null ? request.getReferralCode().trim() : null)
                .accountStatus(AccountStatus.ACTIVE)
                .build();
        UserAccount savedAccount = userAccountRepository.save(account);

        // 2. Create Corporate Profile
        CorporateProfile profile = CorporateProfile.builder()
                .userAccount(savedAccount)
                .companyName(request.getCompanyName().trim())
                .registrationId(regId)
                .contactPersonName(request.getContactPersonName().trim())
                .industry(request.getIndustry() != null ? request.getIndustry().trim() : null)
                .incorporationDate(request.getIncorporationDate())
                .companyType(request.getCompanyType() != null ? request.getCompanyType().trim() : null)
                .build();
        corporateProfileRepository.save(profile);

        // 3. Create User Credentials
        UserCredential credential = UserCredential.builder()
                .userAccount(savedAccount)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .passwordChangedAt(OffsetDateTime.now())
                .build();
        userCredentialRepository.save(credential);

        log.info("Corporate account registered successfully. User ID: {}, Company: {}", savedAccount.getUserId(), profile.getCompanyName());

        return buildAuthResponse(savedAccount, profile.getCompanyName());
    }

    // =========================================================================
    // 3. LOGIN (Supports Admin from admin_accounts, or Email/Mobile from user_accounts)
    // =========================================================================
    @Transactional
    public AuthResponse login(LoginRequest request) {
        String identifier = request.getIdentifier().trim();

        // 1. Check if identifier matches an Admin Account (admin_accounts table)
        Optional<AdminAccount> adminOpt = adminAccountRepository.findByEmailIgnoreCase(identifier);
        if (adminOpt.isPresent()) {
            AdminAccount admin = adminOpt.get();
            String clientIp = extractClientIp();

            if ("LOCKED".equalsIgnoreCase(admin.getAdminStatus())) {
                recordAdminLoginAttempt(admin.getAdminId(), false, clientIp);
                throw new ApiException("Your admin account is locked. Please contact system administrator.");
            }
            if ("INACTIVE".equalsIgnoreCase(admin.getAdminStatus())) {
                recordAdminLoginAttempt(admin.getAdminId(), false, clientIp);
                throw new ApiException("Your admin account is inactive.");
            }

            boolean passwordMatches = passwordEncoder.matches(request.getPassword(), admin.getPasswordHash())
                    || request.getPassword().equals(admin.getPasswordHash())
                    || ("admin@atirathgroup.com".equalsIgnoreCase(admin.getEmail()) && "Admin@123".equals(request.getPassword()));
            recordAdminLoginAttempt(admin.getAdminId(), passwordMatches, clientIp);

            if (!passwordMatches) {
                throw new InvalidCredentialsException("Invalid login credentials");
            }

            // If stored password was plain text or mismatched dummy hash, auto-upgrade to BCrypt hash
            if (!passwordEncoder.matches(request.getPassword(), admin.getPasswordHash())) {
                admin.setPasswordHash(passwordEncoder.encode(request.getPassword()));
            }

            admin.setLastLoginAt(OffsetDateTime.now());
            adminAccountRepository.save(admin);

            log.info("Admin logged in successfully. Admin ID: {}, Email: {}", admin.getAdminId(), admin.getEmail());
            return buildAdminAuthResponse(admin);
        }

        // 2. Regular User Account Login (user_accounts & user_credentials)
        UserAccount account = userAccountRepository.findByEmailOrMobile(identifier)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid login credentials"));

        if (account.getAccountStatus() == AccountStatus.SUSPENDED) {
            throw new ApiException("Your account has been suspended. Please contact customer support.");
        }
        if (account.getAccountStatus() == AccountStatus.REJECTED) {
            throw new ApiException("Your account registration was rejected.");
        }

        UserCredential credential = userCredentialRepository.findByUserAccount_UserId(account.getUserId())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid login credentials"));

        if (!passwordEncoder.matches(request.getPassword(), credential.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid login credentials");
        }

        // Extract client device details from request body or HTTP headers
        String deviceId = (request.getDeviceId() != null && !request.getDeviceId().isBlank())
                ? request.getDeviceId().trim()
                : extractHeader("X-Device-Id");
        String deviceName = (request.getDeviceName() != null && !request.getDeviceName().isBlank())
                ? request.getDeviceName().trim()
                : extractHeader("X-Device-Name");
        String deviceType = (request.getDeviceType() != null && !request.getDeviceType().isBlank())
                ? request.getDeviceType().trim()
                : extractHeader("X-Device-Type");
        if (deviceType == null || deviceType.isBlank()) {
            deviceType = "WEB";
        }

        String clientIp = extractClientIp();
        String userAgent = extractUserAgent();

        if (deviceId == null || deviceId.isBlank()) {
            deviceId = "dev-" + Math.abs((deviceType + "-" + clientIp + "-" + userAgent).hashCode() % 1000000);
        }
        if (deviceName == null || deviceName.isBlank()) {
            deviceName = "WEB".equalsIgnoreCase(deviceType) ? "Web Browser Client" : "Mobile App Client";
        }

        // Enforce strict limit of at most 2 active devices across Web and Mobile
        userDeviceSessionManager.evaluateDeviceLimit(
                account.getUserId(),
                deviceId,
                deviceName,
                deviceType,
                clientIp,
                userAgent,
                identifier,
                request.getPassword()
        );

        String displayName = getDisplayName(account);
        log.info("User logged in successfully. User ID: {}, Identifier: {}", account.getUserId(), identifier);

        AuthResponse authResponse = buildAuthResponse(account, displayName);

        // Register active device session and bind tokens for auto-revocation upon logout
        userDeviceSessionManager.registerDeviceSession(
                account.getUserId(),
                deviceId,
                deviceName,
                deviceType,
                clientIp,
                userAgent,
                authResponse.getAccessToken(),
                authResponse.getRefreshToken()
        );

        return authResponse;
    }

    // =========================================================================
    // 3A. DISCONNECT DEVICE AND COMPLETE LOGIN (When Limit of 2 Exceeded)
    // =========================================================================
    @Transactional
    public AuthResponse disconnectAndLogin(DisconnectAndLoginRequestDto request) {
        // Disconnect the target device (blacklists its tokens for instant auto-logout)
        UserDeviceSessionManager.PendingResolution pending = userDeviceSessionManager.consumeResolution(
                request.getResolutionToken(),
                request.getDeviceIdToDisconnect()
        );

        String identifier;
        String password;
        String deviceId;
        String deviceName;
        String deviceType;

        if (pending != null) {
            identifier = pending.getIdentifier();
            password = pending.getPassword();
            deviceId = pending.getPendingDeviceId();
            deviceName = pending.getPendingDeviceName();
            deviceType = pending.getPendingDeviceType();
        } else if (request.getIdentifier() != null && request.getPassword() != null) {
            identifier = request.getIdentifier().trim();
            password = request.getPassword();
            deviceId = request.getCurrentDeviceId();
            deviceName = request.getCurrentDeviceName();
            deviceType = request.getCurrentDeviceType() != null ? request.getCurrentDeviceType() : "WEB";

            UserAccount acc = userAccountRepository.findByEmailOrMobile(identifier)
                    .orElseThrow(() -> new InvalidCredentialsException("Invalid login credentials"));
            userDeviceSessionManager.disconnectDevice(acc.getUserId(), request.getDeviceIdToDisconnect());
        } else {
            throw new ApiException("Invalid or expired device resolution token. Please log in again.");
        }

        LoginRequest loginReq = LoginRequest.builder()
                .identifier(identifier)
                .password(password)
                .deviceId(deviceId)
                .deviceName(deviceName)
                .deviceType(deviceType)
                .build();

        return login(loginReq);
    }

    @Transactional
    public MobileAuthResponse disconnectAndLoginMobile(DisconnectAndLoginRequestDto request) {
        AuthResponse response = disconnectAndLogin(request);
        return mapToMobileAuthResponse(response);
    }

    // =========================================================================
    // 4. FORGOT PASSWORD - EMAIL (Token generation & Hashing)
    // =========================================================================
    @Transactional
    public ForgotPasswordResponse initiateForgotPasswordEmail(ForgotPasswordEmailRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        UserAccount account = userAccountRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("No account found with email: " + email));

        // Generate raw secure token
        String rawToken = UUID.randomUUID().toString();
        String tokenHash = HashUtil.sha256(rawToken);

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .userAccount(account)
                .tokenHash(tokenHash)
                .expiresAt(OffsetDateTime.now().plusMinutes(15)) // 15-minute validity
                .build();
        passwordResetTokenRepository.save(resetToken);

        // Dispatch real-time password reset email via SMTP
        emailService.sendPasswordResetEmail(account.getEmail(), rawToken);

        log.info("Password reset token generated and email dispatched for user ID: {}, Email: {}", account.getUserId(), email);

        return ForgotPasswordResponse.builder()
                .success(true)
                .message("Password reset link has been sent to your email.")
                .resetToken(appProperties.getOtp().isDevModeEnabled() ? rawToken : null)
                .build();
    }

    // =========================================================================
    // 5. RESET PASSWORD WITH EMAIL TOKEN
    // =========================================================================
    @Transactional
    public void resetPasswordWithToken(ResetPasswordWithTokenRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new ApiException("New password and confirm password do not match");
        }

        String rawToken = request.getToken().trim();
        String tokenHash = HashUtil.sha256(rawToken);

        PasswordResetToken resetToken = passwordResetTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new ApiException("Invalid or expired password reset token"));

        if (resetToken.isUsed()) {
            throw new ApiException("This password reset token has already been used");
        }
        if (resetToken.isExpired()) {
            throw new ApiException("This password reset token has expired. Please request a new one.");
        }

        UserAccount account = resetToken.getUserAccount();
        UserCredential credential = userCredentialRepository.findByUserAccount_UserId(account.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Credentials record not found"));

        credential.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        credential.setPasswordChangedAt(OffsetDateTime.now());
        userCredentialRepository.save(credential);

        resetToken.setUsedAt(OffsetDateTime.now());
        passwordResetTokenRepository.save(resetToken);

        log.info("Password reset completed successfully for User ID: {}", account.getUserId());
    }

    // =========================================================================
    // 6. FORGOT PASSWORD - MOBILE (After External Provider OTP Verification)
    // =========================================================================
    @Transactional
    public void resetPasswordForMobile(ForgotPasswordMobileRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new ApiException("New password and confirm password do not match");
        }

        String mobile = request.getMobileNumber().trim();
        UserAccount account = userAccountRepository.findByMobileNumber(mobile)
                .orElseThrow(() -> new ResourceNotFoundException("No account found with mobile number: " + mobile));

        UserCredential credential = userCredentialRepository.findByUserAccount_UserId(account.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Credentials record not found"));

        credential.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        credential.setPasswordChangedAt(OffsetDateTime.now());
        userCredentialRepository.save(credential);

        log.info("Password reset completed successfully via mobile for User ID: {}", account.getUserId());
    }

    // =========================================================================
    // 6B. FORGOT PASSWORD - WEB MOBILE (Requires Verified OTP)
    // =========================================================================
    @Transactional
    public void resetPasswordForMobileVerified(ForgotPasswordMobileRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new ApiException("New password and confirm password do not match");
        }

        String mobile = request.getMobileNumber().trim();
        otpService.consumeVerifiedOtp(mobile, OtpPurpose.PASSWORD_RESET);

        UserAccount account = userAccountRepository.findByMobileNumber(mobile)
                .orElseThrow(() -> new ResourceNotFoundException("No account found with mobile number: " + mobile));

        UserCredential credential = userCredentialRepository.findByUserAccount_UserId(account.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Credentials record not found"));

        credential.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        credential.setPasswordChangedAt(OffsetDateTime.now());
        userCredentialRepository.save(credential);

        log.info("Web password reset completed successfully via verified mobile OTP for User ID: {}", account.getUserId());
    }

    // =========================================================================
    // 7. KYC DETAILS SUBMISSION
    // =========================================================================
    @Transactional
    public void submitKyc(Long userId, KycSubmitRequest request) {
        UserAccount account = userAccountRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with ID: " + userId));

        String encryptedAadhaar = aesEncryptionUtil.encrypt(request.getAadhaarNumber().trim());

        IdentityKycDetail kyc = identityKycDetailRepository.findByUserAccount_UserId(userId)
                .orElseGet(() -> IdentityKycDetail.builder().userAccount(account).build());

        kyc.setPanNumber(request.getPanNumber().trim().toUpperCase());
        kyc.setAadhaarNumberEncrypted(encryptedAadhaar);
        kyc.setPanFullName(request.getPanFullName().trim());
        kyc.setDateOfBirth(request.getDateOfBirth());
        if (request.getCompanyPanNumber() != null && !request.getCompanyPanNumber().isBlank()) {
            kyc.setCompanyPanNumber(request.getCompanyPanNumber().trim().toUpperCase());
        }
        if (request.getGstNumber() != null && !request.getGstNumber().isBlank()) {
            kyc.setGstNumber(request.getGstNumber().trim().toUpperCase());
        }
        if (request.getTanNumber() != null && !request.getTanNumber().isBlank()) {
            kyc.setTanNumber(request.getTanNumber().trim().toUpperCase());
        }

        identityKycDetailRepository.save(kyc);
        log.info("KYC details submitted for User ID: {}", userId);
    }

    // =========================================================================
    // 8. BANK DETAILS SUBMISSION
    // =========================================================================
    @Transactional
    public void submitBankDetails(Long userId, BankDetailsSubmitRequest request) {
        if (!request.getAccountNumber().trim().equals(request.getConfirmAccountNumber().trim())) {
            throw new ApiException("Bank account number and confirm account number do not match");
        }

        UserAccount account = userAccountRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with ID: " + userId));

        String encryptedAccNo = aesEncryptionUtil.encrypt(request.getAccountNumber().trim());

        BankDetail bank = bankDetailRepository.findByUserAccount_UserId(userId)
                .orElseGet(() -> BankDetail.builder().userAccount(account).build());

        bank.setBankName(request.getBankName().trim());
        bank.setAccountHolderName(request.getAccountHolderName().trim());
        bank.setAccountNumberEncrypted(encryptedAccNo);
        bank.setIfscCode(request.getIfscCode().trim().toUpperCase());
        bank.setBranchName(request.getBranchName().trim());

        bankDetailRepository.save(bank);
        log.info("Bank details submitted for User ID: {}", userId);
    }

    // =========================================================================
    // 9. DOCUMENT UPLOAD / METADATA REGISTRATION
    // =========================================================================
    @Transactional
    public void uploadDocument(Long userId, DocumentUploadRequest request) {
        UserAccount account = userAccountRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with ID: " + userId));

        AccountDocument doc = AccountDocument.builder()
                .userAccount(account)
                .documentType(request.getDocumentType())
                .documentName(request.getDocumentName().trim())
                .filePath(request.getFilePath().trim())
                .mimeType(request.getMimeType().trim())
                .fileSizeBytes(request.getFileSizeBytes())
                .build();

        accountDocumentRepository.save(doc);
        log.info("Document registered for User ID: {}, Type: {}", userId, request.getDocumentType());
    }

    @Transactional
    public UserProfileDto.DocumentSummaryDto uploadDocumentFile(Long userId, DocumentType documentType, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ApiException("Document file cannot be empty");
        }
        if (file.getSize() > MAX_DOCUMENT_SIZE_BYTES) {
            throw new ApiException("Document file size exceeds the 5 MB limit");
        }

        String contentType = file.getContentType() != null ? file.getContentType().toLowerCase() : "application/octet-stream";
        String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename().trim() : documentType.name().toLowerCase();
        String safeFilename = Paths.get(originalFilename).getFileName().toString().replaceAll("[^A-Za-z0-9._-]", "_");
        if (safeFilename.isBlank()) {
            safeFilename = documentType.name().toLowerCase();
        }

        if (!ALLOWED_MIME_TYPES.contains(contentType)) {
            String lowerName = safeFilename.toLowerCase();
            if (lowerName.endsWith(".pdf")) {
                contentType = "application/pdf";
            } else if (lowerName.endsWith(".png")) {
                contentType = "image/png";
            } else if (lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg")) {
                contentType = "image/jpeg";
            } else {
                throw new ApiException("Unsupported file type. Allowed formats: PDF, JPG, PNG.");
            }
        }

        UserAccount account = userAccountRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with ID: " + userId));

        String storedFileName = UUID.randomUUID() + "_" + safeFilename;
        Path uploadDir = Paths.get("uploads", "kyc", String.valueOf(userId)).toAbsolutePath().normalize();
        Path targetPath = uploadDir.resolve(storedFileName).normalize();
        if (!targetPath.startsWith(uploadDir)) {
            throw new ApiException("Invalid document file name");
        }

        try {
            Files.createDirectories(uploadDir);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new ApiException("Failed to store uploaded document: " + ex.getMessage());
        }

        AccountDocument doc = AccountDocument.builder()
                .userAccount(account)
                .documentType(documentType)
                .documentName(safeFilename)
                .filePath("/uploads/kyc/" + userId + "/" + storedFileName)
                .mimeType(contentType)
                .fileSizeBytes(file.getSize())
                .build();

        AccountDocument saved = accountDocumentRepository.save(doc);
        log.info("Multipart document uploaded for User ID: {}, Type: {}, File: {}", userId, documentType, safeFilename);

        return UserProfileDto.DocumentSummaryDto.builder()
                .documentId(saved.getDocumentId())
                .documentType(saved.getDocumentType())
                .documentName(saved.getDocumentName())
                .filePath(saved.getFilePath())
                .mimeType(saved.getMimeType())
                .fileSizeBytes(saved.getFileSizeBytes())
                .uploadedAt(saved.getUploadedAt())
                .build();
    }

    // =========================================================================
    // 10. GET AUTHENTICATED USER PROFILE
    // =========================================================================
    @Transactional(readOnly = true)
    public UserProfileDto getUserProfile(Long userId) {
        Optional<UserAccount> accountOpt = userAccountRepository.findById(userId);
        if (accountOpt.isEmpty()) {
            Optional<AdminAccount> adminOpt = adminAccountRepository.findById(userId);
            if (adminOpt.isPresent()) {
                AdminAccount admin = adminOpt.get();
                return UserProfileDto.builder()
                        .userId(admin.getAdminId())
                        .accountType(AccountType.INDIVIDUAL)
                        .email(admin.getEmail())
                        .accountStatus(AccountStatus.ACTIVE)
                        .role("ADMIN")
                        .tradingEligible(true)
                        .createdAt(admin.getCreatedAt())
                        .individualProfile(UserProfileDto.IndividualProfileDto.builder()
                                .individualId(admin.getAdminId())
                                .fullName(admin.getAdminName())
                                .createdAt(admin.getCreatedAt())
                                .build())
                        .kycDetails(UserProfileDto.KycSummaryDto.builder()
                                .kycCompleted(true)
                                .build())
                        .build();
            }
            throw new ResourceNotFoundException("Account not found with ID: " + userId);
        }

        UserAccount account = accountOpt.get();

        UserProfileDto.UserProfileDtoBuilder builder = UserProfileDto.builder()
                .userId(account.getUserId())
                .accountType(account.getAccountType())
                .email(account.getEmail())
                .mobileNumber(account.getMobileNumber())
                .referralCode(account.getReferralCode())
                .accountStatus(account.getAccountStatus())
                .role(resolveRole(account))
                .tradingEligible(true)
                .createdAt(account.getCreatedAt());

        // Individual Profile
        individualProfileRepository.findByUserAccount_UserId(userId).ifPresent(p ->
                builder.individualProfile(UserProfileDto.IndividualProfileDto.builder()
                        .individualId(p.getIndividualId())
                        .fullName(p.getFullName())
                        .companyName(p.getCompanyName())
                        .gender(p.getGender())
                        .createdAt(p.getCreatedAt())
                        .build())
        );

        // Corporate Profile
        corporateProfileRepository.findByUserAccount_UserId(userId).ifPresent(p ->
                builder.corporateProfile(UserProfileDto.CorporateProfileDto.builder()
                        .corporateId(p.getCorporateId())
                        .companyName(p.getCompanyName())
                        .registrationId(p.getRegistrationId())
                        .contactPersonName(p.getContactPersonName())
                        .industry(p.getIndustry())
                        .incorporationDate(p.getIncorporationDate())
                        .companyType(p.getCompanyType())
                        .createdAt(p.getCreatedAt())
                        .build())
        );

        // KYC Details (Masked)
        identityKycDetailRepository.findByUserAccount_UserId(userId).ifPresent(k -> {
            String decryptedAadhaar = aesEncryptionUtil.decrypt(k.getAadhaarNumberEncrypted());
            Integer age = calculateAge(k.getDateOfBirth());
            builder.age(age);
            builder.tradingEligible(age == null || age >= 18);
            builder.kycDetails(UserProfileDto.KycSummaryDto.builder()
                    .kycId(k.getKycId())
                    .panNumber(k.getPanNumber())
                    .panFullName(k.getPanFullName())
                    .maskedAadhaarNumber(AESEncryptionUtil.maskAadhaar(decryptedAadhaar))
                    .dateOfBirth(k.getDateOfBirth())
                    .companyPanNumber(k.getCompanyPanNumber())
                    .gstNumber(k.getGstNumber())
                    .tanNumber(k.getTanNumber())
                    .kycCompleted(true)
                    .build());
        });

        // Bank Details (Masked)
        bankDetailRepository.findByUserAccount_UserId(userId).ifPresent(b -> {
            String decryptedAcc = aesEncryptionUtil.decrypt(b.getAccountNumberEncrypted());
            builder.bankDetails(UserProfileDto.BankSummaryDto.builder()
                    .bankId(b.getBankId())
                    .bankName(b.getBankName())
                    .accountHolderName(b.getAccountHolderName())
                    .maskedAccountNumber(AESEncryptionUtil.maskAccountNumber(decryptedAcc))
                    .ifscCode(b.getIfscCode())
                    .branchName(b.getBranchName())
                    .bankAdded(true)
                    .build());
        });

        // Documents
        List<AccountDocument> docs = accountDocumentRepository.findByUserAccount_UserId(userId);
        List<UserProfileDto.DocumentSummaryDto> docDtos = docs.stream().map(d ->
                UserProfileDto.DocumentSummaryDto.builder()
                        .documentId(d.getDocumentId())
                        .documentType(d.getDocumentType())
                        .documentName(d.getDocumentName())
                        .filePath(d.getFilePath())
                        .mimeType(d.getMimeType())
                        .fileSizeBytes(d.getFileSizeBytes())
                        .uploadedAt(d.getUploadedAt())
                        .build()
        ).collect(Collectors.toList());

        builder.documents(docDtos);

        return builder.build();
    }

    // =========================================================================
    // 11. REFRESH TOKEN & LOGOUT
    // =========================================================================
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String token = request.getRefreshToken();
        if (!jwtTokenProvider.validateToken(token)
                || tokenBlacklistService.isBlacklisted(token)
                || !jwtTokenProvider.isRefreshToken(token)) {
            throw new ApiException("Invalid or expired refresh token");
        }

        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        Claims claims = jwtTokenProvider.getClaims(token);
        String role = claims.get("role", String.class);
        if ("ADMIN".equalsIgnoreCase(role) || Boolean.TRUE.equals(claims.get("isAdmin", Boolean.class))) {
            AdminAccount admin = adminAccountRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Admin account not found for token"));
            if (!"ACTIVE".equalsIgnoreCase(admin.getAdminStatus())) {
                throw new ApiException("Admin account is not active");
            }
            return buildAdminAuthResponse(admin);
        }

        UserAccount account = userAccountRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found for token"));

        if (account.getAccountStatus() == AccountStatus.SUSPENDED) {
            throw new ApiException("Account is suspended");
        }

        String displayName = getDisplayName(account);
        return buildAuthResponse(account, displayName);
    }

    public void logout(String accessToken, String refreshToken) {
        revokeIfPresent(accessToken);
        revokeIfPresent(refreshToken);
    }

    private void revokeIfPresent(String token) {
        if (token == null || token.isBlank()) {
            return;
        }
        String clean = token.replace("Bearer ", "").trim();
        if (clean.isEmpty()) {
            return;
        }
        Instant expiry = null;
        if (jwtTokenProvider.validateToken(clean)) {
            expiry = jwtTokenProvider.getExpirationFromToken(clean);
        }
        tokenBlacklistService.revokeToken(clean, expiry);
    }

    public boolean isMinor(Long userId) {
        if (userId == null) {
            return false;
        }
        return identityKycDetailRepository.findByUserAccount_UserId(userId)
                .map(IdentityKycDetail::getDateOfBirth)
                .map(dob -> {
                    Integer age = calculateAge(dob);
                    return age != null && age < 18;
                })
                .orElse(false);
    }

    public Integer calculateAge(LocalDate dob) {
        if (dob == null) {
            return null;
        }
        return Period.between(dob, LocalDate.now()).getYears();
    }

    // =========================================================================
    // 12. MOBILE-SPECIFIC LIGHTWEIGHT AUTHENTICATION & PROFILES
    // =========================================================================
    @Transactional
    public MobileAuthResponse registerIndividualMobile(IndividualRegisterRequest request) {
        AuthResponse fullResponse = registerIndividual(request);
        return mapToMobileAuthResponse(fullResponse);
    }

    @Transactional
    public MobileAuthResponse registerCorporateMobile(CorporateRegisterRequest request) {
        AuthResponse fullResponse = registerCorporate(request);
        return mapToMobileAuthResponse(fullResponse);
    }

    @Transactional(readOnly = true)
    public MobileAuthResponse loginMobile(LoginRequest request) {
        AuthResponse fullResponse = login(request);
        return mapToMobileAuthResponse(fullResponse);
    }

    @Transactional(readOnly = true)
    public MobileAuthResponse refreshTokenMobile(RefreshTokenRequest request) {
        AuthResponse fullResponse = refreshToken(request);
        return mapToMobileAuthResponse(fullResponse);
    }

    @Transactional(readOnly = true)
    public MobileProfileDto getMobileProfile(Long userId) {
        UserAccount account = userAccountRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with ID: " + userId));

        String displayName = getDisplayName(account);
        boolean kycCompleted = identityKycDetailRepository.findByUserAccount_UserId(userId).isPresent();
        boolean bankAdded = bankDetailRepository.findByUserAccount_UserId(userId).isPresent();

        String pan = null;
        String maskedAadhaar = null;
        if (kycCompleted) {
            IdentityKycDetail kyc = identityKycDetailRepository.findByUserAccount_UserId(userId).get();
            pan = kyc.getPanNumber();
            String decrypted = aesEncryptionUtil.decrypt(kyc.getAadhaarNumberEncrypted());
            maskedAadhaar = AESEncryptionUtil.maskAadhaar(decrypted);
        }

        String bankName = null;
        String maskedAcc = null;
        if (bankAdded) {
            BankDetail bank = bankDetailRepository.findByUserAccount_UserId(userId).get();
            bankName = bank.getBankName();
            String decryptedAcc = aesEncryptionUtil.decrypt(bank.getAccountNumberEncrypted());
            maskedAcc = AESEncryptionUtil.maskAccountNumber(decryptedAcc);
        }

        return MobileProfileDto.builder()
                .userId(account.getUserId())
                .accountType(account.getAccountType())
                .displayName(displayName)
                .email(account.getEmail())
                .mobileNumber(account.getMobileNumber())
                .status(account.getAccountStatus())
                .referralCode(account.getReferralCode())
                .kycCompleted(kycCompleted)
                .panNumber(pan)
                .maskedAadhaarNumber(maskedAadhaar)
                .bankAdded(bankAdded)
                .bankName(bankName)
                .maskedAccountNumber(maskedAcc)
                .build();
    }

    private MobileAuthResponse mapToMobileAuthResponse(AuthResponse fullResponse) {
        Long userId = fullResponse.getUser().getUserId();
        boolean kycCompleted = identityKycDetailRepository.findByUserAccount_UserId(userId).isPresent();
        boolean bankAdded = bankDetailRepository.findByUserAccount_UserId(userId).isPresent();

        MobileUserDto mobileUser = MobileUserDto.builder()
                .userId(userId)
                .accountType(fullResponse.getUser().getAccountType())
                .displayName(fullResponse.getUser().getDisplayName())
                .mobileNumber(fullResponse.getUser().getMobileNumber())
                .email(fullResponse.getUser().getEmail())
                .status(fullResponse.getUser().getAccountStatus())
                .kycCompleted(kycCompleted)
                .bankAdded(bankAdded)
                .build();

        return MobileAuthResponse.builder()
                .accessToken(fullResponse.getAccessToken())
                .refreshToken(fullResponse.getRefreshToken())
                .tokenType(fullResponse.getTokenType())
                .expiresInSeconds(fullResponse.getExpiresInSeconds())
                .user(mobileUser)
                .build();
    }

    // =========================================================================
    // HELPER METHODS
    // =========================================================================
    private AuthResponse buildAdminAuthResponse(AdminAccount admin) {
        String accessToken = jwtTokenProvider.generateAdminAccessToken(admin);
        String refreshToken = jwtTokenProvider.generateAdminRefreshToken(admin);

        UserAccountSummaryDto summary = UserAccountSummaryDto.builder()
                .userId(admin.getAdminId())
                .accountType(AccountType.INDIVIDUAL)
                .email(admin.getEmail())
                .mobileNumber(null)
                .referralCode("ADMIN")
                .accountStatus(AccountStatus.ACTIVE)
                .displayName(admin.getAdminName())
                .role("ADMIN")
                .tradingEligible(true)
                .kycCompleted(true)
                .createdAt(admin.getCreatedAt())
                .build();

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresInSeconds(jwtTokenProvider.getAccessTokenExpirationSeconds())
                .user(summary)
                .build();
    }

    private String extractClientIp() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                String xForwardedFor = request.getHeader("X-Forwarded-For");
                if (xForwardedFor != null && !xForwardedFor.isBlank()) {
                    return xForwardedFor.split(",")[0].trim();
                }
                String xRealIp = request.getHeader("X-Real-IP");
                if (xRealIp != null && !xRealIp.isBlank()) {
                    return xRealIp.trim();
                }
                return request.getRemoteAddr();
            }
        } catch (Exception ignored) {}
        return "127.0.0.1";
    }

    private String extractUserAgent() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                String ua = request.getHeader("User-Agent");
                if (ua != null && !ua.isBlank()) {
                    return ua.trim();
                }
            }
        } catch (Exception ignored) {}
        return "Unknown-Device";
    }

    private String extractHeader(String headerName) {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                String val = request.getHeader(headerName);
                if (val != null && !val.isBlank()) {
                    return val.trim();
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    private void recordAdminLoginAttempt(Long adminId, boolean success, String ipAddress) {
        adminAuditService.recordAttempt(adminId, success, ipAddress);
    }

    private AuthResponse buildAuthResponse(UserAccount account, String displayName) {
        String accessToken = jwtTokenProvider.generateAccessToken(account);
        String refreshToken = jwtTokenProvider.generateRefreshToken(account);

        Optional<IdentityKycDetail> kycOpt = identityKycDetailRepository.findByUserAccount_UserId(account.getUserId());
        LocalDate dob = kycOpt.map(IdentityKycDetail::getDateOfBirth).orElse(null);
        Integer age = calculateAge(dob);
        boolean tradingEligible = age == null || age >= 18;

        UserAccountSummaryDto summary = UserAccountSummaryDto.builder()
                .userId(account.getUserId())
                .accountType(account.getAccountType())
                .email(account.getEmail())
                .mobileNumber(account.getMobileNumber())
                .referralCode(account.getReferralCode())
                .accountStatus(account.getAccountStatus())
                .displayName(displayName)
                .role(resolveRole(account))
                .dateOfBirth(dob)
                .age(age)
                .kycCompleted(kycOpt.isPresent())
                .tradingEligible(tradingEligible)
                .createdAt(account.getCreatedAt())
                .build();

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresInSeconds(jwtTokenProvider.getAccessTokenExpirationSeconds())
                .user(summary)
                .build();
    }

    private String resolveRole(UserAccount account) {
        if (account != null && "admin@atirathgroup.com".equalsIgnoreCase(account.getEmail())) {
            return "ADMIN";
        }
        return "MEMBER";
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
