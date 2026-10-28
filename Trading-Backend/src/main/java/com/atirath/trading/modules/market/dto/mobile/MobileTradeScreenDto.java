package com.atirath.trading.modules.market.dto.mobile;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Lightweight DTO tailored specifically for Flutter trade_screen.dart.
 * Contains only the data needed for quick trading, user balance, and live order book.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MobileTradeScreenDto {
    private Long companyId;
    private String symbol;
    private String shortCode;
    private String companyName;
    private String sector;
    private BigDecimal currentPrice;
    private BigDecimal priceChange;
    private BigDecimal priceChangePercent;
    private Boolean isGainer;
    private BigDecimal openPrice;
    private BigDecimal dayHigh;
    private BigDecimal dayLow;
    private BigDecimal high52;
    private BigDecimal low52;
    private BigDecimal bestBid;
    private BigDecimal bestAsk;
    private BigDecimal spread;

    // User Balance context
    private BigDecimal availableCash;    // From user escrow wallet (e.g. ₹ 12,48,500.00)
    private Long availableShares;        // From user holdings for this stock (e.g. 3,500 shares)

    // Quick quantity chips for mobile UI: [100, 500, 1000, 5000, 10000]
    private List<Long> quickQuantities;

    // 5-depth order book
    private List<MobileOrderBookRowDto> bids;
    private List<MobileOrderBookRowDto> asks;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MobileOrderBookRowDto {
        private BigDecimal price;
        private Long quantity;
        private BigDecimal total;
    }
}
