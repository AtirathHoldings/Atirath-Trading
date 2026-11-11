package com.atirath.trading.modules.admin.subsidiary.operations.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InitiateTransferRequestDto {
    private String fromParty;
    private String toParty;
    private String shareClass;
    private String shares;
    private Double pricePerShare;
    private String totalValue;
    private String type; // Transfer, Gift, Inheritance
    private String reason;
    private String remarks;
}
