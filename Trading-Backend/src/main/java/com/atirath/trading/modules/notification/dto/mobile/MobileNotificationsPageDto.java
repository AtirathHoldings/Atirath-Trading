package com.atirath.trading.modules.notification.dto.mobile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MobileNotificationsPageDto {
    private Integer totalCount;
    private Integer unreadCount;
    private Integer tradeCount;
    private Integer systemCount;
    private List<MobileNotificationItemDto> notifications;
}
