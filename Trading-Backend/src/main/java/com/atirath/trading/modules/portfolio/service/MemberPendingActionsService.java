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
import com.atirath.trading.modules.market.model.Escrow;
import com.atirath.trading.modules.market.model.EscrowAccount;
import com.atirath.trading.modules.market.model.MarketOrder;
import com.atirath.trading.modules.market.model.ShareTransfer;
import com.atirath.trading.modules.market.repository.AppNotificationRepository;
import com.atirath.trading.modules.market.repository.EscrowAccountRepository;
import com.atirath.trading.modules.market.repository.EscrowRepository;
import com.atirath.trading.modules.market.repository.MarketOrderRepository;
import com.atirath.trading.modules.market.repository.ShareTransferRepository;
import com.atirath.trading.modules.negotiation.model.Negotiation;
import com.atirath.trading.modules.negotiation.repository.NegotiationRepository;
import com.atirath.trading.modules.portfolio.dto.MemberPendingActionsDto;
import com.atirath.trading.modules.portfolio.dto.MemberPendingActionsDto.DeadlineItemDto;
import com.atirath.trading.modules.portfolio.dto.MemberPendingActionsDto.PendingActionRowDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Read-only service aggregating real per-user pending actions across KYC,
 * Orders, Negotiations, Escrow settlements, and Account alerts.
 */
@Service
@RequiredArgsConstructor
public class MemberPendingActionsService {

    private static final DateTimeFormatter HEADER_DT_FMT =
            DateTimeFormatter.ofPattern("EEE, dd MMM yyyy | hh:mm a", Locale.ENGLISH);
    private static final DateTimeFormatter DUE_DATE_FMT =
            DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter MONTH_SHORT_FMT =
            DateTimeFormatter.ofPattern("MMM", Locale.ENGLISH);
    private static final Set<String> ACTIVE_ORDER_STATUSES =
            Set.of("PENDING", "OPEN", "IN_ESCROW", "PARTIALLY_FILLED");
    private static final Set<String> ACTIVE_NEGOTIATION_STATUSES =
            Set.of("OPEN", "COUNTERED", "PENDING", "IN_PROGRESS", "AWAITING_RESPONSE");
    private static final Set<String> ACTIVE_ESCROW_STATUSES =
            Set.of("CREATED", "FUNDS_BLOCKED", "PENDING", "IN_PROGRESS", "AWAITING_RELEASE");

    private final PortfolioDataHelper dataHelper;
    private final UserAccountRepository userAccountRepository;
    private final IdentityKycDetailRepository identityKycDetailRepository;
    private final BankDetailRepository bankDetailRepository;
    private final AccountDocumentRepository accountDocumentRepository;
    private final MarketOrderRepository orderRepository;
    private final NegotiationRepository negotiationRepository;
    private final EscrowRepository escrowRepository;
    private final EscrowAccountRepository escrowAccountRepository;
    private final ShareTransferRepository shareTransferRepository;
    private final AppNotificationRepository appNotificationRepository;

