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
public class TradeDetailDto {

    private String id;
    private String tradeId;
    private String orderTypeTitle; // Buy Order
    private String status;
    private String statusTheme;
    private String createdInfo;

    // Tabs inside drawer
    private List<String> tabs;
    private String activeTab;

    // Trade Information
    private String type;
    private String company;
    private String shareClass;
    private String investor;
    private String noOfShares;
    private String pricePerShare;
    private String totalValue;
    private String tradeDate;
    private String pendingSince;
    private String settlementDate;

    // Approvals
    private String complianceReviewStatus; // Pending
    private String riskApprovalStatus; // Not Started
    private String settlementApprovalStatus; // Not Started

    // Additional Information
    private String isin;
    private String orderSource;
    private String createdBy;
    private String createdOn;
    private String remarks;
}
