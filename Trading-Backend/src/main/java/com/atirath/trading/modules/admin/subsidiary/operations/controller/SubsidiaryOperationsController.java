package com.atirath.trading.modules.admin.subsidiary.operations.controller;

import com.atirath.trading.common.ApiResponse;
import com.atirath.trading.modules.admin.subsidiary.operations.dto.*;
import com.atirath.trading.modules.admin.subsidiary.operations.service.SubsidiaryOperationsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/subsidiary/operations")
@RequiredArgsConstructor
@Tag(name = "Subsidiary Admin Operations", description = "Endpoints for Operations: Dashboard, Pending Trades, Pending Transfers, Escrow Monitoring, and Settlement Monitoring")
public class SubsidiaryOperationsController {

    private final SubsidiaryOperationsService operationsService;

    // ==========================================
    // 1. OPERATIONS DASHBOARD
    // ==========================================
    @GetMapping({"", "/dashboard"})
    @Operation(summary = "Get Operations Dashboard KPIs, volume trend, status donut, recent trades/transfers, alerts, and monitoring widgets")
    public ResponseEntity<ApiResponse<OperationsDashboardDto>> getOperationsDashboard(
            @RequestParam(required = false, defaultValue = "Atirath Holdings") String company,
            @RequestParam(required = false, defaultValue = "01 Apr 2026 - 30 Sep 2026") String dateRange) {
        log.info("GET /api/v1/admin/subsidiary/operations/dashboard requested (company={}, dateRange={})", company, dateRange);
        OperationsDashboardDto dashboard = operationsService.getOperationsDashboard(company, dateRange);
        return ResponseEntity.ok(ApiResponse.success(dashboard, "Operations dashboard retrieved successfully"));
    }

    // ==========================================
    // 2. PENDING TRADES
    // ==========================================
    @GetMapping("/trades")
    @Operation(summary = "Get Pending Trades page data with KPIs, status tabs, filters, donut chart, volume trends, and trade table")
    public ResponseEntity<ApiResponse<PendingTradesPageDto>> getPendingTrades(
            @RequestParam(required = false, defaultValue = "Atirath Holdings") String company,
            @RequestParam(required = false) String tab,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String shareClass,
            @RequestParam(required = false) String tradeType,
            @RequestParam(required = false) String status) {
        log.info("GET /api/v1/admin/subsidiary/operations/trades requested (tab={}, query={}, shareClass={}, tradeType={}, status={})",
                tab, query, shareClass, tradeType, status);
        PendingTradesPageDto page = operationsService.getPendingTradesPage(company, tab, query, shareClass, tradeType, status);
        return ResponseEntity.ok(ApiResponse.success(page, "Pending trades retrieved successfully"));
    }

    @GetMapping("/trades/{id}")
    @Operation(summary = "Get detailed Trade side drawer by ID")
    public ResponseEntity<ApiResponse<TradeDetailDto>> getTradeDetail(@PathVariable String id) {
        log.info("GET /api/v1/admin/subsidiary/operations/trades/{} requested", id);
        TradeDetailDto detail = operationsService.getTradeDetail(id);
        return ResponseEntity.ok(ApiResponse.success(detail, "Trade detail retrieved successfully"));
    }

    @PostMapping("/trades")
    @Operation(summary = "Create / Add a new Trade")
    public ResponseEntity<ApiResponse<TradeDetailDto>> addTrade(@RequestBody AddTradeRequestDto request) {
        log.info("POST /api/v1/admin/subsidiary/operations/trades requested for company={}, investor={}", request.getCompany(), request.getInvestor());
        TradeDetailDto created = operationsService.addTrade(request);
        return ResponseEntity.ok(ApiResponse.success(created, "Trade added successfully"));
    }

    @PutMapping("/trades/{id}/approve")
    @Operation(summary = "Approve trade approval workflow step")
    public ResponseEntity<ApiResponse<TradeDetailDto>> approveTrade(@PathVariable String id) {
        log.info("PUT /api/v1/admin/subsidiary/operations/trades/{}/approve requested", id);
        TradeDetailDto updated = operationsService.approveTrade(id);
        return ResponseEntity.ok(ApiResponse.success(updated, "Trade approved successfully"));
    }

