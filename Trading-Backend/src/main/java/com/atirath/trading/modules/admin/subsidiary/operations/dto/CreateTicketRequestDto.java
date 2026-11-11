package com.atirath.trading.modules.admin.subsidiary.operations.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTicketRequestDto {
    private String subject;
    private String category; // Trade, Settlement, Transfer, Payment, Compliance, Reporting, User Management, Document, Reconciliation
    private String company;
    private String raisedBy;
    private String priority; // High, Medium, Low
    private String description;
}
