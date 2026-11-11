package com.atirath.trading.modules.admin.subsidiary.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestrictUserRequestDto {
    private String fullName;
    private String entity;
    private String role; // Investor, Shareholder, Director, Employee
    private String email;
    private String phone;
    private String restrictionType; // Trading Restriction, Corporate Action Restriction, System Access Restriction, Temporary Restriction, Permanent Restriction
    private String reason;
    private String duration;
    private String notes;
}
