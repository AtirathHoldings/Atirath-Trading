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
public class SubsidiaryDetailDto {
    private AdminUserHeaderDto adminUser;
    
    // Header Info
    private String id;
    private String companyName;
    private String initials;
    private String status;
    private String subIndustry;
    private String cin;
    private String incorporationDate;
    private String headquarters;
    private String sector;
    private int totalMembers;
    private String totalTradeValue;

    // Available Tabs
    private List<String> availableTabs;
    private String activeTab;

    // Overview KPIs
    private List<KpiCardDto> overviewKpis;

    // Company Information Card
    private CompanyInfoDto companyInfo;

    // Key Metrics (Last 6 Months)
    private KeyMetricsDto keyMetrics;

    // Recent Activities
    private List<SubsidiaryActivityDto> recentActivities;

    // Compliance Status
    private List<ComplianceItemDto> complianceStatus;

    // Tab-specific Data Models
    private FinancialsTabDto financials;
    private ShareholdingTabDto shareholding;
    private MembersTabDto members;
    private TradingActivityTabDto tradingActivity;
    private ComplianceTabDto compliance;
    private DocumentsTabDto documents;
    private SettingsTabDto settings;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompanyInfoDto {
        private String companyName;
        private String cin;
        private String incorporationDate;
        private String sector;
        private String industry;
        private String registeredAddress;
        private String headquarters;
        private String pan;
        private String gstNumber;
        private String status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KeyMetricsDto {
        private String filterRange; // "Last 6 Months"
        private MetricChartDto tradeVolume;
        private MetricChartDto settlementValue;
        private MetricChartDto activeMembers;
        private MetricChartDto newMembers;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MetricChartDto {
        private String title;
        private String currentValue;
        private List<MonthlyDataPointDto> monthlyData;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyDataPointDto {
        private String month;
        private Double value;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubsidiaryActivityDto {
        private String dateTime;
        private String activity;
        private String performedBy;
        private String status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComplianceItemDto {
        private String title;
        private String description;
        private String status; // "Compliant"
    }

    // ==========================================
    // TAB DATA MODELS
    // ==========================================
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FinancialsTabDto {
        private List<KpiCardDto> kpis;
        private MetricChartDto revenueTrend;
        private MetricChartDto profitTrend;
        private List<FinancialStatementDto> statements;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FinancialStatementDto {
        private String period;
        private String revenue;
        private String expenses;
        private String netProfit;
        private String profitMargin;
        private String filingStatus;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShareholdingTabDto {
        private String authorizedCapital;
        private String issuedCapital;
        private String paidUpCapital;
        private String totalShares;
        private String faceValue;
        private List<ShareholderItemDto> topShareholders;
        private List<CategoryShareDto> categoryBreakdown;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShareholderItemDto {
        private String shareholderName;
        private String category;
        private String shares;
        private Double percentage;
        private String status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryShareDto {
        private String category;
        private Double percentage;
        private String color;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MembersTabDto {
        private int totalMembers;
        private int activeMembers;
        private int pendingKyc;
        private int corporateMembers;
        private int individualMembers;
        private List<MemberItemDto> memberList;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemberItemDto {
        private String id;
        private String memberName;
        private String type;
        private String email;
        private String joinDate;
        private String kycStatus;
        private String status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TradingActivityTabDto {
        private int totalTrades;
        private String tradeVolume;
        private String buyVolume;
        private String sellVolume;
        private int openOrdersCount;
        private List<SubsidiaryTradeItemDto> recentTrades;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubsidiaryTradeItemDto {
        private String tradeId;
        private String dateTime;
        private String orderType;
        private String quantity;
        private String price;
        private String totalValue;
        private String status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComplianceTabDto {
        private String overallStatus;
        private String lastAuditDate;
        private String nextFilingDueDate;
        private List<ComplianceItemDto> items;
        private List<ComplianceRecordDto> records;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComplianceRecordDto {
        private String filingType;
        private String dueDate;
        private String submittedDate;
        private String acknowledgementNo;
        private String status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DocumentsTabDto {
        private int totalDocuments;
        private int verifiedDocuments;
        private int pendingVerification;
        private List<DocumentItemDto> documentList;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DocumentItemDto {
        private String id;
        private String documentName;
        private String category;
        private String fileType;
        private String uploadDate;
        private String status;
        private String downloadUrl;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SettingsTabDto {
        private String tradingStatus;
        private String minOrderQuantity;
        private String maxOrderQuantity;
        private String circuitBreakerPercentage;
        private String primaryContactName;
        private String primaryContactEmail;
        private String primaryContactPhone;
    }
}
