package com.atirath.trading.modules.market.dto.mobile;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Lightweight DTO specifically for Flutter Sell Shares screen/sheet.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MobileSellFormDto {
    private Long companyId;
    private String symbol;
    private String shortCode;
    private String companyName;
    private String sector;
    private BigDecimal currentPrice;
    private BigDecimal priceChange;
    private BigDecimal priceChangePercent;
    private Boolean isPositive;
    private Long sharesOwned;
    private Long sharesAvailable;
    private Long sharesLocked;
    private BigDecimal avgCostPrice;
    private BigDecimal totalInvested;
    private BigDecimal currentValue;
    private Long defaultQuantity;
    private BigDecimal defaultPrice;
    private List<Long> quickQuantities;
    private String settlementCycle;
}
