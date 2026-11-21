package com.atirath.trading.modules.negotiation.dto.mobile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Mobile Screen 5: Trade Agreement
 * Delivers agreement details, sign status, and prompt to proceed to Escrow.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MobileTradeAgreementDto {
    private String agreementNo;
    private String negotiationNo;
    private String status; // AGREEMENT_READY, CONFIRMED, IN_ESCROW, COMPLETED
    private String statusBannerTitle;
    private String statusBannerSubtitle;

    // Agreement Data Rows
    private String buyerName;
    private String buyerRole;
    private String sellerName;
    private String sellerRole;
    private String companyName;
    private String companySymbol;
    private Long quantity;
    private String quantityFormatted;
    private BigDecimal agreedPrice;
    private String agreedPriceFormatted;
    private BigDecimal totalValue;
    private String totalValueFormatted;
    private String settlementMode;
    private String escrowAccountRef;
    private String legalDisclaimer;

    // Action Flags
    private boolean canSignAndProceed;
    private boolean isSignedByMe;
    private boolean isSignedByCounterparty;
    private String nextScreen; // e.g. "ESCROW_SCREEN"
    private String pdfDownloadUrl;
}
