package com.atirath.trading.modules.escrow.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferFundsRequestDto {

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1.00", message = "Amount must be greater than zero")
    private BigDecimal amount;

    @NotBlank(message = "Payment mode is required")
    private String paymentMode; // e.g. "Bank Transfer" or "UPI"

    private String transactionReference;

    private String utrNumber;

    private String remarks;
}
