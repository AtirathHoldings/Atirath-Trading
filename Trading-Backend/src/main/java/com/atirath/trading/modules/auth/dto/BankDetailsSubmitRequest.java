package com.atirath.trading.modules.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankDetailsSubmitRequest {

    @NotBlank(message = "Bank name is required")
    @Size(max = 150, message = "Bank name cannot exceed 150 characters")
    @Schema(description = "Name of the Bank", example = "HDFC Bank")
    private String bankName;

    @NotBlank(message = "Account holder name is required")
    @Size(max = 150, message = "Account holder name cannot exceed 150 characters")
    @Schema(description = "Account holder name", example = "Sairam V")
    private String accountHolderName;

    @NotBlank(message = "Account number is required")
    @Pattern(regexp = "^[0-9]{9,18}$", message = "Bank account number must be between 9 and 18 digits")
    @Schema(description = "Bank account number (will be encrypted at rest)", example = "50100123456789")
    private String accountNumber;

    @NotBlank(message = "Confirm account number is required")
    @Schema(description = "Re-enter bank account number for confirmation", example = "50100123456789")
    private String confirmAccountNumber;

    @NotBlank(message = "IFSC code is required")
    @Pattern(regexp = "^[A-Z]{4}0[A-Z0-9]{6}$", message = "Invalid IFSC Code format (e.g. HDFC0001234)")
    @Schema(description = "11-character IFSC code", example = "HDFC0001234")
    private String ifscCode;

    @NotBlank(message = "Branch name is required")
    @Size(max = 150, message = "Branch name cannot exceed 150 characters")
    @Schema(description = "Bank branch name", example = "Banjara Hills Branch")
    private String branchName;
}
