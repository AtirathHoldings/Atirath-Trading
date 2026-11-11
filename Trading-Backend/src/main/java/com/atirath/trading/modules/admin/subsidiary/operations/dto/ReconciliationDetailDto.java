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
public class ReconciliationDetailDto {
    private String id;
    private String reconciliationId;
    private String reconciliationTitle;
    private String createdInfo;
    private String status;
    private String statusTheme;

    private List<String> tabs;
    private String activeTab;

    // Reconciliation Information
    private String type;
    private String period;
    private String company;
    private String dataSources;
    private Integer totalRecords;
    private Integer matchedRecords;
    private Integer unmatchedRecords;
    private Integer underReview;
    private Integer exceptions;
    private String startedOn;
    private String completedOn;
    private String createdBy;
    private String remarks;
}
