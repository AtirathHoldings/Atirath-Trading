package com.atirath.trading.modules.market.service;

import com.atirath.trading.common.ResourceNotFoundException;
import com.atirath.trading.modules.auth.model.UserAccount;
import com.atirath.trading.modules.auth.repository.UserAccountRepository;
import com.atirath.trading.modules.market.dto.CompanySummaryDto;
import com.atirath.trading.modules.market.dto.WatchlistDto;
import com.atirath.trading.modules.market.model.MarketCompany;
import com.atirath.trading.modules.market.model.MemberWatchlist;
import com.atirath.trading.modules.market.repository.MarketCompanyRepository;
import com.atirath.trading.modules.market.repository.MemberWatchlistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WatchlistService {

    private final MemberWatchlistRepository watchlistRepository;
    private final MarketCompanyRepository companyRepository;
    private final UserAccountRepository userAccountRepository;
    private final MarketCompanyService marketCompanyService;

    @Transactional(readOnly = true)
    public List<WatchlistDto> getUserWatchlist(Long userId) {
        List<MemberWatchlist> entries = watchlistRepository.findByUserAccount_UserIdOrderByCreatedAtDesc(userId);

        return entries.stream().map(entry -> {
            MarketCompany comp = entry.getCompany();
            CompanySummaryDto summary = marketCompanyService.mapToSummaryDto(comp, userId);

            return WatchlistDto.builder()
                    .watchlistId(entry.getWatchlistId())
                    .companyId(comp.getCompanyId())
                    .companyCode(comp.getCompanyCode())
                    .companyName(comp.getCompanyName())
                    .symbol(comp.getSymbol())
                    .sector(comp.getSector())
                    .logoUrl(comp.getLogoUrl())
                    .currentPrice(summary.getCurrentPrice())
                    .priceChange(summary.getPriceChange())
                    .priceChangePercentage(summary.getPriceChangePercentage())
                    .addedAt(entry.getCreatedAt())
                    .build();
        }).collect(Collectors.toList());
    }

    @Transactional
    public void addToWatchlist(Long userId, Long companyId) {
        if (watchlistRepository.existsByUserAccount_UserIdAndCompany_CompanyId(userId, companyId)) {
            log.info("User {} already has company {} in watchlist", userId, companyId);
            return;
        }

        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        MarketCompany company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + companyId));

        MemberWatchlist watchlist = MemberWatchlist.builder()
                .userAccount(user)
                .company(company)
                .build();

        watchlistRepository.save(watchlist);
        log.info("Added company {} to watchlist for user {}", companyId, userId);
    }

    @Transactional
    public void removeFromWatchlist(Long userId, Long companyId) {
        watchlistRepository.deleteByUserAccount_UserIdAndCompany_CompanyId(userId, companyId);
        log.info("Removed company {} from watchlist for user {}", companyId, userId);
    }

    @Transactional(readOnly = true)
    public boolean isWatched(Long userId, Long companyId) {
        return watchlistRepository.existsByUserAccount_UserIdAndCompany_CompanyId(userId, companyId);
    }
}
