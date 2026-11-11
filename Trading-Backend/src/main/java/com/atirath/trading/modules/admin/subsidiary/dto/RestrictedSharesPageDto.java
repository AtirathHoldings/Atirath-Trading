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
public class RestrictedSharesPageDto {

    private AdminUserHeaderDto adminUser;
    private String pageTitle;
    private String pageSubtitle;
    private String selectedCompany;
    private String dateRange;

    // 4 Top KPI Cards
    private List<KpiCardDto> kpis;

    // Tabs
    private List<String> tabs;
    private String activeTab;

    // Filter Dropdowns
    private List<String> entities;
    private List<String> shareClasses;
    private List<String> restrictionTypes;
    private List<String> statuses;

    // Table items
    private List<RestrictedShareItemDto> shares;
    private int totalRestrictedSharesCount;
    private int currentPage;
    private int totalPages;
    private int pageSize;

    // Bottom Chart 1: Restricted Shares by Type (Donut)
    private RestrictedSharesByTypeDonutDto restrictedSharesByType;

    // Bottom Chart 2: Restriction Expiry Timeline (Stacked Bar)
    private List<RestrictionExpiryBarDto> restrictionExpiryTimeline;

    // Selected Drawer item
    private RestrictedShareDetailDto selectedShareDetail;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RestrictedSharesByTypeDonutDto {
        private String totalRestrictedShares;
        private List<RestrictedShareTypeSliceDto> slices;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RestrictedShareTypeSliceDto {
        private String label;
        private double percentage;
        private String count;
        private String color;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RestrictionExpiryBarDto {
        private String month;
        private int temporaryRestrictions;
        private int permanentRestrictions;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RestrictedShareItemDto {
        private String id;
        private int number;
        private String shareId;
        private String shareholder;
        private String entity;
        private String shareClass;
        private String noOfShares;
        private String restrictionType;
        private String restrictionTypeTheme; // pink, yellow, orange, red, blue
        private String fromDate;
        private String toDate;
        private String status;
        private String statusTheme; // red, orange, blue
    }
}
