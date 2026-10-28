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
public class PortfolioOrderDetailDto {
    private Long orderId;
    private String orderNo;             // "ORD250918001"
    private String companyName;         // "Atirath Holdings India Limited"
    private String symbol;              // "ATG-H"
    private String orderSide;           // "Buy" / "Sell"
    private Long quantity;
    private String formattedQuantity;   // "1,000"
    private BigDecimal pricePerShare;
    private String formattedPricePerShare; // "₹ 230.00"
    private BigDecimal orderValue;
    private String formattedOrderValue; // "₹ 2,30,000"
    private String orderDateTime;       // "18 Sep 2025, 10:15 AM"
    private String validity;            // "Day Order"
    private Long executedQuantity;
    private String formattedExecutedQuantity; // "1,000"
    private BigDecimal averageExecutedPrice;
    private String formattedAverageExecutedPrice; // "₹ 230.00"
    private String status;              // "Executed"
    private String settlementRef;       // "SET250918001"
    
    private List<PortfolioTimelineStepDto> timeline;
}
