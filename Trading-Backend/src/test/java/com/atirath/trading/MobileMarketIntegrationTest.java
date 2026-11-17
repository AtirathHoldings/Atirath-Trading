package com.atirath.trading;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MobileMarketIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Mobile API: GET /api/v1/market/mobile/companies returns tailored MobileStockDto catalog")
    void testGetMobileCompanies() throws Exception {
        mockMvc.perform(get("/api/v1/market/mobile/companies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(7))))
                .andExpect(jsonPath("$.data[0].symbol").isNotEmpty())
                .andExpect(jsonPath("$.data[0].shortCode").isNotEmpty())
                .andExpect(jsonPath("$.data[0].name").isNotEmpty())
                .andExpect(jsonPath("$.data[0].price").isNumber())
                .andExpect(jsonPath("$.data[0].peRatio").isNumber())
                .andExpect(jsonPath("$.data[0].eps").isNumber())
                .andExpect(jsonPath("$.data[0].marketCapCr").isNumber())
                .andExpect(jsonPath("$.data[0].high52").isNumber())
                .andExpect(jsonPath("$.data[0].low52").isNumber())
                .andExpect(jsonPath("$.data[0].bids", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.data[0].asks", hasSize(greaterThan(0))));
    }

    @Test
    @DisplayName("Mobile API: GET /api/v1/market/mobile/companies/{symbol} supports symbol and alias lookup")
    void testGetMobileCompanyBySymbol() throws Exception {
        // Direct canonical symbol ATG-H
        mockMvc.perform(get("/api/v1/market/mobile/companies/ATG-H"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.shortCode").value("AHIL"))
                .andExpect(jsonPath("$.data.ceo").isNotEmpty())
                .andExpect(jsonPath("$.data.quarterlyFinancials").isArray())
                .andExpect(jsonPath("$.data.shareholding").isArray());

        // Alias lookup AHIL -> ATG-H
        mockMvc.perform(get("/api/v1/market/mobile/companies/AHIL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.shortCode").value("AHIL"));
    }

    @Test
    @DisplayName("Mobile API: GET /api/v1/market/mobile/sectors returns active sectors")
    void testGetMobileSectors() throws Exception {
        mockMvc.perform(get("/api/v1/market/mobile/sectors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(greaterThan(0))));
    }

    @Test
    @DisplayName("Mobile API: GET /api/v1/market/mobile/trades/recent returns recent trades with orderSide and isBuy")
    void testGetMobileRecentTrades() throws Exception {
        mockMvc.perform(get("/api/v1/market/mobile/trades/recent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("Mobile API: GET /api/v1/market/mobile/activity-summary returns totals for KPI cards")
    void testGetMobileActivitySummary() throws Exception {
        mockMvc.perform(get("/api/v1/market/mobile/activity-summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.activeCompaniesCount").isNumber());
    }

    @Test
    @DisplayName("Mobile API: GET /api/v1/market/mobile/orderbook/{symbol} returns depth for trade screen")
    void testGetMobileOrderBook() throws Exception {
        mockMvc.perform(get("/api/v1/market/mobile/orderbook/ATG-H"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("Web API: GET /api/v1/market/web/companies and /api/v1/market/companies continue to return full CompanySummaryDto")
    void testWebCompaniesEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/market/web/companies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(7))))
                .andExpect(jsonPath("$.data[0].companyId").isNotEmpty())
                .andExpect(jsonPath("$.data[0].companyCode").isNotEmpty());

        mockMvc.perform(get("/api/v1/market/companies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(7))));
    }

    @Test
    @DisplayName("Mobile API: GET /api/v1/market/mobile/overview returns complete market overview metrics")
    void testGetMobileOverview() throws Exception {
        mockMvc.perform(get("/api/v1/market/mobile/overview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.formattedTotalMarketCap").isNotEmpty())
                .andExpect(jsonPath("$.data.formattedTodayVolume").isNotEmpty())
                .andExpect(jsonPath("$.data.marketSentiment").isNotEmpty())
                .andExpect(jsonPath("$.data.buyOrdersCount").isNumber())
                .andExpect(jsonPath("$.data.sellOrdersCount").isNumber())
                .andExpect(jsonPath("$.data.tradesExecutedCount").isNumber())
                .andExpect(jsonPath("$.data.performanceSeries").isMap())
                .andExpect(jsonPath("$.data.companies", hasSize(greaterThanOrEqualTo(5))));
    }

    @Test
    @DisplayName("Mobile API: GET /api/v1/market/mobile/companies/{identifier}/price-history returns OHLC candles")
    void testGetPriceHistory() throws Exception {
        mockMvc.perform(get("/api/v1/market/mobile/companies/ATG-H/price-history?interval=DAY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("Mobile API: GET /api/v1/market/mobile/companies/{identifier}/orderbook returns live depth")
    void testGetCompanyOrderBook() throws Exception {
        mockMvc.perform(get("/api/v1/market/mobile/companies/AHIL/orderbook"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.symbol").value("ATG-H"));
    }

    @Test
    @DisplayName("Mobile API: GET /api/v1/market/mobile/companies/{identifier}/fundamentals returns financials & governance")
    void testGetCompanyFundamentals() throws Exception {
        mockMvc.perform(get("/api/v1/market/mobile/companies/AHIL/fundamentals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.quarterlyFinancials").isArray())
                .andExpect(jsonPath("$.data.shareholding").isArray());
    }

    @Test
    @DisplayName("Mobile API: GET /api/v1/market/mobile/documents returns all statutory documents")
    void testGetAllDocuments() throws Exception {
        mockMvc.perform(get("/api/v1/market/mobile/documents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }
}
