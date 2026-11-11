package com.atirath.trading.modules.admin.system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GrantAccessRequestDto {
    private String userName;
    private String company;
    private String role;
    private String accessLevel; // Full Access, Limited, Read Only
    private List<String> modules;
    private String expiresOn;
}
