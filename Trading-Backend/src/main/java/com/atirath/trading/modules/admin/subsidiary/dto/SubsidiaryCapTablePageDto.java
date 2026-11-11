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
public class SubsidiaryCapTablePageDto {

    private AdminUserHeaderDto adminUser;
    private String pageTitle;
    private String pageSubtitle;
    private String selectedCompany;
    private String companyStatus;

    // 4 KPIs
    private List<KpiCardDto> kpis;

    // Tabs: Current Cap Table, Fully Diluted, Investment Rounds, Dilution Analysis, Historical Cap Table
    private List<String> tabs;
    private String activeTab;

    // Cap Table Rows
    private List<CapTableRowDto> capTableRows;
    private int totalShareholders;
    private int currentPage;
    private int totalPages;

    // Right Column Widgets:
    // 1. Shareholding Summary Donut
    private CapTableSummaryDonutDto shareholdingSummary;

    // 2. Key Information Card
    private CapTableKeyInfoDto keyInformation;

    // Bottom Widgets:
    // 1. Ownership by Share Class Donut
    private CapTableOwnershipShareClassDonutDto ownershipByShareClass;

    // 2. Ownership by Category Donut
    private CapTableOwnershipCategoryDonutDto ownershipByCategory;

    // 3. Investment vs Ownership Chart
    private List<InvestmentVsOwnershipPointDto> investmentVsOwnership;

    // 4. Recent Transactions Table
    private List<CapTableTransactionDto> recentTransactions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CapTableRowDto {
        private String id;
        private int number;
        private String shareholderName;
        private String initials;
        private String category; // Institutional, Individual, Foreign
        private String categoryTheme;
        private String shareClass; // Equity (ES), Preference (PS), Series A (A1), ESOP (ESOP)
        private String noOfShares;
        private double ownershipPercentage;
        private double investedAmountCr;
        private double avgCost;
        private String status; // Active
        private String statusTheme; // green
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CapTableSummaryDonutDto {
        private String totalShares;
        private List<CapTableSummarySliceDto> slices;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CapTableSummarySliceDto {
        private String name;
        private double percentage;
        private String color;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CapTableKeyInfoDto {
        private String authorizedCapital;
        private String issuedCapital;
        private String paidUpCapital;
        private String totalShares;
        private String faceValue;
        private int shareClasses;
        private int shareholders;
        private String lastUpdated;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CapTableOwnershipShareClassDonutDto {
        private String totalShares;
        private List<OwnershipShareClassSliceDto> slices;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OwnershipShareClassSliceDto {
        private String shareClass;
        private double percentage;
        private String color;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CapTableOwnershipCategoryDonutDto {
        private String totalShares;
        private List<OwnershipCategorySliceDto> slices;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OwnershipCategorySliceDto {
        private String category;
        private double percentage;
        private String color;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InvestmentVsOwnershipPointDto {
        private String investor;
        private double investmentCr;
        private double ownershipPercentage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CapTableTransactionDto {
        private String date;
        private String type; // Issuance, Transfer
        private String typeTheme; // green, orange
        private String sharesFormatted;
        private double price;
    }
}
