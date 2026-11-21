package com.atirath.trading.modules.negotiation.controller;

import com.atirath.trading.common.ApiResponse;
import com.atirath.trading.config.JwtTokenProvider;
import com.atirath.trading.modules.negotiation.dto.NegotiationMessageRequestDto;
import com.atirath.trading.modules.negotiation.dto.mobile.*;
import com.atirath.trading.modules.negotiation.service.MobileNegotiationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Dedicated REST Controller delivering lightweight, mobile-optimized responses for Flutter negotiation screens:
 * 1. Negotiation Dashboard
 * 2. Negotiation Details
 * 3. Counter Offer Sheet / Screen
 * 4. Negotiation History
 * 5. Trade Agreement
 * 6. Negotiation Notifications
 */
@Slf4j
@RestController
@RequestMapping({"/api/v1/negotiations/mobile", "/api/v1/mobile/negotiations"})
@RequiredArgsConstructor
@Tag(name = "Mobile Negotiation API", description = "Screen-specific lightweight APIs for Flutter negotiation flows")
public class MobileNegotiationController {

    private final MobileNegotiationService mobileNegotiationService;
    private final JwtTokenProvider jwtTokenProvider;

    private Long extractUserId(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring(7).trim();
                return jwtTokenProvider.getUserIdFromToken(token);
            } catch (Exception e) {
                log.debug("[MobileNegotiationController] Could not parse JWT: {}", e.getMessage());
            }
        }
        return null;
    }

    // =========================================================================
    // SCREEN 1: NEGOTIATION DASHBOARD
    // =========================================================================
    @GetMapping("/dashboard")
    @Operation(summary = "Screen 1: Mobile Negotiation Dashboard",
            description = "Returns compact summary metric counters, quick tabs, and lightweight cards list for Flutter negotiation dashboard")
    public ResponseEntity<ApiResponse<MobileNegotiationDashboardDto>> getDashboard(
            @Parameter(description = "Optional tab: ALL, ACTIVE, AWAITING_RESPONSE, ACCEPTED, HISTORY")
            @RequestParam(value = "tab", required = false, defaultValue = "ALL") String tab,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = extractUserId(authHeader);
        MobileNegotiationDashboardDto dashboard = mobileNegotiationService.getMobileDashboard(tab, userId);
        return ResponseEntity.ok(ApiResponse.success(dashboard, "Mobile negotiation dashboard data retrieved successfully"));
    }

    // =========================================================================
    // SCREEN 2: NEGOTIATION DETAILS
    // =========================================================================
    @GetMapping("/{negotiationNo}")
    @Operation(summary = "Screen 2: Mobile Negotiation Details",
            description = "Returns company summary, CMP, parties, current offer terms, and action capability flags")
    public ResponseEntity<ApiResponse<MobileNegotiationDetailDto>> getNegotiationDetail(
            @PathVariable("negotiationNo") String negotiationNo,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = extractUserId(authHeader);
        MobileNegotiationDetailDto detail = mobileNegotiationService.getMobileNegotiationDetail(negotiationNo, userId);
        return ResponseEntity.ok(ApiResponse.success(detail, "Mobile negotiation details retrieved successfully"));
    }

    // =========================================================================
    // SCREEN 3: COUNTER OFFER (PREFILL & SUBMIT)
    // =========================================================================
    @GetMapping("/{negotiationNo}/counter-offer")
    @Operation(summary = "Screen 3: Mobile Counter Offer Form Pre-fill",
            description = "Pre-populates Counter Offer sheet with stepper bounds, current price, quantity, and settlement terms")
    public ResponseEntity<ApiResponse<MobileCounterOfferFormDto>> getCounterOfferForm(
            @PathVariable("negotiationNo") String negotiationNo,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = extractUserId(authHeader);
        MobileCounterOfferFormDto form = mobileNegotiationService.getMobileCounterOfferForm(negotiationNo, userId);
        return ResponseEntity.ok(ApiResponse.success(form, "Mobile counter offer form prefilled successfully"));
    }

    @PostMapping("/{negotiationNo}/counter-offer")
    @Operation(summary = "Screen 3: Submit Mobile Counter Offer",
            description = "Submits revised price, quantity, settlement terms and returns toast confirmation")
    public ResponseEntity<ApiResponse<MobileCounterOfferResultDto>> submitCounterOffer(
            @PathVariable("negotiationNo") String negotiationNo,
            @Valid @RequestBody MobileCounterOfferRequestDto request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = extractUserId(authHeader);
        MobileCounterOfferResultDto result = mobileNegotiationService.submitMobileCounterOffer(negotiationNo, request, userId);
        return ResponseEntity.ok(ApiResponse.success(result, result.getToastMessage()));
    }

    // =========================================================================
    // SCREEN 4: NEGOTIATION HISTORY & TIMELINE
    // =========================================================================
    @GetMapping("/{negotiationNo}/history")
    @Operation(summary = "Screen 4: Mobile Negotiation History Timeline",
            description = "Returns lightweight chronological events timeline of offers, counter-offers, and notes")
    public ResponseEntity<ApiResponse<MobileNegotiationHistoryDto>> getHistory(
            @PathVariable("negotiationNo") String negotiationNo,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = extractUserId(authHeader);
        MobileNegotiationHistoryDto history = mobileNegotiationService.getMobileNegotiationHistory(negotiationNo, userId);
        return ResponseEntity.ok(ApiResponse.success(history, "Mobile negotiation history retrieved successfully"));
    }

    @PostMapping("/{negotiationNo}/messages")
    @Operation(summary = "Screen 4: Post Timeline Message to Counterparty",
            description = "Sends a message in the mobile negotiation timeline")
    public ResponseEntity<ApiResponse<MobileTimelineEventDto>> postMessage(
            @PathVariable("negotiationNo") String negotiationNo,
            @RequestBody Map<String, String> payload,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = extractUserId(authHeader);
        String messageText = payload != null ? payload.getOrDefault("message", "") : "";
        MobileTimelineEventDto event = mobileNegotiationService.postMobileMessage(negotiationNo, messageText, userId);
        return ResponseEntity.ok(ApiResponse.success(event, "Message sent to counterparty"));
    }

    // =========================================================================
    // SCREEN 5: TRADE AGREEMENT
    // =========================================================================
    @GetMapping("/{negotiationNo}/trade-agreement")
    @Operation(summary = "Screen 5: Mobile Trade Agreement",
            description = "Returns agreed trade terms, legal disclaimer, and sign button status for Flutter Trade Agreement bottom sheet")
    public ResponseEntity<ApiResponse<MobileTradeAgreementDto>> getTradeAgreement(
            @PathVariable("negotiationNo") String negotiationNo,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = extractUserId(authHeader);
        MobileTradeAgreementDto agreement = mobileNegotiationService.getMobileTradeAgreement(negotiationNo, userId);
        return ResponseEntity.ok(ApiResponse.success(agreement, "Mobile trade agreement retrieved successfully"));
    }

    @PostMapping("/{negotiationNo}/trade-agreement/confirm")
    @Operation(summary = "Screen 5: Sign & Proceed to Escrow",
            description = "Signs and confirms trade agreement and initiates escrow transfer")
    public ResponseEntity<ApiResponse<MobileTradeAgreementDto>> confirmAgreement(
            @PathVariable("negotiationNo") String negotiationNo,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = extractUserId(authHeader);
        MobileTradeAgreementDto agreement = mobileNegotiationService.confirmMobileAgreement(negotiationNo, userId);
        return ResponseEntity.ok(ApiResponse.success(agreement, "Trade agreement signed successfully. Moving to Escrow."));
    }

    // =========================================================================
    // SCREEN 6: NEGOTIATION NOTIFICATIONS
    // =========================================================================
    @GetMapping("/notifications")
    @Operation(summary = "Screen 6: Mobile Negotiation Notifications",
            description = "Returns unread count and lightweight notification cards feed for Flutter notifications")
    public ResponseEntity<ApiResponse<MobileNegotiationNotificationsDto>> getNotifications(
            @RequestParam(value = "category", required = false, defaultValue = "ALL") String category,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = extractUserId(authHeader);
        MobileNegotiationNotificationsDto dto = mobileNegotiationService.getMobileNotifications(category, userId);
        return ResponseEntity.ok(ApiResponse.success(dto, "Mobile negotiation notifications retrieved successfully"));
    }

    @PostMapping("/notifications/{id}/read")
    @Operation(summary = "Screen 6: Mark Specific Notification as Read",
            description = "Marks a specific negotiation notification as read")
    public ResponseEntity<ApiResponse<Boolean>> markNotificationAsRead(
            @PathVariable("id") Long id) {

        boolean success = mobileNegotiationService.markNotificationRead(id);
        return ResponseEntity.ok(ApiResponse.success(success, "Notification marked as read"));
    }

    @PostMapping("/notifications/read-all")
    @Operation(summary = "Screen 6: Mark All Notifications as Read",
            description = "Marks all negotiation notifications as read")
    public ResponseEntity<ApiResponse<Integer>> markAllNotificationsAsRead() {
        int count = mobileNegotiationService.markAllNotificationsRead();
        return ResponseEntity.ok(ApiResponse.success(count, "All notifications marked as read"));
    }
}
