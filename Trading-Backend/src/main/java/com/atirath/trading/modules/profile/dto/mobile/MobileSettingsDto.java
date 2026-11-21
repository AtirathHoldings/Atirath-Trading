package com.atirath.trading.modules.profile.dto.mobile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MobileSettingsDto {
    private String activeTheme; // Light, Dark, Auto
    private String language;
    private String currency;
    private boolean tradeAlerts;
    private boolean negotiationUpdates;
    private boolean escrowAlerts;
    private boolean portfolioAlerts;
    private boolean marketUpdates;
    private boolean securityAlerts;
    private String appVersion;
}
