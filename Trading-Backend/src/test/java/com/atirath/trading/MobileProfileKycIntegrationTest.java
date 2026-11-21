package com.atirath.trading;

import com.atirath.trading.modules.profile.dto.mobile.MobileKycSubmitRequestDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "sairam.v@atirathgroup.com")
class MobileProfileKycIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("1. GET /api/v1/profile/mobile/overview returns compact profile screen DTO")
    void testGetMobileProfile() throws Exception {
        mockMvc.perform(get("/api/v1/profile/mobile/overview")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fullName").isNotEmpty())
                .andExpect(jsonPath("$.data.memberId").isNotEmpty())
                .andExpect(jsonPath("$.data.kycStatus").isNotEmpty());
    }

    @Test
    @DisplayName("2. GET /api/v1/profile/mobile/kyc returns 3-step KYC verification details")
    void testGetMobileKyc() throws Exception {
        mockMvc.perform(get("/api/v1/profile/mobile/kyc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.statusBadgeTitle").isNotEmpty())
                .andExpect(jsonPath("$.data.panStatus").isNotEmpty())
                .andExpect(jsonPath("$.data.internalDpId").isNotEmpty());
    }

    @Test
    @DisplayName("3. POST /api/v1/profile/mobile/kyc successfully submits KYC to database")
    void testSubmitMobileKyc() throws Exception {
        MobileKycSubmitRequestDto request = MobileKycSubmitRequestDto.builder()
                .panNumber("ABCDE1234F")
                .panFullName("Sairam V")
                .aadhaarNumber("123456789012")
                .dateOfBirth("1995-05-15")
                .bankName("State Bank of India")
                .accountNumber("1234567890")
                .ifscCode("SBIN0001234")
                .build();

        mockMvc.perform(post("/api/v1/profile/mobile/kyc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.panNumberMasked").value("ABCDE****F"))
                .andExpect(jsonPath("$.data.bankName").value("State Bank of India"))
                .andExpect(jsonPath("$.data.ifscCode").value("SBIN0001234"));
    }
}
