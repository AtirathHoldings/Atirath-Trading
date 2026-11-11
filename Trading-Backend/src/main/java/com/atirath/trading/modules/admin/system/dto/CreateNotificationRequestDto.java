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
public class CreateNotificationRequestDto {
    private String notificationName;
    private String eventTrigger;
    private String category; // Trade, Settlement, User, Compliance, System
    private List<String> channels; // In-App, Email, SMS
    private String subject;
    private String message;
    private String recipients;
    private Boolean sendEmail;
    private Boolean sendInApp;
    private Boolean sendSms;
    private Boolean retryOnFailure;
    private Integer maximumRetries;
}
