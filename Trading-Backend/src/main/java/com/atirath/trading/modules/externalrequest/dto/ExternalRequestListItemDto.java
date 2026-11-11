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
@Schema(description = "Summary item for Applicant List view")
public class ExternalRequestListItemDto {

    @Schema(description = "Request ID", example = "REQ-2026-00045")
    private String requestId;

    @Schema(description = "Applicant Full Name", example = "Rohit Sharma")
    private String fullName;

    @Schema(description = "Email", example = "rohit.sharma@example.com")
    private String email;

    @Schema(description = "Mobile", example = "+91 98765 43210")
    private String mobileNumber;

    @Schema(description = "Company Name", example = "GreenTech Solutions Pvt Ltd")
    private String companyName;

    @Schema(description = "Designation", example = "Director")
    private String designation;

    @Schema(description = "Industry", example = "Renewable Energy")
    private String industry;

    @Schema(description = "Status code", example = "UNDER_REVIEW")
    private String status;

    @Schema(description = "Status Badge", example = "Under Review")
    private String statusBadge;

    @Schema(description = "Submitted On", example = "15 Sep 2026, 10:24 AM")
    private String submittedOn;

    @Schema(description = "Last Updated", example = "16 Sep 2026, 09:30 AM")
    private String lastUpdated;

    @Schema(description = "Avatar initials", example = "RS")
    private String avatarInitials;

    @Schema(description = "Requested Access Tags")
    @Builder.Default
    private List<String> requestedAccess = new ArrayList<>();
}
