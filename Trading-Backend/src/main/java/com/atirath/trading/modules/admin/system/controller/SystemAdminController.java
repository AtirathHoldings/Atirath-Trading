package com.atirath.trading.modules.admin.system.controller;

import com.atirath.trading.common.ApiResponse;
import com.atirath.trading.modules.admin.system.dto.*;
import com.atirath.trading.modules.admin.system.service.SystemAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/system")
@RequiredArgsConstructor
@Tag(name = "System Administration", description = "Endpoints for System Administration: User Management, Role Management, Permission Management, Access Control, and Approval Workflows")
public class SystemAdminController {

    private final SystemAdminService systemAdminService;

    // ==========================================
    // 1. USER MANAGEMENT
    // ==========================================
    @GetMapping("/users")
    @Operation(summary = "Get User Management page data with KPIs, status tabs, role donut, status bar, and users table")
    public ResponseEntity<ApiResponse<UserManagementPageDto>> getUsers(
            @RequestParam(required = false, defaultValue = "Atirath Holdings") String company,
            @RequestParam(required = false) String tab,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status) {
        log.info("GET /api/v1/admin/system/users requested (tab={}, query={}, dept={}, role={}, status={})", tab, query, department, role, status);
        UserManagementPageDto page = systemAdminService.getUserManagementPage(company, tab, query, department, role, status);
        return ResponseEntity.ok(ApiResponse.success(page, "Users retrieved successfully"));
    }

    @GetMapping("/users/{id}")
    @Operation(summary = "Get detailed User side drawer by ID")
    public ResponseEntity<ApiResponse<UserDetailDto>> getUserDetail(@PathVariable String id) {
        log.info("GET /api/v1/admin/system/users/{} requested", id);
        UserDetailDto detail = systemAdminService.getUserDetail(id);
        return ResponseEntity.ok(ApiResponse.success(detail, "User detail retrieved successfully"));
    }

    @PostMapping("/users")
    @Operation(summary = "Create / Add a new User")
    public ResponseEntity<ApiResponse<UserDetailDto>> createUser(@RequestBody CreateUserRequestDto request) {
        log.info("POST /api/v1/admin/system/users requested for fullName={}, email={}", request.getFullName(), request.getEmail());
        UserDetailDto created = systemAdminService.createUser(request);
        return ResponseEntity.ok(ApiResponse.success(created, "User created successfully"));
    }

    @PutMapping("/users/{id}/reset-password")
    @Operation(summary = "Reset password for user")
    public ResponseEntity<ApiResponse<UserDetailDto>> resetPassword(@PathVariable String id) {
        log.info("PUT /api/v1/admin/system/users/{}/reset-password requested", id);
        UserDetailDto updated = systemAdminService.resetPassword(id);
        return ResponseEntity.ok(ApiResponse.success(updated, "Password reset successfully"));
    }

    @PutMapping("/users/{id}/deactivate")
    @Operation(summary = "Deactivate user account")
    public ResponseEntity<ApiResponse<UserDetailDto>> deactivateUser(@PathVariable String id) {
        log.info("PUT /api/v1/admin/system/users/{}/deactivate requested", id);
        UserDetailDto updated = systemAdminService.deactivateUser(id);
        return ResponseEntity.ok(ApiResponse.success(updated, "User deactivated successfully"));
    }

    @PutMapping("/users/{id}/activate")
    @Operation(summary = "Activate user account")
    public ResponseEntity<ApiResponse<UserDetailDto>> activateUser(@PathVariable String id) {
        log.info("PUT /api/v1/admin/system/users/{}/activate requested", id);
        UserDetailDto updated = systemAdminService.activateUser(id);
        return ResponseEntity.ok(ApiResponse.success(updated, "User activated successfully"));
    }

