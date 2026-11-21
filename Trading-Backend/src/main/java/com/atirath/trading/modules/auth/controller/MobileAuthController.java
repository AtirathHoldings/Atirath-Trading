package com.atirath.trading.modules.auth.controller;

import com.atirath.trading.common.ApiResponse;
import com.atirath.trading.config.JwtTokenProvider;
import com.atirath.trading.modules.auth.dto.*;
import com.atirath.trading.modules.auth.dto.biometric.*;
import com.atirath.trading.modules.auth.dto.common.RefreshTokenRequest;
import com.atirath.trading.modules.auth.dto.device.DisconnectAndLoginRequestDto;
import com.atirath.trading.modules.auth.dto.mobile.MobileAuthResponse;
import com.atirath.trading.modules.auth.dto.mobile.MobileProfileDto;
import com.atirath.trading.modules.auth.service.AuthService;
import com.atirath.trading.modules.auth.service.BiometricService;
import com.atirath.trading.modules.auth.service.OtpService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth/mobile")
@RequiredArgsConstructor
@Tag(name = "Mobile Authentication & App", description = "Lightweight endpoints optimized for Mobile App payload size & speed")
public class MobileAuthController {

    private final AuthService authService;
    private final OtpService otpService;
    private final BiometricService biometricService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/otp/send")
    @Operation(summary = "Mobile: Send SMS/Email OTP via Gateway", description = "Generates and sends 60-second OTP to mobile number or email")
    public ResponseEntity<ApiResponse<OtpResponse>> sendOtp(
            @Valid @RequestBody OtpSendRequest request) {
        OtpResponse response = otpService.sendOtp(request);
        return ResponseEntity.ok(ApiResponse.success(response, response.getMessage()));
    }

    @PostMapping("/otp/verify")
    @Operation(summary = "Mobile: Verify SMS/Email OTP", description = "Verifies the 6-digit OTP code")
    public ResponseEntity<ApiResponse<Boolean>> verifyOtp(
            @Valid @RequestBody OtpVerifyRequest request) {
        boolean verified = otpService.verifyOtp(request);
        return ResponseEntity.ok(ApiResponse.success(verified, "OTP verified successfully"));
    }

