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
public class CreateRoleRequestDto {
    private String roleName;
    private String roleType; // System, Custom
    private String description;
    private String accessLevel; // Full Access, High, Medium, Low
    private List<String> permissions;
}
