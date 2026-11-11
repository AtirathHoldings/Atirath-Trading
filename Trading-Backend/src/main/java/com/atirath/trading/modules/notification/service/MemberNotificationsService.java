package com.atirath.trading.modules.notification.service;

import com.atirath.trading.common.AccountStatus;
import com.atirath.trading.common.ApiException;
import com.atirath.trading.common.ResourceNotFoundException;
import com.atirath.trading.modules.auth.model.IdentityKycDetail;
import com.atirath.trading.modules.auth.model.UserAccount;
import com.atirath.trading.modules.auth.repository.IdentityKycDetailRepository;
import com.atirath.trading.modules.auth.repository.UserAccountRepository;
import com.atirath.trading.modules.market.model.AppNotification;
import com.atirath.trading.modules.market.model.CompanyAnnouncement;
import com.atirath.trading.modules.market.model.Escrow;
import com.atirath.trading.modules.market.model.MarketOrder;
import com.atirath.trading.modules.market.model.MarketTrade;
import com.atirath.trading.modules.market.model.ShareTransfer;
import com.atirath.trading.modules.market.repository.AppNotificationRepository;
import com.atirath.trading.modules.market.repository.CompanyAnnouncementRepository;
import com.atirath.trading.modules.market.repository.EscrowRepository;
import com.atirath.trading.modules.market.repository.MarketOrderRepository;
import com.atirath.trading.modules.market.repository.MarketTradeRepository;
import com.atirath.trading.modules.market.repository.ShareTransferRepository;
import com.atirath.trading.modules.negotiation.model.Negotiation;
import com.atirath.trading.modules.negotiation.repository.NegotiationRepository;
import com.atirath.trading.modules.notification.dto.MemberNotificationsDto;
import com.atirath.trading.modules.notification.dto.MemberNotificationsDto.NotificationItemDto;
import com.atirath.trading.modules.notification.dto.MemberNotificationsDto.SummaryCountsDto;
import com.atirath.trading.modules.portfolio.service.PortfolioDataHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Read/write per-user service for the web Notifications screen (/notifications).
 * Combines real persisted AppNotifications with live domain events and persists
 * read/dismiss state per user in PostgreSQL without altering schema.
 */
@Service
@RequiredArgsConstructor
public class MemberNotificationsService {

    private static final String ACK_PREFIX = "ACK_";
    private static final String STATUS_READ = "READ";
    private static final String STATUS_UNREAD = "UNREAD";
    private static final String STATUS_DISMISSED = "DISMISSED";
    private static final DateTimeFormatter HEADER_DT_FMT =
            DateTimeFormatter.ofPattern("EEE, dd MMM yyyy | hh:mm a", Locale.ENGLISH);
    private static final DateTimeFormatter ITEM_TS_FMT =
            DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a", Locale.ENGLISH);
    private static final DateTimeFormatter SHORT_DATE_FMT =
            DateTimeFormatter.ofPattern("dd MMM", Locale.ENGLISH);

    private final PortfolioDataHelper dataHelper;
    private final UserAccountRepository userAccountRepository;
    private final IdentityKycDetailRepository identityKycDetailRepository;
    private final AppNotificationRepository appNotificationRepository;
    private final MarketOrderRepository orderRepository;
    private final MarketTradeRepository tradeRepository;
    private final NegotiationRepository negotiationRepository;
    private final EscrowRepository escrowRepository;
    private final ShareTransferRepository shareTransferRepository;
    private final CompanyAnnouncementRepository announcementRepository;

