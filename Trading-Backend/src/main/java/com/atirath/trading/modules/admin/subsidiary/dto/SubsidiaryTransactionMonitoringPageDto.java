package com.atirath.trading.modules.admin.subsidiary.dto;

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
public class SubsidiaryTransactionMonitoringPageDto {

    private AdminUserHeaderDto adminUser;
    private String pageTitle;
    private String pageSubtitle;
    private String selectedCompany;
    private String dateRange;

    // 4 Top KPI Cards
    private List<KpiCardDto> kpis;

    // Chart 1: Transaction Volume Trend (Bar + Line)
    private List<MonthlyVolumeTrendDto> transactionVolumeTrend;

    // Chart 2: Transaction Types (Donut)
    private TransactionTypesDonutDto transactionTypes;

    // Tabs
    private List<String> tabs;
    private String activeTab;

    // Filter selections
    private List<String> entities;
    private List<String> transactionTypesList;
    private List<String> riskLevels;
    private List<String> statuses;

    // Table items
    private List<MonitoredTransactionItemDto> transactions;
    private int totalTransactions;
    private int currentPage;
    private int totalPages;
    private int pageSize;

    // Selected Drawer item
    private MonitoredTransactionDetailDto selectedTransactionDetail;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyVolumeTrendDto {
        private String month;
        private int totalTransactions;
        private int flaggedTransactions;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransactionTypesDonutDto {
        private int totalTransactions;
        private List<TransactionTypeSliceDto> slices;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransactionTypeSliceDto {
        private String label;
        private double percentage;
        private int count;
        private String color;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonitoredTransactionItemDto {
        private String id;
        private int number;
        private String transactionId;
        private String dateTime;
        private String entity;
        private String type;
        private String investorOrShareholder;
        private String amount;
        private String riskLevel;
        private String riskTheme; // red, orange, green
        private String status;
        private String statusTheme; // red, orange, green
    }
}
