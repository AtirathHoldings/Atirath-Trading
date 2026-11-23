package com.atirath.trading.modules.auth.dto;

import com.atirath.trading.common.AccountStatus;
import com.atirath.trading.common.AccountType;
import com.atirath.trading.common.DocumentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDto {

    private Long userId;
    private AccountType accountType;
    private String email;
    private String mobileNumber;
    private String referralCode;
    private AccountStatus accountStatus;
    private String role;
    private Boolean tradingEligible;
    private Integer age;
    private OffsetDateTime createdAt;

    // Individual Profile (if INDIVIDUAL)
    private IndividualProfileDto individualProfile;

    // Corporate Profile (if CORPORATE)
    private CorporateProfileDto corporateProfile;

    // Identity / KYC (Masked)
    private KycSummaryDto kycDetails;

    // Bank Details (Masked)
    private BankSummaryDto bankDetails;

    // Documents
    private List<DocumentSummaryDto> documents;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IndividualProfileDto {
        private Long individualId;
        private String fullName;
        private String companyName;
        private String gender;
        private OffsetDateTime createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CorporateProfileDto {
        private Long corporateId;
        private String companyName;
        private String registrationId;
        private String contactPersonName;
        private String industry;
        private LocalDate incorporationDate;
        private String companyType;
        private OffsetDateTime createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KycSummaryDto {
        private Long kycId;
        private String panNumber;
        private String panFullName;
        private String maskedAadhaarNumber;
        private LocalDate dateOfBirth;
        private String companyPanNumber;
        private String gstNumber;
        private String tanNumber;
        private boolean kycCompleted;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BankSummaryDto {
        private Long bankId;
        private String bankName;
        private String accountHolderName;
        private String maskedAccountNumber;
        private String ifscCode;
        private String branchName;
        private boolean bankAdded;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DocumentSummaryDto {
        private Long documentId;
        private DocumentType documentType;
        private String documentName;
        private String filePath;
        private String mimeType;
        private Long fileSizeBytes;
        private OffsetDateTime uploadedAt;
    }
}
