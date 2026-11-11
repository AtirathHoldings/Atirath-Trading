package com.atirath.trading.modules.admin.subsidiary.dto;

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
public class SubsidiaryApproversPageDto {

    private AdminUserHeaderDto adminUser;
    private String pageTitle;
    private String pageSubtitle;
    private String selectedCompany;
    private String companyStatus;

    // 4 KPIs
    private List<KpiCardDto> kpis;

    // Tabs: All Approvers (8), By Department (5), By Approval Type (5), Inactive (2)
    private List<String> tabs;
    private String activeTab;

    // Table: Company Approvers
    private List<CompanyApproverItemDto> approvers;
    private int totalApprovers;

    // Right Drawer Detail
    private CompanyApproverDetailDto selectedApproverDetail;

    // Bottom Widget 1: Approvers by Department Donut
    private ApproversByDepartmentDonutDto approversByDepartment;

    // Bottom Widget 2: Approvers by Approval Type Donut
    private ApproversByApprovalTypeDonutDto approversByApprovalType;

    // Bottom Widget 3: Approval Limit Distribution Bar Chart
    private List<ApprovalLimitBarDto> approvalLimitDistribution;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompanyApproverItemDto {
        private String id;
        private int number;
        private String approverName;
        private String initials;
        private String roleOrDesignation;
        private String department;
        private String approvalTypes;
        private String approvalLimit;
        private String status; // Active, Inactive
        private String statusTheme; // green, red
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApproversByDepartmentDonutDto {
        private int totalApprovers;
        private List<DepartmentApproverSliceDto> slices;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DepartmentApproverSliceDto {
        private String department;
        private int count;
        private double percentage;
        private String color;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApproversByApprovalTypeDonutDto {
        private int totalApprovers;
        private List<ApprovalTypeSliceDto> slices;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApprovalTypeSliceDto {
        private String approvalType;
        private int count;
        private double percentage;
        private String color;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApprovalLimitBarDto {
        private String range;
        private int count;
        private String color;
    }
}
