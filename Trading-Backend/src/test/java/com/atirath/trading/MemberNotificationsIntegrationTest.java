package com.atirath.trading;

import com.atirath.trading.common.AccountStatus;
import com.atirath.trading.common.AccountType;
import com.atirath.trading.config.JwtTokenProvider;
import com.atirath.trading.modules.auth.model.IndividualProfile;
import com.atirath.trading.modules.auth.model.UserAccount;
import com.atirath.trading.modules.auth.repository.IndividualProfileRepository;
import com.atirath.trading.modules.auth.repository.UserAccountRepository;
import com.atirath.trading.modules.market.model.AppNotification;
import com.atirath.trading.modules.market.model.MarketCompany;
import com.atirath.trading.modules.market.repository.AppNotificationRepository;
import com.atirath.trading.modules.market.repository.MarketCompanyRepository;
import com.atirath.trading.modules.negotiation.model.Negotiation;
import com.atirath.trading.modules.negotiation.repository.NegotiationRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MemberNotificationsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private IndividualProfileRepository individualProfileRepository;

    @Autowired
    private AppNotificationRepository appNotificationRepository;

    @Autowired
    private MarketCompanyRepository companyRepository;

    @Autowired
    private NegotiationRepository negotiationRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("Member notifications endpoints reject unauthenticated access with 401")
    void shouldRejectUnauthenticatedRequests() throws Exception {
        mockMvc.perform(get("/api/v1/notifications/member"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(patch("/api/v1/notifications/member/db-1/read"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/v1/notifications/member/mark-all-read"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(delete("/api/v1/notifications/member/db-1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Member notifications aggregate real DB alerts, support mark-read/mark-all/dismiss, and isolate users")
    void shouldAggregateFilterMutateAndIsolateNotifications() throws Exception {
        long suffix = System.nanoTime() % 1_000_000_000L;
        UserAccount userA = userAccountRepository.save(UserAccount.builder()
                .accountType(AccountType.INDIVIDUAL)
                .email("ananya.notif." + suffix + "@test.com")
                .mobileNumber("7" + String.format("%09d", suffix))
                .accountStatus(AccountStatus.PENDING)
                .build());

        individualProfileRepository.save(IndividualProfile.builder()
                .userAccount(userA)
                .fullName("Ananya Notif")
                .build());

        long suffixB = (System.nanoTime() + 41) % 1_000_000_000L;
        UserAccount userB = userAccountRepository.save(UserAccount.builder()
                .accountType(AccountType.INDIVIDUAL)
                .email("other.notif." + suffixB + "@test.com")
                .mobileNumber("8" + String.format("%09d", suffixB))
                .accountStatus(AccountStatus.ACTIVE)
                .build());

        AppNotification dbNote = appNotificationRepository.save(AppNotification.builder()
                .userAccount(userA)
                .refId(991L)
                .refTyp("MARKET_ORDER")
                .title("Order Placed Successfully!")
                .message("Your BUY order ORD-NOTIF-1 has been submitted.")
                .notificationType("ORDER_UPDATE")
                .modeOfNotification("IN_APP")
                .status("UNREAD")
                .build());

        List<MarketCompany> companies = companyRepository.findByStatusOrderByCompanyNameAsc("ACTIVE");
        MarketCompany comp = companies.stream().findFirst().orElseThrow();

        negotiationRepository.save(Negotiation.builder()
                .negotiationNo("NEG-NOTIF-" + suffix)
                .company(comp)
                .initiatorUser(userB)
                .counterpartyUser(userA)
                .quantity(250L)
                .proposedPrice(new BigDecimal("210.00"))
                .status("COUNTERED")
                .expiresAt(OffsetDateTime.now().plusDays(2))
                .build());

        entityManager.flush();

        String tokenA = jwtTokenProvider.generateAccessToken(userA);
        String tokenB = jwtTokenProvider.generateAccessToken(userB);
        String dbItemId = "db-" + dbNote.getNotificationId();

        // 1. User A fetches real notifications without hardcoded mock items
        mockMvc.perform(get("/api/v1/notifications/member")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.unreadCount", greaterThanOrEqualTo(3)))
                .andExpect(jsonPath("$.data.items[*].id", hasItem(dbItemId)))
                .andExpect(jsonPath("$.data.items[*].id", hasItem("kyc-pending-submit")))
                .andExpect(jsonPath("$.data.items[*].desc", not(hasItem("₹ 58,500 has been moved to escrow for share transfer (Ref: ATG-E-250915-003)."))));

        // 2. Category filtering works
        mockMvc.perform(get("/api/v1/notifications/member")
                        .param("category", "Orders")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].id").value(dbItemId));

        // 3. Cross-user access control: User B cannot mark/delete User A's notification
        mockMvc.perform(patch("/api/v1/notifications/member/" + dbItemId + "/read")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/v1/notifications/member/" + dbItemId)
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isForbidden());

        // 4. Mark single DB notification as read
        mockMvc.perform(patch("/api/v1/notifications/member/" + dbItemId + "/read")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk());

        // 5. Mark all remaining notifications as read -> unreadCount drops to 0
        mockMvc.perform(post("/api/v1/notifications/member/mark-all-read")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/notifications/member")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.unreadCount").value(0));

        // 6. Dismiss DB notification -> no longer returned
        mockMvc.perform(delete("/api/v1/notifications/member/" + dbItemId)
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/notifications/member")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[*].id", not(hasItem(dbItemId))));
    }
}

