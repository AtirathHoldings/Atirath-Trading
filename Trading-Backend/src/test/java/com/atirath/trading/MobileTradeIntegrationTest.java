package com.atirath.trading;

import com.atirath.trading.modules.market.dto.mobile.MobilePlaceOrderRequest;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MobileTradeIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Mobile API: GET /api/v1/trade/mobile/terminal/{symbol} returns lightweight trade screen data")
    void testGetMobileTradeScreen() throws Exception {
        mockMvc.perform(get("/api/v1/trade/mobile/terminal/ATG-H"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.symbol").value("ATG-H"))
                .andExpect(jsonPath("$.data.companyName").isNotEmpty())
                .andExpect(jsonPath("$.data.currentPrice").isNumber())
                .andExpect(jsonPath("$.data.bestBid").isNumber())
                .andExpect(jsonPath("$.data.bestAsk").isNumber())
                .andExpect(jsonPath("$.data.availableCash").isNumber())
                .andExpect(jsonPath("$.data.availableShares").isNumber())
                .andExpect(jsonPath("$.data.quickQuantities", hasSize(5)))
                .andExpect(jsonPath("$.data.bids", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.data.asks", hasSize(greaterThan(0))));
    }

    @Test
    @DisplayName("Mobile API: POST /api/v1/trade/mobile/preview calculates exact 0.1% fee, 18% GST and balances")
    void testPreviewMobileOrder() throws Exception {
        MobilePlaceOrderRequest req = MobilePlaceOrderRequest.builder()
                .symbol("ATG-H")
                .side("BUY")
                .orderType("LIMIT")
                .quantity(1000L)
                .price(new BigDecimal("242.00"))
                .build();

        mockMvc.perform(post("/api/v1/trade/mobile/preview")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.orderValue").value(242000.0))
                .andExpect(jsonPath("$.data.transactionFee").value(242.0))
                .andExpect(jsonPath("$.data.gst").value(43.56))
                .andExpect(jsonPath("$.data.totalCharges").value(285.56))
                .andExpect(jsonPath("$.data.totalAmount").value(242285.56))
                .andExpect(jsonPath("$.data.isSufficient").value(true));
    }

    @Test
    @DisplayName("Mobile API: POST /api/v1/trade/mobile/orders creates order and returns concise 6-stage lifecycle")
    void testPlaceMobileOrder() throws Exception {
        MobilePlaceOrderRequest req = MobilePlaceOrderRequest.builder()
                .symbol("ATG-H")
                .side("BUY")
                .orderType("LIMIT")
                .quantity(500L)
                .price(new BigDecimal("240.00"))
                .validity("DAY_ORDER")
                .acceptTerms(true)
                .build();

        mockMvc.perform(post("/api/v1/trade/mobile/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.orderId").isNumber())
                .andExpect(jsonPath("$.data.orderNo").isNotEmpty())
                .andExpect(jsonPath("$.data.symbol").value("ATG-H"))
                .andExpect(jsonPath("$.data.statusBadge").isNotEmpty())
                .andExpect(jsonPath("$.data.nextSteps", hasSize(6)))
                .andExpect(jsonPath("$.data.nextSteps[0].status").value("COMPLETED"));
    }

    @Test
    @DisplayName("Mobile API: GET /api/v1/trade/mobile/my-orders returns tab counts and compact cards")
    void testGetMobileMyOrders() throws Exception {
        mockMvc.perform(get("/api/v1/trade/mobile/my-orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.tabCounts.ALL").isNumber())
                .andExpect(jsonPath("$.data.tabCounts.OPEN").isNumber())
                .andExpect(jsonPath("$.data.orders", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.data.orders[0].orderNo").isNotEmpty())
                .andExpect(jsonPath("$.data.orders[0].statusBadge").isNotEmpty())
                .andExpect(jsonPath("$.data.orders[0].canCancel").isBoolean());
    }

    @Test
    @DisplayName("Mobile API: GET /api/v1/trade/mobile/orders/{orderId}/lifecycle returns mobile stepper and escrow info")
    void testGetMobileOrderLifecycle() throws Exception {
        mockMvc.perform(get("/api/v1/trade/mobile/orders/status-by-no/BUY25091814322/lifecycle"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.orderNo").value("BUY25091814322"))
                .andExpect(jsonPath("$.data.currentStatusTitle").value("In Escrow"))
                .andExpect(jsonPath("$.data.currentStepIndex").isNumber())
                .andExpect(jsonPath("$.data.steps", hasSize(7)))
                .andExpect(jsonPath("$.data.escrow.referenceNo").value("ESC2509180021"))
                .andExpect(jsonPath("$.data.escrow.amountBlocked").value(242000.00))
                .andExpect(jsonPath("$.data.counterpartyName").value("Rakesh Mehta"))
                .andExpect(jsonPath("$.data.recentActivity", hasSize(greaterThan(0))));
    }

    @Test
    @DisplayName("Mobile API: GET /api/v1/trade/mobile/wallet and /holdings return lightweight user state")
    void testMobileWalletAndHoldings() throws Exception {
        mockMvc.perform(get("/api/v1/trade/mobile/wallet"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.availableBalance").isNumber())
                .andExpect(jsonPath("$.data.currency").value("INR"));

        mockMvc.perform(get("/api/v1/trade/mobile/holdings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("Mobile Stock DTO has openPrice, prevClose, dayHigh, dayLow, turnoverCr for stock_model.dart")
    void testMobileStockModelFields() throws Exception {
        mockMvc.perform(get("/api/v1/market/mobile/companies/ATG-H"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.openPrice").isNumber())
                .andExpect(jsonPath("$.data.prevClose").isNumber())
                .andExpect(jsonPath("$.data.dayHigh").isNumber())
                .andExpect(jsonPath("$.data.dayLow").isNumber())
                .andExpect(jsonPath("$.data.turnoverCr").isNumber())
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }
}
