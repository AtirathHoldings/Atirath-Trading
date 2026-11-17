package com.atirath.trading.modules.market.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderBookUpdateDto {
    private String symbol;
    private Long companyId;
    private BigDecimal currentPrice;
    private BigDecimal spread;
    private BigDecimal spreadPercentage;
    private Long totalBidQuantity;
    private Long totalAskQuantity;
    private List<OrderBookLevelDto> bids;
    private List<OrderBookLevelDto> asks;
    private OffsetDateTime timestamp;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderBookLevelDto {
        private BigDecimal price;
        private Long quantity;
        private Integer ordersCount;
    }
}
