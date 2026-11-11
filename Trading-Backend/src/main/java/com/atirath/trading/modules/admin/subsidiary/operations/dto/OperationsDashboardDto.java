package com.atirath.trading.modules.admin.subsidiary.operations.dto;

import com.atirath.trading.modules.admin.parent.dto.AdminUserHeaderDto;
import com.atirath.trading.modules.admin.parent.dto.KpiCardDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperationsDashboardDto {

    private AdminUserHeaderDto adminUser;
    private String pageTitle;
    private String pageSubtitle;
    private String selectedCompany;
    private String dateRange;

    // 6 Top KPI Cards
    private List<KpiCardDto> kpis;

    // Operational Alerts List (Top Right)
    private List<OperationalAlertDto> operationalAlerts;

    // Middle Chart 1: Transaction Volume Trend (Line Chart Apr-Sep)
    private List<VolumeTrendMonthDto> transactionVolumeTrend;

    // Middle Chart 2: Transaction Status Donut (2,156 total)
    private TransactionStatusDonutDto transactionStatus;

    // Middle Row: Recent Trades & Recent Transfers
    private List<RecentTradeItemDto> recentTrades;
    private List<RecentTransferItemDto> recentTransfers;

    // Bottom Row Summary Cards
    private EscrowSummaryWidgetDto escrowMonitoring;
    private SettlementSummaryWidgetDto settlementMonitoring;
    private FailedTransactionsWidgetDto failedTransactions;
    private DisputesOverviewWidgetDto disputesOverview;
    private SupportTicketsWidgetDto supportTickets;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OperationalAlertDto {
        private String title;
        private String description;
        private String timeAgo;
        private String type; // failed, pending, exception, request, high-value
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VolumeTrendMonthDto {
        private String month;
        private int trades;
        private int transfers;
        private int settlements;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransactionStatusDonutDto {
        private String totalTransactions;
        private List<StatusSliceDto> slices;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusSliceDto {
        private String label;
        private double percentage;
        private String count;
        private String color;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentTradeItemDto {
        private String tradeId;
        private String company;
        private String shareClass;
        private String investor;
        private String value;
        private String status;
        private String statusTheme; // green, orange, red
        private String date;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentTransferItemDto {
        private String transferId;
        private String fromParty;
        private String toParty;
        private String shares;
        private String status;
        private String statusTheme; // green, orange, purple, red
        private String date;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EscrowSummaryWidgetDto {
        private String totalEscrowBalance;
        private String onHold;
        private List<EscrowRowDto> accounts;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EscrowRowDto {
        private String escrowId;
        private String company;
        private String balance;
        private String status;
        private String statusTheme;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SettlementSummaryWidgetDto {
        private int pendingSettlements;
        private String avgSettlementTime;
        private List<SettlementRowDto> settlements;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SettlementRowDto {
        private String settlementId;
        private String tradeId;
        private String amount;
        private String status;
        private String statusTheme;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FailedTransactionsWidgetDto {
        private int totalFailed;
        private String resolutionRate;
        private List<FailedTxnRowDto> transactions;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FailedTxnRowDto {
        private String txnId;
        private String type;
        private String amount;
        private String reason;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DisputesOverviewWidgetDto {
        private int openDisputes;
        private int resolvedThisQuarter;
        private List<DisputeRowDto> disputes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DisputeRowDto {
        private String disputeId;
        private String relatedTxn;
        private String type;
        private String raisedBy;
        private String status;
        private String statusTheme;
        private String date;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SupportTicketsWidgetDto {
        private int openTickets;
        private String avgResolutionTime;
        private List<TicketRowDto> tickets;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TicketRowDto {
        private String ticketId;
        private String category;
        private String raisedBy;
        private String priority;
        private String priorityTheme;
        private String status;
        private String statusTheme;
        private String date;
    }
}
