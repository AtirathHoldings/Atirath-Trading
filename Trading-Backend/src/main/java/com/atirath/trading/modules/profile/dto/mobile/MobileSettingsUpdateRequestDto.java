package com.atirath.trading.modules.profile.dto.mobile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MobileSettingsUpdateRequestDto {
    private String activeTheme;
    private Boolean tradeAlerts;
    private Boolean negotiationUpdates;
    private Boolean escrowAlerts;
    private Boolean portfolioAlerts;
    private Boolean marketUpdates;
    private Boolean securityAlerts;
}
