package com.atirath.trading.modules.portfolio.dto.mobile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MobileAllocationDto {
    private Long companyId;
    private String label;        // e.g. "AHIL"
    private String name;         // e.g. "Atirath Holdings"
    private Double pct;          // e.g. 50.2
    private String colorHex;     // e.g. "#1E40AF"
    private BigDecimal value;    // e.g. 2420375.00
    private String formattedValue; // e.g. "₹24.20 L"
}
