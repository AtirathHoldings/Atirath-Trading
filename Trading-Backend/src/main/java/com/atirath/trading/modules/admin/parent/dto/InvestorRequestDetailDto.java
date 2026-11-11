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
public class InvestorRequestDetailDto {
    private String requestId; // "INV-2026-0036"
    private String investorName; // "Rajesh Mehta"
    private String requestedOn; // "Requested on 16 Sep 2026, 11:20 AM"
    private String status; // "Pending", "Under Review", "Approved", "Rejected"
    private String statusTheme;

    // Tabs
    private List<String> tabs; // "Details", "Documents (4)", "Comments (3)", "History"
    private String activeTab;

    // Details fields
    private String investorType;
    private String contactEmail;
    private String contactPhone;
    private String company;
    private String sector;
    private String investmentType;
    private String proposedAmount;
    private String proposedEquity;
    private String purpose;
    private String expectedRoi;
    private String timeline;
    private String assignedTo;
    private String description;
}
