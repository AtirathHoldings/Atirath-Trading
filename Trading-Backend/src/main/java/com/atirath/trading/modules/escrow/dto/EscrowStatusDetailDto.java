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
public class EscrowStatusDetailDto {
    private String escrowId;
    private String tradeId;
    private String companyName;
    private String companySymbol;
    private String type; // Buy, Sell
    private Long quantity;
    private BigDecimal pricePerShare;
    private BigDecimal totalAmount;
    private String buyerName;
    private String buyerMemberId;
    private String sellerName;
    private String sellerMemberId;
    private String status; // Completed
    private String statusMessage;
    private String createdOn;
    private String completedOn;
    private String lastUpdated;

    private List<HorizontalStepDto> horizontalSteps;
    private EscrowSummaryCardDto escrowSummary;
    private List<ProgressStepDetailDto> progressSteps;
    private List<QuickActionDto> quickActions;

    // Tab contents
    private TransactionHistoryTabDto transactionHistory;
    private PaymentDetailsTabDto paymentDetails;
    private ShareDetailsTabDto shareDetails;
    private SettlementDetailsTabDto settlementDetails;
    private List<EscrowDocumentTabDto> documents;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HorizontalStepDto {
        private int stepNumber;
        private String title;
        private String status;
        private String timestamp;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EscrowSummaryCardDto {
        private String escrowId;
        private String tradeId;
        private String companyName;
        private String type;
        private Long quantity;
        private BigDecimal pricePerShare;
        private BigDecimal totalEscrowAmount;
        private String buyer;
        private String seller;
        private String status;
        private String createdOn;
        private String completedOn;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProgressStepDetailDto {
        private int stepNumber;
        private String title;
        private String description;
        private String timestamp;
        private boolean completed;
        private String viewActionUrl;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuickActionDto {
        private String id;
        private String title;
        private String actionType;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransactionHistoryTabDto {
        private List<HistoryItemDto> events;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HistoryItemDto {
        private String event;
        private String timestamp;
        private String actor;
        private String status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentDetailsTabDto {
        private String paymentId;
        private String paymentMethod;
        private BigDecimal amount;
        private String gatewayRef;
        private String status;
        private String paidAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShareDetailsTabDto {
        private String lockId;
        private String transferId;
        private String isin;
        private Long shares;
        private String fromDemat;
        private String toDemat;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SettlementDetailsTabDto {
        private String settlementId;
        private BigDecimal payoutAmount;
        private String payoutBank;
        private String referenceNo;
        private String completedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EscrowDocumentTabDto {
        private String name;
        private String size;
        private String date;
        private String downloadUrl;
    }
}
