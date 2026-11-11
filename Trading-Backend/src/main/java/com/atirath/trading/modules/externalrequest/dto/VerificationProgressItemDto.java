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
@Schema(description = "Item in verification progress checklist")
public class VerificationProgressItemDto {

    @Schema(description = "Verification stage name", example = "Applicant Information")
    private String name;

    @Schema(description = "Detailed description", example = "Basic details submitted")
    private String description;

    @Schema(description = "Status: Completed, In Progress, Pending", example = "Completed")
    private String status;
}
