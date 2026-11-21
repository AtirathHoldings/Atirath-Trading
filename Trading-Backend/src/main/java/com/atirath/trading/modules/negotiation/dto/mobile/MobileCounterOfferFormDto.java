package com.atirath.trading.modules.negotiation.dto.mobile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Mobile Screen 3: Counter Offer Sheet / Screen
 * Pre-populates the Counter Price stepper, fixed quantity, bounds, and terms.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MobileCounterOfferFormDto {
    private String negotiationNo;
    private String companySymbol;
    private String companyName;

    // Price Bounds for Stepper
    private BigDecimal currentOfferPrice;
    private BigDecimal defaultCounterPrice;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private BigDecimal priceStep;

    // Quantity & Terms
    private Long quantity;
    private String quantityFormatted;
    private String counterpartyName;
    private String counterpartyMemberId;
    private List<String> allowedSettlementTerms;
    private String defaultSettlementTerm;
    private String escrowProtectionNote;
}
