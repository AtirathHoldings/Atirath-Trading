package com.atirath.trading.modules.admin.subsidiary.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitShareIssuanceRequestDto {
    private String shareClass;
    private String numberOfShares;
    private double faceValue;
    private double issuePrice;
    private double totalAmount;
    private String investor;
    private String issueDate;
    private String allotmentType;
    private String remarks;
}
