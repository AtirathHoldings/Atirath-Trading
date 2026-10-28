package com.atirath.trading.modules.auth.service;

import com.atirath.trading.common.AccountStatus;
import com.atirath.trading.common.AccountType;
import com.atirath.trading.common.ApiException;
import com.atirath.trading.common.InvalidCredentialsException;
import com.atirath.trading.common.ResourceNotFoundException;
import com.atirath.trading.common.util.AESEncryptionUtil;
import com.atirath.trading.common.util.HashUtil;
import com.atirath.trading.config.JwtTokenProvider;
import com.atirath.trading.modules.auth.dto.*;
import com.atirath.trading.modules.auth.dto.common.AuthResponse;
import com.atirath.trading.modules.auth.dto.common.RefreshTokenRequest;
import com.atirath.trading.modules.auth.dto.mobile.*;
import com.atirath.trading.modules.auth.model.*;
import com.atirath.trading.modules.auth.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
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
    // 3. LOGIN (Supports Email or Mobile + Password)
    // =========================================================================
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String identifier = request.getIdentifier().trim();

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

        String displayName = getDisplayName(account);
        log.info("User logged in successfully. User ID: {}, Identifier: {}", account.getUserId(), identifier);

        return buildAuthResponse(account, displayName);
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

        log.info("Password reset token generated for user ID: {}, Email: {}", account.getUserId(), email);

        return ForgotPasswordResponse.builder()
                .success(true)
                .message("Password reset token generated. Use this token to reset your password.")
                .resetToken(rawToken) // Returned for convenient dev/client testing
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

    // =========================================================================
    // 10. GET AUTHENTICATED USER PROFILE
    // =========================================================================
    @Transactional(readOnly = true)
    public UserProfileDto getUserProfile(Long userId) {
        UserAccount account = userAccountRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with ID: " + userId));

        UserProfileDto.UserProfileDtoBuilder builder = UserProfileDto.builder()
                .userId(account.getUserId())
                .accountType(account.getAccountType())
                .email(account.getEmail())
                .mobileNumber(account.getMobileNumber())
                .referralCode(account.getReferralCode())
                .accountStatus(account.getAccountStatus())
                .createdAt(account.getCreatedAt());

        // Individual Profile
        individualProfileRepository.findByUserAccount_UserId(userId).ifPresent(p ->
                builder.individualProfile(UserProfileDto.IndividualProfileDto.builder()
                        .individualId(p.getIndividualId())
                        .fullName(p.getFullName())
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
                        .createdAt(p.getCreatedAt())
                        .build())
        );

        // KYC Details (Masked)
        identityKycDetailRepository.findByUserAccount_UserId(userId).ifPresent(k -> {
            String decryptedAadhaar = aesEncryptionUtil.decrypt(k.getAadhaarNumberEncrypted());
            builder.kycDetails(UserProfileDto.KycSummaryDto.builder()
                    .kycId(k.getKycId())
                    .panNumber(k.getPanNumber())
                    .panFullName(k.getPanFullName())
                    .maskedAadhaarNumber(AESEncryptionUtil.maskAadhaar(decryptedAadhaar))
                    .dateOfBirth(k.getDateOfBirth())
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
    // 11. REFRESH TOKEN
    // =========================================================================
    @Transactional(readOnly = true)
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String token = request.getRefreshToken();
        if (!jwtTokenProvider.validateToken(token)) {
            throw new ApiException("Invalid or expired refresh token");
        }

        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        UserAccount account = userAccountRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found for token"));

        if (account.getAccountStatus() == AccountStatus.SUSPENDED) {
            throw new ApiException("Account is suspended");
        }

        String displayName = getDisplayName(account);
        return buildAuthResponse(account, displayName);
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
    private AuthResponse buildAuthResponse(UserAccount account, String displayName) {
        String accessToken = jwtTokenProvider.generateAccessToken(account);
        String refreshToken = jwtTokenProvider.generateRefreshToken(account);

        UserAccountSummaryDto summary = UserAccountSummaryDto.builder()
                .userId(account.getUserId())
                .accountType(account.getAccountType())
                .email(account.getEmail())
                .mobileNumber(account.getMobileNumber())
                .referralCode(account.getReferralCode())
                .accountStatus(account.getAccountStatus())
                .displayName(displayName)
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
