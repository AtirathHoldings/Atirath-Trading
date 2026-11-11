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
public class SettlementMonitoringPageDto {

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

    // Top Chart 1: Settlement Volume Trend (Line Chart Apr-Sep)
    private List<SettlementTrendMonthDto> volumeTrend;

    // Top Chart 2: Settlement Status Distribution (Donut Chart)
    private SettlementStatusDonutDto statusDistribution;

    // Table items
    private List<SettlementItemDto> settlements;
    private int totalSettlements;
    private int currentPage;
    private int totalPages;
    private int pageSize;

    // Bottom Row Summary Cards
    private List<UpcomingSettlementRowDto> upcomingSettlements;
    private List<FailedSettlementRowDto> failedSettlements;

    // Selected Drawer item
    private SettlementDetailDto selectedSettlementDetail;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SettlementTrendMonthDto {
        private String month;
        private double settledValueCr;
        private int noOfSettlements;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SettlementStatusDonutDto {
        private int totalSettlements;
        private List<SettlementStatusSliceDto> slices;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SettlementStatusSliceDto {
        private String label;
        private double percentage;
        private int count;
        private String color;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SettlementItemDto {
        private String id;
        private int number;
        private String settlementId;
        private String tradeId;
        private String company;
        private String investor;
        private String shareClass;
        private String amount;
        private String settlementDate;
        private String status; // Settled, Pending, Failed, On Hold, Under Review
        private String statusTheme; // green, orange, red, purple, blue
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpcomingSettlementRowDto {
        private String settlementId;
        private String tradeId;
        private String company;
        private String amount;
        private String expectedDate;
        private int daysLeft;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FailedSettlementRowDto {
        private String settlementId;
        private String tradeId;
        private String company;
        private String amount;
        private String reason;
        private String date;
    }
}
