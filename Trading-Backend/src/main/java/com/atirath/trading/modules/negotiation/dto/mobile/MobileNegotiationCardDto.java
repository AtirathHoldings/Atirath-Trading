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
public class MobileNegotiationCardDto {
    private String negotiationNo;
    private String companySymbol;
    private String companyName;
    private String companyLogoUrl;
    private String type; // BUY or SELL
    private String counterpartyName;
    private String counterpartyMemberId;
    private Long shares;
    private BigDecimal proposedPrice;
    private String proposedPriceFormatted;
    private BigDecimal currentPrice;
    private String currentPriceFormatted;
    private BigDecimal totalValue;
    private String totalValueFormatted;
    private String status; // AWAITING_RESPONSE, ACTIVE, ACCEPTED, REJECTED, EXPIRED
    private String statusBadgeText;
    private String statusBadgeColor;
    private String lastUpdated;
    private String relativeTime;
    private boolean canAction;
    private boolean agreementReady;
}
