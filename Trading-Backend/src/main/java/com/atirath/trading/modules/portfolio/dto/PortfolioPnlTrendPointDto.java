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
public class PortfolioPnlTrendPointDto {
    private String month; // "Jan 2025", "Feb 2025", ..., "Sep 2025"
    private BigDecimal realizedPnl;
    private String formattedRealizedPnl;
    private BigDecimal unrealizedPnl;
    private String formattedUnrealizedPnl;
    private BigDecimal totalPnl;
    private String formattedTotalPnl;
}
