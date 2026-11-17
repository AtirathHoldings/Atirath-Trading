package com.atirath.trading.modules.portfolio.service;

import com.atirath.trading.common.ApiException;
import com.atirath.trading.common.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import com.atirath.trading.modules.auth.model.UserAccount;
import com.atirath.trading.modules.auth.repository.UserAccountRepository;
import com.atirath.trading.modules.market.model.*;
import com.atirath.trading.modules.market.repository.*;
import com.atirath.trading.modules.market.service.MarketBroadcastService;
import com.atirath.trading.modules.portfolio.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PortfolioService {

    private final UserAccountRepository userAccountRepository;
    private final MarketCompanyRepository companyRepository;
    private final UserShareHoldingRepository shareHoldingRepository;
    private final MarketOrderRepository orderRepository;
    private final MarketTradeRepository tradeRepository;
    private final MarketBroadcastService broadcastService;
    private final EscrowAccountRepository escrowAccountRepository;
    private final EscrowRepository escrowRepository;
    private final ShareTransferRepository shareTransferRepository;
    private final PortfolioDataHelper dataHelper;

    private static final DateTimeFormatter DISPLAY_DT_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a", Locale.ENGLISH);
    private static final DateTimeFormatter DATE_ONLY_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter MONTH_YEAR_FORMATTER = DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH);

    // =========================================================================
    // 1. PORTFOLIO DASHBOARD (Real Per-User DB Aggregation)
    // =========================================================================
    @Transactional(readOnly = true)
    public PortfolioDashboardDto getDashboard(Long userId, String period) {
        if (userId == null) {
            throw new ApiException("Authentication required to load portfolio dashboard", HttpStatus.UNAUTHORIZED);
        }

        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User account not found"));

        String activePeriod = dataHelper.normalizePeriod(period);
        OffsetDateTime now = OffsetDateTime.now();
        String fullName = dataHelper.resolveUserFullName(user);
        String memberId = "ATG" + userId;
        String avatarInitials = dataHelper.extractInitials(fullName);
        String asOfDate = now.format(DISPLAY_DT_FORMATTER);

        List<UserShareHolding> rawHoldings = shareHoldingRepository.findByUserAccount_UserId(userId).stream()
                .filter(h -> h.getTotalShares() != null && h.getTotalShares() > 0 && h.getCompany() != null)
                .toList();

        PortfolioDataHelper.HoldingSummary summary = dataHelper.aggregateHoldings(rawHoldings);
        BigDecimal totalValue = summary.totalValue();
        BigDecimal investedAmount = summary.investedAmount();
        BigDecimal unrealizedPnl = totalValue.subtract(investedAmount).setScale(2, RoundingMode.HALF_UP);

        BigDecimal dayChange = totalValue.subtract(summary.prevTotalValue());
        BigDecimal valueChangePct = dataHelper.computePercent(dayChange, summary.prevTotalValue());
        BigDecimal unrealizedPct = dataHelper.computePercent(unrealizedPnl, investedAmount);

        List<MarketOrder> userOrders = orderRepository.findByUserAccount_UserIdOrderByPlacedAtDesc(userId);
        List<MarketTrade> userTrades = tradeRepository.findUserTrades(userId);
        List<ShareTransfer> fromTransfers = shareTransferRepository.findByFromUser_UserIdOrderByTransferredAtDesc(userId);
        List<ShareTransfer> toTransfers = shareTransferRepository.findByToUser_UserIdOrderByTransferredAtDesc(userId);

        Map<Long, BigDecimal> avgCostByCompany = new HashMap<>();
        for (PortfolioDataHelper.ComputedHolding ch : summary.holdings()) {
            avgCostByCompany.put(ch.company().getCompanyId(), ch.avgPrice());
        }

        BigDecimal realizedPnl = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        for (MarketTrade t : userTrades) {
            if (t.getSellerUser() != null && userId.equals(t.getSellerUser().getUserId())
                    && t.getCompany() != null && t.getTradePrice() != null && t.getQuantity() != null) {
                BigDecimal costBasis = avgCostByCompany.get(t.getCompany().getCompanyId());
                if (costBasis != null) {
                    realizedPnl = realizedPnl.add(
                            t.getTradePrice().subtract(costBasis).multiply(BigDecimal.valueOf(t.getQuantity()))
                    );
                }
            }
        }
        realizedPnl = realizedPnl.setScale(2, RoundingMode.HALF_UP);
        BigDecimal realizedPct = dataHelper.computePercent(realizedPnl, investedAmount);

        List<PortfolioSummaryKpiDto> summaryKpis = List.of(
                PortfolioSummaryKpiDto.builder()
                        .id("total_portfolio_value")
                        .title("Total Portfolio Value")
                        .amount(totalValue)
                        .formattedAmount(dataHelper.formatInr(totalValue))
                        .changePercentage(valueChangePct)
                        .formattedChangePercentage(dataHelper.formatSignedPercent(valueChangePct))
                        .isPositive(valueChangePct.compareTo(BigDecimal.ZERO) >= 0)
                        .comparisonLabel("vs. previous close")
                        .icon("wallet")
                        .build(),
                PortfolioSummaryKpiDto.builder()
                        .id("total_invested_amount")
                        .title("Total Invested Amount")
                        .amount(investedAmount)
                        .formattedAmount(dataHelper.formatInr(investedAmount))
                        .changePercentage(unrealizedPct)
                        .formattedChangePercentage(dataHelper.formatSignedPercent(unrealizedPct))
                        .isPositive(unrealizedPct.compareTo(BigDecimal.ZERO) >= 0)
                        .comparisonLabel("cost basis")
                        .icon("coins")
                        .build(),
                PortfolioSummaryKpiDto.builder()
                        .id("unrealized_pnl")
                        .title("Unrealized P&L")
                        .amount(unrealizedPnl)
                        .formattedAmount(dataHelper.formatSignedInr(unrealizedPnl))
                        .changePercentage(unrealizedPct)
                        .formattedChangePercentage(dataHelper.formatSignedPercent(unrealizedPct))
                        .isPositive(unrealizedPnl.compareTo(BigDecimal.ZERO) >= 0)
                        .comparisonLabel("overall return")
                        .icon("trending-up")
                        .build(),
                PortfolioSummaryKpiDto.builder()
                        .id("realized_pnl")
                        .title("Realized P&L")
                        .amount(realizedPnl)
                        .formattedAmount(dataHelper.formatSignedInr(realizedPnl))
                        .changePercentage(realizedPct)
                        .formattedChangePercentage(dataHelper.formatSignedPercent(realizedPct))
                        .isPositive(realizedPnl.compareTo(BigDecimal.ZERO) >= 0)
                        .comparisonLabel("from executed sells")
                        .icon("dollar-sign")
                        .build(),
                PortfolioSummaryKpiDto.builder()
                        .id("total_holdings")
                        .title("Total Holdings")
                        .count((long) summary.holdings().size())
                        .formattedCount(dataHelper.formatCount(summary.holdings().size()))
                        .comparisonLabel("Group Companies")
                        .icon("pie-chart")
                        .build(),
                PortfolioSummaryKpiDto.builder()
                        .id("total_shares")
                        .title("Total Shares")
                        .count(summary.totalShares())
                        .formattedCount(dataHelper.formatCount(summary.totalShares()))
                        .comparisonLabel("Shares")
                        .icon("file-text")
                        .build()
        );

        List<PortfolioTrendPointDto> trend = dataHelper.buildPerformanceSeries(
                rawHoldings,
                totalValue,
                investedAmount,
                summary.prevTotalValue(),
                activePeriod,
                now
        ).stream().map(snap -> PortfolioTrendPointDto.builder()
                .period(snap.label())
                .date(snap.dateIso())
                .value(snap.value())
                .formattedValue(snap.formattedValue())
                .isCurrent(snap.isCurrent())
                .build()).toList();

        List<PortfolioAllocationSliceDto> allocation = new ArrayList<>();
        List<PortfolioHoldingItemDto> topHoldings = new ArrayList<>();
        for (int i = 0; i < summary.holdings().size(); i++) {
            PortfolioDataHelper.ComputedHolding ch = summary.holdings().get(i);
            String color = PortfolioDataHelper.CHART_COLORS.get(i % PortfolioDataHelper.CHART_COLORS.size());
            BigDecimal allocPct = totalValue.compareTo(BigDecimal.ZERO) > 0
                    ? ch.currentValue().multiply(new BigDecimal("100")).divide(totalValue, 1, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO.setScale(1, RoundingMode.HALF_UP);

            allocation.add(PortfolioAllocationSliceDto.builder()
                    .companyId(ch.company().getCompanyId())
                    .companyName(ch.company().getCompanyName())
                    .symbol(ch.company().getSymbol())
                    .percentage(allocPct)
                    .formattedPercentage(allocPct.toPlainString() + "%")
                    .value(ch.currentValue())
                    .formattedValue(dataHelper.formatInr(ch.currentValue()))
                    .color(color)
                    .build());

            topHoldings.add(PortfolioHoldingItemDto.builder()
                    .companyId(ch.company().getCompanyId())
                    .companyName(ch.company().getCompanyName())
                    .symbol(ch.company().getSymbol())
                    .companyCode(ch.company().getCompanyCode())
                    .logoUrl(ch.company().getLogoUrl())
                    .sharesHeld(ch.shares())
                    .formattedSharesHeld(dataHelper.formatCount(ch.shares()))
                    .averageAcquisitionPrice(ch.avgPrice())
                    .formattedAverageAcquisitionPrice(dataHelper.formatInr(ch.avgPrice()))
                    .currentPrice(ch.currentPrice())
                    .formattedCurrentPrice(dataHelper.formatInr(ch.currentPrice()))
                    .currentValue(ch.currentValue())
                    .formattedCurrentValue(dataHelper.formatInr(ch.currentValue()))
                    .investedValue(ch.investedValue())
                    .formattedInvestedValue(dataHelper.formatInr(ch.investedValue()))
                    .unrealizedPnl(ch.pnl())
                    .formattedUnrealizedPnl(dataHelper.formatSignedInrWithPlus(ch.pnl()))
                    .unrealizedPnlPercentage(ch.pnlPercentage())
                    .formattedUnrealizedPnlPercentage(dataHelper.formatSignedPercent(ch.pnlPercentage()))
                    .isProfit(ch.pnl().compareTo(BigDecimal.ZERO) >= 0)
                    .lockedShares(ch.lockedShares())
                    .formattedLockedShares(dataHelper.formatCount(ch.lockedShares()))
                    .availableShares(ch.availableShares())
                    .formattedAvailableShares(dataHelper.formatCount(ch.availableShares()))
                    .allocationPercentage(allocPct)
                    .formattedAllocationPercentage(allocPct.toPlainString() + "%")
                    .progressBarColor(color)
                    .build());
        }

        Optional<PortfolioDataHelper.ComputedHolding> bestHolding = summary.holdings().stream()
                .max(Comparator.comparing(PortfolioDataHelper.ComputedHolding::pnlPercentage));
        Optional<PortfolioDataHelper.ComputedHolding> worstHolding = summary.holdings().stream()
                .min(Comparator.comparing(PortfolioDataHelper.ComputedHolding::pnlPercentage));

        int activeOrdersCount = (int) userOrders.stream()
                .filter(o -> o.getStatus() != null &&
                        Set.of("PENDING", "OPEN", "IN_ESCROW", "PARTIALLY_FILLED")
                                .contains(o.getStatus().toUpperCase(Locale.ROOT)))
                .count();

        Set<Long> seenPendingTransferIds = new HashSet<>();
        long pendingTransfersQty = 0L;
        for (ShareTransfer st : fromTransfers) {
            if ("PENDING".equalsIgnoreCase(st.getTransferStatus()) && seenPendingTransferIds.add(st.getTransferId())) {
                pendingTransfersQty += st.getQuantity() != null ? st.getQuantity() : 0L;
            }
        }
        for (ShareTransfer st : toTransfers) {
            if ("PENDING".equalsIgnoreCase(st.getTransferStatus()) && seenPendingTransferIds.add(st.getTransferId())) {
                pendingTransfersQty += st.getQuantity() != null ? st.getQuantity() : 0L;
            }
        }

        PortfolioPerformanceDto performance = PortfolioPerformanceDto.builder()
                .totalReturn(unrealizedPct)
                .formattedTotalReturn(unrealizedPct.toPlainString() + "%")
                .annualizedReturn(unrealizedPct)
                .formattedAnnualizedReturn(unrealizedPct.toPlainString() + "%")
                .bestPerformingHolding(bestHolding.map(h -> h.company().getCompanyName()).orElse("-"))
                .bestPerformingHoldingChange(bestHolding.map(h -> dataHelper.formatSignedPercent(h.pnlPercentage())).orElse("+0.00%"))
                .worstPerformingHolding(worstHolding.map(h -> h.company().getCompanyName()).orElse("-"))
                .worstPerformingHoldingChange(worstHolding.map(h -> dataHelper.formatSignedPercent(h.pnlPercentage())).orElse("+0.00%"))
                .dividendReceivedYtd(BigDecimal.ZERO)
                .formattedDividendReceivedYtd("₹ 0")
                .activeOrders(activeOrdersCount)
                .lockedSharesInEscrow(summary.totalLockedShares())
                .pendingTransfers(pendingTransfersQty)
                .build();

        List<PortfolioActivityItemDto> recentActivity = buildRealPortfolioActivity(
                userId, userOrders, userTrades, fromTransfers, toTransfers
        );

        return PortfolioDashboardDto.builder()
                .userName(fullName)
                .memberId(memberId)
                .avatarInitials(avatarInitials)
                .asOfDate(asOfDate)
                .summaryKpis(summaryKpis)
                .activeTrendPeriod(activePeriod)
                .valueTrend(trend)
                .allocation(allocation)
                .performance(performance)
                .topHoldings(topHoldings)
                .recentActivity(recentActivity)
                .build();
    }

    private List<PortfolioActivityItemDto> buildRealPortfolioActivity(Long userId,
                                                                      List<MarketOrder> orders,
                                                                      List<MarketTrade> trades,
                                                                      List<ShareTransfer> fromTransfers,
                                                                      List<ShareTransfer> toTransfers) {
        record TimedActivity(OffsetDateTime time, PortfolioActivityItemDto item) {}
        List<TimedActivity> all = new ArrayList<>();

        for (MarketOrder o : orders) {
            OffsetDateTime dt = o.getPlacedAt() != null ? o.getPlacedAt() : OffsetDateTime.MIN;
            boolean isBuy = "BUY".equalsIgnoreCase(o.getOrderSide());
            String compName = o.getCompany() != null ? o.getCompany().getCompanyName() : "Atirath Group";
            long qty = o.getQuantity() != null ? o.getQuantity() : 0L;
            String title = isBuy ? "Buy Order Placed" : "Sell Order Placed";
            String type = isBuy ? "RECEIVE" : "LOCK";
            String icon = isBuy ? "plus-circle" : "clock";
            if ("EXECUTED".equalsIgnoreCase(o.getStatus()) || "COMPLETED".equalsIgnoreCase(o.getStatus())) {
                title = isBuy ? "Shares Purchased" : "Shares Sold";
            }
            all.add(new TimedActivity(dt, PortfolioActivityItemDto.builder()
                    .id("ord_" + o.getOrderId())
                    .title(title)
                    .description(dataHelper.formatCount(qty) + " shares - " + compName)
                    .timestamp(dt != OffsetDateTime.MIN ? dt.format(DISPLAY_DT_FORMATTER) : "-")
                    .activityType(type)
                    .icon(icon)
                    .build()));
        }

        for (MarketTrade t : trades) {
            OffsetDateTime dt = t.getExecutedAt() != null ? t.getExecutedAt() : OffsetDateTime.MIN;
            all.add(new TimedActivity(dt, PortfolioActivityItemDto.builder()
                    .id("trd_" + t.getTradeId())
                    .title("Settlement Completed")
                    .description("Trade ID: " + t.getTradeNo())
                    .timestamp(dt != OffsetDateTime.MIN ? dt.format(DISPLAY_DT_FORMATTER) : "-")
                    .activityType("SETTLEMENT")
                    .icon("file-check")
                    .build()));
        }

        Set<Long> seenTransferIds = new HashSet<>();
        for (ShareTransfer st : toTransfers) {
            if (seenTransferIds.add(st.getTransferId())) {
                OffsetDateTime dt = st.getTransferredAt() != null ? st.getTransferredAt() : OffsetDateTime.MIN;
                String compName = st.getCompany() != null ? st.getCompany().getCompanyName() : "Atirath Group";
                long qty = st.getQuantity() != null ? st.getQuantity() : 0L;
                all.add(new TimedActivity(dt, PortfolioActivityItemDto.builder()
                        .id("trf_in_" + st.getTransferId())
                        .title("Shares Received")
                        .description(dataHelper.formatCount(qty) + " shares - " + compName)
                        .timestamp(dt != OffsetDateTime.MIN ? dt.format(DISPLAY_DT_FORMATTER) : "-")
                        .activityType("RECEIVE")
                        .icon("plus-circle")
                        .build()));
            }
        }
        for (ShareTransfer st : fromTransfers) {
            if (seenTransferIds.add(st.getTransferId())) {
                OffsetDateTime dt = st.getTransferredAt() != null ? st.getTransferredAt() : OffsetDateTime.MIN;
                String compName = st.getCompany() != null ? st.getCompany().getCompanyName() : "Atirath Group";
                long qty = st.getQuantity() != null ? st.getQuantity() : 0L;
                all.add(new TimedActivity(dt, PortfolioActivityItemDto.builder()
                        .id("trf_out_" + st.getTransferId())
                        .title("Shares Transferred")
                        .description(dataHelper.formatCount(qty) + " shares - " + compName)
                        .timestamp(dt != OffsetDateTime.MIN ? dt.format(DISPLAY_DT_FORMATTER) : "-")
                        .activityType("TRANSFER")
                        .icon("repeat")
                        .build()));
            }
        }

        all.sort(Comparator.comparing(TimedActivity::time).reversed());
        return all.stream().limit(5).map(TimedActivity::item).toList();
    }

    // =========================================================================
    // 2. PORTFOLIO HOLDINGS (Real Per-User DB Aggregation)
    // =========================================================================
    @Transactional(readOnly = true)
    public PortfolioHoldingsResponseDto getHoldings(Long userId, String search, String company, String holdingType, String status, int page, int size) {
        if (userId == null) {
            throw new ApiException("Authentication required to load holdings", HttpStatus.UNAUTHORIZED);
        }

        List<UserShareHolding> rawHoldings = shareHoldingRepository.findByUserAccount_UserId(userId).stream()
                .filter(h -> h.getTotalShares() != null && h.getTotalShares() > 0 && h.getCompany() != null)
                .toList();

        PortfolioDataHelper.HoldingSummary summary = dataHelper.aggregateHoldings(rawHoldings);
        BigDecimal totalValue = summary.totalValue();
        BigDecimal investedAmount = summary.investedAmount();
        BigDecimal unrealizedPnl = totalValue.subtract(investedAmount).setScale(2, RoundingMode.HALF_UP);
        BigDecimal dayChange = totalValue.subtract(summary.prevTotalValue());
        BigDecimal valueChangePct = dataHelper.computePercent(dayChange, summary.prevTotalValue());
        BigDecimal unrealizedPct = dataHelper.computePercent(unrealizedPnl, investedAmount);

        long totalShares = summary.totalShares();
        long lockedShares = summary.totalLockedShares();
        long availableShares = Math.max(0L, totalShares - lockedShares);
        BigDecimal lockedPct = totalShares > 0
                ? BigDecimal.valueOf(lockedShares).multiply(new BigDecimal("100")).divide(BigDecimal.valueOf(totalShares), 1, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(1, RoundingMode.HALF_UP);
        BigDecimal availablePct = totalShares > 0
                ? BigDecimal.valueOf(availableShares).multiply(new BigDecimal("100")).divide(BigDecimal.valueOf(totalShares), 1, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(1, RoundingMode.HALF_UP);

        List<PortfolioSummaryKpiDto> summaryKpis = List.of(
                PortfolioSummaryKpiDto.builder()
                        .id("total_holdings_value")
                        .title("Total Holdings Value")
                        .amount(totalValue)
                        .formattedAmount(dataHelper.formatInr(totalValue))
                        .changePercentage(valueChangePct)
                        .formattedChangePercentage(dataHelper.formatSignedPercent(valueChangePct))
                        .isPositive(valueChangePct.compareTo(BigDecimal.ZERO) >= 0)
                        .comparisonLabel("vs. previous close")
                        .icon("file-text")
                        .build(),
                PortfolioSummaryKpiDto.builder()
                        .id("total_shares_held")
                        .title("Total Shares Held")
                        .count(totalShares)
                        .formattedCount(dataHelper.formatCount(totalShares))
                        .comparisonLabel("Across " + summary.holdings().size() + (summary.holdings().size() == 1 ? " company" : " companies"))
                        .icon("pie-chart")
                        .build(),
                PortfolioSummaryKpiDto.builder()
                        .id("invested_amount")
                        .title("Invested Amount")
                        .amount(investedAmount)
                        .formattedAmount(dataHelper.formatInr(investedAmount))
                        .changePercentage(unrealizedPct)
                        .formattedChangePercentage(dataHelper.formatSignedPercent(unrealizedPct))
                        .isPositive(unrealizedPct.compareTo(BigDecimal.ZERO) >= 0)
                        .comparisonLabel("cost basis")
                        .icon("coins")
                        .build(),
                PortfolioSummaryKpiDto.builder()
                        .id("unrealized_pnl")
                        .title("Unrealized P&L")
                        .amount(unrealizedPnl)
                        .formattedAmount(dataHelper.formatSignedInr(unrealizedPnl))
                        .changePercentage(unrealizedPct)
                        .formattedChangePercentage(dataHelper.formatSignedPercent(unrealizedPct))
                        .isPositive(unrealizedPnl.compareTo(BigDecimal.ZERO) >= 0)
                        .comparisonLabel("overall return")
                        .icon("trending-up")
                        .build(),
                PortfolioSummaryKpiDto.builder()
                        .id("locked_shares")
                        .title("Locked Shares")
                        .count(lockedShares)
                        .formattedCount(dataHelper.formatCount(lockedShares))
                        .comparisonLabel(lockedPct.toPlainString() + "% of total shares")
                        .icon("lock")
                        .build(),
                PortfolioSummaryKpiDto.builder()
                        .id("available_shares")
                        .title("Available Shares")
                        .count(availableShares)
                        .formattedCount(dataHelper.formatCount(availableShares))
                        .comparisonLabel(availablePct.toPlainString() + "% of total shares")
                        .icon("shield-check")
                        .build()
        );

        List<PortfolioHoldingItemDto> allHoldings = new ArrayList<>();
        List<PortfolioAllocationSliceDto> allocation = new ArrayList<>();
        for (int i = 0; i < summary.holdings().size(); i++) {
            PortfolioDataHelper.ComputedHolding ch = summary.holdings().get(i);
            String color = PortfolioDataHelper.CHART_COLORS.get(i % PortfolioDataHelper.CHART_COLORS.size());
            BigDecimal allocPct = totalValue.compareTo(BigDecimal.ZERO) > 0
                    ? ch.currentValue().multiply(new BigDecimal("100")).divide(totalValue, 1, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO.setScale(1, RoundingMode.HALF_UP);

            allocation.add(PortfolioAllocationSliceDto.builder()
                    .companyId(ch.company().getCompanyId())
                    .companyName(ch.company().getCompanyName())
                    .symbol(ch.company().getSymbol())
                    .percentage(allocPct)
                    .formattedPercentage(allocPct.toPlainString() + "%")
                    .value(ch.currentValue())
                    .formattedValue(dataHelper.formatInr(ch.currentValue()))
                    .color(color)
                    .build());

            allHoldings.add(PortfolioHoldingItemDto.builder()
                    .companyId(ch.company().getCompanyId())
                    .companyName(ch.company().getCompanyName())
                    .symbol(ch.company().getSymbol())
                    .companyCode(ch.company().getCompanyCode())
                    .logoUrl(ch.company().getLogoUrl())
                    .sharesHeld(ch.shares())
                    .formattedSharesHeld(dataHelper.formatCount(ch.shares()))
                    .averageAcquisitionPrice(ch.avgPrice())
                    .formattedAverageAcquisitionPrice(dataHelper.formatInr(ch.avgPrice()))
                    .currentPrice(ch.currentPrice())
                    .formattedCurrentPrice(dataHelper.formatInr(ch.currentPrice()))
                    .currentValue(ch.currentValue())
                    .formattedCurrentValue(dataHelper.formatInr(ch.currentValue()))
                    .investedValue(ch.investedValue())
                    .formattedInvestedValue(dataHelper.formatInr(ch.investedValue()))
                    .unrealizedPnl(ch.pnl())
                    .formattedUnrealizedPnl(dataHelper.formatSignedInrWithPlus(ch.pnl()))
                    .unrealizedPnlPercentage(ch.pnlPercentage())
                    .formattedUnrealizedPnlPercentage(dataHelper.formatSignedPercent(ch.pnlPercentage()))
                    .isProfit(ch.pnl().compareTo(BigDecimal.ZERO) >= 0)
                    .lockedShares(ch.lockedShares())
                    .formattedLockedShares(dataHelper.formatCount(ch.lockedShares()))
                    .availableShares(ch.availableShares())
                    .formattedAvailableShares(dataHelper.formatCount(ch.availableShares()))
                    .allocationPercentage(allocPct)
                    .formattedAllocationPercentage(allocPct.toPlainString() + "%")
                    .progressBarColor(color)
                    .build());
        }

        List<PortfolioHoldingItemDto> filtered = allHoldings.stream()
                .filter(h -> {
                    if (search != null && !search.isBlank()) {
                        String s = search.toLowerCase(Locale.ROOT);
                        boolean matchName = h.getCompanyName() != null && h.getCompanyName().toLowerCase(Locale.ROOT).contains(s);
                        boolean matchCode = h.getCompanyCode() != null && h.getCompanyCode().toLowerCase(Locale.ROOT).contains(s);
                        boolean matchSymbol = h.getSymbol() != null && h.getSymbol().toLowerCase(Locale.ROOT).contains(s);
                        if (!matchName && !matchCode && !matchSymbol) return false;
                    }
                    if (company != null && !company.isBlank() && !"ALL".equalsIgnoreCase(company)) {
                        boolean match = (h.getCompanyName() != null && h.getCompanyName().equalsIgnoreCase(company))
                                || (h.getCompanyCode() != null && h.getCompanyCode().equalsIgnoreCase(company))
                                || (h.getSymbol() != null && h.getSymbol().equalsIgnoreCase(company));
                        if (!match) return false;
                    }
                    if (holdingType != null && !holdingType.isBlank() && !"ALL".equalsIgnoreCase(holdingType)) {
                        if ("LOCKED".equalsIgnoreCase(holdingType) && (h.getLockedShares() == null || h.getLockedShares() <= 0)) {
                            return false;
                        }
                        if ("AVAILABLE".equalsIgnoreCase(holdingType) && (h.getAvailableShares() == null || h.getAvailableShares() <= 0)) {
                            return false;
                        }
                    }
                    if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
                        if (("PROFIT".equalsIgnoreCase(status) || "PROFITABLE".equalsIgnoreCase(status)) && !Boolean.TRUE.equals(h.getIsProfit())) {
                            return false;
                        }
                        if (("LOSS".equalsIgnoreCase(status) || "LOSS_MAKING".equalsIgnoreCase(status)) && Boolean.TRUE.equals(h.getIsProfit())) {
                            return false;
                        }
                    }
                    return true;
                })
                .collect(Collectors.toList());

        Optional<PortfolioDataHelper.ComputedHolding> bestHolding = summary.holdings().stream()
                .max(Comparator.comparing(PortfolioDataHelper.ComputedHolding::pnlPercentage));
        Optional<PortfolioDataHelper.ComputedHolding> worstHolding = summary.holdings().stream()
                .min(Comparator.comparing(PortfolioDataHelper.ComputedHolding::pnlPercentage));
        Optional<PortfolioDataHelper.ComputedHolding> mostAllocated = summary.holdings().stream()
                .max(Comparator.comparing(PortfolioDataHelper.ComputedHolding::currentValue));
        Optional<PortfolioDataHelper.ComputedHolding> highestLocked = summary.holdings().stream()
                .max(Comparator.comparing(PortfolioDataHelper.ComputedHolding::lockedShares));

        String topAllocPct = mostAllocated.isPresent() && totalValue.compareTo(BigDecimal.ZERO) > 0
                ? mostAllocated.get().currentValue().multiply(new BigDecimal("100")).divide(totalValue, 1, RoundingMode.HALF_UP).toPlainString() + "%"
                : "0%";

        List<PortfolioQuickInsightDto> quickInsights = List.of(
                PortfolioQuickInsightDto.builder()
                        .title("Best Performing Holding")
                        .value(bestHolding.map(h -> h.company().getCompanyName()).orElse("-"))
                        .change(bestHolding.map(h -> dataHelper.formatSignedPercent(h.pnlPercentage())).orElse("+0.00%"))
                        .isPositive(bestHolding.map(h -> h.pnlPercentage().compareTo(BigDecimal.ZERO) >= 0).orElse(true))
                        .icon("trending-up")
                        .build(),
                PortfolioQuickInsightDto.builder()
                        .title("Worst Performing Holding")
                        .value(worstHolding.map(h -> h.company().getCompanyName()).orElse("-"))
                        .change(worstHolding.map(h -> dataHelper.formatSignedPercent(h.pnlPercentage())).orElse("0.00%"))
                        .isPositive(worstHolding.map(h -> h.pnlPercentage().compareTo(BigDecimal.ZERO) >= 0).orElse(true))
                        .icon("trending-down")
                        .build(),
                PortfolioQuickInsightDto.builder()
                        .title("Most Allocated")
                        .value(mostAllocated.map(h -> h.company().getCompanyName()).orElse("-"))
                        .change(topAllocPct)
                        .isPositive(true)
                        .icon("pie-chart")
                        .build(),
                PortfolioQuickInsightDto.builder()
                        .title("Highest Locked Shares")
                        .value(highestLocked.map(h -> h.company().getCompanyName()).orElse("-"))
                        .change(highestLocked.map(h -> dataHelper.formatCount(h.lockedShares()) + " shares").orElse("0 shares"))
                        .isPositive(true)
                        .icon("lock")
                        .build(),
                PortfolioQuickInsightDto.builder()
                        .title("Total Dividend Received (YTD)")
                        .value("₹ 0")
                        .change(null)
                        .isPositive(true)
                        .icon("dollar-sign")
                        .build()
        );

        String insightSummary = summary.holdings().isEmpty()
                ? "You do not hold any active shares yet. Explore the Internal Market or Trade terminal to start building your portfolio."
                : "Your holdings total " + dataHelper.formatInr(totalValue) + " across " + summary.holdings().size()
                        + (summary.holdings().size() == 1 ? " company" : " companies")
                        + " with an overall unrealized return of " + dataHelper.formatSignedPercent(unrealizedPct) + ".";

        int pageSize = size > 0 ? size : 10;
        int currentPage = Math.max(page, 1);
        int totalCount = filtered.size();
        int totalPages = Math.max((int) Math.ceil((double) totalCount / pageSize), 1);
        int fromIndex = Math.min((currentPage - 1) * pageSize, totalCount);
        int toIndex = Math.min(fromIndex + pageSize, totalCount);
        List<PortfolioHoldingItemDto> paged = filtered.subList(fromIndex, toIndex);

        return PortfolioHoldingsResponseDto.builder()
                .asOfDate(OffsetDateTime.now().format(DISPLAY_DT_FORMATTER))
                .summaryKpis(summaryKpis)
                .holdings(paged)
                .allocation(allocation)
                .quickInsights(quickInsights)
                .insightSummary(insightSummary)
                .totalCount(totalCount)
                .currentPage(currentPage)
                .totalPages(totalPages)
                .build();
    }

    // =========================================================================
    // 3. PORTFOLIO ORDERS (Image 3)
    // =========================================================================
    @Transactional(readOnly = true)
    public PortfolioOrdersResponseDto getOrders(Long userId, String tab, String company, String orderType, String status, String startDate, String endDate, Long selectedOrderId, int page, int size) {
        String activeTab = (tab != null && !tab.isBlank()) ? tab.toUpperCase() : "ALL";

        List<PortfolioSummaryKpiDto> summaryKpis = List.of(
                PortfolioSummaryKpiDto.builder()
                        .id("total_orders")
                        .title("Total Orders")
                        .count(28L)
                        .formattedCount("28")
                        .comparisonLabel("All time")
                        .icon("file-text")
                        .build(),
                PortfolioSummaryKpiDto.builder()
                        .id("open_orders")
                        .title("Open Orders")
                        .count(6L)
                        .formattedCount("6")
                        .comparisonLabel("21.4%")
                        .icon("clock")
                        .build(),
                PortfolioSummaryKpiDto.builder()
                        .id("executed_orders")
                        .title("Executed Orders")
                        .count(18L)
                        .formattedCount("18")
                        .comparisonLabel("64.3%")
                        .icon("check-circle")
                        .build(),
                PortfolioSummaryKpiDto.builder()
                        .id("cancelled_orders")
                        .title("Cancelled Orders")
                        .count(3L)
                        .formattedCount("3")
                        .comparisonLabel("10.7%")
                        .icon("x-circle")
                        .build(),
                PortfolioSummaryKpiDto.builder()
                        .id("rejected_orders")
                        .title("Rejected Orders")
                        .count(1L)
                        .formattedCount("1")
                        .comparisonLabel("3.6%")
                        .icon("alert-triangle")
                        .build()
        );

        List<PortfolioOrderItemDto> allOrders = getOrdersList();

        // Apply Tab Filter
        List<PortfolioOrderItemDto> filtered = allOrders.stream()
                .filter(o -> {
                    if ("BUY".equalsIgnoreCase(activeTab) || "BUY_ORDERS".equalsIgnoreCase(activeTab)) {
                        return "BUY".equalsIgnoreCase(o.getOrderSide());
                    }
                    if ("SELL".equalsIgnoreCase(activeTab) || "SELL_ORDERS".equalsIgnoreCase(activeTab)) {
                        return "SELL".equalsIgnoreCase(o.getOrderSide());
                    }
                    if ("OPEN".equalsIgnoreCase(activeTab) || "OPEN_ORDERS".equalsIgnoreCase(activeTab)) {
                        return "Open".equalsIgnoreCase(o.getStatus());
                    }
                    if ("EXECUTED".equalsIgnoreCase(activeTab) || "EXECUTED_ORDERS".equalsIgnoreCase(activeTab)) {
                        return "Executed".equalsIgnoreCase(o.getStatus());
                    }
                    if ("CANCELLED".equalsIgnoreCase(activeTab) || "CANCELLED_ORDERS".equalsIgnoreCase(activeTab)) {
                        return "Cancelled".equalsIgnoreCase(o.getStatus());
                    }
                    if ("REJECTED".equalsIgnoreCase(activeTab) || "REJECTED_ORDERS".equalsIgnoreCase(activeTab)) {
                        return "Rejected".equalsIgnoreCase(o.getStatus());
                    }
                    return true;
                })
                .filter(o -> {
                    if (company != null && !company.isBlank() && !"ALL".equalsIgnoreCase(company)) {
                        boolean match = o.getCompanyName().equalsIgnoreCase(company) ||
                                        o.getCompanyCode().equalsIgnoreCase(company) ||
                                        (o.getSymbol() != null && o.getSymbol().equalsIgnoreCase(company));
                        if (!match) return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());

        // Default selected order detail
        PortfolioOrderDetailDto selectedDetail = getOrderDetail(selectedOrderId != null ? selectedOrderId : 1L, null, userId);

        int pageSize = size > 0 ? size : 10;
        int currentPage = Math.max(page, 1);
        int totalItems = filtered.size();
        int totalPages = Math.max((int) Math.ceil((double) totalItems / pageSize), 1);
        int fromIndex = (currentPage - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, totalItems);

        List<PortfolioOrderItemDto> pagedOrders = fromIndex < totalItems ? filtered.subList(fromIndex, toIndex) : Collections.emptyList();

        return PortfolioOrdersResponseDto.builder()
                .summaryKpis(summaryKpis)
                .activeTab(activeTab)
                .orders(pagedOrders)
                .selectedOrderDetail(selectedDetail)
                .totalOrdersCount(28)
                .currentPage(currentPage)
                .totalPages(totalPages)
                .pageSize(pageSize)
                .build();
    }

    @Transactional(readOnly = true)
    public PortfolioOrderDetailDto getOrderDetail(Long orderId, String orderNo, Long userId) {
        // Image 3 right side panel exact data: ORD250918001
        List<PortfolioTimelineStepDto> timeline = List.of(
                PortfolioTimelineStepDto.builder().stepNumber(1).stepName("Order Placed").timestamp("18 Sep 2025, 10:15 AM").status("COMPLETED").isCurrent(false).build(),
                PortfolioTimelineStepDto.builder().stepNumber(2).stepName("Order Validated").timestamp("18 Sep 2025, 10:15 AM").status("COMPLETED").isCurrent(false).build(),
                PortfolioTimelineStepDto.builder().stepNumber(3).stepName("Matched with Seller").timestamp("18 Sep 2025, 10:16 AM").status("COMPLETED").isCurrent(false).build(),
                PortfolioTimelineStepDto.builder().stepNumber(4).stepName("Trade Executed").timestamp("18 Sep 2025, 10:16 AM").status("COMPLETED").isCurrent(false).build(),
                PortfolioTimelineStepDto.builder().stepNumber(5).stepName("Sent to Escrow").timestamp("18 Sep 2025, 10:17 AM").status("COMPLETED").isCurrent(true).build()
        );

        if (orderNo != null && !orderNo.isBlank()) {
            Optional<PortfolioOrderItemDto> match = getOrdersList().stream().filter(o -> o.getOrderNo().equalsIgnoreCase(orderNo)).findFirst();
            if (match.isPresent()) {
                PortfolioOrderItemDto m = match.get();
                return PortfolioOrderDetailDto.builder()
                        .orderId(m.getOrderId())
                        .orderNo(m.getOrderNo())
                        .companyName(m.getCompanyName())
                        .symbol(m.getSymbol())
                        .orderSide(m.getOrderSide())
                        .quantity(m.getQuantity())
                        .formattedQuantity(m.getFormattedQuantity())
                        .pricePerShare(m.getPrice())
                        .formattedPricePerShare(m.getFormattedPrice())
                        .orderValue(m.getOrderValue())
                        .formattedOrderValue(m.getFormattedOrderValue())
                        .orderDateTime(m.getOrderDateTime())
                        .validity("Day Order")
                        .executedQuantity("Executed".equalsIgnoreCase(m.getStatus()) ? m.getQuantity() : 0L)
                        .formattedExecutedQuantity("Executed".equalsIgnoreCase(m.getStatus()) ? m.getFormattedQuantity() : "0")
                        .averageExecutedPrice(m.getPrice())
                        .formattedAverageExecutedPrice(m.getFormattedPrice())
                        .status(m.getStatus())
                        .settlementRef("SET" + m.getOrderNo().replace("ORD", ""))
                        .timeline(timeline)
                        .build();
            }
        }

        return PortfolioOrderDetailDto.builder()
                .orderId(1L)
                .orderNo("ORD250918001")
                .companyName("Atirath Holdings India Limited")
                .symbol("ATG-H")
                .orderSide("Buy")
                .quantity(1000L)
                .formattedQuantity("1,000")
                .pricePerShare(new BigDecimal("230.00"))
                .formattedPricePerShare("₹ 230.00")
                .orderValue(new BigDecimal("230000.00"))
                .formattedOrderValue("₹ 2,30,000")
                .orderDateTime("18 Sep 2025, 10:15 AM")
                .validity("Day Order")
                .executedQuantity(1000L)
                .formattedExecutedQuantity("1,000")
                .averageExecutedPrice(new BigDecimal("230.00"))
                .formattedAverageExecutedPrice("₹ 230.00")
                .status("Executed")
                .settlementRef("SET250918001")
                .timeline(timeline)
                .build();
    }

    // =========================================================================
    // 4. PORTFOLIO POSITIONS (Image 4)
    // =========================================================================
    // =========================================================================
    // 4. PORTFOLIO POSITIONS (Real Per-User DB Aggregation)
    // =========================================================================
    @Transactional(readOnly = true)
    public PortfolioPositionsResponseDto getPositions(Long userId, String tab, String company, String search) {
        return getPositions(userId, tab, company, search, "6M");
    }

    @Transactional(readOnly = true)
    public PortfolioPositionsResponseDto getPositions(Long userId, String tab, String company, String search, String period) {
        if (userId == null) {
            throw new ApiException("Authentication required to load positions", HttpStatus.UNAUTHORIZED);
        }

        String activeTab = (tab != null && !tab.isBlank()) ? tab.toUpperCase(Locale.ROOT) : "ALL";
        String activePeriod = dataHelper.normalizePeriod(period != null && !period.isBlank() ? period : "6M");

        List<UserShareHolding> rawHoldings = shareHoldingRepository.findByUserAccount_UserId(userId).stream()
                .filter(h -> h.getTotalShares() != null && h.getTotalShares() > 0 && h.getCompany() != null)
                .toList();

        PortfolioDataHelper.HoldingSummary summary = dataHelper.aggregateHoldings(rawHoldings);
        BigDecimal totalValue = summary.totalValue();
        BigDecimal investedAmount = summary.investedAmount();
        BigDecimal unrealizedPnl = totalValue.subtract(investedAmount).setScale(2, RoundingMode.HALF_UP);
        BigDecimal unrealizedPct = dataHelper.computePercent(unrealizedPnl, investedAmount);
        BigDecimal dayPnl = totalValue.subtract(summary.prevTotalValue()).setScale(2, RoundingMode.HALF_UP);
        BigDecimal dayPnlPct = dataHelper.computePercent(dayPnl, summary.prevTotalValue());

        List<PortfolioSummaryKpiDto> summaryKpis = List.of(
                PortfolioSummaryKpiDto.builder()
                        .id("total_open_positions")
                        .title("Total Open Positions")
                        .count((long) summary.holdings().size())
                        .formattedCount(dataHelper.formatCount(summary.holdings().size()))
                        .comparisonLabel("Across " + summary.holdings().size() + (summary.holdings().size() == 1 ? " company" : " companies"))
                        .icon("file-text")
                        .build(),
                PortfolioSummaryKpiDto.builder()
                        .id("total_invested_value")
                        .title("Total Invested Value")
                        .amount(investedAmount)
                        .formattedAmount(dataHelper.formatInr(investedAmount))
                        .comparisonLabel(null)
                        .icon("coins")
                        .build(),
                PortfolioSummaryKpiDto.builder()
                        .id("current_market_value")
                        .title("Current Market Value")
                        .amount(totalValue)
                        .formattedAmount(dataHelper.formatInr(totalValue))
                        .changePercentage(unrealizedPct)
                        .formattedChangePercentage(dataHelper.formatSignedPercent(unrealizedPct))
                        .isPositive(unrealizedPct.compareTo(BigDecimal.ZERO) >= 0)
                        .icon("trending-up")
                        .build(),
                PortfolioSummaryKpiDto.builder()
                        .id("unrealized_pnl")
                        .title("Unrealized P&L")
                        .amount(unrealizedPnl)
                        .formattedAmount(dataHelper.formatSignedInr(unrealizedPnl))
                        .changePercentage(unrealizedPct)
                        .formattedChangePercentage(dataHelper.formatSignedPercent(unrealizedPct))
                        .isPositive(unrealizedPnl.compareTo(BigDecimal.ZERO) >= 0)
                        .icon("bar-chart")
                        .build(),
                PortfolioSummaryKpiDto.builder()
                        .id("days_pnl")
                        .title("Day's P&L")
                        .amount(dayPnl)
                        .formattedAmount(dataHelper.formatSignedInr(dayPnl))
                        .changePercentage(dayPnlPct)
                        .formattedChangePercentage(dataHelper.formatSignedPercent(dayPnlPct))
                        .isPositive(dayPnl.compareTo(BigDecimal.ZERO) >= 0)
                        .icon("percent")
                        .build()
        );

        List<PortfolioPositionItemDto> allPositions = new ArrayList<>();
        List<PortfolioAllocationSliceDto> valueDistribution = new ArrayList<>();
        List<PortfolioDayMovementDto> daysMovement = new ArrayList<>();

        for (int i = 0; i < summary.holdings().size(); i++) {
            PortfolioDataHelper.ComputedHolding ch = summary.holdings().get(i);
            String color = PortfolioDataHelper.CHART_COLORS.get(i % PortfolioDataHelper.CHART_COLORS.size());
            boolean isProfit = ch.pnl().compareTo(BigDecimal.ZERO) >= 0;

            allPositions.add(PortfolioPositionItemDto.builder()
                    .positionId(ch.company().getCompanyId())
                    .companyId(ch.company().getCompanyId())
                    .companyName(ch.company().getCompanyName())
                    .companyCode(ch.company().getCompanyCode())
                    .symbol(ch.company().getSymbol())
                    .logoUrl(ch.company().getLogoUrl())
                    .positionType("Long")
                    .quantity(ch.shares())
                    .formattedQuantity(dataHelper.formatCount(ch.shares()))
                    .averagePrice(ch.avgPrice())
                    .formattedAveragePrice(dataHelper.formatInr(ch.avgPrice()))
                    .currentPrice(ch.currentPrice())
                    .formattedCurrentPrice(dataHelper.formatInr(ch.currentPrice()))
                    .investedValue(ch.investedValue())
                    .formattedInvestedValue(dataHelper.formatInr(ch.investedValue()))
                    .currentValue(ch.currentValue())
                    .formattedCurrentValue(dataHelper.formatInr(ch.currentValue()))
                    .unrealizedPnl(ch.pnl())
                    .formattedUnrealizedPnl(dataHelper.formatSignedInrWithPlus(ch.pnl()))
                    .unrealizedPnlPercentage(ch.pnlPercentage())
                    .formattedUnrealizedPnlPercentage(dataHelper.formatSignedPercent(ch.pnlPercentage()))
                    .isProfit(isProfit)
                    .build());

            BigDecimal distPct = totalValue.compareTo(BigDecimal.ZERO) > 0
                    ? ch.currentValue().multiply(new BigDecimal("100")).divide(totalValue, 1, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO.setScale(1, RoundingMode.HALF_UP);
            valueDistribution.add(PortfolioAllocationSliceDto.builder()
                    .companyId(ch.company().getCompanyId())
                    .companyName(ch.company().getCompanyName())
                    .symbol(ch.company().getSymbol())
                    .percentage(distPct)
                    .formattedPercentage(distPct.toPlainString() + "%")
                    .value(ch.currentValue())
                    .formattedValue(dataHelper.formatInr(ch.currentValue()))
                    .color(color)
                    .build());

            BigDecimal priceDelta = ch.currentPrice().subtract(ch.previousClose()).setScale(2, RoundingMode.HALF_UP);
            BigDecimal priceDeltaPct = dataHelper.computePercent(priceDelta, ch.previousClose());
            boolean movePositive = priceDelta.compareTo(BigDecimal.ZERO) >= 0;
            daysMovement.add(PortfolioDayMovementDto.builder()
                    .companyName(ch.company().getCompanyName())
                    .changeAmount(priceDelta)
                    .formattedChangeAmount((movePositive ? "+" : "") + priceDelta.toPlainString())
                    .changePercentage(priceDeltaPct)
                    .formattedChangePercentage(dataHelper.formatSignedPercent(priceDeltaPct))
                    .isPositive(movePositive)
                    .build());
        }

        List<PortfolioPositionItemDto> filtered = allPositions.stream()
                .filter(p -> {
                    if ("PROFITABLE".equalsIgnoreCase(activeTab)) {
                        return Boolean.TRUE.equals(p.getIsProfit());
                    }
                    if ("LOSS_MAKING".equalsIgnoreCase(activeTab) || "LOSS".equalsIgnoreCase(activeTab)) {
                        return Boolean.FALSE.equals(p.getIsProfit());
                    }
                    return true;
                })
                .filter(p -> {
                    if (company != null && !company.isBlank() && !"ALL".equalsIgnoreCase(company)) {
                        boolean match = (p.getCompanyName() != null && p.getCompanyName().equalsIgnoreCase(company))
                                || (p.getCompanyCode() != null && p.getCompanyCode().equalsIgnoreCase(company))
                                || (p.getSymbol() != null && p.getSymbol().equalsIgnoreCase(company));
                        if (!match) return false;
                    }
                    if (search != null && !search.isBlank()) {
                        String s = search.toLowerCase(Locale.ROOT);
                        boolean match = (p.getCompanyName() != null && p.getCompanyName().toLowerCase(Locale.ROOT).contains(s))
                                || (p.getCompanyCode() != null && p.getCompanyCode().toLowerCase(Locale.ROOT).contains(s))
                                || (p.getSymbol() != null && p.getSymbol().toLowerCase(Locale.ROOT).contains(s));
                        if (!match) return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());

        Optional<PortfolioAllocationSliceDto> largestDist = valueDistribution.stream()
                .max(Comparator.comparing(PortfolioAllocationSliceDto::getPercentage));
        Optional<PortfolioPositionItemDto> highestGain = allPositions.stream()
                .max(Comparator.comparing(PortfolioPositionItemDto::getUnrealizedPnlPercentage));
        Optional<PortfolioPositionItemDto> highestLoss = allPositions.stream()
                .min(Comparator.comparing(PortfolioPositionItemDto::getUnrealizedPnlPercentage));
        Optional<UserShareHolding> mostRecentHolding = rawHoldings.stream()
                .filter(h -> h.getUpdatedAt() != null || h.getCreatedAt() != null)
                .max(Comparator.comparing(h -> h.getUpdatedAt() != null ? h.getUpdatedAt() : h.getCreatedAt()));

        List<PortfolioQuickInsightDto> positionInsights = List.of(
                PortfolioQuickInsightDto.builder()
                        .title("Largest Position")
                        .value(largestDist.map(d -> d.getCompanyName() + " (" + d.getFormattedPercentage() + ")").orElse("-"))
                        .icon("pie-chart")
                        .build(),
                PortfolioQuickInsightDto.builder()
                        .title("Highest Gain")
                        .value(highestGain.map(p -> p.getCompanyName() + " (" + p.getFormattedUnrealizedPnlPercentage() + ")").orElse("-"))
                        .icon("trending-up")
                        .isPositive(highestGain.map(p -> Boolean.TRUE.equals(p.getIsProfit())).orElse(true))
                        .build(),
                PortfolioQuickInsightDto.builder()
                        .title("Highest Loss")
                        .value(highestLoss.map(p -> p.getCompanyName() + " (" + p.getFormattedUnrealizedPnlPercentage() + ")").orElse("-"))
                        .icon("trending-down")
                        .isPositive(highestLoss.map(p -> Boolean.TRUE.equals(p.getIsProfit())).orElse(true))
                        .build(),
                PortfolioQuickInsightDto.builder()
                        .title("Most Recent Position")
                        .value(mostRecentHolding.map(h -> {
                            OffsetDateTime dt = h.getUpdatedAt() != null ? h.getUpdatedAt() : h.getCreatedAt();
                            return h.getCompany().getCompanyName() + (dt != null ? " (" + dt.format(DATE_ONLY_FORMATTER) + ")" : "");
                        }).orElse("-"))
                        .icon("clock")
                        .build()
        );

        List<PortfolioTrendPointDto> valueTrend = dataHelper.buildPerformanceSeries(
                rawHoldings, totalValue, investedAmount, summary.prevTotalValue(), activePeriod, OffsetDateTime.now()
        ).stream().map(snap -> PortfolioTrendPointDto.builder()
                .period(snap.label())
                .date(snap.dateIso())
                .value(snap.value())
                .formattedValue(snap.formattedValue())
                .isCurrent(snap.isCurrent())
                .build()).toList();

        return PortfolioPositionsResponseDto.builder()
                .summaryKpis(summaryKpis)
                .activeTab(activeTab)
                .positions(filtered)
                .valueDistribution(valueDistribution)
                .positionInsights(positionInsights)
                .valueTrend(valueTrend)
                .daysMovement(daysMovement)
                .totalPositionsCount(allPositions.size())
                .build();
    }

    // =========================================================================
    // 5. PORTFOLIO TRADE HISTORY (Real Per-User DB Aggregation)
    // =========================================================================
    @Transactional(readOnly = true)
    public PortfolioTradesResponseDto getTradeHistory(Long userId, String search, String company, String tradeType, String status, String startDate, String endDate, Long selectedTradeId, int page, int size) {
        if (userId == null) {
            throw new ApiException("Authentication required to load trade history", HttpStatus.UNAUTHORIZED);
        }

        List<UserShareHolding> rawHoldings = shareHoldingRepository.findByUserAccount_UserId(userId);
        Map<Long, BigDecimal> avgCostByCompany = new HashMap<>();
        for (UserShareHolding h : rawHoldings) {
            if (h.getCompany() != null && h.getAverageBuyPrice() != null) {
                avgCostByCompany.put(h.getCompany().getCompanyId(), h.getAverageBuyPrice());
            }
        }

        List<MarketTrade> userTrades = tradeRepository.findUserTrades(userId);
        BigDecimal totalBuyValue = BigDecimal.ZERO;
        BigDecimal totalSellValue = BigDecimal.ZERO;
        BigDecimal realizedPnl = BigDecimal.ZERO;
        int profitableCount = 0;

        LocalDate startFilter = parseIsoDate(startDate);
        LocalDate endFilter = parseIsoDate(endDate);

        List<PortfolioTradeItemDto> allTradeItems = new ArrayList<>();
        for (MarketTrade t : userTrades) {
            boolean isBuy = t.getBuyerUser() != null && userId.equals(t.getBuyerUser().getUserId());
            String typeLabel = isBuy ? "Buy" : "Sell";
            BigDecimal val = t.getTradeValue() != null ? t.getTradeValue().setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            BigDecimal price = t.getTradePrice() != null ? t.getTradePrice().setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            long qty = t.getQuantity() != null ? t.getQuantity() : 0L;
            String rawStatus = t.getStatus() != null ? t.getStatus().toUpperCase(Locale.ROOT) : "EXECUTED";
            boolean cancelled = "REVERSED".equals(rawStatus) || "CANCELLED".equals(rawStatus);
            String statusLabel = cancelled ? "Cancelled" : "Settled";

            if (!cancelled) {
                if (isBuy) {
                    totalBuyValue = totalBuyValue.add(val);
                    if (t.getCompany() != null) {
                        BigDecimal currentPrice = dataHelper.resolvePriceQuote(t.getCompany(), price).currentPrice();
                        if (currentPrice.compareTo(price) >= 0) {
                            profitableCount++;
                        }
                    }
                } else {
                    totalSellValue = totalSellValue.add(val);
                    if (t.getCompany() != null) {
                        BigDecimal costBasis = avgCostByCompany.getOrDefault(t.getCompany().getCompanyId(), price);
                        BigDecimal pnl = price.subtract(costBasis).multiply(BigDecimal.valueOf(qty));
                        realizedPnl = realizedPnl.add(pnl);
                        if (pnl.compareTo(BigDecimal.ZERO) >= 0) {
                            profitableCount++;
                        }
                    }
                }
            }

            UserAccount counterpartyUser = isBuy ? t.getSellerUser() : t.getBuyerUser();
            String counterpartyName = counterpartyUser != null ? dataHelper.resolveUserFullName(counterpartyUser) : "-";
            String dateTimeLabel = t.getExecutedAt() != null ? t.getExecutedAt().format(DATE_ONLY_FORMATTER) + "\n" + t.getExecutedAt().format(DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH)) : "-";

            allTradeItems.add(PortfolioTradeItemDto.builder()
                    .tradeId(t.getTradeId())
                    .tradeNo(t.getTradeNo())
                    .tradeDateTime(dateTimeLabel)
                    .companyId(t.getCompany() != null ? t.getCompany().getCompanyId() : null)
                    .companyName(t.getCompany() != null ? t.getCompany().getCompanyName() : "Atirath Group")
                    .companyCode(t.getCompany() != null ? t.getCompany().getCompanyCode() : "ATG")
                    .symbol(t.getCompany() != null ? t.getCompany().getSymbol() : "ATG")
                    .logoUrl(t.getCompany() != null ? t.getCompany().getLogoUrl() : null)
                    .tradeType(typeLabel)
                    .quantity(qty)
                    .formattedQuantity(dataHelper.formatCount(qty))
                    .price(price)
                    .formattedPrice(dataHelper.formatInr(price))
                    .tradeValue(val)
                    .formattedTradeValue(dataHelper.formatInr(val))
                    .status(statusLabel)
                    .counterparty(counterpartyName)
                    .build());
        }

        totalBuyValue = totalBuyValue.setScale(2, RoundingMode.HALF_UP);
        totalSellValue = totalSellValue.setScale(2, RoundingMode.HALF_UP);
        realizedPnl = realizedPnl.setScale(2, RoundingMode.HALF_UP);
        BigDecimal realizedPct = dataHelper.computePercent(realizedPnl, totalSellValue.compareTo(BigDecimal.ZERO) > 0 ? totalSellValue : totalBuyValue);
        int winRatePct = userTrades.isEmpty() ? 0 : (int) Math.round((profitableCount * 100.0) / userTrades.size());

        List<PortfolioSummaryKpiDto> summaryKpis = List.of(
                PortfolioSummaryKpiDto.builder()
                        .id("total_trades")
                        .title("Total Trades")
                        .count((long) userTrades.size())
                        .formattedCount(dataHelper.formatCount(userTrades.size()))
                        .comparisonLabel("All time")
                        .icon("repeat")
                        .build(),
                PortfolioSummaryKpiDto.builder()
                        .id("total_buy_value")
                        .title("Total Buy Value")
                        .amount(totalBuyValue)
                        .formattedAmount(dataHelper.formatInr(totalBuyValue))
                        .comparisonLabel(null)
                        .icon("dollar-sign")
                        .build(),
                PortfolioSummaryKpiDto.builder()
                        .id("total_sell_value")
                        .title("Total Sell Value")
                        .amount(totalSellValue)
                        .formattedAmount(dataHelper.formatInr(totalSellValue))
                        .comparisonLabel(null)
                        .icon("dollar-sign")
                        .build(),
                PortfolioSummaryKpiDto.builder()
                        .id("realized_pnl")
                        .title("Realized P&L")
                        .amount(realizedPnl)
                        .formattedAmount(dataHelper.formatSignedInr(realizedPnl))
                        .changePercentage(realizedPct)
                        .formattedChangePercentage(dataHelper.formatSignedPercent(realizedPct))
                        .isPositive(realizedPnl.compareTo(BigDecimal.ZERO) >= 0)
                        .icon("trending-up")
                        .build(),
                PortfolioSummaryKpiDto.builder()
                        .id("profitable_trades")
                        .title("Profitable Trades")
                        .formattedCount(winRatePct + "%")
                        .comparisonLabel(profitableCount + " out of " + userTrades.size())
                        .icon("check-circle")
                        .build()
        );

        Map<Long, LocalDate> tradeDates = new HashMap<>();
        for (MarketTrade t : userTrades) {
            if (t.getExecutedAt() != null) {
                tradeDates.put(t.getTradeId(), t.getExecutedAt().toLocalDate());
            }
        }

        List<PortfolioTradeItemDto> filtered = allTradeItems.stream()
                .filter(t -> {
                    if (search != null && !search.isBlank()) {
                        String s = search.toLowerCase(Locale.ROOT);
                        boolean matchName = t.getCompanyName() != null && t.getCompanyName().toLowerCase(Locale.ROOT).contains(s);
                        boolean matchNo = t.getTradeNo() != null && t.getTradeNo().toLowerCase(Locale.ROOT).contains(s);
                        boolean matchParty = t.getCounterparty() != null && t.getCounterparty().toLowerCase(Locale.ROOT).contains(s);
                        if (!matchName && !matchNo && !matchParty) return false;
                    }
                    if (company != null && !company.isBlank() && !"ALL".equalsIgnoreCase(company)) {
                        boolean match = (t.getCompanyName() != null && t.getCompanyName().equalsIgnoreCase(company))
                                || (t.getCompanyCode() != null && t.getCompanyCode().equalsIgnoreCase(company))
                                || (t.getSymbol() != null && t.getSymbol().equalsIgnoreCase(company));
                        if (!match) return false;
                    }
                    if (tradeType != null && !tradeType.isBlank() && !"ALL".equalsIgnoreCase(tradeType)) {
                        if (!t.getTradeType().equalsIgnoreCase(tradeType)) return false;
                    }
                    if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
                        if (!t.getStatus().equalsIgnoreCase(status)) return false;
                    }
                    if (startFilter != null || endFilter != null) {
                        LocalDate d = tradeDates.get(t.getTradeId());
                        if (d == null) return false;
                        if (startFilter != null && d.isBefore(startFilter)) return false;
                        if (endFilter != null && d.isAfter(endFilter)) return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());

        int pageSize = size > 0 ? size : 10;
        int currentPage = Math.max(page, 1);
        int totalItems = filtered.size();
        int totalPages = Math.max((int) Math.ceil((double) totalItems / pageSize), 1);
        int fromIndex = Math.min((currentPage - 1) * pageSize, totalItems);
        int toIndex = Math.min(fromIndex + pageSize, totalItems);

        List<PortfolioTradeItemDto> pagedTrades = fromIndex < totalItems ? filtered.subList(fromIndex, toIndex) : Collections.emptyList();

        Long effectiveSelectedId = selectedTradeId;
        if (effectiveSelectedId == null && !pagedTrades.isEmpty()) {
            effectiveSelectedId = pagedTrades.get(0).getTradeId();
        }
        PortfolioTradeDetailDto selectedDetail = effectiveSelectedId != null
                ? buildTradeDetailSafe(effectiveSelectedId, userId)
                : null;

        return PortfolioTradesResponseDto.builder()
                .summaryKpis(summaryKpis)
                .trades(pagedTrades)
                .selectedTradeDetail(selectedDetail)
                .totalTradesCount(totalItems)
                .currentPage(currentPage)
                .totalPages(totalPages)
                .pageSize(pageSize)
                .build();
    }

    @Transactional(readOnly = true)
    public PortfolioTradeDetailDto getTradeDetail(Long tradeId, String tradeNo, Long userId) {
        if (userId == null) {
            throw new ApiException("Authentication required to load trade detail", HttpStatus.UNAUTHORIZED);
        }
        MarketTrade trade = null;
        if (tradeId != null) {
            trade = tradeRepository.findById(tradeId).orElse(null);
        } else if (tradeNo != null && !tradeNo.isBlank()) {
            trade = tradeRepository.findUserTrades(userId).stream()
                    .filter(t -> tradeNo.equalsIgnoreCase(t.getTradeNo()))
                    .findFirst()
                    .orElse(null);
        }
        if (trade == null) {
            throw new ResourceNotFoundException("Trade not found");
        }
        boolean isParticipant = (trade.getBuyerUser() != null && userId.equals(trade.getBuyerUser().getUserId()))
                || (trade.getSellerUser() != null && userId.equals(trade.getSellerUser().getUserId()));
        if (!isParticipant) {
            throw new ApiException("You are not authorized to view this trade", HttpStatus.FORBIDDEN);
        }
        return toTradeDetailDto(trade, userId);
    }

    private PortfolioTradeDetailDto buildTradeDetailSafe(Long tradeId, Long userId) {
        return tradeRepository.findById(tradeId)
                .filter(t -> (t.getBuyerUser() != null && userId.equals(t.getBuyerUser().getUserId()))
                        || (t.getSellerUser() != null && userId.equals(t.getSellerUser().getUserId())))
                .map(t -> toTradeDetailDto(t, userId))
                .orElse(null);
    }

    private PortfolioTradeDetailDto toTradeDetailDto(MarketTrade t, Long userId) {
        boolean isBuy = t.getBuyerUser() != null && userId.equals(t.getBuyerUser().getUserId());
        UserAccount otherUser = isBuy ? t.getSellerUser() : t.getBuyerUser();
        String counterparty = otherUser != null
                ? dataHelper.resolveUserFullName(otherUser) + " (ATG" + otherUser.getUserId() + ")"
                : "-";

        Long userOrderId = isBuy ? t.getBuyOrderId() : t.getSellOrderId();
        MarketOrder linkedOrder = userOrderId != null ? orderRepository.findById(userOrderId).orElse(null) : null;
        Escrow escrow = escrowRepository.findByTrade_TradeId(t.getTradeId()).orElse(null);

        String orderTime = linkedOrder != null && linkedOrder.getPlacedAt() != null
                ? linkedOrder.getPlacedAt().format(DISPLAY_DT_FORMATTER)
                : (t.getExecutedAt() != null ? t.getExecutedAt().format(DISPLAY_DT_FORMATTER) : "-");
        String tradeTime = t.getExecutedAt() != null ? t.getExecutedAt().format(DISPLAY_DT_FORMATTER) : "-";
        String settlementTime = escrow != null && escrow.getReleasedAt() != null
                ? escrow.getReleasedAt().format(DISPLAY_DT_FORMATTER)
                : tradeTime;
        String settlementRef = escrow != null && escrow.getEscrowReference() != null
                ? escrow.getEscrowReference()
                : "SET" + (t.getTradeNo() != null ? t.getTradeNo().replace("TRD", "") : t.getTradeId());

        String rawStatus = t.getStatus() != null ? t.getStatus().toUpperCase(Locale.ROOT) : "EXECUTED";
        boolean cancelled = "REVERSED".equals(rawStatus) || "CANCELLED".equals(rawStatus);
        String statusLabel = cancelled ? "Cancelled" : "Settled";
        String escrowTime = escrow != null && escrow.getCreatedAt() != null
                ? escrow.getCreatedAt().format(DISPLAY_DT_FORMATTER)
                : tradeTime;

        List<PortfolioTimelineStepDto> timeline = List.of(
                PortfolioTimelineStepDto.builder().stepNumber(1).stepName("Order Placed").timestamp(orderTime).status("COMPLETED").isCurrent(false).build(),
                PortfolioTimelineStepDto.builder().stepNumber(2).stepName(isBuy ? "Matched with Seller" : "Matched with Buyer").timestamp(tradeTime).status("COMPLETED").isCurrent(false).build(),
                PortfolioTimelineStepDto.builder().stepNumber(3).stepName("Payment to Escrow").timestamp(escrowTime).status(cancelled ? "PENDING" : "COMPLETED").isCurrent(false).build(),
                PortfolioTimelineStepDto.builder().stepNumber(4).stepName("Shares Locked").timestamp(escrowTime).status(cancelled ? "PENDING" : "COMPLETED").isCurrent(false).build(),
                PortfolioTimelineStepDto.builder().stepNumber(5).stepName("Shares Transferred").timestamp(tradeTime).status(cancelled ? "PENDING" : "COMPLETED").isCurrent(false).build(),
                PortfolioTimelineStepDto.builder().stepNumber(6).stepName(cancelled ? "Trade Cancelled" : "Settlement Completed").timestamp(settlementTime).status(cancelled ? "CANCELLED" : "COMPLETED").isCurrent(true).build()
        );

        long qty = t.getQuantity() != null ? t.getQuantity() : 0L;
        BigDecimal price = t.getTradePrice() != null ? t.getTradePrice().setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        BigDecimal val = t.getTradeValue() != null ? t.getTradeValue().setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        return PortfolioTradeDetailDto.builder()
                .tradeId(t.getTradeId())
                .tradeNo(t.getTradeNo())
                .companyName(t.getCompany() != null ? t.getCompany().getCompanyName() : "Atirath Group")
                .symbol(t.getCompany() != null ? t.getCompany().getSymbol() : "ATG")
                .tradeType(isBuy ? "Buy" : "Sell")
                .quantity(qty)
                .formattedQuantity(dataHelper.formatCount(qty))
                .pricePerShare(price)
                .formattedPricePerShare(dataHelper.formatInr(price))
                .totalValue(val)
                .formattedTotalValue(dataHelper.formatInr(val))
                .orderDateTime(orderTime)
                .tradeDateTime(tradeTime)
                .settlementDate(settlementTime)
                .status(statusLabel)
                .counterparty(counterparty)
                .settlementRef(settlementRef)
                .timeline(timeline)
                .build();
    }

    private LocalDate parseIsoDate(String val) {
        if (val == null || val.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(val.trim());
        } catch (Exception e) {
            return null;
        }
    }

    // =========================================================================
    // STATIC / SEED DATA PROVIDERS (Match images pixel-perfectly)
    // =========================================================================
    private List<PortfolioHoldingItemDto> getHoldingsList() {
        return List.of(
                PortfolioHoldingItemDto.builder()
                        .companyId(1L)
                        .companyName("Atirath Holdings India Limited")
                        .companyCode("ATG")
                        .symbol("ATG-H")
                        .sharesHeld(3500L)
                        .formattedSharesHeld("3,500")
                        .averageAcquisitionPrice(new BigDecimal("150.00"))
                        .formattedAverageAcquisitionPrice("₹ 150.00")
                        .currentPrice(new BigDecimal("230.00"))
                        .formattedCurrentPrice("₹ 230.00")
                        .currentValue(new BigDecimal("805000.00"))
                        .formattedCurrentValue("₹ 8,05,000")
                        .investedValue(new BigDecimal("525000.00"))
                        .formattedInvestedValue("₹ 5,25,000")
                        .unrealizedPnl(new BigDecimal("280000.00"))
                        .formattedUnrealizedPnl("+₹ 2,80,000")
                        .unrealizedPnlPercentage(new BigDecimal("53.8"))
                        .formattedUnrealizedPnlPercentage("+53.8%")
                        .isProfit(true)
                        .lockedShares(500L)
                        .formattedLockedShares("500")
                        .availableShares(3000L)
                        .formattedAvailableShares("3,000")
                        .allocationPercentage(new BigDecimal("35.5"))
                        .formattedAllocationPercentage("35.5%")
                        .progressBarColor("#10B981")
                        .build(),
                PortfolioHoldingItemDto.builder()
                        .companyId(2L)
                        .companyName("Atirath Technologies Private Limited")
                        .companyCode("ATT")
                        .symbol("ATG-T")
                        .sharesHeld(2000L)
                        .formattedSharesHeld("2,000")
                        .averageAcquisitionPrice(new BigDecimal("120.00"))
                        .formattedAverageAcquisitionPrice("₹ 120.00")
                        .currentPrice(new BigDecimal("180.00"))
                        .formattedCurrentPrice("₹ 180.00")
                        .currentValue(new BigDecimal("360000.00"))
                        .formattedCurrentValue("₹ 3,60,000")
                        .investedValue(new BigDecimal("240000.00"))
                        .formattedInvestedValue("₹ 2,40,000")
                        .unrealizedPnl(new BigDecimal("120000.00"))
                        .formattedUnrealizedPnl("+₹ 1,20,000")
                        .unrealizedPnlPercentage(new BigDecimal("50.0"))
                        .formattedUnrealizedPnlPercentage("+50.0%")
                        .isProfit(true)
                        .lockedShares(400L)
                        .formattedLockedShares("400")
                        .availableShares(1600L)
                        .formattedAvailableShares("1,600")
                        .allocationPercentage(new BigDecimal("20.6"))
                        .formattedAllocationPercentage("20.6%")
                        .progressBarColor("#3B82F6")
                        .build(),
                PortfolioHoldingItemDto.builder()
                        .companyId(3L)
                        .companyName("Farm Vantara Private Limited")
                        .companyCode("FVPL")
                        .symbol("ATG-FV")
                        .sharesHeld(1800L)
                        .formattedSharesHeld("1,800")
                        .averageAcquisitionPrice(new BigDecimal("100.00"))
                        .formattedAverageAcquisitionPrice("₹ 100.00")
                        .currentPrice(new BigDecimal("135.00"))
                        .formattedCurrentPrice("₹ 135.00")
                        .currentValue(new BigDecimal("243000.00"))
                        .formattedCurrentValue("₹ 2,43,000")
                        .investedValue(new BigDecimal("180000.00"))
                        .formattedInvestedValue("₹ 1,80,000")
                        .unrealizedPnl(new BigDecimal("63000.00"))
                        .formattedUnrealizedPnl("+₹ 63,000")
                        .unrealizedPnlPercentage(new BigDecimal("35.0"))
                        .formattedUnrealizedPnlPercentage("+35.0%")
                        .isProfit(true)
                        .lockedShares(300L)
                        .formattedLockedShares("300")
                        .availableShares(1500L)
                        .formattedAvailableShares("1,500")
                        .allocationPercentage(new BigDecimal("15.7"))
                        .formattedAllocationPercentage("15.7%")
                        .progressBarColor("#F59E0B")
                        .build(),
                PortfolioHoldingItemDto.builder()
                        .companyId(4L)
                        .companyName("Tri Energy Battery Private Limited")
                        .companyCode("TEB")
                        .symbol("ATG-TEB")
                        .sharesHeld(1200L)
                        .formattedSharesHeld("1,200")
                        .averageAcquisitionPrice(new BigDecimal("200.00"))
                        .formattedAverageAcquisitionPrice("₹ 200.00")
                        .currentPrice(new BigDecimal("225.00"))
                        .formattedCurrentPrice("₹ 225.00")
                        .currentValue(new BigDecimal("270000.00"))
                        .formattedCurrentValue("₹ 2,70,000")
                        .investedValue(new BigDecimal("240000.00"))
                        .formattedInvestedValue("₹ 2,40,000")
                        .unrealizedPnl(new BigDecimal("30000.00"))
                        .formattedUnrealizedPnl("+₹ 30,000")
                        .unrealizedPnlPercentage(new BigDecimal("12.5"))
                        .formattedUnrealizedPnlPercentage("+12.5%")
                        .isProfit(true)
                        .lockedShares(200L)
                        .formattedLockedShares("200")
                        .availableShares(1000L)
                        .formattedAvailableShares("1,000")
                        .allocationPercentage(new BigDecimal("10.9"))
                        .formattedAllocationPercentage("10.9%")
                        .progressBarColor("#8B5CF6")
                        .build(),
                PortfolioHoldingItemDto.builder()
                        .companyId(5L)
                        .companyName("Royal One Heating Equipments Pvt Ltd")
                        .companyCode("ROHE")
                        .symbol("ATG-ROHE")
                        .sharesHeld(900L)
                        .formattedSharesHeld("900")
                        .averageAcquisitionPrice(new BigDecimal("180.00"))
                        .formattedAverageAcquisitionPrice("₹ 180.00")
                        .currentPrice(new BigDecimal("235.00"))
                        .formattedCurrentPrice("₹ 235.00")
                        .currentValue(new BigDecimal("211500.00"))
                        .formattedCurrentValue("₹ 2,11,500")
                        .investedValue(new BigDecimal("162000.00"))
                        .formattedInvestedValue("₹ 1,62,000")
                        .unrealizedPnl(new BigDecimal("49500.00"))
                        .formattedUnrealizedPnl("+₹ 49,500")
                        .unrealizedPnlPercentage(new BigDecimal("30.4"))
                        .formattedUnrealizedPnlPercentage("+30.4%")
                        .isProfit(true)
                        .lockedShares(200L)
                        .formattedLockedShares("200")
                        .availableShares(700L)
                        .formattedAvailableShares("700")
                        .allocationPercentage(new BigDecimal("8.5"))
                        .formattedAllocationPercentage("8.5%")
                        .progressBarColor("#EF4444")
                        .build(),
                PortfolioHoldingItemDto.builder()
                        .companyId(6L)
                        .companyName("CS Interio Private Limited")
                        .companyCode("CSI")
                        .symbol("ATG-CSI")
                        .sharesHeld(600L)
                        .formattedSharesHeld("600")
                        .averageAcquisitionPrice(new BigDecimal("160.00"))
                        .formattedAverageAcquisitionPrice("₹ 160.00")
                        .currentPrice(new BigDecimal("145.00"))
                        .formattedCurrentPrice("₹ 145.00")
                        .currentValue(new BigDecimal("87000.00"))
                        .formattedCurrentValue("₹ 87,000")
                        .investedValue(new BigDecimal("96000.00"))
                        .formattedInvestedValue("₹ 96,000")
                        .unrealizedPnl(new BigDecimal("-9000.00"))
                        .formattedUnrealizedPnl("-₹ 9,000")
                        .unrealizedPnlPercentage(new BigDecimal("-9.4"))
                        .formattedUnrealizedPnlPercentage("-9.4%")
                        .isProfit(false)
                        .lockedShares(150L)
                        .formattedLockedShares("150")
                        .availableShares(450L)
                        .formattedAvailableShares("450")
                        .allocationPercentage(new BigDecimal("5.8"))
                        .formattedAllocationPercentage("5.8%")
                        .progressBarColor("#06B6D4")
                        .build()
        );
    }

    private List<PortfolioAllocationSliceDto> getAllocationData() {
        return List.of(
                PortfolioAllocationSliceDto.builder().companyName("Atirath Holdings").percentage(new BigDecimal("35.5")).formattedPercentage("35.5%").value(new BigDecimal("805000.00")).formattedValue("₹ 8,05,000").color("#1E40AF").build(),
                PortfolioAllocationSliceDto.builder().companyName("Atirath Technologies").percentage(new BigDecimal("20.6")).formattedPercentage("20.6%").value(new BigDecimal("360000.00")).formattedValue("₹ 3,60,000").color("#3B82F6").build(),
                PortfolioAllocationSliceDto.builder().companyName("Farm Vantara").percentage(new BigDecimal("15.7")).formattedPercentage("15.7%").value(new BigDecimal("243000.00")).formattedValue("₹ 2,43,000").color("#F59E0B").build(),
                PortfolioAllocationSliceDto.builder().companyName("Tri Energy Battery").percentage(new BigDecimal("10.9")).formattedPercentage("10.9%").value(new BigDecimal("270000.00")).formattedValue("₹ 2,70,000").color("#8B5CF6").build(),
                PortfolioAllocationSliceDto.builder().companyName("Royal One Heating").percentage(new BigDecimal("8.5")).formattedPercentage("8.5%").value(new BigDecimal("211500.00")).formattedValue("₹ 2,11,500").color("#EF4444").build(),
                PortfolioAllocationSliceDto.builder().companyName("CS Interio").percentage(new BigDecimal("5.8")).formattedPercentage("5.8%").value(new BigDecimal("87000.00")).formattedValue("₹ 87,000").color("#10B981").build(),
                PortfolioAllocationSliceDto.builder().companyName("Others").percentage(new BigDecimal("3.0")).formattedPercentage("3.0%").value(new BigDecimal("74400.00")).formattedValue("₹ 74,400").color("#64748B").build()
        );
    }

    private List<PortfolioTrendPointDto> getTrendData(String period) {
        return List.of(
                PortfolioTrendPointDto.builder().period("Mar 2025").date("2025-03-31").value(new BigDecimal("1120000.00")).formattedValue("₹ 11.2L").isCurrent(false).build(),
                PortfolioTrendPointDto.builder().period("Apr 2025").date("2025-04-30").value(new BigDecimal("1350000.00")).formattedValue("₹ 13.5L").isCurrent(false).build(),
                PortfolioTrendPointDto.builder().period("May 2025").date("2025-05-31").value(new BigDecimal("1510000.00")).formattedValue("₹ 15.1L").isCurrent(false).build(),
                PortfolioTrendPointDto.builder().period("Jun 2025").date("2025-06-30").value(new BigDecimal("1680000.00")).formattedValue("₹ 16.8L").isCurrent(false).build(),
                PortfolioTrendPointDto.builder().period("Jul 2025").date("2025-07-31").value(new BigDecimal("1920000.00")).formattedValue("₹ 19.2L").isCurrent(false).build(),
                PortfolioTrendPointDto.builder().period("Aug 2025").date("2025-08-31").value(new BigDecimal("2240000.00")).formattedValue("₹ 22.4L").isCurrent(false).build(),
                PortfolioTrendPointDto.builder().period("18 Sep 2025").date("2025-09-18").value(new BigDecimal("2480000.00")).formattedValue("₹ 24,80,000").isCurrent(true).build()
        );
    }

    private List<PortfolioTrendPointDto> getPositionTrendData() {
        return List.of(
                PortfolioTrendPointDto.builder().period("Mar 2025").date("2025-03-31").value(new BigDecimal("210000.00")).formattedValue("₹ 2.1L").isCurrent(false).build(),
                PortfolioTrendPointDto.builder().period("Apr 2025").date("2025-04-30").value(new BigDecimal("350000.00")).formattedValue("₹ 3.5L").isCurrent(false).build(),
                PortfolioTrendPointDto.builder().period("May 2025").date("2025-05-31").value(new BigDecimal("480000.00")).formattedValue("₹ 4.8L").isCurrent(false).build(),
                PortfolioTrendPointDto.builder().period("Jun 2025").date("2025-06-30").value(new BigDecimal("520000.00")).formattedValue("₹ 5.2L").isCurrent(false).build(),
                PortfolioTrendPointDto.builder().period("Jul 2025").date("2025-07-31").value(new BigDecimal("690000.00")).formattedValue("₹ 6.9L").isCurrent(false).build(),
                PortfolioTrendPointDto.builder().period("Aug 2025").date("2025-08-31").value(new BigDecimal("780000.00")).formattedValue("₹ 7.8L").isCurrent(false).build(),
                PortfolioTrendPointDto.builder().period("18 Sep 2025").date("2025-09-18").value(new BigDecimal("867500.00")).formattedValue("₹ 8,67,500").isCurrent(true).build()
        );
    }

    private List<PortfolioOrderItemDto> getOrdersList() {
        return List.of(
                PortfolioOrderItemDto.builder().orderId(1L).orderNo("ORD250918001").companyId(1L).companyName("Atirath Holdings India Limited").companyCode("ATG").symbol("ATG-H").orderSide("Buy").quantity(1000L).formattedQuantity("1,000").price(new BigDecimal("230.00")).formattedPrice("230.00").orderValue(new BigDecimal("230000.00")).formattedOrderValue("2,30,000").orderDateTime("18 Sep 2025\n10:15 AM").status("Executed").build(),
                PortfolioOrderItemDto.builder().orderId(2L).orderNo("ORD250917006").companyId(6L).companyName("CS Interio Private Limited").companyCode("CSI").symbol("ATG-CSI").orderSide("Sell").quantity(500L).formattedQuantity("500").price(new BigDecimal("145.00")).formattedPrice("145.00").orderValue(new BigDecimal("72500.00")).formattedOrderValue("72,500").orderDateTime("17 Sep 2025\n04:32 PM").status("Open").build(),
                PortfolioOrderItemDto.builder().orderId(3L).orderNo("ORD250917005").companyId(3L).companyName("Farm Vantara Private Limited").companyCode("FVPL").symbol("ATG-FV").orderSide("Buy").quantity(750L).formattedQuantity("750").price(new BigDecimal("135.00")).formattedPrice("135.00").orderValue(new BigDecimal("101250.00")).formattedOrderValue("1,01,250").orderDateTime("17 Sep 2025\n11:20 AM").status("Executed").build(),
                PortfolioOrderItemDto.builder().orderId(4L).orderNo("ORD250916004").companyId(2L).companyName("Atirath Technologies Private Limited").companyCode("ATT").symbol("ATG-T").orderSide("Sell").quantity(1200L).formattedQuantity("1,200").price(new BigDecimal("180.00")).formattedPrice("180.00").orderValue(new BigDecimal("216000.00")).formattedOrderValue("2,16,000").orderDateTime("16 Sep 2025\n02:45 PM").status("Cancelled").build(),
                PortfolioOrderItemDto.builder().orderId(5L).orderNo("ORD250915003").companyId(4L).companyName("Tri Energy Battery Private Limited").companyCode("TEB").symbol("ATG-TEB").orderSide("Buy").quantity(600L).formattedQuantity("600").price(new BigDecimal("225.00")).formattedPrice("225.00").orderValue(new BigDecimal("135000.00")).formattedOrderValue("1,35,000").orderDateTime("15 Sep 2025\n12:10 PM").status("Executed").build(),
                PortfolioOrderItemDto.builder().orderId(6L).orderNo("ORD250915002").companyId(5L).companyName("Royal One Heating Equipments Pvt Ltd").companyCode("ROHE").symbol("ATG-ROHE").orderSide("Buy").quantity(400L).formattedQuantity("400").price(new BigDecimal("235.00")).formattedPrice("235.00").orderValue(new BigDecimal("94000.00")).formattedOrderValue("94,000").orderDateTime("15 Sep 2025\n11:35 AM").status("Rejected").build(),
                PortfolioOrderItemDto.builder().orderId(7L).orderNo("ORD250914001").companyId(1L).companyName("Atirath Holdings India Limited").companyCode("ATG").symbol("ATG-H").orderSide("Sell").quantity(800L).formattedQuantity("800").price(new BigDecimal("240.00")).formattedPrice("240.00").orderValue(new BigDecimal("192000.00")).formattedOrderValue("1,92,000").orderDateTime("14 Sep 2025\n03:20 PM").status("Executed").build(),
                PortfolioOrderItemDto.builder().orderId(8L).orderNo("ORD250913008").companyId(6L).companyName("CS Interio Private Limited").companyCode("CSI").symbol("ATG-CSI").orderSide("Buy").quantity(1000L).formattedQuantity("1,000").price(new BigDecimal("160.00")).formattedPrice("160.00").orderValue(new BigDecimal("160000.00")).formattedOrderValue("1,60,000").orderDateTime("13 Sep 2025\n10:05 AM").status("Open").build(),
                PortfolioOrderItemDto.builder().orderId(9L).orderNo("ORD250912007").companyId(3L).companyName("Farm Vantara Private Limited").companyCode("FVPL").symbol("ATG-FV").orderSide("Sell").quantity(300L).formattedQuantity("300").price(new BigDecimal("140.00")).formattedPrice("140.00").orderValue(new BigDecimal("42000.00")).formattedOrderValue("42,000").orderDateTime("12 Sep 2025\n04:18 PM").status("Executed").build(),
                PortfolioOrderItemDto.builder().orderId(10L).orderNo("ORD250911006").companyId(2L).companyName("Atirath Technologies Private Limited").companyCode("ATT").symbol("ATG-T").orderSide("Buy").quantity(500L).formattedQuantity("500").price(new BigDecimal("175.00")).formattedPrice("175.00").orderValue(new BigDecimal("87500.00")).formattedOrderValue("87,500").orderDateTime("11 Sep 2025\n01:25 PM").status("Executed").build()
        );
    }

    // =========================================================================
    // 6. PORTFOLIO ANALYTICS (Real Per-User DB Aggregation)
    // =========================================================================
    @Transactional(readOnly = true)
    public PortfolioAnalyticsResponseDto getAnalytics(Long userId, String allocationTab, String trendPeriod, String dateRange) {
        if (userId == null) {
            throw new ApiException("Authentication required to load portfolio analytics", HttpStatus.UNAUTHORIZED);
        }

        String activeAllocTab = (allocationTab != null && !allocationTab.isBlank()) ? allocationTab : "By Company";
        String activePeriod = dataHelper.normalizePeriod(trendPeriod != null && !trendPeriod.isBlank() ? trendPeriod : "6M");
        OffsetDateTime now = OffsetDateTime.now();
        String activeDateRange = (dateRange != null && !dateRange.isBlank() && !dateRange.contains("2025"))
                ? dateRange
                : now.minusMonths(6).format(DATE_ONLY_FORMATTER) + " - " + now.format(DATE_ONLY_FORMATTER);

        List<UserShareHolding> rawHoldings = shareHoldingRepository.findByUserAccount_UserId(userId).stream()
                .filter(h -> h.getTotalShares() != null && h.getTotalShares() > 0 && h.getCompany() != null)
                .toList();
        PortfolioDataHelper.HoldingSummary summary = dataHelper.aggregateHoldings(rawHoldings);

        BigDecimal totalValue = summary.totalValue();
        BigDecimal investedAmount = summary.investedAmount();
        BigDecimal unrealizedPnl = totalValue.subtract(investedAmount).setScale(2, RoundingMode.HALF_UP);
        BigDecimal dayChange = totalValue.subtract(summary.prevTotalValue());
        BigDecimal dayChangePct = dataHelper.computePercent(dayChange, summary.prevTotalValue());
        BigDecimal unrealizedPct = dataHelper.computePercent(unrealizedPnl, investedAmount);

        List<MarketTrade> userTrades = tradeRepository.findUserTrades(userId);
        Map<Long, BigDecimal> avgCostByCompany = new HashMap<>();
        for (PortfolioDataHelper.ComputedHolding ch : summary.holdings()) {
            avgCostByCompany.put(ch.company().getCompanyId(), ch.avgPrice());
        }

        BigDecimal realizedPnl = BigDecimal.ZERO;
        for (MarketTrade t : userTrades) {
            if (t.getSellerUser() != null && userId.equals(t.getSellerUser().getUserId())
                    && t.getCompany() != null && t.getTradePrice() != null && t.getQuantity() != null
                    && !"REVERSED".equalsIgnoreCase(t.getStatus()) && !"CANCELLED".equalsIgnoreCase(t.getStatus())) {
                BigDecimal cost = avgCostByCompany.getOrDefault(t.getCompany().getCompanyId(), t.getTradePrice());
                realizedPnl = realizedPnl.add(t.getTradePrice().subtract(cost).multiply(BigDecimal.valueOf(t.getQuantity())));
            }
        }
        realizedPnl = realizedPnl.setScale(2, RoundingMode.HALF_UP);
        BigDecimal realizedPct = dataHelper.computePercent(realizedPnl, investedAmount);
        BigDecimal totalCombinedPnl = unrealizedPnl.add(realizedPnl).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalReturnPct = dataHelper.computePercent(totalCombinedPnl, investedAmount);

        List<PortfolioSummaryKpiDto> summaryKpis = List.of(
                PortfolioSummaryKpiDto.builder()
                        .id("total_portfolio_value").title("Total Portfolio Value")
                        .amount(totalValue).formattedAmount(dataHelper.formatInr(totalValue))
                        .changePercentage(dayChangePct).formattedChangePercentage(dataHelper.formatSignedPercent(dayChangePct))
                        .isPositive(dayChangePct.compareTo(BigDecimal.ZERO) >= 0)
                        .comparisonLabel("vs. previous close").icon("wallet").build(),
                PortfolioSummaryKpiDto.builder()
                        .id("total_invested_amount").title("Total Invested Amount")
                        .amount(investedAmount).formattedAmount(dataHelper.formatInr(investedAmount))
                        .changePercentage(unrealizedPct).formattedChangePercentage(dataHelper.formatSignedPercent(unrealizedPct))
                        .isPositive(unrealizedPct.compareTo(BigDecimal.ZERO) >= 0)
                        .comparisonLabel("cost basis").icon("coins").build(),
                PortfolioSummaryKpiDto.builder()
                        .id("unrealized_pnl").title("Unrealized P&L")
                        .amount(unrealizedPnl).formattedAmount(dataHelper.formatSignedInr(unrealizedPnl))
                        .changePercentage(unrealizedPct).formattedChangePercentage(dataHelper.formatSignedPercent(unrealizedPct))
                        .isPositive(unrealizedPnl.compareTo(BigDecimal.ZERO) >= 0)
                        .comparisonLabel("open holdings").icon("trending-up").build(),
                PortfolioSummaryKpiDto.builder()
                        .id("realized_pnl").title("Realized P&L")
                        .amount(realizedPnl).formattedAmount(dataHelper.formatSignedInr(realizedPnl))
                        .changePercentage(realizedPct).formattedChangePercentage(dataHelper.formatSignedPercent(realizedPct))
                        .isPositive(realizedPnl.compareTo(BigDecimal.ZERO) >= 0)
                        .comparisonLabel("executed sells").icon("dollar-sign").build(),
                PortfolioSummaryKpiDto.builder()
                        .id("total_return").title("Total Return")
                        .formattedCount(dataHelper.formatSignedPercent(totalReturnPct))
                        .comparisonLabel("(Realized + Unrealized)").icon("percent").build()
        );

        List<PortfolioAllocationSliceDto> byCompany = new ArrayList<>();
        Map<String, BigDecimal> sectorTotals = new LinkedHashMap<>();
        BigDecimal availableEquityVal = BigDecimal.ZERO;
        BigDecimal lockedEquityVal = BigDecimal.ZERO;
        List<PortfolioAnalyticsPerformanceRowDto> companyPerf = new ArrayList<>();

        for (int i = 0; i < summary.holdings().size(); i++) {
            PortfolioDataHelper.ComputedHolding ch = summary.holdings().get(i);
            String color = PortfolioDataHelper.CHART_COLORS.get(i % PortfolioDataHelper.CHART_COLORS.size());
            BigDecimal pct = totalValue.compareTo(BigDecimal.ZERO) > 0
                    ? ch.currentValue().multiply(new BigDecimal("100")).divide(totalValue, 1, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO.setScale(1, RoundingMode.HALF_UP);

            byCompany.add(PortfolioAllocationSliceDto.builder()
                    .companyId(ch.company().getCompanyId())
                    .companyName(ch.company().getCompanyName())
                    .symbol(ch.company().getSymbol())
                    .percentage(pct).formattedPercentage(pct.toPlainString() + "%")
                    .value(ch.currentValue()).formattedValue(dataHelper.formatInr(ch.currentValue()))
                    .color(color).build());

            String sector = ch.company().getSector() != null && !ch.company().getSector().isBlank()
                    ? ch.company().getSector().trim()
                    : "Diversified";
            sectorTotals.merge(sector, ch.currentValue(), BigDecimal::add);

            availableEquityVal = availableEquityVal.add(ch.currentPrice().multiply(BigDecimal.valueOf(ch.availableShares())));
            lockedEquityVal = lockedEquityVal.add(ch.currentPrice().multiply(BigDecimal.valueOf(ch.lockedShares())));

            companyPerf.add(PortfolioAnalyticsPerformanceRowDto.builder()
                    .companyId(ch.company().getCompanyId())
                    .companyName(ch.company().getCompanyName())
                    .symbol(ch.company().getSymbol())
                    .logoUrl(ch.company().getLogoUrl())
                    .investedAmount(ch.investedValue())
                    .formattedInvestedAmount(dataHelper.formatInr(ch.investedValue()))
                    .currentValue(ch.currentValue())
                    .formattedCurrentValue(dataHelper.formatInr(ch.currentValue()))
                    .unrealizedPnl(ch.pnl())
                    .formattedUnrealizedPnl(dataHelper.formatSignedInrWithPlus(ch.pnl()))
                    .returnPercentage(ch.pnlPercentage())
                    .formattedReturnPercentage(dataHelper.formatSignedPercent(ch.pnlPercentage()))
                    .isPositive(ch.pnl().compareTo(BigDecimal.ZERO) >= 0)
                    .build());
        }

        List<PortfolioAllocationSliceDto> bySector = new ArrayList<>();
        int sIdx = 0;
        for (Map.Entry<String, BigDecimal> e : sectorTotals.entrySet()) {
            BigDecimal pct = totalValue.compareTo(BigDecimal.ZERO) > 0
                    ? e.getValue().multiply(new BigDecimal("100")).divide(totalValue, 1, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO.setScale(1, RoundingMode.HALF_UP);
            bySector.add(PortfolioAllocationSliceDto.builder()
                    .companyName(e.getKey())
                    .percentage(pct).formattedPercentage(pct.toPlainString() + "%")
                    .value(e.getValue()).formattedValue(dataHelper.formatInr(e.getValue()))
                    .color(PortfolioDataHelper.CHART_COLORS.get(sIdx % PortfolioDataHelper.CHART_COLORS.size()))
                    .build());
            sIdx++;
        }

        EscrowAccount escrowAcc = escrowAccountRepository.findByUserAccount_UserId(userId).orElse(null);
        BigDecimal escrowAvail = escrowAcc != null && escrowAcc.getAvailableBalance() != null ? escrowAcc.getAvailableBalance() : BigDecimal.ZERO;
        BigDecimal escrowBlocked = escrowAcc != null && escrowAcc.getBlockedBalance() != null ? escrowAcc.getBlockedBalance() : BigDecimal.ZERO;

        availableEquityVal = availableEquityVal.setScale(2, RoundingMode.HALF_UP);
        lockedEquityVal = lockedEquityVal.setScale(2, RoundingMode.HALF_UP);

        List<PortfolioAllocationSliceDto> byType = new ArrayList<>();
        if (totalValue.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal availPct = availableEquityVal.multiply(new BigDecimal("100")).divide(totalValue, 1, RoundingMode.HALF_UP);
            byType.add(PortfolioAllocationSliceDto.builder().companyName("Direct Equity (Available)").percentage(availPct).formattedPercentage(availPct.toPlainString() + "%").value(availableEquityVal).formattedValue(dataHelper.formatInr(availableEquityVal)).color("#1E40AF").build());
            if (lockedEquityVal.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal lockPct = lockedEquityVal.multiply(new BigDecimal("100")).divide(totalValue, 1, RoundingMode.HALF_UP);
                byType.add(PortfolioAllocationSliceDto.builder().companyName("Escrow Locked Equity").percentage(lockPct).formattedPercentage(lockPct.toPlainString() + "%").value(lockedEquityVal).formattedValue(dataHelper.formatInr(lockedEquityVal)).color("#F59E0B").build());
            }
        }

        BigDecimal totalAllAssets = availableEquityVal.add(lockedEquityVal).add(escrowBlocked).add(escrowAvail);
        List<PortfolioAllocationSliceDto> assetTypeAlloc = new ArrayList<>();
        if (totalAllAssets.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal eqPct = availableEquityVal.multiply(new BigDecimal("100")).divide(totalAllAssets, 1, RoundingMode.HALF_UP);
            BigDecimal lkPct = lockedEquityVal.multiply(new BigDecimal("100")).divide(totalAllAssets, 1, RoundingMode.HALF_UP);
            BigDecimal blPct = escrowBlocked.multiply(new BigDecimal("100")).divide(totalAllAssets, 1, RoundingMode.HALF_UP);
            BigDecimal avPct = escrowAvail.multiply(new BigDecimal("100")).divide(totalAllAssets, 1, RoundingMode.HALF_UP);
            assetTypeAlloc.add(PortfolioAllocationSliceDto.builder().companyName("Available Equity").percentage(eqPct).formattedPercentage(eqPct.toPlainString() + "%").value(availableEquityVal).formattedValue(dataHelper.formatInr(availableEquityVal)).color("#1E40AF").build());
            assetTypeAlloc.add(PortfolioAllocationSliceDto.builder().companyName("Locked Shares").percentage(lkPct).formattedPercentage(lkPct.toPlainString() + "%").value(lockedEquityVal).formattedValue(dataHelper.formatInr(lockedEquityVal)).color("#10B981").build());
            assetTypeAlloc.add(PortfolioAllocationSliceDto.builder().companyName("Escrow Blocked").percentage(blPct).formattedPercentage(blPct.toPlainString() + "%").value(escrowBlocked).formattedValue(dataHelper.formatInr(escrowBlocked)).color("#8B5CF6").build());
            assetTypeAlloc.add(PortfolioAllocationSliceDto.builder().companyName("Escrow Cash").percentage(avPct).formattedPercentage(avPct.toPlainString() + "%").value(escrowAvail).formattedValue(dataHelper.formatInr(escrowAvail)).color("#F59E0B").build());
        }

        List<PortfolioTrendPointDto> pvTrend = dataHelper.buildPerformanceSeries(
                rawHoldings, totalValue, investedAmount, summary.prevTotalValue(), activePeriod, now
        ).stream().map(snap -> PortfolioTrendPointDto.builder()
                .period(snap.label())
                .date(snap.dateIso())
                .value(snap.value())
                .formattedValue(snap.formattedValue())
                .isCurrent(snap.isCurrent())
                .build()).toList();

        List<PortfolioTrendPointDto> invTrend = pvTrend.stream()
                .map(pt -> PortfolioTrendPointDto.builder()
                        .period(pt.getPeriod())
                        .date(pt.getDate())
                        .value(investedAmount)
                        .formattedValue(dataHelper.formatInr(investedAmount))
                        .isCurrent(pt.getIsCurrent())
                        .build())
                .toList();

        BigDecimal annualizedReturn = dataHelper.computePeriodReturnPct(
                rawHoldings, totalValue, investedAmount, now.toLocalDate().minusYears(1)
        );
        if (annualizedReturn.compareTo(BigDecimal.ZERO) == 0 && unrealizedPct.compareTo(BigDecimal.ZERO) != 0) {
            annualizedReturn = unrealizedPct;
        }
        PortfolioReturnAnalysisDto returnAnalysis = PortfolioReturnAnalysisDto.builder()
                .totalReturn(totalReturnPct).formattedTotalReturn(totalReturnPct.toPlainString() + "%")
                .annualizedReturn(annualizedReturn).formattedAnnualizedReturn(annualizedReturn.toPlainString() + "%")
                .priceAppreciation(unrealizedPct).formattedPriceAppreciation(unrealizedPct.toPlainString() + "%")
                .dividendReturn(BigDecimal.ZERO.setScale(1, RoundingMode.HALF_UP)).formattedDividendReturn("0.0%")
                .build();

        List<PortfolioPriceMoverDto> allMovers = new ArrayList<>();
        for (MarketCompany comp : companyRepository.findByStatusOrderByCompanyNameAsc("ACTIVE")) {
            PortfolioDataHelper.PriceQuote q = dataHelper.resolvePriceQuote(comp, BigDecimal.ZERO);
            if (q.currentPrice().compareTo(BigDecimal.ZERO) <= 0) continue;
            BigDecimal chg = q.currentPrice().subtract(q.previousClose()).setScale(2, RoundingMode.HALF_UP);
            BigDecimal chgPct = dataHelper.computePercent(chg, q.previousClose());
            boolean isGainer = chg.compareTo(BigDecimal.ZERO) >= 0;
            allMovers.add(PortfolioPriceMoverDto.builder()
                    .companyName(comp.getCompanyName())
                    .symbol(comp.getSymbol())
                    .currentPrice(q.currentPrice())
                    .formattedCurrentPrice(q.currentPrice().toPlainString())
                    .changeAmount(chg)
                    .formattedChangeAmount((isGainer ? "+ " : "- ") + chg.abs().toPlainString())
                    .changePercentage(chgPct)
                    .formattedChangePercentage(dataHelper.formatSignedPercent(chgPct))
                    .isGainer(isGainer)
                    .build());
        }
        List<PortfolioPriceMoverDto> gainers = allMovers.stream()
                .filter(m -> Boolean.TRUE.equals(m.getIsGainer()))
                .sorted(Comparator.comparing(PortfolioPriceMoverDto::getChangePercentage).reversed())
                .limit(3)
                .toList();
        List<PortfolioPriceMoverDto> losers = allMovers.stream()
                .filter(m -> Boolean.FALSE.equals(m.getIsGainer()))
                .sorted(Comparator.comparing(PortfolioPriceMoverDto::getChangePercentage))
                .limit(3)
                .toList();

        List<String> keyInsights = new ArrayList<>();
        if (summary.holdings().isEmpty()) {
            keyInsights.add("No active equity holdings found yet. Place your first order from Internal Market to start tracking analytics.");
        } else {
            keyInsights.add("Your total portfolio return (realized + unrealized) is " + dataHelper.formatSignedPercent(totalReturnPct) + ".");
            Optional<PortfolioDataHelper.ComputedHolding> best = summary.holdings().stream().max(Comparator.comparing(PortfolioDataHelper.ComputedHolding::pnlPercentage));
            best.ifPresent(b -> keyInsights.add(b.company().getCompanyName() + " is your best performing investment (" + dataHelper.formatSignedPercent(b.pnlPercentage()) + ")."));
            if (summary.holdings().size() >= 2 && totalValue.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal topTwo = summary.holdings().get(0).currentValue().add(summary.holdings().get(1).currentValue());
                BigDecimal concPct = topTwo.multiply(new BigDecimal("100")).divide(totalValue, 1, RoundingMode.HALF_UP);
                keyInsights.add("Top 2 holdings account for " + concPct.toPlainString() + "% of your portfolio value.");
            }
            Optional<PortfolioDataHelper.ComputedHolding> worst = summary.holdings().stream().min(Comparator.comparing(PortfolioDataHelper.ComputedHolding::pnlPercentage));
            if (worst.isPresent() && worst.get().pnl().compareTo(BigDecimal.ZERO) < 0) {
                keyInsights.add(worst.get().company().getCompanyName() + " is currently below cost basis (" + dataHelper.formatSignedPercent(worst.get().pnlPercentage()) + ").");
            }
            keyInsights.add("Portfolio is invested across " + summary.holdings().size() + (summary.holdings().size() == 1 ? " company" : " companies") + " and " + bySector.size() + (bySector.size() == 1 ? " sector." : " sectors."));
        }

        return PortfolioAnalyticsResponseDto.builder()
                .asOfDate(now.format(DISPLAY_DT_FORMATTER))
                .dateRange(activeDateRange)
                .summaryKpis(summaryKpis)
                .activeAllocationTab(activeAllocTab)
                .allocationByCompany(byCompany)
                .allocationBySector(bySector)
                .allocationByInvestmentType(byType)
                .activeTrendPeriod(activePeriod)
                .portfolioValueTrend(pvTrend)
                .investedAmountTrend(invTrend)
                .returnAnalysis(returnAnalysis)
                .companyPerformance(companyPerf)
                .assetTypeAllocation(assetTypeAlloc)
                .keyInsights(keyInsights)
                .topGainers(gainers)
                .topLosers(losers)
                .build();
    }

    // =========================================================================
    // 7. PORTFOLIO P&L REPORT (Real Per-User DB Aggregation)
    // =========================================================================
    @Transactional(readOnly = true)
    public PortfolioPnlResponseDto getPnlReport(Long userId, String tab, String company, String sector, String dateRange) {
        if (userId == null) {
            throw new ApiException("Authentication required to load P&L report", HttpStatus.UNAUTHORIZED);
        }

        String activeTab = (tab != null && !tab.isBlank()) ? tab : "Company-wise P&L";
        OffsetDateTime now = OffsetDateTime.now();
        String activeDateRange = (dateRange != null && !dateRange.isBlank() && !dateRange.contains("2025"))
                ? dateRange
                : now.minusMonths(6).format(DATE_ONLY_FORMATTER) + " - " + now.format(DATE_ONLY_FORMATTER);

        List<UserShareHolding> rawHoldings = shareHoldingRepository.findByUserAccount_UserId(userId).stream()
                .filter(h -> h.getTotalShares() != null && h.getTotalShares() > 0 && h.getCompany() != null)
                .toList();
        PortfolioDataHelper.HoldingSummary summary = dataHelper.aggregateHoldings(rawHoldings);

        List<MarketTrade> userTrades = tradeRepository.findUserTrades(userId);
        Map<Long, BigDecimal> avgCostByCompany = new HashMap<>();
        for (UserShareHolding h : shareHoldingRepository.findByUserAccount_UserId(userId)) {
            if (h.getCompany() != null && h.getAverageBuyPrice() != null) {
                avgCostByCompany.put(h.getCompany().getCompanyId(), h.getAverageBuyPrice());
            }
        }

        Map<Long, BigDecimal> realizedByCompany = new HashMap<>();
        Map<YearMonth, BigDecimal> realizedByMonth = new TreeMap<>();
        for (MarketTrade t : userTrades) {
            if (t.getSellerUser() != null && userId.equals(t.getSellerUser().getUserId())
                    && t.getCompany() != null && t.getTradePrice() != null && t.getQuantity() != null
                    && !"REVERSED".equalsIgnoreCase(t.getStatus()) && !"CANCELLED".equalsIgnoreCase(t.getStatus())) {
                BigDecimal cost = avgCostByCompany.getOrDefault(t.getCompany().getCompanyId(), t.getTradePrice());
                BigDecimal pnl = t.getTradePrice().subtract(cost).multiply(BigDecimal.valueOf(t.getQuantity()));
                realizedByCompany.merge(t.getCompany().getCompanyId(), pnl, BigDecimal::add);
                if (t.getExecutedAt() != null) {
                    realizedByMonth.merge(YearMonth.from(t.getExecutedAt()), pnl, BigDecimal::add);
                }
            }
        }

        List<PortfolioDataHelper.ComputedHolding> filteredHoldings = summary.holdings().stream()
                .filter(ch -> {
                    if (company != null && !company.isBlank() && !"ALL".equalsIgnoreCase(company)) {
                        MarketCompany c = ch.company();
                        boolean match = c.getCompanyName().equalsIgnoreCase(company)
                                || (c.getCompanyCode() != null && c.getCompanyCode().equalsIgnoreCase(company))
                                || (c.getSymbol() != null && c.getSymbol().equalsIgnoreCase(company));
                        if (!match) return false;
                    }
                    if (sector != null && !sector.isBlank() && !"ALL".equalsIgnoreCase(sector)) {
                        String compSector = ch.company().getSector() != null ? ch.company().getSector() : "Diversified";
                        if (!compSector.equalsIgnoreCase(sector)) return false;
                    }
                    return true;
                })
                .toList();

        BigDecimal totalInvestment = BigDecimal.ZERO;
        BigDecimal currentValue = BigDecimal.ZERO;
        BigDecimal unrealizedPnl = BigDecimal.ZERO;
        BigDecimal realizedPnl = BigDecimal.ZERO;

        List<PortfolioCompanyPnlRowDto> companyPnlList = new ArrayList<>();
        Map<String, BigDecimal[]> sectorAgg = new LinkedHashMap<>();

        for (PortfolioDataHelper.ComputedHolding ch : filteredHoldings) {
            Long cid = ch.company().getCompanyId();
            BigDecimal compRealized = realizedByCompany.getOrDefault(cid, BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
            BigDecimal compUnrealized = ch.pnl().setScale(2, RoundingMode.HALF_UP);
            BigDecimal compTotalPnl = compRealized.add(compUnrealized).setScale(2, RoundingMode.HALF_UP);
            BigDecimal retPct = dataHelper.computePercent(compTotalPnl, ch.investedValue());

            totalInvestment = totalInvestment.add(ch.investedValue());
            currentValue = currentValue.add(ch.currentValue());
            unrealizedPnl = unrealizedPnl.add(compUnrealized);
            realizedPnl = realizedPnl.add(compRealized);

            companyPnlList.add(PortfolioCompanyPnlRowDto.builder()
                    .companyId(cid)
                    .companyName(ch.company().getCompanyName())
                    .symbol(ch.company().getSymbol())
                    .companyCode(ch.company().getCompanyCode())
                    .logoUrl(ch.company().getLogoUrl())
                    .investedAmount(ch.investedValue())
                    .formattedInvestedAmount(dataHelper.formatInr(ch.investedValue()))
                    .currentValue(ch.currentValue())
                    .formattedCurrentValue(dataHelper.formatInr(ch.currentValue()))
                    .realizedPnl(compRealized)
                    .formattedRealizedPnl(dataHelper.formatSignedInr(compRealized))
                    .unrealizedPnl(compUnrealized)
                    .formattedUnrealizedPnl(dataHelper.formatSignedInr(compUnrealized))
                    .totalPnl(compTotalPnl)
                    .formattedTotalPnl(dataHelper.formatSignedInr(compTotalPnl))
                    .returnPercentage(retPct)
                    .formattedReturnPercentage(dataHelper.formatSignedPercent(retPct))
                    .isProfit(compTotalPnl.compareTo(BigDecimal.ZERO) >= 0)
                    .build());

            String sec = ch.company().getSector() != null && !ch.company().getSector().isBlank() ? ch.company().getSector() : "Diversified";
            BigDecimal[] arr = sectorAgg.computeIfAbsent(sec, k -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO});
            arr[0] = arr[0].add(ch.investedValue());
            arr[1] = arr[1].add(ch.currentValue());
            arr[2] = arr[2].add(compRealized);
            arr[3] = arr[3].add(compUnrealized);
        }

        if ("Sector-wise".equalsIgnoreCase(activeTab)) {
            companyPnlList = new ArrayList<>();
            for (Map.Entry<String, BigDecimal[]> entry : sectorAgg.entrySet()) {
                BigDecimal[] arr = entry.getValue();
                BigDecimal inv = arr[0].setScale(2, RoundingMode.HALF_UP);
                BigDecimal cur = arr[1].setScale(2, RoundingMode.HALF_UP);
                BigDecimal rel = arr[2].setScale(2, RoundingMode.HALF_UP);
                BigDecimal unrel = arr[3].setScale(2, RoundingMode.HALF_UP);
                BigDecimal tot = rel.add(unrel).setScale(2, RoundingMode.HALF_UP);
                BigDecimal ret = dataHelper.computePercent(tot, inv);
                companyPnlList.add(PortfolioCompanyPnlRowDto.builder()
                        .companyName(entry.getKey())
                        .companyCode("SECTOR")
                        .investedAmount(inv).formattedInvestedAmount(dataHelper.formatInr(inv))
                        .currentValue(cur).formattedCurrentValue(dataHelper.formatInr(cur))
                        .realizedPnl(rel).formattedRealizedPnl(dataHelper.formatSignedInr(rel))
                        .unrealizedPnl(unrel).formattedUnrealizedPnl(dataHelper.formatSignedInr(unrel))
                        .totalPnl(tot).formattedTotalPnl(dataHelper.formatSignedInr(tot))
                        .returnPercentage(ret).formattedReturnPercentage(dataHelper.formatSignedPercent(ret))
                        .isProfit(tot.compareTo(BigDecimal.ZERO) >= 0)
                        .build());
            }
        } else if ("Realized P&L".equalsIgnoreCase(activeTab)) {
            companyPnlList = companyPnlList.stream()
                    .filter(r -> r.getRealizedPnl() != null && r.getRealizedPnl().compareTo(BigDecimal.ZERO) != 0)
                    .toList();
        } else if ("Unrealized P&L".equalsIgnoreCase(activeTab)) {
            companyPnlList = companyPnlList.stream()
                    .filter(r -> r.getUnrealizedPnl() != null && r.getUnrealizedPnl().compareTo(BigDecimal.ZERO) != 0)
                    .toList();
        }

        totalInvestment = totalInvestment.setScale(2, RoundingMode.HALF_UP);
        currentValue = currentValue.setScale(2, RoundingMode.HALF_UP);
        realizedPnl = realizedPnl.setScale(2, RoundingMode.HALF_UP);
        unrealizedPnl = unrealizedPnl.setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalPnl = realizedPnl.add(unrealizedPnl).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalRetPct = dataHelper.computePercent(totalPnl, totalInvestment);
        BigDecimal realizedRetPct = dataHelper.computePercent(realizedPnl, totalInvestment);
        BigDecimal unrealizedRetPct = dataHelper.computePercent(unrealizedPnl, totalInvestment);

        List<PortfolioSummaryKpiDto> summaryKpis = List.of(
                PortfolioSummaryKpiDto.builder().id("total_investment").title("Total Investment").amount(totalInvestment).formattedAmount(dataHelper.formatInr(totalInvestment)).icon("coins").build(),
                PortfolioSummaryKpiDto.builder().id("current_value").title("Current Value").amount(currentValue).formattedAmount(dataHelper.formatInr(currentValue)).changePercentage(unrealizedRetPct).formattedChangePercentage(dataHelper.formatSignedPercent(unrealizedRetPct)).isPositive(unrealizedRetPct.compareTo(BigDecimal.ZERO) >= 0).icon("trending-up").build(),
                PortfolioSummaryKpiDto.builder().id("total_pnl").title("Total P&L").amount(totalPnl).formattedAmount(dataHelper.formatSignedInr(totalPnl)).changePercentage(totalRetPct).formattedChangePercentage(dataHelper.formatSignedPercent(totalRetPct)).isPositive(totalPnl.compareTo(BigDecimal.ZERO) >= 0).icon("bar-chart").build(),
                PortfolioSummaryKpiDto.builder().id("realized_pnl").title("Realized P&L").amount(realizedPnl).formattedAmount(dataHelper.formatSignedInr(realizedPnl)).changePercentage(realizedRetPct).formattedChangePercentage(dataHelper.formatSignedPercent(realizedRetPct)).isPositive(realizedPnl.compareTo(BigDecimal.ZERO) >= 0).icon("dollar-sign").build(),
                PortfolioSummaryKpiDto.builder().id("unrealized_pnl").title("Unrealized P&L").amount(unrealizedPnl).formattedAmount(dataHelper.formatSignedInr(unrealizedPnl)).changePercentage(unrealizedRetPct).formattedChangePercentage(dataHelper.formatSignedPercent(unrealizedRetPct)).isPositive(unrealizedPnl.compareTo(BigDecimal.ZERO) >= 0).icon("trending-up").build()
        );

        PortfolioPnlSummaryDto pnlSummary = PortfolioPnlSummaryDto.builder()
                .totalInvestment(totalInvestment).formattedTotalInvestment(dataHelper.formatInr(totalInvestment))
                .currentValue(currentValue).formattedCurrentValue(dataHelper.formatInr(currentValue))
                .totalPnl(totalPnl).formattedTotalPnl(dataHelper.formatSignedInr(totalPnl))
                .totalReturn(totalRetPct).formattedTotalReturn(dataHelper.formatSignedPercent(totalRetPct))
                .realizedPnl(realizedPnl).formattedRealizedPnl(dataHelper.formatSignedInr(realizedPnl))
                .unrealizedPnl(unrealizedPnl).formattedUnrealizedPnl(dataHelper.formatSignedInr(unrealizedPnl))
                .realizedReturn(realizedRetPct).formattedRealizedReturn(dataHelper.formatSignedPercent(realizedRetPct))
                .unrealizedReturn(unrealizedRetPct).formattedUnrealizedReturn(dataHelper.formatSignedPercent(unrealizedRetPct))
                .build();

        List<PortfolioDataHelper.TrendSnapshot> perfSeries = dataHelper.buildPerformanceSeries(
                rawHoldings, currentValue, totalInvestment, summary.prevTotalValue(), "6M", now
        );
        List<PortfolioPnlTrendPointDto> pnlTrend = new ArrayList<>();
        if (perfSeries.isEmpty() && realizedByMonth.isEmpty()) {
            YearMonth ym = YearMonth.from(now);
            pnlTrend.add(PortfolioPnlTrendPointDto.builder()
                    .month(ym.format(MONTH_YEAR_FORMATTER))
                    .realizedPnl(realizedPnl).formattedRealizedPnl(dataHelper.formatSignedInr(realizedPnl))
                    .unrealizedPnl(unrealizedPnl).formattedUnrealizedPnl(dataHelper.formatSignedInr(unrealizedPnl))
                    .totalPnl(totalPnl).formattedTotalPnl(dataHelper.formatSignedInr(totalPnl))
                    .build());
        } else {
            BigDecimal runningRealized = BigDecimal.ZERO;
            YearMonth currentYm = YearMonth.from(now);
            for (PortfolioDataHelper.TrendSnapshot snap : perfSeries) {
                BigDecimal unrel = snap.value().subtract(totalInvestment).setScale(2, RoundingMode.HALF_UP);
                YearMonth snapYm = currentYm;
                try {
                    snapYm = YearMonth.from(LocalDate.parse(snap.dateIso()));
                } catch (Exception ignored) {}
                BigDecimal rel = realizedByMonth.getOrDefault(snapYm, BigDecimal.ZERO);
                runningRealized = runningRealized.add(rel);
                BigDecimal relVal = snap.isCurrent() ? realizedPnl : runningRealized.setScale(2, RoundingMode.HALF_UP);
                BigDecimal totVal = relVal.add(unrel).setScale(2, RoundingMode.HALF_UP);
                pnlTrend.add(PortfolioPnlTrendPointDto.builder()
                        .month(snap.label())
                        .realizedPnl(relVal).formattedRealizedPnl(dataHelper.formatSignedInr(relVal))
                        .unrealizedPnl(unrel).formattedUnrealizedPnl(dataHelper.formatSignedInr(unrel))
                        .totalPnl(totVal).formattedTotalPnl(dataHelper.formatSignedInr(totVal))
                        .build());
            }
        }

        List<String> keyInsights = new ArrayList<>();
        if (filteredHoldings.isEmpty()) {
            keyInsights.add("No P&L data available for the selected filters or current account.");
        } else {
            keyInsights.add("Your total return across selected holdings is " + dataHelper.formatSignedPercent(totalRetPct) + ".");
            filteredHoldings.stream().max(Comparator.comparing(PortfolioDataHelper.ComputedHolding::pnlPercentage))
                    .ifPresent(b -> keyInsights.add(b.company().getCompanyName() + " is your top performing holding (" + dataHelper.formatSignedPercent(b.pnlPercentage()) + ")."));
            filteredHoldings.stream().min(Comparator.comparing(PortfolioDataHelper.ComputedHolding::pnlPercentage))
                    .filter(w -> w.pnl().compareTo(BigDecimal.ZERO) < 0)
                    .ifPresent(w -> keyInsights.add(w.company().getCompanyName() + " is underperforming (" + dataHelper.formatSignedPercent(w.pnlPercentage()) + "). Review your position."));
            if (totalPnl.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal unrelShare = unrealizedPnl.multiply(new BigDecimal("100")).divide(totalPnl, 0, RoundingMode.HALF_UP);
                keyInsights.add(unrelShare.toPlainString() + "% of your total gains are unrealized.");
            }
            keyInsights.add("Your holdings span " + filteredHoldings.size() + (filteredHoldings.size() == 1 ? " company" : " companies") + " across " + sectorAgg.size() + (sectorAgg.size() == 1 ? " sector." : " sectors."));
        }

        return PortfolioPnlResponseDto.builder()
                .dateRange(activeDateRange)
                .summaryKpis(summaryKpis)
                .pnlTrend(pnlTrend)
                .pnlSummary(pnlSummary)
                .activeTab(activeTab)
                .companyPnl(companyPnlList)
                .keyInsights(keyInsights)
                .totalCompaniesCount(companyPnlList.size())
                .build();
    }

    // =========================================================================
    // 8. PORTFOLIO OWNERSHIP STATEMENT (Real Per-User DB Aggregation)
    // =========================================================================
    @Transactional(readOnly = true)
    public PortfolioOwnershipResponseDto getOwnershipStatement(Long userId, String tab, String asOfDate) {
        if (userId == null) {
            throw new ApiException("Authentication required to load ownership statement", HttpStatus.UNAUTHORIZED);
        }

        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User account not found"));

        String activeTab = (tab != null && !tab.isBlank()) ? tab : "Company-wise";
        OffsetDateTime now = OffsetDateTime.now();
        String date = (asOfDate != null && !asOfDate.isBlank() && !asOfDate.contains("2025"))
                ? asOfDate
                : now.format(DATE_ONLY_FORMATTER);

        List<UserShareHolding> rawHoldings = shareHoldingRepository.findByUserAccount_UserId(userId).stream()
                .filter(h -> h.getTotalShares() != null && h.getTotalShares() > 0 && h.getCompany() != null)
                .toList();
        PortfolioDataHelper.HoldingSummary summary = dataHelper.aggregateHoldings(rawHoldings);

        BigDecimal totalInvestment = summary.investedAmount();
        BigDecimal currentValue = summary.totalValue();
        BigDecimal unrealizedPnl = currentValue.subtract(totalInvestment).setScale(2, RoundingMode.HALF_UP);
        BigDecimal unrealizedPct = dataHelper.computePercent(unrealizedPnl, totalInvestment);
        long totalSharesOwned = summary.totalShares();

        long totalIssuedAcrossHeld = 0L;
        for (PortfolioDataHelper.ComputedHolding ch : summary.holdings()) {
            long issued = ch.company().getIssuedShares() != null ? ch.company().getIssuedShares() : 0L;
            totalIssuedAcrossHeld += issued;
        }
        BigDecimal totalOwnershipPct = totalIssuedAcrossHeld > 0
                ? BigDecimal.valueOf(totalSharesOwned).multiply(new BigDecimal("100"))
                        .divide(BigDecimal.valueOf(totalIssuedAcrossHeld), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        BigDecimal weightedAvgPrice = totalSharesOwned > 0
                ? totalInvestment.divide(BigDecimal.valueOf(totalSharesOwned), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        List<PortfolioSummaryKpiDto> summaryKpis = List.of(
                PortfolioSummaryKpiDto.builder().id("total_companies").title("Total Companies").count((long) summary.holdings().size()).formattedCount(dataHelper.formatCount(summary.holdings().size())).comparisonLabel("Group Companies").icon("layers").build(),
                PortfolioSummaryKpiDto.builder().id("total_shares_owned").title("Total Shares Owned").count(totalSharesOwned).formattedCount(dataHelper.formatCount(totalSharesOwned)).comparisonLabel("Shares").icon("pie-chart").build(),
                PortfolioSummaryKpiDto.builder().id("total_investment").title("Total Investment").amount(totalInvestment).formattedAmount(dataHelper.formatInr(totalInvestment)).icon("coins").build(),
                PortfolioSummaryKpiDto.builder().id("current_market_value").title("Current Market Value").amount(currentValue).formattedAmount(dataHelper.formatInr(currentValue)).changePercentage(unrealizedPct).formattedChangePercentage(dataHelper.formatSignedPercent(unrealizedPct)).isPositive(unrealizedPct.compareTo(BigDecimal.ZERO) >= 0).icon("trending-up").build(),
                PortfolioSummaryKpiDto.builder().id("total_ownership").title("Total Ownership").formattedCount(totalOwnershipPct.toPlainString() + "%").comparisonLabel("Across Group").icon("percent").build()
        );

        List<PortfolioCompanyOwnershipRowDto> rows = new ArrayList<>();
        List<PortfolioAllocationSliceDto> ownershipDistribution = new ArrayList<>();
        List<PortfolioAnalyticsPerformanceRowDto> invVsCur = new ArrayList<>();

        Map<String, List<PortfolioDataHelper.ComputedHolding>> groupedBySector = new LinkedHashMap<>();
        long totalAvailable = 0L;
        long totalLocked = 0L;
        BigDecimal availInv = BigDecimal.ZERO;
        BigDecimal availCur = BigDecimal.ZERO;
        BigDecimal lockInv = BigDecimal.ZERO;
        BigDecimal lockCur = BigDecimal.ZERO;

        for (int i = 0; i < summary.holdings().size(); i++) {
            PortfolioDataHelper.ComputedHolding ch = summary.holdings().get(i);
            MarketCompany comp = ch.company();
            String color = PortfolioDataHelper.CHART_COLORS.get(i % PortfolioDataHelper.CHART_COLORS.size());

            long issued = comp.getIssuedShares() != null ? comp.getIssuedShares() : 0L;
            BigDecimal compOwnPct = issued > 0
                    ? BigDecimal.valueOf(ch.shares()).multiply(new BigDecimal("100")).divide(BigDecimal.valueOf(issued), 2, RoundingMode.HALF_UP)
                    : (totalSharesOwned > 0
                            ? BigDecimal.valueOf(ch.shares()).multiply(new BigDecimal("100")).divide(BigDecimal.valueOf(totalSharesOwned), 2, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));

            BigDecimal shareDistPct = totalSharesOwned > 0
                    ? BigDecimal.valueOf(ch.shares()).multiply(new BigDecimal("100")).divide(BigDecimal.valueOf(totalSharesOwned), 1, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO.setScale(1, RoundingMode.HALF_UP);

            ownershipDistribution.add(PortfolioAllocationSliceDto.builder()
                    .companyId(comp.getCompanyId())
                    .companyName(comp.getCompanyName())
                    .symbol(comp.getSymbol())
                    .percentage(shareDistPct)
                    .formattedPercentage(shareDistPct.toPlainString() + "%")
                    .value(ch.currentValue())
                    .formattedValue(dataHelper.formatInr(ch.currentValue()))
                    .color(color)
                    .build());

            invVsCur.add(PortfolioAnalyticsPerformanceRowDto.builder()
                    .companyId(comp.getCompanyId())
                    .companyName(comp.getCompanyCode() != null ? comp.getCompanyCode() : comp.getSymbol())
                    .symbol(comp.getSymbol())
                    .investedAmount(ch.investedValue())
                    .currentValue(ch.currentValue())
                    .build());

            String sec = comp.getSector() != null && !comp.getSector().isBlank() ? comp.getSector() : "Diversified";
            groupedBySector.computeIfAbsent(sec, k -> new ArrayList<>()).add(ch);

            totalAvailable += ch.availableShares();
            totalLocked += ch.lockedShares();
            availInv = availInv.add(ch.avgPrice().multiply(BigDecimal.valueOf(ch.availableShares())));
            availCur = availCur.add(ch.currentPrice().multiply(BigDecimal.valueOf(ch.availableShares())));
            lockInv = lockInv.add(ch.avgPrice().multiply(BigDecimal.valueOf(ch.lockedShares())));
            lockCur = lockCur.add(ch.currentPrice().multiply(BigDecimal.valueOf(ch.lockedShares())));

            if ("Company-wise".equalsIgnoreCase(activeTab) || (!"Sector-wise".equalsIgnoreCase(activeTab) && !"Share Class-wise".equalsIgnoreCase(activeTab))) {
                rows.add(PortfolioCompanyOwnershipRowDto.builder()
                        .companyId(comp.getCompanyId())
                        .companyName(comp.getCompanyName())
                        .companyCode(comp.getCompanyCode())
                        .symbol(comp.getSymbol())
                        .logoUrl(comp.getLogoUrl())
                        .totalSharesOwned(ch.shares())
                        .formattedTotalSharesOwned(dataHelper.formatCount(ch.shares()))
                        .ownershipPercentage(compOwnPct)
                        .formattedOwnershipPercentage(compOwnPct.toPlainString() + "%")
                        .averagePrice(ch.avgPrice())
                        .formattedAveragePrice(dataHelper.formatInr(ch.avgPrice()))
                        .totalInvestment(ch.investedValue())
                        .formattedTotalInvestment(dataHelper.formatInr(ch.investedValue()))
                        .currentPrice(ch.currentPrice())
                        .formattedCurrentPrice(dataHelper.formatInr(ch.currentPrice()))
                        .currentValue(ch.currentValue())
                        .formattedCurrentValue(dataHelper.formatInr(ch.currentValue()))
                        .unrealizedPnl(ch.pnl())
                        .formattedUnrealizedPnl(dataHelper.formatSignedInr(ch.pnl()))
                        .unrealizedPnlPercentage(ch.pnlPercentage())
                        .formattedUnrealizedPnlPercentage(dataHelper.formatSignedPercent(ch.pnlPercentage()))
                        .isProfit(ch.pnl().compareTo(BigDecimal.ZERO) >= 0)
                        .build());
            }
        }

        if ("Sector-wise".equalsIgnoreCase(activeTab)) {
            for (Map.Entry<String, List<PortfolioDataHelper.ComputedHolding>> entry : groupedBySector.entrySet()) {
                long secShares = entry.getValue().stream().mapToLong(PortfolioDataHelper.ComputedHolding::shares).sum();
                BigDecimal secInv = entry.getValue().stream().map(PortfolioDataHelper.ComputedHolding::investedValue).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
                BigDecimal secCur = entry.getValue().stream().map(PortfolioDataHelper.ComputedHolding::currentValue).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
                BigDecimal secPnl = secCur.subtract(secInv).setScale(2, RoundingMode.HALF_UP);
                BigDecimal secRet = dataHelper.computePercent(secPnl, secInv);
                BigDecimal secAvg = secShares > 0 ? secInv.divide(BigDecimal.valueOf(secShares), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
                BigDecimal secCurPrice = secShares > 0 ? secCur.divide(BigDecimal.valueOf(secShares), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
                BigDecimal secPct = totalSharesOwned > 0 ? BigDecimal.valueOf(secShares).multiply(new BigDecimal("100")).divide(BigDecimal.valueOf(totalSharesOwned), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

                rows.add(PortfolioCompanyOwnershipRowDto.builder()
                        .companyName(entry.getKey())
                        .companyCode("SECTOR")
                        .totalSharesOwned(secShares).formattedTotalSharesOwned(dataHelper.formatCount(secShares))
                        .ownershipPercentage(secPct).formattedOwnershipPercentage(secPct.toPlainString() + "%")
                        .averagePrice(secAvg).formattedAveragePrice(dataHelper.formatInr(secAvg))
                        .totalInvestment(secInv).formattedTotalInvestment(dataHelper.formatInr(secInv))
                        .currentPrice(secCurPrice).formattedCurrentPrice(dataHelper.formatInr(secCurPrice))
                        .currentValue(secCur).formattedCurrentValue(dataHelper.formatInr(secCur))
                        .unrealizedPnl(secPnl).formattedUnrealizedPnl(dataHelper.formatSignedInr(secPnl))
                        .unrealizedPnlPercentage(secRet).formattedUnrealizedPnlPercentage(dataHelper.formatSignedPercent(secRet))
                        .isProfit(secPnl.compareTo(BigDecimal.ZERO) >= 0)
                        .build());
            }
        } else if ("Share Class-wise".equalsIgnoreCase(activeTab)) {
            if (totalAvailable > 0) {
                BigDecimal p = availCur.subtract(availInv).setScale(2, RoundingMode.HALF_UP);
                BigDecimal rp = dataHelper.computePercent(p, availInv);
                BigDecimal pct = totalSharesOwned > 0 ? BigDecimal.valueOf(totalAvailable).multiply(new BigDecimal("100")).divide(BigDecimal.valueOf(totalSharesOwned), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
                rows.add(PortfolioCompanyOwnershipRowDto.builder()
                        .companyName("Free / Available Equity Shares").companyCode("FREE")
                        .totalSharesOwned(totalAvailable).formattedTotalSharesOwned(dataHelper.formatCount(totalAvailable))
                        .ownershipPercentage(pct).formattedOwnershipPercentage(pct.toPlainString() + "%")
                        .averagePrice(availInv.divide(BigDecimal.valueOf(totalAvailable), 2, RoundingMode.HALF_UP))
                        .formattedAveragePrice(dataHelper.formatInr(availInv.divide(BigDecimal.valueOf(totalAvailable), 2, RoundingMode.HALF_UP)))
                        .totalInvestment(availInv.setScale(2, RoundingMode.HALF_UP)).formattedTotalInvestment(dataHelper.formatInr(availInv))
                        .currentPrice(availCur.divide(BigDecimal.valueOf(totalAvailable), 2, RoundingMode.HALF_UP))
                        .formattedCurrentPrice(dataHelper.formatInr(availCur.divide(BigDecimal.valueOf(totalAvailable), 2, RoundingMode.HALF_UP)))
                        .currentValue(availCur.setScale(2, RoundingMode.HALF_UP)).formattedCurrentValue(dataHelper.formatInr(availCur))
                        .unrealizedPnl(p).formattedUnrealizedPnl(dataHelper.formatSignedInr(p))
                        .unrealizedPnlPercentage(rp).formattedUnrealizedPnlPercentage(dataHelper.formatSignedPercent(rp))
                        .isProfit(p.compareTo(BigDecimal.ZERO) >= 0)
                        .build());
            }
            if (totalLocked > 0) {
                BigDecimal p = lockCur.subtract(lockInv).setScale(2, RoundingMode.HALF_UP);
                BigDecimal rp = dataHelper.computePercent(p, lockInv);
                BigDecimal pct = totalSharesOwned > 0 ? BigDecimal.valueOf(totalLocked).multiply(new BigDecimal("100")).divide(BigDecimal.valueOf(totalSharesOwned), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
                rows.add(PortfolioCompanyOwnershipRowDto.builder()
                        .companyName("Escrow Locked Equity Shares").companyCode("LOCKED")
                        .totalSharesOwned(totalLocked).formattedTotalSharesOwned(dataHelper.formatCount(totalLocked))
                        .ownershipPercentage(pct).formattedOwnershipPercentage(pct.toPlainString() + "%")
                        .averagePrice(lockInv.divide(BigDecimal.valueOf(totalLocked), 2, RoundingMode.HALF_UP))
                        .formattedAveragePrice(dataHelper.formatInr(lockInv.divide(BigDecimal.valueOf(totalLocked), 2, RoundingMode.HALF_UP)))
                        .totalInvestment(lockInv.setScale(2, RoundingMode.HALF_UP)).formattedTotalInvestment(dataHelper.formatInr(lockInv))
                        .currentPrice(lockCur.divide(BigDecimal.valueOf(totalLocked), 2, RoundingMode.HALF_UP))
                        .formattedCurrentPrice(dataHelper.formatInr(lockCur.divide(BigDecimal.valueOf(totalLocked), 2, RoundingMode.HALF_UP)))
                        .currentValue(lockCur.setScale(2, RoundingMode.HALF_UP)).formattedCurrentValue(dataHelper.formatInr(lockCur))
                        .unrealizedPnl(p).formattedUnrealizedPnl(dataHelper.formatSignedInr(p))
                        .unrealizedPnlPercentage(rp).formattedUnrealizedPnlPercentage(dataHelper.formatSignedPercent(rp))
                        .isProfit(p.compareTo(BigDecimal.ZERO) >= 0)
                        .build());
            }
        }

        PortfolioOwnershipSummaryDetailDto statementSummary = PortfolioOwnershipSummaryDetailDto.builder()
                .memberName(dataHelper.resolveUserFullName(user))
                .memberId("ATG" + userId)
                .statementDate(date)
                .totalCompanies(summary.holdings().size())
                .totalSharesOwned(totalSharesOwned)
                .formattedTotalSharesOwned(dataHelper.formatCount(totalSharesOwned))
                .totalInvestment(totalInvestment)
                .formattedTotalInvestment(dataHelper.formatInr(totalInvestment))
                .currentMarketValue(currentValue)
                .formattedCurrentMarketValue(dataHelper.formatInr(currentValue))
                .totalOwnership(totalOwnershipPct)
                .formattedTotalOwnership(totalOwnershipPct.toPlainString() + "%")
                .averagePrice(weightedAvgPrice)
                .formattedAveragePrice(dataHelper.formatInr(weightedAvgPrice))
                .unrealizedPnl(unrealizedPnl)
                .formattedUnrealizedPnl(dataHelper.formatSignedInr(unrealizedPnl))
                .unrealizedPnlPercentage(unrealizedPct)
                .formattedUnrealizedPnlPercentage(dataHelper.formatSignedPercent(unrealizedPct))
                .build();

        List<PortfolioTrendPointDto> ownershipTrend = dataHelper.buildPerformanceSeries(
                rawHoldings, currentValue, totalInvestment, summary.prevTotalValue(), "6M", now
        ).stream().map(snap -> {
            BigDecimal ratioPct = currentValue.compareTo(BigDecimal.ZERO) > 0
                    ? totalOwnershipPct.multiply(snap.value()).divide(currentValue, 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            return PortfolioTrendPointDto.builder()
                    .period(snap.label())
                    .date(snap.dateIso())
                    .value(ratioPct)
                    .formattedValue(ratioPct.toPlainString() + "%")
                    .isCurrent(snap.isCurrent())
                    .build();
        }).toList();

        return PortfolioOwnershipResponseDto.builder()
                .asOfDate(date)
                .summaryKpis(summaryKpis)
                .activeTab(activeTab)
                .companies(rows)
                .statementSummary(statementSummary)
                .ownershipDistribution(ownershipDistribution)
                .ownershipTrend(ownershipTrend)
                .investmentVsCurrentValue(invVsCur)
                .build();
    }

    @Transactional(readOnly = true)
    public String buildAnalyticsCsv(Long userId, String period, String dateRange) {
        PortfolioAnalyticsResponseDto dto = getAnalytics(userId, "By Company", period, dateRange);
        StringBuilder sb = new StringBuilder();
        sb.append("Portfolio Analytics Report\n");
        sb.append("As Of,").append(escapeCsv(dto.getAsOfDate())).append('\n');
        sb.append("Date Range,").append(escapeCsv(dto.getDateRange())).append("\n\n");
        sb.append("Metric,Value,Change\n");
        for (PortfolioSummaryKpiDto kpi : dto.getSummaryKpis()) {
            String val = kpi.getFormattedAmount() != null ? kpi.getFormattedAmount() : (kpi.getFormattedCount() != null ? kpi.getFormattedCount() : "-");
            sb.append(escapeCsv(kpi.getTitle())).append(',')
                    .append(escapeCsv(val)).append(',')
                    .append(escapeCsv(kpi.getFormattedChangePercentage() != null ? kpi.getFormattedChangePercentage() : "-"))
                    .append('\n');
        }
        sb.append("\nCompany,Invested Amount,Current Value,Unrealized PnL,Return %\n");
        for (PortfolioAnalyticsPerformanceRowDto row : dto.getCompanyPerformance()) {
            sb.append(escapeCsv(row.getCompanyName())).append(',')
                    .append(row.getInvestedAmount() != null ? row.getInvestedAmount().toPlainString() : "0").append(',')
                    .append(row.getCurrentValue() != null ? row.getCurrentValue().toPlainString() : "0").append(',')
                    .append(row.getUnrealizedPnl() != null ? row.getUnrealizedPnl().toPlainString() : "0").append(',')
                    .append(escapeCsv(row.getFormattedReturnPercentage())).append('\n');
        }
        return sb.toString();
    }

    @Transactional(readOnly = true)
    public String buildPnlReportCsv(Long userId, String tab, String company, String sector, String dateRange) {
        PortfolioPnlResponseDto dto = getPnlReport(userId, tab, company, sector, dateRange);
        StringBuilder sb = new StringBuilder();
        sb.append("Company / Group,Invested Amount,Current Value,Realized PnL,Unrealized PnL,Total PnL,Return\n");
        for (PortfolioCompanyPnlRowDto row : dto.getCompanyPnl()) {
            sb.append(escapeCsv(row.getCompanyName())).append(',')
                    .append(row.getInvestedAmount() != null ? row.getInvestedAmount().toPlainString() : "0").append(',')
                    .append(row.getCurrentValue() != null ? row.getCurrentValue().toPlainString() : "0").append(',')
                    .append(row.getRealizedPnl() != null ? row.getRealizedPnl().toPlainString() : "0").append(',')
                    .append(row.getUnrealizedPnl() != null ? row.getUnrealizedPnl().toPlainString() : "0").append(',')
                    .append(row.getTotalPnl() != null ? row.getTotalPnl().toPlainString() : "0").append(',')
                    .append(escapeCsv(row.getFormattedReturnPercentage())).append('\n');
        }
        return sb.toString();
    }

    @Transactional(readOnly = true)
    public String buildTradeReceiptCsv(Long userId, Long tradeId) {
        PortfolioTradeDetailDto detail = getTradeDetail(tradeId, null, userId);
        return "Field,Value\n" +
                "Trade No," + escapeCsv(detail.getTradeNo()) + "\n" +
                "Company," + escapeCsv(detail.getCompanyName()) + "\n" +
                "Symbol," + escapeCsv(detail.getSymbol()) + "\n" +
                "Trade Type," + escapeCsv(detail.getTradeType()) + "\n" +
                "Quantity," + detail.getQuantity() + "\n" +
                "Price Per Share," + detail.getPricePerShare().toPlainString() + "\n" +
                "Total Value," + detail.getTotalValue().toPlainString() + "\n" +
                "Order Date," + escapeCsv(detail.getOrderDateTime()) + "\n" +
                "Trade Date," + escapeCsv(detail.getTradeDateTime()) + "\n" +
                "Settlement Date," + escapeCsv(detail.getSettlementDate()) + "\n" +
                "Settlement Ref," + escapeCsv(detail.getSettlementRef()) + "\n" +
                "Counterparty," + escapeCsv(detail.getCounterparty()) + "\n" +
                "Status," + escapeCsv(detail.getStatus()) + "\n";
    }

    @Transactional(readOnly = true)
    public String buildTradeHistoryCsv(Long userId, String search, String company, String tradeType, String status, String startDate, String endDate) {
        PortfolioTradesResponseDto dto = getTradeHistory(userId, search, company, tradeType, status, startDate, endDate, null, 1, 500);
        StringBuilder sb = new StringBuilder();
        sb.append("Trade No,Date & Time,Company,Symbol,Type,Quantity,Price,Trade Value,Status,Counterparty\n");
        for (PortfolioTradeItemDto row : dto.getTrades()) {
            sb.append(escapeCsv(row.getTradeNo())).append(',')
                    .append(escapeCsv(row.getTradeDateTime() != null ? row.getTradeDateTime().replace('\n', ' ') : "-")).append(',')
                    .append(escapeCsv(row.getCompanyName())).append(',')
                    .append(escapeCsv(row.getSymbol())).append(',')
                    .append(escapeCsv(row.getTradeType())).append(',')
                    .append(row.getQuantity() != null ? row.getQuantity() : 0).append(',')
                    .append(row.getPrice() != null ? row.getPrice().toPlainString() : "0").append(',')
                    .append(row.getTradeValue() != null ? row.getTradeValue().toPlainString() : "0").append(',')
                    .append(escapeCsv(row.getStatus())).append(',')
                    .append(escapeCsv(row.getCounterparty())).append('\n');
        }
        return sb.toString();
    }

    private String escapeCsv(String val) {
        if (val == null) return "";
        String cleaned = val.replace("\"", "\"\"");
        if (cleaned.contains(",") || cleaned.contains("\n") || cleaned.contains("\"")) {
            return "\"" + cleaned + "\"";
        }
        return cleaned;
    }

    // =========================================================================
    // 9. PORTFOLIO TRANSACTION STATEMENT (Image 4)
    // =========================================================================
    @Transactional(readOnly = true)
    public PortfolioTransactionResponseDto getTransactionStatement(Long userId, String search, String company, String transactionType, String status, String dateRange, int page, int size) {
        String activeDateRange = (dateRange != null && !dateRange.isBlank()) ? dateRange : "01 Jan 2025 - 18 Sep 2025";

        List<PortfolioSummaryKpiDto> summaryKpis = List.of(
                PortfolioSummaryKpiDto.builder().id("total_transactions").title("Total Transactions").count(128L).formattedCount("128").comparisonLabel(null).icon("repeat").build(),
                PortfolioSummaryKpiDto.builder().id("total_credits").title("Total Credits (₹)").amount(new BigDecimal("3240000.00")).formattedAmount(formatInr(new BigDecimal("3240000.00"))).icon("trending-up").build(),
                PortfolioSummaryKpiDto.builder().id("total_debits").title("Total Debits (₹)").amount(new BigDecimal("1850000.00")).formattedAmount(formatInr(new BigDecimal("1850000.00"))).icon("trending-down").build(),
                PortfolioSummaryKpiDto.builder().id("net_amount").title("Net Amount (₹)").amount(new BigDecimal("1390000.00")).formattedAmount(formatInr(new BigDecimal("1390000.00"))).icon("wallet").build(),
                PortfolioSummaryKpiDto.builder().id("pending_settlements").title("Pending Settlements").count(3L).formattedCount("3").comparisonLabel("₹ 2,35,000").icon("clock").build()
        );

        List<PortfolioTransactionItemDto> allTxns = List.of(
                PortfolioTransactionItemDto.builder().id(1L).txnId("TXN250918004").dateAndTime("18 Sep 2025\n11:28 AM").companyId(1L).companyName("Atirath Holdings").transactionType("Buy").sharesQuantity(1000L).formattedSharesQuantity("1,000").price(new BigDecimal("230.00")).formattedPrice("230.00").amount(new BigDecimal("230000.00")).formattedAmount("2,30,000").paymentMode("Bank Transfer").status("Completed").remarks("-").build(),
                PortfolioTransactionItemDto.builder().id(2L).txnId("TXN250917021").dateAndTime("17 Sep 2025\n04:15 PM").companyId(6L).companyName("CS Interio").transactionType("Sell").sharesQuantity(500L).formattedSharesQuantity("500").price(new BigDecimal("145.00")).formattedPrice("145.00").amount(new BigDecimal("72500.00")).formattedAmount("72,500").paymentMode("Escrow").status("Completed").remarks("-").build(),
                PortfolioTransactionItemDto.builder().id(3L).txnId("TXN250915033").dateAndTime("15 Sep 2025\n12:10 PM").companyId(4L).companyName("Tri Energy Battery").transactionType("Buy").sharesQuantity(600L).formattedSharesQuantity("600").price(new BigDecimal("225.00")).formattedPrice("225.00").amount(new BigDecimal("135000.00")).formattedAmount("1,35,000").paymentMode("UPI").status("Completed").remarks("-").build(),
                PortfolioTransactionItemDto.builder().id(4L).txnId("TXN250914018").dateAndTime("14 Sep 2025\n03:45 PM").companyId(5L).companyName("Royal One Heating").transactionType("Sell").sharesQuantity(400L).formattedSharesQuantity("400").price(new BigDecimal("235.00")).formattedPrice("235.00").amount(new BigDecimal("94000.00")).formattedAmount("94,000").paymentMode("Escrow").status("Completed").remarks("-").build(),
                PortfolioTransactionItemDto.builder().id(5L).txnId("TXN250912011").dateAndTime("12 Sep 2025\n11:20 AM").companyId(3L).companyName("Farm Vantara").transactionType("Dividend").sharesQuantity(null).formattedSharesQuantity("-").price(null).formattedPrice("-").amount(new BigDecimal("12500.00")).formattedAmount("12,500").paymentMode("Bank Transfer").status("Completed").remarks("Q1 2025").build(),
                PortfolioTransactionItemDto.builder().id(6L).txnId("TXN250910026").dateAndTime("10 Sep 2025\n02:18 PM").companyId(2L).companyName("Atirath Technologies").transactionType("Buy").sharesQuantity(800L).formattedSharesQuantity("800").price(new BigDecimal("180.00")).formattedPrice("180.00").amount(new BigDecimal("144000.00")).formattedAmount("1,44,000").paymentMode("Bank Transfer").status("Completed").remarks("-").build(),
                PortfolioTransactionItemDto.builder().id(7L).txnId("TXN250908014").dateAndTime("08 Sep 2025\n10:30 AM").companyId(6L).companyName("CS Interio").transactionType("Corporate Action").sharesQuantity(null).formattedSharesQuantity("-").price(null).formattedPrice("-").amount(null).formattedAmount("-").paymentMode("-").status("Completed").remarks("Bonus Shares (1:1)").build(),
                PortfolioTransactionItemDto.builder().id(8L).txnId("TXN250905017").dateAndTime("05 Sep 2025\n04:05 PM").companyId(1L).companyName("Atirath Holdings").transactionType("Sell").sharesQuantity(300L).formattedSharesQuantity("300").price(new BigDecimal("240.00")).formattedPrice("240.00").amount(new BigDecimal("72000.00")).formattedAmount("72,000").paymentMode("UPI").status("Completed").remarks("-").build(),
                PortfolioTransactionItemDto.builder().id(9L).txnId("TXN250902009").dateAndTime("02 Sep 2025\n01:22 PM").companyId(4L).companyName("Tri Energy Battery").transactionType("Buy").sharesQuantity(500L).formattedSharesQuantity("500").price(new BigDecimal("210.00")).formattedPrice("210.00").amount(new BigDecimal("105000.00")).formattedAmount("1,05,000").paymentMode("Bank Transfer").status("Pending").remarks("Settlement in progress").build(),
                PortfolioTransactionItemDto.builder().id(10L).txnId("TXN250830012").dateAndTime("30 Aug 2025\n11:16 AM").companyId(3L).companyName("Farm Vantara").transactionType("Sell").sharesQuantity(750L).formattedSharesQuantity("750").price(new BigDecimal("140.00")).formattedPrice("140.00").amount(new BigDecimal("105000.00")).formattedAmount("1,05,000").paymentMode("Escrow").status("Completed").remarks("-").build()
        );

        List<PortfolioTransactionItemDto> filtered = allTxns.stream()
                .filter(t -> {
                    if (search != null && !search.isBlank()) {
                        String s = search.toLowerCase();
                        boolean matchId = t.getTxnId().toLowerCase().contains(s);
                        boolean matchName = t.getCompanyName() != null && t.getCompanyName().toLowerCase().contains(s);
                        boolean matchRem = t.getRemarks() != null && t.getRemarks().toLowerCase().contains(s);
                        if (!matchId && !matchName && !matchRem) return false;
                    }
                    if (company != null && !company.isBlank() && !"ALL".equalsIgnoreCase(company)) {
                        if (!company.equalsIgnoreCase(t.getCompanyName())) return false;
                    }
                    if (transactionType != null && !transactionType.isBlank() && !"ALL".equalsIgnoreCase(transactionType)) {
                        if (!transactionType.equalsIgnoreCase(t.getTransactionType())) return false;
                    }
                    if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
                        if (!status.equalsIgnoreCase(t.getStatus())) return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());

        List<PortfolioDistributionItemDto> txnTypeBreakdown = List.of(
                PortfolioDistributionItemDto.builder().name("Buy").count(42L).percentage(new BigDecimal("32.8")).formattedPercentage("32.8%").color("#10B981").build(),
                PortfolioDistributionItemDto.builder().name("Sell").count(38L).percentage(new BigDecimal("29.7")).formattedPercentage("29.7%").color("#EF4444").build(),
                PortfolioDistributionItemDto.builder().name("Dividend").count(12L).percentage(new BigDecimal("9.4")).formattedPercentage("9.4%").color("#F59E0B").build(),
                PortfolioDistributionItemDto.builder().name("Corporate Action").count(8L).percentage(new BigDecimal("6.3")).formattedPercentage("6.3%").color("#8B5CF6").build(),
                PortfolioDistributionItemDto.builder().name("Fees").count(6L).percentage(new BigDecimal("4.7")).formattedPercentage("4.7%").color("#06B6D4").build(),
                PortfolioDistributionItemDto.builder().name("Others").count(22L).percentage(new BigDecimal("17.1")).formattedPercentage("17.1%").color("#64748B").build()
        );

        List<PortfolioDistributionItemDto> paymentModeDist = List.of(
                PortfolioDistributionItemDto.builder().name("Bank Transfer").count(48L).percentage(new BigDecimal("37.5")).formattedPercentage("37.5%").color("#3B82F6").build(),
                PortfolioDistributionItemDto.builder().name("Escrow").count(32L).percentage(new BigDecimal("25.0")).formattedPercentage("25.0%").color("#10B981").build(),
                PortfolioDistributionItemDto.builder().name("UPI").count(28L).percentage(new BigDecimal("21.9")).formattedPercentage("21.9%").color("#8B5CF6").build(),
                PortfolioDistributionItemDto.builder().name("Wallet").count(12L).percentage(new BigDecimal("9.4")).formattedPercentage("9.4%").color("#F59E0B").build(),
                PortfolioDistributionItemDto.builder().name("Others").count(8L).percentage(new BigDecimal("6.2")).formattedPercentage("6.2%").color("#64748B").build()
        );

        int pageSize = size > 0 ? size : 10;
        int currentPage = Math.max(page, 1);
        int totalItems = 128;
        int totalPages = 13;

        return PortfolioTransactionResponseDto.builder()
                .dateRange(activeDateRange)
                .summaryKpis(summaryKpis)
                .transactions(filtered)
                .transactionTypeBreakdown(txnTypeBreakdown)
                .paymentModeDistribution(paymentModeDist)
                .totalTransactionsCount(totalItems)
                .currentPage(currentPage)
                .totalPages(totalPages)
                .pageSize(pageSize)
                .build();
    }

    private UserAccount resolveUserAccount(Long userId) {
        if (userId != null) {
            return userAccountRepository.findById(userId)
                    .orElse(null);
        }
        return userAccountRepository.findByEmailIgnoreCase("sairam.v@atirathgroup.com")
                .or(() -> userAccountRepository.findAll().stream().findFirst())
                .orElse(null);
    }

    private String formatInr(BigDecimal amount) {
        if (amount == null) return "₹ 0";
        long val = amount.longValue();
        if (val == 0) return "₹ 0";
        
        StringBuilder sb = new StringBuilder();
        String str = String.valueOf(Math.abs(val));
        int len = str.length();
        
        if (len <= 3) {
            sb.append(str);
        } else {
            String lastThree = str.substring(len - 3);
            String rest = str.substring(0, len - 3);
            
            StringBuilder restFormatted = new StringBuilder();
            int count = 0;
            for (int i = rest.length() - 1; i >= 0; i--) {
                restFormatted.insert(0, rest.charAt(i));
                count++;
                if (count == 2 && i != 0) {
                    restFormatted.insert(0, ",");
                    count = 0;
                }
            }
            sb.append(restFormatted).append(",").append(lastThree);
        }
        return (val < 0 ? "-₹ " : "₹ ") + sb.toString();
    }
}
