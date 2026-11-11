package com.atirath.trading;

import com.atirath.trading.common.AccountStatus;
import com.atirath.trading.common.AccountType;
import com.atirath.trading.config.JwtTokenProvider;
import com.atirath.trading.modules.auth.model.IndividualProfile;
import com.atirath.trading.modules.auth.model.UserAccount;
import com.atirath.trading.modules.auth.repository.IndividualProfileRepository;
import com.atirath.trading.modules.auth.repository.UserAccountRepository;
import com.atirath.trading.modules.market.model.EscrowAccount;
import com.atirath.trading.modules.market.model.MarketCompany;
import com.atirath.trading.modules.market.model.MarketOrder;
import com.atirath.trading.modules.market.model.MarketTrade;
import com.atirath.trading.modules.market.model.UserShareHolding;
import com.atirath.trading.modules.market.repository.EscrowAccountRepository;
import com.atirath.trading.modules.market.repository.MarketCompanyRepository;
import com.atirath.trading.modules.market.repository.MarketOrderRepository;
import com.atirath.trading.modules.market.repository.MarketTradeRepository;
import com.atirath.trading.modules.market.repository.UserShareHoldingRepository;
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
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PortfolioScreenIntegrationTest {

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
    private UserShareHoldingRepository shareHoldingRepository;

    @Autowired
    private MarketOrderRepository orderRepository;

    @Autowired
    private MarketTradeRepository tradeRepository;

    @Autowired
    private EscrowAccountRepository escrowAccountRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("Portfolio dashboard, overview, and CSV downloads reject unauthenticated requests with 401")
    void shouldRejectUnauthenticatedPortfolioRequests() throws Exception {
        mockMvc.perform(get("/api/v1/portfolio/dashboard"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/portfolio/overview"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/portfolio/download/report"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/portfolio/download/statement"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Portfolio dashboard & overview return clean zero state for a brand new user")
    void shouldReturnZeroStateForNewUser() throws Exception {
        long suffix = System.nanoTime() % 1_000_000_000L;
        UserAccount user = userAccountRepository.save(UserAccount.builder()
                .accountType(AccountType.INDIVIDUAL)
                .email("kumar.port." + suffix + "@test.com")
                .mobileNumber("7" + String.format("%09d", suffix))
                .accountStatus(AccountStatus.PENDING)
                .build());

        individualProfileRepository.save(IndividualProfile.builder()
                .userAccount(user)
                .fullName("Kumar Portfolio")
                .build());

        entityManager.flush();
        String token = jwtTokenProvider.generateAccessToken(user);

        mockMvc.perform(get("/api/v1/portfolio/dashboard")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userName").value("Kumar Portfolio"))
                .andExpect(jsonPath("$.data.memberId").value("ATG" + user.getUserId()))
                .andExpect(jsonPath("$.data.summaryKpis[0].id").value("total_portfolio_value"))
                .andExpect(jsonPath("$.data.summaryKpis[0].amount").value(0))
                .andExpect(jsonPath("$.data.summaryKpis[4].count").value(0))
                .andExpect(jsonPath("$.data.summaryKpis[5].count").value(0))
                .andExpect(jsonPath("$.data.valueTrend", hasSize(0)))
                .andExpect(jsonPath("$.data.allocation", hasSize(0)))
                .andExpect(jsonPath("$.data.topHoldings", hasSize(0)))
                .andExpect(jsonPath("$.data.recentActivity", hasSize(0)));

        mockMvc.perform(get("/api/v1/portfolio/overview")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userName").value("Kumar Portfolio"))
                .andExpect(jsonPath("$.data.totalPortfolioValue").value(0))
                .andExpect(jsonPath("$.data.investedAmount").value(0))
                .andExpect(jsonPath("$.data.companiesInvestedCount").value(0))
                .andExpect(jsonPath("$.data.totalCompaniesCount", greaterThan(0)))
                .andExpect(jsonPath("$.data.valueTrend", hasSize(0)))
                .andExpect(jsonPath("$.data.allocationByCompany", hasSize(0)))
                .andExpect(jsonPath("$.data.assetBreakdown", hasSize(4)))
                .andExpect(jsonPath("$.data.returnSummary", hasSize(5)));
    }

    @Test
    @DisplayName("Portfolio dashboard, overview, and CSV exports aggregate real holdings, escrow, transfers, and orders")
    void shouldAggregateRealPortfolioDashboardOverviewAndCsvExports() throws Exception {
        long suffix = System.nanoTime() % 1_000_000_000L;
        UserAccount user = userAccountRepository.save(UserAccount.builder()
                .accountType(AccountType.INDIVIDUAL)
                .email("ananya.port." + suffix + "@test.com")
                .mobileNumber("6" + String.format("%09d", suffix))
                .accountStatus(AccountStatus.ACTIVE)
                .build());

        individualProfileRepository.save(IndividualProfile.builder()
                .userAccount(user)
                .fullName("Ananya Rao")
                .build());

        long suffix2 = (System.nanoTime() + 77) % 1_000_000_000L;
        UserAccount receiver = userAccountRepository.save(UserAccount.builder()
                .accountType(AccountType.INDIVIDUAL)
                .email("recv.port." + suffix2 + "@test.com")
                .mobileNumber("8" + String.format("%09d", suffix2))
                .accountStatus(AccountStatus.ACTIVE)
                .build());

        List<MarketCompany> activeCompanies = companyRepository.findByStatusOrderByCompanyNameAsc("ACTIVE");
        MarketCompany comp = activeCompanies.stream().findFirst().orElseThrow();

        escrowAccountRepository.save(EscrowAccount.builder()
                .userAccount(user)
                .accountReference("ESC-PORT-" + suffix)
                .availableBalance(new BigDecimal("50000.00"))
                .blockedBalance(new BigDecimal("10000.00"))
                .currencyCode("INR")
                .accountStatus("ACTIVE")
                .build());

        shareHoldingRepository.save(UserShareHolding.builder()
                .userAccount(user)
                .company(comp)
                .totalShares(100L)
                .lockedShares(20L)
                .averageBuyPrice(new BigDecimal("200.00"))
                .build());

        MarketOrder openBuyOrder = orderRepository.save(MarketOrder.builder()
                .orderNo("ORD-PORT-" + suffix)
                .userAccount(user)
                .company(comp)
                .orderSide("BUY")
                .orderType("LIMIT")
                .quantity(50L)
                .pricePerShare(new BigDecimal("200.00"))
                .orderValue(new BigDecimal("10000.00"))
                .status("OPEN")
                .build());

        MarketOrder executedSellOrder = orderRepository.save(MarketOrder.builder()
                .orderNo("ORD-SELL-" + suffix)
                .userAccount(user)
                .company(comp)
                .orderSide("SELL")
                .orderType("LIMIT")
                .quantity(10L)
                .pricePerShare(new BigDecimal("250.00"))
                .orderValue(new BigDecimal("2500.00"))
                .status("EXECUTED")
                .build());

        MarketTrade trade = tradeRepository.save(MarketTrade.builder()
                .tradeNo("TRD-PORT-" + suffix)
                .company(comp)
                .buyOrderId(openBuyOrder.getOrderId())
                .sellOrderId(executedSellOrder.getOrderId())
                .buyerUser(receiver)
                .sellerUser(user)
                .quantity(10L)
                .tradePrice(new BigDecimal("250.00"))
                .tradeValue(new BigDecimal("2500.00"))
                .executedAt(OffsetDateTime.now())
                .status("EXECUTED")
                .build());

        entityManager.createNativeQuery(
                        "INSERT INTO share_transfers (transfer_ref_no, company_id, from_user_id, to_user_id, trade_id, quantity, status, transfer_status) " +
                        "VALUES (:refNo, :companyId, :fromId, :toId, :tradeId, :qty, 'PENDING', 'PENDING')")
                .setParameter("refNo", "TRF-TEST-" + suffix)
                .setParameter("companyId", comp.getCompanyId())
                .setParameter("fromId", user.getUserId())
                .setParameter("toId", receiver.getUserId())
                .setParameter("tradeId", trade.getTradeId())
                .setParameter("qty", 15L)
                .executeUpdate();

        entityManager.flush();
        String token = jwtTokenProvider.generateAccessToken(user);

        mockMvc.perform(get("/api/v1/portfolio/dashboard")
                        .param("period", "6M")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userName").value("Ananya Rao"))
                .andExpect(jsonPath("$.data.memberId").value("ATG" + user.getUserId()))
                .andExpect(jsonPath("$.data.activeTrendPeriod").value("6M"))
                .andExpect(jsonPath("$.data.summaryKpis[1].amount").value(20000.0))
                .andExpect(jsonPath("$.data.summaryKpis[3].id").value("realized_pnl"))
                .andExpect(jsonPath("$.data.summaryKpis[3].amount").value(500.0))
                .andExpect(jsonPath("$.data.summaryKpis[4].count").value(1))
                .andExpect(jsonPath("$.data.summaryKpis[5].count").value(100))
                .andExpect(jsonPath("$.data.performance.activeOrders").value(1))
                .andExpect(jsonPath("$.data.performance.lockedSharesInEscrow").value(20))
                .andExpect(jsonPath("$.data.performance.pendingTransfers").value(15))
                .andExpect(jsonPath("$.data.topHoldings", hasSize(1)))
                .andExpect(jsonPath("$.data.topHoldings[0].sharesHeld").value(100))
                .andExpect(jsonPath("$.data.allocation", hasSize(1)))
                .andExpect(jsonPath("$.data.valueTrend.length()", greaterThan(1)))
                .andExpect(jsonPath("$.data.recentActivity.length()", greaterThan(0)));

        mockMvc.perform(get("/api/v1/portfolio/overview")
                        .param("period", "1M")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userName").value("Ananya Rao"))
                .andExpect(jsonPath("$.data.investedAmount").value(20000.0))
                .andExpect(jsonPath("$.data.companiesInvestedCount").value(1))
                .andExpect(jsonPath("$.data.allocationByCompany[0].percentage").value(100.0))
                .andExpect(jsonPath("$.data.assetBreakdown", hasSize(4)))
                .andExpect(jsonPath("$.data.assetBreakdown[2].amount").value(10000.0))
                .andExpect(jsonPath("$.data.assetBreakdown[3].amount").value(50000.0))
                .andExpect(jsonPath("$.data.returnSummary", hasSize(5)))
                .andExpect(jsonPath("$.data.valueTrend.length()", greaterThan(1)));

        mockMvc.perform(get("/api/v1/portfolio/download/report")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Ananya Rao")))
                .andExpect(content().string(containsString(comp.getCompanyName())))
                .andExpect(content().string(not(containsString("Sairam Vempati"))));

        mockMvc.perform(get("/api/v1/portfolio/download/statement")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Ananya Rao")))
                .andExpect(content().string(containsString("Total Shares,100")))
                .andExpect(content().string(not(containsString("ATG10027"))));
    }
}

