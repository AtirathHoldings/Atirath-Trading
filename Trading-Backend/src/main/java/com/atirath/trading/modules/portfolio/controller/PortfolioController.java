package com.atirath.trading.modules.portfolio.controller;

import com.atirath.trading.common.ApiException;
import com.atirath.trading.common.ApiResponse;
import com.atirath.trading.config.JwtTokenProvider;
import com.atirath.trading.modules.portfolio.dto.*;
import com.atirath.trading.modules.portfolio.service.MemberDashboardService;
import com.atirath.trading.modules.portfolio.service.MemberPendingActionsService;
import com.atirath.trading.modules.portfolio.service.MemberTransactionsService;
import com.atirath.trading.modules.portfolio.service.PortfolioOverviewService;
import com.atirath.trading.modules.portfolio.service.PortfolioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/portfolio")
@RequiredArgsConstructor
@Tag(name = "Portfolio Module", description = "Endpoints for Portfolio Dashboard, Holdings, Orders, Positions, Trade History, and Statements")
public class PortfolioController {

    private final PortfolioService portfolioService;
    private final MemberDashboardService memberDashboardService;
    private final MemberPendingActionsService memberPendingActionsService;
    private final MemberTransactionsService memberTransactionsService;
    private final PortfolioOverviewService portfolioOverviewService;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping("/dashboard")
    @Operation(summary = "Get Portfolio Dashboard", description = "Returns complete Portfolio Dashboard data for the authenticated member: KPI cards, value trend chart, allocation donut, portfolio performance metrics, top holdings, and recent activity.")
    public ResponseEntity<ApiResponse<PortfolioDashboardDto>> getDashboard(
            @Parameter(description = "Trend period: 1M, 3M, 6M, 1Y, ALL (default 6M)")
            @RequestParam(required = false, defaultValue = "6M") String period,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = requireAuthenticatedUserId(authHeader, "portfolio dashboard");
        PortfolioDashboardDto dashboard = portfolioService.getDashboard(userId, period);
        return ResponseEntity.ok(ApiResponse.success(dashboard, "Portfolio dashboard retrieved successfully"));
    }

    @GetMapping("/overview")
    @Operation(summary = "Get Portfolio Overview", description = "Returns complete Portfolio Overview data for the authenticated member: KPI cards, trend chart, company allocation, asset breakdown, return summary, top gainers, and top losers.")
    public ResponseEntity<ApiResponse<PortfolioOverviewDto>> getPortfolioOverview(
            @Parameter(description = "Trend period: 1M, 3M, 6M, 1Y, ALL (default 1M)")
            @RequestParam(required = false, defaultValue = "1M") String period,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = requireAuthenticatedUserId(authHeader, "portfolio overview");
        PortfolioOverviewDto overview = portfolioOverviewService.getPortfolioOverview(userId, period);
        return ResponseEntity.ok(ApiResponse.success(overview, "Portfolio overview retrieved successfully"));
    }

    @GetMapping("/member-dashboard")
    @Operation(summary = "Get Member Dashboard", description = "Returns real authenticated per-user data for the web Member Dashboard.")
    public ResponseEntity<ApiResponse<MemberDashboardDto>> getMemberDashboard(
            @Parameter(description = "Trend period: 1M, 3M, 6M, 1Y, ALL (default 1M)")
            @RequestParam(required = false, defaultValue = "1M") String period,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = requireAuthenticatedUserId(authHeader, "member dashboard");
        MemberDashboardDto dashboard = memberDashboardService.getMemberDashboard(userId, period);
        return ResponseEntity.ok(ApiResponse.success(dashboard, "Member dashboard retrieved successfully"));
    }

