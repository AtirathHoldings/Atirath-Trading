package com.atirath.trading.modules.negotiation.dto.mobile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MobileTimelineEventDto {
    private String eventId;
    private String eventType; // OFFER, COUNTER_OFFER, MESSAGE, CALL_REQUEST, STATUS_CHANGE, AGREEMENT
    private String senderRole; // ME, COUNTERPARTY, SYSTEM
    private String senderName;
    private BigDecimal price;
    private String priceFormatted;
    private Long quantity;
    private BigDecimal totalValue;
    private String totalValueFormatted;
    private String messageText;
    private String status; // PENDING, REJECTED, ACCEPTED
    private String statusBadgeColor;
    private String timestamp;
    private String relativeTime;
}
