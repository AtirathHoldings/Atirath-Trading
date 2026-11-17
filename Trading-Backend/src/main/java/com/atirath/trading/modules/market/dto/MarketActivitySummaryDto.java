package com.atirath.trading.modules.market.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketActivitySummaryDto {
    private long totalTrades;
    private BigDecimal totalTradedValue;
    private long activeCompaniesCount;
    private long todayTradesCount;
    private BigDecimal todayTradedValue;
    private String marketStatus;
    private String marketHours;
}