    @Transactional(readOnly = true)
    public MemberPendingActionsDto getMemberPendingActions(Long userId, String category, String sortBy) {
        if (userId == null) {
            throw new ApiException("Authentication required to load pending actions", HttpStatus.UNAUTHORIZED);
        }

        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User account not found"));

        OffsetDateTime now = OffsetDateTime.now();
        LocalDate today = now.toLocalDate();
        String fullName = dataHelper.resolveUserFullName(user);
        String memberId = "ATG" + userId;

        List<ActionEntry> allEntries = collectPendingActions(userId, user, memberId, today);

        int highCount = 0;
        int mediumCount = 0;
        int normalCount = 0;

        Map<String, Integer> categoryCounts = new LinkedHashMap<>();
        categoryCounts.put("All", allEntries.size());
        categoryCounts.put("KYC", 0);
        categoryCounts.put("Orders", 0);
        categoryCounts.put("Negotiations", 0);
        categoryCounts.put("Escrow", 0);
        categoryCounts.put("Account", 0);

        for (ActionEntry entry : allEntries) {
            switch (entry.row().getPriority()) {
                case "High" -> highCount++;
                case "Medium" -> mediumCount++;
                default -> normalCount++;
            }
            categoryCounts.computeIfPresent(entry.row().getCategory(), (k, v) -> v + 1);
        }

        String activeCategory = normalizeCategory(category);
        String activeSort = "Date".equalsIgnoreCase(sortBy) ? "Date" : "Priority";

        List<ActionEntry> filtered = allEntries.stream()
                .filter(e -> "All".equalsIgnoreCase(activeCategory)
                        || e.row().getCategory().equalsIgnoreCase(activeCategory))
                .sorted(buildComparator(activeSort))
                .toList();

        List<DeadlineItemDto> upcomingDeadlines = allEntries.stream()
                .sorted(Comparator.comparing(ActionEntry::dueDate))
                .limit(4)
                .map(entry -> DeadlineItemDto.builder()
                        .id(entry.row().getId())
                        .dueDay(String.format("%02d", entry.dueDate().getDayOfMonth()))
                        .dueMonth(entry.dueDate().format(MONTH_SHORT_FMT))
                        .title(entry.row().getTitle())
                        .relatedTitle(entry.row().getRelatedTitle())
                        .daysLeft(entry.row().getDaysLeft())
                        .urgent(entry.row().isUrgent())
                        .route(entry.row().getRoute())
                        .build())
                .toList();

        return MemberPendingActionsDto.builder()
                .userId(userId)
                .memberId(memberId)
                .userName(fullName)
                .asOfFormatted(now.format(HEADER_DT_FMT))
                .totalPending(allEntries.size())
                .highPriorityCount(highCount)
                .mediumPriorityCount(mediumCount)
                .normalPriorityCount(normalCount)
                .categoryCounts(categoryCounts)
                .activeCategory(activeCategory)
                .sortBy(activeSort)
                .items(filtered.stream().map(ActionEntry::row).toList())
                .upcomingDeadlines(upcomingDeadlines)
                .build();
    }

