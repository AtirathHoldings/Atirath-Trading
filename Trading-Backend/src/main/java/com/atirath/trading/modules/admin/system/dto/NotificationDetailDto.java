package com.atirath.trading.modules.admin.system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDetailDto {
    private String id;
    private String notificationName;
    private String notificationTitle;
    private String description;
    private String status;
    private String statusTheme;

    private List<String> tabs;
    private String activeTab;

    // Basic Information
    private String eventTrigger;
    private String category;
    private String channels;
    private String createdOn;
    private String createdBy;
    private String lastUpdated;
    private String updatedBy;

    // Notification Content
    private String subject;
    private String message;

    // Delivery Settings
    private String recipients;
    private String sendEmail;
    private String sendInApp;
    private String sendSms;
    private String retryOnFailure;
    private Integer maximumRetries;
}
