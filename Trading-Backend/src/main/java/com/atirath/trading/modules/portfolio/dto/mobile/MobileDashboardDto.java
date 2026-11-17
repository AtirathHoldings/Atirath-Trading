package com.atirath.trading.modules.portfolio.dto.mobile;

import com.atirath.trading.modules.market.dto.MobileStockDto;
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
public class MobileDashboardDto {
    // 1. User & Greeting
    private String greeting;                   // "Good morning,", "Good afternoon,", "Good evening,"
    private String userName;                   // "Siva Sai Ram"
    private String memberId;                   // "ATG10027"
    private String platformSubtitle;           // "Atirath Group Internal Share Trading Platform"

    // 2. Portfolio Hero Card
    private BigDecimal totalPortfolioValue;    // 4825000.00
    private String formattedTotalValue;        // "₹48.25 L"
    private BigDecimal dayChange;              // 32450.00
    private String formattedDayChange;         // "+₹32,450"
    private BigDecimal dayChangePercent;       // 0.68
    private String formattedDayChangePercent;  // "+0.68%"
    private boolean dayChangePositive;         // true

    private BigDecimal investedValue;          // 4250000.00
    private String formattedInvestedValue;     // "₹42,50,000"

    private BigDecimal unrealizedPnL;          // 574750.00
    private String formattedUnrealizedPnL;     // "+₹5,74,750"
    private boolean unrealizedPnLPositive;     // true

    private BigDecimal realizedPnL;            // 120500.00
    private String formattedRealizedPnL;       // "+₹1,20,500"
    private boolean realizedPnLPositive;       // true

    // 3. Action Required / Pending Actions
    private int pendingActionsCount;           // 2
    private List<MobilePendingActionDto> pendingActions;

    // 4. Group Market Snapshot (Top 3 active stocks)
    private List<MobileStockDto> marketStocks;

    // 5. My Holdings (Top 2 Holdings Summary Cards)
    private List<MobileDashboardHoldingDto> topHoldings;

    // 6. Recent Transactions (Top 3 recent transactions)
    private List<MobileDashboardTransactionDto> recentTransactions;
}
