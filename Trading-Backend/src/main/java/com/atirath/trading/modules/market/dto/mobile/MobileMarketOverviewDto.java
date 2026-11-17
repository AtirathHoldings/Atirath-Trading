package com.atirath.trading.modules.market.dto.mobile;

import com.atirath.trading.modules.market.dto.MobileStockDto;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MobileMarketOverviewDto {
    private BigDecimal totalMarketCapCr;
    private String formattedTotalMarketCap; // e.g. "₹89,310 Cr"
    private Long todayVolumeShares;
    private String formattedTodayVolume;    // e.g. "49,070"
    private Integer advancesCount;
    private Integer declinesCount;
    private String marketSentiment;         // e.g. "4/5 ↑"
    private MobileStockDto topGainer;
    private MobileStockDto topDecliner;
    private Long buyOrdersCount;
    private String buyOrdersChangeText;     // e.g. "↑ +27%"
    private Long sellOrdersCount;
    private String sellOrdersChangeText;    // e.g. "↑ +11%"
    private Long tradesExecutedCount;
    private String tradesChangeText;        // e.g. "↑ +33%"
    private Long totalVolumeShares;
    private String volumeChangeText;        // e.g. "↑ +28%"
    private BigDecimal totalTurnover;
    private String formattedTotalTurnover;  // e.g. "12,48,500"
    private String turnoverChangeText;      // e.g. "↑ +18%"
    private String marketStatus;            // e.g. "OPEN"
    private String marketHours;             // e.g. "09:00 AM - 05:00 PM"
    private Map<String, List<BigDecimal>> performanceSeries;
    private List<String> performanceXLabels;
    private List<MobileStockDto> companies;
}
