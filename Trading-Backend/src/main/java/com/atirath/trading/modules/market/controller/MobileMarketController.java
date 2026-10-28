package com.atirath.trading.modules.market.controller;

import com.atirath.trading.common.ApiResponse;
import com.atirath.trading.modules.market.dto.MarketActivitySummaryDto;
import com.atirath.trading.modules.market.dto.MarketTradeDto;
import com.atirath.trading.modules.market.dto.MobileStockDto;
import com.atirath.trading.modules.market.dto.OrderBookUpdateDto;
import com.atirath.trading.modules.market.service.MarketBroadcastService;
import com.atirath.trading.modules.market.service.MarketCompanyService;
import com.atirath.trading.modules.market.service.MarketTradeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/market/mobile")
@RequiredArgsConstructor
@Tag(name = "Mobile Market API", description = "Mobile-optimized endpoints for Market Screen, Company Details Screen, and Trade Screen")
public class MobileMarketController {

    private final MarketCompanyService marketCompanyService;
    private final MarketTradeService marketTradeService;
    private final MarketBroadcastService broadcastService;

    @GetMapping("/companies")
    @Operation(summary = "Get Mobile Market Stocks", description = "Returns lightweight, mobile-tailored stocks catalog with live ticker, ratios, market cap, and order book for market_screen.dart & trade_screen.dart")
    public ResponseEntity<ApiResponse<List<MobileStockDto>>> getMobileCompanies(
            @Parameter(description = "Optional sector filter")
            @RequestParam(required = false) String sector,
            @Parameter(description = "Optional search keyword")
            @RequestParam(required = false) String search) {

        List<MobileStockDto> stocks = marketCompanyService.getMobileStocks(sector, search);
        return ResponseEntity.ok(ApiResponse.success(stocks, "Mobile market stocks retrieved successfully"));
    }

    @GetMapping("/companies/{identifier}")
    @Operation(summary = "Get Mobile Company Details", description = "Returns full mobile stock details by Company ID or Symbol for company_details_screen.dart")
    public ResponseEntity<ApiResponse<MobileStockDto>> getMobileCompanyDetails(
            @PathVariable("identifier") String identifier) {

        MobileStockDto stock = marketCompanyService.getMobileStockByIdentifier(identifier);
        return ResponseEntity.ok(ApiResponse.success(stock, "Mobile company details retrieved successfully"));
    }

    @GetMapping("/companies/symbol/{symbol}")
    @Operation(summary = "Get Mobile Company Details by Symbol", description = "Returns full mobile stock details by Symbol (e.g. AHIL or ATG-H)")
    public ResponseEntity<ApiResponse<MobileStockDto>> getMobileCompanyBySymbol(
            @PathVariable("symbol") String symbol) {

        MobileStockDto stock = marketCompanyService.getMobileStockByIdentifier(symbol);
        return ResponseEntity.ok(ApiResponse.success(stock, "Mobile company details retrieved successfully"));
    }

    @GetMapping("/sectors")
    @Operation(summary = "Get Active Sectors", description = "Returns active sectors for mobile filter chips in market_screen.dart")
    public ResponseEntity<ApiResponse<List<String>>> getSectors() {
        List<String> sectors = marketCompanyService.getAllSectors();
        return ResponseEntity.ok(ApiResponse.success(sectors, "Sectors retrieved successfully"));
    }

    @GetMapping("/trades/recent")
    @Operation(summary = "Get Recent Executed Trades", description = "Returns recent executed trades for mobile Recent Trades tab")
    public ResponseEntity<ApiResponse<List<MarketTradeDto>>> getRecentTrades(
            @RequestParam(required = false) Long companyId,
            @RequestParam(defaultValue = "20") int limit) {

        List<MarketTradeDto> trades = marketTradeService.getRecentTrades(companyId, limit);
        return ResponseEntity.ok(ApiResponse.success(trades, "Recent trades retrieved successfully"));
    }

    @GetMapping("/activity-summary")
    @Operation(summary = "Get Market Activity Summary", description = "Returns market totals and metrics for mobile KPI summary cards")
    public ResponseEntity<ApiResponse<MarketActivitySummaryDto>> getActivitySummary() {
        MarketActivitySummaryDto summary = marketTradeService.getMarketActivitySummary();
        return ResponseEntity.ok(ApiResponse.success(summary, "Market activity summary retrieved successfully"));
    }

    @GetMapping("/orderbook/{symbol}")
    @Operation(summary = "Get Live Order Book Snapshot", description = "Returns 5-depth live order book for trade_screen.dart & Order Book tab")
    public ResponseEntity<ApiResponse<OrderBookUpdateDto>> getOrderBookSnapshot(
            @PathVariable("symbol") String symbol) {

        String canonicalSymbol = marketCompanyService.resolveCanonicalSymbol(symbol);
        OrderBookUpdateDto ob = broadcastService.getOrderBook(canonicalSymbol != null ? canonicalSymbol : symbol);
        return ResponseEntity.ok(ApiResponse.success(ob, "Order book snapshot retrieved successfully"));
    }
}
