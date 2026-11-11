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
public class AccessControlPageDto {
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
    private List<String> companies;
    private List<String> modules;
    private List<String> roles;
    private List<String> accessStatuses;

    // Table rows
    private List<AccessItemDto> accesses;
    private Integer totalAccesses;
    private Integer currentPage;
    private Integer totalPages;
    private Integer pageSize;

    // Bottom Visualizations
    private AccessByModuleDonutDto accessByModule;
    private List<AccessLevelBarDto> accessByAccessLevel;
    private List<AccessTrendMonthDto> accessTrend;

    // Preselected Drawer Detail
    private AccessDetailDto selectedAccessDetail;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccessItemDto {
        private String id;
        private Integer number;
        private String initials;
        private String userName;
        private String roles;
        private String moduleAccess;
        private String accessLevel;
        private String accessLevelTheme; // green, blue, yellow, red
        private String status;
        private String statusTheme; // green, orange
        private String grantedOn;
        private String expiresOn;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccessByModuleDonutDto {
        private String totalAccesses;
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
    public static class AccessLevelBarDto {
        private String accessLevel;
        private Double percentage;
        private Integer count;
        private String color;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AccessTrendMonthDto {
        private String month;
        private Integer active;
        private Integer limited;
        private Integer readOnly;
        private Integer noAccess;
    }
}
