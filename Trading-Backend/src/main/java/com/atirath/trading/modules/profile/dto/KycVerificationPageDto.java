package com.atirath.trading.modules.profile.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "KYC & Verification page payload matching Image 2")
public class KycVerificationPageDto {

    // --- Header ---
    @Schema(description = "Member ID", example = "ATG10027")
    private String memberId;

    @Schema(description = "Full Name", example = "Rohit Sharma")
    private String fullName;

    @Schema(description = "Member Status Badge", example = "Verified Member")
    private String memberBadge;

    @Schema(description = "Email", example = "rohit.sharma@example.com")
    private String email;

    @Schema(description = "Mobile Number", example = "+91 98765 43210")
    private String mobileNumber;

    @Schema(description = "Company Name", example = "GreenTech Solutions Pvt Ltd")
    private String companyName;

    @Schema(description = "Designation", example = "Director")
    private String designation;

    @Schema(description = "Industry", example = "Renewable Energy")
    private String industry;

    @Schema(description = "Member Since", example = "12 Jan 2021")
    private String memberSince;

    @Schema(description = "Avatar display initials", example = "RS")
    private String avatarInitials;

    // --- Stepper ---
    @Schema(description = "Current active step (1-5)", example = "4")
    private Integer activeStep;

    @Schema(description = "Stepper steps list")
    @Builder.Default
    private List<String> stepperSteps = new ArrayList<>();

    // --- KYC Completion Metrics ---
    @Schema(description = "Overall KYC completion percentage", example = "80")
    private Integer completionPercentage;

    @Schema(description = "Verified documents count", example = "4")
    private Integer verifiedCount;

    @Schema(description = "Under Review documents count", example = "1")
    private Integer underReviewCount;

    @Schema(description = "Not Verified documents count", example = "1")
    private Integer notVerifiedCount;

    @Schema(description = "Not Required documents count", example = "0")
    private Integer notRequiredCount;

    // --- Status Alert Box ---
    @Schema(description = "Verification alert status title", example = "Identity, Address and Organization verification completed.")
    private String statusAlertTitle;

    @Schema(description = "Verification alert status description", example = "Complete the remaining verification to access all platform features.")
    private String statusAlertMessage;

    // --- Important Notes ---
    @Schema(description = "List of important KYC compliance guidelines")
    @Builder.Default
    private List<String> importantNotes = new ArrayList<>();

    // --- Documents List ---
    @Schema(description = "List of all KYC documents")
    @Builder.Default
    private List<KycDocumentItemDto> documents = new ArrayList<>();

    // --- Verification History ---
    @Schema(description = "Chronological audit trail of verification actions")
    @Builder.Default
    private List<VerificationHistoryLogDto> verificationHistory = new ArrayList<>();
}
