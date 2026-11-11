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
public class SubsidiaryShareholdersPageDto {

    private AdminUserHeaderDto adminUser;
    private String pageTitle;
    private String pageSubtitle;
    private String selectedCompany;
    private String companyStatus;

    // 4 KPIs
    private List<KpiCardDto> kpis;

    // Tabs: All Shareholders (12), Institutional (5), Individual (6), Foreign (1)
    private List<String> tabs;
    private String activeTab;

    // Table: Shareholders
    private List<ShareholderItemDto> shareholders;
    private int totalShareholders;
    private int currentPage;
    private int totalPages;

    // Right Drawer Detail
    private ShareholderDetailDto selectedShareholderDetail;

    // Bottom Widget 1: Shareholding Pattern Donut
    private ShareholdingPatternDonutDto shareholdingPattern;

    // Bottom Widget 2: Shareholders by Category Bar Chart
    private List<CategoryCountBarDto> shareholdersByCategory;

    // Bottom Widget 3: Top Shareholders Horizontal Bars
    private List<TopShareholderBarDto> topShareholders;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShareholderItemDto {
        private String id;
        private int number;
        private String shareholderName;
        private String initials;
        private String category; // Institutional, Individual, Foreign
        private String categoryTheme; // blue, purple, orange
        private String type; // VC Fund, PE Fund, Strategic, Corporate, Promoter, Individual
        private String noOfShares;
        private double ownershipPercentage;
        private double investmentCr;
        private String status; // Active
        private String statusTheme; // green
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShareholdingPatternDonutDto {
        private String totalShares;
        private List<ShareholderSliceDto> slices;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShareholderSliceDto {
        private String name;
        private double percentage;
        private String color;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryCountBarDto {
        private String category;
        private int count;
        private String color;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopShareholderBarDto {
        private String name;
        private double percentage;
        private String color;
    }
}
