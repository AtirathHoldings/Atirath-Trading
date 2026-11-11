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
public class ParentDashboardDto {
    private AdminUserHeaderDto adminUser;
    private String pageTitle;
    private String pageSubtitle;
    private String dateFilter;

    // 5 Top KPI Cards
    private List<KpiCardDto> kpis;

    // Charts & Performance
    private List<GroupCompanyPerformanceDto> groupCompanyPerformance;
    private List<TradeActivityTrendDto> tradeActivityTrends;
    private MarketOverviewDto marketOverview;

    // Tables & Widgets
    private List<TopTradedSecurityDto> topTradedSecurities;
    private List<PendingApprovalItemDto> pendingApprovals;
    private List<RecentTradeActivityDto> recentTradeActivities;
    private List<RecentMemberActivityDto> recentMemberActivities;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GroupCompanyPerformanceDto {
        private String company;
        private Double volumeCr;
        private String color;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TradeActivityTrendDto {
        private String date;
        private Double buyVolume;
        private Double sellVolume;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MarketOverviewDto {
        private String totalVolume;
        private List<MarketShareSliceDto> slices;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MarketShareSliceDto {
        private String company;
        private Double percentage;
        private String color;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopTradedSecurityDto {
        private String security;
        private String company;
        private String volume;
        private String ltp;
        private String change;
        private String trend; // "positive", "negative"
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PendingApprovalItemDto {
        private String title;
        private Integer count;
        private String icon;
        private String theme;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentTradeActivityDto {
        private String time;
        private String security;
        private String company;
        private String type; // "Buy", "Sell"
        private String quantity;
        private String price;
        private String value;
        private String status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentMemberActivityDto {
        private String time;
        private String memberOrInvestor;
        private String activity;
        private String status;
        private String statusType; // "Pending", "Approved"
    }
}
