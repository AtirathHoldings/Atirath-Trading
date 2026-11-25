package com.atirath.trading.modules.admin.subsidiary.operations.service;

import com.atirath.trading.modules.admin.parent.dto.AdminUserHeaderDto;
import com.atirath.trading.modules.admin.parent.dto.KpiCardDto;
import com.atirath.trading.modules.admin.subsidiary.operations.dto.*;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SubsidiaryOperationsService {

    private AdminUserHeaderDto adminUser;

    // 1. Pending Trades Store
    private final List<PendingTradesPageDto.PendingTradeItemDto> pendingTradesList = new CopyOnWriteArrayList<>();
    private final Map<String, TradeDetailDto> tradeDetailsMap = new ConcurrentHashMap<>();
    private final AtomicInteger tradeCounter = new AtomicInteger(10);

    // 2. Pending Transfers Store
    private final List<PendingTransfersPageDto.PendingTransferItemDto> pendingTransfersList = new CopyOnWriteArrayList<>();
    private final Map<String, TransferDetailDto> transferDetailsMap = new ConcurrentHashMap<>();
    private final AtomicInteger transferCounter = new AtomicInteger(10);

    // 3. Escrow Store
    private final List<EscrowMonitoringPageDto.EscrowAccountItemDto> escrowAccountsList = new CopyOnWriteArrayList<>();
    private final Map<String, EscrowDetailDto> escrowDetailsMap = new ConcurrentHashMap<>();
    private final AtomicInteger escrowCounter = new AtomicInteger(10);

    // 4. Settlement Store
    private final List<SettlementMonitoringPageDto.SettlementItemDto> settlementsList = new CopyOnWriteArrayList<>();
    private final Map<String, SettlementDetailDto> settlementDetailsMap = new ConcurrentHashMap<>();
    private final AtomicInteger settlementCounter = new AtomicInteger(10);

    // 5. Failed Transactions Store
    private final List<FailedTransactionsPageDto.FailedTransactionItemDto> failedTransactionsList = new CopyOnWriteArrayList<>();
    private final Map<String, FailedTransactionDetailDto> failedDetailsMap = new ConcurrentHashMap<>();
    private final AtomicInteger failedCounter = new AtomicInteger(10);

    // 6. Reconciliation Store
    private final List<ReconciliationPageDto.ReconciliationRecordItemDto> reconciliationRecordsList = new CopyOnWriteArrayList<>();
    private final Map<String, ReconciliationDetailDto> reconciliationDetailsMap = new ConcurrentHashMap<>();
    private final AtomicInteger reconciliationCounter = new AtomicInteger(10);

    // 7. Disputes Store
    private final List<DisputesPageDto.DisputeItemDto> disputesList = new CopyOnWriteArrayList<>();
    private final Map<String, DisputeDetailDto> disputeDetailsMap = new ConcurrentHashMap<>();
    private final AtomicInteger disputeCounter = new AtomicInteger(10);

    // 8. Support Tickets Store
    private final List<SupportTicketsPageDto.TicketItemDto> supportTicketsList = new CopyOnWriteArrayList<>();
    private final Map<String, SupportTicketDetailDto> supportTicketDetailsMap = new ConcurrentHashMap<>();
    private final AtomicInteger ticketCounter = new AtomicInteger(10);

    @PostConstruct
    public void init() {
        log.info("[SubsidiaryOperationsService] Initializing Operations datasets matching UI screenshots...");

        adminUser = AdminUserHeaderDto.builder()
                .name("Admin User")
                .initials("AD")
                .role("Subsidiary Administration")
                .currentDate("01 Apr 2026 - 30 Sep 2026")
                .unreadNotifications(3)
                .build();

        seedPendingTrades();
        seedPendingTransfers();
        seedEscrowMonitoring();
        seedSettlementMonitoring();
        seedFailedTransactions();
        seedReconciliation();
        seedDisputes();
        seedSupportTickets();

        log.info("[SubsidiaryOperationsService] Initialization completed. Ready for Operations requests.");
    }

    // ==========================================
    // 1. OPERATIONS DASHBOARD
    // ==========================================
    public OperationsDashboardDto getOperationsDashboard(String company, String dateRange) {
        List<KpiCardDto> kpis = List.of(
                KpiCardDto.builder().title("Total Trades").value("1,264").change("+18% vs last quarter").comparisonText("").trend("positive").icon("repeat").color("blue").build(),
                KpiCardDto.builder().title("Total Transfers").value("892").change("+12% vs last quarter").comparisonText("").trend("positive").icon("arrow-right-left").color("green").build(),
                KpiCardDto.builder().title("Escrow Value").value("₹ 12.8 Cr").change("+25% vs last quarter").comparisonText("").trend("positive").icon("building").color("yellow").build(),
                KpiCardDto.builder().title("Settlements").value("1,156").change("+16% vs last quarter").comparisonText("").trend("positive").icon("file-check").color("red").build(),
                KpiCardDto.builder().title("Failed Transactions").value("28").change("+133% vs last quarter").comparisonText("").trend("negative").icon("alert-circle").color("red").build(),
                KpiCardDto.builder().title("Open Disputes").value("6").change("+50% vs last quarter").comparisonText("").trend("negative").icon("headphones").color("blue").build()
        );

        List<OperationsDashboardDto.OperationalAlertDto> alerts = List.of(
                new OperationsDashboardDto.OperationalAlertDto("28 Failed Transactions", "Require investigation", "2 mins ago", "failed"),
                new OperationsDashboardDto.OperationalAlertDto("45 Pending Settlements", "Exceeding T+2 timeline", "18 mins ago", "pending"),
                new OperationsDashboardDto.OperationalAlertDto("6 Escrow Exceptions", "Insufficient balance / hold issues", "32 mins ago", "exception"),
                new OperationsDashboardDto.OperationalAlertDto("12 Transfer Requests", "Pending approval", "1 hour ago", "request"),
                new OperationsDashboardDto.OperationalAlertDto("4 High Value Trades", "Pending compliance review", "2 hours ago", "high-value")
        );

        List<OperationsDashboardDto.VolumeTrendMonthDto> volumeTrend = List.of(
                new OperationsDashboardDto.VolumeTrendMonthDto("Apr 2026", 95, 38, 70),
                new OperationsDashboardDto.VolumeTrendMonthDto("May 2026", 130, 52, 95),
                new OperationsDashboardDto.VolumeTrendMonthDto("Jun 2026", 115, 45, 88),
                new OperationsDashboardDto.VolumeTrendMonthDto("Jul 2026", 155, 78, 120),
                new OperationsDashboardDto.VolumeTrendMonthDto("Aug 2026", 140, 68, 110),
                new OperationsDashboardDto.VolumeTrendMonthDto("Sep 2026", 185, 85, 130)
        );

        OperationsDashboardDto.TransactionStatusDonutDto donut = OperationsDashboardDto.TransactionStatusDonutDto.builder()
                .totalTransactions("2,156 Total Transactions")
                .slices(List.of(
                        new OperationsDashboardDto.StatusSliceDto("Completed", 74.1, "1,598", "#22c55e"),
                        new OperationsDashboardDto.StatusSliceDto("Pending", 12.0, "258", "#eab308"),
                        new OperationsDashboardDto.StatusSliceDto("Failed", 6.3, "136", "#ef4444"),
                        new OperationsDashboardDto.StatusSliceDto("Under Review", 4.1, "88", "#a855f7"),
                        new OperationsDashboardDto.StatusSliceDto("Disputed", 3.6, "76", "#3b82f6")
                )).build();

        List<OperationsDashboardDto.RecentTradeItemDto> recentTrades = List.of(
                new OperationsDashboardDto.RecentTradeItemDto("TR-2026-001", "Atirath Traders", "Equity - A", "Rakesh Kumar", "25,00,000", "Completed", "green", "29 Sep 2026"),
                new OperationsDashboardDto.RecentTradeItemDto("TR-2026-002", "Farm Vantara", "Equity - B", "Priya Mehta", "18,50,000", "Pending", "orange", "28 Sep 2026"),
                new OperationsDashboardDto.RecentTradeItemDto("TR-2026-003", "Atirath Holdings", "Preference", "Vikram Rao", "42,00,000", "Completed", "green", "27 Sep 2026"),
                new OperationsDashboardDto.RecentTradeItemDto("TR-2026-004", "Sunrise Ventures", "Equity - A", "Neha Sharma", "15,75,000", "Failed", "red", "26 Sep 2026"),
                new OperationsDashboardDto.RecentTradeItemDto("TR-2026-005", "Green Future Fund", "Equity - B", "Suresh Kumar", "33,20,000", "Completed", "green", "25 Sep 2026")
        );

        List<OperationsDashboardDto.RecentTransferItemDto> recentTransfers = List.of(
                new OperationsDashboardDto.RecentTransferItemDto("TF-2026-001", "Rakesh Kumar", "Priya Mehta", "10,000", "Completed", "green", "29 Sep 2026"),
                new OperationsDashboardDto.RecentTransferItemDto("TF-2026-002", "Global Agri Fund", "Atirath Traders", "25,000", "Pending", "orange", "28 Sep 2026"),
                new OperationsDashboardDto.RecentTransferItemDto("TF-2026-003", "Sunrise Ventures", "Vikram Rao", "5,000", "Completed", "green", "27 Sep 2026"),
                new OperationsDashboardDto.RecentTransferItemDto("TF-2026-004", "Neha Sharma", "Suresh Kumar", "12,500", "Under Review", "purple", "26 Sep 2026"),
                new OperationsDashboardDto.RecentTransferItemDto("TF-2026-005", "Farm Vantara", "Ananya Reddy", "20,000", "Failed", "red", "25 Sep 2026")
        );

        OperationsDashboardDto.EscrowSummaryWidgetDto escrowWidget = OperationsDashboardDto.EscrowSummaryWidgetDto.builder()
                .totalEscrowBalance("₹ 12.8 Cr (+25%)")
                .onHold("₹ 2.4 Cr (+12%)")
                .accounts(List.of(
                        new OperationsDashboardDto.EscrowRowDto("ES-2026-001", "Atirath Traders", "3,25,00,000", "Active", "green"),
                        new OperationsDashboardDto.EscrowRowDto("ES-2026-002", "Farm Vantara", "2,80,00,000", "Active", "green"),
                        new OperationsDashboardDto.EscrowRowDto("ES-2026-003", "Atirath Holdings", "4,50,00,000", "On Hold", "orange"),
                        new OperationsDashboardDto.EscrowRowDto("ES-2026-004", "Sunrise Ventures", "1,75,00,000", "Active", "green"),
                        new OperationsDashboardDto.EscrowRowDto("ES-2026-005", "Green Future Fund", "1,95,00,000", "Active", "green")
                )).build();

        OperationsDashboardDto.SettlementSummaryWidgetDto settlementWidget = OperationsDashboardDto.SettlementSummaryWidgetDto.builder()
                .pendingSettlements(45)
                .avgSettlementTime("1.6 Days (-20%)")
                .settlements(List.of(
                        new OperationsDashboardDto.SettlementRowDto("ST-2026-001", "TR-2026-002", "18,50,000", "Pending", "orange"),
                        new OperationsDashboardDto.SettlementRowDto("ST-2026-002", "TR-2026-004", "15,75,000", "Pending", "orange"),
                        new OperationsDashboardDto.SettlementRowDto("ST-2026-003", "TR-2026-003", "42,00,000", "Completed", "green"),
                        new OperationsDashboardDto.SettlementRowDto("ST-2026-004", "TR-2026-005", "33,20,000", "Pending", "orange"),
                        new OperationsDashboardDto.SettlementRowDto("ST-2026-005", "TR-2026-001", "25,00,000", "Completed", "green")
                )).build();

        OperationsDashboardDto.FailedTransactionsWidgetDto failedWidget = OperationsDashboardDto.FailedTransactionsWidgetDto.builder()
                .totalFailed(28)
                .resolutionRate("78% (+12%)")
                .transactions(List.of(
                        new OperationsDashboardDto.FailedTxnRowDto("FT-2026-001", "Trade", "15,75,000", "Insufficient balance"),
                        new OperationsDashboardDto.FailedTxnRowDto("FT-2026-002", "Transfer", "8,20,000", "KYC mismatch"),
                        new OperationsDashboardDto.FailedTxnRowDto("FT-2026-003", "Settlement", "22,50,000", "Bank rejection"),
                        new OperationsDashboardDto.FailedTxnRowDto("FT-2026-004", "Trade", "11,00,000", "Price limit breach"),
                        new OperationsDashboardDto.FailedTxnRowDto("FT-2026-005", "Transfer", "6,75,000", "Blocked user")
                )).build();

        OperationsDashboardDto.DisputesOverviewWidgetDto disputesWidget = OperationsDashboardDto.DisputesOverviewWidgetDto.builder()
                .openDisputes(6)
                .resolvedThisQuarter(12)
                .disputes(List.of(
                        new OperationsDashboardDto.DisputeRowDto("DP-2026-001", "TR-2026-004", "Trade Dispute", "Neha Sharma", "Open", "red", "26 Sep 2026"),
                        new OperationsDashboardDto.DisputeRowDto("DP-2026-002", "TF-2026-005", "Transfer Dispute", "Vikram Rao", "Under Review", "orange", "24 Sep 2026"),
                        new OperationsDashboardDto.DisputeRowDto("DP-2026-003", "ST-2026-002", "Settlement Dispute", "Priya Mehta", "Open", "red", "21 Sep 2026")
                )).build();

        OperationsDashboardDto.SupportTicketsWidgetDto ticketsWidget = OperationsDashboardDto.SupportTicketsWidgetDto.builder()
                .openTickets(14)
                .avgResolutionTime("1.8 Days (-30%)")
                .tickets(List.of(
                        new OperationsDashboardDto.TicketRowDto("TK-2026-001", "Transaction Issue", "Rakesh Kumar", "High", "red", "Open", "red", "29 Sep 2026"),
                        new OperationsDashboardDto.TicketRowDto("TK-2026-002", "KYC / Verification", "Priya Mehta", "Medium", "orange", "In Progress", "blue", "28 Sep 2026"),
                        new OperationsDashboardDto.TicketRowDto("TK-2026-003", "Settlement Delay", "Suresh Kumar", "High", "red", "Open", "red", "27 Sep 2026")
                )).build();

        return OperationsDashboardDto.builder()
                .adminUser(adminUser)
                .pageTitle("Operations Dashboard")
                .pageSubtitle("Monitor and manage trading, transfers, escrow, settlement and overall operational activities.")
                .selectedCompany(company != null ? company : "Atirath Holdings")
                .dateRange(dateRange != null ? dateRange : "01 Apr 2026 - 30 Sep 2026")
                .kpis(kpis)
                .operationalAlerts(alerts)
                .transactionVolumeTrend(volumeTrend)
                .transactionStatus(donut)
                .recentTrades(recentTrades)
                .recentTransfers(recentTransfers)
                .escrowMonitoring(escrowWidget)
                .settlementMonitoring(settlementWidget)
                .failedTransactions(failedWidget)
                .disputesOverview(disputesWidget)
                .supportTickets(ticketsWidget)
                .build();
    }

    // ==========================================
    // 2. PENDING TRADES
    // ==========================================
    public PendingTradesPageDto getPendingTradesPage(String company, String tab, String query, String shareClass, String tradeType, String status) {
        List<KpiCardDto> kpis = List.of(
                KpiCardDto.builder().title("Total Pending Trades").value("156").change("+28% vs last quarter").comparisonText("").trend("positive").icon("hourglass").color("blue").build(),
                KpiCardDto.builder().title("Awaiting Approval").value("48").change("+20% vs last quarter").comparisonText("").trend("negative").icon("file-text").color("red").build(),
                KpiCardDto.builder().title("Pending Verification").value("36").change("+33% vs last quarter").comparisonText("").trend("neutral").icon("clock").color("orange").build(),
                KpiCardDto.builder().title("Pending Settlement").value("52").change("+35% vs last quarter").comparisonText("").trend("positive").icon("building").color("green").build(),
                KpiCardDto.builder().title("Overdue Trades").value("20").change("+67% vs last quarter").comparisonText("").trend("negative").icon("alert-triangle").color("red").build()
        );

        PendingTradesPageDto.PendingTradesStatusDonutDto donut = PendingTradesPageDto.PendingTradesStatusDonutDto.builder()
                .totalPendingTrades(156)
                .slices(List.of(
                        new PendingTradesPageDto.TradeStatusSliceDto("Approval", 30.8, 48, "#eab308"),
                        new PendingTradesPageDto.TradeStatusSliceDto("Verification", 23.1, 36, "#3b82f6"),
                        new PendingTradesPageDto.TradeStatusSliceDto("Settlement", 33.3, 52, "#22c55e"),
                        new PendingTradesPageDto.TradeStatusSliceDto("Overdue", 12.8, 20, "#ef4444")
                )).build();

        List<PendingTradesPageDto.PendingTradesTrendMonthDto> trend = List.of(
                new PendingTradesPageDto.PendingTradesTrendMonthDto("Apr 2026", 14, 10, 16, 5),
                new PendingTradesPageDto.PendingTradesTrendMonthDto("May 2026", 18, 12, 20, 6),
                new PendingTradesPageDto.PendingTradesTrendMonthDto("Jun 2026", 22, 15, 24, 8),
                new PendingTradesPageDto.PendingTradesTrendMonthDto("Jul 2026", 25, 18, 28, 9),
                new PendingTradesPageDto.PendingTradesTrendMonthDto("Aug 2026", 30, 22, 32, 12),
                new PendingTradesPageDto.PendingTradesTrendMonthDto("Sep 2026", 48, 36, 52, 20)
        );

        List<PendingTradesPageDto.PendingTradeItemDto> filtered = pendingTradesList.stream()
                .filter(t -> {
                    if (query != null && !query.isBlank()) {
                        String q = query.toLowerCase();
                        if (!t.getTradeId().toLowerCase().contains(q) &&
                            !t.getCompany().toLowerCase().contains(q) &&
                            !t.getInvestor().toLowerCase().contains(q)) return false;
                    }
                    if (tab != null && !tab.isBlank() && !tab.equalsIgnoreCase("All Pending Trades")) {
                        if (tab.equalsIgnoreCase("Awaiting Approval") && !t.getStatus().equalsIgnoreCase("Approval")) return false;
                        if (tab.equalsIgnoreCase("Pending Verification") && !t.getStatus().equalsIgnoreCase("Verification")) return false;
                        if (tab.equalsIgnoreCase("Pending Settlement") && !t.getStatus().equalsIgnoreCase("Settlement")) return false;
                        if (tab.equalsIgnoreCase("Overdue") && !t.getStatus().equalsIgnoreCase("Overdue")) return false;
                    }
                    if (shareClass != null && !shareClass.isBlank() && !shareClass.equalsIgnoreCase("All Share Classes")) {
                        if (!t.getShareClass().equalsIgnoreCase(shareClass)) return false;
                    }
                    if (tradeType != null && !tradeType.isBlank() && !tradeType.equalsIgnoreCase("All Trade Types")) {
                        if (!t.getType().equalsIgnoreCase(tradeType)) return false;
                    }
                    if (status != null && !status.isBlank() && !status.equalsIgnoreCase("All Status")) {
                        if (!t.getStatus().equalsIgnoreCase(status)) return false;
                    }
                    return true;
                }).collect(Collectors.toList());

        TradeDetailDto selected = tradeDetailsMap.getOrDefault("tr-1", tradeDetailsMap.values().iterator().next());

        return PendingTradesPageDto.builder()
                .adminUser(adminUser)
                .pageTitle("Pending Trades")
                .pageSubtitle("View and process trades pending approval, settlement or additional verification.")
                .selectedCompany(company != null ? company : "Atirath Holdings")
                .dateRange("01 Apr 2026 - 30 Sep 2026")
                .kpis(kpis)
                .tabs(List.of("All Pending Trades", "Awaiting Approval", "Pending Verification", "Pending Settlement", "Overdue", "On Hold"))
                .activeTab(tab != null ? tab : "All Pending Trades")
                .companies(List.of("All Companies", "Atirath Traders", "Farm Vantara", "Atirath Holdings", "Sunrise Ventures", "Green Future Fund", "Atirath Technologies", "Global Agri Fund"))
                .shareClasses(List.of("All Share Classes", "Equity - A", "Equity - B", "Preference"))
                .tradeTypes(List.of("All Trade Types", "Buy", "Sell", "Transfer"))
                .statuses(List.of("All Status", "Approval", "Verification", "Settlement", "Overdue"))
                .trades(filtered)
                .totalTrades(156)
                .currentPage(1)
                .totalPages(16)
                .pageSize(10)
                .pendingTradesByStatus(donut)
                .pendingTradesTrend(trend)
                .selectedTradeDetail(selected)
                .build();
    }

    public TradeDetailDto getTradeDetail(String id) {
        return tradeDetailsMap.getOrDefault(id, tradeDetailsMap.get("tr-1"));
    }

    public TradeDetailDto addTrade(AddTradeRequestDto req) {
        int nextNum = tradeCounter.incrementAndGet();
        String id = "tr-" + nextNum;
        String tid = "TR-2026-" + String.format("%03d", nextNum);

        PendingTradesPageDto.PendingTradeItemDto item = PendingTradesPageDto.PendingTradeItemDto.builder()
                .id(id)
                .number(nextNum)
                .tradeId(tid)
                .tradeDate("29 Sep 2026")
                .company(req.getCompany() != null ? req.getCompany() : "Atirath Traders")
                .shareClass(req.getShareClass() != null ? req.getShareClass() : "Equity - A")
                .investor(req.getInvestor())
                .type(req.getType() != null ? req.getType() : "Buy")
                .qtyOrShares(req.getNoOfShares() != null ? req.getNoOfShares() : "10,000")
                .value(req.getTotalValue() != null ? req.getTotalValue() : "15,00,000")
                .status("Approval")
                .statusTheme("orange")
                .pendingSince("0 days")
                .build();

        pendingTradesList.add(0, item);

        TradeDetailDto detail = TradeDetailDto.builder()
                .id(id)
                .tradeId(tid)
                .orderTypeTitle((req.getType() != null ? req.getType() : "Buy") + " Order")
                .status("Awaiting Approval")
                .statusTheme("orange")
                .createdInfo("Created on 29 Sep 2026, 04:30 PM")
                .tabs(List.of("Details", "History", "Documents", "Related Transactions"))
                .activeTab("Details")
                .type(req.getType() != null ? req.getType() : "Buy")
                .company(req.getCompany() != null ? req.getCompany() : "Atirath Traders")
                .shareClass(req.getShareClass() != null ? req.getShareClass() : "Equity - A")
                .investor(req.getInvestor())
                .noOfShares(req.getNoOfShares() != null ? req.getNoOfShares() : "10,000")
                .pricePerShare(req.getPricePerShare() != null ? String.valueOf(req.getPricePerShare().intValue()) : "150")
                .totalValue(req.getTotalValue() != null ? req.getTotalValue() : "15,00,000")
                .tradeDate("29 Sep 2026")
                .pendingSince("0 days")
                .settlementDate("-")
                .complianceReviewStatus("Pending")
                .riskApprovalStatus("Not Started")
                .settlementApprovalStatus("Not Started")
                .isin(req.getIsin() != null ? req.getIsin() : "INE123A01034")
                .orderSource(req.getOrderSource() != null ? req.getOrderSource() : "Web Portal")
                .createdBy("Admin User")
                .createdOn("29 Sep 2026, 04:30 PM")
                .remarks(req.getRemarks() != null ? req.getRemarks() : "Standard operational trade entry.")
                .build();

        tradeDetailsMap.put(id, detail);
        return detail;
    }

    public TradeDetailDto approveTrade(String id) {
        TradeDetailDto detail = getTradeDetail(id);
        detail.setStatus("Approved");
        detail.setStatusTheme("green");
        detail.setComplianceReviewStatus("Completed");
        detail.setRiskApprovalStatus("Completed");

        pendingTradesList.stream().filter(t -> t.getId().equalsIgnoreCase(id)).findFirst().ifPresent(t -> {
            t.setStatus("Settlement");
            t.setStatusTheme("green");
        });
        return detail;
    }

    public TradeDetailDto rejectTrade(String id) {
        TradeDetailDto detail = getTradeDetail(id);
        detail.setStatus("Rejected");
        detail.setStatusTheme("red");
        detail.setComplianceReviewStatus("Rejected");

        pendingTradesList.stream().filter(t -> t.getId().equalsIgnoreCase(id)).findFirst().ifPresent(t -> {
            t.setStatus("Rejected");
            t.setStatusTheme("red");
        });
        return detail;
    }

    // ==========================================
    // 3. PENDING TRANSFERS
    // ==========================================
    public PendingTransfersPageDto getPendingTransfersPage(String company, String tab, String query, String shareClass, String transferType, String status) {
        List<KpiCardDto> kpis = List.of(
                KpiCardDto.builder().title("Total Pending Transfers").value("892").change("+12% vs last quarter").comparisonText("").trend("positive").icon("file-text").color("blue").build(),
                KpiCardDto.builder().title("Awaiting Approval").value("214").change("+25% vs last quarter").comparisonText("").trend("negative").icon("hourglass").color("red").build(),
                KpiCardDto.builder().title("Pending Verification").value("176").change("+18% vs last quarter").comparisonText("").trend("neutral").icon("check-circle").color("green").build(),
                KpiCardDto.builder().title("Pending Settlement").value("312").change("+10% vs last quarter").comparisonText("").trend("positive").icon("building").color("blue").build(),
                KpiCardDto.builder().title("Overdue Transfers").value("90").change("+67% vs last quarter").comparisonText("").trend("negative").icon("alert-triangle").color("red").build()
        );

        PendingTransfersPageDto.TransfersStatusDonutDto donut = PendingTransfersPageDto.TransfersStatusDonutDto.builder()
                .totalTransfers(892)
                .slices(List.of(
                        new PendingTransfersPageDto.TransferStatusSliceDto("Approval", 24.0, 214, "#eab308"),
                        new PendingTransfersPageDto.TransferStatusSliceDto("Verification", 19.7, 176, "#3b82f6"),
                        new PendingTransfersPageDto.TransferStatusSliceDto("Settlement", 35.0, 312, "#22c55e"),
                        new PendingTransfersPageDto.TransferStatusSliceDto("Overdue", 10.1, 90, "#ef4444"),
                        new PendingTransfersPageDto.TransferStatusSliceDto("Rejected", 11.2, 100, "#06b6d4")
                )).build();

        List<PendingTransfersPageDto.TransfersTrendMonthDto> trend = List.of(
                new PendingTransfersPageDto.TransfersTrendMonthDto("Apr 2026", 30, 25, 45, 12, 10),
                new PendingTransfersPageDto.TransfersTrendMonthDto("May 2026", 40, 32, 55, 15, 12),
                new PendingTransfersPageDto.TransfersTrendMonthDto("Jun 2026", 48, 38, 68, 18, 15),
                new PendingTransfersPageDto.TransfersTrendMonthDto("Jul 2026", 52, 42, 75, 20, 18),
                new PendingTransfersPageDto.TransfersTrendMonthDto("Aug 2026", 60, 48, 85, 24, 20),
                new PendingTransfersPageDto.TransfersTrendMonthDto("Sep 2026", 75, 60, 110, 30, 25)
        );

        List<PendingTransfersPageDto.PendingTransferItemDto> filtered = pendingTransfersList.stream()
                .filter(t -> {
                    if (query != null && !query.isBlank()) {
                        String q = query.toLowerCase();
                        if (!t.getTransferId().toLowerCase().contains(q) &&
                            !t.getFromParty().toLowerCase().contains(q) &&
                            !t.getToParty().toLowerCase().contains(q)) return false;
                    }
                    if (tab != null && !tab.isBlank() && !tab.equalsIgnoreCase("All Transfers")) {
                        if (tab.equalsIgnoreCase("Awaiting Approval") && !t.getStatus().equalsIgnoreCase("Approval")) return false;
                        if (tab.equalsIgnoreCase("Pending Verification") && !t.getStatus().equalsIgnoreCase("Verification")) return false;
                        if (tab.equalsIgnoreCase("Pending Settlement") && !t.getStatus().equalsIgnoreCase("Settlement")) return false;
                        if (tab.equalsIgnoreCase("Overdue") && !t.getStatus().equalsIgnoreCase("Overdue")) return false;
                    }
                    if (shareClass != null && !shareClass.isBlank() && !shareClass.equalsIgnoreCase("All Share Classes")) {
                        if (!t.getShareClass().equalsIgnoreCase(shareClass)) return false;
                    }
                    if (transferType != null && !transferType.isBlank() && !transferType.equalsIgnoreCase("All Transfer Types")) {
                        if (!t.getType().equalsIgnoreCase(transferType)) return false;
                    }
                    if (status != null && !status.isBlank() && !status.equalsIgnoreCase("All Status")) {
                        if (!t.getStatus().equalsIgnoreCase(status)) return false;
                    }
                    return true;
                }).collect(Collectors.toList());

        TransferDetailDto selected = transferDetailsMap.getOrDefault("tf-1", transferDetailsMap.values().iterator().next());

        return PendingTransfersPageDto.builder()
                .adminUser(adminUser)
                .pageTitle("Pending Transfers")
                .pageSubtitle("View and process share transfer requests pending approval, verification or settlement.")
                .selectedCompany(company != null ? company : "Atirath Holdings")
                .dateRange("01 Apr 2026 - 30 Sep 2026")
                .kpis(kpis)
                .tabs(List.of("All Transfers", "Awaiting Approval", "Pending Verification", "Pending Settlement", "Overdue", "Rejected", "Completed"))
                .activeTab(tab != null ? tab : "All Transfers")
                .companies(List.of("All Companies", "Atirath Traders", "Farm Vantara", "Atirath Holdings", "Sunrise Ventures", "Green Future Fund", "Atirath Technologies", "Global Agri Fund"))
                .shareClasses(List.of("All Share Classes", "Equity - A", "Equity - B", "Preference"))
                .transferTypes(List.of("All Transfer Types", "Transfer", "Gift", "Inheritance"))
                .statuses(List.of("All Status", "Approval", "Verification", "Settlement", "Overdue"))
                .transfers(filtered)
                .totalTransfers(892)
                .currentPage(1)
                .totalPages(90)
                .pageSize(10)
                .transfersByStatus(donut)
                .transfersTrend(trend)
                .selectedTransferDetail(selected)
                .build();
    }

    public TransferDetailDto getTransferDetail(String id) {
        return transferDetailsMap.getOrDefault(id, transferDetailsMap.get("tf-1"));
    }

    public TransferDetailDto initiateTransfer(InitiateTransferRequestDto req) {
        int nextNum = transferCounter.incrementAndGet();
        String id = "tf-" + nextNum;
        String tid = "TF-2026-" + String.format("%03d", nextNum);

        PendingTransfersPageDto.PendingTransferItemDto item = PendingTransfersPageDto.PendingTransferItemDto.builder()
                .id(id)
                .number(nextNum)
                .transferId(tid)
                .transferDate("29 Sep 2026")
                .fromParty(req.getFromParty())
                .toParty(req.getToParty())
                .shares(req.getShares() != null ? req.getShares() : "10,000")
                .shareClass(req.getShareClass() != null ? req.getShareClass() : "Equity - A")
                .type(req.getType() != null ? req.getType() : "Transfer")
                .typeTheme("gray")
                .status("Approval")
                .statusTheme("orange")
                .settlementDate("-")
                .build();

        pendingTransfersList.add(0, item);

        TransferDetailDto detail = TransferDetailDto.builder()
                .id(id)
                .transferId(tid)
                .transferTypeTitle("Share Transfer")
                .status("Awaiting Approval")
                .statusTheme("orange")
                .createdInfo("Created on 29 Sep 2026, 04:45 PM")
                .tabs(List.of("Details", "Documents", "History", "Related Transactions"))
                .activeTab("Details")
                .type(req.getType() != null ? req.getType() : "Transfer")
                .fromParty(req.getFromParty())
                .toParty(req.getToParty())
                .shares(req.getShares() != null ? req.getShares() : "10,000")
                .shareClass(req.getShareClass() != null ? req.getShareClass() : "Equity - A")
                .pricePerShare(req.getPricePerShare() != null ? String.valueOf(req.getPricePerShare().intValue()) : "150")
                .totalValue(req.getTotalValue() != null ? req.getTotalValue() : "15,00,000")
                .transferDate("29 Sep 2026")
                .proposedSettlement("-")
                .initiatedDate("29 Sep 2026, 04:45 PM")
                .complianceReviewStatus("Pending")
                .riskApprovalStatus("Not Started")
                .finalApprovalStatus("Not Started")
                .settlementStatus("Not Started")
                .reason(req.getReason() != null ? req.getReason() : "Secondary Sale")
                .remarks(req.getRemarks() != null ? req.getRemarks() : "-")
                .createdBy("Admin User")
                .createdOn("29 Sep 2026, 04:45 PM")
                .build();

        transferDetailsMap.put(id, detail);
        return detail;
    }

    public TransferDetailDto approveTransfer(String id) {
        TransferDetailDto detail = getTransferDetail(id);
        detail.setStatus("Approved");
        detail.setStatusTheme("green");
        detail.setComplianceReviewStatus("Completed");
        detail.setFinalApprovalStatus("Completed");

        pendingTransfersList.stream().filter(t -> t.getId().equalsIgnoreCase(id)).findFirst().ifPresent(t -> {
            t.setStatus("Settlement");
            t.setStatusTheme("green");
        });
        return detail;
    }

    public TransferDetailDto rejectTransfer(String id) {
        TransferDetailDto detail = getTransferDetail(id);
        detail.setStatus("Rejected");
        detail.setStatusTheme("red");
        detail.setComplianceReviewStatus("Rejected");

        pendingTransfersList.stream().filter(t -> t.getId().equalsIgnoreCase(id)).findFirst().ifPresent(t -> {
            t.setStatus("Rejected");
            t.setStatusTheme("red");
        });
        return detail;
    }

    // ==========================================
    // 4. ESCROW MONITORING
    // ==========================================
    public EscrowMonitoringPageDto getEscrowMonitoringPage(String company, String tab, String query, String shareClass, String status) {
        List<KpiCardDto> kpis = List.of(
                KpiCardDto.builder().title("Total Escrow Balance").value("₹ 12.8 Cr").change("+25% vs last quarter").comparisonText("").trend("positive").icon("building").color("green").build(),
                KpiCardDto.builder().title("On Hold").value("₹ 2.4 Cr").change("+12% vs last quarter").comparisonText("").trend("negative").icon("lock").color("red").build(),
                KpiCardDto.builder().title("Released (This Quarter)").value("₹ 8.6 Cr").change("+32% vs last quarter").comparisonText("").trend("positive").icon("check-circle").color("green").build(),
                KpiCardDto.builder().title("Pending Release").value("₹ 1.8 Cr").change("+18% vs last quarter").comparisonText("").trend("neutral").icon("clock").color("orange").build(),
                KpiCardDto.builder().title("Exceptions").value("6").change("+200% vs last quarter").comparisonText("").trend("negative").icon("alert-triangle").color("red").build()
        );

        List<EscrowMonitoringPageDto.EscrowTrendMonthDto> balanceTrend = List.of(
                new EscrowMonitoringPageDto.EscrowTrendMonthDto("Apr 2026", 9.2),
                new EscrowMonitoringPageDto.EscrowTrendMonthDto("May 2026", 11.5),
                new EscrowMonitoringPageDto.EscrowTrendMonthDto("Jun 2026", 10.8),
                new EscrowMonitoringPageDto.EscrowTrendMonthDto("Jul 2026", 14.2),
                new EscrowMonitoringPageDto.EscrowTrendMonthDto("Aug 2026", 13.6),
                new EscrowMonitoringPageDto.EscrowTrendMonthDto("Sep 2026", 17.5)
        );

        EscrowMonitoringPageDto.EscrowStatusDonutDto dist = EscrowMonitoringPageDto.EscrowStatusDonutDto.builder()
                .totalBalance("12.8 Cr Total Balance")
                .slices(List.of(
                        new EscrowMonitoringPageDto.EscrowStatusSliceDto("Available", 67.2, "₹ 8.6 Cr", "#22c55e"),
                        new EscrowMonitoringPageDto.EscrowStatusSliceDto("On Hold", 18.8, "₹ 2.4 Cr", "#f97316"),
                        new EscrowMonitoringPageDto.EscrowStatusSliceDto("Pending Release", 10.2, "₹ 1.3 Cr", "#ef4444"),
                        new EscrowMonitoringPageDto.EscrowStatusSliceDto("Disputed", 3.8, "₹ 0.5 Cr", "#a855f7")
                )).build();

        List<EscrowMonitoringPageDto.UpcomingReleaseRowDto> upcomingReleases = List.of(
                new EscrowMonitoringPageDto.UpcomingReleaseRowDto("ES-2026-004", "Sunrise Ventures", "1,75,00,000", "02 Oct 2026", 3),
                new EscrowMonitoringPageDto.UpcomingReleaseRowDto("ES-2026-009", "Farm Vantara", "2,60,00,000", "28 Sep 2026", 1),
                new EscrowMonitoringPageDto.UpcomingReleaseRowDto("ES-2026-003", "Atirath Holdings", "4,50,00,000", "30 Sep 2026", 2),
                new EscrowMonitoringPageDto.UpcomingReleaseRowDto("ES-2026-006", "Global Agri Fund", "2,20,00,000", "24 Sep 2026", 0),
                new EscrowMonitoringPageDto.UpcomingReleaseRowDto("ES-2026-008", "Priya Mehta", "1,85,00,000", "25 Sep 2026", 1)
        );

        List<EscrowMonitoringPageDto.EscrowExceptionRowDto> exceptions = List.of(
                new EscrowMonitoringPageDto.EscrowExceptionRowDto("ES-2026-005", "Dispute", "red", "1,95,00,000", "Investor dispute", 5),
                new EscrowMonitoringPageDto.EscrowExceptionRowDto("ES-2026-002", "Hold", "orange", "2,80,00,000", "KYC verification", 3),
                new EscrowMonitoringPageDto.EscrowExceptionRowDto("ES-2026-007", "Hold", "orange", "1,10,00,000", "Compliance review", 3),
                new EscrowMonitoringPageDto.EscrowExceptionRowDto("ES-2026-011", "Hold", "orange", "90,00,000", "Document mismatch", 4),
                new EscrowMonitoringPageDto.EscrowExceptionRowDto("ES-2026-012", "Dispute", "red", "75,00,000", "Price variance", 6)
        );

        List<EscrowMonitoringPageDto.EscrowAccountItemDto> filtered = escrowAccountsList.stream()
                .filter(e -> {
                    if (query != null && !query.isBlank()) {
                        String q = query.toLowerCase();
                        if (!e.getEscrowId().toLowerCase().contains(q) &&
                            !e.getCompany().toLowerCase().contains(q) &&
                            !e.getInvestor().toLowerCase().contains(q)) return false;
                    }
                    if (tab != null && !tab.isBlank() && !tab.equalsIgnoreCase("All Escrow Accounts")) {
                        if (tab.equalsIgnoreCase("On Hold") && !e.getStatus().equalsIgnoreCase("On Hold")) return false;
                        if (tab.equalsIgnoreCase("Pending Release") && !e.getStatus().equalsIgnoreCase("Pending Release")) return false;
                        if (tab.equalsIgnoreCase("Released") && !e.getStatus().equalsIgnoreCase("Released") && !e.getStatus().equalsIgnoreCase("Available")) return false;
                        if (tab.equalsIgnoreCase("Disputed") && !e.getStatus().equalsIgnoreCase("Disputed")) return false;
                    }
                    if (shareClass != null && !shareClass.isBlank() && !shareClass.equalsIgnoreCase("All Share Classes")) {
                        if (!e.getShareClass().equalsIgnoreCase(shareClass)) return false;
                    }
                    if (status != null && !status.isBlank() && !status.equalsIgnoreCase("All Status")) {
                        if (!e.getStatus().equalsIgnoreCase(status)) return false;
                    }
                    return true;
                }).collect(Collectors.toList());

        EscrowDetailDto selected = escrowDetailsMap.getOrDefault("es-1", escrowDetailsMap.values().iterator().next());

        return EscrowMonitoringPageDto.builder()
                .adminUser(adminUser)
                .pageTitle("Escrow Monitoring")
                .pageSubtitle("Monitor escrow balances, holds, releases and linked transactions.")
                .selectedCompany(company != null ? company : "Atirath Holdings")
                .dateRange("01 Apr 2026 - 30 Sep 2026")
                .kpis(kpis)
                .tabs(List.of("All Escrow Accounts", "On Hold", "Pending Release", "Released", "Disputed", "Exceptions"))
                .activeTab(tab != null ? tab : "All Escrow Accounts")
                .companies(List.of("All Companies", "Atirath Traders", "Farm Vantara", "Atirath Holdings", "Sunrise Ventures", "Green Future Fund", "Global Agri Fund", "Atirath Technologies"))
                .shareClasses(List.of("All Share Classes", "Equity - A", "Equity - B", "Preference", "Transfer"))
                .statuses(List.of("All Status", "Available", "On Hold", "Pending Release", "Disputed"))
                .balanceTrend(balanceTrend)
                .statusDistribution(dist)
                .accounts(filtered)
                .totalAccounts(156)
                .currentPage(1)
                .totalPages(16)
                .pageSize(10)
                .upcomingReleases(upcomingReleases)
                .escrowExceptions(exceptions)
                .selectedEscrowDetail(selected)
                .build();
    }

    public EscrowDetailDto getEscrowDetail(String id) {
        return escrowDetailsMap.getOrDefault(id, escrowDetailsMap.get("es-1"));
    }

    public EscrowDetailDto createEscrow(CreateEscrowRequestDto req) {
        int nextNum = escrowCounter.incrementAndGet();
        String id = "es-" + nextNum;
        String eid = "ES-2026-" + String.format("%03d", nextNum);

        EscrowMonitoringPageDto.EscrowAccountItemDto item = EscrowMonitoringPageDto.EscrowAccountItemDto.builder()
                .id(id)
                .number(nextNum)
                .escrowId(eid)
                .company(req.getCompany() != null ? req.getCompany() : "Atirath Traders")
                .tradeId(req.getTradeId() != null ? req.getTradeId() : "TR-2026-" + nextNum)
                .investor(req.getInvestor())
                .shareClass(req.getShareClass() != null ? req.getShareClass() : "Equity - A")
                .amount(req.getAmount() != null ? req.getAmount() : "1,00,00,000")
                .status("Available")
                .statusTheme("green")
                .createdOn("29 Sep 2026")
                .expectedRelease(req.getExpectedRelease() != null ? req.getExpectedRelease() : "29 Sep 2026")
                .build();

        escrowAccountsList.add(0, item);

        EscrowDetailDto detail = EscrowDetailDto.builder()
                .id(id)
                .escrowId(eid)
                .companyTitle(req.getCompany() != null ? req.getCompany() : "Atirath Traders")
                .status("Available")
                .statusTheme("green")
                .createdInfo("Created on 29 Sep 2026, 05:00 PM")
                .tabs(List.of("Details", "Transactions", "Hold / Release History", "Notes"))
                .activeTab("Details")
                .company(req.getCompany() != null ? req.getCompany() : "Atirath Traders")
                .tradeId(req.getTradeId() != null ? req.getTradeId() : "TR-2026-" + nextNum)
                .investor(req.getInvestor())
                .shareClass(req.getShareClass() != null ? req.getShareClass() : "Equity - A")
                .amount(req.getAmount() != null ? req.getAmount() : "1,00,00,000")
                .createdOn("29 Sep 2026, 05:00 PM")
                .expectedRelease(req.getExpectedRelease() != null ? req.getExpectedRelease() : "29 Sep 2026")
                .releasedOn("-")
                .escrowBank(req.getEscrowBank() != null ? req.getEscrowBank() : "HDFC Bank")
                .accountNo(req.getAccountNo() != null ? req.getAccountNo() : "50200012345678")
                .ifscCode(req.getIfscCode() != null ? req.getIfscCode() : "HDFC0001234")
                .totalAmount(req.getAmount() != null ? req.getAmount() : "1,00,00,000")
                .releasedAmount("0")
                .onHoldAmount("0")
                .pendingAmount("0")
                .availableAmount(req.getAmount() != null ? req.getAmount() : "1,00,00,000")
                .kycStatus("Verified")
                .amlCheck("Clear")
                .sanctionsCheck("Clear")
                .relatedTrade(req.getTradeId() != null ? req.getTradeId() : "TR-2026-" + nextNum)
                .investorProfile(req.getInvestor())
                .documentsCount("3 Documents")
                .build();

        escrowDetailsMap.put(id, detail);
        return detail;
    }

    public EscrowDetailDto releaseEscrowFunds(String id) {
        EscrowDetailDto detail = getEscrowDetail(id);
        detail.setStatus("Released");
        detail.setStatusTheme("green");
        detail.setReleasedAmount(detail.getTotalAmount());
        detail.setAvailableAmount("0");
        detail.setReleasedOn("29 Sep 2026, 05:10 PM");

        escrowAccountsList.stream().filter(e -> e.getId().equalsIgnoreCase(id)).findFirst().ifPresent(e -> {
            e.setStatus("Released");
            e.setStatusTheme("green");
        });
        return detail;
    }

    public EscrowDetailDto placeEscrowHold(String id) {
        EscrowDetailDto detail = getEscrowDetail(id);
        detail.setStatus("On Hold");
        detail.setStatusTheme("orange");
        detail.setOnHoldAmount(detail.getTotalAmount());
        detail.setAvailableAmount("0");

        escrowAccountsList.stream().filter(e -> e.getId().equalsIgnoreCase(id)).findFirst().ifPresent(e -> {
            e.setStatus("On Hold");
            e.setStatusTheme("orange");
        });
        return detail;
    }

    // ==========================================
    // 5. SETTLEMENT MONITORING
    // ==========================================
    public SettlementMonitoringPageDto getSettlementMonitoringPage(String company, String tab, String query, String shareClass, String status) {
        List<KpiCardDto> kpis = List.of(
                KpiCardDto.builder().title("Total Trade Settlements").value("1,156").change("+16% vs last quarter").comparisonText("").trend("positive").icon("building").color("green").build(),
                KpiCardDto.builder().title("Settled").value("982").change("+18% vs last quarter").comparisonText("").trend("positive").icon("check-circle").color("green").build(),
                KpiCardDto.builder().title("Pending Settlement").value("108").change("+25% vs last quarter").comparisonText("").trend("neutral").icon("clock").color("orange").build(),
                KpiCardDto.builder().title("Failed Settlements").value("28").change("+133% vs last quarter").comparisonText("").trend("negative").icon("alert-triangle").color("red").build(),
                KpiCardDto.builder().title("Avg. Settlement Time").value("1.6 Days").change("-20% vs last quarter").comparisonText("").trend("positive").icon("hourglass").color("green").build()
        );

        List<SettlementMonitoringPageDto.SettlementTrendMonthDto> volumeTrend = List.of(
                new SettlementMonitoringPageDto.SettlementTrendMonthDto("Apr 2026", 8.5, 140),
                new SettlementMonitoringPageDto.SettlementTrendMonthDto("May 2026", 11.2, 175),
                new SettlementMonitoringPageDto.SettlementTrendMonthDto("Jun 2026", 10.4, 160),
                new SettlementMonitoringPageDto.SettlementTrendMonthDto("Jul 2026", 14.8, 210),
                new SettlementMonitoringPageDto.SettlementTrendMonthDto("Aug 2026", 13.9, 195),
                new SettlementMonitoringPageDto.SettlementTrendMonthDto("Sep 2026", 16.5, 276)
        );

        SettlementMonitoringPageDto.SettlementStatusDonutDto dist = SettlementMonitoringPageDto.SettlementStatusDonutDto.builder()
                .totalSettlements(1156)
                .slices(List.of(
                        new SettlementMonitoringPageDto.SettlementStatusSliceDto("Settled", 84.9, 982, "#22c55e"),
                        new SettlementMonitoringPageDto.SettlementStatusSliceDto("Pending", 9.4, 108, "#eab308"),
                        new SettlementMonitoringPageDto.SettlementStatusSliceDto("Failed", 2.4, 28, "#ef4444"),
                        new SettlementMonitoringPageDto.SettlementStatusSliceDto("On Hold", 2.1, 24, "#a855f7"),
                        new SettlementMonitoringPageDto.SettlementStatusSliceDto("Under Review", 1.2, 14, "#3b82f6")
                )).build();

        List<SettlementMonitoringPageDto.UpcomingSettlementRowDto> upcoming = List.of(
                new SettlementMonitoringPageDto.UpcomingSettlementRowDto("ST-2026-011", "TR-2026-011", "Atirath Holdings", "22,00,000", "30 Sep 2026", 2),
                new SettlementMonitoringPageDto.UpcomingSettlementRowDto("ST-2026-012", "TR-2026-012", "Farm Vantara", "11,50,000", "30 Sep 2026", 2),
                new SettlementMonitoringPageDto.UpcomingSettlementRowDto("ST-2026-013", "TR-2026-013", "Green Future Fund", "27,00,000", "01 Oct 2026", 3),
                new SettlementMonitoringPageDto.UpcomingSettlementRowDto("ST-2026-014", "TR-2026-014", "Sunrise Ventures", "18,75,000", "01 Oct 2026", 3),
                new SettlementMonitoringPageDto.UpcomingSettlementRowDto("ST-2026-015", "TR-2026-015", "Global Agri Fund", "15,60,000", "02 Oct 2026", 4)
        );

        List<SettlementMonitoringPageDto.FailedSettlementRowDto> failed = List.of(
                new SettlementMonitoringPageDto.FailedSettlementRowDto("ST-2026-004", "TR-2026-004", "Sunrise Ventures", "15,75,000", "Insufficient funds", "26 Sep 2026"),
                new SettlementMonitoringPageDto.FailedSettlementRowDto("ST-2026-010", "TR-2026-010", "Sunrise Ventures", "10,80,000", "Bank rejection", "20 Sep 2026"),
                new SettlementMonitoringPageDto.FailedSettlementRowDto("ST-2026-016", "TR-2026-016", "Farm Vantara", "9,20,000", "KYC mismatch", "18 Sep 2026"),
                new SettlementMonitoringPageDto.FailedSettlementRowDto("ST-2026-018", "TR-2026-018", "Atirath Traders", "12,50,000", "Account blocked", "15 Sep 2026"),
                new SettlementMonitoringPageDto.FailedSettlementRowDto("ST-2026-021", "TR-2026-021", "Global Agri Fund", "8,75,000", "Payment reversal", "12 Sep 2026")
        );

        List<SettlementMonitoringPageDto.SettlementItemDto> filtered = settlementsList.stream()
                .filter(s -> {
                    if (query != null && !query.isBlank()) {
                        String q = query.toLowerCase();
                        if (!s.getSettlementId().toLowerCase().contains(q) &&
                            !s.getTradeId().toLowerCase().contains(q) &&
                            !s.getCompany().toLowerCase().contains(q) &&
                            !s.getInvestor().toLowerCase().contains(q)) return false;
                    }
                    if (tab != null && !tab.isBlank() && !tab.equalsIgnoreCase("All Settlements")) {
                        if (tab.equalsIgnoreCase("Pending") && !s.getStatus().equalsIgnoreCase("Pending")) return false;
                        if (tab.equalsIgnoreCase("Failed") && !s.getStatus().equalsIgnoreCase("Failed")) return false;
                        if (tab.equalsIgnoreCase("On Hold") && !s.getStatus().equalsIgnoreCase("On Hold")) return false;
                        if (tab.equalsIgnoreCase("Under Review") && !s.getStatus().equalsIgnoreCase("Under Review")) return false;
                    }
                    if (shareClass != null && !shareClass.isBlank() && !shareClass.equalsIgnoreCase("All Share Classes")) {
                        if (!s.getShareClass().equalsIgnoreCase(shareClass)) return false;
                    }
                    if (status != null && !status.isBlank() && !status.equalsIgnoreCase("All Status")) {
                        if (!s.getStatus().equalsIgnoreCase(status)) return false;
                    }
                    return true;
                }).collect(Collectors.toList());

        SettlementDetailDto selected = settlementDetailsMap.getOrDefault("st-2", settlementDetailsMap.values().iterator().next());

        return SettlementMonitoringPageDto.builder()
                .adminUser(adminUser)
                .pageTitle("Settlement Monitoring")
                .pageSubtitle("Monitor trade settlements, settlement cycles and pending settlements.")
                .selectedCompany(company != null ? company : "Atirath Holdings")
                .dateRange("01 Apr 2026 - 30 Sep 2026")
                .kpis(kpis)
                .tabs(List.of("All Settlements", "Pending", "Failed", "On Hold", "Under Review"))
                .activeTab(tab != null ? tab : "All Settlements")
                .companies(List.of("All Companies", "Atirath Traders", "Farm Vantara", "Atirath Holdings", "Sunrise Ventures", "Green Future Fund", "Global Agri Fund", "Atirath Technologies"))
                .shareClasses(List.of("All Share Classes", "Equity - A", "Equity - B", "Preference", "Transfer"))
                .statuses(List.of("All Status", "Settled", "Pending", "Failed", "On Hold", "Under Review"))
                .volumeTrend(volumeTrend)
                .statusDistribution(dist)
                .settlements(filtered)
                .totalSettlements(1156)
                .currentPage(1)
                .totalPages(116)
                .pageSize(10)
                .upcomingSettlements(upcoming)
                .failedSettlements(failed)
                .selectedSettlementDetail(selected)
                .build();
    }

    public SettlementDetailDto getSettlementDetail(String id) {
        return settlementDetailsMap.getOrDefault(id, settlementDetailsMap.get("st-2"));
    }

    public SettlementDetailDto markSettlementSettled(String id) {
        SettlementDetailDto detail = getSettlementDetail(id);
        detail.setStatus("Settled");
        detail.setStatusTheme("green");
        detail.setAmountReceived(detail.getExpectedAmount());
        detail.getSteps().forEach(step -> {
            step.setStatus("Completed");
            step.setStatusTheme("green");
        });

        settlementsList.stream().filter(s -> s.getId().equalsIgnoreCase(id)).findFirst().ifPresent(s -> {
            s.setStatus("Settled");
            s.setStatusTheme("green");
        });
        return detail;
    }

    public SettlementDetailDto markSettlementFailed(String id) {
        SettlementDetailDto detail = getSettlementDetail(id);
        detail.setStatus("Failed");
        detail.setStatusTheme("red");

        settlementsList.stream().filter(s -> s.getId().equalsIgnoreCase(id)).findFirst().ifPresent(s -> {
            s.setStatus("Failed");
            s.setStatusTheme("red");
        });
        return detail;
    }

    // ==========================================
    // 6. FAILED TRANSACTIONS
    // ==========================================
    public FailedTransactionsPageDto getFailedTransactionsPage(String company, String tab, String query, String transactionType, String failureReason) {
        List<KpiCardDto> kpis = List.of(
                KpiCardDto.builder().title("Total Failed Transactions").value("28").change("+133% vs last quarter").comparisonText("").trend("negative").icon("alert-circle").color("red").build(),
                KpiCardDto.builder().title("Failed Trades").value("12").change("+100% vs last quarter").comparisonText("").trend("negative").icon("alert-triangle").color("red").build(),
                KpiCardDto.builder().title("Failed Transfers").value("6").change("+50% vs last quarter").comparisonText("").trend("negative").icon("repeat").color("orange").build(),
                KpiCardDto.builder().title("Failed Settlements").value("8").change("+167% vs last quarter").comparisonText("").trend("negative").icon("building").color("green").build(),
                KpiCardDto.builder().title("Failed Payments").value("2").change("+100% vs last quarter").comparisonText("").trend("negative").icon("credit-card").color("red").build()
        );

        FailedTransactionsPageDto.FailedTypesDonutDto donut = FailedTransactionsPageDto.FailedTypesDonutDto.builder()
                .totalFailed("28 Failed")
                .slices(List.of(
                        new FailedTransactionsPageDto.TypeSliceDto("Trade", 42.9, 12, "#ef4444"),
                        new FailedTransactionsPageDto.TypeSliceDto("Transfer", 21.4, 6, "#3b82f6"),
                        new FailedTransactionsPageDto.TypeSliceDto("Settlement", 28.6, 8, "#a855f7"),
                        new FailedTransactionsPageDto.TypeSliceDto("Payment", 7.1, 2, "#14b8a6")
                )).build();

        List<FailedTransactionsPageDto.FailureReasonBarDto> reasons = List.of(
                new FailedTransactionsPageDto.FailureReasonBarDto("Insufficient balance", 6, 21.4, "#ef4444"),
                new FailedTransactionsPageDto.FailureReasonBarDto("Bank rejection", 5, 17.9, "#ef4444"),
                new FailedTransactionsPageDto.FailureReasonBarDto("KYC mismatch", 4, 14.3, "#f97316"),
                new FailedTransactionsPageDto.FailureReasonBarDto("Price limit breach", 3, 10.7, "#f97316"),
                new FailedTransactionsPageDto.FailureReasonBarDto("Compliance block", 2, 7.1, "#ef4444"),
                new FailedTransactionsPageDto.FailureReasonBarDto("Payment gateway error", 2, 7.1, "#ef4444"),
                new FailedTransactionsPageDto.FailureReasonBarDto("Beneficiary not found", 2, 7.1, "#f97316"),
                new FailedTransactionsPageDto.FailureReasonBarDto("Others", 4, 14.3, "#ef4444")
        );

        List<FailedTransactionsPageDto.FailedTrendMonthDto> trend = List.of(
                new FailedTransactionsPageDto.FailedTrendMonthDto("Apr 2026", 6),
                new FailedTransactionsPageDto.FailedTrendMonthDto("May 2026", 9),
                new FailedTransactionsPageDto.FailedTrendMonthDto("Jun 2026", 7),
                new FailedTransactionsPageDto.FailedTrendMonthDto("Jul 2026", 12),
                new FailedTransactionsPageDto.FailedTrendMonthDto("Aug 2026", 14),
                new FailedTransactionsPageDto.FailedTrendMonthDto("Sep 2026", 17)
        );

        List<FailedTransactionsPageDto.FailedTransactionItemDto> filtered = failedTransactionsList.stream()
                .filter(t -> {
                    if (query != null && !query.isBlank()) {
                        String q = query.toLowerCase();
                        if (!t.getTransactionId().toLowerCase().contains(q) &&
                            !t.getCompany().toLowerCase().contains(q) &&
                            !t.getInvestor().toLowerCase().contains(q) &&
                            !t.getFailureReason().toLowerCase().contains(q)) return false;
                    }
                    if (tab != null && !tab.isBlank() && !tab.equalsIgnoreCase("All Failed Transactions")) {
                        if (tab.equalsIgnoreCase("Trades") && !t.getType().equalsIgnoreCase("Trade")) return false;
                        if (tab.equalsIgnoreCase("Transfers") && !t.getType().equalsIgnoreCase("Transfer")) return false;
                        if (tab.equalsIgnoreCase("Settlements") && !t.getType().equalsIgnoreCase("Settlement")) return false;
                        if (tab.equalsIgnoreCase("Payments") && !t.getType().equalsIgnoreCase("Payment")) return false;
                    }
                    if (transactionType != null && !transactionType.isBlank() && !transactionType.equalsIgnoreCase("All Transaction Types")) {
                        if (!t.getType().equalsIgnoreCase(transactionType)) return false;
                    }
                    if (failureReason != null && !failureReason.isBlank() && !failureReason.equalsIgnoreCase("All Failure Reasons")) {
                        if (!t.getFailureReason().equalsIgnoreCase(failureReason)) return false;
                    }
                    return true;
                }).collect(Collectors.toList());

        FailedTransactionDetailDto selected = failedDetailsMap.getOrDefault("ft-1", failedDetailsMap.values().iterator().next());

        return FailedTransactionsPageDto.builder()
                .adminUser(adminUser)
                .pageTitle("Failed Transactions")
                .pageSubtitle("View and manage failed trades, transfers, settlements and payment transactions.")
                .selectedCompany(company != null ? company : "Atirath Holdings")
                .dateRange("01 Apr 2026 - 30 Sep 2026")
                .kpis(kpis)
                .tabs(List.of("All Failed Transactions", "Trades", "Transfers", "Settlements", "Payments"))
                .activeTab(tab != null ? tab : "All Failed Transactions")
                .companies(List.of("All Companies", "Atirath Traders", "Farm Vantara", "Atirath Holdings", "Sunrise Ventures", "Green Future Fund", "Global Agri Fund", "Atirath Technologies"))
                .transactionTypes(List.of("All Transaction Types", "Trade", "Transfer", "Settlement", "Payment"))
                .failureReasons(List.of("All Failure Reasons", "Insufficient balance", "KYC mismatch", "Bank rejection", "Price limit breach", "Payment gateway error", "Beneficiary not found", "Compliance block", "Price variance", "Bank timeout"))
                .transactions(filtered)
                .totalTransactions(28)
                .currentPage(1)
                .totalPages(3)
                .pageSize(10)
                .failedTransactionsByType(donut)
                .failureReasonsDistribution(reasons)
                .failedTransactionsTrend(trend)
                .selectedTransactionDetail(selected)
                .build();
    }

    public FailedTransactionDetailDto getFailedTransactionDetail(String id) {
        return failedDetailsMap.getOrDefault(id, failedDetailsMap.get("ft-1"));
    }

    public FailedTransactionDetailDto retryFailedTransaction(String id) {
        FailedTransactionDetailDto detail = getFailedTransactionDetail(id);
        detail.setRetryStatus("Retrying");
        detail.setRetryCount("1 / 3");

        failedTransactionsList.stream().filter(f -> f.getId().equalsIgnoreCase(id)).findFirst().ifPresent(f -> {
            f.setRetryStatus("Retrying");
            f.setRetryStatusTheme("green");
        });
        return detail;
    }

    public FailedTransactionDetailDto cancelFailedTransaction(String id) {
        FailedTransactionDetailDto detail = getFailedTransactionDetail(id);
        detail.setStatus("Cancelled");
        detail.setStatusTheme("gray");

        failedTransactionsList.stream().filter(f -> f.getId().equalsIgnoreCase(id)).findFirst().ifPresent(f -> {
            f.setRetryStatus("Cancelled");
            f.setRetryStatusTheme("gray");
        });
        return detail;
    }

    // ==========================================
    // 7. RECONCILIATION
    // ==========================================
    public ReconciliationPageDto getReconciliationPage(String company, String tab, String query, String recordType, String status) {
        List<KpiCardDto> kpis = List.of(
                KpiCardDto.builder().title("Total Records").value("12,856").change("+18% vs last quarter").comparisonText("").trend("positive").icon("check-circle").color("green").build(),
                KpiCardDto.builder().title("Matched").value("11,942").change("+15% vs last quarter").comparisonText("").trend("positive").icon("alert-circle").color("red").build(),
                KpiCardDto.builder().title("Unmatched").value("724").change("+67% vs last quarter").comparisonText("").trend("negative").icon("clock").color("orange").build(),
                KpiCardDto.builder().title("Under Review").value("142").change("+45% vs last quarter").comparisonText("").trend("neutral").icon("refresh-cw").color("purple").build(),
                KpiCardDto.builder().title("Exceptions").value("48").change("+100% vs last quarter").comparisonText("").trend("negative").icon("alert-triangle").color("red").build()
        );

        List<ReconciliationPageDto.ReconciliationTrendMonthDto> trend = List.of(
                new ReconciliationPageDto.ReconciliationTrendMonthDto("Apr 2026", 900, 200, 20),
                new ReconciliationPageDto.ReconciliationTrendMonthDto("May 2026", 1200, 250, 25),
                new ReconciliationPageDto.ReconciliationTrendMonthDto("Jun 2026", 1100, 280, 28),
                new ReconciliationPageDto.ReconciliationTrendMonthDto("Jul 2026", 1450, 320, 35),
                new ReconciliationPageDto.ReconciliationTrendMonthDto("Aug 2026", 1580, 340, 40),
                new ReconciliationPageDto.ReconciliationTrendMonthDto("Sep 2026", 1680, 360, 48)
        );

        ReconciliationPageDto.ReconciliationStatusDonutDto donut = ReconciliationPageDto.ReconciliationStatusDonutDto.builder()
                .totalRecords("12,856 Total Records")
                .slices(List.of(
                        new ReconciliationPageDto.StatusSliceDto("Matched", 92.8, "11,942", "#22c55e"),
                        new ReconciliationPageDto.StatusSliceDto("Unmatched", 5.6, "724", "#ef4444"),
                        new ReconciliationPageDto.StatusSliceDto("Under Review", 1.1, "142", "#f97316"),
                        new ReconciliationPageDto.StatusSliceDto("Exceptions", 0.5, "48", "#a855f7")
                )).build();

        List<ReconciliationPageDto.VarianceByTypeDto> variance = List.of(
                new ReconciliationPageDto.VarianceByTypeDto("Trade", 0.0, 5000.0),
                new ReconciliationPageDto.VarianceByTypeDto("Transfer", 100000.0, 20000.0),
                new ReconciliationPageDto.VarianceByTypeDto("Settlement", 0.0, 25000.0),
                new ReconciliationPageDto.VarianceByTypeDto("Payment", 15000.0, 20000.0)
        );

        List<ReconciliationPageDto.TopExceptionRowDto> exceptions = List.of(
                new ReconciliationPageDto.TopExceptionRowDto(1, "RC-2026-008", "Payment", "PY-2026-002", "-5,000", "Bank amount mismatch", "Exceptions", "red"),
                new ReconciliationPageDto.TopExceptionRowDto(2, "RC-2026-005", "Trade", "TR-2026-002", "-5,000", "Price variance", "Under Review", "orange"),
                new ReconciliationPageDto.TopExceptionRowDto(3, "RC-2026-003", "Transfer", "TF-2026-001", "-5,000", "Fee not applied", "Unmatched", "red"),
                new ReconciliationPageDto.TopExceptionRowDto(4, "RC-2026-011", "Settlement", "ST-2026-004", "-7,500", "Rounding difference", "Exceptions", "red"),
                new ReconciliationPageDto.TopExceptionRowDto(5, "RC-2026-012", "Trade", "TR-2026-005", "-12,000", "Quantity mismatch", "Under Review", "orange")
        );

        List<ReconciliationPageDto.ReconciliationRecordItemDto> filtered = reconciliationRecordsList.stream()
                .filter(r -> {
                    if (query != null && !query.isBlank()) {
                        String q = query.toLowerCase();
                        if (!r.getRecordId().toLowerCase().contains(q) &&
                            !r.getReferenceId().toLowerCase().contains(q) &&
                            !r.getCompany().toLowerCase().contains(q)) return false;
                    }
                    if (tab != null && !tab.isBlank() && !tab.equalsIgnoreCase("All Records")) {
                        if (tab.equalsIgnoreCase("Matched") && !r.getStatus().equalsIgnoreCase("Matched")) return false;
                        if (tab.equalsIgnoreCase("Unmatched") && !r.getStatus().equalsIgnoreCase("Unmatched")) return false;
                        if (tab.equalsIgnoreCase("Under Review") && !r.getStatus().equalsIgnoreCase("Under Review")) return false;
                        if (tab.equalsIgnoreCase("Exceptions") && !r.getStatus().equalsIgnoreCase("Exceptions")) return false;
                    }
                    if (recordType != null && !recordType.isBlank() && !recordType.equalsIgnoreCase("All Record Types")) {
                        if (!r.getRecordType().equalsIgnoreCase(recordType)) return false;
                    }
                    if (status != null && !status.isBlank() && !status.equalsIgnoreCase("All Status")) {
                        if (!r.getStatus().equalsIgnoreCase(status)) return false;
                    }
                    return true;
                }).collect(Collectors.toList());

        ReconciliationDetailDto selected = reconciliationDetailsMap.getOrDefault("rc-1", reconciliationDetailsMap.values().iterator().next());

        return ReconciliationPageDto.builder()
                .adminUser(adminUser)
                .pageTitle("Reconciliation")
                .pageSubtitle("Reconcile trades, transfers, settlements and payments across systems.")
                .selectedCompany(company != null ? company : "Atirath Holdings")
                .dateRange("01 Apr 2026 - 30 Sep 2026")
                .kpis(kpis)
                .reconciliationTrend(trend)
                .statusDistribution(donut)
                .tabs(List.of("All Records", "Matched", "Unmatched", "Under Review", "Exceptions"))
                .activeTab(tab != null ? tab : "All Records")
                .companies(List.of("All Companies", "Atirath Traders", "Farm Vantara", "Atirath Holdings", "Sunrise Ventures", "Green Future Fund", "Global Agri Fund", "Atirath Technologies", "Priya Mehta"))
                .recordTypes(List.of("All Record Types", "Trade", "Settlement", "Transfer", "Payment"))
                .statuses(List.of("All Status", "Matched", "Unmatched", "Under Review", "Exceptions"))
                .records(filtered)
                .totalRecords(12856)
                .currentPage(1)
                .totalPages(1286)
                .pageSize(10)
                .varianceByRecordType(variance)
                .topReconciliationExceptions(exceptions)
                .selectedReconciliationDetail(selected)
                .build();
    }

    public ReconciliationDetailDto getReconciliationDetail(String id) {
        return reconciliationDetailsMap.getOrDefault(id, reconciliationDetailsMap.get("rc-1"));
    }

    public ReconciliationDetailDto runReconciliation(RunReconciliationRequestDto req) {
        int nextNum = reconciliationCounter.incrementAndGet();
        String id = "rc-" + nextNum;
        String rcid = "RC-2026-" + String.format("%03d", nextNum);

        ReconciliationDetailDto detail = ReconciliationDetailDto.builder()
                .id(id)
                .reconciliationId(rcid)
                .reconciliationTitle((req.getType() != null ? req.getType() : "Monthly") + " Reconciliation")
                .createdInfo("Created on 29 Sep 2026, 05:30 PM")
                .status("Under Review")
                .statusTheme("orange")
                .tabs(List.of("Details", "Differences", "Logs", "Related Records"))
                .activeTab("Details")
                .type(req.getType() != null ? req.getType() : "Monthly")
                .period(req.getPeriod() != null ? req.getPeriod() : "Sep 2026")
                .company(req.getCompany() != null ? req.getCompany() : "Atirath Holdings")
                .dataSources(req.getDataSources() != null ? req.getDataSources() : "Trading System, Escrow System, Bank Statements")
                .totalRecords(12856)
                .matchedRecords(11942)
                .unmatchedRecords(724)
                .underReview(142)
                .exceptions(48)
                .startedOn("29 Sep 2026, 05:30 PM")
                .completedOn("-")
                .createdBy("Admin User")
                .remarks(req.getRemarks() != null ? req.getRemarks() : "Manual run initiated.")
                .build();

        reconciliationDetailsMap.put(id, detail);
        return detail;
    }

    public ReconciliationDetailDto approveReconciliation(String id) {
        ReconciliationDetailDto detail = getReconciliationDetail(id);
        detail.setStatus("Approved");
        detail.setStatusTheme("green");
        detail.setCompletedOn("29 Sep 2026, 05:35 PM");
        return detail;
    }

    public ReconciliationDetailDto rejectReconciliation(String id) {
        ReconciliationDetailDto detail = getReconciliationDetail(id);
        detail.setStatus("Rejected");
        detail.setStatusTheme("red");
        return detail;
    }

    public ReconciliationDetailDto markReconciliationReviewed(String id) {
        ReconciliationDetailDto detail = getReconciliationDetail(id);
        detail.setStatus("Reviewed");
        detail.setStatusTheme("green");
        return detail;
    }

    // ==========================================
    // 8. DISPUTES
    // ==========================================
    public DisputesPageDto getDisputesPage(String company, String tab, String query, String disputeType, String status, String priority) {
        List<KpiCardDto> kpis = List.of(
                KpiCardDto.builder().title("Total Disputes").value("36").change("+20% vs last quarter").comparisonText("").trend("negative").icon("file-text").color("red").build(),
                KpiCardDto.builder().title("Open Disputes").value("18").change("+50% vs last quarter").comparisonText("").trend("negative").icon("hourglass").color("red").build(),
                KpiCardDto.builder().title("In Progress").value("10").change("+11% vs last quarter").comparisonText("").trend("neutral").icon("refresh-cw").color("blue").build(),
                KpiCardDto.builder().title("Resolved").value("6").change("+50% vs last quarter").comparisonText("").trend("positive").icon("check-circle").color("green").build(),
                KpiCardDto.builder().title("Escalated").value("2").change("+100% vs last quarter").comparisonText("").trend("negative").icon("x-circle").color("red").build(),
                KpiCardDto.builder().title("Avg. Resolution Time").value("5.8 Days").change("-25% vs last quarter").comparisonText("").trend("positive").icon("clock").color("blue").build()
        );

        DisputesPageDto.DisputesTypeDonutDto donut = DisputesPageDto.DisputesTypeDonutDto.builder()
                .totalDisputes("36 Total Disputes")
                .slices(List.of(
                        new DisputesPageDto.DisputeSliceDto("Trade Dispute", 36.1, 13, "#ef4444"),
                        new DisputesPageDto.DisputeSliceDto("Payment Dispute", 22.2, 8, "#3b82f6"),
                        new DisputesPageDto.DisputeSliceDto("Settlement Dispute", 19.4, 7, "#f59e0b"),
                        new DisputesPageDto.DisputeSliceDto("Transfer Dispute", 13.9, 5, "#a855f7"),
                        new DisputesPageDto.DisputeSliceDto("Others", 8.3, 3, "#14b8a6")
                )).build();

        List<DisputesPageDto.DisputeStatusBarDto> statusBar = List.of(
                new DisputesPageDto.DisputeStatusBarDto("Open", 50.0, 18, "#ef4444"),
                new DisputesPageDto.DisputeStatusBarDto("In Progress", 27.8, 10, "#3b82f6"),
                new DisputesPageDto.DisputeStatusBarDto("Resolved", 16.7, 6, "#22c55e"),
                new DisputesPageDto.DisputeStatusBarDto("Escalated", 5.6, 2, "#dc2626")
        );

        List<DisputesPageDto.DisputeTrendMonthDto> trend = List.of(
                new DisputesPageDto.DisputeTrendMonthDto("Apr 2026", 5, 3, 2, 0),
                new DisputesPageDto.DisputeTrendMonthDto("May 2026", 7, 4, 3, 1),
                new DisputesPageDto.DisputeTrendMonthDto("Jun 2026", 10, 5, 4, 1),
                new DisputesPageDto.DisputeTrendMonthDto("Jul 2026", 12, 6, 4, 1),
                new DisputesPageDto.DisputeTrendMonthDto("Aug 2026", 15, 8, 5, 2),
                new DisputesPageDto.DisputeTrendMonthDto("Sep 2026", 18, 10, 6, 2)
        );

        List<DisputesPageDto.DisputeItemDto> filtered = disputesList.stream()
                .filter(d -> {
                    if (query != null && !query.isBlank()) {
                        String q = query.toLowerCase();
                        if (!d.getDisputeId().toLowerCase().contains(q) &&
                            !d.getRelatedId().toLowerCase().contains(q) &&
                            !d.getInvestorOrCounterparty().toLowerCase().contains(q)) return false;
                    }
                    if (tab != null && !tab.isBlank() && !tab.equalsIgnoreCase("All Disputes")) {
                        if (tab.equalsIgnoreCase("Open") && !d.getStatus().equalsIgnoreCase("Open")) return false;
                        if (tab.equalsIgnoreCase("In Progress") && !d.getStatus().equalsIgnoreCase("In Progress")) return false;
                        if (tab.equalsIgnoreCase("Resolved") && !d.getStatus().equalsIgnoreCase("Resolved")) return false;
                        if (tab.equalsIgnoreCase("Escalated") && !d.getStatus().equalsIgnoreCase("Escalated")) return false;
                    }
                    if (disputeType != null && !disputeType.isBlank() && !disputeType.equalsIgnoreCase("All Dispute Types")) {
                        if (!d.getType().equalsIgnoreCase(disputeType)) return false;
                    }
                    if (status != null && !status.isBlank() && !status.equalsIgnoreCase("All Status")) {
                        if (!d.getStatus().equalsIgnoreCase(status)) return false;
                    }
                    if (priority != null && !priority.isBlank() && !priority.equalsIgnoreCase("Priority")) {
                        if (!d.getPriority().equalsIgnoreCase(priority)) return false;
                    }
                    return true;
                }).collect(Collectors.toList());

        DisputeDetailDto selected = disputeDetailsMap.getOrDefault("dp-1", disputeDetailsMap.values().iterator().next());

        return DisputesPageDto.builder()
                .adminUser(adminUser)
                .pageTitle("Disputes")
                .pageSubtitle("Manage investor and counterparty disputes related to trades, transfers, settlements and payments.")
                .selectedCompany(company != null ? company : "Atirath Holdings")
                .dateRange("01 Apr 2026 - 30 Sep 2026")
                .kpis(kpis)
                .avgResolutionTime("5.8 Days")
                .avgResolutionTimeChange("-25% vs last quarter")
                .tabs(List.of("All Disputes", "Open", "In Progress", "Resolved", "Escalated", "My Disputes"))
                .activeTab(tab != null ? tab : "All Disputes")
                .companies(List.of("All Companies", "Atirath Traders", "Farm Vantara", "Atirath Holdings", "Sunrise Ventures", "Green Future Fund", "Global Agri Fund", "Atirath Technologies"))
                .disputeTypes(List.of("All Dispute Types", "Trade Dispute", "Payment Dispute", "Settlement Dispute", "Transfer Dispute"))
                .statuses(List.of("All Status", "Open", "In Progress", "Resolved", "Escalated"))
                .priorities(List.of("Priority", "High", "Medium", "Low"))
                .disputes(filtered)
                .totalDisputes(36)
                .currentPage(1)
                .totalPages(4)
                .pageSize(10)
                .disputesByType(donut)
                .disputesByStatus(statusBar)
                .disputesTrend(trend)
                .selectedDisputeDetail(selected)
                .build();
    }

    public DisputeDetailDto getDisputeDetail(String id) {
        return disputeDetailsMap.getOrDefault(id, disputeDetailsMap.get("dp-1"));
    }

    public DisputeDetailDto raiseDispute(RaiseDisputeRequestDto req) {
        int nextNum = disputeCounter.incrementAndGet();
        String id = "dp-" + nextNum;
        String dpid = "DP-2026-" + String.format("%03d", nextNum);

        DisputesPageDto.DisputeItemDto item = DisputesPageDto.DisputeItemDto.builder()
                .id(id)
                .number(nextNum)
                .disputeId(dpid)
                .type(req.getType() != null ? req.getType() : "Trade Dispute")
                .typeTheme("red")
                .relatedId(req.getRelatedId() != null ? req.getRelatedId() : "TR-2026-001")
                .investorOrCounterparty(req.getInvestor() != null ? req.getInvestor() : "Investor")
                .amount(req.getAmount() != null ? req.getAmount() : "10,000")
                .priority(req.getPriority() != null ? req.getPriority() : "High")
                .priorityTheme(req.getPriority() != null && req.getPriority().equalsIgnoreCase("High") ? "red" : "orange")
                .status("Open")
                .statusTheme("red")
                .raisedOn("29 Sep 2026")
                .targetResolution(req.getTargetResolution() != null ? req.getTargetResolution() : "06 Oct 2026")
                .build();

        disputesList.add(0, item);

        DisputeDetailDto detail = DisputeDetailDto.builder()
                .id(id)
                .disputeId(dpid)
                .disputeTypeTitle(req.getType() != null ? req.getType() : "Trade Dispute")
                .raisedInfo("Raised on 29 Sep 2026, 05:40 PM")
                .status("Open")
                .statusTheme("red")
                .tabs(List.of("Details", "Timeline", "Documents", "Related Transactions"))
                .activeTab("Details")
                .type(req.getType() != null ? req.getType() : "Trade Dispute")
                .relatedTradeId(req.getRelatedId() != null ? req.getRelatedId() : "TR-2026-001")
                .investor(req.getInvestor() != null ? req.getInvestor() : "Investor")
                .counterparty(req.getCounterparty() != null ? req.getCounterparty() : "Atirath Traders")
                .amount(req.getAmount() != null ? req.getAmount() : "10,000")
                .priority(req.getPriority() != null ? req.getPriority() : "High")
                .priorityTheme(req.getPriority() != null && req.getPriority().equalsIgnoreCase("High") ? "red" : "orange")
                .raisedOn("29 Sep 2026, 05:40 PM")
                .targetResolution(req.getTargetResolution() != null ? req.getTargetResolution() : "06 Oct 2026")
                .assignedTo("Operations Team")
                .description(req.getDescription() != null ? req.getDescription() : "Dispute raised for transaction.")
                .stage("Investigation")
                .daysElapsed("0 days")
                .daysRemaining("7 days")
                .relatedTransactions("1 View")
                .relatedSettlements("-")
                .relatedPayments("1 View")
                .build();

        disputeDetailsMap.put(id, detail);
        return detail;
    }

    public DisputeDetailDto resolveDispute(String id) {
        DisputeDetailDto detail = getDisputeDetail(id);
        detail.setStatus("Resolved");
        detail.setStatusTheme("green");

        disputesList.stream().filter(d -> d.getId().equalsIgnoreCase(id)).findFirst().ifPresent(d -> {
            d.setStatus("Resolved");
            d.setStatusTheme("green");
        });
        return detail;
    }

    public DisputeDetailDto escalateDispute(String id) {
        DisputeDetailDto detail = getDisputeDetail(id);
        detail.setStatus("Escalated");
        detail.setStatusTheme("red");

        disputesList.stream().filter(d -> d.getId().equalsIgnoreCase(id)).findFirst().ifPresent(d -> {
            d.setStatus("Escalated");
            d.setStatusTheme("red");
        });
        return detail;
    }

    // ==========================================
    // 9. SUPPORT TICKETS
    // ==========================================
    public SupportTicketsPageDto getSupportTicketsPage(String company, String tab, String query, String category, String priority, String status) {
        List<KpiCardDto> kpis = List.of(
                KpiCardDto.builder().title("Total Tickets").value("142").change("+18% vs last quarter").comparisonText("").trend("positive").icon("file-text").color("green").build(),
                KpiCardDto.builder().title("Open").value("36").change("+20% vs last quarter").comparisonText("").trend("negative").icon("alert-circle").color("red").build(),
                KpiCardDto.builder().title("In Progress").value("58").change("+12% vs last quarter").comparisonText("").trend("neutral").icon("refresh-cw").color("blue").build(),
                KpiCardDto.builder().title("Resolved").value("40").change("+25% vs last quarter").comparisonText("").trend("positive").icon("check-circle").color("green").build(),
                KpiCardDto.builder().title("Closed").value("8").change("+14% vs last quarter").comparisonText("").trend("neutral").icon("check-square").color("gray").build(),
                KpiCardDto.builder().title("Avg. Resolution Time").value("2.6 Days").change("-30% vs last quarter").comparisonText("").trend("positive").icon("clock").color("blue").build()
        );

        SupportTicketsPageDto.TicketsCategoryDonutDto donut = SupportTicketsPageDto.TicketsCategoryDonutDto.builder()
                .totalTickets("142 Total Tickets")
                .slices(List.of(
                        new SupportTicketsPageDto.CategorySliceDto("Trade", 26.8, 38, "#ef4444"),
                        new SupportTicketsPageDto.CategorySliceDto("Settlement", 16.9, 24, "#3b82f6"),
                        new SupportTicketsPageDto.CategorySliceDto("Transfer", 14.8, 21, "#eab308"),
                        new SupportTicketsPageDto.CategorySliceDto("Payment", 11.3, 16, "#14b8a6"),
                        new SupportTicketsPageDto.CategorySliceDto("Compliance", 9.9, 14, "#a855f7"),
                        new SupportTicketsPageDto.CategorySliceDto("Reporting", 7.0, 10, "#6366f1"),
                        new SupportTicketsPageDto.CategorySliceDto("User Management", 6.3, 9, "#ec4899"),
                        new SupportTicketsPageDto.CategorySliceDto("Others", 7.0, 10, "#64748b")
                )).build();

        List<SupportTicketsPageDto.TicketStatusBarDto> statusBar = List.of(
                new SupportTicketsPageDto.TicketStatusBarDto("Open", 25.4, 36, "#ef4444"),
                new SupportTicketsPageDto.TicketStatusBarDto("In Progress", 40.8, 58, "#3b82f6"),
                new SupportTicketsPageDto.TicketStatusBarDto("Resolved", 28.2, 40, "#22c55e"),
                new SupportTicketsPageDto.TicketStatusBarDto("Closed", 5.6, 8, "#94a3b8")
        );

        List<SupportTicketsPageDto.TicketTrendMonthDto> trend = List.of(
                new SupportTicketsPageDto.TicketTrendMonthDto("Apr 2026", 15, 20, 12, 3),
                new SupportTicketsPageDto.TicketTrendMonthDto("May 2026", 18, 26, 18, 4),
                new SupportTicketsPageDto.TicketTrendMonthDto("Jun 2026", 24, 34, 22, 5),
                new SupportTicketsPageDto.TicketTrendMonthDto("Jul 2026", 28, 42, 28, 6),
                new SupportTicketsPageDto.TicketTrendMonthDto("Aug 2026", 32, 50, 34, 7),
                new SupportTicketsPageDto.TicketTrendMonthDto("Sep 2026", 36, 58, 40, 8)
        );

        List<SupportTicketsPageDto.TicketItemDto> filtered = supportTicketsList.stream()
                .filter(t -> {
                    if (query != null && !query.isBlank()) {
                        String q = query.toLowerCase();
                        if (!t.getTicketId().toLowerCase().contains(q) &&
                            !t.getSubject().toLowerCase().contains(q) &&
                            !t.getCompany().toLowerCase().contains(q) &&
                            !t.getRaisedBy().toLowerCase().contains(q)) return false;
                    }
                    if (tab != null && !tab.isBlank() && !tab.equalsIgnoreCase("All Tickets")) {
                        if (tab.equalsIgnoreCase("Open") && !t.getStatus().equalsIgnoreCase("Open")) return false;
                        if (tab.equalsIgnoreCase("In Progress") && !t.getStatus().equalsIgnoreCase("In Progress")) return false;
                        if (tab.equalsIgnoreCase("Resolved") && !t.getStatus().equalsIgnoreCase("Resolved")) return false;
                        if (tab.equalsIgnoreCase("Closed") && !t.getStatus().equalsIgnoreCase("Closed")) return false;
                    }
                    if (category != null && !category.isBlank() && !category.equalsIgnoreCase("All Categories")) {
                        if (!t.getCategory().equalsIgnoreCase(category)) return false;
                    }
                    if (priority != null && !priority.isBlank() && !priority.equalsIgnoreCase("All Priorities")) {
                        if (!t.getPriority().equalsIgnoreCase(priority)) return false;
                    }
                    if (status != null && !status.isBlank() && !status.equalsIgnoreCase("All Status")) {
                        if (!t.getStatus().equalsIgnoreCase(status)) return false;
                    }
                    return true;
                }).collect(Collectors.toList());

        SupportTicketDetailDto selected = supportTicketDetailsMap.getOrDefault("tk-1", supportTicketDetailsMap.values().iterator().next());

        return SupportTicketsPageDto.builder()
                .adminUser(adminUser)
                .pageTitle("Support Tickets")
                .pageSubtitle("Manage internal and external support tickets related to trades, transfers, settlements and platform issues.")
                .selectedCompany(company != null ? company : "Atirath Holdings")
                .dateRange("01 Apr 2026 - 30 Sep 2026")
                .kpis(kpis)
                .avgResolutionTime("2.6 Days")
                .avgResolutionTimeChange("-30% vs last quarter")
                .tabs(List.of("All Tickets", "Open", "In Progress", "Resolved", "Closed", "My Tickets"))
                .activeTab(tab != null ? tab : "All Tickets")
                .companies(List.of("All Companies", "Atirath Traders", "Farm Vantara", "Atirath Holdings", "Sunrise Ventures", "Green Future Fund", "Global Agri Fund", "Atirath Technologies"))
                .categories(List.of("All Categories", "Trade", "Settlement", "Transfer", "Payment", "Compliance", "Reporting", "User Management", "Reconciliation", "Document"))
                .priorities(List.of("All Priorities", "High", "Medium", "Low"))
                .statuses(List.of("All Status", "Open", "In Progress", "Resolved", "Closed"))
                .tickets(filtered)
                .totalTickets(142)
                .currentPage(1)
                .totalPages(15)
                .pageSize(10)
                .ticketsByCategory(donut)
                .ticketsByStatus(statusBar)
                .ticketsTrend(trend)
                .selectedTicketDetail(selected)
                .build();
    }

    public SupportTicketDetailDto getSupportTicketDetail(String id) {
        return supportTicketDetailsMap.getOrDefault(id, supportTicketDetailsMap.get("tk-1"));
    }

    public SupportTicketDetailDto createSupportTicket(CreateTicketRequestDto req) {
        int nextNum = ticketCounter.incrementAndGet();
        String id = "tk-" + nextNum;
        String tkid = "TK-2026-" + String.format("%03d", nextNum);

        SupportTicketsPageDto.TicketItemDto item = SupportTicketsPageDto.TicketItemDto.builder()
                .id(id)
                .number(nextNum)
                .ticketId(tkid)
                .subject(req.getSubject() != null ? req.getSubject() : "Support Request")
                .category(req.getCategory() != null ? req.getCategory() : "Trade")
                .categoryTheme("blue")
                .company(req.getCompany() != null ? req.getCompany() : "Atirath Traders")
                .raisedBy(req.getRaisedBy() != null ? req.getRaisedBy() : "Admin User")
                .priority(req.getPriority() != null ? req.getPriority() : "Medium")
                .priorityTheme(req.getPriority() != null && req.getPriority().equalsIgnoreCase("High") ? "red" : "orange")
                .status("Open")
                .statusTheme("red")
                .raisedOn("29 Sep 2026")
                .expectedResolution("30 Sep 2026")
                .build();

        supportTicketsList.add(0, item);

        SupportTicketDetailDto detail = SupportTicketDetailDto.builder()
                .id(id)
                .ticketId(tkid)
                .subject(req.getSubject() != null ? req.getSubject() : "Support Request")
                .raisedInfo("Raised on 29 Sep 2026, 05:45 PM")
                .status("Open")
                .statusTheme("red")
                .tabs(List.of("Details", "Comments", "Attachments", "Activity"))
                .activeTab("Details")
                .category(req.getCategory() != null ? req.getCategory() : "Trade")
                .company(req.getCompany() != null ? req.getCompany() : "Atirath Traders")
                .raisedBy(req.getRaisedBy() != null ? req.getRaisedBy() : "Admin User")
                .priority(req.getPriority() != null ? req.getPriority() : "Medium")
                .priorityTheme(req.getPriority() != null && req.getPriority().equalsIgnoreCase("High") ? "red" : "orange")
                .raisedOn("29 Sep 2026, 05:45 PM")
                .expectedResolution("30 Sep 2026")
                .assignedTo("Operations Team")
                .description(req.getDescription() != null ? req.getDescription() : "Operational support issue.")
                .slaTarget("1 day")
                .timeElapsed("0 hours")
                .timeRemaining("24 hours")
                .slaStatusTheme("green")
                .relatedTrade("-")
                .relatedSettlement("-")
                .relatedTransfer("-")
                .build();

        supportTicketDetailsMap.put(id, detail);
        return detail;
    }

    public SupportTicketDetailDto updateTicketStatus(String id, String status) {
        SupportTicketDetailDto detail = getSupportTicketDetail(id);
        detail.setStatus(status != null ? status : "Resolved");
        detail.setStatusTheme("green");

        supportTicketsList.stream().filter(t -> t.getId().equalsIgnoreCase(id)).findFirst().ifPresent(t -> {
            t.setStatus(status != null ? status : "Resolved");
            t.setStatusTheme("green");
        });
        return detail;
    }

    public SupportTicketDetailDto assignTicket(String id, String assignee) {
        SupportTicketDetailDto detail = getSupportTicketDetail(id);
        detail.setAssignedTo(assignee != null ? assignee : "Operations Team");
        return detail;
    }

    // ==========================================
    // SEEDING HELPERS
    // ==========================================
    private void seedPendingTrades() {
        pendingTradesList.clear();
        pendingTradesList.add(new PendingTradesPageDto.PendingTradeItemDto("tr-1", 1, "TR-2026-001", "29 Sep 2026", "Atirath Traders", "Equity - A", "Rakesh Kumar", "Buy", "10,000", "15,00,000", "Approval", "orange", "1 day"));
        pendingTradesList.add(new PendingTradesPageDto.PendingTradeItemDto("tr-2", 2, "TR-2026-002", "28 Sep 2026", "Farm Vantara", "Equity - B", "Priya Mehta", "Sell", "25,000", "18,50,000", "Verification", "blue", "2 days"));
        pendingTradesList.add(new PendingTradesPageDto.PendingTradeItemDto("tr-3", 3, "TR-2026-003", "27 Sep 2026", "Atirath Holdings", "Preference", "Vikram Rao", "Buy", "5,000", "42,00,000", "Settlement", "green", "3 days"));
        pendingTradesList.add(new PendingTradesPageDto.PendingTradeItemDto("tr-4", 4, "TR-2026-004", "26 Sep 2026", "Sunrise Ventures", "Equity - A", "Neha Sharma", "Transfer", "12,500", "15,75,000", "Approval", "orange", "2 days"));
        pendingTradesList.add(new PendingTradesPageDto.PendingTradeItemDto("tr-5", 5, "TR-2026-005", "25 Sep 2026", "Green Future Fund", "Equity - B", "Suresh Kumar", "Buy", "20,000", "33,20,000", "Overdue", "red", "5 days"));
        pendingTradesList.add(new PendingTradesPageDto.PendingTradeItemDto("tr-6", 6, "TR-2026-006", "24 Sep 2026", "Atirath Technologies", "Equity - A", "Ananya Reddy", "Sell", "8,000", "11,20,000", "Settlement", "green", "3 days"));
        pendingTradesList.add(new PendingTradesPageDto.PendingTradeItemDto("tr-7", 7, "TR-2026-007", "23 Sep 2026", "Global Agri Fund", "Equity - B", "Lakshmi Narayan", "Buy", "15,000", "24,00,000", "Settlement", "green", "4 days"));
        pendingTradesList.add(new PendingTradesPageDto.PendingTradeItemDto("tr-8", 8, "TR-2026-008", "22 Sep 2026", "Farm Vantara", "Preference", "Sunil Verma", "Transfer", "10,000", "9,50,000", "Approval", "orange", "3 days"));
        pendingTradesList.add(new PendingTradesPageDto.PendingTradeItemDto("tr-9", 9, "TR-2026-009", "21 Sep 2026", "Atirath Traders", "Equity - A", "Kavya Iyer", "Buy", "18,000", "27,00,000", "Overdue", "red", "6 days"));
        pendingTradesList.add(new PendingTradesPageDto.PendingTradeItemDto("tr-10", 10, "TR-2026-010", "20 Sep 2026", "Sunrise Ventures", "Equity - B", "Rajesh Mehta", "Sell", "7,500", "10,80,000", "Verification", "blue", "4 days"));

        TradeDetailDto tr1 = TradeDetailDto.builder()
                .id("tr-1")
                .tradeId("TR-2026-001")
                .orderTypeTitle("Buy Order")
                .status("Awaiting Approval")
                .statusTheme("orange")
                .createdInfo("Created on 29 Sep 2026, 11:20 AM")
                .tabs(List.of("Details", "History", "Documents", "Related Transactions"))
                .activeTab("Details")
                .type("Buy")
                .company("Atirath Traders")
                .shareClass("Equity - A")
                .investor("Rakesh Kumar")
                .noOfShares("10,000")
                .pricePerShare("150")
                .totalValue("15,00,000")
                .tradeDate("29 Sep 2026")
                .pendingSince("1 day")
                .settlementDate("-")
                .complianceReviewStatus("Pending")
                .riskApprovalStatus("Not Started")
                .settlementApprovalStatus("Not Started")
                .isin("INE123A01034")
                .orderSource("Web Portal")
                .createdBy("Rakesh Kumar")
                .createdOn("29 Sep 2026, 11:20 AM")
                .remarks("Large volume trade. Requires approval.")
                .build();

        tradeDetailsMap.put("tr-1", tr1);
    }

    private void seedPendingTransfers() {
        pendingTransfersList.clear();
        pendingTransfersList.add(new PendingTransfersPageDto.PendingTransferItemDto("tf-1", 1, "TF-2026-001", "29 Sep 2026", "Rakesh Kumar", "Priya Mehta", "10,000", "Equity - A", "Transfer", "gray", "Approval", "orange", "-"));
        pendingTransfersList.add(new PendingTransfersPageDto.PendingTransferItemDto("tf-2", 2, "TF-2026-002", "28 Sep 2026", "Global Agri Fund", "Atirath Traders", "25,000", "Equity - B", "Transfer", "gray", "Verification", "blue", "-"));
        pendingTransfersList.add(new PendingTransfersPageDto.PendingTransferItemDto("tf-3", 3, "TF-2026-003", "27 Sep 2026", "Sunrise Ventures", "Vikram Rao", "5,000", "Preference", "Gift", "blue", "Settlement", "green", "30 Sep 2026"));
        pendingTransfersList.add(new PendingTransfersPageDto.PendingTransferItemDto("tf-4", 4, "TF-2026-004", "26 Sep 2026", "Neha Sharma", "Suresh Kumar", "12,500", "Equity - A", "Transfer", "gray", "Approval", "orange", "-"));
        pendingTransfersList.add(new PendingTransfersPageDto.PendingTransferItemDto("tf-5", 5, "TF-2026-005", "25 Sep 2026", "Farm Vantara", "Ananya Reddy", "20,000", "Equity - B", "Inheritance", "orange", "Overdue", "red", "-"));
        pendingTransfersList.add(new PendingTransfersPageDto.PendingTransferItemDto("tf-6", 6, "TF-2026-006", "24 Sep 2026", "Atirath Technologies", "Rajesh Mehta", "8,000", "Equity - A", "Transfer", "gray", "Verification", "blue", "-"));
        pendingTransfersList.add(new PendingTransfersPageDto.PendingTransferItemDto("tf-7", 7, "TF-2026-007", "23 Sep 2026", "Priya Mehta", "Lakshmi Narayan", "15,000", "Preference", "Transfer", "gray", "Settlement", "green", "28 Sep 2026"));
        pendingTransfersList.add(new PendingTransfersPageDto.PendingTransferItemDto("tf-8", 8, "TF-2026-008", "22 Sep 2026", "Green Future Fund", "Sunil Verma", "10,000", "Equity - B", "Transfer", "gray", "Approval", "orange", "-"));
        pendingTransfersList.add(new PendingTransfersPageDto.PendingTransferItemDto("tf-9", 9, "TF-2026-009", "21 Sep 2026", "Vikram Rao", "Kavya Iyer", "16,000", "Equity - A", "Gift", "blue", "Overdue", "red", "-"));
        pendingTransfersList.add(new PendingTransfersPageDto.PendingTransferItemDto("tf-10", 10, "TF-2026-010", "20 Sep 2026", "Atirath Holdings", "Neha Sharma", "7,500", "Preference", "Transfer", "gray", "Verification", "blue", "-"));

        TransferDetailDto tf1 = TransferDetailDto.builder()
                .id("tf-1")
                .transferId("TF-2026-001")
                .transferTypeTitle("Share Transfer")
                .status("Awaiting Approval")
                .statusTheme("orange")
                .createdInfo("Created on 29 Sep 2026, 11:20 AM")
                .tabs(List.of("Details", "Documents", "History", "Related Transactions"))
                .activeTab("Details")
                .type("Transfer")
                .fromParty("Rakesh Kumar")
                .toParty("Priya Mehta")
                .shares("10,000")
                .shareClass("Equity - A")
                .pricePerShare("150")
                .totalValue("15,00,000")
                .transferDate("29 Sep 2026")
                .proposedSettlement("-")
                .initiatedDate("29 Sep 2026, 11:20 AM")
                .complianceReviewStatus("Pending")
                .riskApprovalStatus("Not Started")
                .finalApprovalStatus("Not Started")
                .settlementStatus("Not Started")
                .reason("Secondary Sale")
                .remarks("-")
                .createdBy("Rakesh Kumar")
                .createdOn("29 Sep 2026, 11:20 AM")
                .build();

        transferDetailsMap.put("tf-1", tf1);
    }

    private void seedEscrowMonitoring() {
        escrowAccountsList.clear();
        escrowAccountsList.add(new EscrowMonitoringPageDto.EscrowAccountItemDto("es-1", 1, "ES-2026-001", "Atirath Traders", "TR-2026-001", "Rakesh Kumar", "Equity - A", "3,25,00,000", "Available", "green", "29 Sep 2026", "29 Sep 2026"));
        escrowAccountsList.add(new EscrowMonitoringPageDto.EscrowAccountItemDto("es-2", 2, "ES-2026-002", "Farm Vantara", "TR-2026-002", "Priya Mehta", "Equity - B", "2,80,00,000", "On Hold", "orange", "28 Sep 2026", "-"));
        escrowAccountsList.add(new EscrowMonitoringPageDto.EscrowAccountItemDto("es-3", 3, "ES-2026-003", "Atirath Holdings", "TR-2026-003", "Vikram Rao", "Preference", "4,50,00,000", "Available", "green", "27 Sep 2026", "30 Sep 2026"));
        escrowAccountsList.add(new EscrowMonitoringPageDto.EscrowAccountItemDto("es-4", 4, "ES-2026-004", "Sunrise Ventures", "TR-2026-004", "Neha Sharma", "Equity - A", "1,75,00,000", "Pending Release", "orange", "26 Sep 2026", "02 Oct 2026"));
        escrowAccountsList.add(new EscrowMonitoringPageDto.EscrowAccountItemDto("es-5", 5, "ES-2026-005", "Green Future Fund", "TR-2026-005", "Suresh Kumar", "Equity - B", "1,95,00,000", "Disputed", "red", "25 Sep 2026", "-"));
        escrowAccountsList.add(new EscrowMonitoringPageDto.EscrowAccountItemDto("es-6", 6, "ES-2026-006", "Global Agri Fund", "TR-2026-006", "Lakshmi Narayan", "Equity - A", "2,20,00,000", "Available", "green", "24 Sep 2026", "24 Sep 2026"));
        escrowAccountsList.add(new EscrowMonitoringPageDto.EscrowAccountItemDto("es-7", 7, "ES-2026-007", "Atirath Technologies", "TR-2026-007", "Ananya Reddy", "Equity - B", "1,10,00,000", "On Hold", "orange", "23 Sep 2026", "-"));
        escrowAccountsList.add(new EscrowMonitoringPageDto.EscrowAccountItemDto("es-8", 8, "ES-2026-008", "Priya Mehta", "TR-2026-008", "Sunil Verma", "Transfer", "1,85,00,000", "Available", "green", "22 Sep 2026", "25 Sep 2026"));
        escrowAccountsList.add(new EscrowMonitoringPageDto.EscrowAccountItemDto("es-9", 9, "ES-2026-009", "Farm Vantara", "TR-2026-009", "Kavya Iyer", "Equity - A", "2,50,00,000", "Pending Release", "orange", "21 Sep 2026", "28 Sep 2026"));
        escrowAccountsList.add(new EscrowMonitoringPageDto.EscrowAccountItemDto("es-10", 10, "ES-2026-010", "Atirath Holdings", "TR-2026-010", "Rajesh Mehta", "Equity - B", "1,40,00,000", "Available", "green", "20 Sep 2026", "20 Sep 2026"));

        EscrowDetailDto es1 = EscrowDetailDto.builder()
                .id("es-1")
                .escrowId("ES-2026-001")
                .companyTitle("Atirath Traders")
                .status("Available")
                .statusTheme("green")
                .createdInfo("Created on 29 Sep 2026, 11:20 AM")
                .tabs(List.of("Details", "Transactions", "Hold / Release History", "Notes"))
                .activeTab("Details")
                .company("Atirath Traders")
                .tradeId("TR-2026-001")
                .investor("Rakesh Kumar")
                .shareClass("Equity - A")
                .amount("3,25,00,000")
                .createdOn("29 Sep 2026, 11:20 AM")
                .expectedRelease("29 Sep 2026")
                .releasedOn("-")
                .escrowBank("HDFC Bank")
                .accountNo("50200012345678")
                .ifscCode("HDFC0001234")
                .totalAmount("3,25,00,000")
                .releasedAmount("0")
                .onHoldAmount("0")
                .pendingAmount("0")
                .availableAmount("3,25,00,000")
                .kycStatus("Verified")
                .amlCheck("Clear")
                .sanctionsCheck("Clear")
                .relatedTrade("TR-2026-001")
                .investorProfile("Rakesh Kumar")
                .documentsCount("3 Documents")
                .build();

        escrowDetailsMap.put("es-1", es1);
    }

    private void seedSettlementMonitoring() {
        settlementsList.clear();
        settlementsList.add(new SettlementMonitoringPageDto.SettlementItemDto("st-1", 1, "ST-2026-001", "TR-2026-001", "Atirath Traders", "Rakesh Kumar", "Equity - A", "15,00,000", "29 Sep 2026", "Settled", "green"));
        settlementsList.add(new SettlementMonitoringPageDto.SettlementItemDto("st-2", 2, "ST-2026-002", "TR-2026-002", "Farm Vantara", "Priya Mehta", "Equity - B", "18,50,000", "28 Sep 2026", "Pending", "orange"));
        settlementsList.add(new SettlementMonitoringPageDto.SettlementItemDto("st-3", 3, "ST-2026-003", "TR-2026-003", "Atirath Holdings", "Vikram Rao", "Preference", "42,00,000", "27 Sep 2026", "Settled", "green"));
        settlementsList.add(new SettlementMonitoringPageDto.SettlementItemDto("st-4", 4, "ST-2026-004", "TR-2026-004", "Sunrise Ventures", "Neha Sharma", "Equity - A", "15,75,000", "26 Sep 2026", "Failed", "red"));
        settlementsList.add(new SettlementMonitoringPageDto.SettlementItemDto("st-5", 5, "ST-2026-005", "TR-2026-005", "Green Future Fund", "Suresh Kumar", "Equity - B", "33,20,000", "25 Sep 2026", "Settled", "green"));
        settlementsList.add(new SettlementMonitoringPageDto.SettlementItemDto("st-6", 6, "ST-2026-006", "TR-2026-006", "Global Agri Fund", "Lakshmi Narayan", "Equity - A", "24,00,000", "24 Sep 2026", "Pending", "orange"));
        settlementsList.add(new SettlementMonitoringPageDto.SettlementItemDto("st-7", 7, "ST-2026-007", "TR-2026-007", "Atirath Technologies", "Ananya Reddy", "Equity - B", "11,20,000", "23 Sep 2026", "On Hold", "purple"));
        settlementsList.add(new SettlementMonitoringPageDto.SettlementItemDto("st-8", 8, "ST-2026-008", "TR-2026-008", "Farm Vantara", "Sunil Verma", "Transfer", "9,50,000", "22 Sep 2026", "Under Review", "blue"));
        settlementsList.add(new SettlementMonitoringPageDto.SettlementItemDto("st-9", 9, "ST-2026-009", "TR-2026-009", "Priya Mehta", "Kavya Iyer", "Equity - A", "27,00,000", "21 Sep 2026", "Settled", "green"));
        settlementsList.add(new SettlementMonitoringPageDto.SettlementItemDto("st-10", 10, "ST-2026-010", "TR-2026-010", "Sunrise Ventures", "Rajesh Mehta", "Equity - B", "10,80,000", "20 Sep 2026", "Failed", "red"));

        SettlementDetailDto st2 = SettlementDetailDto.builder()
                .id("st-2")
                .settlementId("ST-2026-002")
                .tradeSettlementTitle("Trade Settlement")
                .status("Pending")
                .statusTheme("orange")
                .createdInfo("Created on 28 Sep 2026, 10:15 AM")
                .tabs(List.of("Details", "Timeline", "Related Transactions", "Documents"))
                .activeTab("Details")
                .tradeId("TR-2026-002")
                .company("Farm Vantara")
                .investor("Priya Mehta")
                .shareClass("Equity - B")
                .amount("18,50,000")
                .settlementDate("28 Sep 2026")
                .expectedDate("28 Sep 2026")
                .settlementBank("HDFC Bank")
                .referenceNo("HDFCTST123456")
                .createdBy("Rakesh Kumar")
                .createdOn("28 Sep 2026, 10:15 AM")
                .steps(List.of(
                        new SettlementDetailDto.SettlementStepDto("Trade Confirmation", "Completed", "green", "28 Sep, 10:15 AM"),
                        new SettlementDetailDto.SettlementStepDto("Payment Received", "Pending", "orange", "-"),
                        new SettlementDetailDto.SettlementStepDto("Shares Transfer", "Not Started", "gray", "-"),
                        new SettlementDetailDto.SettlementStepDto("Settlement Complete", "Not Started", "gray", "-")
                ))
                .paymentMode("NEFT")
                .utrNo("-")
                .paymentDate("-")
                .amountReceived("0")
                .expectedAmount("18,50,000")
                .remarks("-")
                .attachmentsCount("2 Files")
                .build();

        settlementDetailsMap.put("st-2", st2);
    }

    private void seedFailedTransactions() {
        failedTransactionsList.clear();
        failedTransactionsList.add(new FailedTransactionsPageDto.FailedTransactionItemDto("ft-1", 1, "FT-2026-001", "Trade", "red", "Atirath Traders", "Rakesh Kumar", "15,75,000", "Insufficient balance", "red", "29 Sep 2026", "Not Retried", "gray"));
        failedTransactionsList.add(new FailedTransactionsPageDto.FailedTransactionItemDto("ft-2", 2, "FT-2026-002", "Transfer", "blue", "Farm Vantara", "Priya Mehta", "8,20,000", "KYC mismatch", "orange", "28 Sep 2026", "In Progress", "blue"));
        failedTransactionsList.add(new FailedTransactionsPageDto.FailedTransactionItemDto("ft-3", 3, "FT-2026-003", "Settlement", "green", "Atirath Holdings", "Vikram Rao", "22,50,000", "Bank rejection", "red", "27 Sep 2026", "Failed", "red"));
        failedTransactionsList.add(new FailedTransactionsPageDto.FailedTransactionItemDto("ft-4", 4, "FT-2026-004", "Trade", "red", "Sunrise Ventures", "Neha Sharma", "11,00,000", "Price limit breach", "orange", "26 Sep 2026", "Retrying", "green"));
        failedTransactionsList.add(new FailedTransactionsPageDto.FailedTransactionItemDto("ft-5", 5, "FT-2026-005", "Payment", "pink", "Green Future Fund", "Suresh Kumar", "6,75,000", "Payment gateway error", "red", "25 Sep 2026", "Failed", "red"));
        failedTransactionsList.add(new FailedTransactionsPageDto.FailedTransactionItemDto("ft-6", 6, "FT-2026-006", "Trade", "red", "Global Agri Fund", "Lakshmi Narayan", "19,50,000", "Insufficient balance", "red", "24 Sep 2026", "Not Retried", "gray"));
        failedTransactionsList.add(new FailedTransactionsPageDto.FailedTransactionItemDto("ft-7", 7, "FT-2026-007", "Transfer", "blue", "Atirath Technologies", "Ananya Reddy", "9,20,000", "Beneficiary not found", "orange", "23 Sep 2026", "In Progress", "blue"));
        failedTransactionsList.add(new FailedTransactionsPageDto.FailedTransactionItemDto("ft-8", 8, "FT-2026-008", "Settlement", "green", "Farm Vantara", "Sunil Verma", "12,80,000", "Compliance block", "red", "22 Sep 2026", "Not Retried", "gray"));
        failedTransactionsList.add(new FailedTransactionsPageDto.FailedTransactionItemDto("ft-9", 9, "FT-2026-009", "Trade", "red", "Atirath Traders", "Kavya Iyer", "18,00,000", "Price variance", "orange", "21 Sep 2026", "Retrying", "green"));
        failedTransactionsList.add(new FailedTransactionsPageDto.FailedTransactionItemDto("ft-10", 10, "FT-2026-010", "Settlement", "green", "Sunrise Ventures", "Rajesh Mehta", "10,80,000", "Bank timeout", "red", "20 Sep 2026", "Failed", "red"));

        FailedTransactionDetailDto ft1 = FailedTransactionDetailDto.builder()
                .id("ft-1")
                .transactionId("FT-2026-001")
                .transactionTypeTitle("Trade Transaction")
                .createdInfo("Created on 29 Sep 2026, 11:20 AM")
                .status("Failed")
                .statusTheme("red")
                .tabs(List.of("Details", "Logs", "Documents", "Related Transactions"))
                .activeTab("Details")
                .type("Trade")
                .company("Atirath Traders")
                .investor("Rakesh Kumar")
                .shareClass("Equity - A")
                .noOfShares("10,000")
                .pricePerShare("150")
                .totalValue("15,00,000")
                .orderDate("29 Sep 2026, 11:20 AM")
                .executionDate("-")
                .failureReason("Insufficient balance")
                .errorCode("ERR_001")
                .errorMessage("Available balance is less than required amount.")
                .failedOn("29 Sep 2026, 11:22 AM")
                .retryStatus("Not Retried")
                .autoRetry("Enabled")
                .nextRetryOn("29 Sep 2026, 01:22 PM")
                .retryCount("0/3")
                .build();

        failedDetailsMap.put("ft-1", ft1);
    }

    private void seedReconciliation() {
        reconciliationRecordsList.clear();
        reconciliationRecordsList.add(new ReconciliationPageDto.ReconciliationRecordItemDto("rc-1", 1, "RC-2026-001", "Trade", "blue", "TR-2026-001", "Atirath Traders", "15,00,000", "15,00,000", "0", "green", "Matched", "green", "29 Sep 2026"));
        reconciliationRecordsList.add(new ReconciliationPageDto.ReconciliationRecordItemDto("rc-2", 2, "RC-2026-002", "Settlement", "purple", "ST-2026-001", "Farm Vantara", "18,50,000", "18,50,000", "0", "green", "Matched", "green", "29 Sep 2026"));
        reconciliationRecordsList.add(new ReconciliationPageDto.ReconciliationRecordItemDto("rc-3", 3, "RC-2026-003", "Transfer", "blue", "TF-2026-001", "Atirath Holdings", "42,00,000", "41,95,000", "-5,000", "red", "Unmatched", "red", "-"));
        reconciliationRecordsList.add(new ReconciliationPageDto.ReconciliationRecordItemDto("rc-4", 4, "RC-2026-004", "Payment", "teal", "PY-2026-001", "Sunrise Ventures", "10,80,000", "10,80,000", "0", "green", "Matched", "green", "28 Sep 2026"));
        reconciliationRecordsList.add(new ReconciliationPageDto.ReconciliationRecordItemDto("rc-5", 5, "RC-2026-005", "Trade", "blue", "TR-2026-002", "Green Future Fund", "6,75,000", "6,70,000", "-5,000", "orange", "Under Review", "orange", "-"));
        reconciliationRecordsList.add(new ReconciliationPageDto.ReconciliationRecordItemDto("rc-6", 6, "RC-2026-006", "Settlement", "purple", "ST-2026-002", "Global Agri Fund", "24,00,000", "24,00,000", "0", "green", "Matched", "green", "27 Sep 2026"));
        reconciliationRecordsList.add(new ReconciliationPageDto.ReconciliationRecordItemDto("rc-7", 7, "RC-2026-007", "Transfer", "blue", "TF-2026-002", "Atirath Technologies", "9,20,000", "9,20,000", "0", "green", "Matched", "green", "26 Sep 2026"));
        reconciliationRecordsList.add(new ReconciliationPageDto.ReconciliationRecordItemDto("rc-8", 8, "RC-2026-008", "Payment", "teal", "PY-2026-002", "Priya Mehta", "12,80,000", "12,75,000", "-5,000", "red", "Exceptions", "red", "-"));
        reconciliationRecordsList.add(new ReconciliationPageDto.ReconciliationRecordItemDto("rc-9", 9, "RC-2026-009", "Trade", "blue", "TR-2026-003", "Farm Vantara", "18,00,000", "18,00,000", "0", "green", "Matched", "green", "25 Sep 2026"));
        reconciliationRecordsList.add(new ReconciliationPageDto.ReconciliationRecordItemDto("rc-10", 10, "RC-2026-010", "Settlement", "purple", "ST-2026-003", "Atirath Traders", "10,80,000", "10,75,000", "-5,000", "red", "Unmatched", "red", "-"));

        ReconciliationDetailDto rc1 = ReconciliationDetailDto.builder()
                .id("rc-1")
                .reconciliationId("RC-2026-001")
                .reconciliationTitle("Monthly Reconciliation")
                .createdInfo("Created on 29 Sep 2026, 10:20 AM")
                .status("Under Review")
                .statusTheme("orange")
                .tabs(List.of("Details", "Differences", "Logs", "Related Records"))
                .activeTab("Details")
                .type("Monthly")
                .period("Sep 2026")
                .company("Atirath Holdings")
                .dataSources("Trading System, Escrow System, Bank Statements")
                .totalRecords(12856)
                .matchedRecords(11942)
                .unmatchedRecords(724)
                .underReview(142)
                .exceptions(48)
                .startedOn("29 Sep 2026, 10:20 AM")
                .completedOn("-")
                .createdBy("Rakesh Kumar")
                .remarks("Differences in settlement amounts for few trades")
                .build();

        reconciliationDetailsMap.put("rc-1", rc1);
    }

    private void seedDisputes() {
        disputesList.clear();
        disputesList.add(new DisputesPageDto.DisputeItemDto("dp-1", 1, "DP-2026-001", "Trade Dispute", "red", "TR-2026-001", "Rakesh Kumar", "15,00,000", "High", "red", "Open", "red", "29 Sep 2026", "06 Oct 2026"));
        disputesList.add(new DisputesPageDto.DisputeItemDto("dp-2", 2, "DP-2026-002", "Payment Dispute", "blue", "PY-2026-002", "Priya Mehta", "8,20,000", "Medium", "orange", "In Progress", "blue", "28 Sep 2026", "05 Oct 2026"));
        disputesList.add(new DisputesPageDto.DisputeItemDto("dp-3", 3, "DP-2026-003", "Settlement Dispute", "purple", "ST-2026-001", "Vikram Rao", "22,50,000", "High", "red", "Escalated", "red", "27 Sep 2026", "04 Oct 2026"));
        disputesList.add(new DisputesPageDto.DisputeItemDto("dp-4", 4, "DP-2026-004", "Transfer Dispute", "green", "TF-2026-003", "Neha Sharma", "11,00,000", "Low", "green", "Resolved", "green", "26 Sep 2026", "02 Oct 2026"));
        disputesList.add(new DisputesPageDto.DisputeItemDto("dp-5", 5, "DP-2026-005", "Trade Dispute", "red", "TR-2026-005", "Suresh Kumar", "6,75,000", "Medium", "orange", "In Progress", "blue", "25 Sep 2026", "03 Oct 2026"));
        disputesList.add(new DisputesPageDto.DisputeItemDto("dp-6", 6, "DP-2026-006", "Payment Dispute", "blue", "PY-2026-004", "Lakshmi Narayan", "19,50,000", "High", "red", "Open", "red", "24 Sep 2026", "01 Oct 2026"));
        disputesList.add(new DisputesPageDto.DisputeItemDto("dp-7", 7, "DP-2026-007", "Settlement Dispute", "purple", "ST-2026-002", "Ananya Reddy", "9,20,000", "Low", "green", "Resolved", "green", "23 Sep 2026", "29 Sep 2026"));
        disputesList.add(new DisputesPageDto.DisputeItemDto("dp-8", 8, "DP-2026-008", "Trade Dispute", "red", "TR-2026-008", "Sunil Verma", "12,80,000", "Medium", "orange", "Open", "red", "22 Sep 2026", "30 Sep 2026"));
        disputesList.add(new DisputesPageDto.DisputeItemDto("dp-9", 9, "DP-2026-009", "Transfer Dispute", "green", "TF-2026-004", "Kavya Iyer", "18,00,000", "High", "red", "In Progress", "blue", "21 Sep 2026", "29 Sep 2026"));
        disputesList.add(new DisputesPageDto.DisputeItemDto("dp-10", 10, "DP-2026-010", "Payment Dispute", "blue", "PY-2026-006", "Rajesh Mehta", "10,80,000", "Low", "green", "Resolved", "green", "20 Sep 2026", "28 Sep 2026"));

        DisputeDetailDto dp1 = DisputeDetailDto.builder()
                .id("dp-1")
                .disputeId("DP-2026-001")
                .disputeTypeTitle("Trade Dispute")
                .raisedInfo("Raised on 29 Sep 2026, 11:20 AM")
                .status("Open")
                .statusTheme("red")
                .tabs(List.of("Details", "Timeline", "Documents", "Related Transactions"))
                .activeTab("Details")
                .type("Trade Dispute")
                .relatedTradeId("TR-2026-001")
                .investor("Rakesh Kumar")
                .counterparty("Atirath Traders")
                .amount("15,00,000")
                .priority("High")
                .priorityTheme("red")
                .raisedOn("29 Sep 2026, 11:20 AM")
                .targetResolution("06 Oct 2026")
                .assignedTo("Operations Team")
                .description("Counterparty claims shares not received despite successful payment.")
                .stage("Investigation")
                .daysElapsed("1 day")
                .daysRemaining("6 days")
                .relatedTransactions("1 View")
                .relatedSettlements("-")
                .relatedPayments("1 View")
                .build();

        disputeDetailsMap.put("dp-1", dp1);
    }

    private void seedSupportTickets() {
        supportTicketsList.clear();
        supportTicketsList.add(new SupportTicketsPageDto.TicketItemDto("tk-1", 1, "TK-2026-001", "Trade not reflected", "Trade", "blue", "Atirath Traders", "Rakesh Kumar", "High", "red", "Open", "red", "29 Sep 2026", "30 Sep 2026"));
        supportTicketsList.add(new SupportTicketsPageDto.TicketItemDto("tk-2", 2, "TK-2026-002", "KYC verification failed", "Compliance", "purple", "Farm Vantara", "Priya Mehta", "Medium", "orange", "In Progress", "blue", "28 Sep 2026", "30 Sep 2026"));
        supportTicketsList.add(new SupportTicketsPageDto.TicketItemDto("tk-3", 3, "TK-2026-003", "Settlement delay", "Settlement", "teal", "Atirath Holdings", "Vikram Rao", "High", "red", "Open", "red", "27 Sep 2026", "29 Sep 2026"));
        supportTicketsList.add(new SupportTicketsPageDto.TicketItemDto("tk-4", 4, "TK-2026-004", "Transfer status pending", "Transfer", "blue", "Sunrise Ventures", "Neha Sharma", "Low", "green", "Resolved", "green", "26 Sep 2026", "28 Sep 2026"));
        supportTicketsList.add(new SupportTicketsPageDto.TicketItemDto("tk-5", 5, "TK-2026-005", "Escrow release issue", "Escrow", "orange", "Green Future Fund", "Suresh Kumar", "High", "red", "In Progress", "blue", "25 Sep 2026", "28 Sep 2026"));
        supportTicketsList.add(new SupportTicketsPageDto.TicketItemDto("tk-6", 6, "TK-2026-006", "Payment gateway error", "Payment", "red", "Global Agri Fund", "Lakshmi Narayan", "Medium", "orange", "Open", "red", "24 Sep 2026", "27 Sep 2026"));
        supportTicketsList.add(new SupportTicketsPageDto.TicketItemDto("tk-7", 7, "TK-2026-007", "Report generation issue", "Reporting", "blue", "Atirath Technologies", "Ananya Reddy", "Low", "green", "Resolved", "green", "23 Sep 2026", "25 Sep 2026"));
        supportTicketsList.add(new SupportTicketsPageDto.TicketItemDto("tk-8", 8, "TK-2026-008", "Login access issue", "User Management", "purple", "Farm Vantara", "Sunil Verma", "Low", "green", "Closed", "gray", "22 Sep 2026", "23 Sep 2026"));
        supportTicketsList.add(new SupportTicketsPageDto.TicketItemDto("tk-9", 9, "TK-2026-009", "Data mismatch in reconciliation", "Reconciliation", "teal", "Atirath Traders", "Kavya Iyer", "High", "red", "Escalated", "red", "21 Sep 2026", "23 Sep 2026"));
        supportTicketsList.add(new SupportTicketsPageDto.TicketItemDto("tk-10", 10, "TK-2026-010", "Document upload failed", "Document", "gray", "Sunrise Ventures", "Rajesh Mehta", "Low", "green", "Closed", "gray", "20 Sep 2026", "22 Sep 2026"));

        SupportTicketDetailDto tk1 = SupportTicketDetailDto.builder()
                .id("tk-1")
                .ticketId("TK-2026-001")
                .subject("Trade not reflected")
                .raisedInfo("Raised on 29 Sep 2026, 11:20 AM")
                .status("Open")
                .statusTheme("red")
                .tabs(List.of("Details", "Comments", "Attachments", "Activity"))
                .activeTab("Details")
                .category("Trade")
                .company("Atirath Traders")
                .raisedBy("Rakesh Kumar")
                .priority("High")
                .priorityTheme("red")
                .raisedOn("29 Sep 2026, 11:20 AM")
                .expectedResolution("30 Sep 2026")
                .assignedTo("Operations Team")
                .description("Trade executed but not reflected in dashboard. Need immediate support.")
                .slaTarget("1 day")
                .timeElapsed("8 hours")
                .timeRemaining("16 hours")
                .slaStatusTheme("green")
                .relatedTrade("TR-2026-001 View")
                .relatedSettlement("-")
                .relatedTransfer("-")
                .build();

        supportTicketDetailsMap.put("tk-1", tk1);
    }
}