    @PutMapping("/trades/{id}/reject")
    @Operation(summary = "Reject trade approval workflow step")
    public ResponseEntity<ApiResponse<TradeDetailDto>> rejectTrade(@PathVariable String id) {
        log.info("PUT /api/v1/admin/subsidiary/operations/trades/{}/reject requested", id);
        TradeDetailDto updated = operationsService.rejectTrade(id);
        return ResponseEntity.ok(ApiResponse.success(updated, "Trade rejected successfully"));
    }

    // ==========================================
    // 3. PENDING TRANSFERS
    // ==========================================
    @GetMapping("/transfers")
    @Operation(summary = "Get Pending Transfers page data with KPIs, status tabs, filters, donut chart, monthly trend, and transfers table")
    public ResponseEntity<ApiResponse<PendingTransfersPageDto>> getPendingTransfers(
            @RequestParam(required = false, defaultValue = "Atirath Holdings") String company,
            @RequestParam(required = false) String tab,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String shareClass,
            @RequestParam(required = false) String transferType,
            @RequestParam(required = false) String status) {
        log.info("GET /api/v1/admin/subsidiary/operations/transfers requested (tab={}, query={}, shareClass={}, transferType={}, status={})",
                tab, query, shareClass, transferType, status);
        PendingTransfersPageDto page = operationsService.getPendingTransfersPage(company, tab, query, shareClass, transferType, status);
        return ResponseEntity.ok(ApiResponse.success(page, "Pending transfers retrieved successfully"));
    }

    @GetMapping("/transfers/{id}")
    @Operation(summary = "Get detailed Transfer side drawer by ID")
    public ResponseEntity<ApiResponse<TransferDetailDto>> getTransferDetail(@PathVariable String id) {
        log.info("GET /api/v1/admin/subsidiary/operations/transfers/{} requested", id);
        TransferDetailDto detail = operationsService.getTransferDetail(id);
        return ResponseEntity.ok(ApiResponse.success(detail, "Transfer detail retrieved successfully"));
    }

    @PostMapping("/transfers")
    @Operation(summary = "Initiate a new Share Transfer")
    public ResponseEntity<ApiResponse<TransferDetailDto>> initiateTransfer(@RequestBody InitiateTransferRequestDto request) {
        log.info("POST /api/v1/admin/subsidiary/operations/transfers requested from={}, to={}", request.getFromParty(), request.getToParty());
        TransferDetailDto created = operationsService.initiateTransfer(request);
        return ResponseEntity.ok(ApiResponse.success(created, "Transfer initiated successfully"));
    }

    @PutMapping("/transfers/{id}/approve")
    @Operation(summary = "Approve share transfer workflow step")
    public ResponseEntity<ApiResponse<TransferDetailDto>> approveTransfer(@PathVariable String id) {
        log.info("PUT /api/v1/admin/subsidiary/operations/transfers/{}/approve requested", id);
        TransferDetailDto updated = operationsService.approveTransfer(id);
        return ResponseEntity.ok(ApiResponse.success(updated, "Transfer approved successfully"));
    }

    @PutMapping("/transfers/{id}/reject")
    @Operation(summary = "Reject share transfer workflow step")
    public ResponseEntity<ApiResponse<TransferDetailDto>> rejectTransfer(@PathVariable String id) {
        log.info("PUT /api/v1/admin/subsidiary/operations/transfers/{}/reject requested", id);
        TransferDetailDto updated = operationsService.rejectTransfer(id);
        return ResponseEntity.ok(ApiResponse.success(updated, "Transfer rejected successfully"));
    }

    // ==========================================
    // 4. ESCROW MONITORING
    // ==========================================
    @GetMapping("/escrow")
    @Operation(summary = "Get Escrow Monitoring page with balance KPIs, status tabs, balance trends, escrow accounts table, upcoming releases, and exceptions")
    public ResponseEntity<ApiResponse<EscrowMonitoringPageDto>> getEscrowMonitoring(
            @RequestParam(required = false, defaultValue = "Atirath Holdings") String company,
            @RequestParam(required = false) String tab,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String shareClass,
            @RequestParam(required = false) String status) {
        log.info("GET /api/v1/admin/subsidiary/operations/escrow requested (tab={}, query={}, shareClass={}, status={})",
                tab, query, shareClass, status);
        EscrowMonitoringPageDto page = operationsService.getEscrowMonitoringPage(company, tab, query, shareClass, status);
        return ResponseEntity.ok(ApiResponse.success(page, "Escrow monitoring data retrieved successfully"));
    }

