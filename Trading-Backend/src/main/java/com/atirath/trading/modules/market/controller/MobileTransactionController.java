package com.atirath.trading.modules.market.controller;

import com.atirath.trading.common.ApiResponse;
import com.atirath.trading.config.JwtTokenProvider;
import com.atirath.trading.modules.market.dto.mobile.MobileTransactionsPageDto;
import com.atirath.trading.modules.market.service.MobileTransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/transactions/mobile")
@RequiredArgsConstructor
@Tag(name = "Mobile Transactions API", description = "Dedicated mobile endpoint delivering complete real transactions data for Flutter recent_transactions_screen")
public class MobileTransactionController {

    private final MobileTransactionService mobileTransactionService;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping
    @Operation(summary = "Get Mobile Transactions",
            description = "Returns real per-user transaction history with filter support (all, buy, sell, settlement, transfer).")
    public ResponseEntity<ApiResponse<MobileTransactionsPageDto>> getMobileTransactions(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "filter", required = false) String filter) {

        Long userId = extractUserId(authHeader);
        MobileTransactionsPageDto dto = mobileTransactionService.getMobileTransactions(userId, filter);
        return ResponseEntity.ok(ApiResponse.success(dto, "Mobile transactions retrieved successfully"));
    }

    private Long extractUserId(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring(7).trim();
                return jwtTokenProvider.getUserIdFromToken(token);
            } catch (Exception e) {
                log.debug("[MobileTransactionController] Could not parse JWT: {}", e.getMessage());
            }
        }
        return null;
    }
}
