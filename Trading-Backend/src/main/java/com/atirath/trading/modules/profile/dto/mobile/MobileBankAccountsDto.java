package com.atirath.trading.modules.profile.dto.mobile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MobileBankAccountsDto {
    private List<MobileBankAccountItemDto> accounts;
    private String settlementNote;
    private boolean canAddAccount;
}
