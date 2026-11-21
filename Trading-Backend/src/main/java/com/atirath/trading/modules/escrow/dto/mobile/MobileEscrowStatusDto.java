package com.atirath.trading.modules.escrow.dto.mobile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * Mobile DTO for Escrow Status Screen (EscrowStatusScreen).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MobileEscrowStatusDto implements Serializable {

    private String escrowId;
    private String company;
    private BigDecimal amount;
    private String amountFormatted;
    private String status;
    private String statusBadge;
    private String statusColor;

    // Status History Timeline (7 steps)
    private List<MobileEscrowTimelineStepDto> statusHistory;

    private boolean canProceedToShareLock;
}
