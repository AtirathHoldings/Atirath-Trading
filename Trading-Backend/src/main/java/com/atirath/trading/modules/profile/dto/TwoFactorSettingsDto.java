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
@Schema(description = "Two-Factor Authentication settings matching Image 4")
public class TwoFactorSettingsDto {

    @Schema(description = "Overall 2FA enabled status", example = "true")
    private Boolean isEnabled;

    @Schema(description = "Authenticator App status", example = "Connected")
    private String authenticatorAppStatus;

    @Schema(description = "Authenticator App enabled", example = "true")
    private Boolean authenticatorAppEnabled;

    @Schema(description = "SMS Verification Phone", example = "+91 98765 43210")
    private String smsPhoneNumber;

    @Schema(description = "SMS Verification enabled", example = "false")
    private Boolean smsVerificationEnabled;

    @Schema(description = "Backup codes count remaining", example = "8")
    private Integer backupCodesCount;
}
