package com.atirath.trading.modules.admin.parent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubsidiarySummaryDto {
    private String id;
    private int number;
    private String companyName;
    private String initials;
    private String subIndustry;
    private String sector;
    private String incorporationDate;
    private String cin;
    private String headquarters;
    private String status; // "Active", "Under Setup", "Inactive"
    private int totalMembers;
    private String totalTradeValue;
}
