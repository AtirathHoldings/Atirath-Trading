package com.atirath.trading.modules.admin.system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateWorkflowRequestDto {
    private String workflowName;
    private String module;
    private String description;
    private Integer approvalLevels;
    private String approvalType; // Sequential, Parallel
    private String approvalRequired; // All Levels, Any Level
    private Boolean allowDelegation;
    private Boolean autoEscalation;
    private String escalationTime;
    private List<String> conditions;
}
