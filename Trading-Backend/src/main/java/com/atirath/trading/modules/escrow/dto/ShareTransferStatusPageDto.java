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
public class ShareTransferStatusPageDto {
    private ShareTransferMetricsDto summary;
    private Map<String, Integer> tabCounts;
    private PaginatedShareTransferDto transfers;
    private ShareTransferItemDto.ShareTransferDetailDto selectedTransfer;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShareTransferMetricsDto {
        private int totalTransfers;
        private int completed;
        private double completedPct;
        private int inProgress;
        private double inProgressPct;
        private int pending;
        private double pendingPct;
        private int failed;
        private double failedPct;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaginatedShareTransferDto {
        private int totalElements;
        private int totalPages;
        private int currentPage;
        private int pageSize;
        private List<ShareTransferItemDto> content;
    }
}
