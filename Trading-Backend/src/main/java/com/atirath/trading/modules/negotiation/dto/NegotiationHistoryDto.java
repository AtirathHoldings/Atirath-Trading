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
public class NegotiationHistoryDto {
    private String negotiationNo;
    private String status;
    private String initiatedOn;
    private String lastActivity;
    private String expiresIn;
    private String dealType; // Buy, Sell
    private Long quantity;

    private NegotiationDetailDto.PartyDto buyer;
    private NegotiationDetailDto.PartyDto seller;

    private String currentOffer;
    private String sellerLastOffer;

    private List<HistoryItemDto> timeline;
    private LatestTermsComparisonDto latestTermsComparison;
    private NegotiationDetailDto.AvailableActionsDto actions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HistoryItemDto {
        private Long id;
        private String senderCode; // RM, SV
        private String senderName;
        private String senderMemberId;
        private String title; // Initial Offer from Seller, Counter Offer from You, etc.
        private String message;
        private BigDecimal price;
        private Long shares;
        private String timestamp;
        private boolean isCurrentUser;
        private String badge; // Awaiting Response, etc.
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LatestTermsComparisonDto {
        private BigDecimal yourOfferPrice;
        private BigDecimal sellerOfferPrice;
        private Long yourQuantity;
        private Long sellerQuantity;
        private BigDecimal yourOrderValue;
        private BigDecimal sellerOrderValue;
        private String priceDifference;
        private String percentageDifference;
    }
}
