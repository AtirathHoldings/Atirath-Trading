package com.atirath.trading.modules.admin.subsidiary.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyDocumentDetailDto {
    private String id;
    private String documentName;
    private String fileType;
    private String status;
    private String statusTheme;
    private String categoryBadge;
    private String documentTypeBadge;

    private List<String> tabs;
    private String activeTab;

    private String category;
    private String documentType;
    private String fileName;
    private String fileSize;
    private String uploadedOn;
    private String uploadedBy;
    private String expiryDate;
    private String documentNumber;
    private String issuedBy;
    private String description;
    private String downloadUrl;
}
