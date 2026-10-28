package com.atirath.trading.modules.escrow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EscrowValueTrendDto {
    private String timeframe; // "This Month"
    private BigDecimal currentValue;
    private String currentValueFormatted; // "₹ 1,24,50,000"
    private String growth; // "↑ 12% vs last month"
    private List<DataPointDto> dataPoints;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataPointDto {
        private String date; // "1 Sep", "7 Sep", etc.
        private BigDecimal value;
        private String formattedValue;
    }
}
