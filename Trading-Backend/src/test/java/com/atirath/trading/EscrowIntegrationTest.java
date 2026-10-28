package com.atirath.trading;

import com.atirath.trading.modules.escrow.dto.CreateEscrowRequestDto;
import com.atirath.trading.modules.escrow.dto.TransferFundsRequestDto;
import com.atirath.trading.modules.escrow.service.EscrowService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class EscrowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EscrowService escrowService;

    @BeforeEach
    void setUp() {
        escrowService.resetDemoData();
    }

    @Test
    @DisplayName("Image 7: GET /api/v1/escrow/dashboard returns KPI cards, charts & paginated transactions")
    void testGetEscrowDashboard() throws Exception {
        mockMvc.perform(get("/api/v1/escrow/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                // Top KPI Summary Cards
                .andExpect(jsonPath("$.data.summary.totalTransactions").value(24))
                .andExpect(jsonPath("$.data.summary.totalTransactionsChange", containsString("20%")))
                .andExpect(jsonPath("$.data.summary.pendingFunding").value(6))
                .andExpect(jsonPath("$.data.summary.pendingFundingChange", containsString("50%")))
                .andExpect(jsonPath("$.data.summary.fundsInEscrow").value(12450000.00))
                .andExpect(jsonPath("$.data.summary.sharesLocked").value(45000))
                .andExpect(jsonPath("$.data.summary.completedSettlements").value(12))
                .andExpect(jsonPath("$.data.summary.failedCancelled").value(2))
                // Escrow Transaction Status (Donut Chart)
                .andExpect(jsonPath("$.data.statusBreakdown.total").value(24))
                .andExpect(jsonPath("$.data.statusBreakdown.items", hasSize(6)))
                .andExpect(jsonPath("$.data.statusBreakdown.items[0].status").value("Pending Funding"))
                .andExpect(jsonPath("$.data.statusBreakdown.items[0].count").value(6))
                // Settlement Trend (Bar Chart)
                .andExpect(jsonPath("$.data.settlementTrend.months", hasSize(6)))
                .andExpect(jsonPath("$.data.settlementTrend.completed", hasSize(6)))
                .andExpect(jsonPath("$.data.settlementTrend.pending", hasSize(6)))
                // Escrow Value Trend (Line Chart)
                .andExpect(jsonPath("$.data.escrowValueTrend.timeframe").value("This Month"))
                .andExpect(jsonPath("$.data.escrowValueTrend.dataPoints", hasSize(5)))
                // Tab Counts
                .andExpect(jsonPath("$.data.tabCounts.All").value(24))
                .andExpect(jsonPath("$.data.tabCounts['Pending Funding']").value(6))
                .andExpect(jsonPath("$.data.tabCounts['Funds in Escrow']").value(5))
                .andExpect(jsonPath("$.data.tabCounts['Shares Locked']").value(4))
                .andExpect(jsonPath("$.data.tabCounts['Transfer in Progress']").value(3))
                .andExpect(jsonPath("$.data.tabCounts.Completed").value(4))
                .andExpect(jsonPath("$.data.tabCounts.Cancelled").value(2))
                // Transactions Table
                .andExpect(jsonPath("$.data.transactions.totalElements").value(24))
                .andExpect(jsonPath("$.data.transactions.content", hasSize(8)))
                // Row 1 (ESC250918001)
                .andExpect(jsonPath("$.data.transactions.content[0].escrowId").value("ESC250918001"))
                .andExpect(jsonPath("$.data.transactions.content[0].tradeId").value("TRD250918004"))
                .andExpect(jsonPath("$.data.transactions.content[0].companyName").value("Atirath Holdings"))
                .andExpect(jsonPath("$.data.transactions.content[0].buyerName").value("Sairam V. (You)"))
                .andExpect(jsonPath("$.data.transactions.content[0].sellerName").value("Rakesh Mehta"))
                .andExpect(jsonPath("$.data.transactions.content[0].quantity").value(1000))
                .andExpect(jsonPath("$.data.transactions.content[0].pricePerShare").value(242.00))
                .andExpect(jsonPath("$.data.transactions.content[0].tradeValue").value(242000.00))
                .andExpect(jsonPath("$.data.transactions.content[0].currentStatus").value("Pending Funding"))
                .andExpect(jsonPath("$.data.transactions.content[0].createdOn").value("18 Sep 2025"))
                .andExpect(jsonPath("$.data.transactions.content[0].expectedSettlement").value("22 Sep 2025"))
                // Row 2 (ESC250917003)
                .andExpect(jsonPath("$.data.transactions.content[1].escrowId").value("ESC250917003"))
                .andExpect(jsonPath("$.data.transactions.content[1].companyName").value("Farm Vantara"))
                .andExpect(jsonPath("$.data.transactions.content[1].currentStatus").value("Funds in Escrow"));
    }

    @Test
    @DisplayName("Image 7: Dashboard Tab filtering and Search")
    void testDashboardFilteringAndSearch() throws Exception {
        // Tab: Pending Funding
        mockMvc.perform(get("/api/v1/escrow/dashboard").param("tab", "Pending Funding"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.transactions.totalElements").value(6));

        // Tab: Completed
        mockMvc.perform(get("/api/v1/escrow/dashboard").param("tab", "Completed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.transactions.totalElements").value(4));

        // Search: Farm Vantara
        mockMvc.perform(get("/api/v1/escrow/dashboard").param("search", "Farm Vantara"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.transactions.content[0].companyName").value("Farm Vantara"));
    }

    @Test
    @DisplayName("Image 8: GET /api/v1/escrow/ESC250918001 returns full details, parties, bank info & progress")
    void testGetEscrowDetail() throws Exception {
        mockMvc.perform(get("/api/v1/escrow/ESC250918001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.escrowId").value("ESC250918001"))
                .andExpect(jsonPath("$.data.status").value("Pending Funding"))
                .andExpect(jsonPath("$.data.createdOn", containsString("18 Sep 2025")))
                .andExpect(jsonPath("$.data.tradeId").value("TRD250918004"))
                .andExpect(jsonPath("$.data.negotiationId").value("NEG250918001"))
                .andExpect(jsonPath("$.data.agreementId").value("AGR250918001"))
                // Company
                .andExpect(jsonPath("$.data.company.companyName").value("Atirath Holdings India Limited"))
                .andExpect(jsonPath("$.data.company.symbol").value("ATG-H"))
                // Buyer: Sairam Vempati
                .andExpect(jsonPath("$.data.buyer.memberName").value("Sairam Vempati"))
                .andExpect(jsonPath("$.data.buyer.memberId").value("ATG10027"))
                .andExpect(jsonPath("$.data.buyer.email").value("sairam@atirath.com"))
                .andExpect(jsonPath("$.data.buyer.phone").value("+91 98765 43210"))
                .andExpect(jsonPath("$.data.buyer.currentUser").value(true))
                // Seller: Rakesh Mehta
                .andExpect(jsonPath("$.data.seller.memberName").value("Rakesh Mehta"))
                .andExpect(jsonPath("$.data.seller.memberId").value("ATG10056"))
                .andExpect(jsonPath("$.data.seller.email").value("rakesh.mehta@fvt.com"))
                // Trade & Escrow Information
                .andExpect(jsonPath("$.data.tradeAndEscrowInfo.quantity").value(1000))
                .andExpect(jsonPath("$.data.tradeAndEscrowInfo.agreedPricePerShare").value(242.00))
                .andExpect(jsonPath("$.data.tradeAndEscrowInfo.totalTradeValue").value(242000.00))
                .andExpect(jsonPath("$.data.tradeAndEscrowInfo.expectedSettlement", containsString("22 Sep 2025")))
                // Escrow Account Details
                .andExpect(jsonPath("$.data.escrowAccountDetails.bankName").value("HDFC Bank"))
                .andExpect(jsonPath("$.data.escrowAccountDetails.accountName").value("Atirath Escrow Services Pvt Ltd"))
                .andExpect(jsonPath("$.data.escrowAccountDetails.accountNumber").value("5020 9876 5432"))
                .andExpect(jsonPath("$.data.escrowAccountDetails.ifscCode").value("HDFC0001234"))
                .andExpect(jsonPath("$.data.escrowAccountDetails.referenceNo").value("ESC250918001"))
                .andExpect(jsonPath("$.data.escrowAccountDetails.amountToTransfer").value(242000.00))
                // Escrow Progress (5 Steps)
                .andExpect(jsonPath("$.data.escrowProgress", hasSize(5)))
                .andExpect(jsonPath("$.data.escrowProgress[0].title").value("Fund Escrow"))
                .andExpect(jsonPath("$.data.escrowProgress[0].active").value(true))
                .andExpect(jsonPath("$.data.escrowProgress[1].title").value("Funds Verification"))
                // Documents (3 Items)
                .andExpect(jsonPath("$.data.documents", hasSize(3)))
                .andExpect(jsonPath("$.data.documents[0].title", containsString("Trade Agreement")))
                // Recent Activity (3 Items)
                .andExpect(jsonPath("$.data.recentActivity", hasSize(3)))
                .andExpect(jsonPath("$.data.recentActivity[0].title").value("Escrow Created"))
                // Actions
                .andExpect(jsonPath("$.data.actions.canTransferFunds").value(true));
    }

    @Test
    @DisplayName("Image 8: Transfer Funds to Escrow updates status & progress stepper")
    void testTransferFundsToEscrow() throws Exception {
        TransferFundsRequestDto req = TransferFundsRequestDto.builder()
                .amount(new BigDecimal("242000.00"))
                .paymentMode("Bank Transfer")
                .transactionReference("HDFC-TXN-9847123")
                .utrNumber("HDFCR520250918001")
                .remarks("Escrow fund transfer for ATG-H 1,000 shares")
                .build();

        mockMvc.perform(post("/api/v1/escrow/ESC250918001/transfer-funds")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("Funds in Escrow"))
                .andExpect(jsonPath("$.data.escrowProgress[0].completed").value(true))
                .andExpect(jsonPath("$.data.escrowProgress[1].active").value(true))
                .andExpect(jsonPath("$.data.recentActivity[0].title").value("Funds Deposited"));
    }

    @Test
    @DisplayName("Image 7 Action: Create New Escrow Transaction")
    void testCreateEscrow() throws Exception {
        CreateEscrowRequestDto req = CreateEscrowRequestDto.builder()
                .tradeId("TRD250919001")
                .negotiationId("NEG250919001")
                .agreementId("AGR250919001")
                .companySymbol("ATG-H")
                .companyName("Atirath Holdings")
                .buyerMemberId("ATG10027")
                .sellerMemberId("ATG10056")
                .quantity(500L)
                .pricePerShare(new BigDecimal("240.00"))
                .paymentMode("Bank Transfer")
                .expectedSettlement("25 Sep 2025")
                .build();

        mockMvc.perform(post("/api/v1/escrow/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.tradeId").value("TRD250919001"))
                .andExpect(jsonPath("$.data.currentStatus").value("Pending Funding"));
    }

    @Test
    @DisplayName("Image 8 Action: Download Escrow Legal Document")
    void testDownloadDocument() throws Exception {
        mockMvc.perform(get("/api/v1/escrow/ESC250918001/documents/trade-agreement"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("trade-agreement_ESC250918001.pdf")))
                .andExpect(content().string(containsString("ATIRATH GROUP")))
                .andExpect(content().string(containsString("ESC250918001")));
    }

    @Test
    @DisplayName("Image 9: Fund Escrow Stage (Payment method selection & escrow summary)")
    void testFundEscrowStage() throws Exception {
        mockMvc.perform(get("/api/v1/escrow/ESC250918001/pipeline").param("stage", "FUND_ESCROW"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.currentStage").value("FUND_ESCROW"))
                // Stepper: 1 is ACTIVE, others PENDING
                .andExpect(jsonPath("$.data.topStepper[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$.data.topStepper[1].status").value("PENDING"))
                // Summary Card
                .andExpect(jsonPath("$.data.summary.statusBadge").value("Pending Funding"))
                .andExpect(jsonPath("$.data.summary.companySymbol").value("ATG-H"))
                .andExpect(jsonPath("$.data.summary.quantity").value(1000))
                .andExpect(jsonPath("$.data.summary.pricePerShare").value(242.00))
                .andExpect(jsonPath("$.data.summary.totalEscrowAmount").value(242000.00))
                // Payment Methods
                .andExpect(jsonPath("$.data.fundEscrow.paymentMethods", hasSize(3)))
                .andExpect(jsonPath("$.data.fundEscrow.paymentMethods[0].id").value("UPI"))
                .andExpect(jsonPath("$.data.fundEscrow.paymentMethods[0].recommended").value(true))
                .andExpect(jsonPath("$.data.fundEscrow.paymentMethods[1].id").value("NET_BANKING"))
                .andExpect(jsonPath("$.data.fundEscrow.paymentMethods[2].id").value("DEBIT_CARD"));
    }

    @Test
    @DisplayName("Image 10: Payment Verification Stage (Proceed payment to gateway verification)")
    void testPaymentVerificationStage() throws Exception {
        com.atirath.trading.modules.escrow.dto.ProceedPaymentRequestDto req = com.atirath.trading.modules.escrow.dto.ProceedPaymentRequestDto.builder()
                .paymentMethod("UPI")
                .upiApp("Google Pay")
                .build();

        mockMvc.perform(post("/api/v1/escrow/ESC250918001/proceed-payment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.currentStage").value("PAYMENT_VERIFICATION"))
                // Stepper: 1 COMPLETED, 2 ACTIVE
                .andExpect(jsonPath("$.data.topStepper[0].status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.topStepper[1].status").value("ACTIVE"))
                // Payment Details
                .andExpect(jsonPath("$.data.paymentVerification.paymentId").value("PAY250918001"))
                .andExpect(jsonPath("$.data.paymentVerification.paymentMethod").value("UPI (Google Pay)"))
                .andExpect(jsonPath("$.data.paymentVerification.amount").value(242000.00))
                .andExpect(jsonPath("$.data.paymentVerification.gatewayReference").value("GPay/UPI/1837629192"))
                .andExpect(jsonPath("$.data.paymentVerification.status").value("Verification in Progress"))
                .andExpect(jsonPath("$.data.paymentVerification.microProgress", hasSize(4)));
    }

    @Test
    @DisplayName("Image 11: Share Lock Stage (Payment confirmed & seller shareholding lock)")
    void testShareLockStage() throws Exception {
        mockMvc.perform(post("/api/v1/escrow/ESC250918001/verify-payment"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.currentStage").value("SHARE_LOCK"))
                // Stepper: 1 & 2 COMPLETED, 3 ACTIVE
                .andExpect(jsonPath("$.data.topStepper[0].status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.topStepper[1].status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.topStepper[2].status").value("ACTIVE"))
                // Summary Badge: Funds Secured
                .andExpect(jsonPath("$.data.summary.statusBadge").value("Funds Secured"))
                // Seller Demat Info
                .andExpect(jsonPath("$.data.shareLock.sellerShareholding.dematAccount").value("120856300482 (NSDL)"))
                .andExpect(jsonPath("$.data.shareLock.sellerShareholding.availableShares").value(5250))
                .andExpect(jsonPath("$.data.shareLock.sellerShareholding.sharesToLock").value(1000))
                .andExpect(jsonPath("$.data.shareLock.sellerShareholding.postLockBalance").value(4250))
                .andExpect(jsonPath("$.data.shareLock.microProgress", hasSize(4)));
    }

    @Test
    @DisplayName("Image 12: Share Transfer Stage (Shares locked, depository transfer to buyer demat)")
    void testShareTransferStage() throws Exception {
        mockMvc.perform(post("/api/v1/escrow/ESC250918001/lock-shares"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.currentStage").value("SHARE_TRANSFER"))
                // Stepper: 1, 2, 3 COMPLETED, 4 ACTIVE
                .andExpect(jsonPath("$.data.topStepper[2].status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.topStepper[3].status").value("ACTIVE"))
                // Transfer Details
                .andExpect(jsonPath("$.data.shareTransfer.lockReference").value("LOCK250918001"))
                .andExpect(jsonPath("$.data.shareTransfer.fromSeller.memberName", containsString("Rakesh Mehta")))
                .andExpect(jsonPath("$.data.shareTransfer.toBuyer.memberName", containsString("Sairam Vempati")))
                .andExpect(jsonPath("$.data.shareTransfer.transferDetails.transferId").value("TRANS250918001"))
                .andExpect(jsonPath("$.data.shareTransfer.transferDetails.quantity").value(1000))
                .andExpect(jsonPath("$.data.shareTransfer.transferDetails.transferType").value("Off-Market (Internal Trade)"))
                .andExpect(jsonPath("$.data.shareTransfer.transferDetails.status").value("In Progress"));
    }

    @Test
    @DisplayName("Image 13: Settlement Completed Stage (All 5 steps completed & seller payout)")
    void testSettlementCompletedStage() throws Exception {
        mockMvc.perform(post("/api/v1/escrow/ESC250918001/transfer-shares"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.currentStage").value("COMPLETED"))
                // Stepper: All 5 steps COMPLETED
                .andExpect(jsonPath("$.data.topStepper[0].status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.topStepper[1].status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.topStepper[2].status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.topStepper[3].status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.topStepper[4].status").value("COMPLETED"))
                // Summary: Completed
                .andExpect(jsonPath("$.data.summary.statusBadge").value("Completed"))
                // 4 Metric cards
                .andExpect(jsonPath("$.data.settlementCompleted.sharesTraded").value(1000))
                .andExpect(jsonPath("$.data.settlementCompleted.totalTradeValue").value(242000.00))
                .andExpect(jsonPath("$.data.settlementCompleted.completedOn", containsString("18 Sep 2025")))
                .andExpect(jsonPath("$.data.settlementCompleted.settlementId").value("SET250918001"))
                // Settlement Payout Details
                .andExpect(jsonPath("$.data.settlementCompleted.payoutAmount").value(242000.00))
                .andExpect(jsonPath("$.data.settlementCompleted.payoutBankAccount", containsString("HDFC Bank")))
                .andExpect(jsonPath("$.data.settlementCompleted.referenceNo").value("PAYOUT1837291045"))
                .andExpect(jsonPath("$.data.settlementCompleted.status").value("Completed"));
    }

    @Test
    @DisplayName("Image 14: Settlement In Progress Stage (Estimated 10-30m, 5-phase timeline, 5-step checklist & payout details)")
    void testSettlementInProgressStage() throws Exception {
        mockMvc.perform(get("/api/v1/escrow/ESC250918001/pipeline").param("stage", "SETTLEMENT_IN_PROGRESS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.currentStage").value("SETTLEMENT_IN_PROGRESS"))
                .andExpect(jsonPath("$.data.summary.statusBadge").value("Settlement in Progress"))
                .andExpect(jsonPath("$.data.topStepper[0].status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.topStepper[1].status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.topStepper[2].status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.topStepper[3].status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.topStepper[4].status").value("ACTIVE"))
                .andExpect(jsonPath("$.data.topStepper[4].subtext").value("Funds release to seller"))
                .andExpect(jsonPath("$.data.settlementInProgress.estimatedSettlementTime").value("10-30 minutes"))
                .andExpect(jsonPath("$.data.settlementInProgress.overallProgress", hasSize(5)))
                .andExpect(jsonPath("$.data.settlementInProgress.overallProgress[0].title").value("Payment Completed"))
                .andExpect(jsonPath("$.data.settlementInProgress.overallProgress[4].title").value("Settlement"))
                .andExpect(jsonPath("$.data.settlementInProgress.overallProgress[4].status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.data.settlementInProgress.processingChecklist", hasSize(5)))
                .andExpect(jsonPath("$.data.settlementInProgress.processingChecklist[0].title", containsString("Verifying share transfer")))
                .andExpect(jsonPath("$.data.settlementInProgress.processingChecklist[2].status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.data.settlementInProgress.pleaseNote", hasSize(4)))
                .andExpect(jsonPath("$.data.settlementInProgress.settlementDetails.settlementId").value("SET250918001"))
                .andExpect(jsonPath("$.data.settlementInProgress.settlementDetails.expectedPayoutAmount").value(242000.00))
                .andExpect(jsonPath("$.data.settlementInProgress.settlementDetails.payoutBankAccount", containsString("HDFC Bank")))
                .andExpect(jsonPath("$.data.settlementInProgress.settlementDetails.status").value("Processing"))
                .andExpect(jsonPath("$.data.settlementInProgress.settlementDetails.estimatedCompletion", containsString("10-30 minutes")));
    }

    @Test
    @DisplayName("Image 15: GET /api/v1/escrow/status returns 6 KPI cards, 4 tabs, 12 rows & selected escrow details")
    void testGetEscrowStatusPage() throws Exception {
        mockMvc.perform(get("/api/v1/escrow/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                // Summary KPI Cards (Image 15)
                .andExpect(jsonPath("$.data.summary.totalEscrows").value(12))
                .andExpect(jsonPath("$.data.summary.completed").value(5))
                .andExpect(jsonPath("$.data.summary.completedPct").value(41.7))
                .andExpect(jsonPath("$.data.summary.inProgress").value(3))
                .andExpect(jsonPath("$.data.summary.inProgressPct").value(25.0))
                .andExpect(jsonPath("$.data.summary.pending").value(2))
                .andExpect(jsonPath("$.data.summary.pendingPct").value(16.7))
                .andExpect(jsonPath("$.data.summary.failed").value(1))
                .andExpect(jsonPath("$.data.summary.failedPct").value(8.3))
                .andExpect(jsonPath("$.data.summary.cancelled").value(1))
                .andExpect(jsonPath("$.data.summary.cancelledPct").value(8.3))
                // Tab Counts
                .andExpect(jsonPath("$.data.tabCounts['All Escrows']").value(12))
                .andExpect(jsonPath("$.data.tabCounts['My Escrows']").value(8))
                .andExpect(jsonPath("$.data.tabCounts['As a Buyer']").value(7))
                .andExpect(jsonPath("$.data.tabCounts['As a Seller']").value(5))
                // Table
                .andExpect(jsonPath("$.data.escrows.totalElements").value(12))
                .andExpect(jsonPath("$.data.escrows.content", hasSize(8)))
                .andExpect(jsonPath("$.data.escrows.content[0].escrowId").value("ESC250918001"))
                .andExpect(jsonPath("$.data.escrows.content[0].tradeId").value("TRD250918004"))
                .andExpect(jsonPath("$.data.escrows.content[0].companyName").value("Atirath Holdings India Limited"))
                .andExpect(jsonPath("$.data.escrows.content[0].type").value("Buy"))
                .andExpect(jsonPath("$.data.escrows.content[0].quantity").value(1000))
                .andExpect(jsonPath("$.data.escrows.content[0].amount").value(242000.00))
                .andExpect(jsonPath("$.data.escrows.content[0].status").value("Completed"))
                // Selected Escrow Details (Right Panel)
                .andExpect(jsonPath("$.data.selectedEscrow.escrowId").value("ESC250918001"))
                .andExpect(jsonPath("$.data.selectedEscrow.tradeId").value("TRD250918004"))
                .andExpect(jsonPath("$.data.selectedEscrow.pricePerShare").value(242.00))
                .andExpect(jsonPath("$.data.selectedEscrow.progress", hasSize(6)))
                .andExpect(jsonPath("$.data.selectedEscrow.progress[0].title").value("Escrow Created"))
                .andExpect(jsonPath("$.data.selectedEscrow.progress[5].title").value("Settlement Completed"));
    }

    @Test
    @DisplayName("Image 16: GET /api/v1/escrow/status/{escrowId} returns View Escrow full details, horizontal tracker & 6 tabs")
    void testGetEscrowStatusDetail() throws Exception {
        mockMvc.perform(get("/api/v1/escrow/status/ESC250918001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.escrowId").value("ESC250918001"))
                .andExpect(jsonPath("$.data.tradeId").value("TRD250918004"))
                .andExpect(jsonPath("$.data.companyName").value("Atirath Holdings India Limited"))
                .andExpect(jsonPath("$.data.type").value("Buy"))
                .andExpect(jsonPath("$.data.quantity").value(1000))
                .andExpect(jsonPath("$.data.pricePerShare").value(242.00))
                .andExpect(jsonPath("$.data.totalAmount").value(242000.00))
                .andExpect(jsonPath("$.data.buyerName").value("Sairam Vempati"))
                .andExpect(jsonPath("$.data.sellerName").value("Rakesh Mehta"))
                .andExpect(jsonPath("$.data.status").value("Completed"))
                .andExpect(jsonPath("$.data.statusMessage", containsString("All escrow steps completed")))
                // Horizontal 5-Step Tracker
                .andExpect(jsonPath("$.data.horizontalSteps", hasSize(5)))
                .andExpect(jsonPath("$.data.horizontalSteps[0].title", containsString("Fund Escrow")))
                .andExpect(jsonPath("$.data.horizontalSteps[4].title", containsString("Settlement")))
                // Escrow Summary Card
                .andExpect(jsonPath("$.data.escrowSummary.escrowId").value("ESC250918001"))
                .andExpect(jsonPath("$.data.escrowSummary.totalEscrowAmount").value(242000.00))
                // Progress Steps (5 items)
                .andExpect(jsonPath("$.data.progressSteps", hasSize(5)))
                .andExpect(jsonPath("$.data.progressSteps[0].completed").value(true))
                // Quick Actions (4 actions)
                .andExpect(jsonPath("$.data.quickActions", hasSize(4)))
                .andExpect(jsonPath("$.data.quickActions[0].id").value("download_report"))
                // Transaction History Tab
                .andExpect(jsonPath("$.data.transactionHistory.events", hasSize(6)))
                // Payment Details Tab
                .andExpect(jsonPath("$.data.paymentDetails.paymentId").value("PAY250918001"))
                .andExpect(jsonPath("$.data.paymentDetails.paymentMethod", containsString("UPI")))
                // Share Details Tab
                .andExpect(jsonPath("$.data.shareDetails.lockId").value("LOCK250918001"))
                .andExpect(jsonPath("$.data.shareDetails.transferId").value("TRF250918001"))
                // Settlement Details Tab
                .andExpect(jsonPath("$.data.settlementDetails.settlementId").value("SET250918001"))
                // Documents Tab
                .andExpect(jsonPath("$.data.documents", hasSize(4)));
    }

    @Test
    @DisplayName("Image 17: GET /api/v1/escrow/share-locks returns 5 KPI cards, 4 tabs, 8 locks & selected lock progress")
    void testGetShareLockStatusPage() throws Exception {
        mockMvc.perform(get("/api/v1/escrow/share-locks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                // Summary KPI Cards (Image 17)
                .andExpect(jsonPath("$.data.summary.totalRequests").value(8))
                .andExpect(jsonPath("$.data.summary.activeLocks").value(5))
                .andExpect(jsonPath("$.data.summary.activeLocksPct").value(62.5))
                .andExpect(jsonPath("$.data.summary.released").value(2))
                .andExpect(jsonPath("$.data.summary.releasedPct").value(25.0))
                .andExpect(jsonPath("$.data.summary.expiringSoon").value(1))
                .andExpect(jsonPath("$.data.summary.expiringSoonPct").value(12.5))
                .andExpect(jsonPath("$.data.summary.failed").value(0))
                // Tab Counts
                .andExpect(jsonPath("$.data.tabCounts['All Locks']").value(8))
                .andExpect(jsonPath("$.data.tabCounts['My Locks']").value(5))
                .andExpect(jsonPath("$.data.tabCounts['As a Buyer']").value(4))
                .andExpect(jsonPath("$.data.tabCounts['As a Seller']").value(4))
                // Locks Table
                .andExpect(jsonPath("$.data.locks.totalElements").value(8))
                .andExpect(jsonPath("$.data.locks.content", hasSize(8)))
                .andExpect(jsonPath("$.data.locks.content[0].lockId").value("LOCK250918001"))
                .andExpect(jsonPath("$.data.locks.content[0].tradeId").value("TRD250918004"))
                .andExpect(jsonPath("$.data.locks.content[0].companyName").value("Atirath Holdings India Limited"))
                .andExpect(jsonPath("$.data.locks.content[0].isin").value("INE012A01025"))
                .andExpect(jsonPath("$.data.locks.content[0].quantity").value(1000))
                .andExpect(jsonPath("$.data.locks.content[0].status").value("Locked"))
                // Selected Lock Details (Right Panel)
                .andExpect(jsonPath("$.data.selectedLock.lockId").value("LOCK250918001"))
                .andExpect(jsonPath("$.data.selectedLock.unlockCondition").value("On Share Transfer"))
                .andExpect(jsonPath("$.data.selectedLock.lockProgress", hasSize(4)))
                .andExpect(jsonPath("$.data.selectedLock.recentActivity", hasSize(3)))
                .andExpect(jsonPath("$.data.selectedLock.documents", hasSize(2)));
    }

    @Test
    @DisplayName("Image 18: GET /api/v1/escrow/share-transfers returns 5 KPI cards, 4 tabs, 10 transfers & progress timeline")
    void testGetShareTransferStatusPage() throws Exception {
        mockMvc.perform(get("/api/v1/escrow/share-transfers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                // Summary KPI Cards (Image 18)
                .andExpect(jsonPath("$.data.summary.totalTransfers").value(10))
                .andExpect(jsonPath("$.data.summary.completed").value(6))
                .andExpect(jsonPath("$.data.summary.completedPct").value(60.0))
                .andExpect(jsonPath("$.data.summary.inProgress").value(3))
                .andExpect(jsonPath("$.data.summary.inProgressPct").value(30.0))
                .andExpect(jsonPath("$.data.summary.pending").value(1))
                .andExpect(jsonPath("$.data.summary.pendingPct").value(10.0))
                .andExpect(jsonPath("$.data.summary.failed").value(0))
                // Tab Counts
                .andExpect(jsonPath("$.data.tabCounts['All Transfers']").value(10))
                .andExpect(jsonPath("$.data.tabCounts['My Transfers']").value(7))
                .andExpect(jsonPath("$.data.tabCounts['As a Buyer']").value(6))
                .andExpect(jsonPath("$.data.tabCounts['As a Seller']").value(4))
                // Transfers Table
                .andExpect(jsonPath("$.data.transfers.totalElements").value(10))
                .andExpect(jsonPath("$.data.transfers.content", hasSize(8)))
                .andExpect(jsonPath("$.data.transfers.content[0].transferId").value("TRF250918001"))
                .andExpect(jsonPath("$.data.transfers.content[0].tradeId").value("TRD250918004"))
                .andExpect(jsonPath("$.data.transfers.content[0].companyName").value("Atirath Holdings India Limited"))
                .andExpect(jsonPath("$.data.transfers.content[0].fromSeller", containsString("Rakesh Mehta")))
                .andExpect(jsonPath("$.data.transfers.content[0].toBuyer", containsString("Sairam Vempati")))
                .andExpect(jsonPath("$.data.transfers.content[0].quantity").value(1000))
                .andExpect(jsonPath("$.data.transfers.content[0].status").value("Completed"))
                // Selected Transfer Details (Right Panel)
                .andExpect(jsonPath("$.data.selectedTransfer.transferId").value("TRF250918001"))
                .andExpect(jsonPath("$.data.selectedTransfer.settlementRef").value("SET250918001"))
                .andExpect(jsonPath("$.data.selectedTransfer.transferProgress", hasSize(4)))
                .andExpect(jsonPath("$.data.selectedTransfer.transferProgress[0].completed").value(true))
                .andExpect(jsonPath("$.data.selectedTransfer.documents", hasSize(3)));
    }

    @Test
    @DisplayName("Settlement Confirmation Screen: Stepper, Details, Buyer/Seller Metrics, 4 Documents")
    void testSettlementConfirmation() throws Exception {
        mockMvc.perform(get("/api/v1/escrow/settlement/confirmation")
                        .param("settlementId", "SET250918001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.settlementId").value("SET250918001"))
                .andExpect(jsonPath("$.data.escrowId").value("ESC250918001"))
                .andExpect(jsonPath("$.data.tradeId").value("TRD250918004"))
                // 5 Stepper Steps all completed
                .andExpect(jsonPath("$.data.progressSteps", hasSize(5)))
                .andExpect(jsonPath("$.data.progressSteps[0].title", containsString("Fund Escrow")))
                .andExpect(jsonPath("$.data.progressSteps[0].status").value("Completed"))
                .andExpect(jsonPath("$.data.progressSteps[4].title", containsString("Settlement")))
                .andExpect(jsonPath("$.data.progressSteps[4].status").value("Completed"))
                // Settlement Details (Left Card)
                .andExpect(jsonPath("$.data.settlementDetails.settlementId").value("SET250918001"))
                .andExpect(jsonPath("$.data.settlementDetails.escrowId").value("ESC250918001"))
                .andExpect(jsonPath("$.data.settlementDetails.companyName").value("Atirath Holdings India Limited"))
                .andExpect(jsonPath("$.data.settlementDetails.fromSeller", containsString("Rakesh Mehta")))
                .andExpect(jsonPath("$.data.settlementDetails.toBuyer", containsString("Sairam Vempati")))
                .andExpect(jsonPath("$.data.settlementDetails.isin").value("INE012A01025"))
                .andExpect(jsonPath("$.data.settlementDetails.quantity").value(1000))
                .andExpect(jsonPath("$.data.settlementDetails.pricePerShare").value(242.00))
                .andExpect(jsonPath("$.data.settlementDetails.totalAmount").value(242000.00))
                .andExpect(jsonPath("$.data.settlementDetails.settlementStatus").value("Completed"))
                .andExpect(jsonPath("$.data.settlementDetails.settlementReferenceNo").value("NSDL250918123456"))
                .andExpect(jsonPath("$.data.settlementDetails.clearingCorporation").value("NSDL"))
                .andExpect(jsonPath("$.data.settlementDetails.paymentReferenceNo").value("PAY250918789012"))
                // Celebration Success Card (Right Card)
                .andExpect(jsonPath("$.data.successDetails.headline").value("Settlement Successful!"))
                .andExpect(jsonPath("$.data.successDetails.tagline", containsString("Trade Completed. Trust Delivered.")))
                .andExpect(jsonPath("$.data.successDetails.buyerDelivery.sharesCount").value(1000))
                .andExpect(jsonPath("$.data.successDetails.buyerDelivery.sharesFormatted").value("1,000 Shares"))
                .andExpect(jsonPath("$.data.successDetails.sellerPayment.amount").value(242000.00))
                .andExpect(jsonPath("$.data.successDetails.sellerPayment.amountFormatted", containsString("2,42,000.00")))
                // Documents (4 items)
                .andExpect(jsonPath("$.data.documents", hasSize(4)))
                .andExpect(jsonPath("$.data.documents[0].title").value("Settlement Confirmation Letter"))
                .andExpect(jsonPath("$.data.documents[1].title").value("Share Transfer Proof (CDSL/NSDL)"))
                .andExpect(jsonPath("$.data.documents[2].title").value("Payment Release Confirmation"))
                .andExpect(jsonPath("$.data.documents[3].title").value("Escrow Closure Report"));
    }

    @Test
    @DisplayName("Download Settlement Confirmation Document: Returns PDF bytes")
    void testDownloadSettlementDocument() throws Exception {
        mockMvc.perform(get("/api/v1/escrow/settlement/SET250918001/documents/settlement-letter"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("settlement-letter_SET250918001.pdf")))
                .andExpect(content().string(containsString("OFFICIAL SETTLEMENT DOCUMENT")));
    }
}


