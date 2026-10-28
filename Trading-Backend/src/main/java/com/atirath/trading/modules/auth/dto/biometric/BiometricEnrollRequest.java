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
@Schema(description = "Request to enroll device biometric public key")
public class BiometricEnrollRequest {

    @NotBlank(message = "Device ID is required")
    @Schema(description = "Unique device identifier (e.g. UUID / Android ID / IDFV)", example = "device_987a6c5b-1234-4a5b")
    private String deviceId;

    @Schema(description = "Human-readable device model / name", example = "Pixel 8 Pro / iPhone 15")
    private String deviceName;

    @NotBlank(message = "Public key is required")
    @Schema(description = "Base64-encoded X.509 RSA/ECDSA public key generated in hardware Secure Enclave / Keystore",
            example = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA...")
    private String publicKey;
}
