package com.atirath.trading.modules.negotiation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NegotiationNotificationsPageDto {
    private NegotiationNotificationSummaryDto summary;
    private int unreadCount;
    private Map<String, Integer> tabCounts;
    private List<NegotiationNotificationItemDto> notifications;
    private NegotiationNotificationItemDto selectedNotification;
}
