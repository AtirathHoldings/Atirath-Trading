package com.atirath.trading.modules.auth.dto.device;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceLimitExceededDto {
    @Builder.Default
    private String errorCode = "DEVICE_LIMIT_EXCEEDED";

    @Builder.Default
    private int maxAllowed = 2;

    private String resolutionToken;
    private String message;
    private List<ActiveDeviceSessionDto> activeDevices;
}
