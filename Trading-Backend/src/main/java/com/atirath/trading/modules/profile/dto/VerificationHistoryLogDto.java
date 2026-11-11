package com.atirath.trading.modules.profile.dto;

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
@Schema(description = "Verification History item matching Image 2")
public class VerificationHistoryLogDto {

    @Schema(description = "History action title", example = "PAN Verified")
    private String title;

    @Schema(description = "Formatted timestamp", example = "15 Sep 2026, 02:15 PM")
    private String timestamp;

    @Schema(description = "Officer or applicant who took action", example = "Anitha Verma (Compliance)")
    private String actor;

    @Schema(description = "Status: Verified, Submitted, Pending", example = "Verified")
    private String status;
}
