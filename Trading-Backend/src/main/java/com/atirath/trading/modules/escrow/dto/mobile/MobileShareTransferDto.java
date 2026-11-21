package com.atirath.trading.modules.escrow.dto.mobile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Mobile DTO for Share Transfer Screen (ShareTransferScreen) & FundEscrow - Share Transfer Screen.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MobileShareTransferDto implements Serializable {

    private String escrowId;
    private String transferId;
    private String company;
    private Long shares;
    private BigDecimal totalValue;
    private String totalValueFormatted;

    // Party Details & Visual Ledger
    private String fromName;
    private String fromMemberId;
    private Long fromSharesBefore;
    private Long fromSharesAfter;

    private String toName;
    private String toMemberId;
    private Long toSharesBefore;
    private Long toSharesAfter;

    private String transferDate;
    private String settlementMode; // e.g. "Internal Ledger" or "Depository Slip"
    private String status;
    private String statusBadge;
    private boolean canConfirmTransfer;
}
