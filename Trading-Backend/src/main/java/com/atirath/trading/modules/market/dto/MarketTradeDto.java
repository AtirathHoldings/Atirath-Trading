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
public class MarketTradeDto {
    private Long tradeId;
    private String tradeNo;
    private Long companyId;
    private String symbol;
    private String companyName;
    private Long quantity;
    private BigDecimal tradePrice;
    private BigDecimal tradeValue;
    private OffsetDateTime executedAt;
    private String status;
    private String orderSide;
    private Boolean isBuy;
}
