package com.atirath.trading.modules.auth.dto.device;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActiveDeviceSessionDto {
    private String deviceId;
    private String name;
    private String os;
    private String deviceType; // WEB, MOBILE, TABLET
    private String lastActive;
    private String location;
    private String ipAddress;

    @JsonProperty("isCurrent")
    private boolean isCurrent;

    private Instant loginTime;
}
