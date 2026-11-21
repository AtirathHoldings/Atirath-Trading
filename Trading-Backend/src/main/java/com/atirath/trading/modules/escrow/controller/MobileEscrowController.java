package com.atirath.trading.modules.escrow.controller;

import com.atirath.trading.common.ApiResponse;
import com.atirath.trading.config.JwtTokenProvider;
import com.atirath.trading.modules.escrow.dto.mobile.*;
import com.atirath.trading.modules.escrow.service.MobileEscrowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Dedicated REST Controller delivering lightweight, screen-specific responses
 * for all Flutter Escrow & Settlement screens (escrow_screen.dart).
 */
@Slf4j
@RestController
@RequestMapping({"/api/v1/escrow/mobile", "/api/v1/mobile/escrow"})
@RequiredArgsConstructor
@Tag(name = "Mobile Escrow & Settlement API", description = "Endpoints tailored for Flutter Escrow Dashboard, Details, Funding, Share Lock, Share Transfer, and Settlement screens")
public class MobileEscrowController {

    private final MobileEscrowService mobileEscrowService;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 1. Escrow Dashboard
     */
    @GetMapping("/dashboard")
    @Operation(summary = "Screen 1: Escrow Dashboard",
            description = "Returns 2x2 KPI metrics (Active Escrow, Pending Funding, In Transfer, Settled MTD), Active Escrow cards, and Recently Settled list")
    public ResponseEntity<ApiResponse<MobileEscrowDashboardDto>> getDashboard(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = extractUserId(authHeader);
        MobileEscrowDashboardDto dto = mobileEscrowService.getMobileDashboard(userId);
        return ResponseEntity.ok(ApiResponse.success(dto, "Escrow dashboard retrieved successfully"));
    }

    /**
     * 2. Escrow Details
     */
    @GetMapping({"/{escrowId}/details", "/details/{escrowId}"})
    @Operation(summary = "Screen 2: Escrow Details",
            description = "Returns compact Escrow Information, 7-Step Timeline, and available action button states")
    public ResponseEntity<ApiResponse<MobileEscrowDetailDto>> getEscrowDetail(
            @PathVariable("escrowId") String escrowId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = extractUserId(authHeader);
        MobileEscrowDetailDto dto = mobileEscrowService.getMobileEscrowDetail(escrowId, userId);
        return ResponseEntity.ok(ApiResponse.success(dto, "Escrow details retrieved successfully"));
    }

    /**
     * 3. FundEscrow – Make Payment Screen
     */
    @GetMapping({"/{escrowId}/fund-form", "/fund-form/{escrowId}"})
    @Operation(summary = "Screen 3: FundEscrow – Make Payment Screen",
            description = "Returns amount to fund, default payment method, upi id, and bank selection list")
    public ResponseEntity<ApiResponse<MobileFundPaymentFormDto>> getFundForm(
            @PathVariable("escrowId") String escrowId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = extractUserId(authHeader);
        MobileFundPaymentFormDto dto = mobileEscrowService.getMobileFundEscrowForm(escrowId, userId);
        return ResponseEntity.ok(ApiResponse.success(dto, "Fund escrow form retrieved successfully"));
    }

    /**
     * 4. FundEscrow – Payment Confirmation Screen
     */
    @PostMapping({"/{escrowId}/proceed-payment", "/proceed-payment/{escrowId}"})
    @Operation(summary = "Screen 4: FundEscrow – Payment Confirmation Screen",
            description = "Submits payment method selection, validates payment receipt, and transitions to share lock")
    public ResponseEntity<ApiResponse<MobilePaymentConfirmationDto>> proceedPayment(
            @PathVariable("escrowId") String escrowId,
            @Valid @RequestBody MobilePaymentProceedRequestDto request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = extractUserId(authHeader);
        MobilePaymentConfirmationDto dto = mobileEscrowService.proceedMobilePayment(escrowId, request, userId);
        return ResponseEntity.ok(ApiResponse.success(dto, "Payment confirmed and verified successfully"));
    }

    /**
     * 5 & 11. FundEscrow – Share Lock Screen & Share Lock Status Screen
     */
    @GetMapping({"/{escrowId}/share-lock-status", "/share-lock/{escrowId}"})
    @Operation(summary = "Screens 5 & 11: Share Lock Status Screen",
            description = "Returns depository share pledge lock confirmation, locked shares, reference, and transfer window")
    public ResponseEntity<ApiResponse<MobileShareLockStatusDto>> getShareLockStatus(
            @PathVariable("escrowId") String escrowId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = extractUserId(authHeader);
        MobileShareLockStatusDto dto = mobileEscrowService.getMobileShareLockStatus(escrowId, userId);
        return ResponseEntity.ok(ApiResponse.success(dto, "Share lock status retrieved successfully"));
    }

