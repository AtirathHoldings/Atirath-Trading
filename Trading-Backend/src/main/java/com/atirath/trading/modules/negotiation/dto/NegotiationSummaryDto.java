package com.atirath.trading.modules.negotiation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NegotiationSummaryDto {
    private int allNegotiations;
    private int active;
    private int awaitingResponse;
    private int accepted;
    private int rejected;
    private int expired;
}
