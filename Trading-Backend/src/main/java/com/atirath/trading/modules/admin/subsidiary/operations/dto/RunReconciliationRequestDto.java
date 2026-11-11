package com.atirath.trading.modules.admin.subsidiary.operations.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RunReconciliationRequestDto {
    private String type; // Monthly, Daily, Weekly
    private String period; // Sep 2026
    private String company; // Atirath Holdings
    private String dataSources; // Trading System, Escrow System, Bank Statements
    private String remarks;
}