    private List<ActionEntry> collectPendingActions(Long userId, UserAccount user, String memberId, LocalDate today) {
        List<ActionEntry> entries = new ArrayList<>();

        // 1. KYC & Onboarding
        Optional<IdentityKycDetail> kycOpt = identityKycDetailRepository.findByUserAccount_UserId(userId);
        boolean kycCompleted = kycOpt.isPresent();
        int docsCount = accountDocumentRepository.findByUserAccount_UserId(userId).size();
        Integer age = resolveAge(kycOpt.orElse(null), today);
        boolean tradingEligible = user.getAccountType() == AccountType.CORPORATE || age == null || age >= 18;
        boolean bankAdded = bankDetailRepository.findByUserAccount_UserId(userId).isPresent();

        if (!kycCompleted) {
            entries.add(buildEntry(
                    "kyc-complete",
                    "Complete KYC Verification",
                    "Submit your PAN, Aadhaar, and date of birth to verify your identity.",
                    "KYC",
                    "Your Account",
                    memberId,
                    today.plusDays(2),
                    "High",
                    "Pending",
                    "pending",
                    "Continue",
                    "primary",
                    "/register-step3",
                    today
            ));
        } else if (docsCount == 0) {
            entries.add(buildEntry(
                    "kyc-docs",
                    "Upload KYC Documents",
                    "Upload your PAN card and identity verification documents to proceed.",
                    "KYC",
                    "Your Account",
                    memberId,
                    today.plusDays(2),
                    "High",
                    "Action Required",
                    "action",
                    "Continue",
                    "primary",
                    "/register-step3",
                    today
            ));
        } else if (user.getAccountStatus() == AccountStatus.PENDING) {
            entries.add(buildEntry(
                    "kyc-review",
                    "KYC Under Compliance Review",
                    "Your KYC submission (" + docsCount + " document" + (docsCount == 1 ? "" : "s") + ") is awaiting admin approval.",
                    "KYC",
                    "Your Account",
                    memberId,
                    today.plusDays(3),
                    "Medium",
                    "Pending",
                    "pending",
                    "View",
                    "secondary",
                    "/kyc",
                    today
            ));
        }

        // 2. Account Bank, Under-18 & Unread Alerts
        if (!bankAdded) {
            entries.add(buildEntry(
                    "acc-bank",
                    "Add Bank Account Details",
                    "Link your bank account for share settlements and escrow withdrawals.",
                    "Account",
                    "Settlement Bank",
                    memberId,
                    today.plusDays(3),
                    "High",
                    "Action Required",
                    "action",
                    "Continue",
                    "primary",
                    "/bank-accounts",
                    today
            ));
        }

        if (!tradingEligible && age != null) {
            LocalDate eligibilityDate = kycOpt.get().getDateOfBirth().plusYears(18);
            entries.add(buildEntry(
                    "acc-under18",
                    "Under-18 Trading Restriction",
                    "Buying & selling shares unlocks when you turn 18 (current age: " + age + ").",
                    "Account",
                    "Your Account",
                    memberId,
                    eligibilityDate,
                    "Medium",
                    "Pending",
                    "pending",
                    "View",
                    "secondary",
                    "/kyc",
                    today
            ));
        }

        long unreadNotifs = appNotificationRepository.countByUserAccount_UserIdAndStatus(userId, "UNREAD");
        if (unreadNotifs > 0) {
            entries.add(buildEntry(
                    "acc-notif",
                    "Review Unread Alerts",
                    "You have " + unreadNotifs + " unread notification" + (unreadNotifs == 1 ? "" : "s") + " in your inbox.",
                    "Account",
                    "Notifications",
                    unreadNotifs + " unread",
                    today.plusDays(7),
                    "Normal",
                    "Unread",
                    "unread",
                    "View",
                    "secondary",
                    "/notifications",
                    today
            ));
        }

        // 3. Active Orders
        List<MarketOrder> orders = orderRepository.findByUserAccount_UserIdOrderByPlacedAtDesc(userId);
        orders.stream()
                .filter(o -> o.getStatus() != null && ACTIVE_ORDER_STATUSES.contains(o.getStatus().toUpperCase(Locale.ROOT)))
                .limit(10)
                .forEach(o -> {
                    boolean isBuy = "BUY".equalsIgnoreCase(o.getOrderSide());
                    LocalDate due = o.getPlacedAt() != null ? o.getPlacedAt().toLocalDate().plusDays(1) : today.plusDays(1);
                    String compName = o.getCompany() != null ? o.getCompany().getCompanyName() : "Atirath Group";
                    String sub = dataHelper.formatCount(o.getQuantity() != null ? o.getQuantity() : 0L)
                            + " shares @ " + dataHelper.formatInr(o.getPricePerShare());
                    String rawStatus = o.getStatus().toUpperCase(Locale.ROOT);
                    String statusLabel = switch (rawStatus) {
                        case "IN_ESCROW" -> "Awaiting Confirmation";
                        case "PARTIALLY_FILLED" -> "Action Required";
                        default -> "Pending";
                    };
                    String tone = "IN_ESCROW".equals(rawStatus) ? "awaiting" : "pending";
                    String priority = "IN_ESCROW".equals(rawStatus) || !due.isAfter(today) ? "High" : "Medium";

                    entries.add(buildEntry(
                            "ord-" + o.getOrderId(),
                            isBuy ? "Review Buy Order" : "Review Sell Order",
                            "Order " + o.getOrderNo() + " is currently " + statusLabel.toLowerCase(Locale.ENGLISH) + ".",
                            "Orders",
                            compName,
                            sub,
                            due,
                            priority,
                            statusLabel,
                            tone,
                            "View",
                            "secondary",
                            "/orders",
                            today
                    ));
                });

        // 4. Active Negotiations
        List<Negotiation> negotiations = negotiationRepository.findAllForUser(userId);
        negotiations.stream()
                .filter(n -> n.getStatus() != null && ACTIVE_NEGOTIATION_STATUSES.contains(n.getStatus().toUpperCase(Locale.ROOT)))
                .limit(10)
                .forEach(n -> {
                    LocalDate due = n.getExpiresAt() != null
                            ? n.getExpiresAt().toLocalDate()
                            : (n.getCreatedAt() != null ? n.getCreatedAt().toLocalDate().plusDays(3) : today.plusDays(3));
                    String compName = n.getCompany() != null ? n.getCompany().getCompanyName() : "Atirath Group";
                    String sub = dataHelper.formatCount(n.getQuantity() != null ? n.getQuantity() : 0L)
                            + " shares @ " + dataHelper.formatInr(n.getProposedPrice());
                    boolean urgent = ChronoUnit.DAYS.between(today, due) <= 1 || "COUNTERED".equalsIgnoreCase(n.getStatus());

                    entries.add(buildEntry(
                            "neg-" + n.getNegotiationId(),
                            "Respond to Negotiation",
                            "Negotiation " + n.getNegotiationNo() + " awaits your review or counter-offer.",
                            "Negotiations",
                            compName,
                            sub,
                            due,
                            urgent ? "High" : "Medium",
                            "Action Required",
                            "action",
                            "Review",
                            "secondary",
                            "/negotiations",
                            today
                    ));
                });

        // 5. Pending Escrows & Pending Share Transfers
        List<Escrow> escrows = escrowRepository.findUserEscrows(userId);
        List<Escrow> activeEscrows = escrows.stream()
                .filter(e -> e.getEscrowStatus() != null && ACTIVE_ESCROW_STATUSES.contains(e.getEscrowStatus().toUpperCase(Locale.ROOT)))
                .limit(10)
                .toList();

        for (Escrow esc : activeEscrows) {
            boolean isBuyer = esc.getBuyerUser() != null && userId.equals(esc.getBuyerUser().getUserId());
            LocalDate due = esc.getCreatedAt() != null ? esc.getCreatedAt().toLocalDate().plusDays(3) : today.plusDays(3);
            String compName = (esc.getTrade() != null && esc.getTrade().getCompany() != null)
                    ? esc.getTrade().getCompany().getCompanyName()
                    : "Escrow Account";

            entries.add(buildEntry(
                    "esc-" + esc.getEscrowId(),
                    isBuyer ? "Approve Escrow Release" : "Track Escrow Settlement",
                    "Escrow " + esc.getEscrowReference() + " requires settlement confirmation.",
                    "Escrow",
                    compName,
                    dataHelper.formatInr(esc.getAmount()),
                    due,
                    "Medium",
                    "Awaiting Confirmation",
                    "awaiting",
                    "Verify",
                    "secondary",
                    "/escrow",
                    today
            ));
        }

        Set<Long> seenTransfers = new HashSet<>();
        List<ShareTransfer> transfers = new ArrayList<>();
        transfers.addAll(shareTransferRepository.findByFromUser_UserIdOrderByTransferredAtDesc(userId));
        transfers.addAll(shareTransferRepository.findByToUser_UserIdOrderByTransferredAtDesc(userId));

        for (ShareTransfer st : transfers) {
            if (!seenTransfers.add(st.getTransferId())) continue;
            if (!"PENDING".equalsIgnoreCase(st.getTransferStatus())) continue;
            LocalDate due = st.getTransferredAt() != null
                    ? st.getTransferredAt().toLocalDate().plusDays(2)
                    : today.plusDays(2);
            String compName = st.getCompany() != null ? st.getCompany().getCompanyName() : "Atirath Group";
            String sub = dataHelper.formatCount(st.getQuantity() != null ? st.getQuantity() : 0L) + " shares";

            entries.add(buildEntry(
                    "trf-" + st.getTransferId(),
                    "Confirm Share Transfer",
                    "Pending share transfer for " + compName + " requires completion.",
                    "Escrow",
                    compName,
                    sub,
                    due,
                    "Medium",
                    "Pending",
                    "pending",
                    "Verify",
                    "secondary",
                    "/escrow/share-transfer",
                    today
            ));
        }

        EscrowAccount wallet = escrowAccountRepository.findByUserAccount_UserId(userId).orElse(null);
        BigDecimal availableBalance = wallet != null && wallet.getAvailableBalance() != null
                ? wallet.getAvailableBalance()
                : BigDecimal.ZERO;
        if (kycCompleted && tradingEligible && activeEscrows.isEmpty() && availableBalance.compareTo(BigDecimal.ZERO) <= 0) {
            entries.add(buildEntry(
                    "esc-fund",
                    "Fund Escrow Account",
                    "Add funds to your escrow wallet before placing buy orders.",
                    "Escrow",
                    "Escrow Wallet",
                    dataHelper.formatInr(availableBalance),
                    today.plusDays(5),
                    "Normal",
                    "Pending",
                    "pending",
                    "Continue",
                    "secondary",
                    "/escrow/funds",
                    today
            ));
        }

        return entries;
    }

