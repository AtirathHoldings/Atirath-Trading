package com.atirath.trading.modules.escrow.dto.mobile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * Mobile DTO for Transfer Status Screen (TransferStatusScreen).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MobileTransferStatusDto implements Serializable {

    private String escrowId;
    private String transferId;
    private String currentStage;
    private List<MobileEscrowTimelineStepDto> lifecycleSteps;
    private boolean isCompleted;
    private String nextActionText;
}
