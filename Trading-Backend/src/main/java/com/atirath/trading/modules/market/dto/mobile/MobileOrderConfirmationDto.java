package com.atirath.trading.modules.market.dto.mobile;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Lightweight DTO specifically for Flutter order_confirmation_screen.dart.
 * Displays order reference, formatted summary, and 6-stage lifecycle stepper.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MobileOrderConfirmationDto {
    private Long orderId;
    private String orderNo;
    private String symbol;
    private String companyName;
    private String side;             // BUY or SELL
    private String orderTypeDisplay; // "Buy (Limit Order)"
    private Long quantity;
    private BigDecimal price;
    private BigDecimal orderValue;
    private BigDecimal totalCharges;
    private BigDecimal totalAmount;
    private String validity;
    private String placedAtFormatted;
    private String status;           // "IN_ESCROW" or "PENDING"
    private String statusBadge;      // "In Escrow"
    private String message;

    // 6-step lifecycle timeline for mobile "What Happens Next?"
    private List<MobileNextStepDto> nextSteps;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MobileNextStepDto {
        private Integer stepNumber;
        private String title;
        private String description;
        private String status;        // "COMPLETED", "IN_PROGRESS", "PENDING"
        private String timeFormatted;
    }
}
