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
public class SettlementConfirmationDto {
    private String settlementId;
    private String escrowId;
    private String tradeId;
    private List<SettlementProgressStepDto> progressSteps;
    private SettlementDetailsCardDto settlementDetails;
    private SettlementSuccessCardDto successDetails;
    private List<SettlementDocumentDto> documents;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SettlementProgressStepDto {
        private int stepNumber;
        private String title;
        private String status; // Completed
        private String timestamp;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SettlementDetailsCardDto {
        private String settlementId;
        private String escrowId;
        private String tradeId;
        private String companyName;
        private String companySymbol;
        private String fromSeller;
        private String toBuyer;
        private String isin;
        private Long quantity;
        private BigDecimal pricePerShare;
        private BigDecimal totalAmount;
        private String transferDate;
        private String settlementDate;
        private String settlementStatus;
        private String settlementReferenceNo;
        private String clearingCorporation;
        private String paymentReferenceNo;
        private String createdOn;
        private String lastUpdated;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SettlementSuccessCardDto {
        private String headline;
        private String subheadline;
        private String tagline;
        private PartyDeliverySummaryDto buyerDelivery;
        private PartyPaymentSummaryDto sellerPayment;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PartyDeliverySummaryDto {
        private String label;
        private String memberName;
        private String memberId;
        private Long sharesCount;
        private String sharesFormatted;
        private String statusMessage;
        private String timestamp;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PartyPaymentSummaryDto {
        private String label;
        private String memberName;
        private String memberId;
        private BigDecimal amount;
        private String amountFormatted;
        private String statusMessage;
        private String timestamp;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SettlementDocumentDto {
        private String id;
        private String title;
        private String fileFormat;
        private String fileSize;
        private String downloadUrl;
    }
}