    @GetMapping("/escrow/{id}")
    @Operation(summary = "Get detailed Escrow account side drawer by ID")
    public ResponseEntity<ApiResponse<EscrowDetailDto>> getEscrowDetail(@PathVariable String id) {
        log.info("GET /api/v1/admin/subsidiary/operations/escrow/{} requested", id);
        EscrowDetailDto detail = operationsService.getEscrowDetail(id);
        return ResponseEntity.ok(ApiResponse.success(detail, "Escrow detail retrieved successfully"));
    }

    @PostMapping("/escrow")
    @Operation(summary = "Create a new Escrow account")
    public ResponseEntity<ApiResponse<EscrowDetailDto>> createEscrow(@RequestBody CreateEscrowRequestDto request) {
        log.info("POST /api/v1/admin/subsidiary/operations/escrow requested for company={}, investor={}", request.getCompany(), request.getInvestor());
        EscrowDetailDto created = operationsService.createEscrow(request);
        return ResponseEntity.ok(ApiResponse.success(created, "Escrow account created successfully"));
    }

    @PutMapping("/escrow/{id}/release")
    @Operation(summary = "Release funds for an Escrow account")
    public ResponseEntity<ApiResponse<EscrowDetailDto>> releaseEscrowFunds(@PathVariable String id) {
        log.info("PUT /api/v1/admin/subsidiary/operations/escrow/{}/release requested", id);
        EscrowDetailDto updated = operationsService.releaseEscrowFunds(id);
        return ResponseEntity.ok(ApiResponse.success(updated, "Escrow funds released successfully"));
    }

    @PutMapping("/escrow/{id}/hold")
    @Operation(summary = "Place funds on hold for an Escrow account")
    public ResponseEntity<ApiResponse<EscrowDetailDto>> placeEscrowHold(@PathVariable String id) {
        log.info("PUT /api/v1/admin/subsidiary/operations/escrow/{}/hold requested", id);
        EscrowDetailDto updated = operationsService.placeEscrowHold(id);
        return ResponseEntity.ok(ApiResponse.success(updated, "Escrow placed on hold successfully"));
    }

    // ==========================================
    // 5. SETTLEMENT MONITORING
    // ==========================================
    @GetMapping("/settlements")
    @Operation(summary = "Get Settlement Monitoring page with settlement KPIs, cycle tabs, monthly trend, settlement table, upcoming settlements, and failed settlements")
    public ResponseEntity<ApiResponse<SettlementMonitoringPageDto>> getSettlementMonitoring(
            @RequestParam(required = false, defaultValue = "Atirath Holdings") String company,
            @RequestParam(required = false) String tab,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String shareClass,
            @RequestParam(required = false) String status) {
        log.info("GET /api/v1/admin/subsidiary/operations/settlements requested (tab={}, query={}, shareClass={}, status={})",
                tab, query, shareClass, status);
        SettlementMonitoringPageDto page = operationsService.getSettlementMonitoringPage(company, tab, query, shareClass, status);
        return ResponseEntity.ok(ApiResponse.success(page, "Settlement monitoring data retrieved successfully"));
    }

    @GetMapping("/settlements/{id}")
    @Operation(summary = "Get detailed Settlement side drawer by ID")
    public ResponseEntity<ApiResponse<SettlementDetailDto>> getSettlementDetail(@PathVariable String id) {
        log.info("GET /api/v1/admin/subsidiary/operations/settlements/{} requested", id);
        SettlementDetailDto detail = operationsService.getSettlementDetail(id);
        return ResponseEntity.ok(ApiResponse.success(detail, "Settlement detail retrieved successfully"));
    }

    @PutMapping("/settlements/{id}/settle")
    @Operation(summary = "Mark settlement as Settled")
    public ResponseEntity<ApiResponse<SettlementDetailDto>> markSettlementSettled(@PathVariable String id) {
        log.info("PUT /api/v1/admin/subsidiary/operations/settlements/{}/settle requested", id);
        SettlementDetailDto updated = operationsService.markSettlementSettled(id);
        return ResponseEntity.ok(ApiResponse.success(updated, "Settlement marked as settled successfully"));
    }

