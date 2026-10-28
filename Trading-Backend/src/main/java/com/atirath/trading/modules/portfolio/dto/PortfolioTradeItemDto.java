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
public class PortfolioTradeItemDto {
    private Long tradeId;
    private String tradeNo;             // "TRD250918004"
    private String tradeDateTime;       // "18 Sep 2025, 11:05 AM"
    private Long companyId;
    private String companyName;         // "Atirath Holdings India Limited"
    private String companyCode;         // "ATG", "CSI", "FVPL", "ATT", "TEB", "ROHE"
    private String symbol;
    private String logoUrl;
    private String tradeType;           // "Buy" / "Sell"
    private Long quantity;
    private String formattedQuantity;   // "1,000"
    private BigDecimal price;
    private String formattedPrice;      // "₹ 230.00"
    private BigDecimal tradeValue;
    private String formattedTradeValue; // "₹ 2,30,000"
    private String status;              // "Settled", "Cancelled"
    private String counterparty;        // "Rakesh Mehta", "Anil Kumar", "-"
}
