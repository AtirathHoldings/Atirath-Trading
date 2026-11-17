package com.atirath.trading.modules.portfolio.service;

import com.atirath.trading.modules.auth.model.UserAccount;
import com.atirath.trading.modules.auth.repository.UserAccountRepository;
import com.atirath.trading.modules.market.model.MarketCompany;
import com.atirath.trading.modules.market.model.MarketOrder;
import com.atirath.trading.modules.market.model.MarketPriceHistory;
import com.atirath.trading.modules.market.model.MarketTrade;
import com.atirath.trading.modules.market.model.UserShareHolding;
import com.atirath.trading.modules.market.repository.MarketCompanyRepository;
import com.atirath.trading.modules.market.repository.MarketOrderRepository;
import com.atirath.trading.modules.market.repository.MarketPriceHistoryRepository;
import com.atirath.trading.modules.market.repository.MarketTradeRepository;
import com.atirath.trading.modules.market.repository.UserShareHoldingRepository;
import com.atirath.trading.modules.portfolio.dto.mobile.MobileAllocationDto;
import com.atirath.trading.modules.portfolio.dto.mobile.MobileHoldingDetailDto;
import com.atirath.trading.modules.portfolio.dto.mobile.MobileOrderRowDto;
import com.atirath.trading.modules.portfolio.dto.mobile.MobilePortfolioDto;
import com.atirath.trading.modules.portfolio.service.PortfolioDataHelper.ComputedHolding;
import com.atirath.trading.modules.portfolio.service.PortfolioDataHelper.HoldingSummary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class MobilePortfolioService {

    private static final DateTimeFormatter ORDER_DATE_FMT =
            DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.ENGLISH);
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    private final PortfolioDataHelper dataHelper;
    private final UserAccountRepository userAccountRepository;
    private final UserShareHoldingRepository shareHoldingRepository;
    private final MarketCompanyRepository companyRepository;
    private final MarketOrderRepository orderRepository;
    private final MarketTradeRepository tradeRepository;
    private final MarketPriceHistoryRepository priceHistoryRepository;

    @Transactional(readOnly = true)
    public MobilePortfolioDto getMobilePortfolio(Long userId) {
        UserAccount user = null;
        if (userId != null) {
            user = userAccountRepository.findById(userId).orElse(null);
        }

        if (user == null) {
            // Strictly clean zero/empty portfolio when unauthenticated or user not found
            return MobilePortfolioDto.builder()
                    .userName("Member")
                    .memberId("")
                    .totalPortfolioValue(BigDecimal.ZERO)
                    .formattedTotalPortfolioValue("₹ 0")
                    .todayChange(BigDecimal.ZERO)
                    .formattedTodayChange("+₹0")
                    .todayChangePct(BigDecimal.ZERO)
                    .formattedTodayChangePct("+0.00%")
                    .todayChangePositive(true)
                    .investedCapital(BigDecimal.ZERO)
                    .formattedInvestedCapital("₹ 0")
                    .unrealizedPL(BigDecimal.ZERO)
                    .formattedUnrealizedPL("+₹0")
                    .unrealizedPLPositive(true)
                    .realizedPL(BigDecimal.ZERO)
                    .formattedRealizedPL("+₹0")
                    .realizedPLPositive(true)
                    .totalGainLossPct(BigDecimal.ZERO)
                    .formattedTotalGainLossPct("+0.00%")
                    .totalGainLossPositive(true)
                    .holdings(List.of())
                    .orders(List.of())
                    .allocations(List.of())
                    .build();
        }

        String userName = dataHelper.resolveUserFullName(user);
        String memberId = "ATG" + user.getUserId();

        List<UserShareHolding> rawHoldings = (user != null)
                ? shareHoldingRepository.findByUserAccount_UserId(user.getUserId()).stream()
                    .filter(h -> h.getTotalShares() != null && h.getTotalShares() > 0 && h.getCompany() != null)
                    .toList()
                : List.of();

        HoldingSummary summary = dataHelper.aggregateHoldings(rawHoldings);

        BigDecimal totalPortfolioValue = summary.totalValue() != null ? summary.totalValue() : BigDecimal.ZERO;
        BigDecimal investedCapital = summary.investedAmount() != null ? summary.investedAmount() : BigDecimal.ZERO;
        BigDecimal prevTotalValue = summary.prevTotalValue() != null ? summary.prevTotalValue() : BigDecimal.ZERO;

        BigDecimal dayChange = totalPortfolioValue.subtract(prevTotalValue);
        BigDecimal dayChangePct = (prevTotalValue.compareTo(BigDecimal.ZERO) > 0)
                ? dataHelper.computePercent(dayChange, prevTotalValue)
                : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        boolean dayChangePositive = dayChange.compareTo(BigDecimal.ZERO) >= 0;

        BigDecimal unrealizedPL = totalPortfolioValue.subtract(investedCapital);
        boolean unrealizedPLPositive = unrealizedPL.compareTo(BigDecimal.ZERO) >= 0;

        BigDecimal totalGainLossPct = (investedCapital.compareTo(BigDecimal.ZERO) > 0)
                ? dataHelper.computePercent(unrealizedPL, investedCapital)
                : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        boolean totalGainLossPositive = totalGainLossPct.compareTo(BigDecimal.ZERO) >= 0;

        // Calculate Real Realized PnL
        BigDecimal realizedPL = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        if (user != null) {
            List<MarketTrade> userTrades = tradeRepository.findUserTrades(user.getUserId());
            Map<Long, BigDecimal> avgCostByCompany = new HashMap<>();
            for (UserShareHolding h : rawHoldings) {
                if (h.getCompany() != null && h.getAverageBuyPrice() != null) {
                    avgCostByCompany.put(h.getCompany().getCompanyId(), h.getAverageBuyPrice());
                }
            }
            for (MarketTrade t : userTrades) {
                if (t.getSellerUser() != null && user.getUserId().equals(t.getSellerUser().getUserId())
                        && t.getCompany() != null && t.getTradePrice() != null && t.getQuantity() != null) {
                    BigDecimal cost = avgCostByCompany.getOrDefault(t.getCompany().getCompanyId(), t.getTradePrice());
                    BigDecimal pnl = t.getTradePrice().subtract(cost).multiply(BigDecimal.valueOf(t.getQuantity()));
                    realizedPL = realizedPL.add(pnl);
                }
            }
        }
        boolean realizedPLPositive = realizedPL.compareTo(BigDecimal.ZERO) >= 0;

        String formattedTotalValue = (totalPortfolioValue.compareTo(BigDecimal.ZERO) > 0)
                ? dataHelper.formatInr(totalPortfolioValue)
                : "₹ 0";
        String formattedInvestedCapital = (investedCapital.compareTo(BigDecimal.ZERO) > 0)
                ? dataHelper.formatInr(investedCapital)
                : "₹ 0";
        String formattedUnrealizedPL = (unrealizedPLPositive ? "+" : "-") + dataHelper.formatInr(unrealizedPL.abs());
        String formattedRealizedPL = (realizedPLPositive ? "+" : "-") + dataHelper.formatInr(realizedPL.abs());
        String formattedTodayChange = (dayChangePositive ? "+" : "") + dataHelper.formatInr(dayChange.abs());
        String formattedTodayChangePct = (dayChangePositive ? "+" : "") + dayChangePct.setScale(2, RoundingMode.HALF_UP).toPlainString() + "%";
        String formattedTotalGainLossPct = (totalGainLossPositive ? "+" : "") + totalGainLossPct.setScale(2, RoundingMode.HALF_UP).toPlainString() + "%";

        // Allocations
        List<MobileAllocationDto> allocations = buildAllocations(summary.holdings(), totalPortfolioValue);

        // Holdings Detail
        List<MobileHoldingDetailDto> holdings = buildHoldings(rawHoldings, summary.holdings());

        // Orders
        List<MobileOrderRowDto> orders = buildOrders(user != null ? user.getUserId() : null);

        return MobilePortfolioDto.builder()
                .userId(user != null ? user.getUserId() : null)
                .userName(userName)
                .memberId(memberId)
                .totalPortfolioValue(totalPortfolioValue)
                .formattedTotalPortfolioValue(formattedTotalValue)
                .investedCapital(investedCapital)
                .formattedInvestedCapital(formattedInvestedCapital)
                .unrealizedPL(unrealizedPL)
                .formattedUnrealizedPL(formattedUnrealizedPL)
                .unrealizedPLPositive(unrealizedPLPositive)
                .realizedPL(realizedPL)
                .formattedRealizedPL(formattedRealizedPL)
                .realizedPLPositive(realizedPLPositive)
                .todayChange(dayChange)
                .formattedTodayChange(formattedTodayChange)
                .todayChangePct(dayChangePct)
                .formattedTodayChangePct(formattedTodayChangePct)
                .todayChangePositive(dayChangePositive)
                .totalGainLossPct(totalGainLossPct)
                .formattedTotalGainLossPct(formattedTotalGainLossPct)
                .totalGainLossPositive(totalGainLossPositive)
                .allocations(allocations)
                .holdings(holdings)
                .orders(orders)
                .build();
    }

    private List<MobileAllocationDto> buildAllocations(List<ComputedHolding> computedHoldings, BigDecimal totalPortfolioValue) {
        if (computedHoldings == null || computedHoldings.isEmpty() || totalPortfolioValue.compareTo(BigDecimal.ZERO) <= 0) {
            return List.of();
        }

        List<MobileAllocationDto> list = new ArrayList<>();
        List<String> colors = PortfolioDataHelper.CHART_COLORS;

        for (int i = 0; i < computedHoldings.size(); i++) {
            ComputedHolding ch = computedHoldings.get(i);
            BigDecimal val = ch.currentValue();
            BigDecimal pct = val.multiply(ONE_HUNDRED).divide(totalPortfolioValue, 1, RoundingMode.HALF_UP);
            String color = colors.get(i % colors.size());

            list.add(MobileAllocationDto.builder()
                    .companyId(ch.company().getCompanyId())
                    .label(ch.company().getSymbol())
                    .name(ch.company().getCompanyName())
                    .pct(pct.doubleValue())
                    .colorHex(color)
                    .value(val)
                    .formattedValue(dataHelper.formatCompactInr(val))
                    .build());
        }
        return list;
    }

    private List<MobileHoldingDetailDto> buildHoldings(List<UserShareHolding> rawHoldings, List<ComputedHolding> computedHoldings) {
        if (computedHoldings == null || computedHoldings.isEmpty()) {
            return List.of();
        }

        Map<Long, UserShareHolding> rawMap = new HashMap<>();
        for (UserShareHolding r : rawHoldings) {
            if (r.getCompany() != null) {
                rawMap.put(r.getCompany().getCompanyId(), r);
            }
        }

        List<MobileHoldingDetailDto> list = new ArrayList<>();
        for (ComputedHolding ch : computedHoldings) {
            MarketCompany comp = ch.company();
            UserShareHolding raw = rawMap.get(comp.getCompanyId());

            long totalShares = ch.shares();
            long locked = ch.lockedShares();
            long available = ch.availableShares();

            double avgPrice = ch.avgPrice() != null ? ch.avgPrice().doubleValue() : 0.0;
            double curPrice = ch.currentPrice().doubleValue();
            double invested = ch.investedValue().doubleValue();
            double currentVal = ch.currentValue().doubleValue();
            double plVal = ch.pnl().doubleValue();
            double plPctVal = ch.pnlPercentage().doubleValue();
            boolean isPositive = plVal >= 0;

            long issued = comp.getIssuedShares() != null && comp.getIssuedShares() > 0 ? comp.getIssuedShares() : 10000000L;
            BigDecimal ownPct = BigDecimal.valueOf(totalShares).multiply(ONE_HUNDRED)
                    .divide(BigDecimal.valueOf(issued), 4, RoundingMode.HALF_UP);
            String ownershipStr = ownPct.toPlainString() + "%";

            // Sparkline chart data
            List<Double> sparkline = new ArrayList<>();
            try {
                List<MarketPriceHistory> prices = priceHistoryRepository
                        .findByCompany_CompanyIdAndIntervalTypeOrderByPriceTimeAsc(comp.getCompanyId(), "1D");
                if (prices != null && !prices.isEmpty()) {
                    for (MarketPriceHistory p : prices) {
                        if (p.getClosePrice() != null) {
                            sparkline.add(p.getClosePrice().doubleValue());
                        }
                    }
                }
            } catch (Exception ignored) {}

            if (sparkline.isEmpty()) {
                sparkline = List.of(curPrice * 0.95, curPrice * 0.97, curPrice * 0.96, curPrice * 0.99, curPrice);
            }

            list.add(MobileHoldingDetailDto.builder()
                    .companyId(comp.getCompanyId())
                    .code(comp.getSymbol())
                    .symbol(comp.getSymbol())
                    .name(comp.getCompanyName())
                    .shares(totalShares)
                    .available(available)
                    .locked(locked)
                    .avgPrice(avgPrice)
                    .currentPrice(curPrice)
                    .invested(invested)
                    .currentValue(currentVal)
                    .pl(plVal)
                    .plPct(plPctVal)
                    .ownershipPct(ownershipStr)
                    .isPositive(isPositive)
                    .chartData(sparkline)
                    .build());
        }

        return list;
    }

    private List<MobileOrderRowDto> buildOrders(Long userId) {
        if (userId == null) {
            return List.of();
        }

        List<MarketOrder> orders = orderRepository.findByUserAccount_UserIdOrderByPlacedAtDesc(userId);
        if (orders.isEmpty()) {
            return List.of();
        }

        List<MobileOrderRowDto> list = new ArrayList<>();
        for (MarketOrder o : orders) {
            String sym = o.getCompany() != null ? o.getCompany().getSymbol() : "ATG";
            String cname = o.getCompany() != null ? o.getCompany().getCompanyName() : "Atirath Group";
            String side = o.getOrderSide() != null ? o.getOrderSide().toUpperCase(Locale.ROOT) : "BUY";
            long qty = o.getQuantity() != null ? o.getQuantity() : 0L;
            double price = o.getPricePerShare() != null ? o.getPricePerShare().doubleValue() : 0.0;
            BigDecimal val = o.getOrderValue() != null ? o.getOrderValue() : BigDecimal.valueOf(qty * price);
            String dt = o.getPlacedAt() != null ? o.getPlacedAt().format(ORDER_DATE_FMT) : "-";
            String status = o.getStatus() != null ? o.getStatus() : "OPEN";

            list.add(MobileOrderRowDto.builder()
                    .orderId(o.getOrderId())
                    .orderNo(o.getOrderNo())
                    .symbol(sym)
                    .companyName(cname)
                    .orderSide(side)
                    .quantity(qty)
                    .price(price)
                    .orderValue(val)
                    .formattedValue(dataHelper.formatInr(val))
                    .placedAt(dt)
                    .status(status)
                    .build());
        }

        return list;
    }
}
