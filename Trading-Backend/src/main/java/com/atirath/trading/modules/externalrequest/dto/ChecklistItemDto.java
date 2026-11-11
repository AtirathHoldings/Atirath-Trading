package com.atirath.trading.modules.externalrequest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Item in the KYC Compliance Checklist")
public class ChecklistItemDto {

    @Schema(description = "Checklist item name", example = "Identity Proof (PAN)")
    private String name;

    @Schema(description = "Checklist status (Verified, Under Review, Not Uploaded, Optional, Not Verified)", example = "Verified")
    private String status;

    @Schema(description = "Is requirement satisfied", example = "true")
    private Boolean isSatisfied;
}
