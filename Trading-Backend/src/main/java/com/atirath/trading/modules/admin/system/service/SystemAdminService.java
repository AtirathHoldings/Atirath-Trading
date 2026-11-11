package com.atirath.trading.modules.admin.system.service;

import com.atirath.trading.modules.admin.parent.dto.AdminUserHeaderDto;
import com.atirath.trading.modules.admin.parent.dto.KpiCardDto;
import com.atirath.trading.modules.admin.system.dto.*;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SystemAdminService {

    private AdminUserHeaderDto adminUser;

    // 1. User Management Store
    private final List<UserManagementPageDto.UserItemDto> usersList = new CopyOnWriteArrayList<>();
    private final Map<String, UserDetailDto> userDetailsMap = new ConcurrentHashMap<>();
    private final AtomicInteger userCounter = new AtomicInteger(10);

    // 2. Role Management Store
    private final List<RoleManagementPageDto.RoleItemDto> rolesList = new CopyOnWriteArrayList<>();
    private final Map<String, RoleDetailDto> roleDetailsMap = new ConcurrentHashMap<>();
    private final AtomicInteger roleCounter = new AtomicInteger(10);

    // 3. Permission Management Store
    private final List<PermissionManagementPageDto.PermissionItemDto> permissionsList = new CopyOnWriteArrayList<>();
    private final Map<String, PermissionDetailDto> permissionDetailsMap = new ConcurrentHashMap<>();
    private final AtomicInteger permissionCounter = new AtomicInteger(10);

    // 4. Access Control Store
    private final List<AccessControlPageDto.AccessItemDto> accessList = new CopyOnWriteArrayList<>();
    private final Map<String, AccessDetailDto> accessDetailsMap = new ConcurrentHashMap<>();
    private final AtomicInteger accessCounter = new AtomicInteger(10);

    // 5. Approval Workflow Store
    private final List<ApprovalWorkflowPageDto.WorkflowItemDto> workflowsList = new CopyOnWriteArrayList<>();
    private final Map<String, WorkflowDetailDto> workflowDetailsMap = new ConcurrentHashMap<>();
    private final AtomicInteger workflowCounter = new AtomicInteger(10);

    // 6. Trading Rule Store
    private final List<TradingRuleConfigPageDto.TradingRuleItemDto> tradingRulesList = new CopyOnWriteArrayList<>();
    private final Map<String, TradingRuleDetailDto> tradingRuleDetailsMap = new ConcurrentHashMap<>();
    private final AtomicInteger tradingRuleCounter = new AtomicInteger(10);

    // 7. Notification Configuration Store
    private final List<NotificationConfigPageDto.NotificationItemDto> notificationsList = new CopyOnWriteArrayList<>();
    private final Map<String, NotificationDetailDto> notificationDetailsMap = new ConcurrentHashMap<>();
    private final AtomicInteger notificationCounter = new AtomicInteger(10);

    // 8. System Settings Store
    private final List<SystemSettingsPageDto.SettingItemDto> settingsList = new CopyOnWriteArrayList<>();
    private final Map<String, SystemSettingDetailDto> settingDetailsMap = new ConcurrentHashMap<>();
    private final AtomicInteger settingCounter = new AtomicInteger(10);

    // 9. Audit Logs Store
    private final List<AuditActivityLogsPageDto.AuditLogItemDto> auditLogsList = new CopyOnWriteArrayList<>();
    private final Map<String, AuditLogDetailDto> auditLogDetailsMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        log.info("[SystemAdminService] Initializing System Administration datasets matching UI screenshots...");

        adminUser = AdminUserHeaderDto.builder()
                .name("Admin User")
                .initials("AD")
                .role("System Administration")
                .currentDate("01 Jan 2026 - 30 Sep 2026")
                .unreadNotifications(3)
                .build();

        seedUsers();
        seedRoles();
        seedPermissions();
        seedAccess();
        seedWorkflows();
        seedTradingRules();
        seedNotifications();
        seedSettings();
        seedAuditLogs();

        log.info("[SystemAdminService] Initialization completed. Ready for System Administration requests.");
    }

    // ==========================================
    // 1. USER MANAGEMENT
    // ==========================================
    public UserManagementPageDto getUserManagementPage(String company, String tab, String query, String department, String role, String status) {
        List<KpiCardDto> kpis = List.of(
                KpiCardDto.builder().title("Total Users").value("128").change("+12% vs last quarter").comparisonText("").trend("positive").icon("users").color("green").build(),
                KpiCardDto.builder().title("Active Users").value("112").change("+15% vs last quarter").comparisonText("").trend("positive").icon("user-check").color("blue").build(),
                KpiCardDto.builder().title("Inactive Users").value("12").change("-25% vs last quarter").comparisonText("").trend("neutral").icon("user-minus").color("red").build(),
                KpiCardDto.builder().title("Pending Activation").value("4").change("-33% vs last quarter").comparisonText("").trend("neutral").icon("clock").color("orange").build(),
                KpiCardDto.builder().title("Admin Users").value("16").change("+7% vs last quarter").comparisonText("").trend("positive").icon("lock").color("purple").build()
        );

        UserManagementPageDto.UsersByRoleDonutDto donut = UserManagementPageDto.UsersByRoleDonutDto.builder()
                .totalUsers("128 Total Users")
                .slices(List.of(
                        new UserManagementPageDto.RoleSliceDto("Operations Manager", 14.1, 18, "#ef4444"),
                        new UserManagementPageDto.RoleSliceDto("Trade Executive", 18.8, 24, "#3b82f6"),
                        new UserManagementPageDto.RoleSliceDto("Finance Executive", 15.6, 20, "#eab308"),
                        new UserManagementPageDto.RoleSliceDto("Compliance Officer", 12.5, 16, "#f97316"),
                        new UserManagementPageDto.RoleSliceDto("System Administrator", 9.4, 12, "#06b6d4"),
                        new UserManagementPageDto.RoleSliceDto("Others", 29.7, 38, "#14b8a6")
                )).build();

        List<UserManagementPageDto.UserStatusBarDto> statusBar = List.of(
                new UserManagementPageDto.UserStatusBarDto("Active", 87.5, 112, "#22c55e"),
                new UserManagementPageDto.UserStatusBarDto("Inactive", 9.4, 12, "#ef4444"),
                new UserManagementPageDto.UserStatusBarDto("Pending", 3.1, 4, "#eab308")
        );

        List<UserManagementPageDto.UserTrendMonthDto> trend = List.of(
                new UserManagementPageDto.UserTrendMonthDto("Apr 2026", 12),
                new UserManagementPageDto.UserTrendMonthDto("May 2026", 18),
                new UserManagementPageDto.UserTrendMonthDto("Jun 2026", 24),
                new UserManagementPageDto.UserTrendMonthDto("Jul 2026", 30),
                new UserManagementPageDto.UserTrendMonthDto("Aug 2026", 35),
                new UserManagementPageDto.UserTrendMonthDto("Sep 2026", 40)
        );

        List<UserManagementPageDto.UserItemDto> filtered = usersList.stream()
                .filter(u -> {
                    if (query != null && !query.isBlank()) {
                        String q = query.toLowerCase();
                        if (!u.getUserName().toLowerCase().contains(q) &&
                            !u.getEmail().toLowerCase().contains(q) &&
                            !u.getDepartment().toLowerCase().contains(q)) return false;
                    }
                    if (tab != null && !tab.isBlank() && !tab.equalsIgnoreCase("All Users")) {
                        if (tab.equalsIgnoreCase("Active Users") && !u.getStatus().equalsIgnoreCase("Active")) return false;
                        if (tab.equalsIgnoreCase("Inactive Users") && !u.getStatus().equalsIgnoreCase("Inactive")) return false;
                        if (tab.equalsIgnoreCase("Pending Activation") && !u.getStatus().equalsIgnoreCase("Pending")) return false;
                        if (tab.equalsIgnoreCase("Administrators") && !u.getRole().toLowerCase().contains("administrator")) return false;
                    }
                    if (department != null && !department.isBlank() && !department.equalsIgnoreCase("All Departments")) {
                        if (!u.getDepartment().equalsIgnoreCase(department)) return false;
                    }
                    if (role != null && !role.isBlank() && !role.equalsIgnoreCase("All Roles")) {
                        if (!u.getRole().equalsIgnoreCase(role)) return false;
                    }
                    if (status != null && !status.isBlank() && !status.equalsIgnoreCase("All Status")) {
                        if (!u.getStatus().equalsIgnoreCase(status)) return false;
                    }
                    return true;
                }).collect(Collectors.toList());

        UserDetailDto selected = userDetailsMap.getOrDefault("usr-1", userDetailsMap.values().iterator().next());

        return UserManagementPageDto.builder()
                .adminUser(adminUser)
                .pageTitle("User Management")
                .pageSubtitle("Create, view and manage system users, roles and access across the platform.")
                .selectedCompany(company != null ? company : "Atirath Holdings")
                .kpis(kpis)
                .tabs(List.of("All Users", "Active Users", "Inactive Users", "Pending Activation", "Administrators"))
                .activeTab(tab != null ? tab : "All Users")
                .companies(List.of("All Companies", "Atirath Holdings", "Farm Vantara", "Atirath Traders", "Sunrise Ventures", "Green Future Fund", "Global Agri Fund", "Atirath Technologies"))
                .departments(List.of("All Departments", "Operations", "Finance", "Trading", "Compliance", "Settlements", "Investor Relations", "IT"))
                .roles(List.of("All Roles", "Operations Manager", "Finance Executive", "Trade Analyst", "Compliance Officer", "Settlement Executive", "Investor Manager", "System Administrator", "Operations Executive", "Trade Executive", "Finance Manager"))
                .statuses(List.of("All Status", "Active", "Inactive", "Pending"))
                .users(filtered)
                .totalUsers(128)
                .currentPage(1)
                .totalPages(13)
                .pageSize(10)
                .usersByRole(donut)
                .usersByStatus(statusBar)
                .userRegistrationTrend(trend)
                .selectedUserDetail(selected)
                .build();
    }

    public UserDetailDto getUserDetail(String id) {
        return userDetailsMap.getOrDefault(id, userDetailsMap.get("usr-1"));
    }

    public UserDetailDto createUser(CreateUserRequestDto req) {
        int nextNum = userCounter.incrementAndGet();
        String id = "usr-" + nextNum;
        String[] parts = req.getFullName() != null ? req.getFullName().split(" ") : new String[]{"User", "New"};
        String initials = parts.length > 1 ? (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase() : parts[0].substring(0, 1).toUpperCase();

        UserManagementPageDto.UserItemDto item = UserManagementPageDto.UserItemDto.builder()
                .id(id)
                .number(nextNum)
                .initials(initials)
                .userName(req.getFullName() != null ? req.getFullName() : "New User")
                .email(req.getEmail() != null ? req.getEmail() : "user" + nextNum + "@atirath.com")
                .company(req.getCompany() != null ? req.getCompany() : "Atirath Holdings")
                .department(req.getDepartment() != null ? req.getDepartment() : "Operations")
                .role(req.getRole() != null ? req.getRole() : "Operations Executive")
                .status("Active")
                .statusTheme("green")
                .lastLogin("-")
                .createdOn("29 Sep 2026")
                .build();

        usersList.add(0, item);

        UserDetailDto detail = UserDetailDto.builder()
                .id(id)
                .initials(initials)
                .fullName(req.getFullName() != null ? req.getFullName() : "New User")
                .title(req.getRole() != null ? req.getRole() : "Operations Executive")
                .email(req.getEmail() != null ? req.getEmail() : "user" + nextNum + "@atirath.com")
                .status("Active")
                .statusTheme("green")
                .tabs(List.of("Details", "Roles & Permissions", "Activity Logs"))
                .activeTab("Details")
                .phone(req.getPhone() != null ? req.getPhone() : "+91 98765 00000")
                .employeeId("EMP-2026-" + String.format("%03d", nextNum))
                .department(req.getDepartment() != null ? req.getDepartment() : "Operations")
                .company(req.getCompany() != null ? req.getCompany() : "Atirath Holdings")
                .reportingManager(req.getReportingManager() != null ? req.getReportingManager() : "Priya Mehta")
                .location(req.getLocation() != null ? req.getLocation() : "Hyderabad, India")
                .timeZone(req.getTimeZone() != null ? req.getTimeZone() : "IST (UTC+5:30)")
                .createdOn("29 Sep 2026, 06:00 PM")
                .lastLogin("-")
                .loginAttempts(0)
                .passwordLastChanged("29 Sep 2026")
                .mfaEnabled("Yes")
                .accountExpires("-")
                .primaryRole(req.getRole() != null ? req.getRole() : "Operations Executive")
                .additionalRoles("Report Viewer")
                .accessLevel(req.getAccessLevel() != null ? req.getAccessLevel() : "Company Level")
                .build();

        userDetailsMap.put(id, detail);
        return detail;
    }

    public UserDetailDto resetPassword(String id) {
        UserDetailDto detail = getUserDetail(id);
        detail.setPasswordLastChanged("29 Sep 2026, 06:10 PM");
        detail.setLoginAttempts(0);
        return detail;
    }

    public UserDetailDto deactivateUser(String id) {
        UserDetailDto detail = getUserDetail(id);
        detail.setStatus("Inactive");
        detail.setStatusTheme("red");

        usersList.stream().filter(u -> u.getId().equalsIgnoreCase(id)).findFirst().ifPresent(u -> {
            u.setStatus("Inactive");
            u.setStatusTheme("red");
        });
        return detail;
    }

    public UserDetailDto activateUser(String id) {
        UserDetailDto detail = getUserDetail(id);
        detail.setStatus("Active");
        detail.setStatusTheme("green");

        usersList.stream().filter(u -> u.getId().equalsIgnoreCase(id)).findFirst().ifPresent(u -> {
            u.setStatus("Active");
            u.setStatusTheme("green");
        });
        return detail;
    }

    // ==========================================
    // 2. ROLE MANAGEMENT
    // ==========================================
    public RoleManagementPageDto getRoleManagementPage(String company, String tab, String query, String roleType, String status, String accessLevel) {
        List<KpiCardDto> kpis = List.of(
                KpiCardDto.builder().title("Total Roles").value("24").change("+20% vs last quarter").comparisonText("").trend("positive").icon("user-check").color("blue").build(),
                KpiCardDto.builder().title("Active Roles").value("22").change("+10% vs last quarter").comparisonText("").trend("positive").icon("shield").color("blue").build(),
                KpiCardDto.builder().title("Inactive Roles").value("2").change("-50% vs last quarter").comparisonText("").trend("neutral").icon("slash").color("red").build(),
                KpiCardDto.builder().title("Users Assigned").value("128").change("+18% vs last quarter").comparisonText("").trend("positive").icon("users").color("green").build()
        );

        RoleManagementPageDto.RolesByTypeDonutDto donut = RoleManagementPageDto.RolesByTypeDonutDto.builder()
                .totalRoles("24 Total Roles")
                .slices(List.of(
                        new RoleManagementPageDto.TypeSliceDto("System Role", 45.8, 11, "#3b82f6"),
                        new RoleManagementPageDto.TypeSliceDto("Custom Role", 37.5, 9, "#22c55e"),
                        new RoleManagementPageDto.TypeSliceDto("Predefined Role", 16.7, 4, "#f97316")
                )).build();

        List<RoleManagementPageDto.RoleAccessLevelBarDto> accessLevels = List.of(
                new RoleManagementPageDto.RoleAccessLevelBarDto("Full Access", 16.7, 4, "#ef4444"),
                new RoleManagementPageDto.RoleAccessLevelBarDto("High", 25.0, 6, "#f97316"),
                new RoleManagementPageDto.RoleAccessLevelBarDto("Medium", 41.7, 10, "#eab308"),
                new RoleManagementPageDto.RoleAccessLevelBarDto("Low", 16.7, 4, "#22c55e")
        );

        List<RoleManagementPageDto.RoleTrendMonthDto> trend = List.of(
                new RoleManagementPageDto.RoleTrendMonthDto("Apr 2026", 2),
                new RoleManagementPageDto.RoleTrendMonthDto("May 2026", 4),
                new RoleManagementPageDto.RoleTrendMonthDto("Jun 2026", 5),
                new RoleManagementPageDto.RoleTrendMonthDto("Jul 2026", 7),
                new RoleManagementPageDto.RoleTrendMonthDto("Aug 2026", 7),
                new RoleManagementPageDto.RoleTrendMonthDto("Sep 2026", 9)
        );

        List<RoleManagementPageDto.RoleItemDto> filtered = rolesList.stream()
                .filter(r -> {
                    if (query != null && !query.isBlank()) {
                        String q = query.toLowerCase();
                        if (!r.getRoleName().toLowerCase().contains(q) &&
                            !r.getDescription().toLowerCase().contains(q)) return false;
                    }
                    if (tab != null && !tab.isBlank() && !tab.equalsIgnoreCase("All Roles")) {
                        if (tab.equalsIgnoreCase("Active Roles") && !r.getStatus().equalsIgnoreCase("Active")) return false;
                        if (tab.equalsIgnoreCase("Inactive Roles") && !r.getStatus().equalsIgnoreCase("Inactive")) return false;
                        if (tab.equalsIgnoreCase("System Roles") && !r.getRoleType().equalsIgnoreCase("System")) return false;
                        if (tab.equalsIgnoreCase("Custom Roles") && !r.getRoleType().equalsIgnoreCase("Custom")) return false;
                    }
                    if (roleType != null && !roleType.isBlank() && !roleType.equalsIgnoreCase("All Role Types")) {
                        if (!r.getRoleType().equalsIgnoreCase(roleType)) return false;
                    }
                    if (status != null && !status.isBlank() && !status.equalsIgnoreCase("All Status")) {
                        if (!r.getStatus().equalsIgnoreCase(status)) return false;
                    }
                    if (accessLevel != null && !accessLevel.isBlank() && !accessLevel.equalsIgnoreCase("All Access Levels")) {
                        if (!r.getAccessLevel().equalsIgnoreCase(accessLevel)) return false;
                    }
                    return true;
                }).collect(Collectors.toList());

        RoleDetailDto selected = roleDetailsMap.getOrDefault("rol-3", roleDetailsMap.values().iterator().next());

        return RoleManagementPageDto.builder()
                .adminUser(adminUser)
                .pageTitle("Role Management")
                .pageSubtitle("Create, view and manage roles and their permissions across the platform.")
                .selectedCompany(company != null ? company : "Atirath Holdings")
                .kpis(kpis)
                .tabs(List.of("All Roles", "Active Roles", "Inactive Roles", "System Roles", "Custom Roles"))
                .activeTab(tab != null ? tab : "All Roles")
                .roleTypes(List.of("All Role Types", "System", "Custom", "Predefined"))
                .statuses(List.of("All Status", "Active", "Inactive"))
                .accessLevels(List.of("All Access Levels", "Full Access", "High", "Medium", "Low"))
                .roles(filtered)
                .totalRoles(24)
                .currentPage(1)
                .totalPages(3)
                .pageSize(10)
                .rolesByType(donut)
                .rolesByAccessLevel(accessLevels)
                .roleCreationTrend(trend)
                .selectedRoleDetail(selected)
                .build();
    }

    public RoleDetailDto getRoleDetail(String id) {
        return roleDetailsMap.getOrDefault(id, roleDetailsMap.get("rol-3"));
    }

    public RoleDetailDto createRole(CreateRoleRequestDto req) {
        int nextNum = roleCounter.incrementAndGet();
        String id = "rol-" + nextNum;
        String name = req.getRoleName() != null ? req.getRoleName() : "Custom Analyst";
        String[] parts = name.split(" ");
        String initials = parts.length > 1 ? (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase() : name.substring(0, 2).toUpperCase();

        RoleManagementPageDto.RoleItemDto item = RoleManagementPageDto.RoleItemDto.builder()
                .id(id)
                .number(nextNum)
                .roleName(name)
                .roleType(req.getRoleType() != null ? req.getRoleType() : "Custom")
                .roleTypeTheme(req.getRoleType() != null && req.getRoleType().equalsIgnoreCase("System") ? "blue" : "purple")
                .description(req.getDescription() != null ? req.getDescription() : "Custom defined role.")
                .usersAssigned(0)
                .status("Active")
                .statusTheme("green")
                .accessLevel(req.getAccessLevel() != null ? req.getAccessLevel() : "Medium")
                .accessLevelTheme("yellow")
                .createdOn("29 Sep 2026")
                .build();

        rolesList.add(0, item);

        RoleDetailDto detail = RoleDetailDto.builder()
                .id(id)
                .initials(initials)
                .roleName(name)
                .roleType((req.getRoleType() != null ? req.getRoleType() : "Custom") + " Role")
                .description(req.getDescription() != null ? req.getDescription() : "Custom defined role across platform.")
                .status("Active")
                .statusTheme("green")
                .tabs(List.of("Details", "Permissions", "Users", "Activity Logs"))
                .activeTab("Details")
                .accessLevel(req.getAccessLevel() != null ? req.getAccessLevel() : "Medium")
                .accessLevelTheme("yellow")
                .createdOn("29 Sep 2026, 06:15 PM")
                .createdBy("Admin User")
                .lastUpdated("29 Sep 2026, 06:15 PM")
                .totalPermissions(req.getPermissions() != null ? req.getPermissions().size() : 24)
                .usersAssigned(0)
                .modulesCount(8)
                .permissionsCount(req.getPermissions() != null ? req.getPermissions().size() : 24)
                .usersCount(0)
                .conditionsCount(1)
                .recentActivities(List.of(
                        new RoleDetailDto.RoleActivityDto("Role created", "29 Sep 2026, 06:15 PM by Admin User", "blue")
                ))
                .build();

        roleDetailsMap.put(id, detail);
        return detail;
    }

    public RoleDetailDto cloneRole(String id) {
        RoleDetailDto src = getRoleDetail(id);
        int nextNum = roleCounter.incrementAndGet();
        String newId = "rol-" + nextNum;
        String newName = src.getRoleName() + " (Copy)";

        RoleDetailDto copy = RoleDetailDto.builder()
                .id(newId)
                .initials(src.getInitials())
                .roleName(newName)
                .roleType("Custom Role")
                .description("Cloned from " + src.getRoleName())
                .status("Active")
                .statusTheme("green")
                .tabs(src.getTabs())
                .activeTab("Details")
                .accessLevel(src.getAccessLevel())
                .accessLevelTheme(src.getAccessLevelTheme())
                .createdOn("29 Sep 2026, 06:20 PM")
                .createdBy("Admin User")
                .lastUpdated("29 Sep 2026, 06:20 PM")
                .totalPermissions(src.getTotalPermissions())
                .usersAssigned(0)
                .modulesCount(src.getModulesCount())
                .permissionsCount(src.getPermissionsCount())
                .usersCount(0)
                .conditionsCount(src.getConditionsCount())
                .recentActivities(List.of(new RoleDetailDto.RoleActivityDto("Cloned from " + src.getRoleName(), "29 Sep 2026, 06:20 PM", "green")))
                .build();

        rolesList.add(0, RoleManagementPageDto.RoleItemDto.builder()
                .id(newId)
                .number(nextNum)
                .roleName(newName)
                .roleType("Custom")
                .roleTypeTheme("purple")
                .description("Cloned from " + src.getRoleName())
                .usersAssigned(0)
                .status("Active")
                .statusTheme("green")
                .accessLevel(src.getAccessLevel())
                .accessLevelTheme(src.getAccessLevelTheme())
                .createdOn("29 Sep 2026")
                .build());

        roleDetailsMap.put(newId, copy);
        return copy;
    }

    public RoleDetailDto deactivateRole(String id) {
        RoleDetailDto detail = getRoleDetail(id);
        detail.setStatus("Inactive");
        detail.setStatusTheme("red");

        rolesList.stream().filter(r -> r.getId().equalsIgnoreCase(id)).findFirst().ifPresent(r -> {
            r.setStatus("Inactive");
            r.setStatusTheme("red");
        });
        return detail;
    }

    // ==========================================
    // 3. PERMISSION MANAGEMENT
    // ==========================================
    public PermissionManagementPageDto getPermissionManagementPage(String company, String tab, String query, String module, String permissionType, String status) {
        List<KpiCardDto> kpis = List.of(
                KpiCardDto.builder().title("Total Permissions").value("48").change("+14% vs last quarter").comparisonText("").trend("positive").icon("key").color("yellow").build(),
                KpiCardDto.builder().title("Role Permissions").value("36").change("+20% vs last quarter").comparisonText("").trend("positive").icon("users").color("green").build(),
                KpiCardDto.builder().title("User Specific Permissions").value("8").change("-11% vs last quarter").comparisonText("").trend("neutral").icon("user").color("red").build(),
                KpiCardDto.builder().title("Critical Permissions").value("6").change("+50% vs last quarter").comparisonText("").trend("negative").icon("shield").color("purple").build()
        );

        PermissionManagementPageDto.PermissionsByModuleDonutDto donut = PermissionManagementPageDto.PermissionsByModuleDonutDto.builder()
                .totalPermissions("48 Total Permissions")
                .slices(List.of(
                        new PermissionManagementPageDto.ModuleSliceDto("Trade Management", 20.8, 10, "#ef4444"),
                        new PermissionManagementPageDto.ModuleSliceDto("Settlement", 16.7, 8, "#3b82f6"),
                        new PermissionManagementPageDto.ModuleSliceDto("User Management", 16.7, 8, "#eab308"),
                        new PermissionManagementPageDto.ModuleSliceDto("System Settings", 12.5, 6, "#f97316"),
                        new PermissionManagementPageDto.ModuleSliceDto("Approval Workflow", 12.5, 6, "#a855f7"),
                        new PermissionManagementPageDto.ModuleSliceDto("Others", 20.8, 10, "#64748b")
                )).build();

        List<PermissionManagementPageDto.PermissionTypeBarDto> typeBar = List.of(
                new PermissionManagementPageDto.PermissionTypeBarDto("Read", 37.5, 18, "#3b82f6"),
                new PermissionManagementPageDto.PermissionTypeBarDto("Create", 20.8, 10, "#22c55e"),
                new PermissionManagementPageDto.PermissionTypeBarDto("Update", 16.7, 8, "#eab308"),
                new PermissionManagementPageDto.PermissionTypeBarDto("Delete", 12.5, 6, "#ef4444"),
                new PermissionManagementPageDto.PermissionTypeBarDto("Manage", 12.5, 6, "#a855f7")
        );

        List<PermissionManagementPageDto.PermissionTrendMonthDto> trend = List.of(
                new PermissionManagementPageDto.PermissionTrendMonthDto("Apr 2026", 12),
                new PermissionManagementPageDto.PermissionTrendMonthDto("May 2026", 18),
                new PermissionManagementPageDto.PermissionTrendMonthDto("Jun 2026", 28),
                new PermissionManagementPageDto.PermissionTrendMonthDto("Jul 2026", 36),
                new PermissionManagementPageDto.PermissionTrendMonthDto("Aug 2026", 42),
                new PermissionManagementPageDto.PermissionTrendMonthDto("Sep 2026", 48)
        );

        List<PermissionManagementPageDto.PermissionItemDto> filtered = permissionsList.stream()
                .filter(p -> {
                    if (query != null && !query.isBlank()) {
                        String q = query.toLowerCase();
                        if (!p.getPermissionName().toLowerCase().contains(q) &&
                            !p.getModule().toLowerCase().contains(q) &&
                            !p.getDescription().toLowerCase().contains(q)) return false;
                    }
                    if (tab != null && !tab.isBlank() && !tab.equalsIgnoreCase("All Permissions")) {
                        if (tab.equalsIgnoreCase("Module Permissions") && !p.getPermissionType().equalsIgnoreCase("Manage")) return false;
                        if (tab.equalsIgnoreCase("Role Permissions") && p.getAssignedRoles() == 0) return false;
                    }
                    if (module != null && !module.isBlank() && !module.equalsIgnoreCase("All Modules")) {
                        if (!p.getModule().equalsIgnoreCase(module)) return false;
                    }
                    if (permissionType != null && !permissionType.isBlank() && !permissionType.equalsIgnoreCase("All Permission Types")) {
                        if (!p.getPermissionType().equalsIgnoreCase(permissionType)) return false;
                    }
                    if (status != null && !status.isBlank() && !status.equalsIgnoreCase("All Status")) {
                        if (!p.getStatus().equalsIgnoreCase(status)) return false;
                    }
                    return true;
                }).collect(Collectors.toList());

        PermissionDetailDto selected = permissionDetailsMap.getOrDefault("per-1", permissionDetailsMap.values().iterator().next());

        return PermissionManagementPageDto.builder()
                .adminUser(adminUser)
                .pageTitle("Permission Management")
                .pageSubtitle("Define, manage and control permissions for roles and users across all modules.")
                .selectedCompany(company != null ? company : "Atirath Holdings")
                .kpis(kpis)
                .tabs(List.of("All Permissions", "Module Permissions", "Role Permissions", "User Permissions", "Custom Permissions"))
                .activeTab(tab != null ? tab : "All Permissions")
                .modules(List.of("All Modules", "Trade Management", "Settlement", "User Management", "Role Management", "System Settings", "Approval Workflow"))
                .permissionTypes(List.of("All Permission Types", "Read", "Create", "Update", "Delete", "Manage", "Approve"))
                .roles(List.of("All Roles", "Operations Manager", "Trade Analyst", "System Administrator", "Company Admin"))
                .statuses(List.of("All Status", "Active", "Inactive"))
                .permissions(filtered)
                .totalPermissions(48)
                .currentPage(1)
                .totalPages(5)
                .pageSize(10)
                .permissionsByModule(donut)
                .permissionsByType(typeBar)
                .permissionAssignmentTrend(trend)
                .selectedPermissionDetail(selected)
                .build();
    }

    public PermissionDetailDto getPermissionDetail(String id) {
        return permissionDetailsMap.getOrDefault(id, permissionDetailsMap.get("per-1"));
    }

    public PermissionDetailDto assignPermission(AssignPermissionRequestDto req) {
        int nextNum = permissionCounter.incrementAndGet();
        String id = "per-" + nextNum;
        String perId = "PER-" + String.format("%03d", nextNum);

        PermissionManagementPageDto.PermissionItemDto item = PermissionManagementPageDto.PermissionItemDto.builder()
                .id(id)
                .number(nextNum)
                .permissionName(req.getPermissionName() != null ? req.getPermissionName() : "Custom Permission")
                .module(req.getModule() != null ? req.getModule() : "Trade Management")
                .permissionType(req.getPermissionType() != null ? req.getPermissionType() : "Read")
                .permissionTypeTheme("blue")
                .description(req.getDescription() != null ? req.getDescription() : "Standard permission grant.")
                .assignedRoles(req.getRoleNames() != null ? req.getRoleNames().size() : 2)
                .status("Active")
                .statusTheme("green")
                .createdOn("29 Sep 2026")
                .build();

        permissionsList.add(0, item);

        PermissionDetailDto detail = PermissionDetailDto.builder()
                .id(id)
                .permissionName(req.getPermissionName() != null ? req.getPermissionName() : "Custom Permission")
                .permissionId(perId)
                .permissionTypeTitle((req.getPermissionType() != null ? req.getPermissionType() : "Read") + " Permission")
                .status("Active")
                .statusTheme("green")
                .tabs(List.of("Details", "Assigned Roles", "Assigned Users", "Activity Log"))
                .activeTab("Details")
                .module(req.getModule() != null ? req.getModule() : "Trade Management")
                .permissionType(req.getPermissionType() != null ? req.getPermissionType() : "Read")
                .description(req.getDescription() != null ? req.getDescription() : "Standard permission grant.")
                .createdOn("29 Sep 2026, 06:25 PM")
                .createdBy("Admin User")
                .lastUpdated("29 Sep 2026, 06:25 PM")
                .updatedBy("Admin User")
                .rolesCount(req.getRoleNames() != null ? req.getRoleNames().size() : 2)
                .usersCount(15)
                .conditionsCount(1)
                .parentPermission("-")
                .dependencies("-")
                .permissionCode(req.getPermissionCode() != null ? req.getPermissionCode() : "CUSTOM_PERM")
                .build();

        permissionDetailsMap.put(id, detail);
        return detail;
    }

    public PermissionDetailDto deactivatePermission(String id) {
        PermissionDetailDto detail = getPermissionDetail(id);
        detail.setStatus("Inactive");
        detail.setStatusTheme("red");

        permissionsList.stream().filter(p -> p.getId().equalsIgnoreCase(id)).findFirst().ifPresent(p -> {
            p.setStatus("Inactive");
            p.setStatusTheme("red");
        });
        return detail;
    }

    // ==========================================
    // 4. ACCESS CONTROL
    // ==========================================
    public AccessControlPageDto getAccessControlPage(String company, String tab, String query, String module, String role, String accessStatus) {
        List<KpiCardDto> kpis = List.of(
                KpiCardDto.builder().title("Total Users").value("128").change("+12% vs last quarter").comparisonText("").trend("positive").icon("users").color("blue").build(),
                KpiCardDto.builder().title("Active Accesses").value("1,246").change("+18% vs last quarter").comparisonText("").trend("positive").icon("shield-check").color("blue").build(),
                KpiCardDto.builder().title("Restricted Accesses").value("32").change("-20% vs last quarter").comparisonText("").trend("neutral").icon("lock").color("red").build(),
                KpiCardDto.builder().title("Pending Access Requests").value("6").change("-33% vs last quarter").comparisonText("").trend("neutral").icon("clock").color("orange").build(),
                KpiCardDto.builder().title("Expired Accesses").value("14").change("-13% vs last quarter").comparisonText("").trend("neutral").icon("user-x").color("red").build()
        );

        AccessControlPageDto.AccessByModuleDonutDto donut = AccessControlPageDto.AccessByModuleDonutDto.builder()
                .totalAccesses("1,246 Total Accesses")
                .slices(List.of(
                        new AccessControlPageDto.ModuleSliceDto("Trade Management", 28.1, 350, "#ef4444"),
                        new AccessControlPageDto.ModuleSliceDto("Settlement", 18.2, 227, "#3b82f6"),
                        new AccessControlPageDto.ModuleSliceDto("Payments", 14.5, 181, "#eab308"),
                        new AccessControlPageDto.ModuleSliceDto("Compliance", 11.2, 140, "#f97316"),
                        new AccessControlPageDto.ModuleSliceDto("Reports", 8.9, 111, "#06b6d4"),
                        new AccessControlPageDto.ModuleSliceDto("Others", 19.1, 237, "#14b8a6")
                )).build();

        List<AccessControlPageDto.AccessLevelBarDto> levelBar = List.of(
                new AccessControlPageDto.AccessLevelBarDto("Full Access", 42.3, 527, "#22c55e"),
                new AccessControlPageDto.AccessLevelBarDto("Limited", 38.7, 482, "#3b82f6"),
                new AccessControlPageDto.AccessLevelBarDto("Read Only", 12.4, 155, "#eab308"),
                new AccessControlPageDto.AccessLevelBarDto("No Access", 6.6, 82, "#ef4444")
        );

        List<AccessControlPageDto.AccessTrendMonthDto> trend = List.of(
                new AccessControlPageDto.AccessTrendMonthDto("Apr 2026", 180, 140, 60, 20),
                new AccessControlPageDto.AccessTrendMonthDto("May 2026", 240, 200, 80, 30),
                new AccessControlPageDto.AccessTrendMonthDto("Jun 2026", 310, 280, 110, 45),
                new AccessControlPageDto.AccessTrendMonthDto("Jul 2026", 390, 350, 125, 55),
                new AccessControlPageDto.AccessTrendMonthDto("Aug 2026", 460, 410, 140, 68),
                new AccessControlPageDto.AccessTrendMonthDto("Sep 2026", 527, 482, 155, 82)
        );

        List<AccessControlPageDto.AccessItemDto> filtered = accessList.stream()
                .filter(a -> {
                    if (query != null && !query.isBlank()) {
                        String q = query.toLowerCase();
                        if (!a.getUserName().toLowerCase().contains(q) &&
                            !a.getRoles().toLowerCase().contains(q) &&
                            !a.getModuleAccess().toLowerCase().contains(q)) return false;
                    }
                    if (tab != null && !tab.isBlank() && !tab.equalsIgnoreCase("All Access")) {
                        if (tab.equalsIgnoreCase("Pending Requests") && !a.getStatus().equalsIgnoreCase("Pending")) return false;
                    }
                    if (role != null && !role.isBlank() && !role.equalsIgnoreCase("All Roles")) {
                        if (!a.getRoles().equalsIgnoreCase(role)) return false;
                    }
                    if (accessStatus != null && !accessStatus.isBlank() && !accessStatus.equalsIgnoreCase("All Access Status")) {
                        if (!a.getStatus().equalsIgnoreCase(accessStatus)) return false;
                    }
                    return true;
                }).collect(Collectors.toList());

        AccessDetailDto selected = accessDetailsMap.getOrDefault("acc-1", accessDetailsMap.values().iterator().next());

        return AccessControlPageDto.builder()
                .adminUser(adminUser)
                .pageTitle("Access Control")
                .pageSubtitle("Manage user access, role assignments, module permissions and access policies across the platform.")
                .selectedCompany(company != null ? company : "Atirath Holdings")
                .kpis(kpis)
                .tabs(List.of("All Access", "User Access", "Role Access", "Module Access", "Pending Requests", "Access Policies"))
                .activeTab(tab != null ? tab : "All Access")
                .companies(List.of("All Companies", "Atirath Holdings", "Farm Vantara", "Atirath Traders", "Sunrise Ventures", "Green Future Fund", "Global Agri Fund", "Atirath Technologies"))
                .modules(List.of("All Modules", "Trading", "Settlement", "Escrow", "Disputes", "Reports", "Compliance"))
                .roles(List.of("All Roles", "Operations Manager", "Finance Executive", "Trade Analyst", "Compliance Officer", "Settlement Executive", "Investor Manager", "System Administrator", "Operations Executive", "Trade Executive", "Finance Manager"))
                .accessStatuses(List.of("All Access Status", "Active", "Pending", "Expired"))
                .accesses(filtered)
                .totalAccesses(128)
                .currentPage(1)
                .totalPages(13)
                .pageSize(10)
                .accessByModule(donut)
                .accessByAccessLevel(levelBar)
                .accessTrend(trend)
                .selectedAccessDetail(selected)
                .build();
    }

    public AccessDetailDto getAccessDetail(String id) {
        return accessDetailsMap.getOrDefault(id, accessDetailsMap.get("acc-1"));
    }

    public AccessDetailDto grantAccess(GrantAccessRequestDto req) {
        int nextNum = accessCounter.incrementAndGet();
        String id = "acc-" + nextNum;
        String[] parts = req.getUserName() != null ? req.getUserName().split(" ") : new String[]{"User", "New"};
        String initials = parts.length > 1 ? (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase() : parts[0].substring(0, 1).toUpperCase();

        AccessControlPageDto.AccessItemDto item = AccessControlPageDto.AccessItemDto.builder()
                .id(id)
                .number(nextNum)
                .initials(initials)
                .userName(req.getUserName() != null ? req.getUserName() : "New User")
                .roles(req.getRole() != null ? req.getRole() : "Trade Analyst")
                .moduleAccess(req.getModules() != null ? String.join(", ", req.getModules()) : "Trading, Settlement")
                .accessLevel(req.getAccessLevel() != null ? req.getAccessLevel() : "Limited")
                .accessLevelTheme(req.getAccessLevel() != null && req.getAccessLevel().equalsIgnoreCase("Full Access") ? "green" : "blue")
                .status("Active")
                .statusTheme("green")
                .grantedOn("29 Sep 2026")
                .expiresOn(req.getExpiresOn() != null ? req.getExpiresOn() : "-")
                .build();

        accessList.add(0, item);

        AccessDetailDto detail = AccessDetailDto.builder()
                .id(id)
                .initials(initials)
                .userName(req.getUserName() != null ? req.getUserName() : "New User")
                .role(req.getRole() != null ? req.getRole() : "Trade Analyst")
                .email("user" + nextNum + "@atirath.com")
                .status("Active")
                .statusTheme("green")
                .tabs(List.of("Overview", "Module Access", "Role Access", "Activity Log"))
                .activeTab("Overview")
                .userId("EMP-2026-" + String.format("%03d", nextNum))
                .department("Operations")
                .company(req.getCompany() != null ? req.getCompany() : "Atirath Holdings")
                .accessLevel(req.getAccessLevel() != null ? req.getAccessLevel() : "Limited")
                .accessLevelTheme("blue")
                .grantedOn("29 Sep 2026, 06:30 PM")
                .expiresOn(req.getExpiresOn() != null ? req.getExpiresOn() : "-")
                .lastUpdated("29 Sep 2026, 06:30 PM")
                .updatedBy("Admin User")
                .moduleAccessList(List.of(
                        new AccessDetailDto.ModuleAccessItemDto("Trade Management", "Full Access", "green"),
                        new AccessDetailDto.ModuleAccessItemDto("Settlement", "Limited", "blue"),
                        new AccessDetailDto.ModuleAccessItemDto("Reports", "Read Only", "yellow")
                ))
                .primaryRole(req.getRole() != null ? req.getRole() : "Trade Analyst")
                .additionalRoles("-")
                .inheritedRoles("-")
                .recentActivities(List.of(new AccessDetailDto.AccessActivityDto("Access granted", "29 Sep 2026, 06:30 PM by Admin User", "blue")))
                .build();

        accessDetailsMap.put(id, detail);
        return detail;
    }

    public AccessDetailDto revokeAccess(String id) {
        AccessDetailDto detail = getAccessDetail(id);
        detail.setStatus("Revoked");
        detail.setStatusTheme("red");
        detail.setAccessLevel("No Access");
        detail.setAccessLevelTheme("red");

        accessList.stream().filter(a -> a.getId().equalsIgnoreCase(id)).findFirst().ifPresent(a -> {
            a.setStatus("Inactive");
            a.setStatusTheme("red");
            a.setAccessLevel("No Access");
            a.setAccessLevelTheme("red");
        });
        return detail;
    }

    // ==========================================
    // 5. APPROVAL WORKFLOW
    // ==========================================
    public ApprovalWorkflowPageDto getApprovalWorkflowPage(String company, String tab, String query, String module, String status, String approvalLevel) {
        List<KpiCardDto> kpis = List.of(
                KpiCardDto.builder().title("Total Workflows").value("18").change("+20% vs last quarter").comparisonText("").trend("positive").icon("workflow").color("blue").build(),
                KpiCardDto.builder().title("Active Workflows").value("15").change("+15% vs last quarter").comparisonText("").trend("positive").icon("check-circle").color("green").build(),
                KpiCardDto.builder().title("Inactive Workflows").value("3").change("-25% vs last quarter").comparisonText("").trend("neutral").icon("pause-circle").color("red").build(),
                KpiCardDto.builder().title("Total Approval Levels").value("52").change("+18% vs last quarter").comparisonText("").trend("positive").icon("layers").color("purple").build(),
                KpiCardDto.builder().title("Pending Changes").value("4").change("-33% vs last quarter").comparisonText("").trend("neutral").icon("clock").color("orange").build()
        );

        ApprovalWorkflowPageDto.WorkflowsByModuleDonutDto donut = ApprovalWorkflowPageDto.WorkflowsByModuleDonutDto.builder()
                .totalWorkflows("18 Total Workflows")
                .slices(List.of(
                        new ApprovalWorkflowPageDto.ModuleSliceDto("Trade Management", 33.3, 6, "#ef4444"),
                        new ApprovalWorkflowPageDto.ModuleSliceDto("Settlement", 16.7, 3, "#3b82f6"),
                        new ApprovalWorkflowPageDto.ModuleSliceDto("User Management", 11.1, 2, "#eab308"),
                        new ApprovalWorkflowPageDto.ModuleSliceDto("Compliance", 11.1, 2, "#f97316"),
                        new ApprovalWorkflowPageDto.ModuleSliceDto("System Settings", 11.1, 2, "#a855f7"),
                        new ApprovalWorkflowPageDto.ModuleSliceDto("Reports", 11.1, 2, "#06b6d4"),
                        new ApprovalWorkflowPageDto.ModuleSliceDto("Others", 5.6, 1, "#64748b")
                )).build();

        List<ApprovalWorkflowPageDto.WorkflowStatusBarDto> statusBar = List.of(
                new ApprovalWorkflowPageDto.WorkflowStatusBarDto("Active", 83.3, 15, "#22c55e"),
                new ApprovalWorkflowPageDto.WorkflowStatusBarDto("Inactive", 16.7, 3, "#ef4444")
        );

        List<ApprovalWorkflowPageDto.ApprovalLevelsBarDto> levelsBar = List.of(
                new ApprovalWorkflowPageDto.ApprovalLevelsBarDto("1 Level", 11.1, 2, "#a855f7"),
                new ApprovalWorkflowPageDto.ApprovalLevelsBarDto("2 Levels", 44.4, 8, "#3b82f6"),
                new ApprovalWorkflowPageDto.ApprovalLevelsBarDto("3 Levels", 33.3, 6, "#eab308"),
                new ApprovalWorkflowPageDto.ApprovalLevelsBarDto("4+ Levels", 11.1, 2, "#ef4444")
        );

        List<ApprovalWorkflowPageDto.WorkflowItemDto> filtered = workflowsList.stream()
                .filter(w -> {
                    if (query != null && !query.isBlank()) {
                        String q = query.toLowerCase();
                        if (!w.getWorkflowName().toLowerCase().contains(q) &&
                            !w.getModule().toLowerCase().contains(q) &&
                            !w.getDescription().toLowerCase().contains(q)) return false;
                    }
                    if (tab != null && !tab.isBlank() && !tab.equalsIgnoreCase("All Workflows")) {
                        if (tab.equalsIgnoreCase("Trade Workflows") && !w.getModule().equalsIgnoreCase("Trade Management")) return false;
                        if (tab.equalsIgnoreCase("Settlement Workflows") && !w.getModule().equalsIgnoreCase("Settlement")) return false;
                        if (tab.equalsIgnoreCase("User Management") && !w.getModule().equalsIgnoreCase("User Management")) return false;
                        if (tab.equalsIgnoreCase("System Settings") && !w.getModule().equalsIgnoreCase("System Settings")) return false;
                    }
                    if (module != null && !module.isBlank() && !module.equalsIgnoreCase("All Modules")) {
                        if (!w.getModule().equalsIgnoreCase(module)) return false;
                    }
                    if (status != null && !status.isBlank() && !status.equalsIgnoreCase("All Status")) {
                        if (!w.getStatus().equalsIgnoreCase(status)) return false;
                    }
                    return true;
                }).collect(Collectors.toList());

        WorkflowDetailDto selected = workflowDetailsMap.getOrDefault("wf-1", workflowDetailsMap.values().iterator().next());

        return ApprovalWorkflowPageDto.builder()
                .adminUser(adminUser)
                .pageTitle("Approval Workflow Configuration")
                .pageSubtitle("Define and manage multi-level approval workflows for different modules and transactions.")
                .selectedCompany(company != null ? company : "Atirath Holdings")
                .kpis(kpis)
                .tabs(List.of("All Workflows", "Trade Workflows", "Settlement Workflows", "User Management", "System Settings", "Other Modules"))
                .activeTab(tab != null ? tab : "All Workflows")
                .modules(List.of("All Modules", "Trade Management", "Settlement", "User Management", "Role Management", "Permission Management", "Compliance", "Escrow", "System Settings", "Reports"))
                .statuses(List.of("All Status", "Active", "Inactive"))
                .approvalLevels(List.of("All Approval Levels", "1 Level", "2 Levels", "3 Levels", "4 Levels"))
                .workflows(filtered)
                .totalWorkflows(18)
                .currentPage(1)
                .totalPages(2)
                .pageSize(10)
                .workflowsByModule(donut)
                .workflowsByStatus(statusBar)
                .approvalLevelsDistribution(levelsBar)
                .selectedWorkflowDetail(selected)
                .build();
    }

    public WorkflowDetailDto getWorkflowDetail(String id) {
        return workflowDetailsMap.getOrDefault(id, workflowDetailsMap.get("wf-1"));
    }

    public WorkflowDetailDto createWorkflow(CreateWorkflowRequestDto req) {
        int nextNum = workflowCounter.incrementAndGet();
        String id = "wf-" + nextNum;

        ApprovalWorkflowPageDto.WorkflowItemDto item = ApprovalWorkflowPageDto.WorkflowItemDto.builder()
                .id(id)
                .number(nextNum)
                .workflowName(req.getWorkflowName() != null ? req.getWorkflowName() : "Custom Approval Workflow")
                .module(req.getModule() != null ? req.getModule() : "Trade Management")
                .description(req.getDescription() != null ? req.getDescription() : "Standard multi-level approval.")
                .approvalLevels((req.getApprovalLevels() != null ? req.getApprovalLevels() : 2) + " Levels")
                .status("Active")
                .statusTheme("green")
                .lastUpdated("29 Sep 2026")
                .build();

        workflowsList.add(0, item);

        WorkflowDetailDto detail = WorkflowDetailDto.builder()
                .id(id)
                .workflowName(req.getWorkflowName() != null ? req.getWorkflowName() : "Custom Approval Workflow")
                .module(req.getModule() != null ? req.getModule() : "Trade Management")
                .description(req.getDescription() != null ? req.getDescription() : "Approval configuration across platform.")
                .status("Active")
                .statusTheme("green")
                .tabs(List.of("Details", "Approval Levels", "Conditions", "Activity Log"))
                .activeTab("Details")
                .createdOn("29 Sep 2026, 06:35 PM")
                .createdBy("Admin User")
                .lastUpdated("29 Sep 2026, 06:35 PM")
                .updatedBy("Admin User")
                .totalApprovalLevels(req.getApprovalLevels() != null ? req.getApprovalLevels() : 2)
                .approvalType(req.getApprovalType() != null ? req.getApprovalType() : "Sequential")
                .approvalRequired(req.getApprovalRequired() != null ? req.getApprovalRequired() : "All Levels")
                .allowDelegation(req.getAllowDelegation() != null && req.getAllowDelegation() ? "Yes" : "No")
                .autoEscalation(req.getAutoEscalation() != null && req.getAutoEscalation() ? "Yes" : "No")
                .escalationTime(req.getEscalationTime() != null ? req.getEscalationTime() : "24 hours")
                .conditions(List.of(
                        new WorkflowDetailDto.WorkflowConditionDto("Transaction Value > Threshold", true),
                        new WorkflowDetailDto.WorkflowConditionDto("High Risk Counterparty", true)
                ))
                .build();

        workflowDetailsMap.put(id, detail);
        return detail;
    }

    public WorkflowDetailDto cloneWorkflow(String id) {
        WorkflowDetailDto src = getWorkflowDetail(id);
        int nextNum = workflowCounter.incrementAndGet();
        String newId = "wf-" + nextNum;
        String newName = src.getWorkflowName() + " (Copy)";

        WorkflowDetailDto copy = WorkflowDetailDto.builder()
                .id(newId)
                .workflowName(newName)
                .module(src.getModule())
                .description("Cloned from " + src.getWorkflowName())
                .status("Active")
                .statusTheme("green")
                .tabs(src.getTabs())
                .activeTab("Details")
                .createdOn("29 Sep 2026, 06:40 PM")
                .createdBy("Admin User")
                .lastUpdated("29 Sep 2026, 06:40 PM")
                .updatedBy("Admin User")
                .totalApprovalLevels(src.getTotalApprovalLevels())
                .approvalType(src.getApprovalType())
                .approvalRequired(src.getApprovalRequired())
                .allowDelegation(src.getAllowDelegation())
                .autoEscalation(src.getAutoEscalation())
                .escalationTime(src.getEscalationTime())
                .conditions(src.getConditions())
                .build();

        workflowsList.add(0, ApprovalWorkflowPageDto.WorkflowItemDto.builder()
                .id(newId)
                .number(nextNum)
                .workflowName(newName)
                .module(src.getModule())
                .description("Cloned from " + src.getWorkflowName())
                .approvalLevels(src.getTotalApprovalLevels() + " Levels")
                .status("Active")
                .statusTheme("green")
                .lastUpdated("29 Sep 2026")
                .build());

        workflowDetailsMap.put(newId, copy);
        return copy;
    }

    public WorkflowDetailDto deactivateWorkflow(String id) {
        WorkflowDetailDto detail = getWorkflowDetail(id);
        detail.setStatus("Inactive");
        detail.setStatusTheme("red");

        workflowsList.stream().filter(w -> w.getId().equalsIgnoreCase(id)).findFirst().ifPresent(w -> {
            w.setStatus("Inactive");
            w.setStatusTheme("red");
        });
        return detail;
    }

    // ==========================================
    // 6. TRADING RULE CONFIGURATION
    // ==========================================
    public TradingRuleConfigPageDto getTradingRulesPage(String company, String tab, String query, String category, String status, String module) {
        List<KpiCardDto> kpis = List.of(
                KpiCardDto.builder().title("Total Rules").value("48").change("+14% vs last quarter").comparisonText("").trend("positive").icon("settings").color("blue").build(),
                KpiCardDto.builder().title("Active Rules").value("42").change("+20% vs last quarter").comparisonText("").trend("positive").icon("check-circle").color("green").build(),
                KpiCardDto.builder().title("Inactive Rules").value("6").change("-25% vs last quarter").comparisonText("").trend("neutral").icon("ban").color("red").build(),
                KpiCardDto.builder().title("Risk Rules").value("12").change("+33% vs last quarter").comparisonText("").trend("warning").icon("alert-triangle").color("orange").build(),
                KpiCardDto.builder().title("Compliance Rules").value("10").change("+11% vs last quarter").comparisonText("").trend("positive").icon("file-text").color("purple").build()
        );

        TradingRuleConfigPageDto.RulesByCategoryDonutDto donut = TradingRuleConfigPageDto.RulesByCategoryDonutDto.builder()
                .totalRules("48 Total Rules")
                .slices(List.of(
                        new TradingRuleConfigPageDto.CategorySliceDto("Trading Limit", 29.2, 14, "#ef4444"),
                        new TradingRuleConfigPageDto.CategorySliceDto("Risk Management", 25.0, 12, "#3b82f6"),
                        new TradingRuleConfigPageDto.CategorySliceDto("Compliance", 20.8, 10, "#22c55e"),
                        new TradingRuleConfigPageDto.CategorySliceDto("Market Rule", 16.7, 8, "#a855f7"),
                        new TradingRuleConfigPageDto.CategorySliceDto("Custom Rule", 8.3, 4, "#eab308")
                )).build();

        List<TradingRuleConfigPageDto.RuleStatusBarDto> statusBar = List.of(
                new TradingRuleConfigPageDto.RuleStatusBarDto("Active", 87.5, 42, "#22c55e"),
                new TradingRuleConfigPageDto.RuleStatusBarDto("Inactive", 12.5, 6, "#ef4444")
        );

        List<TradingRuleConfigPageDto.RulePriorityBarDto> priorityBar = List.of(
                new TradingRuleConfigPageDto.RulePriorityBarDto("Low", 8.3, 4, "#94a3b8"),
                new TradingRuleConfigPageDto.RulePriorityBarDto("Medium", 33.3, 16, "#f59e0b"),
                new TradingRuleConfigPageDto.RulePriorityBarDto("High", 41.7, 20, "#f97316"),
                new TradingRuleConfigPageDto.RulePriorityBarDto("Critical", 16.7, 8, "#ef4444")
        );

        List<TradingRuleConfigPageDto.TradingRuleItemDto> filtered = tradingRulesList.stream()
                .filter(r -> {
                    if (query != null && !query.isBlank()) {
                        String q = query.toLowerCase();
                        if (!r.getRuleName().toLowerCase().contains(q) &&
                            !r.getDescription().toLowerCase().contains(q) &&
                            !r.getCategory().toLowerCase().contains(q)) return false;
                    }
                    if (tab != null && !tab.isBlank() && !tab.equalsIgnoreCase("All Rules")) {
                        if (tab.equalsIgnoreCase("Trading Limits") && !r.getCategory().equalsIgnoreCase("Trading Limit")) return false;
                        if (tab.equalsIgnoreCase("Risk Rules") && !r.getCategory().equalsIgnoreCase("Risk Management")) return false;
                        if (tab.equalsIgnoreCase("Compliance Rules") && !r.getCategory().equalsIgnoreCase("Compliance")) return false;
                        if (tab.equalsIgnoreCase("Market Rules") && !r.getCategory().equalsIgnoreCase("Market Rule")) return false;
                        if (tab.equalsIgnoreCase("Custom Rules") && !r.getCategory().equalsIgnoreCase("Custom Rule")) return false;
                    }
                    if (category != null && !category.isBlank() && !category.equalsIgnoreCase("All Rule Categories")) {
                        if (!r.getCategory().equalsIgnoreCase(category)) return false;
                    }
                    if (status != null && !status.isBlank() && !status.equalsIgnoreCase("All Status")) {
                        if (!r.getStatus().equalsIgnoreCase(status)) return false;
                    }
                    return true;
                }).collect(Collectors.toList());

        TradingRuleDetailDto selected = tradingRuleDetailsMap.getOrDefault("trd-1", tradingRuleDetailsMap.values().iterator().next());

        return TradingRuleConfigPageDto.builder()
                .adminUser(adminUser)
                .pageTitle("Trading Rule Configuration")
                .pageSubtitle("Define and manage trading rules, limits, risk parameters and validations for the platform.")
                .selectedCompany(company != null ? company : "Atirath Holdings")
                .kpis(kpis)
                .tabs(List.of("All Rules", "Trading Limits", "Risk Rules", "Compliance Rules", "Market Rules", "Custom Rules"))
                .activeTab(tab != null ? tab : "All Rules")
                .ruleCategories(List.of("All Rule Categories", "Trading Limit", "Risk Management", "Compliance", "Market Rule", "Custom Rule"))
                .statuses(List.of("All Status", "Active", "Inactive"))
                .modules(List.of("All Modules", "Trade Execution", "Position Control", "Counterparty Risk", "Instrument Catalog", "Market Schedule", "Settlement Engine"))
                .rules(filtered)
                .totalRules(48)
                .currentPage(1)
                .totalPages(5)
                .pageSize(10)
                .rulesByCategory(donut)
                .rulesByStatus(statusBar)
                .rulesByPriority(priorityBar)
                .selectedRuleDetail(selected)
                .build();
    }

    public TradingRuleDetailDto getTradingRuleDetail(String id) {
        return tradingRuleDetailsMap.getOrDefault(id, tradingRuleDetailsMap.get("trd-1"));
    }

    public TradingRuleDetailDto createTradingRule(CreateTradingRuleRequestDto req) {
        int nextNum = tradingRuleCounter.incrementAndGet();
        String id = "trd-" + nextNum;
        String ruleId = "TRD-" + String.format("%03d", nextNum);

        TradingRuleConfigPageDto.TradingRuleItemDto item = TradingRuleConfigPageDto.TradingRuleItemDto.builder()
                .id(id)
                .number(nextNum)
                .ruleName(req.getRuleName() != null ? req.getRuleName() : "New Trading Rule")
                .category(req.getCategory() != null ? req.getCategory() : "Trading Limit")
                .type(req.getType() != null ? req.getType() : "Threshold")
                .description(req.getDescription() != null ? req.getDescription() : "Custom trading rule validation.")
                .status("Active")
                .statusTheme("green")
                .priority(req.getPriority() != null ? req.getPriority() : "Medium")
                .priorityTheme(req.getPriority() != null && req.getPriority().equalsIgnoreCase("Critical") ? "darkred" : (req.getPriority() != null && req.getPriority().equalsIgnoreCase("High") ? "red" : "orange"))
                .lastUpdated("29 Sep 2026")
                .build();

        tradingRulesList.add(0, item);

        TradingRuleDetailDto detail = TradingRuleDetailDto.builder()
                .id(id)
                .ruleName(req.getRuleName() != null ? req.getRuleName() : "New Trading Rule")
                .ruleCategory(req.getCategory() != null ? req.getCategory() : "Trading Limit")
                .shortDescription(req.getDescription() != null ? req.getDescription() : "Maximum trade value per transaction")
                .status("Active")
                .statusTheme("green")
                .tabs(List.of("Details", "Conditions", "Affected Modules", "Activity Log"))
                .activeTab("Details")
                .ruleId(ruleId)
                .category(req.getCategory() != null ? req.getCategory() : "Trading Limit")
                .type(req.getType() != null ? req.getType() : "Threshold")
                .description(req.getDescription() != null ? req.getDescription() : "Custom trading rule validation.")
                .priority(req.getPriority() != null ? req.getPriority() : "Medium")
                .priorityTheme("orange")
                .createdOn("29 Sep 2026, 06:45 PM")
                .createdBy("Admin User")
                .lastUpdated("29 Sep 2026, 06:45 PM")
                .updatedBy("Admin User")
                .limitType(req.getLimitType() != null ? req.getLimitType() : "Per Transaction")
                .maximumValue(req.getMaximumValue() != null ? req.getMaximumValue() : "₹10,00,00,000")
                .currency(req.getCurrency() != null ? req.getCurrency() : "INR")
                .appliesTo(req.getAppliesTo() != null ? req.getAppliesTo() : "All Users")
                .overrideAllowed(req.getOverrideAllowed() != null && req.getOverrideAllowed() ? "Yes" : "No")
                .alertThreshold(req.getAlertThreshold() != null ? req.getAlertThreshold() : "80%")
                .autoBlock(req.getAutoBlock() != null && req.getAutoBlock() ? "Yes" : "No")
                .conditions(List.of(
                        new TradingRuleDetailDto.TradingRuleConditionDto("User must be KYC verified", true),
                        new TradingRuleDetailDto.TradingRuleConditionDto("Counterparty rating >= Minimum", true),
                        new TradingRuleDetailDto.TradingRuleConditionDto("Instrument must be eligible", true),
                        new TradingRuleDetailDto.TradingRuleConditionDto("Within trading hours", true)
                ))
                .build();

        tradingRuleDetailsMap.put(id, detail);
        return detail;
    }

    public TradingRuleDetailDto cloneTradingRule(String id) {
        TradingRuleDetailDto src = getTradingRuleDetail(id);
        int nextNum = tradingRuleCounter.incrementAndGet();
        String newId = "trd-" + nextNum;
        String newRuleId = "TRD-" + String.format("%03d", nextNum);
        String newName = src.getRuleName() + " (Copy)";

        TradingRuleDetailDto copy = TradingRuleDetailDto.builder()
                .id(newId)
                .ruleName(newName)
                .ruleCategory(src.getRuleCategory())
                .shortDescription("Cloned from " + src.getRuleName())
                .status("Active")
                .statusTheme("green")
                .tabs(src.getTabs())
                .activeTab("Details")
                .ruleId(newRuleId)
                .category(src.getCategory())
                .type(src.getType())
                .description("Cloned from " + src.getRuleName())
                .priority(src.getPriority())
                .priorityTheme(src.getPriorityTheme())
                .createdOn("29 Sep 2026, 06:50 PM")
                .createdBy("Admin User")
                .lastUpdated("29 Sep 2026, 06:50 PM")
                .updatedBy("Admin User")
                .limitType(src.getLimitType())
                .maximumValue(src.getMaximumValue())
                .currency(src.getCurrency())
                .appliesTo(src.getAppliesTo())
                .overrideAllowed(src.getOverrideAllowed())
                .alertThreshold(src.getAlertThreshold())
                .autoBlock(src.getAutoBlock())
                .conditions(src.getConditions())
                .build();

        tradingRulesList.add(0, TradingRuleConfigPageDto.TradingRuleItemDto.builder()
                .id(newId)
                .number(nextNum)
                .ruleName(newName)
                .category(src.getCategory())
                .type(src.getType())
                .description("Cloned from " + src.getRuleName())
                .status("Active")
                .statusTheme("green")
                .priority(src.getPriority())
                .priorityTheme(src.getPriorityTheme())
                .lastUpdated("29 Sep 2026")
                .build());

        tradingRuleDetailsMap.put(newId, copy);
        return copy;
    }

    public TradingRuleDetailDto deactivateTradingRule(String id) {
        TradingRuleDetailDto detail = getTradingRuleDetail(id);
        detail.setStatus("Inactive");
        detail.setStatusTheme("red");

        tradingRulesList.stream().filter(r -> r.getId().equalsIgnoreCase(id)).findFirst().ifPresent(r -> {
            r.setStatus("Inactive");
            r.setStatusTheme("red");
        });
        return detail;
    }

    // ==========================================
    // 7. NOTIFICATION CONFIGURATION
    // ==========================================
    public NotificationConfigPageDto getNotificationConfigPage(String company, String tab, String query, String category, String channel, String status) {
        List<KpiCardDto> kpis = List.of(
                KpiCardDto.builder().title("Total Notifications").value("28").change("+17% vs last quarter").comparisonText("").trend("positive").icon("send").color("blue").build(),
                KpiCardDto.builder().title("Active Notifications").value("24").change("+20% vs last quarter").comparisonText("").trend("positive").icon("bell").color("green").build(),
                KpiCardDto.builder().title("Inactive Notifications").value("4").change("-33% vs last quarter").comparisonText("").trend("neutral").icon("pause-circle").color("red").build(),
                KpiCardDto.builder().title("Notification Channels").value("3").change("Email, In-App, SMS").comparisonText("").trend("positive").icon("radio").color("purple").build()
        );

        NotificationConfigPageDto.NotificationsByCategoryDonutDto donut = NotificationConfigPageDto.NotificationsByCategoryDonutDto.builder()
                .totalNotifications("28 Total")
                .slices(List.of(
                        new NotificationConfigPageDto.CategorySliceDto("Trade", 28.6, 8, "#ef4444"),
                        new NotificationConfigPageDto.CategorySliceDto("Settlement", 21.4, 6, "#3b82f6"),
                        new NotificationConfigPageDto.CategorySliceDto("User", 17.9, 5, "#22c55e"),
                        new NotificationConfigPageDto.CategorySliceDto("Compliance", 14.3, 4, "#a855f7"),
                        new NotificationConfigPageDto.CategorySliceDto("System", 10.7, 3, "#06b6d4"),
                        new NotificationConfigPageDto.CategorySliceDto("Others", 7.1, 2, "#94a3b8")
                )).build();

        List<NotificationConfigPageDto.NotificationChannelBarDto> channelBar = List.of(
                new NotificationConfigPageDto.NotificationChannelBarDto("In-App", 78.6, 22, "#3b82f6"),
                new NotificationConfigPageDto.NotificationChannelBarDto("Email", 64.3, 18, "#22c55e"),
                new NotificationConfigPageDto.NotificationChannelBarDto("SMS", 21.4, 6, "#a855f7")
        );

        List<NotificationConfigPageDto.NotificationTrendMonthDto> trend = List.of(
                new NotificationConfigPageDto.NotificationTrendMonthDto("Apr 2026", 12, 10),
                new NotificationConfigPageDto.NotificationTrendMonthDto("May 2026", 16, 14),
                new NotificationConfigPageDto.NotificationTrendMonthDto("Jun 2026", 20, 18),
                new NotificationConfigPageDto.NotificationTrendMonthDto("Jul 2026", 24, 22),
                new NotificationConfigPageDto.NotificationTrendMonthDto("Aug 2026", 28, 25),
                new NotificationConfigPageDto.NotificationTrendMonthDto("Sep 2026", 32, 28)
        );

        List<NotificationConfigPageDto.NotificationItemDto> filtered = notificationsList.stream()
                .filter(n -> {
                    if (query != null && !query.isBlank()) {
                        String q = query.toLowerCase();
                        if (!n.getNotificationName().toLowerCase().contains(q) &&
                            !n.getEventTrigger().toLowerCase().contains(q) &&
                            !n.getCategory().toLowerCase().contains(q)) return false;
                    }
                    if (tab != null && !tab.isBlank() && !tab.equalsIgnoreCase("All Notifications")) {
                        if (tab.equalsIgnoreCase("Trade Notifications") && !n.getCategory().equalsIgnoreCase("Trade")) return false;
                        if (tab.equalsIgnoreCase("Settlement Notifications") && !n.getCategory().equalsIgnoreCase("Settlement")) return false;
                        if (tab.equalsIgnoreCase("User Notifications") && !n.getCategory().equalsIgnoreCase("User")) return false;
                        if (tab.equalsIgnoreCase("System Notifications") && !n.getCategory().equalsIgnoreCase("System")) return false;
                        if (tab.equalsIgnoreCase("Compliance Notifications") && !n.getCategory().equalsIgnoreCase("Compliance")) return false;
                    }
                    if (category != null && !category.isBlank() && !category.equalsIgnoreCase("All Categories")) {
                        if (!n.getCategory().equalsIgnoreCase(category)) return false;
                    }
                    if (channel != null && !channel.isBlank() && !channel.equalsIgnoreCase("All Channels")) {
                        if (!n.getChannels().toLowerCase().contains(channel.toLowerCase())) return false;
                    }
                    if (status != null && !status.isBlank() && !status.equalsIgnoreCase("All Status")) {
                        if (!n.getStatus().equalsIgnoreCase(status)) return false;
                    }
                    return true;
                }).collect(Collectors.toList());

        NotificationDetailDto selected = notificationDetailsMap.getOrDefault("notif-1", notificationDetailsMap.values().iterator().next());

        return NotificationConfigPageDto.builder()
                .adminUser(adminUser)
                .pageTitle("Notification Configuration")
                .pageSubtitle("Configure and manage system notifications (email, in-app, SMS) for various events across the platform.")
                .selectedCompany(company != null ? company : "Atirath Holdings")
                .kpis(kpis)
                .tabs(List.of("All Notifications", "Trade Notifications", "Settlement Notifications", "User Notifications", "System Notifications", "Compliance Notifications"))
                .activeTab(tab != null ? tab : "All Notifications")
                .categories(List.of("All Categories", "Trade", "Settlement", "User", "Compliance", "System"))
                .channels(List.of("All Channels", "In-App", "Email", "SMS"))
                .statuses(List.of("All Status", "Active", "Inactive"))
                .notifications(filtered)
                .totalNotifications(28)
                .currentPage(1)
                .totalPages(3)
                .pageSize(10)
                .notificationsByCategory(donut)
                .notificationsByChannel(channelBar)
                .notificationTrend(trend)
                .selectedNotificationDetail(selected)
                .build();
    }

    public NotificationDetailDto getNotificationDetail(String id) {
        return notificationDetailsMap.getOrDefault(id, notificationDetailsMap.get("notif-1"));
    }

    public NotificationDetailDto createNotification(CreateNotificationRequestDto req) {
        int nextNum = notificationCounter.incrementAndGet();
        String id = "notif-" + nextNum;
        String channelsJoined = req.getChannels() != null ? String.join(" + ", req.getChannels()) : "In-App";

        NotificationConfigPageDto.NotificationItemDto item = NotificationConfigPageDto.NotificationItemDto.builder()
                .id(id)
                .number(nextNum)
                .notificationName(req.getNotificationName() != null ? req.getNotificationName() : "Custom Notification")
                .eventTrigger(req.getEventTrigger() != null ? req.getEventTrigger() : "Custom event triggered")
                .category(req.getCategory() != null ? req.getCategory() : "Trade")
                .categoryTheme(req.getCategory() != null && req.getCategory().equalsIgnoreCase("Settlement") ? "yellow" : "blue")
                .channels(channelsJoined)
                .channelsTheme("blue")
                .status("Active")
                .statusTheme("green")
                .lastUpdated("29 Sep 2026")
                .build();

        notificationsList.add(0, item);

        NotificationDetailDto detail = NotificationDetailDto.builder()
                .id(id)
                .notificationName(req.getNotificationName() != null ? req.getNotificationName() : "Custom Notification")
                .notificationTitle(req.getCategory() + " Notification")
                .description(req.getMessage() != null ? req.getMessage() : "Triggered when event occurs")
                .status("Active")
                .statusTheme("green")
                .tabs(List.of("Details", "Conditions", "Recipients", "Templates", "Activity Log"))
                .activeTab("Details")
                .eventTrigger(req.getEventTrigger() != null ? req.getEventTrigger() : "Custom event")
                .category(req.getCategory() != null ? req.getCategory() : "Trade")
                .channels(channelsJoined)
                .createdOn("29 Sep 2026, 06:55 PM")
                .createdBy("Admin User")
                .lastUpdated("29 Sep 2026, 06:55 PM")
                .updatedBy("Admin User")
                .subject(req.getSubject() != null ? req.getSubject() : "Notification Alert")
                .message(req.getMessage() != null ? req.getMessage() : "System notification update.")
                .recipients(req.getRecipients() != null ? req.getRecipients() : "Assigned Users")
                .sendEmail(req.getSendEmail() != null && req.getSendEmail() ? "Yes" : "No")
                .sendInApp(req.getSendInApp() != null && req.getSendInApp() ? "Yes" : "No")
                .sendSms(req.getSendSms() != null && req.getSendSms() ? "Yes" : "No")
                .retryOnFailure(req.getRetryOnFailure() != null && req.getRetryOnFailure() ? "Yes" : "No")
                .maximumRetries(req.getMaximumRetries() != null ? req.getMaximumRetries() : 3)
                .build();

        notificationDetailsMap.put(id, detail);
        return detail;
    }

    public NotificationDetailDto cloneNotification(String id) {
        NotificationDetailDto src = getNotificationDetail(id);
        int nextNum = notificationCounter.incrementAndGet();
        String newId = "notif-" + nextNum;
        String newName = src.getNotificationName() + " (Copy)";

        NotificationDetailDto copy = NotificationDetailDto.builder()
                .id(newId)
                .notificationName(newName)
                .notificationTitle(src.getNotificationTitle())
                .description(src.getDescription())
                .status("Active")
                .statusTheme("green")
                .tabs(src.getTabs())
                .activeTab("Details")
                .eventTrigger(src.getEventTrigger())
                .category(src.getCategory())
                .channels(src.getChannels())
                .createdOn("29 Sep 2026, 07:00 PM")
                .createdBy("Admin User")
                .lastUpdated("29 Sep 2026, 07:00 PM")
                .updatedBy("Admin User")
                .subject(src.getSubject())
                .message(src.getMessage())
                .recipients(src.getRecipients())
                .sendEmail(src.getSendEmail())
                .sendInApp(src.getSendInApp())
                .sendSms(src.getSendSms())
                .retryOnFailure(src.getRetryOnFailure())
                .maximumRetries(src.getMaximumRetries())
                .build();

        notificationsList.add(0, NotificationConfigPageDto.NotificationItemDto.builder()
                .id(newId)
                .number(nextNum)
                .notificationName(newName)
                .eventTrigger(src.getEventTrigger())
                .category(src.getCategory())
                .categoryTheme("blue")
                .channels(src.getChannels())
                .channelsTheme("blue")
                .status("Active")
                .statusTheme("green")
                .lastUpdated("29 Sep 2026")
                .build());

        notificationDetailsMap.put(newId, copy);
        return copy;
    }

    public NotificationDetailDto deactivateNotification(String id) {
        NotificationDetailDto detail = getNotificationDetail(id);
        detail.setStatus("Inactive");
        detail.setStatusTheme("red");

        notificationsList.stream().filter(n -> n.getId().equalsIgnoreCase(id)).findFirst().ifPresent(n -> {
            n.setStatus("Inactive");
            n.setStatusTheme("red");
        });
        return detail;
    }

    // ==========================================
    // 8. SYSTEM SETTINGS
    // ==========================================
    public SystemSettingsPageDto getSystemSettingsPage(String company, String tab, String query, String module, String status, String dataType) {
        List<KpiCardDto> kpis = List.of(
                KpiCardDto.builder().title("Total Settings").value("36").change("+12% vs last quarter").comparisonText("").trend("positive").icon("settings").color("blue").build(),
                KpiCardDto.builder().title("Active Settings").value("30").change("+20% vs last quarter").comparisonText("").trend("positive").icon("check-circle").color("green").build(),
                KpiCardDto.builder().title("Disabled Settings").value("6").change("-25% vs last quarter").comparisonText("").trend("neutral").icon("pause-circle").color("red").build(),
                KpiCardDto.builder().title("Configuration Modules").value("8").change("+14% vs last quarter").comparisonText("").trend("positive").icon("grid").color("purple").build()
        );

        SystemSettingsPageDto.SettingsByModuleDonutDto donut = SystemSettingsPageDto.SettingsByModuleDonutDto.builder()
                .totalSettings("36 Total Settings")
                .slices(List.of(
                        new SystemSettingsPageDto.ModuleSliceDto("General", 22.2, 8, "#ef4444"),
                        new SystemSettingsPageDto.ModuleSliceDto("Security", 16.7, 6, "#3b82f6"),
                        new SystemSettingsPageDto.ModuleSliceDto("Trading", 16.7, 6, "#22c55e"),
                        new SystemSettingsPageDto.ModuleSliceDto("Settlement", 11.1, 4, "#f59e0b"),
                        new SystemSettingsPageDto.ModuleSliceDto("Compliance", 11.1, 4, "#a855f7"),
                        new SystemSettingsPageDto.ModuleSliceDto("Notifications", 11.1, 4, "#06b6d4"),
                        new SystemSettingsPageDto.ModuleSliceDto("System", 11.1, 4, "#94a3b8")
                )).build();

        List<SystemSettingsPageDto.SettingStatusBarDto> statusBar = List.of(
                new SystemSettingsPageDto.SettingStatusBarDto("Active", 83.3, 30, "#22c55e"),
                new SystemSettingsPageDto.SettingStatusBarDto("Inactive", 16.7, 6, "#ef4444")
        );

        List<SystemSettingsPageDto.RecentChangeRowDto> recentChanges = List.of(
                new SystemSettingsPageDto.RecentChangeRowDto("31 Aug 2026 04:15 PM", "Max Trade Limit", "₹5,00,00,000", "₹10,00,00,000", "Priya Mehta"),
                new SystemSettingsPageDto.RecentChangeRowDto("28 Aug 2026 11:20 AM", "KYC Mandatory", "False", "True", "Admin User"),
                new SystemSettingsPageDto.RecentChangeRowDto("25 Aug 2026 10:05 AM", "Session Timeout", "15", "30", "Priya Mehta"),
                new SystemSettingsPageDto.RecentChangeRowDto("20 Aug 2026 03:12 PM", "Password Expiry", "60", "90", "System"),
                new SystemSettingsPageDto.RecentChangeRowDto("18 Aug 2026 09:45 AM", "Email Notifications", "False", "True", "Admin User")
        );

        List<SystemSettingsPageDto.SettingItemDto> filtered = settingsList.stream()
                .filter(s -> {
                    if (query != null && !query.isBlank()) {
                        String q = query.toLowerCase();
                        if (!s.getSettingName().toLowerCase().contains(q) &&
                            !s.getSettingKey().toLowerCase().contains(q) &&
                            !s.getModule().toLowerCase().contains(q)) return false;
                    }
                    if (tab != null && !tab.isBlank() && !tab.equalsIgnoreCase("All Settings")) {
                        if (!s.getModule().equalsIgnoreCase(tab)) return false;
                    }
                    if (module != null && !module.isBlank() && !module.equalsIgnoreCase("All Modules")) {
                        if (!s.getModule().equalsIgnoreCase(module)) return false;
                    }
                    if (status != null && !status.isBlank() && !status.equalsIgnoreCase("All Status")) {
                        if (!s.getStatus().equalsIgnoreCase(status)) return false;
                    }
                    if (dataType != null && !dataType.isBlank() && !dataType.equalsIgnoreCase("All Data Types")) {
                        if (!s.getDataType().equalsIgnoreCase(dataType)) return false;
                    }
                    return true;
                }).collect(Collectors.toList());

        SystemSettingDetailDto selected = settingDetailsMap.getOrDefault("set-3", settingDetailsMap.values().iterator().next());

        return SystemSettingsPageDto.builder()
                .adminUser(adminUser)
                .pageTitle("System Settings")
                .pageSubtitle("Configure and manage platform-wide settings, integrations and system parameters.")
                .selectedCompany(company != null ? company : "Atirath Holdings")
                .kpis(kpis)
                .tabs(List.of("All Settings", "General", "Security", "Trading", "Settlement", "Compliance", "Integrations", "Notifications", "System"))
                .activeTab(tab != null ? tab : "All Settings")
                .modules(List.of("All Modules", "General", "Security", "Trading", "Settlement", "Compliance", "Integrations", "Notifications", "System"))
                .statuses(List.of("All Status", "Active", "Disabled"))
                .dataTypes(List.of("All Data Types", "Text", "Boolean", "Number", "Dropdown", "Email"))
                .settings(filtered)
                .totalSettings(36)
                .currentPage(1)
                .totalPages(4)
                .pageSize(10)
                .settingsByModule(donut)
                .settingsByStatus(statusBar)
                .recentConfigurationChanges(recentChanges)
                .selectedSettingDetail(selected)
                .build();
    }

    public SystemSettingDetailDto getSystemSettingDetail(String id) {
        return settingDetailsMap.getOrDefault(id, settingDetailsMap.get("set-3"));
    }

    public SystemSettingDetailDto addSystemSetting(AddSystemSettingRequestDto req) {
        int nextNum = settingCounter.incrementAndGet();
        String id = "set-" + nextNum;

        SystemSettingsPageDto.SettingItemDto item = SystemSettingsPageDto.SettingItemDto.builder()
                .id(id)
                .number(nextNum)
                .settingName(req.getSettingName() != null ? req.getSettingName() : "Custom Setting")
                .settingKey(req.getSettingKey() != null ? req.getSettingKey() : "custom.setting")
                .module(req.getModule() != null ? req.getModule() : "General")
                .dataType(req.getDataType() != null ? req.getDataType() : "Text")
                .currentValue(req.getCurrentValue() != null ? req.getCurrentValue() : "Default")
                .status("Active")
                .statusTheme("green")
                .lastUpdated("29 Sep 2026")
                .build();

        settingsList.add(0, item);

        SystemSettingDetailDto detail = SystemSettingDetailDto.builder()
                .id(id)
                .settingName(req.getSettingName() != null ? req.getSettingName() : "Custom Setting")
                .settingCategory(req.getModule() != null ? req.getModule() : "General")
                .description("Platform system setting configuration")
                .status("Active")
                .statusTheme("green")
                .tabs(List.of("Details", "Change History", "Affected Modules"))
                .activeTab("Details")
                .settingKey(req.getSettingKey() != null ? req.getSettingKey() : "custom.setting")
                .module(req.getModule() != null ? req.getModule() : "General")
                .dataType(req.getDataType() != null ? req.getDataType() : "Text")
                .currentValue(req.getCurrentValue() != null ? req.getCurrentValue() : "Default")
                .defaultValue(req.getDefaultValue() != null ? req.getDefaultValue() : "-")
                .minimumValue(req.getMinimumValue() != null ? req.getMinimumValue() : "-")
                .maximumValue(req.getMaximumValue() != null ? req.getMaximumValue() : "-")
                .createdOn("29 Sep 2026, 07:05 PM")
                .createdBy("Admin User")
                .lastUpdated("29 Sep 2026, 07:05 PM")
                .updatedBy("Admin User")
                .validationRules(List.of(
                        "Must be a positive number",
                        "Applies to all users"
                ))
                .affectedModules(List.of(req.getModule() != null ? req.getModule() : "General"))
                .build();

        settingDetailsMap.put(id, detail);
        return detail;
    }

    public SystemSettingDetailDto updateSystemSetting(String id, UpdateSystemSettingRequestDto req) {
        SystemSettingDetailDto detail = getSystemSettingDetail(id);
        if (req.getCurrentValue() != null) {
            detail.setCurrentValue(req.getCurrentValue());
        }
        if (req.getStatus() != null) {
            detail.setStatus(req.getStatus());
            detail.setStatusTheme(req.getStatus().equalsIgnoreCase("Active") ? "green" : "red");
        }
        detail.setLastUpdated("29 Sep 2026, 07:10 PM");
        detail.setUpdatedBy("Admin User");

        settingsList.stream().filter(s -> s.getId().equalsIgnoreCase(id)).findFirst().ifPresent(s -> {
            if (req.getCurrentValue() != null) s.setCurrentValue(req.getCurrentValue());
            if (req.getStatus() != null) {
                s.setStatus(req.getStatus());
                s.setStatusTheme(req.getStatus().equalsIgnoreCase("Active") ? "green" : "red");
            }
            s.setLastUpdated("29 Sep 2026");
        });

        return detail;
    }

    public SystemSettingDetailDto deactivateSystemSetting(String id) {
        SystemSettingDetailDto detail = getSystemSettingDetail(id);
        detail.setStatus("Disabled");
        detail.setStatusTheme("red");

        settingsList.stream().filter(s -> s.getId().equalsIgnoreCase(id)).findFirst().ifPresent(s -> {
            s.setStatus("Disabled");
            s.setStatusTheme("red");
        });
        return detail;
    }

    // ==========================================
    // 9. AUDIT & ACTIVITY LOGS
    // ==========================================
    public AuditActivityLogsPageDto getAuditLogsPage(String company, String tab, String query, String module, String action, String user, String fromDate, String toDate) {
        List<KpiCardDto> kpis = List.of(
                KpiCardDto.builder().title("Total Logs").value("12,842").change("+18% vs last quarter").comparisonText("").trend("positive").icon("file-text").color("blue").build(),
                KpiCardDto.builder().title("User Activities").value("6,420").change("+12% vs last quarter").comparisonText("").trend("positive").icon("users").color("green").build(),
                KpiCardDto.builder().title("System Changes").value("2,184").change("+25% vs last quarter").comparisonText("").trend("positive").icon("settings").color("red").build(),
                KpiCardDto.builder().title("Security Events").value("1,206").change("-8% vs last quarter").comparisonText("").trend("positive").icon("shield").color("blue").build(),
                KpiCardDto.builder().title("Failed Actions").value("432").change("-15% vs last quarter").comparisonText("").trend("positive").icon("alert-triangle").color("red").build()
        );

        AuditActivityLogsPageDto.LogsByModuleDonutDto donut = AuditActivityLogsPageDto.LogsByModuleDonutDto.builder()
                .totalLogs("12,842 Total Logs")
                .slices(List.of(
                        new AuditActivityLogsPageDto.ModuleSliceDto("Authentication", 28.4, 3648, "#ef4444"),
                        new AuditActivityLogsPageDto.ModuleSliceDto("Trading", 18.2, 2338, "#3b82f6"),
                        new AuditActivityLogsPageDto.ModuleSliceDto("User Management", 15.6, 2002, "#22c55e"),
                        new AuditActivityLogsPageDto.ModuleSliceDto("System Settings", 12.1, 1553, "#f59e0b"),
                        new AuditActivityLogsPageDto.ModuleSliceDto("Notification", 9.8, 1260, "#a855f7"),
                        new AuditActivityLogsPageDto.ModuleSliceDto("Compliance", 8.7, 1118, "#06b6d4"),
                        new AuditActivityLogsPageDto.ModuleSliceDto("Others", 7.2, 923, "#94a3b8")
                )).build();

        List<AuditActivityLogsPageDto.ActionTypeBarDto> actionBars = List.of(
                new AuditActivityLogsPageDto.ActionTypeBarDto("Login", 3648, 28.4, "#10b981"),
                new AuditActivityLogsPageDto.ActionTypeBarDto("Update", 2915, 22.7, "#3b82f6"),
                new AuditActivityLogsPageDto.ActionTypeBarDto("Create", 1982, 15.4, "#a855f7"),
                new AuditActivityLogsPageDto.ActionTypeBarDto("Delete", 923, 7.2, "#ef4444"),
                new AuditActivityLogsPageDto.ActionTypeBarDto("Approve", 1445, 11.3, "#f59e0b"),
                new AuditActivityLogsPageDto.ActionTypeBarDto("Others", 1929, 15.0, "#64748b")
        );

        List<AuditActivityLogsPageDto.ActivityTrendMonthDto> trend = List.of(
                new AuditActivityLogsPageDto.ActivityTrendMonthDto("Apr 2026", 120, 90, 45, 18),
                new AuditActivityLogsPageDto.ActivityTrendMonthDto("May 2026", 180, 140, 60, 24),
                new AuditActivityLogsPageDto.ActivityTrendMonthDto("Jun 2026", 260, 200, 85, 30),
                new AuditActivityLogsPageDto.ActivityTrendMonthDto("Jul 2026", 340, 260, 110, 38),
                new AuditActivityLogsPageDto.ActivityTrendMonthDto("Aug 2026", 420, 330, 135, 45),
                new AuditActivityLogsPageDto.ActivityTrendMonthDto("Sep 2026", 500, 390, 160, 52)
        );

        List<AuditActivityLogsPageDto.AuditLogItemDto> filtered = auditLogsList.stream()
                .filter(l -> {
                    if (query != null && !query.isBlank()) {
                        String q = query.toLowerCase();
                        if (!l.getUserName().toLowerCase().contains(q) &&
                            !l.getAction().toLowerCase().contains(q) &&
                            !l.getModule().toLowerCase().contains(q) &&
                            !l.getIpAddress().toLowerCase().contains(q)) return false;
                    }
                    if (tab != null && !tab.isBlank() && !tab.equalsIgnoreCase("All Logs")) {
                        if (tab.equalsIgnoreCase("User Activities") && (!l.getAction().equalsIgnoreCase("Login") && !l.getAction().equalsIgnoreCase("Create"))) return false;
                        if (tab.equalsIgnoreCase("System Changes") && (!l.getAction().equalsIgnoreCase("Update") && !l.getAction().equalsIgnoreCase("Delete"))) return false;
                        if (tab.equalsIgnoreCase("Security Events") && !l.getModule().equalsIgnoreCase("Authentication")) return false;
                        if (tab.equalsIgnoreCase("Failed Actions") && !l.getStatus().equalsIgnoreCase("Failed")) return false;
                    }
                    if (module != null && !module.isBlank() && !module.equalsIgnoreCase("All Modules")) {
                        if (!l.getModule().equalsIgnoreCase(module)) return false;
                    }
                    if (action != null && !action.isBlank() && !action.equalsIgnoreCase("All Actions")) {
                        if (!l.getAction().equalsIgnoreCase(action)) return false;
                    }
                    if (user != null && !user.isBlank() && !user.equalsIgnoreCase("All Users")) {
                        if (!l.getUserName().equalsIgnoreCase(user)) return false;
                    }
                    return true;
                }).collect(Collectors.toList());

        AuditLogDetailDto selected = auditLogDetailsMap.getOrDefault("log-1", auditLogDetailsMap.values().iterator().next());

        return AuditActivityLogsPageDto.builder()
                .adminUser(adminUser)
                .pageTitle("Audit & Activity Logs")
                .pageSubtitle("Track and review all user activities, system changes and important events across the platform.")
                .selectedCompany(company != null ? company : "Atirath Holdings")
                .kpis(kpis)
                .tabs(List.of("All Logs", "User Activities", "System Changes", "Security Events", "Failed Actions"))
                .activeTab(tab != null ? tab : "All Logs")
                .modules(List.of("All Modules", "Authentication", "Trading Rules", "User Management", "Approval Workflow", "System Settings", "Notification", "Reports", "Access Control"))
                .actions(List.of("All Actions", "Login", "Update", "Create", "Approve", "Delete", "Export", "Failed Login"))
                .users(List.of("All Users", "Rakesh Kumar", "Priya Mehta", "Suresh Kumar", "Ananya Reddy", "Vikram Rao", "Neha Sharma", "Rajesh Mehta", "Lakshmi Narayan", "Kavya Iyer", "Sunil Verma"))
                .logs(filtered)
                .totalLogs(12842)
                .currentPage(1)
                .totalPages(1285)
                .pageSize(10)
                .logsByModule(donut)
                .logsByActionType(actionBars)
                .activityTrend(trend)
                .selectedLogDetail(selected)
                .build();
    }

    public AuditLogDetailDto getAuditLogDetail(String id) {
        return auditLogDetailsMap.getOrDefault(id, auditLogDetailsMap.get("log-1"));
    }

    // ==========================================
    // SEEDING HELPERS
    // ==========================================
    private void seedUsers() {
        usersList.clear();
        usersList.add(new UserManagementPageDto.UserItemDto("usr-1", 1, "RK", "Rakesh Kumar", "rakesh.kumar@atirath.com", "Atirath Holdings", "Operations", "Operations Manager", "Active", "green", "29 Sep 2026, 10:15 AM", "01 Jan 2026"));
        usersList.add(new UserManagementPageDto.UserItemDto("usr-2", 2, "PM", "Priya Mehta", "priya.mehta@farmvantara.com", "Farm Vantara", "Finance", "Finance Executive", "Active", "green", "28 Sep 2026, 05:42 PM", "15 Jan 2026"));
        usersList.add(new UserManagementPageDto.UserItemDto("usr-3", 3, "VR", "Vikram Rao", "vikram.rao@atirath.com", "Atirath Traders", "Trading", "Trade Analyst", "Active", "green", "27 Sep 2026, 11:20 AM", "10 Jan 2026"));
        usersList.add(new UserManagementPageDto.UserItemDto("usr-4", 4, "NS", "Neha Sharma", "neha.sharma@sunrise.com", "Sunrise Ventures", "Compliance", "Compliance Officer", "Active", "green", "26 Sep 2026, 09:18 AM", "12 Jan 2026"));
        usersList.add(new UserManagementPageDto.UserItemDto("usr-5", 5, "SK", "Suresh Kumar", "suresh.kumar@greenfuture.com", "Green Future Fund", "Settlements", "Settlement Executive", "Inactive", "red", "18 Sep 2026, 04:32 PM", "05 Jan 2026"));
        usersList.add(new UserManagementPageDto.UserItemDto("usr-6", 6, "LN", "Lakshmi Narayan", "lakshmi.n@globalagri.com", "Global Agri Fund", "Investor Relations", "Investor Manager", "Active", "green", "29 Sep 2026, 09:05 AM", "08 Jan 2026"));
        usersList.add(new UserManagementPageDto.UserItemDto("usr-7", 7, "AR", "Ananya Reddy", "ananya.reddy@atirathtech.com", "Atirath Technologies", "IT", "System Administrator", "Active", "green", "29 Sep 2026, 08:55 AM", "03 Jan 2026"));
        usersList.add(new UserManagementPageDto.UserItemDto("usr-8", 8, "SV", "Sunil Verma", "sunil.verma@farmvantara.com", "Farm Vantara", "Operations", "Operations Executive", "Pending", "orange", "-", "20 Sep 2026"));
        usersList.add(new UserManagementPageDto.UserItemDto("usr-9", 9, "KI", "Kavya Iyer", "kavya.iyer@atirath.com", "Atirath Traders", "Trading", "Trade Executive", "Active", "green", "28 Sep 2026, 03:45 PM", "18 Jan 2026"));
        usersList.add(new UserManagementPageDto.UserItemDto("usr-10", 10, "RM", "Rajesh Mehta", "rajesh.mehta@sunrise.com", "Sunrise Ventures", "Finance", "Finance Manager", "Inactive", "red", "12 Sep 2026, 11:30 AM", "02 Jan 2026"));

        UserDetailDto usr1 = UserDetailDto.builder()
                .id("usr-1")
                .initials("RK")
                .fullName("Rakesh Kumar")
                .title("Operations Manager")
                .email("rakesh.kumar@atirath.com")
                .status("Active")
                .statusTheme("green")
                .tabs(List.of("Details", "Roles & Permissions", "Activity Logs"))
                .activeTab("Details")
                .phone("+91 98765 43210")
                .employeeId("EMP-2026-001")
                .department("Operations")
                .company("Atirath Holdings")
                .reportingManager("Priya Mehta")
                .location("Hyderabad, India")
                .timeZone("IST (UTC+5:30)")
                .createdOn("01 Jan 2026, 10:00 AM")
                .lastLogin("29 Sep 2026, 10:15 AM")
                .loginAttempts(0)
                .passwordLastChanged("15 Sep 2026")
                .mfaEnabled("Yes")
                .accountExpires("-")
                .primaryRole("Operations Manager")
                .additionalRoles("Trade Viewer, Report Viewer")
                .accessLevel("Company Level")
                .build();

        userDetailsMap.put("usr-1", usr1);
    }

    private void seedRoles() {
        rolesList.clear();
        rolesList.add(new RoleManagementPageDto.RoleItemDto("rol-1", 1, "System Administrator", "System", "blue", "Full system access", 3, "Active", "green", "Full Access", "red", "01 Jan 2026"));
        rolesList.add(new RoleManagementPageDto.RoleItemDto("rol-2", 2, "Company Admin", "System", "blue", "Manage company settings", 5, "Active", "green", "High", "orange", "02 Jan 2026"));
        rolesList.add(new RoleManagementPageDto.RoleItemDto("rol-3", 3, "Operations Manager", "System", "blue", "Manage trading operations", 8, "Active", "green", "High", "orange", "02 Jan 2026"));
        rolesList.add(new RoleManagementPageDto.RoleItemDto("rol-4", 4, "Trade Analyst", "Custom", "purple", "Create and analyse trades", 12, "Active", "green", "Medium", "yellow", "05 Jan 2026"));
        rolesList.add(new RoleManagementPageDto.RoleItemDto("rol-5", 5, "Settlement Executive", "Custom", "purple", "Manage settlements", 10, "Active", "green", "Medium", "yellow", "05 Jan 2026"));
        rolesList.add(new RoleManagementPageDto.RoleItemDto("rol-6", 6, "Compliance Officer", "System", "blue", "Compliance and KYC", 6, "Active", "green", "Medium", "yellow", "06 Jan 2026"));
        rolesList.add(new RoleManagementPageDto.RoleItemDto("rol-7", 7, "Finance Executive", "Custom", "purple", "Payments and reconciliation", 8, "Active", "green", "Medium", "yellow", "06 Jan 2026"));
        rolesList.add(new RoleManagementPageDto.RoleItemDto("rol-8", 8, "Investor Manager", "Custom", "purple", "Manage investors", 4, "Active", "green", "Medium", "yellow", "07 Jan 2026"));
        rolesList.add(new RoleManagementPageDto.RoleItemDto("rol-9", 9, "Report Viewer", "Custom", "purple", "View reports and dashboards", 18, "Active", "green", "Low", "green", "07 Jan 2026"));
        rolesList.add(new RoleManagementPageDto.RoleItemDto("rol-10", 10, "Read Only User", "System", "blue", "Read-only access to platform", 54, "Active", "green", "Low", "green", "08 Jan 2026"));

        RoleDetailDto rol3 = RoleDetailDto.builder()
                .id("rol-3")
                .initials("OM")
                .roleName("Operations Manager")
                .roleType("System Role")
                .description("Manage trading operations across the platform.")
                .status("Active")
                .statusTheme("green")
                .tabs(List.of("Details", "Permissions", "Users", "Activity Logs"))
                .activeTab("Details")
                .accessLevel("High")
                .accessLevelTheme("orange")
                .createdOn("02 Jan 2026, 10:30 AM")
                .createdBy("System")
                .lastUpdated("15 Sep 2026, 09:45 AM")
                .totalPermissions(48)
                .usersAssigned(8)
                .modulesCount(13)
                .permissionsCount(48)
                .usersCount(8)
                .conditionsCount(2)
                .recentActivities(List.of(
                        new RoleDetailDto.RoleActivityDto("Updated permissions", "15 Sep 2026, 09:45 AM by Admin", "blue"),
                        new RoleDetailDto.RoleActivityDto("User assigned", "12 Sep 2026, 04:20 PM by Priya Mehta", "green"),
                        new RoleDetailDto.RoleActivityDto("Role details updated", "05 Sep 2026, 11:10 AM by Admin", "orange")
                ))
                .build();

        roleDetailsMap.put("rol-3", rol3);
    }

    private void seedPermissions() {
        permissionsList.clear();
        permissionsList.add(new PermissionManagementPageDto.PermissionItemDto("per-1", 1, "View Trades", "Trade Management", "Read", "blue", "View trade details and list", 5, "Active", "green", "01 Jan 2026"));
        permissionsList.add(new PermissionManagementPageDto.PermissionItemDto("per-2", 2, "Create Trade", "Trade Management", "Create", "green", "Create new trade", 3, "Active", "green", "01 Jan 2026"));
        permissionsList.add(new PermissionManagementPageDto.PermissionItemDto("per-3", 3, "Edit Trade", "Trade Management", "Update", "yellow", "Modify trade details", 3, "Active", "green", "01 Jan 2026"));
        permissionsList.add(new PermissionManagementPageDto.PermissionItemDto("per-4", 4, "Delete Trade", "Trade Management", "Delete", "red", "Delete trade records", 2, "Active", "green", "01 Jan 2026"));
        permissionsList.add(new PermissionManagementPageDto.PermissionItemDto("per-5", 5, "Approve Trade", "Approval Workflow", "Approve", "purple", "Approve trades for execution", 4, "Active", "green", "02 Jan 2026"));
        permissionsList.add(new PermissionManagementPageDto.PermissionItemDto("per-6", 6, "View Settlements", "Settlement", "Read", "blue", "View settlement records", 5, "Active", "green", "02 Jan 2026"));
        permissionsList.add(new PermissionManagementPageDto.PermissionItemDto("per-7", 7, "Initiate Settlement", "Settlement", "Create", "green", "Initiate settlement process", 3, "Active", "green", "02 Jan 2026"));
        permissionsList.add(new PermissionManagementPageDto.PermissionItemDto("per-8", 8, "Manage Users", "User Management", "Manage", "teal", "Create, edit and manage users", 2, "Active", "green", "03 Jan 2026"));
        permissionsList.add(new PermissionManagementPageDto.PermissionItemDto("per-9", 9, "Manage Roles", "Role Management", "Manage", "teal", "Create and manage roles", 2, "Active", "green", "03 Jan 2026"));
        permissionsList.add(new PermissionManagementPageDto.PermissionItemDto("per-10", 10, "System Settings", "System Settings", "Manage", "teal", "Access and modify system settings", 1, "Active", "green", "03 Jan 2026"));

        PermissionDetailDto per1 = PermissionDetailDto.builder()
                .id("per-1")
                .permissionName("View Trades")
                .permissionId("PER-001")
                .permissionTypeTitle("Read Permission")
                .status("Active")
                .statusTheme("green")
                .tabs(List.of("Details", "Assigned Roles", "Assigned Users", "Activity Log"))
                .activeTab("Details")
                .module("Trade Management")
                .permissionType("Read")
                .description("View trade details and list of trades across the platform.")
                .createdOn("01 Jan 2026, 10:00 AM")
                .createdBy("Admin User")
                .lastUpdated("15 Sep 2026, 09:30 AM")
                .updatedBy("Priya Mehta")
                .rolesCount(5)
                .usersCount(28)
                .conditionsCount(3)
                .parentPermission("-")
                .dependencies("-")
                .permissionCode("TRADE_VIEW")
                .build();

        permissionDetailsMap.put("per-1", per1);
    }

    private void seedAccess() {
        accessList.clear();
        accessList.add(new AccessControlPageDto.AccessItemDto("acc-1", 1, "RK", "Rakesh Kumar", "Operations Manager", "Trading, Settlement +3", "Full Access", "green", "Active", "green", "01 Jan 2026", "-"));
        accessList.add(new AccessControlPageDto.AccessItemDto("acc-2", 2, "PM", "Priya Mehta", "Finance Executive", "Payments, Reconciliation", "Full Access", "green", "Active", "green", "01 Jan 2026", "-"));
        accessList.add(new AccessControlPageDto.AccessItemDto("acc-3", 3, "VR", "Vikram Rao", "Trade Analyst", "Trading, Reports", "Limited", "blue", "Active", "green", "02 Jan 2026", "31 Dec 2026"));
        accessList.add(new AccessControlPageDto.AccessItemDto("acc-4", 4, "NS", "Neha Sharma", "Compliance Officer", "KYC, Compliance +2", "Limited", "blue", "Active", "green", "02 Jan 2026", "-"));
        accessList.add(new AccessControlPageDto.AccessItemDto("acc-5", 5, "SK", "Suresh Kumar", "Settlement Executive", "Settlement, Escrow", "Limited", "blue", "Active", "green", "03 Jan 2026", "31 Dec 2026"));
        accessList.add(new AccessControlPageDto.AccessItemDto("acc-6", 6, "LN", "Lakshmi Narayan", "Investor Manager", "Investors, Reports", "Read Only", "yellow", "Active", "green", "03 Jan 2026", "-"));
        accessList.add(new AccessControlPageDto.AccessItemDto("acc-7", 7, "AR", "Ananya Reddy", "System Administrator", "All Modules", "Full Access", "green", "Active", "green", "01 Jan 2026", "-"));
        accessList.add(new AccessControlPageDto.AccessItemDto("acc-8", 8, "SV", "Sunil Verma", "Operations Executive", "Trading, Disputes", "Limited", "blue", "Pending", "orange", "15 Sep 2026", "-"));
        accessList.add(new AccessControlPageDto.AccessItemDto("acc-9", 9, "KI", "Kavya Iyer", "Trade Executive", "Trading", "Limited", "blue", "Active", "green", "04 Jan 2026", "31 Dec 2026"));
        accessList.add(new AccessControlPageDto.AccessItemDto("acc-10", 10, "RM", "Rajesh Mehta", "Finance Manager", "Payments, Reports +2", "Full Access", "green", "Active", "green", "04 Jan 2026", "-"));

        AccessDetailDto acc1 = AccessDetailDto.builder()
                .id("acc-1")
                .initials("RK")
                .userName("Rakesh Kumar")
                .role("Operations Manager")
                .email("rakesh.kumar@atirath.com")
                .status("Active")
                .statusTheme("green")
                .tabs(List.of("Overview", "Module Access", "Role Access", "Activity Log"))
                .activeTab("Overview")
                .userId("EMP-2026-001")
                .department("Operations")
                .company("Atirath Holdings")
                .accessLevel("Full Access")
                .accessLevelTheme("green")
                .grantedOn("01 Jan 2026, 10:00 AM")
                .expiresOn("-")
                .lastUpdated("15 Sep 2026, 09:30 AM")
                .updatedBy("Admin User")
                .moduleAccessList(List.of(
                        new AccessDetailDto.ModuleAccessItemDto("Trade Management", "Full Access", "green"),
                        new AccessDetailDto.ModuleAccessItemDto("Settlement", "Full Access", "green"),
                        new AccessDetailDto.ModuleAccessItemDto("Escrow", "Full Access", "green"),
                        new AccessDetailDto.ModuleAccessItemDto("Disputes", "Limited", "blue"),
                        new AccessDetailDto.ModuleAccessItemDto("Reports", "Read Only", "yellow"),
                        new AccessDetailDto.ModuleAccessItemDto("System Administration", "No Access", "red")
                ))
                .primaryRole("Operations Manager")
                .additionalRoles("-")
                .inheritedRoles("-")
                .recentActivities(List.of(
                        new AccessDetailDto.AccessActivityDto("Access updated", "15 Sep 2026, 09:30 AM by Admin User", "blue"),
                        new AccessDetailDto.AccessActivityDto("Role assigned", "01 Jan 2026, 10:00 AM by System", "green"),
                        new AccessDetailDto.AccessActivityDto("Module access changed", "20 Dec 2025, 04:15 PM by Admin User", "orange")
                ))
                .build();

        accessDetailsMap.put("acc-1", acc1);
    }

    private void seedWorkflows() {
        workflowsList.clear();
        workflowsList.add(new ApprovalWorkflowPageDto.WorkflowItemDto("wf-1", 1, "Trade Execution Approval", "Trade Management", "Approval for trade execution", "3 Levels", "Active", "green", "15 Sep 2026"));
        workflowsList.add(new ApprovalWorkflowPageDto.WorkflowItemDto("wf-2", 2, "Settlement Release Approval", "Settlement", "Approval for settlement release", "2 Levels", "Active", "green", "14 Sep 2026"));
        workflowsList.add(new ApprovalWorkflowPageDto.WorkflowItemDto("wf-3", 3, "Large Trade Approval", "Trade Management", "High value trade approval (> ₹10 Cr)", "4 Levels", "Active", "green", "12 Sep 2026"));
        workflowsList.add(new ApprovalWorkflowPageDto.WorkflowItemDto("wf-4", 4, "User Creation Approval", "User Management", "Approve new user accounts", "2 Levels", "Active", "green", "10 Sep 2026"));
        workflowsList.add(new ApprovalWorkflowPageDto.WorkflowItemDto("wf-5", 5, "Role Assignment Approval", "Role Management", "Approve role assignments", "2 Levels", "Active", "green", "10 Sep 2026"));
        workflowsList.add(new ApprovalWorkflowPageDto.WorkflowItemDto("wf-6", 6, "Module Access Approval", "Permission Management", "Approve module access changes", "3 Levels", "Active", "green", "09 Sep 2026"));
        workflowsList.add(new ApprovalWorkflowPageDto.WorkflowItemDto("wf-7", 7, "KYC Approval", "Compliance", "Approve KYC verification", "2 Levels", "Active", "green", "08 Sep 2026"));
        workflowsList.add(new ApprovalWorkflowPageDto.WorkflowItemDto("wf-8", 8, "Escrow Release Approval", "Escrow", "Approve escrow fund release", "3 Levels", "Inactive", "red", "05 Sep 2026"));
        workflowsList.add(new ApprovalWorkflowPageDto.WorkflowItemDto("wf-9", 9, "System Settings Approval", "System Settings", "Approve critical system changes", "3 Levels", "Active", "green", "04 Sep 2026"));
        workflowsList.add(new ApprovalWorkflowPageDto.WorkflowItemDto("wf-10", 10, "Report Publication Approval", "Reports", "Approve external report publication", "2 Levels", "Active", "green", "02 Sep 2026"));

        WorkflowDetailDto wf1 = WorkflowDetailDto.builder()
                .id("wf-1")
                .workflowName("Trade Execution Approval")
                .module("Trade Management")
                .description("Approval for trade execution")
                .status("Active")
                .statusTheme("green")
                .tabs(List.of("Details", "Approval Levels", "Conditions", "Activity Log"))
                .activeTab("Details")
                .createdOn("01 Jan 2026, 10:00 AM")
                .createdBy("Admin User")
                .lastUpdated("15 Sep 2026, 09:30 AM")
                .updatedBy("Priya Mehta")
                .totalApprovalLevels(3)
                .approvalType("Sequential")
                .approvalRequired("All Levels")
                .allowDelegation("Yes")
                .autoEscalation("Yes")
                .escalationTime("24 hours")
                .conditions(List.of(
                        new WorkflowDetailDto.WorkflowConditionDto("Trade Value > ₹10 Lakhs", true),
                        new WorkflowDetailDto.WorkflowConditionDto("Counterparty Risk Rating >= Medium", true),
                        new WorkflowDetailDto.WorkflowConditionDto("New Counterparty", true),
                        new WorkflowDetailDto.WorkflowConditionDto("Cross-border Trade", true)
                ))
                .build();

        workflowDetailsMap.put("wf-1", wf1);
    }

    private void seedTradingRules() {
        tradingRulesList.clear();
        tradingRulesList.add(new TradingRuleConfigPageDto.TradingRuleItemDto("trd-1", 1, "Trade Value Limit", "Trading Limit", "blue", "Threshold", "Max trade value per transaction", "Active", "green", "High", "red", "15 Sep 2026"));
        tradingRulesList.add(new TradingRuleConfigPageDto.TradingRuleItemDto("trd-2", 2, "Position Limit", "Trading Limit", "blue", "Threshold", "Max open position per user", "Active", "green", "High", "red", "14 Sep 2026"));
        tradingRulesList.add(new TradingRuleConfigPageDto.TradingRuleItemDto("trd-3", 3, "Counterparty Rating", "Risk Management", "blue", "Validation", "Minimum rating for counterparties", "Active", "green", "Medium", "orange", "12 Sep 2026"));
        tradingRulesList.add(new TradingRuleConfigPageDto.TradingRuleItemDto("trd-4", 4, "Instrument Eligibility", "Market Rule", "blue", "Validation", "Allowed instruments for trading", "Active", "green", "Medium", "orange", "10 Sep 2026"));
        tradingRulesList.add(new TradingRuleConfigPageDto.TradingRuleItemDto("trd-5", 5, "Trading Hours", "Market Rule", "blue", "Time Based", "Allowed trading time windows", "Active", "green", "High", "red", "09 Sep 2026"));
        tradingRulesList.add(new TradingRuleConfigPageDto.TradingRuleItemDto("trd-6", 6, "Price Deviation Check", "Risk Management", "blue", "Validation", "Price deviation threshold", "Inactive", "red", "High", "red", "08 Sep 2026"));
        tradingRulesList.add(new TradingRuleConfigPageDto.TradingRuleItemDto("trd-7", 7, "Daily Trade Limit", "Trading Limit", "blue", "Threshold", "Max daily trade value per user", "Active", "green", "Medium", "orange", "07 Sep 2026"));
        tradingRulesList.add(new TradingRuleConfigPageDto.TradingRuleItemDto("trd-8", 8, "KYC Verification", "Compliance", "purple", "Mandatory", "KYC must be completed before trade", "Active", "green", "High", "red", "05 Sep 2026"));
        tradingRulesList.add(new TradingRuleConfigPageDto.TradingRuleItemDto("trd-9", 9, "Sanction Screening", "Compliance", "purple", "Validation", "Check against sanction lists", "Active", "green", "High", "red", "04 Sep 2026"));
        tradingRulesList.add(new TradingRuleConfigPageDto.TradingRuleItemDto("trd-10", 10, "Market Circuit Breaker", "Market Rule", "blue", "System", "Auto halt on market volatility", "Active", "green", "Critical", "darkred", "02 Sep 2026"));

        TradingRuleDetailDto trd1 = TradingRuleDetailDto.builder()
                .id("trd-1")
                .ruleName("Trade Value Limit")
                .ruleCategory("Trading Limit")
                .shortDescription("Maximum trade value per transaction")
                .status("Active")
                .statusTheme("green")
                .tabs(List.of("Details", "Conditions", "Affected Modules", "Activity Log"))
                .activeTab("Details")
                .ruleId("TRD-001")
                .category("Trading Limit")
                .type("Threshold")
                .description("Maximum trade value per transaction")
                .priority("High")
                .priorityTheme("red")
                .createdOn("01 Jan 2026, 10:00 AM")
                .createdBy("Admin User")
                .lastUpdated("15 Sep 2026, 09:30 AM")
                .updatedBy("Priya Mehta")
                .limitType("Per Transaction")
                .maximumValue("₹10,00,00,000")
                .currency("INR")
                .appliesTo("All Users")
                .overrideAllowed("No")
                .alertThreshold("80%")
                .autoBlock("Yes")
                .conditions(List.of(
                        new TradingRuleDetailDto.TradingRuleConditionDto("User must be KYC verified", true),
                        new TradingRuleDetailDto.TradingRuleConditionDto("Counterparty rating >= Minimum", true),
                        new TradingRuleDetailDto.TradingRuleConditionDto("Instrument must be eligible", true),
                        new TradingRuleDetailDto.TradingRuleConditionDto("Within trading hours", true)
                ))
                .build();

        tradingRuleDetailsMap.put("trd-1", trd1);
    }

    private void seedNotifications() {
        notificationsList.clear();
        notificationsList.add(new NotificationConfigPageDto.NotificationItemDto("notif-1", 1, "Trade Created", "New trade created", "Trade", "blue", "In-App", "blue", "Active", "green", "15 Sep 2026"));
        notificationsList.add(new NotificationConfigPageDto.NotificationItemDto("notif-2", 2, "Trade Approval", "Trade approved", "Trade", "blue", "Email + In-App", "blue", "Active", "green", "14 Sep 2026"));
        notificationsList.add(new NotificationConfigPageDto.NotificationItemDto("notif-3", 3, "Trade Rejected", "Trade rejected", "Trade", "blue", "Email + In-App", "blue", "Active", "green", "14 Sep 2026"));
        notificationsList.add(new NotificationConfigPageDto.NotificationItemDto("notif-4", 4, "Settlement Initiated", "Settlement process started", "Settlement", "yellow", "Email + In-App", "blue", "Active", "green", "12 Sep 2026"));
        notificationsList.add(new NotificationConfigPageDto.NotificationItemDto("notif-5", 5, "Settlement Completed", "Settlement completed", "Settlement", "yellow", "Email + In-App + SMS", "green", "Active", "green", "12 Sep 2026"));
        notificationsList.add(new NotificationConfigPageDto.NotificationItemDto("notif-6", 6, "Settlement Failed", "Settlement failed", "Settlement", "yellow", "Email + SMS", "green", "Active", "green", "10 Sep 2026"));
        notificationsList.add(new NotificationConfigPageDto.NotificationItemDto("notif-7", 7, "KYC Verification", "KYC status updated", "Compliance", "purple", "Email + In-App", "blue", "Active", "green", "09 Sep 2026"));
        notificationsList.add(new NotificationConfigPageDto.NotificationItemDto("notif-8", 8, "User Registration", "New user registration", "User", "purple", "Email + In-App", "blue", "Active", "green", "08 Sep 2026"));
        notificationsList.add(new NotificationConfigPageDto.NotificationItemDto("notif-9", 9, "Role Assignment", "Role assigned to user", "User", "purple", "In-App", "blue", "Active", "green", "07 Sep 2026"));
        notificationsList.add(new NotificationConfigPageDto.NotificationItemDto("notif-10", 10, "System Alert", "Critical system alert", "System", "blue", "Email + SMS", "green", "Inactive", "red", "05 Sep 2026"));

        NotificationDetailDto notif1 = NotificationDetailDto.builder()
                .id("notif-1")
                .notificationName("Trade Created")
                .notificationTitle("Trade Notification")
                .description("Triggered when a new trade is created")
                .status("Active")
                .statusTheme("green")
                .tabs(List.of("Details", "Conditions", "Recipients", "Templates", "Activity Log"))
                .activeTab("Details")
                .eventTrigger("New trade created")
                .category("Trade")
                .channels("In-App")
                .createdOn("01 Jan 2026, 10:00 AM")
                .createdBy("Admin User")
                .lastUpdated("15 Sep 2026, 09:30 AM")
                .updatedBy("Priya Mehta")
                .subject("New Trade Created")
                .message("A new trade has been created with Trade ID: {trade_id}. Please review the trade details.")
                .recipients("Assigned Users, Approvers")
                .sendEmail("Yes")
                .sendInApp("Yes")
                .sendSms("No")
                .retryOnFailure("Yes")
                .maximumRetries(3)
                .build();

        notificationDetailsMap.put("notif-1", notif1);
    }

    private void seedSettings() {
        settingsList.clear();
        settingsList.add(new SystemSettingsPageDto.SettingItemDto("set-1", 1, "Platform Name", "platform.name", "General", "Text", "Atirath Trading Platform", "Active", "green", "01 Sep 2026"));
        settingsList.add(new SystemSettingsPageDto.SettingItemDto("set-2", 2, "Maintenance Mode", "system.maintenance", "System", "Boolean", "False", "Active", "green", "01 Sep 2026"));
        settingsList.add(new SystemSettingsPageDto.SettingItemDto("set-3", 3, "Max Trade Limit", "trading.max_limit", "Trading", "Number", "₹10,00,00,000", "Active", "green", "31 Aug 2026"));
        settingsList.add(new SystemSettingsPageDto.SettingItemDto("set-4", 4, "Default Settlement Cycle", "settlement.cycle", "Settlement", "Dropdown", "T+1", "Active", "green", "30 Aug 2026"));
        settingsList.add(new SystemSettingsPageDto.SettingItemDto("set-5", 5, "KYC Mandatory", "compliance.kyc_required", "Compliance", "Boolean", "True", "Active", "green", "28 Aug 2026"));
        settingsList.add(new SystemSettingsPageDto.SettingItemDto("set-6", 6, "Session Timeout (Minutes)", "security.session_timeout", "Security", "Number", "30", "Active", "green", "25 Aug 2026"));
        settingsList.add(new SystemSettingsPageDto.SettingItemDto("set-7", 7, "Password Expiry (Days)", "security.password_expiry", "Security", "Number", "90", "Active", "green", "20 Aug 2026"));
        settingsList.add(new SystemSettingsPageDto.SettingItemDto("set-8", 8, "Email Notifications", "notification.email_enabled", "Notifications", "Boolean", "True", "Active", "green", "18 Aug 2026"));
        settingsList.add(new SystemSettingsPageDto.SettingItemDto("set-9", 9, "SMS Notifications", "notification.sms_enabled", "Notifications", "Boolean", "True", "Active", "green", "15 Aug 2026"));
        settingsList.add(new SystemSettingsPageDto.SettingItemDto("set-10", 10, "Support Email", "support.email", "System", "Email", "support@atirath.com", "Active", "green", "10 Aug 2026"));

        SystemSettingDetailDto set3 = SystemSettingDetailDto.builder()
                .id("set-3")
                .settingName("Max Trade Limit")
                .settingCategory("Trading Limit")
                .description("Maximum trade value per transaction")
                .status("Active")
                .statusTheme("green")
                .tabs(List.of("Details", "Change History", "Affected Modules"))
                .activeTab("Details")
                .settingKey("trading.max_limit")
                .module("Trading")
                .dataType("Number")
                .currentValue("₹10,00,00,000")
                .defaultValue("₹5,00,00,000")
                .minimumValue("₹1,00,000")
                .maximumValue("₹1,00,00,00,000")
                .createdOn("01 Jan 2026, 10:00 AM")
                .createdBy("Admin User")
                .lastUpdated("31 Aug 2026, 04:15 PM")
                .updatedBy("Priya Mehta")
                .validationRules(List.of(
                        "Must be a positive number",
                        "Minimum value: ₹1,00,000",
                        "Maximum value: ₹1,00,00,00,000",
                        "Applies to all users"
                ))
                .affectedModules(List.of("Trade Management", "Risk Management"))
                .build();

        settingDetailsMap.put("set-3", set3);
    }

    private void seedAuditLogs() {
        auditLogsList.clear();
        auditLogsList.add(new AuditActivityLogsPageDto.AuditLogItemDto("log-1", 1, "15 Sep 2026, 10:24 AM", "RK", "Rakesh Kumar", "Login", "green", "Authentication", "User logged in successfully", "103.21.45.18", "Success", "green"));
        auditLogsList.add(new AuditActivityLogsPageDto.AuditLogItemDto("log-2", 2, "15 Sep 2026, 10:18 AM", "PM", "Priya Mehta", "Update", "blue", "Trading Rules", "Updated trade value limit ...", "103.21.45.22", "Success", "green"));
        auditLogsList.add(new AuditActivityLogsPageDto.AuditLogItemDto("log-3", 3, "15 Sep 2026, 09:45 AM", "SK", "Suresh Kumar", "Create", "green", "User Management", "Created new user account", "103.21.46.11", "Success", "green"));
        auditLogsList.add(new AuditActivityLogsPageDto.AuditLogItemDto("log-4", 4, "14 Sep 2026, 06:32 PM", "AR", "Ananya Reddy", "Approve", "green", "Approval Workflow", "Approved trade execution ...", "103.21.50.14", "Success", "green"));
        auditLogsList.add(new AuditActivityLogsPageDto.AuditLogItemDto("log-5", 5, "14 Sep 2026, 04:21 PM", "VR", "Vikram Rao", "Delete", "red", "System Settings", "Deleted system configura...", "103.21.51.27", "Failed", "red"));
        auditLogsList.add(new AuditActivityLogsPageDto.AuditLogItemDto("log-6", 6, "14 Sep 2026, 02:15 PM", "NS", "Neha Sharma", "Update", "blue", "Notification", "Modified email template", "103.21.49.33", "Success", "green"));
        auditLogsList.add(new AuditActivityLogsPageDto.AuditLogItemDto("log-7", 7, "14 Sep 2026, 11:03 AM", "RM", "Rajesh Mehta", "Export", "yellow", "Reports", "Exported trade report (CSV)", "103.21.47.19", "Success", "green"));
        auditLogsList.add(new AuditActivityLogsPageDto.AuditLogItemDto("log-8", 8, "13 Sep 2026, 05:48 PM", "LN", "Lakshmi Narayan", "Login", "green", "Authentication", "User logged in successfully", "103.21.48.76", "Success", "green"));
        auditLogsList.add(new AuditActivityLogsPageDto.AuditLogItemDto("log-9", 9, "13 Sep 2026, 03:12 PM", "KI", "Kavya Iyer", "Failed Login", "red", "Authentication", "Invalid password attempt", "103.21.44.12", "Failed", "red"));
        auditLogsList.add(new AuditActivityLogsPageDto.AuditLogItemDto("log-10", 10, "13 Sep 2026, 12:06 PM", "SV", "Sunil Verma", "Update", "blue", "Access Control", "Updated role permissions", "103.21.46.90", "Success", "green"));

        AuditLogDetailDto log1 = AuditLogDetailDto.builder()
                .id("log-1")
                .status("Success")
                .statusTheme("green")
                .timestamp("15 Sep 2026, 10:24 AM")
                .initials("RK")
                .userName("Rakesh Kumar")
                .role("Operations Manager")
                .email("rakesh.kumar@atirath.com")
                .action("Login")
                .module("Authentication")
                .description("User logged in successfully")
                .ipAddress("103.21.45.18")
                .device("Windows / Chrome")
                .location("Hyderabad, India")
                .sessionId("sess_9f3a2d1e8b7c")
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .loginMethod("Password")
                .mfaUsed("Yes")
                .duration("2 hours 18 mins")
                .relatedLogs(List.of(
                        new AuditLogDetailDto.RelatedLogDto("Page viewed - Dashboard", "10:25 AM", "teal"),
                        new AuditLogDetailDto.RelatedLogDto("Trade created", "10:42 AM", "yellow"),
                        new AuditLogDetailDto.RelatedLogDto("Settings updated", "11:18 AM", "blue"),
                        new AuditLogDetailDto.RelatedLogDto("Logout", "12:42 PM", "red")
                ))
                .build();

        auditLogDetailsMap.put("log-1", log1);
    }
}
