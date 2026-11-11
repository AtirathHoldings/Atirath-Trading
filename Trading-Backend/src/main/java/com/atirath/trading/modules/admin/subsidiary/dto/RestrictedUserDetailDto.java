package com.atirath.trading.modules.admin.subsidiary.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestrictedUserDetailDto {

    private String id;
    private String userId;
    private String fullName;
    private String initials;
    private String role;
    private String entity;
    private String status;
    private String statusTheme;
    private String restrictedOnDate;

    // Tabs inside drawer
    private List<String> tabs;
    private String activeTab;

    // Basic Information
    private String email;
    private String phone;

    // Restriction Information
    private String restrictionType;
    private String restrictionTypeTheme;
    private String restrictedOn;
    private String restrictedBy;
    private String reason;
    private String duration;
    private String notes;

    // Additional Information
    private String kycStatus;
    private String pepStatus;
    private String sanctionsCheck;
}
