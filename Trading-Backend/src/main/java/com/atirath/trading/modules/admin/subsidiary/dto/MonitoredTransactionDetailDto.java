package com.atirath.trading.modules.admin.subsidiary.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonitoredTransactionDetailDto {

    private String id;
    private String transactionId;
    private String type;
    private String status;
    private String statusTheme;
    private String dateTime;

    // Tabs inside drawer
    private List<String> tabs;
    private String activeTab;

    // Transaction Information
    private String entity;
    private String investorOrShareholder;
    private String fromParty;
    private String toParty;
    private String noOfShares;
    private String amount;
    private String pricePerShare;
    private String transactionDate;
    private String riskLevel;
    private String riskTheme;

    // Screening Summary
    private String pepCheck; // Clear
    private String sanctionsCheck; // Match Found
    private String adverseMedia; // Clear
    private String shareholdingLimit; // Exceeds Limit
    private String unusualPattern; // Detected

    // Risk Notes
    private List<String> riskNotes;
}
