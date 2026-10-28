package com.atirath.trading.modules.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreferencesResponseDto {
    private List<NotificationChannelStatusDto> channelStatuses;
    private List<NotificationPreferenceRowDto> preferenceRows;
    private List<FrequencyOptionDto> frequencyOptions;
    private QuietHoursDto quietHours;
}
