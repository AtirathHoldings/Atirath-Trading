package com.atirath.trading.modules.admin.subsidiary.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateSubsidiaryReportRequestDto {
    private String reportName;
    private String category;
    private String dateRange;
    private String format; // PDF, Excel
    private String parameters;
}
