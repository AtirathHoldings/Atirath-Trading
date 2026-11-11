package com.atirath.trading.modules.admin.parent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddApprovalRequestDto {
    private String approvalType;
    private String company;
    private String requestedBy;
    private String amountOrDetails;
    private String priority;
    private String description;
}
