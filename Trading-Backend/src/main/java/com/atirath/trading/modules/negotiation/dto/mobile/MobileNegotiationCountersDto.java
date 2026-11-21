package com.atirath.trading.modules.negotiation.dto.mobile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MobileNegotiationCountersDto {
    private int totalCount;
    private int activeCount;
    private int awaitingResponseCount;
    private int acceptedCount;
    private int rejectedCount;
}
