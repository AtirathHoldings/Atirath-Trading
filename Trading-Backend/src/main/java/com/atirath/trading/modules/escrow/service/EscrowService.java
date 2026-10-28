package com.atirath.trading.modules.escrow.service;

import com.atirath.trading.common.ResourceNotFoundException;
import com.atirath.trading.modules.escrow.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class EscrowService {

    private static final DecimalFormat CURRENCY_FMT = new DecimalFormat("#,##,##0.00");
    private static final DecimalFormat SHARES_FMT = new DecimalFormat("#,##,###");

    private final List<EscrowListItemDto> liveEscrowsList = Collections.synchronizedList(new ArrayList<>());
    private final Map<String, EscrowDetailDto> liveEscrowDetails = new ConcurrentHashMap<>();
    private final Map<String, String> liveEscrowCurrentStages = new ConcurrentHashMap<>();
    private final List<EscrowStatusItemDto> liveEscrowStatusList = Collections.synchronizedList(new ArrayList<>());
    private final List<ShareLockItemDto> liveShareLocksList = Collections.synchronizedList(new ArrayList<>());
    private final List<ShareTransferItemDto> liveShareTransfersList = Collections.synchronizedList(new ArrayList<>());

    {
        initDemoData();
    }

    public void resetDemoData() {
        initDemoData();
    }

    private void initDemoData() {
        liveEscrowsList.clear();
        liveEscrowDetails.clear();
        liveEscrowCurrentStages.clear();
        liveEscrowCurrentStages.put("ESC250918001", "FUND_ESCROW");

        // 1. Escrow Table Rows (Image 7)
        liveEscrowsList.add(EscrowListItemDto.builder()
                .rowNo(1)
                .escrowId("ESC250918001")
                .tradeId("TRD250918004")
                .companyName("Atirath Holdings")
                .buyerName("Sairam V. (You)")
                .sellerName("Rakesh Mehta")
                .quantity(1000L)
                .pricePerShare(new BigDecimal("242.00"))
                .tradeValue(new BigDecimal("242000.00"))
                .currentStatus("Pending Funding")
                .createdOn("18 Sep 2025")
                .expectedSettlement("22 Sep 2025")
                .build());

        liveEscrowsList.add(EscrowListItemDto.builder()
                .rowNo(2)
                .escrowId("ESC250917003")
                .tradeId("TRD250917012")
                .companyName("Farm Vantara")
                .buyerName("Priya Sharma")
                .sellerName("Amit Reddy")
                .quantity(500L)
                .pricePerShare(new BigDecimal("190.00"))
                .tradeValue(new BigDecimal("95000.00"))
                .currentStatus("Funds in Escrow")
                .createdOn("17 Sep 2025")
                .expectedSettlement("21 Sep 2025")
                .build());

        liveEscrowsList.add(EscrowListItemDto.builder()
                .rowNo(3)
                .escrowId("ESC250916001")
                .tradeId("TRD250916006")
                .companyName("Atirath Holdings")
                .buyerName("Vikram Reddy")
                .sellerName("Sairam V. (You)")
                .quantity(2000L)
                .pricePerShare(new BigDecimal("235.00"))
                .tradeValue(new BigDecimal("470000.00"))
                .currentStatus("Shares Locked")
                .createdOn("16 Sep 2025")
                .expectedSettlement("20 Sep 2025")
                .build());

        liveEscrowsList.add(EscrowListItemDto.builder()
                .rowNo(4)
                .escrowId("ESC250914008")
                .tradeId("TRD250914010")
                .companyName("GreenFuel Energy")
                .buyerName("Anita Desai")
                .sellerName("Karan Malhotra")
                .quantity(800L)
                .pricePerShare(new BigDecimal("310.00"))
                .tradeValue(new BigDecimal("248000.00"))
                .currentStatus("Transfer in Progress")
                .createdOn("14 Sep 2025")
                .expectedSettlement("19 Sep 2025")
                .build());

        liveEscrowsList.add(EscrowListItemDto.builder()
                .rowNo(5)
                .escrowId("ESC250912005")
                .tradeId("TRD250912009")
                .companyName("Atirath Holdings")
                .buyerName("Sairam V. (You)")
                .sellerName("Neha Kapoor")
                .quantity(1500L)
                .pricePerShare(new BigDecimal("240.00"))
                .tradeValue(new BigDecimal("360000.00"))
                .currentStatus("Completed")
                .createdOn("12 Sep 2025")
                .expectedSettlement("15 Sep 2025")
                .build());

        liveEscrowsList.add(EscrowListItemDto.builder()
                .rowNo(6)
                .escrowId("ESC250910004")
                .tradeId("TRD250910007")
                .companyName("Farm Vantara")
                .buyerName("Rakesh Mehta")
                .sellerName("Priya Sharma")
                .quantity(1000L)
                .pricePerShare(new BigDecimal("220.00"))
                .tradeValue(new BigDecimal("220000.00"))
                .currentStatus("Completed")
                .createdOn("10 Sep 2025")
                .expectedSettlement("13 Sep 2025")
                .build());

        liveEscrowsList.add(EscrowListItemDto.builder()
                .rowNo(7)
                .escrowId("ESC250908002")
                .tradeId("TRD250908003")
                .companyName("Atirath Technologies")
                .buyerName("Amit Reddy")
                .sellerName("Sairam V. (You)")
                .quantity(600L)
                .pricePerShare(new BigDecimal("180.00"))
                .tradeValue(new BigDecimal("108000.00"))
                .currentStatus("Cancelled")
                .createdOn("08 Sep 2025")
                .expectedSettlement("-")
                .build());

        liveEscrowsList.add(EscrowListItemDto.builder()
                .rowNo(8)
                .escrowId("ESC250905001")
                .tradeId("TRD250905005")
                .companyName("Tri Energy Battery")
                .buyerName("Karan Malhotra")
                .sellerName("Vikram Reddy")
                .quantity(1200L)
                .pricePerShare(new BigDecimal("205.00"))
                .tradeValue(new BigDecimal("246000.00"))
                .currentStatus("Funds in Escrow")
                .createdOn("05 Sep 2025")
                .expectedSettlement("10 Sep 2025")
                .build());

        // Additional items to make total 24 transactions
        liveEscrowsList.add(EscrowListItemDto.builder().rowNo(9).escrowId("ESC250904001").tradeId("TRD250904002").companyName("Atirath Holdings").buyerName("Sairam V. (You)").sellerName("Rahul Verma").quantity(800L).pricePerShare(new BigDecimal("238.00")).tradeValue(new BigDecimal("190400.00")).currentStatus("Pending Funding").createdOn("04 Sep 2025").expectedSettlement("09 Sep 2025").build());
        liveEscrowsList.add(EscrowListItemDto.builder().rowNo(10).escrowId("ESC250903002").tradeId("TRD250903004").companyName("Farm Vantara").buyerName("Sneha Patel").sellerName("Sairam V. (You)").quantity(1100L).pricePerShare(new BigDecimal("188.00")).tradeValue(new BigDecimal("206800.00")).currentStatus("Pending Funding").createdOn("03 Sep 2025").expectedSettlement("08 Sep 2025").build());
        liveEscrowsList.add(EscrowListItemDto.builder().rowNo(11).escrowId("ESC250902003").tradeId("TRD250902005").companyName("GreenFuel Energy").buyerName("Sairam V. (You)").sellerName("Manoj Tiwari").quantity(700L).pricePerShare(new BigDecimal("305.00")).tradeValue(new BigDecimal("213500.00")).currentStatus("Pending Funding").createdOn("02 Sep 2025").expectedSettlement("07 Sep 2025").build());
        liveEscrowsList.add(EscrowListItemDto.builder().rowNo(12).escrowId("ESC250901004").tradeId("TRD250901006").companyName("Atirath Technologies").buyerName("Pooja Hegde").sellerName("Amit Reddy").quantity(950L).pricePerShare(new BigDecimal("182.00")).tradeValue(new BigDecimal("172900.00")).currentStatus("Pending Funding").createdOn("01 Sep 2025").expectedSettlement("06 Sep 2025").build());
        liveEscrowsList.add(EscrowListItemDto.builder().rowNo(13).escrowId("ESC250830005").tradeId("TRD250830007").companyName("Atirath Holdings").buyerName("Sairam V. (You)").sellerName("Divya Kapoor").quantity(1300L).pricePerShare(new BigDecimal("241.00")).tradeValue(new BigDecimal("313300.00")).currentStatus("Pending Funding").createdOn("30 Aug 2025").expectedSettlement("04 Sep 2025").build());
        liveEscrowsList.add(EscrowListItemDto.builder().rowNo(14).escrowId("ESC250828006").tradeId("TRD250828008").companyName("Tri Energy Battery").buyerName("Rahul Verma").sellerName("Sairam V. (You)").quantity(1400L).pricePerShare(new BigDecimal("204.00")).tradeValue(new BigDecimal("285600.00")).currentStatus("Funds in Escrow").createdOn("28 Aug 2025").expectedSettlement("02 Sep 2025").build());
        liveEscrowsList.add(EscrowListItemDto.builder().rowNo(15).escrowId("ESC250826007").tradeId("TRD250826009").companyName("Farm Vantara").buyerName("Anita Desai").sellerName("Neha Kapoor").quantity(650L).pricePerShare(new BigDecimal("187.00")).tradeValue(new BigDecimal("121550.00")).currentStatus("Funds in Escrow").createdOn("26 Aug 2025").expectedSettlement("31 Aug 2025").build());
        liveEscrowsList.add(EscrowListItemDto.builder().rowNo(16).escrowId("ESC250824008").tradeId("TRD250824010").companyName("Atirath Technologies").buyerName("Sairam V. (You)").sellerName("Karan Malhotra").quantity(750L).pricePerShare(new BigDecimal("185.00")).tradeValue(new BigDecimal("138750.00")).currentStatus("Funds in Escrow").createdOn("24 Aug 2025").expectedSettlement("29 Aug 2025").build());
        liveEscrowsList.add(EscrowListItemDto.builder().rowNo(17).escrowId("ESC250822009").tradeId("TRD250822011").companyName("GreenFuel Energy").buyerName("Vikram Reddy").sellerName("Priya Sharma").quantity(900L).pricePerShare(new BigDecimal("308.00")).tradeValue(new BigDecimal("277200.00")).currentStatus("Shares Locked").createdOn("22 Aug 2025").expectedSettlement("27 Aug 2025").build());
        liveEscrowsList.add(EscrowListItemDto.builder().rowNo(18).escrowId("ESC250820010").tradeId("TRD250820012").companyName("Atirath Holdings").buyerName("Neha Kapoor").sellerName("Sairam V. (You)").quantity(1600L).pricePerShare(new BigDecimal("239.00")).tradeValue(new BigDecimal("382400.00")).currentStatus("Shares Locked").createdOn("20 Aug 2025").expectedSettlement("25 Aug 2025").build());
        liveEscrowsList.add(EscrowListItemDto.builder().rowNo(19).escrowId("ESC250818011").tradeId("TRD250818013").companyName("Tri Energy Battery").buyerName("Sairam V. (You)").sellerName("Amit Reddy").quantity(1150L).pricePerShare(new BigDecimal("206.00")).tradeValue(new BigDecimal("236900.00")).currentStatus("Shares Locked").createdOn("18 Aug 2025").expectedSettlement("23 Aug 2025").build());
        liveEscrowsList.add(EscrowListItemDto.builder().rowNo(20).escrowId("ESC250816012").tradeId("TRD250816014").companyName("Farm Vantara").buyerName("Karan Malhotra").sellerName("Sairam V. (You)").quantity(850L).pricePerShare(new BigDecimal("189.00")).tradeValue(new BigDecimal("160650.00")).currentStatus("Transfer in Progress").createdOn("16 Aug 2025").expectedSettlement("21 Aug 2025").build());
        liveEscrowsList.add(EscrowListItemDto.builder().rowNo(21).escrowId("ESC250814013").tradeId("TRD250814015").companyName("Atirath Technologies").buyerName("Sairam V. (You)").sellerName("Sneha Patel").quantity(500L).pricePerShare(new BigDecimal("184.00")).tradeValue(new BigDecimal("92000.00")).currentStatus("Transfer in Progress").createdOn("14 Aug 2025").expectedSettlement("19 Aug 2025").build());
        liveEscrowsList.add(EscrowListItemDto.builder().rowNo(22).escrowId("ESC250812014").tradeId("TRD250812016").companyName("Atirath Holdings").buyerName("Pooja Hegde").sellerName("Vikram Reddy").quantity(1250L).pricePerShare(new BigDecimal("236.00")).tradeValue(new BigDecimal("295000.00")).currentStatus("Completed").createdOn("12 Aug 2025").expectedSettlement("16 Aug 2025").build());
        liveEscrowsList.add(EscrowListItemDto.builder().rowNo(23).escrowId("ESC250810015").tradeId("TRD250810017").companyName("GreenFuel Energy").buyerName("Sairam V. (You)").sellerName("Rahul Verma").quantity(1000L).pricePerShare(new BigDecimal("302.00")).tradeValue(new BigDecimal("302000.00")).currentStatus("Completed").createdOn("10 Aug 2025").expectedSettlement("14 Aug 2025").build());
        liveEscrowsList.add(EscrowListItemDto.builder().rowNo(24).escrowId("ESC250808016").tradeId("TRD250808018").companyName("Tri Energy Battery").buyerName("Amit Reddy").sellerName("Priya Sharma").quantity(700L).pricePerShare(new BigDecimal("200.00")).tradeValue(new BigDecimal("140000.00")).currentStatus("Cancelled").createdOn("08 Aug 2025").expectedSettlement("-").build());

        // 2. Escrow Detail for ESC250918001 (Image 8)
        EscrowDetailDto detail = EscrowDetailDto.builder()
                .escrowId("ESC250918001")
                .status("Pending Funding")
                .statusDescription("Awaiting buyer to transfer funds to escrow.")
                .createdOn("18 Sep 2025, 10:30 AM")
                .tradeId("TRD250918004")
                .negotiationId("NEG250918001")
                .agreementId("AGR250918001")
                .company(EscrowDetailDto.CompanyInfoDto.builder()
                        .companyName("Atirath Holdings India Limited")
                        .symbol("ATG-H")
                        .logoUrl("/assets/companies/atg-h.png")
                        .build())
                .buyer(EscrowDetailDto.PartyDto.builder()
                        .memberName("Sairam Vempati")
                        .initials("SV")
                        .isCurrentUser(true)
                        .verified(true)
                        .memberId("ATG10027")
                        .email("sairam@atirath.com")
                        .phone("+91 98765 43210")
                        .build())
                .seller(EscrowDetailDto.PartyDto.builder()
                        .memberName("Rakesh Mehta")
                        .initials("RM")
                        .isCurrentUser(false)
                        .verified(true)
                        .memberId("ATG10056")
                        .email("rakesh.mehta@fvt.com")
                        .phone("+91 91234 56789")
                        .build())
                .tradeAndEscrowInfo(EscrowDetailDto.TradeAndEscrowInfoDto.builder()
                        .company("Atirath Holdings India Limited (ATG-H)")
                        .securityType("Equity Shares")
                        .quantity(1000L)
                        .agreedPricePerShare(new BigDecimal("242.00"))
                        .totalTradeValue(new BigDecimal("242000.00"))
                        .escrowAmount("₹ 2,42,000.00 (100%)")
                        .paymentMode("Bank Transfer / UPI (Escrow Account)")
                        .expectedSettlement("22 Sep 2025 (T+3 Days)")
                        .escrowCreatedOn("18 Sep 2025, 10:30 AM")
                        .validTill("25 Sep 2025, 11:59 PM")
                        .remarks("-")
                        .build())
                .escrowAccountDetails(EscrowDetailDto.EscrowAccountDetailsDto.builder()
                        .bankName("HDFC Bank")
                        .accountName("Atirath Escrow Services Pvt Ltd")
                        .accountNumber("5020 9876 5432")
                        .ifscCode("HDFC0001234")
                        .accountType("Current Account")
                        .branch("Gachibowli, Hyderabad")
                        .referenceNo("ESC250918001")
                        .amountToTransfer(new BigDecimal("242000.00"))
                        .instructions("Please transfer the exact amount to the escrow account using the reference number. Funds will be automatically reconciled and updated.")
                        .build())
                .escrowProgress(new ArrayList<>(List.of(
                        EscrowDetailDto.ProgressStepDto.builder()
                                .stepNumber(1)
                                .title("Fund Escrow")
                                .subtext("Awaiting buyer to transfer funds")
                                .status("Pending")
                                .completed(false)
                                .active(true)
                                .build(),
                        EscrowDetailDto.ProgressStepDto.builder()
                                .stepNumber(2)
                                .title("Funds Verification")
                                .subtext("Escrow team will verify the funds")
                                .status("Pending")
                                .completed(false)
                                .active(false)
                                .build(),
                        EscrowDetailDto.ProgressStepDto.builder()
                                .stepNumber(3)
                                .title("Share Lock")
                                .subtext("Seller's shares will be locked")
                                .status("Pending")
                                .completed(false)
                                .active(false)
                                .build(),
                        EscrowDetailDto.ProgressStepDto.builder()
                                .stepNumber(4)
                                .title("Share Transfer")
                                .subtext("Shares will be transferred to buyer")
                                .status("Pending")
                                .completed(false)
                                .active(false)
                                .build(),
                        EscrowDetailDto.ProgressStepDto.builder()
                                .stepNumber(5)
                                .title("Settlement")
                                .subtext("Funds will be released to seller")
                                .status("Pending")
                                .completed(false)
                                .active(false)
                                .build()
                )))
                .documents(List.of(
                        EscrowDetailDto.EscrowDocumentDto.builder()
                                .title("Trade Agreement (AGR250918001)")
                                .fileSize("256 KB")
                                .date("18 Sep 2025")
                                .format("PDF")
                                .downloadUrl("/api/v1/escrow/ESC250918001/documents/trade-agreement")
                                .build(),
                        EscrowDetailDto.EscrowDocumentDto.builder()
                                .title("Escrow Terms & Conditions")
                                .fileSize("142 KB")
                                .date("18 Sep 2025")
                                .format("PDF")
                                .downloadUrl("/api/v1/escrow/ESC250918001/documents/terms")
                                .build(),
                        EscrowDetailDto.EscrowDocumentDto.builder()
                                .title("Company Shareholding Proof")
                                .fileSize("320 KB")
                                .date("18 Sep 2025")
                                .format("PDF")
                                .downloadUrl("/api/v1/escrow/ESC250918001/documents/shareholding-proof")
                                .build()
                ))
                .recentActivity(new ArrayList<>(List.of(
                        EscrowDetailDto.ActivityEventDto.builder()
                                .title("Escrow Created")
                                .timestamp("18 Sep 2025, 10:30 AM")
                                .description("Escrow transaction has been created.")
                                .dotColor("green")
                                .build(),
                        EscrowDetailDto.ActivityEventDto.builder()
                                .title("Agreement Reached")
                                .timestamp("18 Sep 2025, 10:15 AM")
                                .description("Both parties agreed to the trade terms.")
                                .dotColor("green")
                                .build(),
                        EscrowDetailDto.ActivityEventDto.builder()
                                .title("Negotiation Completed")
                                .timestamp("18 Sep 2025, 09:45 AM")
                                .description("Final offer accepted by both parties.")
                                .dotColor("blue")
                                .build()
                )))
                .actions(EscrowDetailDto.EscrowActionsDto.builder()
                        .canTransferFunds(true)
                        .canViewAgreement(true)
                        .canContactSupport(true)
                        .build())
                .build();

        liveEscrowDetails.put("ESC250918001", detail);
        initStatusAndLocksData();
    }

    private void initStatusAndLocksData() {
        liveEscrowStatusList.clear();
        liveShareLocksList.clear();
        liveShareTransfersList.clear();

        // --- Image 15: Escrow Status List (12 items) ---
        EscrowStatusItemDto.EscrowStatusItemDetailDto escDetail001 = EscrowStatusItemDto.EscrowStatusItemDetailDto.builder()
                .escrowId("ESC250918001")
                .tradeId("TRD250918004")
                .companyName("Atirath Holdings India Limited")
                .type("Buy")
                .quantity(1000L)
                .pricePerShare(new BigDecimal("242.00"))
                .totalAmount(new BigDecimal("242000.00"))
                .createdOn("18 Sep 2025, 10:40 AM")
                .lastUpdated("18 Sep 2025, 11:28 AM")
                .status("Completed")
                .progress(List.of(
                        EscrowStatusItemDto.ProgressEventDto.builder().title("Escrow Created").timestamp("18 Sep 2025, 10:40 AM").completed(true).build(),
                        EscrowStatusItemDto.ProgressEventDto.builder().title("Payment Completed").timestamp("18 Sep 2025, 10:42 AM").completed(true).build(),
                        EscrowStatusItemDto.ProgressEventDto.builder().title("Payment Verified").timestamp("18 Sep 2025, 10:43 AM").completed(true).build(),
                        EscrowStatusItemDto.ProgressEventDto.builder().title("Shares Locked").timestamp("18 Sep 2025, 10:51 AM").completed(true).build(),
                        EscrowStatusItemDto.ProgressEventDto.builder().title("Share Transfer Completed").timestamp("18 Sep 2025, 11:05 AM").completed(true).build(),
                        EscrowStatusItemDto.ProgressEventDto.builder().title("Settlement Completed").timestamp("18 Sep 2025, 11:28 AM").completed(true).build()
                ))
                .build();

        liveEscrowStatusList.add(EscrowStatusItemDto.builder()
                .escrowId("ESC250918001").tradeId("TRD250918004").companyName("Atirath Holdings India Limited")
                .type("Buy").quantity(1000L).amount(new BigDecimal("242000.00")).status("Completed").updatedOn("18 Sep 2025 11:28 AM")
                .detail(escDetail001).build());

        liveEscrowStatusList.add(EscrowStatusItemDto.builder()
                .escrowId("ESC250917002").tradeId("TRD250917011").companyName("Atirath Holdings India Limited")
                .type("Sell").quantity(500L).amount(new BigDecimal("121000.00")).status("In Progress").updatedOn("17 Sep 2025 04:12 PM").build());

        liveEscrowStatusList.add(EscrowStatusItemDto.builder()
                .escrowId("ESC250916003").tradeId("TRD250916009").companyName("GreenTech Foods Private Limited")
                .type("Buy").quantity(2000L).amount(new BigDecimal("480000.00")).status("Pending").updatedOn("16 Sep 2025 02:34 PM").build());

        liveEscrowStatusList.add(EscrowStatusItemDto.builder()
                .escrowId("ESC250915004").tradeId("TRD250915006").companyName("Atirath Holdings India Limited")
                .type("Sell").quantity(750L).amount(new BigDecimal("181500.00")).status("Failed").updatedOn("15 Sep 2025 11:20 AM").build());

        liveEscrowStatusList.add(EscrowStatusItemDto.builder()
                .escrowId("ESC250914005").tradeId("TRD250914003").companyName("Sunrise Textiles Private Limited")
                .type("Buy").quantity(1200L).amount(new BigDecimal("324000.00")).status("Cancelled").updatedOn("14 Sep 2025 05:10 PM").build());

        liveEscrowStatusList.add(EscrowStatusItemDto.builder()
                .escrowId("ESC250913006").tradeId("TRD250913001").companyName("Atirath Holdings India Limited")
                .type("Buy").quantity(600L).amount(new BigDecimal("145200.00")).status("Completed").updatedOn("13 Sep 2025 12:18 PM").build());

        liveEscrowStatusList.add(EscrowStatusItemDto.builder()
                .escrowId("ESC250912007").tradeId("TRD250912008").companyName("Bharat Agro Industries")
                .type("Sell").quantity(900L).amount(new BigDecimal("207000.00")).status("In Progress").updatedOn("12 Sep 2025 03:45 PM").build());

        liveEscrowStatusList.add(EscrowStatusItemDto.builder()
                .escrowId("ESC250911008").tradeId("TRD250911002").companyName("Atirath Holdings India Limited")
                .type("Buy").quantity(400L).amount(new BigDecimal("96800.00")).status("Completed").updatedOn("11 Sep 2025 01:22 PM").build());

        liveEscrowStatusList.add(EscrowStatusItemDto.builder()
                .escrowId("ESC250910009").tradeId("TRD250910005").companyName("GreenTech Foods Private Limited")
                .type("Sell").quantity(1500L).amount(new BigDecimal("360000.00")).status("Completed").updatedOn("10 Sep 2025 10:15 AM").build());

        liveEscrowStatusList.add(EscrowStatusItemDto.builder()
                .escrowId("ESC250909010").tradeId("TRD250909007").companyName("Oceanic Beverages Private Limited")
                .type("Buy").quantity(800L).amount(new BigDecimal("192000.00")).status("In Progress").updatedOn("09 Sep 2025 02:50 PM").build());

        liveEscrowStatusList.add(EscrowStatusItemDto.builder()
                .escrowId("ESC250908011").tradeId("TRD250908003").companyName("Metro Health Private Limited")
                .type("Buy").quantity(300L).amount(new BigDecimal("72000.00")).status("Pending").updatedOn("08 Sep 2025 11:40 AM").build());

        liveEscrowStatusList.add(EscrowStatusItemDto.builder()
                .escrowId("ESC250907012").tradeId("TRD250907001").companyName("EcoBuild Materials Private Limited")
                .type("Sell").quantity(1000L).amount(new BigDecimal("240000.00")).status("Completed").updatedOn("07 Sep 2025 04:30 PM").build());

        // --- Image 17: Share Lock List (8 items) ---
        ShareLockItemDto.ShareLockDetailDto lockDetail001 = ShareLockItemDto.ShareLockDetailDto.builder()
                .lockId("LOCK250918001")
                .tradeId("TRD250918004")
                .companyName("Atirath Holdings India Limited")
                .isin("INE012A01025")
                .quantity(1000L)
                .lockDateTime("18 Sep 2025, 10:51 AM")
                .unlockCondition("On Share Transfer")
                .status("Locked")
                .lockProgress(List.of(
                        ShareLockItemDto.LockProgressStepDto.builder().title("Lock Initiated").timestamp("18 Sep 2025, 10:51 AM").status("COMPLETED").build(),
                        ShareLockItemDto.LockProgressStepDto.builder().title("Locked").timestamp("In Progress").status("IN_PROGRESS").build(),
                        ShareLockItemDto.LockProgressStepDto.builder().title("Transfer Initiated").timestamp("Pending").status("PENDING").build(),
                        ShareLockItemDto.LockProgressStepDto.builder().title("Unlocked").timestamp("Pending").status("PENDING").build()
                ))
                .recentActivity(List.of(
                        ShareLockItemDto.LockActivityDto.builder().timestamp("18 Sep 2025, 10:51 AM").description("Shares locked in seller's demat account").dotColor("green").build(),
                        ShareLockItemDto.LockActivityDto.builder().timestamp("18 Sep 2025, 10:52 AM").description("Lock confirmation received from depository").dotColor("green").build(),
                        ShareLockItemDto.LockActivityDto.builder().timestamp("-").description("Awaiting share transfer from seller to buyer").dotColor("gray").build()
                ))
                .documents(List.of("Lock Confirmation", "Depository Certificate"))
                .build();

        liveShareLocksList.add(ShareLockItemDto.builder()
                .lockId("LOCK250918001").tradeId("TRD250918004").companyName("Atirath Holdings India Limited")
                .isin("INE012A01025").quantity(1000L).lockDate("18 Sep 2025 10:51 AM").unlockDate("Until Transfer").status("Locked")
                .detail(lockDetail001).build());

        liveShareLocksList.add(ShareLockItemDto.builder()
                .lockId("LOCK250917002").tradeId("TRD250917011").companyName("GreenTech Foods Private Limited")
                .isin("INE034B01019").quantity(500L).lockDate("17 Sep 2025 04:10 PM").unlockDate("Until Transfer").status("Locked").build());

        liveShareLocksList.add(ShareLockItemDto.builder()
                .lockId("LOCK250916003").tradeId("TRD250916009").companyName("Sunrise Textiles Private Limited")
                .isin("INE067C01021").quantity(2000L).lockDate("16 Sep 2025 02:32 PM").unlockDate("Until Transfer").status("Locked").build());

        liveShareLocksList.add(ShareLockItemDto.builder()
                .lockId("LOCK250915004").tradeId("TRD250915006").companyName("Bharat Agro Industries")
                .isin("INE078D01018").quantity(750L).lockDate("15 Sep 2025 11:18 AM").unlockDate("18 Sep 2025").status("Released").build());

        liveShareLocksList.add(ShareLockItemDto.builder()
                .lockId("LOCK250914005").tradeId("TRD250914003").companyName("Atirath Holdings India Limited")
                .isin("INE012A01025").quantity(1200L).lockDate("14 Sep 2025 05:08 PM").unlockDate("18 Sep 2025").status("Released").build());

        liveShareLocksList.add(ShareLockItemDto.builder()
                .lockId("LOCK250913006").tradeId("TRD250913001").companyName("Oceanic Beverages Private Limited")
                .isin("INE099E01017").quantity(600L).lockDate("13 Sep 2025 12:05 PM").unlockDate("20 Sep 2025").status("Expiring Soon").build());

        liveShareLocksList.add(ShareLockItemDto.builder()
                .lockId("LOCK250912007").tradeId("TRD250912008").companyName("Atirath Holdings India Limited")
                .isin("INE012A01025").quantity(900L).lockDate("12 Sep 2025 03:40 PM").unlockDate("Until Transfer").status("Locked").build());

        liveShareLocksList.add(ShareLockItemDto.builder()
                .lockId("LOCK250911008").tradeId("TRD250911002").companyName("Metro Health Private Limited")
                .isin("INE102F01014").quantity(400L).lockDate("11 Sep 2025 01:15 PM").unlockDate("18 Sep 2025").status("Released").build());

        // --- Image 18: Share Transfer List (10 items) ---
        ShareTransferItemDto.ShareTransferDetailDto trfDetail001 = ShareTransferItemDto.ShareTransferDetailDto.builder()
                .transferId("TRF250918001")
                .tradeId("TRD250918004")
                .companyName("Atirath Holdings India Limited")
                .fromSeller("Rakesh Mehta (ATG10056)")
                .toBuyer("Sairam Vempati (ATG10027)")
                .isin("INE012A01025")
                .quantity(1000L)
                .transferDateTime("18 Sep 2025, 11:05 AM")
                .settlementRef("SET250918001")
                .status("Completed")
                .transferProgress(List.of(
                        ShareTransferItemDto.TransferProgressStepDto.builder().title("Transfer Initiated").description("Request submitted by seller").timestamp("18 Sep 2025, 10:58 AM").completed(true).build(),
                        ShareTransferItemDto.TransferProgressStepDto.builder().title("CDSL Transfer").description("Shares transferred through CDSL").timestamp("18 Sep 2025, 11:02 AM").completed(true).build(),
                        ShareTransferItemDto.TransferProgressStepDto.builder().title("Buyer Confirmation").description("Shares received in buyer demat").timestamp("18 Sep 2025, 11:04 AM").completed(true).build(),
                        ShareTransferItemDto.TransferProgressStepDto.builder().title("Completed").description("Transfer successfully completed").timestamp("18 Sep 2025, 11:05 AM").completed(true).build()
                ))
                .documents(List.of("Transfer Instruction", "CDSL Acknowledgement", "Transfer Receipt"))
                .build();

        liveShareTransfersList.add(ShareTransferItemDto.builder()
                .transferId("TRF250918001").tradeId("TRD250918004").companyName("Atirath Holdings India Limited")
                .fromSeller("Rakesh Mehta (ATG10056)").toBuyer("Sairam Vempati (ATG10027)").quantity(1000L).transferDate("18 Sep 2025 11:05 AM").status("Completed")
                .detail(trfDetail001).build());

        liveShareTransfersList.add(ShareTransferItemDto.builder()
                .transferId("TRF250917002").tradeId("TRD250917011").companyName("GreenTech Foods Private Limited")
                .fromSeller("Anil Kumar (ATG10089)").toBuyer("Sairam Vempati (ATG10027)").quantity(500L).transferDate("17 Sep 2025 04:15 PM").status("In Progress").build());

        liveShareTransfersList.add(ShareTransferItemDto.builder()
                .transferId("TRF250916003").tradeId("TRD250916009").companyName("Sunrise Textiles Private Limited")
                .fromSeller("Priya Sharma (ATG10072)").toBuyer("Sairam Vempati (ATG10027)").quantity(2000L).transferDate("16 Sep 2025 02:40 PM").status("Pending").build());

        liveShareTransfersList.add(ShareTransferItemDto.builder()
                .transferId("TRF250915004").tradeId("TRD250915006").companyName("Bharat Agro Industries")
                .fromSeller("Vikram Reddy (ATG10091)").toBuyer("Sairam Vempati (ATG10027)").quantity(750L).transferDate("15 Sep 2025 11:25 AM").status("Completed").build());

        liveShareTransfersList.add(ShareTransferItemDto.builder()
                .transferId("TRF250914005").tradeId("TRD250914003").companyName("Atirath Holdings India Limited")
                .fromSeller("Rakesh Mehta (ATG10056)").toBuyer("Sairam Vempati (ATG10027)").quantity(1200L).transferDate("14 Sep 2025 05:15 PM").status("Completed").build());

        liveShareTransfersList.add(ShareTransferItemDto.builder()
                .transferId("TRF250913006").tradeId("TRD250913001").companyName("Oceanic Beverages Private Limited")
                .fromSeller("Neha Gupta (ATG10077)").toBuyer("Sairam Vempati (ATG10027)").quantity(600L).transferDate("13 Sep 2025 12:10 PM").status("In Progress").build());

        liveShareTransfersList.add(ShareTransferItemDto.builder()
                .transferId("TRF250912007").tradeId("TRD250912008").companyName("Metro Health Private Limited")
                .fromSeller("Sandeep Rao (ATG10063)").toBuyer("Sairam Vempati (ATG10027)").quantity(900L).transferDate("12 Sep 2025 03:45 PM").status("Completed").build());

        liveShareTransfersList.add(ShareTransferItemDto.builder()
                .transferId("TRF250911008").tradeId("TRD250911002").companyName("EcoBuild Materials Private Limited")
                .fromSeller("Karthik Iyer (ATG10088)").toBuyer("Sairam Vempati (ATG10027)").quantity(400L).transferDate("11 Sep 2025 01:20 PM").status("Completed").build());

        liveShareTransfersList.add(ShareTransferItemDto.builder()
                .transferId("TRF250910009").tradeId("TRD250910005").companyName("GreenTech Foods Private Limited")
                .fromSeller("Anil Kumar (ATG10089)").toBuyer("Sairam Vempati (ATG10027)").quantity(1500L).transferDate("10 Sep 2025 10:20 AM").status("Completed").build());

        liveShareTransfersList.add(ShareTransferItemDto.builder()
                .transferId("TRF250909010").tradeId("TRD250909007").companyName("Oceanic Beverages Private Limited")
                .fromSeller("Neha Gupta (ATG10077)").toBuyer("Sairam Vempati (ATG10027)").quantity(800L).transferDate("09 Sep 2025 02:55 PM").status("In Progress").build());
    }

    /**
     * Image 7: Escrow Dashboard
     */
    public EscrowDashboardDto getEscrowDashboard(String tab, String search, int page, int size) {
        List<EscrowListItemDto> filtered = new ArrayList<>(liveEscrowsList);

        // Tab filter
        if (tab != null && !tab.equalsIgnoreCase("ALL") && !tab.isBlank()) {
            String targetStatus = mapTabToStatus(tab);
            filtered = filtered.stream()
                    .filter(item -> item.getCurrentStatus().equalsIgnoreCase(targetStatus) ||
                            (targetStatus.equalsIgnoreCase("Cancelled") && item.getCurrentStatus().equalsIgnoreCase("Cancelled / Failed")))
                    .toList();
        }

        // Search filter
        if (search != null && !search.isBlank()) {
            String q = search.toLowerCase().trim();
            filtered = filtered.stream()
                    .filter(item -> item.getEscrowId().toLowerCase().contains(q)
                            || item.getTradeId().toLowerCase().contains(q)
                            || item.getCompanyName().toLowerCase().contains(q)
                            || item.getBuyerName().toLowerCase().contains(q)
                            || item.getSellerName().toLowerCase().contains(q))
                    .toList();
        }

        // Pagination
        int safePage = Math.max(1, page);
        int safeSize = Math.max(1, size);
        int totalElements = filtered.size();
        int totalPages = (int) Math.ceil((double) totalElements / safeSize);
        int startIndex = (safePage - 1) * safeSize;
        List<EscrowListItemDto> pagedContent;
        if (startIndex >= totalElements) {
            pagedContent = Collections.emptyList();
        } else {
            int endIndex = Math.min(startIndex + safeSize, totalElements);
            pagedContent = filtered.subList(startIndex, endIndex);
        }

        // KPI Summary Metrics (Image 7 Top Cards)
        EscrowSummaryMetricsDto summary = EscrowSummaryMetricsDto.builder()
                .totalTransactions(24)
                .totalTransactionsChange("↑ 20% vs last month")
                .pendingFunding(6)
                .pendingFundingChange("↑ 50% vs last month")
                .fundsInEscrow(new BigDecimal("12450000.00"))
                .fundsInEscrowFormatted("₹ 1,24,50,000")
                .fundsInEscrowChange("↑ 12% vs last month")
                .sharesLocked(45000L)
                .sharesLockedFormatted("45,000")
                .sharesLockedChange("↑ 18% vs last month")
                .completedSettlements(12)
                .completedSettlementsChange("↑ 33% vs last month")
                .failedCancelled(2)
                .failedCancelledChange("↓ 50% vs last month")
                .build();

        // Escrow Transaction Status (Donut Chart)
        EscrowStatusBreakdownDto statusBreakdown = EscrowStatusBreakdownDto.builder()
                .total(24)
                .items(List.of(
                        EscrowStatusBreakdownDto.StatusItemDto.builder()
                                .status("Pending Funding")
                                .count(6)
                                .percentage(25.0)
                                .color("#3B82F6")
                                .build(),
                        EscrowStatusBreakdownDto.StatusItemDto.builder()
                                .status("Funds in Escrow")
                                .count(5)
                                .percentage(21.0)
                                .color("#1D4ED8")
                                .build(),
                        EscrowStatusBreakdownDto.StatusItemDto.builder()
                                .status("Shares Locked")
                                .count(4)
                                .percentage(17.0)
                                .color("#8B5CF6")
                                .build(),
                        EscrowStatusBreakdownDto.StatusItemDto.builder()
                                .status("Transfer In Progress")
                                .count(3)
                                .percentage(13.0)
                                .color("#F59E0B")
                                .build(),
                        EscrowStatusBreakdownDto.StatusItemDto.builder()
                                .status("Completed")
                                .count(4)
                                .percentage(17.0)
                                .color("#10B981")
                                .build(),
                        EscrowStatusBreakdownDto.StatusItemDto.builder()
                                .status("Cancelled / Failed")
                                .count(2)
                                .percentage(8.0)
                                .color("#EF4444")
                                .build()
                ))
                .build();

        // Settlement Trend (Stacked Bar Chart)
        SettlementTrendDto settlementTrend = SettlementTrendDto.builder()
                .months(List.of("Apr", "May", "Jun", "Jul", "Aug", "Sep"))
                .completed(List.of(4, 5, 6, 6, 8, 11))
                .pending(List.of(3, 4, 6, 5, 8, 9))
                .build();

        // Escrow Value Trend (Line Chart)
        EscrowValueTrendDto escrowValueTrend = EscrowValueTrendDto.builder()
                .timeframe("This Month")
                .currentValue(new BigDecimal("12450000.00"))
                .currentValueFormatted("₹ 1,24,50,000")
                .growth("↑ 12% vs last month")
                .dataPoints(List.of(
                        EscrowValueTrendDto.DataPointDto.builder().date("1 Sep").value(new BigDecimal("7500000.00")).formattedValue("₹ 75,00,000").build(),
                        EscrowValueTrendDto.DataPointDto.builder().date("7 Sep").value(new BigDecimal("8800000.00")).formattedValue("₹ 88,00,000").build(),
                        EscrowValueTrendDto.DataPointDto.builder().date("14 Sep").value(new BigDecimal("10200000.00")).formattedValue("₹ 1,02,00,000").build(),
                        EscrowValueTrendDto.DataPointDto.builder().date("21 Sep").value(new BigDecimal("11500000.00")).formattedValue("₹ 1,15,00,000").build(),
                        EscrowValueTrendDto.DataPointDto.builder().date("30 Sep").value(new BigDecimal("12450000.00")).formattedValue("₹ 1,24,50,000").build()
                ))
                .build();

        // Tab counts matching UI
        Map<String, Integer> tabCounts = new LinkedHashMap<>();
        tabCounts.put("All", 24);
        tabCounts.put("Pending Funding", 6);
        tabCounts.put("Funds in Escrow", 5);
        tabCounts.put("Shares Locked", 4);
        tabCounts.put("Transfer in Progress", 3);
        tabCounts.put("Completed", 4);
        tabCounts.put("Cancelled", 2);

        return EscrowDashboardDto.builder()
                .summary(summary)
                .statusBreakdown(statusBreakdown)
                .settlementTrend(settlementTrend)
                .escrowValueTrend(escrowValueTrend)
                .tabCounts(tabCounts)
                .transactions(EscrowDashboardDto.PaginatedEscrowsDto.builder()
                        .totalElements(totalElements)
                        .totalPages(totalPages)
                        .currentPage(safePage)
                        .pageSize(safeSize)
                        .content(pagedContent)
                        .build())
                .build();
    }

    /**
     * Image 8: Escrow Details
     */
    public EscrowDetailDto getEscrowDetail(String escrowId) {
        EscrowDetailDto detail = liveEscrowDetails.get(escrowId);
        if (detail == null) {
            // Find in list to construct dynamic detail
            Optional<EscrowListItemDto> opt = liveEscrowsList.stream()
                    .filter(e -> e.getEscrowId().equalsIgnoreCase(escrowId))
                    .findFirst();

            if (opt.isPresent()) {
                EscrowListItemDto item = opt.get();
                detail = buildGenericEscrowDetail(item);
                liveEscrowDetails.put(escrowId, detail);
            } else {
                throw new ResourceNotFoundException("Escrow transaction not found with ID: " + escrowId);
            }
        }
        return detail;
    }

    /**
     * Image 8 Action: Transfer Funds to Escrow
     */
    public EscrowDetailDto transferFunds(String escrowId, TransferFundsRequestDto request) {
        EscrowDetailDto detail = getEscrowDetail(escrowId);

        // Update progress stepper
        detail.setStatus("Funds in Escrow");
        detail.setStatusDescription("Funds received in escrow. Verification in progress.");

        if (detail.getEscrowProgress() != null && detail.getEscrowProgress().size() >= 2) {
            detail.getEscrowProgress().get(0).setStatus("Completed");
            detail.getEscrowProgress().get(0).setCompleted(true);
            detail.getEscrowProgress().get(0).setActive(false);

            detail.getEscrowProgress().get(1).setStatus("In Progress");
            detail.getEscrowProgress().get(1).setActive(true);
        }

        // Add activity event
        if (detail.getRecentActivity() != null) {
            detail.getRecentActivity().add(0, EscrowDetailDto.ActivityEventDto.builder()
                    .title("Funds Deposited")
                    .timestamp("Just now")
                    .description("Buyer deposited ₹ " + CURRENCY_FMT.format(request.getAmount()) + " via " + request.getPaymentMode() + (request.getTransactionReference() != null ? " (Ref: " + request.getTransactionReference() + ")" : ""))
                    .dotColor("green")
                    .build());
        }

        // Update table item status
        liveEscrowsList.stream()
                .filter(e -> e.getEscrowId().equalsIgnoreCase(escrowId))
                .findFirst()
                .ifPresent(item -> item.setCurrentStatus("Funds in Escrow"));

        return detail;
    }

    /**
     * Image 7 Action: Create Escrow
     */
    public EscrowListItemDto createEscrow(CreateEscrowRequestDto request) {
        String newEscrowId = "ESC" + System.currentTimeMillis() % 1000000000L;
        BigDecimal tradeVal = request.getPricePerShare().multiply(BigDecimal.valueOf(request.getQuantity()));

        EscrowListItemDto item = EscrowListItemDto.builder()
                .rowNo(1)
                .escrowId(newEscrowId)
                .tradeId(request.getTradeId())
                .companyName(request.getCompanyName())
                .buyerName(request.getBuyerMemberId().equalsIgnoreCase("ATG10027") ? "Sairam V. (You)" : request.getBuyerMemberId())
                .sellerName(request.getSellerMemberId())
                .quantity(request.getQuantity())
                .pricePerShare(request.getPricePerShare())
                .tradeValue(tradeVal)
                .currentStatus("Pending Funding")
                .createdOn("Today")
                .expectedSettlement(request.getExpectedSettlement() != null ? request.getExpectedSettlement() : "In 3 Days")
                .build();

        liveEscrowsList.add(0, item);
        for (int i = 0; i < liveEscrowsList.size(); i++) {
            liveEscrowsList.get(i).setRowNo(i + 1);
        }

        return item;
    }

    /**
     * Image 8 Action: Download Escrow Document (Trade Agreement / Terms / Shareholding Proof)
     */
    public byte[] downloadDocument(String escrowId, String docType) {
        EscrowDetailDto detail = getEscrowDetail(escrowId);
        StringBuilder sb = new StringBuilder();
        sb.append("=================================================================\n");
        sb.append("                     ATIRATH GROUP                               \n");
        sb.append("          ESCROW & SETTLEMENT OFFICIAL DOCUMENT                  \n");
        sb.append("=================================================================\n\n");
        sb.append("Document Type:    ").append(docType.toUpperCase().replace("-", " ")).append("\n");
        sb.append("Escrow Reference: ").append(detail.getEscrowId()).append("\n");
        sb.append("Trade Reference:  ").append(detail.getTradeId()).append("\n");
        sb.append("Agreement Ref:    ").append(detail.getAgreementId()).append("\n");
        sb.append("Created On:       ").append(detail.getCreatedOn()).append("\n");
        sb.append("Current Status:   ").append(detail.getStatus()).append("\n\n");

        sb.append("PARTIES INVOLVED:\n");
        sb.append("-----------------------------------------------------------------\n");
        sb.append("Buyer:  ").append(detail.getBuyer().getMemberName()).append(" (ID: ").append(detail.getBuyer().getMemberId()).append(")\n");
        sb.append("Seller: ").append(detail.getSeller().getMemberName()).append(" (ID: ").append(detail.getSeller().getMemberId()).append(")\n\n");

        sb.append("TRADE SPECIFICATIONS:\n");
        sb.append("-----------------------------------------------------------------\n");
        sb.append("Company:             ").append(detail.getTradeAndEscrowInfo().getCompany()).append("\n");
        sb.append("Security Type:       ").append(detail.getTradeAndEscrowInfo().getSecurityType()).append("\n");
        sb.append("Quantity:            ").append(SHARES_FMT.format(detail.getTradeAndEscrowInfo().getQuantity())).append(" shares\n");
        sb.append("Agreed Price/Share:  ₹ ").append(CURRENCY_FMT.format(detail.getTradeAndEscrowInfo().getAgreedPricePerShare())).append("\n");
        sb.append("Total Trade Value:   ₹ ").append(CURRENCY_FMT.format(detail.getTradeAndEscrowInfo().getTotalTradeValue())).append("\n");
        sb.append("Payment Mode:        ").append(detail.getTradeAndEscrowInfo().getPaymentMode()).append("\n");
        sb.append("Expected Settlement: ").append(detail.getTradeAndEscrowInfo().getExpectedSettlement()).append("\n\n");

        sb.append("ESCROW ACCOUNT DETAILS:\n");
        sb.append("-----------------------------------------------------------------\n");
        sb.append("Bank:                ").append(detail.getEscrowAccountDetails().getBankName()).append("\n");
        sb.append("Account Name:        ").append(detail.getEscrowAccountDetails().getAccountName()).append("\n");
        sb.append("Account Number:      ").append(detail.getEscrowAccountDetails().getAccountNumber()).append("\n");
        sb.append("IFSC Code:           ").append(detail.getEscrowAccountDetails().getIfscCode()).append("\n");
        sb.append("Branch:              ").append(detail.getEscrowAccountDetails().getBranch()).append("\n\n");

        sb.append("Digital Certificate Hash: ATG-ESC-VERIFIED-").append(UUID.randomUUID().toString().toUpperCase()).append("\n");
        sb.append("=================================================================\n");

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String mapTabToStatus(String tab) {
        return switch (tab.toUpperCase().replace(" ", "_").replace("-", "_")) {
            case "PENDING_FUNDING" -> "Pending Funding";
            case "FUNDS_IN_ESCROW" -> "Funds in Escrow";
            case "SHARES_LOCKED" -> "Shares Locked";
            case "TRANSFER_IN_PROGRESS" -> "Transfer in Progress";
            case "COMPLETED" -> "Completed";
            case "CANCELLED", "CANCELLED_FAILED", "FAILED" -> "Cancelled";
            default -> tab;
        };
    }

    private EscrowDetailDto buildGenericEscrowDetail(EscrowListItemDto item) {
        return EscrowDetailDto.builder()
                .escrowId(item.getEscrowId())
                .status(item.getCurrentStatus())
                .statusDescription("Status: " + item.getCurrentStatus())
                .createdOn(item.getCreatedOn() + ", 10:00 AM")
                .tradeId(item.getTradeId())
                .negotiationId("NEG" + item.getEscrowId().substring(3))
                .agreementId("AGR" + item.getEscrowId().substring(3))
                .company(EscrowDetailDto.CompanyInfoDto.builder()
                        .companyName(item.getCompanyName())
                        .symbol("ATG")
                        .logoUrl("/assets/companies/atg.png")
                        .build())
                .buyer(EscrowDetailDto.PartyDto.builder()
                        .memberName(item.getBuyerName())
                        .initials("SV")
                        .isCurrentUser(item.getBuyerName().contains("You"))
                        .verified(true)
                        .memberId("ATG10027")
                        .email("member@atirath.com")
                        .phone("+91 98765 43210")
                        .build())
                .seller(EscrowDetailDto.PartyDto.builder()
                        .memberName(item.getSellerName())
                        .initials("RM")
                        .isCurrentUser(item.getSellerName().contains("You"))
                        .verified(true)
                        .memberId("ATG10056")
                        .email("counterparty@atirath.com")
                        .phone("+91 91234 56789")
                        .build())
                .tradeAndEscrowInfo(EscrowDetailDto.TradeAndEscrowInfoDto.builder()
                        .company(item.getCompanyName())
                        .securityType("Equity Shares")
                        .quantity(item.getQuantity())
                        .agreedPricePerShare(item.getPricePerShare())
                        .totalTradeValue(item.getTradeValue())
                        .escrowAmount("₹ " + CURRENCY_FMT.format(item.getTradeValue()) + " (100%)")
                        .paymentMode("Bank Transfer / UPI (Escrow Account)")
                        .expectedSettlement(item.getExpectedSettlement())
                        .escrowCreatedOn(item.getCreatedOn() + ", 10:00 AM")
                        .validTill("In 7 Days")
                        .remarks("-")
                        .build())
                .escrowAccountDetails(EscrowDetailDto.EscrowAccountDetailsDto.builder()
                        .bankName("HDFC Bank")
                        .accountName("Atirath Escrow Services Pvt Ltd")
                        .accountNumber("5020 9876 5432")
                        .ifscCode("HDFC0001234")
                        .accountType("Current Account")
                        .branch("Gachibowli, Hyderabad")
                        .referenceNo(item.getEscrowId())
                        .amountToTransfer(item.getTradeValue())
                        .instructions("Please transfer the exact amount using reference number " + item.getEscrowId())
                        .build())
                .escrowProgress(List.of(
                        EscrowDetailDto.ProgressStepDto.builder().stepNumber(1).title("Fund Escrow").subtext("Fund escrow account").status(item.getCurrentStatus().equals("Pending Funding") ? "Pending" : "Completed").completed(!item.getCurrentStatus().equals("Pending Funding")).active(item.getCurrentStatus().equals("Pending Funding")).build(),
                        EscrowDetailDto.ProgressStepDto.builder().stepNumber(2).title("Funds Verification").subtext("Verify funds").status("Pending").completed(false).active(false).build(),
                        EscrowDetailDto.ProgressStepDto.builder().stepNumber(3).title("Share Lock").subtext("Lock shares").status("Pending").completed(false).active(false).build(),
                        EscrowDetailDto.ProgressStepDto.builder().stepNumber(4).title("Share Transfer").subtext("Transfer shares").status("Pending").completed(false).active(false).build(),
                        EscrowDetailDto.ProgressStepDto.builder().stepNumber(5).title("Settlement").subtext("Release funds").status("Pending").completed(false).active(false).build()
                ))
                .documents(List.of(
                        EscrowDetailDto.EscrowDocumentDto.builder().title("Trade Agreement").fileSize("256 KB").date(item.getCreatedOn()).format("PDF").downloadUrl("/api/v1/escrow/" + item.getEscrowId() + "/documents/trade-agreement").build()
                ))
                .recentActivity(List.of(
                        EscrowDetailDto.ActivityEventDto.builder().title("Escrow Created").timestamp(item.getCreatedOn()).description("Created").dotColor("green").build()
                ))
                .actions(EscrowDetailDto.EscrowActionsDto.builder()
                        .canTransferFunds(item.getCurrentStatus().equalsIgnoreCase("Pending Funding"))
                        .canViewAgreement(true)
                        .canContactSupport(true)
                        .build())
                .build();
    }

    /**
     * Escrow Pipeline / Wizard for Images 9 to 13
     * Stages: FUND_ESCROW, PAYMENT_VERIFICATION, SHARE_LOCK, SHARE_TRANSFER, COMPLETED
     */
    public EscrowPipelineDto getEscrowPipeline(String escrowId, String stageOverride) {
        EscrowDetailDto detail = getEscrowDetail(escrowId);
        String stage = (stageOverride != null && !stageOverride.isBlank())
                ? stageOverride.toUpperCase()
                : liveEscrowCurrentStages.getOrDefault(escrowId, "FUND_ESCROW");

        // Top 5-step stepper with dynamic status based on current stage
        List<EscrowPipelineDto.TopStepDto> stepper = buildStepperForStage(stage);

        // Stage summary badge
        String statusBadge = switch (stage) {
            case "PAYMENT_VERIFICATION" -> "Verifying Payment";
            case "SHARE_LOCK" -> "Funds Secured";
            case "SHARE_TRANSFER" -> "Transfer in Progress";
            case "SETTLEMENT_IN_PROGRESS", "SETTLEMENT" -> "Settlement in Progress";
            case "COMPLETED" -> "Completed";
            default -> "Pending Funding";
        };

        String statusBadgeColor = switch (stage) {
            case "PAYMENT_VERIFICATION" -> "#F59E0B";
            case "SHARE_LOCK", "SHARE_TRANSFER", "SETTLEMENT_IN_PROGRESS", "SETTLEMENT", "COMPLETED" -> "#10B981";
            default -> "#F59E0B";
        };

        EscrowPipelineDto.EscrowPipelineSummaryDto summary = EscrowPipelineDto.EscrowPipelineSummaryDto.builder()
                .escrowId(detail.getEscrowId())
                .statusBadge(statusBadge)
                .statusBadgeColor(statusBadgeColor)
                .tradeId(detail.getTradeId())
                .negotiationId(detail.getNegotiationId())
                .companyName(detail.getCompany().getCompanyName())
                .companySymbol(detail.getCompany().getSymbol())
                .buyerName(detail.getBuyer().getMemberName() + " (" + detail.getBuyer().getMemberId() + ")")
                .buyerMemberId(detail.getBuyer().getMemberId())
                .sellerName(detail.getSeller().getMemberName() + " (" + detail.getSeller().getMemberId() + ")")
                .sellerMemberId(detail.getSeller().getMemberId())
                .quantity(detail.getTradeAndEscrowInfo().getQuantity())
                .pricePerShare(detail.getTradeAndEscrowInfo().getAgreedPricePerShare())
                .totalEscrowAmount(detail.getTradeAndEscrowInfo().getTotalTradeValue())
                .totalEscrowAmountFormatted("₹ " + CURRENCY_FMT.format(detail.getTradeAndEscrowInfo().getTotalTradeValue()))
                .build();

        // 1. Stage 1: Fund Escrow (Image 9)
        EscrowPipelineDto.FundEscrowStageDto fundEscrow = EscrowPipelineDto.FundEscrowStageDto.builder()
                .selectedMethod("UPI")
                .amount(detail.getTradeAndEscrowInfo().getTotalTradeValue())
                .paymentMethods(List.of(
                        EscrowPipelineDto.PaymentMethodOptionDto.builder()
                                .id("UPI")
                                .title("UPI")
                                .description("Pay using any UPI app (Google Pay, PhonePe, Paytm, etc.)")
                                .extraNote("You will be redirected to a secure UPI payment page.")
                                .recommended(true)
                                .build(),
                        EscrowPipelineDto.PaymentMethodOptionDto.builder()
                                .id("NET_BANKING")
                                .title("Net Banking")
                                .description("Pay using your bank account")
                                .extraNote("Supports all major banks (HDFC, ICICI, SBI, Axis, etc.).")
                                .recommended(false)
                                .build(),
                        EscrowPipelineDto.PaymentMethodOptionDto.builder()
                                .id("DEBIT_CARD")
                                .title("Debit Card")
                                .description("Pay using your debit card")
                                .extraNote("Supported cards: Visa, Mastercard, RuPay (Debit Cards only).")
                                .recommended(false)
                                .build()
                ))
                .whyEscrowPoints(List.of(
                        "Your payment will be held securely in escrow until the shares are transferred to your account. This ensures a safe and transparent transaction for both buyer and seller."
                ))
                .importantInformation(List.of(
                        "Use only your own bank account / payment method.",
                        "Payments are processed through our approved payment gateway.",
                        "You will receive a confirmation once the payment is successful.",
                        "In case of payment failure, no amount will be deducted from your account.",
                        "For any issues, contact our support team."
                ))
                .build();

        // 2. Stage 2: Payment Verification (Image 10)
        EscrowPipelineDto.PaymentVerificationStageDto paymentVerification = EscrowPipelineDto.PaymentVerificationStageDto.builder()
                .paymentId("PAY250918001")
                .paymentMethod("UPI (Google Pay)")
                .amount(detail.getTradeAndEscrowInfo().getTotalTradeValue())
                .initiatedOn("18 Sep 2025, 10:42 AM")
                .gatewayReference("GPay/UPI/1837629192")
                .status("Verification in Progress")
                .microProgress(List.of(
                        EscrowPipelineDto.MicroStepDto.builder().title("Payment Initiated").timestamp("18 Sep 2025, 10:42 AM").status("COMPLETED").build(),
                        EscrowPipelineDto.MicroStepDto.builder().title("Verifying with Payment Gateway").timestamp("In Progress...").status("IN_PROGRESS").build(),
                        EscrowPipelineDto.MicroStepDto.builder().title("Updating Escrow").timestamp("Pending").status("PENDING").build(),
                        EscrowPipelineDto.MicroStepDto.builder().title("Confirmation").timestamp("Pending").status("PENDING").build()
                ))
                .notes(List.of(
                        "We are securely verifying your payment with the payment gateway.",
                        "Do not close or refresh this page while verification is in progress.",
                        "You will be notified automatically once the payment is confirmed.",
                        "If the status does not update within a few minutes, you can check the status or contact support."
                ))
                .build();

        // 3. Stage 3: Share Lock (Image 11)
        EscrowPipelineDto.ShareLockStageDto shareLock = EscrowPipelineDto.ShareLockStageDto.builder()
                .paymentId("PAY250918001")
                .paymentConfirmedAmount(detail.getTradeAndEscrowInfo().getTotalTradeValue())
                .sellerShareholding(EscrowPipelineDto.SellerShareholdingDto.builder()
                        .dematAccount("120856300482 (NSDL)")
                        .availableShares(5250L)
                        .sharesToLock(1000L)
                        .postLockBalance(4250L)
                        .lockType("Pledge / Transfer Lock")
                        .expectedLockTime("1-2 minutes")
                        .build())
                .microProgress(List.of(
                        EscrowPipelineDto.MicroStepDto.builder().title("Escrow Funded").timestamp("18 Sep 2025, 10:45 AM").status("COMPLETED").build(),
                        EscrowPipelineDto.MicroStepDto.builder().title("Initiating Share Lock").timestamp("In Progress...").status("IN_PROGRESS").build(),
                        EscrowPipelineDto.MicroStepDto.builder().title("Verifying Holdings").timestamp("Pending").status("PENDING").build(),
                        EscrowPipelineDto.MicroStepDto.builder().title("Shares Locked").timestamp("Pending").status("PENDING").build()
                ))
                .whatHappensNext(List.of(
                        "We will verify the seller's shareholding with the depository.",
                        "The required quantity of shares will be locked and marked for transfer.",
                        "You will be notified once the shares are successfully locked.",
                        "If there is any issue (insufficient holdings, frozen shares, etc.), we will notify both parties immediately."
                ))
                .build();

        // 4. Stage 4: Share Transfer (Image 12)
        EscrowPipelineDto.ShareTransferStageDto shareTransfer = EscrowPipelineDto.ShareTransferStageDto.builder()
                .lockReference("LOCK250918001")
                .lockedShares(1000L)
                .companyName(detail.getCompany().getCompanyName())
                .fromSeller(EscrowPipelineDto.DematPartyDto.builder()
                        .memberName(detail.getSeller().getMemberName())
                        .memberId(detail.getSeller().getMemberId())
                        .dematNumber("120856300482")
                        .depository("NSDL")
                        .build())
                .toBuyer(EscrowPipelineDto.DematPartyDto.builder()
                        .memberName(detail.getBuyer().getMemberName())
                        .memberId(detail.getBuyer().getMemberId())
                        .dematNumber("120987654321")
                        .depository("NSDL")
                        .build())
                .transferDetails(EscrowPipelineDto.DepositoryTransferDetailsDto.builder()
                        .transferId("TRANS250918001")
                        .initiatedOn("18 Sep 2025, 10:52 AM")
                        .fromDemat("120856300482 (NSDL)")
                        .toDemat("120987654321 (NSDL)")
                        .quantity(1000L)
                        .transferType("Off-Market (Internal Trade)")
                        .status("In Progress")
                        .build())
                .microProgress(List.of(
                        EscrowPipelineDto.MicroStepDto.builder().title("Escrow Funded").timestamp("18 Sep 2025, 10:42 AM").status("COMPLETED").build(),
                        EscrowPipelineDto.MicroStepDto.builder().title("Shares Locked").timestamp("18 Sep 2025, 10:51 AM").status("COMPLETED").build(),
                        EscrowPipelineDto.MicroStepDto.builder().title("Share Transfer").timestamp("In Progress...").status("IN_PROGRESS").build(),
                        EscrowPipelineDto.MicroStepDto.builder().title("Transfer Completed").timestamp("Pending").status("PENDING").build()
                ))
                .whatHappensNext(List.of(
                        "The transfer request is being processed with NSDL/CDSL.",
                        "You will be notified once the shares are successfully transferred to the buyer's account.",
                        "After successful transfer, we will proceed to settlement and release funds to the seller.",
                        "You can track the live status in the Transfer Status section."
                ))
                .build();

        // 5a. Stage 5a: Settlement In Progress (Image 14)
        EscrowPipelineDto.SettlementInProgressStageDto settlementInProgress = EscrowPipelineDto.SettlementInProgressStageDto.builder()
                .estimatedSettlementTime("10-30 minutes")
                .overallProgress(List.of(
                        EscrowPipelineDto.MicroStepDto.builder().title("Payment Completed").timestamp("18 Sep 2025, 10:42 AM").status("COMPLETED").build(),
                        EscrowPipelineDto.MicroStepDto.builder().title("Payment Verified").timestamp("18 Sep 2025, 10:43 AM").status("COMPLETED").build(),
                        EscrowPipelineDto.MicroStepDto.builder().title("Shares Locked").timestamp("18 Sep 2025, 10:51 AM").status("COMPLETED").build(),
                        EscrowPipelineDto.MicroStepDto.builder().title("Share Transfer Completed").timestamp("18 Sep 2025, 11:05 AM").status("COMPLETED").build(),
                        EscrowPipelineDto.MicroStepDto.builder().title("Settlement").timestamp("In Progress").status("IN_PROGRESS").build()
                ))
                .processingChecklist(List.of(
                        EscrowPipelineDto.MicroStepDto.builder().title("Verifying share transfer completion").timestamp("11:05 AM").status("COMPLETED").build(),
                        EscrowPipelineDto.MicroStepDto.builder().title("Calculating settlement amount and charges").timestamp("11:06 AM").status("COMPLETED").build(),
                        EscrowPipelineDto.MicroStepDto.builder().title("Initiating fund transfer to seller").timestamp("In Progress").status("IN_PROGRESS").build(),
                        EscrowPipelineDto.MicroStepDto.builder().title("Confirming payout with payment partner").timestamp("Pending").status("PENDING").build(),
                        EscrowPipelineDto.MicroStepDto.builder().title("Updating settlement records").timestamp("Pending").status("PENDING").build()
                ))
                .pleaseNote(List.of(
                        "Funds will be transferred to the seller's registered bank account.",
                        "Settlement time may vary based on bank processing times.",
                        "You will be notified once the settlement is completed.",
                        "In case of any delay, our team will inform you immediately."
                ))
                .settlementDetails(EscrowPipelineDto.SettlementDetailsDto.builder()
                        .settlementId("SET250918001")
                        .expectedPayoutAmount(detail.getTradeAndEscrowInfo().getTotalTradeValue())
                        .payoutBankAccount("HDFC Bank •••• 3045 (Rakesh Mehta)")
                        .initiatedOn("18 Sep 2025, 11:06 AM")
                        .status("Processing")
                        .estimatedCompletion("Within 10-30 minutes")
                        .build())
                .build();

        // 5b. Stage 5b: Settlement Completed (Image 13)
        EscrowPipelineDto.SettlementCompletedStageDto settlementCompleted = EscrowPipelineDto.SettlementCompletedStageDto.builder()
                .settlementId("SET250918001")
                .settlementDateTime("18 Sep 2025, 11:28 AM")
                .payoutAmount(detail.getTradeAndEscrowInfo().getTotalTradeValue())
                .payoutBankAccount("HDFC Bank •••• 3045 (Rakesh Mehta)")
                .status("Completed")
                .referenceNo("PAYOUT1837291045")
                .sharesTraded(1000L)
                .totalTradeValue(detail.getTradeAndEscrowInfo().getTotalTradeValue())
                .completedOn("18 Sep 2025 (11:28 AM)")
                .whatsNext(List.of(
                        "You can view the complete transaction details in your Portfolio and Trade History.",
                        "A settlement confirmation has been sent to your registered email.",
                        "You can download trade documents for your records.",
                        "Thank you for using Atirath Internal Market."
                ))
                .build();

        return EscrowPipelineDto.builder()
                .escrowId(escrowId)
                .currentStage(stage)
                .topStepper(stepper)
                .summary(summary)
                .fundEscrow(fundEscrow)
                .paymentVerification(paymentVerification)
                .shareLock(shareLock)
                .shareTransfer(shareTransfer)
                .settlementInProgress(settlementInProgress)
                .settlementCompleted(settlementCompleted)
                .build();
    }

    public EscrowPipelineDto proceedPayment(String escrowId, ProceedPaymentRequestDto request) {
        liveEscrowCurrentStages.put(escrowId, "PAYMENT_VERIFICATION");
        updateEscrowStatus(escrowId, "Pending Funding");
        return getEscrowPipeline(escrowId, "PAYMENT_VERIFICATION");
    }

    public EscrowPipelineDto verifyPayment(String escrowId) {
        liveEscrowCurrentStages.put(escrowId, "SHARE_LOCK");
        updateEscrowStatus(escrowId, "Funds in Escrow");
        return getEscrowPipeline(escrowId, "SHARE_LOCK");
    }

    public EscrowPipelineDto lockShares(String escrowId) {
        liveEscrowCurrentStages.put(escrowId, "SHARE_TRANSFER");
        updateEscrowStatus(escrowId, "Shares Locked");
        return getEscrowPipeline(escrowId, "SHARE_TRANSFER");
    }

    public EscrowPipelineDto transferShares(String escrowId) {
        liveEscrowCurrentStages.put(escrowId, "COMPLETED");
        updateEscrowStatus(escrowId, "Completed");
        return getEscrowPipeline(escrowId, "COMPLETED");
    }

    public EscrowPipelineDto setPipelineStage(String escrowId, String stage) {
        liveEscrowCurrentStages.put(escrowId, stage.toUpperCase());
        return getEscrowPipeline(escrowId, stage.toUpperCase());
    }

    private void updateEscrowStatus(String escrowId, String status) {
        liveEscrowsList.stream()
                .filter(e -> e.getEscrowId().equalsIgnoreCase(escrowId))
                .findFirst()
                .ifPresent(e -> e.setCurrentStatus(status));

        EscrowDetailDto detail = liveEscrowDetails.get(escrowId);
        if (detail != null) {
            detail.setStatus(status);
        }
    }

    private List<EscrowPipelineDto.TopStepDto> buildStepperForStage(String stage) {
        boolean isSettlementProgress = stage.equals("SETTLEMENT_IN_PROGRESS") || stage.equals("SETTLEMENT");
        boolean isCompleted = stage.equals("COMPLETED");

        return List.of(
                EscrowPipelineDto.TopStepDto.builder()
                        .stepNumber(1)
                        .title("Fund Escrow")
                        .subtext(stage.equals("FUND_ESCROW") ? "Make payment" : "Payment Completed")
                        .status(stage.equals("FUND_ESCROW") ? "ACTIVE" : "COMPLETED")
                        .build(),
                EscrowPipelineDto.TopStepDto.builder()
                        .stepNumber(2)
                        .title("Payment Verification")
                        .subtext(stage.equals("FUND_ESCROW") ? "Gateway confirmation" : (stage.equals("PAYMENT_VERIFICATION") ? "Gateway confirmation" : "Payment Confirmed"))
                        .status(stage.equals("FUND_ESCROW") ? "PENDING" : (stage.equals("PAYMENT_VERIFICATION") ? "ACTIVE" : "COMPLETED"))
                        .build(),
                EscrowPipelineDto.TopStepDto.builder()
                        .stepNumber(3)
                        .title("Share Lock")
                        .subtext(stage.equals("FUND_ESCROW") || stage.equals("PAYMENT_VERIFICATION") ? "Shares will be locked" : (stage.equals("SHARE_LOCK") ? "Shares will be locked" : "Shares Locked"))
                        .status(stage.equals("FUND_ESCROW") || stage.equals("PAYMENT_VERIFICATION") ? "PENDING" : (stage.equals("SHARE_LOCK") ? "ACTIVE" : "COMPLETED"))
                        .build(),
                EscrowPipelineDto.TopStepDto.builder()
                        .stepNumber(4)
                        .title("Share Transfer")
                        .subtext(isCompleted || isSettlementProgress ? "Transfer Completed" : "Transfer to buyer")
                        .status(isCompleted || isSettlementProgress ? "COMPLETED" : (stage.equals("SHARE_TRANSFER") ? "ACTIVE" : "PENDING"))
                        .build(),
                EscrowPipelineDto.TopStepDto.builder()
                        .stepNumber(5)
                        .title("Settlement")
                        .subtext(isCompleted ? "Funds Released" : "Funds release to seller")
                        .status(isCompleted ? "COMPLETED" : (isSettlementProgress ? "ACTIVE" : "PENDING"))
                        .build()
        );
    }

    /**
     * Image 15: Escrow Status Page
     */
    public EscrowStatusPageDto getEscrowStatusPage(String tab, String status, String search, int page, int size, String selectedId) {
        List<EscrowStatusItemDto> filtered = new ArrayList<>(liveEscrowStatusList);

        if (tab != null && !tab.equalsIgnoreCase("ALL") && !tab.equalsIgnoreCase("All Escrows") && !tab.isBlank()) {
            if (tab.equalsIgnoreCase("As a Buyer") || tab.equalsIgnoreCase("BUYER")) {
                filtered = filtered.stream().filter(e -> "Buy".equalsIgnoreCase(e.getType())).toList();
            } else if (tab.equalsIgnoreCase("As a Seller") || tab.equalsIgnoreCase("SELLER")) {
                filtered = filtered.stream().filter(e -> "Sell".equalsIgnoreCase(e.getType())).toList();
            } else if (tab.equalsIgnoreCase("My Escrows")) {
                filtered = filtered.stream().filter(e -> !e.getEscrowId().equals("ESC250914005") && !e.getEscrowId().equals("ESC250907012") && !e.getEscrowId().equals("ESC250909010") && !e.getEscrowId().equals("ESC250910009")).toList();
            }
        }

        if (status != null && !status.isBlank() && !status.equalsIgnoreCase("ALL")) {
            filtered = filtered.stream().filter(e -> e.getStatus().equalsIgnoreCase(status)).toList();
        }

        if (search != null && !search.isBlank()) {
            String q = search.toLowerCase().trim();
            filtered = filtered.stream()
                    .filter(e -> e.getEscrowId().toLowerCase().contains(q)
                            || e.getTradeId().toLowerCase().contains(q)
                            || e.getCompanyName().toLowerCase().contains(q))
                    .toList();
        }

        int safePage = Math.max(1, page);
        int safeSize = Math.max(1, size);
        int totalElements = filtered.size();
        int totalPages = (int) Math.ceil((double) totalElements / safeSize);
        int startIndex = (safePage - 1) * safeSize;
        List<EscrowStatusItemDto> pagedContent;
        if (startIndex >= totalElements) {
            pagedContent = Collections.emptyList();
        } else {
            int endIndex = Math.min(startIndex + safeSize, totalElements);
            pagedContent = filtered.subList(startIndex, endIndex);
        }

        String targetSelectedId = (selectedId != null && !selectedId.isBlank()) ? selectedId : "ESC250918001";
        EscrowStatusItemDto.EscrowStatusItemDetailDto selectedDetail = liveEscrowStatusList.stream()
                .filter(e -> e.getEscrowId().equalsIgnoreCase(targetSelectedId))
                .findFirst()
                .map(EscrowStatusItemDto::getDetail)
                .orElse(liveEscrowStatusList.get(0).getDetail());

        Map<String, Integer> tabCounts = new LinkedHashMap<>();
        tabCounts.put("All Escrows", 12);
        tabCounts.put("My Escrows", 8);
        tabCounts.put("As a Buyer", 7);
        tabCounts.put("As a Seller", 5);

        return EscrowStatusPageDto.builder()
                .summary(EscrowStatusPageDto.EscrowStatusMetricsDto.builder()
                        .totalEscrows(12)
                        .completed(5).completedPct(41.7)
                        .inProgress(3).inProgressPct(25.0)
                        .pending(2).pendingPct(16.7)
                        .failed(1).failedPct(8.3)
                        .cancelled(1).cancelledPct(8.3)
                        .build())
                .tabCounts(tabCounts)
                .escrows(EscrowStatusPageDto.PaginatedEscrowStatusDto.builder()
                        .totalElements(totalElements)
                        .totalPages(totalPages)
                        .currentPage(safePage)
                        .pageSize(safeSize)
                        .content(pagedContent)
                        .build())
                .selectedEscrow(selectedDetail)
                .build();
    }

    /**
     * Image 16: View Escrow Details
     */
    public EscrowStatusDetailDto getEscrowStatusDetail(String escrowId, String tab) {
        return EscrowStatusDetailDto.builder()
                .escrowId("ESC250918001")
                .tradeId("TRD250918004")
                .companyName("Atirath Holdings India Limited")
                .companySymbol("ATG-H")
                .type("Buy")
                .quantity(1000L)
                .pricePerShare(new BigDecimal("242.00"))
                .totalAmount(new BigDecimal("242000.00"))
                .buyerName("Sairam Vempati")
                .buyerMemberId("ATG10027")
                .sellerName("Rakesh Mehta")
                .sellerMemberId("ATG10056")
                .status("Completed")
                .statusMessage("All escrow steps completed successfully.")
                .createdOn("18 Sep 2025, 10:40 AM")
                .completedOn("18 Sep 2025, 11:28 AM")
                .lastUpdated("18 Sep 2025, 11:28 AM")
                .horizontalSteps(List.of(
                        EscrowStatusDetailDto.HorizontalStepDto.builder().stepNumber(1).title("1. Fund Escrow").status("Completed").timestamp("18 Sep 2025\n10:42 AM").build(),
                        EscrowStatusDetailDto.HorizontalStepDto.builder().stepNumber(2).title("2. Payment Verification").status("Completed").timestamp("18 Sep 2025\n10:43 AM").build(),
                        EscrowStatusDetailDto.HorizontalStepDto.builder().stepNumber(3).title("3. Share Lock").status("Completed").timestamp("18 Sep 2025\n10:51 AM").build(),
                        EscrowStatusDetailDto.HorizontalStepDto.builder().stepNumber(4).title("4. Share Transfer").status("Completed").timestamp("18 Sep 2025\n10:05 AM").build(),
                        EscrowStatusDetailDto.HorizontalStepDto.builder().stepNumber(5).title("5. Settlement").status("Completed").timestamp("18 Sep 2025\n11:28 AM").build()
                ))
                .escrowSummary(EscrowStatusDetailDto.EscrowSummaryCardDto.builder()
                        .escrowId("ESC250918001")
                        .tradeId("TRD250918004")
                        .companyName("Atirath Holdings India Limited")
                        .type("Buy")
                        .quantity(1000L)
                        .pricePerShare(new BigDecimal("242.00"))
                        .totalEscrowAmount(new BigDecimal("242000.00"))
                        .buyer("Sairam Vempati (ATG10027)")
                        .seller("Rakesh Mehta (ATG10056)")
                        .status("Completed")
                        .createdOn("18 Sep 2025, 10:40 AM")
                        .completedOn("18 Sep 2025, 11:28 AM")
                        .build())
                .progressSteps(List.of(
                        EscrowStatusDetailDto.ProgressStepDetailDto.builder().stepNumber(1).title("Fund Escrow").description("Payment completed and held in escrow").timestamp("18 Sep 2025, 10:42 AM").completed(true).viewActionUrl("/api/v1/escrow/pipeline/ESC250918001?stage=FUND_ESCROW").build(),
                        EscrowStatusDetailDto.ProgressStepDetailDto.builder().stepNumber(2).title("Payment Verification").description("Payment confirmed by gateway").timestamp("18 Sep 2025, 10:43 AM").completed(true).viewActionUrl("/api/v1/escrow/pipeline/ESC250918001?stage=PAYMENT_VERIFICATION").build(),
                        EscrowStatusDetailDto.ProgressStepDetailDto.builder().stepNumber(3).title("Share Lock").description("Shares locked in seller's demat account").timestamp("18 Sep 2025, 10:51 AM").completed(true).viewActionUrl("/api/v1/escrow/pipeline/ESC250918001?stage=SHARE_LOCK").build(),
                        EscrowStatusDetailDto.ProgressStepDetailDto.builder().stepNumber(4).title("Share Transfer").description("Shares transferred to buyer's demat account").timestamp("18 Sep 2025, 11:05 AM").completed(true).viewActionUrl("/api/v1/escrow/pipeline/ESC250918001?stage=SHARE_TRANSFER").build(),
                        EscrowStatusDetailDto.ProgressStepDetailDto.builder().stepNumber(5).title("Settlement").description("Funds released to seller").timestamp("18 Sep 2025, 11:28 AM").completed(true).viewActionUrl("/api/v1/escrow/pipeline/ESC250918001?stage=SETTLEMENT").build()
                ))
                .quickActions(List.of(
                        EscrowStatusDetailDto.QuickActionDto.builder().id("download_report").title("Download Escrow Report").actionType("DOWNLOAD").build(),
                        EscrowStatusDetailDto.QuickActionDto.builder().id("view_trade").title("View Trade Details").actionType("LINK").build(),
                        EscrowStatusDetailDto.QuickActionDto.builder().id("view_portfolio").title("View Shares in Portfolio").actionType("LINK").build(),
                        EscrowStatusDetailDto.QuickActionDto.builder().id("raise_ticket").title("Raise a Support Ticket").actionType("SUPPORT").build()
                ))
                .transactionHistory(EscrowStatusDetailDto.TransactionHistoryTabDto.builder()
                        .events(List.of(
                                EscrowStatusDetailDto.HistoryItemDto.builder().event("Settlement Completed").timestamp("18 Sep 2025, 11:28 AM").actor("System").status("Completed").build(),
                                EscrowStatusDetailDto.HistoryItemDto.builder().event("Share Transfer Confirmed").timestamp("18 Sep 2025, 11:05 AM").actor("CDSL / NSDL").status("Completed").build(),
                                EscrowStatusDetailDto.HistoryItemDto.builder().event("Shares Locked in Seller Demat").timestamp("18 Sep 2025, 10:51 AM").actor("Depository").status("Completed").build(),
                                EscrowStatusDetailDto.HistoryItemDto.builder().event("Payment Verified by Gateway").timestamp("18 Sep 2025, 10:43 AM").actor("Payment Gateway").status("Completed").build(),
                                EscrowStatusDetailDto.HistoryItemDto.builder().event("Payment Initiated by Buyer").timestamp("18 Sep 2025, 10:42 AM").actor("Sairam Vempati").status("Completed").build(),
                                EscrowStatusDetailDto.HistoryItemDto.builder().event("Escrow Created").timestamp("18 Sep 2025, 10:40 AM").actor("System").status("Completed").build()
                        ))
                        .build())
                .paymentDetails(EscrowStatusDetailDto.PaymentDetailsTabDto.builder()
                        .paymentId("PAY250918001")
                        .paymentMethod("UPI (Google Pay)")
                        .amount(new BigDecimal("242000.00"))
                        .gatewayRef("GPay/UPI/1837629192")
                        .status("Completed")
                        .paidAt("18 Sep 2025, 10:42 AM")
                        .build())
                .shareDetails(EscrowStatusDetailDto.ShareDetailsTabDto.builder()
                        .lockId("LOCK250918001")
                        .transferId("TRF250918001")
                        .isin("INE012A01025")
                        .shares(1000L)
                        .fromDemat("120856300482 (NSDL)")
                        .toDemat("120987654321 (NSDL)")
                        .build())
                .settlementDetails(EscrowStatusDetailDto.SettlementDetailsTabDto.builder()
                        .settlementId("SET250918001")
                        .payoutAmount(new BigDecimal("242000.00"))
                        .payoutBank("HDFC Bank •••• 3045 (Rakesh Mehta)")
                        .referenceNo("PAYOUT1837291045")
                        .completedAt("18 Sep 2025, 11:28 AM")
                        .build())
                .documents(List.of(
                        EscrowStatusDetailDto.EscrowDocumentTabDto.builder().name("Escrow Agreement").size("2.4 MB").date("18 Sep 2025").downloadUrl("/api/v1/escrow/ESC250918001/documents/agreement").build(),
                        EscrowStatusDetailDto.EscrowDocumentTabDto.builder().name("Payment Confirmation Receipt").size("1.1 MB").date("18 Sep 2025").downloadUrl("/api/v1/escrow/ESC250918001/documents/payment").build(),
                        EscrowStatusDetailDto.EscrowDocumentTabDto.builder().name("Demat Transfer Slip").size("1.8 MB").date("18 Sep 2025").downloadUrl("/api/v1/escrow/ESC250918001/documents/demat").build(),
                        EscrowStatusDetailDto.EscrowDocumentTabDto.builder().name("Settlement Statement").size("950 KB").date("18 Sep 2025").downloadUrl("/api/v1/escrow/ESC250918001/documents/settlement").build()
                ))
                .build();
    }

    /**
     * Image 17: Share Lock Status Page
     */
    public ShareLockStatusPageDto getShareLockStatusPage(String tab, String status, String search, int page, int size, String selectedId) {
        List<ShareLockItemDto> filtered = new ArrayList<>(liveShareLocksList);

        if (tab != null && !tab.equalsIgnoreCase("ALL") && !tab.equalsIgnoreCase("All Locks") && !tab.isBlank()) {
            if (tab.equalsIgnoreCase("As a Buyer") || tab.equalsIgnoreCase("BUYER")) {
                filtered = filtered.stream().filter(l -> l.getLockId().endsWith("001") || l.getLockId().endsWith("003") || l.getLockId().endsWith("005") || l.getLockId().endsWith("007")).toList();
            } else if (tab.equalsIgnoreCase("As a Seller") || tab.equalsIgnoreCase("SELLER")) {
                filtered = filtered.stream().filter(l -> l.getLockId().endsWith("002") || l.getLockId().endsWith("004") || l.getLockId().endsWith("006") || l.getLockId().endsWith("008")).toList();
            } else if (tab.equalsIgnoreCase("My Locks")) {
                filtered = filtered.stream().filter(l -> !l.getLockId().endsWith("004") && !l.getLockId().endsWith("005") && !l.getLockId().endsWith("008")).toList();
            }
        }

        if (status != null && !status.isBlank() && !status.equalsIgnoreCase("ALL")) {
            filtered = filtered.stream().filter(l -> l.getStatus().equalsIgnoreCase(status)).toList();
        }

        if (search != null && !search.isBlank()) {
            String q = search.toLowerCase().trim();
            filtered = filtered.stream()
                    .filter(l -> l.getLockId().toLowerCase().contains(q)
                            || l.getTradeId().toLowerCase().contains(q)
                            || l.getCompanyName().toLowerCase().contains(q)
                            || l.getIsin().toLowerCase().contains(q))
                    .toList();
        }

        int safePage = Math.max(1, page);
        int safeSize = Math.max(1, size);
        int totalElements = filtered.size();
        int totalPages = (int) Math.ceil((double) totalElements / safeSize);
        int startIndex = (safePage - 1) * safeSize;
        List<ShareLockItemDto> pagedContent;
        if (startIndex >= totalElements) {
            pagedContent = Collections.emptyList();
        } else {
            int endIndex = Math.min(startIndex + safeSize, totalElements);
            pagedContent = filtered.subList(startIndex, endIndex);
        }

        String targetSelectedId = (selectedId != null && !selectedId.isBlank()) ? selectedId : "LOCK250918001";
        ShareLockItemDto.ShareLockDetailDto selectedDetail = liveShareLocksList.stream()
                .filter(l -> l.getLockId().equalsIgnoreCase(targetSelectedId))
                .findFirst()
                .map(ShareLockItemDto::getDetail)
                .orElse(liveShareLocksList.get(0).getDetail());

        Map<String, Integer> tabCounts = new LinkedHashMap<>();
        tabCounts.put("All Locks", 8);
        tabCounts.put("My Locks", 5);
        tabCounts.put("As a Buyer", 4);
        tabCounts.put("As a Seller", 4);

        return ShareLockStatusPageDto.builder()
                .summary(ShareLockStatusPageDto.ShareLockMetricsDto.builder()
                        .totalRequests(8)
                        .activeLocks(5).activeLocksPct(62.5)
                        .released(2).releasedPct(25.0)
                        .expiringSoon(1).expiringSoonPct(12.5)
                        .failed(0).failedPct(0.0)
                        .build())
                .tabCounts(tabCounts)
                .locks(ShareLockStatusPageDto.PaginatedShareLockDto.builder()
                        .totalElements(totalElements)
                        .totalPages(totalPages)
                        .currentPage(safePage)
                        .pageSize(safeSize)
                        .content(pagedContent)
                        .build())
                .selectedLock(selectedDetail)
                .build();
    }

    /**
     * Image 18: Share Transfer Status Page
     */
    public ShareTransferStatusPageDto getShareTransferStatusPage(String tab, String status, String search, int page, int size, String selectedId) {
        List<ShareTransferItemDto> filtered = new ArrayList<>(liveShareTransfersList);

        if (tab != null && !tab.equalsIgnoreCase("ALL") && !tab.equalsIgnoreCase("All Transfers") && !tab.isBlank()) {
            if (tab.equalsIgnoreCase("As a Buyer") || tab.equalsIgnoreCase("BUYER")) {
                filtered = filtered.stream().filter(t -> t.getTransferId().endsWith("001") || t.getTransferId().endsWith("002") || t.getTransferId().endsWith("003") || t.getTransferId().endsWith("004") || t.getTransferId().endsWith("005") || t.getTransferId().endsWith("007")).toList();
            } else if (tab.equalsIgnoreCase("As a Seller") || tab.equalsIgnoreCase("SELLER")) {
                filtered = filtered.stream().filter(t -> t.getTransferId().endsWith("006") || t.getTransferId().endsWith("008") || t.getTransferId().endsWith("009") || t.getTransferId().endsWith("010")).toList();
            } else if (tab.equalsIgnoreCase("My Transfers")) {
                filtered = filtered.stream().filter(t -> !t.getTransferId().endsWith("003") && !t.getTransferId().endsWith("008") && !t.getTransferId().endsWith("010")).toList();
            }
        }

        if (status != null && !status.isBlank() && !status.equalsIgnoreCase("ALL")) {
            filtered = filtered.stream().filter(t -> t.getStatus().equalsIgnoreCase(status)).toList();
        }

        if (search != null && !search.isBlank()) {
            String q = search.toLowerCase().trim();
            filtered = filtered.stream()
                    .filter(t -> t.getTransferId().toLowerCase().contains(q)
                            || t.getTradeId().toLowerCase().contains(q)
                            || t.getCompanyName().toLowerCase().contains(q)
                            || t.getFromSeller().toLowerCase().contains(q)
                            || t.getToBuyer().toLowerCase().contains(q))
                    .toList();
        }

        int safePage = Math.max(1, page);
        int safeSize = Math.max(1, size);
        int totalElements = filtered.size();
        int totalPages = (int) Math.ceil((double) totalElements / safeSize);
        int startIndex = (safePage - 1) * safeSize;
        List<ShareTransferItemDto> pagedContent;
        if (startIndex >= totalElements) {
            pagedContent = Collections.emptyList();
        } else {
            int endIndex = Math.min(startIndex + safeSize, totalElements);
            pagedContent = filtered.subList(startIndex, endIndex);
        }

        String targetSelectedId = (selectedId != null && !selectedId.isBlank()) ? selectedId : "TRF250918001";
        ShareTransferItemDto.ShareTransferDetailDto selectedDetail = liveShareTransfersList.stream()
                .filter(t -> t.getTransferId().equalsIgnoreCase(targetSelectedId))
                .findFirst()
                .map(ShareTransferItemDto::getDetail)
                .orElse(liveShareTransfersList.get(0).getDetail());

        Map<String, Integer> tabCounts = new LinkedHashMap<>();
        tabCounts.put("All Transfers", 10);
        tabCounts.put("My Transfers", 7);
        tabCounts.put("As a Buyer", 6);
        tabCounts.put("As a Seller", 4);

        return ShareTransferStatusPageDto.builder()
                .summary(ShareTransferStatusPageDto.ShareTransferMetricsDto.builder()
                        .totalTransfers(10)
                        .completed(6).completedPct(60.0)
                        .inProgress(3).inProgressPct(30.0)
                        .pending(1).pendingPct(10.0)
                        .failed(0).failedPct(0.0)
                        .build())
                .tabCounts(tabCounts)
                .transfers(ShareTransferStatusPageDto.PaginatedShareTransferDto.builder()
                        .totalElements(totalElements)
                        .totalPages(totalPages)
                        .currentPage(safePage)
                        .pageSize(safeSize)
                        .content(pagedContent)
                        .build())
                .selectedTransfer(selectedDetail)
                .build();
    }

    /**
     * Settlement Confirmation (Settlement > Settlement Confirmation Screen)
     */
    public SettlementConfirmationDto getSettlementConfirmation(String settlementId, String escrowId) {
        String effSettlementId = (settlementId != null && !settlementId.isBlank()) ? settlementId : "SET250918001";
        String effEscrowId = (escrowId != null && !escrowId.isBlank()) ? escrowId : "ESC250918001";

        return SettlementConfirmationDto.builder()
                .settlementId(effSettlementId)
                .escrowId(effEscrowId)
                .tradeId("TRD250918004")
                .progressSteps(List.of(
                        SettlementConfirmationDto.SettlementProgressStepDto.builder().stepNumber(1).title("1. Fund Escrow").status("Completed").timestamp("18 Sep 2025, 10:42 AM").build(),
                        SettlementConfirmationDto.SettlementProgressStepDto.builder().stepNumber(2).title("2. Payment Verified").status("Completed").timestamp("18 Sep 2025, 10:43 AM").build(),
                        SettlementConfirmationDto.SettlementProgressStepDto.builder().stepNumber(3).title("3. Share Lock").status("Completed").timestamp("18 Sep 2025, 10:51 AM").build(),
                        SettlementConfirmationDto.SettlementProgressStepDto.builder().stepNumber(4).title("4. Share Transfer").status("Completed").timestamp("18 Sep 2025, 11:05 AM").build(),
                        SettlementConfirmationDto.SettlementProgressStepDto.builder().stepNumber(5).title("5. Settlement").status("Completed").timestamp("18 Sep 2025, 11:28 AM").build()
                ))
                .settlementDetails(SettlementConfirmationDto.SettlementDetailsCardDto.builder()
                        .settlementId(effSettlementId)
                        .escrowId(effEscrowId)
                        .tradeId("TRD250918004")
                        .companyName("Atirath Holdings India Limited")
                        .companySymbol("ATG-H")
                        .fromSeller("Rakesh Mehta (ATG10056)")
                        .toBuyer("Sairam Vempati (ATG10027)")
                        .isin("INE012A01025")
                        .quantity(1000L)
                        .pricePerShare(new BigDecimal("242.00"))
                        .totalAmount(new BigDecimal("242000.00"))
                        .transferDate("18 Sep 2025, 11:05 AM")
                        .settlementDate("18 Sep 2025, 11:28 AM")
                        .settlementStatus("Completed")
                        .settlementReferenceNo("NSDL250918123456")
                        .clearingCorporation("NSDL")
                        .paymentReferenceNo("PAY250918789012")
                        .createdOn("18 Sep 2025, 10:40 AM")
                        .lastUpdated("18 Sep 2025, 11:28 AM")
                        .build())
                .successDetails(SettlementConfirmationDto.SettlementSuccessCardDto.builder()
                        .headline("Settlement Successful!")
                        .subheadline("The shares have been transferred to the buyer and funds have been released to the seller as per the agreed terms.")
                        .tagline("\"Trade Completed. Trust Delivered.\"")
                        .buyerDelivery(SettlementConfirmationDto.PartyDeliverySummaryDto.builder()
                                .label("Buyer (Shares Received)")
                                .memberName("Sairam Vempati")
                                .memberId("ATG10027")
                                .sharesCount(1000L)
                                .sharesFormatted("1,000 Shares")
                                .statusMessage("Shares credited to demat account")
                                .timestamp("18 Sep 2025, 11:28 AM")
                                .build())
                        .sellerPayment(SettlementConfirmationDto.PartyPaymentSummaryDto.builder()
                                .label("Seller (Payment Received)")
                                .memberName("Rakesh Mehta")
                                .memberId("ATG10056")
                                .amount(new BigDecimal("242000.00"))
                                .amountFormatted("₹ 2,42,000.00")
                                .statusMessage("Funds credited to bank account")
                                .timestamp("18 Sep 2025, 11:28 AM")
                                .build())
                        .build())
                .documents(List.of(
                        SettlementConfirmationDto.SettlementDocumentDto.builder().id("settlement-letter").title("Settlement Confirmation Letter").fileFormat("PDF").fileSize("150 KB").downloadUrl("/api/v1/escrow/settlement/" + effSettlementId + "/documents/settlement-letter").build(),
                        SettlementConfirmationDto.SettlementDocumentDto.builder().id("share-transfer-proof").title("Share Transfer Proof (CDSL/NSDL)").fileFormat("PDF").fileSize("220 KB").downloadUrl("/api/v1/escrow/settlement/" + effSettlementId + "/documents/share-transfer-proof").build(),
                        SettlementConfirmationDto.SettlementDocumentDto.builder().id("payment-release").title("Payment Release Confirmation").fileFormat("PDF").fileSize("180 KB").downloadUrl("/api/v1/escrow/settlement/" + effSettlementId + "/documents/payment-release").build(),
                        SettlementConfirmationDto.SettlementDocumentDto.builder().id("escrow-closure").title("Escrow Closure Report").fileFormat("PDF").fileSize("210 KB").downloadUrl("/api/v1/escrow/settlement/" + effSettlementId + "/documents/escrow-closure").build()
                ))
                .build();
    }

    public byte[] downloadSettlementDocument(String settlementId, String docType) {
        StringBuilder sb = new StringBuilder();
        sb.append("=================================================================\n");
        sb.append("                     ATIRATH GROUP                               \n");
        sb.append("             OFFICIAL SETTLEMENT DOCUMENT                        \n");
        sb.append("=================================================================\n\n");
        sb.append("Document: ").append(docType.toUpperCase().replace("-", " ")).append("\n");
        sb.append("Settlement ID: ").append(settlementId).append("\n");
        sb.append("Trade Reference: TRD250918004\n");
        sb.append("Escrow Reference: ESC250918001\n");
        sb.append("Company: Atirath Holdings India Limited (ATG-H)\n");
        sb.append("Buyer: Sairam Vempati (ATG10027)\n");
        sb.append("Seller: Rakesh Mehta (ATG10056)\n");
        sb.append("Shares Settled: 1,000 @ ₹ 242.00\n");
        sb.append("Total Trade Value: ₹ 2,42,000.00\n");
        sb.append("Settlement Reference No: NSDL250918123456\n");
        sb.append("Payment Reference No: PAY250918789012\n");
        sb.append("Settlement Date: 18 Sep 2025, 11:28 AM\n");
        sb.append("Status: COMPLETED\n\n");
        sb.append("Trade Completed. Trust Delivered.\n");
        sb.append("=================================================================\n");
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }
}

