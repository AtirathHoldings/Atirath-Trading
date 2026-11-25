package com.atirath.trading.modules.admin.parent.controller;

import com.atirath.trading.common.ApiResponse;
import com.atirath.trading.modules.admin.parent.dto.*;
import com.atirath.trading.modules.admin.parent.service.ParentCompanyAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/parent")
@RequiredArgsConstructor
@Tag(name = "Parent Company Admin", description = "Endpoints for Parent Company Admin Dashboard, Subsidiaries, Share Capital, Shareholders, Holdings, Market Activity, and Trade Monitoring")
public class ParentCompanyAdminController {

    private final ParentCompanyAdminService adminService;

    // ==========================================
    // 1. DASHBOARD
    // ==========================================
    @GetMapping("/dashboard")
    @Operation(summary = "Get Parent Company Dashboard overview, charts, and metrics")
    public ResponseEntity<ApiResponse<ParentDashboardDto>> getDashboard(
            @RequestParam(required = false) String dateRange,
            @RequestParam(required = false) String company) {
        log.info("GET /api/v1/admin/parent/dashboard requested (dateRange={}, company={})", dateRange, company);
        ParentDashboardDto dashboard = adminService.getDashboard(dateRange, company);
        return ResponseEntity.ok(ApiResponse.success(dashboard, "Parent company dashboard retrieved successfully"));
    }

    // ==========================================
    // 2. SUBSIDIARY COMPANIES
    // ==========================================
    @GetMapping("/subsidiaries")
    @Operation(summary = "Get list of all group subsidiaries with status, KPIs, sector distribution, and recent updates")
    public ResponseEntity<ApiResponse<SubsidiariesPageDto>> getSubsidiaries(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String sector) {
        log.info("GET /api/v1/admin/parent/subsidiaries requested (query={}, status={}, sector={})", query, status, sector);
        SubsidiariesPageDto page = adminService.getSubsidiariesPage(query, status, sector);
        return ResponseEntity.ok(ApiResponse.success(page, "Subsidiary companies retrieved successfully"));
    }

    @PostMapping("/subsidiaries")
    @Operation(summary = "Add a new subsidiary company to the group")
    public ResponseEntity<ApiResponse<SubsidiarySummaryDto>> addSubsidiary(@RequestBody AddSubsidiaryRequestDto request) {
        log.info("POST /api/v1/admin/parent/subsidiaries requested for company: {}", request.getCompanyName());
        SubsidiarySummaryDto created = adminService.addSubsidiary(request);
        return ResponseEntity.ok(ApiResponse.success(created, "Subsidiary company added successfully"));
    }

    @GetMapping("/subsidiaries/export")
    @Operation(summary = "Export subsidiary companies data report")
    public ResponseEntity<ApiResponse<Map<String, String>>> exportSubsidiaries() {
        log.info("GET /api/v1/admin/parent/subsidiaries/export requested");
        Map<String, String> exportInfo = adminService.exportSubsidiaries();
        return ResponseEntity.ok(ApiResponse.success(exportInfo, "Subsidiaries export report generated"));
    }

    // ==========================================
    // 3. SUBSIDIARY DETAIL & TABS
    // ==========================================
    @GetMapping("/subsidiaries/{id}")
    @Operation(summary = "Get comprehensive detail for a subsidiary company including KPIs, metrics, and compliance")
    public ResponseEntity<ApiResponse<SubsidiaryDetailDto>> getSubsidiaryDetail(
            @PathVariable String id,
            @RequestParam(required = false) String tab) {
        log.info("GET /api/v1/admin/parent/subsidiaries/{} requested (tab={})", id, tab);
        SubsidiaryDetailDto detail = adminService.getSubsidiaryDetail(id, tab);
        return ResponseEntity.ok(ApiResponse.success(detail, "Subsidiary company details retrieved successfully"));
    }

