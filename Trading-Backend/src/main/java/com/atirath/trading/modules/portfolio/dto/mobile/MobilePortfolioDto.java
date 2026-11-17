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
public class MobilePortfolioDto {
    private Long userId;
    private String userName;
    private String memberId;

    // Summary Hero & Stats
    private BigDecimal totalPortfolioValue;
    private String formattedTotalPortfolioValue;
    private BigDecimal investedCapital;
    private String formattedInvestedCapital;
    private BigDecimal unrealizedPL;
    private String formattedUnrealizedPL;
    private Boolean unrealizedPLPositive;
    private BigDecimal realizedPL;
    private String formattedRealizedPL;
    private Boolean realizedPLPositive;
    private BigDecimal todayChange;
    private String formattedTodayChange;
    private BigDecimal todayChangePct;
    private String formattedTodayChangePct;
    private Boolean todayChangePositive;
    private BigDecimal totalGainLossPct;
    private String formattedTotalGainLossPct;
    private Boolean totalGainLossPositive;

    // Collections
    private List<MobileAllocationDto> allocations;
    private List<MobileHoldingDetailDto> holdings;
    private List<MobileOrderRowDto> orders;
}
