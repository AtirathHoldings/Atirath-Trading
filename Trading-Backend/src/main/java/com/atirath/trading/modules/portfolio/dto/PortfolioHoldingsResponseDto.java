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
public class PortfolioHoldingsResponseDto {
    private String asOfDate; // e.g. "18 Sep 2025, 11:28 AM"
    private List<PortfolioSummaryKpiDto> summaryKpis;
    private List<PortfolioHoldingItemDto> holdings;
    private List<PortfolioAllocationSliceDto> allocation;
    private List<PortfolioQuickInsightDto> quickInsights;
    private String insightSummary; // "Your portfolio value has increased by 12.6% compared to last month. Consider reviewing your holdings for rebalancing opportunities."
    private Integer totalCount;
    private Integer currentPage;
    private Integer totalPages;
}
