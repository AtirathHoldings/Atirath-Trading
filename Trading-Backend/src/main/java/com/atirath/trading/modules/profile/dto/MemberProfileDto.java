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
@Schema(description = "Full Member Profile overview matching Profile & Settings Image 1")
public class MemberProfileDto {

    // --- Header Information ---
    @Schema(description = "Member ID", example = "ATG10027")
    private String memberId;

    @Schema(description = "Full Name", example = "Rohit Sharma")
    private String fullName;

    @Schema(description = "Membership badge label", example = "Verified Member")
    private String memberBadge;

    @Schema(description = "Primary Email", example = "rohit.sharma@example.com")
    private String email;

    @Schema(description = "Primary Mobile", example = "+91 98765 43210")
    private String mobileNumber;

    @Schema(description = "Organization Name", example = "GreenTech Solutions Pvt Ltd")
    private String companyName;

    @Schema(description = "Designation in company", example = "Director")
    private String designation;

    @Schema(description = "Industry category", example = "Renewable Energy")
    private String industry;

    @Schema(description = "Member Since Date", example = "12 Jan 2021")
    private String memberSince;

    @Schema(description = "Profile Completion Percentage", example = "90")
    private Integer profileCompletion;

    @Schema(description = "Avatar display initials", example = "RS")
    private String avatarInitials;

    // --- Sections ---
    @Schema(description = "Personal Information section")
    private PersonalInfoDto personalInfo;

    @Schema(description = "Organization Information section")
    private OrganizationInfoDto organizationInfo;

    @Schema(description = "Contact Information section")
    private ContactInfoDto contactInfo;

    @Schema(description = "Professional Details section")
    private ProfessionalDetailsDto professionalDetails;

    @Schema(description = "Address Information section")
    private AddressInfoDto addressInfo;

    @Schema(description = "Additional Information section")
    private AdditionalInfoDto additionalInfo;
}
