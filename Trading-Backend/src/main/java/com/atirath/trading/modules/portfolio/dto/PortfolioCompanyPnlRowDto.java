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
public class PortfolioCompanyPnlRowDto {
    private Long companyId;
    private String companyName;
    private String symbol;
    private String companyCode; // "ATG", "ATT", "FVPL", "TEB", "ROHE", "CSI"
    private String logoUrl;
    private BigDecimal investedAmount;
    private String formattedInvestedAmount; // "6,00,000"
    private BigDecimal currentValue;
    private String formattedCurrentValue;   // "8,05,000"
    private BigDecimal realizedPnl;
    private String formattedRealizedPnl;    // "50,000"
    private BigDecimal unrealizedPnl;
    private String formattedUnrealizedPnl;  // "1,55,000" or "-23,000"
    private BigDecimal totalPnl;
    private String formattedTotalPnl;       // "2,05,000" or "-23,000"
    private BigDecimal returnPercentage;
    private String formattedReturnPercentage; // "+34.2%" or "-20.9%"
    private Boolean isProfit;
}
