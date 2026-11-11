package com.atirath.trading.modules.externalrequest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Payload for approving or rejecting an External Member Request")
public class DecisionRequestDto {

    @NotBlank(message = "Decision must be APPROVE or REJECT")
    @Schema(description = "Decision action: APPROVE or REJECT", example = "APPROVE", requiredMode = Schema.RequiredMode.REQUIRED)
    private String decision;

    @Schema(description = "Reviewing officer name", example = "Anitha Verma")
    private String officerName;

    @Schema(description = "Department of reviewing officer", example = "Compliance")
    private String department;

    // --- Specific to APPROVE ---
    @Schema(description = "List of approved permissions")
    @Builder.Default
    private List<String> approvedAccess = new ArrayList<>();

    @Schema(description = "Validity in months/years", example = "1 Year")
    private String validityPeriod;

    @Schema(description = "Welcome note / remarks")
    private String welcomeRemarks;

    // --- Specific to REJECT ---
    @Schema(description = "Specific reason for rejection", example = "The submitted KYC documents are incomplete. Please provide a valid address proof and updated organization registration certificate.")
    private String rejectionReason;

    @Schema(description = "Recommended steps for applicant")
    @Builder.Default
    private List<String> whatYouCanDo = new ArrayList<>();
}
