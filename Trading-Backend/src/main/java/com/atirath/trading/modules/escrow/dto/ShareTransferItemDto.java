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
public class ShareTransferItemDto {
    private String transferId;
    private String tradeId;
    private String companyName;
    private String fromSeller;
    private String toBuyer;
    private Long quantity;
    private String transferDate;
    private String status; // Completed, In Progress, Pending

    private ShareTransferDetailDto detail;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShareTransferDetailDto {
        private String transferId;
        private String tradeId;
        private String companyName;
        private String fromSeller;
        private String toBuyer;
        private String isin;
        private Long quantity;
        private String transferDateTime;
        private String settlementRef;
        private String status;
        private List<TransferProgressStepDto> transferProgress;
        private List<String> documents;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransferProgressStepDto {
        private String title;
        private String description;
        private String timestamp;
        private boolean completed;
    }
}
