package com.atirath.trading.modules.portfolio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Real per-user payload for the Pending Actions screen (/pending-actions).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberPendingActionsDto {

    private Long userId;
    private String memberId;
    private String userName;
    private String asOfFormatted;

    // 4 KPI Summary Cards
    private int totalPending;
    private int highPriorityCount;
    private int mediumPriorityCount;
    private int normalPriorityCount;

    // Category tab badge counts: All, KYC, Orders, Negotiations, Escrow, Account
    private Map<String, Integer> categoryCounts;

    private String activeCategory;
    private String sortBy;

    // Filtered & sorted table rows
    private List<PendingActionRowDto> items;

    // Right sidebar: top 4 earliest deadlines
    private List<DeadlineItemDto> upcomingDeadlines;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PendingActionRowDto {
        private String id;
        private String title;
        private String desc;
        private String category; // "KYC" | "Orders" | "Negotiations" | "Escrow" | "Account"
        private String relatedTitle;
        private String relatedSub;
        private String dueDateIso;
        private String dueDate;
        private String daysLeft;
        private boolean urgent;
        private String priority; // "High" | "Medium" | "Normal"
        private String status;
        private String statusTone; // "pending" | "awaiting" | "action" | "unread"
        private String actionText;
        private String actionType; // "primary" | "secondary"
        private String route;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeadlineItemDto {
        private String id;
        private String dueDay;
        private String dueMonth;
        private String title;
        private String relatedTitle;
        private String daysLeft;
        private boolean urgent;
        private String route;
    }
}

