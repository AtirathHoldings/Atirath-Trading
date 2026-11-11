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
public class RunTransactionScreeningRequestDto {
    private String entity;
    private String dateRange;
    private List<String> screeningChecks; // PEP, Sanctions, Adverse Media, Thresholds
    private Double thresholdAmount;
}
