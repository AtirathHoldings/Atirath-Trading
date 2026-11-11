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
public class SettlementDetailDto {

    private String id;
    private String settlementId;
    private String tradeSettlementTitle; // Trade Settlement
    private String status;
    private String statusTheme;
    private String createdInfo;

    // Tabs inside drawer
    private List<String> tabs;
    private String activeTab;

    // Settlement Information
    private String tradeId;
    private String company;
    private String investor;
    private String shareClass;
    private String amount;
    private String settlementDate;
    private String expectedDate;
    private String settlementBank;
    private String referenceNo;
    private String createdBy;
    private String createdOn;

    // Settlement Steps
    private List<SettlementStepDto> steps;

    // Payment Information
    private String paymentMode;
    private String utrNo;
    private String paymentDate;
    private String amountReceived;
    private String expectedAmount;

    // Additional Information
    private String remarks;
    private String attachmentsCount;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SettlementStepDto {
        private String stepName;
        private String status; // Completed, Pending, Not Started
        private String statusTheme; // green, orange, gray
        private String timestamp;
    }
}
