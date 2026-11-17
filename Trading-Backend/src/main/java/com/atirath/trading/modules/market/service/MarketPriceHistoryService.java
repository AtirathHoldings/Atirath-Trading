package com.atirath.trading.modules.market.service;

import com.atirath.trading.common.ResourceNotFoundException;
import com.atirath.trading.modules.market.dto.PriceCandleDto;
import com.atirath.trading.modules.market.model.MarketCompany;
import com.atirath.trading.modules.market.model.MarketPriceHistory;
import com.atirath.trading.modules.market.repository.MarketCompanyRepository;
import com.atirath.trading.modules.market.repository.MarketPriceHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MarketPriceHistoryService {

    private final MarketPriceHistoryRepository priceHistoryRepository;
    private final MarketCompanyRepository companyRepository;

    @Transactional(readOnly = true)
    public List<PriceCandleDto> getPriceHistory(Long companyId, String intervalType, OffsetDateTime from, OffsetDateTime to) {
        if (!companyRepository.existsById(companyId)) {
            throw new ResourceNotFoundException("Company not found with id: " + companyId);
        }

        String rawInterval = (intervalType != null && !intervalType.trim().isEmpty())
                ? intervalType.trim().toUpperCase()
                : "DAY";

        String interval = switch (rawInterval) {
            case "1D", "INTRADAY" -> "INTRADAY";
            case "1W", "DAY" -> "DAY";
            case "1M", "3M", "WEEK" -> "WEEK";
            case "6M", "1Y", "ALL", "MONTH" -> "MONTH";
            default -> rawInterval;
        };

        List<MarketPriceHistory> history;
        if (from != null && to != null) {
            history = priceHistoryRepository.findByCompany_CompanyIdAndIntervalTypeAndPriceTimeBetweenOrderByPriceTimeAsc(
                    companyId, interval, from, to);
        } else {
            history = priceHistoryRepository.findByCompany_CompanyIdAndIntervalTypeOrderByPriceTimeAsc(
                    companyId, interval);
            // Keep INTRADAY scoped to the latest trading session window so disjoint historical days never stretch 1D axis
            if ("INTRADAY".equals(interval) && !history.isEmpty()) {
                OffsetDateTime latest = history.get(history.size() - 1).getPriceTime();
                if (latest != null) {
                    OffsetDateTime sessionCutoff = latest.minusHours(36);
                    history = history.stream()
                            .filter(p -> p.getPriceTime() == null || !p.getPriceTime().isBefore(sessionCutoff))
                            .collect(Collectors.toList());
                }
            }
        }

        return history.stream()
                .map(p -> PriceCandleDto.builder()
                        .priceHistoryId(p.getPriceHistoryId())
                        .intervalType(p.getIntervalType())
                        .priceTime(p.getPriceTime())
                        .openPrice(p.getOpenPrice())
                        .highPrice(p.getHighPrice())
                        .lowPrice(p.getLowPrice())
                        .closePrice(p.getClosePrice())
                        .volumeShares(p.getVolumeShares())
                        .turnoverValue(p.getTurnoverValue())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PriceCandleDto> getPriceHistoryBySymbol(String symbol, String intervalType, OffsetDateTime from, OffsetDateTime to) {
        MarketCompany company = companyRepository.findBySymbolIgnoreCase(symbol)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with symbol: " + symbol));
        return getPriceHistory(company.getCompanyId(), intervalType, from, to);
    }
}
