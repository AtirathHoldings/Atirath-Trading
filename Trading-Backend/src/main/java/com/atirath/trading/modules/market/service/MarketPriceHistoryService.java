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

        String interval = (intervalType != null && !intervalType.trim().isEmpty())
                ? intervalType.trim().toUpperCase()
                : "DAY";

        List<MarketPriceHistory> history;
        if (from != null && to != null) {
            history = priceHistoryRepository.findByCompany_CompanyIdAndIntervalTypeAndPriceTimeBetweenOrderByPriceTimeAsc(
                    companyId, interval, from, to);
        } else {
            history = priceHistoryRepository.findByCompany_CompanyIdAndIntervalTypeOrderByPriceTimeAsc(
                    companyId, interval);
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
