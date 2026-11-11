package com.atirath.trading.modules.admin.subsidiary.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecordCompanyTransactionRequestDto {
    private String transactionType; // Share Issuance, Share Transfer, Share Buyback, Corporate Action, Others
    private String transactionDate;
    private String numberOfShares;
    private double pricePerShare;
    private String totalValue;
    private String counterparty;
    private String paymentMode;
    private String referenceNo;
    private String remarks;
}