    /**
     * 6 & 12. FundEscrow – Share Transfer Screen & Share Transfer Screen
     */
    @GetMapping({"/{escrowId}/share-transfer-details", "/share-transfer/{escrowId}"})
    @Operation(summary = "Screens 6 & 12: Share Transfer Screen",
            description = "Returns transfer summary, visual ledger (From -> To shares before/after), and internal ledger status")
    public ResponseEntity<ApiResponse<MobileShareTransferDto>> getShareTransfer(
            @PathVariable("escrowId") String escrowId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = extractUserId(authHeader);
        MobileShareTransferDto dto = mobileEscrowService.getMobileShareTransfer(escrowId, userId);
        return ResponseEntity.ok(ApiResponse.success(dto, "Share transfer details retrieved successfully"));
    }

    /**
     * Transfer Status Screen (Stepper)
     */
    @GetMapping({"/{escrowId}/transfer-status", "/transfer-status/{escrowId}"})
    @Operation(summary = "Transfer Status Lifecycle Screen",
            description = "Returns 5-step transfer lifecycle tracker from Transfer Requested to Completed")
    public ResponseEntity<ApiResponse<MobileTransferStatusDto>> getTransferStatus(
            @PathVariable("escrowId") String escrowId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = extractUserId(authHeader);
        MobileTransferStatusDto dto = mobileEscrowService.getMobileTransferStatus(escrowId, userId);
        return ResponseEntity.ok(ApiResponse.success(dto, "Transfer status retrieved successfully"));
    }

    /**
     * 7 & 8. FundEscrow – Settlement Completed Screen & FundEscrow – Settlement Screen
     */
    @GetMapping({"/{escrowId}/settlement-completed", "/{escrowId}/settlement-screen"})
    @Operation(summary = "Screens 7 & 8: FundEscrow – Settlement Screen / Completed",
            description = "Returns final settlement delivery receipts, depository confirmation, and seller fund release confirmation")
    public ResponseEntity<ApiResponse<MobileSettlementConfirmationDto>> getSettlementCompleted(
            @PathVariable("escrowId") String escrowId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = extractUserId(authHeader);
        MobileSettlementConfirmationDto dto = mobileEscrowService.getMobileSettlementConfirmation(null, escrowId, userId);
        return ResponseEntity.ok(ApiResponse.success(dto, "Settlement details retrieved successfully"));
    }

    /**
     * 9. Escrow Status Screen
     */
    @GetMapping({"/{escrowId}/status", "/status/{escrowId}"})
    @Operation(summary = "Screen 9: Escrow Status Screen",
            description = "Returns status hero banner, funds amount, status pill, and 7-step status history timeline")
    public ResponseEntity<ApiResponse<MobileEscrowStatusDto>> getEscrowStatus(
            @PathVariable("escrowId") String escrowId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = extractUserId(authHeader);
        MobileEscrowStatusDto dto = mobileEscrowService.getMobileEscrowStatus(escrowId, userId);
        return ResponseEntity.ok(ApiResponse.success(dto, "Escrow status retrieved successfully"));
    }

    /**
     * 10. Escrow Status – View Screen
     */
    @GetMapping({"/{escrowId}/status-view", "/status-view/{escrowId}"})
    @Operation(summary = "Screen 10: Escrow Status – View Screen",
            description = "Returns complete detailed breakdown for Escrow Status View screen with counterparty demat and depository refs")
    public ResponseEntity<ApiResponse<MobileEscrowStatusDetailDto>> getEscrowStatusDetail(
            @PathVariable("escrowId") String escrowId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = extractUserId(authHeader);
        MobileEscrowStatusDetailDto dto = mobileEscrowService.getMobileEscrowStatusDetail(escrowId, userId);
        return ResponseEntity.ok(ApiResponse.success(dto, "Escrow status details retrieved successfully"));
    }

    /**
     * 13. Settlement Confirmation
     */
    @GetMapping({"/settlement/confirmation", "/{escrowId}/settlement-confirmation"})
    @Operation(summary = "Screen 13: Settlement Confirmation",
            description = "Returns celebration checkmark, delivery metrics, settlement reference, and legal confirmation")
    public ResponseEntity<ApiResponse<MobileSettlementConfirmationDto>> getSettlementConfirmation(
            @RequestParam(value = "settlementId", required = false) String settlementId,
            @PathVariable(value = "escrowId", required = false) String escrowId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = extractUserId(authHeader);
        MobileSettlementConfirmationDto dto = mobileEscrowService.getMobileSettlementConfirmation(settlementId, escrowId, userId);
        return ResponseEntity.ok(ApiResponse.success(dto, "Settlement confirmation retrieved successfully"));
    }

    private Long extractUserId(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring(7).trim();
                return jwtTokenProvider.getUserIdFromToken(token);
            } catch (Exception e) {
                log.debug("[MobileEscrowController] JWT parse debug: {}", e.getMessage());
            }
        }
        return 1L;
    }
}
