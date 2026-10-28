package com.atirath.trading.modules.negotiation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NegotiationInsightDto {
    private String title;
    private String subtitle;
    private List<String> months;
    private List<ChartSeriesDto> series;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChartSeriesDto {
        private String name;
        private List<Integer> data;
    }
}
