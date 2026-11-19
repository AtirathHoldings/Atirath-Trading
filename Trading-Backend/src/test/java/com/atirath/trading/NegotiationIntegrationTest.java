package com.atirath.trading;

import com.atirath.trading.modules.negotiation.dto.CounterOfferRequestDto;
import com.atirath.trading.modules.negotiation.dto.NegotiationMessageRequestDto;
import com.atirath.trading.modules.negotiation.dto.TradeAgreementConfirmRequestDto;
import com.fasterxml.jackson.databind.ObjectMapper;
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
@org.springframework.security.test.context.support.WithMockUser(username = "sairam.v@atirathgroup.com")
class NegotiationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private com.atirath.trading.modules.negotiation.service.NegotiationService negotiationService;

    @Autowired
    private com.atirath.trading.modules.negotiation.repository.NegotiationRepository negotiationRepository;

    @Autowired
    private com.atirath.trading.modules.negotiation.repository.NegotiationOfferRepository negotiationOfferRepository;

    @Autowired
    private com.atirath.trading.modules.negotiation.repository.NegotiationMessageRepository negotiationMessageRepository;

    @Autowired
    private com.atirath.trading.modules.negotiation.repository.TradeAgreementRepository tradeAgreementRepository;

    @Autowired
    private com.atirath.trading.modules.auth.repository.UserAccountRepository userAccountRepository;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        negotiationService.resetDemoData();
        try {
            userAccountRepository.findByEmailIgnoreCase("sairam.v@atirathgroup.com").ifPresent(sairam -> {
                java.util.List<com.atirath.trading.modules.negotiation.model.Negotiation> seed = negotiationRepository.findAll().stream()
                        .filter(n -> n.getNegotiationNo().startsWith("NEG2509"))
                        .toList();
                for (com.atirath.trading.modules.negotiation.model.Negotiation n : seed) {
                    if (n.getInitiatorUser() == null || !sairam.getUserId().equals(n.getInitiatorUser().getUserId())) {
                        n.setInitiatorUser(sairam);
                        negotiationRepository.save(n);
                    }
                }
            });
            negotiationRepository.findByNegotiationNo("NEG250918001").ifPresent(n -> {
                n.setStatus("AWAITING_RESPONSE");
                n.setProposedPrice(new BigDecimal("242.00"));
                negotiationRepository.save(n);
            });
            java.util.List<com.atirath.trading.modules.negotiation.model.Negotiation> nonSeed = negotiationRepository.findAll().stream()
                    .filter(n -> !n.getNegotiationNo().startsWith("NEG2509"))
                    .toList();
            for (com.atirath.trading.modules.negotiation.model.Negotiation n : nonSeed) {
                try {
                    negotiationOfferRepository.deleteAll(negotiationOfferRepository.findByNegotiation_NegotiationNoOrderByCreatedAtAsc(n.getNegotiationNo()));
                } catch (Exception ignored) {}
                try {
                    negotiationMessageRepository.deleteAll(negotiationMessageRepository.findByNegotiation_NegotiationNoOrderBySentAtAsc(n.getNegotiationNo()));
                } catch (Exception ignored) {}
                try {
                    tradeAgreementRepository.findByNegotiation_NegotiationNo(n.getNegotiationNo()).ifPresent(tradeAgreementRepository::delete);
                } catch (Exception ignored) {}
                try {
                    negotiationRepository.delete(n);
                } catch (Exception ignored) {}
            }
        } catch (Exception ignored) {}
    }

    @Test
    @DisplayName("Image 1: GET /api/v1/negotiations/dashboard returns complete metrics, insights & list")
    void testGetNegotiationDashboard() throws Exception {
        mockMvc.perform(get("/api/v1/negotiations/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                // KPI Summary Cards
                .andExpect(jsonPath("$.data.summary.allNegotiations", greaterThanOrEqualTo(10)))
                .andExpect(jsonPath("$.data.summary.active", greaterThanOrEqualTo(2)))
                .andExpect(jsonPath("$.data.summary.awaitingResponse", greaterThanOrEqualTo(2)))
                .andExpect(jsonPath("$.data.summary.accepted", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.data.summary.rejected", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.data.summary.expired", greaterThanOrEqualTo(1)))
                // Negotiation Insights (Bar Chart)
                .andExpect(jsonPath("$.data.insights.title").value("Negotiation Insights"))
                .andExpect(jsonPath("$.data.insights.months", hasSize(6)))
                .andExpect(jsonPath("$.data.insights.series", hasSize(2)))
                .andExpect(jsonPath("$.data.insights.series[0].name").value("Initiated by You"))
                .andExpect(jsonPath("$.data.insights.series[1].name").value("Received from Others"))
                // Negotiation Status (Donut Chart)
                .andExpect(jsonPath("$.data.statusBreakdown.total", greaterThanOrEqualTo(12)))
                .andExpect(jsonPath("$.data.statusBreakdown.items", hasSize(5)))
                // Quick Actions
                .andExpect(jsonPath("$.data.quickActions", hasSize(4)))
                // Negotiations Table Content
                .andExpect(jsonPath("$.data.negotiations.totalElements", greaterThanOrEqualTo(12)))
                .andExpect(jsonPath("$.data.negotiations.content[0].negotiationNo").value("NEG250918001"))
                .andExpect(jsonPath("$.data.negotiations.content[0].companyName", containsString("Atirath Holdings")))
                .andExpect(jsonPath("$.data.negotiations.content[0].companySymbol").value("ATG-H"))
                .andExpect(jsonPath("$.data.negotiations.content[0].type").value("Buy"))
                .andExpect(jsonPath("$.data.negotiations.content[0].counterpartyName").value("Rakesh Mehta"))
                .andExpect(jsonPath("$.data.negotiations.content[0].counterpartyMemberId").value("ATG10056"))
                .andExpect(jsonPath("$.data.negotiations.content[0].shares").value(1000))
                .andExpect(jsonPath("$.data.negotiations.content[0].status").value("Awaiting Response"));
    }

    @Test
    @DisplayName("Image 2: GET /api/v1/negotiations/NEG250918001 returns full details, buyer/seller & terms")
    void testGetNegotiationDetail() throws Exception {
        mockMvc.perform(get("/api/v1/negotiations/NEG250918001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.negotiationNo").value("NEG250918001"))
                // Buyer info (Sairam Vempati)
                .andExpect(jsonPath("$.data.buyer.companyName", containsString("Atirath Holdings")))
                .andExpect(jsonPath("$.data.buyer.memberId").value("ATG10027"))
                .andExpect(jsonPath("$.data.buyer.memberName", startsWith("Sairam")))
                .andExpect(jsonPath("$.data.buyer.currentHoldings").value(12500))
                .andExpect(jsonPath("$.data.buyer.quantity").value(1000))
                // Seller info (Rakesh Mehta)
                .andExpect(jsonPath("$.data.seller.companyName", containsString("Farm Vantara")))
                .andExpect(jsonPath("$.data.seller.memberId").value("ATG10056"))
                .andExpect(jsonPath("$.data.seller.memberName").value("Rakesh Mehta"))
                .andExpect(jsonPath("$.data.seller.currentHoldings").value(5000))
                .andExpect(jsonPath("$.data.seller.quantity").value(1000))
                // Company & Share details
                .andExpect(jsonPath("$.data.companyShareDetails.symbol").value("ATG-H"))
                .andExpect(jsonPath("$.data.companyShareDetails.faceValue").value(10.0))
                .andExpect(jsonPath("$.data.companyShareDetails.lastTradedPrice", greaterThan(200.0)))
                // Negotiation Terms comparison table
                .andExpect(jsonPath("$.data.negotiationTerms", hasSize(6)))
                .andExpect(jsonPath("$.data.negotiationTerms[0].parameter").value("Price per Share"))
                .andExpect(jsonPath("$.data.negotiationTerms[0].buyerOffer", containsString("235.00")))
                .andExpect(jsonPath("$.data.negotiationTerms[0].sellerOffer", containsString("250.00")))
                .andExpect(jsonPath("$.data.negotiationTerms[0].currentProposal", containsString("242.00")))
                // Timeline events
                .andExpect(jsonPath("$.data.timeline", hasSize(greaterThanOrEqualTo(4))))
                .andExpect(jsonPath("$.data.timeline[0].senderName").value("Rakesh Mehta"))
                .andExpect(jsonPath("$.data.availableActions.canAccept").value(true))
                .andExpect(jsonPath("$.data.availableActions.canCounter").value(true));
    }

    @Test
    @DisplayName("Image 3: POST /api/v1/negotiations/NEG250918001/counter-offer updates proposal terms")
    void testSubmitCounterOffer() throws Exception {
        CounterOfferRequestDto req = CounterOfferRequestDto.builder()
                .pricePerShare(new BigDecimal("243.50"))
                .quantity(1000L)
                .settlementTimeline("T+3 Days")
                .paymentMode("Bank Transfer (Escrow)")
                .otherTerms("Counter offer test from Sairam")
                .build();

        mockMvc.perform(post("/api/v1/negotiations/NEG250918001/counter-offer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.negotiationTerms[0].currentProposal", containsString("243.50")));
    }

    @Test
    @DisplayName("Image 4: GET /api/v1/negotiations/NEG250918001/history returns full timeline and chat")
    void testGetNegotiationHistory() throws Exception {
        mockMvc.perform(get("/api/v1/negotiations/NEG250918001/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.negotiationNo").value("NEG250918001"))
                .andExpect(jsonPath("$.data.timeline", hasSize(greaterThanOrEqualTo(6))))
                .andExpect(jsonPath("$.data.timeline[0].senderCode").value("RM"))
                .andExpect(jsonPath("$.data.timeline[1].senderCode").value("SV"))
                .andExpect(jsonPath("$.data.latestTermsComparison.yourOfferPrice").value(242.0))
                .andExpect(jsonPath("$.data.latestTermsComparison.sellerOfferPrice").value(248.0));
    }

    @Test
    @DisplayName("Image 4 (Chat): POST /api/v1/negotiations/NEG250918001/messages adds communication note")
    void testPostNegotiationMessage() throws Exception {
        NegotiationMessageRequestDto req = NegotiationMessageRequestDto.builder()
                .messageText("Can we close at ₹ 242.00 today?")
                .build();

        mockMvc.perform(post("/api/v1/negotiations/NEG250918001/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.timeline[?(@.message == 'Can we close at ₹ 242.00 today?')]").exists());
    }

    @Test
    @DisplayName("Image 5: GET & Confirm Trade Agreement for NEG250918001")
    void testTradeAgreementLifecycle() throws Exception {
        // 1. Get Agreement
        mockMvc.perform(get("/api/v1/negotiations/NEG250918001/trade-agreement"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.finalAgreedTerms.sharePrice").value(242.00))
                .andExpect(jsonPath("$.data.finalAgreedTerms.quantity").value(1000))
                .andExpect(jsonPath("$.data.finalAgreedTerms.totalTradeValue").value(242000.00))
                .andExpect(jsonPath("$.data.document.documentName").value("Trade_Agreement_NEG250918001.pdf"))
                .andExpect(jsonPath("$.data.confirmations", hasSize(3)))
                .andExpect(jsonPath("$.data.nextSteps", hasSize(5)));

        // 2. Confirm Agreement
        TradeAgreementConfirmRequestDto confirmReq = TradeAgreementConfirmRequestDto.builder()
                .readAndUnderstood(true)
                .informationAccurate(true)
                .agreeToEscrowAndSettlement(true)
                .build();

        mockMvc.perform(post("/api/v1/negotiations/NEG250918001/trade-agreement/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(confirmReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("Agreement Confirmed - Escrow Initiated"))
                .andExpect(jsonPath("$.data.nextSteps[0].completed").value(true));

        // 3. Download Agreement PDF / Document
        mockMvc.perform(get("/api/v1/negotiations/NEG250918001/trade-agreement/download"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("Trade_Agreement_NEG250918001.pdf")))
                .andExpect(content().string(containsString("ATIRATH GROUP")));
    }

    @Test
    @DisplayName("Image 6: GET /api/v1/negotiations/notifications returns KPI cards, tab counts & selected summary")
    void testGetNegotiationNotifications() throws Exception {
        mockMvc.perform(get("/api/v1/negotiations/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                // KPI Metric Cards
                .andExpect(jsonPath("$.data.summary.allNotifications").value(12))
                .andExpect(jsonPath("$.data.summary.newOffers").value(3))
                .andExpect(jsonPath("$.data.summary.counterOffers").value(4))
                .andExpect(jsonPath("$.data.summary.agreements").value(2))
                .andExpect(jsonPath("$.data.summary.actionRequired").value(2))
                .andExpect(jsonPath("$.data.summary.systemAlerts").value(1))
                // Unread Count
                .andExpect(jsonPath("$.data.unreadCount").value(3))
                // Tab Counts
                .andExpect(jsonPath("$.data.tabCounts.All").value(12))
                .andExpect(jsonPath("$.data.tabCounts.Unread").value(3))
                .andExpect(jsonPath("$.data.tabCounts.Offers").value(4))
                .andExpect(jsonPath("$.data.tabCounts['Counter Offers']").value(3))
                .andExpect(jsonPath("$.data.tabCounts.Agreements").value(2))
                .andExpect(jsonPath("$.data.tabCounts.Reminders").value(2))
                .andExpect(jsonPath("$.data.tabCounts.System").value(1))
                // List of notifications
                .andExpect(jsonPath("$.data.notifications", hasSize(12)))
                .andExpect(jsonPath("$.data.notifications[0].id").value(1))
                .andExpect(jsonPath("$.data.notifications[0].title").value("New Counter Offer Received"))
                .andExpect(jsonPath("$.data.notifications[0].timeAgo").value("10 minutes ago"))
                .andExpect(jsonPath("$.data.notifications[0].unread").value(true))
                .andExpect(jsonPath("$.data.notifications[0].message", containsString("Rakesh Mehta has sent a counter offer")))
                // Selected item right-side summary card
                .andExpect(jsonPath("$.data.selectedNotification.negotiationSummary.negotiationId").value("NEG250918001"))
                .andExpect(jsonPath("$.data.selectedNotification.negotiationSummary.companySymbol").value("ATG-H"))
                .andExpect(jsonPath("$.data.selectedNotification.negotiationSummary.counterparty", containsString("Rakesh Mehta")))
                .andExpect(jsonPath("$.data.selectedNotification.negotiationSummary.status").value("Awaiting Your Response"))
                .andExpect(jsonPath("$.data.selectedNotification.negotiationSummary.canAccept").value(true))
                .andExpect(jsonPath("$.data.selectedNotification.negotiationSummary.canCounter").value(true));
    }

    @Test
    @DisplayName("Image 6: Notification filter, search, mark read/unread and mark all as read")
    void testNotificationActionsAndFilters() throws Exception {
        // 1. Filter by category UNREAD
        mockMvc.perform(get("/api/v1/negotiations/notifications").param("category", "UNREAD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.notifications", hasSize(3)));

        // 2. Search query filter
        mockMvc.perform(get("/api/v1/negotiations/notifications").param("search", "Priya"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.notifications", hasSize(1)))
                .andExpect(jsonPath("$.data.notifications[0].title").value("Negotiation Agreement Reached"));

        // 3. Mark notification 1 as read
        mockMvc.perform(post("/api/v1/negotiations/notifications/1/mark-read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.unread").value(false));

        // 4. Mark notification 1 as unread
        mockMvc.perform(post("/api/v1/negotiations/notifications/1/mark-unread"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.unread").value(true));

        // 5. Mark all as read
        mockMvc.perform(post("/api/v1/negotiations/notifications/mark-all-read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/v1/negotiations/notifications").param("category", "UNREAD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.notifications", hasSize(0)));
    }

    @Test
    @DisplayName("Request call, Summary/History PDF downloads, and Initiate negotiation work end-to-end")
    void testRequestCallSummaryHistoryDownloadsAndInitiate() throws Exception {
        mockMvc.perform(post("/api/v1/negotiations/NEG250918001/request-call"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/v1/negotiations/NEG250918001/summary/download"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("Negotiation_Summary_NEG250918001.pdf")))
                .andExpect(content().string(containsString("%PDF-1.4")))
                .andExpect(content().string(containsString("ATIRATH GROUP")));

        mockMvc.perform(get("/api/v1/negotiations/NEG250918001/history/download"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("Negotiation_History_NEG250918001.pdf")))
                .andExpect(content().string(containsString("%PDF-1.4")))
                .andExpect(content().string(containsString("ATIRATH GROUP")));

        String initJson = """
                {
                  "companySymbol": "FVT",
                  "type": "BUY",
                  "counterpartyMemberId": "ATG10072",
                  "shares": 250,
                  "proposedPrice": 185.00,
                  "message": "Interested in 250 shares"
                }
                """;
        mockMvc.perform(post("/api/v1/negotiations/initiate")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(initJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.companyName").value("Farm Vantara"))
                .andExpect(jsonPath("$.data.shares").value(250));
    }

    @Test
    @DisplayName("User isolation: Counterparty sees negotiation from their own perspective (SELL side, counterparty is Sairam)")
    @org.springframework.security.test.context.support.WithMockUser(username = "rakesh@farmvantara.com")
    void testNegotiationsForCounterpartyUser() throws Exception {
        mockMvc.perform(get("/api/v1/negotiations/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.negotiations.content[0].negotiationNo").value("NEG250918001"))
                .andExpect(jsonPath("$.data.negotiations.content[0].type").value("Sell"))
                .andExpect(jsonPath("$.data.negotiations.content[0].counterpartyName", startsWith("Sairam")))
                .andExpect(jsonPath("$.data.negotiations.content[0].counterpartyMemberId").value("ATG10027"));
    }

    @Test
    @DisplayName("User isolation: Fresh user without negotiations gets 0 records and zero fallback data")
    @org.springframework.security.test.context.support.WithMockUser(username = "nonexistent.user@example.com")
    void testNegotiationsForUserWithZeroRecords() throws Exception {
        mockMvc.perform(get("/api/v1/negotiations/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.summary.allNegotiations").value(0))
                .andExpect(jsonPath("$.data.negotiations.totalElements").value(0))
                .andExpect(jsonPath("$.data.negotiations.content", hasSize(0)));
    }
}
