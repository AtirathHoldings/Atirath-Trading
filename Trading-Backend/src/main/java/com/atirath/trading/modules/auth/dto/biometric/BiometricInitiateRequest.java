package com.atirath.trading.modules.auth.dto.biometric;

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
@Schema(description = "Request to initiate biometric challenge nonce")
public class BiometricInitiateRequest {

    @NotBlank(message = "Device ID is required")
    @Schema(description = "Unique device identifier", example = "device_987a6c5b-1234-4a5b")
    private String deviceId;

    @Schema(description = "Optional user phone number or email if known", example = "9876543210")
    private String loginIdentifier;
}
