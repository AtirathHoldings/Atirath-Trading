package com.atirath.trading.modules.market.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
public class OrderPreviewRequest {

    @Schema(description = "Trading Symbol (e.g. ATG-H or AHIL)")
    private String symbol;

    @Schema(description = "Company ID (Optional if symbol provided)")
    private Long companyId;

    @NotBlank(message = "Order side is required (BUY or SELL)")
    @Schema(description = "BUY or SELL", example = "BUY")
    private String orderSide;

    @NotBlank(message = "Order type is required (LIMIT, MARKET, STOP_LOSS)")
    @Schema(description = "LIMIT, MARKET, STOP_LOSS", example = "LIMIT")
    private String orderType;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Schema(description = "Number of shares to trade", example = "1000")
    private Long quantity;

    @NotNull(message = "Price per share is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than zero")
    @Schema(description = "Limit price or expected market price", example = "242.00")
    private BigDecimal pricePerShare;

    @Schema(description = "Order validity (DAY_ORDER, IOC)", defaultValue = "DAY_ORDER")
    @Builder.Default
    private String validity = "DAY_ORDER";

    @Schema(description = "Whether stop loss is enabled", defaultValue = "false")
    @Builder.Default
    private Boolean stopLossEnabled = false;

    @Schema(description = "Stop price threshold if stop loss enabled")
    private BigDecimal stopPrice;

    @Schema(description = "Trigger condition type")
    private String triggerType;
}
