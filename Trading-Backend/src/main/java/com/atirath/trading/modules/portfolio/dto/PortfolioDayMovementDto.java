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
public class PortfolioDayMovementDto {
    private String companyName;
    private BigDecimal changeAmount;
    private String formattedChangeAmount; // e.g. "+5.00" or "-5.00"
    private BigDecimal changePercentage;
    private String formattedChangePercentage; // e.g. "+2.2%" or "-3.3%"
    private Boolean isPositive;
}
