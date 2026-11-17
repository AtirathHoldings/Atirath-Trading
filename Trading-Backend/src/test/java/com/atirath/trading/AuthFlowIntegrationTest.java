package com.atirath.trading;

import com.atirath.trading.common.AccountType;
import com.atirath.trading.common.ApiResponse;
import com.atirath.trading.common.OtpChannel;
import com.atirath.trading.common.OtpPurpose;
import com.atirath.trading.modules.auth.dto.*;
import com.atirath.trading.modules.auth.dto.common.AuthResponse;
import com.atirath.trading.modules.auth.dto.common.LogoutRequest;
import com.atirath.trading.modules.auth.dto.common.RefreshTokenRequest;
import com.atirath.trading.modules.auth.dto.mobile.MobileAuthResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
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

        // 3. Mobile OTP Send & Verify must honor configured default-test-otp ("123456") in dev mode
        OtpSendRequest mobileOtpReq = OtpSendRequest.builder()
                .target(testMobile)
                .channel(OtpChannel.SMS)
                .purpose(OtpPurpose.LOGIN)
                .build();

        mockMvc.perform(post("/api/v1/auth/mobile/otp/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mobileOtpReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.devOtp").value("123456"))
                .andExpect(jsonPath("$.data.expiresInSeconds").value(60));

        OtpVerifyRequest mobileVerifyReq = OtpVerifyRequest.builder()
                .target(testMobile)
                .otpCode("123456")
                .purpose(OtpPurpose.LOGIN)
                .build();

        mockMvc.perform(post("/api/v1/auth/mobile/otp/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mobileVerifyReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(true));

        // 4. Mobile Get Compact Profile
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

    @Test
    @DisplayName("Web Auth Security & Rules: 60s OTP, Verified Web Mobile Reset, Multipart Upload, Logout, and 18+ Trading Age Restriction")
    void testWebAuthSecurityAndAgeRestriction() throws Exception {
        long suffix = System.currentTimeMillis() % 1000000000L;
        String minorEmail = "minor." + suffix + "@test.com";
        String minorMobile = "9" + String.format("%09d", suffix);

        // 1. Register Minor User
        IndividualRegisterRequest regReq = IndividualRegisterRequest.builder()
                .fullName("Minor User")
                .email(minorEmail)
                .mobileNumber(minorMobile)
                .password("Password@123")
                .build();

        MvcResult regRes = mockMvc.perform(post("/api/v1/auth/web/register/individual")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regReq)))
                .andExpect(status().isOk())
                .andReturn();

        ApiResponse<AuthResponse> regBody = objectMapper.readValue(
                regRes.getResponse().getContentAsString(),
                new TypeReference<>() {}
        );
        String accessToken = regBody.getData().getAccessToken();
        String refreshToken = regBody.getData().getRefreshToken();

        // 2. Web OTP expiration must be 60 seconds (1 min)
        OtpSendRequest otpReq = OtpSendRequest.builder()
                .target(minorEmail)
                .channel(OtpChannel.EMAIL)
                .purpose(OtpPurpose.LOGIN)
                .build();

        mockMvc.perform(post("/api/v1/auth/web/otp/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(otpReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.devOtp").value("123456"))
                .andExpect(jsonPath("$.data.expiresInSeconds").value(60));

        // 3. Submit KYC with DOB < 18 years ago
        KycSubmitRequest minorKyc = KycSubmitRequest.builder()
                .panNumber("ABCDE1234F")
                .aadhaarNumber("123456789012")
                .panFullName("Minor User")
                .dateOfBirth(LocalDate.now().minusYears(16))
                .build();

        mockMvc.perform(post("/api/v1/auth/kyc")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(minorKyc)))
                .andExpect(status().isOk());

        // Profile should report tradingEligible = false
        mockMvc.perform(get("/api/v1/auth/web/profile")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tradingEligible").value(false));

        // Under-18 user must be blocked from placing buy/sell share orders (403 Forbidden)
        Map<String, Object> buyOrderReq = Map.of(
                "symbol", "ATG-H",
                "orderSide", "BUY",
                "orderType", "LIMIT",
                "quantity", 10,
                "pricePerShare", new BigDecimal("242.00"),
                "validity", "Day Order",
                "acceptTerms", true
        );

        mockMvc.perform(post("/api/v1/trade/orders")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buyOrderReq)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));

        // 4. Multipart KYC document upload
        MockMultipartFile file = new MockMultipartFile(
                "file", "pan_card.pdf", "application/pdf", "dummy-pdf-bytes".getBytes());

        mockMvc.perform(multipart("/api/v1/auth/web/documents/upload")
                        .file(file)
                        .param("documentType", "IDENTITY_PROOF")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.documentName").value("pan_card.pdf"))
                .andExpect(jsonPath("$.data.documentType").value("IDENTITY_PROOF"));

        // 5. Web mobile forgot password must require prior OTP verification
        ForgotPasswordMobileRequest unverifiedReset = ForgotPasswordMobileRequest.builder()
                .mobileNumber(minorMobile)
                .newPassword("NewPassword@123")
                .confirmPassword("NewPassword@123")
                .build();

        mockMvc.perform(post("/api/v1/auth/web/forgot-password/mobile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(unverifiedReset)))
                .andExpect(status().isBadRequest());

        // After verifying PASSWORD_RESET OTP, web mobile password reset must succeed
        OtpSendRequest pwdOtpReq = OtpSendRequest.builder()
                .target(minorMobile)
                .channel(OtpChannel.SMS)
                .purpose(OtpPurpose.PASSWORD_RESET)
                .build();
        mockMvc.perform(post("/api/v1/auth/web/otp/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pwdOtpReq)))
                .andExpect(status().isOk());

        OtpVerifyRequest pwdVerifyReq = OtpVerifyRequest.builder()
                .target(minorMobile)
                .otpCode("123456")
                .purpose(OtpPurpose.PASSWORD_RESET)
                .build();
        mockMvc.perform(post("/api/v1/auth/web/otp/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pwdVerifyReq)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/auth/web/forgot-password/mobile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(unverifiedReset)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // Unregistered email or mobile must fail DB check before proceeding with password reset
        ForgotPasswordEmailRequest unknownEmailReset = ForgotPasswordEmailRequest.builder()
                .email("not.in.db." + suffix + "@test.com")
                .build();
        mockMvc.perform(post("/api/v1/auth/web/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(unknownEmailReset)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));

        OtpSendRequest unknownMobileResetOtp = OtpSendRequest.builder()
                .target("61" + String.format("%08d", suffix % 100000000L))
                .channel(OtpChannel.SMS)
                .purpose(OtpPurpose.PASSWORD_RESET)
                .build();
        mockMvc.perform(post("/api/v1/auth/web/otp/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(unknownMobileResetOtp)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));

        // 6. Logout revokes both access and refresh tokens
        mockMvc.perform(post("/api/v1/auth/web/logout")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LogoutRequest(refreshToken))))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/auth/web/profile")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/v1/auth/web/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RefreshTokenRequest(refreshToken))))
                .andExpect(status().isBadRequest());

        // 7. Adult (18+) user can buy/sell shares
        long adultSuffix = (System.currentTimeMillis() + 123) % 1000000000L;
        String adultEmail = "adult." + adultSuffix + "@test.com";
        String adultMobile = "8" + String.format("%09d", adultSuffix);

        IndividualRegisterRequest adultReg = IndividualRegisterRequest.builder()
                .fullName("Adult Trader")
                .email(adultEmail)
                .mobileNumber(adultMobile)
                .password("Password@123")
                .build();

        MvcResult adultRegRes = mockMvc.perform(post("/api/v1/auth/web/register/individual")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adultReg)))
                .andExpect(status().isOk())
                .andReturn();

        ApiResponse<AuthResponse> adultBody = objectMapper.readValue(
                adultRegRes.getResponse().getContentAsString(),
                new TypeReference<>() {}
        );
        String adultToken = adultBody.getData().getAccessToken();

        KycSubmitRequest adultKyc = KycSubmitRequest.builder()
                .panNumber("ABCDE1234F")
                .aadhaarNumber("123456789012")
                .panFullName("Adult Trader")
                .dateOfBirth(LocalDate.now().minusYears(18))
                .build();

        mockMvc.perform(post("/api/v1/auth/kyc")
                        .header("Authorization", "Bearer " + adultToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adultKyc)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/trade/orders")
                        .header("Authorization", "Bearer " + adultToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buyOrderReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
