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
public class GroupShareCapitalPageDto {
    private AdminUserHeaderDto adminUser;
    private String pageTitle;
    private String pageSubtitle;
    private String dateFilter;

    // 4 Top KPI Cards
    private List<KpiCardDto> kpis;

    // Charts
    private ShareCapitalStructureDto shareCapitalStructure;
    private CapitalComparisonChartDto capitalComparisonChart;

    // Table
    private List<SubsidiaryShareCapitalItemDto> shareCapitalDetails;

    // Bottom Widgets
    private List<ShareCapitalActivityItemDto> recentActivities;
    private List<KeyInsightItemDto> keyInsights;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShareCapitalStructureDto {
        private String totalAuthorizedCapital;
        private List<CapitalStructureSliceDto> slices;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CapitalStructureSliceDto {
        private String company;
        private Double percentage;
        private String color;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CapitalComparisonChartDto {
        private Double authorizedCapitalCr;
        private Double issuedCapitalCr;
        private Double paidUpCapitalCr;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubsidiaryShareCapitalItemDto {
        private String id;
        private int number;
        private String companyName;
        private String initials;
        private String sector;
        private Double authorizedCapitalCr;
        private Double issuedCapitalCr;
        private Double paidUpCapitalCr;
        private String numberOfShares;
        private Integer faceValue;
        private String status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShareCapitalActivityItemDto {
        private String dateTime;
        private String company;
        private String activity;
        private String activityTheme; // "green", "teal", "orange", "pink", "purple"
        private String description;
        private String performedBy;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KeyInsightItemDto {
        private String metric;
        private String label;
        private String icon;
        private String theme;
    }
}
