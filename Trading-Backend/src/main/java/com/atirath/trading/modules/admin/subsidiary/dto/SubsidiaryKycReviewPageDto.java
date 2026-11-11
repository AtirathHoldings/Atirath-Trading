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
public class SubsidiaryKycReviewPageDto {

    private AdminUserHeaderDto adminUser;
    private String pageTitle;
    private String pageSubtitle;
    private String selectedCompany;
    private String dateRange;

    // 5 Top KPI Cards
    private List<KpiCardDto> kpis;

    // Tabs
    private List<String> tabs;
    private String activeTab;

    // Filter selections
    private List<String> entityTypes;
    private List<String> statuses;
    private List<String> riskLevels;

    // Table items
    private List<KycRequestItemDto> requests;
    private int totalRequests;
    private int currentPage;
    private int totalPages;
    private int pageSize;

    // Selected Drawer item
    private KycDetailDto selectedKycDetail;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KycRequestItemDto {
        private String id;
        private int number;
        private String requestId;
        private String nameOrEntity;
        private String entityType;
        private String panOrCin;
        private String requestDate;
        private String status;
        private String statusTheme; // green, orange, blue, red
        private String riskLevel;
        private String riskTheme; // green, orange, red
    }
}
