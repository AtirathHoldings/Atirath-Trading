package com.atirath.trading;

import com.atirath.trading.config.JwtTokenProvider;
import com.atirath.trading.modules.auth.model.UserAccount;
import com.atirath.trading.modules.auth.repository.UserAccountRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TradeTerminalIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("Screen 1: GET /api/v1/trade/terminal/ATG-H returns complete Trade Terminal snapshot")
    void testGetTradeTerminalSnapshot() throws Exception {
        mockMvc.perform(get("/api/v1/trade/terminal/ATG-H"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                // Company Ticker Header
                .andExpect(jsonPath("$.data.company.symbol").value("ATG-H"))
                .andExpect(jsonPath("$.data.company.companyName", containsString("Atirath Holdings")))
                .andExpect(jsonPath("$.data.company.status").value("Active"))
                .andExpect(jsonPath("$.data.company.currentPrice").isNumber())
                .andExpect(jsonPath("$.data.company.priceChange").isNumber())
                .andExpect(jsonPath("$.data.company.openPrice").value(210.00))
                .andExpect(jsonPath("$.data.company.dayHigh").value(250.00))
                .andExpect(jsonPath("$.data.company.dayLow").value(208.00))
                .andExpect(jsonPath("$.data.company.volumeShares", greaterThanOrEqualTo(250000)))
                // Live Order Book (5-depth)
                .andExpect(jsonPath("$.data.orderBook.asks", hasSize(greaterThanOrEqualTo(5))))
                .andExpect(jsonPath("$.data.orderBook.bids", hasSize(greaterThanOrEqualTo(5))))
                .andExpect(jsonPath("$.data.orderBook.spread").isNumber())
                // Recent Trades Tape
                .andExpect(jsonPath("$.data.recentTrades", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.data.recentTrades[0].price").isNumber())
                .andExpect(jsonPath("$.data.recentTrades[0].quantity").isNumber())
                // Market Depth & Key Stats
                .andExpect(jsonPath("$.data.marketDepth.buyOrders").isArray())
                .andExpect(jsonPath("$.data.marketDepth.sellOrders").isArray())
                .andExpect(jsonPath("$.data.keyStatistics.peRatio").value(18.5))
                .andExpect(jsonPath("$.data.keyStatistics.dividendYield").value(1.8))
                // User Wallet & Holdings
                .andExpect(jsonPath("$.data.userAvailableBalance").value(1248500.00))
                .andExpect(jsonPath("$.data.userAvailableShares").value(3500))
                .andExpect(jsonPath("$.data.marketInsight", containsString("ATG-H")));
    }

    @Test
    @DisplayName("Screen 2 & 4: POST /api/v1/trade/orders/preview calculates exact 0.1% Fee, 18% GST, and Post Order Balance")
    void testPreviewBuyOrderCalculations() throws Exception {
        Map<String, Object> req = Map.of(
                "symbol", "ATG-H",
                "orderSide", "BUY",
                "orderType", "LIMIT",
                "quantity", 1000,
                "pricePerShare", new BigDecimal("242.00"),
                "validity", "Day Order (Valid till end of trading day)"
        );

        mockMvc.perform(post("/api/v1/trade/orders/preview")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.symbol").value("ATG-H"))
                .andExpect(jsonPath("$.data.orderSide").value("BUY"))
                .andExpect(jsonPath("$.data.quantity").value(1000))
                .andExpect(jsonPath("$.data.pricePerShare").value(242.00))
                // Calculations matching screenshots:
                // Order Value: 1000 * 242.00 = 242,000.00
                .andExpect(jsonPath("$.data.orderValue").value(242000.00))
                // Transaction Fee: 0.1% = 242.00
                .andExpect(jsonPath("$.data.transactionFee").value(242.00))
                // GST: 18% of 242.00 = 43.56
                .andExpect(jsonPath("$.data.gstAmount").value(43.56))
                // Total Charges: 285.56
                .andExpect(jsonPath("$.data.totalCharges").value(285.56))
                // Total Amount Payable: 242,285.56
                .andExpect(jsonPath("$.data.totalAmount").value(242285.56))
                // Available Balance: 12,48,500.00
                .andExpect(jsonPath("$.data.availableBalance").value(1248500.00))
                // Post Order Balance: 10,06,214.44 (12,48,500 - 242,285.56)
                .andExpect(jsonPath("$.data.postOrderBalance").value(1006214.44))
                .andExpect(jsonPath("$.data.isBalanceSufficient").value(true))
                .andExpect(jsonPath("$.data.validationMessage").value("Sufficient balance to place this order."))
                .andExpect(jsonPath("$.data.canProceed").value(true));
    }

    @Test
    @DisplayName("Screen 3: POST /api/v1/trade/orders/preview for SELL verifies Available Shares 3,500")
    void testPreviewSellOrder() throws Exception {
        Map<String, Object> req = Map.of(
                "symbol", "ATG-H",
                "orderSide", "SELL",
                "orderType", "LIMIT",
                "quantity", 500,
                "pricePerShare", new BigDecimal("244.00")
        );

        mockMvc.perform(post("/api/v1/trade/orders/preview")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.orderSide").value("SELL"))
                .andExpect(jsonPath("$.data.availableShares").value(3500))
                .andExpect(jsonPath("$.data.isSharesSufficient").value(true))
                .andExpect(jsonPath("$.data.canProceed").value(true));
    }

    @Test
    @DisplayName("Screen 5: POST /api/v1/trade/orders places order and returns 6-step lifecycle timeline")
    void testPlaceOrderAndGetConfirmationTimeline() throws Exception {
        Map<String, Object> req = Map.of(
                "symbol", "ATG-H",
                "orderSide", "BUY",
                "orderType", "LIMIT",
                "quantity", 1000,
                "pricePerShare", new BigDecimal("242.00"),
                "validity", "Day Order",
                "acceptTerms", true,
                "orderSource", "WEB"
        );

        mockMvc.perform(post("/api/v1/trade/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.orderReferenceNo", startsWith("BUY")))
                .andExpect(jsonPath("$.data.orderSide").value("BUY"))
                .andExpect(jsonPath("$.data.quantity").value(1000))
                .andExpect(jsonPath("$.data.pricePerShare").value(242.00))
                .andExpect(jsonPath("$.data.orderValue").value(242000.00))
                .andExpect(jsonPath("$.data.transactionFee").value(242.00))
                .andExpect(jsonPath("$.data.gstAmount").value(43.56))
                .andExpect(jsonPath("$.data.totalCharges").value(285.56))
                .andExpect(jsonPath("$.data.totalAmount").value(242285.56))
                // 6-step lifecycle timeline from Image 5
                .andExpect(jsonPath("$.data.timeline", hasSize(6)))
                .andExpect(jsonPath("$.data.timeline[0].title").value("Order Placed"))
                .andExpect(jsonPath("$.data.timeline[0].status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.timeline[1].title").value("Counterparty Matching"))
                .andExpect(jsonPath("$.data.timeline[2].title").value("Negotiation (If Required)"))
                .andExpect(jsonPath("$.data.timeline[3].title").value("Escrow"))
                .andExpect(jsonPath("$.data.timeline[4].title").value("Approval"))
                .andExpect(jsonPath("$.data.timeline[5].title").value("Share Transfer & Settlement"));
    }

    @Test
    @DisplayName("GET /api/v1/trade/wallet returns escrow wallet balance")
    void testGetWallet() throws Exception {
        mockMvc.perform(get("/api/v1/trade/wallet"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accountReference", notNullValue()))
                .andExpect(jsonPath("$.data.availableBalance").isNumber())
                .andExpect(jsonPath("$.data.currencyCode").value("INR"));
    }

    @Test
    @DisplayName("GET /api/v1/trade/holdings returns user stock holdings")
    void testGetHoldings() throws Exception {
        mockMvc.perform(get("/api/v1/trade/holdings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.data[0].symbol").value("ATG-H"))
                .andExpect(jsonPath("$.data[0].totalShares").value(3500))
                .andExpect(jsonPath("$.data[0].currentPrice").isNumber());
    }

    @Test
    @DisplayName("Screens 1 & 3: GET /api/v1/trade/orders/status-by-no/BUY25091814322 returns 7-step progress, Escrow, and Counterparty")
    void testGetOrderStatusAndDetails() throws Exception {
        mockMvc.perform(get("/api/v1/trade/orders/status-by-no/BUY25091814322"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.orderNo").value("BUY25091814322"))
                .andExpect(jsonPath("$.data.company.symbol").value("ATG-H"))
                .andExpect(jsonPath("$.data.company.companyName", containsString("Atirath Holdings")))
                .andExpect(jsonPath("$.data.quantity").value(1000))
                .andExpect(jsonPath("$.data.pricePerShare").value(242.00))
                .andExpect(jsonPath("$.data.orderValue").value(242000.00))
                .andExpect(jsonPath("$.data.currentStatus.title").value("In Escrow"))
                .andExpect(jsonPath("$.data.currentStatus.description", containsString("Funds have been blocked in escrow")))
                // 7-step horizontal progress stepper
                .andExpect(jsonPath("$.data.progressSteps", hasSize(7)))
                .andExpect(jsonPath("$.data.progressSteps[0].title").value("Order Placed"))
                .andExpect(jsonPath("$.data.progressSteps[0].status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.progressSteps[1].title").value("Counterparty Matching"))
                .andExpect(jsonPath("$.data.progressSteps[2].title").value("Negotiation (Optional)"))
                .andExpect(jsonPath("$.data.progressSteps[3].title").value("Escrow In Progress"))
                .andExpect(jsonPath("$.data.progressSteps[3].status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.data.progressSteps[4].title").value("Approval Pending"))
                .andExpect(jsonPath("$.data.progressSteps[5].title").value("Share Transfer Pending"))
                .andExpect(jsonPath("$.data.progressSteps[6].title").value("Settlement Pending"))
                // Counterparty details matching screenshot
                .andExpect(jsonPath("$.data.counterpartyDetails.memberName").value("Rakesh Mehta"))
                .andExpect(jsonPath("$.data.counterpartyDetails.memberId").value("ATG10056"))
                .andExpect(jsonPath("$.data.counterpartyDetails.portfolioHoldings").value("5,000 shares"))
                // Escrow details matching screenshot
                .andExpect(jsonPath("$.data.escrowDetails.escrowReferenceNo").value("ESC2509180021"))
                .andExpect(jsonPath("$.data.escrowDetails.amountBlocked").value(242000.00))
                .andExpect(jsonPath("$.data.escrowDetails.bankOrPartner").value("HDFC Bank (Escrow)"))
                .andExpect(jsonPath("$.data.escrowDetails.statusBadge").value("Funds Blocked"))
                // Market price info
                .andExpect(jsonPath("$.data.marketInformation.currentMarketPrice").isNumber())
                // Documents
                .andExpect(jsonPath("$.data.documents", hasSize(greaterThanOrEqualTo(2))));
    }

    @Test
    @DisplayName("Screen 2: GET /api/v1/trade/orders/my-orders returns dynamic database orders with status tabs, filters, and pagination")
    void testGetMyOrdersScreen() throws Exception {
        mockMvc.perform(get("/api/v1/trade/orders/my-orders?page=1&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.summaryCounts.allCount", notNullValue()))
                .andExpect(jsonPath("$.data.orders", notNullValue()))
                .andExpect(jsonPath("$.data.pagination.currentPage").value(1))
                .andExpect(jsonPath("$.data.pagination.pageSize").value(10));
    }

    @Test
    @Transactional
    @DisplayName("Screens 6 & 8: Document PDF download, Modify Order, and Cancel Order full escrow refund")
    void testOrderDocumentsModifyAndCancelRefund() throws Exception {
        MvcResult orderPdf = mockMvc.perform(get("/api/v1/trade/orders/documents/order-confirmation")
                        .param("orderNo", "BUY25091814322"))
                .andExpect(status().isOk())
                .andReturn();
        assertTrue(orderPdf.getResponse().getContentAsString().startsWith("%PDF-"));

        MvcResult escrowPdf = mockMvc.perform(get("/api/v1/trade/orders/documents/escrow-confirmation")
                        .param("orderNo", "BUY25091814322"))
                .andExpect(status().isOk())
                .andReturn();
        assertTrue(escrowPdf.getResponse().getContentAsString().startsWith("%PDF-"));

        UserAccount user = userAccountRepository.findByEmailIgnoreCase("sairam.v@atirathgroup.com")
                .orElseGet(() -> userAccountRepository.findAll().get(0));
        String auth = "Bearer " + jwtTokenProvider.generateAccessToken(user);

        MvcResult walletBeforeRes = mockMvc.perform(get("/api/v1/trade/wallet").header("Authorization", auth))
                .andExpect(status().isOk())
                .andReturn();
        BigDecimal availBefore = new BigDecimal(objectMapper.readTree(walletBeforeRes.getResponse().getContentAsString())
                .path("data").path("availableBalance").asText());

        Map<String, Object> placeReq = Map.of(
                "symbol", "ATG-H",
                "orderSide", "BUY",
                "orderType", "LIMIT",
                "quantity", 10,
                "pricePerShare", new BigDecimal("242.00"),
                "validity", "DAY_ORDER",
                "acceptTerms", true,
                "orderSource", "WEB"
        );
        MvcResult placedRes = mockMvc.perform(post("/api/v1/trade/orders")
                        .header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(placeReq)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode placedJson = objectMapper.readTree(placedRes.getResponse().getContentAsString()).path("data");
        long orderId = placedJson.path("orderId").asLong();

        Map<String, Object> modReq = Map.of(
                "symbol", "ATG-H",
                "orderSide", "BUY",
                "orderType", "LIMIT",
                "quantity", 12,
                "pricePerShare", new BigDecimal("240.00")
        );
        mockMvc.perform(put("/api/v1/trade/orders/" + orderId)
                        .header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(modReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.quantity").value(12))
                .andExpect(jsonPath("$.data.pricePerShare").value(240.00));

        MvcResult orderIdPdf = mockMvc.perform(get("/api/v1/trade/orders/" + orderId + "/documents/order-confirmation")
                        .header("Authorization", auth))
                .andExpect(status().isOk())
                .andReturn();
        assertTrue(orderIdPdf.getResponse().getContentAsString().startsWith("%PDF-"));

        mockMvc.perform(post("/api/v1/trade/orders/" + orderId + "/cancel")
                        .header("Authorization", auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        MvcResult walletAfterRes = mockMvc.perform(get("/api/v1/trade/wallet").header("Authorization", auth))
                .andExpect(status().isOk())
                .andReturn();
        BigDecimal availAfter = new BigDecimal(objectMapper.readTree(walletAfterRes.getResponse().getContentAsString())
                .path("data").path("availableBalance").asText());
        assertEquals(0, availBefore.compareTo(availAfter), "Cancel must refund full blocked amount including charges");
    }
}
