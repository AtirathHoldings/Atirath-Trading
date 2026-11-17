package com.atirath.trading.modules.notification.dto.mobile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MobileNotificationItemDto {
    private Long id;
    private String type;         // "trade", "negotiation", "escrow", "portfolio", "system"
    private String title;
    private String body;
    private String time;         // "2 min ago", "1 hr ago", "Yesterday"
    private Boolean read;
    private String createdAt;
    private Long refId;
    private String refTyp;
    private Object details;      // optional trade/order details map
}
