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
public class GroupTradeMonitoringPageDto {
    private AdminUserHeaderDto adminUser;
    private String pageTitle;
    private String pageSubtitle;
    private String dateRange; // "01 Sep 2026 - 16 Sep 2026"

    // 5 Top KPI Cards
    private List<KpiCardDto> kpis;

    // Charts
    private TradeValueVolumeTrendDto trendChart;
    private MarketActivityGroupPageDto.SectorTradeDistributionDto sectorDistribution;

    // Table
    private List<TradeMonitoringItemDto> recentTrades;
    private int totalTrades;
    private int currentPage;
    private int totalPages;

    // Bottom Widgets
    private List<UnusualTradeActivityItemDto> unusualActivities;
    private List<MonitoringInsightItemDto> insights;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TradeValueVolumeTrendDto {
        private String filterRange; // "Last 6 Months"
        private List<MonthlyMonitoringPointDto> monthlyTrends;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyMonitoringPointDto {
        private String month;
        private Double tradeValueCr;
        private Integer numberOfTrades;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TradeMonitoringItemDto {
        private String id;
        private int number;
        private String tradeId; // "TRD-100482"
        private String dateTime;
        private String buyer;
        private String seller;
        private String company;
        private String security;
        private String quantity;
        private String price;
        private String tradeValue;
        private String type; // "Buy", "Sell"
        private String status; // "Settled", "Pending", "Failed"
        private String settlementDate; // "18 Sep 2026" or "-"
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UnusualTradeActivityItemDto {
        private String dateTime;
        private String tradeId;
        private String company;
        private String alertType; // "High Value Trade", "Large Quantity", "Price Variation", "Multiple Trades"
        private String alertTheme; // "yellow", "pink", "blue", "red"
        private String details;
        private String status; // "Under Review", "Investigating", "Resolved"
        private String statusTheme;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonitoringInsightItemDto {
        private String description;
        private String icon;
        private String theme;
    }
}
