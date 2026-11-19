package com.atirath.trading;

import com.atirath.trading.config.ClientIntegrityFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class OwaspApiSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("OWASP: Direct browser URL navigation (Sec-Fetch-Mode: navigate) is blocked with 403 Forbidden")
    void testDirectBrowserNavigationBlocked() throws Exception {
        mockMvc.perform(get("/api/v1/portfolio/dashboard")
                        .header("Sec-Fetch-Mode", "navigate")
                        .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message", containsString("Direct browser URL access is restricted")));
    }

    @Test
    @DisplayName("OWASP: Untrusted cross-origin request is blocked by CORS with 403 Forbidden")
    void testUntrustedCorsOriginBlocked() throws Exception {
        mockMvc.perform(get("/api/v1/portfolio/dashboard")
                        .header("Origin", "http://evil-attacker-website.com"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("OWASP: Untrusted third-party client or scraper is rejected with 403 Forbidden")
    void testUntrustedClientBlocked() throws Exception {
        mockMvc.perform(get("/api/v1/portfolio/dashboard")
                        .header("X-Client-App", "MALICIOUS_BOT")
                        .header("X-Client-Key", "wrong-secret-key"))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message", containsString("Untrusted client application")));
    }

    @Test
    @DisplayName("OWASP: Private endpoint without JWT Bearer token is rejected with 401 Unauthorized")
    void testUnauthenticatedPrivateEndpointReturns401() throws Exception {
        mockMvc.perform(get("/api/v1/portfolio/dashboard")
                        .header(ClientIntegrityFilter.HEADER_CLIENT_APP, ClientIntegrityFilter.CLIENT_MOBILE)
                        .header(ClientIntegrityFilter.HEADER_CLIENT_KEY, ClientIntegrityFilter.SHARED_CLIENT_KEY))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message", containsString("Unauthorized")));
    }

    @Test
    @DisplayName("OWASP: CORS Preflight OPTIONS from allowed web origin returns 200 OK with security headers")
    void testCorsPreflightAllowedForWhitelistedOrigin() throws Exception {
        mockMvc.perform(options("/api/v1/market/mobile/companies")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "GET")
                        .header("Access-Control-Request-Headers", "Authorization,Content-Type,X-Client-App,X-Client-Key"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }
}
