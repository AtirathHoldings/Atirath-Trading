package com.atirath.trading.modules.profile.dto.mobile;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MobileDeviceItemDto {
    private String deviceId;
    private String name;
    private String os;
    private String lastActive;
    private String deviceType; // MOBILE, DESKTOP, TABLET

    @JsonProperty("isCurrent")
    private boolean isCurrent;

    private String location;
    private String ipAddress;
}
