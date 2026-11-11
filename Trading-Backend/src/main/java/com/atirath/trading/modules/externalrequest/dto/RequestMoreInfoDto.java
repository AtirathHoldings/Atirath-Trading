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
@Schema(description = "Payload for requesting more information from applicant")
public class RequestMoreInfoDto {

    @NotBlank(message = "Message cannot be empty")
    @Schema(description = "Information requested from applicant", example = "Please upload the latest 3-month bank statement and electricity bill.", requiredMode = Schema.RequiredMode.REQUIRED)
    private String message;

    @Schema(description = "Officer name", example = "Anitha Verma")
    private String requestedBy;
}
