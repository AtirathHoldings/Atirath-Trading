package com.atirath.trading.modules.admin.subsidiary.dto;

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
    private String status;
    private String statusTheme;
    private String ruleTypeBadge;

    private List<String> tabs;
    private String activeTab;

    private String ruleType;
    private String description;
    private String applicableShareClasses;
    private String windowOpenDate;
    private String windowCloseDate;
    private String restrictedPeriod;
    private String conditions;
    private String effectiveFrom;
    private String createdBy;
    private String createdOn;
    private String lastUpdated;
}
