package com.atirath.trading.modules.admin.subsidiary.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddShareClassRequestDto {
    private String className;
    private String classType; // Equity, Preference
    private double faceValue;
    private String issuedShares;
    private String authorizedShares;
    private String votingRights;
    private String dividendRights;
    private String transferability;
    private String investorCategory;
    private String description;
}
