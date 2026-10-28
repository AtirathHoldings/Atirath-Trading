package com.atirath.trading.modules.negotiation.dto;

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
public class InitiateNegotiationRequestDto {

    @NotBlank(message = "Company symbol is required")
    private String companySymbol;

    @NotBlank(message = "Order side (BUY or SELL) is required")
    private String type;

    private String counterpartyMemberId;

    @NotNull(message = "Shares quantity is required")
    @Min(value = 1, message = "Shares must be at least 1")
    private Long shares;

    @NotNull(message = "Proposed price is required")
    @DecimalMin(value = "0.01", message = "Price must be positive")
    private BigDecimal proposedPrice;

    @Builder.Default
    private String settlementTimeline = "T+3 Days";

    @Builder.Default
    private String paymentMode = "Bank Transfer (Escrow)";

    private String message;
}
