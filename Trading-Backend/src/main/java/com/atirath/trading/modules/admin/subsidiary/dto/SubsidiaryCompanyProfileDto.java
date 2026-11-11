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
public class SubsidiaryCompanyProfileDto {

    private AdminUserHeaderDto adminUser;
    private String pageTitle;
    private String pageSubtitle;
    private String selectedCompany;
    private String status;

    private CompanyHeroDto hero;

    private List<String> tabs;
    private String activeTab;

    // 5 KPIs
    private List<KpiCardDto> kpis;

    // Left Column: Company Information
    private CompanyFullInformationDto companyInformation;

    // Middle Column: Shareholding Pattern & Key Management
    private ShareholdingPatternDto shareholdingPattern;
    private List<ManagementPersonDto> keyManagement;

    // Right Column: Financial Summary & Recent Activities
    private List<YearlyFinancialDto> financialSummary;
    private List<ProfileActivityItemDto> recentActivities;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompanyHeroDto {
        private String companyName;
        private String status;
        private String cin;
        private String incorporationDate;
        private String description;
        private String companyType;
        private String industry;
        private String headOffice;
        private String registeredOffice;
        private String website;
        private String imageUrl;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompanyFullInformationDto {
        private String companyName;
        private String cin;
        private String pan;
        private String gstin;
        private String incorporationDate;
        private String companyType;
        private String industry;
        private String authorisedCapital;
        private String paidUpCapital;
        private String registeredOffice;
        private String headOffice;
        private String website;
        private String email;
        private String phone;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShareholdingPatternDto {
        private String totalShares;
        private List<ShareholdingSliceDto> slices;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShareholdingSliceDto {
        private String category;
        private double percentage;
        private String color;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ManagementPersonDto {
        private String name;
        private String initials;
        private String designation;
        private String email;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class YearlyFinancialDto {
        private String year;
        private double revenue;
        private double ebitda;
        private double netProfit;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProfileActivityItemDto {
        private String dateTime;
        private String activity;
        private String user;
    }
}
