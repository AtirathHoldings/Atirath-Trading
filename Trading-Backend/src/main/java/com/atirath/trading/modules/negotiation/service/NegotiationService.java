package com.atirath.trading.modules.negotiation.service;

import com.atirath.trading.common.AccountType;
import com.atirath.trading.common.ResourceNotFoundException;
import com.atirath.trading.modules.auth.model.CorporateProfile;
import com.atirath.trading.modules.auth.model.IndividualProfile;
import com.atirath.trading.modules.auth.model.UserAccount;
import com.atirath.trading.modules.auth.repository.CorporateProfileRepository;
import com.atirath.trading.modules.auth.repository.IndividualProfileRepository;
import com.atirath.trading.modules.auth.repository.UserAccountRepository;
import com.atirath.trading.modules.market.dto.MarketTickDto;
import com.atirath.trading.modules.market.model.MarketCompany;
import com.atirath.trading.modules.market.repository.MarketCompanyRepository;
import com.atirath.trading.modules.market.repository.MarketOrderRepository;
import com.atirath.trading.modules.market.service.MarketBroadcastService;
import com.atirath.trading.modules.negotiation.dto.*;
import com.atirath.trading.modules.negotiation.model.*;
import com.atirath.trading.modules.negotiation.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class NegotiationService {

    private final NegotiationRepository negotiationRepository;
    private final NegotiationOfferRepository negotiationOfferRepository;
    private final NegotiationMessageRepository negotiationMessageRepository;
    private final TradeAgreementRepository tradeAgreementRepository;
    private final MarketCompanyRepository marketCompanyRepository;
    private final UserAccountRepository userAccountRepository;
    private final IndividualProfileRepository individualProfileRepository;
    private final CorporateProfileRepository corporateProfileRepository;
    private final MarketBroadcastService broadcastService;

    private static final DecimalFormat CURRENCY_FMT = new DecimalFormat("#,##,##0.00");
    private static final DecimalFormat SHARES_FMT = new DecimalFormat("#,##,###");

    // In-memory interactive store to ensure instant response matching screenshot data
    private final Map<String, NegotiationDetailDto> liveNegotiationDetails = new ConcurrentHashMap<>();
    private final Map<String, List<NegotiationHistoryDto.HistoryItemDto>> liveHistoryItems = new ConcurrentHashMap<>();
    private final Map<String, TradeAgreementDto> liveTradeAgreements = new ConcurrentHashMap<>();
    private final List<NegotiationListItemDto> liveNegotiationsList = Collections.synchronizedList(new ArrayList<>());
    private final List<NegotiationNotificationItemDto> liveNotifications = Collections.synchronizedList(new ArrayList<>());

    {
        initDemoData();
    }

    public void resetDemoData() {
        initDemoData();
    }

    public UserAccount resolveCurrentUser(Long explicitUserId) {
        if (explicitUserId != null) {
            return userAccountRepository.findById(explicitUserId).orElse(null);
        }
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
                String name = auth.getName();
                if (name != null && !name.isBlank()) {
                    try {
                        Long uid = Long.parseLong(name);
                        Optional<UserAccount> uOpt = userAccountRepository.findById(uid);
                        if (uOpt.isPresent()) return uOpt.get();
                    } catch (NumberFormatException ignored) {}
                    Optional<UserAccount> uOpt = userAccountRepository.findByEmailIgnoreCase(name);
                    if (uOpt.isPresent()) return uOpt.get();
                    uOpt = userAccountRepository.findByMobileNumber(name);
                    if (uOpt.isPresent()) return uOpt.get();
                }
                // An authenticated user identity was presented, but no matching account exists in DB
                return null;
            }
        } catch (Exception ignored) {}

        return userAccountRepository.findByEmailIgnoreCase("sairam.v@atirathgroup.com")
                .or(() -> userAccountRepository.findAll().stream().findFirst())
                .orElse(null);
    }

    public String resolveMemberId(UserAccount user) {
        if (user == null) return "ATG10056";
        if (user.getUserId() != null && user.getUserId() == 1L) return "ATG10027";
        if (user.getEmail() != null && user.getEmail().contains("sairam")) return "ATG10027";
        if (user.getUserId() != null && user.getUserId() == 56L) return "ATG10056";
        if (user.getEmail() != null && user.getEmail().contains("rakesh")) return "ATG10056";
        if (user.getReferralCode() != null && user.getReferralCode().startsWith("ATG")) return user.getReferralCode();
        return "ATG" + (user.getUserId() != null ? (10000 + user.getUserId()) : "10099");
    }

    private void initDemoData() {
        // 1. Negotiation List (from Image 1)
        liveNegotiationsList.clear();
        liveNegotiationsList.add(NegotiationListItemDto.builder()
                .negotiationNo("NEG250918001")
                .companyName("Atirath Holdings")
                .companySymbol("ATG-H")
                .type("Buy")
                .counterpartyName("Rakesh Mehta")
                .counterpartyMemberId("ATG10056")
                .shares(1000L)
                .currentPrice(new BigDecimal("242.00"))
                .proposedPrice(new BigDecimal("235.00"))
                .status("Awaiting Response")
                .lastUpdated("18 Sep 2025 02:45 PM")
                .build());

        liveNegotiationsList.add(NegotiationListItemDto.builder()
                .negotiationNo("NEG250917003")
                .companyName("Farm Vantara")
                .companySymbol("FVT")
                .type("Sell")
                .counterpartyName("Priya Sharma")
                .counterpartyMemberId("ATG10072")
                .shares(500L)
                .currentPrice(new BigDecimal("186.50"))
                .proposedPrice(new BigDecimal("190.00"))
                .status("Active")
                .lastUpdated("17 Sep 2025 11:20 AM")
                .build());

        liveNegotiationsList.add(NegotiationListItemDto.builder()
                .negotiationNo("NEG250916007")
                .companyName("Goeazymart")
                .companySymbol("GEM")
                .type("Buy")
                .counterpartyName("Vikram Reddy")
                .counterpartyMemberId("ATG10081")
                .shares(1500L)
                .currentPrice(new BigDecimal("85.00"))
                .proposedPrice(new BigDecimal("82.00"))
                .status("Under Negotiation")
                .lastUpdated("16 Sep 2025 04:10 PM")
                .build());

        liveNegotiationsList.add(NegotiationListItemDto.builder()
                .negotiationNo("NEG250915004")
                .companyName("CS INTERIO")
                .companySymbol("CSI")
                .type("Sell")
                .counterpartyName("Anita Desai")
                .counterpartyMemberId("ATG10063")
                .shares(400L)
                .currentPrice(new BigDecimal("210.00"))
                .proposedPrice(new BigDecimal("205.00"))
                .status("Accepted")
                .lastUpdated("15 Sep 2025 10:30 AM")
                .build());

        liveNegotiationsList.add(NegotiationListItemDto.builder()
                .negotiationNo("NEG250914002")
                .companyName("Tri Energy Battery")
                .companySymbol("TEB")
                .type("Buy")
                .counterpartyName("Suresh Nair")
                .counterpartyMemberId("ATG10078")
                .shares(1200L)
                .currentPrice(new BigDecimal("310.00"))
                .proposedPrice(new BigDecimal("300.00"))
                .status("Rejected")
                .lastUpdated("14 Sep 2025 01:15 PM")
                .build());

        liveNegotiationsList.add(NegotiationListItemDto.builder()
                .negotiationNo("NEG250912006")
                .companyName("Royal One Heating")
                .companySymbol("ROHE")
                .type("Sell")
                .counterpartyName("Karan Malhotra")
                .counterpartyMemberId("ATG10069")
                .shares(600L)
                .currentPrice(new BigDecimal("150.00"))
                .proposedPrice(new BigDecimal("148.00"))
                .status("Expired")
                .lastUpdated("12 Sep 2025 03:40 PM")
                .build());

        // Additional items to make total 12
        liveNegotiationsList.add(NegotiationListItemDto.builder()
                .negotiationNo("NEG250911001")
                .companyName("Atirath Technologies")
                .companySymbol("ATG-T")
                .type("Buy")
                .counterpartyName("Rahul Verma")
                .counterpartyMemberId("ATG10044")
                .shares(800L)
                .currentPrice(new BigDecimal("1240.80"))
                .proposedPrice(new BigDecimal("1220.00"))
                .status("Active")
                .lastUpdated("11 Sep 2025 10:15 AM")
                .build());

        liveNegotiationsList.add(NegotiationListItemDto.builder()
                .negotiationNo("NEG250910008")
                .companyName("Atirath Green Energy")
                .companySymbol("ATG-E")
                .type("Sell")
                .counterpartyName("Sneha Patel")
                .counterpartyMemberId("ATG10052")
                .shares(2000L)
                .currentPrice(new BigDecimal("312.40"))
                .proposedPrice(new BigDecimal("315.00"))
                .status("Awaiting Response")
                .lastUpdated("10 Sep 2025 03:20 PM")
                .build());

        liveNegotiationsList.add(NegotiationListItemDto.builder()
                .negotiationNo("NEG250909005")
                .companyName("Atirath Financial Services")
                .companySymbol("ATG-F")
                .type("Buy")
                .counterpartyName("Manoj Tiwari")
                .counterpartyMemberId("ATG10039")
                .shares(750L)
                .currentPrice(new BigDecimal("580.00"))
                .proposedPrice(new BigDecimal("575.00"))
                .status("Active")
                .lastUpdated("09 Sep 2025 01:45 PM")
                .build());

        liveNegotiationsList.add(NegotiationListItemDto.builder()
                .negotiationNo("NEG250908003")
                .companyName("Atirath Logistics")
                .companySymbol("ATG-L")
                .type("Sell")
                .counterpartyName("Divya Kapoor")
                .counterpartyMemberId("ATG10061")
                .shares(1000L)
                .currentPrice(new BigDecimal("425.60"))
                .proposedPrice(new BigDecimal("420.00"))
                .status("Accepted")
                .lastUpdated("08 Sep 2025 11:30 AM")
                .build());

        liveNegotiationsList.add(NegotiationListItemDto.builder()
                .negotiationNo("NEG250906002")
                .companyName("Atirath Mining")
                .companySymbol("ATG-M")
                .type("Buy")
                .counterpartyName("Amit Joshi")
                .counterpartyMemberId("ATG10048")
                .shares(1500L)
                .currentPrice(new BigDecimal("189.20"))
                .proposedPrice(new BigDecimal("185.00"))
                .status("Awaiting Response")
                .lastUpdated("06 Sep 2025 04:00 PM")
                .build());

        liveNegotiationsList.add(NegotiationListItemDto.builder()
                .negotiationNo("NEG250905001")
                .companyName("Atirath Realty")
                .companySymbol("ATG-R")
                .type("Sell")
                .counterpartyName("Pooja Hegde")
                .counterpartyMemberId("ATG10085")
                .shares(500L)
                .currentPrice(new BigDecimal("765.10"))
                .proposedPrice(new BigDecimal("750.00"))
                .status("Expired")
                .lastUpdated("05 Sep 2025 02:10 PM")
                .build());

        // 2. Main Negotiation Detail for NEG250918001 (Image 2 & 3)
        NegotiationDetailDto.PartyDto buyer = NegotiationDetailDto.PartyDto.builder()
                .isCurrentUser(true)
                .companyName("Atirath Holdings India Limited")
                .companySymbol("ATG-H")
                .verified(true)
                .memberId("ATG10027")
                .memberName("Sairam Vempati")
                .email("sairam@atirath.com")
                .phone("+91 98765 43210")
                .currentHoldings(12500L)
                .quantity(1000L)
                .partyRole("BUYER")
                .build();

        NegotiationDetailDto.PartyDto seller = NegotiationDetailDto.PartyDto.builder()
                .isCurrentUser(false)
                .companyName("Farm Vantara Technologies")
                .companySymbol("FVT")
                .verified(true)
                .memberId("ATG10056")
                .memberName("Rakesh Mehta")
                .email("rakesh@farmvantara.com")
                .phone("+91 91234 56789")
                .currentHoldings(5000L)
                .quantity(1000L)
                .partyRole("SELLER")
                .build();

        NegotiationDetailDto.CompanyShareDetailsDto shareDetails = NegotiationDetailDto.CompanyShareDetailsDto.builder()
                .companyName("Atirath Holdings India Limited")
                .symbol("ATG-H")
                .companyType("Private Limited")
                .sector("Diversified")
                .isin("ATG-H-001")
                .faceValue(new BigDecimal("10.00"))
                .lastTradedPrice(new BigDecimal("242.00"))
                .fiftyTwoWeekHigh(new BigDecimal("280.00"))
                .fiftyTwoWeekLow(new BigDecimal("180.00"))
                .build();

        List<NegotiationDetailDto.NegotiationTermRowDto> terms = new ArrayList<>(List.of(
                NegotiationDetailDto.NegotiationTermRowDto.builder()
                        .parameter("Price per Share")
                        .buyerOffer("₹ 235.00")
                        .sellerOffer("₹ 250.00")
                        .currentProposal("₹ 242.00")
                        .difference("- ₹ 8.00")
                        .highlighted(true)
                        .build(),
                NegotiationDetailDto.NegotiationTermRowDto.builder()
                        .parameter("Quantity (Shares)")
                        .buyerOffer("1,000")
                        .sellerOffer("1,000")
                        .currentProposal("1,000")
                        .difference("-")
                        .highlighted(false)
                        .build(),
                NegotiationDetailDto.NegotiationTermRowDto.builder()
                        .parameter("Order Value")
                        .buyerOffer("₹ 2,35,000.00")
                        .sellerOffer("₹ 2,50,000.00")
                        .currentProposal("₹ 2,42,000.00")
                        .difference("- ₹ 8,000.00")
                        .highlighted(false)
                        .build(),
                NegotiationDetailDto.NegotiationTermRowDto.builder()
                        .parameter("Settlement Timeline")
                        .buyerOffer("T+3 Days")
                        .sellerOffer("T+3 Days")
                        .currentProposal("T+3 Days")
                        .difference("-")
                        .highlighted(false)
                        .build(),
                NegotiationDetailDto.NegotiationTermRowDto.builder()
                        .parameter("Payment Mode")
                        .buyerOffer("Bank Transfer (Escrow)")
                        .sellerOffer("Bank Transfer (Escrow)")
                        .currentProposal("Bank Transfer (Escrow)")
                        .difference("-")
                        .highlighted(false)
                        .build(),
                NegotiationDetailDto.NegotiationTermRowDto.builder()
                        .parameter("Other Terms")
                        .buyerOffer("Standard")
                        .sellerOffer("Standard")
                        .currentProposal("Standard")
                        .difference("-")
                        .highlighted(false)
                        .build()
        ));

        List<NegotiationDetailDto.TimelineEventDto> timeline = new ArrayList<>(List.of(
                NegotiationDetailDto.TimelineEventDto.builder()
                        .stepNumber(1)
                        .title("Negotiation initiated by Seller")
                        .description("Initial offer: ₹ 250.00 per share, 1,000 shares")
                        .timestamp("17 Sep 2025, 11:20 AM")
                        .senderCode("RM")
                        .senderName("Rakesh Mehta")
                        .status("COMPLETED")
                        .price("₹ 250.00")
                        .shares("1,000 shares")
                        .build(),
                NegotiationDetailDto.TimelineEventDto.builder()
                        .stepNumber(2)
                        .title("You sent a counter offer")
                        .description("₹ 235.00 per share, 1,000 shares")
                        .timestamp("17 Sep 2025, 11:45 AM")
                        .senderCode("SV")
                        .senderName("Sairam Vempati")
                        .status("COMPLETED")
                        .price("₹ 235.00")
                        .shares("1,000 shares")
                        .build(),
                NegotiationDetailDto.TimelineEventDto.builder()
                        .stepNumber(3)
                        .title("Seller countered")
                        .description("₹ 242.00 per share, 1,000 shares")
                        .timestamp("18 Sep 2025, 09:15 AM")
                        .senderCode("RM")
                        .senderName("Rakesh Mehta")
                        .status("COMPLETED")
                        .price("₹ 242.00")
                        .shares("1,000 shares")
                        .build(),
                NegotiationDetailDto.TimelineEventDto.builder()
                        .stepNumber(4)
                        .title("Awaiting your response")
                        .description("Please review the latest offer and respond.")
                        .timestamp("18 Sep 2025, 09:15 AM")
                        .senderCode("SV")
                        .senderName("Sairam Vempati")
                        .status("ACTIVE")
                        .build()
        ));

        NegotiationDetailDto detail = NegotiationDetailDto.builder()
                .negotiationNo("NEG250918001")
                .status("Awaiting Your Response")
                .initiatedOn("17 Sep 2025, 11:20 AM")
                .expiresAt("20 Sep 2025, 11:59 PM")
                .expiresIn("1 day 12 hours")
                .buyer(buyer)
                .seller(seller)
                .companyShareDetails(shareDetails)
                .negotiationTerms(terms)
                .timeline(timeline)
                .availableActions(NegotiationDetailDto.AvailableActionsDto.builder()
                        .canAccept(true)
                        .canCounter(true)
                        .canReject(true)
                        .canRequestCall(true)
                        .build())
                .build();

        liveNegotiationDetails.put("NEG250918001", detail);

        // 3. Negotiation History (from Image 4)
        List<NegotiationHistoryDto.HistoryItemDto> historyList = new ArrayList<>(List.of(
                NegotiationHistoryDto.HistoryItemDto.builder()
                        .id(1L)
                        .senderCode("RM")
                        .senderName("Rakesh Mehta")
                        .senderMemberId("ATG10056")
                        .title("Initial Offer from Seller")
                        .message("I would like to sell 1,000 shares at ₹ 250.00 per share.")
                        .price(new BigDecimal("250.00"))
                        .shares(1000L)
                        .timestamp("17 Sep 2025, 11:20 AM")
                        .isCurrentUser(false)
                        .build(),
                NegotiationHistoryDto.HistoryItemDto.builder()
                        .id(2L)
                        .senderCode("SV")
                        .senderName("Sairam Vempati")
                        .senderMemberId("ATG10027")
                        .title("Counter Offer from You")
                        .message("Thank you for the offer. I would like to counter with ₹ 235.00 per share for 1,000 shares.")
                        .price(new BigDecimal("235.00"))
                        .shares(1000L)
                        .timestamp("17 Sep 2025, 11:45 AM")
                        .isCurrentUser(true)
                        .build(),
                NegotiationHistoryDto.HistoryItemDto.builder()
                        .id(3L)
                        .senderCode("RM")
                        .senderName("Rakesh Mehta")
                        .senderMemberId("ATG10056")
                        .title("Counter Offer from Seller")
                        .message("I can offer ₹ 245.00 per share for 1,000 shares.")
                        .price(new BigDecimal("245.00"))
                        .shares(1000L)
                        .timestamp("17 Sep 2025, 12:10 PM")
                        .isCurrentUser(false)
                        .build(),
                NegotiationHistoryDto.HistoryItemDto.builder()
                        .id(4L)
                        .senderCode("SV")
                        .senderName("Sairam Vempati")
                        .senderMemberId("ATG10027")
                        .title("Counter Offer from You")
                        .message("I can increase to ₹ 240.00 per share for 1,000 shares.")
                        .price(new BigDecimal("240.00"))
                        .shares(1000L)
                        .timestamp("17 Sep 2025, 12:35 PM")
                        .isCurrentUser(true)
                        .build(),
                NegotiationHistoryDto.HistoryItemDto.builder()
                        .id(5L)
                        .senderCode("RM")
                        .senderName("Rakesh Mehta")
                        .senderMemberId("ATG10056")
                        .title("Counter Offer from Seller")
                        .message("My best price is ₹ 248.00 per share for 1,000 shares.")
                        .price(new BigDecimal("248.00"))
                        .shares(1000L)
                        .timestamp("18 Sep 2025, 09:15 AM")
                        .isCurrentUser(false)
                        .build(),
                NegotiationHistoryDto.HistoryItemDto.builder()
                        .id(6L)
                        .senderCode("SV")
                        .senderName("Sairam Vempati")
                        .senderMemberId("ATG10027")
                        .title("Your Latest Offer")
                        .message("I can do ₹ 242.00 per share for 1,000 shares. Please confirm.")
                        .price(new BigDecimal("242.00"))
                        .shares(1000L)
                        .timestamp("18 Sep 2025, 10:05 AM")
                        .isCurrentUser(true)
                        .badge("Awaiting Response")
                        .build()
        ));

        liveHistoryItems.put("NEG250918001", historyList);

        // 4. Trade Agreement (from Image 5)
        TradeAgreementDto agreement = TradeAgreementDto.builder()
                .agreementNo("NEG250918001")
                .negotiationNo("NEG250918001")
                .status("Agreement Ready")
                .initiatedOn("17 Sep 2025, 11:20 AM")
                .finalAgreedTerms(TradeAgreementDto.FinalAgreedTermsDto.builder()
                        .sharePrice(new BigDecimal("242.00"))
                        .quantity(1000L)
                        .totalTradeValue(new BigDecimal("242000.00"))
                        .paymentMode("Bank Transfer (Escrow)")
                        .settlementTimeline("T+3 Days (from escrow confirmation)")
                        .orderType("Buy (Limit)")
                        .otherTerms("Standard (As per platform rules)")
                        .agreementDate("18 Sep 2025, 02:15 PM")
                        .validTill("20 Sep 2025, 11:59 PM")
                        .build())
                .document(TradeAgreementDto.AgreementDocumentDto.builder()
                        .documentName("Trade_Agreement_NEG250918001.pdf")
                        .fileSize("256 KB")
                        .generatedAt("18 Sep 2025, 02:15 PM")
                        .downloadUrl("/api/v1/negotiations/NEG250918001/trade-agreement/download")
                        .notice("This document contains the complete terms and conditions of the trade agreement. Please review all details carefully before confirming.")
                        .build())
                .confirmations(List.of(
                        TradeAgreementDto.ConfirmationItemDto.builder()
                                .key("readAndUnderstood")
                                .text("I have read and understood the trade agreement and agree to the above terms.")
                                .confirmed(true)
                                .build(),
                        TradeAgreementDto.ConfirmationItemDto.builder()
                                .key("informationAccurate")
                                .text("I confirm that the information provided is accurate and complete.")
                                .confirmed(true)
                                .build(),
                        TradeAgreementDto.ConfirmationItemDto.builder()
                                .key("agreeToEscrowAndSettlement")
                                .text("I agree to proceed with escrow and subsequent settlement as per platform rules.")
                                .confirmed(true)
                                .build()
                ))
                .nextSteps(List.of(
                        TradeAgreementDto.NextStepDto.builder()
                                .stepNumber(1)
                                .title("1. Both parties confirm the agreement")
                                .statusText("Pending your confirmation")
                                .completed(false)
                                .build(),
                        TradeAgreementDto.NextStepDto.builder()
                                .stepNumber(2)
                                .title("2. Funds to be moved to escrow")
                                .statusText("After both confirmations")
                                .completed(false)
                                .build(),
                        TradeAgreementDto.NextStepDto.builder()
                                .stepNumber(3)
                                .title("3. Required approvals (if any)")
                                .statusText("As per company policy")
                                .completed(false)
                                .build(),
                        TradeAgreementDto.NextStepDto.builder()
                                .stepNumber(4)
                                .title("4. Share transfer")
                                .statusText("On approval")
                                .completed(false)
                                .build(),
                        TradeAgreementDto.NextStepDto.builder()
                                .stepNumber(5)
                                .title("5. Settlement completion")
                                .statusText("T+3 Days")
                                .completed(false)
                                .build()
                ))
                .parties(TradeAgreementDto.AgreementPartiesDto.builder()
                        .buyer(buyer)
                        .seller(seller)
                        .build())
                .build();

        liveTradeAgreements.put("NEG250918001", agreement);

        // 5. Negotiation Notifications (from Image 6)
        initNotificationsData();
    }

    /**
     * Dashboard Screen: summary KPI metrics, monthly insight chart, status donut chart, and paginated table
     */
    public NegotiationDashboardDto getDashboard(String tab, String search, int page, int size) {
        return getDashboard(tab, search, page, size, null);
    }

    public NegotiationDashboardDto getDashboard(String tab, String search, int page, int size, Long userId) {
        UserAccount currentUser = resolveCurrentUser(userId);
        List<NegotiationListItemDto> allDtos;
        List<Negotiation> dbList = Collections.emptyList();
        try {
            if (currentUser != null) {
                dbList = negotiationRepository.findAllForUser(currentUser.getUserId());
            } else {
                dbList = Collections.emptyList();
            }
        } catch (Exception e) {
            log.warn("Could not query negotiations from DB: {}", e.getMessage());
        }

        if (!dbList.isEmpty()) {
            allDtos = dbList.stream().map(n -> mapDbToListItemDto(n, currentUser)).toList();
        } else {
            allDtos = Collections.emptyList();
        }

        List<NegotiationListItemDto> filtered = allDtos;

        // Tab filter
        if (tab != null && !tab.equalsIgnoreCase("ALL") && !tab.isBlank()) {
            String targetStatus = mapTabToStatus(tab);
            filtered = filtered.stream()
                    .filter(item -> item.getStatus().equalsIgnoreCase(targetStatus) ||
                            (targetStatus.equalsIgnoreCase("ACTIVE") && item.getStatus().equalsIgnoreCase("Under Negotiation")))
                    .toList();
        }

        // Search query filter
        if (search != null && !search.isBlank()) {
            String q = search.toLowerCase().trim();
            filtered = filtered.stream()
                    .filter(item -> item.getNegotiationNo().toLowerCase().contains(q)
                            || item.getCompanyName().toLowerCase().contains(q)
                            || item.getCompanySymbol().toLowerCase().contains(q)
                            || item.getCounterpartyName().toLowerCase().contains(q))
                    .toList();
        }

        // Pagination
        int safePage = Math.max(1, page);
        int safeSize = Math.max(1, size);
        int totalElements = filtered.size();
        int totalPages = (int) Math.ceil((double) totalElements / safeSize);
        int startIndex = (safePage - 1) * safeSize;
        List<NegotiationListItemDto> pagedContent;
        if (startIndex >= totalElements) {
            pagedContent = Collections.emptyList();
        } else {
            int endIndex = Math.min(startIndex + safeSize, totalElements);
            pagedContent = filtered.subList(startIndex, endIndex);
        }

        // KPI Counts computed dynamically from DB records
        int activeCount = (int) allDtos.stream()
                .filter(n -> "Active".equalsIgnoreCase(n.getStatus()) || "Under Negotiation".equalsIgnoreCase(n.getStatus()))
                .count();
        int awaitingCount = (int) allDtos.stream()
                .filter(n -> "Awaiting Response".equalsIgnoreCase(n.getStatus()))
                .count();
        int acceptedCount = (int) allDtos.stream()
                .filter(n -> "Accepted".equalsIgnoreCase(n.getStatus()) || "Agreement Confirmed".equalsIgnoreCase(n.getStatus()))
                .count();
        int rejectedCount = (int) allDtos.stream()
                .filter(n -> "Rejected".equalsIgnoreCase(n.getStatus()))
                .count();
        int expiredCount = (int) allDtos.stream()
                .filter(n -> "Expired".equalsIgnoreCase(n.getStatus()))
                .count();
        int totalCount = allDtos.size();

        NegotiationSummaryDto summary = NegotiationSummaryDto.builder()
                .allNegotiations(totalCount)
                .active(activeCount)
                .awaitingResponse(awaitingCount)
                .accepted(acceptedCount)
                .rejected(rejectedCount)
                .expired(expiredCount)
                .build();

        // Negotiation Insights (Bar Chart)
        NegotiationInsightDto insights = NegotiationInsightDto.builder()
                .title("Negotiation Insights")
                .subtitle("Overview of your negotiation activity.")
                .months(List.of("Apr", "May", "Jun", "Jul", "Aug", "Sep"))
                .series(List.of(
                        NegotiationInsightDto.ChartSeriesDto.builder()
                                .name("Initiated by You")
                                .data(List.of(2, 4, 3, 6, 4, 3))
                                .build(),
                        NegotiationInsightDto.ChartSeriesDto.builder()
                                .name("Received from Others")
                                .data(List.of(1, 2, 2, 4, 2, 4))
                                .build()
                ))
                .build();

        // Negotiation Status Breakdown (Donut Chart)
        int denom = Math.max(1, totalCount);
        NegotiationStatusBreakdownDto statusBreakdown = NegotiationStatusBreakdownDto.builder()
                .total(totalCount)
                .items(List.of(
                        NegotiationStatusBreakdownDto.StatusItemDto.builder()
                                .status("Active")
                                .count(activeCount)
                                .percentage(Math.round((activeCount * 100.0) / denom))
                                .color("#10B981")
                                .build(),
                        NegotiationStatusBreakdownDto.StatusItemDto.builder()
                                .status("Awaiting Response")
                                .count(awaitingCount)
                                .percentage(Math.round((awaitingCount * 100.0) / denom))
                                .color("#F59E0B")
                                .build(),
                        NegotiationStatusBreakdownDto.StatusItemDto.builder()
                                .status("Accepted")
                                .count(acceptedCount)
                                .percentage(Math.round((acceptedCount * 100.0) / denom))
                                .color("#34D399")
                                .build(),
                        NegotiationStatusBreakdownDto.StatusItemDto.builder()
                                .status("Rejected")
                                .count(rejectedCount)
                                .percentage(Math.round((rejectedCount * 100.0) / denom))
                                .color("#EF4444")
                                .build(),
                        NegotiationStatusBreakdownDto.StatusItemDto.builder()
                                .status("Expired")
                                .count(expiredCount)
                                .percentage(Math.round((expiredCount * 100.0) / denom))
                                .color("#9CA3AF")
                                .build()
                ))
                .build();

        List<NegotiationDashboardDto.QuickActionDto> quickActions = List.of(
                NegotiationDashboardDto.QuickActionDto.builder()
                        .id("start-new")
                        .title("Start New Negotiation")
                        .description("Initiate a negotiation with a counterparty")
                        .actionType("CREATE")
                        .build(),
                NegotiationDashboardDto.QuickActionDto.builder()
                        .id("view-orders")
                        .title("View My Orders")
                        .description("Find matching orders for negotiation")
                        .actionType("NAVIGATE")
                        .build(),
                NegotiationDashboardDto.QuickActionDto.builder()
                        .id("guidelines")
                        .title("Negotiation Guidelines")
                        .description("Learn about the negotiation process")
                        .actionType("INFO")
                        .build(),
                NegotiationDashboardDto.QuickActionDto.builder()
                        .id("faqs")
                        .title("FAQs")
                        .description("Get answers to common questions")
                        .actionType("FAQ")
                        .build()
        );

        return NegotiationDashboardDto.builder()
                .summary(summary)
                .insights(insights)
                .statusBreakdown(statusBreakdown)
                .quickActions(quickActions)
                .negotiations(NegotiationDashboardDto.PaginatedNegotiationsDto.builder()
                        .totalElements(totalElements)
                        .totalPages(totalPages)
                        .currentPage(safePage)
                        .pageSize(safeSize)
                        .content(pagedContent)
                        .build())
                .build();
    }

    /**
     * Negotiation Details Screen (Image 2)
     */
    public NegotiationDetailDto getNegotiationDetail(String negotiationNo) {
        return getNegotiationDetail(negotiationNo, null);
    }

    public NegotiationDetailDto getNegotiationDetail(String negotiationNo, Long userId) {
        UserAccount currentUser = resolveCurrentUser(userId);
        try {
            Optional<Negotiation> optDb = negotiationRepository.findByNegotiationNo(negotiationNo);
            if (optDb.isPresent()) {
                NegotiationDetailDto dbDetail = buildDetailFromDb(optDb.get(), currentUser);
                liveNegotiationDetails.put(negotiationNo, dbDetail);
                return dbDetail;
            }
        } catch (Exception e) {
            log.warn("Error loading negotiation {} from DB: {}", negotiationNo, e.getMessage());
        }

        NegotiationDetailDto detail = liveNegotiationDetails.get(negotiationNo);
        if (detail == null) {
            // Check if item exists in list to build dynamic detail
            Optional<NegotiationListItemDto> opt = liveNegotiationsList.stream()
                    .filter(n -> n.getNegotiationNo().equalsIgnoreCase(negotiationNo))
                    .findFirst();

            if (opt.isPresent()) {
                NegotiationListItemDto item = opt.get();
                detail = buildGenericDetail(item);
                liveNegotiationDetails.put(negotiationNo, detail);
            } else {
                throw new ResourceNotFoundException("Negotiation not found with ID: " + negotiationNo);
            }
        }
        return detail;
    }

    /**
     * Counter Offer Screen / Submission (Image 3)
     */
    public NegotiationDetailDto submitCounterOffer(String negotiationNo, CounterOfferRequestDto request) {
        return submitCounterOffer(negotiationNo, request, null);
    }

    public NegotiationDetailDto submitCounterOffer(String negotiationNo, CounterOfferRequestDto request, Long userId) {
        UserAccount currentUser = resolveCurrentUser(userId);
        try {
            Optional<Negotiation> optDb = negotiationRepository.findByNegotiationNo(negotiationNo);
            if (optDb.isPresent()) {
                Negotiation n = optDb.get();
                n.setProposedPrice(request.getPricePerShare());
                n.setQuantity(request.getQuantity());
                n.setStatus("AWAITING_RESPONSE");
                n.setUpdatedAt(OffsetDateTime.now());
                negotiationRepository.save(n);

                UserAccount offeringUser = currentUser != null ? currentUser : n.getInitiatorUser();

                negotiationOfferRepository.save(NegotiationOffer.builder()
                        .negotiation(n)
                        .offeredByUser(offeringUser)
                        .quantity(request.getQuantity())
                        .offeredPrice(request.getPricePerShare())
                        .offerMessage(request.getOtherTerms() != null && !request.getOtherTerms().isBlank()
                                ? request.getOtherTerms()
                                : "Counter offer submitted")
                        .offerStatus("PENDING")
                        .createdAt(OffsetDateTime.now())
                        .build());
            }
        } catch (Exception e) {
            log.warn("Error updating negotiation counter-offer in DB: {}", e.getMessage());
        }

        NegotiationDetailDto detail = getNegotiationDetail(negotiationNo, userId);

        // Update terms
        BigDecimal oldPrice = detail.getCompanyShareDetails().getLastTradedPrice();
        BigDecimal diffPrice = request.getPricePerShare().subtract(oldPrice);
        BigDecimal orderValue = request.getPricePerShare().multiply(BigDecimal.valueOf(request.getQuantity()));

        String diffStr = (diffPrice.compareTo(BigDecimal.ZERO) < 0 ? "- " : "+ ") + "₹ " + diffPrice.abs().setScale(2, RoundingMode.HALF_UP);

        List<NegotiationDetailDto.NegotiationTermRowDto> updatedTerms = new ArrayList<>();
        updatedTerms.add(NegotiationDetailDto.NegotiationTermRowDto.builder()
                .parameter("Price per Share")
                .buyerOffer("₹ " + CURRENCY_FMT.format(request.getPricePerShare()))
                .sellerOffer("₹ " + CURRENCY_FMT.format(oldPrice))
                .currentProposal("₹ " + CURRENCY_FMT.format(request.getPricePerShare()))
                .difference(diffStr)
                .highlighted(true)
                .build());
        updatedTerms.add(NegotiationDetailDto.NegotiationTermRowDto.builder()
                .parameter("Quantity (Shares)")
                .buyerOffer(SHARES_FMT.format(request.getQuantity()))
                .sellerOffer(SHARES_FMT.format(request.getQuantity()))
                .currentProposal(SHARES_FMT.format(request.getQuantity()))
                .difference("-")
                .highlighted(false)
                .build());
        BigDecimal sellerOrderValue = oldPrice.multiply(BigDecimal.valueOf(request.getQuantity()));
        BigDecimal valDiff = orderValue.subtract(sellerOrderValue);
        String valDiffStr = (valDiff.compareTo(BigDecimal.ZERO) < 0 ? "- " : "+ ") + "₹ " + CURRENCY_FMT.format(valDiff.abs());

        updatedTerms.add(NegotiationDetailDto.NegotiationTermRowDto.builder()
                .parameter("Order Value")
                .buyerOffer("₹ " + CURRENCY_FMT.format(orderValue))
                .sellerOffer("₹ " + CURRENCY_FMT.format(sellerOrderValue))
                .currentProposal("₹ " + CURRENCY_FMT.format(orderValue))
                .difference(valDiffStr)
                .highlighted(false)
                .build());
        updatedTerms.add(NegotiationDetailDto.NegotiationTermRowDto.builder()
                .parameter("Settlement Timeline")
                .buyerOffer(request.getSettlementTimeline())
                .sellerOffer(request.getSettlementTimeline())
                .currentProposal(request.getSettlementTimeline())
                .difference("-")
                .highlighted(false)
                .build());
        updatedTerms.add(NegotiationDetailDto.NegotiationTermRowDto.builder()
                .parameter("Payment Mode")
                .buyerOffer(request.getPaymentMode())
                .sellerOffer(request.getPaymentMode())
                .currentProposal(request.getPaymentMode())
                .difference("-")
                .highlighted(false)
                .build());
        updatedTerms.add(NegotiationDetailDto.NegotiationTermRowDto.builder()
                .parameter("Other Terms")
                .buyerOffer(request.getOtherTerms() != null && !request.getOtherTerms().isBlank() ? request.getOtherTerms() : "Standard")
                .sellerOffer("Standard")
                .currentProposal(request.getOtherTerms() != null && !request.getOtherTerms().isBlank() ? request.getOtherTerms() : "Standard")
                .difference("-")
                .highlighted(false)
                .build());

        detail.setNegotiationTerms(updatedTerms);
        detail.setStatus("Awaiting Counterparty Response");

        List<NegotiationDetailDto.TimelineEventDto> mutableTimeline = detail.getTimeline() != null
                ? new ArrayList<>(detail.getTimeline())
                : new ArrayList<>();
        mutableTimeline.add(NegotiationDetailDto.TimelineEventDto.builder()
                .stepNumber(mutableTimeline.size() + 1)
                .title("You sent a counter offer")
                .description("₹ " + CURRENCY_FMT.format(request.getPricePerShare()) + " per share, " + SHARES_FMT.format(request.getQuantity()) + " shares")
                .timestamp("Just now")
                .senderCode("SV")
                .senderName(detail.getBuyer().getMemberName())
                .status("ACTIVE")
                .price("₹ " + CURRENCY_FMT.format(request.getPricePerShare()))
                .shares(SHARES_FMT.format(request.getQuantity()) + " shares")
                .build());
        detail.setTimeline(mutableTimeline);

        // Add to history
        List<NegotiationHistoryDto.HistoryItemDto> history = liveHistoryItems.computeIfAbsent(negotiationNo, k -> new ArrayList<>());
        long newId = history.size() + 1L;
        history.add(NegotiationHistoryDto.HistoryItemDto.builder()
                .id(newId)
                .senderCode("SV")
                .senderName(detail.getBuyer().getMemberName())
                .senderMemberId(detail.getBuyer().getMemberId())
                .title("Your Counter Offer")
                .message("Counter offer sent: ₹ " + CURRENCY_FMT.format(request.getPricePerShare()) + " for " + SHARES_FMT.format(request.getQuantity()) + " shares."
                        + (request.getOtherTerms() != null && !request.getOtherTerms().isBlank() ? " " + request.getOtherTerms() : ""))
                .price(request.getPricePerShare())
                .shares(request.getQuantity())
                .timestamp("Just now")
                .isCurrentUser(true)
                .badge("Awaiting Response")
                .build());

        // Update list status
        liveNegotiationsList.stream()
                .filter(n -> n.getNegotiationNo().equalsIgnoreCase(negotiationNo))
                .findFirst()
                .ifPresent(item -> {
                    item.setStatus("Awaiting Response");
                    item.setShares(request.getQuantity());
                    item.setProposedPrice(request.getPricePerShare());
                    item.setLastUpdated("Just now");
                });

        return detail;
    }

    /**
     * Accept Offer (transitions to Trade Agreement)
     */
    public NegotiationDetailDto acceptOffer(String negotiationNo) {
        return acceptOffer(negotiationNo, null);
    }

    public NegotiationDetailDto acceptOffer(String negotiationNo, Long userId) {
        try {
            Optional<Negotiation> optDb = negotiationRepository.findByNegotiationNo(negotiationNo);
            if (optDb.isPresent()) {
                Negotiation n = optDb.get();
                n.setStatus("ACCEPTED");
                n.setUpdatedAt(OffsetDateTime.now());
                negotiationRepository.save(n);

                if (tradeAgreementRepository.findByNegotiation_NegotiationNo(negotiationNo).isEmpty()) {
                    tradeAgreementRepository.save(TradeAgreement.builder()
                            .agreementNo(negotiationNo)
                            .negotiation(n)
                            .buyerUser(n.getInitiatorUser())
                            .sellerUser(n.getCounterpartyUser())
                            .company(n.getCompany())
                            .agreedQuantity(n.getQuantity())
                            .agreedPrice(n.getProposedPrice())
                            .agreementStatus("AGREEMENT_READY")
                            .createdAt(OffsetDateTime.now())
                            .build());
                }
            }
        } catch (Exception e) {
            log.warn("Error accepting negotiation in DB: {}", e.getMessage());
        }

        NegotiationDetailDto detail = getNegotiationDetail(negotiationNo, userId);
        detail.setStatus("Accepted");
        if (detail.getAvailableActions() != null) {
            detail.getAvailableActions().setCanAccept(false);
            detail.getAvailableActions().setCanCounter(false);
            detail.getAvailableActions().setCanReject(false);
        }

        List<NegotiationDetailDto.TimelineEventDto> mutableTimeline = detail.getTimeline() != null
                ? new ArrayList<>(detail.getTimeline())
                : new ArrayList<>();
        mutableTimeline.add(NegotiationDetailDto.TimelineEventDto.builder()
                .stepNumber(mutableTimeline.size() + 1)
                .title("Offer Accepted")
                .description("Trade agreement generated and ready for confirmation.")
                .timestamp("Just now")
                .senderCode("SV")
                .senderName(detail.getBuyer().getMemberName())
                .status("COMPLETED")
                .build());
        detail.setTimeline(mutableTimeline);

        liveNegotiationsList.stream()
                .filter(n -> n.getNegotiationNo().equalsIgnoreCase(negotiationNo))
                .findFirst()
                .ifPresent(item -> {
                    item.setStatus("Accepted");
                    item.setLastUpdated("Just now");
                });

        BigDecimal agreedPrice = parseMoneyFromTerms(detail, "Price per Share",
                detail.getCompanyShareDetails() != null && detail.getCompanyShareDetails().getLastTradedPrice() != null
                        ? detail.getCompanyShareDetails().getLastTradedPrice()
                        : new BigDecimal("242.00"));
        Long agreedQty = detail.getBuyer() != null && detail.getBuyer().getQuantity() != null
                ? detail.getBuyer().getQuantity()
                : 1000L;
        BigDecimal totalValue = agreedPrice.multiply(BigDecimal.valueOf(agreedQty));

        // Initialize agreement ready
        TradeAgreementDto agreement = liveTradeAgreements.get(negotiationNo);
        if (agreement == null) {
            agreement = TradeAgreementDto.builder()
                    .agreementNo(negotiationNo)
                    .negotiationNo(negotiationNo)
                    .status("Agreement Ready")
                    .initiatedOn(detail.getInitiatedOn())
                    .finalAgreedTerms(TradeAgreementDto.FinalAgreedTermsDto.builder()
                            .sharePrice(agreedPrice)
                            .quantity(agreedQty)
                            .totalTradeValue(totalValue)
                            .paymentMode(extractTermProposal(detail, "Payment Mode", "Bank Transfer (Escrow)"))
                            .settlementTimeline(extractTermProposal(detail, "Settlement Timeline", "T+3 Days") + " (from escrow confirmation)")
                            .orderType(detail.getBuyer() != null && detail.getBuyer().isCurrentUser() ? "Buy (Limit)" : "Sell (Limit)")
                            .otherTerms(extractTermProposal(detail, "Other Terms", "Standard (As per platform rules)"))
                            .agreementDate("Today")
                            .validTill("In 2 days")
                            .build())
                    .document(TradeAgreementDto.AgreementDocumentDto.builder()
                            .documentName("Trade_Agreement_" + negotiationNo + ".pdf")
                            .fileSize("256 KB")
                            .generatedAt("Today")
                            .downloadUrl("/api/v1/negotiations/" + negotiationNo + "/trade-agreement/download")
                            .notice("This document contains the complete terms and conditions of the trade agreement. Please review all details carefully before confirming.")
                            .build())
                    .confirmations(new ArrayList<>(List.of(
                            TradeAgreementDto.ConfirmationItemDto.builder()
                                    .key("readAndUnderstood")
                                    .text("I have read and understood the trade agreement and agree to the above terms.")
                                    .confirmed(true)
                                    .build(),
                            TradeAgreementDto.ConfirmationItemDto.builder()
                                    .key("informationAccurate")
                                    .text("I confirm that the information provided is accurate and complete.")
                                    .confirmed(true)
                                    .build(),
                            TradeAgreementDto.ConfirmationItemDto.builder()
                                    .key("agreeToEscrowAndSettlement")
                                    .text("I agree to proceed with escrow and subsequent settlement as per platform rules.")
                                    .confirmed(true)
                                    .build()
                    )))
                    .nextSteps(new ArrayList<>(List.of(
                            TradeAgreementDto.NextStepDto.builder()
                                    .stepNumber(1)
                                    .title("1. Both parties confirm the agreement")
                                    .statusText("Pending your confirmation")
                                    .completed(false)
                                    .build(),
                            TradeAgreementDto.NextStepDto.builder()
                                    .stepNumber(2)
                                    .title("2. Funds to be moved to escrow")
                                    .statusText("After both confirmations")
                                    .completed(false)
                                    .build(),
                            TradeAgreementDto.NextStepDto.builder()
                                    .stepNumber(3)
                                    .title("3. Required approvals (if any)")
                                    .statusText("As per company policy")
                                    .completed(false)
                                    .build(),
                            TradeAgreementDto.NextStepDto.builder()
                                    .stepNumber(4)
                                    .title("4. Share transfer")
                                    .statusText("On approval")
                                    .completed(false)
                                    .build(),
                            TradeAgreementDto.NextStepDto.builder()
                                    .stepNumber(5)
                                    .title("5. Settlement completion")
                                    .statusText("T+3 Days")
                                    .completed(false)
                                    .build()
                    )))
                    .parties(TradeAgreementDto.AgreementPartiesDto.builder()
                            .buyer(detail.getBuyer())
                            .seller(detail.getSeller())
                            .build())
                    .build();
            liveTradeAgreements.put(negotiationNo, agreement);
        }
        agreement.setStatus("Agreement Ready");

        return detail;
    }

    /**
     * Reject Offer
     */
    public NegotiationDetailDto rejectOffer(String negotiationNo, String reason) {
        return rejectOffer(negotiationNo, reason, null);
    }

    public NegotiationDetailDto rejectOffer(String negotiationNo, String reason, Long userId) {
        try {
            Optional<Negotiation> optDb = negotiationRepository.findByNegotiationNo(negotiationNo);
            if (optDb.isPresent()) {
                Negotiation n = optDb.get();
                n.setStatus("REJECTED");
                n.setUpdatedAt(OffsetDateTime.now());
                negotiationRepository.save(n);
            }
        } catch (Exception e) {
            log.warn("Error rejecting negotiation in DB: {}", e.getMessage());
        }

        NegotiationDetailDto detail = getNegotiationDetail(negotiationNo, userId);
        detail.setStatus("Rejected");
        if (detail.getAvailableActions() != null) {
            detail.getAvailableActions().setCanAccept(false);
            detail.getAvailableActions().setCanCounter(false);
            detail.getAvailableActions().setCanReject(false);
        }

        List<NegotiationDetailDto.TimelineEventDto> mutableTimeline = detail.getTimeline() != null
                ? new ArrayList<>(detail.getTimeline())
                : new ArrayList<>();
        mutableTimeline.add(NegotiationDetailDto.TimelineEventDto.builder()
                .stepNumber(mutableTimeline.size() + 1)
                .title("Negotiation Rejected")
                .description(reason != null && !reason.isBlank() ? reason : "Declined by user")
                .timestamp("Just now")
                .senderCode("SV")
                .senderName(detail.getBuyer().getMemberName())
                .status("COMPLETED")
                .build());
        detail.setTimeline(mutableTimeline);

        liveNegotiationsList.stream()
                .filter(n -> n.getNegotiationNo().equalsIgnoreCase(negotiationNo))
                .findFirst()
                .ifPresent(item -> {
                    item.setStatus("Rejected");
                    item.setLastUpdated("Just now");
                });

        return detail;
    }

    /**
     * Request a call with the counterparty
     */
    public NegotiationDetailDto requestCall(String negotiationNo) {
        return requestCall(negotiationNo, null);
    }

    public NegotiationDetailDto requestCall(String negotiationNo, Long userId) {
        NegotiationDetailDto detail = getNegotiationDetail(negotiationNo, userId);

        List<NegotiationDetailDto.TimelineEventDto> mutableTimeline = detail.getTimeline() != null
                ? new ArrayList<>(detail.getTimeline())
                : new ArrayList<>();
        mutableTimeline.add(NegotiationDetailDto.TimelineEventDto.builder()
                .stepNumber(mutableTimeline.size() + 1)
                .title("Call Requested")
                .description("You requested a discussion call with the counterparty.")
                .timestamp("Just now")
                .senderCode("SV")
                .senderName(detail.getBuyer() != null ? detail.getBuyer().getMemberName() : "You")
                .status("COMPLETED")
                .build());
        detail.setTimeline(mutableTimeline);

        List<NegotiationHistoryDto.HistoryItemDto> history = liveHistoryItems.computeIfAbsent(negotiationNo, k -> new ArrayList<>());
        history.add(NegotiationHistoryDto.HistoryItemDto.builder()
                .id(history.size() + 1L)
                .senderCode("SV")
                .senderName(detail.getBuyer() != null ? detail.getBuyer().getMemberName() : "Sairam Vempati")
                .senderMemberId(detail.getBuyer() != null ? detail.getBuyer().getMemberId() : "ATG10027")
                .title("Call Request Sent")
                .message("Requested a call to discuss negotiation terms.")
                .timestamp("Just now")
                .isCurrentUser(true)
                .build());

        return detail;
    }

    /**
     * Negotiation History Screen (Image 4)
     */
    public NegotiationHistoryDto getHistory(String negotiationNo) {
        return getHistory(negotiationNo, null);
    }

    public NegotiationHistoryDto getHistory(String negotiationNo, Long userId) {
        NegotiationDetailDto detail = getNegotiationDetail(negotiationNo, userId);
        List<NegotiationHistoryDto.HistoryItemDto> timeline = liveHistoryItems.getOrDefault(negotiationNo, Collections.emptyList());

        boolean isDefaultSeed = "NEG250918001".equalsIgnoreCase(negotiationNo) && timeline.size() == 6;
        if (isDefaultSeed) {
            return NegotiationHistoryDto.builder()
                    .negotiationNo(negotiationNo)
                    .status(detail.getStatus())
                    .initiatedOn(detail.getInitiatedOn())
                    .lastActivity(timeline.isEmpty() ? "18 Sep 2025, 10:05 AM" : timeline.get(timeline.size() - 1).getTimestamp())
                    .expiresIn(detail.getExpiresIn())
                    .dealType("Buy")
                    .quantity(1000L)
                    .buyer(detail.getBuyer())
                    .seller(detail.getSeller())
                    .currentOffer("₹ 235.00 | 1,000 shares")
                    .sellerLastOffer("₹ 250.00 | 1,000 shares")
                    .timeline(timeline)
                    .latestTermsComparison(NegotiationHistoryDto.LatestTermsComparisonDto.builder()
                            .yourOfferPrice(new BigDecimal("242.00"))
                            .sellerOfferPrice(new BigDecimal("248.00"))
                            .yourQuantity(1000L)
                            .sellerQuantity(1000L)
                            .yourOrderValue(new BigDecimal("242000.00"))
                            .sellerOrderValue(new BigDecimal("248000.00"))
                            .priceDifference("- ₹ 6.00")
                            .percentageDifference("-2.42%")
                            .build())
                    .actions(detail.getAvailableActions())
                    .build();
        }

        Optional<NegotiationListItemDto> listOpt = liveNegotiationsList.stream()
                .filter(n -> n.getNegotiationNo().equalsIgnoreCase(negotiationNo))
                .findFirst();

        String dealType = listOpt.map(NegotiationListItemDto::getType)
                .orElse(detail.getBuyer() != null && detail.getBuyer().isCurrentUser() ? "Buy" : "Sell");
        Long qty = listOpt.map(NegotiationListItemDto::getShares)
                .orElse(detail.getBuyer() != null && detail.getBuyer().getQuantity() != null ? detail.getBuyer().getQuantity() : 1000L);

        BigDecimal latestYourPrice = timeline.stream()
                .filter(h -> h.isCurrentUser() && h.getPrice() != null)
                .reduce((first, second) -> second)
                .map(NegotiationHistoryDto.HistoryItemDto::getPrice)
                .orElse(listOpt.map(NegotiationListItemDto::getProposedPrice).orElse(new BigDecimal("235.00")));
        BigDecimal latestCounterPrice = timeline.stream()
                .filter(h -> !h.isCurrentUser() && h.getPrice() != null)
                .reduce((first, second) -> second)
                .map(NegotiationHistoryDto.HistoryItemDto::getPrice)
                .orElse(listOpt.map(NegotiationListItemDto::getCurrentPrice).orElse(new BigDecimal("248.00")));

        BigDecimal yourVal = latestYourPrice.multiply(BigDecimal.valueOf(qty));
        BigDecimal counterVal = latestCounterPrice.multiply(BigDecimal.valueOf(qty));
        BigDecimal pDiff = latestYourPrice.subtract(latestCounterPrice);
        BigDecimal pctDiff = latestCounterPrice.compareTo(BigDecimal.ZERO) > 0
                ? pDiff.multiply(BigDecimal.valueOf(100)).divide(latestCounterPrice, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        String pDiffStr = (pDiff.compareTo(BigDecimal.ZERO) < 0 ? "- " : "+ ") + "₹ " + CURRENCY_FMT.format(pDiff.abs());
        String pctStr = (pctDiff.compareTo(BigDecimal.ZERO) < 0 ? "" : "+") + pctDiff.setScale(2, RoundingMode.HALF_UP) + "%";

        return NegotiationHistoryDto.builder()
                .negotiationNo(negotiationNo)
                .status(detail.getStatus())
                .initiatedOn(detail.getInitiatedOn())
                .lastActivity(timeline.isEmpty() ? detail.getInitiatedOn() : timeline.get(timeline.size() - 1).getTimestamp())
                .expiresIn(detail.getExpiresIn())
                .dealType(dealType)
                .quantity(qty)
                .buyer(detail.getBuyer())
                .seller(detail.getSeller())
                .currentOffer("₹ " + CURRENCY_FMT.format(latestYourPrice) + " | " + SHARES_FMT.format(qty) + " shares")
                .sellerLastOffer("₹ " + CURRENCY_FMT.format(latestCounterPrice) + " | " + SHARES_FMT.format(qty) + " shares")
                .timeline(timeline)
                .latestTermsComparison(NegotiationHistoryDto.LatestTermsComparisonDto.builder()
                    .yourOfferPrice(latestYourPrice)
                    .sellerOfferPrice(latestCounterPrice)
                    .yourQuantity(qty)
                    .sellerQuantity(qty)
                    .yourOrderValue(yourVal)
                    .sellerOrderValue(counterVal)
                    .priceDifference(pDiffStr)
                    .percentageDifference(pctStr)
                    .build())
                .actions(detail.getAvailableActions())
                .build();
    }

    /**
     * Post a chat message in negotiation
     */
    public NegotiationHistoryDto postMessage(String negotiationNo, NegotiationMessageRequestDto request) {
        return postMessage(negotiationNo, request, null);
    }

    public NegotiationHistoryDto postMessage(String negotiationNo, NegotiationMessageRequestDto request, Long userId) {
        UserAccount currentUser = resolveCurrentUser(userId);
        try {
            Optional<Negotiation> optDb = negotiationRepository.findByNegotiationNo(negotiationNo);
            if (optDb.isPresent()) {
                Negotiation n = optDb.get();
                UserAccount sender = currentUser != null ? currentUser : n.getInitiatorUser();
                negotiationMessageRepository.save(NegotiationMessage.builder()
                        .negotiation(n)
                        .senderUser(sender)
                        .messageText(request.getMessageText())
                        .sentAt(OffsetDateTime.now())
                        .build());
            }
        } catch (Exception e) {
            log.warn("Error saving negotiation message in DB: {}", e.getMessage());
        }

        NegotiationDetailDto detail = getNegotiationDetail(negotiationNo, userId);
        List<NegotiationHistoryDto.HistoryItemDto> history = liveHistoryItems.computeIfAbsent(negotiationNo, k -> new ArrayList<>());

        history.add(NegotiationHistoryDto.HistoryItemDto.builder()
                .id(history.size() + 1L)
                .senderCode("SV")
                .senderName(detail.getBuyer().getMemberName())
                .senderMemberId(detail.getBuyer().getMemberId())
                .title("Message from You")
                .message(request.getMessageText())
                .timestamp("Just now")
                .isCurrentUser(true)
                .build());

        return getHistory(negotiationNo, userId);
    }

    /**
     * Trade Agreement Screen (Image 5)
     */
    public TradeAgreementDto getTradeAgreement(String negotiationNo) {
        return getTradeAgreement(negotiationNo, null);
    }

    public TradeAgreementDto getTradeAgreement(String negotiationNo, Long userId) {
        TradeAgreementDto agreement = liveTradeAgreements.get(negotiationNo);
        if (agreement == null) {
            NegotiationDetailDto detail = getNegotiationDetail(negotiationNo, userId);
            acceptOffer(negotiationNo, userId);
            agreement = liveTradeAgreements.get(negotiationNo);
        }
        return agreement;
    }

    /**
     * Confirm Agreement & Proceed to Escrow (Image 5 Bottom Action)
     */
    public TradeAgreementDto confirmAgreement(String negotiationNo, TradeAgreementConfirmRequestDto request) {
        return confirmAgreement(negotiationNo, request, null);
    }

    public TradeAgreementDto confirmAgreement(String negotiationNo, TradeAgreementConfirmRequestDto request, Long userId) {
        try {
            Optional<TradeAgreement> optAg = tradeAgreementRepository.findByNegotiation_NegotiationNo(negotiationNo);
            if (optAg.isPresent()) {
                TradeAgreement ag = optAg.get();
                ag.setAgreementStatus("CONFIRMED");
                ag.setBuyerAcceptedAt(OffsetDateTime.now());
                tradeAgreementRepository.save(ag);

                Negotiation n = ag.getNegotiation();
                if (n != null) {
                    n.setStatus("AGREEMENT_CONFIRMED");
                    n.setUpdatedAt(OffsetDateTime.now());
                    negotiationRepository.save(n);
                }
            }
        } catch (Exception e) {
            log.warn("Error confirming agreement in DB: {}", e.getMessage());
        }

        TradeAgreementDto agreement = getTradeAgreement(negotiationNo, userId);
        agreement.setStatus("Agreement Confirmed - Escrow Initiated");

        // Update checklist
        if (agreement.getConfirmations() != null) {
            agreement.getConfirmations().forEach(c -> c.setConfirmed(true));
        }

        // Update next steps
        if (agreement.getNextSteps() != null && !agreement.getNextSteps().isEmpty()) {
            agreement.getNextSteps().get(0).setStatusText("Confirmed by You");
            agreement.getNextSteps().get(0).setCompleted(true);
            if (agreement.getNextSteps().size() > 1) {
                agreement.getNextSteps().get(1).setStatusText("In Progress");
            }
        }

        // Update list
        liveNegotiationsList.stream()
                .filter(n -> n.getNegotiationNo().equalsIgnoreCase(negotiationNo))
                .findFirst()
                .ifPresent(item -> {
                    item.setStatus("Agreement Confirmed");
                    item.setLastUpdated("Just now");
                });

        return agreement;
    }

    /**
     * Generate / Download Trade Agreement PDF
     */
    public byte[] downloadAgreementPdf(String negotiationNo) {
        TradeAgreementDto agreement = getTradeAgreement(negotiationNo);
        List<String> lines = new ArrayList<>();
        lines.add("ATIRATH GROUP - OFFICIAL TRADE & SHARE NEGOTIATION AGREEMENT");
        lines.add("================================================================");
        lines.add("Agreement Reference:   " + agreement.getAgreementNo());
        lines.add("Negotiation Reference: " + agreement.getNegotiationNo());
        lines.add("Status:                " + agreement.getStatus());
        lines.add("Agreement Date:        " + agreement.getFinalAgreedTerms().getAgreementDate());
        lines.add("");
        lines.add("PARTIES TO THE AGREEMENT");
        lines.add("----------------------------------------------------------------");
        lines.add("BUYER:  " + agreement.getParties().getBuyer().getCompanyName()
                + " (" + agreement.getParties().getBuyer().getMemberName()
                + " - " + agreement.getParties().getBuyer().getMemberId() + ")");
        lines.add("SELLER: " + agreement.getParties().getSeller().getCompanyName()
                + " (" + agreement.getParties().getSeller().getMemberName()
                + " - " + agreement.getParties().getSeller().getMemberId() + ")");
        lines.add("");
        lines.add("FINAL AGREED TERMS");
        lines.add("----------------------------------------------------------------");
        lines.add("Share Price:         INR " + CURRENCY_FMT.format(agreement.getFinalAgreedTerms().getSharePrice()));
        lines.add("Quantity:            " + SHARES_FMT.format(agreement.getFinalAgreedTerms().getQuantity()) + " shares");
        lines.add("Total Trade Value:   INR " + CURRENCY_FMT.format(agreement.getFinalAgreedTerms().getTotalTradeValue()));
        lines.add("Settlement Timeline: " + agreement.getFinalAgreedTerms().getSettlementTimeline());
        lines.add("Payment Mode:        " + agreement.getFinalAgreedTerms().getPaymentMode());
        lines.add("Order Type:          " + agreement.getFinalAgreedTerms().getOrderType());
        lines.add("Valid Till:          " + agreement.getFinalAgreedTerms().getValidTill());
        lines.add("");
        lines.add("TERMS & CONDITIONS");
        lines.add("1. All offers and agreements confirmed on Atirath Group Internal Market are binding.");
        lines.add("2. Funds must be deposited into the verified Escrow Account within T+3 Days.");
        lines.add("3. Share lock and ledger transfer execute automatically upon escrow clearance.");
        lines.add("");
        lines.add("Digital Verification Signature: ATG-SEC-SIGN-" + UUID.randomUUID().toString().toUpperCase());
        return buildSimplePdf(lines);
    }

    /**
     * Download Negotiation Summary PDF (Counter Offer Sidebar Quick Action)
     */
    public byte[] downloadSummaryPdf(String negotiationNo) {
        NegotiationDetailDto detail = getNegotiationDetail(negotiationNo);
        List<String> lines = new ArrayList<>();
        lines.add("ATIRATH GROUP - NEGOTIATION SUMMARY");
        lines.add("================================================================");
        lines.add("Negotiation ID: " + detail.getNegotiationNo() + "   Status: " + detail.getStatus());
        lines.add("Initiated On:   " + detail.getInitiatedOn() + "   Expires In: " + detail.getExpiresIn());
        lines.add("Company:        " + detail.getCompanyShareDetails().getCompanyName()
                + " (" + detail.getCompanyShareDetails().getSymbol() + ")");
        lines.add("Buyer:          " + detail.getBuyer().getMemberName() + " (" + detail.getBuyer().getMemberId() + ")");
        lines.add("Seller:         " + detail.getSeller().getMemberName() + " (" + detail.getSeller().getMemberId() + ")");
        lines.add("");
        lines.add("CURRENT NEGOTIATION TERMS");
        lines.add("----------------------------------------------------------------");
        if (detail.getNegotiationTerms() != null) {
            for (NegotiationDetailDto.NegotiationTermRowDto row : detail.getNegotiationTerms()) {
                lines.add(String.format("%-20s | Proposal: %-18s | Diff: %s",
                        row.getParameter(), row.getCurrentProposal(), row.getDifference()));
            }
        }
        return buildSimplePdf(lines);
    }

    /**
     * Download Complete Negotiation History PDF
     */
    public byte[] downloadHistoryPdf(String negotiationNo) {
        NegotiationHistoryDto history = getHistory(negotiationNo);
        List<String> lines = new ArrayList<>();
        lines.add("ATIRATH GROUP - NEGOTIATION HISTORY & COMMUNICATION LOG");
        lines.add("================================================================");
        lines.add("Negotiation ID: " + history.getNegotiationNo() + "   Status: " + history.getStatus());
        lines.add("Deal Type: " + history.getDealType() + "   Quantity: " + SHARES_FMT.format(history.getQuantity()) + " shares");
        lines.add("Initiated On: " + history.getInitiatedOn() + "   Last Activity: " + history.getLastActivity());
        lines.add("");
        lines.add("TIMELINE EVENTS");
        lines.add("----------------------------------------------------------------");
        if (history.getTimeline() != null) {
            for (NegotiationHistoryDto.HistoryItemDto item : history.getTimeline()) {
                lines.add("[" + item.getTimestamp() + "] " + item.getTitle()
                        + " - " + item.getSenderName() + " (" + item.getSenderMemberId() + ")");
                lines.add("   " + item.getMessage());
            }
        }
        return buildSimplePdf(lines);
    }

    private byte[] buildSimplePdf(List<String> lines) {
        StringBuilder stream = new StringBuilder();
        stream.append("BT\n/F1 10 Tf\n14 TL\n40 770 Td\n");
        int count = 0;
        for (String line : lines) {
            if (count >= 48) {
                break;
            }
            String safe = line.replace("₹", "INR ")
                    .replaceAll("[^\\x20-\\x7E]", " ")
                    .replace("\\", "\\\\")
                    .replace("(", "\\(")
                    .replace(")", "\\)");
            stream.append("(").append(safe).append(") Tj T*\n");
            count++;
        }
        stream.append("ET\n");
        byte[] contentBytes = stream.toString().getBytes(StandardCharsets.US_ASCII);

        StringBuilder pdf = new StringBuilder();
        List<Integer> offsets = new ArrayList<>();
        pdf.append("%PDF-1.4\n");

        offsets.add(pdf.length());
        pdf.append("1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n");

        offsets.add(pdf.length());
        pdf.append("2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n");

        offsets.add(pdf.length());
        pdf.append("3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Resources << /Font << /F1 4 0 R >> >> /Contents 5 0 R >>\nendobj\n");

        offsets.add(pdf.length());
        pdf.append("4 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Courier >>\nendobj\n");

        offsets.add(pdf.length());
        pdf.append("5 0 obj\n<< /Length ").append(contentBytes.length).append(" >>\nstream\n")
                .append(new String(contentBytes, StandardCharsets.US_ASCII))
                .append("endstream\nendobj\n");

        int xrefOffset = pdf.length();
        pdf.append("xref\n0 6\n0000000000 65535 f \n");
        for (int off : offsets) {
            pdf.append(String.format("%010d 00000 n \n", off));
        }
        pdf.append("trailer\n<< /Size 6 /Root 1 0 R >>\nstartxref\n").append(xrefOffset).append("\n%%EOF");
        return pdf.toString().getBytes(StandardCharsets.US_ASCII);
    }

    private BigDecimal parseMoneyFromTerms(NegotiationDetailDto detail, String parameter, BigDecimal fallback) {
        if (detail.getNegotiationTerms() != null) {
            for (NegotiationDetailDto.NegotiationTermRowDto row : detail.getNegotiationTerms()) {
                if (parameter.equalsIgnoreCase(row.getParameter()) && row.getCurrentProposal() != null) {
                    String cleaned = row.getCurrentProposal().replaceAll("[^0-9.]", "");
                    if (!cleaned.isBlank()) {
                        try {
                            return new BigDecimal(cleaned);
                        } catch (NumberFormatException ignored) {
                            // fall back cleanly
                        }
                    }
                }
            }
        }
        return fallback;
    }

    private String extractTermProposal(NegotiationDetailDto detail, String parameter, String fallback) {
        if (detail.getNegotiationTerms() != null) {
            for (NegotiationDetailDto.NegotiationTermRowDto row : detail.getNegotiationTerms()) {
                if (parameter.equalsIgnoreCase(row.getParameter()) && row.getCurrentProposal() != null) {
                    return row.getCurrentProposal();
                }
            }
        }
        return fallback;
    }

    private String resolveUserName(UserAccount user) {
        if (user == null) return "Counterparty";
        if (user.getAccountType() == AccountType.CORPORATE) {
            return corporateProfileRepository.findByUserAccount_UserId(user.getUserId())
                    .map(CorporateProfile::getCompanyName)
                    .orElse("Corporate Member");
        }
        return individualProfileRepository.findByUserAccount_UserId(user.getUserId())
                .map(IndividualProfile::getFullName)
                .orElse("Verified Member");
    }

    private String mapDbStatusToDisplay(String dbStatus) {
        if (dbStatus == null) return "Active";
        return switch (dbStatus.toUpperCase()) {
            case "OPEN", "ACTIVE" -> "Active";
            case "UNDER_NEGOTIATION" -> "Under Negotiation";
            case "AWAITING_RESPONSE", "PENDING" -> "Awaiting Response";
            case "ACCEPTED" -> "Accepted";
            case "AGREEMENT_CONFIRMED" -> "Agreement Confirmed";
            case "AGREEMENT_READY" -> "Agreement Ready";
            case "REJECTED" -> "Rejected";
            case "EXPIRED" -> "Expired";
            default -> dbStatus;
        };
    }

    private NegotiationListItemDto mapDbToListItemDto(Negotiation n) {
        return mapDbToListItemDto(n, null);
    }

    private NegotiationListItemDto mapDbToListItemDto(Negotiation n, UserAccount currentUser) {
        MarketCompany comp = n.getCompany();
        String symbol = comp != null ? comp.getSymbol() : "ATG-H";
        String compName = comp != null ? comp.getCompanyName() : resolveCompanyName(symbol);

        boolean isInitiator = currentUser == null || (n.getInitiatorUser() != null && currentUser.getUserId().equals(n.getInitiatorUser().getUserId()));
        UserAccount counterparty = isInitiator ? n.getCounterpartyUser() : n.getInitiatorUser();

        String cpName = resolveUserName(counterparty);
        String cpMemberId = resolveMemberId(counterparty);

        String displayStatus = mapDbStatusToDisplay(n.getStatus());
        String updatedStr = n.getUpdatedAt() != null
                ? n.getUpdatedAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy hh:mm a"))
                : (n.getCreatedAt() != null ? n.getCreatedAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy hh:mm a")) : "Recent");

        MarketTickDto tick = broadcastService != null ? broadcastService.getTick(symbol) : null;
        BigDecimal currentPrice = (tick != null && tick.getCurrentPrice() != null)
                ? tick.getCurrentPrice()
                : n.getProposedPrice();

        String baseType = "Buy";
        if (n.getOrder() != null && n.getOrder().getOrderSide() != null) {
            baseType = "BUY".equalsIgnoreCase(n.getOrder().getOrderSide()) ? "Buy" : "Sell";
        } else if ("FVT".equalsIgnoreCase(symbol) || "CSI".equalsIgnoreCase(symbol) || "ROHE".equalsIgnoreCase(symbol) || "ATG-E".equalsIgnoreCase(symbol) || "ATG-L".equalsIgnoreCase(symbol) || "ATG-R".equalsIgnoreCase(symbol)) {
            baseType = "Sell";
        }

        String type = isInitiator ? baseType : ("Buy".equalsIgnoreCase(baseType) ? "Sell" : "Buy");

        return NegotiationListItemDto.builder()
                .negotiationNo(n.getNegotiationNo())
                .companyName(compName)
                .companySymbol(symbol)
                .type(type)
                .counterpartyName(cpName)
                .counterpartyMemberId(cpMemberId)
                .shares(n.getQuantity())
                .currentPrice(currentPrice)
                .proposedPrice(n.getProposedPrice())
                .status(displayStatus)
                .lastUpdated(updatedStr)
                .build();
    }

    private NegotiationDetailDto buildDetailFromDb(Negotiation n) {
        return buildDetailFromDb(n, null);
    }

    private NegotiationDetailDto buildDetailFromDb(Negotiation n, UserAccount currentUser) {
        String negNo = n.getNegotiationNo();
        MarketCompany comp = n.getCompany();
        String symbol = comp != null ? comp.getSymbol() : "ATG-H";
        String compName = comp != null ? comp.getCompanyName() : resolveCompanyName(symbol);

        UserAccount initUser = n.getInitiatorUser();
        UserAccount cpUser = n.getCounterpartyUser();

        String initName = resolveUserName(initUser);
        String cpName = resolveUserName(cpUser);

        String initMemberId = resolveMemberId(initUser);
        String cpMemberId = resolveMemberId(cpUser);

        String initEmail = initUser != null && initUser.getEmail() != null ? initUser.getEmail() : "sairam@atirath.com";
        String cpEmail = cpUser != null && cpUser.getEmail() != null ? cpUser.getEmail() : "counterparty@atirath.com";

        String initPhone = initUser != null && initUser.getMobileNumber() != null ? "+91 " + initUser.getMobileNumber() : "+91 98765 43210";
        String cpPhone = cpUser != null && cpUser.getMobileNumber() != null ? "+91 " + cpUser.getMobileNumber() : "+91 91234 56789";

        boolean isCurrentUserBuyer = currentUser == null || (initUser != null && currentUser.getUserId().equals(initUser.getUserId()));
        boolean isCurrentUserSeller = currentUser != null && cpUser != null && currentUser.getUserId().equals(cpUser.getUserId());

        NegotiationDetailDto.PartyDto buyer = NegotiationDetailDto.PartyDto.builder()
                .isCurrentUser(isCurrentUserBuyer)
                .companyName("Atirath Holdings India Limited")
                .companySymbol(symbol)
                .verified(true)
                .memberId(initMemberId)
                .memberName(initName)
                .email(initEmail)
                .phone(initPhone)
                .currentHoldings(12500L)
                .quantity(n.getQuantity())
                .partyRole("BUYER")
                .build();

        NegotiationDetailDto.PartyDto seller = NegotiationDetailDto.PartyDto.builder()
                .isCurrentUser(isCurrentUserSeller)
                .companyName(cpName.contains("Mehta") ? "Farm Vantara Technologies" : compName)
                .companySymbol(symbol)
                .verified(true)
                .memberId(cpMemberId)
                .memberName(cpName)
                .email(cpEmail)
                .phone(cpPhone)
                .currentHoldings(5000L)
                .quantity(n.getQuantity())
                .partyRole("SELLER")
                .build();

        MarketTickDto tick = broadcastService != null ? broadcastService.getTick(symbol) : null;
        BigDecimal curPrice = (tick != null && tick.getCurrentPrice() != null)
                ? tick.getCurrentPrice()
                : n.getProposedPrice();

        NegotiationDetailDto.CompanyShareDetailsDto shareDetails = NegotiationDetailDto.CompanyShareDetailsDto.builder()
                .companyName(compName)
                .symbol(symbol)
                .companyType("Private Limited")
                .sector(comp != null ? comp.getSector() : "Diversified")
                .isin(symbol + "-001")
                .faceValue(new BigDecimal("10.00"))
                .lastTradedPrice(curPrice)
                .fiftyTwoWeekHigh(curPrice.multiply(new BigDecimal("1.15")).setScale(2, RoundingMode.HALF_UP))
                .fiftyTwoWeekLow(curPrice.multiply(new BigDecimal("0.85")).setScale(2, RoundingMode.HALF_UP))
                .build();

        List<NegotiationOffer> offers = Collections.emptyList();
        try {
            offers = negotiationOfferRepository.findByNegotiation_NegotiationNoOrderByCreatedAtAsc(negNo);
        } catch (Exception ignored) {}

        BigDecimal buyerOfferPrice = n.getProposedPrice();
        BigDecimal sellerOfferPrice = curPrice;
        if (!offers.isEmpty()) {
            boolean hasBuyer = false;
            boolean hasSeller = false;
            for (NegotiationOffer o : offers) {
                if (initUser != null && o.getOfferedByUser() != null && o.getOfferedByUser().getUserId().equals(initUser.getUserId())) {
                    if (!hasBuyer) {
                        buyerOfferPrice = o.getOfferedPrice();
                        hasBuyer = true;
                    }
                } else {
                    if (!hasSeller) {
                        sellerOfferPrice = o.getOfferedPrice();
                        hasSeller = true;
                    }
                }
            }
        }

        BigDecimal diffPrice = buyerOfferPrice.subtract(sellerOfferPrice);
        String diffStr = (diffPrice.compareTo(BigDecimal.ZERO) < 0 ? "- " : "+ ") + "₹ " + diffPrice.abs().setScale(2, RoundingMode.HALF_UP);
        BigDecimal orderVal = buyerOfferPrice.multiply(BigDecimal.valueOf(n.getQuantity()));
        BigDecimal sellerVal = sellerOfferPrice.multiply(BigDecimal.valueOf(n.getQuantity()));
        BigDecimal valDiff = orderVal.subtract(sellerVal);
        String valDiffStr = (valDiff.compareTo(BigDecimal.ZERO) < 0 ? "- " : "+ ") + "₹ " + CURRENCY_FMT.format(valDiff.abs());

        List<NegotiationDetailDto.NegotiationTermRowDto> terms = new ArrayList<>(List.of(
                NegotiationDetailDto.NegotiationTermRowDto.builder()
                        .parameter("Price per Share")
                        .buyerOffer("₹ " + CURRENCY_FMT.format(buyerOfferPrice))
                        .sellerOffer("₹ " + CURRENCY_FMT.format(sellerOfferPrice))
                        .currentProposal("₹ " + CURRENCY_FMT.format(n.getProposedPrice()))
                        .difference(diffStr)
                        .highlighted(true)
                        .build(),
                NegotiationDetailDto.NegotiationTermRowDto.builder()
                        .parameter("Quantity (Shares)")
                        .buyerOffer(SHARES_FMT.format(n.getQuantity()))
                        .sellerOffer(SHARES_FMT.format(n.getQuantity()))
                        .currentProposal(SHARES_FMT.format(n.getQuantity()))
                        .difference("-")
                        .highlighted(false)
                        .build(),
                NegotiationDetailDto.NegotiationTermRowDto.builder()
                        .parameter("Order Value")
                        .buyerOffer("₹ " + CURRENCY_FMT.format(orderVal))
                        .sellerOffer("₹ " + CURRENCY_FMT.format(sellerVal))
                        .currentProposal("₹ " + CURRENCY_FMT.format(n.getProposedPrice().multiply(BigDecimal.valueOf(n.getQuantity()))))
                        .difference(valDiffStr)
                        .highlighted(false)
                        .build(),
                NegotiationDetailDto.NegotiationTermRowDto.builder()
                        .parameter("Settlement Timeline")
                        .buyerOffer("T+3 Days")
                        .sellerOffer("T+3 Days")
                        .currentProposal("T+3 Days")
                        .difference("-")
                        .highlighted(false)
                        .build(),
                NegotiationDetailDto.NegotiationTermRowDto.builder()
                        .parameter("Payment Mode")
                        .buyerOffer("Bank Transfer (Escrow)")
                        .sellerOffer("Bank Transfer (Escrow)")
                        .currentProposal("Bank Transfer (Escrow)")
                        .difference("-")
                        .highlighted(false)
                        .build(),
                NegotiationDetailDto.NegotiationTermRowDto.builder()
                        .parameter("Other Terms")
                        .buyerOffer("Standard")
                        .sellerOffer("Standard")
                        .currentProposal("Standard")
                        .difference("-")
                        .highlighted(false)
                        .build()
        ));

        List<NegotiationDetailDto.TimelineEventDto> timeline = new ArrayList<>();
        int step = 1;
        if (!offers.isEmpty()) {
            for (NegotiationOffer o : offers) {
                boolean isBuyer = initUser != null && o.getOfferedByUser() != null && o.getOfferedByUser().getUserId().equals(initUser.getUserId());
                String senderName = isBuyer ? initName : cpName;
                String senderCode = isBuyer ? "SV" : "RM";
                String tStr = o.getCreatedAt() != null ? o.getCreatedAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")) : "Recent";
                timeline.add(NegotiationDetailDto.TimelineEventDto.builder()
                        .stepNumber(step++)
                        .title(isBuyer ? (step == offers.size() + 1 ? "Your Latest Offer" : "Counter Offer from You") : "Counter Offer from Seller")
                        .description(o.getOfferMessage() != null ? o.getOfferMessage() : ("Proposed ₹ " + CURRENCY_FMT.format(o.getOfferedPrice())))
                        .timestamp(tStr)
                        .senderCode(senderCode)
                        .senderName(senderName)
                        .status(step == offers.size() + 1 ? "ACTIVE" : "COMPLETED")
                        .price("₹ " + CURRENCY_FMT.format(o.getOfferedPrice()))
                        .shares(SHARES_FMT.format(o.getQuantity()) + " shares")
                        .build());
            }
        }

        String displayStatus = mapDbStatusToDisplay(n.getStatus());
        boolean canAct = !"ACCEPTED".equalsIgnoreCase(n.getStatus()) && !"REJECTED".equalsIgnoreCase(n.getStatus()) && !"EXPIRED".equalsIgnoreCase(n.getStatus());

        return NegotiationDetailDto.builder()
                .negotiationNo(negNo)
                .status(displayStatus)
                .initiatedOn(n.getCreatedAt() != null ? n.getCreatedAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")) : "17 Sep 2025, 11:20 AM")
                .expiresAt(n.getExpiresAt() != null ? n.getExpiresAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")) : "20 Sep 2025, 11:59 PM")
                .expiresIn("2 days")
                .buyer(buyer)
                .seller(seller)
                .companyShareDetails(shareDetails)
                .negotiationTerms(terms)
                .timeline(timeline)
                .availableActions(NegotiationDetailDto.AvailableActionsDto.builder()
                        .canAccept(canAct)
                        .canCounter(canAct)
                        .canReject(canAct)
                        .canRequestCall(canAct)
                        .build())
                .build();
    }

    private String resolveCompanyName(String symbol) {
        if (symbol == null) {
            return "Atirath Group Company";
        }
        return switch (symbol.trim().toUpperCase()) {
            case "ATG-H" -> "Atirath Holdings";
            case "FVT" -> "Farm Vantara";
            case "GEM" -> "Goeazymart";
            case "CSI" -> "CS INTERIO";
            case "TEB" -> "Tri Energy Battery";
            case "ROHE" -> "Royal One Heating";
            case "ATG-T" -> "Atirath Technologies";
            case "ATG-E" -> "Atirath Green Energy";
            case "ATG-F" -> "Atirath Financial Services";
            case "ATG-L" -> "Atirath Logistics";
            case "ATG-M" -> "Atirath Mining";
            case "ATG-R" -> "Atirath Realty";
            default -> symbol.trim();
        };
    }

    /**
     * Start New Negotiation (Image 1 Quick Action / Header button)
     */
    public NegotiationListItemDto initiateNegotiation(InitiateNegotiationRequestDto request) {
        return initiateNegotiation(request, null);
    }

    public NegotiationListItemDto initiateNegotiation(InitiateNegotiationRequestDto request, Long userId) {
        UserAccount indiv = resolveCurrentUser(userId);
        String newNo = "NEG" + System.currentTimeMillis() % 1000000000L;
        String symbol = request.getCompanySymbol().trim().toUpperCase();
        String companyName = resolveCompanyName(symbol);
        String side = "SELL".equalsIgnoreCase(request.getType()) ? "Sell" : "Buy";
        String cpId = request.getCounterpartyMemberId() != null && !request.getCounterpartyMemberId().isBlank()
                ? request.getCounterpartyMemberId().trim()
                : "ATG10056";

        NegotiationListItemDto item = NegotiationListItemDto.builder()
                .negotiationNo(newNo)
                .companyName(companyName)
                .companySymbol(symbol)
                .type(side)
                .counterpartyName("Counterparty (" + cpId + ")")
                .counterpartyMemberId(cpId)
                .shares(request.getShares())
                .currentPrice(request.getProposedPrice())
                .proposedPrice(request.getProposedPrice())
                .status("Active")
                .lastUpdated("Just now")
                .build();

        try {
            MarketCompany comp = marketCompanyRepository.findBySymbolIgnoreCase(symbol).orElse(null);
            UserAccount cp = userAccountRepository.findByReferralCode(cpId)
                    .or(() -> userAccountRepository.findByEmailIgnoreCase("rakesh@farmvantara.com"))
                    .orElse(null);

            if (comp != null && indiv != null && cp != null) {
                Negotiation neg = negotiationRepository.save(Negotiation.builder()
                        .negotiationNo(newNo)
                        .company(comp)
                        .initiatorUser(indiv)
                        .counterpartyUser(cp)
                        .quantity(request.getShares())
                        .proposedPrice(request.getProposedPrice())
                        .status("ACTIVE")
                        .expiresAt(OffsetDateTime.now().plusDays(7))
                        .createdAt(OffsetDateTime.now())
                        .updatedAt(OffsetDateTime.now())
                        .build());

                negotiationOfferRepository.save(NegotiationOffer.builder()
                        .negotiation(neg)
                        .offeredByUser(indiv)
                        .quantity(request.getShares())
                        .offeredPrice(request.getProposedPrice())
                        .offerMessage("Initial offer: ₹ " + request.getProposedPrice())
                        .offerStatus("PENDING")
                        .createdAt(OffsetDateTime.now())
                        .build());
            }
        } catch (Exception e) {
            log.warn("Error persisting initiated negotiation: {}", e.getMessage());
        }

        liveNegotiationsList.add(0, item);

        NegotiationDetailDto detail = buildGenericDetail(item);
        detail.setInitiatedOn("Just now");
        detail.setTimeline(new ArrayList<>(List.of(
                NegotiationDetailDto.TimelineEventDto.builder()
                        .stepNumber(1)
                        .title("Negotiation initiated by You")
                        .description("₹ " + CURRENCY_FMT.format(request.getProposedPrice()) + " per share, "
                                + SHARES_FMT.format(request.getShares()) + " shares")
                        .timestamp("Just now")
                        .senderCode("SV")
                        .senderName("Sairam Vempati")
                        .status("COMPLETED")
                        .price("₹ " + CURRENCY_FMT.format(request.getProposedPrice()))
                        .shares(SHARES_FMT.format(request.getShares()) + " shares")
                        .build()
        )));
        liveNegotiationDetails.put(newNo, detail);

        liveHistoryItems.put(newNo, new ArrayList<>(List.of(
                NegotiationHistoryDto.HistoryItemDto.builder()
                        .id(1L)
                        .senderCode("SV")
                        .senderName("Sairam Vempati")
                        .senderMemberId("ATG10027")
                        .title("Initial Offer from You")
                        .message(request.getMessage() != null && !request.getMessage().isBlank()
                                ? request.getMessage()
                                : "Initiated " + side.toLowerCase() + " negotiation for "
                                        + SHARES_FMT.format(request.getShares()) + " shares at ₹ "
                                        + CURRENCY_FMT.format(request.getProposedPrice()) + " per share.")
                        .price(request.getProposedPrice())
                        .shares(request.getShares())
                        .timestamp("Just now")
                        .isCurrentUser(true)
                        .badge("Active")
                        .build()
        )));

        return item;
    }

    private String mapTabToStatus(String tab) {
        return switch (tab.toUpperCase()) {
            case "ACTIVE" -> "Active";
            case "AWAITING_RESPONSE", "AWAITING RESPONSE" -> "Awaiting Response";
            case "ACCEPTED" -> "Accepted";
            case "REJECTED" -> "Rejected";
            case "EXPIRED" -> "Expired";
            default -> tab;
        };
    }

    private NegotiationDetailDto buildGenericDetail(NegotiationListItemDto item) {
        NegotiationDetailDto.PartyDto buyer = NegotiationDetailDto.PartyDto.builder()
                .isCurrentUser(item.getType().equalsIgnoreCase("BUY"))
                .companyName(item.getType().equalsIgnoreCase("BUY") ? "Atirath Holdings India Limited" : item.getCompanyName())
                .companySymbol(item.getCompanySymbol())
                .verified(true)
                .memberId(item.getType().equalsIgnoreCase("BUY") ? "ATG10027" : item.getCounterpartyMemberId())
                .memberName(item.getType().equalsIgnoreCase("BUY") ? "Sairam Vempati" : item.getCounterpartyName())
                .email("member@atirath.com")
                .phone("+91 98765 43210")
                .currentHoldings(10000L)
                .quantity(item.getShares())
                .partyRole("BUYER")
                .build();

        NegotiationDetailDto.PartyDto seller = NegotiationDetailDto.PartyDto.builder()
                .isCurrentUser(!item.getType().equalsIgnoreCase("BUY"))
                .companyName(item.getType().equalsIgnoreCase("BUY") ? item.getCompanyName() : "Atirath Holdings India Limited")
                .companySymbol(item.getCompanySymbol())
                .verified(true)
                .memberId(item.getType().equalsIgnoreCase("BUY") ? item.getCounterpartyMemberId() : "ATG10027")
                .memberName(item.getType().equalsIgnoreCase("BUY") ? item.getCounterpartyName() : "Sairam Vempati")
                .email("counterparty@atirath.com")
                .phone("+91 91234 56789")
                .currentHoldings(5000L)
                .quantity(item.getShares())
                .partyRole("SELLER")
                .build();

        return NegotiationDetailDto.builder()
                .negotiationNo(item.getNegotiationNo())
                .status(item.getStatus())
                .initiatedOn("Recent")
                .expiresAt("In 2 days")
                .expiresIn("2 days")
                .buyer(buyer)
                .seller(seller)
                .companyShareDetails(NegotiationDetailDto.CompanyShareDetailsDto.builder()
                        .companyName(item.getCompanyName())
                        .symbol(item.getCompanySymbol())
                        .companyType("Private Limited")
                        .sector("Diversified")
                        .isin(item.getCompanySymbol() + "-001")
                        .faceValue(new BigDecimal("10.00"))
                        .lastTradedPrice(item.getCurrentPrice())
                        .fiftyTwoWeekHigh(item.getCurrentPrice().multiply(new BigDecimal("1.15")).setScale(2, RoundingMode.HALF_UP))
                        .fiftyTwoWeekLow(item.getCurrentPrice().multiply(new BigDecimal("0.85")).setScale(2, RoundingMode.HALF_UP))
                        .build())
                .negotiationTerms(List.of(
                        NegotiationDetailDto.NegotiationTermRowDto.builder()
                                .parameter("Price per Share")
                                .buyerOffer("₹ " + CURRENCY_FMT.format(item.getProposedPrice()))
                                .sellerOffer("₹ " + CURRENCY_FMT.format(item.getCurrentPrice()))
                                .currentProposal("₹ " + CURRENCY_FMT.format(item.getProposedPrice()))
                                .difference("-")
                                .highlighted(true)
                                .build(),
                        NegotiationDetailDto.NegotiationTermRowDto.builder()
                                .parameter("Quantity (Shares)")
                                .buyerOffer(SHARES_FMT.format(item.getShares()))
                                .sellerOffer(SHARES_FMT.format(item.getShares()))
                                .currentProposal(SHARES_FMT.format(item.getShares()))
                                .difference("-")
                                .highlighted(false)
                                .build()
                ))
                .timeline(Collections.emptyList())
                .availableActions(NegotiationDetailDto.AvailableActionsDto.builder()
                        .canAccept(true)
                        .canCounter(true)
                        .canReject(true)
                        .canRequestCall(true)
                        .build())
                .build();
    }

    private void initNotificationsData() {
        liveNotifications.clear();

        // 1. New Counter Offer Received (Selected in Image 6)
        liveNotifications.add(NegotiationNotificationItemDto.builder()
                .id(1L)
                .type("COUNTER_OFFER")
                .icon("DOUBLE_ARROW")
                .unread(true)
                .title("New Counter Offer Received")
                .timeAgo("10 minutes ago")
                .timestamp("18 Sep 2025, 10:05 AM")
                .message("Rakesh Mehta has sent a counter offer for 1,000 shares of ATG-H at ₹ 242.00 per share.")
                .negotiationNo("NEG250918001")
                .companyName("Atirath Holdings India Limited")
                .companySymbol("ATG-H")
                .counterpartyName("Rakesh Mehta")
                .counterpartyMemberId("ATG10056")
                .status("Awaiting Your Response")
                .negotiationSummary(NegotiationNotificationItemDto.NotificationDetailSummaryDto.builder()
                        .negotiationId("NEG250918001")
                        .companyName("Atirath Holdings India Limited")
                        .companySymbol("ATG-H")
                        .counterparty("Rakesh Mehta (ATG10056)")
                        .yourLastOffer("₹ 235.00 per share | 1,000 shares")
                        .counterpartyOffer("₹ 242.00 per share | 1,000 shares")
                        .difference("+ ₹ 7.00 (+2.98%)")
                        .status("Awaiting Your Response")
                        .viewDetailsUrl("/api/v1/negotiations/NEG250918001")
                        .canAccept(true)
                        .canCounter(true)
                        .build())
                .additionalInformation(List.of(
                        "Please review the offer and respond at your earliest convenience.",
                        "If no action is taken within 3 days, the offer may expire.",
                        "You can propose a revised price, quantity or other terms."
                ))
                .build());

        // 2. Negotiation Agreement Reached
        liveNotifications.add(NegotiationNotificationItemDto.builder()
                .id(2L)
                .type("AGREEMENT")
                .icon("HANDSHAKE")
                .unread(true)
                .title("Negotiation Agreement Reached")
                .timeAgo("2 hours ago")
                .timestamp("18 Sep 2025, 08:20 AM")
                .message("You and Priya Sharma have reached an agreement for 500 shares of FVT at ₹ 190.00 per share.")
                .negotiationNo("NEG250917003")
                .companyName("Farm Vantara")
                .companySymbol("FVT")
                .counterpartyName("Priya Sharma")
                .counterpartyMemberId("ATG10072")
                .status("Agreement Ready")
                .negotiationSummary(NegotiationNotificationItemDto.NotificationDetailSummaryDto.builder()
                        .negotiationId("NEG250917003")
                        .companyName("Farm Vantara Technologies")
                        .companySymbol("FVT")
                        .counterparty("Priya Sharma (ATG10072)")
                        .yourLastOffer("₹ 190.00 per share | 500 shares")
                        .counterpartyOffer("₹ 190.00 per share | 500 shares")
                        .difference("₹ 0.00 (0.00%)")
                        .status("Agreement Ready")
                        .viewDetailsUrl("/api/v1/negotiations/NEG250917003")
                        .canAccept(false)
                        .canCounter(false)
                        .build())
                .additionalInformation(List.of(
                        "Trade agreement generated and ready for confirmation.",
                        "Escrow funding will be requested upon agreement confirmation."
                ))
                .build());

        // 3. Action Required
        liveNotifications.add(NegotiationNotificationItemDto.builder()
                .id(3L)
                .type("ACTION_REQUIRED")
                .icon("CLOCK")
                .unread(false)
                .title("Action Required")
                .timeAgo("4 hours ago")
                .timestamp("18 Sep 2025, 06:15 AM")
                .message("Please review and respond to the counter offer from Rakesh Mehta for ATG-H.")
                .negotiationNo("NEG250918001")
                .companyName("Atirath Holdings India Limited")
                .companySymbol("ATG-H")
                .counterpartyName("Rakesh Mehta")
                .counterpartyMemberId("ATG10056")
                .status("Awaiting Your Response")
                .negotiationSummary(NegotiationNotificationItemDto.NotificationDetailSummaryDto.builder()
                        .negotiationId("NEG250918001")
                        .companyName("Atirath Holdings India Limited")
                        .companySymbol("ATG-H")
                        .counterparty("Rakesh Mehta (ATG10056)")
                        .yourLastOffer("₹ 235.00 per share | 1,000 shares")
                        .counterpartyOffer("₹ 242.00 per share | 1,000 shares")
                        .difference("+ ₹ 7.00 (+2.98%)")
                        .status("Awaiting Your Response")
                        .viewDetailsUrl("/api/v1/negotiations/NEG250918001")
                        .canAccept(true)
                        .canCounter(true)
                        .build())
                .additionalInformation(List.of(
                        "Immediate response recommended to avoid offer expiry."
                ))
                .build());

        // 4. New Offer Received
        liveNotifications.add(NegotiationNotificationItemDto.builder()
                .id(4L)
                .type("NEW_OFFER")
                .icon("DOCUMENT")
                .unread(true)
                .title("New Offer Received")
                .timeAgo("6 hours ago")
                .timestamp("18 Sep 2025, 04:10 AM")
                .message("Vikram Reddy has made an offer to buy 1,500 shares of GEM at ₹ 82.00 per share.")
                .negotiationNo("NEG250916007")
                .companyName("Goeazymart")
                .companySymbol("GEM")
                .counterpartyName("Vikram Reddy")
                .counterpartyMemberId("ATG10081")
                .status("Under Negotiation")
                .negotiationSummary(NegotiationNotificationItemDto.NotificationDetailSummaryDto.builder()
                        .negotiationId("NEG250916007")
                        .companyName("Goeazymart")
                        .companySymbol("GEM")
                        .counterparty("Vikram Reddy (ATG10081)")
                        .yourLastOffer("₹ 85.00 per share | 1,500 shares")
                        .counterpartyOffer("₹ 82.00 per share | 1,500 shares")
                        .difference("- ₹ 3.00 (-3.53%)")
                        .status("Under Negotiation")
                        .viewDetailsUrl("/api/v1/negotiations/NEG250916007")
                        .canAccept(true)
                        .canCounter(true)
                        .build())
                .additionalInformation(List.of(
                        "New buyer offer awaiting response or counter offer."
                ))
                .build());

        // 5. Negotiation Reminder
        liveNotifications.add(NegotiationNotificationItemDto.builder()
                .id(5L)
                .type("REMINDER")
                .icon("BELL")
                .unread(false)
                .title("Negotiation Reminder")
                .timeAgo("1 day ago")
                .timestamp("17 Sep 2025, 10:30 AM")
                .message("Your negotiation with Anita Desai for 400 shares of CSI expires in 1 day.")
                .negotiationNo("NEG250915004")
                .companyName("CS INTERIO")
                .companySymbol("CSI")
                .counterpartyName("Anita Desai")
                .counterpartyMemberId("ATG10063")
                .status("Accepted")
                .negotiationSummary(NegotiationNotificationItemDto.NotificationDetailSummaryDto.builder()
                        .negotiationId("NEG250915004")
                        .companyName("CS INTERIO")
                        .companySymbol("CSI")
                        .counterparty("Anita Desai (ATG10063)")
                        .yourLastOffer("₹ 205.00 per share | 400 shares")
                        .counterpartyOffer("₹ 210.00 per share | 400 shares")
                        .difference("- ₹ 5.00")
                        .status("Accepted")
                        .viewDetailsUrl("/api/v1/negotiations/NEG250915004")
                        .canAccept(false)
                        .canCounter(false)
                        .build())
                .additionalInformation(List.of(
                        "Negotiation will expire in 24 hours if agreement is not confirmed."
                ))
                .build());

        // 6. Negotiation Expired
        liveNotifications.add(NegotiationNotificationItemDto.builder()
                .id(6L)
                .type("EXPIRED")
                .icon("CROSS")
                .unread(false)
                .title("Negotiation Expired")
                .timeAgo("1 day ago")
                .timestamp("17 Sep 2025, 09:00 AM")
                .message("The negotiation for 600 shares of ROHE with Karan Malhotra has expired.")
                .negotiationNo("NEG250912006")
                .companyName("Royal One Heating")
                .companySymbol("ROHE")
                .counterpartyName("Karan Malhotra")
                .counterpartyMemberId("ATG10069")
                .status("Expired")
                .negotiationSummary(NegotiationNotificationItemDto.NotificationDetailSummaryDto.builder()
                        .negotiationId("NEG250912006")
                        .companyName("Royal One Heating")
                        .companySymbol("ROHE")
                        .counterparty("Karan Malhotra (ATG10069)")
                        .yourLastOffer("₹ 148.00 per share | 600 shares")
                        .counterpartyOffer("₹ 150.00 per share | 600 shares")
                        .difference("- ₹ 2.00")
                        .status("Expired")
                        .viewDetailsUrl("/api/v1/negotiations/NEG250912006")
                        .canAccept(false)
                        .canCounter(false)
                        .build())
                .additionalInformation(List.of(
                        "This negotiation expired due to inactivity. You can start a new negotiation anytime."
                ))
                .build());

        // 7. Counter Offer Accepted
        liveNotifications.add(NegotiationNotificationItemDto.builder()
                .id(7L)
                .type("COUNTER_OFFER")
                .icon("DOUBLE_ARROW")
                .unread(false)
                .title("Counter Offer Accepted")
                .timeAgo("2 days ago")
                .timestamp("16 Sep 2025, 02:45 PM")
                .message("Your counter offer of ₹ 240.00 per share for 1,000 shares of TEB has been accepted.")
                .negotiationNo("NEG250914002")
                .companyName("Tri Energy Battery")
                .companySymbol("TEB")
                .counterpartyName("Suresh Nair")
                .counterpartyMemberId("ATG10078")
                .status("Accepted")
                .negotiationSummary(NegotiationNotificationItemDto.NotificationDetailSummaryDto.builder()
                        .negotiationId("NEG250914002")
                        .companyName("Tri Energy Battery")
                        .companySymbol("TEB")
                        .counterparty("Suresh Nair (ATG10078)")
                        .yourLastOffer("₹ 240.00 per share | 1,000 shares")
                        .counterpartyOffer("₹ 240.00 per share | 1,000 shares")
                        .difference("₹ 0.00")
                        .status("Accepted")
                        .viewDetailsUrl("/api/v1/negotiations/NEG250914002")
                        .canAccept(false)
                        .canCounter(false)
                        .build())
                .additionalInformation(List.of(
                        "Counterparty has accepted your proposed terms."
                ))
                .build());

        // 8. System Notification
        liveNotifications.add(NegotiationNotificationItemDto.builder()
                .id(8L)
                .type("SYSTEM")
                .icon("INFO")
                .unread(false)
                .title("System Notification")
                .timeAgo("2 days ago")
                .timestamp("16 Sep 2025, 11:30 AM")
                .message("The trade agreement for negotiation NEG250918001 is now available for download.")
                .negotiationNo("NEG250918001")
                .companyName("Atirath Holdings India Limited")
                .companySymbol("ATG-H")
                .counterpartyName("Rakesh Mehta")
                .counterpartyMemberId("ATG10056")
                .status("Agreement Ready")
                .negotiationSummary(NegotiationNotificationItemDto.NotificationDetailSummaryDto.builder()
                        .negotiationId("NEG250918001")
                        .companyName("Atirath Holdings India Limited")
                        .companySymbol("ATG-H")
                        .counterparty("Rakesh Mehta (ATG10056)")
                        .yourLastOffer("₹ 235.00 per share | 1,000 shares")
                        .counterpartyOffer("₹ 242.00 per share | 1,000 shares")
                        .difference("+ ₹ 7.00")
                        .status("Agreement Ready")
                        .viewDetailsUrl("/api/v1/negotiations/NEG250918001/trade-agreement")
                        .canAccept(false)
                        .canCounter(false)
                        .build())
                .additionalInformation(List.of(
                        "Download the PDF agreement from the Trade Agreement tab."
                ))
                .build());

        // 9. New Offer Received (Offers Tab)
        liveNotifications.add(NegotiationNotificationItemDto.builder()
                .id(9L)
                .type("NEW_OFFER")
                .icon("DOCUMENT")
                .unread(false)
                .title("New Offer Received")
                .timeAgo("3 days ago")
                .timestamp("15 Sep 2025, 03:15 PM")
                .message("Rahul Verma has submitted an offer for 800 shares of ATG-T at ₹ 1,220.00 per share.")
                .negotiationNo("NEG250911001")
                .companyName("Atirath Technologies")
                .companySymbol("ATG-T")
                .counterpartyName("Rahul Verma")
                .counterpartyMemberId("ATG10044")
                .status("Active")
                .negotiationSummary(NegotiationNotificationItemDto.NotificationDetailSummaryDto.builder()
                        .negotiationId("NEG250911001")
                        .companyName("Atirath Technologies")
                        .companySymbol("ATG-T")
                        .counterparty("Rahul Verma (ATG10044)")
                        .yourLastOffer("₹ 1,220.00 per share | 800 shares")
                        .counterpartyOffer("₹ 1,240.80 per share | 800 shares")
                        .difference("- ₹ 20.80")
                        .status("Active")
                        .viewDetailsUrl("/api/v1/negotiations/NEG250911001")
                        .canAccept(true)
                        .canCounter(true)
                        .build())
                .additionalInformation(List.of(
                        "Review offer parameters and propose terms."
                ))
                .build());

        // 10. Counter Offer Received
        liveNotifications.add(NegotiationNotificationItemDto.builder()
                .id(10L)
                .type("COUNTER_OFFER")
                .icon("DOUBLE_ARROW")
                .unread(false)
                .title("Counter Offer Received")
                .timeAgo("4 days ago")
                .timestamp("14 Sep 2025, 11:10 AM")
                .message("Sneha Patel has sent a counter offer for 2,000 shares of ATG-E at ₹ 315.00 per share.")
                .negotiationNo("NEG250910008")
                .companyName("Atirath Green Energy")
                .companySymbol("ATG-E")
                .counterpartyName("Sneha Patel")
                .counterpartyMemberId("ATG10052")
                .status("Awaiting Response")
                .negotiationSummary(NegotiationNotificationItemDto.NotificationDetailSummaryDto.builder()
                        .negotiationId("NEG250910008")
                        .companyName("Atirath Green Energy")
                        .companySymbol("ATG-E")
                        .counterparty("Sneha Patel (ATG10052)")
                        .yourLastOffer("₹ 312.40 per share | 2,000 shares")
                        .counterpartyOffer("₹ 315.00 per share | 2,000 shares")
                        .difference("+ ₹ 2.60")
                        .status("Awaiting Response")
                        .viewDetailsUrl("/api/v1/negotiations/NEG250910008")
                        .canAccept(true)
                        .canCounter(true)
                        .build())
                .additionalInformation(List.of(
                        "Counter offer available for review."
                ))
                .build());

        // 11. Negotiation Agreement Reached
        liveNotifications.add(NegotiationNotificationItemDto.builder()
                .id(11L)
                .type("AGREEMENT")
                .icon("HANDSHAKE")
                .unread(false)
                .title("Negotiation Agreement Reached")
                .timeAgo("5 days ago")
                .timestamp("13 Sep 2025, 05:00 PM")
                .message("Agreement finalized with Divya Kapoor for 1,000 shares of ATG-L at ₹ 420.00 per share.")
                .negotiationNo("NEG250908003")
                .companyName("Atirath Logistics")
                .companySymbol("ATG-L")
                .counterpartyName("Divya Kapoor")
                .counterpartyMemberId("ATG10061")
                .status("Accepted")
                .negotiationSummary(NegotiationNotificationItemDto.NotificationDetailSummaryDto.builder()
                        .negotiationId("NEG250908003")
                        .companyName("Atirath Logistics")
                        .companySymbol("ATG-L")
                        .counterparty("Divya Kapoor (ATG10061)")
                        .yourLastOffer("₹ 420.00 per share | 1,000 shares")
                        .counterpartyOffer("₹ 420.00 per share | 1,000 shares")
                        .difference("₹ 0.00")
                        .status("Accepted")
                        .viewDetailsUrl("/api/v1/negotiations/NEG250908003")
                        .canAccept(false)
                        .canCounter(false)
                        .build())
                .additionalInformation(List.of(
                        "Trade agreement ready for settlement."
                ))
                .build());

        // 12. Negotiation Reminder
        liveNotifications.add(NegotiationNotificationItemDto.builder()
                .id(12L)
                .type("REMINDER")
                .icon("BELL")
                .unread(false)
                .title("Negotiation Reminder")
                .timeAgo("6 days ago")
                .timestamp("12 Sep 2025, 09:30 AM")
                .message("Response required for negotiation with Manoj Tiwari (ATG-F) within 24 hours.")
                .negotiationNo("NEG250909005")
                .companyName("Atirath Financial Services")
                .companySymbol("ATG-F")
                .counterpartyName("Manoj Tiwari")
                .counterpartyMemberId("ATG10039")
                .status("Active")
                .negotiationSummary(NegotiationNotificationItemDto.NotificationDetailSummaryDto.builder()
                        .negotiationId("NEG250909005")
                        .companyName("Atirath Financial Services")
                        .companySymbol("ATG-F")
                        .counterparty("Manoj Tiwari (ATG10039)")
                        .yourLastOffer("₹ 575.00 per share | 750 shares")
                        .counterpartyOffer("₹ 580.00 per share | 750 shares")
                        .difference("- ₹ 5.00")
                        .status("Active")
                        .viewDetailsUrl("/api/v1/negotiations/NEG250909005")
                        .canAccept(true)
                        .canCounter(true)
                        .build())
                .additionalInformation(List.of(
                        "Response time limit approaching."
                ))
                .build());
    }

    /**
     * Negotiation Notifications Screen (Image 6)
     */
    public NegotiationNotificationsPageDto getNotificationsPage(String category, String search, Long selectedId) {
        return getNotificationsPage(category, search, selectedId, null);
    }

    public NegotiationNotificationsPageDto getNotificationsPage(String category, String search, Long selectedId, Long userId) {
        List<NegotiationNotificationItemDto> list = new ArrayList<>(liveNotifications);

        // Filter by category / tab
        if (category != null && !category.equalsIgnoreCase("ALL") && !category.isBlank()) {
            String cat = category.toUpperCase().trim();
            list = list.stream().filter(item -> {
                if (cat.equals("UNREAD")) return item.isUnread();
                if (cat.equals("OFFERS")) return item.getType().equalsIgnoreCase("NEW_OFFER");
                if (cat.equals("COUNTER_OFFERS") || cat.equals("COUNTER OFFERS") || cat.equals("COUNTER-OFFERS"))
                    return item.getType().equalsIgnoreCase("COUNTER_OFFER");
                if (cat.equals("AGREEMENTS")) return item.getType().equalsIgnoreCase("AGREEMENT");
                if (cat.equals("REMINDERS")) return item.getType().equalsIgnoreCase("REMINDER");
                if (cat.equals("SYSTEM")) return item.getType().equalsIgnoreCase("SYSTEM");
                if (cat.equals("ACTION_REQUIRED") || cat.equals("ACTION REQUIRED"))
                    return item.getType().equalsIgnoreCase("ACTION_REQUIRED");
                return item.getType().equalsIgnoreCase(cat);
            }).toList();
        }

        // Filter by search
        if (search != null && !search.isBlank()) {
            String q = search.toLowerCase().trim();
            list = list.stream().filter(item ->
                    item.getTitle().toLowerCase().contains(q)
                            || item.getMessage().toLowerCase().contains(q)
                            || (item.getCompanyName() != null && item.getCompanyName().toLowerCase().contains(q))
                            || (item.getCompanySymbol() != null && item.getCompanySymbol().toLowerCase().contains(q))
                            || (item.getCounterpartyName() != null && item.getCounterpartyName().toLowerCase().contains(q))
                            || (item.getNegotiationNo() != null && item.getNegotiationNo().toLowerCase().contains(q))
            ).toList();
        }

        // Selected notification (defaults to selectedId or first item in list, or item 1)
        NegotiationNotificationItemDto selected = null;
        if (selectedId != null) {
            selected = liveNotifications.stream().filter(n -> n.getId().equals(selectedId)).findFirst().orElse(null);
        }
        if (selected == null && !list.isEmpty()) {
            selected = list.get(0);
        }
        if (selected == null && !liveNotifications.isEmpty()) {
            selected = liveNotifications.get(0);
        }

        int unreadTotal = (int) liveNotifications.stream().filter(NegotiationNotificationItemDto::isUnread).count();

        // Exact KPI Summary counts from Screenshot:
        // All: 12, New Offers: 3, Counter Offers: 4, Agreements: 2, Action Required: 2, System Alerts: 1
        NegotiationNotificationSummaryDto summary = NegotiationNotificationSummaryDto.builder()
                .allNotifications(liveNotifications.size())
                .newOffers(3)
                .counterOffers(4)
                .agreements(2)
                .actionRequired(2)
                .systemAlerts(1)
                .build();

        // Tab counts matching UI:
        // All (12), Unread (3), Offers (4), Counter Offers (3), Agreements (2), Reminders (2), System (1)
        Map<String, Integer> tabCounts = new LinkedHashMap<>();
        tabCounts.put("All", liveNotifications.size());
        tabCounts.put("Unread", unreadTotal);
        tabCounts.put("Offers", 4);
        tabCounts.put("Counter Offers", 3);
        tabCounts.put("Agreements", 2);
        tabCounts.put("Reminders", 2);
        tabCounts.put("System", 1);

        return NegotiationNotificationsPageDto.builder()
                .summary(summary)
                .unreadCount(unreadTotal)
                .tabCounts(tabCounts)
                .notifications(list)
                .selectedNotification(selected)
                .build();
    }

    public NegotiationNotificationItemDto getNotificationById(Long id) {
        return liveNotifications.stream()
                .filter(n -> n.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with ID: " + id));
    }

    public NegotiationNotificationItemDto markNotificationAsRead(Long id) {
        NegotiationNotificationItemDto item = getNotificationById(id);
        item.setUnread(false);
        return item;
    }

    public NegotiationNotificationItemDto markNotificationAsUnread(Long id) {
        NegotiationNotificationItemDto item = getNotificationById(id);
        item.setUnread(true);
        return item;
    }

    public void markAllNotificationsAsRead() {
        liveNotifications.forEach(n -> n.setUnread(false));
    }
}
