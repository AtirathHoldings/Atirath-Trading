package com.atirath.trading.modules.profile.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Platform preferences toggles matching Image 4")
public class PlatformPreferencesDto {

    @Schema(description = "Receive Email Notifications", example = "true")
    private Boolean receiveEmailNotifications;

    @Schema(description = "Receive SMS Notifications", example = "true")
    private Boolean receiveSmsNotifications;

    @Schema(description = "Marketing Communications", example = "false")
    private Boolean marketingCommunications;

    @Schema(description = "Show Portfolio Summary on Login", example = "true")
    private Boolean showPortfolioSummaryOnLogin;

    @Schema(description = "Auto Logout timeout option", example = "30 Minutes")
    private String autoLogout;
}
