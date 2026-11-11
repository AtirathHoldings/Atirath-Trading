package com.atirath.trading.modules.admin.subsidiary.operations.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RaiseDisputeRequestDto {
    private String type; // Trade Dispute, Payment Dispute, Settlement Dispute, Transfer Dispute
    private String relatedId; // TR-2026-001, etc.
    private String investor;
    private String counterparty;
    private String amount;
    private String priority; // High, Medium, Low
    private String targetResolution;
    private String description;
}
