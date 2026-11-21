package com.atirath.trading;

import com.atirath.trading.modules.auth.dto.LoginRequest;
import com.atirath.trading.modules.auth.dto.device.DisconnectAndLoginRequestDto;
import com.atirath.trading.modules.auth.service.UserDeviceSessionManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DeviceLimitSessionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserDeviceSessionManager userDeviceSessionManager;

    private static final String TEST_EMAIL = "sairam.v@atirathgroup.com";
    private static final String TEST_PASSWORD = "Password@123";

    @BeforeEach
    void setUp() {
        userDeviceSessionManager.clearSessionsForUser(1L);
    }

    @Test
    @DisplayName("Verify 2-Device Limit Enforced: 3rd device triggers 409 Conflict with active devices, and kicking out device 1 revokes its token")
    void testActiveDeviceLimitAndRevocation() throws Exception {
        // Device 1: Web Login
        LoginRequest dev1Req = LoginRequest.builder()
                .identifier(TEST_EMAIL)
                .password(TEST_PASSWORD)
                .deviceId("dev-test-chrome-web")
                .deviceName("Chrome on Windows 11")
                .deviceType("WEB")
                .build();

        MvcResult dev1Result = mockMvc.perform(post("/api/v1/auth/web/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dev1Req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andReturn();

        JsonNode dev1Root = objectMapper.readTree(dev1Result.getResponse().getContentAsString());
        String tokenDev1 = dev1Root.path("data").path("accessToken").asText();
        assertNotNull(tokenDev1);

        // Verify Device 1 token can access protected mobile profile
        mockMvc.perform(get("/api/v1/auth/mobile/profile")
                        .header("Authorization", "Bearer " + tokenDev1))
                .andExpect(status().isOk());

        // Device 2: Mobile Login
        LoginRequest dev2Req = LoginRequest.builder()
                .identifier(TEST_EMAIL)
                .password(TEST_PASSWORD)
                .deviceId("dev-test-iphone-mobile")
                .deviceName("iPhone 15 Pro")
                .deviceType("MOBILE")
                .build();

        mockMvc.perform(post("/api/v1/auth/mobile/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dev2Req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // Device 3: Another Web Login -> MUST TRIGGER 409 Conflict (DEVICE_LIMIT_EXCEEDED)
        LoginRequest dev3Req = LoginRequest.builder()
                .identifier(TEST_EMAIL)
                .password(TEST_PASSWORD)
                .deviceId("dev-test-macbook-web")
                .deviceName("Safari on MacBook")
                .deviceType("WEB")
                .build();

        MvcResult conflictResult = mockMvc.perform(post("/api/v1/auth/web/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dev3Req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.data.errorCode").value("DEVICE_LIMIT_EXCEEDED"))
                .andExpect(jsonPath("$.data.maxAllowed").value(2))
                .andExpect(jsonPath("$.data.resolutionToken").isNotEmpty())
                .andExpect(jsonPath("$.data.activeDevices").isArray())
                .andReturn();

        JsonNode conflictNode = objectMapper.readTree(conflictResult.getResponse().getContentAsString());
        JsonNode dataNode = conflictNode.path("data");
        String resolutionToken = dataNode.path("resolutionToken").asText();
        assertNotNull(resolutionToken);
        assertEquals(2, dataNode.path("activeDevices").size(), "Must report exactly 2 active devices");

        // Now user selects Device 1 ("dev-test-chrome-web") to disconnect and continue login on Device 3
        DisconnectAndLoginRequestDto disconnectReq = DisconnectAndLoginRequestDto.builder()
                .resolutionToken(resolutionToken)
                .deviceIdToDisconnect("dev-test-chrome-web")
                .build();

        MvcResult dev3LoginResult = mockMvc.perform(post("/api/v1/auth/web/devices/disconnect-and-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(disconnectReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andReturn();

        JsonNode dev3Root = objectMapper.readTree(dev3LoginResult.getResponse().getContentAsString());
        String tokenDev3 = dev3Root.path("data").path("accessToken").asText();
        assertNotNull(tokenDev3);

        // CRITICAL CHECK: Device 1's token MUST now be blacklisted -> Auto-logout (401 Unauthorized)
        mockMvc.perform(get("/api/v1/auth/mobile/profile")
                        .header("Authorization", "Bearer " + tokenDev1))
                .andExpect(status().isUnauthorized());

        // Device 3's token works properly
        mockMvc.perform(get("/api/v1/auth/mobile/profile")
                        .header("Authorization", "Bearer " + tokenDev3))
                .andExpect(status().isOk());
    }

    @Test
    void testAdminLoginWorks() throws Exception {
        LoginRequest adminReq = LoginRequest.builder()
                .identifier("admin@atirathgroup.com")
                .password("Admin@123")
                .build();

        mockMvc.perform(post("/api/v1/auth/web/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.user.role").value("ADMIN"))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty());
    }
}