    // ==========================================
    // 2. ROLE MANAGEMENT
    // ==========================================
    @GetMapping("/roles")
    @Operation(summary = "Get Role Management page data with KPIs, status tabs, role type donut, access bar, and roles table")
    public ResponseEntity<ApiResponse<RoleManagementPageDto>> getRoles(
            @RequestParam(required = false, defaultValue = "Atirath Holdings") String company,
            @RequestParam(required = false) String tab,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String roleType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String accessLevel) {
        log.info("GET /api/v1/admin/system/roles requested (tab={}, query={}, roleType={}, status={}, accessLevel={})", tab, query, roleType, status, accessLevel);
        RoleManagementPageDto page = systemAdminService.getRoleManagementPage(company, tab, query, roleType, status, accessLevel);
        return ResponseEntity.ok(ApiResponse.success(page, "Roles retrieved successfully"));
    }

    @GetMapping("/roles/{id}")
    @Operation(summary = "Get detailed Role side drawer by ID")
    public ResponseEntity<ApiResponse<RoleDetailDto>> getRoleDetail(@PathVariable String id) {
        log.info("GET /api/v1/admin/system/roles/{} requested", id);
        RoleDetailDto detail = systemAdminService.getRoleDetail(id);
        return ResponseEntity.ok(ApiResponse.success(detail, "Role detail retrieved successfully"));
    }

    @PostMapping("/roles")
    @Operation(summary = "Create a new Role")
    public ResponseEntity<ApiResponse<RoleDetailDto>> createRole(@RequestBody CreateRoleRequestDto request) {
        log.info("POST /api/v1/admin/system/roles requested for roleName={}, roleType={}", request.getRoleName(), request.getRoleType());
        RoleDetailDto created = systemAdminService.createRole(request);
        return ResponseEntity.ok(ApiResponse.success(created, "Role created successfully"));
    }

    @PostMapping("/roles/{id}/clone")
    @Operation(summary = "Clone an existing Role")
    public ResponseEntity<ApiResponse<RoleDetailDto>> cloneRole(@PathVariable String id) {
        log.info("POST /api/v1/admin/system/roles/{}/clone requested", id);
        RoleDetailDto cloned = systemAdminService.cloneRole(id);
        return ResponseEntity.ok(ApiResponse.success(cloned, "Role cloned successfully"));
    }

    @PutMapping("/roles/{id}/deactivate")
    @Operation(summary = "Deactivate role")
    public ResponseEntity<ApiResponse<RoleDetailDto>> deactivateRole(@PathVariable String id) {
        log.info("PUT /api/v1/admin/system/roles/{}/deactivate requested", id);
        RoleDetailDto updated = systemAdminService.deactivateRole(id);
        return ResponseEntity.ok(ApiResponse.success(updated, "Role deactivated successfully"));
    }

    // ==========================================
    // 3. PERMISSION MANAGEMENT
    // ==========================================
    @GetMapping("/permissions")
    @Operation(summary = "Get Permission Management page with KPIs, module donut, permission types bar, trend, and permissions table")
    public ResponseEntity<ApiResponse<PermissionManagementPageDto>> getPermissions(
            @RequestParam(required = false, defaultValue = "Atirath Holdings") String company,
            @RequestParam(required = false) String tab,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String permissionType,
            @RequestParam(required = false) String status) {
        log.info("GET /api/v1/admin/system/permissions requested (tab={}, query={}, module={}, type={}, status={})", tab, query, module, permissionType, status);
        PermissionManagementPageDto page = systemAdminService.getPermissionManagementPage(company, tab, query, module, permissionType, status);
        return ResponseEntity.ok(ApiResponse.success(page, "Permissions retrieved successfully"));
    }

    @GetMapping("/permissions/{id}")
    @Operation(summary = "Get detailed Permission side drawer by ID")
    public ResponseEntity<ApiResponse<PermissionDetailDto>> getPermissionDetail(@PathVariable String id) {
        log.info("GET /api/v1/admin/system/permissions/{} requested", id);
        PermissionDetailDto detail = systemAdminService.getPermissionDetail(id);
        return ResponseEntity.ok(ApiResponse.success(detail, "Permission detail retrieved successfully"));
    }

