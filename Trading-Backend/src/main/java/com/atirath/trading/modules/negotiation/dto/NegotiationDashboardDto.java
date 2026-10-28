package com.atirath.trading.modules.negotiation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NegotiationDashboardDto {
    private NegotiationSummaryDto summary;
    private NegotiationInsightDto insights;
    private NegotiationStatusBreakdownDto statusBreakdown;
    private List<QuickActionDto> quickActions;
    private PaginatedNegotiationsDto negotiations;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuickActionDto {
        private String id;
        private String title;
        private String description;
        private String actionType;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaginatedNegotiationsDto {
        private long totalElements;
        private int totalPages;
        private int currentPage;
        private int pageSize;
        private List<NegotiationListItemDto> content;
    }
}