    @PutMapping("/settlements/{id}/fail")
    @Operation(summary = "Mark settlement as Failed")
    public ResponseEntity<ApiResponse<SettlementDetailDto>> markSettlementFailed(@PathVariable String id) {
        log.info("PUT /api/v1/admin/subsidiary/operations/settlements/{}/fail requested", id);
        SettlementDetailDto updated = operationsService.markSettlementFailed(id);
        return ResponseEntity.ok(ApiResponse.success(updated, "Settlement marked as failed successfully"));
    }

    // ==========================================
    // 6. FAILED TRANSACTIONS
    // ==========================================
    @GetMapping("/failed-transactions")
    @Operation(summary = "Get Failed Transactions page data with KPIs, tabs, filters, donut chart, reasons chart, trend, and transactions table")
    public ResponseEntity<ApiResponse<FailedTransactionsPageDto>> getFailedTransactions(
            @RequestParam(required = false, defaultValue = "Atirath Holdings") String company,
            @RequestParam(required = false) String tab,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String transactionType,
            @RequestParam(required = false) String failureReason) {
        log.info("GET /api/v1/admin/subsidiary/operations/failed-transactions requested (tab={}, query={}, transactionType={}, failureReason={})",
                tab, query, transactionType, failureReason);
        FailedTransactionsPageDto page = operationsService.getFailedTransactionsPage(company, tab, query, transactionType, failureReason);
        return ResponseEntity.ok(ApiResponse.success(page, "Failed transactions retrieved successfully"));
    }

    @GetMapping("/failed-transactions/{id}")
    @Operation(summary = "Get detailed Failed Transaction side drawer by ID")
    public ResponseEntity<ApiResponse<FailedTransactionDetailDto>> getFailedTransactionDetail(@PathVariable String id) {
        log.info("GET /api/v1/admin/subsidiary/operations/failed-transactions/{} requested", id);
        FailedTransactionDetailDto detail = operationsService.getFailedTransactionDetail(id);
        return ResponseEntity.ok(ApiResponse.success(detail, "Failed transaction detail retrieved successfully"));
    }

    @PostMapping("/failed-transactions/{id}/retry")
    @Operation(summary = "Retry a failed transaction")
    public ResponseEntity<ApiResponse<FailedTransactionDetailDto>> retryFailedTransaction(@PathVariable String id) {
        log.info("POST /api/v1/admin/subsidiary/operations/failed-transactions/{}/retry requested", id);
        FailedTransactionDetailDto updated = operationsService.retryFailedTransaction(id);
        return ResponseEntity.ok(ApiResponse.success(updated, "Transaction retry scheduled successfully"));
    }

    @PutMapping("/failed-transactions/{id}/cancel")
    @Operation(summary = "Cancel a failed transaction")
    public ResponseEntity<ApiResponse<FailedTransactionDetailDto>> cancelFailedTransaction(@PathVariable String id) {
        log.info("PUT /api/v1/admin/subsidiary/operations/failed-transactions/{}/cancel requested", id);
        FailedTransactionDetailDto updated = operationsService.cancelFailedTransaction(id);
        return ResponseEntity.ok(ApiResponse.success(updated, "Transaction cancelled successfully"));
    }

    // ==========================================
    // 7. RECONCILIATION
    // ==========================================
    @GetMapping("/reconciliation")
    @Operation(summary = "Get Reconciliation page with KPIs, status tabs, trends, status distribution, record table, variance chart, and exceptions")
    public ResponseEntity<ApiResponse<ReconciliationPageDto>> getReconciliation(
            @RequestParam(required = false, defaultValue = "Atirath Holdings") String company,
            @RequestParam(required = false) String tab,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String recordType,
            @RequestParam(required = false) String status) {
        log.info("GET /api/v1/admin/subsidiary/operations/reconciliation requested (tab={}, query={}, recordType={}, status={})",
                tab, query, recordType, status);
        ReconciliationPageDto page = operationsService.getReconciliationPage(company, tab, query, recordType, status);
        return ResponseEntity.ok(ApiResponse.success(page, "Reconciliation data retrieved successfully"));
    }