    @PostMapping("/permissions")
    @Operation(summary = "Assign / Create a new Permission")
    public ResponseEntity<ApiResponse<PermissionDetailDto>> assignPermission(@RequestBody AssignPermissionRequestDto request) {
        log.info("POST /api/v1/admin/system/permissions requested for permissionName={}, module={}", request.getPermissionName(), request.getModule());
        PermissionDetailDto created = systemAdminService.assignPermission(request);
        return ResponseEntity.ok(ApiResponse.success(created, "Permission assigned successfully"));
    }

    @PutMapping("/permissions/{id}/deactivate")
    @Operation(summary = "Deactivate permission")
    public ResponseEntity<ApiResponse<PermissionDetailDto>> deactivatePermission(@PathVariable String id) {
        log.info("PUT /api/v1/admin/system/permissions/{}/deactivate requested", id);
        PermissionDetailDto updated = systemAdminService.deactivatePermission(id);
        return ResponseEntity.ok(ApiResponse.success(updated, "Permission deactivated successfully"));
    }

    // ==========================================
    // 4. ACCESS CONTROL
    // ==========================================
    @GetMapping("/access")
    @Operation(summary = "Get Access Control page with access KPIs, tabs, module access donut, access level bar, and access records table")
    public ResponseEntity<ApiResponse<AccessControlPageDto>> getAccessControl(
            @RequestParam(required = false, defaultValue = "Atirath Holdings") String company,
            @RequestParam(required = false) String tab,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String accessStatus) {
        log.info("GET /api/v1/admin/system/access requested (tab={}, query={}, module={}, role={}, status={})", tab, query, module, role, accessStatus);
        AccessControlPageDto page = systemAdminService.getAccessControlPage(company, tab, query, module, role, accessStatus);
        return ResponseEntity.ok(ApiResponse.success(page, "Access control records retrieved successfully"));
    }

    @GetMapping("/access/{id}")
    @Operation(summary = "Get detailed Access side drawer by ID")
    public ResponseEntity<ApiResponse<AccessDetailDto>> getAccessDetail(@PathVariable String id) {
        log.info("GET /api/v1/admin/system/access/{} requested", id);
        AccessDetailDto detail = systemAdminService.getAccessDetail(id);
        return ResponseEntity.ok(ApiResponse.success(detail, "Access detail retrieved successfully"));
    }

    @PostMapping("/access")
    @Operation(summary = "Grant access to user")
    public ResponseEntity<ApiResponse<AccessDetailDto>> grantAccess(@RequestBody GrantAccessRequestDto request) {
        log.info("POST /api/v1/admin/system/access requested for userName={}, role={}", request.getUserName(), request.getRole());
        AccessDetailDto created = systemAdminService.grantAccess(request);
        return ResponseEntity.ok(ApiResponse.success(created, "Access granted successfully"));
    }

    @PutMapping("/access/{id}/revoke")
    @Operation(summary = "Revoke user access")
    public ResponseEntity<ApiResponse<AccessDetailDto>> revokeAccess(@PathVariable String id) {
        log.info("PUT /api/v1/admin/system/access/{}/revoke requested", id);
        AccessDetailDto updated = systemAdminService.revokeAccess(id);
        return ResponseEntity.ok(ApiResponse.success(updated, "Access revoked successfully"));
    }

    // ==========================================
    // 5. APPROVAL WORKFLOW
    // ==========================================
    @GetMapping("/workflows")
    @Operation(summary = "Get Approval Workflow Configuration page with workflow KPIs, tabs, module donut, levels bar, and workflows table")
    public ResponseEntity<ApiResponse<ApprovalWorkflowPageDto>> getWorkflows(
            @RequestParam(required = false, defaultValue = "Atirath Holdings") String company,
            @RequestParam(required = false) String tab,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String approvalLevel) {
        log.info("GET /api/v1/admin/system/workflows requested (tab={}, query={}, module={}, status={}, level={})", tab, query, module, status, approvalLevel);
        ApprovalWorkflowPageDto page = systemAdminService.getApprovalWorkflowPage(company, tab, query, module, status, approvalLevel);
        return ResponseEntity.ok(ApiResponse.success(page, "Approval workflows retrieved successfully"));
    }

