package com.atirath.trading.modules.admin.subsidiary.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateComplianceAlertRequestDto {
    private String entity;
    private String alertType;
    private String riskLevel; // High, Medium, Low
    private String description;
    private String detectionSource;
    private String investor;
    private String fromParty;
    private String toParty;
    private String amount;
}
