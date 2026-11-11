package com.atirath.trading.modules.admin.subsidiary.controller;

import com.atirath.trading.common.ApiResponse;
import com.atirath.trading.modules.admin.subsidiary.dto.*;
import com.atirath.trading.modules.admin.subsidiary.service.SubsidiaryAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/subsidiary")
@RequiredArgsConstructor
@Tag(name = "Subsidiary Admin", description = "Endpoints for Subsidiary Administration: Dashboard, Company Profile, Share Classes, and Share Issuance")
public class SubsidiaryAdminController {

    private final SubsidiaryAdminService subsidiaryAdminService;

    // ==========================================
    // 1. SUBSIDIARY DASHBOARD
    // ==========================================
    @GetMapping("/dashboard")
    @Operation(summary = "Get Subsidiary Dashboard overview, performance overview table, project status, compliance, and milestones")
    public ResponseEntity<ApiResponse<SubsidiaryAdminDashboardDto>> getDashboard(
            @RequestParam(required = false) String subsidiary,
            @RequestParam(required = false) String dateRange) {
        log.info("GET /api/v1/admin/subsidiary/dashboard requested (subsidiary={}, dateRange={})", subsidiary, dateRange);
        SubsidiaryAdminDashboardDto dashboard = subsidiaryAdminService.getSubsidiaryDashboard(subsidiary, dateRange);
        return ResponseEntity.ok(ApiResponse.success(dashboard, "Subsidiary dashboard retrieved successfully"));
    }

    // ==========================================
    // 2. SUBSIDIARY COMPANY PROFILE
    // ==========================================
    @GetMapping("/company-profile")
    @Operation(summary = "Get detailed company profile, hero, shareholding pattern, key management, and financial summaries")
    public ResponseEntity<ApiResponse<SubsidiaryCompanyProfileDto>> getCompanyProfile(
            @RequestParam(required = false, defaultValue = "Atirath Holdings") String company) {
        log.info("GET /api/v1/admin/subsidiary/company-profile requested for company: {}", company);
        SubsidiaryCompanyProfileDto profile = subsidiaryAdminService.getCompanyProfile(company);
        return ResponseEntity.ok(ApiResponse.success(profile, "Subsidiary company profile retrieved successfully"));
    }

    @PutMapping("/company-profile")
    @Operation(summary = "Update subsidiary company details and registration info")
    public ResponseEntity<ApiResponse<SubsidiaryCompanyProfileDto>> updateCompanyProfile(
            @RequestParam(required = false, defaultValue = "Atirath Holdings") String company,
            @RequestBody UpdateSubsidiaryCompanyProfileDto request) {
        log.info("PUT /api/v1/admin/subsidiary/company-profile requested for company: {}", company);
        SubsidiaryCompanyProfileDto updated = subsidiaryAdminService.updateCompanyProfile(company, request);
        return ResponseEntity.ok(ApiResponse.success(updated, "Subsidiary company profile updated successfully"));
    }

    // ==========================================
    // 3. SHARE CLASSES
    // ==========================================
    @GetMapping("/share-classes")
    @Operation(summary = "Get share classes list, distribution charts, authorized vs issued bars, and investor categories")
    public ResponseEntity<ApiResponse<SubsidiaryShareClassesPageDto>> getShareClasses(
            @RequestParam(required = false, defaultValue = "Atirath Holdings") String company,
            @RequestParam(required = false) String tab,
            @RequestParam(required = false) String query) {
        log.info("GET /api/v1/admin/subsidiary/share-classes requested (company={}, tab={}, query={})", company, tab, query);
        SubsidiaryShareClassesPageDto page = subsidiaryAdminService.getShareClassesPage(company, tab, query);
        return ResponseEntity.ok(ApiResponse.success(page, "Share classes retrieved successfully"));
    }

    @GetMapping("/share-classes/{id}")
    @Operation(summary = "Get detailed share class specification including voting rights, dividend rights, and transferability")
    public ResponseEntity<ApiResponse<ShareClassDetailDto>> getShareClassDetail(@PathVariable String id) {
        log.info("GET /api/v1/admin/subsidiary/share-classes/{} requested", id);
        ShareClassDetailDto detail = subsidiaryAdminService.getShareClassDetail(id);
        return ResponseEntity.ok(ApiResponse.success(detail, "Share class detail retrieved successfully"));
    }

