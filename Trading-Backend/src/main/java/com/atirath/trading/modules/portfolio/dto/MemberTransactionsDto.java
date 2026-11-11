package com.atirath.trading.modules.portfolio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Real per-user payload for the Recent Transactions screen (/transactions).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberTransactionsDto {

    private Long userId;
    private String memberId;
    private String userName;
    private String asOfFormatted;

    // Period applied to KPI cards and Insights donut ("1M", "3M", "6M", "1Y", "ALL")
    private String activePeriod;
    private String periodSubtitle;

    // 4 KPI Cards
    private long totalTransactions;
    private long buyCount;
    private BigDecimal buyAmount;
    private String formattedBuyAmount;
    private long sellCount;
    private BigDecimal sellAmount;
    private String formattedSellAmount;
    private long transferCount;
    private BigDecimal transferAmount;
    private String formattedTransferAmount;

    // Donut Insights
    private TransactionInsightsDto insights;

    // Filter options
    private List<String> availableCompanies;
    private List<String> availableStatuses;

    // Paginated table rows
    private List<TransactionRowDto> transactions;
    private long totalElements;
    private int totalPages;
    private int currentPage;
    private int pageSize;

    // Recent Activity sidebar widget
    private List<ActivityItemDto> recentActivity;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransactionInsightsDto {
        private long totalCount;
        private long buyCount;
        private int buyPercent;
        private long sellCount;
        private int sellPercent;
        private long transferCount;
        private int transferPercent;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransactionRowDto {
        private String uid;
        private String referenceId;
        private String dateIso;
        private String date;
        private String timestampFormatted;
        private Long companyId;
        private String company;
        private String symbol;
        private String type; // "Buy" | "Sell" | "Transfer"
        private long shares;
        private BigDecimal price;
        private String formattedPrice;
        private BigDecimal amount;
        private String formattedAmount;
        private String status;
        private String source; // "ORDER" | "TRADE" | "TRANSFER"
        private String detailsSummary;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivityItemDto {
        private String id;
        private String title;
        private String description;
        private String relativeTime;
        private String activityType; // "buy" | "sell" | "transfer" | "settle"
    }
}

