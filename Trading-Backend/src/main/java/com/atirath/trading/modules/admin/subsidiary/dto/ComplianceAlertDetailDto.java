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
public class ComplianceAlertDetailDto {

    private String id;
    private String alertId;
    private String alertType;
    private String status;
    private String statusTheme;
    private String detectedOn;

    // Tabs inside drawer
    private List<String> tabs;
    private String activeTab;

    // Alert Information
    private String riskLevel;
    private String riskTheme;
    private String entity;
    private String detectionSource;
    private String description;

    // Involved Parties
    private String investor;
    private String fromParty;
    private String toParty;
    private String shares;
    private String amount;
    private String transactionDate;
    private String referenceNo;

    // Risk Indicators
    private List<String> riskIndicators;
}
