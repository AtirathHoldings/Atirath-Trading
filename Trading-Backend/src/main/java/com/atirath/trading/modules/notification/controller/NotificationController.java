package com.atirath.trading.modules.notification.controller;

import com.atirath.trading.common.ApiResponse;
import com.atirath.trading.config.JwtTokenProvider;
import com.atirath.trading.modules.notification.dto.*;
import com.atirath.trading.modules.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification Module", description = "Endpoints for Notifications Center and Notification Preferences")
public class NotificationController {

    private final NotificationService notificationService;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping
    @Operation(summary = "Get Notifications Center", description = "Returns notifications list, summary KPI cards (Total, Unread, Action Required, Important), category tabs, and selected detail drawer.")
    public ResponseEntity<ApiResponse<NotificationsResponseDto>> getNotifications(
            @Parameter(description = "Category tab: all, trade, negotiation, escrowPayment, shareTransfer, settlement, portfolio, system")
            @RequestParam(required = false, defaultValue = "all") String tab,
            @Parameter(description = "Search keyword by title, company, message")
            @RequestParam(required = false) String search,
            @Parameter(description = "Status filter: all, unread, read, action_required")
            @RequestParam(required = false, defaultValue = "all") String status,
            @Parameter(description = "Priority filter: all, success, action_required, important, information")
            @RequestParam(required = false, defaultValue = "all") String priority,
            @Parameter(description = "Date range: Last 30 Days, Today, Yesterday, etc.")
            @RequestParam(required = false, defaultValue = "Last 30 Days") String dateRange,
            @Parameter(description = "Selected notification ID for detail panel")
            @RequestParam(required = false) Long selectedId,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            @RequestParam(required = false) Long userId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long effectiveUserId = userId != null ? userId : extractUserId(authHeader);
        if (effectiveUserId == null) effectiveUserId = 1L;

        NotificationsResponseDto response = notificationService.getNotifications(
                effectiveUserId, tab, search, status, priority, dateRange, selectedId, page, size);
        return ResponseEntity.ok(ApiResponse.success(response, "Notifications retrieved successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Notification Details", description = "Returns full details for a single notification (used by detail side drawer).")
    public ResponseEntity<ApiResponse<NotificationDetailDto>> getNotificationDetail(
            @PathVariable("id") Long id,
            @RequestParam(required = false) Long userId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long effectiveUserId = userId != null ? userId : extractUserId(authHeader);
        if (effectiveUserId == null) effectiveUserId = 1L;

        NotificationDetailDto detail = notificationService.getNotificationDetail(id, effectiveUserId);
        return ResponseEntity.ok(ApiResponse.success(detail, "Notification details retrieved successfully"));
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Mark Notification as Read")
    public ResponseEntity<ApiResponse<String>> markAsRead(
            @PathVariable("id") Long id,
            @RequestParam(required = false) Long userId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long effectiveUserId = userId != null ? userId : extractUserId(authHeader);
        notificationService.markAsRead(id, effectiveUserId);
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read", "Success"));
    }

    @PostMapping("/mark-all-read")
    @Operation(summary = "Mark All Notifications as Read")
    public ResponseEntity<ApiResponse<String>> markAllAsRead(
            @RequestParam(required = false) Long userId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long effectiveUserId = userId != null ? userId : extractUserId(authHeader);
        notificationService.markAllAsRead(effectiveUserId);
        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read", "Success"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Notification")
    public ResponseEntity<ApiResponse<String>> deleteNotification(
            @PathVariable("id") Long id,
            @RequestParam(required = false) Long userId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long effectiveUserId = userId != null ? userId : extractUserId(authHeader);
        notificationService.deleteNotification(id, effectiveUserId);
        return ResponseEntity.ok(ApiResponse.success("Notification deleted successfully", "Success"));
    }

    // ==========================================
    // NOTIFICATION PREFERENCES
    // ==========================================

    @GetMapping("/preferences")
    @Operation(summary = "Get Notification Preferences", description = "Returns channel statuses (Email, SMS, Push, In-App), 11 preference matrix rows, quiet hours, and frequency options.")
    public ResponseEntity<ApiResponse<NotificationPreferencesResponseDto>> getPreferences(
            @RequestParam(required = false) Long userId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long effectiveUserId = userId != null ? userId : extractUserId(authHeader);
        if (effectiveUserId == null) effectiveUserId = 1L;

        NotificationPreferencesResponseDto response = notificationService.getPreferences(effectiveUserId);
        return ResponseEntity.ok(ApiResponse.success(response, "Notification preferences retrieved successfully"));
    }

    @PutMapping("/preferences")
    @Operation(summary = "Save Notification Preferences", description = "Updates fine-grained channel toggles, frequencies, and quiet hours.")
    public ResponseEntity<ApiResponse<NotificationPreferencesResponseDto>> updatePreferences(
            @RequestBody UpdateNotificationPreferencesRequest request,
            @RequestParam(required = false) Long userId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long effectiveUserId = userId != null ? userId : extractUserId(authHeader);
        if (effectiveUserId == null) effectiveUserId = 1L;

        NotificationPreferencesResponseDto updated = notificationService.updatePreferences(effectiveUserId, request);
        return ResponseEntity.ok(ApiResponse.success(updated, "Preferences saved successfully"));
    }

    @PostMapping("/preferences/reset")
    @Operation(summary = "Reset Notification Preferences to Default")
    public ResponseEntity<ApiResponse<NotificationPreferencesResponseDto>> resetPreferences(
            @RequestParam(required = false) Long userId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long effectiveUserId = userId != null ? userId : extractUserId(authHeader);
        if (effectiveUserId == null) effectiveUserId = 1L;

        NotificationPreferencesResponseDto reset = notificationService.resetPreferences(effectiveUserId);
        return ResponseEntity.ok(ApiResponse.success(reset, "Preferences reset to default successfully"));
    }

    private Long extractUserId(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring(7).trim();
                return jwtTokenProvider.getUserIdFromToken(token);
            } catch (Exception e) {
                log.debug("[NotificationController] Could not parse JWT: {}", e.getMessage());
            }
        }
        return null;
    }
}
