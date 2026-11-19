package com.atirath.trading.modules.market.dto.mobile;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Lightweight DTO for real-time order calculation & review on trade_screen.dart.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MobileOrderPreviewDto {
    private String symbol;
    private String companyName;
    private String side;                 // BUY or SELL
    private String orderType;            // LIMIT or MARKET
    private Long quantity;
    private BigDecimal price;
    private BigDecimal orderValue;       // quantity * price
    private BigDecimal transactionFee;   // 0.1%
    private BigDecimal gst;              // 18% of txn fee
    private BigDecimal totalCharges;     // txnFee + gst
    private BigDecimal totalAmount;      // BUY: orderValue + charges; SELL: orderValue - charges
    private BigDecimal availableBalance; // Available INR if BUY; Available Shares if SELL
    private Boolean isSufficient;
    private BigDecimal postOrderBalance;
    private String message;

    // Additional fields for Screen 4: Order Review
    private String settlementType;           // "Internal T+2 Cycle"
    private String estimatedSettlementDate;  // e.g. "21 Nov 2026"
    private String bankAccountMasked;        // "HDFC Bank (Escrow) ****4821"
    private String escrowVerification;       // "Required upon matching"
}
