package com.atirath.trading.modules.admin.parent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCompanyInfoDto {
    private String registeredAddress;
    private String headquarters;
    private String pan;
    private String gstNumber;
    private String status;
}
