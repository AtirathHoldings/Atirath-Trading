package com.atirath.trading.modules.portfolio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioQuickInsightDto {
    private String title;
    private String value;
    private String change;
    private Boolean isPositive;
    private String icon;
}
