package com.atirath.trading.modules.admin.subsidiary.operations.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupportTicketDetailDto {
    private String id;
    private String ticketId;
    private String subject;
    private String raisedInfo;
    private String status;
    private String statusTheme;

    private List<String> tabs;
    private String activeTab;

    // Ticket Information
    private String category;
    private String company;
    private String raisedBy;
    private String priority;
    private String priorityTheme;
    private String raisedOn;
    private String expectedResolution;
    private String assignedTo;
    private String description;

    // SLA Information
    private String slaTarget;
    private String timeElapsed;
    private String timeRemaining;
    private String slaStatusTheme;

    // Related Information
    private String relatedTrade;
    private String relatedSettlement;
    private String relatedTransfer;
}
