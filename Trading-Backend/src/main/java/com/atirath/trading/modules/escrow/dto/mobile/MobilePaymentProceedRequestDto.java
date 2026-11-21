package com.atirath.trading.modules.escrow.dto.mobile;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Mobile request payload for proceeding with escrow payment.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MobilePaymentProceedRequestDto implements Serializable {

    @NotBlank(message = "Payment method is required")
    private String paymentMethod; // "upi", "netbanking", "debit"

    private String upiId;
    private String bankName;
    private String accountNumber;
}