    @PostMapping("/share-classes")
    @Operation(summary = "Add a new share class to the subsidiary")
    public ResponseEntity<ApiResponse<ShareClassDetailDto>> addShareClass(@RequestBody AddShareClassRequestDto request) {
        log.info("POST /api/v1/admin/subsidiary/share-classes requested for class: {}", request.getClassName());
        ShareClassDetailDto created = subsidiaryAdminService.addShareClass(request);
        return ResponseEntity.ok(ApiResponse.success(created, "Share class added successfully"));
    }

    // ==========================================
    // 4. SHARE ISSUANCE
    // ==========================================
    @GetMapping("/share-issuance")
    @Operation(summary = "Get share issuance requests, issuance trend charts, recent approvals, and activity log")
    public ResponseEntity<ApiResponse<SubsidiaryShareIssuancePageDto>> getShareIssuance(
            @RequestParam(required = false, defaultValue = "Atirath Holdings") String company,
            @RequestParam(required = false) String tab,
            @RequestParam(required = false) String query) {
        log.info("GET /api/v1/admin/subsidiary/share-issuance requested (company={}, tab={}, query={})", company, tab, query);
        SubsidiaryShareIssuancePageDto page = subsidiaryAdminService.getShareIssuancePage(company, tab, query);
        return ResponseEntity.ok(ApiResponse.success(page, "Share issuances retrieved successfully"));
    }

    @PostMapping("/share-issuance")
    @Operation(summary = "Submit a new share issuance request")
    public ResponseEntity<ApiResponse<SubsidiaryShareIssuancePageDto.ShareIssuanceItemDto>> submitShareIssuance(
            @RequestBody SubmitShareIssuanceRequestDto request) {
        log.info("POST /api/v1/admin/subsidiary/share-issuance requested for share class: {}", request.getShareClass());
        SubsidiaryShareIssuancePageDto.ShareIssuanceItemDto created = subsidiaryAdminService.submitShareIssuance(request);
        return ResponseEntity.ok(ApiResponse.success(created, "Share issuance request submitted successfully"));
    }

    @PutMapping("/share-issuance/{requestId}/approve")
    @Operation(summary = "Approve a share issuance request")
    public ResponseEntity<ApiResponse<SubsidiaryShareIssuancePageDto.ShareIssuanceItemDto>> approveIssuance(
            @PathVariable String requestId) {
        log.info("PUT /api/v1/admin/subsidiary/share-issuance/{}/approve requested", requestId);
        SubsidiaryShareIssuancePageDto.ShareIssuanceItemDto approved = subsidiaryAdminService.approveIssuance(requestId);
        return ResponseEntity.ok(ApiResponse.success(approved, "Share issuance approved successfully"));
    }

    @PutMapping("/share-issuance/{requestId}/reject")
    @Operation(summary = "Reject a share issuance request")
    public ResponseEntity<ApiResponse<SubsidiaryShareIssuancePageDto.ShareIssuanceItemDto>> rejectIssuance(
            @PathVariable String requestId) {
        log.info("PUT /api/v1/admin/subsidiary/share-issuance/{}/reject requested", requestId);
        SubsidiaryShareIssuancePageDto.ShareIssuanceItemDto rejected = subsidiaryAdminService.rejectIssuance(requestId);
        return ResponseEntity.ok(ApiResponse.success(rejected, "Share issuance rejected successfully"));
    }

    // ==========================================
    // 5. SUBSIDIARY SHAREHOLDERS
    // ==========================================
    @GetMapping("/shareholders")
    @Operation(summary = "Get subsidiary shareholders list, categories, shareholding pattern, and top shareholders")
    public ResponseEntity<ApiResponse<SubsidiaryShareholdersPageDto>> getShareholders(
            @RequestParam(required = false, defaultValue = "Atirath Holdings") String company,
            @RequestParam(required = false) String tab,
            @RequestParam(required = false) String query) {
        log.info("GET /api/v1/admin/subsidiary/shareholders requested (company={}, tab={}, query={})", company, tab, query);
        SubsidiaryShareholdersPageDto page = subsidiaryAdminService.getSubsidiaryShareholdersPage(company, tab, query);
        return ResponseEntity.ok(ApiResponse.success(page, "Subsidiary shareholders retrieved successfully"));
    }

