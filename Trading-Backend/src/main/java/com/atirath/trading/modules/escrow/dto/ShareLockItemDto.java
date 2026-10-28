package com.atirath.trading.modules.escrow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShareLockItemDto {
    private String lockId;
    private String tradeId;
    private String companyName;
    private String isin;
    private Long quantity;
    private String lockDate;
    private String unlockDate;
    private String status; // Locked, Released, Expiring Soon

    private ShareLockDetailDto detail;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShareLockDetailDto {
        private String lockId;
        private String tradeId;
        private String companyName;
        private String isin;
        private Long quantity;
        private String lockDateTime;
        private String unlockCondition;
        private String status;
        private List<LockProgressStepDto> lockProgress;
        private List<LockActivityDto> recentActivity;
        private List<String> documents;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LockProgressStepDto {
        private String title;
        private String status; // COMPLETED, IN_PROGRESS, PENDING
        private String timestamp;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LockActivityDto {
        private String timestamp;
        private String description;
        private String dotColor;
    }
}
