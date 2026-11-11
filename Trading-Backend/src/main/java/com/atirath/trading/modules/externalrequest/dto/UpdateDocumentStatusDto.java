package com.atirath.trading.modules.externalrequest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Payload for verifying or updating a KYC document status")
public class UpdateDocumentStatusDto {

    @NotBlank(message = "Document status is required (e.g. Verified, Under Review, Not Verified)")
    @Schema(description = "New status: Verified, Under Review, Not Verified", example = "Verified", requiredMode = Schema.RequiredMode.REQUIRED)
    private String status;

    @Schema(description = "Officer or reviewer name", example = "Anitha Verma (Compliance)")
    private String verifiedBy;

    @Schema(description = "Review comments or remarks", example = "Valid document verified against MCA database.")
    private String remarks;
}
