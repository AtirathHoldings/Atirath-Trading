package com.atirath.trading.modules.auth.dto.device;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DisconnectAndLoginRequestDto {
    @NotBlank(message = "Device ID to disconnect is required")
    private String deviceIdToDisconnect;

    private String resolutionToken;
    private String currentDeviceId;
    private String currentDeviceName;
    private String currentDeviceType; // WEB or MOBILE
    private String identifier;
    private String password;
}
