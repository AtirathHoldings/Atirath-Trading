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
@Schema(description = "Bank & Settlement Accounts page payload matching Image 3")
public class BankAccountsPageDto {

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

    // --- Bank Accounts List ---
    @Schema(description = "Linked Bank Accounts list")
    @Builder.Default
    private List<BankAccountItemDto> bankAccounts = new ArrayList<>();

    // --- Account Guidelines ---
    @Schema(description = "List of banking and settlement guidelines")
    @Builder.Default
    private List<String> accountGuidelines = new ArrayList<>();

    // --- Settlement Info Note ---
    @Schema(description = "Primary settlement account explanation")
    private String settlementInfo;
}
