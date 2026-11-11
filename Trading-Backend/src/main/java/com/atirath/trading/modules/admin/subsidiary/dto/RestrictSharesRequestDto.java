package com.atirath.trading.modules.admin.subsidiary.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestrictSharesRequestDto {
    private String shareholder;
    private String entity;
    private String shareClass;
    private String noOfShares;
    private String restrictionType; // Lock-in, Regulatory Hold, Promoter Lock-in, Litigation Hold, Employee ESOP, Court Order
    private String fromDate;
    private String toDate;
    private String reason;
    private String applicableTo; // Trading, Transfer, Pledge
    private String certificateNo;
}
