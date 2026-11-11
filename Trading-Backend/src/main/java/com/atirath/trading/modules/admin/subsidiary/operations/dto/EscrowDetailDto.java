package com.atirath.trading.modules.admin.subsidiary.operations.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EscrowDetailDto {

    private String id;
    private String escrowId;
    private String companyTitle;
    private String status;
    private String statusTheme;
    private String createdInfo;

    // Tabs inside drawer
    private List<String> tabs;
    private String activeTab;

    // Escrow Information
    private String company;
    private String tradeId;
    private String investor;
    private String shareClass;
    private String amount;
    private String createdOn;
    private String expectedRelease;
    private String releasedOn;
    private String escrowBank;
    private String accountNo;
    private String ifscCode;

    // Amount Details
    private String totalAmount;
    private String releasedAmount;
    private String onHoldAmount;
    private String pendingAmount;
    private String availableAmount;

    // Compliance Information
    private String kycStatus;
    private String amlCheck;
    private String sanctionsCheck;

    // Related Information
    private String relatedTrade;
    private String investorProfile;
    private String documentsCount;
}
