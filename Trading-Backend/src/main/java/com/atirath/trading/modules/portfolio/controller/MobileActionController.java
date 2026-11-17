package com.atirath.trading.modules.portfolio.controller;

import com.atirath.trading.common.ApiResponse;
import com.atirath.trading.config.JwtTokenProvider;
import com.atirath.trading.modules.portfolio.dto.mobile.MobileActionsPageDto;
import com.atirath.trading.modules.portfolio.service.MobileActionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/actions/mobile")
@RequiredArgsConstructor
@Tag(name = "Mobile Actions API", description = "Dedicated mobile endpoint delivering complete real pending actions for Flutter pending_actions_screen")
public class MobileActionController {

    private final MobileActionService mobileActionService;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping
    @Operation(summary = "Get Mobile Pending Actions",
            description = "Returns real per-user pending actions list (KYC, Bank, Escrow, Open Orders, Notifications) with dynamic status banner.")
    public ResponseEntity<ApiResponse<MobileActionsPageDto>> getMobileActions(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = extractUserId(authHeader);
        MobileActionsPageDto dto = mobileActionService.getMobileActions(userId);
        return ResponseEntity.ok(ApiResponse.success(dto, "Mobile pending actions retrieved successfully"));
    }

    private Long extractUserId(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring(7).trim();
                return jwtTokenProvider.getUserIdFromToken(token);
            } catch (Exception e) {
                log.debug("[MobileActionController] Could not parse JWT: {}", e.getMessage());
            }
        }
        return null;
    }
}
