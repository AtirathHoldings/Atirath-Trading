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
@Schema(description = "Full External Access Request profile including applicant, organization, access and status details")
public class ExternalAccessRequestDto {

    @Schema(description = "Request tracking ID", example = "REQ-2026-00045")
    private String requestId;

    @Schema(description = "Current request status", example = "UNDER_REVIEW")
    private String status;

    @Schema(description = "Human-readable status badge", example = "Under Review")
    private String statusBadge;

    @Schema(description = "Source of request submission", example = "Web Application")
    private String source;

    @Schema(description = "Formatted submission timestamp", example = "15 Sep 2026, 10:24 AM")
    private String submittedOn;

    @Schema(description = "Formatted last updated timestamp", example = "16 Sep 2026, 09:30 AM")
    private String lastUpdated;

    // --- Personal Information ---
    @Schema(description = "Applicant Full Name", example = "Rohit Sharma")
    private String fullName;

    @Schema(description = "Date of Birth", example = "12 Mar 1995")
    private String dateOfBirth;

    @Schema(description = "Email Address", example = "rohit.sharma@example.com")
    private String email;

    @Schema(description = "Mobile Number", example = "+91 98765 43210")
    private String mobileNumber;

    @Schema(description = "Permanent Account Number (PAN)", example = "ABCDE1234F")
    private String panNumber;

    @Schema(description = "Aadhaar Number", example = "1234 5678 9012")
    private String aadhaarNumber;

    @Schema(description = "Applicant Residential/Communication Address", example = "Plot No. 45, Tech Park, Madhapur Hyderabad, Telangana - 500081 India")
    private String address;

    // --- Additional Information ---
    @Schema(description = "Nationality", example = "Indian")
    private String nationality;

    @Schema(description = "Occupation", example = "Business Professional")
    private String occupation;

    @Schema(description = "Professional Experience", example = "8+ Years")
    private String experience;

    @Schema(description = "LinkedIn Profile URL", example = "https://linkedin.com/in/rohitsharma")
    private String linkedInProfile;

    @Schema(description = "Avatar display initials", example = "RS")
    private String avatarInitials;

    // --- Organization Information ---
    @Schema(description = "Organization / Company Name", example = "GreenTech Solutions Pvt Ltd")
    private String companyName;

    @Schema(description = "Industry category", example = "Renewable Energy")
    private String industry;

    @Schema(description = "Company Legal Entity Type", example = "Private Limited")
    private String companyType;

    @Schema(description = "Designation of Applicant in Organization", example = "Director")
    private String designation;

    @Schema(description = "Company PAN Number", example = "AABCG1234H")
    private String companyPan;

    @Schema(description = "Corporate Registration / CIN Number", example = "U40100TG2020PTC123456")
    private String registrationNumber;

    @Schema(description = "Company Registered Address", example = "Plot No. 45, Tech Park, Madhapur Hyderabad, Telangana - 500081")
    private String companyAddress;

    @Schema(description = "Official Website URL", example = "https://www.greentechsolutions.com")
    private String companyWebsite;

    @Schema(description = "Organization Logo or Brand Tagline", example = "GreenTech Solutions Renewable Energy for a Better Tomorrow")
    private String companyLogo;

    // --- Access & Purpose ---
    @Schema(description = "Purpose of requesting member access", example = "To invest in ATIRATH Group renewable energy companies for strategic partnership and long-term collaboration.")
    private String purposeOfRequest;

    @Schema(description = "List of requested platform access permissions")
    @Builder.Default
    private List<String> requestedAccess = new ArrayList<>();

    // --- Financial Profile (Self Declaration) ---
    @Schema(description = "Expected Investment Range", example = "₹10,00,000 - ₹50,00,000")
    private String expectedInvestmentRange;

    @Schema(description = "Investment Purpose", example = "Strategic Investment")
    private String investmentPurpose;

    @Schema(description = "Source of Funds", example = "Business Profits")
    private String sourceOfFunds;

    @Schema(description = "Annual Income Range", example = "₹25,00,000 - ₹50,00,000")
    private String annualIncomeRange;

    // --- Approval Information ---
    @Schema(description = "Approval timestamp", example = "16 Sep 2026, 11:20 AM")
    private String approvedOn;

    @Schema(description = "List of granted access permissions")
    @Builder.Default
    private List<String> approvedAccess = new ArrayList<>();

    @Schema(description = "Access validity start date", example = "16 Sep 2026")
    private String accessValidFrom;

    @Schema(description = "Access validity end date with duration label", example = "15 Sep 2027 (1 Year)")
    private String accessValidTill;

    @Schema(description = "Approved by officer name", example = "Compliance Team")
    private String approvedBy;

    @Schema(description = "Welcome message on approval")
    private String welcomeMessage;

    // --- Rejection Information ---
    @Schema(description = "Rejection timestamp", example = "16 Sep 2026, 04:15 PM")
    private String rejectedOn;

    @Schema(description = "Rejected by officer name and department", example = "Anitha Verma (Compliance)")
    private String rejectedBy;

    @Schema(description = "Reason explaining the rejection")
    private String reasonForRejection;

    @Schema(description = "Actionable steps following rejection")
    @Builder.Default
    private List<String> whatYouCanDo = new ArrayList<>();

    // --- Notes and Timeline ---
    @Builder.Default
    private List<NoteDto> notes = new ArrayList<>();

    @Builder.Default
    private List<TimelineStepDto> timeline = new ArrayList<>();
}
