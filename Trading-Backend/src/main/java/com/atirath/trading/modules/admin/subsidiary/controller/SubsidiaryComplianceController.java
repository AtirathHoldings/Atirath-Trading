package com.atirath.trading.modules.admin.subsidiary.controller;

import com.atirath.trading.common.ApiResponse;
import com.atirath.trading.modules.admin.subsidiary.dto.*;
import com.atirath.trading.modules.admin.subsidiary.service.SubsidiaryComplianceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/subsidiary/compliance")
@RequiredArgsConstructor
@Tag(name = "Subsidiary Admin Compliance", description = "Endpoints for Subsidiary Compliance: Dashboard, KYC, Alerts, Monitoring, Suspicious Txns, Restricted Users, Restricted Shares, Cases")
public class SubsidiaryComplianceController {

    private final SubsidiaryComplianceService complianceService;

    // ==========================================
    // 1. COMPLIANCE DASHBOARD
    // ==========================================
    @GetMapping({"", "/dashboard"})
    @Operation(summary = "Get Compliance Dashboard overview, subsidiary compliance status, KYC & cases donuts, and recent alerts")
    public ResponseEntity<ApiResponse<SubsidiaryComplianceDashboardDto>> getComplianceDashboard(
            @RequestParam(required = false, defaultValue = "Atirath Holdings") String company,
            @RequestParam(required = false, defaultValue = "01 Apr 2026 - 30 Sep 2026") String dateRange) {
        log.info("GET /api/v1/admin/subsidiary/compliance/dashboard requested (company={}, dateRange={})", company, dateRange);
        SubsidiaryComplianceDashboardDto dashboard = complianceService.getComplianceDashboard(company, dateRange);
        return ResponseEntity.ok(ApiResponse.success(dashboard, "Compliance dashboard retrieved successfully"));
    }

    // ==========================================
    // 2. KYC REVIEW
    // ==========================================
    @GetMapping("/kyc")
    @Operation(summary = "Get KYC review requests list, entity-type tabs, KYC KPIs, and filters")
    public ResponseEntity<ApiResponse<SubsidiaryKycReviewPageDto>> getKycReview(
            @RequestParam(required = false, defaultValue = "Atirath Holdings") String company,
            @RequestParam(required = false) String tab,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String riskLevel) {
        log.info("GET /api/v1/admin/subsidiary/compliance/kyc requested (tab={}, query={}, entityType={}, status={}, riskLevel={})",
                tab, query, entityType, status, riskLevel);
        SubsidiaryKycReviewPageDto page = complianceService.getKycReviewPage(company, tab, query, entityType, status, riskLevel);
        return ResponseEntity.ok(ApiResponse.success(page, "KYC review requests retrieved successfully"));
    }

    @GetMapping("/kyc/{id}")
    @Operation(summary = "Get KYC details drawer for individual/corporate investor including identity, risk, and documents")
    public ResponseEntity<ApiResponse<KycDetailDto>> getKycDetail(@PathVariable String id) {
        log.info("GET /api/v1/admin/subsidiary/compliance/kyc/{} requested", id);
        KycDetailDto detail = complianceService.getKycDetail(id);
        return ResponseEntity.ok(ApiResponse.success(detail, "KYC detail retrieved successfully"));
    }

    @PostMapping("/kyc")
    @Operation(summary = "Submit a new KYC request")
    public ResponseEntity<ApiResponse<KycDetailDto>> createKycRequest(@RequestBody CreateKycRequestDto request) {
        log.info("POST /api/v1/admin/subsidiary/compliance/kyc requested for: {}", request.getFullName());
        KycDetailDto created = complianceService.createKycRequest(request);
        return ResponseEntity.ok(ApiResponse.success(created, "KYC request created successfully"));
    }

