package com.atirath.trading.modules.negotiation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NegotiationListItemDto {
    private String negotiationNo;
    private String companyName;
    private String companySymbol;
    private String companyLogoUrl;
    private String type; // BUY, SELL
    private String counterpartyName;
    private String counterpartyMemberId;
    private Long shares;
    private BigDecimal currentPrice;
    private BigDecimal proposedPrice;
    private String status; // Awaiting Response, Active, Under Negotiation, Accepted, Rejected, Expired
    private String lastUpdated;
}
