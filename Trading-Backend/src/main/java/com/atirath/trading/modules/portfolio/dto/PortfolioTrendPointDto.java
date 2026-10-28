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
public class PortfolioTrendPointDto {
    private String period; // e.g. "Mar 2025", "18 Sep 2025"
    private String date;   // ISO-8601 or YYYY-MM-DD
    private BigDecimal value;
    private String formattedValue;
    private Boolean isCurrent;
}
