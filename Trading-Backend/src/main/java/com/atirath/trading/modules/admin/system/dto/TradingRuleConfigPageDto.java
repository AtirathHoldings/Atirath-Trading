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
public class TradingRuleConfigPageDto {
    private AdminUserHeaderDto adminUser;
    private String pageTitle;
    private String pageSubtitle;
    private String selectedCompany;

    // 5 KPIs
    private List<KpiCardDto> kpis;

    // Tabs
    private List<String> tabs;
    private String activeTab;

    // Filter Options
    private List<String> ruleCategories;
    private List<String> statuses;
    private List<String> modules;

    // Table rows
    private List<TradingRuleItemDto> rules;
    private Integer totalRules;
    private Integer currentPage;
    private Integer totalPages;
    private Integer pageSize;

    // Bottom Visualizations
    private RulesByCategoryDonutDto rulesByCategory;
    private List<RuleStatusBarDto> rulesByStatus;
    private List<RulePriorityBarDto> rulesByPriority;

    // Preselected Drawer Detail
    private TradingRuleDetailDto selectedRuleDetail;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TradingRuleItemDto {
        private String id;
        private Integer number;
        private String ruleName;
        private String category;
        private String categoryTheme; // blue
        private String type;
        private String description;
        private String status;
        private String statusTheme; // green, red
        private String priority;
        private String priorityTheme; // red, orange, dark-red
        private String lastUpdated;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RulesByCategoryDonutDto {
        private String totalRules;
        private List<CategorySliceDto> slices;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CategorySliceDto {
        private String category;
        private Double percentage;
        private Integer count;
        private String color;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RuleStatusBarDto {
        private String status;
        private Double percentage;
        private Integer count;
        private String color;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RulePriorityBarDto {
        private String priority;
        private Double percentage;
        private Integer count;
        private String color;
    }
}
