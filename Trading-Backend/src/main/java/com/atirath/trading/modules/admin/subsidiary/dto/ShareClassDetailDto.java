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
public class ShareClassDetailDto {
    private String id;
    private String className;
    private String subheader;
    private String status;
    private String statusTheme;

    private List<String> tabs;
    private String activeTab;

    private String classType;
    private String faceValue;
    private String issuedShares;
    private String authorizedShares;
    private String approxValue;
    private String votingRights;
    private String dividendRights;
    private String transferability;
    private String investorCategory;
    private String createdOn;
    private String lastUpdated;
    private String description;
}
