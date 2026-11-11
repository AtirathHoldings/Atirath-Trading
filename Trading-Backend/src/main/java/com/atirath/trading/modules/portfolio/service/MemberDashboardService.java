package com.atirath.trading.modules.portfolio.service;

import com.atirath.trading.common.AccountStatus;
import com.atirath.trading.common.AccountType;
import com.atirath.trading.common.ApiException;
import com.atirath.trading.common.ResourceNotFoundException;
import com.atirath.trading.modules.auth.model.IdentityKycDetail;
import com.atirath.trading.modules.auth.model.UserAccount;
import com.atirath.trading.modules.auth.repository.AccountDocumentRepository;
import com.atirath.trading.modules.auth.repository.BankDetailRepository;
import com.atirath.trading.modules.auth.repository.IdentityKycDetailRepository;
import com.atirath.trading.modules.auth.repository.UserAccountRepository;
import com.atirath.trading.modules.market.model.EscrowAccount;
import com.atirath.trading.modules.market.model.MarketOrder;
import com.atirath.trading.modules.market.model.MarketTrade;
import com.atirath.trading.modules.market.model.UserShareHolding;
import com.atirath.trading.modules.market.repository.AppNotificationRepository;
import com.atirath.trading.modules.market.repository.EscrowAccountRepository;
import com.atirath.trading.modules.market.repository.MarketCompanyRepository;
import com.atirath.trading.modules.market.repository.MarketOrderRepository;
import com.atirath.trading.modules.market.repository.MarketTradeRepository;
import com.atirath.trading.modules.market.repository.UserShareHoldingRepository;
import com.atirath.trading.modules.portfolio.dto.MemberDashboardDto;
import com.atirath.trading.modules.portfolio.dto.MemberDashboardDto.AllocationSliceDto;
import com.atirath.trading.modules.portfolio.dto.MemberDashboardDto.HoldingRowDto;
import com.atirath.trading.modules.portfolio.dto.MemberDashboardDto.PendingActionDto;
import com.atirath.trading.modules.portfolio.dto.MemberDashboardDto.PerformancePointDto;
import com.atirath.trading.modules.portfolio.dto.MemberDashboardDto.TransactionRowDto;
import com.atirath.trading.modules.portfolio.service.PortfolioDataHelper.ComputedHolding;
import com.atirath.trading.modules.portfolio.service.PortfolioDataHelper.HoldingSummary;
import com.atirath.trading.modules.portfolio.service.PortfolioDataHelper.TrendSnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/**
 * Read-only service aggregating real per-user data from the database
 * for the Member Dashboard screen (/dashboard).
 */
