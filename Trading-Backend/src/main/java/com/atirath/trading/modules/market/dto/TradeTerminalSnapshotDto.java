package com.atirath.trading.modules.market.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeTerminalSnapshotDto {

    @Schema(description = "Company and ticker details")
    private TerminalCompanyHeaderDto company;

    @Schema(description = "5-Level Live Order Book")
    private TerminalOrderBookDto orderBook;

    @Schema(description = "Tape of recent executed trades")
    private List<TerminalRecentTradeDto> recentTrades;

    @Schema(description = "Market Depth chart distribution")
    private TerminalMarketDepthDto marketDepth;

    @Schema(description = "Key fundamental and intraday statistics")
    private TerminalKeyStatisticsDto keyStatistics;

    @Schema(description = "Automated market sentiment / insight", example = "ATG-H has seen a 16.67% increase today with strong buying interest. Keep track of order book and recent trades for better entry points.")
    private String marketInsight;

    @Schema(description = "Current authenticated user's wallet available balance in INR", example = "1248500.00")
    private BigDecimal userAvailableBalance;

    @Schema(description = "Current authenticated user's available shares of this company", example = "3500")
    private Long userAvailableShares;

    @Schema(description = "Whether internal market is open", example = "true")
    @Builder.Default
    private Boolean isMarketOpen = true;

    // --- Inner DTOs ---

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TerminalCompanyHeaderDto {
        private Long companyId;
        private String companyCode;
        private String companyName;
        private String symbol;
        private String sector;
        private String status;
        private BigDecimal currentPrice;
        private BigDecimal priceChange;
        private BigDecimal priceChangePercentage;
        private BigDecimal openPrice;
        private BigDecimal dayHigh;
        private BigDecimal dayLow;
        private BigDecimal fiftyTwoWeekHigh;
        private BigDecimal fiftyTwoWeekLow;
        private Long volumeShares;
        private String marketCapDisplay;
        private BigDecimal marketCapValue;
        private BigDecimal bestAsk;
        private BigDecimal bestBid;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TerminalOrderBookDto {
        private List<OrderBookLevelDetailDto> asks;
        private List<OrderBookLevelDetailDto> bids;
        private BigDecimal spread;
        private Long totalBidQuantity;
        private Long totalAskQuantity;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderBookLevelDetailDto {
        private BigDecimal price;
        private Long quantity;
        private BigDecimal totalValue;
        private Integer ordersCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TerminalRecentTradeDto {
        private String time;
        private BigDecimal price;
        private Long quantity;
        private BigDecimal value;
        private String type; // BUY or SELL
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TerminalMarketDepthDto {
        private List<DepthBinDto> buyOrders;
        private List<DepthBinDto> sellOrders;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DepthBinDto {
        private BigDecimal price;
        private Long cumulativeQuantity;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TerminalKeyStatisticsDto {
        private BigDecimal currentPrice;
        private BigDecimal changeToday;
        private BigDecimal changePercentage;
        private BigDecimal openPrice;
        private BigDecimal dayHigh;
        private BigDecimal dayLow;
        private BigDecimal fiftyTwoWeekHigh;
        private BigDecimal fiftyTwoWeekLow;
        private Long volumeShares;
        private String turnoverDisplay; // e.g. "6.1 Cr"
        private BigDecimal turnoverValue;
        private BigDecimal peRatio;
        private BigDecimal dividendYield;
    }
}
