package com.atirath.trading.modules.market.dto.mobile;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Lightweight DTO specifically for Flutter order_lifecycle_screen.dart (Order Details & Status).
 * Provides a clean stepper progression, escrow summary, and recent activity timeline.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MobileOrderLifecycleDto {
    private Long orderId;
    private String orderNo;                  // e.g. "BUY25091814322"
    private String orderDateFormatted;       // "18 Sep 2025, 02:32 PM"
    private Long companyId;
    private String companyName;              // "Atirath Holdings India Limited"
    private String symbol;                   // "ATG-H"
    private String side;                     // "BUY"
    private String sideBadge;                // "Buy Order"
    private String orderTypeDisplay;         // "Buy (Limit Order)"
    private Long quantity;                   // 1000
    private BigDecimal price;                // 242.00
    private BigDecimal orderValue;           // 242000.00
    private BigDecimal totalAmount;          // 242285.56
    private String validity;                 // "Day Order"
    private String stopLoss;                 // "Not Set"

    // Current Status Card
    private String currentStatus;            // "IN_ESCROW"
    private String currentStatusTitle;       // "In Escrow"
    private String currentStatusDescription; // "Funds have been blocked in escrow. Awaiting approvals from the counterparty and admin."
    private String estimatedCompletion;      // "19 Sep 2025 (EOD)"

    // Mobile Stepper Widget support
    private Integer currentStepIndex;        // 4 (1-indexed)
    private Integer totalSteps;              // 7
    private List<MobileLifecycleStepDto> steps;

    // Escrow Block Box
    private MobileEscrowSummaryDto escrow;

    // Counterparty Profile Box (Concise)
    private String counterpartyName;         // "Rakesh Mehta"
    private String counterpartyType;         // "Internal Member"

    // Recent Activity Timeline
    private List<MobileLifecycleActivityDto> recentActivity;

    // Interactive Flags
    private Boolean canCancel;
    private Boolean canModify;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class MobileLifecycleStepDto {
        private Integer stepNumber;
        private String title;
        private String status;               // "COMPLETED", "IN_PROGRESS", "PENDING"
        private String timestamp;            // "18 Sep 2025, 02:32 PM" or null
        private String note;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class MobileEscrowSummaryDto {
        private String referenceNo;          // "ESC2509180021"
        private BigDecimal amountBlocked;    // 242000.00
        private String amountBlockedFormatted; // "₹ 2,42,000.00"
        private String blockedDateFormatted; // "18 Sep 2025, 02:40 PM"
        private String bankPartner;          // "HDFC Bank (Escrow)"
        private String statusBadge;          // "Funds Blocked"
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class MobileLifecycleActivityDto {
        private String title;
        private String description;
        private String timestamp;
        private String state;                // "COMPLETED", "ACTIVE", "PENDING"
    }
}