    @GetMapping("/shareholders/{id}")
    @Operation(summary = "Get detailed shareholder profile including PAN, country, address, and KYC status")
    public ResponseEntity<ApiResponse<ShareholderDetailDto>> getShareholderDetail(@PathVariable String id) {
        log.info("GET /api/v1/admin/subsidiary/shareholders/{} requested", id);
        ShareholderDetailDto detail = subsidiaryAdminService.getShareholderDetail(id);
        return ResponseEntity.ok(ApiResponse.success(detail, "Shareholder detail retrieved successfully"));
    }

    @PostMapping("/shareholders")
    @Operation(summary = "Add a new shareholder to the subsidiary")
    public ResponseEntity<ApiResponse<ShareholderDetailDto>> addShareholder(@RequestBody AddSubsidiaryShareholderRequestDto request) {
        log.info("POST /api/v1/admin/subsidiary/shareholders requested for: {}", request.getShareholderName());
        ShareholderDetailDto created = subsidiaryAdminService.addShareholder(request);
        return ResponseEntity.ok(ApiResponse.success(created, "Shareholder added successfully"));
    }

    // ==========================================
    // 6. CAP TABLE
    // ==========================================
    @GetMapping("/cap-table")
    @Operation(summary = "Get subsidiary cap table, ownership by share class/category, investment vs ownership, and recent transactions")
    public ResponseEntity<ApiResponse<SubsidiaryCapTablePageDto>> getCapTable(
            @RequestParam(required = false, defaultValue = "Atirath Holdings") String company,
            @RequestParam(required = false) String tab,
            @RequestParam(required = false) String query) {
        log.info("GET /api/v1/admin/subsidiary/cap-table requested (company={}, tab={}, query={})", company, tab, query);
        SubsidiaryCapTablePageDto page = subsidiaryAdminService.getCapTablePage(company, tab, query);
        return ResponseEntity.ok(ApiResponse.success(page, "Cap table retrieved successfully"));
    }

    @PostMapping("/cap-table/transactions")
    @Operation(summary = "Record a new issuance or transfer transaction on the cap table")
    public ResponseEntity<ApiResponse<SubsidiaryCapTablePageDto.CapTableTransactionDto>> addCapTableTransaction(
            @RequestBody AddCapTableTransactionRequestDto request) {
        log.info("POST /api/v1/admin/subsidiary/cap-table/transactions requested for: {}", request.getShareholderName());
        SubsidiaryCapTablePageDto.CapTableTransactionDto created = subsidiaryAdminService.addCapTableTransaction(request);
        return ResponseEntity.ok(ApiResponse.success(created, "Cap table transaction recorded successfully"));
    }

    @GetMapping("/cap-table/export")
    @Operation(summary = "Export subsidiary cap table statement")
    public ResponseEntity<ApiResponse<java.util.Map<String, String>>> exportCapTable(
            @RequestParam(required = false, defaultValue = "Atirath Holdings") String company) {
        log.info("GET /api/v1/admin/subsidiary/cap-table/export requested for company: {}", company);
        java.util.Map<String, String> exportInfo = subsidiaryAdminService.exportCapTable(company);
        return ResponseEntity.ok(ApiResponse.success(exportInfo, "Cap table export report generated successfully"));
    }

    // ==========================================
    // 7. SHARE TRANSFER RULES
    // ==========================================
    @GetMapping("/share-transfer-rules")
    @Operation(summary = "Get share transfer rules, restriction types, applicable classes, and transfer approval status")
    public ResponseEntity<ApiResponse<SubsidiaryShareTransferRulesPageDto>> getShareTransferRules(
            @RequestParam(required = false, defaultValue = "Atirath Holdings") String company,
            @RequestParam(required = false) String tab,
            @RequestParam(required = false) String query) {
        log.info("GET /api/v1/admin/subsidiary/share-transfer-rules requested (company={}, tab={}, query={})", company, tab, query);
        SubsidiaryShareTransferRulesPageDto page = subsidiaryAdminService.getShareTransferRulesPage(company, tab, query);
        return ResponseEntity.ok(ApiResponse.success(page, "Share transfer rules retrieved successfully"));
    }