    @GetMapping("/subsidiaries/{id}/overview")
    @Operation(summary = "Get overview metrics and details for a subsidiary company")
    public ResponseEntity<ApiResponse<SubsidiaryDetailDto>> getSubsidiaryOverview(@PathVariable String id) {
        log.info("GET /api/v1/admin/parent/subsidiaries/{}/overview requested", id);
        SubsidiaryDetailDto detail = adminService.getSubsidiaryDetail(id, "Overview");
        return ResponseEntity.ok(ApiResponse.success(detail, "Subsidiary overview retrieved successfully"));
    }

    @GetMapping("/subsidiaries/{id}/financials")
    @Operation(summary = "Get financial performance and statements for a subsidiary company")
    public ResponseEntity<ApiResponse<SubsidiaryDetailDto.FinancialsTabDto>> getSubsidiaryFinancials(@PathVariable String id) {
        log.info("GET /api/v1/admin/parent/subsidiaries/{}/financials requested", id);
        SubsidiaryDetailDto.FinancialsTabDto financials = adminService.getSubsidiaryFinancials(id);
        return ResponseEntity.ok(ApiResponse.success(financials, "Subsidiary financials retrieved successfully"));
    }

    @GetMapping("/subsidiaries/{id}/shareholding")
    @Operation(summary = "Get shareholding pattern and cap table for a subsidiary company")
    public ResponseEntity<ApiResponse<SubsidiaryDetailDto.ShareholdingTabDto>> getSubsidiaryShareholding(@PathVariable String id) {
        log.info("GET /api/v1/admin/parent/subsidiaries/{}/shareholding requested", id);
        SubsidiaryDetailDto.ShareholdingTabDto shareholding = adminService.getSubsidiaryShareholding(id);
        return ResponseEntity.ok(ApiResponse.success(shareholding, "Subsidiary shareholding retrieved successfully"));
    }

    @GetMapping("/subsidiaries/{id}/members")
    @Operation(summary = "Get member directory and demographics for a subsidiary company")
    public ResponseEntity<ApiResponse<SubsidiaryDetailDto.MembersTabDto>> getSubsidiaryMembers(@PathVariable String id) {
        log.info("GET /api/v1/admin/parent/subsidiaries/{}/members requested", id);
        SubsidiaryDetailDto.MembersTabDto members = adminService.getSubsidiaryMembers(id);
        return ResponseEntity.ok(ApiResponse.success(members, "Subsidiary members retrieved successfully"));
    }

    @GetMapping("/subsidiaries/{id}/trading-activity")
    @Operation(summary = "Get trading activity and recent order logs for a subsidiary company")
    public ResponseEntity<ApiResponse<SubsidiaryDetailDto.TradingActivityTabDto>> getSubsidiaryTradingActivity(@PathVariable String id) {
        log.info("GET /api/v1/admin/parent/subsidiaries/{}/trading-activity requested", id);
        SubsidiaryDetailDto.TradingActivityTabDto activity = adminService.getSubsidiaryTradingActivity(id);
        return ResponseEntity.ok(ApiResponse.success(activity, "Subsidiary trading activity retrieved successfully"));
    }

    @GetMapping("/subsidiaries/{id}/compliance")
    @Operation(summary = "Get statutory filings and compliance status for a subsidiary company")
    public ResponseEntity<ApiResponse<SubsidiaryDetailDto.ComplianceTabDto>> getSubsidiaryCompliance(@PathVariable String id) {
        log.info("GET /api/v1/admin/parent/subsidiaries/{}/compliance requested", id);
        SubsidiaryDetailDto.ComplianceTabDto compliance = adminService.getSubsidiaryCompliance(id);
        return ResponseEntity.ok(ApiResponse.success(compliance, "Subsidiary compliance status retrieved successfully"));
    }

