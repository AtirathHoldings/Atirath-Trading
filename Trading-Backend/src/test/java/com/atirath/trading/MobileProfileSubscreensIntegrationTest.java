package com.atirath.trading;

import com.atirath.trading.modules.profile.dto.mobile.MobileAddBankAccountRequestDto;
import com.atirath.trading.modules.profile.dto.mobile.MobileSettingsUpdateRequestDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "sairam.v@atirathgroup.com")
class MobileProfileSubscreensIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("1. GET /api/v1/profile/mobile/bank-accounts returns linked bank accounts")
    void testGetBankAccounts() throws Exception {
        mockMvc.perform(get("/api/v1/profile/mobile/bank-accounts")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accounts").isArray())
                .andExpect(jsonPath("$.data.settlementNote").isNotEmpty());
    }

    @Test
    @DisplayName("2. POST /api/v1/profile/mobile/bank-accounts adds a new bank account")
    void testAddBankAccount() throws Exception {
        MobileAddBankAccountRequestDto req = MobileAddBankAccountRequestDto.builder()
                .bankName("State Bank of India")
                .accountNumber("987654321012")
                .ifscCode("SBIN0001234")
                .accountType("Savings")
                .upiId("rajesh@sbi")
                .accountHolderName("Rajesh Kumar Sharma")
                .build();

        mockMvc.perform(post("/api/v1/profile/mobile/bank-accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accounts").isArray());
    }

    @Test
    @DisplayName("3. PUT /api/v1/profile/mobile/bank-accounts/1/primary sets primary bank account")
    void testSetPrimaryBankAccount() throws Exception {
        String res = mockMvc.perform(get("/api/v1/profile/mobile/bank-accounts")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        long accountId = 1L;
        try {
            com.fasterxml.jackson.databind.JsonNode root = objectMapper.readTree(res);
            com.fasterxml.jackson.databind.JsonNode accs = root.path("data").path("accounts");
            if (accs.isArray() && accs.size() > 0) {
                accountId = accs.get(0).path("accountId").asLong(1L);
            }
        } catch (Exception ignored) {}

        mockMvc.perform(put("/api/v1/profile/mobile/bank-accounts/" + accountId + "/primary")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accounts").isArray());
    }

    @Test
    @DisplayName("4. GET /api/v1/profile/mobile/security returns security status")
    void testGetSecurity() throws Exception {
        mockMvc.perform(get("/api/v1/profile/mobile/security")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.biometricSupported").value(true))
                .andExpect(jsonPath("$.data.activeDevicesCount").isNumber());
    }

    @Test
    @DisplayName("5. PUT /api/v1/profile/mobile/security/2fa toggles two-factor authentication")
    void testToggle2fa() throws Exception {
        mockMvc.perform(put("/api/v1/profile/mobile/security/2fa?enable=true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.twoFactorEnabled").value(true));
    }

    @Test
    @DisplayName("6. PUT /api/v1/profile/mobile/security/login-alerts toggles login alerts")
    void testToggleLoginAlerts() throws Exception {
        mockMvc.perform(put("/api/v1/profile/mobile/security/login-alerts?enable=true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.loginAlertsEnabled").value(true));
    }

    @Test
    @DisplayName("7. POST /api/v1/profile/mobile/security/sign-out-all terminates other sessions")
    void testSignOutAll() throws Exception {
        mockMvc.perform(post("/api/v1/profile/mobile/security/sign-out-all")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("8. GET /api/v1/profile/mobile/devices returns active devices")
    void testGetActiveDevices() throws Exception {
        mockMvc.perform(get("/api/v1/profile/mobile/devices")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalDevices").isNumber())
                .andExpect(jsonPath("$.data.devices").isArray());
    }

    @Test
    @DisplayName("9. DELETE /api/v1/profile/mobile/devices/dev-macbook-14 revokes a device")
    void testRevokeDevice() throws Exception {
        mockMvc.perform(delete("/api/v1/profile/mobile/devices/dev-macbook-14")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("10. GET /api/v1/profile/mobile/documents returns categorized documents")
    void testGetDocuments() throws Exception {
        mockMvc.perform(get("/api/v1/profile/mobile/documents")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.groups").isArray())
                .andExpect(jsonPath("$.data.groups[0].categoryKey").value("KYC"));
    }

    @Test
    @DisplayName("11. GET /api/v1/profile/mobile/settings returns app settings")
    void testGetSettings() throws Exception {
        mockMvc.perform(get("/api/v1/profile/mobile/settings")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.activeTheme").isNotEmpty())
                .andExpect(jsonPath("$.data.language").value("English"));
    }

    @Test
    @DisplayName("12. PUT /api/v1/profile/mobile/settings updates settings")
    void testUpdateSettings() throws Exception {
        MobileSettingsUpdateRequestDto req = MobileSettingsUpdateRequestDto.builder()
                .activeTheme("Dark")
                .tradeAlerts(true)
                .build();

        mockMvc.perform(put("/api/v1/profile/mobile/settings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.activeTheme").value("Dark"));
    }

    @Test
    @DisplayName("13. GET /api/v1/profile/mobile/notifications returns notification center items")
    void testGetNotifications() throws Exception {
        mockMvc.perform(get("/api/v1/profile/mobile/notifications")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.notifications").isArray());
    }
}
