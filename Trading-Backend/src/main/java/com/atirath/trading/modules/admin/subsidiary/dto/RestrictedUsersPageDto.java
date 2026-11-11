package com.atirath.trading.modules.admin.subsidiary.dto;

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
public class RestrictedUsersPageDto {

    private AdminUserHeaderDto adminUser;
    private String pageTitle;
    private String pageSubtitle;
    private String selectedCompany;
    private String dateRange;

    // 4 Top KPI Cards
    private List<KpiCardDto> kpis;

    // Tabs
    private List<String> tabs;
    private String activeTab;

    // Filter Dropdowns
    private List<String> entities;
    private List<String> restrictionTypes;
    private List<String> statuses;

    // Table items
    private List<RestrictedUserItemDto> users;
    private int totalUsers;
    private int currentPage;
    private int totalPages;
    private int pageSize;

    // Selected Drawer item
    private RestrictedUserDetailDto selectedUserDetail;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RestrictedUserItemDto {
        private String id;
        private int number;
        private String userId;
        private String name;
        private String entity;
        private String role;
        private String restrictionType;
        private String restrictionTypeTheme; // red, orange, blue
        private String reason;
        private String restrictedOn;
        private String status;
        private String statusTheme; // red, orange, green
    }
}
