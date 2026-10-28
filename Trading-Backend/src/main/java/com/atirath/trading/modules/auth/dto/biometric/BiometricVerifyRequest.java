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
@Schema(description = "Request to verify biometric signature against challenge nonce")
public class BiometricVerifyRequest {

    @NotBlank(message = "Device ID is required")
    @Schema(description = "Device ID", example = "device_987a6c5b-1234-4a5b")
    private String deviceId;

    @NotBlank(message = "Challenge is required")
    @Schema(description = "Challenge string received from /biometric/initiate", example = "e5b8d234a98f12cd764512bc9834abcd...")
    private String challenge;

    @NotBlank(message = "Signature is required")
    @Schema(description = "Base64 encoded cryptographic signature of the challenge created by the Secure Keystore private key",
            example = "MEUCIQDf8...signature...")
    private String signature;

    @Schema(description = "Optional login identifier (email/phone)", example = "9876543210")
    private String loginIdentifier;
}
