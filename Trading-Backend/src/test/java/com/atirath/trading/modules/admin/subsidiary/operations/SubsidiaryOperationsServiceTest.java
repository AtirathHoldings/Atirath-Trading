package com.atirath.trading.modules.admin.subsidiary.operations;

import com.atirath.trading.modules.admin.subsidiary.operations.dto.*;
import com.atirath.trading.modules.admin.subsidiary.operations.service.SubsidiaryOperationsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SubsidiaryOperationsServiceTest {

    private SubsidiaryOperationsService service;

    @BeforeEach
    void setUp() {
        service = new SubsidiaryOperationsService();
        service.init();
    }

    // ==========================================
    // SCREEN 1: OPERATIONS DASHBOARD
    // ==========================================
    @Test
    @DisplayName("Screen 1: Operations Dashboard should match KPIs, trend, donut, tables, and widgets")
    void testOperationsDashboard() {
        OperationsDashboardDto dto = service.getOperationsDashboard("Atirath Holdings", "01 Apr 2026 - 30 Sep 2026");

        assertNotNull(dto);
        assertEquals("Operations Dashboard", dto.getPageTitle());
        assertEquals("Atirath Holdings", dto.getSelectedCompany());
        assertEquals("01 Apr 2026 - 30 Sep 2026", dto.getDateRange());

        // 6 KPIs
        assertEquals(6, dto.getKpis().size());
        assertEquals("1,264", dto.getKpis().get(0).getValue());
        assertEquals("+18% vs last quarter", dto.getKpis().get(0).getChange());
        assertEquals("892", dto.getKpis().get(1).getValue());
        assertEquals("+12% vs last quarter", dto.getKpis().get(1).getChange());
        assertEquals("₹ 12.8 Cr", dto.getKpis().get(2).getValue());
        assertEquals("+25% vs last quarter", dto.getKpis().get(2).getChange());
        assertEquals("1,156", dto.getKpis().get(3).getValue());
        assertEquals("+16% vs last quarter", dto.getKpis().get(3).getChange());
        assertEquals("28", dto.getKpis().get(4).getValue());
        assertEquals("+133% vs last quarter", dto.getKpis().get(4).getChange());
        assertEquals("6", dto.getKpis().get(5).getValue());
        assertEquals("+50% vs last quarter", dto.getKpis().get(5).getChange());

        // Operational Alerts
        assertEquals(5, dto.getOperationalAlerts().size());
        assertEquals("28 Failed Transactions", dto.getOperationalAlerts().get(0).getTitle());
        assertEquals("45 Pending Settlements", dto.getOperationalAlerts().get(1).getTitle());

        // Volume Trend (6 months)
        assertEquals(6, dto.getTransactionVolumeTrend().size());
        assertEquals("Apr 2026", dto.getTransactionVolumeTrend().get(0).getMonth());
        assertEquals("Sep 2026", dto.getTransactionVolumeTrend().get(5).getMonth());

        // Donut (2,156 total, 5 slices)
        assertNotNull(dto.getTransactionStatus());
        assertEquals("2,156 Total Transactions", dto.getTransactionStatus().getTotalTransactions());
        assertEquals(5, dto.getTransactionStatus().getSlices().size());
        assertEquals("Completed", dto.getTransactionStatus().getSlices().get(0).getLabel());
        assertEquals(74.1, dto.getTransactionStatus().getSlices().get(0).getPercentage());
        assertEquals("1,598", dto.getTransactionStatus().getSlices().get(0).getCount());

        // Recent Trades (5 rows)
        assertEquals(5, dto.getRecentTrades().size());
        assertEquals("TR-2026-001", dto.getRecentTrades().get(0).getTradeId());
        assertEquals("Atirath Traders", dto.getRecentTrades().get(0).getCompany());
        assertEquals("Completed", dto.getRecentTrades().get(0).getStatus());

        // Recent Transfers (5 rows)
        assertEquals(5, dto.getRecentTransfers().size());
        assertEquals("TF-2026-001", dto.getRecentTransfers().get(0).getTransferId());
        assertEquals("Rakesh Kumar", dto.getRecentTransfers().get(0).getFromParty());
        assertEquals("Priya Mehta", dto.getRecentTransfers().get(0).getToParty());
        assertEquals("Completed", dto.getRecentTransfers().get(0).getStatus());

        // Escrow Monitoring
        assertNotNull(dto.getEscrowMonitoring());
        assertEquals("₹ 12.8 Cr (+25%)", dto.getEscrowMonitoring().getTotalEscrowBalance());
        assertEquals("₹ 2.4 Cr (+12%)", dto.getEscrowMonitoring().getOnHold());
        assertEquals(5, dto.getEscrowMonitoring().getAccounts().size());

        // Settlement Monitoring
        assertNotNull(dto.getSettlementMonitoring());
        assertEquals(45, dto.getSettlementMonitoring().getPendingSettlements());
        assertEquals("1.6 Days (-20%)", dto.getSettlementMonitoring().getAvgSettlementTime());
        assertEquals(5, dto.getSettlementMonitoring().getSettlements().size());
        assertEquals("ST-2026-001", dto.getSettlementMonitoring().getSettlements().get(0).getSettlementId());
        assertEquals("TR-2026-002", dto.getSettlementMonitoring().getSettlements().get(0).getTradeId());

        // Failed Transactions
        assertNotNull(dto.getFailedTransactions());
        assertEquals(28, dto.getFailedTransactions().getTotalFailed());
        assertEquals("78% (+12%)", dto.getFailedTransactions().getResolutionRate());
        assertEquals(5, dto.getFailedTransactions().getTransactions().size());

        // Disputes Overview
        assertNotNull(dto.getDisputesOverview());
        assertEquals(6, dto.getDisputesOverview().getOpenDisputes());
        assertEquals(12, dto.getDisputesOverview().getResolvedThisQuarter());
        assertEquals(3, dto.getDisputesOverview().getDisputes().size());
        assertEquals("DP-2026-002", dto.getDisputesOverview().getDisputes().get(1).getDisputeId());
        assertEquals("TF-2026-005", dto.getDisputesOverview().getDisputes().get(1).getRelatedTxn());

        // Support Tickets
        assertNotNull(dto.getSupportTickets());
        assertEquals(14, dto.getSupportTickets().getOpenTickets());
        assertEquals("1.8 Days (-30%)", dto.getSupportTickets().getAvgResolutionTime());
        assertEquals(3, dto.getSupportTickets().getTickets().size());
    }

    // ==========================================
    // SCREEN 2: PENDING TRADES
    // ==========================================
    @Test
    @DisplayName("Screen 2: Pending Trades page should match 5 KPIs, tabs, donut, trend, 10 table rows, drawer, and mutations")
    void testPendingTradesPage() {
        PendingTradesPageDto dto = service.getPendingTradesPage("Atirath Holdings", "All Pending Trades", null, null, null, null);

        assertNotNull(dto);
        assertEquals("Pending Trades", dto.getPageTitle());
        assertEquals(156, dto.getTotalTrades());
        assertEquals(16, dto.getTotalPages());

        // 5 KPIs
        assertEquals(5, dto.getKpis().size());
        assertEquals("156", dto.getKpis().get(0).getValue());
        assertEquals("+28% vs last quarter", dto.getKpis().get(0).getChange());
        assertEquals("48", dto.getKpis().get(1).getValue());
        assertEquals("+20% vs last quarter", dto.getKpis().get(1).getChange());
        assertEquals("36", dto.getKpis().get(2).getValue());
        assertEquals("+33% vs last quarter", dto.getKpis().get(2).getChange());
        assertEquals("52", dto.getKpis().get(3).getValue());
        assertEquals("+35% vs last quarter", dto.getKpis().get(3).getChange());
        assertEquals("20", dto.getKpis().get(4).getValue());
        assertEquals("+67% vs last quarter", dto.getKpis().get(4).getChange());

        // Tabs
        assertEquals(6, dto.getTabs().size());
        assertTrue(dto.getTabs().contains("All Pending Trades"));
        assertTrue(dto.getTabs().contains("Awaiting Approval"));
        assertTrue(dto.getTabs().contains("Pending Verification"));
        assertTrue(dto.getTabs().contains("Pending Settlement"));
        assertTrue(dto.getTabs().contains("Overdue"));
        assertTrue(dto.getTabs().contains("On Hold"));

        // Donut
        assertNotNull(dto.getPendingTradesByStatus());
        assertEquals(156, dto.getPendingTradesByStatus().getTotalPendingTrades());
        assertEquals(4, dto.getPendingTradesByStatus().getSlices().size());
        assertEquals("Approval", dto.getPendingTradesByStatus().getSlices().get(0).getLabel());
        assertEquals(30.8, dto.getPendingTradesByStatus().getSlices().get(0).getPercentage());
        assertEquals(48, dto.getPendingTradesByStatus().getSlices().get(0).getCount());

        // Trend (6 months)
        assertEquals(6, dto.getPendingTradesTrend().size());
        assertEquals("Apr 2026", dto.getPendingTradesTrend().get(0).getMonth());
        assertEquals("Sep 2026", dto.getPendingTradesTrend().get(5).getMonth());

        // 10 Table rows
        assertEquals(10, dto.getTrades().size());
        PendingTradesPageDto.PendingTradeItemDto row1 = dto.getTrades().get(0);
        assertEquals("TR-2026-001", row1.getTradeId());
        assertEquals("Atirath Traders", row1.getCompany());
        assertEquals("Equity - A", row1.getShareClass());
        assertEquals("Rakesh Kumar", row1.getInvestor());
        assertEquals("Buy", row1.getType());
        assertEquals("10,000", row1.getQtyOrShares());
        assertEquals("15,00,000", row1.getValue());
        assertEquals("Approval", row1.getStatus());
        assertEquals("1 day", row1.getPendingSince());

        PendingTradesPageDto.PendingTradeItemDto row6 = dto.getTrades().get(5);
        assertEquals("TR-2026-006", row6.getTradeId());
        assertEquals("Settlement", row6.getStatus());

        // Drawer
        TradeDetailDto drawer = dto.getSelectedTradeDetail();
        assertNotNull(drawer);
        assertEquals("TR-2026-001", drawer.getTradeId());
        assertEquals("Buy Order", drawer.getOrderTypeTitle());
        assertEquals("Awaiting Approval", drawer.getStatus());
        assertEquals("Atirath Traders", drawer.getCompany());
        assertEquals("Equity - A", drawer.getShareClass());
        assertEquals("Rakesh Kumar", drawer.getInvestor());
        assertEquals("10,000", drawer.getNoOfShares());
        assertEquals("150", drawer.getPricePerShare());
        assertEquals("15,00,000", drawer.getTotalValue());
        assertEquals("1 day", drawer.getPendingSince());
        assertEquals("Pending", drawer.getComplianceReviewStatus());
        assertEquals("Not Started", drawer.getRiskApprovalStatus());
        assertEquals("INE123A01034", drawer.getIsin());
        assertEquals("Web Portal", drawer.getOrderSource());

        // Mutation: add trade
        AddTradeRequestDto addReq = AddTradeRequestDto.builder()
                .company("Farm Vantara")
                .shareClass("Equity - B")
                .investor("Sachin Tendulkar")
                .type("Buy")
                .noOfShares("5,000")
                .totalValue("7,50,000")
                .pricePerShare(150.0)
                .build();
        TradeDetailDto created = service.addTrade(addReq);
        assertNotNull(created);
        assertEquals("Sachin Tendulkar", created.getInvestor());

        // Mutation: approve trade
        TradeDetailDto approved = service.approveTrade("tr-1");
        assertEquals("Approved", approved.getStatus());

        // Mutation: reject trade
        TradeDetailDto rejected = service.rejectTrade("tr-1");
        assertEquals("Rejected", rejected.getStatus());
    }

    // ==========================================
    // SCREEN 3: PENDING TRANSFERS
    // ==========================================
    @Test
    @DisplayName("Screen 3: Pending Transfers page should match 5 KPIs, tabs, donut, trend, 10 table rows, drawer, and mutations")
    void testPendingTransfersPage() {
        PendingTransfersPageDto dto = service.getPendingTransfersPage("Atirath Holdings", "All Transfers", null, null, null, null);

        assertNotNull(dto);
        assertEquals("Pending Transfers", dto.getPageTitle());
        assertEquals(892, dto.getTotalTransfers());
        assertEquals(90, dto.getTotalPages());

        // 5 KPIs
        assertEquals(5, dto.getKpis().size());
        assertEquals("892", dto.getKpis().get(0).getValue());
        assertEquals("+12% vs last quarter", dto.getKpis().get(0).getChange());
        assertEquals("214", dto.getKpis().get(1).getValue());
        assertEquals("+25% vs last quarter", dto.getKpis().get(1).getChange());
        assertEquals("176", dto.getKpis().get(2).getValue());
        assertEquals("+18% vs last quarter", dto.getKpis().get(2).getChange());
        assertEquals("312", dto.getKpis().get(3).getValue());
        assertEquals("+10% vs last quarter", dto.getKpis().get(3).getChange());
        assertEquals("90", dto.getKpis().get(4).getValue());
        assertEquals("+67% vs last quarter", dto.getKpis().get(4).getChange());

        // Tabs
        assertEquals(7, dto.getTabs().size());
        assertTrue(dto.getTabs().contains("All Transfers"));
        assertTrue(dto.getTabs().contains("Awaiting Approval"));
        assertTrue(dto.getTabs().contains("Pending Verification"));
        assertTrue(dto.getTabs().contains("Pending Settlement"));
        assertTrue(dto.getTabs().contains("Overdue"));
        assertTrue(dto.getTabs().contains("Rejected"));
        assertTrue(dto.getTabs().contains("Completed"));

        // Donut
        assertNotNull(dto.getTransfersByStatus());
        assertEquals(892, dto.getTransfersByStatus().getTotalTransfers());
        assertEquals(5, dto.getTransfersByStatus().getSlices().size());
        assertEquals("Approval", dto.getTransfersByStatus().getSlices().get(0).getLabel());
        assertEquals(24.0, dto.getTransfersByStatus().getSlices().get(0).getPercentage());
        assertEquals(214, dto.getTransfersByStatus().getSlices().get(0).getCount());

        // Trend (6 months)
        assertEquals(6, dto.getTransfersTrend().size());
        assertEquals("Apr 2026", dto.getTransfersTrend().get(0).getMonth());
        assertEquals("Sep 2026", dto.getTransfersTrend().get(5).getMonth());

        // 10 Table rows
        assertEquals(10, dto.getTransfers().size());
        PendingTransfersPageDto.PendingTransferItemDto row1 = dto.getTransfers().get(0);
        assertEquals("TF-2026-001", row1.getTransferId());
        assertEquals("29 Sep 2026", row1.getTransferDate());
        assertEquals("Rakesh Kumar", row1.getFromParty());
        assertEquals("Priya Mehta", row1.getToParty());
        assertEquals("10,000", row1.getShares());
        assertEquals("Equity - A", row1.getShareClass());
        assertEquals("Transfer", row1.getType());
        assertEquals("Approval", row1.getStatus());

        // Drawer
        TransferDetailDto drawer = dto.getSelectedTransferDetail();
        assertNotNull(drawer);
        assertEquals("TF-2026-001", drawer.getTransferId());
        assertEquals("Share Transfer", drawer.getTransferTypeTitle());
        assertEquals("Awaiting Approval", drawer.getStatus());
        assertEquals("Transfer", drawer.getType());
        assertEquals("Rakesh Kumar", drawer.getFromParty());
        assertEquals("Priya Mehta", drawer.getToParty());
        assertEquals("10,000", drawer.getShares());
        assertEquals("Equity - A", drawer.getShareClass());
        assertEquals("150", drawer.getPricePerShare());
        assertEquals("15,00,000", drawer.getTotalValue());
        assertEquals("29 Sep 2026", drawer.getTransferDate());
        assertEquals("Pending", drawer.getComplianceReviewStatus());
        assertEquals("Secondary Sale", drawer.getReason());

        // Mutation: initiate transfer
        InitiateTransferRequestDto initReq = InitiateTransferRequestDto.builder()
                .fromParty("Sunil Gavaskar")
                .toParty("Kapil Dev")
                .shares("5,000")
                .shareClass("Equity - B")
                .type("Transfer")
                .pricePerShare(150.0)
                .totalValue("7,50,000")
                .build();
        TransferDetailDto created = service.initiateTransfer(initReq);
        assertNotNull(created);
        assertEquals("Sunil Gavaskar", created.getFromParty());

        // Mutation: approve transfer
        TransferDetailDto approved = service.approveTransfer("tf-1");
        assertEquals("Approved", approved.getStatus());

        // Mutation: reject transfer
        TransferDetailDto rejected = service.rejectTransfer("tf-1");
        assertEquals("Rejected", rejected.getStatus());
    }

    // ==========================================
    // 4. ESCROW MONITORING PAGE
    // ==========================================
    @Test
    @DisplayName("Screen 4: Escrow Monitoring - KPIs, Trend, Donut, Accounts, Releases, Exceptions & Drawer")
    void testEscrowMonitoringPage() {
        EscrowMonitoringPageDto page = service.getEscrowMonitoringPage("Atirath Holdings", "All Escrow Accounts", null, null, null);
        assertNotNull(page);
        assertEquals("Escrow Monitoring", page.getPageTitle());
        assertEquals("Atirath Holdings", page.getSelectedCompany());

        // 5 KPIs
        assertEquals(5, page.getKpis().size());
        assertEquals("Total Escrow Balance", page.getKpis().get(0).getTitle());
        assertEquals("₹ 12.8 Cr", page.getKpis().get(0).getValue());
        assertEquals("+25% vs last quarter", page.getKpis().get(0).getChange());

        assertEquals("On Hold", page.getKpis().get(1).getTitle());
        assertEquals("₹ 2.4 Cr", page.getKpis().get(1).getValue());

        assertEquals("Released (This Quarter)", page.getKpis().get(2).getTitle());
        assertEquals("₹ 8.6 Cr", page.getKpis().get(2).getValue());

        assertEquals("Pending Release", page.getKpis().get(3).getTitle());
        assertEquals("₹ 1.8 Cr", page.getKpis().get(3).getValue());

        assertEquals("Exceptions", page.getKpis().get(4).getTitle());
        assertEquals("6", page.getKpis().get(4).getValue());
        assertEquals("+200% vs last quarter", page.getKpis().get(4).getChange());

        // 6 Tabs
        assertEquals(6, page.getTabs().size());
        assertTrue(page.getTabs().contains("All Escrow Accounts"));
        assertTrue(page.getTabs().contains("On Hold"));
        assertTrue(page.getTabs().contains("Pending Release"));
        assertTrue(page.getTabs().contains("Released"));
        assertTrue(page.getTabs().contains("Disputed"));
        assertTrue(page.getTabs().contains("Exceptions"));

        // Donut
        assertNotNull(page.getStatusDistribution());
        assertEquals("12.8 Cr Total Balance", page.getStatusDistribution().getTotalBalance());
        assertEquals(4, page.getStatusDistribution().getSlices().size());

        // Accounts Table
        assertEquals(10, page.getAccounts().size());
        assertEquals("ES-2026-001", page.getAccounts().get(0).getEscrowId());
        assertEquals("Atirath Traders", page.getAccounts().get(0).getCompany());
        assertEquals("3,25,00,000", page.getAccounts().get(0).getAmount());
        assertEquals("Available", page.getAccounts().get(0).getStatus());

        // Row 9 check
        assertEquals("ES-2026-009", page.getAccounts().get(8).getEscrowId());
        assertEquals("2,50,00,000", page.getAccounts().get(8).getAmount());

        // Upcoming Releases (5 items)
        assertEquals(5, page.getUpcomingReleases().size());
        assertEquals("ES-2026-004", page.getUpcomingReleases().get(0).getEscrowId());
        assertEquals("ES-2026-006", page.getUpcomingReleases().get(3).getEscrowId());

        // Escrow Exceptions (5 items)
        assertEquals(5, page.getEscrowExceptions().size());
        assertEquals("ES-2026-005", page.getEscrowExceptions().get(0).getEscrowId());
        assertEquals("Dispute", page.getEscrowExceptions().get(0).getType());

        // Side Drawer
        EscrowDetailDto drawer = page.getSelectedEscrowDetail();
        assertNotNull(drawer);
        assertEquals("ES-2026-001", drawer.getEscrowId());
        assertEquals("Atirath Traders", drawer.getCompanyTitle());
        assertEquals("Available", drawer.getStatus());
        assertEquals("Rakesh Kumar", drawer.getInvestor());
        assertEquals("HDFC Bank", drawer.getEscrowBank());
        assertEquals("50200012345678", drawer.getAccountNo());
        assertEquals("HDFC0001234", drawer.getIfscCode());
        assertEquals("3,25,00,000", drawer.getAvailableAmount());
        assertEquals("Verified", drawer.getKycStatus());

        // Mutations
        EscrowDetailDto held = service.placeEscrowHold("es-1");
        assertEquals("On Hold", held.getStatus());
        EscrowDetailDto released = service.releaseEscrowFunds("es-1");
        assertEquals("Released", released.getStatus());
    }

    // ==========================================
    // 5. SETTLEMENT MONITORING PAGE
    // ==========================================
    @Test
    @DisplayName("Screen 5: Settlement Monitoring - KPIs, Volume Trend, Donut, Table, Upcoming, Failed & Drawer")
    void testSettlementMonitoringPage() {
        SettlementMonitoringPageDto page = service.getSettlementMonitoringPage("Atirath Holdings", "All Settlements", null, null, null);
        assertNotNull(page);
        assertEquals("Settlement Monitoring", page.getPageTitle());
        assertEquals("Atirath Holdings", page.getSelectedCompany());

        // 5 KPIs
        assertEquals(5, page.getKpis().size());
        assertEquals("Total Trade Settlements", page.getKpis().get(0).getTitle());
        assertEquals("1,156", page.getKpis().get(0).getValue());
        assertEquals("+16% vs last quarter", page.getKpis().get(0).getChange());

        assertEquals("Settled", page.getKpis().get(1).getTitle());
        assertEquals("982", page.getKpis().get(1).getValue());
        assertEquals("+18% vs last quarter", page.getKpis().get(1).getChange());

        assertEquals("Pending Settlement", page.getKpis().get(2).getTitle());
        assertEquals("108", page.getKpis().get(2).getValue());
        assertEquals("+25% vs last quarter", page.getKpis().get(2).getChange());

        assertEquals("Failed Settlements", page.getKpis().get(3).getTitle());
        assertEquals("28", page.getKpis().get(3).getValue());
        assertEquals("+133% vs last quarter", page.getKpis().get(3).getChange());

        assertEquals("Avg. Settlement Time", page.getKpis().get(4).getTitle());
        assertEquals("1.6 Days", page.getKpis().get(4).getValue());
        assertEquals("-20% vs last quarter", page.getKpis().get(4).getChange());

        // 5 Tabs
        assertEquals(5, page.getTabs().size());
        assertTrue(page.getTabs().contains("All Settlements"));
        assertTrue(page.getTabs().contains("Pending"));
        assertTrue(page.getTabs().contains("Failed"));
        assertTrue(page.getTabs().contains("On Hold"));
        assertTrue(page.getTabs().contains("Under Review"));

        // Donut
        assertNotNull(page.getStatusDistribution());
        assertEquals(1156, page.getStatusDistribution().getTotalSettlements());
        assertEquals(5, page.getStatusDistribution().getSlices().size());

        // Table
        assertEquals(10, page.getSettlements().size());
        assertEquals("ST-2026-001", page.getSettlements().get(0).getSettlementId());
        assertEquals("TR-2026-001", page.getSettlements().get(0).getTradeId());
        assertEquals("Settled", page.getSettlements().get(0).getStatus());

        assertEquals("ST-2026-002", page.getSettlements().get(1).getSettlementId());
        assertEquals("Pending", page.getSettlements().get(1).getStatus());

        // Row 9 company
        assertEquals("ST-2026-009", page.getSettlements().get(8).getSettlementId());
        assertEquals("Priya Mehta", page.getSettlements().get(8).getCompany());

        // Upcoming (5 items)
        assertEquals(5, page.getUpcomingSettlements().size());
        assertEquals("ST-2026-011", page.getUpcomingSettlements().get(0).getSettlementId());

        // Failed (5 items)
        assertEquals(5, page.getFailedSettlements().size());
        assertEquals("ST-2026-004", page.getFailedSettlements().get(0).getSettlementId());
        assertEquals("Insufficient funds", page.getFailedSettlements().get(0).getReason());

        // Side Drawer
        SettlementDetailDto drawer = page.getSelectedSettlementDetail();
        assertNotNull(drawer);
        assertEquals("ST-2026-002", drawer.getSettlementId());
        assertEquals("Trade Settlement", drawer.getTradeSettlementTitle());
        assertEquals("Pending", drawer.getStatus());
        assertEquals("Farm Vantara", drawer.getCompany());
        assertEquals("Priya Mehta", drawer.getInvestor());
        assertEquals("18,50,000", drawer.getAmount());
        assertEquals("HDFC Bank", drawer.getSettlementBank());
        assertEquals("HDFCTST123456", drawer.getReferenceNo());
        assertEquals(4, drawer.getSteps().size());
        assertEquals("Completed", drawer.getSteps().get(0).getStatus());
        assertEquals("Pending", drawer.getSteps().get(1).getStatus());

        // Mutations
        SettlementDetailDto settled = service.markSettlementSettled("st-2");
        assertEquals("Settled", settled.getStatus());
        SettlementDetailDto failedMut = service.markSettlementFailed("st-2");
        assertEquals("Failed", failedMut.getStatus());
    }

    // ==========================================
    // 6. FAILED TRANSACTIONS PAGE
    // ==========================================
    @Test
    @DisplayName("Screen 6: Failed Transactions - KPIs, Donut, Reasons Bar, Trend, Table & Drawer")
    void testFailedTransactionsPage() {
        FailedTransactionsPageDto page = service.getFailedTransactionsPage("Atirath Holdings", "All Failed Transactions", null, null, null);
        assertNotNull(page);
        assertEquals("Failed Transactions", page.getPageTitle());
        assertEquals("Atirath Holdings", page.getSelectedCompany());

        // 5 KPIs
        assertEquals(5, page.getKpis().size());
        assertEquals("Total Failed Transactions", page.getKpis().get(0).getTitle());
        assertEquals("28", page.getKpis().get(0).getValue());
        assertEquals("+133% vs last quarter", page.getKpis().get(0).getChange());

        assertEquals("Failed Trades", page.getKpis().get(1).getTitle());
        assertEquals("12", page.getKpis().get(1).getValue());
        assertEquals("+100% vs last quarter", page.getKpis().get(1).getChange());

        assertEquals("Failed Transfers", page.getKpis().get(2).getTitle());
        assertEquals("6", page.getKpis().get(2).getValue());
        assertEquals("+50% vs last quarter", page.getKpis().get(2).getChange());

        assertEquals("Failed Settlements", page.getKpis().get(3).getTitle());
        assertEquals("8", page.getKpis().get(3).getValue());
        assertEquals("+167% vs last quarter", page.getKpis().get(3).getChange());

        assertEquals("Failed Payments", page.getKpis().get(4).getTitle());
        assertEquals("2", page.getKpis().get(4).getValue());
        assertEquals("+100% vs last quarter", page.getKpis().get(4).getChange());

        // 5 Tabs
        assertEquals(5, page.getTabs().size());
        assertTrue(page.getTabs().contains("All Failed Transactions"));
        assertTrue(page.getTabs().contains("Trades"));
        assertTrue(page.getTabs().contains("Transfers"));
        assertTrue(page.getTabs().contains("Settlements"));
        assertTrue(page.getTabs().contains("Payments"));

        // Donut by Type
        assertNotNull(page.getFailedTransactionsByType());
        assertEquals("28 Failed", page.getFailedTransactionsByType().getTotalFailed());
        assertEquals(4, page.getFailedTransactionsByType().getSlices().size());

        // Failure Reasons Bar Chart
        assertEquals(8, page.getFailureReasonsDistribution().size());
        assertEquals("Insufficient balance", page.getFailureReasonsDistribution().get(0).getReason());
        assertEquals(6, page.getFailureReasonsDistribution().get(0).getCount());

        // Table (10 rows)
        assertEquals(10, page.getTransactions().size());
        assertEquals("FT-2026-001", page.getTransactions().get(0).getTransactionId());
        assertEquals("Trade", page.getTransactions().get(0).getType());
        assertEquals("15,75,000", page.getTransactions().get(0).getAmount());
        assertEquals("Insufficient balance", page.getTransactions().get(0).getFailureReason());
        assertEquals("Not Retried", page.getTransactions().get(0).getRetryStatus());

        // Row 3 (FT-2026-003)
        assertEquals("FT-2026-003", page.getTransactions().get(2).getTransactionId());
        assertEquals("Settlement", page.getTransactions().get(2).getType());
        assertEquals("Failed", page.getTransactions().get(2).getRetryStatus());

        // Side Drawer
        FailedTransactionDetailDto drawer = page.getSelectedTransactionDetail();
        assertNotNull(drawer);
        assertEquals("FT-2026-001", drawer.getTransactionId());
        assertEquals("Trade Transaction", drawer.getTransactionTypeTitle());
        assertEquals("Failed", drawer.getStatus());
        assertEquals("Atirath Traders", drawer.getCompany());
        assertEquals("Rakesh Kumar", drawer.getInvestor());
        assertEquals("Insufficient balance", drawer.getFailureReason());
        assertEquals("ERR_001", drawer.getErrorCode());
        assertEquals("Available balance is less than required amount.", drawer.getErrorMessage());
        assertEquals("Not Retried", drawer.getRetryStatus());
        assertEquals("Enabled", drawer.getAutoRetry());
        assertEquals("0/3", drawer.getRetryCount());

        // Mutations
        FailedTransactionDetailDto retried = service.retryFailedTransaction("ft-1");
        assertEquals("Retrying", retried.getRetryStatus());
        FailedTransactionDetailDto cancelled = service.cancelFailedTransaction("ft-1");
        assertEquals("Cancelled", cancelled.getStatus());
    }

    // ==========================================
    // 7. RECONCILIATION PAGE
    // ==========================================
    @Test
    @DisplayName("Screen 7: Reconciliation - KPIs, Trend, Donut, Records Table, Exceptions & Drawer")
    void testReconciliationPage() {
        ReconciliationPageDto page = service.getReconciliationPage("Atirath Holdings", "All Records", null, null, null);
        assertNotNull(page);
        assertEquals("Reconciliation", page.getPageTitle());
        assertEquals("Atirath Holdings", page.getSelectedCompany());

        // 5 KPIs
        assertEquals(5, page.getKpis().size());
        assertEquals("Total Records", page.getKpis().get(0).getTitle());
        assertEquals("12,856", page.getKpis().get(0).getValue());
        assertEquals("+18% vs last quarter", page.getKpis().get(0).getChange());

        assertEquals("Matched", page.getKpis().get(1).getTitle());
        assertEquals("11,942", page.getKpis().get(1).getValue());
        assertEquals("+15% vs last quarter", page.getKpis().get(1).getChange());

        assertEquals("Unmatched", page.getKpis().get(2).getTitle());
        assertEquals("724", page.getKpis().get(2).getValue());
        assertEquals("+67% vs last quarter", page.getKpis().get(2).getChange());

        assertEquals("Under Review", page.getKpis().get(3).getTitle());
        assertEquals("142", page.getKpis().get(3).getValue());
        assertEquals("+45% vs last quarter", page.getKpis().get(3).getChange());

        assertEquals("Exceptions", page.getKpis().get(4).getTitle());
        assertEquals("48", page.getKpis().get(4).getValue());
        assertEquals("+100% vs last quarter", page.getKpis().get(4).getChange());

        // 5 Tabs
        assertEquals(5, page.getTabs().size());
        assertTrue(page.getTabs().contains("All Records"));
        assertTrue(page.getTabs().contains("Matched"));
        assertTrue(page.getTabs().contains("Unmatched"));
        assertTrue(page.getTabs().contains("Under Review"));
        assertTrue(page.getTabs().contains("Exceptions"));

        // Donut
        assertNotNull(page.getStatusDistribution());
        assertEquals("12,856 Total Records", page.getStatusDistribution().getTotalRecords());
        assertEquals(4, page.getStatusDistribution().getSlices().size());

        // Records Table (10 rows)
        assertEquals(10, page.getRecords().size());
        assertEquals("RC-2026-001", page.getRecords().get(0).getRecordId());
        assertEquals("Trade", page.getRecords().get(0).getRecordType());
        assertEquals("Matched", page.getRecords().get(0).getStatus());

        assertEquals("RC-2026-003", page.getRecords().get(2).getRecordId());
        assertEquals("Transfer", page.getRecords().get(2).getRecordType());
        assertEquals("Unmatched", page.getRecords().get(2).getStatus());

        // Top Exceptions (5 items)
        assertEquals(5, page.getTopReconciliationExceptions().size());
        assertEquals("RC-2026-008", page.getTopReconciliationExceptions().get(0).getRecordId());
        assertEquals("Bank amount mismatch", page.getTopReconciliationExceptions().get(0).getReason());

        // Side Drawer
        ReconciliationDetailDto drawer = page.getSelectedReconciliationDetail();
        assertNotNull(drawer);
        assertEquals("RC-2026-001", drawer.getReconciliationId());
        assertEquals("Monthly Reconciliation", drawer.getReconciliationTitle());
        assertEquals("Under Review", drawer.getStatus());
        assertEquals("Monthly", drawer.getType());
        assertEquals("Sep 2026", drawer.getPeriod());
        assertEquals("Atirath Holdings", drawer.getCompany());
        assertEquals(12856, drawer.getTotalRecords());
        assertEquals(11942, drawer.getMatchedRecords());
        assertEquals(724, drawer.getUnmatchedRecords());
        assertEquals(142, drawer.getUnderReview());
        assertEquals(48, drawer.getExceptions());
        assertEquals("Rakesh Kumar", drawer.getCreatedBy());

        // Mutations
        ReconciliationDetailDto approved = service.approveReconciliation("rc-1");
        assertEquals("Approved", approved.getStatus());
        ReconciliationDetailDto rejected = service.rejectReconciliation("rc-1");
        assertEquals("Rejected", rejected.getStatus());
        ReconciliationDetailDto reviewed = service.markReconciliationReviewed("rc-1");
        assertEquals("Reviewed", reviewed.getStatus());
    }

    // ==========================================
    // 8. DISPUTES PAGE
    // ==========================================
    @Test
    @DisplayName("Screen 8: Disputes - 6 KPIs, Donut, Status Bar, Trend, Disputes Table & Drawer")
    void testDisputesPage() {
        DisputesPageDto page = service.getDisputesPage("Atirath Holdings", "All Disputes", null, null, null, null);
        assertNotNull(page);
        assertEquals("Disputes", page.getPageTitle());
        assertEquals("Atirath Holdings", page.getSelectedCompany());

        // 6 KPIs
        assertEquals(6, page.getKpis().size());
        assertEquals("Total Disputes", page.getKpis().get(0).getTitle());
        assertEquals("36", page.getKpis().get(0).getValue());
        assertEquals("+20% vs last quarter", page.getKpis().get(0).getChange());

        assertEquals("Open Disputes", page.getKpis().get(1).getTitle());
        assertEquals("18", page.getKpis().get(1).getValue());
        assertEquals("+50% vs last quarter", page.getKpis().get(1).getChange());

        assertEquals("In Progress", page.getKpis().get(2).getTitle());
        assertEquals("10", page.getKpis().get(2).getValue());
        assertEquals("+11% vs last quarter", page.getKpis().get(2).getChange());

        assertEquals("Resolved", page.getKpis().get(3).getTitle());
        assertEquals("6", page.getKpis().get(3).getValue());
        assertEquals("+50% vs last quarter", page.getKpis().get(3).getChange());

        assertEquals("Escalated", page.getKpis().get(4).getTitle());
        assertEquals("2", page.getKpis().get(4).getValue());
        assertEquals("+100% vs last quarter", page.getKpis().get(4).getChange());

        assertEquals("Avg. Resolution Time", page.getKpis().get(5).getTitle());
        assertEquals("5.8 Days", page.getKpis().get(5).getValue());
        assertEquals("-25% vs last quarter", page.getKpis().get(5).getChange());

        // 6 Tabs
        assertEquals(6, page.getTabs().size());
        assertTrue(page.getTabs().contains("All Disputes"));
        assertTrue(page.getTabs().contains("Open"));
        assertTrue(page.getTabs().contains("In Progress"));
        assertTrue(page.getTabs().contains("Resolved"));
        assertTrue(page.getTabs().contains("Escalated"));
        assertTrue(page.getTabs().contains("My Disputes"));

        // Donut by Type
        assertNotNull(page.getDisputesByType());
        assertEquals("36 Total Disputes", page.getDisputesByType().getTotalDisputes());
        assertEquals(5, page.getDisputesByType().getSlices().size());

        // Status Bar
        assertEquals(4, page.getDisputesByStatus().size());
        assertEquals("Open", page.getDisputesByStatus().get(0).getStatus());
        assertEquals(18, page.getDisputesByStatus().get(0).getCount());

        // Table (10 rows)
        assertEquals(10, page.getDisputes().size());
        assertEquals("DP-2026-001", page.getDisputes().get(0).getDisputeId());
        assertEquals("Trade Dispute", page.getDisputes().get(0).getType());
        assertEquals("Open", page.getDisputes().get(0).getStatus());

        assertEquals("DP-2026-003", page.getDisputes().get(2).getDisputeId());
        assertEquals("Escalated", page.getDisputes().get(2).getStatus());

        // Side Drawer
        DisputeDetailDto drawer = page.getSelectedDisputeDetail();
        assertNotNull(drawer);
        assertEquals("DP-2026-001", drawer.getDisputeId());
        assertEquals("Trade Dispute", drawer.getDisputeTypeTitle());
        assertEquals("Open", drawer.getStatus());
        assertEquals("Rakesh Kumar", drawer.getInvestor());
        assertEquals("Atirath Traders", drawer.getCounterparty());
        assertEquals("15,00,000", drawer.getAmount());
        assertEquals("High", drawer.getPriority());
        assertEquals("Investigation", drawer.getStage());
        assertEquals("1 day", drawer.getDaysElapsed());
        assertEquals("6 days", drawer.getDaysRemaining());

        // Mutations
        DisputeDetailDto resolved = service.resolveDispute("dp-1");
        assertEquals("Resolved", resolved.getStatus());
        DisputeDetailDto escalated = service.escalateDispute("dp-1");
        assertEquals("Escalated", escalated.getStatus());
    }

    // ==========================================
    // 9. SUPPORT TICKETS PAGE
    // ==========================================
    @Test
    @DisplayName("Screen 9: Support Tickets - 6 KPIs, Category Donut, Status Bar, Table & Drawer")
    void testSupportTicketsPage() {
        SupportTicketsPageDto page = service.getSupportTicketsPage("Atirath Holdings", "All Tickets", null, null, null, null);
        assertNotNull(page);
        assertEquals("Support Tickets", page.getPageTitle());
        assertEquals("Atirath Holdings", page.getSelectedCompany());

        // 6 KPIs
        assertEquals(6, page.getKpis().size());
        assertEquals("Total Tickets", page.getKpis().get(0).getTitle());
        assertEquals("142", page.getKpis().get(0).getValue());
        assertEquals("+18% vs last quarter", page.getKpis().get(0).getChange());

        assertEquals("Open", page.getKpis().get(1).getTitle());
        assertEquals("36", page.getKpis().get(1).getValue());
        assertEquals("+20% vs last quarter", page.getKpis().get(1).getChange());

        assertEquals("In Progress", page.getKpis().get(2).getTitle());
        assertEquals("58", page.getKpis().get(2).getValue());
        assertEquals("+12% vs last quarter", page.getKpis().get(2).getChange());

        assertEquals("Resolved", page.getKpis().get(3).getTitle());
        assertEquals("40", page.getKpis().get(3).getValue());
        assertEquals("+25% vs last quarter", page.getKpis().get(3).getChange());

        assertEquals("Closed", page.getKpis().get(4).getTitle());
        assertEquals("8", page.getKpis().get(4).getValue());
        assertEquals("+14% vs last quarter", page.getKpis().get(4).getChange());

        assertEquals("Avg. Resolution Time", page.getKpis().get(5).getTitle());
        assertEquals("2.6 Days", page.getKpis().get(5).getValue());
        assertEquals("-30% vs last quarter", page.getKpis().get(5).getChange());

        // 6 Tabs
        assertEquals(6, page.getTabs().size());
        assertTrue(page.getTabs().contains("All Tickets"));
        assertTrue(page.getTabs().contains("Open"));
        assertTrue(page.getTabs().contains("In Progress"));
        assertTrue(page.getTabs().contains("Resolved"));
        assertTrue(page.getTabs().contains("Closed"));
        assertTrue(page.getTabs().contains("My Tickets"));

        // Category Donut
        assertNotNull(page.getTicketsByCategory());
        assertEquals("142 Total Tickets", page.getTicketsByCategory().getTotalTickets());
        assertEquals(8, page.getTicketsByCategory().getSlices().size());

        // Status Bar
        assertEquals(4, page.getTicketsByStatus().size());
        assertEquals("Open", page.getTicketsByStatus().get(0).getStatus());
        assertEquals(36, page.getTicketsByStatus().get(0).getCount());

        // Tickets Table (10 rows)
        assertEquals(10, page.getTickets().size());
        assertEquals("TK-2026-001", page.getTickets().get(0).getTicketId());
        assertEquals("Trade not reflected", page.getTickets().get(0).getSubject());
        assertEquals("Open", page.getTickets().get(0).getStatus());

        // Row 8 check
        assertEquals("TK-2026-008", page.getTickets().get(7).getTicketId());
        assertEquals("Low", page.getTickets().get(7).getPriority());
        assertEquals("Closed", page.getTickets().get(7).getStatus());
        assertEquals("23 Sep 2026", page.getTickets().get(7).getExpectedResolution());

        // Side Drawer
        SupportTicketDetailDto drawer = page.getSelectedTicketDetail();
        assertNotNull(drawer);
        assertEquals("TK-2026-001", drawer.getTicketId());
        assertEquals("Trade not reflected", drawer.getSubject());
        assertEquals("Open", drawer.getStatus());
        assertEquals("Trade", drawer.getCategory());
        assertEquals("Atirath Traders", drawer.getCompany());
        assertEquals("Rakesh Kumar", drawer.getRaisedBy());
        assertEquals("High", drawer.getPriority());
        assertEquals("1 day", drawer.getSlaTarget());
        assertEquals("8 hours", drawer.getTimeElapsed());
        assertEquals("16 hours", drawer.getTimeRemaining());
    }
}
