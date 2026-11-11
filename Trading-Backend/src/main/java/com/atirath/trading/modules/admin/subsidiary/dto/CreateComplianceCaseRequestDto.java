package com.atirath.trading.modules.admin.subsidiary.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateComplianceCaseRequestDto {
    private String title;
    private String entity;
    private String caseType; // Market Abuse, KYC/AML, Market Manipulation, Policy Violation, Sanctions, Regulatory Breach, Insider Trading, Ownership Violation, Fraud, Financial Irregularity
    private String riskLevel; // Critical, High, Medium, Low
    private String priority; // High, Medium, Low
    private String assignedTo;
    private String dueDate;
    private String description;
}
