package com.atirath.trading.modules.admin.subsidiary.operations.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddTradeRequestDto {
    private String company;
    private String shareClass;
    private String investor;
    private String type; // Buy, Sell, Transfer
    private String noOfShares;
    private Double pricePerShare;
    private String totalValue;
    private String isin;
    private String orderSource;
    private String remarks;
}
