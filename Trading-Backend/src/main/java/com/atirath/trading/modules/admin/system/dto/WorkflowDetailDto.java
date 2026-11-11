package com.atirath.trading.modules.admin.system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowDetailDto {
    private String id;
    private String workflowName;
    private String module;
    private String description;
    private String status;
    private String statusTheme;

    private List<String> tabs;
    private String activeTab;

    // Basic Information
    private String createdOn;
    private String createdBy;
    private String lastUpdated;
    private String updatedBy;

    // Approval Configuration
    private Integer totalApprovalLevels;
    private String approvalType;
    private String approvalRequired;
    private String allowDelegation;
    private String autoEscalation;
    private String escalationTime;

    // Applicable Conditions (checklist items)
    private List<WorkflowConditionDto> conditions;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class WorkflowConditionDto {
        private String conditionText;
        private Boolean enabled;
    }
}
