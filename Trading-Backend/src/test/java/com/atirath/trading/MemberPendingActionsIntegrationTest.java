package com.atirath.trading;

import com.atirath.trading.common.AccountStatus;
import com.atirath.trading.common.AccountType;
import com.atirath.trading.common.DocumentType;
import com.atirath.trading.config.JwtTokenProvider;
import com.atirath.trading.modules.auth.model.AccountDocument;
import com.atirath.trading.modules.auth.model.BankDetail;
import com.atirath.trading.modules.auth.model.IdentityKycDetail;
import com.atirath.trading.modules.auth.model.IndividualProfile;
import com.atirath.trading.modules.auth.model.UserAccount;
import com.atirath.trading.modules.auth.repository.AccountDocumentRepository;
import com.atirath.trading.modules.auth.repository.BankDetailRepository;
import com.atirath.trading.modules.auth.repository.IdentityKycDetailRepository;
import com.atirath.trading.modules.auth.repository.IndividualProfileRepository;
import com.atirath.trading.modules.auth.repository.UserAccountRepository;
import com.atirath.trading.modules.market.model.Escrow;
import com.atirath.trading.modules.market.model.EscrowAccount;
import com.atirath.trading.modules.market.model.MarketCompany;
import com.atirath.trading.modules.market.model.MarketOrder;
import com.atirath.trading.modules.market.repository.EscrowAccountRepository;
import com.atirath.trading.modules.market.repository.EscrowRepository;
import com.atirath.trading.modules.market.repository.MarketCompanyRepository;
import com.atirath.trading.modules.market.repository.MarketOrderRepository;
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
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MemberPendingActionsIntegrationTest {

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
    private BankDetailRepository bankDetailRepository;

    @Autowired
    private AccountDocumentRepository accountDocumentRepository;

    @Autowired
    private MarketCompanyRepository companyRepository;

    @Autowired
    private MarketOrderRepository orderRepository;

    @Autowired
    private NegotiationRepository negotiationRepository;

    @Autowired
    private EscrowRepository escrowRepository;

    @Autowired
    private EscrowAccountRepository escrowAccountRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("Pending Actions endpoint rejects unauthenticated requests with 401")
    void shouldRejectUnauthenticatedPendingActionsRequest() throws Exception {
        mockMvc.perform(get("/api/v1/portfolio/member-pending-actions"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Pending Actions surfaces missing KYC and Bank setup for new users without hardcoded demo items")
    void shouldReturnRealOnboardingPendingActionsForNewUser() throws Exception {
        long suffix = System.nanoTime() % 1_000_000_000L;
        UserAccount user = userAccountRepository.save(UserAccount.builder()
                .accountType(AccountType.INDIVIDUAL)
                .email("kumar.pa." + suffix + "@test.com")
                .mobileNumber("7" + String.format("%09d", suffix))
                .accountStatus(AccountStatus.PENDING)
                .build());

        individualProfileRepository.save(IndividualProfile.builder()
                .userAccount(user)
                .fullName("Kumar Pending")
                .build());

        entityManager.flush();
        String token = jwtTokenProvider.generateAccessToken(user);

        mockMvc.perform(get("/api/v1/portfolio/member-pending-actions")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalPending", greaterThanOrEqualTo(2)))
                .andExpect(jsonPath("$.data.items[*].id", hasItem("kyc-complete")))
                .andExpect(jsonPath("$.data.items[*].id", hasItem("acc-bank")))
                .andExpect(jsonPath("$.data.items[*].relatedSub", not(hasItem("ATG10027"))))
                .andExpect(jsonPath("$.data.categoryCounts.KYC").value(1))
                .andExpect(jsonPath("$.data.categoryCounts.Account").value(1))
                .andExpect(jsonPath("$.data.upcomingDeadlines.length()", greaterThanOrEqualTo(2)));
    }

    @Test
    @DisplayName("Pending Actions aggregates active Orders, Negotiations, and Escrows with category filtering and date sort")
    void shouldAggregateOrdersNegotiationsAndEscrowWithFiltering() throws Exception {
        long suffix = System.nanoTime() % 1_000_000_000L;
        UserAccount user = userAccountRepository.save(UserAccount.builder()
                .accountType(AccountType.INDIVIDUAL)
                .email("ananya.pa." + suffix + "@test.com")
                .mobileNumber("6" + String.format("%09d", suffix))
                .accountStatus(AccountStatus.ACTIVE)
                .build());

        individualProfileRepository.save(IndividualProfile.builder()
                .userAccount(user)
                .fullName("Ananya Actions")
                .build());

        identityKycDetailRepository.save(IdentityKycDetail.builder()
                .userAccount(user)
                .panNumber("ABCDE1234K")
                .aadhaarNumberEncrypted("enc-aadhaar")
                .panFullName("Ananya Actions")
                .dateOfBirth(LocalDate.of(1994, 4, 15))
                .build());

        bankDetailRepository.save(BankDetail.builder()
                .userAccount(user)
                .bankName("HDFC Bank")
                .accountHolderName("Ananya Actions")
                .accountNumberEncrypted("enc-acct")
                .ifscCode("HDFC0001234")
                .branchName("Hyderabad")
                .build());

        accountDocumentRepository.save(AccountDocument.builder()
                .userAccount(user)
                .documentType(DocumentType.IDENTITY_PROOF)
                .documentName("pan.pdf")
                .filePath("uploads/pan.pdf")
                .mimeType("application/pdf")
                .fileSizeBytes(1200L)
                .build());

        escrowAccountRepository.save(EscrowAccount.builder()
                .userAccount(user)
                .accountReference("ESC-PA-" + suffix)
                .availableBalance(new BigDecimal("100000.00"))
                .blockedBalance(new BigDecimal("16500.00"))
                .currencyCode("INR")
                .accountStatus("ACTIVE")
                .build());

        long suffix2 = (System.nanoTime() + 55) % 1_000_000_000L;
        UserAccount counterparty = userAccountRepository.save(UserAccount.builder()
                .accountType(AccountType.INDIVIDUAL)
                .email("cp.pa." + suffix2 + "@test.com")
                .mobileNumber("8" + String.format("%09d", suffix2))
                .accountStatus(AccountStatus.ACTIVE)
                .build());

        List<MarketCompany> companies = companyRepository.findByStatusOrderByCompanyNameAsc("ACTIVE");
        MarketCompany comp = companies.stream().findFirst().orElseThrow();

        MarketOrder order = orderRepository.save(MarketOrder.builder()
                .orderNo("ORD-PA-" + suffix)
                .userAccount(user)
                .company(comp)
                .orderSide("BUY")
                .orderType("LIMIT")
                .quantity(500L)
                .pricePerShare(new BigDecimal("245.00"))
                .orderValue(new BigDecimal("122500.00"))
                .status("IN_ESCROW")
                .build());

        negotiationRepository.save(Negotiation.builder()
                .negotiationNo("NEG-PA-" + suffix)
                .order(order)
                .company(comp)
                .initiatorUser(counterparty)
                .counterpartyUser(user)
                .quantity(300L)
                .proposedPrice(new BigDecimal("240.00"))
                .status("COUNTERED")
                .expiresAt(OffsetDateTime.now().plusDays(4))
                .build());

        escrowRepository.save(Escrow.builder()
                .escrowReference("ESCR-PA-" + suffix)
                .buyerUser(user)
                .sellerUser(counterparty)
                .amount(new BigDecimal("16500.00"))
                .escrowStatus("FUNDS_BLOCKED")
                .build());

        entityManager.flush();
        String token = jwtTokenProvider.generateAccessToken(user);

        mockMvc.perform(get("/api/v1/portfolio/member-pending-actions")
                        .param("category", "All")
                        .param("sortBy", "Priority")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalPending").value(3))
                .andExpect(jsonPath("$.data.highPriorityCount").value(2))
                .andExpect(jsonPath("$.data.mediumPriorityCount").value(1))
                .andExpect(jsonPath("$.data.categoryCounts.KYC").value(0))
                .andExpect(jsonPath("$.data.categoryCounts.Orders").value(1))
                .andExpect(jsonPath("$.data.categoryCounts.Negotiations").value(1))
                .andExpect(jsonPath("$.data.categoryCounts.Escrow").value(1))
                .andExpect(jsonPath("$.data.items", hasSize(3)))
                .andExpect(jsonPath("$.data.upcomingDeadlines", hasSize(3)));

        mockMvc.perform(get("/api/v1/portfolio/member-pending-actions")
                        .param("category", "Negotiations")
                        .param("sortBy", "Date")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items", hasSize(1)))
                .andExpect(jsonPath("$.data.items[0].category").value("Negotiations"))
                .andExpect(jsonPath("$.data.items[0].priority").value("High"));
    }
}

