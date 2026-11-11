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
public class SystemSettingsPageDto {
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
    private List<String> statuses;
    private List<String> dataTypes;

    // Table rows
    private List<SettingItemDto> settings;
    private Integer totalSettings;
    private Integer currentPage;
    private Integer totalPages;
    private Integer pageSize;

    // Bottom Widgets
    private SettingsByModuleDonutDto settingsByModule;
    private List<SettingStatusBarDto> settingsByStatus;
    private List<RecentChangeRowDto> recentConfigurationChanges;

    // Preselected Drawer Detail
    private SystemSettingDetailDto selectedSettingDetail;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SettingItemDto {
        private String id;
        private Integer number;
        private String settingName;
        private String settingKey;
        private String module;
        private String dataType;
        private String currentValue;
        private String status;
        private String statusTheme; // green, red
        private String lastUpdated;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SettingsByModuleDonutDto {
        private String totalSettings;
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
    public static class SettingStatusBarDto {
        private String status;
        private Double percentage;
        private Integer count;
        private String color;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RecentChangeRowDto {
        private String dateTime;
        private String setting;
        private String oldValue;
        private String newValue;
        private String updatedBy;
    }
}
