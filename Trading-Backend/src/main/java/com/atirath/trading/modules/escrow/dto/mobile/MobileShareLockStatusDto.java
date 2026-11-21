package com.atirath.trading.modules.escrow.dto.mobile;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Mobile DTO for Share Lock Status Screen (ShareLockStatusScreen) & FundEscrow - Share Lock Screen.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MobileShareLockStatusDto implements Serializable {

    private String escrowId;
    private String lockReference;
    private String company;
    private String companyFullName;
    private Long sharesLocked;
    private String lockedFrom;
    private String lockedFor;
    private String lockInitiated;
    private String lockConfirmed;
    private String transferWindow;
    private String status;
    private String statusBadge;
    private String statusColor;

    @JsonProperty("isLocked")
    private boolean isLocked;

    private String infoNotice;
    private boolean canProceedToTransfer;
}
