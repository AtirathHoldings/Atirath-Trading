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
public class PortfolioReturnAnalysisDto {
    private BigDecimal totalReturn;
    private String formattedTotalReturn; // "40.5%"
    private BigDecimal annualizedReturn;
    private String formattedAnnualizedReturn; // "18.6%"
    private BigDecimal priceAppreciation;
    private String formattedPriceAppreciation; // "12.4%"
    private BigDecimal dividendReturn;
    private String formattedDividendReturn; // "6.8%"
}
