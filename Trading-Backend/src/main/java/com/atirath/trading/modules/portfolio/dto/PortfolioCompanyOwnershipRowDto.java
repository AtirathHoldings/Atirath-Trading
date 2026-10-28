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
public class PortfolioCompanyOwnershipRowDto {
    private Long companyId;
    private String companyName;
    private String companyCode; // "ATG", "ATT", "FVPL", "TEB", "ROHE", "CSI"
    private String symbol;
    private String logoUrl;
    private Long totalSharesOwned;
    private String formattedTotalSharesOwned; // "2,000"
    private BigDecimal ownershipPercentage;
    private String formattedOwnershipPercentage; // "8.00%"
    private BigDecimal averagePrice;
    private String formattedAveragePrice; // "230.00"
    private BigDecimal totalInvestment;
    private String formattedTotalInvestment; // "4,60,000"
    private BigDecimal currentPrice;
    private String formattedCurrentPrice; // "320.00"
    private BigDecimal currentValue;
    private String formattedCurrentValue; // "6,40,000"
    private BigDecimal unrealizedPnl;
    private String formattedUnrealizedPnl; // "+1,80,000" or "-12,500"
    private BigDecimal unrealizedPnlPercentage;
    private String formattedUnrealizedPnlPercentage; // "+39.1%" or "-10.6%"
    private Boolean isProfit;
}
