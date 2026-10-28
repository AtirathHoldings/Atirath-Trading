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
public class ShareLockStatusPageDto {
    private ShareLockMetricsDto summary;
    private Map<String, Integer> tabCounts;
    private PaginatedShareLockDto locks;
    private ShareLockItemDto.ShareLockDetailDto selectedLock;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShareLockMetricsDto {
        private int totalRequests;
        private int activeLocks;
        private double activeLocksPct;
        private int released;
        private double releasedPct;
        private int expiringSoon;
        private double expiringSoonPct;
        private int failed;
        private double failedPct;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaginatedShareLockDto {
        private int totalElements;
        private int totalPages;
        private int currentPage;
        private int pageSize;
        private List<ShareLockItemDto> content;
    }
}
