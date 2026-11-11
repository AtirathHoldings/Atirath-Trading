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
public class RoleManagementPageDto {
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
    private List<String> roleTypes;
    private List<String> statuses;
    private List<String> accessLevels;

    // Table rows
    private List<RoleItemDto> roles;
    private Integer totalRoles;
    private Integer currentPage;
    private Integer totalPages;
    private Integer pageSize;

    // Bottom Visualizations
    private RolesByTypeDonutDto rolesByType;
    private List<RoleAccessLevelBarDto> rolesByAccessLevel;
    private List<RoleTrendMonthDto> roleCreationTrend;

    // Preselected Drawer Detail
    private RoleDetailDto selectedRoleDetail;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoleItemDto {
        private String id;
        private Integer number;
        private String roleName;
        private String roleType;
        private String roleTypeTheme; // blue, purple
        private String description;
        private Integer usersAssigned;
        private String status;
        private String statusTheme; // green, red
        private String accessLevel;
        private String accessLevelTheme; // red, orange, yellow, green
        private String createdOn;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RolesByTypeDonutDto {
        private String totalRoles;
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
    public static class RoleAccessLevelBarDto {
        private String accessLevel;
        private Double percentage;
        private Integer count;
        private String color;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RoleTrendMonthDto {
        private String month;
        private Integer count;
    }
}
