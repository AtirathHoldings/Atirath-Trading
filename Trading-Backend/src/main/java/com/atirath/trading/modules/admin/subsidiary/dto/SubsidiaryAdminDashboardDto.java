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
public class SubsidiaryAdminDashboardDto {

    private AdminUserHeaderDto adminUser;
    private String pageTitle;
    private String pageSubtitle;
    private String dateRange;
    private String selectedSubsidiary;

    // 5 KPIs
    private List<KpiCardDto> kpis;

    // Table: Subsidiary Performance Overview
    private List<SubsidiaryPerformanceItemDto> performanceOverview;

    // Widget: Projects Status
    private ProjectsStatusDonutDto projectsStatus;

    // Widget: Compliance Status
    private List<ComplianceCategoryItemDto> complianceStatus;

    // Chart: Investment vs Revenue Trend
    private List<MonthlyInvestmentRevenueDto> investmentVsRevenueTrend;

    // Widget: Employee Distribution
    private EmployeeDistributionDonutDto employeeDistribution;

    // Widget: Recent Activities
    private List<SubsidiaryActivityItemDto> recentActivities;

    // Widget: Upcoming Milestones
    private List<SubsidiaryMilestoneItemDto> upcomingMilestones;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubsidiaryPerformanceItemDto {
        private int number;
        private String subsidiaryName;
        private String sector;
        private int activeProjects;
        private int employees;
        private double investmentCr;
        private double revenueCr;
        private String status; // "Active", "Planning"
        private String statusTheme; // "green", "orange"
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectsStatusDonutDto {
        private int totalProjects;
        private List<ProjectStatusSliceDto> slices;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectStatusSliceDto {
        private String status;
        private int count;
        private double percentage;
        private String color;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComplianceCategoryItemDto {
        private String category;
        private int percentage;
        private String color;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyInvestmentRevenueDto {
        private String month;
        private double investment;
        private double revenue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmployeeDistributionDonutDto {
        private int totalEmployees;
        private List<EmployeeDepartmentSliceDto> slices;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmployeeDepartmentSliceDto {
        private String department;
        private int count;
        private double percentage;
        private String color;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubsidiaryActivityItemDto {
        private String dateTime;
        private String subsidiary;
        private String activity;
        private String user;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubsidiaryMilestoneItemDto {
        private String date;
        private String subsidiary;
        private String milestone;
        private String status; // "Due Soon", "On Track", "At Risk"
        private String statusTheme; // "orange", "green", "red"
    }
}
