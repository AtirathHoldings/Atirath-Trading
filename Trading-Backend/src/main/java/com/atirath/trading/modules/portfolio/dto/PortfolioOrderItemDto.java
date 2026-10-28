package com.atirath.trading.modules.portfolio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioOrderItemDto {
    private Long orderId;
    private String orderNo;             // e.g. "ORD250918001"
    private Long companyId;
    private String companyName;
    private String companyCode;         // e.g. "ATG", "CSI", "FVPL", "ATT", "TEB", "ROHE"
    private String symbol;
    private String logoUrl;
    private String orderSide;           // "BUY", "SELL"
    private Long quantity;
    private String formattedQuantity;   // "1,000"
    private BigDecimal price;
    private String formattedPrice;      // "₹ 230.00"
    private BigDecimal orderValue;
    private String formattedOrderValue; // "₹ 2,30,000"
    private String orderDateTime;       // "18 Sep 2025, 10:15 AM"
    private String status;              // "Executed", "Open", "Cancelled", "Rejected"
}
