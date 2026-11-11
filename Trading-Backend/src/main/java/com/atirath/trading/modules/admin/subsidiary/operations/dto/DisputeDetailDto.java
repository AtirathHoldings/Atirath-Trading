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
public class DisputeDetailDto {
    private String id;
    private String disputeId;
    private String disputeTypeTitle;
    private String raisedInfo;
    private String status;
    private String statusTheme;

    private List<String> tabs;
    private String activeTab;

    // Dispute Information
    private String type;
    private String relatedTradeId;
    private String investor;
    private String counterparty;
    private String amount;
    private String priority;
    private String priorityTheme;
    private String raisedOn;
    private String targetResolution;
    private String assignedTo;
    private String description;

    // Current Stage
    private String stage;
    private String daysElapsed;
    private String daysRemaining;

    // Related Information
    private String relatedTransactions;
    private String relatedSettlements;
    private String relatedPayments;
}
