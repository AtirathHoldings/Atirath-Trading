package com.atirath.trading.modules.portfolio.controller;

import com.atirath.trading.common.ApiResponse;
import com.atirath.trading.config.JwtTokenProvider;
import com.atirath.trading.modules.portfolio.dto.mobile.MobileDashboardDto;
import com.atirath.trading.modules.portfolio.service.MobileDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/dashboard/mobile")
@RequiredArgsConstructor
@Tag(name = "Mobile Dashboard API", description = "Dedicated mobile endpoint delivering complete real dynamic dashboard data for Flutter home_dashboard_screen.dart")
public class MobileDashboardController {

    private final MobileDashboardService mobileDashboardService;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping
    @Operation(summary = "Get Mobile Home Dashboard Data",
            description = "Returns real per-user dynamic data for home_dashboard_screen.dart: greeting, portfolio hero card, pending actions, top market snapshot stocks, top holdings, and recent transactions.")
    public ResponseEntity<ApiResponse<MobileDashboardDto>> getMobileDashboard(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = extractUserId(authHeader);
        MobileDashboardDto dto = mobileDashboardService.getMobileDashboard(userId);
        return ResponseEntity.ok(ApiResponse.success(dto, "Mobile dashboard data retrieved successfully"));
    }

    private Long extractUserId(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring(7).trim();
                return jwtTokenProvider.getUserIdFromToken(token);
            } catch (Exception e) {
                log.debug("[MobileDashboardController] Could not parse JWT: {}", e.getMessage());
            }
        }
        return null;
    }
}
