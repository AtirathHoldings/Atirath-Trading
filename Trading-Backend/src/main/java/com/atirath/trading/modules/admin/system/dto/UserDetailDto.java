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
public class UserDetailDto {
    private String id;
    private String initials;
    private String fullName;
    private String title;
    private String email;
    private String status;
    private String statusTheme;

    private List<String> tabs;
    private String activeTab;

    // Basic Information
    private String phone;
    private String employeeId;
    private String department;
    private String company;
    private String reportingManager;
    private String location;
    private String timeZone;
    private String createdOn;

    // Account Status
    private String lastLogin;
    private Integer loginAttempts;
    private String passwordLastChanged;
    private String mfaEnabled;
    private String accountExpires;

    // Role Information
    private String primaryRole;
    private String additionalRoles;
    private String accessLevel;
}
