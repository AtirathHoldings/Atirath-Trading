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
public class NotificationsResponseDto {
    private List<NotificationSummaryKpiDto> summaryKpis;
    private List<NotificationTabCountDto> tabCounts;
    private List<NotificationListItemDto> notifications;
    private NotificationDetailDto selectedNotificationDetail;
    private Long totalElements;
    private Integer totalPages;
    private Integer currentPage;
    private Integer pageSize;
}
