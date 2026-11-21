package com.atirath.trading.modules.negotiation.dto.mobile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MobileNegotiationNotificationCardDto {
    private Long notificationId;
    private String type; // OFFER_RECEIVED, COUNTER_RECEIVED, OFFER_ACCEPTED, OFFER_REJECTED, NEW_MESSAGE, AGREEMENT_READY, CALL_REQUESTED
    private String title;
    private String body;
    private String negotiationNo;
    private String companySymbol;
    private boolean isRead;
    private String timeAgo;
    private String timestamp;
    private String targetScreen; // NEGOTIATION_DETAILS, COUNTER_OFFER, TRADE_AGREEMENT
}
