package com.atirath.trading.modules.admin.parent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KpiCardDto {
    private String title;
    private String value;
    private String change;
    private String comparisonText;
    private String trend; // "positive", "negative", "neutral"
    private String icon;
    private String color;
}
