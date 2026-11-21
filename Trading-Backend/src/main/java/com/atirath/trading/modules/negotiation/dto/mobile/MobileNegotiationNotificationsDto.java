package com.atirath.trading.modules.negotiation.dto.mobile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Mobile Screen 6: Negotiation Notifications
 * Delivers unread badge counters, category tabs, and notifications cards feed.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MobileNegotiationNotificationsDto {
    private int unreadCount;
    private int totalCount;
    private String currentCategory;
    private List<MobileNegotiationNotificationCardDto> notifications;
}
