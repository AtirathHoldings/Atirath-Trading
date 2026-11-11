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
public class SubsidiaryShareClassesPageDto {

    private AdminUserHeaderDto adminUser;
    private String pageTitle;
    private String pageSubtitle;
    private String selectedCompany;
    private String companyStatus;

    // 4 KPIs
    private List<KpiCardDto> kpis;

    // Tabs: All Share Classes (4), Equity (2), Preference (1), Other (1)
    private List<String> tabs;
    private String activeTab;

    // Table: Share Classes
    private List<ShareClassItemDto> shareClasses;
    private int totalClasses;

    // Right Drawer Detail
    private ShareClassDetailDto selectedShareClassDetail;

    // Bottom Widget 1: Share Class Distribution (Issued Shares)
    private ShareClassDistributionDonutDto shareClassDistribution;

    // Bottom Widget 2: Authorized vs Issued Shares
    private List<AuthorizedVsIssuedBarDto> authorizedVsIssued;

    // Bottom Widget 3: Investor Categories
    private InvestorCategoriesDonutDto investorCategories;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShareClassItemDto {
        private String id;
        private int number;
        private String className;
        private String classType; // Equity, Preference
        private String classTypeTheme; // blue, purple
        private double faceValue;
        private String issuedShares;
        private String authorizedShares;
        private String votingRights;
        private String dividendRights;
        private String status; // Active
        private String statusTheme; // green
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShareClassDistributionDonutDto {
        private String totalShares;
        private List<ShareClassDistributionSliceDto> slices;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShareClassDistributionSliceDto {
        private String shareClass;
        private double percentage;
        private String sharesCount;
        private String color;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthorizedVsIssuedBarDto {
        private String shareClass;
        private double issuedLakhs;
        private double authorizedLakhs;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InvestorCategoriesDonutDto {
        private String totalShares;
        private List<InvestorCategorySliceDto> slices;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InvestorCategorySliceDto {
        private String category;
        private double percentage;
        private String color;
    }
}
