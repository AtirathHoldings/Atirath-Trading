package com.atirath.trading.modules.market.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderListItemDto {

    @Schema(description = "Order ID", example = "101")
    private Long orderId;

    @Schema(description = "Order Reference Number", example = "BUY25091814322")
    private String orderNo;

    @Schema(description = "Company Name", example = "Atirath Holdings India Limited")
    private String companyName;

    @Schema(description = "Trading Symbol", example = "ATG-H")
    private String symbol;

    @Schema(description = "Order Side", example = "BUY")
    private String orderSide;

    @Schema(description = "Order Type", example = "LIMIT")
    private String orderType;

    @Schema(description = "Quantity", example = "1000")
    private Long quantity;

    @Schema(description = "Price per share", example = "242.00")
    private BigDecimal pricePerShare;

    @Schema(description = "Order Value", example = "242000.00")
    private BigDecimal orderValue;

    @Schema(description = "Validity", example = "DAY_ORDER")
    private String validity;

    @Schema(description = "Order Status", example = "PENDING")
    private String status;

    @Schema(description = "Order placed timestamp")
    private OffsetDateTime placedAt;

    @Schema(description = "Breakdown of order charges")
    private List<OrderChargeSummaryDto> charges;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderChargeSummaryDto {
        private String chargeType;
        private BigDecimal chargeAmount;
        private BigDecimal taxAmount;
    }
}
