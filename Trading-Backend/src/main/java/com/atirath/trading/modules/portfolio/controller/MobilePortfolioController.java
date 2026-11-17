package com.atirath.trading.modules.portfolio.controller;

import com.atirath.trading.common.ApiResponse;
import com.atirath.trading.config.JwtTokenProvider;
import com.atirath.trading.modules.portfolio.dto.mobile.MobilePortfolioDto;
import com.atirath.trading.modules.portfolio.service.MobilePortfolioService;
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
@RequestMapping("/api/v1/portfolio/mobile")
@RequiredArgsConstructor
@Tag(name = "Mobile Portfolio API", description = "Dedicated mobile endpoint delivering complete real portfolio data for Flutter portfolio_screen, portfolio_overview_screen, and my_holdings_screen")
public class MobilePortfolioController {

    private final MobilePortfolioService mobilePortfolioService;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping
    @Operation(summary = "Get Mobile Portfolio Data",
            description = "Returns real per-user portfolio summary metrics, company allocations, holdings details with sparklines, and user orders.")
    public ResponseEntity<ApiResponse<MobilePortfolioDto>> getMobilePortfolio(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = extractUserId(authHeader);
        MobilePortfolioDto dto = mobilePortfolioService.getMobilePortfolio(userId);
        return ResponseEntity.ok(ApiResponse.success(dto, "Mobile portfolio data retrieved successfully"));
    }

    private Long extractUserId(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring(7).trim();
                return jwtTokenProvider.getUserIdFromToken(token);
            } catch (Exception e) {
                log.debug("[MobilePortfolioController] Could not parse JWT: {}", e.getMessage());
            }
        }
        return null;
    }
}
