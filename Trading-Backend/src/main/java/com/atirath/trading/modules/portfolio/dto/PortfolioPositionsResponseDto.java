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
public class PortfolioPositionsResponseDto {
    private List<PortfolioSummaryKpiDto> summaryKpis;
    private String activeTab; // "ALL", "PROFITABLE", "LOSS_MAKING"
    private List<PortfolioPositionItemDto> positions;
    private List<PortfolioAllocationSliceDto> valueDistribution;
    private List<PortfolioQuickInsightDto> positionInsights;
    private List<PortfolioTrendPointDto> valueTrend;
    private List<PortfolioDayMovementDto> daysMovement;
    private Integer totalPositionsCount;
}
