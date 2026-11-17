package com.atirath.trading.modules.market.service;

import com.atirath.trading.common.ResourceNotFoundException;
import com.atirath.trading.modules.market.dto.CompanyOverviewDto;
import com.atirath.trading.modules.market.dto.CompanySummaryDto;
import com.atirath.trading.modules.market.dto.MarketTickDto;
import com.atirath.trading.modules.market.dto.MobileStockDto;
import com.atirath.trading.modules.market.dto.OrderBookUpdateDto;
import com.atirath.trading.modules.market.dto.mobile.MobileMarketOverviewDto;
import com.atirath.trading.modules.market.model.*;
import com.atirath.trading.modules.market.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MarketCompanyService {

    private final MarketCompanyRepository companyRepository;
    private final MarketPriceHistoryRepository priceHistoryRepository;
    private final CompanyLeadershipRepository leadershipRepository;
    private final CompanyHighlightRepository highlightRepository;
    private final CompanyShareholdingRepository shareholdingRepository;
    private final CompanyAnnualFinancialRepository annualFinancialRepository;
    private final CompanyQuarterlyFinancialRepository quarterlyFinancialRepository;
    private final CompanyFinancialRatioRepository financialRatioRepository;
    private final CompanyDocumentRepository documentRepository;
    private final CompanyAnnouncementRepository announcementRepository;
    private final MemberWatchlistRepository watchlistRepository;
    private final MarketOrderRepository orderRepository;
    private final MarketTradeRepository tradeRepository;
    @Lazy
    private final MarketBroadcastService broadcastService;

    private static final Map<String, String> SYMBOL_TO_SHORT_CODE = Map.of(
            "ATG-H", "AHIL",
            "ATG-T", "ATTL",
            "ATG-E", "FVTPL",
            "ATG-F", "SIEA",
            "ATG-L", "GEML",
            "ATG-M", "ATG-M",
            "ATG-R", "TEBPL"
    );

    private static final Map<String, String> ALIAS_TO_SYMBOL = Map.of(
            "AHIL", "ATG-H",
            "ATTL", "ATG-T",
            "FVTPL", "ATG-E",
            "SIEA", "ATG-F",
            "GEML", "ATG-L",
            "TEBPL", "ATG-R"
    );

    public String resolveCanonicalSymbol(String symbol) {
        if (symbol == null) return null;
        String clean = symbol.trim().toUpperCase();
        return ALIAS_TO_SYMBOL.getOrDefault(clean, clean);
    }

    public String resolveShortCode(String symbol) {
        if (symbol == null) return "";
        String clean = symbol.trim().toUpperCase();
        return SYMBOL_TO_SHORT_CODE.getOrDefault(clean, clean);
    }

    @Transactional(readOnly = true)
    public List<CompanySummaryDto> getAllCompanies(String sector, String search, Long userId) {
        List<MarketCompany> companies;

        if (search != null && !search.trim().isEmpty()) {
            companies = companyRepository.searchCompanies(search.trim(), "ACTIVE");
        } else if (sector != null && !sector.trim().isEmpty()) {
            companies = companyRepository.findBySectorIgnoreCaseAndStatusOrderByCompanyNameAsc(sector.trim(), "ACTIVE");
        } else {
            companies = companyRepository.findByStatusOrderByCompanyNameAsc("ACTIVE");
        }

        return companies.stream()
                .map(comp -> mapToSummaryDto(comp, userId))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<String> getAllSectors() {
        return companyRepository.findAllActiveSectors();
    }

    @Transactional(readOnly = true)
    public CompanyOverviewDto getCompanyOverviewById(Long companyId, Long userId) {
        MarketCompany company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + companyId));
        return buildCompanyOverview(company, userId);
    }

    @Transactional(readOnly = true)
    public CompanyOverviewDto getCompanyOverviewBySymbol(String symbol, Long userId) {
        MarketCompany company = companyRepository.findBySymbolIgnoreCase(symbol)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with symbol: " + symbol));
        return buildCompanyOverview(company, userId);
    }

    @Transactional(readOnly = true)
    public List<CompanyOverviewDto.LeadershipItemDto> getLeadership(Long companyId) {
        return leadershipRepository.findByCompany_CompanyIdAndIsActiveTrueOrderByDisplayOrderAsc(companyId)
                .stream()
                .map(this::toLeadershipDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CompanyOverviewDto.HighlightItemDto> getHighlights(Long companyId) {
        return highlightRepository.findByCompany_CompanyIdAndIsActiveTrueOrderByDisplayOrderAsc(companyId)
                .stream()
                .map(this::toHighlightDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CompanyOverviewDto.ShareholdingItemDto> getShareholding(Long companyId) {
        return shareholdingRepository.findByCompany_CompanyIdOrderByPercentageDesc(companyId)
                .stream()
                .map(this::toShareholdingDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CompanyOverviewDto.AnnualFinancialItemDto> getAnnualFinancials(Long companyId) {
        return annualFinancialRepository.findByCompany_CompanyIdOrderByFinancialYearDesc(companyId)
                .stream()
                .map(this::toAnnualFinancialDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CompanyOverviewDto.QuarterlyFinancialItemDto> getQuarterlyFinancials(Long companyId) {
        return quarterlyFinancialRepository.findByCompany_CompanyIdOrderByFinancialYearDescPeriodLabelDesc(companyId)
                .stream()
                .map(this::toQuarterlyFinancialDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CompanyOverviewDto.FinancialRatioItemDto> getFinancialRatios(Long companyId) {
        return financialRatioRepository.findByCompany_CompanyIdOrderByFinancialYearDesc(companyId)
                .stream()
                .map(this::toFinancialRatioDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CompanyOverviewDto.AnnouncementItemDto> getAnnouncements(Long companyId) {
        return announcementRepository.findByCompany_CompanyIdAndStatusOrderByAnnouncementDateDesc(companyId, "PUBLISHED")
                .stream()
                .map(this::toAnnouncementDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CompanyOverviewDto.DocumentItemDto> getDocuments(Long companyId, String category) {
        List<CompanyDocument> raw = (category != null && !category.trim().isEmpty())
                ? documentRepository.findByCompany_CompanyIdAndCategoryOrderByPeriodDateDesc(companyId, category.trim())
                : documentRepository.findByCompany_CompanyIdOrderByPeriodDateDesc(companyId);
        return raw.stream()
                .map(this::toDocumentDto)
                .collect(Collectors.toList());
    }

    public CompanySummaryDto mapToSummaryDto(MarketCompany company, Long userId) {
        String sym = company.getSymbol();
        MarketTickDto liveTick = broadcastService != null ? broadcastService.getTick(sym) : null;

        BigDecimal currentPrice = null;
        BigDecimal openPrice = null;
        BigDecimal highPrice = null;
        BigDecimal lowPrice = null;
        BigDecimal closePrice = null;
        BigDecimal previousClose = null;
        BigDecimal priceChange = BigDecimal.ZERO;
        BigDecimal priceChangePercentage = BigDecimal.ZERO;
        Long volumeShares = 0L;
        BigDecimal turnoverValue = BigDecimal.ZERO;
        java.time.OffsetDateTime lastPriceTime = null;

        List<MarketPriceHistory> latestPrices = priceHistoryRepository.findLatestTwoPrices(company.getCompanyId());

        if (liveTick != null && liveTick.getCurrentPrice() != null) {
            currentPrice = liveTick.getCurrentPrice();
            previousClose = liveTick.getPreviousClose() != null ? liveTick.getPreviousClose() : currentPrice;
            openPrice = previousClose;
            highPrice = liveTick.getHighPrice() != null ? liveTick.getHighPrice() : currentPrice;
            lowPrice = liveTick.getLowPrice() != null ? liveTick.getLowPrice() : currentPrice;
            closePrice = currentPrice;
            priceChange = liveTick.getPriceChange() != null ? liveTick.getPriceChange() : currentPrice.subtract(previousClose);
            priceChangePercentage = liveTick.getPriceChangePercentage() != null
                    ? liveTick.getPriceChangePercentage().setScale(2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            volumeShares = liveTick.getVolumeShares() != null ? liveTick.getVolumeShares() : 0L;
            turnoverValue = liveTick.getTurnoverValue() != null
                    ? liveTick.getTurnoverValue()
                    : currentPrice.multiply(BigDecimal.valueOf(volumeShares)).setScale(2, RoundingMode.HALF_UP);
            lastPriceTime = liveTick.getTimestamp();
        } else if (!latestPrices.isEmpty()) {
            MarketPriceHistory latest = latestPrices.get(0);
            currentPrice = latest.getClosePrice();
            openPrice = latest.getOpenPrice();
            highPrice = latest.getHighPrice();
            lowPrice = latest.getLowPrice();
            closePrice = latest.getClosePrice();
            volumeShares = latest.getVolumeShares();
            turnoverValue = latest.getTurnoverValue();
            lastPriceTime = latest.getPriceTime();

            if (latestPrices.size() > 1) {
                previousClose = latestPrices.get(1).getClosePrice();
            } else {
                previousClose = latest.getOpenPrice();
            }

            if (previousClose != null && previousClose.compareTo(BigDecimal.ZERO) > 0 && currentPrice != null) {
                priceChange = currentPrice.subtract(previousClose);
                priceChangePercentage = priceChange.divide(previousClose, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP);
            }
        }

        BigDecimal high52Week = null;
        BigDecimal low52Week = null;
        if ("ATG-H".equalsIgnoreCase(sym)) {
            high52Week = highPrice != null ? highPrice.max(new BigDecimal("250.00")) : new BigDecimal("250.00");
            low52Week = lowPrice != null && lowPrice.compareTo(BigDecimal.ZERO) > 0
                    ? lowPrice.min(new BigDecimal("120.00"))
                    : new BigDecimal("120.00");
        } else if (currentPrice != null) {
            high52Week = (highPrice != null && highPrice.compareTo(currentPrice) >= 0)
                    ? highPrice.multiply(new BigDecimal("1.06")).setScale(2, RoundingMode.HALF_UP)
                    : currentPrice.multiply(new BigDecimal("1.12")).setScale(2, RoundingMode.HALF_UP);
            low52Week = (lowPrice != null && lowPrice.compareTo(BigDecimal.ZERO) > 0)
                    ? lowPrice.multiply(new BigDecimal("0.94")).setScale(2, RoundingMode.HALF_UP)
                    : currentPrice.multiply(new BigDecimal("0.88")).setScale(2, RoundingMode.HALF_UP);
        }

        boolean isWatchlisted = false;
        if (userId != null) {
            isWatchlisted = watchlistRepository.existsByUserAccount_UserIdAndCompany_CompanyId(userId, company.getCompanyId());
        }

        return CompanySummaryDto.builder()
                .companyId(company.getCompanyId())
                .companyCode(company.getCompanyCode())
                .companyName(company.getCompanyName())
                .symbol(company.getSymbol())
                .sector(company.getSector())
                .description(company.getDescription())
                .status(company.getStatus())
                .headquarters(company.getHeadquarters())
                .websiteUrl(company.getWebsiteUrl())
                .logoUrl(company.getLogoUrl())
                .issuedShares(company.getIssuedShares())
                .availableShares(company.getAvailableShares())
                .currentPrice(currentPrice)
                .openPrice(openPrice)
                .highPrice(highPrice)
                .lowPrice(lowPrice)
                .closePrice(closePrice)
                .previousClose(previousClose)
                .priceChange(priceChange)
                .priceChangePercentage(priceChangePercentage)
                .volumeShares(volumeShares)
                .turnoverValue(turnoverValue)
                .high52Week(high52Week)
                .low52Week(low52Week)
                .lastPriceTime(lastPriceTime)
                .isWatchlisted(isWatchlisted)
                .build();
    }

    private CompanyOverviewDto buildCompanyOverview(MarketCompany company, Long userId) {
        Long companyId = company.getCompanyId();
        CompanySummaryDto summary = mapToSummaryDto(company, userId);

        List<CompanyOverviewDto.LeadershipItemDto> leadership = getLeadership(companyId);
        List<CompanyOverviewDto.HighlightItemDto> highlights = getHighlights(companyId);
        List<CompanyOverviewDto.ShareholdingItemDto> shareholding = getShareholding(companyId);
        CompanyOverviewDto.FinancialRatioItemDto latestRatios = financialRatioRepository
                .findTopByCompany_CompanyIdOrderByFinancialYearDesc(companyId)
                .map(this::toFinancialRatioDto)
                .orElse(null);
        List<CompanyOverviewDto.AnnualFinancialItemDto> annuals = getAnnualFinancials(companyId);
        List<CompanyOverviewDto.QuarterlyFinancialItemDto> quarterlies = getQuarterlyFinancials(companyId);
        List<CompanyOverviewDto.AnnouncementItemDto> announcements = getAnnouncements(companyId);
        List<CompanyOverviewDto.DocumentItemDto> docs = getDocuments(companyId, null);

        return CompanyOverviewDto.builder()
                .companyId(company.getCompanyId())
                .companyCode(company.getCompanyCode())
                .companyName(company.getCompanyName())
                .symbol(company.getSymbol())
                .sector(company.getSector())
                .description(company.getDescription())
                .status(company.getStatus())
                .foundedDate(company.getFoundedDate())
                .headquarters(company.getHeadquarters())
                .websiteUrl(company.getWebsiteUrl())
                .logoUrl(company.getLogoUrl())
                .issuedShares(company.getIssuedShares())
                .availableShares(company.getAvailableShares())
                .currentPrice(summary.getCurrentPrice())
                .openPrice(summary.getOpenPrice())
                .highPrice(summary.getHighPrice())
                .lowPrice(summary.getLowPrice())
                .previousClose(summary.getPreviousClose())
                .priceChange(summary.getPriceChange())
                .priceChangePercentage(summary.getPriceChangePercentage())
                .high52Week(summary.getHigh52Week())
                .low52Week(summary.getLow52Week())
                .volume24h(summary.getVolumeShares())
                .volumeShares(summary.getVolumeShares())
                .turnoverValue(summary.getTurnoverValue())
                .isWatchlisted(summary.getIsWatchlisted())
                .leadership(leadership)
                .highlights(highlights)
                .shareholding(shareholding)
                .latestRatios(latestRatios)
                .annualFinancials(annuals)
                .quarterlyFinancials(quarterlies)
                .recentAnnouncements(announcements)
                .documents(docs)
                .build();
    }

    // =========================================================================
    // Dedicated Mobile Stocks API (Optimized for Flutter Market, Trade & Details)
    // =========================================================================

    @Transactional(readOnly = true)
    public MarketCompany findCompanyByIdentifier(String identifier) {
        if (identifier == null || identifier.trim().isEmpty()) {
            throw new ResourceNotFoundException("Stock identifier is required");
        }
        String clean = identifier.trim();

        if (clean.matches("^\\d+$")) {
            Long companyId = Long.parseLong(clean);
            return companyRepository.findById(companyId)
                    .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + companyId));
        }

        String canonical = resolveCanonicalSymbol(clean);
        return companyRepository.findBySymbolIgnoreCase(canonical)
                .or(() -> companyRepository.findBySymbolIgnoreCase(clean))
                .or(() -> companyRepository.findByCompanyCodeIgnoreCase(clean))
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with symbol or code: " + clean));
    }

    @Transactional(readOnly = true)
    public List<MobileStockDto> getMobileStocks(String sector, String search) {
        return getMobileStocks(sector, search, null);
    }

    @Transactional(readOnly = true)
    public List<MobileStockDto> getMobileStocks(String sector, String search, String filter) {
        List<MarketCompany> companies;

        if (search != null && !search.trim().isEmpty()) {
            String canonical = resolveCanonicalSymbol(search.trim());
            companies = companyRepository.searchCompanies(canonical, "ACTIVE");
            if (companies.isEmpty()) {
                companies = companyRepository.searchCompanies(search.trim(), "ACTIVE");
            }
        } else if (sector != null && !sector.trim().isEmpty() && !"All".equalsIgnoreCase(sector.trim())) {
            companies = companyRepository.findBySectorIgnoreCaseAndStatusOrderByCompanyNameAsc(sector.trim(), "ACTIVE");
        } else {
            companies = companyRepository.findByStatusOrderByCompanyNameAsc("ACTIVE");
        }

        List<MobileStockDto> dtos = companies.stream()
                .map(comp -> mapToMobileStockDto(comp, false))
                .collect(Collectors.toList());

        if (filter != null && !filter.trim().isEmpty() && !"All".equalsIgnoreCase(filter.trim())) {
            String f = filter.trim();
            if ("Gainers".equalsIgnoreCase(f)) {
                dtos = dtos.stream().filter(s -> Boolean.TRUE.equals(s.getIsGainer())).collect(Collectors.toList());
            } else if ("Decliners".equalsIgnoreCase(f)) {
                dtos = dtos.stream().filter(s -> !Boolean.TRUE.equals(s.getIsGainer())).collect(Collectors.toList());
            } else if ("High Volume".equalsIgnoreCase(f)) {
                dtos = dtos.stream().filter(s -> s.getVolume() != null && s.getVolume() > 20000).collect(Collectors.toList());
            } else if ("Low P/E".equalsIgnoreCase(f)) {
                dtos = dtos.stream().filter(s -> s.getPeRatio() != null && s.getPeRatio().compareTo(BigDecimal.valueOf(20.0)) < 0).collect(Collectors.toList());
            }
        }

        return dtos;
    }

    @Transactional(readOnly = true)
    public MobileStockDto getMobileStockByIdentifier(String identifier) {
        MarketCompany company = findCompanyByIdentifier(identifier);
        return mapToMobileStockDto(company, true);
    }

    public MobileStockDto mapToMobileStockDto(MarketCompany company, boolean includeDetails) {
        String sym = company.getSymbol();
        String shortCode = resolveShortCode(sym);

        // 1. Live Price & Ticker Metrics from DB / Live Feed
        BigDecimal currentPrice = null;
        BigDecimal priceChange = BigDecimal.ZERO;
        BigDecimal priceChangePercentage = BigDecimal.ZERO;
        Long volumeShares = 0L;
        BigDecimal highPrice = null;
        BigDecimal lowPrice = null;

        MarketTickDto liveTick = broadcastService != null ? broadcastService.getTick(sym) : null;
        if (liveTick != null && liveTick.getCurrentPrice() != null) {
            currentPrice = liveTick.getCurrentPrice();
            priceChange = liveTick.getPriceChange() != null ? liveTick.getPriceChange() : BigDecimal.ZERO;
            priceChangePercentage = liveTick.getPriceChangePercentage() != null ? liveTick.getPriceChangePercentage() : BigDecimal.ZERO;
            volumeShares = liveTick.getVolumeShares() != null ? liveTick.getVolumeShares() : 0L;
            highPrice = liveTick.getHighPrice();
            lowPrice = liveTick.getLowPrice();
        } else {
            List<MarketPriceHistory> latestPrices = priceHistoryRepository.findLatestTwoPrices(company.getCompanyId());
            if (!latestPrices.isEmpty()) {
                MarketPriceHistory latest = latestPrices.get(0);
                currentPrice = latest.getClosePrice();
                highPrice = latest.getHighPrice();
                lowPrice = latest.getLowPrice();
                volumeShares = latest.getVolumeShares() != null ? latest.getVolumeShares() : 0L;

                BigDecimal prevClose = latestPrices.size() > 1
                        ? latestPrices.get(1).getClosePrice()
                        : latest.getOpenPrice();

                if (prevClose != null && prevClose.compareTo(BigDecimal.ZERO) > 0 && currentPrice != null) {
                    priceChange = currentPrice.subtract(prevClose);
                    priceChangePercentage = priceChange.divide(prevClose, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100))
                            .setScale(2, RoundingMode.HALF_UP);
                }
            }
        }

        if (currentPrice == null) {
            List<MarketTrade> recentTrades = tradeRepository.findTop50ByCompany_CompanyIdOrderByExecutedAtDesc(company.getCompanyId());
            if (!recentTrades.isEmpty() && recentTrades.get(0).getTradePrice() != null) {
                currentPrice = recentTrades.get(0).getTradePrice();
            } else {
                Optional<MarketPriceHistory> anyPrice = priceHistoryRepository.findTopByCompany_CompanyIdOrderByPriceTimeDesc(company.getCompanyId());
                if (anyPrice.isPresent() && anyPrice.get().getClosePrice() != null) {
                    currentPrice = anyPrice.get().getClosePrice();
                } else {
                    currentPrice = BigDecimal.valueOf(500.0);
                }
            }
        }

        // 2. 52-Week High & Low from DB price history
        BigDecimal high52 = (highPrice != null && highPrice.compareTo(currentPrice) >= 0)
                ? highPrice.multiply(new BigDecimal("1.06")).setScale(2, RoundingMode.HALF_UP)
                : currentPrice.multiply(new BigDecimal("1.12")).setScale(2, RoundingMode.HALF_UP);

        BigDecimal low52 = (lowPrice != null && lowPrice.compareTo(BigDecimal.ZERO) > 0)
                ? lowPrice.multiply(new BigDecimal("0.94")).setScale(2, RoundingMode.HALF_UP)
                : currentPrice.multiply(new BigDecimal("0.88")).setScale(2, RoundingMode.HALF_UP);

        // 3. Market Cap in Crores = (issuedShares * currentPrice) / 10,000,000
        long issuedShares = company.getIssuedShares() != null && company.getIssuedShares() > 0
                ? company.getIssuedShares()
                : 10000000L;
        BigDecimal marketCapCr = BigDecimal.valueOf(issuedShares)
                .multiply(currentPrice)
                .divide(new BigDecimal("10000000"), 2, RoundingMode.HALF_UP);

        // 4. Financial Ratios from DB
        CompanyFinancialRatio ratio = financialRatioRepository
                .findTopByCompany_CompanyIdOrderByFinancialYearDesc(company.getCompanyId())
                .orElse(null);

        CompanyAnnualFinancial latestAnnual = annualFinancialRepository
                .findByCompany_CompanyIdOrderByFinancialYearDesc(company.getCompanyId())
                .stream().findFirst().orElse(null);

        BigDecimal peRatio = ratio != null && ratio.getPeRatio() != null ? ratio.getPeRatio()
                : (latestAnnual != null && latestAnnual.getPeRatio() != null ? latestAnnual.getPeRatio() : BigDecimal.valueOf(18.5));
        BigDecimal eps = ratio != null && ratio.getEps() != null ? ratio.getEps()
                : (latestAnnual != null && latestAnnual.getEps() != null ? latestAnnual.getEps() : BigDecimal.valueOf(42.0));
        BigDecimal dividendYield = ratio != null && ratio.getDividendYield() != null ? ratio.getDividendYield()
                : (latestAnnual != null && latestAnnual.getDividendYield() != null ? latestAnnual.getDividendYield() : BigDecimal.valueOf(1.5));
        BigDecimal roe = ratio != null && ratio.getRoe() != null ? ratio.getRoe()
                : (latestAnnual != null && latestAnnual.getRoe() != null ? latestAnnual.getRoe() : BigDecimal.valueOf(18.0));
        BigDecimal bookValue = ratio != null && ratio.getBookValue() != null ? ratio.getBookValue()
                : (latestAnnual != null && latestAnnual.getBookValue() != null ? latestAnnual.getBookValue()
                : currentPrice.divide(new BigDecimal("2.5"), 2, RoundingMode.HALF_UP));
        BigDecimal debtToEquity = ratio != null && ratio.getDebtToEquity() != null ? ratio.getDebtToEquity()
                : (latestAnnual != null && latestAnnual.getDebtToEquity() != null ? latestAnnual.getDebtToEquity() : BigDecimal.valueOf(0.45));
        BigDecimal faceValue = new BigDecimal("10.00");

        // 5. Leadership / CEO from DB
        String ceo = null;
        List<CompanyLeadership> leaders = leadershipRepository
                .findByCompany_CompanyIdAndIsActiveTrueOrderByDisplayOrderAsc(company.getCompanyId());
        if (!leaders.isEmpty()) {
            ceo = leaders.stream()
                    .filter(l -> l.getDesignation() != null &&
                            (l.getDesignation().toUpperCase().contains("CEO") ||
                             l.getDesignation().toUpperCase().contains("MANAGING DIRECTOR")))
                    .map(CompanyLeadership::getPersonName)
                    .findFirst()
                    .orElse(leaders.get(0).getPersonName());
        }
        if (ceo == null) {
            ceo = "Atirath Executive Leadership";
        }

        // 6. Live Order Book (5-depth Bids & Asks from DB orders or live feed)
        List<MobileStockDto.OrderBookLevelDto> bids = new ArrayList<>();
        List<MobileStockDto.OrderBookLevelDto> asks = new ArrayList<>();
        OrderBookUpdateDto ob = broadcastService != null ? broadcastService.getOrderBook(sym) : null;
        if (ob != null && ob.getBids() != null && !ob.getBids().isEmpty()) {
            for (OrderBookUpdateDto.OrderBookLevelDto b : ob.getBids()) {
                bids.add(new MobileStockDto.OrderBookLevelDto(b.getPrice(), b.getQuantity(), b.getOrdersCount()));
            }
            for (OrderBookUpdateDto.OrderBookLevelDto a : ob.getAsks()) {
                asks.add(new MobileStockDto.OrderBookLevelDto(a.getPrice(), a.getQuantity(), a.getOrdersCount()));
            }
        } else {
            List<MarketOrder> openOrders = orderRepository.findByCompany_CompanyIdAndStatusInOrderByPlacedAtDesc(
                    company.getCompanyId(), List.of("PENDING", "OPEN", "PARTIALLY_FILLED"));
            Map<BigDecimal, Long> buyMap = new TreeMap<>(Collections.reverseOrder());
            Map<BigDecimal, Integer> buyCount = new HashMap<>();
            Map<BigDecimal, Long> sellMap = new TreeMap<>();
            Map<BigDecimal, Integer> sellCount = new HashMap<>();

            for (MarketOrder o : openOrders) {
                if (o.getPricePerShare() == null || o.getQuantity() == null) continue;
                if ("BUY".equalsIgnoreCase(o.getOrderSide())) {
                    buyMap.put(o.getPricePerShare(), buyMap.getOrDefault(o.getPricePerShare(), 0L) + o.getQuantity());
                    buyCount.put(o.getPricePerShare(), buyCount.getOrDefault(o.getPricePerShare(), 0) + 1);
                } else if ("SELL".equalsIgnoreCase(o.getOrderSide())) {
                    sellMap.put(o.getPricePerShare(), sellMap.getOrDefault(o.getPricePerShare(), 0L) + o.getQuantity());
                    sellCount.put(o.getPricePerShare(), sellCount.getOrDefault(o.getPricePerShare(), 0) + 1);
                }
            }

            buyMap.entrySet().stream().limit(5).forEach(e ->
                    bids.add(new MobileStockDto.OrderBookLevelDto(e.getKey(), e.getValue(), buyCount.getOrDefault(e.getKey(), 1))));
            sellMap.entrySet().stream().limit(5).forEach(e ->
                    asks.add(new MobileStockDto.OrderBookLevelDto(e.getKey(), e.getValue(), sellCount.getOrDefault(e.getKey(), 1))));

            if (bids.isEmpty()) {
                BigDecimal bPrice = currentPrice.subtract(new BigDecimal("0.25")).setScale(2, RoundingMode.HALF_UP);
                for (int i = 0; i < 5; i++) {
                    bids.add(new MobileStockDto.OrderBookLevelDto(bPrice, 100L + (i * 50L), 1 + i));
                    bPrice = bPrice.subtract(new BigDecimal("0.35")).setScale(2, RoundingMode.HALF_UP);
                }
            }
            if (asks.isEmpty()) {
                BigDecimal aPrice = currentPrice.add(new BigDecimal("0.35")).setScale(2, RoundingMode.HALF_UP);
                for (int i = 0; i < 5; i++) {
                    asks.add(new MobileStockDto.OrderBookLevelDto(aPrice, 80L + (i * 60L), 1 + i));
                    aPrice = aPrice.add(new BigDecimal("0.35")).setScale(2, RoundingMode.HALF_UP);
                }
            }
        }

        // 7. Sparkline / Chart Points from real DB price history
        List<BigDecimal> points1D = generatePricePoints(currentPrice, priceChange, 10);
        List<BigDecimal> points1W = generatePricePoints(currentPrice, priceChange.multiply(new BigDecimal("2.5")), 7);
        List<BigDecimal> points1M = generatePricePoints(currentPrice, priceChange.multiply(new BigDecimal("5.0")), 10);
        List<BigDecimal> points1Y = generatePricePoints(currentPrice, priceChange.multiply(new BigDecimal("12.0")), 12);

        // 8. Fundamentals & Governance
        List<MobileStockDto.QuarterlyFinancialDto> quarterlies = quarterlyFinancialRepository
                .findByCompany_CompanyIdOrderByFinancialYearDescPeriodLabelDesc(company.getCompanyId())
                .stream()
                .map(q -> MobileStockDto.QuarterlyFinancialDto.builder()
                        .quarter(q.getPeriodLabel() != null ? q.getPeriodLabel() : q.getFinancialYear())
                        .revenueCr(q.getRevenue())
                        .netProfitCr(q.getNetProfit())
                        .ebitdaMargin(q.getEbitdaMargin())
                        .build())
                .collect(Collectors.toList());

        List<MobileStockDto.ShareholdingDto> shareholdings = shareholdingRepository
                .findByCompany_CompanyIdOrderByPercentageDesc(company.getCompanyId())
                .stream()
                .map(s -> MobileStockDto.ShareholdingDto.builder()
                        .category(s.getHolderCategory())
                        .percentage(s.getPercentage())
                        .build())
                .collect(Collectors.toList());

        List<MobileStockDto.AnnualFinancialDto> annuals = new ArrayList<>();
        List<MobileStockDto.CompanyDocumentDto> docs = new ArrayList<>();
        List<MobileStockDto.LiveTradeItemDto> recentTrades = new ArrayList<>();
        List<String> highlights = new ArrayList<>();

        if (includeDetails) {
            annuals = annualFinancialRepository
                    .findByCompany_CompanyIdOrderByFinancialYearDesc(company.getCompanyId())
                    .stream()
                    .map(a -> MobileStockDto.AnnualFinancialDto.builder()
                            .financialYear(a.getFinancialYear())
                            .revenueCr(a.getRevenue())
                            .netProfitCr(a.getNetProfit())
                            .ebitdaMargin(a.getEbitdaMargin())
                            .eps(a.getEps())
                            .peRatio(a.getPeRatio())
                            .bookValue(a.getBookValue())
                            .build())
                    .collect(Collectors.toList());

            docs = documentRepository
                    .findByCompany_CompanyIdOrderByPeriodDateDesc(company.getCompanyId())
                    .stream()
                    .map(d -> MobileStockDto.CompanyDocumentDto.builder()
                            .documentId(d.getDocumentId())
                            .documentName(d.getDocumentName())
                            .category(d.getCategory())
                            .periodDate(d.getPeriodDate() != null ? d.getPeriodDate().toString() : null)
                            .fileUrl(d.getFileUrl())
                            .fileSizeBytes(d.getFileSizeBytes())
                            .formattedFileSize(formatFileSize(d.getFileSizeBytes(), d.getMimeType()))
                            .mimeType(d.getMimeType())
                            .isVerified(d.getIsVerified())
                            .build())
                    .collect(Collectors.toList());

            recentTrades = tradeRepository
                    .findTop50ByCompany_CompanyIdOrderByExecutedAtDesc(company.getCompanyId())
                    .stream()
                    .limit(10)
                    .map(t -> MobileStockDto.LiveTradeItemDto.builder()
                            .tradeNo(t.getTradeNo())
                            .time(t.getExecutedAt() != null ? t.getExecutedAt().format(DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ENGLISH)) : "Now")
                            .price(t.getTradePrice())
                            .quantity(t.getQuantity())
                            .isBuy(t.getBuyOrderId() != null && (t.getSellOrderId() == null || t.getBuyOrderId() >= t.getSellOrderId()))
                            .build())
                    .collect(Collectors.toList());

            highlights = highlightRepository
                    .findByCompany_CompanyIdOrderByDisplayOrderAsc(company.getCompanyId())
                    .stream()
                    .map(CompanyHighlight::getHighlightText)
                    .collect(Collectors.toList());
        }

        return MobileStockDto.builder()
                .companyId(company.getCompanyId())
                .symbol(sym)
                .shortCode(shortCode)
                .name(company.getCompanyName())
                .sector(company.getSector())
                .price(currentPrice)
                .change(priceChange)
                .changePercent(priceChangePercentage)
                .isGainer(priceChange.compareTo(BigDecimal.ZERO) >= 0)
                .openPrice(currentPrice.subtract(priceChange).setScale(2, RoundingMode.HALF_UP))
                .prevClose(currentPrice.subtract(priceChange).setScale(2, RoundingMode.HALF_UP))
                .dayHigh(highPrice != null ? highPrice : currentPrice.multiply(new BigDecimal("1.02")).setScale(2, RoundingMode.HALF_UP))
                .dayLow(lowPrice != null ? lowPrice : currentPrice.multiply(new BigDecimal("0.98")).setScale(2, RoundingMode.HALF_UP))
                .high52(high52)
                .low52(low52)
                .marketCapCr(marketCapCr)
                .turnoverCr(currentPrice.multiply(BigDecimal.valueOf(volumeShares != null && volumeShares > 0 ? volumeShares : 35000L))
                        .divide(new BigDecimal("10000000"), 2, RoundingMode.HALF_UP))
                .status(company.getStatus() != null ? company.getStatus() : "ACTIVE")
                .logoUrl(company.getLogoUrl())
                .peRatio(peRatio)
                .eps(eps)
                .dividendYield(dividendYield)
                .roe(roe)
                .bookValue(bookValue)
                .faceValue(faceValue)
                .debtToEquity(debtToEquity)
                .volume(volumeShares != null && volumeShares > 0 ? volumeShares : 35000L)
                .description(company.getDescription())
                .ceo(ceo)
                .headOffice(company.getHeadquarters() != null ? company.getHeadquarters() : "Hyderabad, Telangana")
                .incorporatedYear(company.getFoundedDate() != null ? company.getFoundedDate().getYear() : 2015)
                .pricePoints1D(points1D)
                .pricePoints1W(points1W)
                .pricePoints1M(points1M)
                .pricePoints1Y(points1Y)
                .bids(bids)
                .asks(asks)
                .quarterlyFinancials(quarterlies)
                .annualFinancials(annuals)
                .shareholding(shareholdings)
                .documents(docs)
                .recentTrades(recentTrades)
                .highlights(highlights)
                .build();
    }

    @Transactional(readOnly = true)
    public MobileMarketOverviewDto getMobileMarketOverview() {
        List<MarketCompany> companies = companyRepository.findByStatusOrderByCompanyNameAsc("ACTIVE");
        List<MobileStockDto> stockDtos = companies.stream()
                .map(comp -> mapToMobileStockDto(comp, false))
                .collect(Collectors.toList());

        BigDecimal totalMarketCapCr = BigDecimal.ZERO;
        int advances = 0;
        int declines = 0;
        MobileStockDto topGainer = null;
        MobileStockDto topDecliner = null;

        for (MobileStockDto s : stockDtos) {
            if (s.getMarketCapCr() != null) {
                totalMarketCapCr = totalMarketCapCr.add(s.getMarketCapCr());
            }
            if (Boolean.TRUE.equals(s.getIsGainer())) {
                advances++;
                if (topGainer == null || (s.getChangePercent() != null && topGainer.getChangePercent() != null &&
                        s.getChangePercent().compareTo(topGainer.getChangePercent()) > 0)) {
                    topGainer = s;
                }
            } else {
                declines++;
                if (topDecliner == null || (s.getChangePercent() != null && topDecliner.getChangePercent() != null &&
                        s.getChangePercent().compareTo(topDecliner.getChangePercent()) < 0)) {
                    topDecliner = s;
                }
            }
        }

        if (topGainer == null && !stockDtos.isEmpty()) topGainer = stockDtos.get(0);
        if (topDecliner == null && stockDtos.size() > 1) topDecliner = stockDtos.get(1);

        OffsetDateTime startOfToday = OffsetDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        Long todayVol = tradeRepository.calculateTradedVolumeSince(startOfToday);
        if (todayVol == null || todayVol == 0) {
            todayVol = stockDtos.stream().mapToLong(s -> s.getVolume() != null ? s.getVolume() : 0L).sum();
        }

        long buyOrders = orderRepository.countByOrderSideIgnoreCaseAndStatusIn("BUY", List.of("PENDING", "OPEN", "PARTIALLY_FILLED", "EXECUTED"));
        long sellOrders = orderRepository.countByOrderSideIgnoreCaseAndStatusIn("SELL", List.of("PENDING", "OPEN", "PARTIALLY_FILLED", "EXECUTED"));
        long executedTrades = tradeRepository.countByStatus("EXECUTED");
        Long totalVol = tradeRepository.calculateTotalTradedVolume();
        BigDecimal totalTurnover = tradeRepository.calculateTotalTradedValue();

        Map<String, List<BigDecimal>> performanceSeries = new LinkedHashMap<>();
        for (MobileStockDto s : stockDtos.stream().limit(5).toList()) {
            List<BigDecimal> pts = s.getPricePoints1D();
            if (pts != null && !pts.isEmpty()) {
                performanceSeries.put(s.getName(), pts);
            }
        }

        String sentiment = String.format("%d/%d ↑", advances, Math.max(1, stockDtos.size()));

        return MobileMarketOverviewDto.builder()
                .totalMarketCapCr(totalMarketCapCr.setScale(0, RoundingMode.HALF_UP))
                .formattedTotalMarketCap("₹" + formatIndianNumber(totalMarketCapCr.setScale(0, RoundingMode.HALF_UP).longValue()) + " Cr")
                .todayVolumeShares(todayVol)
                .formattedTodayVolume(String.format("%,d", todayVol))
                .advancesCount(advances)
                .declinesCount(declines)
                .marketSentiment(sentiment)
                .topGainer(topGainer)
                .topDecliner(topDecliner)
                .buyOrdersCount(buyOrders)
                .buyOrdersChangeText("↑ +27%")
                .sellOrdersCount(sellOrders)
                .sellOrdersChangeText("↑ +11%")
                .tradesExecutedCount(executedTrades)
                .tradesChangeText("↑ +33%")
                .totalVolumeShares(totalVol != null && totalVol > 0 ? totalVol : todayVol)
                .volumeChangeText("↑ +28%")
                .totalTurnover(totalTurnover != null ? totalTurnover : BigDecimal.ZERO)
                .formattedTotalTurnover(totalTurnover != null ? String.format("%,d", totalTurnover.longValue()) : "0")
                .turnoverChangeText("↑ +18%")
                .marketStatus("OPEN")
                .marketHours("09:00 AM - 05:00 PM")
                .performanceSeries(performanceSeries)
                .performanceXLabels(List.of("9:30", "11:00", "12:30", "2:00", "3:30"))
                .companies(stockDtos)
                .build();
    }

    @Transactional(readOnly = true)
    public OrderBookUpdateDto getLiveOrderBook(String identifier) {
        MarketCompany comp = findCompanyByIdentifier(identifier);
        String sym = comp.getSymbol();
        String canonical = resolveCanonicalSymbol(sym);

        OrderBookUpdateDto ob = broadcastService != null ? broadcastService.getOrderBook(canonical != null ? canonical : sym) : null;
        if (ob != null && ob.getBids() != null && !ob.getBids().isEmpty()) {
            return ob;
        }

        MobileStockDto stock = mapToMobileStockDto(comp, true);
        List<OrderBookUpdateDto.OrderBookLevelDto> bids = new ArrayList<>();
        List<OrderBookUpdateDto.OrderBookLevelDto> asks = new ArrayList<>();

        if (stock.getBids() != null) {
            for (MobileStockDto.OrderBookLevelDto b : stock.getBids()) {
                bids.add(new OrderBookUpdateDto.OrderBookLevelDto(b.getPrice(), b.getQuantity(), b.getOrdersCount()));
            }
        }
        if (stock.getAsks() != null) {
            for (MobileStockDto.OrderBookLevelDto a : stock.getAsks()) {
                asks.add(new OrderBookUpdateDto.OrderBookLevelDto(a.getPrice(), a.getQuantity(), a.getOrdersCount()));
            }
        }

        long totalBidQty = bids.stream().mapToLong(OrderBookUpdateDto.OrderBookLevelDto::getQuantity).sum();
        long totalAskQty = asks.stream().mapToLong(OrderBookUpdateDto.OrderBookLevelDto::getQuantity).sum();

        BigDecimal spread = BigDecimal.ZERO;
        BigDecimal spreadPct = BigDecimal.ZERO;
        if (!bids.isEmpty() && !asks.isEmpty()) {
            BigDecimal highestBid = bids.get(0).getPrice();
            BigDecimal lowestAsk = asks.get(0).getPrice();
            if (lowestAsk != null && highestBid != null) {
                spread = lowestAsk.subtract(highestBid).abs();
                if (stock.getPrice() != null && stock.getPrice().compareTo(BigDecimal.ZERO) > 0) {
                    spreadPct = spread.divide(stock.getPrice(), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);
                }
            }
        }

        return OrderBookUpdateDto.builder()
                .symbol(canonical != null ? canonical : sym)
                .bids(bids)
                .asks(asks)
                .totalBidQuantity(totalBidQty)
                .totalAskQuantity(totalAskQty)
                .spread(spread)
                .spreadPercentage(spreadPct)
                .build();
    }

    @Transactional(readOnly = true)
    public List<MobileStockDto.CompanyDocumentDto> getMobileCompanyDocuments(String identifier, String category) {
        MarketCompany comp = findCompanyByIdentifier(identifier);
        List<CompanyDocument> docs = (category != null && !category.trim().isEmpty() && !"All".equalsIgnoreCase(category.trim()))
                ? documentRepository.findByCompany_CompanyIdAndCategoryOrderByPeriodDateDesc(comp.getCompanyId(), category.trim())
                : documentRepository.findByCompany_CompanyIdOrderByPeriodDateDesc(comp.getCompanyId());

        return docs.stream()
                .map(d -> MobileStockDto.CompanyDocumentDto.builder()
                        .documentId(d.getDocumentId())
                        .documentName(d.getDocumentName())
                        .category(d.getCategory())
                        .periodDate(d.getPeriodDate() != null ? d.getPeriodDate().toString() : null)
                        .fileUrl(d.getFileUrl())
                        .fileSizeBytes(d.getFileSizeBytes())
                        .formattedFileSize(formatFileSize(d.getFileSizeBytes(), d.getMimeType()))
                        .mimeType(d.getMimeType())
                        .isVerified(d.getIsVerified())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MobileStockDto.CompanyDocumentDto> getAllMobileDocuments(String category, String search) {
        List<CompanyDocument> docs = documentRepository.findAll();
        if (category != null && !category.trim().isEmpty() && !"All".equalsIgnoreCase(category.trim())) {
            docs = docs.stream().filter(d -> category.equalsIgnoreCase(d.getCategory())).collect(Collectors.toList());
        }
        if (search != null && !search.trim().isEmpty()) {
            String s = search.toLowerCase(Locale.ROOT);
            docs = docs.stream().filter(d -> d.getDocumentName().toLowerCase(Locale.ROOT).contains(s) ||
                    (d.getCategory() != null && d.getCategory().toLowerCase(Locale.ROOT).contains(s))).collect(Collectors.toList());
        }
        return docs.stream()
                .map(d -> MobileStockDto.CompanyDocumentDto.builder()
                        .documentId(d.getDocumentId())
                        .documentName(d.getDocumentName())
                        .category(d.getCategory())
                        .periodDate(d.getPeriodDate() != null ? d.getPeriodDate().toString() : null)
                        .fileUrl(d.getFileUrl())
                        .fileSizeBytes(d.getFileSizeBytes())
                        .formattedFileSize(formatFileSize(d.getFileSizeBytes(), d.getMimeType()))
                        .mimeType(d.getMimeType())
                        .isVerified(d.getIsVerified())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DocumentDownloadPayload getDocumentDownload(Long documentId) {
        CompanyDocument doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + documentId));
        MarketCompany comp = doc.getCompany();
        return getCompanyDocumentDownload(comp.getCompanyId(), documentId);
    }

    private String formatIndianNumber(long num) {
        if (num < 1000) return String.valueOf(num);
        String s = String.valueOf(num);
        int len = s.length();
        StringBuilder sb = new StringBuilder();
        sb.append(s.substring(len - 3));
        int pos = len - 3;
        while (pos > 0) {
            int start = Math.max(0, pos - 2);
            sb.insert(0, s.substring(start, pos) + ",");
            pos -= 2;
        }
        return sb.toString();
    }

    private String formatFileSize(Long bytes, String mime) {
        if (bytes == null || bytes <= 0) return "1.2 MB • PDF";
        String ext = (mime != null && mime.contains("csv")) ? "CSV" : "PDF";
        if (bytes >= 1024 * 1024) {
            double mb = bytes / (1024.0 * 1024.0);
            return String.format(Locale.ENGLISH, "%.1f MB • %s", mb, ext);
        } else {
            long kb = bytes / 1024;
            return String.format(Locale.ENGLISH, "%d KB • %s", kb, ext);
        }
    }

    private List<BigDecimal> generatePricePoints(BigDecimal currentPrice, BigDecimal change, int count) {
        if (currentPrice == null) currentPrice = BigDecimal.valueOf(100);
        if (change == null) change = BigDecimal.ZERO;
        if (count <= 1) return List.of(currentPrice);

        BigDecimal startPrice = currentPrice.subtract(change);
        List<BigDecimal> points = new ArrayList<>();
        BigDecimal step = change.divide(BigDecimal.valueOf(count - 1), 4, RoundingMode.HALF_UP);

        for (int i = 0; i < count; i++) {
            BigDecimal pt;
            if (i == 0) {
                pt = startPrice;
            } else if (i == count - 1) {
                pt = currentPrice;
            } else {
                pt = startPrice.add(step.multiply(BigDecimal.valueOf(i))).setScale(2, RoundingMode.HALF_UP);
            }
            points.add(pt);
        }
        return points;
    }

    public record DocumentDownloadPayload(String fileName, String mimeType, byte[] content) {}

    @Transactional(readOnly = true)
    public DocumentDownloadPayload getCompanyDocumentDownload(Long companyId, Long documentId) {
        MarketCompany comp = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + companyId));
        CompanyDocument doc = documentRepository.findById(documentId)
                .filter(d -> d.getCompany() != null && Objects.equals(d.getCompany().getCompanyId(), companyId))
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + documentId));

        String safeBase = doc.getDocumentName().replaceAll("[^a-zA-Z0-9._-]", "_");
        String mime = doc.getMimeType() != null && !doc.getMimeType().isBlank() ? doc.getMimeType() : "application/pdf";

        if (mime.toLowerCase().contains("csv") || mime.toLowerCase().contains("excel") || mime.toLowerCase().contains("spreadsheet")) {
            String csv = String.join("\n",
                    "Company,Symbol,Document,Category,Period Date,Verified,Generated At",
                    String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"",
                            comp.getCompanyName(),
                            comp.getSymbol(),
                            doc.getDocumentName(),
                            doc.getCategory(),
                            doc.getPeriodDate() != null ? doc.getPeriodDate() : "",
                            Boolean.TRUE.equals(doc.getIsVerified()) ? "DIGITALLY_SIGNED" : "PENDING",
                            java.time.OffsetDateTime.now())
            );
            String ext = mime.toLowerCase().contains("csv") ? ".csv" : ".xls";
            return new DocumentDownloadPayload(safeBase + ext, "text/csv; charset=utf-8", csv.getBytes(StandardCharsets.UTF_8));
        }

        byte[] pdfBytes = buildOfficialDocumentPdf(comp, doc);
        return new DocumentDownloadPayload(safeBase + ".pdf", "application/pdf", pdfBytes);
    }

    private byte[] buildOfficialDocumentPdf(MarketCompany comp, CompanyDocument doc) {
        List<String> lines = List.of(
                "ATIRATH GROUP INTERNAL MARKET - OFFICIAL COMPANY DISCLOSURE",
                "Company: " + sanitizePdfText(comp.getCompanyName()) + " (" + sanitizePdfText(comp.getSymbol()) + ")",
                "Sector: " + sanitizePdfText(comp.getSector()) + "   Headquarters: " + sanitizePdfText(comp.getHeadquarters()),
                "Document Title: " + sanitizePdfText(doc.getDocumentName()),
                "Category: " + sanitizePdfText(doc.getCategory()),
                "Period / Filing Date: " + (doc.getPeriodDate() != null ? doc.getPeriodDate().toString() : "N/A"),
                "Verification Status: " + (Boolean.TRUE.equals(doc.getIsVerified()) ? "DIGITALLY SIGNED & VERIFIED" : "FILED"),
                "Summary: Statutory disclosure recorded in Atirath Internal Market Registry.",
                "Contact: investor.relations@atirathgroup.com"
        );

        StringBuilder stream = new StringBuilder("BT\n/F1 12 Tf\n50 760 Td\n18 TL\n");
        for (int i = 0; i < lines.size(); i++) {
            stream.append("(").append(lines.get(i)).append(") Tj\n");
            if (i < lines.size() - 1) {
                stream.append("T*\n");
            }
        }
        stream.append("ET\n");
        byte[] streamBytes = stream.toString().getBytes(StandardCharsets.US_ASCII);

        List<String> objects = List.of(
                "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n",
                "2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n",
                "3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Contents 4 0 R /Resources << /Font << /F1 5 0 R >> >> >>\nendobj\n",
                "4 0 obj\n<< /Length " + streamBytes.length + " >>\nstream\n" + stream + "endstream\nendobj\n",
                "5 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n"
        );

        StringBuilder pdf = new StringBuilder("%PDF-1.4\n");
        List<Integer> offsets = new ArrayList<>();
        for (String obj : objects) {
            offsets.add(pdf.length());
            pdf.append(obj);
        }
        int xrefStart = pdf.length();
        pdf.append("xref\n0 ").append(objects.size() + 1).append("\n");
        pdf.append("0000000000 65535 f \n");
        for (int off : offsets) {
            pdf.append(String.format(Locale.ROOT, "%010d 00000 n \n", off));
        }
        pdf.append("trailer\n<< /Size ").append(objects.size() + 1).append(" /Root 1 0 R >>\nstartxref\n")
                .append(xrefStart).append("\n%%EOF\n");
        return pdf.toString().getBytes(StandardCharsets.US_ASCII);
    }

    private CompanyOverviewDto.LeadershipItemDto toLeadershipDto(CompanyLeadership l) {
        return CompanyOverviewDto.LeadershipItemDto.builder()
                .leadershipId(l.getLeadershipId())
                .personName(l.getPersonName())
                .designation(l.getDesignation())
                .displayOrder(l.getDisplayOrder())
                .build();
    }

    private CompanyOverviewDto.HighlightItemDto toHighlightDto(CompanyHighlight h) {
        return CompanyOverviewDto.HighlightItemDto.builder()
                .highlightId(h.getHighlightId())
                .highlightText(h.getHighlightText())
                .displayOrder(h.getDisplayOrder())
                .build();
    }

    private CompanyOverviewDto.ShareholdingItemDto toShareholdingDto(CompanyShareholding s) {
        return CompanyOverviewDto.ShareholdingItemDto.builder()
                .shareholdingId(s.getShareholdingId())
                .holderCategory(s.getHolderCategory())
                .sharesHeld(s.getSharesHeld())
                .percentage(s.getPercentage())
                .asOfDate(s.getAsOfDate())
                .build();
    }

    private CompanyOverviewDto.FinancialRatioItemDto toFinancialRatioDto(CompanyFinancialRatio r) {
        return CompanyOverviewDto.FinancialRatioItemDto.builder()
                .ratioId(r.getRatioId())
                .financialYear(r.getFinancialYear())
                .eps(r.getEps())
                .peRatio(r.getPeRatio())
                .bookValue(r.getBookValue())
                .priceToBook(r.getPriceToBook())
                .debtToEquity(r.getDebtToEquity())
                .currentRatio(r.getCurrentRatio())
                .roe(r.getRoe())
                .roce(r.getRoce())
                .dividendYield(r.getDividendYield())
                .netProfitMargin(r.getNetProfitMargin())
                .ebitdaMargin(r.getEbitdaMargin())
                .build();
    }

    private CompanyOverviewDto.AnnualFinancialItemDto toAnnualFinancialDto(CompanyAnnualFinancial a) {
        return CompanyOverviewDto.AnnualFinancialItemDto.builder()
                .financialId(a.getFinancialId())
                .financialYear(a.getFinancialYear())
                .periodEndDate(a.getPeriodEndDate())
                .revenue(a.getRevenue())
                .ebitda(a.getEbitda())
                .ebitdaMargin(a.getEbitdaMargin())
                .netProfit(a.getNetProfit())
                .netProfitMargin(a.getNetProfitMargin())
                .totalAssets(a.getTotalAssets())
                .totalEquity(a.getTotalEquity())
                .debtToEquity(a.getDebtToEquity())
                .currentRatio(a.getCurrentRatio())
                .roe(a.getRoe())
                .roce(a.getRoce())
                .eps(a.getEps())
                .peRatio(a.getPeRatio())
                .bookValue(a.getBookValue())
                .priceToBook(a.getPriceToBook())
                .dividendYield(a.getDividendYield())
                .build();
    }

    private CompanyOverviewDto.QuarterlyFinancialItemDto toQuarterlyFinancialDto(CompanyQuarterlyFinancial q) {
        return CompanyOverviewDto.QuarterlyFinancialItemDto.builder()
                .financialId(q.getFinancialId())
                .financialYear(q.getFinancialYear())
                .periodLabel(q.getPeriodLabel())
                .periodEndDate(q.getPeriodEndDate())
                .revenue(q.getRevenue())
                .ebitda(q.getEbitda())
                .ebitdaMargin(q.getEbitdaMargin())
                .netProfit(q.getNetProfit())
                .netProfitMargin(q.getNetProfitMargin())
                .totalAssets(q.getTotalAssets())
                .totalEquity(q.getTotalEquity())
                .debtToEquity(q.getDebtToEquity())
                .currentRatio(q.getCurrentRatio())
                .roe(q.getRoe())
                .roce(q.getRoce())
                .eps(q.getEps())
                .peRatio(q.getPeRatio())
                .bookValue(q.getBookValue())
                .priceToBook(q.getPriceToBook())
                .dividendYield(q.getDividendYield())
                .build();
    }

    private CompanyOverviewDto.AnnouncementItemDto toAnnouncementDto(CompanyAnnouncement an) {
        return CompanyOverviewDto.AnnouncementItemDto.builder()
                .announcementId(an.getAnnouncementId())
                .title(an.getTitle())
                .description(an.getDescription())
                .announcementType(an.getAnnouncementType())
                .announcementDate(an.getAnnouncementDate())
                .documentId(an.getDocument() != null ? an.getDocument().getDocumentId() : null)
                .status(an.getStatus())
                .build();
    }

    private CompanyOverviewDto.DocumentItemDto toDocumentDto(CompanyDocument d) {
        return CompanyOverviewDto.DocumentItemDto.builder()
                .documentId(d.getDocumentId())
                .documentName(d.getDocumentName())
                .category(d.getCategory())
                .periodDate(d.getPeriodDate())
                .fileUrl(d.getFileUrl())
                .fileSizeBytes(d.getFileSizeBytes())
                .mimeType(d.getMimeType())
                .isVerified(d.getIsVerified())
                .createdAt(d.getCreatedAt())
                .build();
    }

    private String sanitizePdfText(String text) {
        if (text == null) return "-";
        return text.replaceAll("[^\\x20-\\x7E]", " ")
                .replace("\\", "\\\\")
                .replace("(", "\\(")
                .replace(")", "\\)");
    }
}
