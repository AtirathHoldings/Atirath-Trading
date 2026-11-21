package com.atirath.trading.modules.negotiation.dto.mobile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Mobile Screen 4: Negotiation History
 * Delivers chronological offers timeline, price rounds, and counterparty communications.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MobileNegotiationHistoryDto {
    private String negotiationNo;
    private String companySymbol;
    private String companyName;
    private String currentStatus;
    private List<MobileTimelineEventDto> events;
}
