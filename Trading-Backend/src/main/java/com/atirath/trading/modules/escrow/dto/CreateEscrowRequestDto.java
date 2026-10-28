package com.atirath.trading.modules.escrow.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
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
public class CreateEscrowRequestDto {

    @NotBlank(message = "Trade ID is required")
    private String tradeId;

    private String negotiationId;

    private String agreementId;

    @NotBlank(message = "Company symbol is required")
    private String companySymbol;

    @NotBlank(message = "Company name is required")
    private String companyName;

    @NotBlank(message = "Buyer member ID is required")
    private String buyerMemberId;

    @NotBlank(message = "Seller member ID is required")
    private String sellerMemberId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Long quantity;

    @NotNull(message = "Price per share is required")
    @DecimalMin(value = "0.01", message = "Price per share must be greater than zero")
    private BigDecimal pricePerShare;

    private String paymentMode;

    private String expectedSettlement;
}
