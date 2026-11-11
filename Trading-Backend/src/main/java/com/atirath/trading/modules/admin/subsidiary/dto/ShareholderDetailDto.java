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
public class ShareholderDetailDto {
    private String id;
    private String shareholderName;
    private String initials;
    private String status;
    private String statusTheme;
    private String categoryBadge;
    private String typeBadge;

    private List<String> tabs;
    private String activeTab;

    private String category;
    private String type;
    private String noOfShares;
    private double ownershipPercentage;
    private String investmentAmount;
    private String pan;
    private String country;
    private String registeredAddress;
    private String contactPerson;
    private String email;
    private String phone;
    private String kycStatus;
    private String kycStatusTheme;
    private String lastUpdated;
}
