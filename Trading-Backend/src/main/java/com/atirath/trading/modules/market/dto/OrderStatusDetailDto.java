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
public class OrderStatusDetailDto {

    @Schema(description = "Primary Order ID", example = "101")
    private Long orderId;

    @Schema(description = "Order Reference Number", example = "BUY25091814322")
    private String orderNo;

    @Schema(description = "Order Date formatted", example = "18 Sep 2025, 02:32 PM")
    private String orderDate;

    @Schema(description = "Order Placement ISO timestamp")
    private OffsetDateTime placedAt;

    // --- Company Information ---
    @Schema(description = "Company Summary")
    private CompanyInfo company;

    // --- Order Details ---
    @Schema(description = "Order Side", example = "BUY")
    private String orderSide;

    @Schema(description = "Order Side Badge text", example = "Buy Order")
    private String orderSideBadge;

    @Schema(description = "Order Type", example = "LIMIT")
    private String orderType;

    @Schema(description = "Order Type Display Label", example = "Buy (Limit Order)")
    private String orderTypeDisplay;

    @Schema(description = "Quantity in shares", example = "1000")
    private Long quantity;

    @Schema(description = "Price per share (₹)", example = "242.00")
    private BigDecimal pricePerShare;

    @Schema(description = "Total Order Value (₹)", example = "242000.00")
    private BigDecimal orderValue;

    @Schema(description = "Validity Description", example = "Day Order (Valid till end of trading day)")
    private String validity;

    @Schema(description = "Stop-loss Summary", example = "Not Set")
    private String stopLoss;

    @Schema(description = "Trigger Type", example = "-")
    private String triggerType;

    @Schema(description = "Order Channel / Source", example = "Web Platform")
    private String orderSource;

    // --- Current Status & High-level State ---
    @Schema(description = "Current lifecycle state summary")
    private CurrentStatusInfo currentStatus;

    // --- 7-Step Horizontal Stepper Progress ---
    @Schema(description = "7-Step Order Progress Stepper")
    private List<ProgressStepDto> progressSteps;

    // --- Chronological Activity History ---
    @Schema(description = "Recent Activity timeline items")
    private List<ActivityItemDto> recentActivity;

    // --- Counterparty Details ---
    @Schema(description = "Matched counterparty details")
    private CounterpartyDto counterpartyDetails;

    // --- Escrow & Payment Details ---
    @Schema(description = "Escrow and fund blocking details")
    private EscrowDetailDto escrowDetails;

    // --- Market & Price Information ---
    @Schema(description = "Real-time stock price and day range")
    private MarketPriceInfoDto marketInformation;

    // --- Downloadable Documents ---
    @Schema(description = "Related trade documents")
    private List<OrderDocumentDto> documents;

    // --- Inner Models ---

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompanyInfo {
        private Long companyId;
        private String companyName;
        private String symbol;
        private String sector;
        private String status;
        private String logoUrl;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CurrentStatusInfo {
        private String code; // IN_ESCROW, PENDING_MATCH, COMPLETED, CANCELLED, REJECTED
        private String title; // "In Escrow"
        private String description; // "Funds have been blocked in escrow. Awaiting approvals from the counterparty and admin."
        private String estimatedCompletion; // "19 Sep 2025 (EOD)"
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProgressStepDto {
        private Integer stepNumber; // 1 to 7
        private String title; // e.g. "Order Placed", "Counterparty Matching", etc.
        private String timestamp; // e.g. "18 Sep 2025, 02:32 PM" or "Not Required" or "-"
        private String status; // COMPLETED, IN_PROGRESS, PENDING
        private String icon; // "file", "users", "chat", "lock", "check", "transfer", "flag"
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivityItemDto {
        private String title; // "Order placed successfully"
        private String description; // "Your buy order has been submitted."
        private String timestamp; // "18 Sep 2025, 02:32 PM" or "-"
        private String state; // COMPLETED, ACTIVE, PENDING
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CounterpartyDto {
        private String memberName; // "Rakesh Mehta"
        private String memberId; // "ATG10056"
        private String memberType; // "Internal Member"
        private String portfolioHoldings; // "5,000 shares"
        private String previousTrades; // "12 trades"
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EscrowDetailDto {
        private String escrowReferenceNo; // "ESC2509180021"
        private BigDecimal amountBlocked; // 242000.00
        private String amountBlockedFormatted; // "₹ 2,42,000.00"
        private String blockedOn; // "18 Sep 2025, 02:40 PM"
        private String bankOrPartner; // "HDFC Bank (Escrow)"
        private String statusBadge; // "Funds Blocked"
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MarketPriceInfoDto {
        private BigDecimal currentMarketPrice; // 245.00
        private String priceChangeFormatted; // "+35.00 (+16.67%)"
        private BigDecimal dayHigh; // 250.00
        private BigDecimal dayLow; // 208.00
        private BigDecimal fiftyTwoWeekHigh; // 250.00
        private BigDecimal fiftyTwoWeekLow; // 120.00
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderDocumentDto {
        private String documentName; // "Order Confirmation", "Escrow Confirmation"
        private String downloadUrl; // URL or endpoint
    }
}
