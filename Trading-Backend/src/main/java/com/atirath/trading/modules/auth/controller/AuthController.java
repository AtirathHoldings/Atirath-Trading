package com.atirath.trading.modules.auth.controller;

import com.atirath.trading.common.ApiResponse;
import com.atirath.trading.config.JwtTokenProvider;
import com.atirath.trading.modules.auth.dto.*;
import com.atirath.trading.modules.auth.dto.common.AuthResponse;
import com.atirath.trading.modules.auth.dto.common.RefreshTokenRequest;
import com.atirath.trading.modules.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication & Account Management", description = "Registration, Login, Password Reset, and Profile APIs")
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/register/individual")
    @Operation(summary = "Register Individual Account", description = "Creates a new Individual user account with profile and BCrypt password credentials")
    public ResponseEntity<ApiResponse<AuthResponse>> registerIndividual(
            @Valid @RequestBody IndividualRegisterRequest request) {
        AuthResponse response = authService.registerIndividual(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Individual account registered successfully"));
    }

    @PostMapping("/register/corporate")
    @Operation(summary = "Register Corporate Account", description = "Creates a new Corporate/Institution account with company details and BCrypt password credentials")
    public ResponseEntity<ApiResponse<AuthResponse>> registerCorporate(
            @Valid @RequestBody CorporateRegisterRequest request) {
        AuthResponse response = authService.registerCorporate(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Corporate account registered successfully"));
    }

    @PostMapping("/login")
    @Operation(summary = "Login with Email or Mobile Number", description = "Authenticates user using Email or Mobile Number + Password and returns JWT tokens")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
    }

    @PostMapping("/forgot-password/email")
    @Operation(summary = "Forgot Password via Email", description = "Generates secure password reset token and saves its SHA-256 hash in password_reset_tokens table")
    public ResponseEntity<ApiResponse<ForgotPasswordResponse>> forgotPasswordEmail(
            @Valid @RequestBody ForgotPasswordEmailRequest request) {
        ForgotPasswordResponse response = authService.initiateForgotPasswordEmail(request);
        return ResponseEntity.ok(ApiResponse.success(response, response.getMessage()));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset Password with Email Token", description = "Validates the reset token from email and updates the password in user_credentials")
    public ResponseEntity<ApiResponse<Void>> resetPasswordWithToken(
            @Valid @RequestBody ResetPasswordWithTokenRequest request) {
        authService.resetPasswordWithToken(request);
        return ResponseEntity.ok(ApiResponse.success(null, "Password reset successfully. You can now log in with your new password."));
    }

    @PostMapping("/forgot-password/mobile")
    @Operation(summary = "Reset Password for Mobile (Post-OTP)", description = "Updates password for registered mobile number after OTP verification by external provider")
    public ResponseEntity<ApiResponse<Void>> resetPasswordForMobile(
            @Valid @RequestBody ForgotPasswordMobileRequest request) {
        authService.resetPasswordForMobile(request);
        return ResponseEntity.ok(ApiResponse.success(null, "Password updated successfully for mobile number."));
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "Refresh JWT Access Token", description = "Exchange a valid Refresh Token for a new pair of JWT Access and Refresh tokens")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Token refreshed successfully"));
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get Authenticated User Profile", description = "Returns full profile including Account, Individual/Corporate, Masked KYC, and Masked Bank details",
            security = @SecurityRequirement(name = "BearerAuthentication"))
    public ResponseEntity<ApiResponse<UserProfileDto>> getProfile(
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "").trim();
        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        UserProfileDto profile = authService.getUserProfile(userId);
        return ResponseEntity.ok(ApiResponse.success(profile, "Profile retrieved successfully"));
    }

    @PostMapping("/kyc")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Submit Identity / KYC Details", description = "Saves PAN, DOB, and AES-encrypted Aadhaar number",
            security = @SecurityRequirement(name = "BearerAuthentication"))
    public ResponseEntity<ApiResponse<Void>> submitKyc(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody KycSubmitRequest request) {
        String token = authHeader.replace("Bearer ", "").trim();
        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        authService.submitKyc(userId, request);
        return ResponseEntity.ok(ApiResponse.success(null, "KYC details submitted successfully"));
    }

    @PostMapping("/bank-details")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Submit Bank Account Details", description = "Validates and saves AES-encrypted Bank Account details",
            security = @SecurityRequirement(name = "BearerAuthentication"))
    public ResponseEntity<ApiResponse<Void>> submitBankDetails(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody BankDetailsSubmitRequest request) {
        String token = authHeader.replace("Bearer ", "").trim();
        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        authService.submitBankDetails(userId, request);
        return ResponseEntity.ok(ApiResponse.success(null, "Bank details submitted successfully"));
    }

    @PostMapping("/documents")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Upload / Register Account Document", description = "Saves file metadata and secure storage path for Address Proof, ID Proof, Passbook, or Live Photo",
            security = @SecurityRequirement(name = "BearerAuthentication"))
    public ResponseEntity<ApiResponse<Void>> uploadDocument(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody DocumentUploadRequest request) {
        String token = authHeader.replace("Bearer ", "").trim();
        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        authService.uploadDocument(userId, request);
        return ResponseEntity.ok(ApiResponse.success(null, "Document registered successfully"));
    }
}
