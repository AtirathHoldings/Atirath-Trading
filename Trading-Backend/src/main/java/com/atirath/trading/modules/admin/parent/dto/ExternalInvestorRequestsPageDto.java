package com.atirath.trading.modules.admin.parent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExternalInvestorRequestsPageDto {
    private AdminUserHeaderDto adminUser;
    private String pageTitle;
    private String pageSubtitle;
    private String dateRange; // "01 Sep 2026 - 16 Sep 2026"

    // 5 Top KPI Cards
    private List<KpiCardDto> kpis;

    // Tabs
    private List<ApprovalsPageDto.TabCountDto> tabs;
    private String activeTab;

    // Investor Requests Table
    private List<InvestorRequestItemDto> requests;
    private int totalRequests;
    private int currentPage;
    private int totalPages;

    // Selected Drawer Request Detail
    private InvestorRequestDetailDto selectedRequestDetail;

    // Bottom Widgets
    private InvestmentRequestsByCompanyDonutDto requestsByCompany;
    private ApprovalsPageDto.ApprovalTrendChartDto statusTrend;
    private List<TopInvestorTypeBarDto> topInvestorTypes;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InvestorRequestItemDto {
        private String id;
        private int number;
        private String requestId; // "INV-2026-0036"
        private String dateTime;
        private String investorName;
        private String company;
        private String sector;
        private String investmentAmount;
        private String status; // "Pending", "Under Review", "Approved", "Rejected"
        private String statusTheme;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InvestmentRequestsByCompanyDonutDto {
        private String totalRequests;
        private List<CompanyRequestSliceDto> slices;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompanyRequestSliceDto {
        private String company;
        private Integer count;
        private Double percentage;
        private String color;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopInvestorTypeBarDto {
        private String investorType;
        private Integer count;
        private Double percentage;
        private String color;
    }
}
