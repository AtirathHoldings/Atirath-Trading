package com.atirath.trading.modules.profile.dto.mobile;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MobileBankAccountItemDto {
    private Long accountId;
    private String bankName;
    private String accountNo;
    private String accountType;
    private String ifscCode;
    private String upiId;

    @JsonProperty("isPrimary")
    private boolean isPrimary;

    private String status;
    private String holderName;
}
