package com.atirath.trading.modules.admin.parent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddSubsidiaryRequestDto {
    private String companyName;
    private String subIndustry;
    private String sector;
    private String incorporationDate;
    private String cin;
    private String headquarters;
    private String status; // "Active", "Under Setup"
    private Integer initialMembers;
    private String initialTradeValue;
    private Double authorizedCapitalCr;
    private Double issuedCapitalCr;
    private Double paidUpCapitalCr;
}
