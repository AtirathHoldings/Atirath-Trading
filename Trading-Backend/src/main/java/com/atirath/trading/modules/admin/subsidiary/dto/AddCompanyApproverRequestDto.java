package com.atirath.trading.modules.admin.subsidiary.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddCompanyApproverRequestDto {
    private String fullName;
    private String designation;
    private String department;
    private String email;
    private String phone;
    private String approvalLimit;
    private String approvalTypes;
    private String effectiveFrom;
    private String remarks;
}
