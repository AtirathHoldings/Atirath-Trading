package com.atirath.trading.modules.profile.dto;

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
@Schema(description = "Payload for adding a new bank account")
public class AddBankAccountRequestDto {

    @NotBlank(message = "Bank Name is required")
    @Schema(description = "Bank Name", example = "Axis Bank", requiredMode = Schema.RequiredMode.REQUIRED)
    private String bankName;

    @NotBlank(message = "Account Holder Name is required")
    @Schema(description = "Account Holder Name", example = "Rohit Sharma", requiredMode = Schema.RequiredMode.REQUIRED)
    private String accountHolderName;

    @NotBlank(message = "Account Number is required")
    @Schema(description = "Account Number", example = "234567890123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String accountNumber;

    @NotBlank(message = "IFSC Code is required")
    @Schema(description = "IFSC Code", example = "UTIB0004567", requiredMode = Schema.RequiredMode.REQUIRED)
    private String ifscCode;

    @Schema(description = "Set as Primary settlement account", example = "false")
    private Boolean setAsPrimary;
}
