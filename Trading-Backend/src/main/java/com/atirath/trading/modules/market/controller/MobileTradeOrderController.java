package com.atirath.trading.modules.market.controller;

import com.atirath.trading.common.ApiResponse;
import com.atirath.trading.config.JwtTokenProvider;
import com.atirath.trading.modules.market.dto.mobile.*;
import com.atirath.trading.modules.market.service.TradeOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Dedicated REST Controller delivering lightweight, mobile-tailored responses for Flutter apps:
 * - trade_screen.dart
 * - order_confirmation_screen.dart
 * - my_orders_screen.dart
 * - order_lifecycle_screen.dart
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/trade/mobile")
@RequiredArgsConstructor
@Tag(name = "Mobile Trade API", description = "Lightweight endpoints optimized for Flutter trade_screen, order_confirmation_screen, my_orders_screen, and order_lifecycle_screen")
public class MobileTradeOrderController {

    private final TradeOrderService tradeOrderService;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping(value = {"/terminal", "/terminal/{symbol}"})
    @Operation(summary = "Get Mobile Trade Screen Data",
            description = "Returns compact live price, 5-depth order book, available cash and available shares for trade_screen.dart")
    public ResponseEntity<ApiResponse<MobileTradeScreenDto>> getMobileTradeScreen(
            @PathVariable(value = "symbol", required = false) String symbol,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = extractUserId(authHeader);
        String sym = (symbol != null && !symbol.isBlank()) ? symbol : "ATG-H";
        MobileTradeScreenDto dto = tradeOrderService.getMobileTradeScreen(sym, userId);
        return ResponseEntity.ok(ApiResponse.success(dto, "Mobile trade screen data retrieved successfully"));
    }

    @PostMapping("/preview")
    @Operation(summary = "Preview Mobile Order & Dynamic Calculation",
            description = "Calculates exact 0.1% transaction fee, 18% GST, checks wallet sufficiency, and returns post-order balance for trade_screen.dart")
    public ResponseEntity<ApiResponse<MobileOrderPreviewDto>> previewMobileOrder(
            @Valid @RequestBody MobilePlaceOrderRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = extractUserId(authHeader);
        MobileOrderPreviewDto preview = tradeOrderService.previewMobileOrder(request, userId);
        return ResponseEntity.ok(ApiResponse.success(preview, "Mobile order preview calculated successfully"));
    }

    @PostMapping("/orders")
    @Operation(summary = "Place Mobile Order (Order Confirmation)",
            description = "Submits order and returns concise confirmation with 6-stage lifecycle for order_confirmation_screen.dart")
    public ResponseEntity<ApiResponse<MobileOrderConfirmationDto>> placeMobileOrder(
            @Valid @RequestBody MobilePlaceOrderRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = extractUserId(authHeader);
        MobileOrderConfirmationDto confirmation = tradeOrderService.placeMobileOrder(request, userId);
        return ResponseEntity.ok(ApiResponse.success(confirmation, "Order submitted successfully"));
    }

    @GetMapping("/my-orders")
    @Operation(summary = "Get Mobile My Orders List",
            description = "Returns status tab counts and lightweight order cards for my_orders_screen.dart")
    public ResponseEntity<ApiResponse<MobileMyOrdersResponseDto>> getMobileMyOrders(
            @Parameter(description = "Filter Tab (ALL, OPEN, IN_PROGRESS, COMPLETED, CANCELLED, REJECTED)")
            @RequestParam(required = false, defaultValue = "ALL") String tab,
            @Parameter(description = "Search keyword / symbol")
            @RequestParam(required = false) String search,
            @Parameter(description = "Side (BUY or SELL)")
            @RequestParam(required = false) String side,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = extractUserId(authHeader);
        MobileMyOrdersResponseDto response = tradeOrderService.getMobileMyOrders(tab, search, side, page, size, userId);
        return ResponseEntity.ok(ApiResponse.success(response, "Mobile orders retrieved successfully"));
    }

    @GetMapping("/orders/{orderId}/lifecycle")
    @Operation(summary = "Get Mobile Order Lifecycle & Stepper by ID",
            description = "Returns 7-step horizontal progress stepper, escrow details, and concise activity timeline for order_lifecycle_screen.dart")
    public ResponseEntity<ApiResponse<MobileOrderLifecycleDto>> getMobileOrderLifecycleById(
            @PathVariable("orderId") Long orderId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = extractUserId(authHeader);
        MobileOrderLifecycleDto lifecycle = tradeOrderService.getMobileOrderLifecycle(orderId, null, userId);
        return ResponseEntity.ok(ApiResponse.success(lifecycle, "Order lifecycle retrieved successfully"));
    }

    @GetMapping("/orders/status-by-no/{orderNo}/lifecycle")
    @Operation(summary = "Get Mobile Order Lifecycle & Stepper by Order No",
            description = "Returns order lifecycle details using Order Reference No (e.g. BUY25091814322) for order_lifecycle_screen.dart")
    public ResponseEntity<ApiResponse<MobileOrderLifecycleDto>> getMobileOrderLifecycleByNo(
            @PathVariable("orderNo") String orderNo,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = extractUserId(authHeader);
        MobileOrderLifecycleDto lifecycle = tradeOrderService.getMobileOrderLifecycle(null, orderNo, userId);
        return ResponseEntity.ok(ApiResponse.success(lifecycle, "Order lifecycle retrieved successfully"));
    }

    @PostMapping("/orders/{orderId}/cancel")
    @Operation(summary = "Cancel Mobile Order",
            description = "Cancels a pending mobile order and releases blocked funds/shares")
    public ResponseEntity<ApiResponse<Void>> cancelMobileOrder(
            @PathVariable("orderId") Long orderId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = extractUserId(authHeader);
        tradeOrderService.cancelOrder(orderId, userId);
        return ResponseEntity.ok(ApiResponse.success(null, "Order cancelled successfully"));
    }

    @GetMapping("/wallet")
    @Operation(summary = "Get Mobile Wallet Balance",
            description = "Returns compact available and blocked balance for mobile wallet chip")
    public ResponseEntity<ApiResponse<MobileWalletSummaryDto>> getMobileWallet(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = extractUserId(authHeader);
        MobileWalletSummaryDto wallet = tradeOrderService.getMobileWallet(userId);
        return ResponseEntity.ok(ApiResponse.success(wallet, "Mobile wallet retrieved successfully"));
    }

    @GetMapping("/holdings")
    @Operation(summary = "Get Mobile User Holdings",
            description = "Returns lightweight list of user company holdings for portfolio and sell screens")
    public ResponseEntity<ApiResponse<List<MobileHoldingSummaryDto>>> getMobileHoldings(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = extractUserId(authHeader);
        List<MobileHoldingSummaryDto> holdings = tradeOrderService.getMobileHoldings(userId);
        return ResponseEntity.ok(ApiResponse.success(holdings, "Mobile holdings retrieved successfully"));
    }

    private Long extractUserId(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring(7).trim();
                return jwtTokenProvider.getUserIdFromToken(token);
            } catch (Exception e) {
                log.debug("[MobileTradeOrderController] Could not parse JWT: {}", e.getMessage());
            }
        }
        return null;
    }
}
