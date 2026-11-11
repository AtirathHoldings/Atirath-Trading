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
public class CompanyApproverDetailDto {
    private String id;
    private String approverName;
    private String initials;
    private String designation;
    private String department;
    private String status;
    private String statusTheme;

    private List<String> tabs;
    private String activeTab;

    private String fullName;
    private String email;
    private String phone;
    private String approvalLimit;
    private String approvalTypes;
    private String effectiveFrom;
    private String createdBy;
    private String createdOn;
    private String lastUpdated;
    private String remarks;
}
