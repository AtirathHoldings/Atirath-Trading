package com.atirath.trading.modules.profile.dto.mobile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Compact, lightweight DTO delivering only the essential user profile data
 * required by mobile apps (profile_screen.dart).
 * Strictly sourced from database entities: user_accounts, individual_profiles,
 * corporate_profiles, identity_kyc_details, bank_details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MobileProfileScreenDto implements Serializable {

    private Long userId;
    private String memberId;
    private String fullName;
    private String email;
    private String mobileNumber;
    private String accountType; // INDIVIDUAL / CORPORATE
    private String status;      // ACTIVE, PENDING, etc.
    private String memberSince;
    private String avatarInitials;

    // KYC & Trading Status
    private boolean kycCompleted;
    private String kycStatus;     // VERIFIED, UNDER_REVIEW, NOT_PROVIDED
    private String kycBadgeText;  // e.g. "KYC Verified" or "Action Needed"
    private String kycBadgeColor; // green, yellow, red
    private boolean tradingEligible;

    // Identity summary
    private String panNumber;           // Masked e.g. "ABCDE****F" or null / "Not Provided"
    private String maskedAadhaarNumber; // e.g. "XXXX-XXXX-8921" or null / "Not Provided"
    private String dateOfBirth;

    // Primary Bank Summary
    private boolean bankAdded;
    private String bankName;            // e.g. "State Bank of India" or null
    private String maskedAccountNumber; // e.g. "**** **** 4590" or null
    private String ifscCode;            // e.g. "SBIN0001234" or null
    private String primaryBankDisplay;  // e.g. "SBI (****4590) · Primary" or "No Bank Linked"

    // Company (for Corporate accounts)
    private String companyName;
    private String designation;
}
