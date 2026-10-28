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
public class PortfolioOrdersResponseDto {
    private List<PortfolioSummaryKpiDto> summaryKpis;
    private String activeTab; // "ALL", "BUY", "SELL", "OPEN", "EXECUTED", "CANCELLED", "REJECTED"
    private List<PortfolioOrderItemDto> orders;
    private PortfolioOrderDetailDto selectedOrderDetail;
    private Integer totalOrdersCount;
    private Integer currentPage;
    private Integer totalPages;
    private Integer pageSize;
}
