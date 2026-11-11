package com.atirath.trading.modules.admin.system.dto;

import com.atirath.trading.modules.admin.parent.dto.AdminUserHeaderDto;
import com.atirath.trading.modules.admin.parent.dto.KpiCardDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalWorkflowPageDto {
    private AdminUserHeaderDto adminUser;
    private String pageTitle;
    private String pageSubtitle;
    private String selectedCompany;

    // 5 KPIs
    private List<KpiCardDto> kpis;

    // Tabs
    private List<String> tabs;
    private String activeTab;

    // Filter Options
    private List<String> modules;
    private List<String> statuses;
    private List<String> approvalLevels;

    // Table rows
    private List<WorkflowItemDto> workflows;
    private Integer totalWorkflows;
    private Integer currentPage;
    private Integer totalPages;
    private Integer pageSize;

    // Bottom Visualizations
    private WorkflowsByModuleDonutDto workflowsByModule;
    private List<WorkflowStatusBarDto> workflowsByStatus;
    private List<ApprovalLevelsBarDto> approvalLevelsDistribution;

    // Preselected Drawer Detail
    private WorkflowDetailDto selectedWorkflowDetail;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkflowItemDto {
        private String id;
        private Integer number;
        private String workflowName;
        private String module;
        private String description;
        private String approvalLevels;
        private String status;
        private String statusTheme; // green, red
        private String lastUpdated;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkflowsByModuleDonutDto {
        private String totalWorkflows;
        private List<ModuleSliceDto> slices;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ModuleSliceDto {
        private String module;
        private Double percentage;
        private Integer count;
        private String color;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class WorkflowStatusBarDto {
        private String status;
        private Double percentage;
        private Integer count;
        private String color;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ApprovalLevelsBarDto {
        private String level;
        private Double percentage;
        private Integer count;
        private String color;
    }
}
