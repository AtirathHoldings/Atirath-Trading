package com.atirath.trading.modules.portfolio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioPnlSummaryDto {
    private BigDecimal totalInvestment;
    private String formattedTotalInvestment; // "₹ 18,50,000"
    private BigDecimal currentValue;
    private String formattedCurrentValue;   // "₹ 24,80,000"
    private BigDecimal totalPnl;
    private String formattedTotalPnl;       // "₹ 6,30,000"
    private BigDecimal totalReturn;
    private String formattedTotalReturn;    // "+34.1%"
    private BigDecimal realizedPnl;
    private String formattedRealizedPnl;    // "₹ 1,20,000"
    private BigDecimal unrealizedPnl;
    private String formattedUnrealizedPnl;  // "₹ 5,10,000"
    private BigDecimal realizedReturn;
    private String formattedRealizedReturn; // "+10.0%"
    private BigDecimal unrealizedReturn;
    private String formattedUnrealizedReturn; // "+43.6%"
}
