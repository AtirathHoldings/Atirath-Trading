package com.atirath.trading.modules.escrow.dto.mobile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Mobile DTO for Settlement Confirmation Screen (SettlementConfirmationScreen)
 * and FundEscrow - Settlement Completed Screen.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MobileSettlementConfirmationDto implements Serializable {

    private String settlementId;
    private String escrowId;
    private String tradeId;
    private String agreementId;
    private String company;
    private String companyFullName;
    private Long shares;
    private BigDecimal settlementAmount;
    private String settlementAmountFormatted;

    // Delivery summary
    private String buyerName;
    private String buyerMemberId;
    private String buyerDeliveryMessage; // e.g. "30 ARE Shares credited to demat"

    private String sellerName;
    private String sellerMemberId;
    private String sellerPaymentMessage; // e.g. "₹1,01,850 credited to primary bank"

    private String settlementDateFormatted;
    private String settlementReferenceNo;
    private String status;
    private String statusBadge;

    // Celebration header
    private String celebrationTitle;
    private String celebrationSubtitle;
}
