package com.atirath.trading.modules.escrow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EscrowSummaryMetricsDto {
    private int totalTransactions;
    private String totalTransactionsChange; // "↑ 20% vs last month"

    private int pendingFunding;
    private String pendingFundingChange; // "↑ 50% vs last month"

    private BigDecimal fundsInEscrow;
    private String fundsInEscrowFormatted; // "₹ 1,24,50,000"
    private String fundsInEscrowChange; // "↑ 12% vs last month"

    private Long sharesLocked;
    private String sharesLockedFormatted; // "45,000"
    private String sharesLockedChange; // "↑ 18% vs last month"

    private int completedSettlements;
    private String completedSettlementsChange; // "↑ 33% vs last month"

    private int failedCancelled;
    private String failedCancelledChange; // "↓ 50% vs last month"
}
