package com.atirath.trading.modules.market.dto.mobile;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Lightweight DTO tailored specifically for Flutter OrderDetailsScreen (Figma 4.8).
 * Provides compact Order Information, Trade & Status breakdown, and available action flags.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MobileOrderDetailDto {
    private Long orderId;
    private String orderNo;
    private String symbol;
    private String companyName;
    private String side;                  // BUY or SELL
    private String sideBadge;             // BUY or SELL
    private String orderType;             // Limit or Market
    private Long quantity;
    private BigDecimal price;
    private BigDecimal totalValue;
    private String totalValueFormatted;
    private String placedOnFormatted;

    // Status & Trade Breakdown
    private String status;                // OPEN, IN_ESCROW, COMPLETED, CANCELLED, REJECTED
    private String statusBadge;           // Open, In Escrow, Completed, Cancelled, Rejected
    private String tradeId;               // TRD-xxx or "—"
    private String negotiationStatus;     // Awaiting Match, Under Negotiation, Settled, Not Applicable
    private String escrowStatus;          // Funds Blocked, Shares Locked, Settled, Released, Not Initiated
    private String transferStatus;        // Completed, Pending, Not Initiated
    private String settlementStatus;      // Completed (T+2), Pending, Cancelled

    // Action Capabilities
    private Boolean canCancel;
    private Boolean canTrackLifecycle;
    private Boolean canViewNegotiation;
}
