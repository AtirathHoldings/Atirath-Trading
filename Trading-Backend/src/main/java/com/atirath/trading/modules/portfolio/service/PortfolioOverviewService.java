package com.atirath.trading.modules.portfolio.service;

import com.atirath.trading.common.ApiException;
import com.atirath.trading.common.ResourceNotFoundException;
import com.atirath.trading.modules.auth.model.UserAccount;
import com.atirath.trading.modules.auth.repository.UserAccountRepository;
import com.atirath.trading.modules.market.model.EscrowAccount;
import com.atirath.trading.modules.market.model.MarketCompany;
import com.atirath.trading.modules.market.model.UserShareHolding;
import com.atirath.trading.modules.market.repository.EscrowAccountRepository;
import com.atirath.trading.modules.market.repository.MarketCompanyRepository;
import com.atirath.trading.modules.market.repository.UserShareHoldingRepository;
import com.atirath.trading.modules.portfolio.dto.PortfolioOverviewDto;
import com.atirath.trading.modules.portfolio.dto.PortfolioOverviewDto.AllocationSliceDto;
import com.atirath.trading.modules.portfolio.dto.PortfolioOverviewDto.AssetBreakdownItemDto;
import com.atirath.trading.modules.portfolio.dto.PortfolioOverviewDto.MarketMoverItemDto;
import com.atirath.trading.modules.portfolio.dto.PortfolioOverviewDto.ReturnSummaryItemDto;
import com.atirath.trading.modules.portfolio.dto.PortfolioOverviewDto.TrendPointDto;
import com.atirath.trading.modules.portfolio.service.PortfolioDataHelper.ComputedHolding;
import com.atirath.trading.modules.portfolio.service.PortfolioDataHelper.HoldingSummary;
import com.atirath.trading.modules.portfolio.service.PortfolioDataHelper.PriceQuote;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Read-only service powering the Portfolio Overview screen (/portfolio-overview)
 * and authenticated per-user CSV portfolio exports.
 */