    private ActionEntry buildEntry(String id,
                                 String title,
                                 String desc,
                                 String category,
                                 String relatedTitle,
                                 String relatedSub,
                                 LocalDate dueLocalDate,
                                 String priority,
                                 String status,
                                 String statusTone,
                                 String actionText,
                                 String actionType,
                                 String route,
                                 LocalDate today) {
        long diff = ChronoUnit.DAYS.between(today, dueLocalDate);
        String daysLeft;
        boolean urgent;
        if (diff < 0) {
            daysLeft = Math.abs(diff) + (Math.abs(diff) == 1 ? " day overdue" : " days overdue");
            urgent = true;
        } else if (diff == 0) {
            daysLeft = "Today";
            urgent = true;
        } else if (diff == 1) {
            daysLeft = "1 day left";
            urgent = true;
        } else {
            daysLeft = diff + " days left";
            urgent = diff <= 2;
        }

        PendingActionRowDto dto = PendingActionRowDto.builder()
                .id(id)
                .title(title)
                .desc(desc)
                .category(category)
                .relatedTitle(relatedTitle)
                .relatedSub(relatedSub)
                .dueDateIso(dueLocalDate.toString())
                .dueDate(dueLocalDate.format(DUE_DATE_FMT))
                .daysLeft(daysLeft)
                .urgent(urgent)
                .priority(priority)
                .status(status)
                .statusTone(statusTone)
                .actionText(actionText)
                .actionType(actionType)
                .route(route)
                .build();
        return new ActionEntry(dto, dueLocalDate);
    }