    @Transactional(readOnly = true)
    public MemberNotificationsDto getMemberNotifications(Long userId, String category) {
        if (userId == null) {
            throw new ApiException("Authentication required to load notifications", HttpStatus.UNAUTHORIZED);
        }

        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User account not found"));

        OffsetDateTime now = OffsetDateTime.now();
        String fullName = dataHelper.resolveUserFullName(user);
        String activeCategory = normalizeCategory(category);

        List<TimedNotification> all = buildUserNotifications(userId, user, now);

        long unreadCount = all.stream().filter(n -> n.dto().isUnread()).count();
        long tradeCount = 0;
        long ordersCount = 0;
        long negotiationsCount = 0;
        long escrowCount = 0;
        long accountCount = 0;
        long systemCount = 0;
        long companyUpdatesCount = 0;

        for (TimedNotification tn : all) {
            NotificationItemDto item = tn.dto();
            switch (item.getCategory()) {
                case "Trade" -> tradeCount++;
                case "Orders" -> ordersCount++;
                case "Negotiations" -> negotiationsCount++;
                case "Escrow" -> escrowCount++;
                case "Account" -> accountCount++;
                case "System" -> {
                    systemCount++;
                    if ("announcement".equals(item.getIconType())) {
                        companyUpdatesCount++;
                    }
                }
                default -> systemCount++;
            }
        }

        Map<String, Long> categoryCounts = new LinkedHashMap<>();
        categoryCounts.put("All", (long) all.size());
        categoryCounts.put("Unread", unreadCount);
        categoryCounts.put("Trade", tradeCount);
        categoryCounts.put("Orders", ordersCount);
        categoryCounts.put("Negotiations", negotiationsCount);
        categoryCounts.put("Escrow", escrowCount);
        categoryCounts.put("Account", accountCount);
        categoryCounts.put("System", systemCount);

        List<NotificationItemDto> filtered = all.stream()
                .map(TimedNotification::dto)
                .filter(dto -> switch (activeCategory) {
                    case "All" -> true;
                    case "Unread" -> dto.isUnread();
                    default -> dto.getCategory().equalsIgnoreCase(activeCategory);
                })
                .toList();

        SummaryCountsDto summary = SummaryCountsDto.builder()
                .totalNotifications(all.size())
                .unread(unreadCount)
                .tradeRelated(tradeCount)
                .orderUpdates(ordersCount)
                .negotiations(negotiationsCount)
                .escrow(escrowCount)
                .companyUpdates(companyUpdatesCount)
                .system(systemCount)
                .build();

        return MemberNotificationsDto.builder()
                .userId(userId)
                .memberId("ATG" + userId)
                .userName(fullName)
                .asOfFormatted(now.format(HEADER_DT_FMT))
                .activeCategory(activeCategory)
                .totalCount(all.size())
                .unreadCount(unreadCount)
                .categoryCounts(categoryCounts)
                .summary(summary)
                .items(filtered)
                .build();
    }

    @Transactional
    public void markItemAsRead(Long userId, String itemId) {
        if (userId == null) {
            throw new ApiException("Authentication required to update notifications", HttpStatus.UNAUTHORIZED);
        }
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User account not found"));

        if (itemId == null || itemId.isBlank()) {
            throw new ApiException("Invalid notification identifier", HttpStatus.BAD_REQUEST);
        }

        if (itemId.startsWith("db-")) {
            Long noteId = parseDbId(itemId);
            AppNotification note = appNotificationRepository.findById(noteId)
                    .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
            verifyOwnership(note, userId);
            if (!STATUS_DISMISSED.equalsIgnoreCase(note.getStatus())) {
                note.setStatus(STATUS_READ);
                note.setReadAt(OffsetDateTime.now());
                appNotificationRepository.save(note);
            }
            return;
        }

        upsertSyntheticMarker(user, itemId, STATUS_READ);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        if (userId == null) {
            throw new ApiException("Authentication required to update notifications", HttpStatus.UNAUTHORIZED);
        }
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User account not found"));

        OffsetDateTime now = OffsetDateTime.now();
        List<AppNotification> existing = appNotificationRepository.findByUserAccount_UserIdOrderByCreatedAtDesc(userId);
        List<AppNotification> toUpdate = new ArrayList<>();
        Set<String> ackedKeys = new HashSet<>();

        for (AppNotification n : existing) {
            if (n.getRefTyp() != null && n.getRefTyp().startsWith(ACK_PREFIX)) {
                ackedKeys.add(n.getRefTyp().substring(ACK_PREFIX.length()));
                if (STATUS_UNREAD.equalsIgnoreCase(n.getStatus())) {
                    n.setStatus(STATUS_READ);
                    n.setReadAt(now);
                    toUpdate.add(n);
                }
            } else if (STATUS_UNREAD.equalsIgnoreCase(n.getStatus())) {
                n.setStatus(STATUS_READ);
                n.setReadAt(now);
                toUpdate.add(n);
            }
        }
        if (!toUpdate.isEmpty()) {
            appNotificationRepository.saveAll(toUpdate);
        }

        List<TimedNotification> currentItems = buildUserNotifications(userId, user, now);
        List<AppNotification> newMarkers = new ArrayList<>();
        for (TimedNotification tn : currentItems) {
            NotificationItemDto item = tn.dto();
            if (!item.isPersisted() && item.isUnread() && ackedKeys.add(item.getId())) {
                newMarkers.add(AppNotification.builder()
                        .userAccount(user)
                        .refTyp(truncateRefTyp(ACK_PREFIX + item.getId()))
                        .title(item.getTitle())
                        .message("Read acknowledgement")
                        .notificationType("ACK")
                        .modeOfNotification("IN_APP")
                        .status(STATUS_READ)
                        .readAt(now)
                        .build());
            }
        }
        if (!newMarkers.isEmpty()) {
            appNotificationRepository.saveAll(newMarkers);
        }
    }

