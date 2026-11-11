package com.atirath.trading.modules.admin.system.dto;

import com.atirath.trading.modules.admin.parent.dto.AdminUserHeaderDto;
import com.atirath.trading.modules.admin.parent.dto.KpiCardDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserManagementPageDto {
    private AdminUserHeaderDto adminUser;
    private String pageTitle;
    private String pageSubtitle;
    private String selectedCompany;

    // 5 KPIs
    private List<KpiCardDto> kpis;

    // Tabs
    private List<String> tabs;
    private String activeTab;

    // Filter Options
    private List<String> companies;
    private List<String> departments;
    private List<String> roles;
    private List<String> statuses;

    // Table rows
    private List<UserItemDto> users;
    private Integer totalUsers;
    private Integer currentPage;
    private Integer totalPages;
    private Integer pageSize;

    // Bottom Visualizations
    private UsersByRoleDonutDto usersByRole;
    private List<UserStatusBarDto> usersByStatus;
    private List<UserTrendMonthDto> userRegistrationTrend;

    // Preselected Drawer Detail
    private UserDetailDto selectedUserDetail;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserItemDto {
        private String id;
        private Integer number;
        private String initials;
        private String userName;
        private String email;
        private String company;
        private String department;
        private String role;
        private String status;
        private String statusTheme; // green, red, orange
        private String lastLogin;
        private String createdOn;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UsersByRoleDonutDto {
        private String totalUsers;
        private List<RoleSliceDto> slices;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RoleSliceDto {
        private String role;
        private Double percentage;
        private Integer count;
        private String color;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserStatusBarDto {
        private String status;
        private Double percentage;
        private Integer count;
        private String color;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserTrendMonthDto {
        private String month;
        private Integer count;
    }
}
