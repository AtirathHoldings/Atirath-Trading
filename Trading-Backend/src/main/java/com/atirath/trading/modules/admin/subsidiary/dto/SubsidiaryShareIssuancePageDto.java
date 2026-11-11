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
public class SubsidiaryShareIssuancePageDto {

    private AdminUserHeaderDto adminUser;
    private String pageTitle;
    private String pageSubtitle;
    private String selectedCompany;
    private String companyStatus;

    // 4 KPIs
    private List<KpiCardDto> kpis;

    // Tabs: Issuance Requests (6), Approved Issuances (10), All Issuances (16)
    private List<String> tabs;
    private String activeTab;

    // Table: Share Issuances
    private List<ShareIssuanceItemDto> requests;
    private int totalRequests;
    private int currentPage;
    private int totalPages;

    // Middle Chart 1: Issuance Trend
    private List<MonthlyIssuancePointDto> issuanceTrend;

    // Middle Chart 2: Share Class-wise Issuance
    private ShareClassWiseIssuanceDonutDto shareClassWiseIssuance;

    // Bottom Widget 1: Recent Approvals
    private List<IssuanceApprovalDto> recentApprovals;

    // Bottom Widget 2: Activity Log
    private List<IssuanceActivityLogDto> activityLog;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShareIssuanceItemDto {
        private String id;
        private int number;
        private String requestId;
        private String shareClass;
        private String noOfShares;
        private String faceValue;
        private String issuePrice;
        private String totalAmount;
        private String investor;
        private String status; // "Pending Approval", "Under Review", "Pending Documents", "Approved", "Issued"
        private String statusTheme; // "orange", "blue", "pink", "green", "teal"
        private String requestedOn;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyIssuancePointDto {
        private String month;
        private double sharesLakhs;
        private double capitalRaisedCr;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShareClassWiseIssuanceDonutDto {
        private String totalShares;
        private List<ShareClassIssuanceSliceDto> slices;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShareClassIssuanceSliceDto {
        private String shareClass;
        private double percentage;
        private String sharesFormatted;
        private String color;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IssuanceApprovalDto {
        private int number;
        private String requestId;
        private String shareClass;
        private String noOfShares;
        private String investor;
        private String approvedOn;
        private String approvedBy;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IssuanceActivityLogDto {
        private String dateTime;
        private String activity;
        private String user;
    }
}
