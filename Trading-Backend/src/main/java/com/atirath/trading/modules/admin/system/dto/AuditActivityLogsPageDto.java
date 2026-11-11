package com.atirath.trading.modules.admin.system.dto;

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
public class AuditActivityLogsPageDto {
    private AdminUserHeaderDto adminUser;
    private String pageTitle;
    private String pageSubtitle;
    private String selectedCompany;

    // 5 KPIs
    private List<KpiCardDto> kpis;

    // Tabs
    private List<String> tabs;
    private String activeTab;

    // Filter Options
    private List<String> modules;
    private List<String> actions;
    private List<String> users;

    // Table rows
    private List<AuditLogItemDto> logs;
    private Integer totalLogs;
    private Integer currentPage;
    private Integer totalPages;
    private Integer pageSize;

    // Bottom Visualizations
    private LogsByModuleDonutDto logsByModule;
    private List<ActionTypeBarDto> logsByActionType;
    private List<ActivityTrendMonthDto> activityTrend;

    // Preselected Drawer Detail
    private AuditLogDetailDto selectedLogDetail;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuditLogItemDto {
        private String id;
        private Integer number;
        private String timestamp;
        private String initials;
        private String userName;
        private String action;
        private String actionTheme; // blue, purple, green, red, yellow
        private String module;
        private String details;
        private String ipAddress;
        private String status;
        private String statusTheme; // green, red
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LogsByModuleDonutDto {
        private String totalLogs;
        private List<ModuleSliceDto> slices;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ModuleSliceDto {
        private String module;
        private Double percentage;
        private Integer count;
        private String color;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ActionTypeBarDto {
        private String action;
        private Integer count;
        private Double percentage;
        private String color;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ActivityTrendMonthDto {
        private String month;
        private Integer userActivity;
        private Integer systemChanges;
        private Integer securityEvents;
        private Integer failedActions;
    }
}
