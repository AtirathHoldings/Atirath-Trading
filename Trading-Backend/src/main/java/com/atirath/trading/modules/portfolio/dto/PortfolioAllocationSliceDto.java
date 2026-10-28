package com.atirath.trading.modules.portfolio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioAllocationSliceDto {
    private Long companyId;
    private String companyName;
    private String symbol;
    private BigDecimal percentage;
    private String formattedPercentage;
    private BigDecimal value;
    private String formattedValue;
    private String color; // hex code for donut slices
}
