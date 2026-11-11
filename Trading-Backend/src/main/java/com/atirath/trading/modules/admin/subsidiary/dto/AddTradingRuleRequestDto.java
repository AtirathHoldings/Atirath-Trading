package com.atirath.trading.modules.admin.subsidiary.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddTradingRuleRequestDto {
    private String ruleName;
    private String ruleType; // Trading Window, Price Limit, Order Limit, Eligibility Rule, Compliance Rule, Approval Rule
    private String applicableShareClasses;
    private String description;
    private String parameters;
    private String conditions;
    private String effectiveFrom;
}
