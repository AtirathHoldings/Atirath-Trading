package com.atirath.trading.modules.auth.dto.biometric;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Biometric status for the current user and device")
public class BiometricStatusResponse {

    private boolean isEnrolled;
    private String deviceId;
    private String deviceName;
    private OffsetDateTime enrolledAt;
    private OffsetDateTime lastUsedAt;
}
