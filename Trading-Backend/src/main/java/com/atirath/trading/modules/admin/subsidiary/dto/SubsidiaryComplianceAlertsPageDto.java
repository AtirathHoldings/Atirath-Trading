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
public class SubsidiaryComplianceAlertsPageDto {

    private AdminUserHeaderDto adminUser;
    private String pageTitle;
    private String pageSubtitle;
    private String selectedCompany;
    private String dateRange;

    // 4 Top KPI Cards
    private List<KpiCardDto> kpis;

    // Tabs
    private List<String> tabs;
    private String activeTab;

    // Filter selections
    private List<String> entities;
    private List<String> alertTypes;
    private List<String> riskLevels;
    private List<String> statuses;

    // Table items
    private List<ComplianceAlertItemDto> alerts;
    private int totalAlerts;
    private int currentPage;
    private int totalPages;
    private int pageSize;

    // Selected Drawer item
    private ComplianceAlertDetailDto selectedAlertDetail;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComplianceAlertItemDto {
        private String id;
        private int number;
        private String alertId;
        private String dateTime;
        private String entity;
        private String alertType;
        private String riskLevel;
        private String riskTheme; // red, orange, green
        private String status;
        private String statusTheme; // red, orange, blue, green, gray
        private String description;
    }
}
