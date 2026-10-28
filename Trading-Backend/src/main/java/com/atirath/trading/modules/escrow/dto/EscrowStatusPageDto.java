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
public class EscrowStatusPageDto {
    private EscrowStatusMetricsDto summary;
    private Map<String, Integer> tabCounts;
    private PaginatedEscrowStatusDto escrows;
    private EscrowStatusItemDto.EscrowStatusItemDetailDto selectedEscrow;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EscrowStatusMetricsDto {
        private int totalEscrows;
        private int completed;
        private double completedPct;
        private int inProgress;
        private double inProgressPct;
        private int pending;
        private double pendingPct;
        private int failed;
        private double failedPct;
        private int cancelled;
        private double cancelledPct;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaginatedEscrowStatusDto {
        private int totalElements;
        private int totalPages;
        private int currentPage;
        private int pageSize;
        private List<EscrowStatusItemDto> content;
    }
}
