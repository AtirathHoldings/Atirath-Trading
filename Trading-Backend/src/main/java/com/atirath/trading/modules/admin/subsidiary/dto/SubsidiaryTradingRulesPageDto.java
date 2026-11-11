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
public class SubsidiaryTradingRulesPageDto {

    private AdminUserHeaderDto adminUser;
    private String pageTitle;
    private String pageSubtitle;
    private String selectedCompany;
    private String companyStatus;

    // 4 KPIs
    private List<KpiCardDto> kpis;

    // Tabs: All Trading Rules (10), Trading Windows (3), Price Limits (2), Order Limits (2), Eligibility Rules (2), Other Rules (1)
    private List<String> tabs;
    private String activeTab;

    // Table: Trading Rules
    private List<TradingRuleItemDto> rules;
    private int totalRules;
    private int currentPage;
    private int totalPages;

    // Right Drawer Detail
    private TradingRuleDetailDto selectedRuleDetail;

    // Bottom Widget 1: Rules by Type Donut
    private TradingRulesByTypeDonutDto rulesByType;

    // Bottom Widget 2: Rule Status Donut
    private TradingRuleStatusDonutDto ruleStatus;

    // Bottom Widget 3: Applicable Share Classes Bar Chart
    private List<TradingApplicableClassBarDto> applicableShareClasses;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TradingRuleItemDto {
        private String id;
        private int number;
        private String ruleName;
        private String ruleType; // Trading Window, Price Limit, Order Limit, Eligibility Rule, Compliance Rule, Other Rule, Approval Rule
        private String ruleTypeTheme; // blue, purple, green, orange, pink
        private String applicableShareClasses;
        private String parameters;
        private String status; // Active, Suspended, Inactive
        private String statusTheme; // green, orange, red
        private String effectiveFrom;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TradingRulesByTypeDonutDto {
        private int totalRules;
        private List<TradingRuleTypeSliceDto> slices;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TradingRuleTypeSliceDto {
        private String type;
        private int count;
        private double percentage;
        private String color;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TradingRuleStatusDonutDto {
        private int totalRules;
        private List<TradingRuleStatusSliceDto> slices;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TradingRuleStatusSliceDto {
        private String status;
        private int count;
        private double percentage;
        private String color;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TradingApplicableClassBarDto {
        private String shareClass;
        private int rulesCount;
        private String color;
    }
}
