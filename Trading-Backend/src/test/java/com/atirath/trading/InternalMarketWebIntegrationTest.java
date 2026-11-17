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

import java.time.Duration;
import java.time.OffsetDateTime;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class InternalMarketWebIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String bearerForDemoUser() {
        UserAccount user = userAccountRepository.findByEmailIgnoreCase("sairam.v@atirathgroup.com")
                .orElseGet(() -> userAccountRepository.findAll().get(0));
        return "Bearer " + jwtTokenProvider.generateAccessToken(user);
    }

    @Test
    @DisplayName("Web Internal Market: Companies, Overview, Live Pricing & Activity Summary")
    void testOverviewAndCompaniesEndpoints() throws Exception {
        mockMvc.perform(get("/api/v1/market/companies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(7))))
                .andExpect(jsonPath("$.data[0].currentPrice").isNumber())
                .andExpect(jsonPath("$.data[0].high52Week").isNumber())
                .andExpect(jsonPath("$.data[0].low52Week").isNumber());

        mockMvc.perform(get("/api/v1/market/companies/sectors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(greaterThan(0))));

        mockMvc.perform(get("/api/v1/market/companies/symbol/ATG-H"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.symbol").value("ATG-H"))
                .andExpect(jsonPath("$.data.openPrice").isNumber())
                .andExpect(jsonPath("$.data.highPrice").isNumber())
                .andExpect(jsonPath("$.data.lowPrice").isNumber())
                .andExpect(jsonPath("$.data.previousClose").isNumber())
                .andExpect(jsonPath("$.data.turnoverValue").isNumber())
                .andExpect(jsonPath("$.data.leadership", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.data.highlights", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.data.shareholding", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.data.latestRatios.peRatio").isNumber())
                .andExpect(jsonPath("$.data.annualFinancials", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.data.quarterlyFinancials", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.data.recentAnnouncements", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.data.documents", hasSize(greaterThan(0))));

        mockMvc.perform(get("/api/v1/market/trades/activity-summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.activeCompaniesCount").isNumber())
                .andExpect(jsonPath("$.data.marketStatus").value("OPEN"))
                .andExpect(jsonPath("$.data.marketHours").value("09:00 AM - 05:00 PM"));
    }

    @Test
    @DisplayName("Web Internal Market: Sub-endpoints return 200 without LazyInitialization 500s")
    void testCompanySubEndpoints() throws Exception {
        for (String suffix : new String[]{
                "leadership", "highlights", "shareholding",
                "financials/annual", "financials/quarterly",
                "ratios", "announcements", "documents"
        }) {
            mockMvc.perform(get("/api/v1/market/companies/3/" + suffix))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data", hasSize(greaterThan(0))));
        }

        for (String interval : new String[]{"INTRADAY", "DAY", "WEEK", "MONTH"}) {
            MvcResult histResult = mockMvc.perform(get("/api/v1/market/companies/3/price-history")
                            .param("intervalType", interval))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(10))))
                    .andReturn();
            if ("INTRADAY".equals(interval)) {
                JsonNode intradayArr = objectMapper.readTree(histResult.getResponse().getContentAsString()).path("data");
                OffsetDateTime firstTs = OffsetDateTime.parse(intradayArr.get(0).path("priceTime").asText());
                OffsetDateTime lastTs = OffsetDateTime.parse(intradayArr.get(intradayArr.size() - 1).path("priceTime").asText());
                assertTrue(Duration.between(firstTs, lastTs).toHours() <= 36,
                        "INTRADAY candles must stay within contiguous <= 36h window");
            }
        }

        // Verify downloadable PDF bytes from document endpoint
        MvcResult docsResult = mockMvc.perform(get("/api/v1/market/companies/3/documents"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode docsJson = objectMapper.readTree(docsResult.getResponse().getContentAsString()).path("data");
        long firstDocId = docsJson.get(0).path("documentId").asLong();
        MvcResult dlResult = mockMvc.perform(get("/api/v1/market/companies/3/documents/" + firstDocId + "/download")
                        .param("inline", "true"))
                .andExpect(status().isOk())
                .andReturn();
        assertTrue(dlResult.getResponse().getContentAsString().startsWith("%PDF-"));
    }

    @Test
    @DisplayName("Web Internal Market: Trades, OrderBook, Watchlist, Wallet, Holdings & Order Submit")
    void testInteractiveTradingAndWatchlistEndpoints() throws Exception {
        String auth = bearerForDemoUser();

        mockMvc.perform(get("/api/v1/market/trades/company/3").param("limit", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.data[0].buyerName").isNotEmpty())
                .andExpect(jsonPath("$.data[0].sellerName").isNotEmpty());

        mockMvc.perform(get("/api/v1/market/live/orderbook/ATG-H"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.bids", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.data.asks", hasSize(greaterThan(0))));

        mockMvc.perform(post("/api/v1/market/watchlist/3").header("Authorization", auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/v1/market/watchlist").header("Authorization", auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(delete("/api/v1/market/watchlist/3").header("Authorization", auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/v1/trade/wallet").header("Authorization", auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.availableBalance").isNumber());

        mockMvc.perform(get("/api/v1/trade/holdings").header("Authorization", auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());

        // Small realistic quantity (10 shares) so test succeeds regardless of prior runs
        mockMvc.perform(post("/api/v1/trade/orders")
                        .header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"symbol\":\"ATG-H\",\"orderSide\":\"BUY\",\"orderType\":\"LIMIT\",\"quantity\":10,\"pricePerShare\":245.00,\"validity\":\"DAY_ORDER\",\"acceptTerms\":true,\"orderSource\":\"WEB\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.orderReferenceNo").isNotEmpty());
    }
}
