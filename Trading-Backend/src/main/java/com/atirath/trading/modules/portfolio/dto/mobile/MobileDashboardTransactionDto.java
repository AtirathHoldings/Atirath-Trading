package com.atirath.trading.modules.portfolio.dto.mobile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MobileDashboardTransactionDto {
    private Long id;
    private boolean isBuy;
    private String type;          // "Buy", "Sell", "Transfer"
    private String company;       // "AHIL", "AFC"
    private String companyName;
    private int shares;
    private double price;
    private String total;         // e.g. "₹47,025"
    private BigDecimal totalAmount;
    private String date;          // e.g. "Sep 24, 2026"
    private String status;        // "Settled", "Pending", "In Escrow"
}
