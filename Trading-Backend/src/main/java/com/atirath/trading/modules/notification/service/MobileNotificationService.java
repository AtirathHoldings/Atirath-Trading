package com.atirath.trading.modules.notification.service;

import com.atirath.trading.modules.auth.model.UserAccount;
import com.atirath.trading.modules.auth.repository.UserAccountRepository;
import com.atirath.trading.modules.market.model.AppNotification;
import com.atirath.trading.modules.market.model.MarketOrder;
import com.atirath.trading.modules.market.model.MarketTrade;
import com.atirath.trading.modules.market.repository.AppNotificationRepository;
import com.atirath.trading.modules.market.repository.MarketOrderRepository;
import com.atirath.trading.modules.market.repository.MarketTradeRepository;
import com.atirath.trading.modules.notification.dto.MemberNotificationsDto;
import com.atirath.trading.modules.notification.dto.MemberNotificationsDto.NotificationItemDto;
import com.atirath.trading.modules.notification.dto.mobile.MobileNotificationItemDto;
import com.atirath.trading.modules.notification.dto.mobile.MobileNotificationsPageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class MobileNotificationService {

    private final UserAccountRepository userAccountRepository;
    private final AppNotificationRepository appNotificationRepository;
    private final MarketOrderRepository orderRepository;
    private final MarketTradeRepository tradeRepository;
    private final MemberNotificationsService memberNotificationsService;

    private final Map<Long, String> idToRawKeyMap = new ConcurrentHashMap<>();

    @Transactional(readOnly = true)
    public MobileNotificationsPageDto getMobileNotifications(Long userId, String filter) {
        UserAccount user = null;
        if (userId != null) {
            user = userAccountRepository.findById(userId).orElse(null);
        }
        if (user == null) {
            user = userAccountRepository.findAll().stream().findFirst().orElse(null);
        }

        if (user == null) {
            return MobileNotificationsPageDto.builder()
                    .totalCount(0)
                    .unreadCount(0)
                    .tradeCount(0)
                    .systemCount(0)
                    .notifications(List.of())
                    .build();
        }

        Long uid = user.getUserId();
        MemberNotificationsDto memberNotes = memberNotificationsService.getMemberNotifications(uid, "All");
        List<MobileNotificationItemDto> items = new ArrayList<>();
        OffsetDateTime now = OffsetDateTime.now();

        if (memberNotes != null && memberNotes.getItems() != null) {
            for (NotificationItemDto item : memberNotes.getItems()) {
                String rawId = item.getId();
                Long numericId = toMobileId(rawId);
                String cat = item.getCategory() != null ? item.getCategory() : "System";
                String mobileType = mapToMobileType(cat);
                boolean isRead = !item.isUnread();

                MobileNotificationItemDto.MobileNotificationItemDtoBuilder itemBuilder = MobileNotificationItemDto.builder()
                        .id(numericId)
                        .type(mobileType)
                        .title(item.getTitle())
                        .body(item.getDesc())
                        .time(item.getTimeAgo() != null ? item.getTimeAgo() : "Just now")
                        .read(isRead)
                        .createdAt(now.toString());

                if (rawId != null && rawId.startsWith("ord-")) {
                    try {
                        Long orderId = Long.parseLong(rawId.substring(4));
                        itemBuilder.refId(orderId);
                        itemBuilder.refTyp("MARKET_ORDER");
                        orderRepository.findById(orderId).ifPresent(o -> {
                            Map<String, Object> details = new LinkedHashMap<>();
                            details.put("id", o.getOrderNo());
                            details.put("type", o.getOrderSide() != null ? o.getOrderSide().toLowerCase(Locale.ROOT) : "buy");
                            details.put("company", o.getCompany() != null ? o.getCompany().getCompanyName() : "Atirath Group");
                            details.put("symbol", o.getCompany() != null ? o.getCompany().getSymbol() : "AG");
                            details.put("qty", o.getQuantity());
                            details.put("price", o.getPricePerShare());
                            BigDecimal total = (o.getPricePerShare() != null && o.getQuantity() != null) ?
                                    o.getPricePerShare().multiply(BigDecimal.valueOf(o.getQuantity())) : BigDecimal.ZERO;
                            details.put("total", total);
                            details.put("orderType", o.getOrderType() != null ? o.getOrderType() : "Market");
                            details.put("date", o.getPlacedAt() != null ? o.getPlacedAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.ENGLISH)) : "Today");
                            details.put("status", o.getStatus() != null ? o.getStatus() : "Pending");
                            Map<String, Object> stock = new LinkedHashMap<>();
                            stock.put("symbol", o.getCompany() != null ? o.getCompany().getSymbol() : "AG");
                            stock.put("name", o.getCompany() != null ? o.getCompany().getCompanyName() : "Atirath Group");
                            stock.put("price", o.getPricePerShare());
                            details.put("stock", stock);
                            itemBuilder.details(details);
                        });
                    } catch (Exception e) {
                        log.debug("[MobileNotificationService] Could not parse orderId from {}: {}", rawId, e.getMessage());
                    }
                } else if (rawId != null && rawId.startsWith("trd-")) {
                    try {
                        Long tradeId = Long.parseLong(rawId.substring(4));
                        itemBuilder.refId(tradeId);
                        itemBuilder.refTyp("MARKET_TRADE");
                        tradeRepository.findById(tradeId).ifPresent(t -> {
                            Map<String, Object> details = new LinkedHashMap<>();
                            boolean isBuyer = t.getBuyerUser() != null && uid.equals(t.getBuyerUser().getUserId());
                            details.put("id", "ORD-" + t.getTradeNo());
                            details.put("tradeId", t.getTradeNo());
                            details.put("type", isBuyer ? "buy" : "sell");
                            details.put("company", t.getCompany() != null ? t.getCompany().getCompanyName() : "Atirath Group");
                            details.put("symbol", t.getCompany() != null ? t.getCompany().getSymbol() : "AG");
                            details.put("qty", t.getQuantity());
                            details.put("price", t.getTradePrice());
                            BigDecimal total = (t.getTradePrice() != null && t.getQuantity() != null) ?
                                    t.getTradePrice().multiply(BigDecimal.valueOf(t.getQuantity())) : BigDecimal.ZERO;
                            details.put("total", total);
                            details.put("orderType", "Market");
                            details.put("date", t.getExecutedAt() != null ? t.getExecutedAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.ENGLISH)) : "Today");
                            details.put("status", "Completed");
                            details.put("negotiationStatus", "Settled");
                            details.put("escrowStatus", "Released");
                            details.put("transferStatus", "Completed");
                            details.put("settlementStatus", "Completed (T+2)");
                            Map<String, Object> stock = new LinkedHashMap<>();
                            stock.put("symbol", t.getCompany() != null ? t.getCompany().getSymbol() : "AG");
                            stock.put("name", t.getCompany() != null ? t.getCompany().getCompanyName() : "Atirath Group");
                            stock.put("price", t.getTradePrice());
                            details.put("stock", stock);
                            itemBuilder.details(details);
                        });
                    } catch (Exception e) {
                        log.debug("[MobileNotificationService] Could not parse tradeId from {}: {}", rawId, e.getMessage());
                    }
                }

                items.add(itemBuilder.build());
            }
        }

        int totalCount = items.size();
        int unreadCount = (int) items.stream().filter(it -> !Boolean.TRUE.equals(it.getRead())).count();
        int tradeCount = (int) items.stream().filter(it -> isTradeRelated(it.getType())).count();
        int systemCount = (int) items.stream().filter(it -> !isTradeRelated(it.getType())).count();

        List<MobileNotificationItemDto> filtered = items;
        if (filter != null && !filter.isBlank() && !"all".equalsIgnoreCase(filter.trim())) {
            String f = filter.trim().toLowerCase(Locale.ROOT);
            if ("unread".equalsIgnoreCase(f)) {
                filtered = items.stream().filter(it -> !Boolean.TRUE.equals(it.getRead())).toList();
            } else if ("trade".equalsIgnoreCase(f)) {
                filtered = items.stream().filter(it -> isTradeRelated(it.getType())).toList();
            } else if ("system".equalsIgnoreCase(f)) {
                filtered = items.stream().filter(it -> !isTradeRelated(it.getType())).toList();
            } else {
                filtered = items.stream().filter(it -> f.equalsIgnoreCase(it.getType())).toList();
            }
        }

        return MobileNotificationsPageDto.builder()
                .totalCount(totalCount)
                .unreadCount(unreadCount)
                .tradeCount(tradeCount)
                .systemCount(systemCount)
                .notifications(filtered)
                .build();
    }

    @Transactional
    public boolean markAsRead(Long notificationId, Long userId) {
        if (notificationId == null) return false;
        if (userId == null) {
            UserAccount defaultUser = userAccountRepository.findAll().stream().findFirst().orElse(null);
            if (defaultUser != null) userId = defaultUser.getUserId();
        }
        if (userId == null) return false;

        String rawId = idToRawKeyMap.get(notificationId);
        if (rawId == null) {
            if (notificationId < 100000L) {
                rawId = "db-" + notificationId;
            } else if (notificationId < 200000L) {
                rawId = "ord-" + (notificationId - 100000L);
            } else if (notificationId < 300000L) {
                rawId = "trd-" + (notificationId - 200000L);
            } else if (notificationId < 400000L) {
                rawId = "neg-" + (notificationId - 300000L);
            } else if (notificationId < 500000L) {
                rawId = "esc-" + (notificationId - 400000L);
            } else if (notificationId < 600000L) {
                rawId = "trf-" + (notificationId - 500000L);
            } else if (notificationId < 700000L) {
                rawId = "ann-" + (notificationId - 600000L);
            } else if (notificationId == 900001L) {
                rawId = "kyc-pending-submit";
            } else if (notificationId == 900002L) {
                rawId = "kyc-under-review";
            } else if (notificationId == 900003L) {
                rawId = "kyc-verified";
            } else {
                rawId = "db-" + notificationId;
            }
        }

        try {
            memberNotificationsService.markItemAsRead(userId, rawId);
            return true;
        } catch (Exception e) {
            log.warn("[MobileNotificationService] Could not mark notification {} as read: {}", rawId, e.getMessage());
            return false;
        }
    }

    @Transactional
    public int markAllAsRead(Long userId) {
        if (userId == null) {
            UserAccount defaultUser = userAccountRepository.findAll().stream().findFirst().orElse(null);
            if (defaultUser != null) userId = defaultUser.getUserId();
        }
        if (userId == null) return 0;

        try {
            MemberNotificationsDto before = memberNotificationsService.getMemberNotifications(userId, "All");
            int count = before != null ? (int) before.getUnreadCount() : 0;
            memberNotificationsService.markAllAsRead(userId);
            return count;
        } catch (Exception e) {
            log.warn("[MobileNotificationService] Could not mark all notifications as read: {}", e.getMessage());
            return 0;
        }
    }

    private Long toMobileId(String rawId) {
        if (rawId == null || rawId.isBlank()) return 0L;
        Long numericId;
        if (rawId.startsWith("db-")) {
            try { numericId = Long.parseLong(rawId.substring(3)); }
            catch (Exception e) { numericId = (long) Math.abs(rawId.hashCode()); }
        } else if (rawId.startsWith("ord-")) {
            try { numericId = 100000L + Long.parseLong(rawId.substring(4)); }
            catch (Exception e) { numericId = (long) Math.abs(rawId.hashCode()); }
        } else if (rawId.startsWith("trd-")) {
            try { numericId = 200000L + Long.parseLong(rawId.substring(4)); }
            catch (Exception e) { numericId = (long) Math.abs(rawId.hashCode()); }
        } else if (rawId.startsWith("neg-")) {
            try { numericId = 300000L + Long.parseLong(rawId.substring(4)); }
            catch (Exception e) { numericId = (long) Math.abs(rawId.hashCode()); }
        } else if (rawId.startsWith("esc-")) {
            try { numericId = 400000L + Long.parseLong(rawId.substring(4)); }
            catch (Exception e) { numericId = (long) Math.abs(rawId.hashCode()); }
        } else if (rawId.startsWith("trf-")) {
            try { numericId = 500000L + Long.parseLong(rawId.substring(4)); }
            catch (Exception e) { numericId = (long) Math.abs(rawId.hashCode()); }
        } else if (rawId.startsWith("ann-")) {
            try { numericId = 600000L + Long.parseLong(rawId.substring(4)); }
            catch (Exception e) { numericId = (long) Math.abs(rawId.hashCode()); }
        } else if ("kyc-pending-submit".equals(rawId)) {
            numericId = 900001L;
        } else if ("kyc-under-review".equals(rawId)) {
            numericId = 900002L;
        } else if ("kyc-verified".equals(rawId)) {
            numericId = 900003L;
        } else {
            numericId = (long) Math.abs(rawId.hashCode());
        }
        idToRawKeyMap.put(numericId, rawId);
        return numericId;
    }

    private String mapToMobileType(String category) {
        if (category == null) return "system";
        String cat = category.toLowerCase(Locale.ROOT).trim();
        return switch (cat) {
            case "orders", "trade" -> "trade";
            case "negotiations" -> "negotiation";
            case "escrow" -> "escrow";
            case "portfolio" -> "portfolio";
            default -> "system";
        };
    }

    private boolean isTradeRelated(String type) {
        if (type == null) return false;
        String t = type.toLowerCase(Locale.ROOT);
        return "trade".equals(t) || "order".equals(t) || "negotiation".equals(t) || "escrow".equals(t);
    }
}
