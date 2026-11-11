package com.atirath.trading.modules.admin.subsidiary.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateKycRequestDto {
    private String fullName;
    private String entityType; // Individual Investor, Corporate Investor, Director / KMP, Employee
    private String panOrCin;
    private String email;
    private String phone;
    private String dateOfBirth;
    private String address;
    private String aadhaar;
    private String occupation;
    private String sourceOfFunds;
    private String riskLevel;
}
