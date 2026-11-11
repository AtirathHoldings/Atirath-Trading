package com.atirath.trading.modules.admin.subsidiary.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlagSuspiciousTransactionRequestDto {
    private String entity;
    private String investorOrShareholder;
    private String alertType; // Unusual Trading Pattern, Price Manipulation, Layering, Insider Trading, Wash Trading
    private String riskLevel; // High, Medium, Low
    private String transactionType;
    private String fromParty;
    private String toParty;
    private String noOfShares;
    private String amount;
    private String notes;
}
