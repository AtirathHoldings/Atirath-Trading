package com.atirath.trading.modules.escrow.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProceedPaymentRequestDto {

    @NotBlank(message = "Payment method is required (UPI, NET_BANKING, DEBIT_CARD)")
    private String paymentMethod; // UPI, NET_BANKING, DEBIT_CARD

    private String upiApp; // Google Pay, PhonePe, Paytm

    private String bankName; // HDFC, ICICI, SBI, etc.

    private String cardNumberLast4;
}
