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
@Schema(description = "Bank Account item matching Image 3")
public class BankAccountItemDto {

    @Schema(description = "Internal Bank Account ID", example = "1")
    private Long id;

    @Schema(description = "Bank Name", example = "State Bank of India")
    private String bankName;

    @Schema(description = "Bank Logo identifier / URL", example = "SBI")
    private String bankLogo;

    @Schema(description = "Account Holder Name", example = "Rohit Sharma")
    private String accountHolderName;

    @Schema(description = "Bank Account Number formatted/masked", example = "1234 5678 9012")
    private String accountNumber;

    @Schema(description = "IFSC Code", example = "SBIN0001234")
    private String ifscCode;

    @Schema(description = "Whether this is the Primary Account for settlements", example = "true")
    private Boolean isPrimary;

    @Schema(description = "Verification Status: Verified, Under Verification, Rejected", example = "Verified")
    private String status;

    @Schema(description = "Date account was added", example = "12 Jan 2024")
    private String addedOn;
}
