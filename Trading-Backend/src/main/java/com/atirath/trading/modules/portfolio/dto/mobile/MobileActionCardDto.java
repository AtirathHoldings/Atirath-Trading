package com.atirath.trading.modules.portfolio.dto.mobile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MobileActionCardDto {
    private String id;           // "ACT_KYC_1"
    private String iconType;     // "shield", "bank", "handshake", "order", "alert"
    private String title;
    private String subtitle;
    private String urgency;      // "HIGH", "NORMAL", "LOW"
    private String urgencyColor; // "RED", "BLUE", "AMBER"
    private String actionLabel;  // "Complete KYC", "Add Bank", "View Orders", etc.
    private String targetRoute;  // "KYC", "BANK", "TRADE", "ESCROW", "NOTIFICATIONS"
}
