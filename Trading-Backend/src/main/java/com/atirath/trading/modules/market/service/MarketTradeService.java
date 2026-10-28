package com.atirath.trading.modules.market.service;

import com.atirath.trading.modules.market.dto.MarketActivitySummaryDto;
import com.atirath.trading.modules.market.dto.MarketTradeDto;
import com.atirath.trading.modules.market.model.MarketTrade;
import com.atirath.trading.modules.market.repository.MarketCompanyRepository;
import com.atirath.trading.modules.market.repository.MarketTradeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MarketTradeService {

    private final MarketTradeRepository tradeRepository;
    private final MarketCompanyRepository companyRepository;

    @Transactional(readOnly = true)
    public List<MarketTradeDto> getRecentTrades(Long companyId, int limit) {
        List<MarketTrade> trades;
        if (companyId != null) {
            trades = tradeRepository.findTop50ByCompany_CompanyIdOrderByExecutedAtDesc(companyId);
        } else {
            trades = tradeRepository.findTop50ByOrderByExecutedAtDesc();
        }

        return trades.stream()
                .limit(limit > 0 ? limit : 50)
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MarketActivitySummaryDto getMarketActivitySummary() {
        long totalTrades = tradeRepository.countByStatus("EXECUTED");
        BigDecimal totalTradedValue = tradeRepository.calculateTotalTradedValue();
        long activeCompanies = companyRepository.findByStatusOrderByCompanyNameAsc("ACTIVE").size();

        OffsetDateTime startOfToday = OffsetDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        long todayTrades = tradeRepository.countTradesSince(startOfToday);

        return MarketActivitySummaryDto.builder()
                .totalTrades(totalTrades)
                .totalTradedValue(totalTradedValue != null ? totalTradedValue : BigDecimal.ZERO)
                .activeCompaniesCount(activeCompanies)
                .todayTradesCount(todayTrades)
                .build();
    }

    private MarketTradeDto mapToDto(MarketTrade t) {
        return MarketTradeDto.builder()
                .tradeId(t.getTradeId())
                .tradeNo(t.getTradeNo())
                .companyId(t.getCompany() != null ? t.getCompany().getCompanyId() : null)
                .symbol(t.getCompany() != null ? t.getCompany().getSymbol() : null)
                .companyName(t.getCompany() != null ? t.getCompany().getCompanyName() : null)
                .quantity(t.getQuantity())
                .tradePrice(t.getTradePrice())
                .tradeValue(t.getTradeValue())
                .executedAt(t.getExecutedAt())
                .status(t.getStatus())
                .orderSide(t.getBuyOrderId() != null ? "BUY" : "SELL")
                .isBuy(t.getBuyOrderId() != null)
                .build();
    }
}