    @GetMapping("/member-transactions")
    @Operation(summary = "Get Member Transactions", description = "Returns real authenticated Buy, Sell, and Share Transfer transactions, insights, and recent activity.")
    public ResponseEntity<ApiResponse<MemberTransactionsDto>> getMemberTransactions(
            @RequestParam(required = false, defaultValue = "3M") String period,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false, defaultValue = "All Companies") String company,
            @RequestParam(required = false, defaultValue = "All Types") String type,
            @RequestParam(required = false, defaultValue = "All Status") String status,
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = requireAuthenticatedUserId(authHeader, "member transactions");
        MemberTransactionsDto result = memberTransactionsService.getMemberTransactions(
                userId, period, startDate, endDate, company, type, status, search, page, size
        );
        return ResponseEntity.ok(ApiResponse.success(result, "Member transactions retrieved successfully"));
    }

    @GetMapping("/download/member-transactions")
    @Operation(summary = "Export Member Transactions CSV", description = "Exports filtered real Buy, Sell, and Transfer transactions in CSV format for the authenticated member.")
    public ResponseEntity<byte[]> downloadMemberTransactions(
            @RequestParam(required = false, defaultValue = "3M") String period,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false, defaultValue = "All Companies") String company,
            @RequestParam(required = false, defaultValue = "All Types") String type,
            @RequestParam(required = false, defaultValue = "All Status") String status,
            @RequestParam(required = false) String search,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = requireAuthenticatedUserId(authHeader, "transactions export");
        String csv = memberTransactionsService.buildTransactionsCsv(
                userId, period, startDate, endDate, company, type, status, search
        );
        byte[] bytes = csv.getBytes(StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"transactions_ATG" + userId + ".csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(bytes);
    }

    @GetMapping("/member-pending-actions")
    @Operation(summary = "Get Member Pending Actions", description = "Returns real authenticated pending actions across KYC, Orders, Negotiations, Escrow, and Account.")
    public ResponseEntity<ApiResponse<MemberPendingActionsDto>> getMemberPendingActions(
            @RequestParam(required = false, defaultValue = "All") String category,
            @RequestParam(required = false, defaultValue = "Priority") String sortBy,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = requireAuthenticatedUserId(authHeader, "pending actions");
        MemberPendingActionsDto actions = memberPendingActionsService.getMemberPendingActions(userId, category, sortBy);
        return ResponseEntity.ok(ApiResponse.success(actions, "Member pending actions retrieved successfully"));
    }

    @GetMapping("/holdings")
    @Operation(summary = "Get Current Holdings", description = "Returns complete Holdings screen data (Screen 2): summary KPIs, group company holdings, portfolio allocation, and quick insights.")
    public ResponseEntity<ApiResponse<PortfolioHoldingsResponseDto>> getHoldings(
            @Parameter(description = "Search text by company name, symbol or ISIN")
            @RequestParam(required = false) String search,
            @Parameter(description = "Company filter (ALL or specific company name/symbol)")
            @RequestParam(required = false, defaultValue = "ALL") String company,
            @Parameter(description = "Holding type filter (ALL or specific)")
            @RequestParam(required = false, defaultValue = "ALL") String holdingType,
            @Parameter(description = "Holding status filter (ALL or specific)")
            @RequestParam(required = false, defaultValue = "ALL") String status,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = extractUserId(authHeader);
        PortfolioHoldingsResponseDto holdings = portfolioService.getHoldings(userId, search, company, holdingType, status, page, size);
        return ResponseEntity.ok(ApiResponse.success(holdings, "Portfolio holdings retrieved successfully"));
    }

    @GetMapping("/orders")
    @Operation(summary = "Get Portfolio Orders", description = "Returns complete Orders screen data (Screen 3): order KPI cards, tabs (All, Buy, Sell, Open, Executed, Cancelled, Rejected), orders table, and selected order detail with 5-stage timeline.")
    public ResponseEntity<ApiResponse<PortfolioOrdersResponseDto>> getOrders(
            @Parameter(description = "Order tab filter: ALL, BUY, SELL, OPEN, EXECUTED, CANCELLED, REJECTED")
            @RequestParam(required = false, defaultValue = "ALL") String tab,
            @Parameter(description = "Company filter (ALL or company name/symbol)")
            @RequestParam(required = false, defaultValue = "ALL") String company,
            @Parameter(description = "Order side / type")
            @RequestParam(required = false) String orderType,
            @Parameter(description = "Status filter")
            @RequestParam(required = false) String status,
            @Parameter(description = "Start Date (YYYY-MM-DD)")
            @RequestParam(required = false) String startDate,
            @Parameter(description = "End Date (YYYY-MM-DD)")
            @RequestParam(required = false) String endDate,
            @Parameter(description = "Pre-selected order ID for details panel")
            @RequestParam(required = false) Long selectedOrderId,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = extractUserId(authHeader);
        PortfolioOrdersResponseDto orders = portfolioService.getOrders(userId, tab, company, orderType, status, startDate, endDate, selectedOrderId, page, size);
        return ResponseEntity.ok(ApiResponse.success(orders, "Portfolio orders retrieved successfully"));
    }

    @GetMapping("/orders/{orderId}")
    @Operation(summary = "Get Order Detail & Timeline", description = "Returns single order detail and full execution timeline for side drawer / modal view.")
    public ResponseEntity<ApiResponse<PortfolioOrderDetailDto>> getOrderDetail(
            @PathVariable("orderId") Long orderId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = extractUserId(authHeader);
        PortfolioOrderDetailDto orderDetail = portfolioService.getOrderDetail(orderId, null, userId);
        return ResponseEntity.ok(ApiResponse.success(orderDetail, "Order detail retrieved successfully"));
    }

    @GetMapping("/positions")
    @Operation(summary = "Get Open Positions", description = "Returns complete Positions screen data (Screen 4): position KPI cards, tabs (All, Profitable, Loss Making), position table, value distribution, insights, trend, and day's movement.")
    public ResponseEntity<ApiResponse<PortfolioPositionsResponseDto>> getPositions(
            @Parameter(description = "Positions tab: ALL, PROFITABLE, LOSS_MAKING")
            @RequestParam(required = false, defaultValue = "ALL") String tab,
            @Parameter(description = "Company filter")
            @RequestParam(required = false, defaultValue = "ALL") String company,
            @Parameter(description = "Search text")
            @RequestParam(required = false) String search,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = extractUserId(authHeader);
        PortfolioPositionsResponseDto positions = portfolioService.getPositions(userId, tab, company, search);
        return ResponseEntity.ok(ApiResponse.success(positions, "Portfolio positions retrieved successfully"));
    }

    @GetMapping(value = {"/trades", "/trade-history"})
    @Operation(summary = "Get Trade History", description = "Returns complete Trade History screen data (Screen 5): trade KPI cards, filters, trades table, and selected trade details with 6-stage trade timeline.")
    public ResponseEntity<ApiResponse<PortfolioTradesResponseDto>> getTradeHistory(
            @Parameter(description = "Search by company, trade ID, counterparty")
            @RequestParam(required = false) String search,
            @Parameter(description = "Company filter")
            @RequestParam(required = false, defaultValue = "ALL") String company,
            @Parameter(description = "Trade type filter: ALL, BUY, SELL")
            @RequestParam(required = false, defaultValue = "ALL") String tradeType,
            @Parameter(description = "Status filter: ALL, SETTLED, CANCELLED")
            @RequestParam(required = false, defaultValue = "ALL") String status,
            @Parameter(description = "Start Date (YYYY-MM-DD)")
            @RequestParam(required = false) String startDate,
            @Parameter(description = "End Date (YYYY-MM-DD)")
            @RequestParam(required = false) String endDate,
            @Parameter(description = "Pre-selected trade ID for details panel")
            @RequestParam(required = false) Long selectedTradeId,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = extractUserId(authHeader);
        PortfolioTradesResponseDto trades = portfolioService.getTradeHistory(userId, search, company, tradeType, status, startDate, endDate, selectedTradeId, page, size);
        return ResponseEntity.ok(ApiResponse.success(trades, "Portfolio trade history retrieved successfully"));
    }

    @GetMapping("/trades/{tradeId}")
    @Operation(summary = "Get Trade Detail & Timeline", description = "Returns single trade details, escrow settlement reference, counterparty details, and 6-stage lifecycle stepper.")
    public ResponseEntity<ApiResponse<PortfolioTradeDetailDto>> getTradeDetail(
            @PathVariable("tradeId") Long tradeId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = extractUserId(authHeader);
        PortfolioTradeDetailDto tradeDetail = portfolioService.getTradeDetail(tradeId, null, userId);
        return ResponseEntity.ok(ApiResponse.success(tradeDetail, "Trade detail retrieved successfully"));
    }

    @GetMapping("/analytics")
    @Operation(summary = "Get Portfolio Analytics", description = "Returns complete Portfolio Analytics data (Image 1): KPI cards, allocation by company/sector/type, 2-line value trend, return analysis bars, company performance, asset type distribution, gainers/losers, and key insights.")
    public ResponseEntity<ApiResponse<PortfolioAnalyticsResponseDto>> getAnalytics(
            @Parameter(description = "Allocation Tab: By Company, By Sector, By Investment Type")
            @RequestParam(required = false, defaultValue = "By Company") String allocationTab,
            @Parameter(description = "Trend period: 1M, 3M, 6M, 1Y, All")
            @RequestParam(required = false, defaultValue = "6M") String period,
            @Parameter(description = "Date range string")
            @RequestParam(required = false, defaultValue = "01 Jan 2025 - 18 Sep 2025") String dateRange,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = extractUserId(authHeader);
        PortfolioAnalyticsResponseDto analytics = portfolioService.getAnalytics(userId, allocationTab, period, dateRange);
        return ResponseEntity.ok(ApiResponse.success(analytics, "Portfolio analytics retrieved successfully"));
    }

    @GetMapping("/pnl-report")
    @Operation(summary = "Get P&L Report", description = "Returns complete P&L Report data (Image 2): P&L summary KPIs, realized & unrealized trend chart, summary side card, tabs, company-wise P&L breakdown table, and insights.")
    public ResponseEntity<ApiResponse<PortfolioPnlResponseDto>> getPnlReport(
            @Parameter(description = "P&L Tab: Company-wise P&L, Realized P&L, Unrealized P&L, Sector-wise, Monthly Summary")
            @RequestParam(required = false, defaultValue = "Company-wise P&L") String tab,
            @Parameter(description = "Company filter")
            @RequestParam(required = false, defaultValue = "ALL") String company,
            @Parameter(description = "Sector filter")
            @RequestParam(required = false, defaultValue = "ALL") String sector,
            @Parameter(description = "Date range")
            @RequestParam(required = false, defaultValue = "01 Jan 2025 - 18 Sep 2025") String dateRange,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = extractUserId(authHeader);
        PortfolioPnlResponseDto pnl = portfolioService.getPnlReport(userId, tab, company, sector, dateRange);
        return ResponseEntity.ok(ApiResponse.success(pnl, "P&L report retrieved successfully"));
    }

    @GetMapping("/ownership-statement")
    @Operation(summary = "Get Ownership Statement", description = "Returns official shareholding statement (Image 3): ownership KPI cards, company ownership table, statement details box, ownership distribution donut, and investment vs current value bars.")
    public ResponseEntity<ApiResponse<PortfolioOwnershipResponseDto>> getOwnershipStatement(
            @Parameter(description = "Tab: Company-wise, Sector-wise, Share Class-wise")
            @RequestParam(required = false, defaultValue = "Company-wise") String tab,
            @Parameter(description = "As of date")
            @RequestParam(required = false, defaultValue = "18 Sep 2025") String asOfDate,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = extractUserId(authHeader);
        PortfolioOwnershipResponseDto statement = portfolioService.getOwnershipStatement(userId, tab, asOfDate);
        return ResponseEntity.ok(ApiResponse.success(statement, "Ownership statement retrieved successfully"));
    }

    @GetMapping("/transaction-statement")
    @Operation(summary = "Get Transaction Statement", description = "Returns complete transaction statement (Image 4): credit/debit summary KPIs, paginated transactions table, transaction type breakdown donut, and payment mode distribution.")
    public ResponseEntity<ApiResponse<PortfolioTransactionResponseDto>> getTransactionStatement(
            @Parameter(description = "Search text")
            @RequestParam(required = false) String search,
            @Parameter(description = "Company filter")
            @RequestParam(required = false, defaultValue = "ALL") String company,
            @Parameter(description = "Transaction type filter")
            @RequestParam(required = false, defaultValue = "ALL") String transactionType,
            @Parameter(description = "Status filter")
            @RequestParam(required = false, defaultValue = "ALL") String status,
            @Parameter(description = "Date range")
            @RequestParam(required = false, defaultValue = "01 Jan 2025 - 18 Sep 2025") String dateRange,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = extractUserId(authHeader);
        PortfolioTransactionResponseDto txns = portfolioService.getTransactionStatement(userId, search, company, transactionType, status, dateRange, page, size);
        return ResponseEntity.ok(ApiResponse.success(txns, "Transaction statement retrieved successfully"));
    }

    // =========================================================================
    // EXPORT & REPORT DOWNLOAD ENDPOINTS
    // =========================================================================
    @GetMapping("/download/report")
    @Operation(summary = "Download Portfolio Report", description = "Generates and downloads full portfolio summary report in CSV format for the authenticated member")
    public ResponseEntity<byte[]> downloadReport(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Long userId = requireAuthenticatedUserId(authHeader, "portfolio report");
        String csv = portfolioOverviewService.buildPortfolioReportCsv(userId);
        byte[] bytes = csv.getBytes(StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"portfolio_report_ATG" + userId + ".csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(bytes);
    }

    @GetMapping("/download/holdings")
    @Operation(summary = "Download Holdings", description = "Generates and downloads current shareholdings statement in CSV format")
    public ResponseEntity<byte[]> downloadHoldings(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return downloadReport(authHeader);
    }

    @GetMapping("/download/statement")
    @Operation(summary = "Download Statement", description = "Generates and downloads user account ownership statement in CSV format for the authenticated member")
    public ResponseEntity<byte[]> downloadStatement(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Long userId = requireAuthenticatedUserId(authHeader, "portfolio statement");
        String csv = portfolioOverviewService.buildStatementCsv(userId);
        byte[] bytes = csv.getBytes(StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"ownership_statement_ATG" + userId + ".csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(bytes);
    }

    @GetMapping("/download/analytics")
    @Operation(summary = "Download Analytics Report", description = "Generates and downloads portfolio analytics summary report in CSV format")
    public ResponseEntity<byte[]> downloadAnalytics() {
        String csv = "Metric,Value\n" +
                "Total Portfolio Value,₹ 24,80,000\n" +
                "Total Invested,₹ 18,50,000\n" +
                "Total Return,40.5%\n" +
                "Annualized Return,18.6%\n" +
                "Price Appreciation,12.4%\n" +
                "Dividend Return,6.8%\n";
        byte[] bytes = csv.getBytes(StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"portfolio_analytics_18Sep2025.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(bytes);
    }

    @GetMapping("/download/pnl-report")
    @Operation(summary = "Download P&L Report", description = "Generates and downloads profit & loss statement in CSV format")
    public ResponseEntity<byte[]> downloadPnlReport() {
        String csv = "Company,Invested Amount,Current Value,Realized PnL,Unrealized PnL,Total PnL,Return\n" +
                "Atirath Holdings India Limited,600000,805000,50000,155000,205000,+34.2%\n" +
                "Atirath Technologies,350000,360000,30000,80000,110000,+31.4%\n" +
                "Farm Vantara,280000,390000,25000,85000,110000,+39.3%\n" +
                "Tri Energy Battery,220000,270000,10000,40000,50000,+22.7%\n" +
                "Royal One Heating,170000,211500,5000,36500,41500,+24.4%\n" +
                "CS Interio,110000,87000,0,-23000,-23000,-20.9%\n";
        byte[] bytes = csv.getBytes(StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"pnl_report_18Sep2025.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(bytes);
    }

    @GetMapping("/download/ownership-statement")
    @Operation(summary = "Download Ownership Certificate & Statement", description = "Generates official shareholding ownership certificate and statement in CSV/PDF format")
    public ResponseEntity<byte[]> downloadOwnershipStatement(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return downloadStatement(authHeader);
    }

    @GetMapping("/download/transaction-statement")
    @Operation(summary = "Download Transaction Statement", description = "Generates full portfolio transaction statement in CSV format")
    public ResponseEntity<byte[]> downloadTransactionStatement() {
        String csv = "Txn ID,Date,Company,Type,Quantity,Price,Amount,Payment Mode,Status,Remarks\n" +
                "TXN250918004,18 Sep 2025 11:28 AM,Atirath Holdings,Buy,1000,230.00,230000.00,Bank Transfer,Completed,-\n" +
                "TXN250917021,17 Sep 2025 04:15 PM,CS Interio,Sell,500,145.00,72500.00,Escrow,Completed,-\n" +
                "TXN250915033,15 Sep 2025 12:10 PM,Tri Energy Battery,Buy,600,225.00,135000.00,UPI,Completed,-\n" +
                "TXN250914018,14 Sep 2025 03:45 PM,Royal One Heating,Sell,400,235.00,94000.00,Escrow,Completed,-\n" +
                "TXN250912011,12 Sep 2025 11:20 AM,Farm Vantara,Dividend,-,-,12500.00,Bank Transfer,Completed,Q1 2025\n" +
                "TXN250910026,10 Sep 2025 02:18 PM,Atirath Technologies,Buy,800,180.00,144000.00,Bank Transfer,Completed,-\n" +
                "TXN250908014,08 Sep 2025 10:30 AM,CS Interio,Corporate Action,-,-,-,-,Completed,Bonus Shares (1:1)\n" +
                "TXN250905017,05 Sep 2025 04:05 PM,Atirath Holdings,Sell,300,240.00,72000.00,UPI,Completed,-\n" +
                "TXN250902009,02 Sep 2025 01:22 PM,Tri Energy Battery,Buy,500,210.00,105000.00,Bank Transfer,Pending,Settlement in progress\n" +
                "TXN250830012,30 Aug 2025 11:16 AM,Farm Vantara,Sell,750,140.00,105000.00,Escrow,Completed,-\n";
        byte[] bytes = csv.getBytes(StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"transaction_statement_18Sep2025.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(bytes);
    }

    @GetMapping("/download/order-slip/{orderId}")
    @Operation(summary = "Download Order Slip", description = "Generates and downloads official order slip receipt")
    public ResponseEntity<Map<String, Object>> downloadOrderSlip(@PathVariable("orderId") Long orderId) {
        return ResponseEntity.ok(Map.of(
                "orderId", orderId,
                "orderNo", "ORD250918001",
                "status", "GENERATED",
                "downloadUrl", "/api/v1/portfolio/download/statement",
                "message", "Order slip generated successfully"
        ));
    }

    @GetMapping("/download/trade-receipt/{tradeId}")
    @Operation(summary = "Download Trade Receipt", description = "Generates and downloads trade settlement receipt")
    public ResponseEntity<Map<String, Object>> downloadTradeReceipt(@PathVariable("tradeId") Long tradeId) {
        return ResponseEntity.ok(Map.of(
                "tradeId", tradeId,
                "tradeNo", "TRD250918004",
                "status", "GENERATED",
                "downloadUrl", "/api/v1/portfolio/download/statement",
                "message", "Trade receipt generated successfully"
        ));
    }

    private Long requireAuthenticatedUserId(String authHeader, String resourceLabel) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof AnonymousAuthenticationToken) {
            throw new ApiException("Authentication required to access " + resourceLabel, HttpStatus.UNAUTHORIZED);
        }
        Long userId = extractUserId(authHeader);
        if (userId == null) {
            throw new ApiException("Authentication required to access " + resourceLabel, HttpStatus.UNAUTHORIZED);
        }
        return userId;
    }

    private Long extractUserId(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring(7).trim();
                return jwtTokenProvider.getUserIdFromToken(token);
            } catch (Exception e) {
                log.debug("[PortfolioController] Could not parse JWT: {}", e.getMessage());
            }
        }
        return null;
    }
}
