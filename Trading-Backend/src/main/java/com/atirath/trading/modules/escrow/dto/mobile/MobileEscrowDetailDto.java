package com.atirath.trading.modules.escrow.dto.mobile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * Mobile-tailored DTO for Escrow Details Screen (EscrowDetailsScreen).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MobileEscrowDetailDto implements Serializable {

    // Header info
    private String id;
    private String company;
    private String companyFullName;
    private String status;
    private String statusBadge;
    private String statusColor;

    // Escrow Information Card
    private String tradeId;
    private String agreementId;
    private BigDecimal amount;
    private String amountFormatted;
    private Long shares;
    private BigDecimal pricePerShare;
    private String pricePerShareFormatted;
    private String buyer;
    private String buyerId;
    private String seller;
    private String sellerId;
    private String createdAt;
    private String updatedAt;

    // 7-Step Escrow Timeline
    private List<MobileEscrowTimelineStepDto> timeline;

    // Action Capabilities
    private boolean canFund;
    private boolean canLockShares;
    private boolean canTransferShares;
    private boolean canViewStatus;
    private boolean canViewSettlement;
    private String currentStage;
}
