package com.atirath.trading.config;

import com.atirath.trading.common.AccountStatus;
import com.atirath.trading.common.AccountType;
import com.atirath.trading.modules.auth.model.CorporateProfile;
import com.atirath.trading.modules.auth.model.IndividualProfile;
import com.atirath.trading.modules.auth.model.UserAccount;
import com.atirath.trading.modules.auth.model.UserCredential;
import com.atirath.trading.modules.auth.repository.CorporateProfileRepository;
import com.atirath.trading.modules.auth.repository.IndividualProfileRepository;
import com.atirath.trading.modules.auth.repository.UserAccountRepository;
import com.atirath.trading.modules.auth.repository.UserCredentialRepository;
import com.atirath.trading.modules.market.model.*;
import com.atirath.trading.modules.market.repository.*;
import com.atirath.trading.modules.market.service.MarketBroadcastService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserAccountRepository userAccountRepository;
    private final IndividualProfileRepository individualProfileRepository;
    private final CorporateProfileRepository corporateProfileRepository;
    private final UserCredentialRepository userCredentialRepository;
    private final PasswordEncoder passwordEncoder;

    // Market Repositories
    private final MarketCompanyRepository marketCompanyRepository;
    private final MarketPriceHistoryRepository priceHistoryRepository;
    private final MarketTradeRepository tradeRepository;
    private final CompanyLeadershipRepository leadershipRepository;
    private final CompanyHighlightRepository highlightRepository;
    private final CompanyShareholdingRepository shareholdingRepository;
    private final CompanyFinancialRatioRepository financialRatioRepository;
    private final CompanyQuarterlyFinancialRepository quarterlyFinancialRepository;
    private final CompanyAnnualFinancialRepository annualFinancialRepository;
    private final CompanyAnnouncementRepository announcementRepository;
    private final CompanyDocumentRepository documentRepository;
    private final MarketOrderRepository orderRepository;
    private final MarketBroadcastService broadcastService;
    private final EscrowAccountRepository escrowAccountRepository;
    private final UserShareHoldingRepository userShareHoldingRepository;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Checking and seeding initial demo accounts & market data if not present...");

        // 1. Seed Individual Account
        UserAccount indivAccount = null;
        if (!userAccountRepository.existsByEmailIgnoreCase("sairam.v@atirathgroup.com")) {
            indivAccount = userAccountRepository.save(UserAccount.builder()
                    .accountType(AccountType.INDIVIDUAL)
                    .email("sairam.v@atirathgroup.com")
                    .mobileNumber("9876543210")
                    .referralCode("REF_INDIV_01")
                    .accountStatus(AccountStatus.ACTIVE)
                    .build());

            individualProfileRepository.save(IndividualProfile.builder()
                    .userAccount(indivAccount)
                    .fullName("Sairam V")
                    .build());

            userCredentialRepository.save(UserCredential.builder()
                    .userAccount(indivAccount)
                    .passwordHash(passwordEncoder.encode("Password@123"))
                    .passwordChangedAt(OffsetDateTime.now())
                    .build());

            log.info("-> Seeded Individual: sairam.v@atirathgroup.com");
        } else {
            indivAccount = userAccountRepository.findByEmailIgnoreCase("sairam.v@atirathgroup.com").orElse(null);
        }

        // 2. Seed Corporate Account
        UserAccount corpAccount = null;
        if (!userAccountRepository.existsByEmailIgnoreCase("corporate@atirathgroup.com")) {
            corpAccount = userAccountRepository.save(UserAccount.builder()
                    .accountType(AccountType.CORPORATE)
                    .email("corporate@atirathgroup.com")
                    .mobileNumber("9811122233")
                    .referralCode("REF_CORP_01")
                    .accountStatus(AccountStatus.ACTIVE)
                    .build());

            corporateProfileRepository.save(CorporateProfile.builder()
                    .userAccount(corpAccount)
                    .companyName("Atirath Capital Private Limited")
                    .registrationId("U67120MH2021PTC355123")
                    .contactPersonName("Rajesh Sharma")
                    .build());

            userCredentialRepository.save(UserCredential.builder()
                    .userAccount(corpAccount)
                    .passwordHash(passwordEncoder.encode("Corporate@123"))
                    .passwordChangedAt(OffsetDateTime.now())
                    .build());

            log.info("-> Seeded Corporate: corporate@atirathgroup.com");
        } else {
            corpAccount = userAccountRepository.findByEmailIgnoreCase("corporate@atirathgroup.com").orElse(null);
        }

        // 3. Seed Tradable Market Companies
        seedMarketCompanies(indivAccount, corpAccount);

        // 4. Seed Demo Escrow Wallet & Stock Holdings for Sairam Vempati
        if (indivAccount != null) {
            final UserAccount demoUser = indivAccount;
            try {
                if (escrowAccountRepository.findByUserAccount_UserId(demoUser.getUserId()).isEmpty()) {
                    escrowAccountRepository.save(EscrowAccount.builder()
                            .userAccount(demoUser)
                            .accountReference("ESCROW-ATG-10027")
                            .availableBalance(new BigDecimal("1248500.00")) // ₹ 12,48,500.00 from screenshot
                            .blockedBalance(BigDecimal.ZERO)
                            .currencyCode("INR")
                            .accountStatus("ACTIVE")
                            .build());
                    log.info("-> Seeded Escrow Wallet for Sairam Vempati: ₹ 12,48,500.00");
                }
            } catch (Exception e) {
                log.warn("Escrow account seeding skipped or table not ready: {}", e.getMessage());
            }

            try {
                marketCompanyRepository.findBySymbolIgnoreCase("ATG-H").ifPresent(atgComp -> {
                    if (userShareHoldingRepository.findByUserAccount_UserIdAndCompany_CompanyId(demoUser.getUserId(), atgComp.getCompanyId()).isEmpty()) {
                        userShareHoldingRepository.save(UserShareHolding.builder()
                                .userAccount(demoUser)
                                .company(atgComp)
                                .totalShares(3500L) // 3,500 shares from screenshot
                                .lockedShares(0L)
                                .averageBuyPrice(new BigDecimal("215.00"))
                                .build());
                        log.info("-> Seeded Holdings for Sairam Vempati: 3,500 shares of ATG-H");
                    }

                    if (orderRepository.findByOrderNo("BUY25091814322").isEmpty()) {
                        orderRepository.save(MarketOrder.builder()
                                .orderNo("BUY25091814322")
                                .userAccount(demoUser)
                                .company(atgComp)
                                .orderSide("BUY")
                                .orderType("LIMIT")
                                .quantity(1000L)
                                .pricePerShare(new BigDecimal("242.00"))
                                .orderValue(new BigDecimal("242000.00"))
                                .status("IN_ESCROW")
                                .validity("Day Order (Valid till end of trading day)")
                                .placedAt(OffsetDateTime.now().minusHours(2))
                                .build());
                        log.info("-> Seeded MarketOrder BUY25091814322 in PostgreSQL database");
                    }
                });
            } catch (Exception e) {
                log.warn("Holdings/Order seeding skipped or table not ready: {}", e.getMessage());
            }
        }
    }

    private void seedMarketCompanies(UserAccount buyer, UserAccount seller) {
        if (marketCompanyRepository.count() > 0) {
            log.info("Market companies already seeded (count={}). Checking missing sub-data...", marketCompanyRepository.count());
            seedMissingCompanySubData();
            broadcastService.refreshCompanies();
            return;
        }

        log.info("Seeding Atirath Group 7 tradable companies, fundamentals & initial price histories...");

        record CompanySeed(String code, String name, String symbol, String sector, BigDecimal price,
                           String desc, String ceo, String hq, int year, BigDecimal pe, BigDecimal eps,
                           BigDecimal roe, BigDecimal divYield, Long issuedShares, Long availShares) {}

        List<CompanySeed> seeds = List.of(
                new CompanySeed("AHIL", "Atirath Holdings India Limited", "ATG-H", "Diversified", new BigDecimal("245.00"),
                        "Flagship apex investment holding enterprise of Atirath Group.", "Rajesh Atirath", "Hyderabad, Telangana", 2012,
                        new BigDecimal("18.5"), new BigDecimal("45.95"), new BigDecimal("19.2"), new BigDecimal("1.80"), 10000000L, 2500000L),
                new CompanySeed("ATG-TEC", "Atirath Technologies Ltd", "ATG-T", "Information Technology", new BigDecimal("1240.80"),
                        "Enterprise AI solutions, cloud infra, and mission-critical systems.", "Ananya Sharma", "Bengaluru, Karnataka", 2016,
                        new BigDecimal("28.6"), new BigDecimal("43.38"), new BigDecimal("24.8"), new BigDecimal("0.85"), 8000000L, 1800000L),
                new CompanySeed("ATG-ENE", "Atirath Green Energy Ltd", "ATG-E", "Renewable Energy", new BigDecimal("312.40"),
                        "Solar, hybrid wind parks, and next-gen green hydrogen generation.", "Vikramaditya Roy", "Ahmedabad, Gujarat", 2018,
                        new BigDecimal("22.1"), new BigDecimal("14.13"), new BigDecimal("15.6"), new BigDecimal("1.10"), 15000000L, 4200000L),
                new CompanySeed("ATG-FIN", "Atirath Financial Services Ltd", "ATG-F", "Financial Services", new BigDecimal("580.00"),
                        "Institutional escrow, wealth management, asset custody, and NBFC.", "Sunita Menon", "Mumbai, Maharashtra", 2015,
                        new BigDecimal("14.8"), new BigDecimal("39.18"), new BigDecimal("21.5"), new BigDecimal("2.25"), 6500000L, 1200000L),
                new CompanySeed("ATG-LOG", "Atirath Logistics & Ports Ltd", "ATG-L", "Logistics & Infra", new BigDecimal("425.60"),
                        "Multimodal freight, cold-chain warehouses, and inland container depots.", "Karthik Verma", "Chennai, Tamil Nadu", 2017,
                        new BigDecimal("19.7"), new BigDecimal("21.60"), new BigDecimal("17.4"), new BigDecimal("1.60"), 9000000L, 2100000L),
                new CompanySeed("ATG-MIN", "Atirath Mining & Minerals", "ATG-M", "Metals & Mining", new BigDecimal("189.20"),
                        "Rare earth extraction, high-grade graphite, and battery-grade minerals.", "Ramesh Kulkarni", "Nagpur, Maharashtra", 2014,
                        new BigDecimal("11.3"), new BigDecimal("16.74"), new BigDecimal("18.9"), new BigDecimal("3.10"), 12000000L, 3500000L),
                new CompanySeed("ATG-REA", "Atirath Realty & Infra Ltd", "ATG-R", "Real Estate & Infra", new BigDecimal("765.10"),
                        "Commercial SEZ IT parks, smart townships, and luxury commercial estates.", "Deepika Rao", "Hyderabad, Telangana", 2013,
                        new BigDecimal("16.2"), new BigDecimal("47.22"), new BigDecimal("14.8"), new BigDecimal("1.80"), 7500000L, 1600000L)
        );

        int tradeIndex = 1;
        for (CompanySeed cs : seeds) {
            MarketCompany comp = marketCompanyRepository.save(MarketCompany.builder()
                    .companyCode(cs.code)
                    .companyName(cs.name)
                    .symbol(cs.symbol)
                    .sector(cs.sector)
                    .description(cs.desc)
                    .status("ACTIVE")
                    .foundedDate(LocalDate.of(cs.year, 1, 15))
                    .headquarters(cs.hq)
                    .websiteUrl("https://atirathgroup.com/" + cs.symbol.toLowerCase())
                    .issuedShares(cs.issuedShares)
                    .availableShares(cs.availShares)
                    .build());

            // 1. Seed Intraday & Daily Price History
            BigDecimal prevClose = cs.price.multiply(new BigDecimal("0.991")).setScale(2, RoundingMode.HALF_UP);
            BigDecimal open = prevClose.add(new BigDecimal("0.50"));
            BigDecimal high = cs.price.multiply(new BigDecimal("1.018")).setScale(2, RoundingMode.HALF_UP);
            BigDecimal low = cs.price.multiply(new BigDecimal("0.985")).setScale(2, RoundingMode.HALF_UP);

            // Previous Day Close
            priceHistoryRepository.save(MarketPriceHistory.builder()
                    .company(comp)
                    .intervalType("INTRADAY")
                    .priceTime(OffsetDateTime.now().minusHours(24))
                    .openPrice(open.subtract(new BigDecimal("2.00")))
                    .highPrice(high.subtract(new BigDecimal("1.00")))
                    .lowPrice(low.subtract(new BigDecimal("2.00")))
                    .closePrice(prevClose)
                    .volumeShares(42000L)
                    .turnoverValue(prevClose.multiply(new BigDecimal("42000")).setScale(2, RoundingMode.HALF_UP))
                    .build());

            // Today's Intraday Latest
            priceHistoryRepository.save(MarketPriceHistory.builder()
                    .company(comp)
                    .intervalType("INTRADAY")
                    .priceTime(OffsetDateTime.now())
                    .openPrice(open)
                    .highPrice(high)
                    .lowPrice(low)
                    .closePrice(cs.price)
                    .volumeShares(35800L)
                    .turnoverValue(cs.price.multiply(new BigDecimal("35800")).setScale(2, RoundingMode.HALF_UP))
                    .build());

            // Daily Candle
            priceHistoryRepository.save(MarketPriceHistory.builder()
                    .company(comp)
                    .intervalType("DAY")
                    .priceTime(OffsetDateTime.now())
                    .openPrice(open)
                    .highPrice(high)
                    .lowPrice(low)
                    .closePrice(cs.price)
                    .volumeShares(35800L)
                    .turnoverValue(cs.price.multiply(new BigDecimal("35800")).setScale(2, RoundingMode.HALF_UP))
                    .build());

            // 2. Leadership
            leadershipRepository.save(CompanyLeadership.builder()
                    .company(comp)
                    .personName(cs.ceo)
                    .designation("Managing Director & CEO")
                    .displayOrder(1)
                    .isActive(true)
                    .build());

            leadershipRepository.save(CompanyLeadership.builder()
                    .company(comp)
                    .personName("P. Sundaram")
                    .designation("Chief Financial Officer")
                    .displayOrder(2)
                    .isActive(true)
                    .build());

            // 3. Highlights
            highlightRepository.save(CompanyHighlight.builder()
                    .company(comp)
                    .highlightText("Consistent double-digit CAGR profit growth over 5 financial years.")
                    .displayOrder(1)
                    .isActive(true)
                    .build());

            highlightRepository.save(CompanyHighlight.builder()
                    .company(comp)
                    .highlightText("AA+ rated balance sheet with conservative leverage ratio.")
                    .displayOrder(2)
                    .isActive(true)
                    .build());

            // 4. Shareholding
            shareholdingRepository.save(CompanyShareholding.builder()
                    .company(comp)
                    .holderCategory("PROMOTERS")
                    .sharesHeld((long) (cs.issuedShares * 0.68))
                    .percentage(new BigDecimal("68.00"))
                    .asOfDate(LocalDate.now())
                    .build());

            shareholdingRepository.save(CompanyShareholding.builder()
                    .company(comp)
                    .holderCategory("EMPLOYEES")
                    .sharesHeld((long) (cs.issuedShares * 0.22))
                    .percentage(new BigDecimal("22.00"))
                    .asOfDate(LocalDate.now())
                    .build());

            shareholdingRepository.save(CompanyShareholding.builder()
                    .company(comp)
                    .holderCategory("GROUP_COMPANIES")
                    .sharesHeld((long) (cs.issuedShares * 0.10))
                    .percentage(new BigDecimal("10.00"))
                    .asOfDate(LocalDate.now())
                    .build());

            // 5. Financial Ratios
            financialRatioRepository.save(CompanyFinancialRatio.builder()
                    .company(comp)
                    .financialYear("FY2025-26")
                    .peRatio(cs.pe)
                    .eps(cs.eps)
                    .bookValue(cs.price.divide(new BigDecimal("2.5"), 2, RoundingMode.HALF_UP))
                    .priceToBook(new BigDecimal("2.50"))
                    .debtToEquity(new BigDecimal("0.42"))
                    .currentRatio(new BigDecimal("1.85"))
                    .roe(cs.roe)
                    .roce(cs.roe.multiply(new BigDecimal("1.1")).setScale(2, RoundingMode.HALF_UP))
                    .dividendYield(cs.divYield)
                    .netProfitMargin(new BigDecimal("18.40"))
                    .ebitdaMargin(new BigDecimal("26.80"))
                    .build());

            // 6. Quarterly Financials
            quarterlyFinancialRepository.save(CompanyQuarterlyFinancial.builder()
                    .company(comp)
                    .financialYear("FY25")
                    .periodLabel("Q3 FY25")
                    .revenue(new BigDecimal("480.50"))
                    .ebitda(new BigDecimal("128.80"))
                    .netProfit(new BigDecimal("88.40"))
                    .ebitdaMargin(new BigDecimal("26.80"))
                    .build());

            quarterlyFinancialRepository.save(CompanyQuarterlyFinancial.builder()
                    .company(comp)
                    .financialYear("FY25")
                    .periodLabel("Q2 FY25")
                    .revenue(new BigDecimal("445.10"))
                    .ebitda(new BigDecimal("115.60"))
                    .netProfit(new BigDecimal("79.20"))
                    .ebitdaMargin(new BigDecimal("25.90"))
                    .build());

            // 7. Seed Initial Trade with matching Buy and Sell Orders
            MarketOrder buyOrder = orderRepository.save(MarketOrder.builder()
                    .orderNo(String.format("ORD-BUY-%s-%03d", cs.symbol, tradeIndex))
                    .userAccount(buyer)
                    .company(comp)
                    .orderSide("BUY")
                    .orderType("LIMIT")
                    .quantity(100L)
                    .pricePerShare(cs.price)
                    .orderValue(cs.price.multiply(new BigDecimal("100")))
                    .status("EXECUTED")
                    .build());

            MarketOrder sellOrder = orderRepository.save(MarketOrder.builder()
                    .orderNo(String.format("ORD-SEL-%s-%03d", cs.symbol, tradeIndex))
                    .userAccount(seller)
                    .company(comp)
                    .orderSide("SELL")
                    .orderType("LIMIT")
                    .quantity(100L)
                    .pricePerShare(cs.price)
                    .orderValue(cs.price.multiply(new BigDecimal("100")))
                    .status("EXECUTED")
                    .build());

            tradeRepository.save(MarketTrade.builder()
                    .tradeNo(String.format("TRD-%s-INIT%02d", cs.symbol, tradeIndex++))
                    .company(comp)
                    .buyOrderId(buyOrder.getOrderId())
                    .sellOrderId(sellOrder.getOrderId())
                    .buyerUser(buyer)
                    .sellerUser(seller)
                    .quantity(100L)
                    .tradePrice(cs.price)
                    .tradeValue(cs.price.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP))
                    .executedAt(OffsetDateTime.now().minusMinutes(tradeIndex * 15L))
                    .status("EXECUTED")
                    .build());
        }

        log.info("Finished seeding 7 market companies with full fundamentals, price history, and trades.");
        seedMissingCompanySubData();
        broadcastService.refreshCompanies();
    }

    private void seedMissingCompanySubData() {
        List<MarketCompany> companies = marketCompanyRepository.findAll();
        for (MarketCompany comp : companies) {
            Long cid = comp.getCompanyId();
            String sym = comp.getSymbol();

            BigDecimal anchorPrice = switch (sym != null ? sym.toUpperCase() : "") {
                case "ATG-H" -> new BigDecimal("245.00");
                case "ATG-T" -> new BigDecimal("1240.80");
                case "ATG-E" -> new BigDecimal("312.40");
                case "ATG-F" -> new BigDecimal("580.00");
                case "ATG-L" -> new BigDecimal("425.60");
                case "ATG-M" -> new BigDecimal("189.20");
                case "ATG-R" -> new BigDecimal("765.10");
                default -> new BigDecimal("245.00");
            };

            // 1. Backfill Annual Financials (5 financial years) if empty
            if (annualFinancialRepository.findByCompany_CompanyIdOrderByFinancialYearDesc(cid).isEmpty()) {
                BigDecimal scaleFactor = anchorPrice.divide(new BigDecimal("245.00"), 2, RoundingMode.HALF_UP)
                        .max(new BigDecimal("0.65"));
                CompanyFinancialRatio latestRatio = financialRatioRepository
                        .findTopByCompany_CompanyIdOrderByFinancialYearDesc(cid).orElse(null);

                List<String> years = List.of("FY 2021", "FY 2022", "FY 2023", "FY 2024", "FY 2025");
                double[] baseRev = {320, 410, 520, 620, 720};
                double[] baseEbitda = {90, 120, 160, 190, 230};
                double[] basePat = {40, 55, 68, 75, 95};
                double[] baseAssets = {980, 1120, 1290, 1450, 1680};
                double[] baseEquity = {540, 620, 710, 820, 940};

                for (int i = 0; i < years.size(); i++) {
                    BigDecimal rev = BigDecimal.valueOf(baseRev[i]).multiply(scaleFactor).setScale(2, RoundingMode.HALF_UP);
                    BigDecimal ebitda = BigDecimal.valueOf(baseEbitda[i]).multiply(scaleFactor).setScale(2, RoundingMode.HALF_UP);
                    BigDecimal pat = BigDecimal.valueOf(basePat[i]).multiply(scaleFactor).setScale(2, RoundingMode.HALF_UP);
                    BigDecimal ebitdaMargin = ebitda.divide(rev, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100")).setScale(1, RoundingMode.HALF_UP);
                    BigDecimal patMargin = pat.divide(rev, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100")).setScale(1, RoundingMode.HALF_UP);
                    BigDecimal assets = BigDecimal.valueOf(baseAssets[i] * 10000000.0).multiply(scaleFactor).setScale(2, RoundingMode.HALF_UP);
                    BigDecimal equity = BigDecimal.valueOf(baseEquity[i] * 10000000.0).multiply(scaleFactor).setScale(2, RoundingMode.HALF_UP);

                    annualFinancialRepository.save(CompanyAnnualFinancial.builder()
                            .company(comp)
                            .financialYear(years.get(i))
                            .periodEndDate(LocalDate.of(2021 + i, 3, 31))
                            .revenue(rev)
                            .ebitda(ebitda)
                            .ebitdaMargin(ebitdaMargin)
                            .netProfit(pat)
                            .netProfitMargin(patMargin)
                            .totalAssets(assets)
                            .totalEquity(equity)
                            .debtToEquity(latestRatio != null && latestRatio.getDebtToEquity() != null ? latestRatio.getDebtToEquity() : new BigDecimal("0.42"))
                            .currentRatio(latestRatio != null && latestRatio.getCurrentRatio() != null ? latestRatio.getCurrentRatio() : new BigDecimal("1.86"))
                            .roe(latestRatio != null && latestRatio.getRoe() != null ? latestRatio.getRoe() : new BigDecimal("12.80"))
                            .roce(latestRatio != null && latestRatio.getRoce() != null ? latestRatio.getRoce() : new BigDecimal("15.40"))
                            .eps(latestRatio != null && latestRatio.getEps() != null ? latestRatio.getEps() : new BigDecimal("7.50"))
                            .peRatio(latestRatio != null && latestRatio.getPeRatio() != null ? latestRatio.getPeRatio() : new BigDecimal("18.50"))
                            .bookValue(latestRatio != null && latestRatio.getBookValue() != null ? latestRatio.getBookValue() : new BigDecimal("82.40"))
                            .priceToBook(latestRatio != null && latestRatio.getPriceToBook() != null ? latestRatio.getPriceToBook() : new BigDecimal("2.50"))
                            .dividendYield(latestRatio != null && latestRatio.getDividendYield() != null ? latestRatio.getDividendYield() : new BigDecimal("1.80"))
                            .build());
                }
            }

            // 2. Backfill Company Documents if empty
            if (documentRepository.findByCompany_CompanyIdOrderByPeriodDateDesc(cid).isEmpty()) {
                record DocSeed(String name, String category, LocalDate date, long sizeBytes, String mime, boolean verified) {}
                List<DocSeed> docSeeds = List.of(
                        new DocSeed("Annual Report FY 2024", "Annual Reports", LocalDate.of(2025, 3, 31), 4404019L, "application/pdf", true),
                        new DocSeed("Q1 FY2025 Financial Results", "Financial Statements", LocalDate.of(2025, 6, 30), 1887436L, "application/pdf", true),
                        new DocSeed("Investor Presentation FY25", "Investor Presentations", LocalDate.of(2025, 5, 15), 6815744L, "application/vnd.ms-powerpoint", true),
                        new DocSeed("Notice of Annual General Meeting", "Board Announcements", LocalDate.of(2025, 4, 12), 1153433L, "application/pdf", true),
                        new DocSeed("Corporate Governance Report", "Policies & Governance", LocalDate.of(2025, 3, 31), 2411724L, "application/pdf", true),
                        new DocSeed("Shareholding Pattern Report", "Shareholder Information", LocalDate.of(2025, 6, 30), 943718L, "application/pdf", true),
                        new DocSeed("Financial Highlights (Excel)", "Financial Statements", LocalDate.of(2025, 6, 30), 1258291L, "application/vnd.ms-excel", true),
                        new DocSeed("ESG & Sustainability Report", "Others", LocalDate.of(2025, 3, 31), 3774873L, "application/pdf", true),
                        new DocSeed("Related Party Transactions Disclosure", "Regulatory Filings", LocalDate.of(2025, 3, 31), 1468006L, "application/pdf", true),
                        new DocSeed("Dividend Distribution Announcement", "Board Announcements", LocalDate.of(2025, 8, 20), 734003L, "application/pdf", true)
                );
                for (DocSeed ds : docSeeds) {
                    CompanyDocument saved = documentRepository.save(CompanyDocument.builder()
                            .company(comp)
                            .documentName(ds.name)
                            .category(ds.category)
                            .periodDate(ds.date)
                            .fileUrl("PENDING")
                            .fileSizeBytes(ds.sizeBytes)
                            .mimeType(ds.mime)
                            .isVerified(ds.verified)
                            .build());
                    saved.setFileUrl("/api/v1/market/companies/" + cid + "/documents/" + saved.getDocumentId() + "/download");
                    documentRepository.save(saved);
                }
            }

            // 3. Backfill Company Announcements if empty
            if (announcementRepository.findByCompany_CompanyIdOrderByAnnouncementDateDesc(cid).isEmpty()) {
                List<CompanyDocument> docs = documentRepository.findByCompany_CompanyIdOrderByPeriodDateDesc(cid);
                CompanyDocument linkedDoc = docs.isEmpty() ? null : docs.get(0);

                announcementRepository.saveAll(List.of(
                        CompanyAnnouncement.builder()
                                .company(comp)
                                .title("Board Approves Strategic Capacity Expansion for " + comp.getCompanyName())
                                .description("Board of directors approved internal capital allocation for growth projects.")
                                .announcementType("BOARD")
                                .announcementDate(LocalDate.now().minusDays(6))
                                .document(linkedDoc)
                                .status("PUBLISHED")
                                .build(),
                        CompanyAnnouncement.builder()
                                .company(comp)
                                .title("Quarterly Financial Results Declared")
                                .description("Reviewed unaudited financial statements for the latest quarter.")
                                .announcementType("RESULT")
                                .announcementDate(LocalDate.now().minusDays(14))
                                .document(linkedDoc)
                                .status("PUBLISHED")
                                .build(),
                        CompanyAnnouncement.builder()
                                .company(comp)
                                .title("Dividend & Shareholder Update Announced")
                                .description("Declared interim dividend and updated shareholder disclosure records.")
                                .announcementType("DIVIDEND")
                                .announcementDate(LocalDate.now().minusDays(28))
                                .document(linkedDoc)
                                .status("PUBLISHED")
                                .build(),
                        CompanyAnnouncement.builder()
                                .company(comp)
                                .title("Operational Excellence & Sustainability Milestone")
                                .description("Achieved quarterly operating targets across group divisions.")
                                .announcementType("STRATEGIC")
                                .announcementDate(LocalDate.now().minusDays(45))
                                .document(linkedDoc)
                                .status("PUBLISHED")
                                .build()
                ));
            }

            // 4. Backfill OHLC Price History candles across INTRADAY, DAY, WEEK, MONTH if sparse (< 10 points)
            seedIntervalCandlesIfSparse(comp, "INTRADAY", anchorPrice, 36, 15L);
            seedIntervalCandlesIfSparse(comp, "DAY", anchorPrice, 30, 1440L);
            seedIntervalCandlesIfSparse(comp, "WEEK", anchorPrice, 26, 10080L);
            seedIntervalCandlesIfSparse(comp, "MONTH", anchorPrice, 24, 43200L);
        }
        log.info("Verified & backfilled sub-data (annual financials, documents, announcements, OHLC charts) for {} companies.", companies.size());
    }

    private void seedIntervalCandlesIfSparse(MarketCompany comp, String intervalType, BigDecimal targetClose, int count, long stepMinutes) {
        List<MarketPriceHistory> existing = priceHistoryRepository
                .findByCompany_CompanyIdAndIntervalTypeOrderByPriceTimeAsc(comp.getCompanyId(), intervalType);

        // Clean legacy Oct-12/Oct-13 outliers so charts remain contiguous and scaled to live prices
        if (!existing.isEmpty()) {
            OffsetDateTime latestTime = existing.get(existing.size() - 1).getPriceTime();
            double target = targetClose.doubleValue();
            List<MarketPriceHistory> staleOutliers = existing.stream()
                    .filter(p -> {
                        boolean disconnectedIntraday = "INTRADAY".equalsIgnoreCase(intervalType)
                                && latestTime != null
                                && p.getPriceTime() != null
                                && p.getPriceTime().isBefore(latestTime.minusHours(36));
                        double close = p.getClosePrice() != null ? p.getClosePrice().doubleValue() : target;
                        boolean priceOutlier = target > 0 && (close > target * 1.65 || close < target * 0.35);
                        return disconnectedIntraday || priceOutlier;
                    })
                    .toList();
            if (!staleOutliers.isEmpty()) {
                priceHistoryRepository.deleteAll(staleOutliers);
                existing = priceHistoryRepository
                        .findByCompany_CompanyIdAndIntervalTypeOrderByPriceTimeAsc(comp.getCompanyId(), intervalType);
            }
        }

        if (existing.size() >= 10) {
            return;
        }

        double endPrice = targetClose.doubleValue();
        double startPrice = "ATG-H".equalsIgnoreCase(comp.getSymbol()) && "INTRADAY".equals(intervalType)
                ? 210.00
                : endPrice * 0.90;
        long seed = comp.getCompanyId() * 31L + intervalType.hashCode();
        OffsetDateTime now = OffsetDateTime.now();

        double curOpen = startPrice;
        List<MarketPriceHistory> batch = new java.util.ArrayList<>();
        for (int i = 0; i < count; i++) {
            double progress = (double) i / Math.max(1, count - 1);
            double wave = Math.sin(progress * Math.PI * 3.2 + (seed % 7)) * (endPrice * 0.025);
            double close = (i == count - 1)
                    ? endPrice
                    : Math.max(5.0, startPrice + (endPrice - startPrice) * progress + wave);
            double wick = Math.max(Math.abs(close - curOpen) * 0.55, endPrice * 0.004);
            double high = Math.max(curOpen, close) + wick;
            double low = Math.max(1.0, Math.min(curOpen, close) - wick * 0.85);
            long vol = 3500L + ((i * 733L + Math.abs(seed)) % 18500L);

            BigDecimal o = BigDecimal.valueOf(curOpen).setScale(2, RoundingMode.HALF_UP);
            BigDecimal c = BigDecimal.valueOf(close).setScale(2, RoundingMode.HALF_UP);
            BigDecimal h = BigDecimal.valueOf(high).max(o).max(c).setScale(2, RoundingMode.HALF_UP);
            BigDecimal l = BigDecimal.valueOf(low).min(o).min(c).setScale(2, RoundingMode.HALF_UP);

            batch.add(MarketPriceHistory.builder()
                    .company(comp)
                    .intervalType(intervalType)
                    .priceTime(now.minusMinutes((long) (count - 1 - i) * stepMinutes))
                    .openPrice(o)
                    .highPrice(h)
                    .lowPrice(l)
                    .closePrice(c)
                    .volumeShares(vol)
                    .turnoverValue(c.multiply(BigDecimal.valueOf(vol)).setScale(2, RoundingMode.HALF_UP))
                    .build());

            curOpen = close;
        }
        priceHistoryRepository.saveAll(batch);
    }
}