@Service
@RequiredArgsConstructor
public class MemberDashboardService {

    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
    private static final DateTimeFormatter AS_OF_FMT =
            DateTimeFormatter.ofPattern("EEE, dd MMM yyyy | hh:mm a", Locale.ENGLISH);
    private static final DateTimeFormatter TXN_DATE_FMT =
            DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);
    private static final Set<String> ACTIVE_ORDER_STATUSES =
            Set.of("PENDING", "OPEN", "IN_ESCROW", "PARTIALLY_FILLED");

    private final PortfolioDataHelper dataHelper;
    private final UserAccountRepository userAccountRepository;
    private final IdentityKycDetailRepository identityKycDetailRepository;
    private final BankDetailRepository bankDetailRepository;
    private final AccountDocumentRepository accountDocumentRepository;
    private final UserShareHoldingRepository shareHoldingRepository;
    private final MarketCompanyRepository companyRepository;
    private final MarketOrderRepository orderRepository;
    private final MarketTradeRepository tradeRepository;
    private final EscrowAccountRepository escrowAccountRepository;
    private final AppNotificationRepository appNotificationRepository;

    @Transactional(readOnly = true)
    public MemberDashboardDto getMemberDashboard(Long userId, String period) {
        if (userId == null) {
            throw new ApiException("Authentication required to load member dashboard", HttpStatus.UNAUTHORIZED);
        }

        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User account not found"));

        String normalizedPeriod = dataHelper.normalizePeriod(period);
        OffsetDateTime now = OffsetDateTime.now();

        String fullName = dataHelper.resolveUserFullName(user);
        String displayName = dataHelper.extractFirstName(fullName);

        Optional<IdentityKycDetail> kycOpt = identityKycDetailRepository.findByUserAccount_UserId(userId);
        boolean kycCompleted = kycOpt.isPresent();
        Integer age = resolveAge(kycOpt.orElse(null));
        boolean tradingEligible = user.getAccountType() == AccountType.CORPORATE || age == null || age >= 18;
        boolean bankAdded = bankDetailRepository.findByUserAccount_UserId(userId).isPresent();
        int documentsCount = accountDocumentRepository.findByUserAccount_UserId(userId).size();

        KycCardState kycCard = buildKycCardState(user.getAccountStatus(), kycCompleted, bankAdded, documentsCount, age, tradingEligible);

        int totalCompaniesCount = companyRepository.findByStatusOrderByCompanyNameAsc("ACTIVE").size();
        List<UserShareHolding> rawHoldings = shareHoldingRepository.findByUserAccount_UserId(userId).stream()
                .filter(h -> h.getTotalShares() != null && h.getTotalShares() > 0 && h.getCompany() != null)
                .toList();

        HoldingSummary summary = dataHelper.aggregateHoldings(rawHoldings);
        List<HoldingRowDto> holdingRows = toHoldingRows(summary.holdings());
        List<AllocationSliceDto> allocationSlices = buildAllocationSlices(holdingRows, summary.totalValue());

        List<MarketOrder> userOrders = orderRepository.findByUserAccount_UserIdOrderByPlacedAtDesc(userId);
        List<MarketTrade> userTrades = tradeRepository.findUserTrades(userId);
        List<TransactionRowDto> recentTransactions = buildRecentTransactions(userId, userOrders, userTrades);

        EscrowAccount escrow = escrowAccountRepository.findByUserAccount_UserId(userId).orElse(null);
        BigDecimal escrowAvailable = escrow != null && escrow.getAvailableBalance() != null
                ? escrow.getAvailableBalance() : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        BigDecimal escrowBlocked = escrow != null && escrow.getBlockedBalance() != null
                ? escrow.getBlockedBalance() : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        List<PerformancePointDto> performanceSeries = dataHelper.buildPerformanceSeries(
                rawHoldings,
                summary.totalValue(),
                summary.investedAmount(),
                summary.prevTotalValue(),
                normalizedPeriod,
                now
        ).stream().map(this::toPerformancePoint).toList();

        long openOrdersCount = userOrders.stream()
                .filter(o -> o.getStatus() != null && ACTIVE_ORDER_STATUSES.contains(o.getStatus().toUpperCase(Locale.ROOT)))
                .count();
        long unreadNotifications = appNotificationRepository.countByUserAccount_UserIdAndStatus(userId, "UNREAD");

        List<PendingActionDto> pendingActions = buildPendingActions(
                user.getAccountStatus(), kycCompleted, bankAdded, documentsCount,
                tradingEligible, openOrdersCount, unreadNotifications, escrowAvailable
        );

        BigDecimal totalGainLoss = summary.totalValue().subtract(summary.investedAmount()).setScale(2, RoundingMode.HALF_UP);
        BigDecimal gainLossPct = dataHelper.computePercent(totalGainLoss, summary.investedAmount());
        BigDecimal dayChange = summary.totalValue().subtract(summary.prevTotalValue());
        BigDecimal portfolioChangePct = dataHelper.computePercent(dayChange, summary.prevTotalValue());

        return MemberDashboardDto.builder()
                .userId(userId)
                .memberId("ATG" + userId)
                .userName(fullName)
                .displayName(displayName)
                .accountType(user.getAccountType() != null ? user.getAccountType().name() : AccountType.INDIVIDUAL.name())
                .accountStatus(user.getAccountStatus() != null ? user.getAccountStatus().name() : AccountStatus.PENDING.name())
                .kycCompleted(kycCompleted)
                .bankAdded(bankAdded)
                .documentsCount(documentsCount)
                .age(age)
                .tradingEligible(tradingEligible)
                .kycStatusTitle(kycCard.title())
                .kycStatusDescription(kycCard.description())
                .kycActionLabel(kycCard.actionLabel())
                .kycActionRoute(kycCard.actionRoute())
                .kycCardTone(kycCard.tone())
                .asOfFormatted(now.format(AS_OF_FMT))
                .asOfIso(now.toString())
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
                .companiesInvestedCount(holdingRows.size())
                .totalCompaniesCount(totalCompaniesCount)
                .escrowAvailableBalance(escrowAvailable)
                .formattedEscrowAvailableBalance(dataHelper.formatInr(escrowAvailable))
                .escrowBlockedBalance(escrowBlocked)
                .activePeriod(normalizedPeriod)
                .performanceSeries(performanceSeries)
                .allocationSlices(allocationSlices)
                .holdings(holdingRows)
                .recentTransactions(recentTransactions)
                .pendingActions(pendingActions)
                .build();
    }

    private List<HoldingRowDto> toHoldingRows(List<ComputedHolding> computed) {
        return computed.stream().map(c -> HoldingRowDto.builder()
                .companyId(c.company().getCompanyId())
                .name(c.company().getCompanyName())
                .companyCode(c.company().getCompanyCode())
                .symbol(c.company().getSymbol())
                .shares(c.shares())
                .availableShares(c.availableShares())
                .lockedShares(c.lockedShares())
                .avgPrice(c.avgPrice())
                .formattedAvgPrice(dataHelper.formatInr(c.avgPrice()))
                .currentPrice(c.currentPrice())
                .formattedCurrentPrice(dataHelper.formatInr(c.currentPrice()))
                .currentValue(c.currentValue())
                .formattedCurrentValue(dataHelper.formatInr(c.currentValue()))
                .pnl(c.pnl())
                .pnlPercentage(c.pnlPercentage())
                .formattedPnlPercentage(dataHelper.formatSignedPercent(c.pnlPercentage()))
                .isPositive(c.pnl().compareTo(BigDecimal.ZERO) >= 0)
                .build()).toList();
    }

    private PerformancePointDto toPerformancePoint(TrendSnapshot snap) {
        return PerformancePointDto.builder()
                .name(snap.label())
                .value(snap.value())
                .formattedValue(snap.formattedValue())
                .build();
    }

    private Integer resolveAge(IdentityKycDetail kyc) {
        if (kyc == null || kyc.getDateOfBirth() == null) {
            return null;
        }
        return Math.max(0, Period.between(kyc.getDateOfBirth(), LocalDate.now()).getYears());
    }

    private KycCardState buildKycCardState(AccountStatus status, boolean kycCompleted, boolean bankAdded,
                                           int documentsCount, Integer age, boolean tradingEligible) {
        if (status == AccountStatus.SUSPENDED || status == AccountStatus.REJECTED) {
            return new KycCardState(
                    "Account " + status.name().charAt(0) + status.name().substring(1).toLowerCase(Locale.ENGLISH),
                    "Your account status requires attention. Review your KYC details or contact support.",
                    "View KYC Details",
                    "/kyc",
                    "danger"
            );
        }
        if (!tradingEligible && age != null) {
            return new KycCardState(
                    "Under-18 Account (Restricted)",
                    "Share buying & selling is enabled only for members aged 18 or above (Age: " + age + ").",
                    "View KYC Status",
                    "/kyc",
                    "warning"
            );
        }
        if (!kycCompleted || !bankAdded || documentsCount == 0) {
            return new KycCardState(
                    "Complete Onboarding",
                    "Submit pending KYC, bank details, or documents to complete your account profile.",
                    !kycCompleted ? "Complete KYC" : (!bankAdded ? "Add Bank Details" : "Upload Documents"),
                    !kycCompleted ? "/register-step3" : (!bankAdded ? "/bank-accounts" : "/register-step3"),
                    "warning"
            );
        }
        if (status == AccountStatus.ACTIVE) {
            return new KycCardState(
                    "KYC Verified",
                    "Your identity, PAN & bank details are verified and active for trading.",
                    "View KYC Profile",
                    "/kyc",
                    "success"
            );
        }
        return new KycCardState(
                "KYC Under Review",
                "Your documents (" + documentsCount + ") have been submitted and are currently under review.",
                "View KYC Status",
                "/kyc",
                "warning"
        );
    }

    private List<AllocationSliceDto> buildAllocationSlices(List<HoldingRowDto> holdings, BigDecimal totalValue) {
        if (holdings.isEmpty() || totalValue.compareTo(BigDecimal.ZERO) <= 0) {
            return List.of();
        }
        List<AllocationSliceDto> slices = new ArrayList<>();
        for (int i = 0; i < holdings.size(); i++) {
            HoldingRowDto h = holdings.get(i);
            BigDecimal pct = h.getCurrentValue()
                    .multiply(ONE_HUNDRED)
                    .divide(totalValue, 1, RoundingMode.HALF_UP);
            slices.add(AllocationSliceDto.builder()
                    .companyId(h.getCompanyId())
                    .name(h.getName())
                    .companyCode(h.getCompanyCode())
                    .symbol(h.getSymbol())
                    .value(h.getCurrentValue())
                    .formattedValue(h.getFormattedCurrentValue())
                    .percentage(pct)
                    .color(PortfolioDataHelper.CHART_COLORS.get(i % PortfolioDataHelper.CHART_COLORS.size()))
                    .build());
        }
        return slices;
    }

    private List<TransactionRowDto> buildRecentTransactions(Long userId, List<MarketOrder> orders, List<MarketTrade> trades) {
        List<SortableTransaction> items = new ArrayList<>();
        Set<Long> coveredOrderIds = new HashSet<>();

        for (MarketOrder o : orders) {
            if (o.getOrderId() != null) {
                coveredOrderIds.add(o.getOrderId());
            }
            boolean isBuy = "BUY".equalsIgnoreCase(o.getOrderSide());
            BigDecimal amount = o.getOrderValue() != null ? o.getOrderValue().setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
            OffsetDateTime dt = o.getPlacedAt();
            items.add(new SortableTransaction(
                    dt != null ? dt : OffsetDateTime.MIN,
                    TransactionRowDto.builder()
                            .id(o.getOrderId())
                            .referenceNo(o.getOrderNo())
                            .date(dt != null ? dt.format(TXN_DATE_FMT) : "-")
                            .company(o.getCompany() != null ? o.getCompany().getCompanyName() : "Atirath Group")
                            .symbol(o.getCompany() != null ? o.getCompany().getSymbol() : "")
                            .type(isBuy ? "Buy" : "Sell")
                            .shares(o.getQuantity() != null ? o.getQuantity() : 0L)
                            .amount(amount)
                            .formattedAmount(dataHelper.formatInr(amount))
                            .status(normalizeOrderStatus(o.getStatus()))
                            .isBuy(isBuy)
                            .build()
            ));
        }

        for (MarketTrade t : trades) {
            boolean isBuyer = t.getBuyerUser() != null && userId.equals(t.getBuyerUser().getUserId());
            Long matchingOrderId = isBuyer ? t.getBuyOrderId() : t.getSellOrderId();
            if (matchingOrderId != null && coveredOrderIds.contains(matchingOrderId)) {
                continue;
            }
            BigDecimal amount = t.getTradeValue() != null ? t.getTradeValue().setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
            OffsetDateTime dt = t.getExecutedAt();
            items.add(new SortableTransaction(
                    dt != null ? dt : OffsetDateTime.MIN,
                    TransactionRowDto.builder()
                            .id(t.getTradeId())
                            .referenceNo(t.getTradeNo())
                            .date(dt != null ? dt.format(TXN_DATE_FMT) : "-")
                            .company(t.getCompany() != null ? t.getCompany().getCompanyName() : "Atirath Group")
                            .symbol(t.getCompany() != null ? t.getCompany().getSymbol() : "")
                            .type(isBuyer ? "Buy" : "Sell")
                            .shares(t.getQuantity() != null ? t.getQuantity() : 0L)
                            .amount(amount)
                            .formattedAmount(dataHelper.formatInr(amount))
                            .status(normalizeOrderStatus(t.getStatus()))
                            .isBuy(isBuyer)
                            .build()
            ));
        }

        items.sort(Comparator.comparing(SortableTransaction::timestamp).reversed());
        return items.stream().limit(5).map(SortableTransaction::row).toList();
    }

    private List<PendingActionDto> buildPendingActions(AccountStatus status, boolean kycCompleted, boolean bankAdded,
                                                       int documentsCount, boolean tradingEligible, long openOrdersCount,
                                                       long unreadNotifications, BigDecimal escrowAvailable) {
        List<PendingActionDto> actions = new ArrayList<>();

        if (!kycCompleted) {
            actions.add(PendingActionDto.builder()
                    .id("complete-kyc")
                    .title("Complete KYC Verification")
                    .description("Submit your PAN, Aadhaar & DOB details")
                    .route("/register-step3")
                    .iconType("shield")
                    .badgeColor("orange")
                    .build());
        }
        if (!bankAdded) {
            actions.add(PendingActionDto.builder()
                    .id("add-bank")
                    .title("Update Bank Details")
                    .description("Link a bank account for settlements & payouts")
                    .route("/bank-accounts")
                    .iconType("card")
                    .badgeColor("blue")
                    .build());
        }
        if (documentsCount == 0) {
            actions.add(PendingActionDto.builder()
                    .id("upload-docs")
                    .title("Upload Supporting Documents")
                    .description("Upload PAN & identity verification documents")
                    .route("/register-step3")
                    .iconType("doc")
                    .badgeColor("purple")
                    .build());
        }
        if (kycCompleted && bankAdded && documentsCount > 0 && status == AccountStatus.PENDING) {
            actions.add(PendingActionDto.builder()
                    .id("kyc-review")
                    .title("KYC Under Compliance Review")
                    .description("Check live KYC approval progress")
                    .route("/kyc")
                    .iconType("shield")
                    .badgeColor("orange")
                    .build());
        }
        if (!tradingEligible) {
            actions.add(PendingActionDto.builder()
                    .id("under-18-notice")
                    .title("Under-18 Trading Restriction")
                    .description("Share buy & sell unlocks once you turn 18")
                    .route("/kyc")
                    .iconType("shield")
                    .badgeColor("orange")
                    .build());
        }
        if (openOrdersCount > 0) {
            actions.add(PendingActionDto.builder()
                    .id("open-orders")
                    .title(openOrdersCount + (openOrdersCount == 1 ? " Active Order" : " Active Orders"))
                    .description("Review open orders and escrow status")
                    .route("/orders")
                    .iconType("check")
                    .badgeColor("blue")
                    .build());
        }
        if (unreadNotifications > 0) {
            actions.add(PendingActionDto.builder()
                    .id("unread-notifs")
                    .title(unreadNotifications + (unreadNotifications == 1 ? " Unread Notification" : " Unread Notifications"))
                    .description("View recent market and account alerts")
                    .route("/notifications")
                    .iconType("bell")
                    .badgeColor("green")
                    .build());
        }
        if (kycCompleted && tradingEligible && escrowAvailable.compareTo(BigDecimal.ZERO) <= 0) {
            actions.add(PendingActionDto.builder()
                    .id("fund-escrow")
                    .title("Fund Escrow Account")
                    .description("Add balance to place buy orders on Internal Market")
                    .route("/escrow")
                    .iconType("card")
                    .badgeColor("green")
                    .build());
        }

        return actions.stream().limit(4).toList();
    }

    private String normalizeOrderStatus(String raw) {
        if (raw == null || raw.isBlank()) {
            return "Pending";
        }
        return switch (raw.trim().toUpperCase(Locale.ROOT)) {
            case "EXECUTED", "COMPLETED" -> "Completed";
            case "IN_ESCROW" -> "In Escrow";
            case "PARTIALLY_FILLED" -> "Partial";
            case "CANCELLED" -> "Cancelled";
            case "REJECTED" -> "Rejected";
            case "OPEN" -> "Open";
            default -> "Pending";
        };
    }

    private record KycCardState(String title, String description, String actionLabel, String actionRoute, String tone) {}

    private record SortableTransaction(OffsetDateTime timestamp, TransactionRowDto row) {}
}
