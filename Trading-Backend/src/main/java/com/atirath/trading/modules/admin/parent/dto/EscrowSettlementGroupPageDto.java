package com.atirath.trading.modules.admin.parent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EscrowSettlementGroupPageDto {
    private AdminUserHeaderDto adminUser;
    private String pageTitle;
    private String pageSubtitle;
    private String dateRange; // "01 Sep 2026 - 16 Sep 2026"

    // 5 Top KPI Cards
    private List<KpiCardDto> kpis;

    // Charts
    private EscrowTrendChartDto escrowTrend;
    private SettlementStatusDonutDto settlementStatus;

    // Available Tabs
    private List<String> tabs;
    private String activeTab;

    // Table: Escrow Transactions
    private List<EscrowTransactionItemDto> transactions;
    private int totalTransactions;
    private int currentPage;
    private int totalPages;

    // Bottom Widgets
    private List<PendingSettlementSummaryItemDto> pendingSettlementSummary;
    private List<RecentSettlementActivityItemDto> recentSettlementActivity;
    private List<SettlementInsightItemDto> keyInsights;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EscrowTrendChartDto {
        private String filterRange; // "Last 6 Months"
        private List<MonthlyEscrowPointDto> monthlyData;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyEscrowPointDto {
        private String month;
        private Double amountCr;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SettlementStatusDonutDto {
        private String totalTrades;
        private List<SettlementStatusSliceDto> slices;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SettlementStatusSliceDto {
        private String status; // "Settled", "Pending", "Under Review", "Disputed"
        private Double percentage;
        private String amountFormatted;
        private String color;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EscrowTransactionItemDto {
        private String id;
        private int number;
        private String tradeId; // "ESC-100482"
        private String dateTime;
        private String buyer;
        private String seller;
        private String company;
        private String escrowAmount;
        private String tradeValue;
        private String settlementDate;
        private String status; // "Settled", "Pending", "Under Review", "Disputed"
        private String statusTheme; // "green", "yellow", "blue", "red"
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PendingSettlementSummaryItemDto {
        private String company;
        private Integer numberOfTrades;
        private String amount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentSettlementActivityItemDto {
        private String dateTime;
        private String tradeId;
        private String company;
        private String amount;
        private String status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SettlementInsightItemDto {
        private String description;
        private String icon;
        private String theme;
    }
}
