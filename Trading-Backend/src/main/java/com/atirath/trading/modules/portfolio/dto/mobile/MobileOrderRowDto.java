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
public class MobileOrderRowDto {
    private Long orderId;
    private String orderNo;
    private String symbol;
    private String companyName;
    private String orderSide;     // "BUY" or "SELL"
    private Long quantity;
    private Double price;
    private BigDecimal orderValue;
    private String formattedValue;
    private String placedAt;
    private String status;        // "OPEN", "COMPLETED", "CANCELLED", etc.
}