    @GetMapping("/subsidiaries/{id}/documents")
    @Operation(summary = "Get verified statutory and company documents for a subsidiary company")
    public ResponseEntity<ApiResponse<SubsidiaryDetailDto.DocumentsTabDto>> getSubsidiaryDocuments(@PathVariable String id) {
        log.info("GET /api/v1/admin/parent/subsidiaries/{}/documents requested", id);
        SubsidiaryDetailDto.DocumentsTabDto docs = adminService.getSubsidiaryDocuments(id);
        return ResponseEntity.ok(ApiResponse.success(docs, "Subsidiary documents retrieved successfully"));
    }

    @GetMapping("/subsidiaries/{id}/settings")
    @Operation(summary = "Get trading configuration and operational settings for a subsidiary company")
    public ResponseEntity<ApiResponse<SubsidiaryDetailDto.SettingsTabDto>> getSubsidiarySettings(@PathVariable String id) {
        log.info("GET /api/v1/admin/parent/subsidiaries/{}/settings requested", id);
        SubsidiaryDetailDto.SettingsTabDto settings = adminService.getSubsidiarySettings(id);
        return ResponseEntity.ok(ApiResponse.success(settings, "Subsidiary settings retrieved successfully"));
    }

    @PutMapping("/subsidiaries/{id}/info")
    @Operation(summary = "Edit basic corporate and tax information for a subsidiary")
    public ResponseEntity<ApiResponse<SubsidiaryDetailDto>> updateSubsidiaryInfo(
            @PathVariable String id,
            @RequestBody UpdateCompanyInfoDto request) {
        log.info("PUT /api/v1/admin/parent/subsidiaries/{}/info requested", id);
        SubsidiaryDetailDto updated = adminService.updateSubsidiaryInfo(id, request);
        return ResponseEntity.ok(ApiResponse.success(updated, "Subsidiary information updated successfully"));
    }

