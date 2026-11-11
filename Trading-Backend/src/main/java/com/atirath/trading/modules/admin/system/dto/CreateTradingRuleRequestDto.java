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
public class CreateTradingRuleRequestDto {
    private String ruleName;
    private String category; // Trading Limit, Risk Management, Compliance, Market Rule, Custom Rule
    private String type; // Threshold, Validation, Time Based, Mandatory, System
    private String description;
    private String priority; // High, Medium, Low, Critical
    private String limitType;
    private String maximumValue;
    private String currency;
    private String appliesTo;
    private Boolean overrideAllowed;
    private String alertThreshold;
    private Boolean autoBlock;
    private List<String> conditions;
}
