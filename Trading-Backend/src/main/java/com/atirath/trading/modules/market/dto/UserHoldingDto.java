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
public class UserHoldingDto {

    @Schema(description = "Company ID")
    private Long companyId;

    @Schema(description = "Company Name", example = "Atirath Holdings India Limited")
    private String companyName;

    @Schema(description = "Trading Symbol", example = "ATG-H")
    private String symbol;

    @Schema(description = "Total Shares Owned", example = "3500")
    private Long totalShares;

    @Schema(description = "Locked Shares (in open sell orders)", example = "0")
    private Long lockedShares;

    @Schema(description = "Available Shares free to sell", example = "3500")
    private Long availableShares;

    @Schema(description = "Average Buy Price (₹)", example = "215.00")
    private BigDecimal averageBuyPrice;

    @Schema(description = "Current Market Price (₹)", example = "245.00")
    private BigDecimal currentPrice;

    @Schema(description = "Current Value (₹)", example = "857500.00")
    private BigDecimal currentValue;

    @Schema(description = "Total Invested Value (₹)", example = "752500.00")
    private BigDecimal investedValue;

    @Schema(description = "Unrealized P&L (₹)", example = "105000.00")
    private BigDecimal unrealizedPnl;

    @Schema(description = "P&L Percentage (%)", example = "13.95")
    private BigDecimal pnlPercentage;
}