    @Transactional
    public void dismissItem(Long userId, String itemId) {
        if (userId == null) {
            throw new ApiException("Authentication required to remove notification", HttpStatus.UNAUTHORIZED);
        }
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User account not found"));

        if (itemId == null || itemId.isBlank()) {
            throw new ApiException("Invalid notification identifier", HttpStatus.BAD_REQUEST);
        }

        if (itemId.startsWith("db-")) {
            Long noteId = parseDbId(itemId);
            AppNotification note = appNotificationRepository.findById(noteId)
                    .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
            verifyOwnership(note, userId);
            note.setStatus(STATUS_DISMISSED);
            appNotificationRepository.save(note);
            return;
        }

        upsertSyntheticMarker(user, itemId, STATUS_DISMISSED);
    }

    private List<TimedNotification> buildUserNotifications(Long userId, UserAccount user, OffsetDateTime now) {
        List<TimedNotification> items = new ArrayList<>();
        Set<String> ackedKeys = new HashSet<>();
        Set<String> dismissedKeys = new HashSet<>();
        Set<Long> coveredOrderIds = new HashSet<>();

        List<AppNotification> dbNotes = appNotificationRepository.findByUserAccount_UserIdOrderByCreatedAtDesc(userId);
        for (AppNotification note : dbNotes) {
            String refTyp = note.getRefTyp();
            if (refTyp != null && refTyp.startsWith(ACK_PREFIX)) {
                String key = refTyp.substring(ACK_PREFIX.length());
                if (STATUS_DISMISSED.equalsIgnoreCase(note.getStatus())) {
                    dismissedKeys.add(key);
                    ackedKeys.add(key);
                } else {
                    ackedKeys.add(key);
                }
                continue;
            }

            if ("MARKET_ORDER".equalsIgnoreCase(refTyp) && note.getRefId() != null) {
                coveredOrderIds.add(note.getRefId());
            }

            if (STATUS_DISMISSED.equalsIgnoreCase(note.getStatus())) {
                continue;
            }

            String category = mapNotificationCategory(note.getNotificationType(), refTyp);
            OffsetDateTime created = note.getCreatedAt() != null ? note.getCreatedAt() : now;
            boolean unread = STATUS_UNREAD.equalsIgnoreCase(note.getStatus());

            items.add(new TimedNotification(created, NotificationItemDto.builder()
                    .id("db-" + note.getNotificationId())
                    .title(note.getTitle())
                    .desc(note.getMessage())
                    .category(category)
                    .timeAgo(formatTimeAgo(created, now))
                    .timestampFormatted(created.format(ITEM_TS_FMT))
                    .unread(unread)
                    .iconType(mapIconForCategory(category, note.getTitle()))
                    .route(mapRouteForCategory(category))
                    .persisted(true)
                    .build()));
        }

        // 2. Real Orders not yet represented by an AppNotification row
        List<MarketOrder> orders = orderRepository.findByUserAccount_UserIdOrderByPlacedAtDesc(userId);
        for (MarketOrder o : orders.stream().limit(8).toList()) {
            if (o.getOrderId() != null && coveredOrderIds.contains(o.getOrderId())) {
                continue;
            }
            String key = "ord-" + o.getOrderId();
            if (dismissedKeys.contains(key)) continue;

            boolean isBuy = "BUY".equalsIgnoreCase(o.getOrderSide());
            String rawStatus = o.getStatus() != null ? o.getStatus().toUpperCase(Locale.ROOT) : "PENDING";
            String statusLabel = formatOrderStatusLabel(rawStatus);
            OffsetDateTime dt = o.getPlacedAt() != null ? o.getPlacedAt() : now;
            String compName = o.getCompany() != null ? o.getCompany().getCompanyName() : "Atirath Group";
            String desc = String.format("Your %s order %s for %s shares of %s at %s is %s.",
                    isBuy ? "buy" : "sell",
                    o.getOrderNo(),
                    dataHelper.formatCount(o.getQuantity() != null ? o.getQuantity() : 0L),
                    compName,
                    dataHelper.formatInr(o.getPricePerShare()),
                    statusLabel.toLowerCase(Locale.ENGLISH));

            boolean activeOrder = Set.of("PENDING", "OPEN", "IN_ESCROW", "PARTIALLY_FILLED").contains(rawStatus);
            boolean unread = activeOrder && !ackedKeys.contains(key);
            String iconType = ("CANCELLED".equals(rawStatus) || "REJECTED".equals(rawStatus)) ? "trade-down" : "order";

            items.add(new TimedNotification(dt, NotificationItemDto.builder()
                    .id(key)
                    .title((isBuy ? "Buy" : "Sell") + " Order " + statusLabel)
                    .desc(desc)
                    .category("Orders")
                    .timeAgo(formatTimeAgo(dt, now))
                    .timestampFormatted(dt.format(ITEM_TS_FMT))
                    .unread(unread)
                    .iconType(iconType)
                    .route("/orders")
                    .persisted(false)
                    .build()));
        }

        // 3. Real Executed Trades
        List<MarketTrade> trades = tradeRepository.findUserTrades(userId);
        for (MarketTrade t : trades.stream().limit(8).toList()) {
            String key = "trd-" + t.getTradeId();
            if (dismissedKeys.contains(key)) continue;

            boolean isBuyer = t.getBuyerUser() != null && userId.equals(t.getBuyerUser().getUserId());
            OffsetDateTime dt = t.getExecutedAt() != null ? t.getExecutedAt() : now;
            String compName = t.getCompany() != null ? t.getCompany().getCompanyName() : "Atirath Group";
            String desc = String.format("%s %s shares of %s at %s (Trade %s).",
                    isBuyer ? "Bought" : "Sold",
                    dataHelper.formatCount(t.getQuantity() != null ? t.getQuantity() : 0L),
                    compName,
                    dataHelper.formatInr(t.getTradePrice()),
                    t.getTradeNo());

            boolean isRecent = Duration.between(dt, now).toHours() <= 24;
            boolean unread = isRecent && !ackedKeys.contains(key);

            items.add(new TimedNotification(dt, NotificationItemDto.builder()
                    .id(key)
                    .title(isBuyer ? "Buy Trade Executed" : "Sell Trade Executed")
                    .desc(desc)
                    .category("Trade")
                    .timeAgo(formatTimeAgo(dt, now))
                    .timestampFormatted(dt.format(ITEM_TS_FMT))
                    .unread(unread)
                    .iconType(isBuyer ? "trade-up" : "trade-down")
                    .route("/transactions")
                    .persisted(false)
                    .build()));
        }

        // 4. Negotiations
        List<Negotiation> negotiations = negotiationRepository.findAllForUser(userId);
        for (Negotiation n : negotiations.stream().limit(6).toList()) {
            String key = "neg-" + n.getNegotiationId();
            if (dismissedKeys.contains(key)) continue;

            OffsetDateTime dt = n.getUpdatedAt() != null ? n.getUpdatedAt() : (n.getCreatedAt() != null ? n.getCreatedAt() : now);
            String compName = n.getCompany() != null ? n.getCompany().getCompanyName() : "Atirath Group";
            String rawStatus = n.getStatus() != null ? n.getStatus().toUpperCase(Locale.ROOT) : "OPEN";
            boolean active = Set.of("OPEN", "COUNTERED", "PENDING", "IN_PROGRESS", "AWAITING_RESPONSE").contains(rawStatus);

            items.add(new TimedNotification(dt, NotificationItemDto.builder()
                    .id(key)
                    .title("Negotiation Update")
                    .desc(String.format("Negotiation %s for %s shares of %s at %s is %s.",
                            n.getNegotiationNo(),
                            dataHelper.formatCount(n.getQuantity() != null ? n.getQuantity() : 0L),
                            compName,
                            dataHelper.formatInr(n.getProposedPrice()),
                            rawStatus.toLowerCase(Locale.ENGLISH)))
                    .category("Negotiations")
                    .timeAgo(formatTimeAgo(dt, now))
                    .timestampFormatted(dt.format(ITEM_TS_FMT))
                    .unread(active && !ackedKeys.contains(key))
                    .iconType("negotiation")
                    .route("/negotiations")
                    .persisted(false)
                    .build()));
        }

        // 5. Escrow Records
        List<Escrow> escrows = escrowRepository.findUserEscrows(userId);
        for (Escrow esc : escrows.stream().limit(6).toList()) {
            String key = "esc-" + esc.getEscrowId();
            if (dismissedKeys.contains(key)) continue;

            OffsetDateTime dt = esc.getReleasedAt() != null ? esc.getReleasedAt() : (esc.getCreatedAt() != null ? esc.getCreatedAt() : now);
            String rawStatus = esc.getEscrowStatus() != null ? esc.getEscrowStatus().toUpperCase(Locale.ROOT) : "CREATED";
            boolean active = Set.of("CREATED", "FUNDS_BLOCKED", "PENDING", "IN_PROGRESS", "AWAITING_RELEASE").contains(rawStatus);

            items.add(new TimedNotification(dt, NotificationItemDto.builder()
                    .id(key)
                    .title("RELEASED".equals(rawStatus) ? "Escrow Released" : "Escrow Initiated")
                    .desc(String.format("%s escrow settlement (Ref: %s) status: %s.",
                            dataHelper.formatInr(esc.getAmount()),
                            esc.getEscrowReference(),
                            rawStatus.replace('_', ' ').toLowerCase(Locale.ENGLISH)))
                    .category("Escrow")
                    .timeAgo(formatTimeAgo(dt, now))
                    .timestampFormatted(dt.format(ITEM_TS_FMT))
                    .unread(active && !ackedKeys.contains(key))
                    .iconType("escrow")
                    .route("/escrow")
                    .persisted(false)
                    .build()));
        }

        // 6. Share Transfers
        Set<Long> seenTransferIds = new HashSet<>();
        List<ShareTransfer> transfers = new ArrayList<>();
        transfers.addAll(shareTransferRepository.findByToUser_UserIdOrderByTransferredAtDesc(userId));
        transfers.addAll(shareTransferRepository.findByFromUser_UserIdOrderByTransferredAtDesc(userId));

        for (ShareTransfer st : transfers.stream().limit(6).toList()) {
            if (!seenTransferIds.add(st.getTransferId())) continue;
            String key = "trf-" + st.getTransferId();
            if (dismissedKeys.contains(key)) continue;

            boolean incoming = st.getToUser() != null && userId.equals(st.getToUser().getUserId());
            OffsetDateTime dt = st.getTransferredAt() != null ? st.getTransferredAt() : now;
            String compName = st.getCompany() != null ? st.getCompany().getCompanyName() : "Atirath Group";
            String status = st.getTransferStatus() != null ? st.getTransferStatus().toUpperCase(Locale.ROOT) : "PENDING";

            items.add(new TimedNotification(dt, NotificationItemDto.builder()
                    .id(key)
                    .title("COMPLETED".equals(status) ? "Share Transfer Completed" : "Share Transfer " + status)
                    .desc(String.format("%s shares of %s %s.",
                            dataHelper.formatCount(st.getQuantity() != null ? st.getQuantity() : 0L),
                            compName,
                            incoming ? "transferred to your account" : "transferred from your account"))
                    .category("Trade")
                    .timeAgo(formatTimeAgo(dt, now))
                    .timestampFormatted(dt.format(ITEM_TS_FMT))
                    .unread("PENDING".equals(status) && !ackedKeys.contains(key))
                    .iconType("transfer")
                    .route("/escrow/share-transfer")
                    .persisted(false)
                    .build()));
        }

        // 7. Account & KYC Status
        Optional<IdentityKycDetail> kycOpt = identityKycDetailRepository.findByUserAccount_UserId(userId);
        OffsetDateTime acctDt = user.getUpdatedAt() != null ? user.getUpdatedAt() : (user.getCreatedAt() != null ? user.getCreatedAt() : now);
        if (kycOpt.isEmpty()) {
            String key = "kyc-pending-submit";
            if (!dismissedKeys.contains(key)) {
                items.add(new TimedNotification(acctDt, NotificationItemDto.builder()
                        .id(key)
                        .title("Complete KYC Verification")
                        .desc("Submit your PAN, Aadhaar, and DOB details to unlock trading.")
                        .category("Account")
                        .timeAgo(formatTimeAgo(acctDt, now))
                        .timestampFormatted(acctDt.format(ITEM_TS_FMT))
                        .unread(!ackedKeys.contains(key))
                        .iconType("kyc")
                        .route("/register-step3")
                        .persisted(false)
                        .build()));
            }
        } else if (user.getAccountStatus() == AccountStatus.PENDING) {
            String key = "kyc-under-review";
            if (!dismissedKeys.contains(key)) {
                items.add(new TimedNotification(acctDt, NotificationItemDto.builder()
                        .id(key)
                        .title("KYC Under Review")
                        .desc("Your KYC documents have been submitted and are currently under review.")
                        .category("Account")
                        .timeAgo(formatTimeAgo(acctDt, now))
                        .timestampFormatted(acctDt.format(ITEM_TS_FMT))
                        .unread(!ackedKeys.contains(key))
                        .iconType("kyc")
                        .route("/kyc")
                        .persisted(false)
                        .build()));
            }
        } else if (user.getAccountStatus() == AccountStatus.ACTIVE) {
            String key = "kyc-verified";
            if (!dismissedKeys.contains(key)) {
                items.add(new TimedNotification(acctDt, NotificationItemDto.builder()
                        .id(key)
                        .title("Account & KYC Verified")
                        .desc("Your member profile and KYC verification are active.")
                        .category("Account")
                        .timeAgo(formatTimeAgo(acctDt, now))
                        .timestampFormatted(acctDt.format(ITEM_TS_FMT))
                        .unread(false)
                        .iconType("kyc")
                        .route("/kyc")
                        .persisted(false)
                        .build()));
            }
        }

        // 8. Published Company Announcements
        List<CompanyAnnouncement> announcements = announcementRepository.findTop5ByStatusOrderByAnnouncementDateDesc("PUBLISHED");
        for (CompanyAnnouncement ca : announcements) {
            String key = "ann-" + ca.getAnnouncementId();
            if (dismissedKeys.contains(key)) continue;

            LocalDate annDate = ca.getAnnouncementDate() != null ? ca.getAnnouncementDate() : now.toLocalDate();
            OffsetDateTime dt = annDate.atTime(9, 0).atOffset(ZoneOffset.UTC);
            String compName = ca.getCompany() != null ? ca.getCompany().getCompanyName() : "Atirath Group";
            String desc = (ca.getDescription() != null && !ca.getDescription().isBlank())
                    ? ca.getDescription()
                    : compName + " has published a corporate announcement.";
            boolean isRecent = !annDate.isBefore(now.toLocalDate().minusDays(14));

            items.add(new TimedNotification(dt, NotificationItemDto.builder()
                    .id(key)
                    .title(ca.getTitle())
                    .desc(compName + ": " + desc)
                    .category("System")
                    .timeAgo(formatTimeAgo(dt, now))
                    .timestampFormatted(annDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH)))
                    .unread(isRecent && !ackedKeys.contains(key))
                    .iconType("announcement")
                    .route("/group-companies")
                    .persisted(false)
                    .build()));
        }

        items.sort(Comparator.comparing(TimedNotification::timestamp).reversed());
        return items;
    }

    private void upsertSyntheticMarker(UserAccount user, String itemId, String targetStatus) {
        String markerRefTyp = truncateRefTyp(ACK_PREFIX + itemId);
        List<AppNotification> existing = appNotificationRepository.findByUserAccount_UserIdOrderByCreatedAtDesc(user.getUserId());
        AppNotification found = existing.stream()
                .filter(n -> markerRefTyp.equals(n.getRefTyp()))
                .findFirst()
                .orElse(null);

        if (found != null) {
            found.setStatus(targetStatus);
            if (STATUS_READ.equals(targetStatus)) {
                found.setReadAt(OffsetDateTime.now());
            }
            appNotificationRepository.save(found);
        } else {
            appNotificationRepository.save(AppNotification.builder()
                    .userAccount(user)
                    .refTyp(markerRefTyp)
                    .title("Notification " + targetStatus.toLowerCase(Locale.ENGLISH))
                    .message("User marker for " + itemId)
                    .notificationType("ACK")
                    .modeOfNotification("IN_APP")
                    .status(targetStatus)
                    .readAt(STATUS_READ.equals(targetStatus) ? OffsetDateTime.now() : null)
                    .build());
        }
    }

    private Long parseDbId(String itemId) {
        try {
            return Long.parseLong(itemId.substring(3));
        } catch (NumberFormatException ex) {
            throw new ApiException("Invalid notification ID format", HttpStatus.BAD_REQUEST);
        }
    }

    private void verifyOwnership(AppNotification note, Long userId) {
        if (note.getUserAccount() == null || !userId.equals(note.getUserAccount().getUserId())) {
            throw new ApiException("You do not have permission to access this notification", HttpStatus.FORBIDDEN);
        }
    }

    private String truncateRefTyp(String value) {
        return value.length() > 50 ? value.substring(0, 50) : value;
    }

    private String normalizeCategory(String raw) {
        if (raw == null || raw.isBlank()) return "All";
        return switch (raw.trim().toUpperCase(Locale.ROOT)) {
            case "UNREAD" -> "Unread";
            case "TRADE" -> "Trade";
            case "ORDERS" -> "Orders";
            case "NEGOTIATIONS" -> "Negotiations";
            case "ESCROW" -> "Escrow";
            case "ACCOUNT" -> "Account";
            case "SYSTEM" -> "System";
            default -> "All";
        };
    }

    private String mapNotificationCategory(String notificationType, String refTyp) {
        String combined = ((notificationType != null ? notificationType : "") + " "
                + (refTyp != null ? refTyp : "")).toUpperCase(Locale.ROOT);
        if (combined.contains("ORDER")) return "Orders";
        if (combined.contains("NEGOTIATION")) return "Negotiations";
        if (combined.contains("ESCROW")) return "Escrow";
        if (combined.contains("TRADE") || combined.contains("TRANSFER")) return "Trade";
        if (combined.contains("KYC") || combined.contains("ACCOUNT") || combined.contains("BANK")) return "Account";
        return "System";
    }

    private String mapIconForCategory(String category, String title) {
        return switch (category) {
            case "Orders" -> "order";
            case "Negotiations" -> "negotiation";
            case "Escrow" -> "escrow";
            case "Account" -> "kyc";
            case "Trade" -> (title != null && title.toLowerCase(Locale.ENGLISH).contains("sell")) ? "trade-down" : "trade-up";
            default -> "system";
        };
    }

    private String mapRouteForCategory(String category) {
        return switch (category) {
            case "Orders" -> "/orders";
            case "Negotiations" -> "/negotiations";
            case "Escrow" -> "/escrow";
            case "Account" -> "/kyc";
            case "Trade" -> "/transactions";
            default -> "/group-companies";
        };
    }

    private String formatOrderStatusLabel(String rawStatus) {
        return switch (rawStatus) {
            case "EXECUTED", "COMPLETED" -> "Executed";
            case "IN_ESCROW" -> "In Escrow";
            case "PARTIALLY_FILLED" -> "Partially Filled";
            case "CANCELLED" -> "Cancelled";
            case "REJECTED" -> "Rejected";
            case "OPEN" -> "Open";
            default -> "Pending";
        };
    }

    private String formatTimeAgo(OffsetDateTime dt, OffsetDateTime now) {
        long minutes = Math.max(0, Duration.between(dt, now).toMinutes());
        if (minutes < 1) return "Just now";
        if (minutes < 60) return minutes + "m ago";
        long hours = minutes / 60;
        if (hours < 24) return hours + "h ago";
        long days = hours / 24;
        if (days <= 30) return days + "d ago";
        return dt.format(SHORT_DATE_FMT);
    }

    private record TimedNotification(OffsetDateTime timestamp, NotificationItemDto dto) {}
}

