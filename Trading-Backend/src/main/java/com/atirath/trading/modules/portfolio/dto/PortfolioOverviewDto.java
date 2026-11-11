package com.atirath.trading.modules.portfolio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Real per-user payload for the Portfolio Overview screen (/portfolio-overview).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioOverviewDto {

    private Long userId;
    private String memberId;
    private String userName;
    private String asOfFormatted;
    private String activePeriod;

    private BigDecimal totalPortfolioValue;
    private String formattedPortfolioValue;
    private String formattedPortfolioCompact;
    private BigDecimal portfolioChangePercentage;
    private String formattedPortfolioChangePercentage;
    private boolean portfolioChangePositive;

    private BigDecimal investedAmount;
    private String formattedInvestedAmount;

    private BigDecimal totalGainLoss;
    private String formattedTotalGainLoss;
    private BigDecimal gainLossPercentage;
    private String formattedGainLossPercentage;
    private boolean gainLossPositive;

    private int companiesInvestedCount;
    private int totalCompaniesCount;

    private List<TrendPointDto> valueTrend;
    private List<AllocationSliceDto> allocationByCompany;
    private List<AssetBreakdownItemDto> assetBreakdown;
    private List<ReturnSummaryItemDto> returnSummary;
    private List<MarketMoverItemDto> topGainers;
    private List<MarketMoverItemDto> topLosers;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrendPointDto {
        private String name;
        private BigDecimal value;
        private String formattedValue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AllocationSliceDto {
        private Long companyId;
        private String name;
        private String symbol;
        private BigDecimal value;
        private String formattedValue;
        private BigDecimal percentage;
        private String color;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssetBreakdownItemDto {
        private String label;
        private BigDecimal amount;
        private String formattedAmount;
        private BigDecimal percentage;
        private String fillClass;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReturnSummaryItemDto {
        private String label;
        private BigDecimal percentage;
        private String formattedPercentage;
        private boolean isPositive;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MarketMoverItemDto {
        private Long companyId;
        private String companyName;
        private String symbol;
        private BigDecimal currentPrice;
        private String formattedCurrentPrice;
        private BigDecimal changePercentage;
        private String formattedChangePercentage;
        private boolean isPositive;
    }
}

