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
public class FailedTransactionsPageDto {
    private AdminUserHeaderDto adminUser;
    private String pageTitle;
    private String pageSubtitle;
    private String selectedCompany;
    private String dateRange;

    // 5 KPIs
    private List<KpiCardDto> kpis;

    // Tabs
    private List<String> tabs;
    private String activeTab;

    // Filter Options
    private List<String> companies;
    private List<String> transactionTypes;
    private List<String> failureReasons;

    // Table rows
    private List<FailedTransactionItemDto> transactions;
    private Integer totalTransactions;
    private Integer currentPage;
    private Integer totalPages;
    private Integer pageSize;

    // Bottom Visualizations
    private FailedTypesDonutDto failedTransactionsByType;
    private List<FailureReasonBarDto> failureReasonsDistribution;
    private List<FailedTrendMonthDto> failedTransactionsTrend;

    // Preselected Drawer Detail
    private FailedTransactionDetailDto selectedTransactionDetail;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FailedTransactionItemDto {
        private String id;
        private Integer number;
        private String transactionId;
        private String type;
        private String typeTheme; // red, blue, purple, teal
        private String company;
        private String investor;
        private String amount;
        private String failureReason;
        private String failureReasonTheme; // red, orange
        private String failedOn;
        private String retryStatus;
        private String retryStatusTheme; // gray, blue, green, red
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FailedTypesDonutDto {
        private String totalFailed;
        private List<TypeSliceDto> slices;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TypeSliceDto {
        private String type;
        private Double percentage;
        private Integer count;
        private String color;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FailureReasonBarDto {
        private String reason;
        private Integer count;
        private Double percentage;
        private String color;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FailedTrendMonthDto {
        private String month;
        private Integer count;
    }
}
