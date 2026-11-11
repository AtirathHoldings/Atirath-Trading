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
public class PermissionManagementPageDto {
    private AdminUserHeaderDto adminUser;
    private String pageTitle;
    private String pageSubtitle;
    private String selectedCompany;

    // 4 KPIs
    private List<KpiCardDto> kpis;

    // Tabs
    private List<String> tabs;
    private String activeTab;

    // Filter Options
    private List<String> modules;
    private List<String> permissionTypes;
    private List<String> roles;
    private List<String> statuses;

    // Table rows
    private List<PermissionItemDto> permissions;
    private Integer totalPermissions;
    private Integer currentPage;
    private Integer totalPages;
    private Integer pageSize;

    // Bottom Visualizations
    private PermissionsByModuleDonutDto permissionsByModule;
    private List<PermissionTypeBarDto> permissionsByType;
    private List<PermissionTrendMonthDto> permissionAssignmentTrend;

    // Preselected Drawer Detail
    private PermissionDetailDto selectedPermissionDetail;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PermissionItemDto {
        private String id;
        private Integer number;
        private String permissionName;
        private String module;
        private String permissionType;
        private String permissionTypeTheme; // blue, green, yellow, red, purple, teal
        private String description;
        private Integer assignedRoles;
        private String status;
        private String statusTheme; // green, red
        private String createdOn;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PermissionsByModuleDonutDto {
        private String totalPermissions;
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
    public static class PermissionTypeBarDto {
        private String type;
        private Double percentage;
        private Integer count;
        private String color;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PermissionTrendMonthDto {
        private String month;
        private Integer count;
    }
}
