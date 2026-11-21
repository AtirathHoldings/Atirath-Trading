package com.atirath.trading.modules.profile.dto.mobile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MobileSecurityDto {
    private String lastPasswordChange;
    private boolean biometricSupported;
    private boolean biometricEnabled;
    private boolean twoFactorEnabled;
    private String twoFactorSubtitle;
    private boolean loginAlertsEnabled;
    private String loginAlertsSubtitle;
    private int activeDevicesCount;
    private String activeDevicesSummary;
}
