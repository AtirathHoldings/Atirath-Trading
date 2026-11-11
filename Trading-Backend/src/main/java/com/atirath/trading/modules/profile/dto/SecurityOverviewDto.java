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
@Schema(description = "Full Security Dashboard payload matching Image 4")
public class SecurityOverviewDto {

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

    // --- Password Section ---
    @Schema(description = "Masked password representation", example = "••••••••••••")
    private String maskedPassword;

    @Schema(description = "Date password was last changed", example = "12 Jan 2024")
    private String passwordLastChanged;

    // --- 2FA Section ---
    @Schema(description = "2FA Configuration & Status")
    private TwoFactorSettingsDto twoFactor;

    // --- Active Sessions & Devices ---
    @Schema(description = "Active Sessions and Devices list")
    @Builder.Default
    private List<ActiveSessionDto> activeSessions = new ArrayList<>();

    // --- Security Settings Toggles ---
    @Schema(description = "Configured security toggles")
    private SecurityPreferencesDto securityPreferences;

    // --- Login History ---
    @Schema(description = "Recent login activity history")
    @Builder.Default
    private List<LoginHistoryItemDto> loginHistory = new ArrayList<>();
}
