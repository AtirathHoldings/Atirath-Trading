package com.atirath.trading.modules.market.dto.mobile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MobileTransactionItemDto {
    private String id;           // "TXN-89421" or "ORD-101"
    private Long orderId;
    private String type;         // "buy", "sell", "settlement", "transfer"
    private String company;      // "AHIL"
    private String companyName;  // "Atirath Holdings India Limited"
    private Integer shares;      // 50
    private Double price;        // 940.50
    private Double total;        // 47025.0
    private String formattedTotal; // "₹47,025"
    private String date;         // "Sep 24, 2026"
    private String status;       // "settled", "pending", "cancelled"
    private Boolean isBuy;
}
