package com.atirath.trading.modules.negotiation.dto;

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
public class TradeAgreementDto {
    private String agreementNo;
    private String negotiationNo;
    private String status; // Agreement Ready, Agreement Reached, Escrow Pending
    private String initiatedOn;

    private FinalAgreedTermsDto finalAgreedTerms;
    private AgreementDocumentDto document;
    private List<ConfirmationItemDto> confirmations;
    private List<NextStepDto> nextSteps;
    private AgreementPartiesDto parties;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FinalAgreedTermsDto {
        private BigDecimal sharePrice;
        private Long quantity;
        private BigDecimal totalTradeValue;
        private String paymentMode;
        private String settlementTimeline;
        private String orderType;
        private String otherTerms;
        private String agreementDate;
        private String validTill;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AgreementDocumentDto {
        private String documentName;
        private String fileSize;
        private String generatedAt;
        private String downloadUrl;
        private String notice;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConfirmationItemDto {
        private String key;
        private String text;
        private boolean confirmed;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NextStepDto {
        private int stepNumber;
        private String title;
        private String statusText;
        private boolean completed;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AgreementPartiesDto {
        private NegotiationDetailDto.PartyDto buyer;
        private NegotiationDetailDto.PartyDto seller;
    }
}
