package com.atirath.trading;

import com.atirath.trading.common.AccountType;
import com.atirath.trading.common.ApiResponse;
import com.atirath.trading.modules.auth.dto.*;
import com.atirath.trading.modules.auth.dto.common.AuthResponse;
import com.atirath.trading.modules.auth.dto.mobile.MobileAuthResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Web Portal Flow: Full Web Registration -> Full Web Login -> KYC -> Comprehensive Profile")
    void testWebPortalFlow() throws Exception {
        String testEmail = "web.investor." + System.currentTimeMillis() + "@test.com";
        String testMobile = "9" + (System.currentTimeMillis() % 1000000000L);

        // 1. Web Register Individual
        IndividualRegisterRequest regRequest = IndividualRegisterRequest.builder()
                .fullName("Web Investor")
                .email(testEmail)
                .mobileNumber(testMobile)
                .password("Password@123")
                .referralCode("WEB_REF_01")
                .build();

        MvcResult regResult = mockMvc.perform(post("/api/v1/auth/web/register/individual")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.user.email").value(testEmail))
                .andReturn();

        ApiResponse<AuthResponse> regResponse = objectMapper.readValue(
                regResult.getResponse().getContentAsString(),
                new TypeReference<>() {}
        );
        String token = regResponse.getData().getAccessToken();
        assertNotNull(token);

        // 2. Submit KYC
        KycSubmitRequest kycRequest = KycSubmitRequest.builder()
                .panNumber("ABCDE1234F")
                .aadhaarNumber("123456789012")
                .panFullName("Web Investor")
                .dateOfBirth(LocalDate.of(1992, 6, 20))
                .build();

        mockMvc.perform(post("/api/v1/auth/kyc")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(kycRequest)))
                .andExpect(status().isOk());

        // 3. Web Get Comprehensive Profile
        mockMvc.perform(get("/api/v1/auth/web/profile")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value(testEmail))
                .andExpect(jsonPath("$.data.individualProfile.fullName").value("Web Investor"))
                .andExpect(jsonPath("$.data.kycDetails.panNumber").value("ABCDE1234F"))
                .andExpect(jsonPath("$.data.kycDetails.maskedAadhaarNumber").value("XXXX-XXXX-9012"));
    }

    @Test
    @DisplayName("Mobile App Flow: Lightweight Registration -> Mobile Login -> Compact Mobile Profile")
    void testMobileAppFlow() throws Exception {
        String testEmail = "mobile.user." + System.currentTimeMillis() + "@test.com";
        String testMobile = "8" + (System.currentTimeMillis() % 1000000000L);

        // 1. Mobile Register Individual (Lightweight Payload)
        IndividualRegisterRequest regRequest = IndividualRegisterRequest.builder()
                .fullName("Mobile Trader")
                .email(testEmail)
                .mobileNumber(testMobile)
                .password("Password@123")
                .build();

        MvcResult regResult = mockMvc.perform(post("/api/v1/auth/mobile/register/individual")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.user.displayName").value("Mobile Trader"))
                .andReturn();

        ApiResponse<MobileAuthResponse> regResponse = objectMapper.readValue(
                regResult.getResponse().getContentAsString(),
                new TypeReference<>() {}
        );
        String token = regResponse.getData().getAccessToken();

        // 2. Mobile Login (Phone number + Password)
        LoginRequest loginRequest = LoginRequest.builder()
                .identifier(testMobile)
                .password("Password@123")
                .build();

        mockMvc.perform(post("/api/v1/auth/mobile/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.user.mobileNumber").value(testMobile));

        // 3. Mobile Get Compact Profile
        mockMvc.perform(get("/api/v1/auth/mobile/profile")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.mobileNumber").value(testMobile))
                .andExpect(jsonPath("$.data.displayName").value("Mobile Trader"))
                .andExpect(jsonPath("$.data.kycCompleted").value(false))
                .andExpect(jsonPath("$.data.bankAdded").value(false));
    }

    @Test
    @DisplayName("Password Reset Flow: Email Token for Web and Mobile Reset")
    void testPasswordResetFlow() throws Exception {
        String testEmail = "reset." + System.currentTimeMillis() + "@test.com";
        String testMobile = "7" + (System.currentTimeMillis() % 1000000000L);

        // 1. Register User
        IndividualRegisterRequest regRequest = IndividualRegisterRequest.builder()
                .fullName("Reset User")
                .email(testEmail)
                .mobileNumber(testMobile)
                .password("OldPassword@123")
                .build();

        mockMvc.perform(post("/api/v1/auth/register/individual")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regRequest)))
                .andExpect(status().isOk());

        // 2. Web Forgot Password Request
        ForgotPasswordEmailRequest emailReq = ForgotPasswordEmailRequest.builder()
                .email(testEmail)
                .build();

        MvcResult resetInitResult = mockMvc.perform(post("/api/v1/auth/web/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emailReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.resetToken").isNotEmpty())
                .andReturn();

        ApiResponse<ForgotPasswordResponse> resetInitResponse = objectMapper.readValue(
                resetInitResult.getResponse().getContentAsString(),
                new TypeReference<>() {}
        );
        String rawToken = resetInitResponse.getData().getResetToken();

        // 3. Web Reset Password with Token
        ResetPasswordWithTokenRequest resetRequest = ResetPasswordWithTokenRequest.builder()
                .token(rawToken)
                .newPassword("NewBrandPassword@123")
                .confirmPassword("NewBrandPassword@123")
                .build();

        mockMvc.perform(post("/api/v1/auth/web/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resetRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 4. Mobile Password Reset Flow (After external SMS OTP)
        ForgotPasswordMobileRequest mobileResetReq = ForgotPasswordMobileRequest.builder()
                .mobileNumber(testMobile)
                .newPassword("MobileNewPassword@123")
                .confirmPassword("MobileNewPassword@123")
                .build();

        mockMvc.perform(post("/api/v1/auth/mobile/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mobileResetReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
