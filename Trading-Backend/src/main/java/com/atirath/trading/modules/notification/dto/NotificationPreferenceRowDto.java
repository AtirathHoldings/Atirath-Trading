package com.atirath.trading.modules.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreferenceRowDto {
    private String id;           // trade, negotiation, escrow, shareTransfer, settlement, portfolio, priceAlerts, corporateActions, kyc, accountSecurity, system
    private String title;        // Trade Notifications, etc.
    private String description;  // Buy/Sell orders, trade execution, order updates, etc.
    private String icon;
    private Boolean inApp;
    private Boolean email;
    private Boolean sms;
    private Boolean push;
    private String frequency;    // Instant, Daily Summary, Weekly Summary, As it happens
}
