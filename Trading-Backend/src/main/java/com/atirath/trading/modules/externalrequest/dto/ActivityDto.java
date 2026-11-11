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
@Schema(description = "Recent activity entry on status tracking page")
public class ActivityDto {

    @Schema(description = "Actor full name", example = "Anitha Verma")
    private String actorName;

    @Schema(description = "Department / Role", example = "Compliance")
    private String actorRole;

    @Schema(description = "Initials", example = "AV")
    private String initials;

    @Schema(description = "Timestamp", example = "16 Sep 2026, 09:30 AM")
    private String timestamp;

    @Schema(description = "Activity text", example = "Application moved to KYC verification. Documents are under review.")
    private String message;
}
