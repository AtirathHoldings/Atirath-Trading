package com.atirath.trading.modules.externalrequest.dto;

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
@Schema(description = "KYC Review data payload matching Image 3")
public class KycReviewDto {

    @Schema(description = "Request tracking ID", example = "REQ-2026-00045")
    private String requestId;

    @Schema(description = "Applicant Full Name", example = "Rohit Sharma")
    private String fullName;

    @Schema(description = "Avatar display initials", example = "RS")
    private String avatarInitials;

    @Schema(description = "Email Address", example = "rohit.sharma@example.com")
    private String email;

    @Schema(description = "Mobile Number", example = "+91 98765 43210")
    private String mobileNumber;

    @Schema(description = "Company Name", example = "GreenTech Solutions Pvt Ltd")
    private String companyName;

    @Schema(description = "Designation", example = "Director")
    private String designation;

    @Schema(description = "Industry", example = "Renewable Energy")
    private String industry;

    @Schema(description = "KYC Review Status badge", example = "KYC Under Review")
    private String statusBadge;

    @Schema(description = "Submission timestamp", example = "15 Sep 2026, 10:24 AM")
    private String submittedOn;

    @Schema(description = "Last updated timestamp", example = "16 Sep 2026, 09:30 AM")
    private String lastUpdated;

    // --- KYC Status Metrics ---
    @Schema(description = "Overall KYC completion percentage", example = "60")
    private Integer completionPercentage;

    @Schema(description = "Number of verified documents", example = "2")
    private Integer verifiedCount;

    @Schema(description = "Number of documents currently under review", example = "2")
    private Integer underReviewCount;

    @Schema(description = "Number of unverified documents", example = "1")
    private Integer notVerifiedCount;

    @Schema(description = "Number of optional documents", example = "0")
    private Integer optionalCount;

    // --- Categorized Documents ---
    @Schema(description = "Identity Documents (PAN, Aadhaar)")
    @Builder.Default
    private List<KycDocumentDto> identityDocuments = new ArrayList<>();

    @Schema(description = "Address Proof Documents (Electricity Bill, etc.)")
    @Builder.Default
    private List<KycDocumentDto> addressProof = new ArrayList<>();

    @Schema(description = "Organization Documents (Certificate of Inc, etc.)")
    @Builder.Default
    private List<KycDocumentDto> organizationDocuments = new ArrayList<>();

    // --- Compliance Checklist ---
    @Schema(description = "Compliance items checklist")
    @Builder.Default
    private List<ChecklistItemDto> complianceChecklist = new ArrayList<>();

    // --- Verification Notes & History ---
    @Schema(description = "Verification notes added by compliance")
    @Builder.Default
    private List<NoteDto> verificationNotes = new ArrayList<>();

    @Schema(description = "Timeline of verification activities")
    @Builder.Default
    private List<VerificationHistoryItemDto> verificationHistory = new ArrayList<>();
}
