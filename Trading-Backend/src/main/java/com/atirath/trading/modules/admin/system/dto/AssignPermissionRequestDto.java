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
public class AssignPermissionRequestDto {
    private String permissionName;
    private String module;
    private String permissionType; // Read, Create, Update, Delete, Manage, Approve
    private String description;
    private List<String> roleNames;
    private String permissionCode;
}
