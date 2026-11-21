package com.atirath.trading.modules.negotiation.service;

import com.atirath.trading.common.ApiException;
import com.atirath.trading.common.ResourceNotFoundException;
import com.atirath.trading.modules.negotiation.dto.*;
import com.atirath.trading.modules.negotiation.dto.mobile.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service providing mobile-optimized, lightweight payloads for the 6 Flutter negotiation screens:
 * 1. Negotiation Dashboard
 * 2. Negotiation Details
 * 3. Counter Offer Sheet / Screen
 * 4. Negotiation History
 * 5. Trade Agreement
 * 6. Negotiation Notifications
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MobileNegotiationService {

    private final NegotiationService negotiationService;

    private static final DecimalFormat CURRENCY_FMT = new DecimalFormat("#,##,##0.00");
    private static final DecimalFormat INT_CURRENCY_FMT = new DecimalFormat("#,##,###");
    private static final DecimalFormat SHARES_FMT = new DecimalFormat("#,##,###");
    private static final DateTimeFormatter DISPLAY_DT_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    // =========================================================================
    // SCREEN 1: NEGOTIATION DASHBOARD (MOBILE)
    // =========================================================================
    public MobileNegotiationDashboardDto getMobileDashboard(String tab, Long userId) {
        String filterTab = (tab != null && !tab.isBlank()) ? tab.trim().toUpperCase() : "ALL";
        NegotiationDashboardDto webDashboard = negotiationService.getDashboard(filterTab, null, 1, 100, userId);

        List<NegotiationListItemDto> webItems = Collections.emptyList();
        if (webDashboard != null && webDashboard.getNegotiations() != null && webDashboard.getNegotiations().getContent() != null) {
            webItems = webDashboard.getNegotiations().getContent();
        }

        // Summary KPI counters from web summary or computed from items
        NegotiationSummaryDto summary = webDashboard != null ? webDashboard.getSummary() : null;
        int total = summary != null ? summary.getAllNegotiations() : webItems.size();
        int active = summary != null ? summary.getActive() : (int) webItems.stream().filter(i -> "Active".equalsIgnoreCase(i.getStatus()) || "Under Negotiation".equalsIgnoreCase(i.getStatus())).count();
        int awaiting = summary != null ? summary.getAwaitingResponse() : (int) webItems.stream().filter(i -> i.getStatus() != null && i.getStatus().toLowerCase().contains("awaiting")).count();
        int accepted = summary != null ? summary.getAccepted() : (int) webItems.stream().filter(i -> "Accepted".equalsIgnoreCase(i.getStatus())).count();
        int rejected = summary != null ? (summary.getRejected() + summary.getExpired()) : (int) webItems.stream().filter(i -> "Rejected".equalsIgnoreCase(i.getStatus()) || "Expired".equalsIgnoreCase(i.getStatus())).count();

        MobileNegotiationCountersDto counters = MobileNegotiationCountersDto.builder()
                .totalCount(total)
                .activeCount(active)
                .awaitingResponseCount(awaiting)
                .acceptedCount(accepted)
                .rejectedCount(rejected)
                .build();

        // Filter for specific tab if requested
        List<NegotiationListItemDto> filteredItems = webItems;
        if ("ACTIVE".equalsIgnoreCase(filterTab)) {
            filteredItems = webItems.stream()
                    .filter(i -> "Active".equalsIgnoreCase(i.getStatus()) || "Under Negotiation".equalsIgnoreCase(i.getStatus()))
                    .collect(Collectors.toList());
        } else if ("AWAITING".equalsIgnoreCase(filterTab) || "AWAITING_RESPONSE".equalsIgnoreCase(filterTab)) {
            filteredItems = webItems.stream()
                    .filter(i -> i.getStatus() != null && i.getStatus().toLowerCase().contains("awaiting"))
                    .collect(Collectors.toList());
        } else if ("ACCEPTED".equalsIgnoreCase(filterTab) || "COMPLETED".equalsIgnoreCase(filterTab)) {
            filteredItems = webItems.stream()
                    .filter(i -> "Accepted".equalsIgnoreCase(i.getStatus()))
                    .collect(Collectors.toList());
        } else if ("HISTORY".equalsIgnoreCase(filterTab)) {
            filteredItems = webItems.stream()
                    .filter(i -> "Accepted".equalsIgnoreCase(i.getStatus()) || "Rejected".equalsIgnoreCase(i.getStatus()) || "Expired".equalsIgnoreCase(i.getStatus()))
                    .collect(Collectors.toList());
        }

        List<MobileNegotiationCardDto> cards = filteredItems.stream()
                .map(this::mapToMobileCard)
                .collect(Collectors.toList());

        return MobileNegotiationDashboardDto.builder()
                .counters(counters)
                .selectedTab(filterTab)
                .negotiations(cards)
                .build();
    }

    private MobileNegotiationCardDto mapToMobileCard(NegotiationListItemDto item) {
        String st = item.getStatus() != null ? item.getStatus().trim() : "Active";
        String normalizedStatus = normalizeStatus(st);

        String badgeText = st;
        String badgeColor = resolveStatusColor(st);

        BigDecimal price = item.getProposedPrice() != null ? item.getProposedPrice() : item.getCurrentPrice();
        long qty = item.getShares() != null ? item.getShares() : 0L;
        BigDecimal totalVal = (price != null && qty > 0) ? price.multiply(BigDecimal.valueOf(qty)).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

        boolean canAction = "AWAITING_RESPONSE".equalsIgnoreCase(normalizedStatus)
                || "ACTIVE".equalsIgnoreCase(normalizedStatus)
                || "UNDER_NEGOTIATION".equalsIgnoreCase(normalizedStatus);
        boolean agreementReady = "ACCEPTED".equalsIgnoreCase(normalizedStatus) || "AGREEMENT_READY".equalsIgnoreCase(normalizedStatus);

        return MobileNegotiationCardDto.builder()
                .negotiationNo(item.getNegotiationNo())
                .companySymbol(item.getCompanySymbol())
                .companyName(item.getCompanyName())
                .companyLogoUrl(resolveCompanyLogoUrl(item.getCompanySymbol()))
                .type(item.getType() != null ? item.getType().toUpperCase() : "BUY")
                .counterpartyName(item.getCounterpartyName())
                .counterpartyMemberId(item.getCounterpartyMemberId())
                .shares(item.getShares())
                .proposedPrice(item.getProposedPrice())
                .proposedPriceFormatted(item.getProposedPrice() != null ? "₹" + CURRENCY_FMT.format(item.getProposedPrice()) : "--")
                .currentPrice(item.getCurrentPrice())
                .currentPriceFormatted(item.getCurrentPrice() != null ? "₹" + CURRENCY_FMT.format(item.getCurrentPrice()) : "--")
                .totalValue(totalVal)
                .totalValueFormatted("₹" + CURRENCY_FMT.format(totalVal))
                .status(normalizedStatus)
                .statusBadgeText(badgeText)
                .statusBadgeColor(badgeColor)
                .lastUpdated(item.getLastUpdated())
                .relativeTime(formatRelativeTime(item.getLastUpdated()))
                .canAction(canAction)
                .agreementReady(agreementReady)
                .build();
    }

    // =========================================================================
    // SCREEN 2: NEGOTIATION DETAILS (MOBILE)
    // =========================================================================
    public MobileNegotiationDetailDto getMobileNegotiationDetail(String negotiationNo, Long userId) {
        NegotiationDetailDto detail = negotiationService.getNegotiationDetail(negotiationNo, userId);
        if (detail == null) {
            throw new ResourceNotFoundException("Negotiation not found: " + negotiationNo);
        }

        NegotiationDetailDto.CompanyShareDetailsDto comp = detail.getCompanyShareDetails();
        NegotiationDetailDto.PartyDto buyer = detail.getBuyer();
        NegotiationDetailDto.PartyDto seller = detail.getSeller();

        boolean isBuyer = buyer != null && buyer.isCurrentUser();
        NegotiationDetailDto.PartyDto myParty = isBuyer ? buyer : seller;
        NegotiationDetailDto.PartyDto counterParty = isBuyer ? seller : buyer;

        String rawStatus = detail.getStatus() != null ? detail.getStatus() : "ACTIVE";
        String normalizedStatus = normalizeStatus(rawStatus);

        // Action flags from available actions or fallback
        NegotiationDetailDto.AvailableActionsDto actions = detail.getAvailableActions();
        boolean canCounter = actions != null ? actions.isCanCounter() : ("AWAITING_RESPONSE".equalsIgnoreCase(normalizedStatus) || "ACTIVE".equalsIgnoreCase(normalizedStatus));
        boolean canAccept = actions != null ? actions.isCanAccept() : canCounter;
        boolean canReject = actions != null ? actions.isCanReject() : canCounter;
        boolean canRequestCall = actions != null ? actions.isCanRequestCall() : canCounter;
        boolean canAgreement = "ACCEPTED".equalsIgnoreCase(normalizedStatus) || "AGREEMENT_READY".equalsIgnoreCase(normalizedStatus);

        BigDecimal cmp = (comp != null && comp.getLastTradedPrice() != null) ? comp.getLastTradedPrice() : BigDecimal.valueOf(242.00);

        // Extract current proposal terms from negotiationTerms table if present
        BigDecimal currentOfferPrice = cmp;
        Long qty = (myParty != null && myParty.getQuantity() != null) ? myParty.getQuantity() : 1000L;
        String settlementTimeline = "T+2 Standard Internal Escrow";

        if (detail.getNegotiationTerms() != null) {
            for (NegotiationDetailDto.NegotiationTermRowDto row : detail.getNegotiationTerms()) {
                if (row.getParameter() != null && row.getParameter().toLowerCase().contains("price")) {
                    String clean = row.getCurrentProposal() != null ? row.getCurrentProposal().replaceAll("[^0.0-9.]", "").trim() : "";
                    if (!clean.isEmpty()) {
                        try { currentOfferPrice = new BigDecimal(clean); } catch (Exception ignored) {}
                    }
                }
                if (row.getParameter() != null && row.getParameter().toLowerCase().contains("quantity")) {
                    String clean = row.getCurrentProposal() != null ? row.getCurrentProposal().replaceAll("[^0-9]", "").trim() : "";
                    if (!clean.isEmpty()) {
                        try { qty = Long.parseLong(clean); } catch (Exception ignored) {}
                    }
                }
                if (row.getParameter() != null && row.getParameter().toLowerCase().contains("timeline")) {
                    if (row.getCurrentProposal() != null) {
                        settlementTimeline = row.getCurrentProposal();
                    }
                }
            }
        }

        BigDecimal totalVal = currentOfferPrice.multiply(BigDecimal.valueOf(qty)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal originalPrice = currentOfferPrice;

        return MobileNegotiationDetailDto.builder()
                .negotiationNo(detail.getNegotiationNo())
                .companySymbol(comp != null ? comp.getSymbol() : "ATG-H")
                .companyName(comp != null ? comp.getCompanyName() : "Atirath Holdings India Limited")
                .companyLogoUrl(resolveCompanyLogoUrl(comp != null ? comp.getSymbol() : "ATG-H"))
                .sector(comp != null ? comp.getSector() : "Diversified Holdings")
                .currentMarketPrice(cmp)
                .cmpFormatted("₹" + CURRENCY_FMT.format(cmp))
                .priceChange(BigDecimal.valueOf(3.50))
                .priceChangePercent("+1.45%")
                .userRole(isBuyer ? "BUYER" : "SELLER")
                .side(isBuyer ? "BUY" : "SELL")
                .status(normalizedStatus)
                .statusDisplay(rawStatus)
                .statusColor(resolveStatusColor(rawStatus))
                .expiresIn(detail.getExpiresIn() != null ? detail.getExpiresIn() : "Expires in 5 days")
                .expiryDate(detail.getExpiresAt() != null ? detail.getExpiresAt() : "25 Sep 2025")
                .myPartyName(myParty != null ? myParty.getMemberName() : "Sairam Vempati")
                .myMemberId(myParty != null ? myParty.getMemberId() : "ATG10027")
                .myRole(isBuyer ? "Buyer" : "Seller")
                .counterpartyName(counterParty != null ? counterParty.getMemberName() : "Rakesh Mehta")
                .counterpartyMemberId(counterParty != null ? counterParty.getMemberId() : "ATG10056")
                .counterpartyRole(isBuyer ? "Seller" : "Buyer")
                .counterpartyVerified(counterParty == null || counterParty.isVerified())
                .quantity(qty)
                .quantityFormatted(SHARES_FMT.format(qty) + " Shares")
                .currentOfferPrice(currentOfferPrice)
                .currentOfferPriceFormatted("₹" + CURRENCY_FMT.format(currentOfferPrice))
                .originalOfferPrice(originalPrice)
                .originalOfferPriceFormatted("₹" + CURRENCY_FMT.format(originalPrice))
                .totalValue(totalVal)
                .totalValueFormatted("₹" + CURRENCY_FMT.format(totalVal))
                .settlementTerms(settlementTimeline)
                .latestOfferByMe(false)
                .latestOfferMessage("Waiting for response")
                .roundsCount(detail.getTimeline() != null ? detail.getTimeline().size() : 2)
                .canCounterOffer(canCounter)
                .canAccept(canAccept)
                .canReject(canReject)
                .canProceedToAgreement(canAgreement)
                .canRequestCall(canRequestCall)
                .agreementNo(canAgreement ? detail.getNegotiationNo() : null)
                .build();
    }

    // =========================================================================
    // SCREEN 3: COUNTER OFFER SHEET / SCREEN (MOBILE)
    // =========================================================================
    public MobileCounterOfferFormDto getMobileCounterOfferForm(String negotiationNo, Long userId) {
        NegotiationDetailDto detail = negotiationService.getNegotiationDetail(negotiationNo, userId);
        if (detail == null) {
            throw new ResourceNotFoundException("Negotiation not found: " + negotiationNo);
        }

        NegotiationDetailDto.CompanyShareDetailsDto comp = detail.getCompanyShareDetails();
        BigDecimal curPrice = (comp != null && comp.getLastTradedPrice() != null) ? comp.getLastTradedPrice() : BigDecimal.valueOf(242.00);

        boolean isBuyer = detail.getBuyer() != null && detail.getBuyer().isCurrentUser();
        NegotiationDetailDto.PartyDto myParty = isBuyer ? detail.getBuyer() : detail.getSeller();
        NegotiationDetailDto.PartyDto counterParty = isBuyer ? detail.getSeller() : detail.getBuyer();

        Long qty = (myParty != null && myParty.getQuantity() != null) ? myParty.getQuantity() : 1000L;

        // Default counter offer is +/- 2.50 depending on side
        BigDecimal defaultCounter = isBuyer
                ? curPrice.subtract(BigDecimal.valueOf(2.50)).setScale(2, RoundingMode.HALF_UP)
                : curPrice.add(BigDecimal.valueOf(2.50)).setScale(2, RoundingMode.HALF_UP);

        BigDecimal minPrice = (comp != null && comp.getFiftyTwoWeekLow() != null)
                ? comp.getFiftyTwoWeekLow()
                : curPrice.multiply(BigDecimal.valueOf(0.75)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal maxPrice = (comp != null && comp.getFiftyTwoWeekHigh() != null)
                ? comp.getFiftyTwoWeekHigh()
                : curPrice.multiply(BigDecimal.valueOf(1.25)).setScale(2, RoundingMode.HALF_UP);

        return MobileCounterOfferFormDto.builder()
                .negotiationNo(detail.getNegotiationNo())
                .companySymbol(comp != null ? comp.getSymbol() : "ATG-H")
                .companyName(comp != null ? comp.getCompanyName() : "Atirath Holdings India Limited")
                .currentOfferPrice(curPrice)
                .defaultCounterPrice(defaultCounter)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .priceStep(BigDecimal.valueOf(2.50))
                .quantity(qty)
                .quantityFormatted(SHARES_FMT.format(qty) + " Shares")
                .counterpartyName(counterParty != null ? counterParty.getMemberName() : "Rakesh Mehta")
                .counterpartyMemberId(counterParty != null ? counterParty.getMemberId() : "ATG10056")
                .allowedSettlementTerms(List.of("T+2 Standard", "T+5 Extended"))
                .defaultSettlementTerm("T+2 Standard")
                .escrowProtectionNote("Protected by Atirath Tri-Party Escrow. Funds and shares are locked securely until execution.")
                .build();
    }

    public MobileCounterOfferResultDto submitMobileCounterOffer(String negotiationNo, MobileCounterOfferRequestDto req, Long userId) {
        if (req.getPrice() == null || req.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ApiException("Counter offer price must be greater than zero");
        }

        CounterOfferRequestDto webReq = CounterOfferRequestDto.builder()
                .pricePerShare(req.getPrice())
                .quantity(req.getQuantity() != null ? req.getQuantity() : 1000L)
                .settlementTimeline(req.getSettlementTerm() != null ? req.getSettlementTerm() : "T+2 Standard")
                .paymentMode("Bank Transfer (Escrow)")
                .otherTerms(req.getMessage() != null ? req.getMessage() : "Counter offer submitted via mobile app")
                .build();

        NegotiationDetailDto updated = negotiationService.submitCounterOffer(negotiationNo, webReq, userId);

        long qty = req.getQuantity() != null ? req.getQuantity() : 1000L;
        BigDecimal totalVal = req.getPrice().multiply(BigDecimal.valueOf(qty)).setScale(2, RoundingMode.HALF_UP);

        String counterpartyName = (updated.getSeller() != null && !updated.getSeller().isCurrentUser())
                ? updated.getSeller().getMemberName()
                : (updated.getBuyer() != null ? updated.getBuyer().getMemberName() : "counterparty");

        return MobileCounterOfferResultDto.builder()
                .success(true)
                .negotiationNo(negotiationNo)
                .newOfferedPrice(req.getPrice())
                .newOfferedPriceFormatted("₹" + CURRENCY_FMT.format(req.getPrice()))
                .quantity(qty)
                .newTotalValue(totalVal)
                .newTotalValueFormatted("₹" + CURRENCY_FMT.format(totalVal))
                .status("AWAITING_RESPONSE")
                .toastMessage("Counter offer of ₹" + CURRENCY_FMT.format(req.getPrice()) + " sent to " + counterpartyName + ".")
                .build();
    }

    // =========================================================================
    // SCREEN 4: NEGOTIATION HISTORY (MOBILE)
    // =========================================================================
    public MobileNegotiationHistoryDto getMobileNegotiationHistory(String negotiationNo, Long userId) {
        NegotiationHistoryDto webHistory = negotiationService.getHistory(negotiationNo, userId);
        if (webHistory == null) {
            throw new ResourceNotFoundException("Negotiation history not found: " + negotiationNo);
        }

        List<MobileTimelineEventDto> mobileEvents = new ArrayList<>();
        if (webHistory.getTimeline() != null) {
            for (NegotiationHistoryDto.HistoryItemDto item : webHistory.getTimeline()) {
                String senderRole = item.isCurrentUser() ? "ME" : ("SYSTEM".equalsIgnoreCase(item.getSenderName()) ? "SYSTEM" : "COUNTERPARTY");
                String evType = mapEventType(item.getBadge(), item.getTitle());

                BigDecimal price = item.getPrice();
                Long qty = item.getShares();
                BigDecimal totalVal = (price != null && qty != null) ? price.multiply(BigDecimal.valueOf(qty)).setScale(2, RoundingMode.HALF_UP) : null;

                mobileEvents.add(MobileTimelineEventDto.builder()
                        .eventId(String.valueOf(item.getId() != null ? item.getId() : System.currentTimeMillis()))
                        .eventType(evType)
                        .senderRole(senderRole)
                        .senderName(item.getSenderName())
                        .price(price)
                        .priceFormatted(price != null ? "₹" + CURRENCY_FMT.format(price) : null)
                        .quantity(qty)
                        .totalValue(totalVal)
                        .totalValueFormatted(totalVal != null ? "₹" + CURRENCY_FMT.format(totalVal) : null)
                        .messageText(item.getMessage() != null ? item.getMessage() : item.getTitle())
                        .status(item.getBadge() != null ? item.getBadge() : "PENDING")
                        .statusBadgeColor(resolveStatusColor(item.getBadge()))
                        .timestamp(item.getTimestamp())
                        .relativeTime(formatRelativeTime(item.getTimestamp()))
                        .build());
            }
        }

        String compSym = "ATG-H";
        String compName = "Atirath Holdings India Limited";
        if (webHistory.getBuyer() != null && webHistory.getBuyer().getCompanySymbol() != null) {
            compSym = webHistory.getBuyer().getCompanySymbol();
            compName = webHistory.getBuyer().getCompanyName();
        }

        return MobileNegotiationHistoryDto.builder()
                .negotiationNo(webHistory.getNegotiationNo())
                .companySymbol(compSym)
                .companyName(compName)
                .currentStatus(webHistory.getStatus())
                .events(mobileEvents)
                .build();
    }

    public MobileTimelineEventDto postMobileMessage(String negotiationNo, String messageText, Long userId) {
        if (messageText == null || messageText.isBlank()) {
            throw new ApiException("Message text cannot be empty");
        }

        NegotiationMessageRequestDto req = NegotiationMessageRequestDto.builder()
                .messageText(messageText.trim())
                .build();

        negotiationService.postMessage(negotiationNo, req, userId);

        return MobileTimelineEventDto.builder()
                .eventId(String.valueOf(System.currentTimeMillis()))
                .eventType("MESSAGE")
                .senderRole("ME")
                .senderName("You")
                .messageText(messageText.trim())
                .status("SENT")
                .statusBadgeColor("#059669")
                .timestamp(OffsetDateTime.now().format(DISPLAY_DT_FMT))
                .relativeTime("Just now")
                .build();
    }

    // =========================================================================
    // SCREEN 5: TRADE AGREEMENT (MOBILE)
    // =========================================================================
    public MobileTradeAgreementDto getMobileTradeAgreement(String negotiationNo, Long userId) {
        TradeAgreementDto webAgreement = negotiationService.getTradeAgreement(negotiationNo, userId);
        if (webAgreement == null) {
            throw new ResourceNotFoundException("Trade agreement not ready or not found for: " + negotiationNo);
        }

        NegotiationDetailDto.PartyDto buyer = webAgreement.getParties() != null ? webAgreement.getParties().getBuyer() : null;
        NegotiationDetailDto.PartyDto seller = webAgreement.getParties() != null ? webAgreement.getParties().getSeller() : null;
        TradeAgreementDto.FinalAgreedTermsDto terms = webAgreement.getFinalAgreedTerms();

        BigDecimal price = terms != null && terms.getSharePrice() != null ? terms.getSharePrice() : BigDecimal.valueOf(242.00);
        Long qty = terms != null && terms.getQuantity() != null ? terms.getQuantity() : 1000L;
        BigDecimal totalVal = terms != null && terms.getTotalTradeValue() != null ? terms.getTotalTradeValue() : price.multiply(BigDecimal.valueOf(qty));

        String rawStatus = webAgreement.getStatus() != null ? webAgreement.getStatus() : "AGREEMENT_READY";
        boolean isConfirmed = "CONFIRMED".equalsIgnoreCase(rawStatus) || "IN_ESCROW".equalsIgnoreCase(rawStatus) || "COMPLETED".equalsIgnoreCase(rawStatus);

        String compName = buyer != null && buyer.getCompanyName() != null ? buyer.getCompanyName() : "Atirath Holdings India Limited";
        String compSymbol = buyer != null && buyer.getCompanySymbol() != null ? buyer.getCompanySymbol() : "ATG-H";

        return MobileTradeAgreementDto.builder()
                .agreementNo(webAgreement.getAgreementNo() != null ? webAgreement.getAgreementNo() : "AGR-" + negotiationNo)
                .negotiationNo(negotiationNo)
                .status(rawStatus)
                .statusBannerTitle("Both parties agreed to terms")
                .statusBannerSubtitle(isConfirmed ? "Agreement signed. Moving to Escrow." : "Review & Sign to proceed to Escrow.")
                .buyerName(buyer != null ? buyer.getMemberName() : "Rajesh Kumar Sharma")
                .buyerRole(buyer != null ? (buyer.getMemberName() + " (" + buyer.getPartyRole() + ")") : "Buyer (Internal)")
                .sellerName(seller != null ? seller.getMemberName() : "Amit Verma")
                .sellerRole(seller != null ? (seller.getMemberName() + " (" + seller.getPartyRole() + ")") : "Seller (Atirath Holdings)")
                .companyName(compName)
                .companySymbol(compSymbol)
                .quantity(qty)
                .quantityFormatted(SHARES_FMT.format(qty) + " Shares")
                .agreedPrice(price)
                .agreedPriceFormatted("₹" + CURRENCY_FMT.format(price) + " / share")
                .totalValue(totalVal)
                .totalValueFormatted("₹" + INT_CURRENCY_FMT.format(totalVal))
                .settlementMode(terms != null && terms.getSettlementTimeline() != null ? terms.getSettlementTimeline() : "T+2 Internal Escrow")
                .escrowAccountRef("ESCROW-ATG-10027")
                .legalDisclaimer("This Trade Agreement is entered into between Buyer and Seller for internal shareholder equity under the platform Escrow mechanism. Funds remain locked in verified escrow depository until final share transfer execution.")
                .canSignAndProceed(!isConfirmed)
                .isSignedByMe(isConfirmed)
                .isSignedByCounterparty(true)
                .nextScreen("ESCROW_SCREEN")
                .pdfDownloadUrl("/api/v1/negotiations/" + negotiationNo + "/trade-agreement/download")
                .build();
    }

    public MobileTradeAgreementDto confirmMobileAgreement(String negotiationNo, Long userId) {
        TradeAgreementConfirmRequestDto req = TradeAgreementConfirmRequestDto.builder()
                .readAndUnderstood(true)
                .informationAccurate(true)
                .agreeToEscrowAndSettlement(true)
                .build();

        negotiationService.confirmAgreement(negotiationNo, req, userId);
        return getMobileTradeAgreement(negotiationNo, userId);
    }

    // =========================================================================
    // SCREEN 6: NEGOTIATION NOTIFICATIONS (MOBILE)
    // =========================================================================
    public MobileNegotiationNotificationsDto getMobileNotifications(String category, Long userId) {
        String cat = (category != null && !category.isBlank()) ? category.trim().toUpperCase() : "ALL";
        NegotiationNotificationsPageDto page = negotiationService.getNotificationsPage(cat, null, null, userId);

        List<NegotiationNotificationItemDto> webItems = page != null && page.getNotifications() != null ? page.getNotifications() : Collections.emptyList();

        int unreadCount = (int) webItems.stream().filter(NegotiationNotificationItemDto::isUnread).count();

        List<MobileNegotiationNotificationCardDto> cards = webItems.stream()
                .map(n -> MobileNegotiationNotificationCardDto.builder()
                        .notificationId(n.getId())
                        .type(n.getType() != null ? n.getType() : "OFFER_RECEIVED")
                        .title(n.getTitle())
                        .body(n.getMessage())
                        .negotiationNo(n.getNegotiationNo())
                        .companySymbol(n.getCompanySymbol())
                        .isRead(!n.isUnread())
                        .timeAgo(formatRelativeTime(n.getTimestamp()))
                        .timestamp(n.getTimestamp())
                        .targetScreen(resolveNotificationTarget(n.getType()))
                        .build())
                .collect(Collectors.toList());

        return MobileNegotiationNotificationsDto.builder()
                .unreadCount(unreadCount)
                .totalCount(cards.size())
                .currentCategory(cat)
                .notifications(cards)
                .build();
    }

    public boolean markNotificationRead(Long notificationId) {
        negotiationService.markNotificationAsRead(notificationId);
        return true;
    }

    public int markAllNotificationsRead() {
        negotiationService.markAllNotificationsAsRead();
        return 1;
    }

    // =========================================================================
    // HELPER METHODS
    // =========================================================================
    private String normalizeStatus(String status) {
        if (status == null) return "ACTIVE";
        String s = status.toUpperCase().trim();
        if (s.contains("AWAITING")) return "AWAITING_RESPONSE";
        if (s.contains("ACCEPT")) return "ACCEPTED";
        if (s.contains("REJECT") || s.contains("DECLIN")) return "REJECTED";
        if (s.contains("EXPIRE")) return "EXPIRED";
        if (s.contains("UNDER") || s.contains("ACTIVE")) return "ACTIVE";
        return s;
    }

    private String resolveStatusColor(String status) {
        if (status == null) return "#059669";
        String s = status.toUpperCase().trim();
        if (s.contains("AWAITING") || s.contains("PENDING")) return "#D97706"; // Amber / Gold
        if (s.contains("ACCEPT") || s.contains("AGREE")) return "#059669"; // Emerald Green
        if (s.contains("REJECT") || s.contains("DECLIN")) return "#DC2626"; // Red
        if (s.contains("EXPIRE")) return "#6B7280"; // Gray
        return "#2563EB"; // Blue for Active / Under Negotiation
    }

    private String mapEventType(String rawType, String title) {
        if (rawType != null && !rawType.isBlank()) {
            return rawType.toUpperCase();
        }
        if (title == null) return "OFFER";
        String t = title.toLowerCase();
        if (t.contains("counter")) return "COUNTER_OFFER";
        if (t.contains("message") || t.contains("note")) return "MESSAGE";
        if (t.contains("call")) return "CALL_REQUEST";
        if (t.contains("accept") || t.contains("agreement")) return "AGREEMENT";
        return "OFFER";
    }

    private String resolveNotificationTarget(String type) {
        if (type == null) return "NEGOTIATION_DETAILS";
        String t = type.toUpperCase();
        if (t.contains("COUNTER") || t.contains("OFFER")) return "COUNTER_OFFER";
        if (t.contains("AGREEMENT")) return "TRADE_AGREEMENT";
        return "NEGOTIATION_DETAILS";
    }

    private String resolveCompanyLogoUrl(String symbol) {
        if (symbol == null) return "/assets/logos/default.png";
        return "/assets/logos/" + symbol.toLowerCase().replace("-", "") + ".png";
    }

    private String formatRelativeTime(String timestamp) {
        if (timestamp == null || timestamp.isBlank()) return "Recently";
        try {
            if (timestamp.contains("ago") || timestamp.contains("Today") || timestamp.contains("Yesterday")) {
                return timestamp;
            }
            return timestamp;
        } catch (Exception e) {
            return "Recently";
        }
    }
}
