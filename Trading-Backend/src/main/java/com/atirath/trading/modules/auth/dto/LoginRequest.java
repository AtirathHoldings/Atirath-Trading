package com.atirath.trading.modules.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "Email or mobile number is required")
    @Schema(description = "Registered Email address or Mobile Number", example = "sairam.v@atirathgroup.com")
    private String identifier;

    @NotBlank(message = "Password is required")
    @Schema(description = "Account password", example = "Password@123")
    private String password;

    @Schema(description = "Optional client device ID (UUID/fingerprint)", example = "dev-web-chrome-01")
    private String deviceId;

    @Schema(description = "Optional client device name", example = "Chrome on Windows 11")
    private String deviceName;

    @Schema(description = "Optional client device type: WEB or MOBILE", example = "WEB")
    private String deviceType;
}
