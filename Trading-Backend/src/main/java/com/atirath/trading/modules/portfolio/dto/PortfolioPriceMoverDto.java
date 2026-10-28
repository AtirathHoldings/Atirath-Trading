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
public class PortfolioPriceMoverDto {
    private String companyName;
    private String symbol;
    private BigDecimal currentPrice;
    private String formattedCurrentPrice; // "135.00"
    private BigDecimal changeAmount;
    private String formattedChangeAmount; // "+ 35.00" or "- 15.00"
    private BigDecimal changePercentage;
    private String formattedChangePercentage; // "+35.0%" or "-9.4%"
    private Boolean isGainer;
}
