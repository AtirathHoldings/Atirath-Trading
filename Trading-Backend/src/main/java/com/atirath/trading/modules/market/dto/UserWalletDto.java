package com.atirath.trading.modules.market.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserWalletDto {

    @Schema(description = "Account Reference", example = "ESCROW-ATG-10027")
    private String accountReference;

    @Schema(description = "Available Balance in INR", example = "1248500.00")
    private BigDecimal availableBalance;

    @Schema(description = "Blocked Balance in Escrow in INR", example = "0.00")
    private BigDecimal blockedBalance;

    @Schema(description = "Total Balance (Available + Blocked) in INR", example = "1248500.00")
    private BigDecimal totalBalance;

    @Schema(description = "Currency Code", example = "INR")
    private String currencyCode;

    @Schema(description = "Account Status", example = "ACTIVE")
    private String accountStatus;
}
