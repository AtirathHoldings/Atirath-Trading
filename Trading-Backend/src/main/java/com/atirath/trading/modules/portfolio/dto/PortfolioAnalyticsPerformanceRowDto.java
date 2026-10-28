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
public class PortfolioAnalyticsPerformanceRowDto {
    private Long companyId;
    private String companyName;
    private String symbol;
    private String logoUrl;
    private BigDecimal investedAmount;
    private String formattedInvestedAmount; // "6,00,000"
    private BigDecimal currentValue;
    private String formattedCurrentValue;   // "8,05,000"
    private BigDecimal unrealizedPnl;
    private String formattedUnrealizedPnl;  // "+ 2,05,000" or "- 23,000"
    private BigDecimal returnPercentage;
    private String formattedReturnPercentage; // "+34.2%" or "-20.9%"
    private Boolean isPositive;
}
