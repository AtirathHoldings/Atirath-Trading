package com.atirath.trading.modules.admin.subsidiary.dto;

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
public class SubsidiaryShareTransferRulesPageDto {

    private AdminUserHeaderDto adminUser;
    private String pageTitle;
    private String pageSubtitle;
    private String selectedCompany;
    private String companyStatus;

    // 4 KPIs
    private List<KpiCardDto> kpis;

    // Tabs: All Rules (8), Transfer Restrictions (3), Approval Workflow (2), Pre-emptive Rights (1), Other Rules (2)
    private List<String> tabs;
    private String activeTab;

    // Table: Share Transfer Rules
    private List<ShareTransferRuleItemDto> rules;
    private int totalRules;

    // Right Drawer Detail
    private ShareTransferRuleDetailDto selectedRuleDetail;

    // Bottom Widget 1: Rules by Type Donut
    private RulesByTypeDonutDto rulesByType;

    // Bottom Widget 2: Applicable Share Classes Bar Chart
    private List<ApplicableClassBarDto> applicableShareClasses;

    // Bottom Widget 3: Transfer Approval Status Donut
    private TransferApprovalStatusDonutDto transferApprovalStatus;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShareTransferRuleItemDto {
        private String id;
        private int number;
        private String ruleName;
        private String ruleType; // Approval Workflow, Pre-emptive Right, Restriction, General Rule
        private String ruleTypeTheme; // blue, purple, red, green
        private String applicableShareClasses;
        private String conditions;
        private String approvalRequired;
        private String status; // Active, Inactive
        private String statusTheme; // green, red
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RulesByTypeDonutDto {
        private int totalRules;
        private List<RuleTypeSliceDto> slices;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RuleTypeSliceDto {
        private String type;
        private int count;
        private double percentage;
        private String color;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApplicableClassBarDto {
        private String shareClass;
        private int rulesCount;
        private String color;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransferApprovalStatusDonutDto {
        private int totalRequests;
        private List<ApprovalStatusSliceDto> slices;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApprovalStatusSliceDto {
        private String status;
        private int count;
        private double percentage;
        private String color;
    }
}
