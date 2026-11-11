package com.atirath.trading.modules.admin.system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemSettingDetailDto {
    private String id;
    private String settingName;
    private String settingCategory;
    private String description;
    private String status;
    private String statusTheme;

    private List<String> tabs;
    private String activeTab;

    // Basic Information
    private String settingKey;
    private String module;
    private String dataType;
    private String currentValue;
    private String defaultValue;
    private String minimumValue;
    private String maximumValue;
    private String createdOn;
    private String createdBy;
    private String lastUpdated;
    private String updatedBy;

    // Validation Rules
    private List<String> validationRules;

    // Affected Modules
    private List<String> affectedModules;
}
