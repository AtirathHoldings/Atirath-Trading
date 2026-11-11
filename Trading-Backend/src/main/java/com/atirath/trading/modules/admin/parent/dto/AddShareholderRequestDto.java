package com.atirath.trading.modules.admin.parent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddShareholderRequestDto {
    private String shareholderName;
    private String type; // "Corporate", "Individual", "Trust"
    private String category; // "Promoter", "Individual", "Institutional", "Foreign"
    private String country;
    private String noOfShares;
    private Double shareholdingPercent;
    private String totalValue;
}
