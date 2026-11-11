package com.atirath.trading.modules.admin.subsidiary.operations.dto;

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
public class EscrowMonitoringPageDto {

    private AdminUserHeaderDto adminUser;
    private String pageTitle;
    private String pageSubtitle;
    private String selectedCompany;
    private String dateRange;

    // 5 Top KPI Cards
    private List<KpiCardDto> kpis;

    // Tabs
    private List<String> tabs;
    private String activeTab;

    // Filter selections
    private List<String> companies;
    private List<String> shareClasses;
    private List<String> statuses;

    // Top Chart 1: Escrow Balance Trend (Line Chart Apr-Sep)
    private List<EscrowTrendMonthDto> balanceTrend;

    // Top Chart 2: Escrow Status Distribution (Donut Chart)
    private EscrowStatusDonutDto statusDistribution;

    // Table items
    private List<EscrowAccountItemDto> accounts;
    private int totalAccounts;
    private int currentPage;
    private int totalPages;
    private int pageSize;

    // Bottom Row Summary Cards
    private List<UpcomingReleaseRowDto> upcomingReleases;
    private List<EscrowExceptionRowDto> escrowExceptions;

    // Selected Drawer item
    private EscrowDetailDto selectedEscrowDetail;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EscrowTrendMonthDto {
        private String month;
        private double balanceCr;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EscrowStatusDonutDto {
        private String totalBalance;
        private List<EscrowStatusSliceDto> slices;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EscrowStatusSliceDto {
        private String label;
        private double percentage;
        private String amountCr;
        private String color;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EscrowAccountItemDto {
        private String id;
        private int number;
        private String escrowId;
        private String company;
        private String tradeId;
        private String investor;
        private String shareClass;
        private String amount;
        private String status; // Available, On Hold, Pending Release, Disputed
        private String statusTheme; // green, orange, red
        private String createdOn;
        private String expectedRelease;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpcomingReleaseRowDto {
        private String escrowId;
        private String company;
        private String amount;
        private String expectedRelease;
        private int daysLeft;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EscrowExceptionRowDto {
        private String escrowId;
        private String type; // Dispute, Hold
        private String typeTheme; // red, orange
        private String amount;
        private String reason;
        private int daysSince;
    }
}
