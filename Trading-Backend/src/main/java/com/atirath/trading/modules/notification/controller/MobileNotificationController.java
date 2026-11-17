package com.atirath.trading.modules.notification.controller;

import com.atirath.trading.common.ApiResponse;
import com.atirath.trading.config.JwtTokenProvider;
import com.atirath.trading.modules.notification.dto.mobile.MobileNotificationsPageDto;
import com.atirath.trading.modules.notification.service.MobileNotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/notifications/mobile")
@RequiredArgsConstructor
@Tag(name = "Mobile Notifications API", description = "Dedicated mobile endpoint delivering complete real notifications for Flutter notification_center_screen")
public class MobileNotificationController {

    private final MobileNotificationService mobileNotificationService;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping
    @Operation(summary = "Get Mobile Notifications",
            description = "Returns real per-user notifications list with category counts (all, unread, trade, system).")
    public ResponseEntity<ApiResponse<MobileNotificationsPageDto>> getMobileNotifications(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "filter", required = false) String filter) {

        Long userId = extractUserId(authHeader);
        MobileNotificationsPageDto dto = mobileNotificationService.getMobileNotifications(userId, filter);
        return ResponseEntity.ok(ApiResponse.success(dto, "Mobile notifications retrieved successfully"));
    }

    @PostMapping("/{id}/read")
    @Operation(summary = "Mark Notification as Read", description = "Marks a specific notification as read in the database.")
    public ResponseEntity<ApiResponse<Map<String, Object>>> markAsRead(
            @PathVariable("id") Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = extractUserId(authHeader);
        boolean success = mobileNotificationService.markAsRead(id, userId);
        return ResponseEntity.ok(ApiResponse.success(Map.of("markedRead", success), "Notification marked as read"));
    }

    @PostMapping("/mark-all-read")
    @Operation(summary = "Mark All Notifications as Read", description = "Marks all unread notifications as read for the user.")
    public ResponseEntity<ApiResponse<Map<String, Object>>> markAllAsRead(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = extractUserId(authHeader);
        int updated = mobileNotificationService.markAllAsRead(userId);
        return ResponseEntity.ok(ApiResponse.success(Map.of("markedCount", updated), "All notifications marked as read"));
    }

    private Long extractUserId(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring(7).trim();
                return jwtTokenProvider.getUserIdFromToken(token);
            } catch (Exception e) {
                log.debug("[MobileNotificationController] Could not parse JWT: {}", e.getMessage());
            }
        }
        return null;
    }
}