    @GetMapping("/reconciliation/{id}")
    @Operation(summary = "Get detailed Reconciliation side drawer by ID")
    public ResponseEntity<ApiResponse<ReconciliationDetailDto>> getReconciliationDetail(@PathVariable String id) {
        log.info("GET /api/v1/admin/subsidiary/operations/reconciliation/{} requested", id);
        ReconciliationDetailDto detail = operationsService.getReconciliationDetail(id);
        return ResponseEntity.ok(ApiResponse.success(detail, "Reconciliation detail retrieved successfully"));
    }

    @PostMapping("/reconciliation/run")
    @Operation(summary = "Run reconciliation process")
    public ResponseEntity<ApiResponse<ReconciliationDetailDto>> runReconciliation(@RequestBody RunReconciliationRequestDto request) {
        log.info("POST /api/v1/admin/subsidiary/operations/reconciliation/run requested for company={}, period={}", request.getCompany(), request.getPeriod());
        ReconciliationDetailDto created = operationsService.runReconciliation(request);
        return ResponseEntity.ok(ApiResponse.success(created, "Reconciliation process initiated successfully"));
    }

    @PutMapping("/reconciliation/{id}/approve")
    @Operation(summary = "Approve reconciliation")
    public ResponseEntity<ApiResponse<ReconciliationDetailDto>> approveReconciliation(@PathVariable String id) {
        log.info("PUT /api/v1/admin/subsidiary/operations/reconciliation/{}/approve requested", id);
        ReconciliationDetailDto updated = operationsService.approveReconciliation(id);
        return ResponseEntity.ok(ApiResponse.success(updated, "Reconciliation approved successfully"));
    }

    @PutMapping("/reconciliation/{id}/reject")
    @Operation(summary = "Reject reconciliation")
    public ResponseEntity<ApiResponse<ReconciliationDetailDto>> rejectReconciliation(@PathVariable String id) {
        log.info("PUT /api/v1/admin/subsidiary/operations/reconciliation/{}/reject requested", id);
        ReconciliationDetailDto updated = operationsService.rejectReconciliation(id);
        return ResponseEntity.ok(ApiResponse.success(updated, "Reconciliation rejected successfully"));
    }

    @PutMapping("/reconciliation/{id}/reviewed")
    @Operation(summary = "Mark reconciliation as reviewed")
    public ResponseEntity<ApiResponse<ReconciliationDetailDto>> markReconciliationReviewed(@PathVariable String id) {
        log.info("PUT /api/v1/admin/subsidiary/operations/reconciliation/{}/reviewed requested", id);
        ReconciliationDetailDto updated = operationsService.markReconciliationReviewed(id);
        return ResponseEntity.ok(ApiResponse.success(updated, "Reconciliation marked as reviewed successfully"));
    }

    // ==========================================
    // 8. DISPUTES
    // ==========================================
    @GetMapping("/disputes")
    @Operation(summary = "Get Disputes page with dispute KPIs, status tabs, donut chart, status bar chart, trend, and disputes table")
    public ResponseEntity<ApiResponse<DisputesPageDto>> getDisputes(
            @RequestParam(required = false, defaultValue = "Atirath Holdings") String company,
            @RequestParam(required = false) String tab,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String disputeType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority) {
        log.info("GET /api/v1/admin/subsidiary/operations/disputes requested (tab={}, query={}, disputeType={}, status={}, priority={})",
                tab, query, disputeType, status, priority);
        DisputesPageDto page = operationsService.getDisputesPage(company, tab, query, disputeType, status, priority);
        return ResponseEntity.ok(ApiResponse.success(page, "Disputes data retrieved successfully"));
    }

    @GetMapping("/disputes/{id}")
    @Operation(summary = "Get detailed Dispute side drawer by ID")
    public ResponseEntity<ApiResponse<DisputeDetailDto>> getDisputeDetail(@PathVariable String id) {
        log.info("GET /api/v1/admin/subsidiary/operations/disputes/{} requested", id);
        DisputeDetailDto detail = operationsService.getDisputeDetail(id);
        return ResponseEntity.ok(ApiResponse.success(detail, "Dispute detail retrieved successfully"));
    }

    @PostMapping("/disputes")
    @Operation(summary = "Raise a new Dispute")
    public ResponseEntity<ApiResponse<DisputeDetailDto>> raiseDispute(@RequestBody RaiseDisputeRequestDto request) {
        log.info("POST /api/v1/admin/subsidiary/operations/disputes requested for investor={}, amount={}", request.getInvestor(), request.getAmount());
        DisputeDetailDto created = operationsService.raiseDispute(request);
        return ResponseEntity.ok(ApiResponse.success(created, "Dispute raised successfully"));
    }

