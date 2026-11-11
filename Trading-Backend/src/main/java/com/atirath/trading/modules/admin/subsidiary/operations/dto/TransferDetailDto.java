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
public class TransferDetailDto {

    private String id;
    private String transferId;
    private String transferTypeTitle; // Share Transfer
    private String status;
    private String statusTheme;
    private String createdInfo;

    // Tabs inside drawer
    private List<String> tabs;
    private String activeTab;

    // Transfer Information
    private String type;
    private String fromParty;
    private String toParty;
    private String shares;
    private String shareClass;
    private String pricePerShare;
    private String totalValue;
    private String transferDate;
    private String proposedSettlement;

    // Approval Workflow
    private String initiatedDate;
    private String complianceReviewStatus; // Pending
    private String riskApprovalStatus; // Not Started
    private String finalApprovalStatus; // Not Started
    private String settlementStatus; // Not Started

    // Additional Information
    private String reason;
    private String remarks;
    private String createdBy;
    private String createdOn;
}
