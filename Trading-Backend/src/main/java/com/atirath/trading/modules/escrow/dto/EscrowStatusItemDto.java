package com.atirath.trading.modules.escrow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EscrowStatusItemDto {
    private String escrowId;
    private String tradeId;
    private String companyName;
    private String type; // Buy, Sell
    private Long quantity;
    private BigDecimal amount;
    private String status; // Completed, In Progress, Pending, Failed, Cancelled
    private String updatedOn;

    // Right-panel details when selected
    private EscrowStatusItemDetailDto detail;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EscrowStatusItemDetailDto {
        private String escrowId;
        private String tradeId;
        private String companyName;
        private String type;
        private Long quantity;
        private BigDecimal pricePerShare;
        private BigDecimal totalAmount;
        private String createdOn;
        private String lastUpdated;
        private String status;
        private List<ProgressEventDto> progress;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProgressEventDto {
        private String title;
        private String timestamp;
        private boolean completed;
    }
}
