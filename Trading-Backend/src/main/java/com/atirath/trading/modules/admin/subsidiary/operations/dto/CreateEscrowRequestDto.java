package com.atirath.trading.modules.admin.subsidiary.operations.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateEscrowRequestDto {
    private String company;
    private String tradeId;
    private String investor;
    private String shareClass;
    private String amount;
    private String escrowBank;
    private String accountNo;
    private String ifscCode;
    private String expectedRelease;
}
