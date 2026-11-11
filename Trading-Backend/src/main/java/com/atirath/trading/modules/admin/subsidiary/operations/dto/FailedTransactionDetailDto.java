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
public class FailedTransactionDetailDto {
    private String id;
    private String transactionId;
    private String transactionTypeTitle;
    private String createdInfo;
    private String status;
    private String statusTheme;

    private List<String> tabs;
    private String activeTab;

    // Transaction Information
    private String type;
    private String company;
    private String investor;
    private String shareClass;
    private String noOfShares;
    private String pricePerShare;
    private String totalValue;
    private String orderDate;
    private String executionDate;

    // Failure Information
    private String failureReason;
    private String errorCode;
    private String errorMessage;
    private String failedOn;

    // Retry Information
    private String retryStatus;
    private String autoRetry;
    private String nextRetryOn;
    private String retryCount;
}
