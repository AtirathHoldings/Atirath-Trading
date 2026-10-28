package com.atirath.trading.modules.negotiation.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
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
public class CounterOfferRequestDto {

    @NotNull(message = "Price per share is required")
    @DecimalMin(value = "0.01", message = "Price per share must be positive")
    private BigDecimal pricePerShare;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Long quantity;

    @Builder.Default
    private String settlementTimeline = "T+3 Days";

    @Builder.Default
    private String paymentMode = "Bank Transfer (Escrow)";

    private String otherTerms;
}
