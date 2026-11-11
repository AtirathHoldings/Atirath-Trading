package com.atirath.trading.modules.admin.subsidiary.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddShareTransferRuleRequestDto {
    private String ruleName;
    private String ruleType; // Approval Workflow, Pre-emptive Right, Restriction, General Rule
    private String applicableShareClasses;
    private String description;
    private String conditions;
    private String approvalRequired;
    private String effectiveFrom;
}
