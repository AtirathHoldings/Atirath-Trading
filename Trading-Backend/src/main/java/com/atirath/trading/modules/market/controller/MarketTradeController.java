package com.atirath.trading.modules.market.controller;

import com.atirath.trading.common.ApiResponse;
import com.atirath.trading.modules.market.dto.MarketActivitySummaryDto;
import com.atirath.trading.modules.market.dto.MarketTradeDto;
import com.atirath.trading.modules.market.service.MarketTradeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/market/trades")
@RequiredArgsConstructor
@Tag(name = "Market Trades & Activity", description = "Executed trades, Trade Terminal tape, and Dashboard market metrics")
public class MarketTradeController {

    private final MarketTradeService marketTradeService;

    @GetMapping("/recent")
    @Operation(summary = "Get Recent Trades", description = "Returns recent executed trade records for market activity tape, Trade Terminal, and dashboards")
    public ResponseEntity<ApiResponse<List<MarketTradeDto>>> getRecentTrades(
            @Parameter(description = "Optional companyId filter")
            @RequestParam(required = false) Long companyId,
            @Parameter(description = "Number of trades to return (max 50, default 20)")
            @RequestParam(defaultValue = "20") int limit) {

        List<MarketTradeDto> trades = marketTradeService.getRecentTrades(companyId, limit);
        return ResponseEntity.ok(ApiResponse.success(trades, "Recent trades retrieved successfully"));
    }

    @GetMapping("/company/{companyId}")
    @Operation(summary = "Get Company Trades", description = "Returns recent trades specifically for a single company")
    public ResponseEntity<ApiResponse<List<MarketTradeDto>>> getCompanyTrades(
            @PathVariable("companyId") Long companyId,
            @RequestParam(defaultValue = "20") int limit) {

        List<MarketTradeDto> trades = marketTradeService.getRecentTrades(companyId, limit);
        return ResponseEntity.ok(ApiResponse.success(trades, "Company trades retrieved successfully"));
    }

    @GetMapping("/activity-summary")
    @Operation(summary = "Get Market Activity Summary", description = "Returns aggregated market trade metrics: Total Trades, Total Traded Value, Active Companies, Today's Trades")
    public ResponseEntity<ApiResponse<MarketActivitySummaryDto>> getActivitySummary() {
        MarketActivitySummaryDto summary = marketTradeService.getMarketActivitySummary();
        return ResponseEntity.ok(ApiResponse.success(summary, "Market activity summary retrieved successfully"));
    }
}
