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
public class LiveTradeDto {
    private String tradeNo;
    private String symbol;
    private String companyName;
    private BigDecimal tradePrice;
    private Long quantity;
    private BigDecimal tradeValue;
    private OffsetDateTime executedAt;
    private Boolean isBuy;
}