    @PostMapping("/register/individual")
    @Operation(summary = "Mobile: Register Individual Account", description = "Creates Individual account and returns lightweight Mobile Auth payload")
    public ResponseEntity<ApiResponse<MobileAuthResponse>> registerIndividual(
            @Valid @RequestBody IndividualRegisterRequest request) {
        MobileAuthResponse response = authService.registerIndividualMobile(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Account registered successfully"));
    }

    @PostMapping("/register/corporate")
    @Operation(summary = "Mobile: Register Corporate Account", description = "Creates Corporate account and returns lightweight Mobile Auth payload")
    public ResponseEntity<ApiResponse<MobileAuthResponse>> registerCorporate(
            @Valid @RequestBody CorporateRegisterRequest request) {
        MobileAuthResponse response = authService.registerCorporateMobile(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Corporate account registered successfully"));
    }

    @PostMapping("/login")
    @Operation(summary = "Mobile: Login with Phone/Email + Password", description = "Mobile login returning compact payload and tokens")
    public ResponseEntity<ApiResponse<MobileAuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        MobileAuthResponse response = authService.loginMobile(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
    }

    @PostMapping("/devices/disconnect-and-login")
    @Operation(summary = "Mobile: Disconnect active device and complete mobile login", description = "Revokes tokens of target device and completes login on mobile")
    public ResponseEntity<ApiResponse<MobileAuthResponse>> disconnectAndLoginMobile(
            @Valid @RequestBody DisconnectAndLoginRequestDto request) {
        MobileAuthResponse response = authService.disconnectAndLoginMobile(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Device disconnected and mobile login successful"));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Mobile: Reset Password (Post-SMS OTP)", description = "Updates password for mobile number after external SMS OTP verification")
    public ResponseEntity<ApiResponse<Void>> forgotPasswordMobile(
            @Valid @RequestBody ForgotPasswordMobileRequest request) {
        authService.resetPasswordForMobile(request);
        return ResponseEntity.ok(ApiResponse.success(null, "Password updated successfully for mobile number"));
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "Mobile: Refresh Access Token", description = "Refreshes mobile tokens")
    public ResponseEntity<ApiResponse<MobileAuthResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {
        MobileAuthResponse response = authService.refreshTokenMobile(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Token refreshed successfully"));
    }

    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Mobile: Get Compact Profile", description = "Returns fast, lightweight profile data tailored for mobile screen widgets",
            security = @SecurityRequirement(name = "BearerAuthentication"))
    public ResponseEntity<ApiResponse<MobileProfileDto>> getMobileProfile(
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "").trim();
        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        MobileProfileDto profile = authService.getMobileProfile(userId);
        return ResponseEntity.ok(ApiResponse.success(profile, "Profile retrieved successfully"));
    }

    // =========================================================================
    // BIOMETRIC AUTHENTICATION (FaceID / Fingerprint / Hardware Keystore)
    // =========================================================================

    @PostMapping("/biometric/enroll")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Mobile: Enroll Device Biometric Public Key", description = "Registers device public key generated in hardware Secure Enclave/Keystore",
            security = @SecurityRequirement(name = "BearerAuthentication"))
    public ResponseEntity<ApiResponse<com.atirath.trading.modules.auth.dto.biometric.BiometricStatusResponse>> enrollBiometric(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody com.atirath.trading.modules.auth.dto.biometric.BiometricEnrollRequest request) {
        String token = authHeader.replace("Bearer ", "").trim();
        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        com.atirath.trading.modules.auth.dto.biometric.BiometricStatusResponse response = biometricService.enrollBiometric(userId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Biometric credentials enrolled successfully"));
    }

    @PostMapping("/biometric/initiate")
    @Operation(summary = "Mobile: Initiate Biometric Challenge", description = "Generates a 60-second cryptographic challenge nonce to sign with native FaceID/Fingerprint")
    public ResponseEntity<ApiResponse<com.atirath.trading.modules.auth.dto.biometric.BiometricInitiateResponse>> initiateBiometric(
            @Valid @RequestBody com.atirath.trading.modules.auth.dto.biometric.BiometricInitiateRequest request) {
        com.atirath.trading.modules.auth.dto.biometric.BiometricInitiateResponse response = biometricService.initiateBiometric(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Biometric challenge generated"));
    }

    @PostMapping("/biometric/verify")
    @Operation(summary = "Mobile: Verify Biometric Signature & Login", description = "Verifies cryptographic signature against challenge nonce and issues JWT tokens")
    public ResponseEntity<ApiResponse<MobileAuthResponse>> verifyBiometric(
            @Valid @RequestBody com.atirath.trading.modules.auth.dto.biometric.BiometricVerifyRequest request) {
        MobileAuthResponse response = biometricService.verifyBiometric(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Biometric authentication successful"));
    }

    @GetMapping("/biometric/status")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Mobile: Get Biometric Enrollment Status", description = "Checks whether the given device is enrolled for the current authenticated user",
            security = @SecurityRequirement(name = "BearerAuthentication"))
    public ResponseEntity<ApiResponse<com.atirath.trading.modules.auth.dto.biometric.BiometricStatusResponse>> getBiometricStatus(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam("deviceId") String deviceId) {
        String token = authHeader.replace("Bearer ", "").trim();
        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        com.atirath.trading.modules.auth.dto.biometric.BiometricStatusResponse status = biometricService.getBiometricStatus(userId, deviceId);
        return ResponseEntity.ok(ApiResponse.success(status, "Biometric status retrieved"));
    }

    @DeleteMapping("/biometric/deactivate")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Mobile: Deactivate Device Biometric", description = "Deactivates biometric login for the current user and device",
            security = @SecurityRequirement(name = "BearerAuthentication"))
    public ResponseEntity<ApiResponse<Void>> deactivateBiometric(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam("deviceId") String deviceId) {
        String token = authHeader.replace("Bearer ", "").trim();
        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        biometricService.deactivateBiometric(userId, deviceId);
        return ResponseEntity.ok(ApiResponse.success(null, "Biometric deactivated successfully for this device"));
    }
}