    @PutMapping("/disputes/{id}/resolve")
    @Operation(summary = "Resolve dispute")
    public ResponseEntity<ApiResponse<DisputeDetailDto>> resolveDispute(@PathVariable String id) {
        log.info("PUT /api/v1/admin/subsidiary/operations/disputes/{}/resolve requested", id);
        DisputeDetailDto updated = operationsService.resolveDispute(id);
        return ResponseEntity.ok(ApiResponse.success(updated, "Dispute resolved successfully"));
    }

    @PutMapping("/disputes/{id}/escalate")
    @Operation(summary = "Escalate dispute")
    public ResponseEntity<ApiResponse<DisputeDetailDto>> escalateDispute(@PathVariable String id) {
        log.info("PUT /api/v1/admin/subsidiary/operations/disputes/{}/escalate requested", id);
        DisputeDetailDto updated = operationsService.escalateDispute(id);
        return ResponseEntity.ok(ApiResponse.success(updated, "Dispute escalated successfully"));
    }

    // ==========================================
    // 9. SUPPORT TICKETS
    // ==========================================
    @GetMapping("/tickets")
    @Operation(summary = "Get Support Tickets page with KPIs, status tabs, category donut, status bar, trend, and tickets table")
    public ResponseEntity<ApiResponse<SupportTicketsPageDto>> getSupportTickets(
            @RequestParam(required = false, defaultValue = "Atirath Holdings") String company,
            @RequestParam(required = false) String tab,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String status) {
        log.info("GET /api/v1/admin/subsidiary/operations/tickets requested (tab={}, query={}, category={}, priority={}, status={})",
                tab, query, category, priority, status);
        SupportTicketsPageDto page = operationsService.getSupportTicketsPage(company, tab, query, category, priority, status);
        return ResponseEntity.ok(ApiResponse.success(page, "Support tickets retrieved successfully"));
    }

    @GetMapping("/tickets/{id}")
    @Operation(summary = "Get detailed Support Ticket side drawer by ID")
    public ResponseEntity<ApiResponse<SupportTicketDetailDto>> getSupportTicketDetail(@PathVariable String id) {
        log.info("GET /api/v1/admin/subsidiary/operations/tickets/{} requested", id);
        SupportTicketDetailDto detail = operationsService.getSupportTicketDetail(id);
        return ResponseEntity.ok(ApiResponse.success(detail, "Support ticket detail retrieved successfully"));
    }

    @PostMapping("/tickets")
    @Operation(summary = "Create a new Support Ticket")
    public ResponseEntity<ApiResponse<SupportTicketDetailDto>> createSupportTicket(@RequestBody CreateTicketRequestDto request) {
        log.info("POST /api/v1/admin/subsidiary/operations/tickets requested for subject={}, raisedBy={}", request.getSubject(), request.getRaisedBy());
        SupportTicketDetailDto created = operationsService.createSupportTicket(request);
        return ResponseEntity.ok(ApiResponse.success(created, "Support ticket created successfully"));
    }

    @PutMapping("/tickets/{id}/status")
    @Operation(summary = "Update ticket status")
    public ResponseEntity<ApiResponse<SupportTicketDetailDto>> updateTicketStatus(
            @PathVariable String id,
            @RequestParam(required = false, defaultValue = "Resolved") String status) {
        log.info("PUT /api/v1/admin/subsidiary/operations/tickets/{}/status requested with status={}", id, status);
        SupportTicketDetailDto updated = operationsService.updateTicketStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success(updated, "Ticket status updated successfully"));
    }

    @PutMapping("/tickets/{id}/assign")
    @Operation(summary = "Assign ticket to team member or group")
    public ResponseEntity<ApiResponse<SupportTicketDetailDto>> assignTicket(
            @PathVariable String id,
            @RequestParam(required = false, defaultValue = "Operations Team") String assignee) {
        log.info("PUT /api/v1/admin/subsidiary/operations/tickets/{}/assign requested with assignee={}", id, assignee);
        SupportTicketDetailDto updated = operationsService.assignTicket(id, assignee);
        return ResponseEntity.ok(ApiResponse.success(updated, "Ticket assigned successfully"));
    }
}
