package com.atirath.trading.modules.escrow.dto.mobile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Compact recently settled row for mobile escrow dashboard.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MobileSettledRowDto implements Serializable {

    private String id;
    private String company;
    private String date;
    private String amount;
}
