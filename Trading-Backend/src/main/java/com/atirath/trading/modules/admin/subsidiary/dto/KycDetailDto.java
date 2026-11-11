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
public class KycDetailDto {

    private String id;
    private String requestId;
    private String requestDate;
    private String fullName;
    private String initials;
    private String entityType;
    private String status;
    private String statusTheme;

    // Tabs inside drawer
    private List<String> tabs;
    private String activeTab;

    // Basic Information
    private String dateOfBirth;
    private String email;
    private String phone;
    private String address;

    // Identity Information
    private String pan;
    private String aadhaar;
    private String nationality;
    private String occupation;
    private String sourceOfFunds;

    // Risk Assessment
    private String riskLevel;
    private String riskTheme;
    private String pepStatus;
    private String sanctionsCheck;
    private String adverseMedia;

    // Documents list for Documents tab
    private List<KycDocumentDto> documents;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KycDocumentDto {
        private String documentType;
        private String documentNumber;
        private String fileName;
        private String status;
        private String verifiedOn;
    }
}
