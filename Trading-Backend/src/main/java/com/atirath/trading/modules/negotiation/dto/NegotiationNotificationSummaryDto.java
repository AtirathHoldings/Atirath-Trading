package com.atirath.trading.modules.negotiation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NegotiationNotificationSummaryDto {
    private int allNotifications;
    private int newOffers;
    private int counterOffers;
    private int agreements;
    private int actionRequired;
    private int systemAlerts;
}
