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
public class MobileDashboardHoldingDto {
    private Long companyId;
    private String symbol;
    private String companyName;
    private String shares;        // e.g. "850 shares"
    private long totalShares;
    private String value;         // e.g. "₹7.99 L"
    private BigDecimal currentValue;
    private String pct;           // e.g. "+14.7%"
    private BigDecimal pnlPercent;
    private boolean isPositive;
}
