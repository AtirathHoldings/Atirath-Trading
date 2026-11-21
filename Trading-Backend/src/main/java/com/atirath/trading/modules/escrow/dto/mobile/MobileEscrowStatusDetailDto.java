package com.atirath.trading.modules.escrow.dto.mobile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * Mobile DTO for Escrow Status - View Screen (EscrowStatusScreen View Details).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MobileEscrowStatusDetailDto implements Serializable {

    private String escrowId;
    private String tradeId;
    private String agreementId;
    private String company;
    private Long shares;
    private BigDecimal amount;
    private String amountFormatted;

    private String buyerName;
    private String buyerDemat;
    private String sellerName;
    private String sellerDemat;

    private String status;
    private String statusBadge;
    private String currentStage;
    private String depositoryReference;
    private String internalLedgerStatus;
    private String lastUpdated;

    private List<MobileEscrowTimelineStepDto> statusHistory;
}
