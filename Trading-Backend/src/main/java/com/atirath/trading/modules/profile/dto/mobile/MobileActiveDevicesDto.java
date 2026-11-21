package com.atirath.trading.modules.profile.dto.mobile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MobileActiveDevicesDto {
    private int totalDevices;
    private String infoText;
    private List<MobileDeviceItemDto> devices;
}