@Service
@RequiredArgsConstructor
public class PortfolioOverviewService {

    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
    private static final DateTimeFormatter AS_OF_FMT =
            DateTimeFormatter.ofPattern("'As on' dd MMM yyyy, hh:mm a", Locale.ENGLISH);
    private static final DateTimeFormatter STATEMENT_DATE_FMT =
            DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);

    private final PortfolioDataHelper dataHelper;
    private final UserAccountRepository userAccountRepository;
    private final UserShareHoldingRepository shareHoldingRepository;
    private final MarketCompanyRepository companyRepository;
    private final EscrowAccountRepository escrowAccountRepository;

    @Transactional(readOnly = true)
    public PortfolioOverviewDto getPortfolioOverview(Long userId, String period) {
        if (userId == null) {
            throw new ApiException("Authentication required to load portfolio overview", HttpStatus.UNAUTHORIZED);
        }

        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User account not found"));

        String normalizedPeriod = dataHelper.normalizePeriod(period);
        OffsetDateTime now = OffsetDateTime.now();
        String fullName = dataHelper.resolveUserFullName(user);

        List<MarketCompany> activeCompanies = companyRepository.findByStatusOrderByCompanyNameAsc("ACTIVE");
        List<UserShareHolding> rawHoldings = shareHoldingRepository.findByUserAccount_UserId(userId).stream()
                .filter(h -> h.getTotalShares() != null && h.getTotalShares() > 0 && h.getCompany() != null)
                .toList();

        HoldingSummary summary = dataHelper.aggregateHoldings(rawHoldings);

        EscrowAccount escrow = escrowAccountRepository.findByUserAccount_UserId(userId).orElse(null);
        BigDecimal escrowAvailable = escrow != null && escrow.getAvailableBalance() != null
                ? escrow.getAvailableBalance().setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        BigDecimal escrowBlocked = escrow != null && escrow.getBlockedBalance() != null
                ? escrow.getBlockedBalance().setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        BigDecimal totalGainLoss = summary.totalValue().subtract(summary.investedAmount()).setScale(2, RoundingMode.HALF_UP);
        BigDecimal gainLossPct = dataHelper.computePercent(totalGainLoss, summary.investedAmount());
        BigDecimal dayChange = summary.totalValue().subtract(summary.prevTotalValue());
        BigDecimal portfolioChangePct = dataHelper.computePercent(dayChange, summary.prevTotalValue());

        List<TrendPointDto> valueTrend = dataHelper.buildPerformanceSeries(
                rawHoldings,
                summary.totalValue(),
                summary.investedAmount(),
                summary.prevTotalValue(),
                normalizedPeriod,
                now
        ).stream().map(snap -> TrendPointDto.builder()
                .name(snap.label())
                .value(snap.value())
                .formattedValue(snap.formattedValue())
                .build()).toList();

        List<AllocationSliceDto> allocationSlices = buildAllocationSlices(summary.holdings(), summary.totalValue());
        List<AssetBreakdownItemDto> assetBreakdown = buildAssetBreakdown(summary.holdings(), escrowAvailable, escrowBlocked);
        List<ReturnSummaryItemDto> returnSummary = buildReturnSummary(rawHoldings, summary.totalValue(), summary.investedAmount(), gainLossPct, now);

        List<MarketMoverItemDto> marketMovers = computeMarketMovers(activeCompanies);
        List<MarketMoverItemDto> topGainers = marketMovers.stream()
                .filter(m -> m.getChangePercentage().compareTo(BigDecimal.ZERO) > 0)
                .sorted(Comparator.comparing(MarketMoverItemDto::getChangePercentage).reversed())
                .limit(3)
                .toList();
        List<MarketMoverItemDto> topLosers = marketMovers.stream()
                .filter(m -> m.getChangePercentage().compareTo(BigDecimal.ZERO) < 0)
                .sorted(Comparator.comparing(MarketMoverItemDto::getChangePercentage))
                .limit(3)
                .toList();

        return PortfolioOverviewDto.builder()
                .userId(userId)
                .memberId("ATG" + userId)
                .userName(fullName)
                .asOfFormatted(now.format(AS_OF_FMT))
                .activePeriod(normalizedPeriod)
                .totalPortfolioValue(summary.totalValue())
                .formattedPortfolioValue(dataHelper.formatInr(summary.totalValue()))
                .formattedPortfolioCompact(dataHelper.formatCompactInr(summary.totalValue()))
                .portfolioChangePercentage(portfolioChangePct)
                .formattedPortfolioChangePercentage(dataHelper.formatSignedPercent(portfolioChangePct))
                .portfolioChangePositive(portfolioChangePct.compareTo(BigDecimal.ZERO) >= 0)
                .investedAmount(summary.investedAmount())
                .formattedInvestedAmount(dataHelper.formatInr(summary.investedAmount()))
                .totalGainLoss(totalGainLoss)
                .formattedTotalGainLoss(dataHelper.formatSignedInr(totalGainLoss))
                .gainLossPercentage(gainLossPct)
                .formattedGainLossPercentage(dataHelper.formatSignedPercent(gainLossPct))
                .gainLossPositive(totalGainLoss.compareTo(BigDecimal.ZERO) >= 0)
                .companiesInvestedCount(summary.holdings().size())
                .totalCompaniesCount(activeCompanies.size())
                .valueTrend(valueTrend)
                .allocationByCompany(allocationSlices)
                .assetBreakdown(assetBreakdown)
                .returnSummary(returnSummary)
                .topGainers(topGainers)
                .topLosers(topLosers)
                .build();
    }

    @Transactional(readOnly = true)
    public String buildPortfolioReportCsv(Long userId) {
        if (userId == null) {
            throw new ApiException("Authentication required to export portfolio report", HttpStatus.UNAUTHORIZED);
        }

        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User account not found"));
        String fullName = dataHelper.resolveUserFullName(user);
        String memberId = "ATG" + userId;

        List<UserShareHolding> rawHoldings = shareHoldingRepository.findByUserAccount_UserId(userId).stream()
                .filter(h -> h.getTotalShares() != null && h.getTotalShares() > 0 && h.getCompany() != null)
                .toList();
        HoldingSummary summary = dataHelper.aggregateHoldings(rawHoldings);

        StringBuilder sb = new StringBuilder();
        sb.append("Account Holder,").append(escapeCsv(fullName)).append('\n');
        sb.append("Member ID,").append(memberId).append('\n');
        sb.append("Generated On,").append(escapeCsv(OffsetDateTime.now().format(STATEMENT_DATE_FMT))).append('\n');
        sb.append("Total Portfolio Value,").append(summary.totalValue().toPlainString()).append('\n');
        sb.append("Total Invested Amount,").append(summary.investedAmount().toPlainString()).append("\n\n");
        sb.append("Company,Symbol,Shares Held,Avg Price,Current Price,Current Value,Unrealized PnL,Allocation %\n");

        if (summary.holdings().isEmpty()) {
            sb.append("No active holdings,- ,0,0.00,0.00,0.00,0.00,0.0%\n");
        } else {
            for (ComputedHolding h : summary.holdings()) {
                BigDecimal allocPct = summary.totalValue().compareTo(BigDecimal.ZERO) > 0
                        ? h.currentValue().multiply(ONE_HUNDRED).divide(summary.totalValue(), 1, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO.setScale(1, RoundingMode.HALF_UP);
                sb.append(escapeCsv(h.company().getCompanyName())).append(',')
                        .append(escapeCsv(h.company().getSymbol())).append(',')
                        .append(h.shares()).append(',')
                        .append(h.avgPrice().toPlainString()).append(',')
                        .append(h.currentPrice().toPlainString()).append(',')
                        .append(h.currentValue().toPlainString()).append(',')
                        .append(h.pnl().toPlainString()).append(',')
                        .append(allocPct.toPlainString()).append("%\n");
            }
        }
        return sb.toString();
    }

    @Transactional(readOnly = true)
    public String buildStatementCsv(Long userId) {
        if (userId == null) {
            throw new ApiException("Authentication required to download statement", HttpStatus.UNAUTHORIZED);
        }

        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User account not found"));
        String fullName = dataHelper.resolveUserFullName(user);

        List<UserShareHolding> rawHoldings = shareHoldingRepository.findByUserAccount_UserId(userId).stream()
                .filter(h -> h.getTotalShares() != null && h.getTotalShares() > 0 && h.getCompany() != null)
                .toList();
        HoldingSummary summary = dataHelper.aggregateHoldings(rawHoldings);

        return "Account Holder," + escapeCsv(fullName) + "\n" +
                "Member ID,ATG" + userId + "\n" +
                "Total Portfolio Value," + escapeCsv(dataHelper.formatInr(summary.totalValue())) + "\n" +
                "Total Invested Amount," + escapeCsv(dataHelper.formatInr(summary.investedAmount())) + "\n" +
                "Total Shares," + summary.totalShares() + "\n" +
                "Statement Date," + OffsetDateTime.now().format(STATEMENT_DATE_FMT) + "\n" +
                "Status," + (user.getAccountStatus() != null ? user.getAccountStatus().name() : "PENDING") + "\n";
    }

    private List<AllocationSliceDto> buildAllocationSlices(List<ComputedHolding> holdings, BigDecimal totalValue) {
        if (holdings.isEmpty() || totalValue.compareTo(BigDecimal.ZERO) <= 0) {
            return List.of();
        }
        List<AllocationSliceDto> slices = new ArrayList<>();
        for (int i = 0; i < holdings.size(); i++) {
            ComputedHolding h = holdings.get(i);
            BigDecimal pct = h.currentValue().multiply(ONE_HUNDRED).divide(totalValue, 1, RoundingMode.HALF_UP);
            slices.add(AllocationSliceDto.builder()
                    .companyId(h.company().getCompanyId())
                    .name(h.company().getCompanyName())
                    .symbol(h.company().getSymbol())
                    .value(h.currentValue())
                    .formattedValue(dataHelper.formatInr(h.currentValue()))
                    .percentage(pct)
                    .color(PortfolioDataHelper.CHART_COLORS.get(i % PortfolioDataHelper.CHART_COLORS.size()))
                    .build());
        }
        return slices;
    }

    private List<AssetBreakdownItemDto> buildAssetBreakdown(List<ComputedHolding> holdings,
                                                            BigDecimal escrowAvailable,
                                                            BigDecimal escrowBlocked) {
        BigDecimal availableEquityVal = BigDecimal.ZERO;
        BigDecimal lockedEquityVal = BigDecimal.ZERO;
        for (ComputedHolding h : holdings) {
            availableEquityVal = availableEquityVal.add(h.currentPrice().multiply(BigDecimal.valueOf(h.availableShares())));
            lockedEquityVal = lockedEquityVal.add(h.currentPrice().multiply(BigDecimal.valueOf(h.lockedShares())));
        }
        availableEquityVal = availableEquityVal.setScale(2, RoundingMode.HALF_UP);
        lockedEquityVal = lockedEquityVal.setScale(2, RoundingMode.HALF_UP);

        BigDecimal totalAssets = availableEquityVal.add(lockedEquityVal).add(escrowBlocked).add(escrowAvailable);

        return List.of(
                buildBreakdownItem("Equity Shares (Available)", availableEquityVal, totalAssets, "fill-blue"),
                buildBreakdownItem("Locked in Escrow", lockedEquityVal, totalAssets, "fill-green"),
                buildBreakdownItem("Pending Settlement", escrowBlocked, totalAssets, "fill-yellow"),
                buildBreakdownItem("Available Balance", escrowAvailable, totalAssets, "fill-purple")
        );
    }

    private AssetBreakdownItemDto buildBreakdownItem(String label, BigDecimal amount, BigDecimal total, String fillClass) {
        BigDecimal pct = total.compareTo(BigDecimal.ZERO) > 0
                ? amount.multiply(ONE_HUNDRED).divide(total, 1, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(1, RoundingMode.HALF_UP);
        return AssetBreakdownItemDto.builder()
                .label(label)
                .amount(amount)
                .formattedAmount(dataHelper.formatInr(amount))
                .percentage(pct)
                .fillClass(fillClass)
                .build();
    }

    private List<ReturnSummaryItemDto> buildReturnSummary(List<UserShareHolding> rawHoldings,
                                                          BigDecimal totalValue,
                                                          BigDecimal investedAmount,
                                                          BigDecimal sinceInceptionPct,
                                                          OffsetDateTime now) {
        BigDecimal ret1m = dataHelper.computePeriodReturnPct(rawHoldings, totalValue, investedAmount, now.toLocalDate().minusMonths(1));
        BigDecimal ret3m = dataHelper.computePeriodReturnPct(rawHoldings, totalValue, investedAmount, now.toLocalDate().minusMonths(3));
        BigDecimal ret6m = dataHelper.computePeriodReturnPct(rawHoldings, totalValue, investedAmount, now.toLocalDate().minusMonths(6));
        BigDecimal ret1y = dataHelper.computePeriodReturnPct(rawHoldings, totalValue, investedAmount, now.toLocalDate().minusYears(1));

        return List.of(
                toReturnItem("1 Month", ret1m),
                toReturnItem("3 Months", ret3m),
                toReturnItem("6 Months", ret6m),
                toReturnItem("1 Year", ret1y),
                toReturnItem("Since Inception", sinceInceptionPct)
        );
    }

    private ReturnSummaryItemDto toReturnItem(String label, BigDecimal pct) {
        return ReturnSummaryItemDto.builder()
                .label(label)
                .percentage(pct)
                .formattedPercentage(dataHelper.formatSignedPercent(pct))
                .isPositive(pct.compareTo(BigDecimal.ZERO) >= 0)
                .build();
    }

    private List<MarketMoverItemDto> computeMarketMovers(List<MarketCompany> companies) {
        List<MarketMoverItemDto> movers = new ArrayList<>();
        for (MarketCompany comp : companies) {
            PriceQuote quote = dataHelper.resolvePriceQuote(comp, BigDecimal.ZERO);
            BigDecimal changePct = dataHelper.computePercent(
                    quote.currentPrice().subtract(quote.previousClose()),
                    quote.previousClose()
            );
            movers.add(MarketMoverItemDto.builder()
                    .companyId(comp.getCompanyId())
                    .companyName(comp.getCompanyName())
                    .symbol(comp.getSymbol())
                    .currentPrice(quote.currentPrice())
                    .formattedCurrentPrice(dataHelper.formatInr(quote.currentPrice()))
                    .changePercentage(changePct)
                    .formattedChangePercentage(dataHelper.formatSignedPercent(changePct))
                    .isPositive(changePct.compareTo(BigDecimal.ZERO) >= 0)
                    .build());
        }
        return movers;
    }

    private String escapeCsv(String val) {
        if (val == null) return "";
        if (val.contains(",") || val.contains("\"") || val.contains("\n")) {
            return "\"" + val.replace("\"", "\"\"") + "\"";
        }
        return val;
    }
}

