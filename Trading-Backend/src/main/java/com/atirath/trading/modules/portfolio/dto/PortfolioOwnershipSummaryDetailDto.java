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
public class PortfolioOwnershipSummaryDetailDto {
    private String memberName; // "Sairam Vempati"
    private String memberId;   // "ATG10027"
    private String statementDate; // "18 Sep 2025"
    private Integer totalCompanies; // 6
    private Long totalSharesOwned; // 5450
    private String formattedTotalSharesOwned; // "5,450"
    private BigDecimal totalInvestment; // 1850000.00
    private String formattedTotalInvestment; // "₹ 18,50,000"
    private BigDecimal currentMarketValue; // 2480000.00
    private String formattedCurrentMarketValue; // "₹ 24,80,000"
    private BigDecimal totalOwnership; // 4.85
    private String formattedTotalOwnership; // "4.85%"
    private BigDecimal averagePrice; // 194.50
    private String formattedAveragePrice; // "194.50"
    private BigDecimal unrealizedPnl; // 630000.00
    private String formattedUnrealizedPnl; // "+6,30,000"
    private BigDecimal unrealizedPnlPercentage; // 34.1
    private String formattedUnrealizedPnlPercentage; // "+34.1%"
}
