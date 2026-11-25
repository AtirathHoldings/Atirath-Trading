package com.atirath.trading.modules.admin.system;

import com.atirath.trading.modules.admin.system.dto.*;
import com.atirath.trading.modules.admin.system.service.SystemAdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SystemAdminServiceTest {

    private SystemAdminService service;

    @BeforeEach
    void setUp() {
        service = new SystemAdminService();
        service.init();
    }

    // ==========================================
    // SCREEN 1: USER MANAGEMENT
    // ==========================================
    @Test
    @DisplayName("Screen 1: User Management should match 5 KPIs, 5 tabs, charts, 10 table rows, and drawer details")
    void testUserManagementPage() {
        UserManagementPageDto dto = service.getUserManagementPage("Atirath Holdings", "All Users", null, null, null, null);

        assertNotNull(dto);
        assertEquals("User Management", dto.getPageTitle());
        assertEquals("Atirath Holdings", dto.getSelectedCompany());

        // 5 KPIs
        assertEquals(5, dto.getKpis().size());
        assertEquals("128", dto.getKpis().get(0).getValue());
        assertEquals("+12% vs last quarter", dto.getKpis().get(0).getChange());
        assertEquals("112", dto.getKpis().get(1).getValue());
        assertEquals("+15% vs last quarter", dto.getKpis().get(1).getChange());
        assertEquals("12", dto.getKpis().get(2).getValue());
        assertEquals("-25% vs last quarter", dto.getKpis().get(2).getChange());
        assertEquals("4", dto.getKpis().get(3).getValue());
        assertEquals("-33% vs last quarter", dto.getKpis().get(3).getChange());
        assertEquals("16", dto.getKpis().get(4).getValue());
        assertEquals("+7% vs last quarter", dto.getKpis().get(4).getChange());

        // 5 Tabs
        assertEquals(5, dto.getTabs().size());
        assertTrue(dto.getTabs().contains("All Users"));
        assertTrue(dto.getTabs().contains("Active Users"));
        assertTrue(dto.getTabs().contains("Inactive Users"));
        assertTrue(dto.getTabs().contains("Pending Activation"));
        assertTrue(dto.getTabs().contains("Administrators"));

        // Donut: Users by Role
        assertNotNull(dto.getUsersByRole());
        assertEquals("128 Total Users", dto.getUsersByRole().getTotalUsers());
        assertEquals(6, dto.getUsersByRole().getSlices().size());
        assertEquals("Operations Manager", dto.getUsersByRole().getSlices().get(0).getRole());
        assertEquals(18, dto.getUsersByRole().getSlices().get(0).getCount());

        // Status bar (Active, Inactive, Pending)
        assertEquals(3, dto.getUsersByStatus().size());
        assertEquals(112, dto.getUsersByStatus().get(0).getCount());
        assertEquals(12, dto.getUsersByStatus().get(1).getCount());
        assertEquals(4, dto.getUsersByStatus().get(2).getCount());

        // Table (10 users)
        assertEquals(10, dto.getUsers().size());
        assertEquals("usr-1", dto.getUsers().get(0).getId());
        assertEquals("Rakesh Kumar", dto.getUsers().get(0).getUserName());
        assertEquals("rakesh.kumar@atirath.com", dto.getUsers().get(0).getEmail());
        assertEquals("Operations", dto.getUsers().get(0).getDepartment());
        assertEquals("Operations Manager", dto.getUsers().get(0).getRole());
        assertEquals("Active", dto.getUsers().get(0).getStatus());

        // Drawer for usr-1
        UserDetailDto drawer = dto.getSelectedUserDetail();
        assertNotNull(drawer);
        assertEquals("usr-1", drawer.getId());
        assertEquals("Rakesh Kumar", drawer.getFullName());
        assertEquals("EMP-2026-001", drawer.getEmployeeId());
        assertEquals("Hyderabad, India", drawer.getLocation());
        assertEquals("Priya Mehta", drawer.getReportingManager());
        assertEquals("Yes", drawer.getMfaEnabled());
        assertEquals(0, drawer.getLoginAttempts());

        // Mutations
        CreateUserRequestDto createReq = new CreateUserRequestDto();
        createReq.setFullName("Test User");
        createReq.setEmail("test@atirath.com");
        createReq.setDepartment("Trading");
        createReq.setRole("Trade Analyst");
        createReq.setAccessLevel("Company Level");
        UserDetailDto created = service.createUser(createReq);
        assertNotNull(created);
        assertEquals("Test User", created.getFullName());

        UserDetailDto pwReset = service.resetPassword("usr-1");
        assertEquals(0, pwReset.getLoginAttempts());

        UserDetailDto deactivated = service.deactivateUser("usr-1");
        assertEquals("Inactive", deactivated.getStatus());

        UserDetailDto activated = service.activateUser("usr-1");
        assertEquals("Active", activated.getStatus());
    }

    // ==========================================
    // SCREEN 2: ROLE MANAGEMENT
    // ==========================================
    @Test
    @DisplayName("Screen 2: Role Management should match 4 KPIs, 5 tabs, charts, 10 table rows, and drawer details")
    void testRoleManagementPage() {
        RoleManagementPageDto dto = service.getRoleManagementPage("Atirath Holdings", "All Roles", null, null, null, null);

        assertNotNull(dto);
        assertEquals("Role Management", dto.getPageTitle());

        // 4 KPIs
        assertEquals(4, dto.getKpis().size());
        assertEquals("24", dto.getKpis().get(0).getValue());
        assertEquals("+20% vs last quarter", dto.getKpis().get(0).getChange());
        assertEquals("22", dto.getKpis().get(1).getValue());
        assertEquals("+10% vs last quarter", dto.getKpis().get(1).getChange());
        assertEquals("2", dto.getKpis().get(2).getValue());
        assertEquals("-50% vs last quarter", dto.getKpis().get(2).getChange());
        assertEquals("128", dto.getKpis().get(3).getValue());
        assertEquals("+18% vs last quarter", dto.getKpis().get(3).getChange());

        // 5 Tabs
        assertEquals(5, dto.getTabs().size());
        assertTrue(dto.getTabs().contains("All Roles"));
        assertTrue(dto.getTabs().contains("Active Roles"));
        assertTrue(dto.getTabs().contains("Inactive Roles"));
        assertTrue(dto.getTabs().contains("System Roles"));
        assertTrue(dto.getTabs().contains("Custom Roles"));

        // Donut: Roles by Type
        assertNotNull(dto.getRolesByType());
        assertEquals("24 Total Roles", dto.getRolesByType().getTotalRoles());
        assertEquals(3, dto.getRolesByType().getSlices().size());

        // Access Level Bar
        assertEquals(4, dto.getRolesByAccessLevel().size());
        assertEquals("Full Access", dto.getRolesByAccessLevel().get(0).getAccessLevel());

        // Table (10 roles)
        assertEquals(10, dto.getRoles().size());
        assertEquals("System Administrator", dto.getRoles().get(0).getRoleName());
        assertEquals("Company Admin", dto.getRoles().get(1).getRoleName());
        assertEquals("Operations Manager", dto.getRoles().get(2).getRoleName());

        // Drawer for rol-3
        RoleDetailDto drawer = dto.getSelectedRoleDetail();
        assertNotNull(drawer);
        assertEquals("rol-3", drawer.getId());
        assertEquals("Operations Manager", drawer.getRoleName());
        assertEquals("System Role", drawer.getRoleType());
        assertEquals("High", drawer.getAccessLevel());
        assertEquals(48, drawer.getTotalPermissions());
        assertEquals(8, drawer.getUsersAssigned());
        assertEquals(13, drawer.getModulesCount());
        assertEquals(2, drawer.getConditionsCount());

        // Mutations
        CreateRoleRequestDto createReq = new CreateRoleRequestDto();
        createReq.setRoleName("Compliance Reviewer");
        createReq.setRoleType("Custom");
        createReq.setDescription("Review compliance alerts");
        createReq.setAccessLevel("Medium");
        createReq.setPermissions(List.of("View Alerts", "Edit Alerts"));
        RoleDetailDto created = service.createRole(createReq);
        assertNotNull(created);
        assertEquals("Compliance Reviewer", created.getRoleName());

        RoleDetailDto cloned = service.cloneRole("rol-3");
        assertNotNull(cloned);
        assertTrue(cloned.getRoleName().contains("Copy"));

        RoleDetailDto deactivated = service.deactivateRole("rol-3");
        assertEquals("Inactive", deactivated.getStatus());
    }

    // ==========================================
    // SCREEN 3: PERMISSION MANAGEMENT
    // ==========================================
    @Test
    @DisplayName("Screen 3: Permission Management should match 4 KPIs, 5 tabs, charts, 10 table rows, and drawer details")
    void testPermissionManagementPage() {
        PermissionManagementPageDto dto = service.getPermissionManagementPage("Atirath Holdings", "All Permissions", null, null, null, null);

        assertNotNull(dto);
        assertEquals("Permission Management", dto.getPageTitle());

        // 4 KPIs
        assertEquals(4, dto.getKpis().size());
        assertEquals("48", dto.getKpis().get(0).getValue());
        assertEquals("+14% vs last quarter", dto.getKpis().get(0).getChange());
        assertEquals("36", dto.getKpis().get(1).getValue());
        assertEquals("+20% vs last quarter", dto.getKpis().get(1).getChange());
        assertEquals("8", dto.getKpis().get(2).getValue());
        assertEquals("-11% vs last quarter", dto.getKpis().get(2).getChange());
        assertEquals("6", dto.getKpis().get(3).getValue());
        assertEquals("+50% vs last quarter", dto.getKpis().get(3).getChange());

        // 5 Tabs
        assertEquals(5, dto.getTabs().size());
        assertTrue(dto.getTabs().contains("All Permissions"));
        assertTrue(dto.getTabs().contains("Module Permissions"));
        assertTrue(dto.getTabs().contains("Role Permissions"));
        assertTrue(dto.getTabs().contains("User Permissions"));
        assertTrue(dto.getTabs().contains("Custom Permissions"));

        // Donut: Permissions by Module
        assertNotNull(dto.getPermissionsByModule());
        assertEquals("48 Total Permissions", dto.getPermissionsByModule().getTotalPermissions());
        assertEquals(6, dto.getPermissionsByModule().getSlices().size());

        // Type bar
        assertEquals(5, dto.getPermissionsByType().size());
        assertEquals("Read", dto.getPermissionsByType().get(0).getType());

        // Table (10 permissions)
        assertEquals(10, dto.getPermissions().size());
        assertEquals("View Trades", dto.getPermissions().get(0).getPermissionName());
        assertEquals("Create Trade", dto.getPermissions().get(1).getPermissionName());
        assertEquals("Edit Trade", dto.getPermissions().get(2).getPermissionName());

        // Drawer for per-1
        PermissionDetailDto drawer = dto.getSelectedPermissionDetail();
        assertNotNull(drawer);
        assertEquals("per-1", drawer.getId());
        assertEquals("View Trades", drawer.getPermissionName());
        assertEquals("PER-001", drawer.getPermissionId());
        assertEquals("Trade Management", drawer.getModule());
        assertEquals("Read", drawer.getPermissionType());
        assertEquals(5, drawer.getRolesCount());
        assertEquals(28, drawer.getUsersCount());
        assertEquals(3, drawer.getConditionsCount());

        // Mutations
        AssignPermissionRequestDto assignReq = new AssignPermissionRequestDto();
        assignReq.setPermissionName("Export Reports");
        assignReq.setModule("Reports");
        assignReq.setPermissionType("Read");
        assignReq.setDescription("Export reports to Excel and PDF");
        assignReq.setRoleNames(List.of("rol-1", "rol-2"));
        PermissionDetailDto created = service.assignPermission(assignReq);
        assertNotNull(created);
        assertEquals("Export Reports", created.getPermissionName());

        PermissionDetailDto deactivated = service.deactivatePermission("per-1");
        assertEquals("Inactive", deactivated.getStatus());
    }

    // ==========================================
    // SCREEN 4: ACCESS CONTROL
    // ==========================================
    @Test
    @DisplayName("Screen 4: Access Control should match 5 KPIs, 6 tabs, charts, 10 table rows, and drawer details")
    void testAccessControlPage() {
        AccessControlPageDto dto = service.getAccessControlPage("Atirath Holdings", "All Access", null, null, null, null);
        assertNotNull(dto);
        assertEquals("Access Control", dto.getPageTitle());
        assertEquals("Atirath Holdings", dto.getSelectedCompany());

        // 5 KPIs
        assertEquals(5, dto.getKpis().size());
        assertEquals("128", dto.getKpis().get(0).getValue());
        assertEquals("+12% vs last quarter", dto.getKpis().get(0).getChange());
        assertEquals("1,246", dto.getKpis().get(1).getValue());
        assertEquals("+18% vs last quarter", dto.getKpis().get(1).getChange());
        assertEquals("32", dto.getKpis().get(2).getValue());
        assertEquals("-20% vs last quarter", dto.getKpis().get(2).getChange());
        assertEquals("6", dto.getKpis().get(3).getValue());
        assertEquals("-33% vs last quarter", dto.getKpis().get(3).getChange());
        assertEquals("14", dto.getKpis().get(4).getValue());
        assertEquals("-13% vs last quarter", dto.getKpis().get(4).getChange());

        // 6 Tabs
        assertEquals(6, dto.getTabs().size());
        assertTrue(dto.getTabs().contains("All Access"));
        assertTrue(dto.getTabs().contains("User Access"));
        assertTrue(dto.getTabs().contains("Role Access"));
        assertTrue(dto.getTabs().contains("Module Access"));
        assertTrue(dto.getTabs().contains("Pending Requests"));
        assertTrue(dto.getTabs().contains("Access Policies"));

        // Donut: Access by Module
        assertNotNull(dto.getAccessByModule());
        assertEquals("1,246 Total Accesses", dto.getAccessByModule().getTotalAccesses());
        assertEquals(6, dto.getAccessByModule().getSlices().size());

        // Bar: Access by Access Level
        assertEquals(4, dto.getAccessByAccessLevel().size());
        assertEquals("Full Access", dto.getAccessByAccessLevel().get(0).getAccessLevel());
        assertEquals(527, dto.getAccessByAccessLevel().get(0).getCount());

        // Table (10 rows)
        assertEquals(10, dto.getAccesses().size());
        assertEquals("acc-1", dto.getAccesses().get(0).getId());
        assertEquals("Rakesh Kumar", dto.getAccesses().get(0).getUserName());
        assertEquals("Operations Manager", dto.getAccesses().get(0).getRoles());
        assertEquals("Trading, Settlement +3", dto.getAccesses().get(0).getModuleAccess());
        assertEquals("Full Access", dto.getAccesses().get(0).getAccessLevel());
        assertEquals("Active", dto.getAccesses().get(0).getStatus());

        // Drawer for acc-1
        AccessDetailDto drawer = dto.getSelectedAccessDetail();
        assertNotNull(drawer);
        assertEquals("acc-1", drawer.getId());
        assertEquals("Rakesh Kumar", drawer.getUserName());
        assertEquals("Operations Manager", drawer.getRole());
        assertEquals("EMP-2026-001", drawer.getUserId());
        assertEquals("Operations", drawer.getDepartment());
        assertEquals("Full Access", drawer.getAccessLevel());
        assertEquals("Active", drawer.getStatus());
        assertEquals(6, drawer.getModuleAccessList().size());

        GrantAccessRequestDto req = new GrantAccessRequestDto();
        req.setUserName("Vikram Rao");
        req.setRole("Trade Analyst");
        req.setModules(List.of("Trading"));
        req.setAccessLevel("Read-Only");
        AccessDetailDto granted = service.grantAccess(req);
        assertNotNull(granted);
        assertEquals("Vikram Rao", granted.getUserName());

        AccessDetailDto revoked = service.revokeAccess("acc-1");
        assertEquals("Revoked", revoked.getStatus());
    }

    // ==========================================
    // SCREEN 5: APPROVAL WORKFLOW
    // ==========================================
    @Test
    @DisplayName("Screen 5: Approval Workflow should match 5 KPIs, 6 tabs, charts, 10 table rows, and drawer details")
    void testApprovalWorkflowPage() {
        ApprovalWorkflowPageDto dto = service.getApprovalWorkflowPage("Atirath Holdings", "All Workflows", null, null, null, null);
        assertNotNull(dto);
        assertEquals("Approval Workflow Configuration", dto.getPageTitle());
        assertEquals("Atirath Holdings", dto.getSelectedCompany());

        // 5 KPIs
        assertEquals(5, dto.getKpis().size());
        assertEquals("18", dto.getKpis().get(0).getValue());
        assertEquals("+20% vs last quarter", dto.getKpis().get(0).getChange());
        assertEquals("15", dto.getKpis().get(1).getValue());
        assertEquals("+15% vs last quarter", dto.getKpis().get(1).getChange());
        assertEquals("3", dto.getKpis().get(2).getValue());
        assertEquals("-25% vs last quarter", dto.getKpis().get(2).getChange());
        assertEquals("52", dto.getKpis().get(3).getValue());
        assertEquals("+18% vs last quarter", dto.getKpis().get(3).getChange());
        assertEquals("4", dto.getKpis().get(4).getValue());
        assertEquals("-33% vs last quarter", dto.getKpis().get(4).getChange());

        // 6 Tabs
        assertEquals(6, dto.getTabs().size());
        assertTrue(dto.getTabs().contains("All Workflows"));
        assertTrue(dto.getTabs().contains("Trade Workflows"));
        assertTrue(dto.getTabs().contains("Settlement Workflows"));
        assertTrue(dto.getTabs().contains("User Management"));
        assertTrue(dto.getTabs().contains("System Settings"));
        assertTrue(dto.getTabs().contains("Other Modules"));

        // Donut: Workflows by Module
        assertNotNull(dto.getWorkflowsByModule());
        assertEquals("18 Total Workflows", dto.getWorkflowsByModule().getTotalWorkflows());
        assertEquals(7, dto.getWorkflowsByModule().getSlices().size());

        // Bars
        assertEquals(2, dto.getWorkflowsByStatus().size());
        assertEquals(15, dto.getWorkflowsByStatus().get(0).getCount());
        assertEquals(4, dto.getApprovalLevelsDistribution().size());

        // Table (10 rows)
        assertEquals(10, dto.getWorkflows().size());
        assertEquals("wf-1", dto.getWorkflows().get(0).getId());
        assertEquals("Trade Execution Approval", dto.getWorkflows().get(0).getWorkflowName());
        assertEquals("Trade Management", dto.getWorkflows().get(0).getModule());
        assertEquals("3 Levels", dto.getWorkflows().get(0).getApprovalLevels());
        assertEquals("Active", dto.getWorkflows().get(0).getStatus());

        // Drawer for wf-1
        WorkflowDetailDto drawer = dto.getSelectedWorkflowDetail();
        assertNotNull(drawer);
        assertEquals("wf-1", drawer.getId());
        assertEquals("Trade Execution Approval", drawer.getWorkflowName());
        assertEquals("Trade Management", drawer.getModule());
        assertEquals("Sequential", drawer.getApprovalType());
        assertEquals(3, drawer.getTotalApprovalLevels());
        assertEquals("Yes", drawer.getAllowDelegation());
        assertEquals("Yes", drawer.getAutoEscalation());
        assertEquals("24 hours", drawer.getEscalationTime());
        assertEquals(4, drawer.getConditions().size());

        CreateWorkflowRequestDto req = new CreateWorkflowRequestDto();
        req.setWorkflowName("Transfer Approval 2");
        req.setModule("Share Transfer");
        req.setApprovalLevels(2);
        req.setDescription("Two level approval");
        WorkflowDetailDto created = service.createWorkflow(req);
        assertNotNull(created);
        assertEquals("Transfer Approval 2", created.getWorkflowName());

        WorkflowDetailDto cloned = service.cloneWorkflow("wf-1");
        assertNotNull(cloned);

        WorkflowDetailDto deactivated = service.deactivateWorkflow("wf-1");
        assertEquals("Inactive", deactivated.getStatus());
    }

    // ==========================================
    // SCREEN 6: TRADING RULES
    // ==========================================
    @Test
    @DisplayName("Screen 6: Trading Rules should match 5 KPIs, 6 tabs, charts, 10 table rows, and drawer details")
    void testTradingRulesPage() {
        TradingRuleConfigPageDto dto = service.getTradingRulesPage("Atirath Holdings", "All Rules", null, null, null, null);
        assertNotNull(dto);
        assertEquals("Trading Rule Configuration", dto.getPageTitle());
        assertEquals("Atirath Holdings", dto.getSelectedCompany());

        // 5 KPIs
        assertEquals(5, dto.getKpis().size());
        assertEquals("48", dto.getKpis().get(0).getValue());
        assertEquals("+14% vs last quarter", dto.getKpis().get(0).getChange());
        assertEquals("42", dto.getKpis().get(1).getValue());
        assertEquals("+20% vs last quarter", dto.getKpis().get(1).getChange());
        assertEquals("6", dto.getKpis().get(2).getValue());
        assertEquals("-25% vs last quarter", dto.getKpis().get(2).getChange());
        assertEquals("12", dto.getKpis().get(3).getValue());
        assertEquals("+33% vs last quarter", dto.getKpis().get(3).getChange());
        assertEquals("10", dto.getKpis().get(4).getValue());
        assertEquals("+11% vs last quarter", dto.getKpis().get(4).getChange());

        // 6 Tabs
        assertEquals(6, dto.getTabs().size());
        assertTrue(dto.getTabs().contains("All Rules"));
        assertTrue(dto.getTabs().contains("Trading Limits"));
        assertTrue(dto.getTabs().contains("Risk Rules"));
        assertTrue(dto.getTabs().contains("Compliance Rules"));
        assertTrue(dto.getTabs().contains("Market Rules"));
        assertTrue(dto.getTabs().contains("Custom Rules"));

        // Donut: Rules by Category
        assertNotNull(dto.getRulesByCategory());
        assertEquals("48 Total Rules", dto.getRulesByCategory().getTotalRules());
        assertEquals(5, dto.getRulesByCategory().getSlices().size());

        // Bars
        assertEquals(2, dto.getRulesByStatus().size());
        assertEquals(42, dto.getRulesByStatus().get(0).getCount());
        assertEquals(4, dto.getRulesByPriority().size());

        // Table (10 rows)
        assertEquals(10, dto.getRules().size());
        assertEquals("trd-1", dto.getRules().get(0).getId());
        assertEquals("Trade Value Limit", dto.getRules().get(0).getRuleName());
        assertEquals("Trading Limit", dto.getRules().get(0).getCategory());
        assertEquals("Threshold", dto.getRules().get(0).getType());
        assertEquals("Active", dto.getRules().get(0).getStatus());
        assertEquals("High", dto.getRules().get(0).getPriority());

        // Drawer for trd-1
        TradingRuleDetailDto drawer = dto.getSelectedRuleDetail();
        assertNotNull(drawer);
        assertEquals("trd-1", drawer.getId());
        assertEquals("Trade Value Limit", drawer.getRuleName());
        assertEquals("TRD-001", drawer.getRuleId());
        assertEquals("Trading Limit", drawer.getCategory());
        assertEquals("Threshold", drawer.getType());
        assertEquals("Per Transaction", drawer.getLimitType());
        assertEquals("₹10,00,00,000", drawer.getMaximumValue());
        assertEquals("INR", drawer.getCurrency());
        assertEquals("All Users", drawer.getAppliesTo());
        assertEquals("No", drawer.getOverrideAllowed());
        assertEquals("80%", drawer.getAlertThreshold());
        assertEquals("Yes", drawer.getAutoBlock());
        assertEquals(4, drawer.getConditions().size());

        CreateTradingRuleRequestDto req = new CreateTradingRuleRequestDto();
        req.setRuleName("Price Range Limit");
        req.setCategory("Price Control");
        req.setType("Range");
        req.setPriority("High");
        req.setMaximumValue("5,000,000");
        TradingRuleDetailDto created = service.createTradingRule(req);
        assertNotNull(created);

        TradingRuleDetailDto cloned = service.cloneTradingRule("trd-1");
        assertNotNull(cloned);

        TradingRuleDetailDto deactivated = service.deactivateTradingRule("trd-1");
        assertEquals("Inactive", deactivated.getStatus());
    }

    // ==========================================
    // SCREEN 7: NOTIFICATION CONFIGURATION
    // ==========================================
    @Test
    @DisplayName("Screen 7: Notification Configuration should match 4 KPIs, 6 tabs, charts, 10 table rows, and drawer details")
    void testNotificationConfigPage() {
        NotificationConfigPageDto dto = service.getNotificationConfigPage("Atirath Holdings", "All Notifications", null, null, null, null);
        assertNotNull(dto);
        assertEquals("Notification Configuration", dto.getPageTitle());
        assertEquals("Atirath Holdings", dto.getSelectedCompany());

        // 4 KPIs
        assertEquals(4, dto.getKpis().size());
        assertEquals("28", dto.getKpis().get(0).getValue());
        assertEquals("+17% vs last quarter", dto.getKpis().get(0).getChange());
        assertEquals("24", dto.getKpis().get(1).getValue());
        assertEquals("+20% vs last quarter", dto.getKpis().get(1).getChange());
        assertEquals("4", dto.getKpis().get(2).getValue());
        assertEquals("-33% vs last quarter", dto.getKpis().get(2).getChange());
        assertEquals("3", dto.getKpis().get(3).getValue());
        assertEquals("Email, In-App, SMS", dto.getKpis().get(3).getChange());

        // 6 Tabs
        assertEquals(6, dto.getTabs().size());
        assertTrue(dto.getTabs().contains("All Notifications"));
        assertTrue(dto.getTabs().contains("Trade Notifications"));
        assertTrue(dto.getTabs().contains("Settlement Notifications"));
        assertTrue(dto.getTabs().contains("User Notifications"));
        assertTrue(dto.getTabs().contains("System Notifications"));
        assertTrue(dto.getTabs().contains("Compliance Notifications"));

        // Donut: Notifications by Category
        assertNotNull(dto.getNotificationsByCategory());
        assertEquals("28 Total", dto.getNotificationsByCategory().getTotalNotifications());
        assertEquals(6, dto.getNotificationsByCategory().getSlices().size());

        // Bars
        assertEquals(3, dto.getNotificationsByChannel().size());
        assertEquals("In-App", dto.getNotificationsByChannel().get(0).getChannel());
        assertEquals(22, dto.getNotificationsByChannel().get(0).getCount());

        // Table (10 rows)
        assertEquals(10, dto.getNotifications().size());
        assertEquals("notif-1", dto.getNotifications().get(0).getId());
        assertEquals("Trade Created", dto.getNotifications().get(0).getNotificationName());
        assertEquals("New trade created", dto.getNotifications().get(0).getEventTrigger());
        assertEquals("Trade", dto.getNotifications().get(0).getCategory());
        assertEquals("In-App", dto.getNotifications().get(0).getChannels());
        assertEquals("Active", dto.getNotifications().get(0).getStatus());

        // Drawer for notif-1
        NotificationDetailDto drawer = dto.getSelectedNotificationDetail();
        assertNotNull(drawer);
        assertEquals("notif-1", drawer.getId());
        assertEquals("Trade Created", drawer.getNotificationName());
        assertEquals("Trade Notification", drawer.getNotificationTitle());
        assertEquals("New trade created", drawer.getEventTrigger());
        assertEquals("Trade", drawer.getCategory());
        assertEquals("Active", drawer.getStatus());
        assertEquals("New Trade Created", drawer.getSubject());
        assertEquals("Assigned Users, Approvers", drawer.getRecipients());
        assertEquals("Yes", drawer.getSendEmail());
        assertEquals("Yes", drawer.getSendInApp());
        assertEquals("No", drawer.getSendSms());
        assertEquals(3, drawer.getMaximumRetries());

        CreateNotificationRequestDto req = new CreateNotificationRequestDto();
        req.setNotificationName("KYC Expiry Alert");
        req.setEventTrigger("KYC about to expire");
        req.setCategory("Compliance");
        req.setChannels(List.of("In-App", "Email"));
        NotificationDetailDto created = service.createNotification(req);
        assertNotNull(created);

        NotificationDetailDto cloned = service.cloneNotification("notif-1");
        assertNotNull(cloned);

        NotificationDetailDto deactivated = service.deactivateNotification("notif-1");
        assertEquals("Inactive", deactivated.getStatus());
    }

    // ==========================================
    // SCREEN 8: SYSTEM SETTINGS
    // ==========================================
    @Test
    @DisplayName("Screen 8: System Settings should match 4 KPIs, 9 tabs, charts, 10 table rows, and drawer details")
    void testSystemSettingsPage() {
        SystemSettingsPageDto dto = service.getSystemSettingsPage("Atirath Holdings", "All Settings", null, null, null, null);
        assertNotNull(dto);
        assertEquals("System Settings", dto.getPageTitle());
        assertEquals("Atirath Holdings", dto.getSelectedCompany());

        // 4 KPIs
        assertEquals(4, dto.getKpis().size());
        assertEquals("36", dto.getKpis().get(0).getValue());
        assertEquals("+12% vs last quarter", dto.getKpis().get(0).getChange());
        assertEquals("30", dto.getKpis().get(1).getValue());
        assertEquals("+20% vs last quarter", dto.getKpis().get(1).getChange());
        assertEquals("6", dto.getKpis().get(2).getValue());
        assertEquals("-25% vs last quarter", dto.getKpis().get(2).getChange());
        assertEquals("8", dto.getKpis().get(3).getValue());
        assertEquals("+14% vs last quarter", dto.getKpis().get(3).getChange());

        // 9 Tabs
        assertEquals(9, dto.getTabs().size());
        assertTrue(dto.getTabs().contains("All Settings"));
        assertTrue(dto.getTabs().contains("General"));
        assertTrue(dto.getTabs().contains("Security"));
        assertTrue(dto.getTabs().contains("Trading"));
        assertTrue(dto.getTabs().contains("Settlement"));
        assertTrue(dto.getTabs().contains("Compliance"));
        assertTrue(dto.getTabs().contains("Integrations"));
        assertTrue(dto.getTabs().contains("Notifications"));
        assertTrue(dto.getTabs().contains("System"));

        // Donut: Settings by Module
        assertNotNull(dto.getSettingsByModule());
        assertEquals("36 Total Settings", dto.getSettingsByModule().getTotalSettings());
        assertEquals(7, dto.getSettingsByModule().getSlices().size());

        // Status bar & Recent Changes
        assertEquals(2, dto.getSettingsByStatus().size());
        assertEquals(30, dto.getSettingsByStatus().get(0).getCount());
        assertEquals(5, dto.getRecentConfigurationChanges().size());

        // Table (10 rows)
        assertEquals(10, dto.getSettings().size());
        assertEquals("set-1", dto.getSettings().get(0).getId());
        assertEquals("Platform Name", dto.getSettings().get(0).getSettingName());
        assertEquals("platform.name", dto.getSettings().get(0).getSettingKey());
        assertEquals("General", dto.getSettings().get(0).getModule());
        assertEquals("Text", dto.getSettings().get(0).getDataType());
        assertEquals("Atirath Trading Platform", dto.getSettings().get(0).getCurrentValue());
        assertEquals("Active", dto.getSettings().get(0).getStatus());

        // Drawer for set-3
        SystemSettingDetailDto drawer = dto.getSelectedSettingDetail();
        assertNotNull(drawer);
        assertEquals("set-3", drawer.getId());
        assertEquals("Max Trade Limit", drawer.getSettingName());
        assertEquals("trading.max_limit", drawer.getSettingKey());
        assertEquals("Trading", drawer.getModule());
        assertEquals("Number", drawer.getDataType());
        assertEquals("₹10,00,00,000", drawer.getCurrentValue());
        assertEquals("₹5,00,00,000", drawer.getDefaultValue());
        assertEquals("₹1,00,000", drawer.getMinimumValue());
        assertEquals("₹1,00,00,00,000", drawer.getMaximumValue());
        assertEquals("Active", drawer.getStatus());
        assertEquals(4, drawer.getValidationRules().size());
        assertEquals(2, drawer.getAffectedModules().size());

        AddSystemSettingRequestDto addReq = new AddSystemSettingRequestDto();
        addReq.setSettingName("Session Timeout");
        addReq.setSettingKey("auth.session.timeout");
        addReq.setModule("Security");
        addReq.setDataType("Integer");
        addReq.setCurrentValue("30");
        SystemSettingDetailDto added = service.addSystemSetting(addReq);
        assertNotNull(added);

        UpdateSystemSettingRequestDto updateReq = new UpdateSystemSettingRequestDto();
        updateReq.setValue("60");
        SystemSettingDetailDto updated = service.updateSystemSetting("set-1", updateReq);
        assertNotNull(updated);

        SystemSettingDetailDto deactivated = service.deactivateSystemSetting("set-1");
        assertEquals("Disabled", deactivated.getStatus());
    }

    // ==========================================
    // SCREEN 9: AUDIT LOGS
    // ==========================================
    @Test
    @DisplayName("Screen 9: Audit Logs should match 5 KPIs, 5 tabs, charts, 10 table rows, and drawer details")
    void testAuditLogsPage() {
        AuditActivityLogsPageDto dto = service.getAuditLogsPage("Atirath Holdings", "All Logs", null, null, null, null, null, null);
        assertNotNull(dto);
        assertEquals("Audit & Activity Logs", dto.getPageTitle());
        assertEquals("Atirath Holdings", dto.getSelectedCompany());

        // 5 KPIs
        assertEquals(5, dto.getKpis().size());
        assertEquals("12,842", dto.getKpis().get(0).getValue());
        assertEquals("+18% vs last quarter", dto.getKpis().get(0).getChange());
        assertEquals("6,420", dto.getKpis().get(1).getValue());
        assertEquals("+12% vs last quarter", dto.getKpis().get(1).getChange());
        assertEquals("2,184", dto.getKpis().get(2).getValue());
        assertEquals("+25% vs last quarter", dto.getKpis().get(2).getChange());
        assertEquals("1,206", dto.getKpis().get(3).getValue());
        assertEquals("-8% vs last quarter", dto.getKpis().get(3).getChange());
        assertEquals("432", dto.getKpis().get(4).getValue());
        assertEquals("-15% vs last quarter", dto.getKpis().get(4).getChange());

        // 5 Tabs
        assertEquals(5, dto.getTabs().size());
        assertTrue(dto.getTabs().contains("All Logs"));
        assertTrue(dto.getTabs().contains("User Activities"));
        assertTrue(dto.getTabs().contains("System Changes"));
        assertTrue(dto.getTabs().contains("Security Events"));
        assertTrue(dto.getTabs().contains("Failed Actions"));

        // Donut: Logs by Module
        assertNotNull(dto.getLogsByModule());
        assertEquals("12,842 Total Logs", dto.getLogsByModule().getTotalLogs());
        assertEquals(7, dto.getLogsByModule().getSlices().size());

        // Action Bars
        assertEquals(6, dto.getLogsByActionType().size());
        assertEquals("Login", dto.getLogsByActionType().get(0).getAction());
        assertEquals(3648, dto.getLogsByActionType().get(0).getCount());

        // Table (10 rows)
        assertEquals(10, dto.getLogs().size());
        assertEquals("log-1", dto.getLogs().get(0).getId());
        assertEquals("15 Sep 2026, 10:24 AM", dto.getLogs().get(0).getTimestamp());
        assertEquals("Rakesh Kumar", dto.getLogs().get(0).getUserName());
        assertEquals("Login", dto.getLogs().get(0).getAction());
        assertEquals("Authentication", dto.getLogs().get(0).getModule());
        assertEquals("103.21.45.18", dto.getLogs().get(0).getIpAddress());
        assertEquals("Success", dto.getLogs().get(0).getStatus());

        // Drawer for log-1
        AuditLogDetailDto drawer = service.getAuditLogDetail("log-1");
        assertNotNull(drawer);
        assertEquals("log-1", drawer.getId());
        assertEquals("Rakesh Kumar", drawer.getUserName());
        assertEquals("Operations Manager", drawer.getRole());
        assertEquals("Login", drawer.getAction());
        assertEquals("Authentication", drawer.getModule());
        assertEquals("15 Sep 2026, 10:24 AM", drawer.getTimestamp());
        assertEquals("103.21.45.18", drawer.getIpAddress());
        assertEquals("Windows / Chrome", drawer.getDevice());
        assertEquals("Hyderabad, India", drawer.getLocation());
        assertEquals("sess_9f3a2d1e8b7c", drawer.getSessionId());
        assertEquals("Password", drawer.getLoginMethod());
        assertEquals("Yes", drawer.getMfaUsed());
        assertEquals("2 hours 18 mins", drawer.getDuration());
        assertEquals(4, drawer.getRelatedLogs().size());
    }
}
