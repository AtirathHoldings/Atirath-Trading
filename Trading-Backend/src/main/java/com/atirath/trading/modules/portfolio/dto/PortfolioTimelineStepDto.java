package com.atirath.trading.modules.portfolio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioTimelineStepDto {
    private Integer stepNumber;
    private String stepName;      // e.g. "Order Placed", "Order Validated", "Matched with Seller", "Trade Executed", "Sent to Escrow"
    private String timestamp;     // e.g. "18 Sep 2025, 10:15 AM"
    private String status;        // "COMPLETED", "IN_PROGRESS", "PENDING"
    private Boolean isCurrent;
}
