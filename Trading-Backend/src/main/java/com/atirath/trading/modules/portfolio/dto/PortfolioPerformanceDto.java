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
public class PortfolioPerformanceDto {
    private BigDecimal totalReturn;
    private String formattedTotalReturn; // e.g. "34.1%"
    private BigDecimal annualizedReturn;
    private String formattedAnnualizedReturn; // e.g. "18.6%"
    private String bestPerformingHolding;     // e.g. "Atirath Holdings"
    private String bestPerformingHoldingChange; // e.g. "+52.3%"
    private String worstPerformingHolding;    // e.g. "CS Interio"
    private String worstPerformingHoldingChange; // e.g. "-4.8%"
    private BigDecimal dividendReceivedYtd;
    private String formattedDividendReceivedYtd; // e.g. "₹ 0"
    private Integer activeOrders; // 3
    private Long lockedSharesInEscrow; // 1,750
    private Long pendingTransfers; // 500
}
