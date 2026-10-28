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
public class OrderConfirmationDto {

    @Schema(description = "Primary Order ID", example = "101")
    private Long orderId;

    @Schema(description = "Unique Order Reference No", example = "BUY25091814322")
    private String orderReferenceNo;

    @Schema(description = "Order placement timestamp")
    private OffsetDateTime placedAt;

    @Schema(description = "Formatted placement date/time", example = "18 Sep 2025, 02:32 PM")
    private String placedAtFormatted;

    @Schema(description = "Current lifecycle status", example = "PENDING")
    private String status;

    // --- Company Information ---
    @Schema(description = "Company Name", example = "Atirath Holdings India Limited")
    private String companyName;

    @Schema(description = "Trading Symbol", example = "ATG-H")
    private String symbol;

    @Schema(description = "Sector", example = "Diversified")
    private String sector;

    @Schema(description = "Company Active Status", example = "Active")
    private String companyStatus;

    // --- Order Details ---
    @Schema(description = "Order side", example = "BUY")
    private String orderSide;

    @Schema(description = "Order Type description", example = "Buy (Limit Order)")
    private String orderTypeDescription;

    @Schema(description = "Quantity of shares", example = "1000")
    private Long quantity;

    @Schema(description = "Price per Share (₹)", example = "242.00")
    private BigDecimal pricePerShare;

    @Schema(description = "Order Value (₹)", example = "242000.00")
    private BigDecimal orderValue;

    @Schema(description = "Validity text", example = "Day Order (Valid till end of trading day)")
    private String validity;

    @Schema(description = "Stop Loss summary", example = "Not Set")
    private String stopLoss;

    @Schema(description = "Trigger Type", example = "-")
    private String triggerType;

    // --- Charges Summary ---
    @Schema(description = "Transaction Fee (0.1%)", example = "242.00")
    private BigDecimal transactionFee;

    @Schema(description = "GST (18%)", example = "43.56")
    private BigDecimal gstAmount;

    @Schema(description = "Total Charges", example = "285.56")
    private BigDecimal totalCharges;

    @Schema(description = "Total Amount Payable / Receivable", example = "242285.56")
    private BigDecimal totalAmount;

    // --- Post-Order Balances ---
    @Schema(description = "User available balance after order placement", example = "1006214.44")
    private BigDecimal remainingAvailableBalance;

    @Schema(description = "Total blocked in escrow for this order", example = "242285.56")
    private BigDecimal blockedEscrowAmount;

    // --- 6-Step Lifecycle Timeline ---
    @Schema(description = "What Happens Next? 6-step lifecycle workflow")
    private List<LifecycleTimelineStepDto> timeline;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LifecycleTimelineStepDto {
        @Schema(description = "Step number (1 to 6)", example = "1")
        private Integer stepNumber;

        @Schema(description = "Step Title", example = "Order Placed")
        private String title;

        @Schema(description = "Detailed explanation", example = "Your buy order has been submitted.")
        private String description;

        @Schema(description = "Status: COMPLETED, IN_PROGRESS, PENDING", example = "COMPLETED")
        private String status;

        @Schema(description = "Timestamp when step was completed/updated", example = "18 Sep 2025, 02:32 PM")
        private String timestamp;
    }
}
