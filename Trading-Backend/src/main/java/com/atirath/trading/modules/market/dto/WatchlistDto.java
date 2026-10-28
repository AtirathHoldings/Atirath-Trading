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
public class WatchlistDto {
    private Long watchlistId;
    private Long companyId;
    private String companyCode;
    private String companyName;
    private String symbol;
    private String sector;
    private String logoUrl;
    private BigDecimal currentPrice;
    private BigDecimal priceChange;
    private BigDecimal priceChangePercentage;
    private OffsetDateTime addedAt;
}
