package com.atirath.trading.modules.auth.dto.biometric;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response containing cryptographic challenge nonce to sign with Biometric private key")
public class BiometricInitiateResponse {

    @Schema(description = "Cryptographically secure random challenge nonce to be signed", example = "e5b8d234a98f12cd764512bc9834abcd...")
    private String challenge;

    @Schema(description = "Challenge expiration in seconds", example = "60")
    @Builder.Default
    private long expiresInSeconds = 60;

    @Schema(description = "Registered device identifier", example = "device_987a6c5b-1234-4a5b")
    private String deviceId;
}
