package com.atirath.trading;

import com.atirath.trading.modules.escrow.dto.mobile.MobilePaymentProceedRequestDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "sairam.v@atirathgroup.com")
class MobileEscrowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("1. GET /api/v1/escrow/mobile/dashboard returns 2x2 KPIs and active escrows")
    void testGetDashboard() throws Exception {
        mockMvc.perform(get("/api/v1/escrow/mobile/dashboard")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.activeEscrow").isNumber())
                .andExpect(jsonPath("$.data.activeEscrows").isArray());
    }

    @Test
    @DisplayName("2. GET /api/v1/escrow/mobile/ESC-2024-000312/details returns escrow details and 7-step timeline")
    void testGetEscrowDetails() throws Exception {
        mockMvc.perform(get("/api/v1/escrow/mobile/ESC-2024-000312/details")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").isNotEmpty())
                .andExpect(jsonPath("$.data.amount").isNumber())
                .andExpect(jsonPath("$.data.timeline").isArray());
    }

    @Test
    @DisplayName("3. GET /api/v1/escrow/mobile/ESC-2024-000312/fund-form returns payment methods")
    void testGetFundForm() throws Exception {
        mockMvc.perform(get("/api/v1/escrow/mobile/ESC-2024-000312/fund-form")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.amountToFund").isNumber())
                .andExpect(jsonPath("$.data.supportedPaymentMethods").isArray());
    }

    @Test
    @DisplayName("4. POST /api/v1/escrow/mobile/ESC-2024-000312/proceed-payment returns payment confirmation")
    void testProceedPayment() throws Exception {
        MobilePaymentProceedRequestDto req = MobilePaymentProceedRequestDto.builder()
                .paymentMethod("upi")
                .upiId("rajesh@hdfcbank")
                .build();

        mockMvc.perform(post("/api/v1/escrow/mobile/ESC-2024-000312/proceed-payment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.paymentStatus").value("SUCCESS"))
                .andExpect(jsonPath("$.data.transactionRef").isNotEmpty());
    }

    @Test
    @DisplayName("5. GET /api/v1/escrow/mobile/ESC-2024-000312/share-lock-status returns lock reference")
    void testGetShareLockStatus() throws Exception {
        mockMvc.perform(get("/api/v1/escrow/mobile/ESC-2024-000312/share-lock-status")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.lockReference").isNotEmpty())
                .andExpect(jsonPath("$.data.isLocked").value(true));
    }

    @Test
    @DisplayName("6. GET /api/v1/escrow/mobile/ESC-2024-000312/share-transfer-details returns transfer ledger")
    void testGetShareTransfer() throws Exception {
        mockMvc.perform(get("/api/v1/escrow/mobile/ESC-2024-000312/share-transfer-details")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.transferId").isNotEmpty())
                .andExpect(jsonPath("$.data.fromName").isNotEmpty())
                .andExpect(jsonPath("$.data.toName").isNotEmpty());
    }

    @Test
    @DisplayName("7. GET /api/v1/escrow/mobile/ESC-2024-000312/transfer-status returns transfer lifecycle")
    void testGetTransferStatus() throws Exception {
        mockMvc.perform(get("/api/v1/escrow/mobile/ESC-2024-000312/transfer-status")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.lifecycleSteps").isArray());
    }

    @Test
    @DisplayName("8. GET /api/v1/escrow/mobile/ESC-2024-000312/status returns status hero and timeline")
    void testGetEscrowStatus() throws Exception {
        mockMvc.perform(get("/api/v1/escrow/mobile/ESC-2024-000312/status")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.statusHistory").isArray());
    }

    @Test
    @DisplayName("9. GET /api/v1/escrow/mobile/ESC-2024-000312/status-view returns status detail breakdown")
    void testGetEscrowStatusDetail() throws Exception {
        mockMvc.perform(get("/api/v1/escrow/mobile/ESC-2024-000312/status-view")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.depositoryReference").isNotEmpty());
    }

    @Test
    @DisplayName("10. GET /api/v1/escrow/mobile/settlement/confirmation returns settlement celebration")
    void testGetSettlementConfirmation() throws Exception {
        mockMvc.perform(get("/api/v1/escrow/mobile/settlement/confirmation")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.celebrationTitle").isNotEmpty())
                .andExpect(jsonPath("$.data.settlementReferenceNo").isNotEmpty());
    }
}