    @GetMapping("/share-transfer-rules/{id}")
    @Operation(summary = "Get detailed share transfer rule conditions, approval workflow, and history")
    public ResponseEntity<ApiResponse<ShareTransferRuleDetailDto>> getShareTransferRuleDetail(@PathVariable String id) {
        log.info("GET /api/v1/admin/subsidiary/share-transfer-rules/{} requested", id);
        ShareTransferRuleDetailDto detail = subsidiaryAdminService.getShareTransferRuleDetail(id);
        return ResponseEntity.ok(ApiResponse.success(detail, "Share transfer rule detail retrieved successfully"));
    }

    @PostMapping("/share-transfer-rules")
    @Operation(summary = "Create a new share transfer rule")
    public ResponseEntity<ApiResponse<ShareTransferRuleDetailDto>> addShareTransferRule(@RequestBody AddShareTransferRuleRequestDto request) {
        log.info("POST /api/v1/admin/subsidiary/share-transfer-rules requested for: {}", request.getRuleName());
        ShareTransferRuleDetailDto created = subsidiaryAdminService.addShareTransferRule(request);
        return ResponseEntity.ok(ApiResponse.success(created, "Share transfer rule created successfully"));
    }

    // ==========================================
    // 8. TRADING RULES
    // ==========================================
    @GetMapping("/trading-rules")
    @Operation(summary = "Get subsidiary trading rules, order limits, trading windows, and compliance requirements")
    public ResponseEntity<ApiResponse<SubsidiaryTradingRulesPageDto>> getTradingRules(
            @RequestParam(required = false, defaultValue = "Atirath Holdings") String company,
            @RequestParam(required = false) String tab,
            @RequestParam(required = false) String query) {
        log.info("GET /api/v1/admin/subsidiary/trading-rules requested (company={}, tab={}, query={})", company, tab, query);
        SubsidiaryTradingRulesPageDto page = subsidiaryAdminService.getTradingRulesPage(company, tab, query);
        return ResponseEntity.ok(ApiResponse.success(page, "Trading rules retrieved successfully"));
    }

    @GetMapping("/trading-rules/{id}")
    @Operation(summary = "Get detailed trading rule specification, parameters, and time windows")
    public ResponseEntity<ApiResponse<TradingRuleDetailDto>> getTradingRuleDetail(@PathVariable String id) {
        log.info("GET /api/v1/admin/subsidiary/trading-rules/{} requested", id);
        TradingRuleDetailDto detail = subsidiaryAdminService.getTradingRuleDetail(id);
        return ResponseEntity.ok(ApiResponse.success(detail, "Trading rule detail retrieved successfully"));
    }

    @PostMapping("/trading-rules")
    @Operation(summary = "Create a new trading rule")
    public ResponseEntity<ApiResponse<TradingRuleDetailDto>> addTradingRule(@RequestBody AddTradingRuleRequestDto request) {
        log.info("POST /api/v1/admin/subsidiary/trading-rules requested for: {}", request.getRuleName());
        TradingRuleDetailDto created = subsidiaryAdminService.addTradingRule(request);
        return ResponseEntity.ok(ApiResponse.success(created, "Trading rule created successfully"));
    }

    @PutMapping("/trading-rules/{id}/toggle-status")
    @Operation(summary = "Toggle a trading rule status between Active and Suspended")
    public ResponseEntity<ApiResponse<TradingRuleDetailDto>> toggleTradingRuleStatus(@PathVariable String id) {
        log.info("PUT /api/v1/admin/subsidiary/trading-rules/{}/toggle-status requested", id);
        TradingRuleDetailDto updated = subsidiaryAdminService.toggleTradingRuleStatus(id);
        return ResponseEntity.ok(ApiResponse.success(updated, "Trading rule status updated successfully"));
    }

    // ==========================================
    // 9. COMPANY APPROVERS
    // ==========================================
    @GetMapping("/company-approvers")
    @Operation(summary = "Get company approvers, approval limits, workflow statistics, and department/approval-type distributions")
    public ResponseEntity<ApiResponse<SubsidiaryApproversPageDto>> getCompanyApprovers(
            @RequestParam(required = false, defaultValue = "Atirath Holdings") String company,
            @RequestParam(required = false) String tab,
            @RequestParam(required = false) String query) {
        log.info("GET /api/v1/admin/subsidiary/company-approvers requested (company={}, tab={}, query={})", company, tab, query);
        SubsidiaryApproversPageDto page = subsidiaryAdminService.getSubsidiaryApproversPage(company, tab, query);
        return ResponseEntity.ok(ApiResponse.success(page, "Company approvers retrieved successfully"));
    }

