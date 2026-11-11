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
public class GroupHoldingsPageDto {
    private AdminUserHeaderDto adminUser;
    private String pageTitle;
    private String pageSubtitle;
    private String dateFilter;

    // 4 Top KPI Cards
    private List<KpiCardDto> kpis;

    // Charts
    private List<CompanyHoldingBarDto> holdingsByCompany;
    private SectorHoldingsDistributionDto sectorHoldings;

    // Table
    private List<GroupHoldingItemDto> holdingsDetails;

    // Bottom Widgets
    private List<HoldingActivityItemDto> recentActivities;
    private List<HoldingInsightItemDto> keyInsights;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompanyHoldingBarDto {
        private String company;
        private Double valueCr;
        private String color;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SectorHoldingsDistributionDto {
        private String totalValue;
        private List<SectorSliceDto> slices;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SectorSliceDto {
        private String sector;
        private Double percentage;
        private String valueFormatted;
        private String color;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GroupHoldingItemDto {
        private String id;
        private int number;
        private String companyName;
        private String initials;
        private String sector;
        private String totalShares;
        private Double shareholdingPercent;
        private String totalValue;
        private String lastValuation;
        private String change24h;
        private String changeTrend; // "positive", "negative"
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HoldingActivityItemDto {
        private String dateTime;
        private String company;
        private String activity;
        private String activityTheme;
        private String details;
        private String performedBy;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HoldingInsightItemDto {
        private String title;
        private String description;
        private String icon;
        private String theme;
    }
}
