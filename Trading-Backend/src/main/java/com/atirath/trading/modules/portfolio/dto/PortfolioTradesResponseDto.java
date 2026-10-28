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
public class PortfolioTradesResponseDto {
    private List<PortfolioSummaryKpiDto> summaryKpis;
    private List<PortfolioTradeItemDto> trades;
    private PortfolioTradeDetailDto selectedTradeDetail;
    private Integer totalTradesCount;
    private Integer currentPage;
    private Integer totalPages;
    private Integer pageSize;
}
