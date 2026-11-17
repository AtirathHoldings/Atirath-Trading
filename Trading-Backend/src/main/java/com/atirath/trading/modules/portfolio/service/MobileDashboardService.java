package com.atirath.trading.modules.portfolio.service;

import com.atirath.trading.modules.auth.model.UserAccount;
import com.atirath.trading.modules.auth.repository.BankDetailRepository;
import com.atirath.trading.modules.auth.repository.IdentityKycDetailRepository;
import com.atirath.trading.modules.auth.repository.UserAccountRepository;
import com.atirath.trading.modules.market.dto.MobileStockDto;
import com.atirath.trading.modules.market.model.MarketCompany;
import com.atirath.trading.modules.market.model.MarketOrder;
import com.atirath.trading.modules.market.model.MarketTrade;
import com.atirath.trading.modules.market.model.UserShareHolding;
import com.atirath.trading.modules.market.repository.AppNotificationRepository;
import com.atirath.trading.modules.market.repository.EscrowAccountRepository;
import com.atirath.trading.modules.market.repository.MarketCompanyRepository;
import com.atirath.trading.modules.market.repository.MarketOrderRepository;
import com.atirath.trading.modules.market.repository.MarketTradeRepository;
import com.atirath.trading.modules.market.repository.UserShareHoldingRepository;
import com.atirath.trading.modules.market.service.MarketCompanyService;
import com.atirath.trading.modules.portfolio.dto.mobile.*;
import com.atirath.trading.modules.portfolio.service.PortfolioDataHelper.ComputedHolding;
import com.atirath.trading.modules.portfolio.service.PortfolioDataHelper.HoldingSummary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class MobileDashboardService {

    private static final DateTimeFormatter TXN_DISPLAY_FMT =
            DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.ENGLISH);
    private static final Set<String> ACTIVE_ORDER_STATUSES =
            Set.of("PENDING", "OPEN", "IN_ESCROW", "PARTIALLY_FILLED");

    private final PortfolioDataHelper dataHelper;
    private final UserAccountRepository userAccountRepository;
    private final UserShareHoldingRepository shareHoldingRepository;
    private final MarketCompanyRepository companyRepository;
    private final MarketCompanyService marketCompanyService;
    private final MarketOrderRepository orderRepository;
    private final MarketTradeRepository tradeRepository;
    private final EscrowAccountRepository escrowAccountRepository;
    private final AppNotificationRepository appNotificationRepository;
    private final IdentityKycDetailRepository identityKycDetailRepository;
    private final BankDetailRepository bankDetailRepository;

    @Transactional(readOnly = true)
    public MobileDashboardDto getMobileDashboard(Long userId) {
        int hour = LocalTime.now().getHour();
        String greeting = hour < 12 ? "Good morning," : (hour < 17 ? "Good afternoon," : "Good evening,");

        UserAccount user = null;
        if (userId != null) {
            user = userAccountRepository.findById(userId).orElse(null);
        }

        if (user == null) {
            // Strictly clean zero/empty state when unauthenticated or user not found
            List<MobileStockDto> marketStocks = marketCompanyService.getMobileStocks(null, null).stream()
                    .limit(3)
                    .toList();
            return MobileDashboardDto.builder()
                    .greeting(greeting)
                    .userName("Member")
                    .memberId("")
                    .platformSubtitle("Atirath Group Internal Share Trading Platform")
                    .totalPortfolioValue(BigDecimal.ZERO)
                    .formattedTotalValue("₹ 0")
                    .dayChange(BigDecimal.ZERO)
                    .formattedDayChange("+₹0")
                    .dayChangePercent(BigDecimal.ZERO)
                    .formattedDayChangePercent("+0.00%")
                    .dayChangePositive(true)
                    .investedValue(BigDecimal.ZERO)
                    .formattedInvestedValue("₹ 0")
                    .unrealizedPnL(BigDecimal.ZERO)
                    .formattedUnrealizedPnL("+₹0")
                    .unrealizedPnLPositive(true)
                    .realizedPnL(BigDecimal.ZERO)
                    .formattedRealizedPnL("+₹0")
                    .realizedPnLPositive(true)
                    .pendingActionsCount(0)
                    .pendingActions(List.of())
                    .marketStocks(marketStocks)
                    .topHoldings(List.of())
                    .recentTransactions(List.of())
                    .build();
        }

        // 1. User & Greeting
        String userName = dataHelper.resolveUserFullName(user);
        String memberId = "ATG" + user.getUserId();

        // 2. Holdings & Hero Card Calculation
        List<UserShareHolding> rawHoldings = (user != null)
                ? shareHoldingRepository.findByUserAccount_UserId(user.getUserId()).stream()
                    .filter(h -> h.getTotalShares() != null && h.getTotalShares() > 0 && h.getCompany() != null)
                    .toList()
                : List.of();

        HoldingSummary summary = dataHelper.aggregateHoldings(rawHoldings);

        BigDecimal totalPortfolioValue = summary.totalValue() != null ? summary.totalValue() : BigDecimal.ZERO;
        BigDecimal investedValue = summary.investedAmount() != null ? summary.investedAmount() : BigDecimal.ZERO;
        BigDecimal prevTotalValue = summary.prevTotalValue() != null ? summary.prevTotalValue() : BigDecimal.ZERO;

        BigDecimal dayChange = totalPortfolioValue.subtract(prevTotalValue);
        BigDecimal dayChangePercent = (prevTotalValue.compareTo(BigDecimal.ZERO) > 0)
                ? dataHelper.computePercent(dayChange, prevTotalValue)
                : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        boolean dayChangePositive = dayChange.compareTo(BigDecimal.ZERO) >= 0;

        BigDecimal unrealizedPnL = totalPortfolioValue.subtract(investedValue);
        boolean unrealizedPnLPositive = unrealizedPnL.compareTo(BigDecimal.ZERO) >= 0;

        // Calculate real realized PnL
        BigDecimal realizedPnL = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
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
                    realizedPnL = realizedPnL.add(pnl);
                }
            }
        }
        boolean realizedPnLPositive = realizedPnL.compareTo(BigDecimal.ZERO) >= 0;

        String formattedTotalValue = (totalPortfolioValue.compareTo(BigDecimal.ZERO) > 0)
                ? dataHelper.formatCompactInr(totalPortfolioValue)
                : "₹ 0";
        String formattedDayChange = (dayChangePositive ? "+" : "") + dataHelper.formatInr(dayChange.abs());
        String formattedDayChangePercent = (dayChangePositive ? "+" : "") + dayChangePercent.setScale(2, RoundingMode.HALF_UP).toPlainString() + "%";
        String formattedInvestedValue = dataHelper.formatInr(investedValue);
        String formattedUnrealizedPnL = (unrealizedPnLPositive ? "+" : "-") + dataHelper.formatInr(unrealizedPnL.abs());
        String formattedRealizedPnL = (realizedPnLPositive ? "+" : "-") + dataHelper.formatInr(realizedPnL.abs());

        // 3. Pending Actions (Strictly Real Data)
        List<MobilePendingActionDto> pendingActions = buildPendingActions(user);

        // 4. Group Market Snapshot (Top 3 active stocks)
        List<MobileStockDto> marketStocks = marketCompanyService.getMobileStocks(null, null).stream()
                .limit(3)
                .toList();

        // 5. Top Holdings (Top 2 Holdings Summary Cards)
        List<MobileDashboardHoldingDto> topHoldings = buildTopHoldings(summary.holdings());

        // 6. Recent Transactions (Top 3)
        List<MobileDashboardTransactionDto> recentTransactions = buildRecentTransactions(user != null ? user.getUserId() : null);

        return MobileDashboardDto.builder()
                .greeting(greeting)
                .userName(userName)
                .memberId(memberId)
                .platformSubtitle("Atirath Group Internal Share Trading Platform")
                .totalPortfolioValue(totalPortfolioValue)
                .formattedTotalValue(formattedTotalValue)
                .dayChange(dayChange)
                .formattedDayChange(formattedDayChange)
                .dayChangePercent(dayChangePercent)
                .formattedDayChangePercent(formattedDayChangePercent)
                .dayChangePositive(dayChangePositive)
                .investedValue(investedValue)
                .formattedInvestedValue(formattedInvestedValue)
                .unrealizedPnL(unrealizedPnL)
                .formattedUnrealizedPnL(formattedUnrealizedPnL)
                .unrealizedPnLPositive(unrealizedPnLPositive)
                .realizedPnL(realizedPnL)
                .formattedRealizedPnL(formattedRealizedPnL)
                .realizedPnLPositive(realizedPnLPositive)
                .pendingActionsCount(pendingActions.size())
                .pendingActions(pendingActions)
                .marketStocks(marketStocks)
                .topHoldings(topHoldings)
                .recentTransactions(recentTransactions)
                .build();
    }

    private List<MobilePendingActionDto> buildPendingActions(UserAccount user) {
        if (user == null) {
            return List.of();
        }

        List<MobilePendingActionDto> list = new ArrayList<>();
        Long userId = user.getUserId();

        // 1. KYC verification if KYC is not verified
        boolean kycDone = identityKycDetailRepository.findByUserAccount_UserId(userId).isPresent();
        if (!kycDone) {
            list.add(MobilePendingActionDto.builder()
                    .id("ACT_KYC_1")
                    .title("Complete KYC Verification")
                    .subtitle("Submit identity proof to unlock complete trading privileges")
                    .urgency("HIGH")
                    .urgencyColor("RED")
                    .actionText("Complete KYC →")
                    .targetRoute("KYC")
                    .build());
        }

        // 2. Bank Details if not added
        boolean bankDone = bankDetailRepository.findByUserAccount_UserId(userId).isPresent();
        if (!bankDone) {
            list.add(MobilePendingActionDto.builder()
                    .id("ACT_BANK_1")
                    .title("Update Bank Details")
                    .subtitle("Link a bank account for settlements & payouts")
                    .urgency("NORMAL")
                    .urgencyColor("BLUE")
                    .actionText("Add Bank →")
                    .targetRoute("KYC")
                    .build());
        }

        // 3. Open Orders
        List<MarketOrder> orders = orderRepository.findByUserAccount_UserIdOrderByPlacedAtDesc(userId);
        long openOrdersCount = orders.stream()
                .filter(o -> o.getStatus() != null && ACTIVE_ORDER_STATUSES.contains(o.getStatus().toUpperCase(Locale.ROOT)))
                .count();
        if (openOrdersCount > 0) {
            list.add(MobilePendingActionDto.builder()
                    .id("ACT_ORDERS_1")
                    .title(openOrdersCount + (openOrdersCount == 1 ? " Active Order" : " Active Orders"))
                    .subtitle("Review your open market orders and status")
                    .urgency("NORMAL")
                    .urgencyColor("BLUE")
                    .actionText("View Orders →")
                    .targetRoute("TRADE")
                    .build());
        }

        // 4. Unread notifications
        long unreadNotifs = appNotificationRepository.countByUserAccount_UserIdAndStatus(userId, "UNREAD");
        if (unreadNotifs > 0) {
            list.add(MobilePendingActionDto.builder()
                    .id("ACT_NOTIF_1")
                    .title(unreadNotifs + (unreadNotifs == 1 ? " Unread Alert" : " Unread Alerts"))
                    .subtitle("Check important account & market notifications")
                    .urgency("NORMAL")
                    .urgencyColor("BLUE")
                    .actionText("View Alerts →")
                    .targetRoute("NOTIFICATIONS")
                    .build());
        }

        return list;
    }

    private List<MobileDashboardHoldingDto> buildTopHoldings(List<ComputedHolding> holdings) {
        List<MobileDashboardHoldingDto> result = new ArrayList<>();

        if (holdings != null && !holdings.isEmpty()) {
            for (ComputedHolding h : holdings) {
                if (result.size() >= 2) break;
                String symbol = h.company().getSymbol();
                long totalShares = h.shares();
                BigDecimal val = h.currentValue();
                BigDecimal pnlPct = h.pnlPercentage();
                boolean isPositive = pnlPct.compareTo(BigDecimal.ZERO) >= 0;
                String pctFormatted = (isPositive ? "+" : "") + pnlPct.setScale(1, RoundingMode.HALF_UP).toPlainString() + "%";

                result.add(MobileDashboardHoldingDto.builder()
                        .companyId(h.company().getCompanyId())
                        .symbol(symbol)
                        .companyName(h.company().getCompanyName())
                        .shares(dataHelper.formatCount(totalShares) + " shares")
                        .totalShares(totalShares)
                        .value(dataHelper.formatCompactInr(val))
                        .currentValue(val)
                        .pct(pctFormatted)
                        .pnlPercent(pnlPct)
                        .isPositive(isPositive)
                        .build());
            }
        }

        return result;
    }

    private List<MobileDashboardTransactionDto> buildRecentTransactions(Long userId) {
        List<MobileDashboardTransactionDto> result = new ArrayList<>();

        if (userId != null) {
            List<MarketOrder> orders = orderRepository.findByUserAccount_UserIdOrderByPlacedAtDesc(userId);
            for (MarketOrder o : orders) {
                if (result.size() >= 3) break;
                if (o.getCompany() == null) continue;
                boolean isBuy = "BUY".equalsIgnoreCase(o.getOrderSide());
                String sym = o.getCompany().getSymbol();
                int shares = o.getQuantity() != null ? o.getQuantity().intValue() : 0;
                double price = o.getPricePerShare() != null ? o.getPricePerShare().doubleValue() : 0.0;
                BigDecimal total = o.getOrderValue() != null && o.getOrderValue().compareTo(BigDecimal.ZERO) > 0
                        ? o.getOrderValue()
                        : (o.getPricePerShare() != null && o.getQuantity() != null
                            ? o.getPricePerShare().multiply(BigDecimal.valueOf(o.getQuantity()))
                            : BigDecimal.ZERO);
                String dateStr = o.getPlacedAt() != null
                        ? o.getPlacedAt().format(TXN_DISPLAY_FMT)
                        : "-";
                String status = "FILLED".equalsIgnoreCase(o.getStatus()) ? "Settled"
                        : ("OPEN".equalsIgnoreCase(o.getStatus()) ? "Pending" : o.getStatus());

                result.add(MobileDashboardTransactionDto.builder()
                        .id(o.getOrderId())
                        .isBuy(isBuy)
                        .type(isBuy ? "Buy" : "Sell")
                        .company(sym)
                        .companyName(o.getCompany().getCompanyName())
                        .shares(shares)
                        .price(price)
                        .total(dataHelper.formatInr(total))
                        .totalAmount(total)
                        .date(dateStr)
                        .status(status)
                        .build());
            }
        }

        return result;
    }
}
