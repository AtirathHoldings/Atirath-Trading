package com.atirath.trading.modules.negotiation.controller;

import com.atirath.trading.common.ApiResponse;
import com.atirath.trading.config.JwtTokenProvider;
import com.atirath.trading.modules.negotiation.dto.*;
import com.atirath.trading.modules.negotiation.service.NegotiationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/negotiations")
@RequiredArgsConstructor
@Tag(name = "Negotiations & Trade Agreements", description = "APIs for Negotiation Dashboard, Counter Offers, Negotiation Timeline & Trade Agreements")
public class NegotiationController {

    private final NegotiationService negotiationService;
    private final JwtTokenProvider jwtTokenProvider;

    private Long extractUserId(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring(7).trim();
                return jwtTokenProvider.getUserIdFromToken(token);
            } catch (Exception e) {
                log.debug("[NegotiationController] Could not parse JWT: {}", e.getMessage());
            }
        }
        return null;
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get Negotiation Dashboard",
            description = "Returns summary KPI counts, monthly insights bar chart, status breakdown donut chart, quick actions, and paginated negotiations list with tab filters")
    public ResponseEntity<ApiResponse<NegotiationDashboardDto>> getDashboard(
            @RequestParam(value = "tab", required = false, defaultValue = "ALL") String tab,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "6") int size,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = extractUserId(authHeader);
        NegotiationDashboardDto dashboard = negotiationService.getDashboard(tab, search, page, size, userId);
        return ResponseEntity.ok(ApiResponse.success(dashboard, "Negotiation dashboard data retrieved successfully"));
    }

    @GetMapping("/notifications")
    @Operation(summary = "Get Negotiation Notifications Page",
            description = "Returns KPI metric cards, tab counts, notifications list, and selected notification summary")
    public ResponseEntity<ApiResponse<NegotiationNotificationsPageDto>> getNotifications(
            @RequestParam(value = "category", required = false, defaultValue = "ALL") String category,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "selectedId", required = false) Long selectedId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = extractUserId(authHeader);
        NegotiationNotificationsPageDto page = negotiationService.getNotificationsPage(category, search, selectedId, userId);
        return ResponseEntity.ok(ApiResponse.success(page, "Negotiation notifications retrieved successfully"));
    }

    @GetMapping("/notifications/{id}")
    @Operation(summary = "Get Negotiation Notification By ID",
            description = "Returns detailed notification item and negotiation summary")
    public ResponseEntity<ApiResponse<NegotiationNotificationItemDto>> getNotificationById(
            @PathVariable("id") Long id) {

        NegotiationNotificationItemDto notification = negotiationService.getNotificationById(id);
        return ResponseEntity.ok(ApiResponse.success(notification, "Notification retrieved successfully"));
    }

    @PostMapping("/notifications/{id}/mark-read")
    @Operation(summary = "Mark Notification as Read",
            description = "Marks a specific notification as read")
    public ResponseEntity<ApiResponse<NegotiationNotificationItemDto>> markNotificationAsRead(
            @PathVariable("id") Long id) {

        NegotiationNotificationItemDto notification = negotiationService.markNotificationAsRead(id);
        return ResponseEntity.ok(ApiResponse.success(notification, "Notification marked as read"));
    }

    @PostMapping("/notifications/{id}/mark-unread")
    @Operation(summary = "Mark Notification as Unread",
            description = "Marks a specific notification as unread")
    public ResponseEntity<ApiResponse<NegotiationNotificationItemDto>> markNotificationAsUnread(
            @PathVariable("id") Long id) {

        NegotiationNotificationItemDto notification = negotiationService.markNotificationAsUnread(id);
        return ResponseEntity.ok(ApiResponse.success(notification, "Notification marked as unread"));
    }

    @PostMapping("/notifications/mark-all-read")
    @Operation(summary = "Mark All Notifications as Read",
            description = "Marks all notifications as read")
    public ResponseEntity<ApiResponse<String>> markAllNotificationsAsRead() {
        negotiationService.markAllNotificationsAsRead();
        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read", "All notifications marked as read"));
    }

    @GetMapping("/{negotiationNo}")
    @Operation(summary = "Get Negotiation Details",
            description = "Returns full negotiation detail: Buyer/Seller verified info, Company ISIN/LTP/52W ranges, Terms comparison table (Buyer vs Seller vs Current), and timeline")
    public ResponseEntity<ApiResponse<NegotiationDetailDto>> getNegotiationDetail(
            @PathVariable("negotiationNo") String negotiationNo,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = extractUserId(authHeader);
        NegotiationDetailDto detail = negotiationService.getNegotiationDetail(negotiationNo, userId);
        return ResponseEntity.ok(ApiResponse.success(detail, "Negotiation details retrieved successfully"));
    }

    @PostMapping("/{negotiationNo}/counter-offer")
    @Operation(summary = "Send Counter Offer",
            description = "Submits revised counter offer (price per share, quantity, timeline, payment mode, other terms) and updates negotiation proposal terms")
    public ResponseEntity<ApiResponse<NegotiationDetailDto>> submitCounterOffer(
            @PathVariable("negotiationNo") String negotiationNo,
            @Valid @RequestBody CounterOfferRequestDto request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = extractUserId(authHeader);
        NegotiationDetailDto updated = negotiationService.submitCounterOffer(negotiationNo, request, userId);
        return ResponseEntity.ok(ApiResponse.success(updated, "Counter offer submitted successfully to counterparty"));
    }

    @PostMapping("/{negotiationNo}/accept")
    @Operation(summary = "Accept Offer",
            description = "Accepts the latest offer and transitions negotiation to Trade Agreement Ready state")
    public ResponseEntity<ApiResponse<NegotiationDetailDto>> acceptOffer(
            @PathVariable("negotiationNo") String negotiationNo,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = extractUserId(authHeader);
        NegotiationDetailDto accepted = negotiationService.acceptOffer(negotiationNo, userId);
        return ResponseEntity.ok(ApiResponse.success(accepted, "Offer accepted successfully. Proceed to Trade Agreement."));
    }

    @PostMapping("/{negotiationNo}/reject")
    @Operation(summary = "Reject Negotiation",
            description = "Declines / rejects the ongoing negotiation")
    public ResponseEntity<ApiResponse<NegotiationDetailDto>> rejectOffer(
            @PathVariable("negotiationNo") String negotiationNo,
            @RequestParam(value = "reason", required = false) String reason,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = extractUserId(authHeader);
        NegotiationDetailDto rejected = negotiationService.rejectOffer(negotiationNo, reason, userId);
        return ResponseEntity.ok(ApiResponse.success(rejected, "Negotiation has been declined"));
    }

    @PostMapping("/{negotiationNo}/request-call")
    @Operation(summary = "Request Discussion Call",
            description = "Records a call scheduling request with the counterparty in the negotiation timeline")
    public ResponseEntity<ApiResponse<NegotiationDetailDto>> requestCall(
            @PathVariable("negotiationNo") String negotiationNo,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = extractUserId(authHeader);
        NegotiationDetailDto updated = negotiationService.requestCall(negotiationNo, userId);
        return ResponseEntity.ok(ApiResponse.success(updated, "Call request sent to counterparty"));
    }

    @GetMapping("/{negotiationNo}/history")
    @Operation(summary = "Get Negotiation History & Messages",
            description = "Returns complete timeline of offers, counter-offers and communication feed with counterparty comparison")
    public ResponseEntity<ApiResponse<NegotiationHistoryDto>> getHistory(
            @PathVariable("negotiationNo") String negotiationNo,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = extractUserId(authHeader);
        NegotiationHistoryDto history = negotiationService.getHistory(negotiationNo, userId);
        return ResponseEntity.ok(ApiResponse.success(history, "Negotiation history retrieved successfully"));
    }

    @PostMapping("/{negotiationNo}/messages")
    @Operation(summary = "Send Message to Counterparty",
            description = "Sends a message or communication note to counterparty recorded for compliance")
    public ResponseEntity<ApiResponse<NegotiationHistoryDto>> postMessage(
            @PathVariable("negotiationNo") String negotiationNo,
            @Valid @RequestBody NegotiationMessageRequestDto request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = extractUserId(authHeader);
        NegotiationHistoryDto history = negotiationService.postMessage(negotiationNo, request, userId);
        return ResponseEntity.ok(ApiResponse.success(history, "Message dispatched to counterparty"));
    }

    @GetMapping("/{negotiationNo}/trade-agreement")
    @Operation(summary = "Get Trade Agreement",
            description = "Returns final agreed terms, downloadable agreement document metadata, checklist confirmations, and next steps")
    public ResponseEntity<ApiResponse<TradeAgreementDto>> getTradeAgreement(
            @PathVariable("negotiationNo") String negotiationNo,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = extractUserId(authHeader);
        TradeAgreementDto agreement = negotiationService.getTradeAgreement(negotiationNo, userId);
        return ResponseEntity.ok(ApiResponse.success(agreement, "Trade agreement retrieved successfully"));
    }

    @PostMapping("/{negotiationNo}/trade-agreement/confirm")
    @Operation(summary = "Confirm Trade Agreement & Proceed",
            description = "Confirms legal agreement checklist and moves trade to Escrow settlement pipeline")
    public ResponseEntity<ApiResponse<TradeAgreementDto>> confirmAgreement(
            @PathVariable("negotiationNo") String negotiationNo,
            @RequestBody TradeAgreementConfirmRequestDto request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = extractUserId(authHeader);
        TradeAgreementDto confirmed = negotiationService.confirmAgreement(negotiationNo, request, userId);
        return ResponseEntity.ok(ApiResponse.success(confirmed, "Trade agreement confirmed. Escrow transfer initiated."));
    }

    @GetMapping("/{negotiationNo}/trade-agreement/download")
    @Operation(summary = "Download Trade Agreement Document",
            description = "Downloads official PDF / legal trade agreement document")
    public ResponseEntity<byte[]> downloadAgreementPdf(
            @PathVariable("negotiationNo") String negotiationNo) {

        byte[] pdfBytes = negotiationService.downloadAgreementPdf(negotiationNo);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"Trade_Agreement_" + negotiationNo + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @GetMapping("/{negotiationNo}/summary/download")
    @Operation(summary = "Download Negotiation Summary PDF",
            description = "Downloads summary PDF of current negotiation terms")
    public ResponseEntity<byte[]> downloadSummaryPdf(
            @PathVariable("negotiationNo") String negotiationNo) {

        byte[] pdfBytes = negotiationService.downloadSummaryPdf(negotiationNo);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"Negotiation_Summary_" + negotiationNo + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @GetMapping("/{negotiationNo}/history/download")
    @Operation(summary = "Download Negotiation History PDF",
            description = "Downloads complete timeline of offers and messages as PDF")
    public ResponseEntity<byte[]> downloadHistoryPdf(
            @PathVariable("negotiationNo") String negotiationNo) {

        byte[] pdfBytes = negotiationService.downloadHistoryPdf(negotiationNo);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"Negotiation_History_" + negotiationNo + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @PostMapping("/initiate")
    @Operation(summary = "Start New Negotiation",
            description = "Initiates a new negotiation session with a counterparty for a specific company stock")
    public ResponseEntity<ApiResponse<NegotiationListItemDto>> initiateNegotiation(
            @Valid @RequestBody InitiateNegotiationRequestDto request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = extractUserId(authHeader);
        NegotiationListItemDto item = negotiationService.initiateNegotiation(request, userId);
        return ResponseEntity.ok(ApiResponse.success(item, "New negotiation initiated successfully"));
    }
}
