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
public class SubsidiaryComplianceDashboardDto {

    private AdminUserHeaderDto adminUser;
    private String pageTitle;
    private String pageSubtitle;
    private String selectedCompany;
    private String dateRange;

    // 4 Top KPI Cards
    private List<KpiCardDto> kpis;

    // Chart 1: Compliance Status by Subsidiary (Stacked Bar)
    private List<ComplianceStatusBySubsidiaryDto> complianceStatusBySubsidiary;

    // Chart 2: KYC Status (Donut)
    private KycStatusDonutDto kycStatus;

    // Chart 3: Compliance Cases (Donut)
    private ComplianceCasesDonutDto complianceCases;

    // Bottom 4 Card Tables
    private List<RecentComplianceAlertDto> recentComplianceAlerts;
    private List<RegulatoryFilingDueDto> regulatoryFilingsDue;
    private List<OpenComplianceCaseDto> openComplianceCases;
    private List<PendingApprovalDto> pendingApprovals;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComplianceStatusBySubsidiaryDto {
        private String entityName;
        private int total;
        private int compliant;
        private int underReview;
        private int nonCompliant;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KycStatusDonutDto {
        private int totalInvestors;
        private List<KycStatusSliceDto> slices;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KycStatusSliceDto {
        private String label;
        private double percentage;
        private int count;
        private String color;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComplianceCasesDonutDto {
        private int openCases;
        private List<ComplianceCaseSliceDto> slices;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComplianceCaseSliceDto {
        private String label;
        private double percentage;
        private int count;
        private String color;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentComplianceAlertDto {
        private String id;
        private int number;
        private String dateTime;
        private String entity;
        private String type;
        private String riskLevel;
        private String riskTheme; // red, orange, green
        private String description;
        private String status;
        private String statusTheme;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegulatoryFilingDueDto {
        private String id;
        private int number;
        private String filingType;
        private String entity;
        private String dueDate;
        private String status;
        private String statusTheme;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OpenComplianceCaseDto {
        private String id;
        private int number;
        private String caseId;
        private String entity;
        private String type;
        private String assignedTo;
        private String openedOn;
        private String status;
        private String statusTheme;
        private String priority;
        private String priorityTheme;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PendingApprovalDto {
        private String id;
        private int number;
        private String requestType;
        private String entity;
        private String requestedOn;
        private String status;
        private String statusTheme;
    }
}
