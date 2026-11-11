package com.atirath.trading;

import com.atirath.trading.common.AccountStatus;
import com.atirath.trading.common.AccountType;
import com.atirath.trading.config.JwtTokenProvider;
import com.atirath.trading.modules.auth.model.IndividualProfile;
import com.atirath.trading.modules.auth.model.UserAccount;
import com.atirath.trading.modules.auth.repository.IndividualProfileRepository;
import com.atirath.trading.modules.auth.repository.UserAccountRepository;
import com.atirath.trading.modules.market.model.MarketCompany;
import com.atirath.trading.modules.market.model.MarketOrder;
import com.atirath.trading.modules.market.model.MarketTrade;
import com.atirath.trading.modules.market.repository.MarketCompanyRepository;
import com.atirath.trading.modules.market.repository.MarketOrderRepository;
import com.atirath.trading.modules.market.repository.MarketTradeRepository;
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

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MemberTransactionsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private IndividualProfileRepository individualProfileRepository;

    @Autowired
    private MarketCompanyRepository companyRepository;

    @Autowired
    private MarketOrderRepository orderRepository;

    @Autowired
    private MarketTradeRepository tradeRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("Transactions endpoints reject unauthenticated access with 401")
    void shouldRejectUnauthenticatedTransactionsRequests() throws Exception {
        mockMvc.perform(get("/api/v1/portfolio/member-transactions"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/portfolio/download/member-transactions"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Transactions endpoint returns clean zero state for a brand new user")
    void shouldReturnEmptyStateForNewUser() throws Exception {
        long suffix = System.nanoTime() % 1_000_000_000L;
        UserAccount user = userAccountRepository.save(UserAccount.builder()
                .accountType(AccountType.INDIVIDUAL)
                .email("kumar.txn." + suffix + "@test.com")
                .mobileNumber("7" + String.format("%09d", suffix))
                .accountStatus(AccountStatus.ACTIVE)
                .build());

        individualProfileRepository.save(IndividualProfile.builder()
                .userAccount(user)
                .fullName("Kumar Transactions")
                .build());

        entityManager.flush();
        String token = jwtTokenProvider.generateAccessToken(user);

        mockMvc.perform(get("/api/v1/portfolio/member-transactions")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalTransactions").value(0))
                .andExpect(jsonPath("$.data.buyCount").value(0))
                .andExpect(jsonPath("$.data.sellCount").value(0))
                .andExpect(jsonPath("$.data.transferCount").value(0))
                .andExpect(jsonPath("$.data.transactions", hasSize(0)))
                .andExpect(jsonPath("$.data.recentActivity", hasSize(0)));
    }

    @Test
    @DisplayName("Transactions endpoint aggregates Buy, Sell, Transfer rows with filtering, pagination, and CSV export")
    void shouldAggregateFilterPaginateAndExportTransactions() throws Exception {
        long suffix = System.nanoTime() % 1_000_000_000L;
        UserAccount user = userAccountRepository.save(UserAccount.builder()
                .accountType(AccountType.INDIVIDUAL)
                .email("ananya.txn." + suffix + "@test.com")
                .mobileNumber("6" + String.format("%09d", suffix))
                .accountStatus(AccountStatus.ACTIVE)
                .build());

        individualProfileRepository.save(IndividualProfile.builder()
                .userAccount(user)
                .fullName("Ananya Transactions")
                .build());

        long suffix2 = (System.nanoTime() + 99) % 1_000_000_000L;
        UserAccount counterparty = userAccountRepository.save(UserAccount.builder()
                .accountType(AccountType.INDIVIDUAL)
                .email("cp.txn." + suffix2 + "@test.com")
                .mobileNumber("8" + String.format("%09d", suffix2))
                .accountStatus(AccountStatus.ACTIVE)
                .build());

        List<MarketCompany> companies = companyRepository.findByStatusOrderByCompanyNameAsc("ACTIVE");
        MarketCompany comp = companies.stream().findFirst().orElseThrow();

        // Counterparty orders so both FK/NOT NULL columns on market_trades are satisfied cleanly
        MarketOrder cpSellOrder = orderRepository.save(MarketOrder.builder()
                .orderNo("ORD-TXN-CPS-" + suffix)
                .userAccount(counterparty)
                .company(comp)
                .orderSide("SELL")
                .orderType("LIMIT")
                .quantity(500L)
                .pricePerShare(new BigDecimal("245.00"))
                .orderValue(new BigDecimal("122500.00"))
                .status("EXECUTED")
                .build());

        MarketOrder cpBuyOrder = orderRepository.save(MarketOrder.builder()
                .orderNo("ORD-TXN-CPB-" + suffix)
                .userAccount(counterparty)
                .company(comp)
                .orderSide("BUY")
                .orderType("LIMIT")
                .quantity(300L)
                .pricePerShare(new BigDecimal("200.00"))
                .orderValue(new BigDecimal("60000.00"))
                .status("EXECUTED")
                .build());

        // 1. Buy Order + matching Trade (must appear as 1 Buy row via order deduplication)
        MarketOrder buyOrder = orderRepository.save(MarketOrder.builder()
                .orderNo("ORD-TXN-B-" + suffix)
                .userAccount(user)
                .company(comp)
                .orderSide("BUY")
                .orderType("LIMIT")
                .quantity(500L)
                .pricePerShare(new BigDecimal("245.00"))
                .orderValue(new BigDecimal("122500.00"))
                .status("EXECUTED")
                .build());

        MarketTrade buyTrade = tradeRepository.save(MarketTrade.builder()
                .tradeNo("TRD-TXN-B-" + suffix)
                .company(comp)
                .buyOrderId(buyOrder.getOrderId())
                .sellOrderId(cpSellOrder.getOrderId())
                .buyerUser(user)
                .sellerUser(counterparty)
                .quantity(500L)
                .tradePrice(new BigDecimal("245.00"))
                .tradeValue(new BigDecimal("122500.00"))
                .executedAt(OffsetDateTime.now().minusHours(2))
                .status("EXECUTED")
                .build());

        // 2. Direct Settled Sell Trade where user has no matching MarketOrder -> appears as 1 Sell row
        tradeRepository.save(MarketTrade.builder()
                .tradeNo("TRD-TXN-S-" + suffix)
                .company(comp)
                .buyOrderId(cpBuyOrder.getOrderId())
                .sellOrderId(cpSellOrder.getOrderId())
                .buyerUser(counterparty)
                .sellerUser(user)
                .quantity(300L)
                .tradePrice(new BigDecimal("200.00"))
                .tradeValue(new BigDecimal("60000.00"))
                .executedAt(OffsetDateTime.now().minusDays(1))
                .status("EXECUTED")
                .build());

        // 3. Share Transfer -> appears as 1 Transfer row
        entityManager.createNativeQuery(
                        "INSERT INTO share_transfers (transfer_ref_no, company_id, from_user_id, to_user_id, trade_id, quantity, status, transfer_status) " +
                        "VALUES (:refNo, :companyId, :fromId, :toId, :tradeId, :qty, 'COMPLETED', 'COMPLETED')")
                .setParameter("refNo", "TRF-TXN-" + suffix)
                .setParameter("companyId", comp.getCompanyId())
                .setParameter("fromId", user.getUserId())
                .setParameter("toId", counterparty.getUserId())
                .setParameter("tradeId", buyTrade.getTradeId())
                .setParameter("qty", 50L)
                .executeUpdate();

        entityManager.flush();
        String token = jwtTokenProvider.generateAccessToken(user);

        // Full summary + pagination page 1 (size 2)
        mockMvc.perform(get("/api/v1/portfolio/member-transactions")
                        .param("period", "3M")
                        .param("page", "1")
                        .param("size", "2")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalTransactions").value(3))
                .andExpect(jsonPath("$.data.buyCount").value(1))
                .andExpect(jsonPath("$.data.buyAmount").value(122500.0))
                .andExpect(jsonPath("$.data.sellCount").value(1))
                .andExpect(jsonPath("$.data.sellAmount").value(60000.0))
                .andExpect(jsonPath("$.data.transferCount").value(1))
                .andExpect(jsonPath("$.data.totalElements").value(3))
                .andExpect(jsonPath("$.data.totalPages").value(2))
                .andExpect(jsonPath("$.data.transactions", hasSize(2)))
                .andExpect(jsonPath("$.data.recentActivity", hasSize(3)));

        // Filter by Type=Transfer
        mockMvc.perform(get("/api/v1/portfolio/member-transactions")
                        .param("type", "Transfer")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.transactions", hasSize(1)))
                .andExpect(jsonPath("$.data.transactions[0].type").value("Transfer"))
                .andExpect(jsonPath("$.data.transactions[0].shares").value(50))
                .andExpect(jsonPath("$.data.transactions[0].status").value("Completed"));

        // Export CSV filtered by Buy
        mockMvc.perform(get("/api/v1/portfolio/download/member-transactions")
                        .param("type", "Buy")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Ananya Transactions")))
                .andExpect(content().string(containsString("ORD-TXN-B-" + suffix)))
                .andExpect(content().string(not(containsString("TRD-TXN-S-" + suffix))))
                .andExpect(content().string(not(containsString("Sairam"))));
    }
}

