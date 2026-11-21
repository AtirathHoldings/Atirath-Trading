package com.atirath.trading.modules.negotiation.dto.mobile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Mobile Screen 2: Negotiation Details
 * Delivers exact terms, CMP, counterpart details, and mobile action buttons.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MobileNegotiationDetailDto {
    private String negotiationNo;

    // Company Header
    private String companySymbol;
    private String companyName;
    private String companyLogoUrl;
    private String sector;
    private BigDecimal currentMarketPrice;
    private String cmpFormatted;
    private BigDecimal priceChange;
    private String priceChangePercent;

    // Status & Expiry
    private String userRole; // BUYER or SELLER
    private String side; // BUY or SELL
    private String status; // AWAITING_RESPONSE, ACTIVE, ACCEPTED, REJECTED, EXPIRED
    private String statusDisplay;
    private String statusColor;
    private String expiresIn;
    private String expiryDate;

    // Parties Info
    private String myPartyName;
    private String myMemberId;
    private String myRole;
    private String counterpartyName;
    private String counterpartyMemberId;
    private String counterpartyRole;
    private boolean counterpartyVerified;

    // Current Proposal
    private Long quantity;
    private String quantityFormatted;
    private BigDecimal currentOfferPrice;
    private String currentOfferPriceFormatted;
    private BigDecimal originalOfferPrice;
    private String originalOfferPriceFormatted;
    private BigDecimal totalValue;
    private String totalValueFormatted;
    private String settlementTerms;

    // Context & Offer state
    private boolean latestOfferByMe;
    private String latestOfferMessage;
    private int roundsCount;

    // Mobile Action Capabilities
    private boolean canCounterOffer;
    private boolean canAccept;
    private boolean canReject;
    private boolean canProceedToAgreement;
    private boolean canRequestCall;

    private String agreementNo;
}
