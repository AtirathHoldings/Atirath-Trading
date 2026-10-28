package com.atirath.trading.modules.market.dto.mobile;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Mobile-friendly request payload for placing buy/sell orders.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MobilePlaceOrderRequest {
    @NotBlank(message = "Stock symbol is required")
    private String symbol;

    @NotBlank(message = "Order side must be BUY or SELL")
    private String side;

    private String orderType; // LIMIT or MARKET (defaults to LIMIT)

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Long quantity;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price per share must be positive")
    private BigDecimal price;

    private String validity;    // DAY_ORDER, IOC, etc. (defaults to DAY_ORDER)
    private BigDecimal stopPrice; // Optional
    private Boolean acceptTerms;  // defaults to true
}
