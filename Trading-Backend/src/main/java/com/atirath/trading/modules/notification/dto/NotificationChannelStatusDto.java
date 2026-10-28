package com.atirath.trading.modules.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationChannelStatusDto {
    private String channel;      // EMAIL, SMS, PUSH, IN_APP
    private String title;        // Email Notifications, SMS Notifications, Push Notifications, In-App Notifications
    private String description;  // Receive notifications via email, etc.
    private Boolean isActive;    // true
    private String icon;
}
