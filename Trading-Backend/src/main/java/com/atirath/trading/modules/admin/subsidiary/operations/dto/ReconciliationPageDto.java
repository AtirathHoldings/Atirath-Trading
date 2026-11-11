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
public class ReconciliationPageDto {
    private AdminUserHeaderDto adminUser;
    private String pageTitle;
    private String pageSubtitle;
    private String selectedCompany;
    private String dateRange;

    // 5 KPIs
    private List<KpiCardDto> kpis;

    // Top Visualizations
    private List<ReconciliationTrendMonthDto> reconciliationTrend;
    private ReconciliationStatusDonutDto statusDistribution;

    // Tabs
    private List<String> tabs;
    private String activeTab;

    // Filter Options
    private List<String> companies;
    private List<String> recordTypes;
    private List<String> statuses;

    // Table rows
    private List<ReconciliationRecordItemDto> records;
    private Integer totalRecords;
    private Integer currentPage;
    private Integer totalPages;
    private Integer pageSize;

    // Bottom Widgets
    private List<VarianceByTypeDto> varianceByRecordType;
    private List<TopExceptionRowDto> topReconciliationExceptions;

    // Preselected Drawer Detail
    private ReconciliationDetailDto selectedReconciliationDetail;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ReconciliationTrendMonthDto {
        private String month;
        private Integer matched;
        private Integer unmatched;
        private Integer exceptions;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReconciliationStatusDonutDto {
        private String totalRecords;
        private List<StatusSliceDto> slices;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StatusSliceDto {
        private String status;
        private Double percentage;
        private String count;
        private String color;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReconciliationRecordItemDto {
        private String id;
        private Integer number;
        private String recordId;
        private String recordType;
        private String recordTypeTheme; // blue, purple, teal
        private String referenceId;
        private String company;
        private String expectedAmount;
        private String actualAmount;
        private String variance;
        private String varianceTheme; // green, red, orange
        private String status;
        private String statusTheme; // green, red, orange
        private String reconciledOn;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class VarianceByTypeDto {
        private String type;
        private Double positiveVariance;
        private Double negativeVariance;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TopExceptionRowDto {
        private Integer number;
        private String recordId;
        private String type;
        private String referenceId;
        private String variance;
        private String reason;
        private String status;
        private String statusTheme;
    }
}
