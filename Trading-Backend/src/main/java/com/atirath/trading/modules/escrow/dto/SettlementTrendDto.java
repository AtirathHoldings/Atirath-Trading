package com.atirath.trading.modules.escrow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettlementTrendDto {
    private List<String> months;
    private List<Integer> completed;
    private List<Integer> pending;
}
