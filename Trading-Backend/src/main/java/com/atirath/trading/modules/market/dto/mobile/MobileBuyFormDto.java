package com.atirath.trading.modules.market.dto.mobile;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Lightweight DTO specifically for Flutter Buy Shares screen/sheet.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MobileBuyFormDto {
    private Long companyId;
    private String symbol;
    private String shortCode;
    private String companyName;
    private String sector;
    private BigDecimal currentPrice;
    private BigDecimal priceChange;
    private BigDecimal priceChangePercent;
    private Boolean isPositive;
    private BigDecimal dayHigh;
    private BigDecimal dayLow;
    private BigDecimal availableCash;
    private String availableCashFormatted;
    private Long defaultQuantity;
    private BigDecimal defaultPrice;
    private List<Long> quickQuantities;
    private String brokerageRate;
    private String gstRate;
    private String settlementCycle;
}
