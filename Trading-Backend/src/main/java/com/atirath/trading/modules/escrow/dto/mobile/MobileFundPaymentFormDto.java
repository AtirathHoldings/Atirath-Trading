package com.atirath.trading.modules.escrow.dto.mobile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * Mobile-specific DTO for FundEscrow - Make Payment Screen (FundEscrowScreen).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MobileFundPaymentFormDto implements Serializable {

    private String escrowId;
    private String company;
    private String companyFullName;
    private Long shares;
    private BigDecimal pricePerShare;
    private BigDecimal amountToFund;
    private String amountToFundFormatted;

    // Supported Payment Methods
    private List<String> supportedPaymentMethods; // "upi", "netbanking", "debit"
    private String defaultMethod;
    private String defaultUpiId;
    private List<String> availableBanks;
    private String termsText;
}
