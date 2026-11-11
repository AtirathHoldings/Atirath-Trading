package com.atirath.trading.modules.admin.subsidiary.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyTransactionDetailDto {
    private String id;
    private String transactionId;
    private String transactionType;
    private String status;
    private String statusTheme;

    private List<String> tabs;
    private String activeTab;

    private String transactionDate;
    private String numberOfShares;
    private String pricePerShare;
    private String totalValue;
    private String counterparty;
    private String paymentMode;
    private String referenceNo;
    private String approvedBy;
    private String approvedOn;
    private String remarks;
}
