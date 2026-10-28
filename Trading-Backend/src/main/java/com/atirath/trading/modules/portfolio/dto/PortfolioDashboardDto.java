package com.atirath.trading.modules.portfolio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioDashboardDto {
    private String userName;        // "Sairam Vempati"
    private String memberId;        // "ATG10027"
    private String avatarInitials;  // "SV"
    private String asOfDate;        // "18 Sep 2025, 11:28 AM"
    
    private List<PortfolioSummaryKpiDto> summaryKpis;
    private String activeTrendPeriod; // "6M"
    private List<PortfolioTrendPointDto> valueTrend;
    private List<PortfolioAllocationSliceDto> allocation;
    private PortfolioPerformanceDto performance;
    private List<PortfolioHoldingItemDto> topHoldings;
    private List<PortfolioActivityItemDto> recentActivity;
}
