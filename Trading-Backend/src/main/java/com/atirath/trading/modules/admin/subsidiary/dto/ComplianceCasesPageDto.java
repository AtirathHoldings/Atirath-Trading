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
public class ComplianceCasesPageDto {

    private AdminUserHeaderDto adminUser;
    private String pageTitle;
    private String pageSubtitle;
    private String selectedCompany;
    private String dateRange;

    // 4 Top KPI Cards
    private List<KpiCardDto> kpis;

    // Tabs
    private List<String> tabs;
    private String activeTab;

    // Filter Dropdowns
    private List<String> entities;
    private List<String> caseTypes;
    private List<String> riskLevels;
    private List<String> statuses;

    // Table items
    private List<ComplianceCaseItemDto> cases;
    private int totalCases;
    private int currentPage;
    private int totalPages;
    private int pageSize;

    // Bottom Chart 1: Cases by Type (Donut)
    private CasesByTypeDonutDto casesByType;

    // Bottom Chart 2: Case Status Trend (Stacked Bar)
    private List<CaseStatusTrendDto> caseStatusTrend;

    // Selected Drawer item
    private ComplianceCaseDetailDto selectedCaseDetail;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CasesByTypeDonutDto {
        private int totalCases;
        private List<CaseTypeSliceDto> slices;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CaseTypeSliceDto {
        private String label;
        private double percentage;
        private int count;
        private String color;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CaseStatusTrendDto {
        private String month;
        private int open;
        private int inProgress;
        private int underReview;
        private int resolved;
        private int closed;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComplianceCaseItemDto {
        private String id;
        private int number;
        private String caseId;
        private String title;
        private String entity;
        private String caseType;
        private String riskLevel;
        private String riskTheme; // red, orange, green, critical
        private String status;
        private String statusTheme; // red, blue, orange, green
        private String assignedTo;
        private String createdOn;
        private String dueDate;
    }
}
