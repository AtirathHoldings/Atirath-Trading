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
public class SubsidiaryDocumentsPageDto {

    private AdminUserHeaderDto adminUser;
    private String pageTitle;
    private String pageSubtitle;
    private String selectedCompany;
    private String companyStatus;

    // 4 KPIs
    private List<KpiCardDto> kpis;

    // Tabs: All Documents (24), Statutory (8), Regulatory (5), Corporate (6), Financial (3), Others (2)
    private List<String> tabs;
    private String activeTab;

    // Table: Documents
    private List<CompanyDocumentItemDto> documents;
    private int totalDocuments;
    private int currentPage;
    private int totalPages;

    // Right Drawer Detail
    private CompanyDocumentDetailDto selectedDocumentDetail;

    // Bottom Widget 1: Documents by Category Donut
    private DocumentsByCategoryDonutDto documentsByCategory;

    // Bottom Widget 2: Document Status Donut
    private DocumentStatusDonutDto documentStatus;

    // Bottom Widget 3: Expiry Timeline Bar Chart
    private List<ExpiryTimelineBarDto> expiryTimeline;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompanyDocumentItemDto {
        private String id;
        private int number;
        private String documentName;
        private String category; // Statutory, Corporate, Financial, Regulatory, Others
        private String categoryTheme; // purple, blue, green, orange
        private String documentType; // Incorporation, Governing Document, Tax Document, Board Resolution, Agreement, Financial Statement, Certification, Compliance
        private String fileType; // PDF, DOCX
        private String uploadedOn;
        private String expiryDate;
        private String status; // Verified, Active, Expiring Soon, Expired
        private String statusTheme; // green, green, orange, red
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DocumentsByCategoryDonutDto {
        private int totalDocuments;
        private List<DocumentCategorySliceDto> slices;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DocumentCategorySliceDto {
        private String category;
        private int count;
        private double percentage;
        private String color;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DocumentStatusDonutDto {
        private int totalDocuments;
        private List<DocStatusSliceDto> slices;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DocStatusSliceDto {
        private String status;
        private int count;
        private double percentage;
        private String color;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExpiryTimelineBarDto {
        private String range;
        private int count;
        private String color;
    }
}
