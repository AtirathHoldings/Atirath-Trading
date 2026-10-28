package com.atirath.trading.modules.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationListItemDto {
    private Long id;
    private String title;
    private String message;
    private String type;             // Trade, Negotiation, Escrow, Share Transfer, Settlement, Portfolio, System, Compliance
    private String priority;         // Success, Action Required, Important, Information
    private String priorityBadgeColor; // green, orange, red, blue
    private String timestamp;        // e.g. "18 Sep 2025 11:28 AM"
    private String rawDate;
    private String actionLabel;      // View Trade, Review Offer, View Escrow, View Transfer, View Settlement, View Details, View Report, Update Now
    private String actionUrl;
    private Boolean isRead;
    private String icon;             // trade, negotiation, escrow, transfer, settlement, alert, report, system, compliance, dividend
    private String iconBgColor;
    private Map<String, Object> metadata;
}
