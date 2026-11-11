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
@Schema(description = "Notification delivery channels matching Image 2")
public class NotificationChannelsDto {

    @Schema(description = "In-App Notifications", example = "true")
    private Boolean inApp;

    @Schema(description = "Email Notifications", example = "true")
    private Boolean email;

    @Schema(description = "SMS Notifications", example = "false")
    private Boolean sms;
}
