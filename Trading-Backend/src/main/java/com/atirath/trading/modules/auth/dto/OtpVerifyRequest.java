package com.atirath.trading.modules.auth.dto;

import com.atirath.trading.common.OtpPurpose;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpVerifyRequest {

    @NotBlank(message = "Target identifier (Email or Mobile Number) is required")
    @Schema(description = "User's Email address or Mobile Number", example = "9876543210")
    private String target;

    @NotBlank(message = "OTP code is required")
    @Schema(description = "6-digit verification OTP", example = "123456")
    private String otpCode;

    @NotNull(message = "OTP purpose is required")
    @Schema(description = "Purpose: LOGIN, REGISTRATION, PASSWORD_RESET, TRANSACTION", example = "LOGIN")
    private OtpPurpose purpose;
}
