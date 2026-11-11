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
public class MarketActivityGroupPageDto {
    private AdminUserHeaderDto adminUser;
    private String pageTitle;
    private String pageSubtitle;
    private String dateFilter;

    // 5 Top KPI Cards
    private List<KpiCardDto> kpis;

    // Charts
    private TradeVolumeTrendChartDto tradeVolumeTrend;
    private SectorTradeDistributionDto sectorTradeDistribution;

    // Table
    private List<MarketActivityTradeDto> recentTrades;
    private int totalTrades;
    private int currentPage;
    private int totalPages;

    // Bottom Widgets
    private List<TopTradedCompanyBarDto> topTradedCompanies;
    private List<MarketHighlightItemDto> highlights;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TradeVolumeTrendChartDto {
        private String filterRange; // "Last 6 Months"
        private List<MonthlyTrendPointDto> monthlyTrends;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyTrendPointDto {
        private String month;
        private Double tradeVolumeCr;
        private Integer numberOfTrades;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SectorTradeDistributionDto {
        private String totalTradeValue;
        private List<SectorTradeSliceDto> slices;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SectorTradeSliceDto {
        private String sector;
        private Double percentage;
        private String valueFormatted;
        private String color;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MarketActivityTradeDto {
        private String id;
        private int number;
        private String dateTime;
        private String company;
        private String companyInitials;
        private String type; // "Buy", "Sell"
        private String security;
        private String quantity;
        private String price;
        private String tradeValue;
        private String buyer;
        private String seller;
        private String status; // "Settled", "Pending"
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopTradedCompanyBarDto {
        private int rank;
        private String company;
        private String initials;
        private String tradeValue;
        private Double valueCr;
        private String color;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MarketHighlightItemDto {
        private String description;
        private String icon;
        private String theme;
    }
}
