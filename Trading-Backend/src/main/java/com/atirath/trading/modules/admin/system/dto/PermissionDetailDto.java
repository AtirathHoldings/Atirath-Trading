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
public class PermissionDetailDto {
    private String id;
    private String permissionName;
    private String permissionId;
    private String permissionTypeTitle;
    private String status;
    private String statusTheme;

    private List<String> tabs;
    private String activeTab;

    // Basic Information
    private String module;
    private String permissionType;
    private String description;
    private String createdOn;
    private String createdBy;
    private String lastUpdated;
    private String updatedBy;

    // Assignment Summary
    private Integer rolesCount;
    private Integer usersCount;
    private Integer conditionsCount;

    // Related Information
    private String parentPermission;
    private String dependencies;
    private String permissionCode;
}
