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
public class DisputesPageDto {
    private AdminUserHeaderDto adminUser;
    private String pageTitle;
    private String pageSubtitle;
    private String selectedCompany;
    private String dateRange;

    // 5 KPIs + Avg Resolution Time
    private List<KpiCardDto> kpis;
    private String avgResolutionTime;
    private String avgResolutionTimeChange;

    // Tabs
    private List<String> tabs;
    private String activeTab;

    // Filter Options
    private List<String> companies;
    private List<String> disputeTypes;
    private List<String> statuses;
    private List<String> priorities;

    // Table rows
    private List<DisputeItemDto> disputes;
    private Integer totalDisputes;
    private Integer currentPage;
    private Integer totalPages;
    private Integer pageSize;

    // Bottom Visualizations
    private DisputesTypeDonutDto disputesByType;
    private List<DisputeStatusBarDto> disputesByStatus;
    private List<DisputeTrendMonthDto> disputesTrend;

    // Preselected Drawer Detail
    private DisputeDetailDto selectedDisputeDetail;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DisputeItemDto {
        private String id;
        private Integer number;
        private String disputeId;
        private String type;
        private String typeTheme; // red, blue, purple, green
        private String relatedId;
        private String investorOrCounterparty;
        private String amount;
        private String priority;
        private String priorityTheme; // red, orange, green
        private String status;
        private String statusTheme; // red, blue, green
        private String raisedOn;
        private String targetResolution;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DisputesTypeDonutDto {
        private String totalDisputes;
        private List<DisputeSliceDto> slices;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DisputeSliceDto {
        private String type;
        private Double percentage;
        private Integer count;
        private String color;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DisputeStatusBarDto {
        private String status;
        private Double percentage;
        private Integer count;
        private String color;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DisputeTrendMonthDto {
        private String month;
        private Integer open;
        private Integer inProgress;
        private Integer resolved;
        private Integer escalated;
    }
}
