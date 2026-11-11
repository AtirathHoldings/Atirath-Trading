package com.atirath.trading.modules.externalrequest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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
@Schema(description = "Payload for submitting or saving draft of an External Access Request")
public class SubmitExternalAccessRequestDto {

    // --- Section 1: Applicant Details ---
    @NotBlank(message = "Full Name is required")
    @Schema(description = "Full Name", example = "Rohit Sharma", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fullName;

    @NotBlank(message = "Email Address is required")
    @Email(message = "Valid email is required")
    @Schema(description = "Email Address", example = "rohit.sharma@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotBlank(message = "Mobile Number is required")
    @Schema(description = "Mobile Number", example = "+91 98765 43210", requiredMode = Schema.RequiredMode.REQUIRED)
    private String mobileNumber;

    @Schema(description = "Date of Birth", example = "12 Mar 1995")
    private String dateOfBirth;

    @NotBlank(message = "PAN Number is required")
    @Schema(description = "PAN Number", example = "ABCDE1234F", requiredMode = Schema.RequiredMode.REQUIRED)
    private String panNumber;

    @NotBlank(message = "Aadhaar Number is required")
    @Schema(description = "Aadhaar Number", example = "1234 5678 9012", requiredMode = Schema.RequiredMode.REQUIRED)
    private String aadhaarNumber;

    @Schema(description = "Residential/Communication Address", example = "Plot No. 45, Tech Park, Madhapur Hyderabad, Telangana - 500081 India")
    private String address;

    @Schema(description = "Nationality", example = "Indian")
    private String nationality;

    @Schema(description = "Occupation", example = "Business Professional")
    private String occupation;

    @Schema(description = "Experience", example = "8+ Years")
    private String experience;

    @Schema(description = "LinkedIn Profile", example = "https://linkedin.com/in/rohitsharma")
    private String linkedInProfile;

    // --- Section 2: Organization Details ---
    @NotBlank(message = "Organization Name is required")
    @Schema(description = "Organization / Company Name", example = "GreenTech Solutions Pvt Ltd", requiredMode = Schema.RequiredMode.REQUIRED)
    private String companyName;

    @NotBlank(message = "Industry is required")
    @Schema(description = "Industry", example = "Renewable Energy", requiredMode = Schema.RequiredMode.REQUIRED)
    private String industry;

    @NotBlank(message = "Designation is required")
    @Schema(description = "Designation", example = "Director", requiredMode = Schema.RequiredMode.REQUIRED)
    private String designation;

    @Schema(description = "Company Legal Entity Type", example = "Private Limited")
    private String companyType;

    @Schema(description = "Company PAN", example = "AABCG1234H")
    private String companyPan;

    @Schema(description = "Company Registration CIN", example = "U40100TG2020PTC123456")
    private String registrationNumber;

    @Schema(description = "Company Registered Address", example = "Plot No. 45, Tech Park, Madhapur Hyderabad, Telangana - 500081")
    private String companyAddress;

    @Schema(description = "Company Website", example = "https://www.greentechsolutions.com")
    private String companyWebsite;

    // --- Section 3: Access & Purpose ---
    @NotBlank(message = "Purpose of Request is required")
    @Schema(description = "Purpose of Request", example = "To invest in ATIRATH Group renewable energy companies for strategic partnership and long-term collaboration.", requiredMode = Schema.RequiredMode.REQUIRED)
    private String purposeOfRequest;

    @Schema(description = "List of requested accesses: View Company Information, Buy Shares, Sell Shares, Negotiation, Participate in Private Placements")
    @Builder.Default
    private List<String> requestedAccess = new ArrayList<>();

    // --- Financial Self-Declaration ---
    @Schema(description = "Expected Investment Range", example = "₹10,00,000 - ₹50,00,000")
    private String expectedInvestmentRange;

    @Schema(description = "Investment Purpose", example = "Strategic Investment")
    private String investmentPurpose;

    @Schema(description = "Source of Funds", example = "Business Profits")
    private String sourceOfFunds;

    @Schema(description = "Annual Income Range", example = "₹25,00,000 - ₹50,00,000")
    private String annualIncomeRange;

    @Schema(description = "Whether to save as Draft without validation errors", example = "false")
    private Boolean isDraft;
}
