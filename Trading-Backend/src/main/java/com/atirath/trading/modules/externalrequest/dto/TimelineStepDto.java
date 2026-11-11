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
@Schema(description = "Step on the Request Status Timeline")
public class TimelineStepDto {

    @Schema(description = "Step number", example = "1")
    private Integer stepNumber;

    @Schema(description = "Step name", example = "Request Submitted")
    private String title;

    @Schema(description = "Detailed status note", example = "Applicant submitted the access request.")
    private String description;

    @Schema(description = "Timestamp when step was reached", example = "15 Sep 2026, 10:24 AM")
    private String timestamp;

    @Schema(description = "Actor responsible", example = "Rohit Sharma")
    private String actor;

    @Schema(description = "Status of step: COMPLETED, IN_PROGRESS, PENDING", example = "COMPLETED")
    private String state;
}
