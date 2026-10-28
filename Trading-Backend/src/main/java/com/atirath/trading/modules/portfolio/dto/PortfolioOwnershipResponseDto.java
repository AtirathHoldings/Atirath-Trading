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
public class PortfolioOwnershipResponseDto {
    private String asOfDate; // "18 Sep 2025"
    private List<PortfolioSummaryKpiDto> summaryKpis;
    
    // Tabs
    private String activeTab; // "Company-wise", "Sector-wise", "Share Class-wise"
    private List<PortfolioCompanyOwnershipRowDto> companies;
    private PortfolioOwnershipSummaryDetailDto statementSummary;
    
    // Charts
    private List<PortfolioAllocationSliceDto> ownershipDistribution;
    private List<PortfolioTrendPointDto> ownershipTrend;
    
    // Bar chart: Investment vs Current Value
    private List<PortfolioAnalyticsPerformanceRowDto> investmentVsCurrentValue;
}
