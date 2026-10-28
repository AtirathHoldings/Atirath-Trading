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
public class CompanySummaryDto {
    private Long companyId;
    private String companyCode;
    private String companyName;
    private String symbol;
    private String sector;
    private String description;
    private String status;
    private String headquarters;
    private String websiteUrl;
    private String logoUrl;
    private Long issuedShares;
    private Long availableShares;

    // Real-time market metrics from latest price history / trades
    private BigDecimal currentPrice;
    private BigDecimal openPrice;
    private BigDecimal highPrice;
    private BigDecimal lowPrice;
    private BigDecimal closePrice;
    private BigDecimal previousClose;
    private BigDecimal priceChange;
    private BigDecimal priceChangePercentage;
    private Long volumeShares;
    private BigDecimal turnoverValue;
    private OffsetDateTime lastPriceTime;

    private Boolean isWatchlisted;
}
