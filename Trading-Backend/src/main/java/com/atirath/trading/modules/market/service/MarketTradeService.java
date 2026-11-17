package com.atirath.trading.modules.market.service;

import com.atirath.trading.modules.auth.model.UserAccount;
import com.atirath.trading.modules.market.dto.MarketActivitySummaryDto;
import com.atirath.trading.modules.market.dto.MarketTradeDto;
import com.atirath.trading.modules.market.model.MarketTrade;
import com.atirath.trading.modules.market.repository.MarketCompanyRepository;
import com.atirath.trading.modules.market.repository.MarketTradeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MarketTradeService {

    private final MarketTradeRepository tradeRepository;
    private final MarketCompanyRepository companyRepository;

    @Transactional(readOnly = true)
    public List<MarketTradeDto> getRecentTrades(Long companyId, int limit) {
        int cappedLimit = limit > 0 ? Math.min(limit, 250) : 20;
        PageRequest pageable = PageRequest.of(0, cappedLimit);

        List<MarketTrade> trades;
        if (companyId != null) {
            trades = tradeRepository.findByCompany_CompanyIdOrderByExecutedAtDesc(companyId, pageable);
        } else {
            trades = tradeRepository.findByOrderByExecutedAtDesc(pageable);
        }

        Map<Long, String> nameCache = new HashMap<>();
        return trades.stream()
                .map(t -> mapToDto(t, nameCache))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MarketActivitySummaryDto getMarketActivitySummary() {
        long totalTrades = tradeRepository.countByStatus("EXECUTED");
        BigDecimal totalTradedValue = tradeRepository.calculateTotalTradedValue();
        long activeCompanies = companyRepository.findByStatusOrderByCompanyNameAsc("ACTIVE").size();

        OffsetDateTime startOfToday = OffsetDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        long todayTrades = tradeRepository.countTradesSince(startOfToday);
        BigDecimal todayTradedValue = tradeRepository.calculateTradedValueSince(startOfToday);

        return MarketActivitySummaryDto.builder()
                .totalTrades(totalTrades)
                .totalTradedValue(totalTradedValue != null ? totalTradedValue : BigDecimal.ZERO)
                .activeCompaniesCount(activeCompanies)
                .todayTradesCount(todayTrades)
                .todayTradedValue(todayTradedValue != null ? todayTradedValue : BigDecimal.ZERO)
                .marketStatus("OPEN")
                .marketHours("09:00 AM - 05:00 PM")
                .build();
    }

    private MarketTradeDto mapToDto(MarketTrade t, Map<Long, String> nameCache) {
        boolean isBuy;
        if (t.getBuyOrderId() != null && t.getSellOrderId() != null) {
            isBuy = t.getBuyOrderId() >= t.getSellOrderId();
        } else {
            isBuy = t.getBuyOrderId() != null;
        }

        String buyerName = resolveUserDisplayName(t.getBuyerUser(), nameCache, "Member (Buyer)");
        String sellerName = resolveUserDisplayName(t.getSellerUser(), nameCache, "Counterparty (Seller)");

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
                .orderSide(isBuy ? "BUY" : "SELL")
                .isBuy(isBuy)
                .buyerName(buyerName)
                .sellerName(sellerName)
                .build();
    }

    private String resolveUserDisplayName(UserAccount user, Map<Long, String> nameCache, String fallback) {
        if (user == null || user.getUserId() == null) {
            return fallback;
        }
        return nameCache.computeIfAbsent(user.getUserId(), id -> {
            try {
                if (user.getIndividualProfile() != null && user.getIndividualProfile().getFullName() != null) {
                    return user.getIndividualProfile().getFullName();
                }
                if (user.getCorporateProfile() != null && user.getCorporateProfile().getCompanyName() != null) {
                    return user.getCorporateProfile().getCompanyName();
                }
                if (user.getEmail() != null && user.getEmail().contains("@")) {
                    return user.getEmail().substring(0, user.getEmail().indexOf('@'));
                }
            } catch (Exception ignored) {
            }
            return fallback;
        });
    }
}
