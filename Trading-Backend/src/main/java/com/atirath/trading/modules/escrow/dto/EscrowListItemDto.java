package com.atirath.trading.modules.escrow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EscrowListItemDto {
    private int rowNo;
    private String escrowId;
    private String tradeId;
    private String companyName;
    private String buyerName;
    private String sellerName;
    private Long quantity;
    private BigDecimal pricePerShare;
    private BigDecimal tradeValue;
    private String currentStatus;
    private String createdOn;
    private String expectedSettlement;
}
