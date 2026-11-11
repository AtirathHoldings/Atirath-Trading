package com.atirath.trading.modules.portfolio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Real per-user data payload for the web Member Dashboard (/dashboard).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberDashboardDto {

    // Authenticated member profile & onboarding/KYC status
    private Long userId;
    private String memberId;
    private String userName;
    private String displayName;
    private String accountType;
    private String accountStatus;
    private boolean kycCompleted;
    private boolean bankAdded;
    private int documentsCount;
    private Integer age;
    private boolean tradingEligible;

    // Top-right KYC / Onboarding card state
    private String kycStatusTitle;
    private String kycStatusDescription;
    private String kycActionLabel;
    private String kycActionRoute;
    private String kycCardTone; // "warning" | "success" | "danger"

    // Server timestamp
    private String asOfFormatted;
    private String asOfIso;

    // 4 KPI Cards
    private BigDecimal totalPortfolioValue;
    private String formattedPortfolioValue;
    private String formattedPortfolioCompact;
    private BigDecimal portfolioChangePercentage;
    private String formattedPortfolioChangePercentage;
    private boolean portfolioChangePositive;

    private BigDecimal investedAmount;
    private String formattedInvestedAmount;

    private BigDecimal totalGainLoss;
    private String formattedTotalGainLoss;
    private BigDecimal gainLossPercentage;
    private String formattedGainLossPercentage;
    private boolean gainLossPositive;

    private int companiesInvestedCount;
    private int totalCompaniesCount;

    // Escrow wallet snapshot
    private BigDecimal escrowAvailableBalance;
    private String formattedEscrowAvailableBalance;
    private BigDecimal escrowBlockedBalance;

    // Active period filter ("1M", "3M", "6M", "1Y", "ALL")
    private String activePeriod;

    // Performance line/area chart points
    private List<PerformancePointDto> performanceSeries;

    // Portfolio allocation donut slices
    private List<AllocationSliceDto> allocationSlices;

    // Member holdings table
    private List<HoldingRowDto> holdings;

    // Recent orders / trades table
    private List<TransactionRowDto> recentTransactions;

    // Actionable pending tasks derived from account, order, and notification state
    private List<PendingActionDto> pendingActions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PerformancePointDto {
        private String name;
        private BigDecimal value;
        private String formattedValue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AllocationSliceDto {
        private Long companyId;
        private String name;
        private String companyCode;
        private String symbol;
        private BigDecimal value;
        private String formattedValue;
        private BigDecimal percentage;
        private String color;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HoldingRowDto {
        private Long companyId;
        private String name;
        private String companyCode;
        private String symbol;
        private long shares;
        private long availableShares;
        private long lockedShares;
        private BigDecimal avgPrice;
        private String formattedAvgPrice;
        private BigDecimal currentPrice;
        private String formattedCurrentPrice;
        private BigDecimal currentValue;
        private String formattedCurrentValue;
        private BigDecimal pnl;
        private BigDecimal pnlPercentage;
        private String formattedPnlPercentage;
        private boolean isPositive;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransactionRowDto {
        private Long id;
        private String referenceNo;
        private String date;
        private String company;
        private String symbol;
        private String type; // "Buy" | "Sell"
        private long shares;
        private BigDecimal amount;
        private String formattedAmount;
        private String status;
        private boolean isBuy;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PendingActionDto {
        private String id;
        private String title;
        private String description;
        private String route;
        private String iconType; // "shield" | "card" | "doc" | "check" | "bell" | "market"
        private String badgeColor; // "orange" | "blue" | "purple" | "green"
    }
}

