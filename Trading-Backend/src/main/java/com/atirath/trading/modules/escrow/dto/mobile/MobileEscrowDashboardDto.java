package com.atirath.trading.modules.escrow.dto.mobile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * Mobile-specific DTO for Escrow Dashboard screen (escrow_screen.dart).
 * Delivers lightweight 2x2 KPI metrics, active escrow cards, and recently settled rows.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MobileEscrowDashboardDto implements Serializable {

    // 2x2 KPI Grid
    private int activeEscrow;
    private int pendingFunding;
    private int inTransfer;
    private int settledMtd;

    // Active Escrow List
    private List<MobileEscrowCardDto> activeEscrows;

    // Recently Settled List
    private List<MobileSettledRowDto> recentlySettled;
}
