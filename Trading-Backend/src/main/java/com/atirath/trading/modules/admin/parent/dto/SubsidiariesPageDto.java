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
public class SubsidiariesPageDto {
    private AdminUserHeaderDto adminUser;
    private String pageTitle;
    private String pageSubtitle;

    // 5 Top KPI Cards
    private List<KpiCardDto> kpis;

    // Subsidiaries List
    private List<SubsidiarySummaryDto> subsidiaries;
    private int totalCompanies;
    private int currentPage;
    private int totalPages;

    // Bottom Widgets
    private SectorDistributionDto sectorDistribution;
    private List<SubsidiaryUpdateItemDto> recentUpdates;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SectorDistributionDto {
        private String totalTradeValue;
        private List<SectorShareSliceDto> slices;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SectorShareSliceDto {
        private String sector;
        private Double valueCr;
        private Double percentage;
        private String color;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubsidiaryUpdateItemDto {
        private String time;
        private String description;
    }
}
