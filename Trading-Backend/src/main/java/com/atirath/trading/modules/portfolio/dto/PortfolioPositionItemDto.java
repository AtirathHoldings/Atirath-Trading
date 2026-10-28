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
public class PortfolioPositionItemDto {
    private Long positionId;
    private Long companyId;
    private String companyName;
    private String companyCode;         // "ATG", "FVPL", "TEB", "CSI", "ROHE"
    private String symbol;
    private String logoUrl;
    private String positionType;        // "Long"
    private Long quantity;
    private String formattedQuantity;   // "1,000"
    private BigDecimal averagePrice;
    private String formattedAveragePrice; // "₹ 150.00"
    private BigDecimal currentPrice;
    private String formattedCurrentPrice; // "₹ 230.00"
    private BigDecimal investedValue;
    private String formattedInvestedValue; // "₹ 1,50,000"
    private BigDecimal currentValue;
    private String formattedCurrentValue; // "₹ 2,30,000"
    private BigDecimal unrealizedPnl;
    private String formattedUnrealizedPnl; // "+₹ 80,000" or "-₹ 7,500"
    private BigDecimal unrealizedPnlPercentage;
    private String formattedUnrealizedPnlPercentage; // "+53.3%" or "-9.4%"
    private Boolean isProfit;
}
