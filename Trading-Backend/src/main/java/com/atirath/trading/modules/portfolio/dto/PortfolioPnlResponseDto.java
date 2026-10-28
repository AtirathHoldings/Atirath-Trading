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
public class PortfolioPnlResponseDto {
    private String dateRange; // "01 Jan 2025 - 18 Sep 2025"
    private List<PortfolioSummaryKpiDto> summaryKpis;
    
    // Trend chart
    private List<PortfolioPnlTrendPointDto> pnlTrend;
    
    // Side Summary
    private PortfolioPnlSummaryDto pnlSummary;
    
    // Tabs & Company table
    private String activeTab; // "Company-wise P&L", "Realized P&L", "Unrealized P&L", "Sector-wise", "Monthly Summary"
    private List<PortfolioCompanyPnlRowDto> companyPnl;
    
    // Key Insights
    private List<String> keyInsights;
    
    private Integer totalCompaniesCount;
}
