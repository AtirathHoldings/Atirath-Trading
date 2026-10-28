package com.atirath.trading.modules.notification.dto;

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
    private Long id;
    private String title;
    private String subtitle;
    private String badge;
    private String badgeColor;
    private String description;
    
    // Key/Value details
    private String transactionId;
    private String company;
    private String orderType;
    private String quantity;
    private String price;
    private String totalAmount;
    private String executionTime;
    private String status;
    private String statusColor;

    // Related Links
    private List<RelatedLinkDto> relatedLinks;
    
    private Boolean isRead;
}
