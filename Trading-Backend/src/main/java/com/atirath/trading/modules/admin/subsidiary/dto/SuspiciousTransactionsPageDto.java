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
public class SuspiciousTransactionsPageDto {

    private AdminUserHeaderDto adminUser;
    private String pageTitle;
    private String pageSubtitle;
    private String selectedCompany;
    private String dateRange;

    // 4 Top KPI Cards
    private List<KpiCardDto> kpis;

    // Middle Chart 1: Suspicious Transactions Trend (Stacked Bar)
    private List<SuspiciousTransactionsTrendDto> trend;

    // Middle Chart 2: Alert Type Distribution (Donut)
    private AlertTypeDistributionDonutDto alertTypeDistribution;

    // Tabs
    private List<String> tabs;
    private String activeTab;

    // Filter Dropdowns
    private List<String> entities;
    private List<String> riskLevels;
    private List<String> alertTypes;
    private List<String> statuses;

    // Table items
    private List<SuspiciousTransactionItemDto> transactions;
    private int totalTransactions;
    private int currentPage;
    private int totalPages;
    private int pageSize;

    // Selected Drawer item
    private SuspiciousTransactionDetailDto selectedTransactionDetail;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SuspiciousTransactionsTrendDto {
        private String month;
        private int total;
        private int highRisk;
        private int mediumRisk;
        private int lowRisk;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlertTypeDistributionDonutDto {
        private int totalFlaggedTransactions;
        private List<AlertTypeSliceDto> slices;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlertTypeSliceDto {
        private String label;
        private double percentage;
        private int count;
        private String color;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SuspiciousTransactionItemDto {
        private String id;
        private int number;
        private String transactionId;
        private String dateTime;
        private String entity;
        private String investorOrShareholder;
        private String alertType;
        private String riskLevel;
        private String riskTheme; // red, orange, green
        private String status;
        private String statusTheme; // orange, blue, green, gray
    }
}
