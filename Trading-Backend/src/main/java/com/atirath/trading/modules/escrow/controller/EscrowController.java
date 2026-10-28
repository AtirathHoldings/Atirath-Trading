package com.atirath.trading.modules.escrow.controller;

import com.atirath.trading.common.ApiResponse;
import com.atirath.trading.modules.escrow.dto.*;
import com.atirath.trading.modules.escrow.service.EscrowService;
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
@RequestMapping("/api/v1/escrow")
@RequiredArgsConstructor
@Tag(name = "Escrow & Settlement", description = "APIs for Escrow Dashboard, Transaction Tracking, Settlements and Funding Details")
public class EscrowController {

    private final EscrowService escrowService;

    @GetMapping("/dashboard")
    @Operation(summary = "Get Escrow Dashboard",
            description = "Returns KPI metric cards (Total: 24, Pending Funding: 6, Funds in Escrow: ₹ 1,24,50,000, Shares Locked: 45,000, Completed: 12, Failed/Cancelled: 2), Donut status breakdown, Settlement Trend bar chart, Escrow Value line chart, and paginated transactions")
    public ResponseEntity<ApiResponse<EscrowDashboardDto>> getEscrowDashboard(
            @RequestParam(value = "tab", required = false, defaultValue = "ALL") String tab,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "8") int size) {

        EscrowDashboardDto dashboard = escrowService.getEscrowDashboard(tab, search, page, size);
        return ResponseEntity.ok(ApiResponse.success(dashboard, "Escrow dashboard retrieved successfully"));
    }

    @GetMapping("/{escrowId}")
    @Operation(summary = "Get Escrow Transaction Details",
            description = "Returns complete escrow transaction details: Buyer/Seller profiles, Trade & Escrow terms, Bank Escrow Account Details, 5-Step Stepper Progress, Legal Documents, and Activity Timeline")
    public ResponseEntity<ApiResponse<EscrowDetailDto>> getEscrowDetail(
            @PathVariable("escrowId") String escrowId) {

        EscrowDetailDto detail = escrowService.getEscrowDetail(escrowId);
        return ResponseEntity.ok(ApiResponse.success(detail, "Escrow details retrieved successfully"));
    }

    @PostMapping("/create")
    @Operation(summary = "Create New Escrow Transaction",
            description = "Initiates a new escrow transaction for trade settlement")
    public ResponseEntity<ApiResponse<EscrowListItemDto>> createEscrow(
            @Valid @RequestBody CreateEscrowRequestDto request) {

        EscrowListItemDto item = escrowService.createEscrow(request);
        return ResponseEntity.ok(ApiResponse.success(item, "Escrow transaction created successfully"));
    }

    @PostMapping("/{escrowId}/transfer-funds")
    @Operation(summary = "Transfer Funds to Escrow",
            description = "Submits deposit confirmation for escrow funding, transitioning status to Funds in Escrow")
    public ResponseEntity<ApiResponse<EscrowDetailDto>> transferFunds(
            @PathVariable("escrowId") String escrowId,
            @Valid @RequestBody TransferFundsRequestDto request) {

        EscrowDetailDto detail = escrowService.transferFunds(escrowId, request);
        return ResponseEntity.ok(ApiResponse.success(detail, "Funds transferred to escrow successfully. Verification in progress."));
    }

    @GetMapping("/{escrowId}/documents/{docType}")
    @Operation(summary = "Download Escrow Document",
            description = "Downloads official legal document (trade-agreement, terms, or shareholding-proof)")
    public ResponseEntity<byte[]> downloadDocument(
            @PathVariable("escrowId") String escrowId,
            @PathVariable("docType") String docType) {

        byte[] pdfBytes = escrowService.downloadDocument(escrowId, docType);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + docType + "_" + escrowId + ".pdf\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(pdfBytes);
    }

    @GetMapping("/{escrowId}/pipeline")
    @Operation(summary = "Get Escrow Pipeline / Wizard Stage",
            description = "Returns current or specific stage data for the 5-step escrow settlement pipeline (FUND_ESCROW, PAYMENT_VERIFICATION, SHARE_LOCK, SHARE_TRANSFER, COMPLETED)")
    public ResponseEntity<ApiResponse<EscrowPipelineDto>> getEscrowPipeline(
            @PathVariable("escrowId") String escrowId,
            @RequestParam(value = "stage", required = false) String stage) {

        EscrowPipelineDto pipeline = escrowService.getEscrowPipeline(escrowId, stage);
        return ResponseEntity.ok(ApiResponse.success(pipeline, "Escrow pipeline data retrieved successfully"));
    }

    @PostMapping("/{escrowId}/proceed-payment")
    @Operation(summary = "Proceed to Payment Verification (Image 9 -> Image 10)",
            description = "Selects payment method (UPI, Net Banking, Debit Card) and initiates payment gateway verification")
    public ResponseEntity<ApiResponse<EscrowPipelineDto>> proceedPayment(
            @PathVariable("escrowId") String escrowId,
            @Valid @RequestBody ProceedPaymentRequestDto request) {

        EscrowPipelineDto pipeline = escrowService.proceedPayment(escrowId, request);
        return ResponseEntity.ok(ApiResponse.success(pipeline, "Payment initiated. Gateway verification in progress."));
    }

    @PostMapping("/{escrowId}/verify-payment")
    @Operation(summary = "Confirm Payment & Advance to Share Lock (Image 10 -> Image 11)",
            description = "Confirms payment gateway receipt and triggers seller share lock with depository")
    public ResponseEntity<ApiResponse<EscrowPipelineDto>> verifyPayment(
            @PathVariable("escrowId") String escrowId) {

        EscrowPipelineDto pipeline = escrowService.verifyPayment(escrowId);
        return ResponseEntity.ok(ApiResponse.success(pipeline, "Payment verified. Share lock initiated with depository."));
    }

    @PostMapping("/{escrowId}/lock-shares")
    @Operation(summary = "Confirm Share Lock & Advance to Share Transfer (Image 11 -> Image 12)",
            description = "Confirms depository share pledge lock and triggers off-market share transfer to buyer demat")
    public ResponseEntity<ApiResponse<EscrowPipelineDto>> lockShares(
            @PathVariable("escrowId") String escrowId) {

        EscrowPipelineDto pipeline = escrowService.lockShares(escrowId);
        return ResponseEntity.ok(ApiResponse.success(pipeline, "Shares locked. Transfer to buyer demat initiated."));
    }

    @PostMapping("/{escrowId}/transfer-shares")
    @Operation(summary = "Confirm Share Transfer & Settle Escrow (Image 12 -> Image 13)",
            description = "Confirms NSDL/CDSL transfer completion and triggers final fund release payout to seller")
    public ResponseEntity<ApiResponse<EscrowPipelineDto>> transferShares(
            @PathVariable("escrowId") String escrowId) {

        EscrowPipelineDto pipeline = escrowService.transferShares(escrowId);
        return ResponseEntity.ok(ApiResponse.success(pipeline, "Share transfer completed. Escrow settlement finalized."));
    }

    @PostMapping("/{escrowId}/set-stage")
    @Operation(summary = "Set Pipeline Stage Directly",
            description = "Navigates directly to any stage: FUND_ESCROW, PAYMENT_VERIFICATION, SHARE_LOCK, SHARE_TRANSFER, SETTLEMENT_IN_PROGRESS, COMPLETED")
    public ResponseEntity<ApiResponse<EscrowPipelineDto>> setPipelineStage(
            @PathVariable("escrowId") String escrowId,
            @RequestParam("stage") String stage) {

        EscrowPipelineDto pipeline = escrowService.setPipelineStage(escrowId, stage);
        return ResponseEntity.ok(ApiResponse.success(pipeline, "Pipeline stage updated to " + stage));
    }

    @GetMapping("/status")
    @Operation(summary = "Get Escrow Status Page (Image 15)",
            description = "Returns KPI metric cards (Total: 12, Completed: 5 [41.7%], In Progress: 3 [25.0%], Pending: 2 [16.7%], Failed: 1 [8.3%], Cancelled: 1 [8.3%]), tab filters, paginated table, and selected escrow details")
    public ResponseEntity<ApiResponse<EscrowStatusPageDto>> getEscrowStatusPage(
            @RequestParam(value = "tab", required = false, defaultValue = "All Escrows") String tab,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "8") int size,
            @RequestParam(value = "selectedId", required = false) String selectedId) {

        EscrowStatusPageDto result = escrowService.getEscrowStatusPage(tab, status, search, page, size, selectedId);
        return ResponseEntity.ok(ApiResponse.success(result, "Escrow status page retrieved successfully"));
    }

    @GetMapping("/status/{escrowId}")
    @Operation(summary = "Get View Escrow Details (Image 16)",
            description = "Returns full escrow status detail view including horizontal 5-step tracker, overview summary, tab navigation, quick actions, and downloadable documents")
    public ResponseEntity<ApiResponse<EscrowStatusDetailDto>> getEscrowStatusDetail(
            @PathVariable("escrowId") String escrowId,
            @RequestParam(value = "tab", required = false, defaultValue = "Overview") String tab) {

        EscrowStatusDetailDto result = escrowService.getEscrowStatusDetail(escrowId, tab);
        return ResponseEntity.ok(ApiResponse.success(result, "Escrow status details retrieved successfully"));
    }

    @GetMapping("/share-locks")
    @Operation(summary = "Get Share Lock Status Page (Image 17)",
            description = "Returns KPI metric cards (Total: 8, Active: 5 [62.5%], Released: 2 [25.0%], Expiring Soon: 1 [12.5%], Failed: 0), tab filters, paginated locks table, and selected lock details with progress timeline")
    public ResponseEntity<ApiResponse<ShareLockStatusPageDto>> getShareLockStatusPage(
            @RequestParam(value = "tab", required = false, defaultValue = "All Locks") String tab,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "8") int size,
            @RequestParam(value = "selectedId", required = false) String selectedId) {

        ShareLockStatusPageDto result = escrowService.getShareLockStatusPage(tab, status, search, page, size, selectedId);
        return ResponseEntity.ok(ApiResponse.success(result, "Share lock status page retrieved successfully"));
    }

    @GetMapping("/share-transfers")
    @Operation(summary = "Get Share Transfer Status Page (Image 18)",
            description = "Returns KPI metric cards (Total: 10, Completed: 6 [60.0%], In Progress: 3 [30.0%], Pending: 1 [10.0%], Failed: 0), tab filters, paginated transfers table, and selected transfer details with progress steps and documents")
    public ResponseEntity<ApiResponse<ShareTransferStatusPageDto>> getShareTransferStatusPage(
            @RequestParam(value = "tab", required = false, defaultValue = "All Transfers") String tab,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "8") int size,
            @RequestParam(value = "selectedId", required = false) String selectedId) {

        ShareTransferStatusPageDto result = escrowService.getShareTransferStatusPage(tab, status, search, page, size, selectedId);
        return ResponseEntity.ok(ApiResponse.success(result, "Share transfer status page retrieved successfully"));
    }

    @GetMapping("/settlement/confirmation")
    @Operation(summary = "Get Settlement Confirmation Details",
            description = "Returns complete settlement confirmation: 5-step completed stepper, settlement details (ISIN, clearing corp, references), celebration card with buyer/seller delivery metrics, and 4 downloadable legal settlement documents")
    public ResponseEntity<ApiResponse<SettlementConfirmationDto>> getSettlementConfirmationQuery(
            @RequestParam(value = "settlementId", required = false) String settlementId,
            @RequestParam(value = "escrowId", required = false) String escrowId) {

        SettlementConfirmationDto result = escrowService.getSettlementConfirmation(settlementId, escrowId);
        return ResponseEntity.ok(ApiResponse.success(result, "Settlement confirmation retrieved successfully"));
    }

    @GetMapping("/settlement/{settlementId}/confirmation")
    @Operation(summary = "Get Settlement Confirmation by Settlement ID",
            description = "Returns settlement confirmation screen data by settlementId")
    public ResponseEntity<ApiResponse<SettlementConfirmationDto>> getSettlementConfirmationBySettlementId(
            @PathVariable("settlementId") String settlementId) {

        SettlementConfirmationDto result = escrowService.getSettlementConfirmation(settlementId, null);
        return ResponseEntity.ok(ApiResponse.success(result, "Settlement confirmation retrieved successfully"));
    }

    @GetMapping("/{escrowId}/settlement-confirmation")
    @Operation(summary = "Get Settlement Confirmation by Escrow ID",
            description = "Returns settlement confirmation screen data by escrowId")
    public ResponseEntity<ApiResponse<SettlementConfirmationDto>> getSettlementConfirmationByEscrowId(
            @PathVariable("escrowId") String escrowId) {

        SettlementConfirmationDto result = escrowService.getSettlementConfirmation(null, escrowId);
        return ResponseEntity.ok(ApiResponse.success(result, "Settlement confirmation retrieved successfully"));
    }

    @GetMapping("/settlement/{settlementId}/documents/{docType}")
    @Operation(summary = "Download Settlement Legal Document",
            description = "Downloads settlement legal document: settlement-letter, share-transfer-proof, payment-release, escrow-closure")
    public ResponseEntity<byte[]> downloadSettlementDocument(
            @PathVariable("settlementId") String settlementId,
            @PathVariable("docType") String docType) {

        byte[] pdfBytes = escrowService.downloadSettlementDocument(settlementId, docType);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + docType + "_" + settlementId + ".pdf\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(pdfBytes);
    }
}


