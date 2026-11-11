package com.atirath.trading;

import com.atirath.trading.common.AccountStatus;
import com.atirath.trading.common.AccountType;
import com.atirath.trading.config.JwtTokenProvider;
import com.atirath.trading.modules.auth.model.IdentityKycDetail;
import com.atirath.trading.modules.auth.model.IndividualProfile;
import com.atirath.trading.modules.auth.model.UserAccount;
import com.atirath.trading.modules.auth.repository.IdentityKycDetailRepository;
import com.atirath.trading.modules.auth.repository.IndividualProfileRepository;
import com.atirath.trading.modules.auth.repository.UserAccountRepository;
import com.atirath.trading.modules.market.model.EscrowAccount;
import com.atirath.trading.modules.market.model.MarketCompany;
import com.atirath.trading.modules.market.model.MarketOrder;
import com.atirath.trading.modules.market.model.UserShareHolding;
import com.atirath.trading.modules.market.repository.EscrowAccountRepository;
import com.atirath.trading.modules.market.repository.MarketCompanyRepository;
import com.atirath.trading.modules.market.repository.MarketOrderRepository;
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
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MemberDashboardIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private IndividualProfileRepository individualProfileRepository;

    @Autowired
    private IdentityKycDetailRepository identityKycDetailRepository;

    @Autowired
    private MarketCompanyRepository companyRepository;

    @Autowired
    private UserShareHoldingRepository shareHoldingRepository;

    @Autowired
    private MarketOrderRepository orderRepository;

    @Autowired
    private EscrowAccountRepository escrowAccountRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("Member Dashboard requires authentication and rejects anonymous access with 401")
    void shouldRejectUnauthenticatedRequest() throws Exception {
        mockMvc.perform(get("/api/v1/portfolio/member-dashboard"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Member Dashboard returns zero holdings and real onboarding state for a brand new user")
    void shouldReturnRealEmptyStateForNewUser() throws Exception {
        long suffix = System.nanoTime() % 1_000_000_000L;
        UserAccount user = userAccountRepository.save(UserAccount.builder()
                .accountType(AccountType.INDIVIDUAL)
                .email("kumar.dash." + suffix + "@test.com")
                .mobileNumber("7" + String.format("%09d", suffix))
                .accountStatus(AccountStatus.PENDING)
                .build());

        individualProfileRepository.save(IndividualProfile.builder()
                .userAccount(user)
                .fullName("Kumar Test")
                .build());

        entityManager.flush();
        String token = jwtTokenProvider.generateAccessToken(user);

        mockMvc.perform(get("/api/v1/portfolio/member-dashboard")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.displayName").value("Kumar"))
                .andExpect(jsonPath("$.data.userName").value("Kumar Test"))
                .andExpect(jsonPath("$.data.memberId").value("ATG" + user.getUserId()))
                .andExpect(jsonPath("$.data.totalPortfolioValue").value(0))
                .andExpect(jsonPath("$.data.investedAmount").value(0))
                .andExpect(jsonPath("$.data.companiesInvestedCount").value(0))
                .andExpect(jsonPath("$.data.kycCompleted").value(false))
                .andExpect(jsonPath("$.data.holdings", hasSize(0)))
                .andExpect(jsonPath("$.data.recentTransactions", hasSize(0)))
                .andExpect(jsonPath("$.data.performanceSeries", hasSize(0)))
                .andExpect(jsonPath("$.data.pendingActions[0].id").value("complete-kyc"));
    }

    @Test
    @DisplayName("Member Dashboard aggregates real holdings, escrow balance, orders, and minor trading restriction")
    void shouldReturnAggregatedHoldingsOrdersAndKycRestriction() throws Exception {
        long suffix = System.nanoTime() % 1_000_000_000L;
        UserAccount user = userAccountRepository.save(UserAccount.builder()
                .accountType(AccountType.INDIVIDUAL)
                .email("trader.dash." + suffix + "@test.com")
                .mobileNumber("6" + String.format("%09d", suffix))
                .accountStatus(AccountStatus.PENDING)
                .build());

        individualProfileRepository.save(IndividualProfile.builder()
                .userAccount(user)
                .fullName("Ananya Rao")
                .build());

        identityKycDetailRepository.save(IdentityKycDetail.builder()
                .userAccount(user)
                .panNumber("ABCDE1234Z")
                .aadhaarNumberEncrypted("enc-aadhaar")
                .panFullName("Ananya Rao")
                .dateOfBirth(LocalDate.now().minusYears(16))
                .build());

        escrowAccountRepository.save(EscrowAccount.builder()
                .userAccount(user)
                .accountReference("ESC-TEST-" + suffix)
                .availableBalance(new BigDecimal("50000.00"))
                .blockedBalance(BigDecimal.ZERO)
                .currencyCode("INR")
                .accountStatus("ACTIVE")
                .build());

        List<MarketCompany> activeCompanies = companyRepository.findByStatusOrderByCompanyNameAsc("ACTIVE");
        MarketCompany comp = activeCompanies.stream().findFirst().orElseThrow();

        shareHoldingRepository.save(UserShareHolding.builder()
                .userAccount(user)
                .company(comp)
                .totalShares(100L)
                .lockedShares(0L)
                .averageBuyPrice(new BigDecimal("200.00"))
                .build());

        orderRepository.save(MarketOrder.builder()
                .orderNo("ORD-DASH-" + suffix)
                .userAccount(user)
                .company(comp)
                .orderSide("BUY")
                .orderType("LIMIT")
                .quantity(100L)
                .pricePerShare(new BigDecimal("200.00"))
                .orderValue(new BigDecimal("20000.00"))
                .status("EXECUTED")
                .build());

        entityManager.flush();
        String token = jwtTokenProvider.generateAccessToken(user);

        mockMvc.perform(get("/api/v1/portfolio/member-dashboard")
                        .param("period", "6M")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.displayName").value("Ananya"))
                .andExpect(jsonPath("$.data.activePeriod").value("6M"))
                .andExpect(jsonPath("$.data.kycCompleted").value(true))
                .andExpect(jsonPath("$.data.tradingEligible").value(false))
                .andExpect(jsonPath("$.data.companiesInvestedCount").value(1))
                .andExpect(jsonPath("$.data.investedAmount").value(20000.0))
                .andExpect(jsonPath("$.data.escrowAvailableBalance").value(50000.0))
                .andExpect(jsonPath("$.data.holdings", hasSize(1)))
                .andExpect(jsonPath("$.data.holdings[0].shares").value(100))
                .andExpect(jsonPath("$.data.recentTransactions", hasSize(1)))
                .andExpect(jsonPath("$.data.recentTransactions[0].status").value("Completed"))
                .andExpect(jsonPath("$.data.allocationSlices", hasSize(1)))
                .andExpect(jsonPath("$.data.allocationSlices[0].percentage").value(100.0))
                .andExpect(jsonPath("$.data.performanceSeries.length()", greaterThan(1)));
    }
}

