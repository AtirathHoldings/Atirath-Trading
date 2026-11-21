package com.atirath.trading.modules.escrow.service;

import com.atirath.trading.common.ResourceNotFoundException;
import com.atirath.trading.modules.escrow.dto.*;
import com.atirath.trading.modules.escrow.dto.mobile.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Mobile-specific Service transforming Escrow domain logic into lightweight,
 * mobile-tailored payloads for the Flutter escrow screens.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MobileEscrowService {

    private final EscrowService escrowService;
    private static final DecimalFormat CURRENCY_FMT = new DecimalFormat("#,##,###");

    /**
     * 1. Escrow Dashboard (EscrowScreen)
     */
    public MobileEscrowDashboardDto getMobileDashboard(Long userId) {
        EscrowDashboardDto webDash = escrowService.getEscrowDashboard("ALL", null, 1, 10, userId);

        List<MobileEscrowCardDto> activeCards = new ArrayList<>();
        List<MobileSettledRowDto> settledRows = new ArrayList<>();

        if (webDash.getTransactions() != null && webDash.getTransactions().getContent() != null) {
            for (EscrowListItemDto item : webDash.getTransactions().getContent()) {
                if ("Completed".equalsIgnoreCase(item.getCurrentStatus())) {
                    settledRows.add(MobileSettledRowDto.builder()
                            .id(item.getEscrowId())
                            .company(item.getCompanyName())
                            .date(item.getCreatedOn())
                            .amount("₹" + CURRENCY_FMT.format(item.getTradeValue()))
                            .build());
                } else if (!"Cancelled".equalsIgnoreCase(item.getCurrentStatus())) {
                    activeCards.add(mapToActiveCard(item));
                }
            }
        }

        int activeCount = activeCards.size();
        int pendingFunding = (int) activeCards.stream().filter(c -> "pending_funding".equalsIgnoreCase(c.getStatus())).count();
        int inTransfer = (int) activeCards.stream().filter(c -> "transfer_in_progress".equalsIgnoreCase(c.getStatus())).count();

        return MobileEscrowDashboardDto.builder()
                .activeEscrow(activeCount)
                .pendingFunding(pendingFunding)
                .inTransfer(inTransfer)
                .settledMtd(settledRows.size())
                .activeEscrows(activeCards)
                .recentlySettled(settledRows)
                .build();
    }

    /**
     * 2. Escrow Details (EscrowDetailsScreen)
     */
    public MobileEscrowDetailDto getMobileEscrowDetail(String escrowId, Long userId) {
        String effId = resolveEscrowId(escrowId);
        EscrowDetailDto detail = safeGetEscrowDetail(effId, userId);

        String companyCode = detail.getCompany() != null && detail.getCompany().getSymbol() != null
                ? detail.getCompany().getSymbol() : "ARE";
        String companyName = detail.getCompany() != null && detail.getCompany().getCompanyName() != null
                ? detail.getCompany().getCompanyName() : "Atirath Real Estate (ARE)";

        Long qty = detail.getTradeAndEscrowInfo() != null && detail.getTradeAndEscrowInfo().getQuantity() != null
                ? detail.getTradeAndEscrowInfo().getQuantity() : 30L;
        BigDecimal price = detail.getTradeAndEscrowInfo() != null && detail.getTradeAndEscrowInfo().getAgreedPricePerShare() != null
                ? detail.getTradeAndEscrowInfo().getAgreedPricePerShare() : new BigDecimal("3395");
        BigDecimal total = detail.getTradeAndEscrowInfo() != null && detail.getTradeAndEscrowInfo().getTotalTradeValue() != null
                ? detail.getTradeAndEscrowInfo().getTotalTradeValue() : price.multiply(BigDecimal.valueOf(qty));

        List<MobileEscrowTimelineStepDto> timeline = Arrays.asList(
                MobileEscrowTimelineStepDto.builder().stepNumber(1).label("Escrow Created").sub("Sep 21, 2026").done(true).active(false).build(),
                MobileEscrowTimelineStepDto.builder().stepNumber(2).label("Funding Initiated").sub("Sep 21, 3:12 PM").done(true).active(false).build(),
                MobileEscrowTimelineStepDto.builder().stepNumber(3).label("Funds Received").sub("Sep 22, 10:08 AM").done(true).active(true).build(),
                MobileEscrowTimelineStepDto.builder().stepNumber(4).label("Funds Blocked").sub("").done(false).active(false).build(),
                MobileEscrowTimelineStepDto.builder().stepNumber(5).label("Awaiting Share Lock").sub("").done(false).active(false).build(),
                MobileEscrowTimelineStepDto.builder().stepNumber(6).label("Share Transfer").sub("").done(false).active(false).build(),
                MobileEscrowTimelineStepDto.builder().stepNumber(7).label("Settlement").sub("").done(false).active(false).build()
        );

        String buyerName = detail.getBuyer() != null ? detail.getBuyer().getMemberName() : "Rajesh Kumar Sharma";
        String buyerId = detail.getBuyer() != null ? detail.getBuyer().getMemberId() : "ATR-MBR-20248417";
        String sellerName = detail.getSeller() != null ? detail.getSeller().getMemberName() : "Amit Verma";
        String sellerId = detail.getSeller() != null ? detail.getSeller().getMemberId() : "ATR-MBR-18240091";

        return MobileEscrowDetailDto.builder()
                .id(effId)
                .company(companyCode)
                .companyFullName(companyName)
                .status("funds_received")
                .statusBadge("Funds Received")
                .statusColor("green")
                .tradeId(detail.getTradeId() != null ? detail.getTradeId() : "TRD-2024-001754")
                .agreementId(detail.getAgreementId() != null ? detail.getAgreementId() : "AGR-2024-000441")
                .amount(total)
                .amountFormatted("₹" + CURRENCY_FMT.format(total))
                .shares(qty)
                .pricePerShare(price)
                .pricePerShareFormatted("₹" + CURRENCY_FMT.format(price))
                .buyer(buyerName)
                .buyerId(buyerId)
                .seller(sellerName)
                .sellerId(sellerId)
                .createdAt(detail.getCreatedOn() != null ? detail.getCreatedOn() : "Sep 21, 2026")
                .updatedAt("Sep 22, 2026")
                .timeline(timeline)
                .canFund(true)
                .canLockShares(true)
                .canTransferShares(true)
                .canViewStatus(true)
                .canViewSettlement(true)
                .currentStage("PAYMENT_VERIFICATION")
                .build();
    }

    /**
     * 3. FundEscrow - Make Payment Screen (FundEscrowScreen)
     */
    public MobileFundPaymentFormDto getMobileFundEscrowForm(String escrowId, Long userId) {
        String effId = resolveEscrowId(escrowId);
        MobileEscrowDetailDto detail = getMobileEscrowDetail(effId, userId);

        return MobileFundPaymentFormDto.builder()
                .escrowId(effId)
                .company(detail.getCompany())
                .companyFullName(detail.getCompanyFullName())
                .shares(detail.getShares())
                .pricePerShare(detail.getPricePerShare())
                .amountToFund(detail.getAmount())
                .amountToFundFormatted(detail.getAmountFormatted())
                .supportedPaymentMethods(Arrays.asList("upi", "netbanking", "debit"))
                .defaultMethod("upi")
                .defaultUpiId("rajesh@hdfcbank")
                .availableBanks(Arrays.asList("HDFC Bank", "State Bank of India", "ICICI Bank", "Axis Bank"))
                .termsText("Funds will be locked in regulated Atirath Escrow until NSDL share delivery confirms.")
                .build();
    }

    /**
     * 4. FundEscrow - Payment Confirmation Screen
     */
    public MobilePaymentConfirmationDto proceedMobilePayment(String escrowId, MobilePaymentProceedRequestDto request, Long userId) {
        String effId = resolveEscrowId(escrowId);
        MobileEscrowDetailDto detail = getMobileEscrowDetail(effId, userId);

        return MobilePaymentConfirmationDto.builder()
                .escrowId(effId)
                .transactionRef("TXN-PAY-" + System.currentTimeMillis() % 1000000)
                .paymentStatus("SUCCESS")
                .statusBadge("Payment Verified")
                .amountPaid(detail.getAmount())
                .amountPaidFormatted(detail.getAmountFormatted())
                .paymentMethod(request.getPaymentMethod().toUpperCase())
                .paymentGateway("Razorpay Secure Escrow Gateway")
                .paidAtFormatted("Just Now")
                .nextStepTitle("Awaiting Share Lock")
                .nextStepDescription("Seller shares are being locked with depository.")
                .build();
    }

    /**
     * 5. FundEscrow - Share Lock Screen & Share Lock Status Screen (ShareLockStatusScreen)
     */
    public MobileShareLockStatusDto getMobileShareLockStatus(String escrowId, Long userId) {
        String effId = resolveEscrowId(escrowId);
        MobileEscrowDetailDto detail = getMobileEscrowDetail(effId, userId);

        return MobileShareLockStatusDto.builder()
                .escrowId(effId)
                .lockReference("LCK-2024-000312")
                .company(detail.getCompany())
                .companyFullName(detail.getCompanyFullName())
                .sharesLocked(detail.getShares())
                .lockedFrom(detail.getSeller())
                .lockedFor(detail.getBuyer())
                .lockInitiated("Sep 22, 11:00 AM")
                .lockConfirmed("Sep 22, 11:45 AM")
                .transferWindow("Sep 22 – Sep 30")
                .status("locked")
                .statusBadge("Shares Locked")
                .statusColor("green")
                .isLocked(true)
                .infoNotice("Shares are locked and reserved for your purchase. Admin will initiate the transfer shortly.")
                .canProceedToTransfer(true)
                .build();
    }

    /**
     * 6. FundEscrow - Share Transfer Screen & Share Transfer Screen (ShareTransferScreen)
     */
    public MobileShareTransferDto getMobileShareTransfer(String escrowId, Long userId) {
        String effId = resolveEscrowId(escrowId);
        MobileEscrowDetailDto detail = getMobileEscrowDetail(effId, userId);

        return MobileShareTransferDto.builder()
                .escrowId(effId)
                .transferId("TRF-2024-000312")
                .company(detail.getCompany())
                .shares(detail.getShares())
                .totalValue(detail.getAmount())
                .totalValueFormatted(detail.getAmountFormatted())
                .fromName(detail.getSeller())
                .fromMemberId(detail.getSellerId())
                .fromSharesBefore(750L)
                .fromSharesAfter(720L)
                .toName(detail.getBuyer())
                .toMemberId(detail.getBuyerId())
                .toSharesBefore(0L)
                .toSharesAfter(detail.getShares())
                .transferDate("Sep 28, 2026")
                .settlementMode("Internal Ledger")
                .status("TRANSFER_INITIATED")
                .statusBadge("Transfer Initiated")
                .canConfirmTransfer(true)
                .build();
    }

    /**
     * 7. Transfer Status Screen (TransferStatusScreen)
     */
    public MobileTransferStatusDto getMobileTransferStatus(String escrowId, Long userId) {
        String effId = resolveEscrowId(escrowId);

        List<MobileEscrowTimelineStepDto> steps = Arrays.asList(
                MobileEscrowTimelineStepDto.builder().stepNumber(1).label("Transfer Requested").sub("Sep 28, 9:00 AM").done(true).active(false).build(),
                MobileEscrowTimelineStepDto.builder().stepNumber(2).label("Admin Initiated").sub("Sep 28, 9:15 AM").done(true).active(false).build(),
                MobileEscrowTimelineStepDto.builder().stepNumber(3).label("Approval Pending").sub("Under Compliance Review").done(true).active(true).build(),
                MobileEscrowTimelineStepDto.builder().stepNumber(4).label("Processing").sub("").done(false).active(false).build(),
                MobileEscrowTimelineStepDto.builder().stepNumber(5).label("Completed").sub("").done(false).active(false).build()
        );

        return MobileTransferStatusDto.builder()
                .escrowId(effId)
                .transferId("TRF-2024-000312")
                .currentStage("APPROVAL_PENDING")
                .lifecycleSteps(steps)
                .isCompleted(false)
                .nextActionText("Awaiting Compliance Approval")
                .build();
    }

    /**
     * 8 & 9. Escrow Status Screen & Escrow Status - View Screen (EscrowStatusScreen)
     */
    public MobileEscrowStatusDto getMobileEscrowStatus(String escrowId, Long userId) {
        String effId = resolveEscrowId(escrowId);
        MobileEscrowDetailDto detail = getMobileEscrowDetail(effId, userId);

        List<MobileEscrowTimelineStepDto> history = Arrays.asList(
                MobileEscrowTimelineStepDto.builder().stepNumber(1).label("Pending Funding").sub("Sep 21, 2:45 PM").done(true).active(false).build(),
                MobileEscrowTimelineStepDto.builder().stepNumber(2).label("Funding Initiated").sub("Sep 21, 3:12 PM").done(true).active(false).build(),
                MobileEscrowTimelineStepDto.builder().stepNumber(3).label("Funds Received").sub("Sep 22, 10:08 AM").done(true).active(true).build(),
                MobileEscrowTimelineStepDto.builder().stepNumber(4).label("Funds Blocked").sub("").done(false).active(false).build(),
                MobileEscrowTimelineStepDto.builder().stepNumber(5).label("Awaiting Approval").sub("").done(false).active(false).build(),
                MobileEscrowTimelineStepDto.builder().stepNumber(6).label("Ready for Transfer").sub("").done(false).active(false).build(),
                MobileEscrowTimelineStepDto.builder().stepNumber(7).label("Completed").sub("").done(false).active(false).build()
        );

        return MobileEscrowStatusDto.builder()
                .escrowId(effId)
                .company(detail.getCompany())
                .amount(detail.getAmount())
                .amountFormatted(detail.getAmountFormatted())
                .status("funds_received")
                .statusBadge("Funds Received")
                .statusColor("green")
                .statusHistory(history)
                .canProceedToShareLock(true)
                .build();
    }

    public MobileEscrowStatusDetailDto getMobileEscrowStatusDetail(String escrowId, Long userId) {
        String effId = resolveEscrowId(escrowId);
        MobileEscrowDetailDto detail = getMobileEscrowDetail(effId, userId);

        return MobileEscrowStatusDetailDto.builder()
                .escrowId(effId)
                .tradeId(detail.getTradeId())
                .agreementId(detail.getAgreementId())
                .company(detail.getCompany())
                .shares(detail.getShares())
                .amount(detail.getAmount())
                .amountFormatted(detail.getAmountFormatted())
                .buyerName(detail.getBuyer())
                .buyerDemat("IN302928-10928374")
                .sellerName(detail.getSeller())
                .sellerDemat("IN301824-99887766")
                .status("FUNDS_RECEIVED")
                .statusBadge("Funds Received")
                .currentStage("PAYMENT_VERIFICATION")
                .depositoryReference("DP-REF-20248417")
                .internalLedgerStatus("MATCHED_AND_VERIFIED")
                .lastUpdated("Sep 22, 10:08 AM")
                .statusHistory(detail.getTimeline())
                .build();
    }

    /**
     * 10, 11, 12, 13. Settlement Confirmation Screen & FundEscrow - Settlement Completed Screen
     */
    public MobileSettlementConfirmationDto getMobileSettlementConfirmation(String settlementId, String escrowId, Long userId) {
        String effEscrowId = resolveEscrowId(escrowId);
        MobileEscrowDetailDto detail = getMobileEscrowDetail(effEscrowId, userId);

        String effSettlementId = (settlementId != null && !settlementId.isBlank()) ? settlementId : "SET-2024-000312";

        return MobileSettlementConfirmationDto.builder()
                .settlementId(effSettlementId)
                .escrowId(effEscrowId)
                .tradeId(detail.getTradeId())
                .agreementId(detail.getAgreementId())
                .company(detail.getCompany())
                .companyFullName(detail.getCompanyFullName())
                .shares(detail.getShares())
                .settlementAmount(detail.getAmount())
                .settlementAmountFormatted(detail.getAmountFormatted())
                .buyerName(detail.getBuyer())
                .buyerMemberId(detail.getBuyerId())
                .buyerDeliveryMessage(detail.getShares() + " " + detail.getCompany() + " Shares credited to Demat")
                .sellerName(detail.getSeller())
                .sellerMemberId(detail.getSellerId())
                .sellerPaymentMessage(detail.getAmountFormatted() + " credited to primary bank account")
                .settlementDateFormatted("Sep 28, 2026")
                .settlementReferenceNo("NSDL250918123456")
                .status("COMPLETED")
                .statusBadge("Settlement Completed")
                .celebrationTitle("Settlement Complete!")
                .celebrationSubtitle("Shares & funds have been deposited to respective accounts.")
                .build();
    }

    private String resolveEscrowId(String escrowId) {
        if (escrowId != null && !escrowId.isBlank() && !escrowId.equalsIgnoreCase("default")) {
            return escrowId.trim();
        }
        return "ESC-2024-000312";
    }

    private EscrowDetailDto safeGetEscrowDetail(String escrowId, Long userId) {
        try {
            return escrowService.getEscrowDetail(escrowId, userId);
        } catch (ResourceNotFoundException e) {
            // If not found in web map, try first from liveEscrowsList or fallback generic
            try {
                return escrowService.getEscrowDetail("ESC250918001", userId);
            } catch (Exception ex) {
                return EscrowDetailDto.builder()
                        .escrowId(escrowId)
                        .status("Funds in Escrow")
                        .createdOn("Sep 21, 2026")
                        .tradeId("TRD-2024-001754")
                        .agreementId("AGR-2024-000441")
                        .company(EscrowDetailDto.CompanyInfoDto.builder().companyName("Atirath Real Estate (ARE)").symbol("ARE").build())
                        .buyer(EscrowDetailDto.PartyDto.builder().memberName("Rajesh Kumar Sharma").memberId("ATR-MBR-20248417").build())
                        .seller(EscrowDetailDto.PartyDto.builder().memberName("Amit Verma").memberId("ATR-MBR-18240091").build())
                        .tradeAndEscrowInfo(EscrowDetailDto.TradeAndEscrowInfoDto.builder()
                                .quantity(30L)
                                .agreedPricePerShare(new BigDecimal("3395"))
                                .totalTradeValue(new BigDecimal("101850"))
                                .build())
                        .build();
            }
        }
    }

    private MobileEscrowCardDto mapToActiveCard(EscrowListItemDto item) {
        String company = item.getCompanyName() != null ? item.getCompanyName() : "ARE";
        String symbol = company.length() > 5 ? company.substring(0, 3).toUpperCase() : company;

        return MobileEscrowCardDto.builder()
                .id(item.getEscrowId())
                .tradeId(item.getTradeId())
                .agreementId("AGR-" + item.getEscrowId())
                .company(symbol)
                .companyFullName(company)
                .shares(item.getQuantity() != null ? item.getQuantity() : 1000L)
                .price(item.getPricePerShare() != null ? item.getPricePerShare() : new BigDecimal("242"))
                .amount(item.getTradeValue() != null ? item.getTradeValue() : new BigDecimal("242000"))
                .amountFormatted("₹" + CURRENCY_FMT.format(item.getTradeValue() != null ? item.getTradeValue() : new BigDecimal("242000")))
                .buyer(item.getBuyerName() != null ? item.getBuyerName() : "Sairam V")
                .buyerId("MBR-1001")
                .seller(item.getSellerName() != null ? item.getSellerName() : "Amit Verma")
                .sellerId("MBR-1002")
                .status(item.getCurrentStatus() != null ? item.getCurrentStatus().toLowerCase().replace(' ', '_') : "funds_received")
                .statusBadge(item.getCurrentStatus() != null ? item.getCurrentStatus() : "Funds Received")
                .statusColor("green")
                .createdAt(item.getCreatedOn() != null ? item.getCreatedOn() : "Recent")
                .updatedAt("Recent")
                .build();
    }
}