    private Integer resolveAge(IdentityKycDetail kyc, LocalDate today) {
        if (kyc == null || kyc.getDateOfBirth() == null) {
            return null;
        }
        return Math.max(0, Period.between(kyc.getDateOfBirth(), today).getYears());
    }

    private String normalizeCategory(String category) {
        if (category == null || category.isBlank()) {
            return "All";
        }
        return switch (category.trim().toUpperCase(Locale.ROOT)) {
            case "KYC" -> "KYC";
            case "ORDERS" -> "Orders";
            case "NEGOTIATIONS" -> "Negotiations";
            case "ESCROW" -> "Escrow";
            case "ACCOUNT" -> "Account";
            default -> "All";
        };
    }

    private Comparator<ActionEntry> buildComparator(String sortBy) {
        if ("Date".equalsIgnoreCase(sortBy)) {
            return Comparator.comparing(ActionEntry::dueDate);
        }
        return Comparator
                .comparingInt((ActionEntry e) -> priorityWeight(e.row().getPriority()))
                .reversed()
                .thenComparing(ActionEntry::dueDate);
    }

    private int priorityWeight(String priority) {
        return switch (priority) {
            case "High" -> 3;
            case "Medium" -> 2;
            default -> 1;
        };
    }

    private record ActionEntry(PendingActionRowDto row, LocalDate dueDate) {}
}