    @GetMapping("/workflows/{id}")
    @Operation(summary = "Get detailed Workflow side drawer by ID")
    public ResponseEntity<ApiResponse<WorkflowDetailDto>> getWorkflowDetail(@PathVariable String id) {
        log.info("GET /api/v1/admin/system/workflows/{} requested", id);
        WorkflowDetailDto detail = systemAdminService.getWorkflowDetail(id);
        return ResponseEntity.ok(ApiResponse.success(detail, "Workflow detail retrieved successfully"));
    }

    @PostMapping("/workflows")
    @Operation(summary = "Create a new Approval Workflow")
    public ResponseEntity<ApiResponse<WorkflowDetailDto>> createWorkflow(@RequestBody CreateWorkflowRequestDto request) {
        log.info("POST /api/v1/admin/system/workflows requested for workflowName={}, module={}", request.getWorkflowName(), request.getModule());
        WorkflowDetailDto created = systemAdminService.createWorkflow(request);
        return ResponseEntity.ok(ApiResponse.success(created, "Workflow created successfully"));
    }

    @PostMapping("/workflows/{id}/clone")
    @Operation(summary = "Clone an existing Approval Workflow")
    public ResponseEntity<ApiResponse<WorkflowDetailDto>> cloneWorkflow(@PathVariable String id) {
        log.info("POST /api/v1/admin/system/workflows/{}/clone requested", id);
        WorkflowDetailDto cloned = systemAdminService.cloneWorkflow(id);
        return ResponseEntity.ok(ApiResponse.success(cloned, "Workflow cloned successfully"));
    }

    @PutMapping("/workflows/{id}/deactivate")
    @Operation(summary = "Deactivate workflow")
    public ResponseEntity<ApiResponse<WorkflowDetailDto>> deactivateWorkflow(@PathVariable String id) {
        log.info("PUT /api/v1/admin/system/workflows/{}/deactivate requested", id);
        WorkflowDetailDto updated = systemAdminService.deactivateWorkflow(id);
        return ResponseEntity.ok(ApiResponse.success(updated, "Workflow deactivated successfully"));
    }

    // ==========================================
    // 6. TRADING RULE CONFIGURATION
    // ==========================================
    @GetMapping("/trading-rules")
    @Operation(summary = "Get Trading Rule Configuration page with KPIs, category donut, status/priority bars, and rules table")
    public ResponseEntity<ApiResponse<TradingRuleConfigPageDto>> getTradingRules(
            @RequestParam(required = false, defaultValue = "Atirath Holdings") String company,
            @RequestParam(required = false) String tab,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String module) {
        log.info("GET /api/v1/admin/system/trading-rules requested (tab={}, query={}, cat={}, status={})", tab, query, category, status);
        TradingRuleConfigPageDto page = systemAdminService.getTradingRulesPage(company, tab, query, category, status, module);
        return ResponseEntity.ok(ApiResponse.success(page, "Trading rules retrieved successfully"));
    }

    @GetMapping("/trading-rules/{id}")
    @Operation(summary = "Get detailed Trading Rule side drawer by ID")
    public ResponseEntity<ApiResponse<TradingRuleDetailDto>> getTradingRuleDetail(@PathVariable String id) {
        log.info("GET /api/v1/admin/system/trading-rules/{} requested", id);
        TradingRuleDetailDto detail = systemAdminService.getTradingRuleDetail(id);
        return ResponseEntity.ok(ApiResponse.success(detail, "Trading rule detail retrieved successfully"));
    }

    @PostMapping("/trading-rules")
    @Operation(summary = "Create a new Trading Rule")
    public ResponseEntity<ApiResponse<TradingRuleDetailDto>> createTradingRule(@RequestBody CreateTradingRuleRequestDto request) {
        log.info("POST /api/v1/admin/system/trading-rules requested for ruleName={}", request.getRuleName());
        TradingRuleDetailDto created = systemAdminService.createTradingRule(request);
        return ResponseEntity.ok(ApiResponse.success(created, "Trading rule created successfully"));
    }

