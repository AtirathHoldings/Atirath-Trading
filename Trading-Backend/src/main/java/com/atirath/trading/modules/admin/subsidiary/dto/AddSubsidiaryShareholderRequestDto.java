package com.atirath.trading.modules.admin.subsidiary.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddSubsidiaryShareholderRequestDto {
    private String shareholderName;
    private String category; // Institutional, Individual, Foreign
    private String type; // VC Fund, PE Fund, Strategic, Corporate, Individual, Promoter
    private String noOfShares;
    private double ownershipPercentage;
    private double investmentCr;
    private String pan;
    private String country;
    private String registeredAddress;
    private String contactPerson;
    private String email;
    private String phone;
}
