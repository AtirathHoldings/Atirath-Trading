package com.atirath.trading.modules.admin.parent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddInvestorRequestDto {
    private String investorName;
    private String investorType;
    private String contactEmail;
    private String contactPhone;
    private String company;
    private String sector;
    private String investmentType;
    private String proposedAmount;
    private String proposedEquity;
    private String purpose;
    private String description;
}
