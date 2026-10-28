package com.atirath.trading.modules.portfolio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioTradeDetailDto {
    private Long tradeId;
    private String tradeNo;             // "TRD250918004"
    private String companyName;         // "Atirath Holdings India Limited"
    private String symbol;              // "ATG-H"
    private String tradeType;           // "Buy" / "Sell"
    private Long quantity;
    private String formattedQuantity;   // "1,000"
    private BigDecimal pricePerShare;
    private String formattedPricePerShare; // "₹ 230.00"
    private BigDecimal totalValue;
    private String formattedTotalValue; // "₹ 2,30,000"
    private String orderDateTime;       // "18 Sep 2025, 10:15 AM"
    private String tradeDateTime;       // "18 Sep 2025, 11:05 AM"
    private String settlementDate;      // "18 Sep 2025, 11:28 AM"
    private String status;              // "Settled"
    private String counterparty;        // "Rakesh Mehta (ATG10056)"
    private String settlementRef;       // "SET250918001"
    
    private List<PortfolioTimelineStepDto> timeline;
}
