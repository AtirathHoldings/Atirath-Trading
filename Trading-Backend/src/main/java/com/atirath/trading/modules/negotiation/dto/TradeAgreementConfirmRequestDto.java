package com.atirath.trading.modules.negotiation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeAgreementConfirmRequestDto {
    private Boolean readAndUnderstood;
    private Boolean informationAccurate;
    private Boolean agreeToEscrowAndSettlement;
}
