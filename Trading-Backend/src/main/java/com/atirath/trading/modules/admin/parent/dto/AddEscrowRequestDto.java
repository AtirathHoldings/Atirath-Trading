package com.atirath.trading.modules.admin.parent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddEscrowRequestDto {
    private String tradeId;
    private String buyer;
    private String seller;
    private String company;
    private String escrowAmount;
    private String tradeValue;
    private String settlementDate;
}
