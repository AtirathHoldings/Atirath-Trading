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
public class RoleDetailDto {
    private String id;
    private String initials;
    private String roleName;
    private String roleType;
    private String description;
    private String status;
    private String statusTheme;

    private List<String> tabs;
    private String activeTab;

    // Basic Information
    private String accessLevel;
    private String accessLevelTheme;
    private String createdOn;
    private String createdBy;
    private String lastUpdated;
    private Integer totalPermissions;
    private Integer usersAssigned;

    // Access Summary
    private Integer modulesCount;
    private Integer permissionsCount;
    private Integer usersCount;
    private Integer conditionsCount;

    // Recent Activity
    private List<RoleActivityDto> recentActivities;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RoleActivityDto {
        private String title;
        private String timeAndUser;
        private String dotColor;
    }
}
