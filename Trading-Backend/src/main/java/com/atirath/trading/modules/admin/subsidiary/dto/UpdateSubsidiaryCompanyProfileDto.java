package com.atirath.trading.modules.admin.subsidiary.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSubsidiaryCompanyProfileDto {
    private String companyName;
    private String description;
    private String industry;
    private String companyType;
    private String pan;
    private String gstin;
    private String authorisedCapital;
    private String paidUpCapital;
    private String registeredOffice;
    private String headOffice;
    private String website;
    private String email;
    private String phone;
}
