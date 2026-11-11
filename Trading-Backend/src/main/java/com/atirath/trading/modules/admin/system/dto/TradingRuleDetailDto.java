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
public class TradingRuleDetailDto {
    private String id;
    private String ruleName;
    private String ruleCategory;
    private String shortDescription;
    private String status;
    private String statusTheme;

    private List<String> tabs;
    private String activeTab;

    // Basic Information
    private String ruleId;
    private String category;
    private String type;
    private String description;
    private String priority;
    private String priorityTheme;
    private String createdOn;
    private String createdBy;
    private String lastUpdated;
    private String updatedBy;

    // Rule Configuration
    private String limitType;
    private String maximumValue;
    private String currency;
    private String appliesTo;
    private String overrideAllowed;
    private String alertThreshold;
    private String autoBlock;

    // Conditions Summary
    private List<TradingRuleConditionDto> conditions;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TradingRuleConditionDto {
        private String conditionText;
        private Boolean enabled;
    }
}
