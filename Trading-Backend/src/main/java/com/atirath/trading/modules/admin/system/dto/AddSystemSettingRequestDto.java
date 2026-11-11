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
public class AddSystemSettingRequestDto {
    private String settingName;
    private String settingKey;
    private String module; // General, Security, Trading, Settlement, Compliance, Notifications, System
    private String dataType; // Text, Number, Boolean, Dropdown, Email
    private String currentValue;
    private String defaultValue;
    private String minimumValue;
    private String maximumValue;
    private String description;
    private List<String> validationRules;
    private List<String> affectedModules;
}
