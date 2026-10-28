package com.atirath.trading.modules.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuietHoursDto {
    private Boolean enabled;
    private String label;
    private String fromTime; // e.g. "10:00 PM"
    private String toTime;   // e.g. "06:00 AM"
}
