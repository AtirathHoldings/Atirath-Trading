package com.atirath.trading.modules.market.controller;

import com.atirath.trading.common.ApiResponse;
import com.atirath.trading.config.JwtTokenProvider;
import com.atirath.trading.modules.market.dto.CompanyOverviewDto;
import com.atirath.trading.modules.market.dto.CompanySummaryDto;
import com.atirath.trading.modules.market.dto.PriceCandleDto;
import com.atirath.trading.modules.market.model.*;
import com.atirath.trading.modules.market.service.MarketCompanyService;
import com.atirath.trading.modules.market.service.MarketPriceHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping({"/api/v1/market/web/companies", "/api/v1/market/companies"})
@RequiredArgsConstructor
@Tag(name = "Internal Market Companies (Web)", description = "Group Companies, Dashboard, Company Overview, Charts, Fundamentals, and Disclosures for Web Clients")
public class MarketCompanyController {

    private final MarketCompanyService marketCompanyService;
    private final MarketPriceHistoryService priceHistoryService;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping
    @Operation(summary = "List Market Companies", description = "Returns active tradable group companies with real-time price metrics. Supports sector filtering and search keyword.")
    public ResponseEntity<ApiResponse<List<CompanySummaryDto>>> getCompanies(
            @Parameter(description = "Optional sector filter (e.g., Technology, Renewable Energy, Finance)")
            @RequestParam(required = false) String sector,
            @Parameter(description = "Optional search keyword for company name, symbol or sector")
            @RequestParam(required = false) String search,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = extractOptionalUserId(authHeader);
        List<CompanySummaryDto> companies = marketCompanyService.getAllCompanies(sector, search, userId);
        return ResponseEntity.ok(ApiResponse.success(companies, "Market companies retrieved successfully"));
    }

