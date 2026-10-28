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
public class PortfolioSummaryKpiDto {
    private String id;
    private String title;
    private BigDecimal amount;
    private String formattedAmount;
    private Long count;
    private String formattedCount;
    private BigDecimal changePercentage;
    private String formattedChangePercentage;
    private Boolean isPositive;
    private String comparisonLabel; // e.g. "vs. previous month", "this financial year", "Across 6 companies"
    private String icon;
}
