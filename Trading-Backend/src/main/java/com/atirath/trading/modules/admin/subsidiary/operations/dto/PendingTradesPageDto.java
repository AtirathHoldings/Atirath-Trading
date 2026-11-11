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
public class PendingTradesPageDto {

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
    private List<String> tradeTypes;
    private List<String> statuses;

    // Table items
    private List<PendingTradeItemDto> trades;
    private int totalTrades;
    private int currentPage;
    private int totalPages;
    private int pageSize;

    // Bottom Chart 1: Pending Trades by Status Donut
    private PendingTradesStatusDonutDto pendingTradesByStatus;

    // Bottom Chart 2: Pending Trades Trend (Monthly Stacked Bar)
    private List<PendingTradesTrendMonthDto> pendingTradesTrend;

    // Selected Drawer item
    private TradeDetailDto selectedTradeDetail;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PendingTradesStatusDonutDto {
        private int totalPendingTrades;
        private List<TradeStatusSliceDto> slices;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TradeStatusSliceDto {
        private String label;
        private double percentage;
        private int count;
        private String color;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PendingTradesTrendMonthDto {
        private String month;
        private int approval;
        private int verification;
        private int settlement;
        private int overdue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PendingTradeItemDto {
        private String id;
        private int number;
        private String tradeId;
        private String tradeDate;
        private String company;
        private String shareClass;
        private String investor;
        private String type; // Buy, Sell, Transfer
        private String qtyOrShares;
        private String value;
        private String status; // Approval, Verification, Settlement, Overdue
        private String statusTheme; // orange, blue, green, red
        private String pendingSince;
    }
}
