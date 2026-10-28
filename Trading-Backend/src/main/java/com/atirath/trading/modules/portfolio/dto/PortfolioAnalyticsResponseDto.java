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
public class PortfolioAnalyticsResponseDto {
    private String asOfDate; // "18 Sep 2025, 11:28 AM"
    private String dateRange; // "01 Jan 2025 - 18 Sep 2025"
    private List<PortfolioSummaryKpiDto> summaryKpis;
    
    // Allocation Tabs
    private String activeAllocationTab; // "By Company", "By Sector", "By Investment Type"
    private List<PortfolioAllocationSliceDto> allocationByCompany;
    private List<PortfolioAllocationSliceDto> allocationBySector;
    private List<PortfolioAllocationSliceDto> allocationByInvestmentType;
    
    // Two-line Trend Chart
    private String activeTrendPeriod; // "6M"
    private List<PortfolioTrendPointDto> portfolioValueTrend;
    private List<PortfolioTrendPointDto> investedAmountTrend;
    
    // Return Analysis Bars
    private PortfolioReturnAnalysisDto returnAnalysis;
    
    // Company Performance Table
    private List<PortfolioAnalyticsPerformanceRowDto> companyPerformance;
    
    // Asset Type Allocation
    private List<PortfolioAllocationSliceDto> assetTypeAllocation;
    
    // Key Insights
    private List<String> keyInsights;
    
    // Gainers and Losers
    private List<PortfolioPriceMoverDto> topGainers;
    private List<PortfolioPriceMoverDto> topLosers;
}