    @PutMapping("/subsidiaries/{id}/status")
    @Operation(summary = "Update subsidiary operational status (Active, Under Setup, Inactive)")
    public ResponseEntity<ApiResponse<SubsidiaryDetailDto>> updateSubsidiaryStatus(
            @PathVariable String id,
            @RequestParam String status) {
        log.info("PUT /api/v1/admin/parent/subsidiaries/{}/status requested with status={}", id, status);
        SubsidiaryDetailDto updated = adminService.updateSubsidiaryStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success(updated, "Subsidiary status updated successfully"));
    }

    @PostMapping("/subsidiaries/{id}/activities")
    @Operation(summary = "Add a new recent activity entry for a subsidiary")
    public ResponseEntity<ApiResponse<SubsidiaryDetailDto>> addSubsidiaryActivity(
            @PathVariable String id,
            @RequestBody SubsidiaryDetailDto.SubsidiaryActivityDto activity) {
        log.info("POST /api/v1/admin/parent/subsidiaries/{}/activities requested", id);
        SubsidiaryDetailDto updated = adminService.addSubsidiaryActivity(id, activity);
        return ResponseEntity.ok(ApiResponse.success(updated, "Subsidiary activity recorded successfully"));
    }

    @DeleteMapping("/subsidiaries/{id}")
    @Operation(summary = "Delete or archive a subsidiary company from the group")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteSubsidiary(@PathVariable String id) {
        log.info("DELETE /api/v1/admin/parent/subsidiaries/{} requested", id);
        boolean deleted = adminService.deleteSubsidiary(id);
        return ResponseEntity.ok(ApiResponse.success(Map.of("deleted", deleted, "id", id), "Subsidiary processed successfully"));
    }

    // ==========================================
    // 4. GROUP SHARE CAPITAL
    // ==========================================
    @GetMapping("/share-capital")
    @Operation(summary = "Get Group Share Capital breakdown, authorized vs issued vs paid-up charts, and subsidiary table")
    public ResponseEntity<ApiResponse<GroupShareCapitalPageDto>> getShareCapital(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String sector) {
        log.info("GET /api/v1/admin/parent/share-capital requested (query={}, sector={})", query, sector);
        GroupShareCapitalPageDto page = adminService.getShareCapitalPage(query, sector);
        return ResponseEntity.ok(ApiResponse.success(page, "Group share capital details retrieved successfully"));
    }

    @GetMapping("/share-capital/export")
    @Operation(summary = "Export group share capital statement report")
    public ResponseEntity<ApiResponse<Map<String, String>>> exportShareCapital() {
        log.info("GET /api/v1/admin/parent/share-capital/export requested");
        Map<String, String> exportInfo = adminService.exportShareCapital();
        return ResponseEntity.ok(ApiResponse.success(exportInfo, "Group share capital statement generated"));
    }

    // ==========================================
    // 5. GROUP SHAREHOLDERS
    // ==========================================
    @GetMapping("/shareholders")
    @Operation(summary = "Get all group shareholders, category distribution, and top 10 bar chart")
    public ResponseEntity<ApiResponse<GroupShareholdersPageDto>> getShareholders(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String company,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size) {
        log.info("GET /api/v1/admin/parent/shareholders requested (query={}, category={}, company={}, page={}, size={})", query, category, company, page, size);
        GroupShareholdersPageDto pageDto = adminService.getShareholdersPage(query, category, company, page, size);
        return ResponseEntity.ok(ApiResponse.success(pageDto, "Group shareholders retrieved successfully"));
    }

    @PostMapping("/shareholders")
    @Operation(summary = "Add a new shareholder to the group")
    public ResponseEntity<ApiResponse<GroupShareholdersPageDto.ShareholderItemDto>> addShareholder(
            @RequestBody AddShareholderRequestDto request) {
        log.info("POST /api/v1/admin/parent/shareholders requested for: {}", request.getShareholderName());
        GroupShareholdersPageDto.ShareholderItemDto created = adminService.addShareholder(request);
        return ResponseEntity.ok(ApiResponse.success(created, "Shareholder added successfully"));
    }

    @GetMapping("/shareholders/export")
    @Operation(summary = "Export group shareholders register report")
    public ResponseEntity<ApiResponse<Map<String, String>>> exportShareholders() {
        log.info("GET /api/v1/admin/parent/shareholders/export requested");
        Map<String, String> exportInfo = adminService.exportShareholders();
        return ResponseEntity.ok(ApiResponse.success(exportInfo, "Shareholders export register generated"));
    }

    // ==========================================
    // 6. GROUP HOLDINGS
    // ==========================================
    @GetMapping("/holdings")
    @Operation(summary = "Get Group Holdings breakdown, company valuations, sector distribution, and activity")
    public ResponseEntity<ApiResponse<GroupHoldingsPageDto>> getGroupHoldings(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String sector,
            @RequestParam(required = false) String bySector) {
        log.info("GET /api/v1/admin/parent/holdings requested (query={}, sector={}, bySector={})", query, sector, bySector);
        GroupHoldingsPageDto pageDto = adminService.getGroupHoldingsPage(query, sector);
        return ResponseEntity.ok(ApiResponse.success(pageDto, "Group holdings retrieved successfully"));
    }

    @GetMapping("/holdings/export")
    @Operation(summary = "Export group holdings valuation report")
    public ResponseEntity<ApiResponse<Map<String, String>>> exportHoldings() {
        log.info("GET /api/v1/admin/parent/holdings/export requested");
        Map<String, String> exportInfo = adminService.exportHoldings();
        return ResponseEntity.ok(ApiResponse.success(exportInfo, "Group holdings report generated"));
    }

    // ==========================================
    // 7. MARKET ACTIVITY (GROUP LEVEL)
    // ==========================================
    @GetMapping("/market-activity")
    @Operation(summary = "Get group level market activity, trade volume trends, and recent trades across companies")
    public ResponseEntity<ApiResponse<MarketActivityGroupPageDto>> getMarketActivity(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String sector,
            @RequestParam(required = false) String company,
            @RequestParam(required = false) String dateRange,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "6") Integer size) {
        log.info("GET /api/v1/admin/parent/market-activity requested (query={}, sector={}, company={}, dateRange={}, page={}, size={})", query, sector, company, dateRange, page, size);
        MarketActivityGroupPageDto pageDto = adminService.getMarketActivityPage(query, sector, company, page, size, dateRange);
        return ResponseEntity.ok(ApiResponse.success(pageDto, "Market activity data retrieved successfully"));
    }

    @GetMapping("/market-activity/export")
    @Operation(summary = "Export group market activity trade transactions")
    public ResponseEntity<ApiResponse<Map<String, String>>> exportMarketActivity() {
        log.info("GET /api/v1/admin/parent/market-activity/export requested");
        Map<String, String> exportInfo = adminService.exportMarketActivity();
        return ResponseEntity.ok(ApiResponse.success(exportInfo, "Market activity export generated"));
    }

    // ==========================================
    // 8. GROUP TRADE MONITORING
    // ==========================================
    @GetMapping("/trade-monitoring")
    @Operation(summary = "Get group trade monitoring live data, unusual activity flags, and monitoring insights")
    public ResponseEntity<ApiResponse<GroupTradeMonitoringPageDto>> getTradeMonitoring(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String company,
            @RequestParam(required = false) String sector,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String dateRange,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "8") Integer size) {
        log.info("GET /api/v1/admin/parent/trade-monitoring requested (query={}, company={}, sector={}, type={}, dateRange={}, page={}, size={})", query, company, sector, type, dateRange, page, size);
        GroupTradeMonitoringPageDto pageDto = adminService.getTradeMonitoringPage(query, company, sector, type, page, size, dateRange);
        return ResponseEntity.ok(ApiResponse.success(pageDto, "Trade monitoring data retrieved successfully"));
    }

    @GetMapping("/trade-monitoring/export")
    @Operation(summary = "Export group trade monitoring log report")
    public ResponseEntity<ApiResponse<Map<String, String>>> exportTradeMonitoring() {
        log.info("GET /api/v1/admin/parent/trade-monitoring/export requested");
        Map<String, String> exportInfo = adminService.exportTradeMonitoring();
        return ResponseEntity.ok(ApiResponse.success(exportInfo, "Trade monitoring report generated"));
    }

    // ==========================================
    // 9. ESCROW & SETTLEMENT (GROUP LEVEL)
    // ==========================================
    @GetMapping("/escrow-settlement")
    @Operation(summary = "Get group escrow and settlement overview, trend chart, settlement status donut, and transactions table")
    public ResponseEntity<ApiResponse<EscrowSettlementGroupPageDto>> getEscrowSettlement(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String company,
            @RequestParam(required = false) String tradeType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String tab,
            @RequestParam(required = false) String dateRange,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "8") Integer size) {
        log.info("GET /api/v1/admin/parent/escrow-settlement requested (query={}, company={}, tradeType={}, status={}, tab={}, dateRange={}, page={}, size={})", query, company, tradeType, status, tab, dateRange, page, size);
        EscrowSettlementGroupPageDto pageDto = adminService.getEscrowSettlementPage(query, company, tradeType, status, tab, page, size, dateRange);
        return ResponseEntity.ok(ApiResponse.success(pageDto, "Escrow and settlement data retrieved successfully"));
    }

    @PostMapping("/escrow-settlement")
    @Operation(summary = "Create a new escrow transaction")
    public ResponseEntity<ApiResponse<EscrowSettlementGroupPageDto.EscrowTransactionItemDto>> addEscrowTransaction(
            @RequestBody AddEscrowRequestDto request) {
        log.info("POST /api/v1/admin/parent/escrow-settlement requested for company: {}", request.getCompany());
        EscrowSettlementGroupPageDto.EscrowTransactionItemDto created = adminService.addEscrowTransaction(request);
        return ResponseEntity.ok(ApiResponse.success(created, "Escrow transaction created successfully"));
    }

    @GetMapping("/escrow-settlement/export")
    @Operation(summary = "Export escrow and settlement statements report")
    public ResponseEntity<ApiResponse<Map<String, String>>> exportEscrowSettlement() {
        log.info("GET /api/v1/admin/parent/escrow-settlement/export requested");
        Map<String, String> exportInfo = adminService.exportEscrowSettlement();
        return ResponseEntity.ok(ApiResponse.success(exportInfo, "Escrow and settlement report generated"));
    }

    // ==========================================
    // 10. APPROVALS
    // ==========================================
    @GetMapping("/approvals")
    @Operation(summary = "Get group approvals list with tabs (All, Pending, Under Review, Approved, Rejected) and charts")
    public ResponseEntity<ApiResponse<ApprovalsPageDto>> getApprovals(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String company,
            @RequestParam(required = false) String approvalType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String tab,
            @RequestParam(required = false) String dateRange,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size) {
        log.info("GET /api/v1/admin/parent/approvals requested (query={}, company={}, type={}, status={}, priority={}, tab={}, dateRange={}, page={}, size={})",
                query, company, approvalType, status, priority, tab, dateRange, page, size);
        ApprovalsPageDto pageDto = adminService.getApprovalsPage(query, company, approvalType, status, priority, tab, page, size, dateRange);
        return ResponseEntity.ok(ApiResponse.success(pageDto, "Approvals list retrieved successfully"));
    }

    @GetMapping("/approvals/{id}")
    @Operation(summary = "Get detailed approval request info including attachments and history")
    public ResponseEntity<ApiResponse<ApprovalDetailDto>> getApprovalDetail(@PathVariable String id) {
        log.info("GET /api/v1/admin/parent/approvals/{} requested", id);
        ApprovalDetailDto detail = adminService.getApprovalDetail(id);
        return ResponseEntity.ok(ApiResponse.success(detail, "Approval detail retrieved successfully"));
    }

    @PostMapping("/approvals")
    @Operation(summary = "Submit a new approval request")
    public ResponseEntity<ApiResponse<ApprovalDetailDto>> addApproval(@RequestBody AddApprovalRequestDto request) {
        log.info("POST /api/v1/admin/parent/approvals requested for type: {}", request.getApprovalType());
        ApprovalDetailDto created = adminService.addApprovalRequest(request);
        return ResponseEntity.ok(ApiResponse.success(created, "Approval request submitted successfully"));
    }

    @PutMapping("/approvals/{id}/approve")
    @Operation(summary = "Approve a pending request")
    public ResponseEntity<ApiResponse<ApprovalDetailDto>> approveRequest(@PathVariable String id) {
        log.info("PUT /api/v1/admin/parent/approvals/{}/approve requested", id);
        ApprovalDetailDto updated = adminService.approveRequest(id);
        return ResponseEntity.ok(ApiResponse.success(updated, "Request approved successfully"));
    }

    @PutMapping("/approvals/{id}/reject")
    @Operation(summary = "Reject a pending request")
    public ResponseEntity<ApiResponse<ApprovalDetailDto>> rejectRequest(@PathVariable String id) {
        log.info("PUT /api/v1/admin/parent/approvals/{}/reject requested", id);
        ApprovalDetailDto updated = adminService.rejectRequest(id);
        return ResponseEntity.ok(ApiResponse.success(updated, "Request rejected successfully"));
    }

    // ==========================================
    // 11. EXTERNAL INVESTOR REQUESTS
    // ==========================================
    @GetMapping("/investor-requests")
    @Operation(summary = "Get external investor requests with tabs, company distribution, and investor type breakdown")
    public ResponseEntity<ApiResponse<ExternalInvestorRequestsPageDto>> getInvestorRequests(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String sector,
            @RequestParam(required = false) String investmentType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String tab,
            @RequestParam(required = false) String dateRange,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size) {
        log.info("GET /api/v1/admin/parent/investor-requests requested (query={}, sector={}, type={}, status={}, tab={}, dateRange={}, page={}, size={})",
                query, sector, investmentType, status, tab, dateRange, page, size);
        ExternalInvestorRequestsPageDto pageDto = adminService.getInvestorRequestsPage(query, sector, investmentType, status, tab, page, size, dateRange);
        return ResponseEntity.ok(ApiResponse.success(pageDto, "External investor requests retrieved successfully"));
    }

    @GetMapping("/investor-requests/{id}")
    @Operation(summary = "Get detailed external investor request including proposal and contact info")
    public ResponseEntity<ApiResponse<InvestorRequestDetailDto>> getInvestorRequestDetail(@PathVariable String id) {
        log.info("GET /api/v1/admin/parent/investor-requests/{} requested", id);
        InvestorRequestDetailDto detail = adminService.getInvestorRequestDetail(id);
        return ResponseEntity.ok(ApiResponse.success(detail, "Investor request detail retrieved successfully"));
    }

    @PostMapping("/investor-requests")
    @Operation(summary = "Submit a new external investor request")
    public ResponseEntity<ApiResponse<InvestorRequestDetailDto>> addInvestorRequest(@RequestBody AddInvestorRequestDto request) {
        log.info("POST /api/v1/admin/parent/investor-requests requested for investor: {}", request.getInvestorName());
        InvestorRequestDetailDto created = adminService.addInvestorRequest(request);
        return ResponseEntity.ok(ApiResponse.success(created, "Investor request created successfully"));
    }

    @PutMapping("/investor-requests/{id}/approve")
    @Operation(summary = "Approve an external investor request")
    public ResponseEntity<ApiResponse<InvestorRequestDetailDto>> approveInvestorRequest(@PathVariable String id) {
        log.info("PUT /api/v1/admin/parent/investor-requests/{}/approve requested", id);
        InvestorRequestDetailDto updated = adminService.approveInvestorRequest(id);
        return ResponseEntity.ok(ApiResponse.success(updated, "Investor request approved successfully"));
    }

    @PutMapping("/investor-requests/{id}/reject")
    @Operation(summary = "Reject an external investor request")
    public ResponseEntity<ApiResponse<InvestorRequestDetailDto>> rejectInvestorRequest(@PathVariable String id) {
        log.info("PUT /api/v1/admin/parent/investor-requests/{}/reject requested", id);
        InvestorRequestDetailDto updated = adminService.rejectInvestorRequest(id);
        return ResponseEntity.ok(ApiResponse.success(updated, "Investor request rejected successfully"));
    }

    // ==========================================
    // 12. GROUP REPORTS
    // ==========================================
    @GetMapping("/reports")
    @Operation(summary = "Get consolidated group reports, financial trends, company performance, and revenue contribution")
    public ResponseEntity<ApiResponse<GroupReportsPageDto>> getReports(
            @RequestParam(required = false) String company,
            @RequestParam(required = false) String dateRange) {
        log.info("GET /api/v1/admin/parent/reports requested (company={}, dateRange={})", company, dateRange);
        GroupReportsPageDto page = adminService.getReportsPage(company, dateRange);
        return ResponseEntity.ok(ApiResponse.success(page, "Group reports retrieved successfully"));
    }

    @GetMapping("/reports/export")
    @Operation(summary = "Export consolidated group reports")
    public ResponseEntity<ApiResponse<Map<String, String>>> exportReports() {
        log.info("GET /api/v1/admin/parent/reports/export requested");
        Map<String, String> exportInfo = adminService.exportReports();
        return ResponseEntity.ok(ApiResponse.success(exportInfo, "Group report generated successfully"));
    }
}

