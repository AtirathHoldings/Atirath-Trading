package com.atirath.trading.modules.negotiation.service;

import com.atirath.trading.common.ResourceNotFoundException;
import com.atirath.trading.modules.negotiation.dto.*;
import com.atirath.trading.modules.negotiation.model.*;
import com.atirath.trading.modules.negotiation.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
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
        List<NegotiationListItemDto> filtered = new ArrayList<>(liveNegotiationsList);

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

        // KPI Counts
        NegotiationSummaryDto summary = NegotiationSummaryDto.builder()
                .allNegotiations(12)
                .active(4)
                .awaitingResponse(3)
                .accepted(2)
                .rejected(1)
                .expired(2)
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
        NegotiationStatusBreakdownDto statusBreakdown = NegotiationStatusBreakdownDto.builder()
                .total(12)
                .items(List.of(
                        NegotiationStatusBreakdownDto.StatusItemDto.builder()
                                .status("Active")
                                .count(4)
                                .percentage(33.0)
                                .color("#10B981")
                                .build(),
                        NegotiationStatusBreakdownDto.StatusItemDto.builder()
                                .status("Awaiting Response")
                                .count(3)
                                .percentage(25.0)
                                .color("#F59E0B")
                                .build(),
                        NegotiationStatusBreakdownDto.StatusItemDto.builder()
                                .status("Accepted")
                                .count(2)
                                .percentage(17.0)
                                .color("#34D399")
                                .build(),
                        NegotiationStatusBreakdownDto.StatusItemDto.builder()
                                .status("Rejected")
                                .count(1)
                                .percentage(8.0)
                                .color("#EF4444")
                                .build(),
                        NegotiationStatusBreakdownDto.StatusItemDto.builder()
                                .status("Expired")
                                .count(2)
                                .percentage(17.0)
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
        NegotiationDetailDto detail = getNegotiationDetail(negotiationNo);

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
        updatedTerms.add(NegotiationDetailDto.NegotiationTermRowDto.builder()
                .parameter("Order Value")
                .buyerOffer("₹ " + CURRENCY_FMT.format(orderValue))
                .sellerOffer("₹ " + CURRENCY_FMT.format(oldPrice.multiply(BigDecimal.valueOf(request.getQuantity()))))
                .currentProposal("₹ " + CURRENCY_FMT.format(orderValue))
                .difference(diffStr + ",000.00")
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

        // Add to history
        List<NegotiationHistoryDto.HistoryItemDto> history = liveHistoryItems.computeIfAbsent(negotiationNo, k -> new ArrayList<>());
        long newId = history.size() + 1L;
        history.add(NegotiationHistoryDto.HistoryItemDto.builder()
                .id(newId)
                .senderCode("SV")
                .senderName(detail.getBuyer().getMemberName())
                .senderMemberId(detail.getBuyer().getMemberId())
                .title("Your Counter Offer")
                .message("Counter offer sent: ₹ " + CURRENCY_FMT.format(request.getPricePerShare()) + " for " + SHARES_FMT.format(request.getQuantity()) + " shares. " + (request.getOtherTerms() != null ? request.getOtherTerms() : ""))
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
                    item.setProposedPrice(request.getPricePerShare());
                    item.setLastUpdated("Just now");
                });

        return detail;
    }

    /**
     * Accept Offer (transitions to Trade Agreement)
     */
    public NegotiationDetailDto acceptOffer(String negotiationNo) {
        NegotiationDetailDto detail = getNegotiationDetail(negotiationNo);
        detail.setStatus("Accepted");

        liveNegotiationsList.stream()
                .filter(n -> n.getNegotiationNo().equalsIgnoreCase(negotiationNo))
                .findFirst()
                .ifPresent(item -> {
                    item.setStatus("Accepted");
                    item.setLastUpdated("Just now");
                });

        // Initialize agreement ready
        TradeAgreementDto agreement = liveTradeAgreements.get(negotiationNo);
        if (agreement == null) {
            agreement = TradeAgreementDto.builder()
                    .agreementNo(negotiationNo)
                    .negotiationNo(negotiationNo)
                    .status("Agreement Ready")
                    .initiatedOn(detail.getInitiatedOn())
                    .finalAgreedTerms(TradeAgreementDto.FinalAgreedTermsDto.builder()
                            .sharePrice(new BigDecimal("242.00"))
                            .quantity(1000L)
                            .totalTradeValue(new BigDecimal("242000.00"))
                            .paymentMode("Bank Transfer (Escrow)")
                            .settlementTimeline("T+3 Days (from escrow confirmation)")
                            .orderType("Buy (Limit)")
                            .otherTerms("Standard (As per platform rules)")
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
        NegotiationDetailDto detail = getNegotiationDetail(negotiationNo);
        detail.setStatus("Rejected");

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
     * Negotiation History Screen (Image 4)
     */
    public NegotiationHistoryDto getHistory(String negotiationNo) {
        NegotiationDetailDto detail = getNegotiationDetail(negotiationNo);
        List<NegotiationHistoryDto.HistoryItemDto> timeline = liveHistoryItems.getOrDefault(negotiationNo, Collections.emptyList());

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

    /**
     * Post a chat message in negotiation
     */
    public NegotiationHistoryDto postMessage(String negotiationNo, NegotiationMessageRequestDto request) {
        NegotiationDetailDto detail = getNegotiationDetail(negotiationNo);
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

        return getHistory(negotiationNo);
    }

    /**
     * Trade Agreement Screen (Image 5)
     */
    public TradeAgreementDto getTradeAgreement(String negotiationNo) {
        TradeAgreementDto agreement = liveTradeAgreements.get(negotiationNo);
        if (agreement == null) {
            NegotiationDetailDto detail = getNegotiationDetail(negotiationNo);
            acceptOffer(negotiationNo);
            agreement = liveTradeAgreements.get(negotiationNo);
        }
        return agreement;
    }

    /**
     * Confirm Agreement & Proceed to Escrow (Image 5 Bottom Action)
     */
    public TradeAgreementDto confirmAgreement(String negotiationNo, TradeAgreementConfirmRequestDto request) {
        TradeAgreementDto agreement = getTradeAgreement(negotiationNo);
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
        StringBuilder sb = new StringBuilder();
        sb.append("=================================================================\n");
        sb.append("                     ATIRATH GROUP                               \n");
        sb.append("         OFFICIAL TRADE & SHARE NEGOTIATION AGREEMENT            \n");
        sb.append("=================================================================\n\n");
        sb.append("Agreement Reference: ").append(agreement.getAgreementNo()).append("\n");
        sb.append("Negotiation Reference: ").append(agreement.getNegotiationNo()).append("\n");
        sb.append("Status: ").append(agreement.getStatus()).append("\n");
        sb.append("Date: ").append(agreement.getFinalAgreedTerms().getAgreementDate()).append("\n\n");

        sb.append("PARTIES TO THE AGREEMENT:\n");
        sb.append("-----------------------------------------------------------------\n");
        sb.append("BUYER:  ").append(agreement.getParties().getBuyer().getCompanyName())
                .append(" (").append(agreement.getParties().getBuyer().getMemberName())
                .append(" - ").append(agreement.getParties().getBuyer().getMemberId()).append(")\n");
        sb.append("SELLER: ").append(agreement.getParties().getSeller().getCompanyName())
                .append(" (").append(agreement.getParties().getSeller().getMemberName())
                .append(" - ").append(agreement.getParties().getSeller().getMemberId()).append(")\n\n");

        sb.append("FINAL AGREED TERMS:\n");
        sb.append("-----------------------------------------------------------------\n");
        sb.append("Share Price:          ₹ ").append(CURRENCY_FMT.format(agreement.getFinalAgreedTerms().getSharePrice())).append("\n");
        sb.append("Quantity:             ").append(SHARES_FMT.format(agreement.getFinalAgreedTerms().getQuantity())).append(" shares\n");
        sb.append("Total Value:          ₹ ").append(CURRENCY_FMT.format(agreement.getFinalAgreedTerms().getTotalTradeValue())).append("\n");
        sb.append("Settlement Timeline:  ").append(agreement.getFinalAgreedTerms().getSettlementTimeline()).append("\n");
        sb.append("Payment Mode:         ").append(agreement.getFinalAgreedTerms().getPaymentMode()).append("\n");
        sb.append("Order Type:           ").append(agreement.getFinalAgreedTerms().getOrderType()).append("\n");
        sb.append("Valid Till:           ").append(agreement.getFinalAgreedTerms().getValidTill()).append("\n\n");

        sb.append("TERMS & CONDITIONS:\n");
        sb.append("1. All offers and agreements confirmed on the Atirath Group Internal Market\n");
        sb.append("   are legally binding.\n");
        sb.append("2. Funds must be deposited into the verified Escrow Account within T+3 Days.\n");
        sb.append("3. Share lock and ledger transfer will execute automatically upon escrow clearance.\n\n");
        sb.append("Digital Verification Signature: ATG-SEC-SIGN-").append(UUID.randomUUID().toString().toUpperCase()).append("\n");
        sb.append("=================================================================\n");

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Start New Negotiation (Image 1 Quick Action / Header button)
     */
    public NegotiationListItemDto initiateNegotiation(InitiateNegotiationRequestDto request) {
        String newNo = "NEG" + System.currentTimeMillis() % 1000000000L;
        NegotiationListItemDto item = NegotiationListItemDto.builder()
                .negotiationNo(newNo)
                .companyName(request.getCompanySymbol())
                .companySymbol(request.getCompanySymbol())
                .type(request.getType().toUpperCase())
                .counterpartyName(request.getCounterpartyMemberId() != null ? request.getCounterpartyMemberId() : "Verified Counterparty")
                .counterpartyMemberId(request.getCounterpartyMemberId() != null ? request.getCounterpartyMemberId() : "ATG10099")
                .shares(request.getShares())
                .currentPrice(request.getProposedPrice())
                .proposedPrice(request.getProposedPrice())
                .status("Active")
                .lastUpdated("Just now")
                .build();

        liveNegotiationsList.add(0, item);
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