    @GetMapping("/company-approvers/{id}")
    @Operation(summary = "Get detailed approver profile including limits, types, and approval history")
    public ResponseEntity<ApiResponse<CompanyApproverDetailDto>> getCompanyApproverDetail(@PathVariable String id) {
        log.info("GET /api/v1/admin/subsidiary/company-approvers/{} requested", id);
        CompanyApproverDetailDto detail = subsidiaryAdminService.getCompanyApproverDetail(id);
        return ResponseEntity.ok(ApiResponse.success(detail, "Company approver detail retrieved successfully"));
    }

    @PostMapping("/company-approvers")
    @Operation(summary = "Add a new company approver authority")
    public ResponseEntity<ApiResponse<CompanyApproverDetailDto>> addCompanyApprover(@RequestBody AddCompanyApproverRequestDto request) {
        log.info("POST /api/v1/admin/subsidiary/company-approvers requested for: {}", request.getFullName());
        CompanyApproverDetailDto created = subsidiaryAdminService.addCompanyApprover(request);
        return ResponseEntity.ok(ApiResponse.success(created, "Company approver added successfully"));
    }

    @PutMapping("/company-approvers/{id}/toggle-status")
    @Operation(summary = "Toggle approver status between Active and Inactive")
    public ResponseEntity<ApiResponse<CompanyApproverDetailDto>> toggleCompanyApproverStatus(@PathVariable String id) {
        log.info("PUT /api/v1/admin/subsidiary/company-approvers/{}/toggle-status requested", id);
        CompanyApproverDetailDto updated = subsidiaryAdminService.toggleCompanyApproverStatus(id);
        return ResponseEntity.ok(ApiResponse.success(updated, "Company approver status updated successfully"));
    }

    // ==========================================
    // 10. COMPANY DOCUMENTS
    // ==========================================
    @GetMapping("/company-documents")
    @Operation(summary = "Get statutory, regulatory, and corporate documents, categories donut, and expiry timeline")
    public ResponseEntity<ApiResponse<SubsidiaryDocumentsPageDto>> getCompanyDocuments(
            @RequestParam(required = false, defaultValue = "Atirath Holdings") String company,
            @RequestParam(required = false) String tab,
            @RequestParam(required = false) String query) {
        log.info("GET /api/v1/admin/subsidiary/company-documents requested (company={}, tab={}, query={})", company, tab, query);
        SubsidiaryDocumentsPageDto page = subsidiaryAdminService.getSubsidiaryDocumentsPage(company, tab, query);
        return ResponseEntity.ok(ApiResponse.success(page, "Company documents retrieved successfully"));
    }

    @GetMapping("/company-documents/{id}")
    @Operation(summary = "Get detailed company document metadata, issuing authority, and download link")
    public ResponseEntity<ApiResponse<CompanyDocumentDetailDto>> getCompanyDocumentDetail(@PathVariable String id) {
        log.info("GET /api/v1/admin/subsidiary/company-documents/{} requested", id);
        CompanyDocumentDetailDto detail = subsidiaryAdminService.getCompanyDocumentDetail(id);
        return ResponseEntity.ok(ApiResponse.success(detail, "Company document detail retrieved successfully"));
    }

    @PostMapping("/company-documents")
    @Operation(summary = "Upload a new company document")
    public ResponseEntity<ApiResponse<CompanyDocumentDetailDto>> uploadCompanyDocument(@RequestBody UploadCompanyDocumentRequestDto request) {
        log.info("POST /api/v1/admin/subsidiary/company-documents requested for: {}", request.getDocumentName());
        CompanyDocumentDetailDto created = subsidiaryAdminService.uploadCompanyDocument(request);
        return ResponseEntity.ok(ApiResponse.success(created, "Company document uploaded successfully"));
    }

    @GetMapping("/company-documents/{id}/download")
    @Operation(summary = "Download company document file")
    public ResponseEntity<ApiResponse<java.util.Map<String, String>>> downloadCompanyDocument(@PathVariable String id) {
        log.info("GET /api/v1/admin/subsidiary/company-documents/{}/download requested", id);
        java.util.Map<String, String> downloadInfo = subsidiaryAdminService.downloadCompanyDocument(id);
        return ResponseEntity.ok(ApiResponse.success(downloadInfo, "Document download link generated successfully"));
    }

