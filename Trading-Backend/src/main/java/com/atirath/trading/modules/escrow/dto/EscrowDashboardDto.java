package com.atirath.trading.modules.escrow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EscrowDashboardDto {
    private EscrowSummaryMetricsDto summary;
    private EscrowStatusBreakdownDto statusBreakdown;
    private SettlementTrendDto settlementTrend;
    private EscrowValueTrendDto escrowValueTrend;
    private Map<String, Integer> tabCounts;
    private PaginatedEscrowsDto transactions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaginatedEscrowsDto {
        private int totalElements;
        private int totalPages;
        private int currentPage;
        private int pageSize;
        private List<EscrowListItemDto> content;
    }
}
