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
@Schema(description = "Security preference toggle switches matching Image 4")
public class SecurityPreferencesDto {

    @Schema(description = "Email notifications for new login", example = "true")
    private Boolean emailNotificationsForNewLogin;

    @Schema(description = "Login alerts for unknown devices", example = "true")
    private Boolean loginAlertsForUnknownDevices;

    @Schema(description = "Require 2FA for all logins", example = "true")
    private Boolean require2faForAllLogins;

    @Schema(description = "Auto logout for inactive sessions (30 mins)", example = "false")
    private Boolean autoLogoutForInactiveSessions;
}
