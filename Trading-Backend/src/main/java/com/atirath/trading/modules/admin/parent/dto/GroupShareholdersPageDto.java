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
public class GroupShareholdersPageDto {
    private AdminUserHeaderDto adminUser;
    private String pageTitle;
    private String pageSubtitle;
    private String dateFilter;

    // 5 Top KPI Cards
    private List<KpiCardDto> kpis;

    // Charts
    private ShareholderCategoryDistributionDto categoryDistribution;
    private List<TopShareholderBarDto> topShareholders;

    // Shareholder List Table
    private List<ShareholderItemDto> shareholders;
    private int totalShareholders;
    private int currentPage;
    private int totalPages;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShareholderCategoryDistributionDto {
        private String totalShareholders;
        private List<CategorySliceDto> slices;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategorySliceDto {
        private String category;
        private Double percentage;
        private Integer count;
        private String color;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopShareholderBarDto {
        private String shareholderName;
        private String category;
        private Double percentage;
        private String color;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShareholderItemDto {
        private String id;
        private int number;
        private String shareholderName;
        private String initials;
        private String type; // "Corporate", "Individual", "Trust", "Various"
        private String category; // "Promoter", "Individual", "Institutional", "Foreign", "Various"
        private String categoryTheme; // "orange", "blue", "indigo", "red", "grey"
        private String country; // "India", "Singapore", "USA", "UK"
        private String countryFlag;
        private String noOfShares;
        private Double shareholdingPercent;
        private String totalValue;
        private String kycStatus; // "Verified"
        private String status; // "Active"
    }
}
