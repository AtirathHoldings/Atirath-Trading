package com.atirath.trading.modules.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationSummaryKpiDto {
    private String id;
    private String title;
    private Long count;
    private String formattedCount;
    private String percentageOrChange; // e.g. "↑ 12%", "9.4% of total", "3.1% of total", "6.3% of total"
    private String subLabel;           // "vs. previous month"
    private String icon;
}