    @PostMapping("/trading-rules/{id}/clone")
    @Operation(summary = "Clone an existing Trading Rule")
    public ResponseEntity<ApiResponse<TradingRuleDetailDto>> cloneTradingRule(@PathVariable String id) {
        log.info("POST /api/v1/admin/system/trading-rules/{}/clone requested", id);
        TradingRuleDetailDto cloned = systemAdminService.cloneTradingRule(id);
        return ResponseEntity.ok(ApiResponse.success(cloned, "Trading rule cloned successfully"));
    }

    @PutMapping("/trading-rules/{id}/deactivate")
    @Operation(summary = "Deactivate trading rule")
    public ResponseEntity<ApiResponse<TradingRuleDetailDto>> deactivateTradingRule(@PathVariable String id) {
        log.info("PUT /api/v1/admin/system/trading-rules/{}/deactivate requested", id);
        TradingRuleDetailDto updated = systemAdminService.deactivateTradingRule(id);
        return ResponseEntity.ok(ApiResponse.success(updated, "Trading rule deactivated successfully"));
    }

    // ==========================================
    // 7. NOTIFICATION CONFIGURATION
    // ==========================================
    @GetMapping("/notifications")
    @Operation(summary = "Get Notification Configuration page with KPIs, channels bar, trend line chart, and notifications table")
    public ResponseEntity<ApiResponse<NotificationConfigPageDto>> getNotifications(
            @RequestParam(required = false, defaultValue = "Atirath Holdings") String company,
            @RequestParam(required = false) String tab,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String channel,
            @RequestParam(required = false) String status) {
        log.info("GET /api/v1/admin/system/notifications requested (tab={}, query={}, cat={}, channel={})", tab, query, category, channel);
        NotificationConfigPageDto page = systemAdminService.getNotificationConfigPage(company, tab, query, category, channel, status);
        return ResponseEntity.ok(ApiResponse.success(page, "Notifications retrieved successfully"));
    }

    @GetMapping("/notifications/{id}")
    @Operation(summary = "Get detailed Notification side drawer by ID")
    public ResponseEntity<ApiResponse<NotificationDetailDto>> getNotificationDetail(@PathVariable String id) {
        log.info("GET /api/v1/admin/system/notifications/{} requested", id);
        NotificationDetailDto detail = systemAdminService.getNotificationDetail(id);
        return ResponseEntity.ok(ApiResponse.success(detail, "Notification detail retrieved successfully"));
    }

    @PostMapping("/notifications")
    @Operation(summary = "Create a new Notification")
    public ResponseEntity<ApiResponse<NotificationDetailDto>> createNotification(@RequestBody CreateNotificationRequestDto request) {
        log.info("POST /api/v1/admin/system/notifications requested for notificationName={}", request.getNotificationName());
        NotificationDetailDto created = systemAdminService.createNotification(request);
        return ResponseEntity.ok(ApiResponse.success(created, "Notification created successfully"));
    }

    @PostMapping("/notifications/{id}/clone")
    @Operation(summary = "Clone an existing Notification")
    public ResponseEntity<ApiResponse<NotificationDetailDto>> cloneNotification(@PathVariable String id) {
        log.info("POST /api/v1/admin/system/notifications/{}/clone requested", id);
        NotificationDetailDto cloned = systemAdminService.cloneNotification(id);
        return ResponseEntity.ok(ApiResponse.success(cloned, "Notification cloned successfully"));
    }

    @PutMapping("/notifications/{id}/deactivate")
    @Operation(summary = "Deactivate notification")
    public ResponseEntity<ApiResponse<NotificationDetailDto>> deactivateNotification(@PathVariable String id) {
        log.info("PUT /api/v1/admin/system/notifications/{}/deactivate requested", id);
        NotificationDetailDto updated = systemAdminService.deactivateNotification(id);
        return ResponseEntity.ok(ApiResponse.success(updated, "Notification deactivated successfully"));
    }

