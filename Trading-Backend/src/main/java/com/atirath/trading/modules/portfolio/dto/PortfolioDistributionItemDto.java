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
public class PortfolioDistributionItemDto {
    private String name;        // e.g. "Buy", "Sell", "Bank Transfer", "Escrow"
    private Long count;         // e.g. 42
    private BigDecimal percentage; // e.g. 32.8
    private String formattedPercentage; // "32.8%"
    private String color;
}
