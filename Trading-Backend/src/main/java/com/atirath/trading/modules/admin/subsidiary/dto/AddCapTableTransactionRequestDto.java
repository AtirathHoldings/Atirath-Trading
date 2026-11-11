package com.atirath.trading.modules.admin.subsidiary.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddCapTableTransactionRequestDto {
    private String shareholderName;
    private String shareClass;
    private String type; // Issuance, Transfer
    private String numberOfShares;
    private double pricePerShare;
    private String transactionDate;
    private String remarks;
}