    // ==========================================
    // 8. SYSTEM SETTINGS
    // ==========================================
    @GetMapping("/settings")
    @Operation(summary = "Get System Settings page with KPIs, module donut, status bar, recent changes, and settings table")
    public ResponseEntity<ApiResponse<SystemSettingsPageDto>> getSettings(
            @RequestParam(required = false, defaultValue = "Atirath Holdings") String company,
            @RequestParam(required = false) String tab,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String dataType) {
        log.info("GET /api/v1/admin/system/settings requested (tab={}, query={}, module={}, type={})", tab, query, module, dataType);
        SystemSettingsPageDto page = systemAdminService.getSystemSettingsPage(company, tab, query, module, status, dataType);
        return ResponseEntity.ok(ApiResponse.success(page, "System settings retrieved successfully"));
    }

    @GetMapping("/settings/{id}")
    @Operation(summary = "Get detailed System Setting side drawer by ID")
    public ResponseEntity<ApiResponse<SystemSettingDetailDto>> getSettingDetail(@PathVariable String id) {
        log.info("GET /api/v1/admin/system/settings/{} requested", id);
        SystemSettingDetailDto detail = systemAdminService.getSystemSettingDetail(id);
        return ResponseEntity.ok(ApiResponse.success(detail, "System setting detail retrieved successfully"));
    }

    @PostMapping("/settings")
    @Operation(summary = "Add a new System Setting")
    public ResponseEntity<ApiResponse<SystemSettingDetailDto>> addSetting(@RequestBody AddSystemSettingRequestDto request) {
        log.info("POST /api/v1/admin/system/settings requested for settingKey={}", request.getSettingKey());
        SystemSettingDetailDto created = systemAdminService.addSystemSetting(request);
        return ResponseEntity.ok(ApiResponse.success(created, "System setting added successfully"));
    }

    @PutMapping("/settings/{id}")
    @Operation(summary = "Update an existing System Setting")
    public ResponseEntity<ApiResponse<SystemSettingDetailDto>> updateSetting(
            @PathVariable String id,
            @RequestBody UpdateSystemSettingRequestDto request) {
        log.info("PUT /api/v1/admin/system/settings/{} requested", id);
        SystemSettingDetailDto updated = systemAdminService.updateSystemSetting(id, request);
        return ResponseEntity.ok(ApiResponse.success(updated, "System setting updated successfully"));
    }

    @PutMapping("/settings/{id}/deactivate")
    @Operation(summary = "Deactivate system setting")
    public ResponseEntity<ApiResponse<SystemSettingDetailDto>> deactivateSetting(@PathVariable String id) {
        log.info("PUT /api/v1/admin/system/settings/{}/deactivate requested", id);
        SystemSettingDetailDto updated = systemAdminService.deactivateSystemSetting(id);
        return ResponseEntity.ok(ApiResponse.success(updated, "System setting deactivated successfully"));
    }

    // ==========================================
    // 9. AUDIT & ACTIVITY LOGS
    // ==========================================
    @GetMapping("/audit-logs")
    @Operation(summary = "Get Audit & Activity Logs page with KPIs, module donut, action bar, activity trend chart, and logs table")
    public ResponseEntity<ApiResponse<AuditActivityLogsPageDto>> getAuditLogs(
            @RequestParam(required = false, defaultValue = "Atirath Holdings") String company,
            @RequestParam(required = false) String tab,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String user,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate) {
        log.info("GET /api/v1/admin/system/audit-logs requested (tab={}, query={}, module={}, action={}, user={})", tab, query, module, action, user);
        AuditActivityLogsPageDto page = systemAdminService.getAuditLogsPage(company, tab, query, module, action, user, fromDate, toDate);
        return ResponseEntity.ok(ApiResponse.success(page, "Audit logs retrieved successfully"));
    }

    @GetMapping("/audit-logs/{id}")
    @Operation(summary = "Get detailed Audit Log side drawer by ID")
    public ResponseEntity<ApiResponse<AuditLogDetailDto>> getAuditLogDetail(@PathVariable String id) {
        log.info("GET /api/v1/admin/system/audit-logs/{} requested", id);
        AuditLogDetailDto detail = systemAdminService.getAuditLogDetail(id);
        return ResponseEntity.ok(ApiResponse.success(detail, "Audit log detail retrieved successfully"));
    }
}
