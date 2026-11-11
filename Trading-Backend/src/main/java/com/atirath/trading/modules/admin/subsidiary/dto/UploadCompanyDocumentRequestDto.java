package com.atirath.trading.modules.admin.subsidiary.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadCompanyDocumentRequestDto {
    private String documentName;
    private String category; // Statutory, Regulatory, Corporate, Financial, Others
    private String documentType;
    private String fileType; // PDF, DOCX
    private String fileName;
    private String fileSize;
    private String expiryDate;
    private String documentNumber;
    private String issuedBy;
    private String description;
}
