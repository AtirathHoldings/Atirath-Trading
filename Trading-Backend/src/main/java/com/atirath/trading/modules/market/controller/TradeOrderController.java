package com.atirath.trading.modules.market.controller;

import com.atirath.trading.common.ApiResponse;
import com.atirath.trading.config.JwtTokenProvider;
import com.atirath.trading.modules.market.dto.*;
import com.atirath.trading.modules.market.service.TradeOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/trade")
@RequiredArgsConstructor
@Tag(name = "Trade Terminal & Orders", description = "APIs for Trade Terminal, Buy/Sell shares, Order Review, and Order Confirmation")
public class TradeOrderController {

    private final TradeOrderService tradeOrderService;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping(value = {"/terminal", "/terminal/{symbol}"})
    @Operation(summary = "Get Trade Terminal Snapshot",
            description = "Returns complete Trade Terminal data: ticker header, 5-level live order book, tape of recent trades, market depth, key stats, insight, and user wallet/holding balances")
    public ResponseEntity<ApiResponse<TradeTerminalSnapshotDto>> getTerminalSnapshot(
            @PathVariable(value = "symbol", required = false) String symbol,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = extractUserId(authHeader);
        String sym = symbol != null ? symbol : "ATG-H";
        TradeTerminalSnapshotDto snapshot = tradeOrderService.getTerminalSnapshot(sym, userId);
        return ResponseEntity.ok(ApiResponse.success(snapshot, "Trade Terminal snapshot retrieved successfully"));
    }

    @PostMapping("/orders/preview")
    @Operation(summary = "Preview Order & Review Charges",
            description = "Calculates exact 0.1% transaction fee, 18% GST, total amount payable/receivable, checks wallet sufficiency, and returns estimated post-order balance for Order Review screen")
    public ResponseEntity<ApiResponse<OrderPreviewResponse>> previewOrder(
            @Valid @RequestBody OrderPreviewRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = extractUserId(authHeader);
        OrderPreviewResponse preview = tradeOrderService.previewOrder(request, userId);
        return ResponseEntity.ok(ApiResponse.success(preview, "Order preview calculated successfully"));
    }

    @PostMapping("/orders")
    @Operation(summary = "Place Buy/Sell Order (Confirm Order)",
            description = "Submits order to internal market, records charges, blocks escrow balance or locks shares, generates audit trail, and returns Order Confirmation with 6-stage lifecycle timeline")
    @SecurityRequirement(name = "BearerAuthentication")
    public ResponseEntity<ApiResponse<OrderConfirmationDto>> placeOrder(
            @Valid @RequestBody PlaceOrderRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = extractUserId(authHeader);
        OrderConfirmationDto confirmation = tradeOrderService.placeOrder(request, userId);
        return ResponseEntity.ok(ApiResponse.success(confirmation, "Order submitted successfully to internal market"));
    }

    @GetMapping("/orders")
    @Operation(summary = "Get User Orders List", description = "Returns simple list of all market orders placed by the user")
    public ResponseEntity<ApiResponse<List<OrderListItemDto>>> getMyOrdersSimple(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = extractUserId(authHeader);
        List<OrderListItemDto> orders = tradeOrderService.getUserOrders(userId);
        return ResponseEntity.ok(ApiResponse.success(orders, "User orders retrieved successfully"));
    }

    @GetMapping("/orders/my-orders")
    @Operation(summary = "Get My Orders (Screen 2)",
            description = "Returns paginated orders catalog with status tabs (All, Open, In Progress, Completed, Cancelled, Rejected), filters, and summary counts")
    public ResponseEntity<ApiResponse<MyOrdersResponseDto>> getMyOrders(
            @Parameter(description = "Status Tab (ALL, OPEN, IN_PROGRESS, COMPLETED, CANCELLED, REJECTED)")
            @RequestParam(required = false, defaultValue = "ALL") String tab,
            @Parameter(description = "Company Name or Symbol filter")
            @RequestParam(required = false) String company,
            @Parameter(description = "Order Type (BUY, SELL, LIMIT, MARKET)")
            @RequestParam(required = false) String orderType,
            @Parameter(description = "Order status filter")
            @RequestParam(required = false) String status,
            @Parameter(description = "Start date (YYYY-MM-DD)")
            @RequestParam(required = false) String startDate,
            @Parameter(description = "End date (YYYY-MM-DD)")
            @RequestParam(required = false) String endDate,
            @Parameter(description = "Page number (1-indexed, default 1)")
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Page size (default 10)")
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = extractUserId(authHeader);
        MyOrdersResponseDto response = tradeOrderService.getMyOrders(tab, company, orderType, status, startDate, endDate, page, size, userId);
        return ResponseEntity.ok(ApiResponse.success(response, "My orders retrieved successfully"));
    }

