package com.atirath.trading.modules.portfolio.dto.mobile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MobilePendingActionDto {
    private String id;
    private String title;
    private String subtitle;
    private String urgency;       // "HIGH", "NORMAL", "LOW"
    private String urgencyColor;  // "RED", "BLUE", "AMBER"
    private String actionText;    // "Deposit Now →", "Review Offer →"
    private String targetRoute;   // "ESCROW", "NEGOTIATION", "KYC", "TRADE", "ORDERS"
}
