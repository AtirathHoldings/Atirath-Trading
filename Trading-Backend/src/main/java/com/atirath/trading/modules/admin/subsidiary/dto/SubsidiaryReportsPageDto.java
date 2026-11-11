package com.atirath.trading.modules.admin.subsidiary.dto;

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
public class SubsidiaryReportsPageDto {

    private AdminUserHeaderDto adminUser;
    private String pageTitle;
    private String pageSubtitle;
    private String selectedCompany;
    private String dateRange;

    // 4 KPIs
    private List<KpiCardDto> kpis;

    // Tabs: Overview, Finance, Shareholding, Transactions, Projects, Plants, Employees, Audit & Compliance
    private List<String> tabs;
    private String activeTab;

    // Top Charts:
    // 1. Subsidiary-wise Investment Bar Chart
    private List<SubsidiaryInvestmentBarDto> subsidiaryWiseInvestment;

    // 2. Shareholding Distribution Donut
    private ShareholdingDistributionDonutDto shareholdingDistribution;

    // 3. Company Performance (Consolidated) Combo Chart
    private List<PerformancePointDto> companyPerformance;

    // Bottom Subtabs: Available Reports, Scheduled Reports
    private List<String> subTabs;
    private String activeSubTab;

    // Table: Available Reports (8 reports)
    private List<ReportItemDto> availableReports;
    private int totalReports;

    // Right Side: Quick Actions & Popular Reports
    private List<String> popularReports;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubsidiaryInvestmentBarDto {
        private String subsidiary;
        private double investmentCr;
        private String color;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShareholdingDistributionDonutDto {
        private String totalShareCapital;
        private List<ShareholdingDistSliceDto> slices;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShareholdingDistSliceDto {
        private String holderType;
        private double percentage;
        private String color;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PerformancePointDto {
        private String month;
        private double revenueCr;
        private double ebitdaCr;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReportItemDto {
        private String id;
        private int number;
        private String reportName;
        private String category; // Finance, Shareholding, Transactions, Projects, Plants, HR
        private String description;
        private String lastGenerated;
        private List<String> formats; // PDF, Excel
        private String downloadPdfUrl;
        private String downloadExcelUrl;
    }
}
