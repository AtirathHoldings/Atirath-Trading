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
public class NegotiationDetailDto {
    private String negotiationNo;
    private String status;
    private String initiatedOn;
    private String expiresAt;
    private String expiresIn;

    private PartyDto buyer;
    private PartyDto seller;
    private CompanyShareDetailsDto companyShareDetails;

    private List<NegotiationTermRowDto> negotiationTerms;
    private List<TimelineEventDto> timeline;
    private AvailableActionsDto availableActions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PartyDto {
        private boolean isCurrentUser;
        private String companyName;
        private String companySymbol;
        private boolean verified;
        private String memberId;
        private String memberName;
        private String email;
        private String phone;
        private Long currentHoldings;
        private Long quantity; // Interested Quantity or Offered Quantity
        private String partyRole; // BUYER, SELLER
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompanyShareDetailsDto {
        private String companyName;
        private String symbol;
        private String companyType;
        private String sector;
        private String isin;
        private BigDecimal faceValue;
        private BigDecimal lastTradedPrice;
        private BigDecimal fiftyTwoWeekHigh;
        private BigDecimal fiftyTwoWeekLow;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NegotiationTermRowDto {
        private String parameter;
        private String buyerOffer;
        private String sellerOffer;
        private String currentProposal;
        private String difference;
        private boolean highlighted;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimelineEventDto {
        private int stepNumber;
        private String title;
        private String description;
        private String timestamp;
        private String senderCode; // RM, SV, etc.
        private String senderName;
        private String status; // COMPLETED, ACTIVE, PENDING
        private String price;
        private String shares;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AvailableActionsDto {
        private boolean canAccept;
        private boolean canCounter;
        private boolean canReject;
        private boolean canRequestCall;
    }
}
