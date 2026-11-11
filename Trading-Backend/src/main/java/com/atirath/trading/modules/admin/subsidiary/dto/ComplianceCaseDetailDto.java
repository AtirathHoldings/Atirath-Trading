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
public class ComplianceCaseDetailDto {

    private String id;
    private String caseId;
    private String title;
    private String status;
    private String statusTheme;
    private String createdInfo;

    // Tabs inside drawer
    private List<String> tabs;
    private String activeTab;

    // Case Information
    private String entity;
    private String caseType;
    private String riskLevel;
    private String riskTheme;
    private String assignedTo;
    private String createdOn;
    private String dueDate;
    private String priority;
    private String priorityTheme;
    private String source;
    private String description;

    // Investigation Summary
    private String investigationOwner;
    private String startDate;
    private String targetCompletion;
    private String currentStage;
    private String findings;

    // Risk Indicators
    private List<String> riskIndicators;
}