    @GetMapping(value = {"/orders/{orderId}", "/orders/{orderId}/status"})
    @Operation(summary = "Get Order Status & Details (Screens 1 & 3)",
            description = "Returns complete order details, 7-step progress stepper, escrow details, counterparty info, and activity log")
    public ResponseEntity<ApiResponse<OrderStatusDetailDto>> getOrderStatus(
            @PathVariable("orderId") Long orderId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = extractUserId(authHeader);
        OrderStatusDetailDto detail = tradeOrderService.getOrderStatusDetail(orderId, null, userId);
        return ResponseEntity.ok(ApiResponse.success(detail, "Order status details retrieved successfully"));
    }

    @GetMapping("/orders/status-by-no/{orderNo}")
    @Operation(summary = "Get Order Status by Order Reference Number",
            description = "Returns order status details using Order Reference No (e.g. BUY25091814322)")
    public ResponseEntity<ApiResponse<OrderStatusDetailDto>> getOrderStatusByNo(
            @PathVariable("orderNo") String orderNo,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = extractUserId(authHeader);
        OrderStatusDetailDto detail = tradeOrderService.getOrderStatusDetail(null, orderNo, userId);
        return ResponseEntity.ok(ApiResponse.success(detail, "Order status retrieved successfully"));
    }

    @PutMapping("/orders/{orderId}")
    @Operation(summary = "Modify Order (Edit Order)",
            description = "Allows modifying quantity or price of an existing pending/in-escrow order")
    public ResponseEntity<ApiResponse<OrderStatusDetailDto>> modifyOrder(
            @PathVariable("orderId") Long orderId,
            @Valid @RequestBody PlaceOrderRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = extractUserId(authHeader);
        OrderStatusDetailDto updated = tradeOrderService.modifyOrder(orderId, request, userId);
        return ResponseEntity.ok(ApiResponse.success(updated, "Order modified successfully"));
    }

    @PostMapping("/orders/{orderId}/cancel")
    @Operation(summary = "Cancel Order", description = "Cancels a pending order, releases blocked escrow funds or locked shares, and records audit log")
    @SecurityRequirement(name = "BearerAuthentication")
    public ResponseEntity<ApiResponse<Void>> cancelOrder(
            @PathVariable("orderId") Long orderId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = extractUserId(authHeader);
        tradeOrderService.cancelOrder(orderId, userId);
        return ResponseEntity.ok(ApiResponse.success(null, "Order cancelled successfully"));
    }

    @GetMapping("/wallet")
    @Operation(summary = "Get User Escrow Wallet", description = "Returns available and blocked escrow wallet balance in INR")
    @SecurityRequirement(name = "BearerAuthentication")
    public ResponseEntity<ApiResponse<UserWalletDto>> getWallet(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = extractUserId(authHeader);
        UserWalletDto wallet = tradeOrderService.getUserWallet(userId);
        return ResponseEntity.ok(ApiResponse.success(wallet, "User escrow wallet retrieved successfully"));
    }

    @GetMapping("/holdings")
    @Operation(summary = "Get User Holdings", description = "Returns company shares owned, locked shares, average buy price, and unrealized P&L")
    @SecurityRequirement(name = "BearerAuthentication")
    public ResponseEntity<ApiResponse<List<UserHoldingDto>>> getHoldings(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = extractUserId(authHeader);
        List<UserHoldingDto> holdings = tradeOrderService.getUserHoldings(userId);
        return ResponseEntity.ok(ApiResponse.success(holdings, "User holdings retrieved successfully"));
    }

    private Long extractUserId(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring(7).trim();
                return jwtTokenProvider.getUserIdFromToken(token);
            } catch (Exception e) {
                log.debug("[TradeOrderController] Could not parse JWT: {}", e.getMessage());
            }
        }
        return null;
    }
}
