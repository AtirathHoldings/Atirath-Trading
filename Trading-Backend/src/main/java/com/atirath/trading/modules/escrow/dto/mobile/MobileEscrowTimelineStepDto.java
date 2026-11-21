package com.atirath.trading.modules.escrow.dto.mobile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Single step item in mobile 7-step escrow timeline.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MobileEscrowTimelineStepDto implements Serializable {

    private int stepNumber;
    private String label;
    private String sub;
    private boolean done;
    private boolean active;
}
