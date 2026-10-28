package com.atirath.trading.modules.portfolio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioActivityItemDto {
    private String id;
    private String title;          // e.g. "Shares Received", "Shares Locked", "Settlement Completed", "Payment Released"
    private String description;    // e.g. "1,000 shares - Atirath Holdings", "Trade ID: TRD250918004", "₹ 2,42,000 to Seller"
    private String timestamp;      // e.g. "18 Sep 2025, 11:28 AM"
    private String activityType;   // "RECEIVE", "LOCK", "TRANSFER", "SETTLEMENT", "PAYMENT"
    private String icon;
}
