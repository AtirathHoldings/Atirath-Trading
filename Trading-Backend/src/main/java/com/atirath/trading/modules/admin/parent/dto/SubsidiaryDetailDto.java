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
public class SubsidiaryDetailDto {
    private AdminUserHeaderDto adminUser;
    
    // Header Info
    private String id;
    private String companyName;
    private String initials;
    private String status;
    private String subIndustry;
    private String cin;
    private String incorporationDate;
    private String headquarters;
    private String sector;
    private int totalMembers;
    private String totalTradeValue;

    // Available Tabs
    private List<String> availableTabs;
    private String activeTab;

    // Overview KPIs
    private List<KpiCardDto> overviewKpis;

    // Company Information Card
    private CompanyInfoDto companyInfo;

    // Key Metrics (Last 6 Months)
    private KeyMetricsDto keyMetrics;

    // Recent Activities
    private List<SubsidiaryActivityDto> recentActivities;

    // Compliance Status
    private List<ComplianceItemDto> complianceStatus;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompanyInfoDto {
        private String companyName;
        private String cin;
        private String incorporationDate;
        private String sector;
        private String industry;
        private String registeredAddress;
        private String headquarters;
        private String pan;
        private String gstNumber;
        private String status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KeyMetricsDto {
        private String filterRange; // "Last 6 Months"
        private MetricChartDto tradeVolume;
        private MetricChartDto settlementValue;
        private MetricChartDto activeMembers;
        private MetricChartDto newMembers;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MetricChartDto {
        private String title;
        private String currentValue;
        private List<MonthlyDataPointDto> monthlyData;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyDataPointDto {
        private String month;
        private Double value;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubsidiaryActivityDto {
        private String dateTime;
        private String activity;
        private String performedBy;
        private String status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComplianceItemDto {
        private String title;
        private String description;
        private String status; // "Compliant"
    }
}
