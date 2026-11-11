package com.atirath.trading.modules.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Real per-user payload for the web Notifications screen (/notifications).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberNotificationsDto {

    private Long userId;
    private String memberId;
    private String userName;
    private String asOfFormatted;

    private String activeCategory;
    private long totalCount;
    private long unreadCount;

    // Exact tab badge counts: All, Unread, Trade, Orders, Negotiations, Escrow, Account, System
    private Map<String, Long> categoryCounts;

    // Right sidebar summary breakdown
    private SummaryCountsDto summary;

    // Filtered list of notifications
    private List<NotificationItemDto> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SummaryCountsDto {
        private long totalNotifications;
        private long unread;
        private long tradeRelated;
        private long orderUpdates;
        private long negotiations;
        private long escrow;
        private long companyUpdates;
        private long system;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotificationItemDto {
        private String id; // e.g. "db-14" or "neg-8"
        private String title;
        private String desc;
        private String category; // "Orders" | "Account" | "Negotiations" | "Escrow" | "Trade" | "System"
        private String timeAgo;
        private String timestampFormatted;
        private boolean unread;
        private String iconType; // "order" | "kyc" | "negotiation" | "escrow" | "announcement" | "trade-up" | "trade-down" | "transfer" | "system"
        private String route;
        private boolean persisted;
    }
}

