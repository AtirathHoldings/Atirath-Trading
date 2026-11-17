package com.atirath.trading.modules.market.dto.mobile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MobileTransactionsPageDto {
    private Integer totalCount;
    private List<MobileTransactionItemDto> transactions;
}
