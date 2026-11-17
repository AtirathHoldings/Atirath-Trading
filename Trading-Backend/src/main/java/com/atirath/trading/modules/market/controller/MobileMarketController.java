package com.atirath.trading.modules.market.controller;

import com.atirath.trading.common.ApiResponse;
import com.atirath.trading.modules.market.dto.MarketActivitySummaryDto;
import com.atirath.trading.modules.market.dto.MarketTradeDto;
import com.atirath.trading.modules.market.dto.MobileStockDto;
import com.atirath.trading.modules.market.dto.OrderBookUpdateDto;
import com.atirath.trading.modules.market.dto.PriceCandleDto;
import com.atirath.trading.modules.market.dto.mobile.MobileMarketOverviewDto;
import com.atirath.trading.modules.market.model.MarketCompany;
import com.atirath.trading.modules.market.service.MarketBroadcastService;
import com.atirath.trading.modules.market.service.MarketCompanyService;
import com.atirath.trading.modules.market.service.MarketPriceHistoryService;
import com.atirath.trading.modules.market.service.MarketTradeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/market/mobile")
@RequiredArgsConstructor
@Tag(name = "Mobile Market API", description = "Mobile-optimized endpoints for Market Screen, Company Details Screen, and Trade Screen")
public class MobileMarketController {

    private final MarketCompanyService marketCompanyService;
    private final MarketPriceHistoryService marketPriceHistoryService;
    private final MarketTradeService marketTradeService;
    private final MarketBroadcastService broadcastService;

    @GetMapping("/overview")
    @Operation(summary = "Get Mobile Market Overview", description = "Returns aggregated live statistics, performance series, gainers/decliners, market activity for market_screen.dart")
    public ResponseEntity<ApiResponse<MobileMarketOverviewDto>> getMobileMarketOverview() {
        MobileMarketOverviewDto overview = marketCompanyService.getMobileMarketOverview();
        return ResponseEntity.ok(ApiResponse.success(overview, "Market overview retrieved successfully"));
    }

    @GetMapping("/companies")
    @Operation(summary = "Get Mobile Market Stocks", description = "Returns lightweight, mobile-tailored stocks catalog with live ticker, ratios, market cap, and order book for market_screen.dart & trade_screen.dart")
    public ResponseEntity<ApiResponse<List<MobileStockDto>>> getMobileCompanies(
            @Parameter(description = "Optional sector filter")
            @RequestParam(required = false) String sector,
            @Parameter(description = "Optional search keyword")
            @RequestParam(required = false) String search,
            @Parameter(description = "Optional filter: Gainers, Decliners, High Volume, Low P/E")
            @RequestParam(required = false) String filter) {

        List<MobileStockDto> stocks = marketCompanyService.getMobileStocks(sector, search, filter);
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

    @GetMapping("/companies/{identifier}/price-history")
    @Operation(summary = "Get Company Price History for Charts", description = "Returns OHLC price candles for mobile chart screen")
    public ResponseEntity<ApiResponse<List<PriceCandleDto>>> getPriceHistory(
            @PathVariable("identifier") String identifier,
            @RequestParam(required = false, defaultValue = "DAY") String interval,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to) {

        MarketCompany company = marketCompanyService.findCompanyByIdentifier(identifier);
        List<PriceCandleDto> history = marketPriceHistoryService.getPriceHistory(company.getCompanyId(), interval, from, to);
        return ResponseEntity.ok(ApiResponse.success(history, "Price history retrieved successfully"));
    }

    @GetMapping("/companies/{identifier}/orderbook")
    @Operation(summary = "Get Live Order Book by Identifier", description = "Returns live order book by Company ID or Symbol")
    public ResponseEntity<ApiResponse<OrderBookUpdateDto>> getCompanyOrderBook(
            @PathVariable("identifier") String identifier) {

        OrderBookUpdateDto ob = marketCompanyService.getLiveOrderBook(identifier);
        return ResponseEntity.ok(ApiResponse.success(ob, "Order book retrieved successfully"));
    }

    @GetMapping("/companies/{identifier}/fundamentals")
    @Operation(summary = "Get Company Fundamentals", description = "Returns quarterly and annual financials, shareholding, ratios, and governance highlights")
    public ResponseEntity<ApiResponse<MobileStockDto>> getCompanyFundamentals(
            @PathVariable("identifier") String identifier) {

        MobileStockDto stock = marketCompanyService.getMobileStockByIdentifier(identifier);
        return ResponseEntity.ok(ApiResponse.success(stock, "Company fundamentals retrieved successfully"));
    }

    @GetMapping("/companies/{identifier}/documents")
    @Operation(summary = "Get Company Documents", description = "Returns verified documents for this company")
    public ResponseEntity<ApiResponse<List<MobileStockDto.CompanyDocumentDto>>> getCompanyDocuments(
            @PathVariable("identifier") String identifier,
            @RequestParam(required = false) String category) {

        List<MobileStockDto.CompanyDocumentDto> docs = marketCompanyService.getMobileCompanyDocuments(identifier, category);
        return ResponseEntity.ok(ApiResponse.success(docs, "Company documents retrieved successfully"));
    }

    @GetMapping("/documents")
    @Operation(summary = "Get All Mobile Documents", description = "Returns documents across all companies with optional category and search filters")
    public ResponseEntity<ApiResponse<List<MobileStockDto.CompanyDocumentDto>>> getAllDocuments(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search) {

        List<MobileStockDto.CompanyDocumentDto> docs = marketCompanyService.getAllMobileDocuments(category, search);
        return ResponseEntity.ok(ApiResponse.success(docs, "Documents retrieved successfully"));
    }

    @GetMapping("/documents/{documentId}/download")
    @Operation(summary = "Download Mobile Document", description = "Downloads official company filing or disclosure PDF/CSV")
    public ResponseEntity<byte[]> downloadDocument(@PathVariable("documentId") Long documentId) {
        MarketCompanyService.DocumentDownloadPayload payload = marketCompanyService.getDocumentDownload(documentId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + payload.fileName() + "\"")
                .header(HttpHeaders.CONTENT_TYPE, payload.mimeType())
                .body(payload.content());
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

        OrderBookUpdateDto ob = marketCompanyService.getLiveOrderBook(symbol);
        return ResponseEntity.ok(ApiResponse.success(ob, "Order book snapshot retrieved successfully"));
    }
}
