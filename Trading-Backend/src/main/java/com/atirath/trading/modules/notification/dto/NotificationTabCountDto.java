package com.atirath.trading.modules.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationTabCountDto {
    private String key;       // all, trade, negotiation, escrowPayment, shareTransfer, settlement, portfolio, system
    private String label;     // All, Trade, Negotiation, etc.
    private Long count;       // 128, 28, 18, 16, 12, 10, 22, 22
}
