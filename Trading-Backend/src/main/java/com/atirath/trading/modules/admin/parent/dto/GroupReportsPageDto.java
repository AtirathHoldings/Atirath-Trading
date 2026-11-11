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
public class GroupReportsPageDto {
    private AdminUserHeaderDto adminUser;
    private String pageTitle;
    private String pageSubtitle;
    private String dateRange; // "01 Apr 2026 - 16 Sep 2026"
    private String selectedCompany; // "All Group Companies"

    // 5 Top KPI Cards
    private List<KpiCardDto> kpis;

    // Charts
    private RevenueProfitInvestmentTrendDto financialTrend;
    private SegmentWiseInvestmentDonutDto segmentWiseInvestment;

    // Main Tables
    private List<CompanyPerformanceReportItemDto> companyPerformance;
    private RevenueContributionDonutDto revenueContribution;

    // Bottom Widgets
    private List<KeyFinancialMetricItemDto> keyFinancialMetrics;
    private List<EmployeeDistributionBarDto> employeeDistribution;
    private List<TopPerformingCompanyProfitDto> topPerformingCompanies;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RevenueProfitInvestmentTrendDto {
        private String filterRange; // "Last 6 Months"
        private List<MonthlyReportPointDto> monthlyTrends;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyReportPointDto {
        private String month;
        private Double revenueCr;
        private Double profitCr;
        private Double investmentCr;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SegmentWiseInvestmentDonutDto {
        private String totalInvestment;
        private List<SegmentSliceDto> slices;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SegmentSliceDto {
        private String segment;
        private Double percentage;
        private String valueFormatted;
        private String color;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompanyPerformanceReportItemDto {
        private int number;
        private String companyName;
        private String sector;
        private Double investmentCr;
        private Double revenueCr;
        private Double profitCr;
        private Integer employees;
        private String growthYoY;
        private String status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RevenueContributionDonutDto {
        private List<CompanyRevenueSliceDto> slices;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompanyRevenueSliceDto {
        private String company;
        private Double percentage;
        private String valueFormatted;
        private String color;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KeyFinancialMetricItemDto {
        private String metric;
        private String currentPeriod;
        private String previousPeriod;
        private String change;
        private String trend; // "positive"
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmployeeDistributionBarDto {
        private String company;
        private Integer count;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopPerformingCompanyProfitDto {
        private int rank;
        private String company;
        private Double profitCr;
        private String growthYoY;
    }
}
