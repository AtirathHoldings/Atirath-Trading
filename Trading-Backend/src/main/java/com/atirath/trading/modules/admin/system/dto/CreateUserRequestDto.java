package com.atirath.trading.modules.admin.system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequestDto {
    private String fullName;
    private String email;
    private String phone;
    private String company;
    private String department;
    private String role;
    private String reportingManager;
    private String location;
    private String timeZone;
    private String accessLevel;
}
