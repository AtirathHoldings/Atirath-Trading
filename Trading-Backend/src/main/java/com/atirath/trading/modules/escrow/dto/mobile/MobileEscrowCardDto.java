package com.atirath.trading.modules.escrow.dto.mobile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Compact active escrow card for mobile escrow dashboard.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MobileEscrowCardDto implements Serializable {

    private String id;
    private String tradeId;
    private String agreementId;
    private String company;
    private String companyFullName;
    private Long shares;
    private BigDecimal price;
    private BigDecimal amount;
    private String amountFormatted;
    private String buyer;
    private String buyerId;
    private String seller;
    private String sellerId;
    private String status;
    private String statusBadge;
    private String statusColor;
    private String createdAt;
    private String updatedAt;
}
