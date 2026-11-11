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
public class SuspiciousTransactionDetailDto {

    private String id;
    private String transactionId;
    private String alertType;
    private String dateTime;
    private String status;
    private String statusTheme;
    private String riskLevel;
    private String riskTheme;

    // Tabs inside drawer
    private List<String> tabs;
    private String activeTab;

    // Transaction Information
    private String transactionType;
    private String entity;
    private String investorOrShareholder;
    private String fromParty;
    private String toParty;
    private String noOfShares;
    private String amount;
    private String pricePerShare;
    private String transactionDate;

    // Alert Information
    private String detectionSource;
    private String detectionRule;
    private String detectedOn;
    private String confidenceScore;

    // Additional Notes
    private String additionalNotes;
}
