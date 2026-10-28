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
public class PortfolioTransactionResponseDto {
    private String dateRange; // "01 Jan 2025 - 18 Sep 2025"
    private List<PortfolioSummaryKpiDto> summaryKpis;
    private List<PortfolioTransactionItemDto> transactions;
    
    // Breakdown widgets
    private List<PortfolioDistributionItemDto> transactionTypeBreakdown;
    private List<PortfolioDistributionItemDto> paymentModeDistribution;
    
    private Integer totalTransactionsCount;
    private Integer currentPage;
    private Integer totalPages;
    private Integer pageSize;
}
