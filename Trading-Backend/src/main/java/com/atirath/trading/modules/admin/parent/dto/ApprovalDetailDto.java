package com.atirath.trading.modules.admin.parent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalDetailDto {
    private String requestId; // "APR-2026-1042"
    private String title; // "Share Transfer Request"
    private String requestedOn; // "Requested on 16 Sep 2026, 11:20 AM"
    private String status; // "Pending", "Under Review", "Approved", "Rejected"
    private String statusTheme;

    // Available drawer tabs
    private List<String> tabs; // "Details", "History", "Comments (2)", "Related Docs (3)"
    private String activeTab;

    // Detailed Fields
    private String approvalType;
    private String company;
    private String requestedBy;
    private String dateTime;
    private String shares;
    private String shareholder;
    private String transferee;
    private String consideration;
    private String purpose;
    private String priority;
    private String description;

    // Attachments
    private List<ApprovalAttachmentDto> attachments;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApprovalAttachmentDto {
        private String name;
        private String size;
        private String downloadUrl;
    }
}
