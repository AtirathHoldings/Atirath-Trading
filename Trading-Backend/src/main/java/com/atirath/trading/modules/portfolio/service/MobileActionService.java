package com.atirath.trading.modules.portfolio.service;

import com.atirath.trading.modules.auth.model.UserAccount;
import com.atirath.trading.modules.auth.repository.BankDetailRepository;
import com.atirath.trading.modules.auth.repository.IdentityKycDetailRepository;
import com.atirath.trading.modules.auth.repository.UserAccountRepository;
import com.atirath.trading.modules.market.model.EscrowAccount;
import com.atirath.trading.modules.market.model.MarketOrder;
import com.atirath.trading.modules.market.repository.AppNotificationRepository;
import com.atirath.trading.modules.market.repository.EscrowAccountRepository;
import com.atirath.trading.modules.market.repository.MarketOrderRepository;
import com.atirath.trading.modules.portfolio.dto.mobile.MobileActionCardDto;
import com.atirath.trading.modules.portfolio.dto.mobile.MobileActionsPageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class MobileActionService {

    private static final Set<String> ACTIVE_ORDER_STATUSES =
            Set.of("PENDING", "OPEN", "IN_ESCROW", "PARTIALLY_FILLED");

    private final UserAccountRepository userAccountRepository;
    private final IdentityKycDetailRepository identityKycDetailRepository;
    private final BankDetailRepository bankDetailRepository;
    private final MarketOrderRepository orderRepository;
    private final EscrowAccountRepository escrowAccountRepository;
    private final AppNotificationRepository appNotificationRepository;

    @Transactional(readOnly = true)
    public MobileActionsPageDto getMobileActions(Long userId) {
        UserAccount user = null;
        if (userId != null) {
            user = userAccountRepository.findById(userId).orElse(null);
        }

        if (user == null) {
            return MobileActionsPageDto.builder()
                    .totalCount(0)
                    .bannerText("No actions required.")
                    .bannerType("INFO")
                    .actions(List.of())
                    .build();
        }

        Long uid = user.getUserId();
        List<MobileActionCardDto> actions = new ArrayList<>();

        // 1. KYC verification
        boolean kycDone = identityKycDetailRepository.findByUserAccount_UserId(uid).isPresent();
        if (!kycDone) {
            actions.add(MobileActionCardDto.builder()
                    .id("ACT_KYC_1")
                    .iconType("shield")
                    .title("Complete KYC Verification")
                    .subtitle("Submit your identity proof (PAN, Aadhaar) to unlock full trading privileges.")
                    .urgency("HIGH")
                    .urgencyColor("RED")
                    .actionLabel("Complete KYC")
                    .targetRoute("KYC")
                    .build());
        }

        // 2. Bank Details
        boolean bankDone = bankDetailRepository.findByUserAccount_UserId(uid).isPresent();
        if (!bankDone) {
            actions.add(MobileActionCardDto.builder()
                    .id("ACT_BANK_1")
                    .iconType("bank")
                    .title("Update Bank Details")
                    .subtitle("Link a verified bank account for trade settlements, payouts and refunds.")
                    .urgency("NORMAL")
                    .urgencyColor("BLUE")
                    .actionLabel("Add Bank")
                    .targetRoute("KYC")
                    .build());
        }

        // 3. Escrow requirement
        EscrowAccount escrow = escrowAccountRepository.findByUserAccount_UserId(uid).orElse(null);
        if (escrow != null && escrow.getBlockedBalance() != null && escrow.getBlockedBalance().compareTo(BigDecimal.ZERO) > 0) {
            actions.add(MobileActionCardDto.builder()
                    .id("ACT_ESCROW_1")
                    .iconType("bank")
                    .title("Escrow Funds in Verification")
                    .subtitle("Funds currently held in escrow for pending buy/sell transactions.")
                    .urgency("NORMAL")
                    .urgencyColor("AMBER")
                    .actionLabel("View Escrow")
                    .targetRoute("ESCROW")
                    .build());
        }

        // 4. Open Orders
        List<MarketOrder> orders = orderRepository.findByUserAccount_UserIdOrderByPlacedAtDesc(uid);
        long openOrdersCount = orders.stream()
                .filter(o -> o.getStatus() != null && ACTIVE_ORDER_STATUSES.contains(o.getStatus().toUpperCase(Locale.ROOT)))
                .count();
        if (openOrdersCount > 0) {
            actions.add(MobileActionCardDto.builder()
                    .id("ACT_ORDERS_1")
                    .iconType("order")
                    .title(openOrdersCount + (openOrdersCount == 1 ? " Active Order in Progress" : " Active Orders in Progress"))
                    .subtitle("Review your open market orders and execution status.")
                    .urgency("NORMAL")
                    .urgencyColor("BLUE")
                    .actionLabel("View Orders")
                    .targetRoute("TRADE")
                    .build());
        }

        // 5. Unread Alerts
        long unreadAlerts = appNotificationRepository.countByUserAccount_UserIdAndStatus(uid, "UNREAD");
        if (unreadAlerts > 0) {
            actions.add(MobileActionCardDto.builder()
                    .id("ACT_NOTIF_1")
                    .iconType("alert")
                    .title(unreadAlerts + (unreadAlerts == 1 ? " Unread Notification" : " Unread Notifications"))
                    .subtitle("Important market or transaction updates waiting for your review.")
                    .urgency("LOW")
                    .urgencyColor("BLUE")
                    .actionLabel("View Alerts")
                    .targetRoute("NOTIFICATIONS")
                    .build());
        }

        String bannerText;
        String bannerType;
        if (actions.isEmpty()) {
            bannerText = "Your account is in good standing. No pending actions require your immediate attention.";
            bannerType = "INFO";
        } else {
            bannerText = actions.size() + " actions require your attention to ensure smooth trade settlement.";
            bannerType = "WARNING";
        }

        return MobileActionsPageDto.builder()
                .totalCount(actions.size())
                .bannerText(bannerText)
                .bannerType(bannerType)
                .actions(actions)
                .build();
    }
}
