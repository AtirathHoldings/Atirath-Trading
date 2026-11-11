package com.atirath.trading.modules.admin.subsidiary.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestrictedShareDetailDto {

    private String id;
    private String shareId;
    private String restrictionTypeTitle;
    private String status;
    private String statusTheme;
    private String createdInfo;

    // Tabs inside drawer
    private List<String> tabs;
    private String activeTab;

    // Share Information
    private String entity;
    private String shareClass;
    private String noOfShares;
    private String faceValue;
    private String certificateNo;

    // Restriction Details
    private String restrictionType;
    private String restrictionTypeTheme;
    private String fromDate;
    private String toDate;
    private String reason;
    private String applicableTo;
    private String createdBy;
    private String createdOn;

    // Shareholder Information
    private String shareholderName;
    private String shareholderType;
    private String pan;
}
