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
@Schema(description = "Request Status & Progress Tracking Payload matching Image 4")
public class RequestStatusDto {

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

    @Schema(description = "Current overall status", example = "Under Review")
    private String currentStatus;

    @Schema(description = "Next step in verification", example = "KYC Verification")
    private String nextStep;

    @Schema(description = "Description for applicant", example = "Please upload and verify all required KYC documents.")
    private String nextStepDescription;

    @Schema(description = "Submission timestamp", example = "15 Sep 2026, 10:24 AM")
    private String submittedOn;

    @Schema(description = "Last update timestamp", example = "16 Sep 2026, 09:30 AM")
    private String lastUpdated;

    @Schema(description = "Estimated review timeframe", example = "2 - 5 Working Days")
    private String expectedReviewTimeline;

    @Schema(description = "List of access permissions requested")
    @Builder.Default
    private List<String> requestedAccess = new ArrayList<>();

    @Schema(description = "Status Timeline from Submission to Decision")
    @Builder.Default
    private List<TimelineStepDto> statusTimeline = new ArrayList<>();

    @Schema(description = "Verification Progress Breakdown")
    @Builder.Default
    private List<VerificationProgressItemDto> verificationProgress = new ArrayList<>();

    @Schema(description = "Required Documents Status Checklist")
    @Builder.Default
    private List<RequiredDocumentDto> requiredDocuments = new ArrayList<>();

    @Schema(description = "Recent system & compliance activities")
    @Builder.Default
    private List<ActivityDto> recentActivities = new ArrayList<>();
}
