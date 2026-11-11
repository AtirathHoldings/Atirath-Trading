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
@Schema(description = "Item in verification history log")
public class VerificationHistoryItemDto {

    @Schema(description = "History action title", example = "PAN Verified")
    private String title;

    @Schema(description = "Timestamp of event", example = "15 Sep 2026, 02:15 PM")
    private String timestamp;

    @Schema(description = "Actor name", example = "Anitha Verma")
    private String actor;

    @Schema(description = "Status of the step (Verified, Submitted, Pending, In Progress)", example = "Verified")
    private String status;
}
