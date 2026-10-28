package com.atirath.trading.modules.market.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketTickDto {
    private String symbol;
    private Long companyId;
    private String companyName;
    private BigDecimal currentPrice;
    private BigDecimal previousClose;
    private BigDecimal priceChange;
    private BigDecimal priceChangePercentage;
    private BigDecimal highPrice;
    private BigDecimal lowPrice;
    private Long volumeShares;
    private BigDecimal turnoverValue;
    private OffsetDateTime timestamp;
}
