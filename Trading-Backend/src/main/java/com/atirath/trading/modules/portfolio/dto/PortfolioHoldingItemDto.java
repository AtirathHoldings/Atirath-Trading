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
public class PortfolioHoldingItemDto {
    private Long companyId;
    private String companyName;
    private String symbol;
    private String companyCode; // e.g. "ATG", "ATT", "FVPL", "TEB", "ROHE", "CSI"
    private String logoUrl;
    private Long sharesHeld;
    private String formattedSharesHeld;
    private BigDecimal averageAcquisitionPrice;
    private String formattedAverageAcquisitionPrice; // e.g. "₹ 150.00"
    private BigDecimal currentPrice;
    private String formattedCurrentPrice;           // e.g. "₹ 230.00"
    private BigDecimal currentValue;
    private String formattedCurrentValue;           // e.g. "₹ 8,05,000"
    private BigDecimal investedValue;
    private String formattedInvestedValue;
    private BigDecimal unrealizedPnl;
    private String formattedUnrealizedPnl;          // e.g. "+₹ 2,80,000" or "-₹ 9,000"
    private BigDecimal unrealizedPnlPercentage;
    private String formattedUnrealizedPnlPercentage;// e.g. "+53.8%" or "-9.4%"
    private Boolean isProfit;
    private Long lockedShares;
    private String formattedLockedShares;
    private Long availableShares;
    private String formattedAvailableShares;
    private BigDecimal allocationPercentage;
    private String formattedAllocationPercentage;   // e.g. "35.5%"
    private String progressBarColor;
}