    @GetMapping("/sectors")
    @Operation(summary = "List Active Sectors", description = "Returns distinct active sectors for filtering group companies")
    public ResponseEntity<ApiResponse<List<String>>> getSectors() {
        List<String> sectors = marketCompanyService.getAllSectors();
        return ResponseEntity.ok(ApiResponse.success(sectors, "Sectors retrieved successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Company Overview by ID", description = "Returns comprehensive company profile including leadership, highlights, shareholding, financial ratios, recent financials, announcements, and documents")
    public ResponseEntity<ApiResponse<CompanyOverviewDto>> getCompanyById(
            @PathVariable("id") Long companyId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = extractOptionalUserId(authHeader);
        CompanyOverviewDto overview = marketCompanyService.getCompanyOverviewById(companyId, userId);
        return ResponseEntity.ok(ApiResponse.success(overview, "Company overview retrieved successfully"));
    }

    @GetMapping("/symbol/{symbol}")
    @Operation(summary = "Get Company Overview by Trading Symbol", description = "Returns comprehensive company profile using trading symbol (e.g. ATG-H)")
    public ResponseEntity<ApiResponse<CompanyOverviewDto>> getCompanyBySymbol(
            @PathVariable("symbol") String symbol,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = extractOptionalUserId(authHeader);
        CompanyOverviewDto overview = marketCompanyService.getCompanyOverviewBySymbol(symbol, userId);
        return ResponseEntity.ok(ApiResponse.success(overview, "Company overview retrieved successfully"));
    }

    @GetMapping("/{id}/price-history")
    @Operation(summary = "Get Price Chart History (OHLC)", description = "Returns historical OHLC candlesticks and volume for price charts. Supported intervals: INTRADAY, DAY, WEEK, MONTH.")
    public ResponseEntity<ApiResponse<List<PriceCandleDto>>> getPriceHistory(
            @PathVariable("id") Long companyId,
            @Parameter(description = "Interval type: INTRADAY, DAY, WEEK, MONTH")
            @RequestParam(defaultValue = "DAY") String intervalType,
            @Parameter(description = "Start timestamp (ISO-8601)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @Parameter(description = "End timestamp (ISO-8601)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to) {

        List<PriceCandleDto> history = priceHistoryService.getPriceHistory(companyId, intervalType, from, to);
        return ResponseEntity.ok(ApiResponse.success(history, "Price history retrieved successfully"));
    }

    @GetMapping("/symbol/{symbol}/price-history")
    @Operation(summary = "Get Price Chart History by Symbol", description = "Returns historical OHLC candlesticks using trading symbol")
    public ResponseEntity<ApiResponse<List<PriceCandleDto>>> getPriceHistoryBySymbol(
            @PathVariable("symbol") String symbol,
            @RequestParam(defaultValue = "DAY") String intervalType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to) {

        List<PriceCandleDto> history = priceHistoryService.getPriceHistoryBySymbol(symbol, intervalType, from, to);
        return ResponseEntity.ok(ApiResponse.success(history, "Price history retrieved successfully"));
    }

    @GetMapping("/{id}/leadership")
    @Operation(summary = "Get Company Leadership", description = "Returns executive board and management leadership team")
    public ResponseEntity<ApiResponse<List<CompanyLeadership>>> getLeadership(@PathVariable("id") Long companyId) {
        List<CompanyLeadership> leadership = marketCompanyService.getLeadership(companyId);
        return ResponseEntity.ok(ApiResponse.success(leadership, "Leadership details retrieved successfully"));
    }

    @GetMapping("/{id}/highlights")
    @Operation(summary = "Get Key Highlights", description = "Returns business highlights and milestones")
    public ResponseEntity<ApiResponse<List<CompanyHighlight>>> getHighlights(@PathVariable("id") Long companyId) {
        List<CompanyHighlight> highlights = marketCompanyService.getHighlights(companyId);
        return ResponseEntity.ok(ApiResponse.success(highlights, "Company highlights retrieved successfully"));
    }

    @GetMapping("/{id}/shareholding")
    @Operation(summary = "Get Shareholding Pattern", description = "Returns percentage shareholding by Promoters, Group Companies, Employees, Others")
    public ResponseEntity<ApiResponse<List<CompanyShareholding>>> getShareholding(@PathVariable("id") Long companyId) {
        List<CompanyShareholding> shareholding = marketCompanyService.getShareholding(companyId);
        return ResponseEntity.ok(ApiResponse.success(shareholding, "Shareholding pattern retrieved successfully"));
    }

    @GetMapping("/{id}/financials/annual")
    @Operation(summary = "Get Annual Financials", description = "Returns annual historical financial performance (Revenue, EBITDA, Net Profit, Ratios)")
    public ResponseEntity<ApiResponse<List<CompanyAnnualFinancial>>> getAnnualFinancials(@PathVariable("id") Long companyId) {
        List<CompanyAnnualFinancial> annuals = marketCompanyService.getAnnualFinancials(companyId);
        return ResponseEntity.ok(ApiResponse.success(annuals, "Annual financials retrieved successfully"));
    }

    @GetMapping("/{id}/financials/quarterly")
    @Operation(summary = "Get Quarterly Financials", description = "Returns quarterly historical financial reports (Q1, Q2, etc.)")
    public ResponseEntity<ApiResponse<List<CompanyQuarterlyFinancial>>> getQuarterlyFinancials(@PathVariable("id") Long companyId) {
        List<CompanyQuarterlyFinancial> quarterlies = marketCompanyService.getQuarterlyFinancials(companyId);
        return ResponseEntity.ok(ApiResponse.success(quarterlies, "Quarterly financials retrieved successfully"));
    }

    @GetMapping("/{id}/ratios")
    @Operation(summary = "Get Key Financial Ratios", description = "Returns latest key ratios: EPS, P/E, Book Value, Debt/Equity, ROE, ROCE")
    public ResponseEntity<ApiResponse<List<CompanyFinancialRatio>>> getFinancialRatios(@PathVariable("id") Long companyId) {
        List<CompanyFinancialRatio> ratios = marketCompanyService.getFinancialRatios(companyId);
        return ResponseEntity.ok(ApiResponse.success(ratios, "Financial ratios retrieved successfully"));
    }

    @GetMapping("/{id}/announcements")
    @Operation(summary = "Get Company Announcements", description = "Returns corporate actions, board updates, results announcements")
    public ResponseEntity<ApiResponse<List<CompanyAnnouncement>>> getAnnouncements(@PathVariable("id") Long companyId) {
        List<CompanyAnnouncement> announcements = marketCompanyService.getAnnouncements(companyId);
        return ResponseEntity.ok(ApiResponse.success(announcements, "Announcements retrieved successfully"));
    }

    @GetMapping("/{id}/documents")
    @Operation(summary = "Get Company Disclosures & Documents", description = "Returns official published annual reports, financial statements, and regulatory filings")
    public ResponseEntity<ApiResponse<List<CompanyDocument>>> getDocuments(
            @PathVariable("id") Long companyId,
            @RequestParam(required = false) String category) {
        List<CompanyDocument> documents = marketCompanyService.getDocuments(companyId, category);
        return ResponseEntity.ok(ApiResponse.success(documents, "Documents retrieved successfully"));
    }

    private Long extractOptionalUserId(String authHeader) {
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring(7).trim();
                if (jwtTokenProvider.validateToken(token)) {
                    return jwtTokenProvider.getUserIdFromToken(token);
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }
}
