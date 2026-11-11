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
public class AccessDetailDto {
    private String id;
    private String initials;
    private String userName;
    private String role;
    private String email;
    private String status;
    private String statusTheme;

    private List<String> tabs;
    private String activeTab;

    // Basic Information
    private String userId;
    private String department;
    private String company;
    private String accessLevel;
    private String accessLevelTheme;
    private String grantedOn;
    private String expiresOn;
    private String lastUpdated;
    private String updatedBy;

    // Module Access List
    private List<ModuleAccessItemDto> moduleAccessList;

    // Role Access
    private String primaryRole;
    private String additionalRoles;
    private String inheritedRoles;

    // Recent Activity
    private List<AccessActivityDto> recentActivities;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ModuleAccessItemDto {
        private String moduleName;
        private String accessLevel;
        private String theme; // green, blue, yellow, red
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AccessActivityDto {
        private String title;
        private String timeAndUser;
        private String dotColor;
    }
}
