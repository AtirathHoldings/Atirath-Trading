package com.atirath.trading.modules.profile.dto.mobile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Mobile-optimized DTO for KYC & Shareholder Verification screen (kyc_screen.dart).
 * Backed 100% by database records (identity_kyc_details, bank_details, user_accounts).
 * No hardcoded fallback data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MobileKycVerificationDto implements Serializable {

    private Long userId;
    private String memberId;
    private String fullName;
    private String accountType;

    // Header Badge Status
    private String kycStatus;           // VERIFIED, UNDER_REVIEW, NOT_VERIFIED
    private String statusBadgeTitle;    // e.g. "KYC Tier-1 Fully Verified", "KYC Pending Verification", "KYC Incomplete"
    private String statusBadgeSubtitle; // e.g. "Eligible to buy, sell, and transfer shares in all Atirath Group entities."
    private String statusColor;         // "green", "yellow", "orange", "red"
    private boolean eligibleToTrade;
    private int completionPercentage;

    // Step 1: PAN Card Verification
    private String panNumberMasked;     // e.g. "ABCDE****F" or "Not Provided"
    private String nameOnPan;           // Name from PAN/KYC or null
    private String panStatus;           // "Verified via NSDL Database", "Submitted", "Not Provided"
    private boolean panVerified;

    // Step 2: Aadhaar / e-KYC Verification
    private String aadhaarNumberMasked; // e.g. "XXXX-XXXX-8921" or "Not Provided"
    private String aadhaarAuthMethod;   // "OTP based Biometric e-KYC", "Manual Upload", "Not Provided"
    private String verifiedAddress;     // Address from DB or "Not Provided"
    private boolean aadhaarVerified;

    // Step 3: Bank & Demat Verification
    private String bankName;            // Primary bank name or "Not Linked"
    private String accountNumberMasked; // "**** **** 4590" or "Not Linked"
    private String ifscCode;            // Bank IFSC code or "Not Linked"
    private String internalDpId;        // Demat / Depository Participant ID
    private boolean bankVerified;
}
