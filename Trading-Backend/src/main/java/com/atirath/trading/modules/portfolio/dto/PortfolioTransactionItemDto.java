package com.atirath.trading.modules.portfolio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioTransactionItemDto {
    private Long id;
    private String txnId;               // "TXN250918004"
    private String dateAndTime;         // "18 Sep 2025\n11:28 AM"
    private Long companyId;
    private String companyName;         // "Atirath Holdings"
    private String companyCode;         // "ATG"
    private String symbol;
    private String logoUrl;
    private String transactionType;     // "Buy", "Sell", "Dividend", "Corporate Action"
    private Long sharesQuantity;        // 1000
    private String formattedSharesQuantity; // "1,000" or "-"
    private BigDecimal price;           // 230.00
    private String formattedPrice;      // "230.00" or "-"
    private BigDecimal amount;          // 230000.00
    private String formattedAmount;     // "2,30,000" or "-"
    private String paymentMode;         // "Bank Transfer", "Escrow", "UPI", "-"
    private String status;              // "Completed", "Pending"
    private String remarks;             // "-", "Q1 2025", "Bonus Shares (1:1)", "Settlement in progress"
}
