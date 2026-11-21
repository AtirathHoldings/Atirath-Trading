package com.atirath.trading.modules.negotiation.dto.mobile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MobileCounterOfferResultDto {
    private boolean success;
    private String negotiationNo;
    private BigDecimal newOfferedPrice;
    private String newOfferedPriceFormatted;
    private Long quantity;
    private BigDecimal newTotalValue;
    private String newTotalValueFormatted;
    private String status;
    private String toastMessage;
}
