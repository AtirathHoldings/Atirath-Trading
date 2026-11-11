package com.atirath.trading.modules.auth.controller;

import com.atirath.trading.common.ApiResponse;
import com.atirath.trading.common.DocumentType;
import com.atirath.trading.config.JwtTokenProvider;
import com.atirath.trading.modules.auth.dto.*;
import com.atirath.trading.modules.auth.dto.common.AuthResponse;
import com.atirath.trading.modules.auth.dto.common.LogoutRequest;
import com.atirath.trading.modules.auth.dto.common.RefreshTokenRequest;
import com.atirath.trading.modules.auth.service.AuthService;
import com.atirath.trading.modules.auth.service.OtpService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/auth/web")
@RequiredArgsConstructor
@Tag(name = "Web Authentication & Portal", description = "Endpoints tailored for Web Portal with comprehensive data payloads")
public class WebAuthController {

    private final AuthService authService;
    private final OtpService otpService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/register/individual")
    @Operation(summary = "Web: Register Individual Account", description = "Creates Individual account and returns comprehensive Web Auth payload")
    public ResponseEntity<ApiResponse<AuthResponse>> registerIndividual(
            @Valid @RequestBody IndividualRegisterRequest request) {
        AuthResponse response = authService.registerIndividual(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Individual account registered successfully"));
    }

    @PostMapping("/register/corporate")
    @Operation(summary = "Web: Register Corporate Account", description = "Creates Corporate account and returns comprehensive Web Auth payload")
    public ResponseEntity<ApiResponse<AuthResponse>> registerCorporate(
            @Valid @RequestBody CorporateRegisterRequest request) {
        AuthResponse response = authService.registerCorporate(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Corporate account registered successfully"));
    }

    @PostMapping("/login")
    @Operation(summary = "Web: Login with Email/Mobile + Password", description = "Web portal login returning full user data and tokens")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Web login successful"));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Web: Forgot Password Email Link", description = "Generates secure email reset token for web users")
    public ResponseEntity<ApiResponse<ForgotPasswordResponse>> forgotPassword(
            @Valid @RequestBody ForgotPasswordEmailRequest request) {
        ForgotPasswordResponse response = authService.initiateForgotPasswordEmail(request);
        return ResponseEntity.ok(ApiResponse.success(response, response.getMessage()));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Web: Reset Password with Token", description = "Updates password using the email reset token")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordWithTokenRequest request) {
        authService.resetPasswordWithToken(request);
        return ResponseEntity.ok(ApiResponse.success(null, "Password reset successfully"));
    }

    @PostMapping("/forgot-password/mobile")
    @Operation(summary = "Web: Reset Password for Mobile (Verified OTP)", description = "Updates password for registered mobile number after verifying SMS OTP via /otp/verify")
    public ResponseEntity<ApiResponse<Void>> forgotPasswordMobile(
            @Valid @RequestBody ForgotPasswordMobileRequest request) {
        authService.resetPasswordForMobileVerified(request);
        return ResponseEntity.ok(ApiResponse.success(null, "Password updated successfully for mobile number."));
    }

    @PostMapping("/logout")
    @Operation(summary = "Web: Logout & Revoke Tokens", description = "Revokes current access and refresh tokens")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody(required = false) LogoutRequest request) {
        authService.logout(authHeader, request != null ? request.getRefreshToken() : null);
        return ResponseEntity.ok(ApiResponse.success(null, "Logged out successfully"));
    }

    @PostMapping(value = "/documents/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Web: Upload KYC Document File", description = "Uploads actual PDF/Image KYC document or Live Photo and saves metadata",
            security = @SecurityRequirement(name = "BearerAuthentication"))
    public ResponseEntity<ApiResponse<UserProfileDto.DocumentSummaryDto>> uploadDocumentFile(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam("documentType") DocumentType documentType,
            @RequestPart("file") MultipartFile file) {
        String token = authHeader.replace("Bearer ", "").trim();
        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        UserProfileDto.DocumentSummaryDto doc = authService.uploadDocumentFile(userId, documentType, file);
        return ResponseEntity.ok(ApiResponse.success(doc, "Document uploaded successfully"));
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "Web: Refresh JWT Access Token", description = "Exchanges refresh token for new access token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Token refreshed successfully"));
    }

    @PostMapping("/otp/send")
    @Operation(summary = "Web: Send Email/SMS OTP via Gateway", description = "Generates and dispatches OTP to email or phone number")
    public ResponseEntity<ApiResponse<OtpResponse>> sendOtp(
            @Valid @RequestBody OtpSendRequest request) {
        OtpResponse response = otpService.sendOtp(request);
        return ResponseEntity.ok(ApiResponse.success(response, response.getMessage()));
    }

    @PostMapping("/otp/verify")
    @Operation(summary = "Web: Verify Email/SMS OTP", description = "Verifies the 6-digit OTP code")
    public ResponseEntity<ApiResponse<Boolean>> verifyOtp(
            @Valid @RequestBody OtpVerifyRequest request) {
        boolean verified = otpService.verifyOtp(request);
        return ResponseEntity.ok(ApiResponse.success(verified, "OTP verified successfully"));
    }

    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Web: Get Comprehensive Profile", description = "Returns full profile including Account, Individual/Corporate, Masked KYC, Masked Bank details, and Document list",
            security = @SecurityRequirement(name = "BearerAuthentication"))
    public ResponseEntity<ApiResponse<UserProfileDto>> getProfile(
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "").trim();
        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        UserProfileDto profile = authService.getUserProfile(userId);
        return ResponseEntity.ok(ApiResponse.success(profile, "Profile retrieved successfully"));
    }
}
