package com.atirath.trading.modules.negotiation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NegotiationNotificationItemDto {
    private Long id;
    private String type; // COUNTER_OFFER, AGREEMENT, ACTION_REQUIRED, NEW_OFFER, REMINDER, EXPIRED, SYSTEM
    private String icon; // DOUBLE_ARROW, HANDSHAKE, CLOCK, DOCUMENT, BELL, CROSS, INFO
    private boolean unread;
    private String title;
    private String timeAgo;
    private String timestamp;
    private String message;

    private String negotiationNo;
    private String companyName;
    private String companySymbol;
    private String counterpartyName;
    private String counterpartyMemberId;
    private String status;

    private NotificationDetailSummaryDto negotiationSummary;
    private List<String> additionalInformation;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotificationDetailSummaryDto {
        private String negotiationId;
        private String companyName;
        private String companySymbol;
        private String counterparty;
        private String yourLastOffer;
        private String counterpartyOffer;
        private String difference;
        private String status;
        private String viewDetailsUrl;
        private boolean canAccept;
        private boolean canCounter;
    }
}
