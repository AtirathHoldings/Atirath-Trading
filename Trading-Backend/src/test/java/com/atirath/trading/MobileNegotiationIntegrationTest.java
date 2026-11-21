package com.atirath.trading;

import com.atirath.trading.modules.negotiation.dto.mobile.MobileCounterOfferRequestDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "sairam.v@atirathgroup.com")
class MobileNegotiationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private com.atirath.trading.modules.negotiation.service.NegotiationService negotiationService;

    @Autowired
    private com.atirath.trading.modules.negotiation.repository.NegotiationRepository negotiationRepository;

    @Autowired
    private com.atirath.trading.modules.auth.repository.UserAccountRepository userAccountRepository;

    @BeforeEach
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
        } catch (Exception ignored) {}
    }

    @Test
    @DisplayName("Screen 1: GET /api/v1/negotiations/mobile/dashboard returns lightweight KPI counters and cards")
    void testMobileNegotiationDashboard() throws Exception {
        mockMvc.perform(get("/api/v1/negotiations/mobile/dashboard?tab=ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.counters.totalCount").isNumber())
                .andExpect(jsonPath("$.data.counters.activeCount").isNumber())
                .andExpect(jsonPath("$.data.counters.awaitingResponseCount").isNumber())
                .andExpect(jsonPath("$.data.negotiations", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.data.negotiations[0].negotiationNo").isNotEmpty())
                .andExpect(jsonPath("$.data.negotiations[0].companySymbol").isNotEmpty())
                .andExpect(jsonPath("$.data.negotiations[0].type").isNotEmpty())
                .andExpect(jsonPath("$.data.negotiations[0].proposedPriceFormatted").isNotEmpty());
    }

    @Test
    @DisplayName("Screen 2: GET /api/v1/negotiations/mobile/{negotiationNo} returns details, parties, and actions")
    void testMobileNegotiationDetail() throws Exception {
        mockMvc.perform(get("/api/v1/negotiations/mobile/NEG250918001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.negotiationNo").value("NEG250918001"))
                .andExpect(jsonPath("$.data.companySymbol").value("ATG-H"))
                .andExpect(jsonPath("$.data.currentMarketPrice").isNumber())
                .andExpect(jsonPath("$.data.currentOfferPrice").isNumber())
                .andExpect(jsonPath("$.data.quantity").value(1000))
                .andExpect(jsonPath("$.data.canCounterOffer").value(true))
                .andExpect(jsonPath("$.data.canAccept").value(true))
                .andExpect(jsonPath("$.data.canReject").value(true));
    }

    @Test
    @DisplayName("Screen 3: GET & POST /api/v1/negotiations/mobile/{negotiationNo}/counter-offer pre-fills and submits counter offer")
    void testMobileCounterOffer() throws Exception {
        // Prefill form
        mockMvc.perform(get("/api/v1/negotiations/mobile/NEG250918001/counter-offer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.negotiationNo").value("NEG250918001"))
                .andExpect(jsonPath("$.data.priceStep").value(2.5))
                .andExpect(jsonPath("$.data.allowedSettlementTerms", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.data.escrowProtectionNote").isNotEmpty());

        // Submit counter offer
        MobileCounterOfferRequestDto req = MobileCounterOfferRequestDto.builder()
                .price(new BigDecimal("238.50"))
                .quantity(1000L)
                .settlementTerm("T+2 Standard")
                .message("Submitting revised counter offer for 1,000 shares")
                .build();

        mockMvc.perform(post("/api/v1/negotiations/mobile/NEG250918001/counter-offer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.negotiationNo").value("NEG250918001"))
                .andExpect(jsonPath("$.data.newOfferedPrice").value(238.50))
                .andExpect(jsonPath("$.data.newTotalValue").value(238500.00))
                .andExpect(jsonPath("$.data.toastMessage").isNotEmpty());
    }

    @Test
    @DisplayName("Screen 4: GET /api/v1/negotiations/mobile/{negotiationNo}/history returns timeline events")
    void testMobileNegotiationHistory() throws Exception {
        mockMvc.perform(get("/api/v1/negotiations/mobile/NEG250918001/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.negotiationNo").value("NEG250918001"))
                .andExpect(jsonPath("$.data.events", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.data.events[0].eventType").isNotEmpty())
                .andExpect(jsonPath("$.data.events[0].senderRole").isNotEmpty());
    }

    @Test
    @DisplayName("Screen 4: POST /api/v1/negotiations/mobile/{negotiationNo}/messages posts timeline message")
    void testMobilePostMessage() throws Exception {
        Map<String, String> payload = Map.of("message", "Can we close this deal today?");
        mockMvc.perform(post("/api/v1/negotiations/mobile/NEG250918001/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.eventType").value("MESSAGE"))
                .andExpect(jsonPath("$.data.senderRole").value("ME"))
                .andExpect(jsonPath("$.data.messageText").value("Can we close this deal today?"));
    }

    @Test
    @DisplayName("Screen 5: GET & POST /api/v1/negotiations/mobile/{negotiationNo}/trade-agreement retrieves and confirms agreement")
    void testMobileTradeAgreement() throws Exception {
        // Retrieve trade agreement
        mockMvc.perform(get("/api/v1/negotiations/mobile/NEG250918001/trade-agreement"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.negotiationNo").value("NEG250918001"))
                .andExpect(jsonPath("$.data.companySymbol").isNotEmpty())
                .andExpect(jsonPath("$.data.agreedPrice").isNumber())
                .andExpect(jsonPath("$.data.totalValue").isNumber())
                .andExpect(jsonPath("$.data.escrowAccountRef").value("ESCROW-ATG-10027"))
                .andExpect(jsonPath("$.data.nextScreen").value("ESCROW_SCREEN"));

        // Confirm trade agreement
        mockMvc.perform(post("/api/v1/negotiations/mobile/NEG250918001/trade-agreement/confirm"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").isNotEmpty());
    }

    @Test
    @DisplayName("Screen 6: GET & POST /api/v1/negotiations/mobile/notifications handles notification feed and read status")
    void testMobileNegotiationNotifications() throws Exception {
        mockMvc.perform(get("/api/v1/negotiations/mobile/notifications?category=ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.unreadCount").isNumber())
                .andExpect(jsonPath("$.data.totalCount").isNumber())
                .andExpect(jsonPath("$.data.notifications", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.data.notifications[0].title").isNotEmpty())
                .andExpect(jsonPath("$.data.notifications[0].targetScreen").isNotEmpty());

        mockMvc.perform(post("/api/v1/negotiations/mobile/notifications/1/read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(true));

        mockMvc.perform(post("/api/v1/negotiations/mobile/notifications/read-all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
