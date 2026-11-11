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
import java.time.OffsetDateTime;
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
    private final ShareTransferRepository shareTransferRepository;
    private final PortfolioDataHelper dataHelper;

    private static final DateTimeFormatter DISPLAY_DT_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

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
    // 2. PORTFOLIO HOLDINGS (Image 2)
    // =========================================================================
    @Transactional(readOnly = true)
    public PortfolioHoldingsResponseDto getHoldings(Long userId, String search, String company, String holdingType, String status, int page, int size) {
        List<PortfolioSummaryKpiDto> summaryKpis = List.of(
                PortfolioSummaryKpiDto.builder()
                        .id("total_holdings_value")
                        .title("Total Holdings Value")
                        .amount(new BigDecimal("2480000.00"))
                        .formattedAmount(formatInr(new BigDecimal("2480000.00")))
                        .changePercentage(new BigDecimal("12.6"))
                        .formattedChangePercentage("↑ 12.6%")
                        .isPositive(true)
                        .comparisonLabel("vs. previous month")
                        .icon("file-text")
                        .build(),
                PortfolioSummaryKpiDto.builder()
                        .id("total_shares_held")
                        .title("Total Shares Held")
                        .count(10250L)
                        .formattedCount("10,250")
                        .comparisonLabel("Across 6 companies")
                        .icon("pie-chart")
                        .build(),
                PortfolioSummaryKpiDto.builder()
                        .id("invested_amount")
                        .title("Invested Amount")
                        .amount(new BigDecimal("1850000.00"))
                        .formattedAmount(formatInr(new BigDecimal("1850000.00")))
                        .changePercentage(new BigDecimal("8.3"))
                        .formattedChangePercentage("↑ 8.3%")
                        .isPositive(true)
                        .comparisonLabel("vs. previous month")
                        .icon("coins")
                        .build(),
                PortfolioSummaryKpiDto.builder()
                        .id("unrealized_pnl")
                        .title("Unrealized P&L")
                        .amount(new BigDecimal("630000.00"))
                        .formattedAmount(formatInr(new BigDecimal("630000.00")))
                        .changePercentage(new BigDecimal("25.4"))
                        .formattedChangePercentage("↑ 25.4%")
                        .isPositive(true)
                        .comparisonLabel("vs. previous month")
                        .icon("trending-up")
                        .build(),
                PortfolioSummaryKpiDto.builder()
                        .id("locked_shares")
                        .title("Locked Shares")
                        .count(1750L)
                        .formattedCount("1,750")
                        .comparisonLabel("17.1% of total shares")
                        .icon("lock")
                        .build(),
                PortfolioSummaryKpiDto.builder()
                        .id("available_shares")
                        .title("Available Shares")
                        .count(8500L)
                        .formattedCount("8,500")
                        .comparisonLabel("82.9% of total shares")
                        .icon("shield-check")
                        .build()
        );

        List<PortfolioHoldingItemDto> allHoldings = getHoldingsList();

        // Filter by search / company
        List<PortfolioHoldingItemDto> filtered = allHoldings.stream()
                .filter(h -> {
                    if (search != null && !search.isBlank()) {
                        String s = search.toLowerCase();
                        boolean matchName = h.getCompanyName() != null && h.getCompanyName().toLowerCase().contains(s);
                        boolean matchCode = h.getCompanyCode() != null && h.getCompanyCode().toLowerCase().contains(s);
                        boolean matchSymbol = h.getSymbol() != null && h.getSymbol().toLowerCase().contains(s);
                        if (!matchName && !matchCode && !matchSymbol) return false;
                    }
                    if (company != null && !company.isBlank() && !"ALL".equalsIgnoreCase(company)) {
                        boolean match = h.getCompanyName().equalsIgnoreCase(company) ||
                                        h.getCompanyCode().equalsIgnoreCase(company) ||
                                        (h.getSymbol() != null && h.getSymbol().equalsIgnoreCase(company));
                        if (!match) return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());

        List<PortfolioQuickInsightDto> quickInsights = List.of(
                PortfolioQuickInsightDto.builder()
                        .title("Best Performing Holding")
                        .value("Atirath Holdings")
                        .change("+53.8%")
                        .isPositive(true)
                        .icon("trending-up")
                        .build(),
                PortfolioQuickInsightDto.builder()
                        .title("Worst Performing Holding")
                        .value("CS Interio")
                        .change("-9.4%")
                        .isPositive(false)
                        .icon("trending-down")
                        .build(),
                PortfolioQuickInsightDto.builder()
                        .title("Most Allocated")
                        .value("Atirath Holdings")
                        .change("35.5%")
                        .isPositive(true)
                        .icon("pie-chart")
                        .build(),
                PortfolioQuickInsightDto.builder()
                        .title("Highest Locked Shares")
                        .value("Atirath Holdings")
                        .change("500 shares")
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

        return PortfolioHoldingsResponseDto.builder()
                .asOfDate("18 Sep 2025, 11:28 AM")
                .summaryKpis(summaryKpis)
                .holdings(filtered)
                .allocation(getAllocationData())
                .quickInsights(quickInsights)
                .insightSummary("Your portfolio value has increased by 12.6% compared to last month. Consider reviewing your holdings for rebalancing opportunities.")
                .totalCount(filtered.size())
                .currentPage(1)
                .totalPages(1)
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
    @Transactional(readOnly = true)
    public PortfolioPositionsResponseDto getPositions(Long userId, String tab, String company, String search) {
        String activeTab = (tab != null && !tab.isBlank()) ? tab.toUpperCase() : "ALL";

        List<PortfolioSummaryKpiDto> summaryKpis = List.of(
                PortfolioSummaryKpiDto.builder()
                        .id("total_open_positions")
                        .title("Total Open Positions")
                        .count(5L)
                        .formattedCount("5")
                        .comparisonLabel("Across 4 companies")
                        .icon("file-text")
                        .build(),
                PortfolioSummaryKpiDto.builder()
                        .id("total_invested_value")
                        .title("Total Invested Value")
                        .amount(new BigDecimal("785000.00"))
                        .formattedAmount(formatInr(new BigDecimal("785000.00")))
                        .comparisonLabel(null)
                        .icon("coins")
                        .build(),
                PortfolioSummaryKpiDto.builder()
                        .id("current_market_value")
                        .title("Current Market Value")
                        .amount(new BigDecimal("867500.00"))
                        .formattedAmount(formatInr(new BigDecimal("867500.00")))
                        .changePercentage(new BigDecimal("10.5"))
                        .formattedChangePercentage("↑ 10.5%")
                        .isPositive(true)
                        .icon("trending-up")
                        .build(),
                PortfolioSummaryKpiDto.builder()
                        .id("unrealized_pnl")
                        .title("Unrealized P&L")
                        .amount(new BigDecimal("82500.00"))
                        .formattedAmount(formatInr(new BigDecimal("82500.00")))
                        .changePercentage(new BigDecimal("10.5"))
                        .formattedChangePercentage("↑ 10.5%")
                        .isPositive(true)
                        .icon("bar-chart")
                        .build(),
                PortfolioSummaryKpiDto.builder()
                        .id("days_pnl")
                        .title("Day's P&L")
                        .amount(new BigDecimal("23750.00"))
                        .formattedAmount(formatInr(new BigDecimal("23750.00")))
                        .changePercentage(new BigDecimal("2.8"))
                        .formattedChangePercentage("↑ 2.8%")
                        .isPositive(true)
                        .icon("percent")
                        .build()
        );

        List<PortfolioPositionItemDto> allPositions = List.of(
                PortfolioPositionItemDto.builder()
                        .positionId(1L)
                        .companyId(1L)
                        .companyName("Atirath Holdings India Limited")
                        .companyCode("ATG")
                        .symbol("ATG-H")
                        .positionType("Long")
                        .quantity(1000L)
                        .formattedQuantity("1,000")
                        .averagePrice(new BigDecimal("150.00"))
                        .formattedAveragePrice("₹ 150.00")
                        .currentPrice(new BigDecimal("230.00"))
                        .formattedCurrentPrice("₹ 230.00")
                        .investedValue(new BigDecimal("150000.00"))
                        .formattedInvestedValue("₹ 1,50,000")
                        .currentValue(new BigDecimal("230000.00"))
                        .formattedCurrentValue("₹ 2,30,000")
                        .unrealizedPnl(new BigDecimal("80000.00"))
                        .formattedUnrealizedPnl("+₹ 80,000")
                        .unrealizedPnlPercentage(new BigDecimal("53.3"))
                        .formattedUnrealizedPnlPercentage("+53.3%")
                        .isProfit(true)
                        .build(),
                PortfolioPositionItemDto.builder()
                        .positionId(2L)
                        .companyId(3L)
                        .companyName("Farm Vantara Private Limited")
                        .companyCode("FVPL")
                        .symbol("ATG-FV")
                        .positionType("Long")
                        .quantity(750L)
                        .formattedQuantity("750")
                        .averagePrice(new BigDecimal("100.00"))
                        .formattedAveragePrice("₹ 100.00")
                        .currentPrice(new BigDecimal("135.00"))
                        .formattedCurrentPrice("₹ 135.00")
                        .investedValue(new BigDecimal("75000.00"))
                        .formattedInvestedValue("₹ 75,000")
                        .currentValue(new BigDecimal("101250.00"))
                        .formattedCurrentValue("₹ 1,01,250")
                        .unrealizedPnl(new BigDecimal("26250.00"))
                        .formattedUnrealizedPnl("+₹ 26,250")
                        .unrealizedPnlPercentage(new BigDecimal("35.0"))
                        .formattedUnrealizedPnlPercentage("+35.0%")
                        .isProfit(true)
                        .build(),
                PortfolioPositionItemDto.builder()
                        .positionId(3L)
                        .companyId(4L)
                        .companyName("Tri Energy Battery Private Limited")
                        .companyCode("TEB")
                        .symbol("ATG-TEB")
                        .positionType("Long")
                        .quantity(600L)
                        .formattedQuantity("600")
                        .averagePrice(new BigDecimal("200.00"))
                        .formattedAveragePrice("₹ 200.00")
                        .currentPrice(new BigDecimal("225.00"))
                        .formattedCurrentPrice("₹ 225.00")
                        .investedValue(new BigDecimal("120000.00"))
                        .formattedInvestedValue("₹ 1,20,000")
                        .currentValue(new BigDecimal("135000.00"))
                        .formattedCurrentValue("₹ 1,35,000")
                        .unrealizedPnl(new BigDecimal("15000.00"))
                        .formattedUnrealizedPnl("+₹ 15,000")
                        .unrealizedPnlPercentage(new BigDecimal("12.5"))
                        .formattedUnrealizedPnlPercentage("+12.5%")
                        .isProfit(true)
                        .build(),
                PortfolioPositionItemDto.builder()
                        .positionId(4L)
                        .companyId(6L)
                        .companyName("CS Interio Private Limited")
                        .companyCode("CSI")
                        .symbol("ATG-CSI")
                        .positionType("Long")
                        .quantity(500L)
                        .formattedQuantity("500")
                        .averagePrice(new BigDecimal("160.00"))
                        .formattedAveragePrice("₹ 160.00")
                        .currentPrice(new BigDecimal("145.00"))
                        .formattedCurrentPrice("₹ 145.00")
                        .investedValue(new BigDecimal("80000.00"))
                        .formattedInvestedValue("₹ 80,000")
                        .currentValue(new BigDecimal("72500.00"))
                        .formattedCurrentValue("₹ 72,500")
                        .unrealizedPnl(new BigDecimal("-7500.00"))
                        .formattedUnrealizedPnl("-₹ 7,500")
                        .unrealizedPnlPercentage(new BigDecimal("-9.4"))
                        .formattedUnrealizedPnlPercentage("-9.4%")
                        .isProfit(false)
                        .build(),
                PortfolioPositionItemDto.builder()
                        .positionId(5L)
                        .companyId(5L)
                        .companyName("Royal One Heating Equipments Pvt Ltd")
                        .companyCode("ROHE")
                        .symbol("ATG-ROHE")
                        .positionType("Long")
                        .quantity(400L)
                        .formattedQuantity("400")
                        .averagePrice(new BigDecimal("180.00"))
                        .formattedAveragePrice("₹ 180.00")
                        .currentPrice(new BigDecimal("235.00"))
                        .formattedCurrentPrice("₹ 235.00")
                        .investedValue(new BigDecimal("72000.00"))
                        .formattedInvestedValue("₹ 72,000")
                        .currentValue(new BigDecimal("94000.00"))
                        .formattedCurrentValue("₹ 94,000")
                        .unrealizedPnl(new BigDecimal("22000.00"))
                        .formattedUnrealizedPnl("+₹ 22,000")
                        .unrealizedPnlPercentage(new BigDecimal("30.6"))
                        .formattedUnrealizedPnlPercentage("+30.6%")
                        .isProfit(true)
                        .build()
        );

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
                        boolean match = p.getCompanyName().equalsIgnoreCase(company) ||
                                        p.getCompanyCode().equalsIgnoreCase(company) ||
                                        (p.getSymbol() != null && p.getSymbol().equalsIgnoreCase(company));
                        if (!match) return false;
                    }
                    if (search != null && !search.isBlank()) {
                        String s = search.toLowerCase();
                        boolean match = p.getCompanyName().toLowerCase().contains(s) ||
                                        p.getCompanyCode().toLowerCase().contains(s);
                        if (!match) return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());

        List<PortfolioAllocationSliceDto> valueDistribution = List.of(
                PortfolioAllocationSliceDto.builder().companyName("Atirath Holdings").percentage(new BigDecimal("26.5")).formattedPercentage("26.5%").color("#1E40AF").build(),
                PortfolioAllocationSliceDto.builder().companyName("Farm Vantara").percentage(new BigDecimal("11.7")).formattedPercentage("11.7%").color("#F59E0B").build(),
                PortfolioAllocationSliceDto.builder().companyName("Tri Energy Battery").percentage(new BigDecimal("15.6")).formattedPercentage("15.6%").color("#8B5CF6").build(),
                PortfolioAllocationSliceDto.builder().companyName("CS Interio").percentage(new BigDecimal("8.4")).formattedPercentage("8.4%").color("#10B981").build(),
                PortfolioAllocationSliceDto.builder().companyName("Royal One Heating").percentage(new BigDecimal("10.8")).formattedPercentage("10.8%").color("#EF4444").build()
        );

        List<PortfolioQuickInsightDto> positionInsights = List.of(
                PortfolioQuickInsightDto.builder().title("Largest Position").value("Atirath Holdings (26.5%)").icon("pie-chart").build(),
                PortfolioQuickInsightDto.builder().title("Highest Gain").value("Atirath Holdings (+53.3%)").icon("trending-up").isPositive(true).build(),
                PortfolioQuickInsightDto.builder().title("Highest Loss").value("CS Interio (-9.4%)").icon("trending-down").isPositive(false).build(),
                PortfolioQuickInsightDto.builder().title("Most Recent Position").value("Royal One Heating (Opened 15 Sep 2025)").icon("clock").build()
        );

        List<PortfolioDayMovementDto> daysMovement = List.of(
                PortfolioDayMovementDto.builder().companyName("Atirath Holdings").changeAmount(new BigDecimal("5.00")).formattedChangeAmount("+5.00").changePercentage(new BigDecimal("2.2")).formattedChangePercentage("+2.2%").isPositive(true).build(),
                PortfolioDayMovementDto.builder().companyName("Farm Vantara").changeAmount(new BigDecimal("3.50")).formattedChangeAmount("+3.50").changePercentage(new BigDecimal("2.7")).formattedChangePercentage("+2.7%").isPositive(true).build(),
                PortfolioDayMovementDto.builder().companyName("Tri Energy Battery").changeAmount(new BigDecimal("2.50")).formattedChangeAmount("+2.50").changePercentage(new BigDecimal("1.1")).formattedChangePercentage("+1.1%").isPositive(true).build(),
                PortfolioDayMovementDto.builder().companyName("CS Interio").changeAmount(new BigDecimal("-5.00")).formattedChangeAmount("-5.00").changePercentage(new BigDecimal("-3.3")).formattedChangePercentage("-3.3%").isPositive(false).build(),
                PortfolioDayMovementDto.builder().companyName("Royal One Heating").changeAmount(new BigDecimal("8.00")).formattedChangeAmount("+8.00").changePercentage(new BigDecimal("3.5")).formattedChangePercentage("+3.5%").isPositive(true).build()
        );

        return PortfolioPositionsResponseDto.builder()
                .summaryKpis(summaryKpis)
                .activeTab(activeTab)
                .positions(filtered)
                .valueDistribution(valueDistribution)
                .positionInsights(positionInsights)
                .valueTrend(getPositionTrendData())
                .daysMovement(daysMovement)
                .totalPositionsCount(5)
                .build();
    }

    // =========================================================================
    // 5. PORTFOLIO TRADE HISTORY (Image 5)
    // =========================================================================
    @Transactional(readOnly = true)
    public PortfolioTradesResponseDto getTradeHistory(Long userId, String search, String company, String tradeType, String status, String startDate, String endDate, Long selectedTradeId, int page, int size) {
        List<PortfolioSummaryKpiDto> summaryKpis = List.of(
                PortfolioSummaryKpiDto.builder()
                        .id("total_trades")
                        .title("Total Trades")
                        .count(62L)
                        .formattedCount("62")
                        .comparisonLabel("All time")
                        .icon("repeat")
                        .build(),
                PortfolioSummaryKpiDto.builder()
                        .id("total_buy_value")
                        .title("Total Buy Value")
                        .amount(new BigDecimal("3240000.00"))
                        .formattedAmount(formatInr(new BigDecimal("3240000.00")))
                        .comparisonLabel(null)
                        .icon("dollar-sign")
                        .build(),
                PortfolioSummaryKpiDto.builder()
                        .id("total_sell_value")
                        .title("Total Sell Value")
                        .amount(new BigDecimal("2875000.00"))
                        .formattedAmount(formatInr(new BigDecimal("2875000.00")))
                        .comparisonLabel(null)
                        .icon("dollar-sign")
                        .build(),
                PortfolioSummaryKpiDto.builder()
                        .id("realized_pnl")
                        .title("Realized P&L")
                        .amount(new BigDecimal("562300.00"))
                        .formattedAmount(formatInr(new BigDecimal("562300.00")))
                        .changePercentage(new BigDecimal("24.3"))
                        .formattedChangePercentage("↑ 24.3%")
                        .isPositive(true)
                        .icon("trending-up")
                        .build(),
                PortfolioSummaryKpiDto.builder()
                        .id("profitable_trades")
                        .title("Profitable Trades")
                        .formattedCount("78%")
                        .comparisonLabel("48 out of 62")
                        .icon("check-circle")
                        .build()
        );

        List<PortfolioTradeItemDto> allTrades = getTradesList();

        List<PortfolioTradeItemDto> filtered = allTrades.stream()
                .filter(t -> {
                    if (search != null && !search.isBlank()) {
                        String s = search.toLowerCase();
                        boolean matchName = t.getCompanyName().toLowerCase().contains(s);
                        boolean matchNo = t.getTradeNo().toLowerCase().contains(s);
                        boolean matchParty = t.getCounterparty() != null && t.getCounterparty().toLowerCase().contains(s);
                        if (!matchName && !matchNo && !matchParty) return false;
                    }
                    if (company != null && !company.isBlank() && !"ALL".equalsIgnoreCase(company)) {
                        boolean match = t.getCompanyName().equalsIgnoreCase(company) ||
                                        t.getCompanyCode().equalsIgnoreCase(company) ||
                                        (t.getSymbol() != null && t.getSymbol().equalsIgnoreCase(company));
                        if (!match) return false;
                    }
                    if (tradeType != null && !tradeType.isBlank() && !"ALL".equalsIgnoreCase(tradeType)) {
                        if (!t.getTradeType().equalsIgnoreCase(tradeType)) return false;
                    }
                    if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
                        if (!t.getStatus().equalsIgnoreCase(status)) return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());

        PortfolioTradeDetailDto selectedDetail = getTradeDetail(selectedTradeId != null ? selectedTradeId : 1L, null, userId);

        int pageSize = size > 0 ? size : 10;
        int currentPage = Math.max(page, 1);
        int totalItems = filtered.size();
        int totalPages = Math.max((int) Math.ceil((double) totalItems / pageSize), 1);
        int fromIndex = (currentPage - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, totalItems);

        List<PortfolioTradeItemDto> pagedTrades = fromIndex < totalItems ? filtered.subList(fromIndex, toIndex) : Collections.emptyList();

        return PortfolioTradesResponseDto.builder()
                .summaryKpis(summaryKpis)
                .trades(pagedTrades)
                .selectedTradeDetail(selectedDetail)
                .totalTradesCount(62)
                .currentPage(currentPage)
                .totalPages(totalPages)
                .pageSize(pageSize)
                .build();
    }

    @Transactional(readOnly = true)
    public PortfolioTradeDetailDto getTradeDetail(Long tradeId, String tradeNo, Long userId) {
        List<PortfolioTimelineStepDto> timeline = List.of(
                PortfolioTimelineStepDto.builder().stepNumber(1).stepName("Order Placed").timestamp("18 Sep 2025, 10:15 AM").status("COMPLETED").isCurrent(false).build(),
                PortfolioTimelineStepDto.builder().stepNumber(2).stepName("Matched with Seller").timestamp("18 Sep 2025, 10:20 AM").status("COMPLETED").isCurrent(false).build(),
                PortfolioTimelineStepDto.builder().stepNumber(3).stepName("Payment to Escrow").timestamp("18 Sep 2025, 10:43 AM").status("COMPLETED").isCurrent(false).build(),
                PortfolioTimelineStepDto.builder().stepNumber(4).stepName("Shares Locked").timestamp("18 Sep 2025, 10:51 AM").status("COMPLETED").isCurrent(false).build(),
                PortfolioTimelineStepDto.builder().stepNumber(5).stepName("Shares Transferred").timestamp("18 Sep 2025, 11:05 AM").status("COMPLETED").isCurrent(false).build(),
                PortfolioTimelineStepDto.builder().stepNumber(6).stepName("Settlement Completed").timestamp("18 Sep 2025, 11:28 AM").status("COMPLETED").isCurrent(true).build()
        );

        if (tradeNo != null && !tradeNo.isBlank()) {
            Optional<PortfolioTradeItemDto> match = getTradesList().stream().filter(t -> t.getTradeNo().equalsIgnoreCase(tradeNo)).findFirst();
            if (match.isPresent()) {
                PortfolioTradeItemDto m = match.get();
                return PortfolioTradeDetailDto.builder()
                        .tradeId(m.getTradeId())
                        .tradeNo(m.getTradeNo())
                        .companyName(m.getCompanyName())
                        .symbol(m.getSymbol())
                        .tradeType(m.getTradeType())
                        .quantity(m.getQuantity())
                        .formattedQuantity(m.getFormattedQuantity())
                        .pricePerShare(m.getPrice())
                        .formattedPricePerShare(m.getFormattedPrice())
                        .totalValue(m.getTradeValue())
                        .formattedTotalValue(m.getFormattedTradeValue())
                        .orderDateTime("18 Sep 2025, 10:15 AM")
                        .tradeDateTime(m.getTradeDateTime())
                        .settlementDate("18 Sep 2025, 11:28 AM")
                        .status(m.getStatus())
                        .counterparty(m.getCounterparty() + " (ATG10056)")
                        .settlementRef("SET" + m.getTradeNo().replace("TRD", ""))
                        .timeline(timeline)
                        .build();
            }
        }

        return PortfolioTradeDetailDto.builder()
                .tradeId(1L)
                .tradeNo("TRD250918004")
                .companyName("Atirath Holdings India Limited")
                .symbol("ATG-H")
                .tradeType("Buy")
                .quantity(1000L)
                .formattedQuantity("1,000")
                .pricePerShare(new BigDecimal("230.00"))
                .formattedPricePerShare("₹ 230.00")
                .totalValue(new BigDecimal("230000.00"))
                .formattedTotalValue("₹ 2,30,000")
                .orderDateTime("18 Sep 2025, 10:15 AM")
                .tradeDateTime("18 Sep 2025, 11:05 AM")
                .settlementDate("18 Sep 2025, 11:28 AM")
                .status("Settled")
                .counterparty("Rakesh Mehta (ATG10056)")
                .settlementRef("SET250918001")
                .timeline(timeline)
                .build();
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

    private List<PortfolioTradeItemDto> getTradesList() {
        return List.of(
                PortfolioTradeItemDto.builder().tradeId(1L).tradeNo("TRD250918004").tradeDateTime("18 Sep 2025\n11:05 AM").companyId(1L).companyName("Atirath Holdings India Limited").companyCode("ATG").symbol("ATG-H").tradeType("Buy").quantity(1000L).formattedQuantity("1,000").price(new BigDecimal("230.00")).formattedPrice("230.00").tradeValue(new BigDecimal("230000.00")).formattedTradeValue("2,30,000").status("Settled").counterparty("Rakesh Mehta").build(),
                PortfolioTradeItemDto.builder().tradeId(2L).tradeNo("TRD250917006").tradeDateTime("17 Sep 2025\n04:32 PM").companyId(6L).companyName("CS Interio Private Limited").companyCode("CSI").symbol("ATG-CSI").tradeType("Sell").quantity(500L).formattedQuantity("500").price(new BigDecimal("145.00")).formattedPrice("145.00").tradeValue(new BigDecimal("72500.00")).formattedTradeValue("72,500").status("Settled").counterparty("Anil Kumar").build(),
                PortfolioTradeItemDto.builder().tradeId(3L).tradeNo("TRD250917005").tradeDateTime("17 Sep 2025\n11:20 AM").companyId(3L).companyName("Farm Vantara Private Limited").companyCode("FVPL").symbol("ATG-FV").tradeType("Buy").quantity(750L).formattedQuantity("750").price(new BigDecimal("135.00")).formattedPrice("135.00").tradeValue(new BigDecimal("101250.00")).formattedTradeValue("1,01,250").status("Settled").counterparty("Priya Sharma").build(),
                PortfolioTradeItemDto.builder().tradeId(4L).tradeNo("TRD250916004").tradeDateTime("16 Sep 2025\n02:45 PM").companyId(2L).companyName("Atirath Technologies Private Limited").companyCode("ATT").symbol("ATG-T").tradeType("Sell").quantity(1200L).formattedQuantity("1,200").price(new BigDecimal("180.00")).formattedPrice("180.00").tradeValue(new BigDecimal("216000.00")).formattedTradeValue("2,16,000").status("Settled").counterparty("Kiran Reddy").build(),
                PortfolioTradeItemDto.builder().tradeId(5L).tradeNo("TRD250915003").tradeDateTime("15 Sep 2025\n12:10 PM").companyId(4L).companyName("Tri Energy Battery Private Limited").companyCode("TEB").symbol("ATG-TEB").tradeType("Buy").quantity(600L).formattedQuantity("600").price(new BigDecimal("225.00")).formattedPrice("225.00").tradeValue(new BigDecimal("135000.00")).formattedTradeValue("1,35,000").status("Settled").counterparty("Suresh Babu").build(),
                PortfolioTradeItemDto.builder().tradeId(6L).tradeNo("TRD250915002").tradeDateTime("15 Sep 2025\n11:35 AM").companyId(5L).companyName("Royal One Heating Equipments Pvt Ltd").companyCode("ROHE").symbol("ATG-ROHE").tradeType("Buy").quantity(400L).formattedQuantity("400").price(new BigDecimal("235.00")).formattedPrice("235.00").tradeValue(new BigDecimal("94000.00")).formattedTradeValue("94,000").status("Cancelled").counterparty("-").build(),
                PortfolioTradeItemDto.builder().tradeId(7L).tradeNo("TRD250914001").tradeDateTime("14 Sep 2025\n03:20 PM").companyId(1L).companyName("Atirath Holdings India Limited").companyCode("ATG").symbol("ATG-H").tradeType("Sell").quantity(800L).formattedQuantity("800").price(new BigDecimal("240.00")).formattedPrice("240.00").tradeValue(new BigDecimal("192000.00")).formattedTradeValue("1,92,000").status("Settled").counterparty("Neha Gupta").build(),
                PortfolioTradeItemDto.builder().tradeId(8L).tradeNo("TRD250913008").tradeDateTime("13 Sep 2025\n10:05 AM").companyId(6L).companyName("CS Interio Private Limited").companyCode("CSI").symbol("ATG-CSI").tradeType("Buy").quantity(1000L).formattedQuantity("1,000").price(new BigDecimal("160.00")).formattedPrice("160.00").tradeValue(new BigDecimal("160000.00")).formattedTradeValue("1,60,000").status("Settled").counterparty("Vikram Rao").build(),
                PortfolioTradeItemDto.builder().tradeId(9L).tradeNo("TRD250912007").tradeDateTime("12 Sep 2025\n04:18 PM").companyId(3L).companyName("Farm Vantara Private Limited").companyCode("FVPL").symbol("ATG-FV").tradeType("Sell").quantity(300L).formattedQuantity("300").price(new BigDecimal("140.00")).formattedPrice("140.00").tradeValue(new BigDecimal("42000.00")).formattedTradeValue("42,000").status("Settled").counterparty("Divya S").build(),
                PortfolioTradeItemDto.builder().tradeId(10L).tradeNo("TRD250911006").tradeDateTime("11 Sep 2025\n01:25 PM").companyId(2L).companyName("Atirath Technologies Private Limited").companyCode("ATT").symbol("ATG-T").tradeType("Buy").quantity(500L).formattedQuantity("500").price(new BigDecimal("175.00")).formattedPrice("175.00").tradeValue(new BigDecimal("87500.00")).formattedTradeValue("87,500").status("Settled").counterparty("Manoj Verma").build()
        );
    }

    // =========================================================================
    // 6. PORTFOLIO ANALYTICS (Image 1)
    // =========================================================================
    @Transactional(readOnly = true)
    public PortfolioAnalyticsResponseDto getAnalytics(Long userId, String allocationTab, String trendPeriod, String dateRange) {
        String activeAllocTab = (allocationTab != null && !allocationTab.isBlank()) ? allocationTab : "By Company";
        String activePeriod = (trendPeriod != null && !trendPeriod.isBlank()) ? trendPeriod.toUpperCase() : "6M";
        String activeDateRange = (dateRange != null && !dateRange.isBlank()) ? dateRange : "01 Jan 2025 - 18 Sep 2025";

        List<PortfolioSummaryKpiDto> summaryKpis = List.of(
                PortfolioSummaryKpiDto.builder().id("total_portfolio_value").title("Total Portfolio Value").amount(new BigDecimal("2480000.00")).formattedAmount(formatInr(new BigDecimal("2480000.00"))).changePercentage(new BigDecimal("12.6")).formattedChangePercentage("↑ 12.6%").isPositive(true).comparisonLabel("vs. previous period").icon("wallet").build(),
                PortfolioSummaryKpiDto.builder().id("total_invested_amount").title("Total Invested Amount").amount(new BigDecimal("1850000.00")).formattedAmount(formatInr(new BigDecimal("1850000.00"))).changePercentage(new BigDecimal("8.3")).formattedChangePercentage("↑ 8.3%").isPositive(true).comparisonLabel("vs. previous period").icon("coins").build(),
                PortfolioSummaryKpiDto.builder().id("unrealized_pnl").title("Unrealized P&L").amount(new BigDecimal("630000.00")).formattedAmount(formatInr(new BigDecimal("630000.00"))).changePercentage(new BigDecimal("25.4")).formattedChangePercentage("↑ 25.4%").isPositive(true).comparisonLabel("vs. previous period").icon("trending-up").build(),
                PortfolioSummaryKpiDto.builder().id("realized_pnl").title("Realized P&L").amount(new BigDecimal("120000.00")).formattedAmount(formatInr(new BigDecimal("120000.00"))).changePercentage(new BigDecimal("10.0")).formattedChangePercentage("↑ 10.0%").isPositive(true).comparisonLabel("in this period").icon("dollar-sign").build(),
                PortfolioSummaryKpiDto.builder().id("total_return").title("Total Return").formattedCount("40.5%").comparisonLabel("(Realized + Unrealized)").icon("percent").build()
        );

        List<PortfolioAllocationSliceDto> byCompany = getAllocationData();
        List<PortfolioAllocationSliceDto> bySector = List.of(
                PortfolioAllocationSliceDto.builder().companyName("Diversified").percentage(new BigDecimal("35.5")).formattedPercentage("35.5%").color("#1E40AF").build(),
                PortfolioAllocationSliceDto.builder().companyName("Information Technology").percentage(new BigDecimal("20.6")).formattedPercentage("20.6%").color("#3B82F6").build(),
                PortfolioAllocationSliceDto.builder().companyName("Agriculture / Dairy").percentage(new BigDecimal("15.7")).formattedPercentage("15.7%").color("#F59E0B").build(),
                PortfolioAllocationSliceDto.builder().companyName("Clean Energy & Battery").percentage(new BigDecimal("10.9")).formattedPercentage("10.9%").color("#8B5CF6").build(),
                PortfolioAllocationSliceDto.builder().companyName("Industrial & Heating").percentage(new BigDecimal("8.5")).formattedPercentage("8.5%").color("#EF4444").build(),
                PortfolioAllocationSliceDto.builder().companyName("Interior & Infra").percentage(new BigDecimal("5.8")).formattedPercentage("5.8%").color("#10B981").build(),
                PortfolioAllocationSliceDto.builder().companyName("Others").percentage(new BigDecimal("3.0")).formattedPercentage("3.0%").color("#64748B").build()
        );
        List<PortfolioAllocationSliceDto> byType = List.of(
                PortfolioAllocationSliceDto.builder().companyName("Direct Equity").percentage(new BigDecimal("75.0")).formattedPercentage("75.0%").color("#1E40AF").build(),
                PortfolioAllocationSliceDto.builder().companyName("Promoted Holdings").percentage(new BigDecimal("15.0")).formattedPercentage("15.0%").color("#3B82F6").build(),
                PortfolioAllocationSliceDto.builder().companyName("ESOP / Strategic").percentage(new BigDecimal("10.0")).formattedPercentage("10.0%").color("#F59E0B").build()
        );

        List<PortfolioTrendPointDto> pvTrend = getTrendData(activePeriod);
        List<PortfolioTrendPointDto> invTrend = List.of(
                PortfolioTrendPointDto.builder().period("Mar 2025").date("2025-03-31").value(new BigDecimal("900000.00")).formattedValue("₹ 9.0L").build(),
                PortfolioTrendPointDto.builder().period("Apr 2025").date("2025-04-30").value(new BigDecimal("1100000.00")).formattedValue("₹ 11.0L").build(),
                PortfolioTrendPointDto.builder().period("May 2025").date("2025-05-31").value(new BigDecimal("1300000.00")).formattedValue("₹ 13.0L").build(),
                PortfolioTrendPointDto.builder().period("Jun 2025").date("2025-06-30").value(new BigDecimal("1450000.00")).formattedValue("₹ 14.5L").build(),
                PortfolioTrendPointDto.builder().period("Jul 2025").date("2025-07-31").value(new BigDecimal("1600000.00")).formattedValue("₹ 16.0L").build(),
                PortfolioTrendPointDto.builder().period("Aug 2025").date("2025-08-31").value(new BigDecimal("1750000.00")).formattedValue("₹ 17.5L").build(),
                PortfolioTrendPointDto.builder().period("18 Sep 2025").date("2025-09-18").value(new BigDecimal("1850000.00")).formattedValue("₹ 18,50,000").isCurrent(true).build()
        );

        PortfolioReturnAnalysisDto returnAnalysis = PortfolioReturnAnalysisDto.builder()
                .totalReturn(new BigDecimal("40.5")).formattedTotalReturn("40.5%")
                .annualizedReturn(new BigDecimal("18.6")).formattedAnnualizedReturn("18.6%")
                .priceAppreciation(new BigDecimal("12.4")).formattedPriceAppreciation("12.4%")
                .dividendReturn(new BigDecimal("6.8")).formattedDividendReturn("6.8%")
                .build();

        List<PortfolioAnalyticsPerformanceRowDto> companyPerf = List.of(
                PortfolioAnalyticsPerformanceRowDto.builder().companyId(1L).companyName("Atirath Holdings").symbol("ATG-H").investedAmount(new BigDecimal("600000.00")).formattedInvestedAmount("6,00,000").currentValue(new BigDecimal("805000.00")).formattedCurrentValue("8,05,000").unrealizedPnl(new BigDecimal("205000.00")).formattedUnrealizedPnl("+ 2,05,000").returnPercentage(new BigDecimal("34.2")).formattedReturnPercentage("+34.2%").isPositive(true).build(),
                PortfolioAnalyticsPerformanceRowDto.builder().companyId(2L).companyName("Atirath Technologies").symbol("ATG-T").investedAmount(new BigDecimal("350000.00")).formattedInvestedAmount("3,50,000").currentValue(new BigDecimal("360000.00")).formattedCurrentValue("3,60,000").unrealizedPnl(new BigDecimal("10000.00")).formattedUnrealizedPnl("+ 10,000").returnPercentage(new BigDecimal("2.9")).formattedReturnPercentage("+2.9%").isPositive(true).build(),
                PortfolioAnalyticsPerformanceRowDto.builder().companyId(3L).companyName("Farm Vantara").symbol("ATG-FV").investedAmount(new BigDecimal("280000.00")).formattedInvestedAmount("2,80,000").currentValue(new BigDecimal("390000.00")).formattedCurrentValue("3,90,000").unrealizedPnl(new BigDecimal("110000.00")).formattedUnrealizedPnl("+ 1,10,000").returnPercentage(new BigDecimal("39.3")).formattedReturnPercentage("+39.3%").isPositive(true).build(),
                PortfolioAnalyticsPerformanceRowDto.builder().companyId(4L).companyName("Tri Energy Battery").symbol("ATG-TEB").investedAmount(new BigDecimal("220000.00")).formattedInvestedAmount("2,20,000").currentValue(new BigDecimal("270000.00")).formattedCurrentValue("2,70,000").unrealizedPnl(new BigDecimal("50000.00")).formattedUnrealizedPnl("+ 50,000").returnPercentage(new BigDecimal("22.7")).formattedReturnPercentage("+22.7%").isPositive(true).build(),
                PortfolioAnalyticsPerformanceRowDto.builder().companyId(5L).companyName("Royal One Heating").symbol("ATG-ROHE").investedAmount(new BigDecimal("170000.00")).formattedInvestedAmount("1,70,000").currentValue(new BigDecimal("211500.00")).formattedCurrentValue("2,11,500").unrealizedPnl(new BigDecimal("41500.00")).formattedUnrealizedPnl("+ 41,500").returnPercentage(new BigDecimal("24.4")).formattedReturnPercentage("+24.4%").isPositive(true).build(),
                PortfolioAnalyticsPerformanceRowDto.builder().companyId(6L).companyName("CS Interio").symbol("ATG-CSI").investedAmount(new BigDecimal("110000.00")).formattedInvestedAmount("1,10,000").currentValue(new BigDecimal("87000.00")).formattedCurrentValue("87,000").unrealizedPnl(new BigDecimal("-23000.00")).formattedUnrealizedPnl("- 23,000").returnPercentage(new BigDecimal("-20.9")).formattedReturnPercentage("-20.9%").isPositive(false).build()
        );

        List<PortfolioAllocationSliceDto> assetTypeAlloc = List.of(
                PortfolioAllocationSliceDto.builder().companyName("Equity Shares").percentage(new BigDecimal("92.5")).formattedPercentage("92.5%").color("#1E40AF").build(),
                PortfolioAllocationSliceDto.builder().companyName("ESOP / RSU").percentage(new BigDecimal("4.2")).formattedPercentage("4.2%").color("#F59E0B").build(),
                PortfolioAllocationSliceDto.builder().companyName("Locked Shares").percentage(new BigDecimal("1.7")).formattedPercentage("1.7%").color("#10B981").build(),
                PortfolioAllocationSliceDto.builder().companyName("Pending Transfer").percentage(new BigDecimal("1.6")).formattedPercentage("1.6%").color("#8B5CF6").build()
        );

        List<String> keyInsights = List.of(
                "Your portfolio value has increased by 12.6% compared to the last 6 months.",
                "Atirath Holdings is your best performing investment (+34.2%).",
                "Diversify further to reduce concentration risk (Top 2 companies = 56.1%).",
                "CS Interio is underperforming (-20.9%). Review your position.",
                "Your total return (40.5%) is higher than the platform average (28.3%)."
        );

        List<PortfolioPriceMoverDto> gainers = List.of(
                PortfolioPriceMoverDto.builder().companyName("Farm Vantara").symbol("ATG-FV").currentPrice(new BigDecimal("135.00")).formattedCurrentPrice("135.00").changeAmount(new BigDecimal("35.00")).formattedChangeAmount("+ 35.00").changePercentage(new BigDecimal("35.0")).formattedChangePercentage("+35.0%").isGainer(true).build(),
                PortfolioPriceMoverDto.builder().companyName("Atirath Holdings").symbol("ATG-H").currentPrice(new BigDecimal("230.00")).formattedCurrentPrice("230.00").changeAmount(new BigDecimal("60.00")).formattedChangeAmount("+ 60.00").changePercentage(new BigDecimal("35.3")).formattedChangePercentage("+35.3%").isGainer(true).build(),
                PortfolioPriceMoverDto.builder().companyName("Tri Energy Battery").symbol("ATG-TEB").currentPrice(new BigDecimal("225.00")).formattedCurrentPrice("225.00").changeAmount(new BigDecimal("25.00")).formattedChangeAmount("+ 25.00").changePercentage(new BigDecimal("12.5")).formattedChangePercentage("+12.5%").isGainer(true).build()
        );

        List<PortfolioPriceMoverDto> losers = List.of(
                PortfolioPriceMoverDto.builder().companyName("CS Interio").symbol("ATG-CSI").currentPrice(new BigDecimal("145.00")).formattedCurrentPrice("145.00").changeAmount(new BigDecimal("-15.00")).formattedChangeAmount("- 15.00").changePercentage(new BigDecimal("-9.4")).formattedChangePercentage("-9.4%").isGainer(false).build(),
                PortfolioPriceMoverDto.builder().companyName("Atirath Technologies").symbol("ATG-T").currentPrice(new BigDecimal("180.00")).formattedCurrentPrice("180.00").changeAmount(new BigDecimal("-10.00")).formattedChangeAmount("- 10.00").changePercentage(new BigDecimal("-5.3")).formattedChangePercentage("-5.3%").isGainer(false).build(),
                PortfolioPriceMoverDto.builder().companyName("Royal One Heating").symbol("ATG-ROHE").currentPrice(new BigDecimal("235.00")).formattedCurrentPrice("235.00").changeAmount(new BigDecimal("-5.00")).formattedChangeAmount("- 5.00").changePercentage(new BigDecimal("-2.1")).formattedChangePercentage("-2.1%").isGainer(false).build()
        );

        return PortfolioAnalyticsResponseDto.builder()
                .asOfDate("18 Sep 2025, 11:28 AM")
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
    // 7. PORTFOLIO P&L REPORT (Image 2)
    // =========================================================================
    @Transactional(readOnly = true)
    public PortfolioPnlResponseDto getPnlReport(Long userId, String tab, String company, String sector, String dateRange) {
        String activeTab = (tab != null && !tab.isBlank()) ? tab : "Company-wise P&L";
        String activeDateRange = (dateRange != null && !dateRange.isBlank()) ? dateRange : "01 Jan 2025 - 18 Sep 2025";

        List<PortfolioSummaryKpiDto> summaryKpis = List.of(
                PortfolioSummaryKpiDto.builder().id("total_investment").title("Total Investment").amount(new BigDecimal("1850000.00")).formattedAmount(formatInr(new BigDecimal("1850000.00"))).icon("coins").build(),
                PortfolioSummaryKpiDto.builder().id("current_value").title("Current Value").amount(new BigDecimal("2480000.00")).formattedAmount(formatInr(new BigDecimal("2480000.00"))).changePercentage(new BigDecimal("34.1")).formattedChangePercentage("↑ 34.1%").isPositive(true).icon("trending-up").build(),
                PortfolioSummaryKpiDto.builder().id("total_pnl").title("Total P&L").amount(new BigDecimal("630000.00")).formattedAmount(formatInr(new BigDecimal("630000.00"))).changePercentage(new BigDecimal("34.1")).formattedChangePercentage("↑ 34.1%").isPositive(true).icon("bar-chart").build(),
                PortfolioSummaryKpiDto.builder().id("realized_pnl").title("Realized P&L").amount(new BigDecimal("120000.00")).formattedAmount(formatInr(new BigDecimal("120000.00"))).changePercentage(new BigDecimal("10.0")).formattedChangePercentage("↑ 10.0%").isPositive(true).icon("dollar-sign").build(),
                PortfolioSummaryKpiDto.builder().id("unrealized_pnl").title("Unrealized P&L").amount(new BigDecimal("510000.00")).formattedAmount(formatInr(new BigDecimal("510000.00"))).changePercentage(new BigDecimal("43.6")).formattedChangePercentage("↑ 43.6%").isPositive(true).icon("trending-up").build()
        );

        List<PortfolioPnlTrendPointDto> pnlTrend = List.of(
                PortfolioPnlTrendPointDto.builder().month("Jan 2025").realizedPnl(new BigDecimal("15000")).formattedRealizedPnl("15,000").unrealizedPnl(new BigDecimal("45000")).formattedUnrealizedPnl("45,000").totalPnl(new BigDecimal("60000")).formattedTotalPnl("60,000").build(),
                PortfolioPnlTrendPointDto.builder().month("Feb 2025").realizedPnl(new BigDecimal("25000")).formattedRealizedPnl("25,000").unrealizedPnl(new BigDecimal("85000")).formattedUnrealizedPnl("85,000").totalPnl(new BigDecimal("110000")).formattedTotalPnl("1,10,000").build(),
                PortfolioPnlTrendPointDto.builder().month("Mar 2025").realizedPnl(new BigDecimal("40000")).formattedRealizedPnl("40,000").unrealizedPnl(new BigDecimal("140000")).formattedUnrealizedPnl("1,40,000").totalPnl(new BigDecimal("180000")).formattedTotalPnl("1,80,000").build(),
                PortfolioPnlTrendPointDto.builder().month("Apr 2025").realizedPnl(new BigDecimal("55000")).formattedRealizedPnl("55,000").unrealizedPnl(new BigDecimal("205000")).formattedUnrealizedPnl("2,05,000").totalPnl(new BigDecimal("260000")).formattedTotalPnl("2,60,000").build(),
                PortfolioPnlTrendPointDto.builder().month("May 2025").realizedPnl(new BigDecimal("70000")).formattedRealizedPnl("70,000").unrealizedPnl(new BigDecimal("280000")).formattedUnrealizedPnl("2,80,000").totalPnl(new BigDecimal("350000")).formattedTotalPnl("3,50,000").build(),
                PortfolioPnlTrendPointDto.builder().month("Jun 2025").realizedPnl(new BigDecimal("85000")).formattedRealizedPnl("85,000").unrealizedPnl(new BigDecimal("345000")).formattedUnrealizedPnl("3,45,000").totalPnl(new BigDecimal("430000")).formattedTotalPnl("4,30,000").build(),
                PortfolioPnlTrendPointDto.builder().month("Jul 2025").realizedPnl(new BigDecimal("95000")).formattedRealizedPnl("95,000").unrealizedPnl(new BigDecimal("415000")).formattedUnrealizedPnl("4,15,000").totalPnl(new BigDecimal("510000")).formattedTotalPnl("5,10,000").build(),
                PortfolioPnlTrendPointDto.builder().month("Aug 2025").realizedPnl(new BigDecimal("110000")).formattedRealizedPnl("1,10,000").unrealizedPnl(new BigDecimal("470000")).formattedUnrealizedPnl("4,70,000").totalPnl(new BigDecimal("580000")).formattedTotalPnl("5,80,000").build(),
                PortfolioPnlTrendPointDto.builder().month("Sep 2025").realizedPnl(new BigDecimal("120000")).formattedRealizedPnl("1,20,000").unrealizedPnl(new BigDecimal("510000")).formattedUnrealizedPnl("5,10,000").totalPnl(new BigDecimal("630000")).formattedTotalPnl("6,30,000").build()
        );

        PortfolioPnlSummaryDto pnlSummary = PortfolioPnlSummaryDto.builder()
                .totalInvestment(new BigDecimal("1850000.00")).formattedTotalInvestment("₹ 18,50,000")
                .currentValue(new BigDecimal("2480000.00")).formattedCurrentValue("₹ 24,80,000")
                .totalPnl(new BigDecimal("630000.00")).formattedTotalPnl("₹ 6,30,000")
                .totalReturn(new BigDecimal("34.1")).formattedTotalReturn("+34.1%")
                .realizedPnl(new BigDecimal("120000.00")).formattedRealizedPnl("₹ 1,20,000")
                .unrealizedPnl(new BigDecimal("510000.00")).formattedUnrealizedPnl("₹ 5,10,000")
                .realizedReturn(new BigDecimal("10.0")).formattedRealizedReturn("+10.0%")
                .unrealizedReturn(new BigDecimal("43.6")).formattedUnrealizedReturn("+43.6%")
                .build();

        List<PortfolioCompanyPnlRowDto> companyPnlList = List.of(
                PortfolioCompanyPnlRowDto.builder().companyId(1L).companyName("Atirath Holdings India Limited (ATG)").companyCode("ATG").investedAmount(new BigDecimal("600000.00")).formattedInvestedAmount("6,00,000").currentValue(new BigDecimal("805000.00")).formattedCurrentValue("8,05,000").realizedPnl(new BigDecimal("50000.00")).formattedRealizedPnl("50,000").unrealizedPnl(new BigDecimal("155000.00")).formattedUnrealizedPnl("1,55,000").totalPnl(new BigDecimal("205000.00")).formattedTotalPnl("2,05,000").returnPercentage(new BigDecimal("34.2")).formattedReturnPercentage("+34.2%").isProfit(true).build(),
                PortfolioCompanyPnlRowDto.builder().companyId(2L).companyName("Atirath Technologies Private Limited (ATT)").companyCode("ATT").investedAmount(new BigDecimal("350000.00")).formattedInvestedAmount("3,50,000").currentValue(new BigDecimal("360000.00")).formattedCurrentValue("3,60,000").realizedPnl(new BigDecimal("30000.00")).formattedRealizedPnl("30,000").unrealizedPnl(new BigDecimal("80000.00")).formattedUnrealizedPnl("80,000").totalPnl(new BigDecimal("110000.00")).formattedTotalPnl("1,10,000").returnPercentage(new BigDecimal("31.4")).formattedReturnPercentage("+31.4%").isProfit(true).build(),
                PortfolioCompanyPnlRowDto.builder().companyId(3L).companyName("Farm Vantara Private Limited (FVPL)").companyCode("FVPL").investedAmount(new BigDecimal("280000.00")).formattedInvestedAmount("2,80,000").currentValue(new BigDecimal("390000.00")).formattedCurrentValue("3,90,000").realizedPnl(new BigDecimal("25000.00")).formattedRealizedPnl("25,000").unrealizedPnl(new BigDecimal("85000.00")).formattedUnrealizedPnl("85,000").totalPnl(new BigDecimal("110000.00")).formattedTotalPnl("1,10,000").returnPercentage(new BigDecimal("39.3")).formattedReturnPercentage("+39.3%").isProfit(true).build(),
                PortfolioCompanyPnlRowDto.builder().companyId(4L).companyName("Tri Energy Battery Private Limited (TEB)").companyCode("TEB").investedAmount(new BigDecimal("220000.00")).formattedInvestedAmount("2,20,000").currentValue(new BigDecimal("270000.00")).formattedCurrentValue("2,70,000").realizedPnl(new BigDecimal("10000.00")).formattedRealizedPnl("10,000").unrealizedPnl(new BigDecimal("40000.00")).formattedUnrealizedPnl("40,000").totalPnl(new BigDecimal("50000.00")).formattedTotalPnl("50,000").returnPercentage(new BigDecimal("22.7")).formattedReturnPercentage("+22.7%").isProfit(true).build(),
                PortfolioCompanyPnlRowDto.builder().companyId(5L).companyName("Royal One Heating Equipments Pvt Ltd (ROHE)").companyCode("ROHE").investedAmount(new BigDecimal("170000.00")).formattedInvestedAmount("1,70,000").currentValue(new BigDecimal("211500.00")).formattedCurrentValue("2,11,500").realizedPnl(new BigDecimal("5000.00")).formattedRealizedPnl("5,000").unrealizedPnl(new BigDecimal("36500.00")).formattedUnrealizedPnl("36,500").totalPnl(new BigDecimal("41500.00")).formattedTotalPnl("41,500").returnPercentage(new BigDecimal("24.4")).formattedReturnPercentage("+24.4%").isProfit(true).build(),
                PortfolioCompanyPnlRowDto.builder().companyId(6L).companyName("CS Interio Private Limited (CSI)").companyCode("CSI").investedAmount(new BigDecimal("110000.00")).formattedInvestedAmount("1,10,000").currentValue(new BigDecimal("87000.00")).formattedCurrentValue("87,000").realizedPnl(BigDecimal.ZERO).formattedRealizedPnl("0").unrealizedPnl(new BigDecimal("-23000.00")).formattedUnrealizedPnl("-23,000").totalPnl(new BigDecimal("-23000.00")).formattedTotalPnl("-23,000").returnPercentage(new BigDecimal("-20.9")).formattedReturnPercentage("-20.9%").isProfit(false).build()
        );

        List<String> keyInsights = List.of(
                "Your total return is 34.1%. Higher than last period (28.3%).",
                "Atirath Holdings is your top performing investment (+34.2%).",
                "CS Interio is underperforming (-20.9%). Consider reviewing your position.",
                "60% of your gains are unrealized. Monitor market movements.",
                "Your portfolio is diversified across 6 companies and 5 sectors."
        );

        return PortfolioPnlResponseDto.builder()
                .dateRange(activeDateRange)
                .summaryKpis(summaryKpis)
                .pnlTrend(pnlTrend)
                .pnlSummary(pnlSummary)
                .activeTab(activeTab)
                .companyPnl(companyPnlList)
                .keyInsights(keyInsights)
                .totalCompaniesCount(6)
                .build();
    }

    // =========================================================================
    // 8. PORTFOLIO OWNERSHIP STATEMENT (Image 3)
    // =========================================================================
    @Transactional(readOnly = true)
    public PortfolioOwnershipResponseDto getOwnershipStatement(Long userId, String tab, String asOfDate) {
        String activeTab = (tab != null && !tab.isBlank()) ? tab : "Company-wise";
        String date = (asOfDate != null && !asOfDate.isBlank()) ? asOfDate : "18 Sep 2025";

        List<PortfolioSummaryKpiDto> summaryKpis = List.of(
                PortfolioSummaryKpiDto.builder().id("total_companies").title("Total Companies").count(6L).formattedCount("6").comparisonLabel("Group Companies").icon("layers").build(),
                PortfolioSummaryKpiDto.builder().id("total_shares_owned").title("Total Shares Owned").count(5450L).formattedCount("5,450").comparisonLabel("Shares").icon("pie-chart").build(),
                PortfolioSummaryKpiDto.builder().id("total_investment").title("Total Investment").amount(new BigDecimal("1850000.00")).formattedAmount(formatInr(new BigDecimal("1850000.00"))).icon("coins").build(),
                PortfolioSummaryKpiDto.builder().id("current_market_value").title("Current Market Value").amount(new BigDecimal("2480000.00")).formattedAmount(formatInr(new BigDecimal("2480000.00"))).changePercentage(new BigDecimal("34.1")).formattedChangePercentage("↑ 34.1%").isPositive(true).icon("trending-up").build(),
                PortfolioSummaryKpiDto.builder().id("total_ownership").title("Total Ownership").formattedCount("4.85%").comparisonLabel("Across Group").icon("percent").build()
        );

        List<PortfolioCompanyOwnershipRowDto> companies = List.of(
                PortfolioCompanyOwnershipRowDto.builder().companyId(1L).companyName("Atirath Holdings India Limited (ATG)").companyCode("ATG").totalSharesOwned(2000L).formattedTotalSharesOwned("2,000").ownershipPercentage(new BigDecimal("8.00")).formattedOwnershipPercentage("8.00%").averagePrice(new BigDecimal("230.00")).formattedAveragePrice("230.00").totalInvestment(new BigDecimal("460000.00")).formattedTotalInvestment("4,60,000").currentPrice(new BigDecimal("320.00")).formattedCurrentPrice("320.00").currentValue(new BigDecimal("640000.00")).formattedCurrentValue("6,40,000").unrealizedPnl(new BigDecimal("180000.00")).formattedUnrealizedPnl("+1,80,000").unrealizedPnlPercentage(new BigDecimal("39.1")).formattedUnrealizedPnlPercentage("+39.1%").isProfit(true).build(),
                PortfolioCompanyOwnershipRowDto.builder().companyId(2L).companyName("Atirath Technologies Private Limited (ATT)").companyCode("ATT").totalSharesOwned(1200L).formattedTotalSharesOwned("1,200").ownershipPercentage(new BigDecimal("6.00")).formattedOwnershipPercentage("6.00%").averagePrice(new BigDecimal("180.00")).formattedAveragePrice("180.00").totalInvestment(new BigDecimal("216000.00")).formattedTotalInvestment("2,16,000").currentPrice(new BigDecimal("240.00")).formattedCurrentPrice("240.00").currentValue(new BigDecimal("288000.00")).formattedCurrentValue("2,88,000").unrealizedPnl(new BigDecimal("72000.00")).formattedUnrealizedPnl("+72,000").unrealizedPnlPercentage(new BigDecimal("33.3")).formattedUnrealizedPnlPercentage("+33.3%").isProfit(true).build(),
                PortfolioCompanyOwnershipRowDto.builder().companyId(3L).companyName("Farm Vantara Private Limited (FVPL)").companyCode("FVPL").totalSharesOwned(750L).formattedTotalSharesOwned("750").ownershipPercentage(new BigDecimal("3.00")).formattedOwnershipPercentage("3.00%").averagePrice(new BigDecimal("135.00")).formattedAveragePrice("135.00").totalInvestment(new BigDecimal("101250.00")).formattedTotalInvestment("1,01,250").currentPrice(new BigDecimal("165.00")).formattedCurrentPrice("165.00").currentValue(new BigDecimal("123750.00")).formattedCurrentValue("1,23,750").unrealizedPnl(new BigDecimal("22500.00")).formattedUnrealizedPnl("+22,500").unrealizedPnlPercentage(new BigDecimal("22.2")).formattedUnrealizedPnlPercentage("+22.2%").isProfit(true).build(),
                PortfolioCompanyOwnershipRowDto.builder().companyId(4L).companyName("Tri Energy Battery Private Limited (TEB)").companyCode("TEB").totalSharesOwned(600L).formattedTotalSharesOwned("600").ownershipPercentage(new BigDecimal("2.40")).formattedOwnershipPercentage("2.40%").averagePrice(new BigDecimal("225.00")).formattedAveragePrice("225.00").totalInvestment(new BigDecimal("135000.00")).formattedTotalInvestment("1,35,000").currentPrice(new BigDecimal("250.00")).formattedCurrentPrice("250.00").currentValue(new BigDecimal("150000.00")).formattedCurrentValue("1,50,000").unrealizedPnl(new BigDecimal("15000.00")).formattedUnrealizedPnl("+15,000").unrealizedPnlPercentage(new BigDecimal("11.1")).formattedUnrealizedPnlPercentage("+11.1%").isProfit(true).build(),
                PortfolioCompanyOwnershipRowDto.builder().companyId(5L).companyName("Royal One Heating Equipments Pvt Ltd (ROHE)").companyCode("ROHE").totalSharesOwned(500L).formattedTotalSharesOwned("500").ownershipPercentage(new BigDecimal("2.00")).formattedOwnershipPercentage("2.00%").averagePrice(new BigDecimal("235.00")).formattedAveragePrice("235.00").totalInvestment(new BigDecimal("117500.00")).formattedTotalInvestment("1,17,500").currentPrice(new BigDecimal("210.00")).formattedCurrentPrice("210.00").currentValue(new BigDecimal("105000.00")).formattedCurrentValue("1,05,000").unrealizedPnl(new BigDecimal("-12500.00")).formattedUnrealizedPnl("-12,500").unrealizedPnlPercentage(new BigDecimal("-10.6")).formattedUnrealizedPnlPercentage("-10.6%").isProfit(false).build(),
                PortfolioCompanyOwnershipRowDto.builder().companyId(6L).companyName("CS Interio Private Limited (CSI)").companyCode("CSI").totalSharesOwned(400L).formattedTotalSharesOwned("400").ownershipPercentage(new BigDecimal("1.60")).formattedOwnershipPercentage("1.60%").averagePrice(new BigDecimal("160.00")).formattedAveragePrice("160.00").totalInvestment(new BigDecimal("64000.00")).formattedTotalInvestment("64,000").currentPrice(new BigDecimal("145.00")).formattedCurrentPrice("145.00").currentValue(new BigDecimal("58000.00")).formattedCurrentValue("58,000").unrealizedPnl(new BigDecimal("-6000.00")).formattedUnrealizedPnl("-6,000").unrealizedPnlPercentage(new BigDecimal("-9.4")).formattedUnrealizedPnlPercentage("-9.4%").isProfit(false).build()
        );

        PortfolioOwnershipSummaryDetailDto statementSummary = PortfolioOwnershipSummaryDetailDto.builder()
                .memberName("Sairam Vempati")
                .memberId("ATG10027")
                .statementDate(date)
                .totalCompanies(6)
                .totalSharesOwned(5450L)
                .formattedTotalSharesOwned("5,450")
                .totalInvestment(new BigDecimal("1850000.00"))
                .formattedTotalInvestment("₹ 18,50,000")
                .currentMarketValue(new BigDecimal("2480000.00"))
                .formattedCurrentMarketValue("₹ 24,80,000")
                .totalOwnership(new BigDecimal("4.85"))
                .formattedTotalOwnership("4.85%")
                .averagePrice(new BigDecimal("194.50"))
                .formattedAveragePrice("194.50")
                .unrealizedPnl(new BigDecimal("630000.00"))
                .formattedUnrealizedPnl("+6,30,000")
                .unrealizedPnlPercentage(new BigDecimal("34.1"))
                .formattedUnrealizedPnlPercentage("+34.1%")
                .build();

        List<PortfolioAllocationSliceDto> ownershipDistribution = List.of(
                PortfolioAllocationSliceDto.builder().companyName("Atirath Holdings").percentage(new BigDecimal("36.7")).formattedPercentage("36.7%").color("#1E40AF").build(),
                PortfolioAllocationSliceDto.builder().companyName("Atirath Technologies").percentage(new BigDecimal("22.0")).formattedPercentage("22.0%").color("#3B82F6").build(),
                PortfolioAllocationSliceDto.builder().companyName("Farm Vantara").percentage(new BigDecimal("13.8")).formattedPercentage("13.8%").color("#F59E0B").build(),
                PortfolioAllocationSliceDto.builder().companyName("Tri Energy Battery").percentage(new BigDecimal("11.0")).formattedPercentage("11.0%").color("#8B5CF6").build(),
                PortfolioAllocationSliceDto.builder().companyName("Royal One Heating").percentage(new BigDecimal("9.2")).formattedPercentage("9.2%").color("#EF4444").build(),
                PortfolioAllocationSliceDto.builder().companyName("CS Interio").percentage(new BigDecimal("7.3")).formattedPercentage("7.3%").color("#10B981").build()
        );

        List<PortfolioTrendPointDto> ownershipTrend = List.of(
                PortfolioTrendPointDto.builder().period("Mar 2025").date("2025-03-31").value(new BigDecimal("1.20")).formattedValue("1.20%").build(),
                PortfolioTrendPointDto.builder().period("Apr 2025").date("2025-04-30").value(new BigDecimal("2.10")).formattedValue("2.10%").build(),
                PortfolioTrendPointDto.builder().period("May 2025").date("2025-05-31").value(new BigDecimal("2.80")).formattedValue("2.80%").build(),
                PortfolioTrendPointDto.builder().period("Jun 2025").date("2025-06-30").value(new BigDecimal("3.40")).formattedValue("3.40%").build(),
                PortfolioTrendPointDto.builder().period("Jul 2025").date("2025-07-31").value(new BigDecimal("4.00")).formattedValue("4.00%").build(),
                PortfolioTrendPointDto.builder().period("Aug 2025").date("2025-08-31").value(new BigDecimal("4.50")).formattedValue("4.50%").build(),
                PortfolioTrendPointDto.builder().period("Sep 2025").date("2025-09-18").value(new BigDecimal("4.85")).formattedValue("4.85%").isCurrent(true).build()
        );

        List<PortfolioAnalyticsPerformanceRowDto> invVsCur = List.of(
                PortfolioAnalyticsPerformanceRowDto.builder().companyName("ATG").investedAmount(new BigDecimal("460000")).currentValue(new BigDecimal("640000")).build(),
                PortfolioAnalyticsPerformanceRowDto.builder().companyName("ATT").investedAmount(new BigDecimal("216000")).currentValue(new BigDecimal("288000")).build(),
                PortfolioAnalyticsPerformanceRowDto.builder().companyName("FVPL").investedAmount(new BigDecimal("101250")).currentValue(new BigDecimal("123750")).build(),
                PortfolioAnalyticsPerformanceRowDto.builder().companyName("TEB").investedAmount(new BigDecimal("135000")).currentValue(new BigDecimal("150000")).build(),
                PortfolioAnalyticsPerformanceRowDto.builder().companyName("ROHE").investedAmount(new BigDecimal("117500")).currentValue(new BigDecimal("105000")).build(),
                PortfolioAnalyticsPerformanceRowDto.builder().companyName("CSI").investedAmount(new BigDecimal("64000")).currentValue(new BigDecimal("58000")).build()
        );

        return PortfolioOwnershipResponseDto.builder()
                .asOfDate(date)
                .summaryKpis(summaryKpis)
                .activeTab(activeTab)
                .companies(companies)
                .statementSummary(statementSummary)
                .ownershipDistribution(ownershipDistribution)
                .ownershipTrend(ownershipTrend)
                .investmentVsCurrentValue(invVsCur)
                .build();
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
