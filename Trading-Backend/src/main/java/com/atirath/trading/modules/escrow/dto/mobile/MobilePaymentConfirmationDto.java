package com.atirath.trading.modules.escrow.dto.mobile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Mobile-specific DTO for FundEscrow - Payment Confirmation Screen.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MobilePaymentConfirmationDto implements Serializable {

    private String escrowId;
    private String transactionRef;
    private String paymentStatus; // "SUCCESS", "PENDING_VERIFICATION"
    private String statusBadge;
    private BigDecimal amountPaid;
    private String amountPaidFormatted;
    private String paymentMethod;
    private String paymentGateway;
    private String paidAtFormatted;
    private String nextStepTitle;
    private String nextStepDescription;
}
