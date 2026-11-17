package com.atirath.trading.modules.portfolio.dto.mobile;

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
public class MobileHoldingDetailDto {
    private Long companyId;
    private String code;
    private String symbol;
    private String name;
    private Long shares;
    private Long available;
    private Long locked;
    private Double avgPrice;
    private Double currentPrice;
    private Double invested;
    private Double currentValue;
    private Double pl;
    private Double plPct;
    private String ownershipPct;
    private Boolean isPositive;
    private List<Double> chartData;
}
