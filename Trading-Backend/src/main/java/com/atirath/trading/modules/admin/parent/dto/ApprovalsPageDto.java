package com.atirath.trading.modules.admin.parent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalsPageDto {
    private AdminUserHeaderDto adminUser;
    private String pageTitle;
    private String pageSubtitle;
    private String dateRange; // "01 Sep 2026 - 16 Sep 2026"

    // 5 Top KPI Cards
    private List<KpiCardDto> kpis;

    // Tabs
    private List<TabCountDto> tabs;
    private String activeTab;

    // Approvals List Table
    private List<ApprovalItemDto> approvals;
    private int totalRequests;
    private int currentPage;
    private int totalPages;

    // Selected Drawer Approval Detail (Default to first or APR-2026-1042)
    private ApprovalDetailDto selectedApprovalDetail;

    // Bottom Widgets
    private ApprovalsByTypeDonutDto approvalsByType;
    private ApprovalTrendChartDto approvalTrend;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TabCountDto {
        private String key; // "All", "Pending", "Under Review", "Approved", "Rejected"
        private String label;
        private Integer count;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApprovalItemDto {
        private String id;
        private int number;
        private String requestId; // "APR-2026-1042"
        private String dateTime;
        private String approvalType; // "Share Transfer", "Trade Settlement", etc.
        private String company;
        private String requestedBy;
        private String amountOrDetails;
        private String priority; // "High", "Medium", "Critical", "Low"
        private String priorityTheme; // "red", "yellow", "purple", "green"
        private String status; // "Pending", "Under Review", "Approved", "Rejected"
        private String statusTheme; // "yellow", "blue", "green", "red"
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApprovalsByTypeDonutDto {
        private String totalRequests;
        private List<TypeSliceDto> slices;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TypeSliceDto {
        private String type;
        private Integer count;
        private Double percentage;
        private String color;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApprovalTrendChartDto {
        private String filterRange; // "Last 6 Months"
        private List<MonthlyApprovalTrendDto> monthlyData;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyApprovalTrendDto {
        private String month;
        private Integer pending;
        private Integer approved;
        private Integer rejected;
    }
}