    // ==========================================
    // 11. COMPANY TRANSACTIONS
    // ==========================================
    @GetMapping("/company-transactions")
    @Operation(summary = "Get company transactions, monthly transaction value, transaction types, and status breakdown")
    public ResponseEntity<ApiResponse<SubsidiaryTransactionsPageDto>> getCompanyTransactions(
            @RequestParam(required = false, defaultValue = "Atirath Holdings") String company,
            @RequestParam(required = false) String tab,
            @RequestParam(required = false) String query) {
        log.info("GET /api/v1/admin/subsidiary/company-transactions requested (company={}, tab={}, query={})", company, tab, query);
        SubsidiaryTransactionsPageDto page = subsidiaryAdminService.getSubsidiaryTransactionsPage(company, tab, query);
        return ResponseEntity.ok(ApiResponse.success(page, "Company transactions retrieved successfully"));
    }

    @GetMapping("/company-transactions/{id}")
    @Operation(summary = "Get detailed transaction execution receipt, counterparties, and approval metadata")
    public ResponseEntity<ApiResponse<CompanyTransactionDetailDto>> getCompanyTransactionDetail(@PathVariable String id) {
        log.info("GET /api/v1/admin/subsidiary/company-transactions/{} requested", id);
        CompanyTransactionDetailDto detail = subsidiaryAdminService.getCompanyTransactionDetail(id);
        return ResponseEntity.ok(ApiResponse.success(detail, "Company transaction detail retrieved successfully"));
    }

    @PostMapping("/company-transactions")
    @Operation(summary = "Record a new share or corporate transaction")
    public ResponseEntity<ApiResponse<CompanyTransactionDetailDto>> recordCompanyTransaction(@RequestBody RecordCompanyTransactionRequestDto request) {
        log.info("POST /api/v1/admin/subsidiary/company-transactions requested for counterparty: {}", request.getCounterparty());
        CompanyTransactionDetailDto created = subsidiaryAdminService.recordCompanyTransaction(request);
        return ResponseEntity.ok(ApiResponse.success(created, "Company transaction recorded successfully"));
    }

    // ==========================================
    // 12. COMPANY REPORTS
    // ==========================================
    @GetMapping("/company-reports")
    @Operation(summary = "Get company reports overview, subsidiary-wise investment, shareholding distribution, and available reports")
    public ResponseEntity<ApiResponse<SubsidiaryReportsPageDto>> getCompanyReports(
            @RequestParam(required = false, defaultValue = "Atirath Holdings") String company,
            @RequestParam(required = false) String tab,
            @RequestParam(required = false) String dateRange) {
        log.info("GET /api/v1/admin/subsidiary/company-reports requested (company={}, tab={}, dateRange={})", company, tab, dateRange);
        SubsidiaryReportsPageDto page = subsidiaryAdminService.getSubsidiaryReportsPage(company, tab, dateRange);
        return ResponseEntity.ok(ApiResponse.success(page, "Company reports retrieved successfully"));
    }

    @PostMapping("/company-reports")
    @Operation(summary = "Generate a custom subsidiary report")
    public ResponseEntity<ApiResponse<SubsidiaryReportsPageDto.ReportItemDto>> generateSubsidiaryReport(@RequestBody GenerateSubsidiaryReportRequestDto request) {
        log.info("POST /api/v1/admin/subsidiary/company-reports requested for report: {}", request.getReportName());
        SubsidiaryReportsPageDto.ReportItemDto created = subsidiaryAdminService.generateSubsidiaryReport(request);
        return ResponseEntity.ok(ApiResponse.success(created, "Report generated successfully"));
    }

    @GetMapping("/company-reports/export-all")
    @Operation(summary = "Export all subsidiary reports as a bundle")
    public ResponseEntity<ApiResponse<java.util.Map<String, String>>> exportAllReports(
            @RequestParam(required = false, defaultValue = "Atirath Holdings") String company) {
        log.info("GET /api/v1/admin/subsidiary/company-reports/export-all requested for company: {}", company);
        java.util.Map<String, String> exportInfo = subsidiaryAdminService.exportAllReports(company);
        return ResponseEntity.ok(ApiResponse.success(exportInfo, "Complete reports bundle export generated successfully"));
    }
}