    @PutMapping("/kyc/{id}/status")
    @Operation(summary = "Update KYC status (Verified, Rejected, Under Review, Pending)")
    public ResponseEntity<ApiResponse<KycDetailDto>> updateKycStatus(
            @PathVariable String id,
            @RequestParam String status) {
        log.info("PUT /api/v1/admin/subsidiary/compliance/kyc/{}/status requested with status: {}", id, status);
        KycDetailDto updated = complianceService.updateKycStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success(updated, "KYC status updated successfully"));
    }

    // ==========================================
    // 3. COMPLIANCE ALERTS
    // ==========================================
    @GetMapping("/alerts")
    @Operation(summary = "Get compliance alerts list, risk level breakdown, status tabs, and filters")
    public ResponseEntity<ApiResponse<SubsidiaryComplianceAlertsPageDto>> getComplianceAlerts(
            @RequestParam(required = false, defaultValue = "Atirath Holdings") String company,
            @RequestParam(required = false) String tab,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String riskLevel,
            @RequestParam(required = false) String status) {
        log.info("GET /api/v1/admin/subsidiary/compliance/alerts requested (tab={}, query={}, riskLevel={}, status={})",
                tab, query, riskLevel, status);
        SubsidiaryComplianceAlertsPageDto page = complianceService.getComplianceAlertsPage(company, tab, query, riskLevel, status);
        return ResponseEntity.ok(ApiResponse.success(page, "Compliance alerts retrieved successfully"));
    }

    @GetMapping("/alerts/{id}")
    @Operation(summary = "Get compliance alert details drawer including involved parties, detection source, and risk indicators")
    public ResponseEntity<ApiResponse<ComplianceAlertDetailDto>> getComplianceAlertDetail(@PathVariable String id) {
        log.info("GET /api/v1/admin/subsidiary/compliance/alerts/{} requested", id);
        ComplianceAlertDetailDto detail = complianceService.getComplianceAlertDetail(id);
        return ResponseEntity.ok(ApiResponse.success(detail, "Compliance alert detail retrieved successfully"));
    }

    @PostMapping("/alerts")
    @Operation(summary = "Create a manual or triggered compliance alert")
    public ResponseEntity<ApiResponse<ComplianceAlertDetailDto>> createComplianceAlert(@RequestBody CreateComplianceAlertRequestDto request) {
        log.info("POST /api/v1/admin/subsidiary/compliance/alerts requested for entity: {}", request.getEntity());
        ComplianceAlertDetailDto created = complianceService.createComplianceAlert(request);
        return ResponseEntity.ok(ApiResponse.success(created, "Compliance alert created successfully"));
    }

    @PutMapping("/alerts/{id}/dismiss")
    @Operation(summary = "Dismiss a compliance alert")
    public ResponseEntity<ApiResponse<ComplianceAlertDetailDto>> dismissComplianceAlert(@PathVariable String id) {
        log.info("PUT /api/v1/admin/subsidiary/compliance/alerts/{}/dismiss requested", id);
        ComplianceAlertDetailDto dismissed = complianceService.dismissComplianceAlert(id);
        return ResponseEntity.ok(ApiResponse.success(dismissed, "Compliance alert dismissed successfully"));
    }

    @PutMapping("/alerts/{id}/status")
    @Operation(summary = "Update compliance alert status (Open, In Progress, Investigating, Resolved, Dismissed)")
    public ResponseEntity<ApiResponse<ComplianceAlertDetailDto>> updateAlertStatus(
            @PathVariable String id,
            @RequestParam String status) {
        log.info("PUT /api/v1/admin/subsidiary/compliance/alerts/{}/status requested with status: {}", id, status);
        ComplianceAlertDetailDto updated = complianceService.updateAlertStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success(updated, "Compliance alert status updated successfully"));
    }

    // ==========================================
    // 4. TRANSACTION MONITORING
    // ==========================================
    @GetMapping("/transactions")
    @Operation(summary = "Get transaction monitoring page, volume trends, transaction types donut, and monitored transactions list")
    public ResponseEntity<ApiResponse<SubsidiaryTransactionMonitoringPageDto>> getTransactionMonitoring(
            @RequestParam(required = false, defaultValue = "Atirath Holdings") String company,
            @RequestParam(required = false) String tab,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String transactionType,
            @RequestParam(required = false) String riskLevel,
            @RequestParam(required = false) String status) {
        log.info("GET /api/v1/admin/subsidiary/compliance/transactions requested (tab={}, query={}, transactionType={}, riskLevel={}, status={})",
                tab, query, transactionType, riskLevel, status);
        SubsidiaryTransactionMonitoringPageDto page = complianceService.getTransactionMonitoringPage(company, tab, query, transactionType, riskLevel, status);
        return ResponseEntity.ok(ApiResponse.success(page, "Monitored transactions retrieved successfully"));
    }

    @GetMapping("/transactions/{id}")
    @Operation(summary = "Get monitored transaction details drawer, screening summary (PEP, Sanctions, Adverse Media), and risk notes")
    public ResponseEntity<ApiResponse<MonitoredTransactionDetailDto>> getMonitoredTransactionDetail(@PathVariable String id) {
        log.info("GET /api/v1/admin/subsidiary/compliance/transactions/{} requested", id);
        MonitoredTransactionDetailDto detail = complianceService.getMonitoredTransactionDetail(id);
        return ResponseEntity.ok(ApiResponse.success(detail, "Monitored transaction detail retrieved successfully"));
    }

    @PostMapping("/transactions/run-screening")
    @Operation(summary = "Run automated AML / sanctions batch screening on transactions")
    public ResponseEntity<ApiResponse<Map<String, Object>>> runScreening(@RequestBody(required = false) RunTransactionScreeningRequestDto request) {
        log.info("POST /api/v1/admin/subsidiary/compliance/transactions/run-screening requested");
        Map<String, Object> result = complianceService.runScreening(request);
        return ResponseEntity.ok(ApiResponse.success(result, "Transaction screening executed successfully"));
    }

    @PutMapping("/transactions/{id}/clear")
    @Operation(summary = "Mark a flagged transaction as Cleared after compliance audit")
    public ResponseEntity<ApiResponse<MonitoredTransactionDetailDto>> clearTransaction(@PathVariable String id) {
        log.info("PUT /api/v1/admin/subsidiary/compliance/transactions/{}/clear requested", id);
        MonitoredTransactionDetailDto cleared = complianceService.clearTransaction(id);
        return ResponseEntity.ok(ApiResponse.success(cleared, "Transaction marked as cleared successfully"));
    }

    // ==========================================
    // 5. SUSPICIOUS TRANSACTIONS
    // ==========================================
    @GetMapping("/suspicious-transactions")
    @Operation(summary = "Get suspicious transactions list, monthly trend, alert type distribution donut, and drawer detail")
    public ResponseEntity<ApiResponse<SuspiciousTransactionsPageDto>> getSuspiciousTransactions(
            @RequestParam(required = false, defaultValue = "Atirath Holdings") String company,
            @RequestParam(required = false) String tab,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String riskLevel,
            @RequestParam(required = false) String alertType,
            @RequestParam(required = false) String status) {
        log.info("GET /api/v1/admin/subsidiary/compliance/suspicious-transactions requested (tab={}, query={}, riskLevel={}, alertType={}, status={})",
                tab, query, riskLevel, alertType, status);
        SuspiciousTransactionsPageDto page = complianceService.getSuspiciousTransactionsPage(company, tab, query, riskLevel, alertType, status);
        return ResponseEntity.ok(ApiResponse.success(page, "Suspicious transactions retrieved successfully"));
    }

    @GetMapping("/suspicious-transactions/{id}")
    @Operation(summary = "Get detailed suspicious transaction drawer with confidence score and detection rule")
    public ResponseEntity<ApiResponse<SuspiciousTransactionDetailDto>> getSuspiciousTransactionDetail(@PathVariable String id) {
        log.info("GET /api/v1/admin/subsidiary/compliance/suspicious-transactions/{} requested", id);
        SuspiciousTransactionDetailDto detail = complianceService.getSuspiciousTransactionDetail(id);
        return ResponseEntity.ok(ApiResponse.success(detail, "Suspicious transaction detail retrieved successfully"));
    }

    @PostMapping("/suspicious-transactions")
    @Operation(summary = "Flag a transaction as suspicious for compliance investigation")
    public ResponseEntity<ApiResponse<SuspiciousTransactionDetailDto>> flagSuspiciousTransaction(@RequestBody FlagSuspiciousTransactionRequestDto request) {
        log.info("POST /api/v1/admin/subsidiary/compliance/suspicious-transactions requested for investor: {}", request.getInvestorOrShareholder());
        SuspiciousTransactionDetailDto created = complianceService.flagSuspiciousTransaction(request);
        return ResponseEntity.ok(ApiResponse.success(created, "Suspicious transaction flagged successfully"));
    }

    @PutMapping("/suspicious-transactions/{id}/status")
    @Operation(summary = "Update suspicious transaction investigation status")
    public ResponseEntity<ApiResponse<SuspiciousTransactionDetailDto>> updateSuspiciousTransactionStatus(
            @PathVariable String id,
            @RequestParam String status) {
        log.info("PUT /api/v1/admin/subsidiary/compliance/suspicious-transactions/{}/status requested with status: {}", id, status);
        SuspiciousTransactionDetailDto updated = complianceService.updateSuspiciousTransactionStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success(updated, "Suspicious transaction status updated successfully"));
    }

    // ==========================================
    // 6. RESTRICTED USERS
    // ==========================================
    @GetMapping("/restricted-users")
    @Operation(summary = "Get restricted users list, temporary vs permanent restriction KPIs, and filterable tabs")
    public ResponseEntity<ApiResponse<RestrictedUsersPageDto>> getRestrictedUsers(
            @RequestParam(required = false, defaultValue = "Atirath Holdings") String company,
            @RequestParam(required = false) String tab,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String entity,
            @RequestParam(required = false) String restrictionType,
            @RequestParam(required = false) String status) {
        log.info("GET /api/v1/admin/subsidiary/compliance/restricted-users requested (tab={}, query={}, entity={}, restrictionType={}, status={})",
                tab, query, entity, restrictionType, status);
        RestrictedUsersPageDto page = complianceService.getRestrictedUsersPage(company, tab, query, entity, restrictionType, status);
        return ResponseEntity.ok(ApiResponse.success(page, "Restricted users retrieved successfully"));
    }

    @GetMapping("/restricted-users/{id}")
    @Operation(summary = "Get detailed user restriction specification, reason, duration, and KYC/sanctions status")
    public ResponseEntity<ApiResponse<RestrictedUserDetailDto>> getRestrictedUserDetail(@PathVariable String id) {
        log.info("GET /api/v1/admin/subsidiary/compliance/restricted-users/{} requested", id);
        RestrictedUserDetailDto detail = complianceService.getRestrictedUserDetail(id);
        return ResponseEntity.ok(ApiResponse.success(detail, "Restricted user detail retrieved successfully"));
    }

    @PostMapping("/restricted-users")
    @Operation(summary = "Apply a new trading, corporate action, or system access restriction on a user")
    public ResponseEntity<ApiResponse<RestrictedUserDetailDto>> restrictUser(@RequestBody RestrictUserRequestDto request) {
        log.info("POST /api/v1/admin/subsidiary/compliance/restricted-users requested for: {}", request.getFullName());
        RestrictedUserDetailDto created = complianceService.restrictUser(request);
        return ResponseEntity.ok(ApiResponse.success(created, "User restriction applied successfully"));
    }

    @PutMapping("/restricted-users/{id}/lift")
    @Operation(summary = "Lift restriction and reinstate user account")
    public ResponseEntity<ApiResponse<RestrictedUserDetailDto>> liftUserRestriction(@PathVariable String id) {
        log.info("PUT /api/v1/admin/subsidiary/compliance/restricted-users/{}/lift requested", id);
        RestrictedUserDetailDto reinstated = complianceService.liftUserRestriction(id);
        return ResponseEntity.ok(ApiResponse.success(reinstated, "User restriction lifted successfully"));
    }

    // ==========================================
    // 7. RESTRICTED SHARES
    // ==========================================
    @GetMapping("/restricted-shares")
    @Operation(summary = "Get restricted shares list, lock-in/hold breakdown donut, and expiry timeline chart")
    public ResponseEntity<ApiResponse<RestrictedSharesPageDto>> getRestrictedShares(
            @RequestParam(required = false, defaultValue = "Atirath Holdings") String company,
            @RequestParam(required = false) String tab,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String entity,
            @RequestParam(required = false) String shareClass,
            @RequestParam(required = false) String restrictionType,
            @RequestParam(required = false) String status) {
        log.info("GET /api/v1/admin/subsidiary/compliance/restricted-shares requested (tab={}, query={}, entity={}, shareClass={}, restrictionType={}, status={})",
                tab, query, entity, shareClass, restrictionType, status);
        RestrictedSharesPageDto page = complianceService.getRestrictedSharesPage(company, tab, query, entity, shareClass, restrictionType, status);
        return ResponseEntity.ok(ApiResponse.success(page, "Restricted shares retrieved successfully"));
    }

    @GetMapping("/restricted-shares/{id}")
    @Operation(summary = "Get detailed restricted share certificate metadata, lock-in reason, and shareholder info")
    public ResponseEntity<ApiResponse<RestrictedShareDetailDto>> getRestrictedShareDetail(@PathVariable String id) {
        log.info("GET /api/v1/admin/subsidiary/compliance/restricted-shares/{} requested", id);
        RestrictedShareDetailDto detail = complianceService.getRestrictedShareDetail(id);
        return ResponseEntity.ok(ApiResponse.success(detail, "Restricted share detail retrieved successfully"));
    }

    @PostMapping("/restricted-shares")
    @Operation(summary = "Impose a trading or lock-in restriction on shares")
    public ResponseEntity<ApiResponse<RestrictedShareDetailDto>> restrictShares(@RequestBody RestrictSharesRequestDto request) {
        log.info("POST /api/v1/admin/subsidiary/compliance/restricted-shares requested for shareholder: {}", request.getShareholder());
        RestrictedShareDetailDto created = complianceService.restrictShares(request);
        return ResponseEntity.ok(ApiResponse.success(created, "Share restriction applied successfully"));
    }

    @PutMapping("/restricted-shares/{id}/lift")
    @Operation(summary = "Lift restriction on shares")
    public ResponseEntity<ApiResponse<RestrictedShareDetailDto>> liftShareRestriction(@PathVariable String id) {
        log.info("PUT /api/v1/admin/subsidiary/compliance/restricted-shares/{}/lift requested", id);
        RestrictedShareDetailDto lifted = complianceService.liftShareRestriction(id);
        return ResponseEntity.ok(ApiResponse.success(lifted, "Share restriction lifted successfully"));
    }

    // ==========================================
    // 8. COMPLIANCE CASES
    // ==========================================
    @GetMapping("/cases")
    @Operation(summary = "Get compliance cases list, cases by type donut, and case status trend chart")
    public ResponseEntity<ApiResponse<ComplianceCasesPageDto>> getComplianceCases(
            @RequestParam(required = false, defaultValue = "Atirath Holdings") String company,
            @RequestParam(required = false) String tab,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String entity,
            @RequestParam(required = false) String caseType,
            @RequestParam(required = false) String riskLevel,
            @RequestParam(required = false) String status) {
        log.info("GET /api/v1/admin/subsidiary/compliance/cases requested (tab={}, query={}, entity={}, caseType={}, riskLevel={}, status={})",
                tab, query, entity, caseType, riskLevel, status);
        ComplianceCasesPageDto page = complianceService.getComplianceCasesPage(company, tab, query, entity, caseType, riskLevel, status);
        return ResponseEntity.ok(ApiResponse.success(page, "Compliance cases retrieved successfully"));
    }

    @GetMapping("/cases/{id}")
    @Operation(summary = "Get detailed compliance case drawer including investigation summary, owner, and risk indicators")
    public ResponseEntity<ApiResponse<ComplianceCaseDetailDto>> getComplianceCaseDetail(@PathVariable String id) {
        log.info("GET /api/v1/admin/subsidiary/compliance/cases/{} requested", id);
        ComplianceCaseDetailDto detail = complianceService.getComplianceCaseDetail(id);
        return ResponseEntity.ok(ApiResponse.success(detail, "Compliance case detail retrieved successfully"));
    }

    @PostMapping("/cases")
    @Operation(summary = "Create a new formal compliance investigation case")
    public ResponseEntity<ApiResponse<ComplianceCaseDetailDto>> createComplianceCase(@RequestBody CreateComplianceCaseRequestDto request) {
        log.info("POST /api/v1/admin/subsidiary/compliance/cases requested with title: {}", request.getTitle());
        ComplianceCaseDetailDto created = complianceService.createComplianceCase(request);
        return ResponseEntity.ok(ApiResponse.success(created, "Compliance case created successfully"));
    }

    @PutMapping("/cases/{id}/status")
    @Operation(summary = "Update compliance case status (Open, In Progress, Under Review, Resolved, Closed)")
    public ResponseEntity<ApiResponse<ComplianceCaseDetailDto>> updateComplianceCaseStatus(
            @PathVariable String id,
            @RequestParam String status) {
        log.info("PUT /api/v1/admin/subsidiary/compliance/cases/{}/status requested with status: {}", id, status);
        ComplianceCaseDetailDto updated = complianceService.updateComplianceCaseStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success(updated, "Compliance case status updated successfully"));
    }
}
