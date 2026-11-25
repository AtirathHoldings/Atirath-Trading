package com.atirath.trading.modules.admin.parent.service;

import com.atirath.trading.modules.admin.parent.dto.*;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ParentCompanyAdminService {

    private AdminUserHeaderDto adminHeader;
    private final List<SubsidiarySummaryDto> subsidiaries = new CopyOnWriteArrayList<>();
    private final Map<String, SubsidiaryDetailDto> subsidiaryDetailsMap = new ConcurrentHashMap<>();
    private final AtomicInteger subsidiaryCounter = new AtomicInteger(6);

    // Share Capital Details Store
    private final List<GroupShareCapitalPageDto.SubsidiaryShareCapitalItemDto> shareCapitalItems = new CopyOnWriteArrayList<>();
    private final List<GroupShareCapitalPageDto.ShareCapitalActivityItemDto> shareCapitalActivities = new CopyOnWriteArrayList<>();

    // Shareholders Store
    private final List<GroupShareholdersPageDto.ShareholderItemDto> shareholders = new CopyOnWriteArrayList<>();
    private final AtomicInteger shareholderCounter = new AtomicInteger(10);

    // Holdings Store
    private final List<GroupHoldingsPageDto.GroupHoldingItemDto> groupHoldings = new CopyOnWriteArrayList<>();

    // Market Activity Store
    private final List<MarketActivityGroupPageDto.MarketActivityTradeDto> marketActivityTrades = new CopyOnWriteArrayList<>();

    // Trade Monitoring Store
    private final List<GroupTradeMonitoringPageDto.TradeMonitoringItemDto> tradeMonitoringItems = new CopyOnWriteArrayList<>();
    private final List<GroupTradeMonitoringPageDto.UnusualTradeActivityItemDto> unusualTradeActivities = new CopyOnWriteArrayList<>();

    // Escrow & Settlement Store
    private final List<EscrowSettlementGroupPageDto.EscrowTransactionItemDto> escrowTransactions = new CopyOnWriteArrayList<>();
    private final AtomicInteger escrowCounter = new AtomicInteger(8);

    // Approvals Store
    private final List<ApprovalsPageDto.ApprovalItemDto> approvalsList = new CopyOnWriteArrayList<>();
    private final Map<String, ApprovalDetailDto> approvalDetailsMap = new ConcurrentHashMap<>();
    private final AtomicInteger approvalCounter = new AtomicInteger(10);

    // External Investor Requests Store
    private final List<ExternalInvestorRequestsPageDto.InvestorRequestItemDto> investorRequestsList = new CopyOnWriteArrayList<>();
    private final Map<String, InvestorRequestDetailDto> investorDetailsMap = new ConcurrentHashMap<>();
    private final AtomicInteger investorCounter = new AtomicInteger(10);

    @PostConstruct
    public void init() {
        log.info("[ParentCompanyAdminService] Initializing Parent Company Admin mock datasets matching UI designs...");

        adminHeader = AdminUserHeaderDto.builder()
                .name("Admin User")
                .initials("AD")
                .role("Parent Company Admin")
                .currentDate("Today, 16 Sep 2026")
                .unreadNotifications(3)
                .build();

        seedSubsidiaries();
        seedSubsidiaryDetails();
        seedShareCapital();
        seedShareholders();
        seedHoldings();
        seedMarketActivity();
        seedTradeMonitoring();
        seedEscrowSettlement();
        seedApprovals();
        seedInvestorRequests();

        log.info("[ParentCompanyAdminService] Initialization completed. Ready for Admin requests.");
    }

    // ==========================================
    // 1. PARENT COMPANY DASHBOARD
    // ==========================================
    public ParentDashboardDto getDashboard() {
        return getDashboard(null, null);
    }

    public ParentDashboardDto getDashboard(String dateRange, String company) {
        String activeDateFilter = dateRange != null && !dateRange.isBlank() ? dateRange : "Today, 16 Sep 2026";

        List<KpiCardDto> kpis = List.of(
                KpiCardDto.builder().title("Total Subsidiaries").value(String.valueOf(subsidiaries.size() > 0 ? subsidiaries.size() : 6)).change("+1").comparisonText("vs last month").trend("positive").icon("building").color("blue").build(),
                KpiCardDto.builder().title("Total Members").value("842").change("+12%").comparisonText("vs last month").trend("positive").icon("users").color("green").build(),
                KpiCardDto.builder().title("Total Trade Volume").value("₹ 24.8 Cr").change("+18%").comparisonText("vs last month").trend("positive").icon("bar-chart").color("orange").build(),
                KpiCardDto.builder().title("Total Settlement Value").value("₹ 23.6 Cr").change("+16%").comparisonText("vs last month").trend("positive").icon("rupee").color("purple").build(),
                KpiCardDto.builder().title("Escrow Balance").value("₹ 4.2 Cr").change("+5%").comparisonText("vs last month").trend("positive").icon("wallet").color("red").build()
        );

        List<ParentDashboardDto.GroupCompanyPerformanceDto> groupPerformance = List.of(
                ParentDashboardDto.GroupCompanyPerformanceDto.builder().company("Atirath Traders").volumeCr(8.4).color("#3b82f6").build(),
                ParentDashboardDto.GroupCompanyPerformanceDto.builder().company("Atirath Holdings").volumeCr(5.2).color("#22c55e").build(),
                ParentDashboardDto.GroupCompanyPerformanceDto.builder().company("Atirath Technologies").volumeCr(3.6).color("#f87171").build(),
                ParentDashboardDto.GroupCompanyPerformanceDto.builder().company("Farm Vantara").volumeCr(2.8).color("#a855f7").build(),
                ParentDashboardDto.GroupCompanyPerformanceDto.builder().company("Goeazymart").volumeCr(2.1).color("#f59e0b").build(),
                ParentDashboardDto.GroupCompanyPerformanceDto.builder().company("Tri Energy Battery").volumeCr(1.9).color("#06b6d4").build()
        );

        List<ParentDashboardDto.TradeActivityTrendDto> tradeTrends = List.of(
                ParentDashboardDto.TradeActivityTrendDto.builder().date("10 Sep").buyVolume(2.1).sellVolume(1.2).build(),
                ParentDashboardDto.TradeActivityTrendDto.builder().date("11 Sep").buyVolume(4.5).sellVolume(2.4).build(),
                ParentDashboardDto.TradeActivityTrendDto.builder().date("12 Sep").buyVolume(6.2).sellVolume(3.8).build(),
                ParentDashboardDto.TradeActivityTrendDto.builder().date("13 Sep").buyVolume(3.9).sellVolume(2.1).build(),
                ParentDashboardDto.TradeActivityTrendDto.builder().date("14 Sep").buyVolume(4.8).sellVolume(3.2).build(),
                ParentDashboardDto.TradeActivityTrendDto.builder().date("15 Sep").buyVolume(6.5).sellVolume(4.6).build(),
                ParentDashboardDto.TradeActivityTrendDto.builder().date("16 Sep").buyVolume(4.8).sellVolume(3.8).build()
        );

        ParentDashboardDto.MarketOverviewDto marketOverview = ParentDashboardDto.MarketOverviewDto.builder()
                .totalVolume("₹ 24.8 Cr")
                .slices(List.of(
                        ParentDashboardDto.MarketShareSliceDto.builder().company("Atirath Traders").percentage(33.9).color("#3b82f6").build(),
                        ParentDashboardDto.MarketShareSliceDto.builder().company("Atirath Holdings").percentage(21.0).color("#22c55e").build(),
                        ParentDashboardDto.MarketShareSliceDto.builder().company("Atirath Technologies").percentage(14.5).color("#f87171").build(),
                        ParentDashboardDto.MarketShareSliceDto.builder().company("Farm Vantara").percentage(11.3).color("#a855f7").build(),
                        ParentDashboardDto.MarketShareSliceDto.builder().company("Goeazymart").percentage(8.5).color("#f59e0b").build(),
                        ParentDashboardDto.MarketShareSliceDto.builder().company("Tri Energy Battery").percentage(7.7).color("#06b6d4").build()
                ))
                .build();

        List<ParentDashboardDto.TopTradedSecurityDto> topSecurities = List.of(
                ParentDashboardDto.TopTradedSecurityDto.builder().security("ATIPL").company("Atirath Holdings").volume("5.2 Cr").ltp("₹ 245.60").change("+ 4.2%").trend("positive").build(),
                ParentDashboardDto.TopTradedSecurityDto.builder().security("ATIT").company("Atirath Traders").volume("4.8 Cr").ltp("₹ 180.30").change("+ 3.1%").trend("positive").build(),
                ParentDashboardDto.TopTradedSecurityDto.builder().security("ATT").company("Atirath Technologies").volume("3.6 Cr").ltp("₹ 98.75").change("- 1.8%").trend("negative").build(),
                ParentDashboardDto.TopTradedSecurityDto.builder().security("FVPL").company("Farm Vantara").volume("2.8 Cr").ltp("₹ 76.40").change("+ 2.6%").trend("positive").build(),
                ParentDashboardDto.TopTradedSecurityDto.builder().security("GEM").company("Goeazymart").volume("2.1 Cr").ltp("₹ 54.20").change("+ 1.9%").trend("positive").build()
        );

        List<ParentDashboardDto.PendingApprovalItemDto> pendingApprovals = List.of(
                ParentDashboardDto.PendingApprovalItemDto.builder().title("Trade Approvals").count(5).icon("trade").theme("red").build(),
                ParentDashboardDto.PendingApprovalItemDto.builder().title("New Member Requests").count(12).icon("member").theme("orange").build(),
                ParentDashboardDto.PendingApprovalItemDto.builder().title("Document Verifications").count(8).icon("document").theme("blue").build(),
                ParentDashboardDto.PendingApprovalItemDto.builder().title("Investor Requests").count(3).icon("investor").theme("dark").build(),
                ParentDashboardDto.PendingApprovalItemDto.builder().title("Bank Account Approvals").count(4).icon("bank").theme("green").build()
        );

        List<ParentDashboardDto.RecentTradeActivityDto> recentTrades = List.of(
                ParentDashboardDto.RecentTradeActivityDto.builder().time("16 Sep 2026, 11:24 AM").security("ATIPL").company("Atirath Holdings").type("Buy").quantity("10,000").price("₹ 245.60").value("₹ 24,56,000").status("Executed").build(),
                ParentDashboardDto.RecentTradeActivityDto.builder().time("16 Sep 2026, 10:18 AM").security("ATIT").company("Atirath Traders").type("Sell").quantity("5,000").price("₹ 180.30").value("₹ 9,01,500").status("Executed").build(),
                ParentDashboardDto.RecentTradeActivityDto.builder().time("16 Sep 2026, 09:45 AM").security("ATT").company("Atirath Technologies").type("Buy").quantity("8,000").price("₹ 98.75").value("₹ 7,90,000").status("Executed").build(),
                ParentDashboardDto.RecentTradeActivityDto.builder().time("15 Sep 2026, 04:12 PM").security("FVPL").company("Farm Vantara").type("Sell").quantity("12,000").price("₹ 76.40").value("₹ 9,16,800").status("Executed").build(),
                ParentDashboardDto.RecentTradeActivityDto.builder().time("15 Sep 2026, 03:28 PM").security("GEM").company("Goeazymart").type("Buy").quantity("20,000").price("₹ 54.20").value("₹ 10,84,000").status("Executed").build()
        );

        List<ParentDashboardDto.RecentMemberActivityDto> recentMembers = List.of(
                ParentDashboardDto.RecentMemberActivityDto.builder().time("16 Sep 2026, 11:05 AM").memberOrInvestor("Ramesh Kumar").activity("New Investor Request").status("Pending").statusType("Pending").build(),
                ParentDashboardDto.RecentMemberActivityDto.builder().time("16 Sep 2026, 10:42 AM").memberOrInvestor("Priya Verma").activity("KYC Verification").status("Approved").statusType("Approved").build(),
                ParentDashboardDto.RecentMemberActivityDto.builder().time("16 Sep 2026, 09:30 AM").memberOrInvestor("Global Renewables Ltd").activity("Investor Onboarding").status("Pending").statusType("Pending").build(),
                ParentDashboardDto.RecentMemberActivityDto.builder().time("15 Sep 2026, 05:18 PM").memberOrInvestor("Vikram Shah").activity("Bank Account Update").status("Approved").statusType("Approved").build(),
                ParentDashboardDto.RecentMemberActivityDto.builder().time("15 Sep 2026, 04:02 PM").memberOrInvestor("Sunil Reddy").activity("New Member Request").status("Pending").statusType("Pending").build()
        );

        return ParentDashboardDto.builder()
                .adminUser(adminHeader)
                .pageTitle("Parent Company Dashboard")
                .pageSubtitle("Overview of group companies, trading activity, investments and key metrics.")
                .dateFilter(activeDateFilter)
                .kpis(kpis)
                .groupCompanyPerformance(groupPerformance)
                .tradeActivityTrends(tradeTrends)
                .marketOverview(marketOverview)
                .topTradedSecurities(topSecurities)
                .pendingApprovals(pendingApprovals)
                .recentTradeActivities(recentTrades)
                .recentMemberActivities(recentMembers)
                .build();
    }

    // ==========================================
    // 2. SUBSIDIARY COMPANIES PAGE
    // ==========================================
    public SubsidiariesPageDto getSubsidiariesPage(String query, String statusFilter, String sectorFilter) {
        long activeCount = subsidiaries.stream().filter(s -> "Active".equalsIgnoreCase(s.getStatus())).count();
        long underSetupCount = subsidiaries.stream().filter(s -> "Under Setup".equalsIgnoreCase(s.getStatus())).count();
        long inactiveCount = subsidiaries.stream().filter(s -> "Inactive".equalsIgnoreCase(s.getStatus())).count();
        int totalEmployees = subsidiaries.stream().mapToInt(SubsidiarySummaryDto::getTotalMembers).sum();

        List<KpiCardDto> kpis = List.of(
                KpiCardDto.builder().title("Total Subsidiaries").value(String.valueOf(subsidiaries.size())).change("Active companies").comparisonText("").trend("neutral").icon("building").color("blue").build(),
                KpiCardDto.builder().title("Active").value(String.valueOf(activeCount)).change("Currently operational").comparisonText("").trend("positive").icon("check-circle").color("green").build(),
                KpiCardDto.builder().title("Under Setup").value(String.valueOf(underSetupCount)).change("Incorporation / Setup").comparisonText("").trend("neutral").icon("pause-circle").color("orange").build(),
                KpiCardDto.builder().title("Inactive").value(String.valueOf(inactiveCount)).change("Not operational").comparisonText("").trend("negative").icon("x-circle").color("red").build(),
                KpiCardDto.builder().title("Total Employees").value(String.valueOf(totalEmployees > 0 ? totalEmployees : 842)).change("Across all subsidiaries").comparisonText("").trend("positive").icon("users").color("purple").build()
        );

        List<SubsidiarySummaryDto> filtered = subsidiaries.stream()
                .filter(s -> {
                    if (query != null && !query.isBlank()) {
                        String q = query.toLowerCase();
                        if (!s.getCompanyName().toLowerCase().contains(q) &&
                            !s.getCin().toLowerCase().contains(q) &&
                            !s.getSector().toLowerCase().contains(q)) {
                            return false;
                        }
                    }
                    if (statusFilter != null && !statusFilter.isBlank() && !statusFilter.equalsIgnoreCase("All Status")) {
                        if (!s.getStatus().equalsIgnoreCase(statusFilter)) return false;
                    }
                    if (sectorFilter != null && !sectorFilter.isBlank() && !sectorFilter.equalsIgnoreCase("All Sectors")) {
                        if (!s.getSector().equalsIgnoreCase(sectorFilter)) return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());

        SubsidiariesPageDto.SectorDistributionDto sectorDistribution = SubsidiariesPageDto.SectorDistributionDto.builder()
                .totalTradeValue("₹ 24.8 Cr")
                .slices(List.of(
                        SubsidiariesPageDto.SectorShareSliceDto.builder().sector("Trading").valueCr(8.4).percentage(33.9).color("#3b82f6").build(),
                        SubsidiariesPageDto.SectorShareSliceDto.builder().sector("Renewable Energy").valueCr(5.2).percentage(21.0).color("#22c55e").build(),
                        SubsidiariesPageDto.SectorShareSliceDto.builder().sector("Technology").valueCr(3.6).percentage(14.5).color("#f87171").build(),
                        SubsidiariesPageDto.SectorShareSliceDto.builder().sector("Agriculture").valueCr(2.8).percentage(11.3).color("#a855f7").build(),
                        SubsidiariesPageDto.SectorShareSliceDto.builder().sector("Retail").valueCr(2.1).percentage(8.5).color("#f59e0b").build(),
                        SubsidiariesPageDto.SectorShareSliceDto.builder().sector("Energy Storage").valueCr(1.9).percentage(7.7).color("#06b6d4").build()
                ))
                .build();

        List<SubsidiariesPageDto.SubsidiaryUpdateItemDto> updates = List.of(
                SubsidiariesPageDto.SubsidiaryUpdateItemDto.builder().time("16 Sep 2026, 11:20 AM").description("Tri Energy Battery Pvt Ltd status updated to Under Setup").build(),
                SubsidiariesPageDto.SubsidiaryUpdateItemDto.builder().time("15 Sep 2026, 04:15 PM").description("Goeazymart E-Commerce Pvt Ltd trade volume crossed ₹ 2 Cr").build(),
                SubsidiariesPageDto.SubsidiaryUpdateItemDto.builder().time("14 Sep 2026, 10:30 AM").description("Farm Vantara AgriTech Pvt Ltd added 12 new members").build(),
                SubsidiariesPageDto.SubsidiaryUpdateItemDto.builder().time("12 Sep 2026, 05:45 PM").description("Atirath Technologies Pvt Ltd launched new trading segment").build(),
                SubsidiariesPageDto.SubsidiaryUpdateItemDto.builder().time("11 Sep 2026, 09:20 AM").description("Atirath Holdings India Limited completed quarterly settlement").build()
        );

        return SubsidiariesPageDto.builder()
                .adminUser(adminHeader)
                .pageTitle("Subsidiary Companies")
                .pageSubtitle("Manage and monitor all subsidiary companies under the group.")
                .kpis(kpis)
                .subsidiaries(filtered)
                .totalCompanies(filtered.size())
                .currentPage(1)
                .totalPages(1)
                .sectorDistribution(sectorDistribution)
                .recentUpdates(updates)
                .build();
    }

    public SubsidiarySummaryDto addSubsidiary(AddSubsidiaryRequestDto req) {
        int nextNum = subsidiaryCounter.incrementAndGet();
        String id = "sub-" + nextNum;
        String initials = req.getCompanyName().length() >= 2 ?
                req.getCompanyName().substring(0, 2).toUpperCase() : "SU";

        SubsidiarySummaryDto newSub = SubsidiarySummaryDto.builder()
                .id(id)
                .number(nextNum)
                .companyName(req.getCompanyName())
                .initials(initials)
                .subIndustry(req.getSubIndustry() != null ? req.getSubIndustry() : "General Business")
                .sector(req.getSector() != null ? req.getSector() : "Diversified")
                .incorporationDate(req.getIncorporationDate() != null ? req.getIncorporationDate() : "16 Sep 2026")
                .cin(req.getCin() != null ? req.getCin() : "U" + System.currentTimeMillis() + "TG2026PTC" + nextNum)
                .headquarters(req.getHeadquarters() != null ? req.getHeadquarters() : "Hyderabad, TG")
                .status(req.getStatus() != null ? req.getStatus() : "Under Setup")
                .totalMembers(req.getInitialMembers() != null ? req.getInitialMembers() : 10)
                .totalTradeValue(req.getInitialTradeValue() != null ? req.getInitialTradeValue() : "₹ 0.5 Cr")
                .build();

        subsidiaries.add(newSub);
        SubsidiaryDetailDto detail = createDefaultDetailFor(newSub);
        subsidiaryDetailsMap.put(id, detail);
        return newSub;
    }

    public Map<String, String> exportSubsidiaries() {
        return Map.of(
                "status", "READY",
                "fileName", "subsidiary_companies_report_2026-09-16.xlsx",
                "downloadUrl", "https://atirathgroup.com/downloads/subsidiaries-export-16Sep2026.xlsx"
        );
    }

    // ==========================================
    // 3. SUBSIDIARY DETAIL VIEW
    // ==========================================
    public SubsidiaryDetailDto getSubsidiaryDetail(String id) {
        return getSubsidiaryDetail(id, null);
    }

    public SubsidiaryDetailDto getSubsidiaryDetail(String id, String tab) {
        if (id == null || id.isBlank()) {
            id = "sub-1";
        }
        String cleanId = id.trim();
        SubsidiaryDetailDto detail = subsidiaryDetailsMap.get(cleanId);
        if (detail == null) {
            SubsidiarySummaryDto found = findSubsidiarySummary(cleanId);
            if (found != null) {
                detail = subsidiaryDetailsMap.computeIfAbsent(found.getId(), k -> createDefaultDetailFor(found));
            } else if (!subsidiaryDetailsMap.isEmpty()) {
                detail = subsidiaryDetailsMap.values().iterator().next();
            } else {
                detail = createDefaultDetailFor(subsidiaries.get(0));
            }
        }
        if (tab != null && !tab.isBlank()) {
            detail.setActiveTab(tab);
        }
        return detail;
    }

    private SubsidiarySummaryDto findSubsidiarySummary(String identifier) {
        if (identifier == null) return subsidiaries.isEmpty() ? null : subsidiaries.get(0);
        String lower = identifier.trim().toLowerCase();
        return subsidiaries.stream()
                .filter(s -> s.getId().equalsIgnoreCase(identifier)
                        || String.valueOf(s.getNumber()).equals(identifier)
                        || s.getCin().equalsIgnoreCase(identifier)
                        || s.getCompanyName().toLowerCase().contains(lower)
                        || s.getCompanyName().toLowerCase().replace(" ", "-").contains(lower))
                .findFirst()
                .orElse(subsidiaries.isEmpty() ? null : subsidiaries.get(0));
    }

    public SubsidiaryDetailDto updateSubsidiaryInfo(String id, UpdateCompanyInfoDto req) {
        SubsidiaryDetailDto detail = getSubsidiaryDetail(id);
        if (req.getRegisteredAddress() != null) detail.getCompanyInfo().setRegisteredAddress(req.getRegisteredAddress());
        if (req.getHeadquarters() != null) {
            detail.getCompanyInfo().setHeadquarters(req.getHeadquarters());
            detail.setHeadquarters(req.getHeadquarters());
            subsidiaries.stream().filter(s -> s.getId().equals(detail.getId())).findFirst().ifPresent(s -> s.setHeadquarters(req.getHeadquarters()));
        }
        if (req.getPan() != null) detail.getCompanyInfo().setPan(req.getPan());
        if (req.getGstNumber() != null) detail.getCompanyInfo().setGstNumber(req.getGstNumber());
        if (req.getStatus() != null) {
            detail.getCompanyInfo().setStatus(req.getStatus());
            detail.setStatus(req.getStatus());
            subsidiaries.stream().filter(s -> s.getId().equals(detail.getId())).findFirst().ifPresent(s -> s.setStatus(req.getStatus()));
        }
        return detail;
    }

    public SubsidiaryDetailDto updateSubsidiaryStatus(String id, String newStatus) {
        SubsidiaryDetailDto detail = getSubsidiaryDetail(id);
        detail.setStatus(newStatus);
        detail.getCompanyInfo().setStatus(newStatus);
        subsidiaries.stream().filter(s -> s.getId().equals(detail.getId())).findFirst().ifPresent(s -> s.setStatus(newStatus));
        return detail;
    }

    public boolean deleteSubsidiary(String id) {
        SubsidiarySummaryDto found = findSubsidiarySummary(id);
        if (found != null) {
            subsidiaries.removeIf(s -> s.getId().equals(found.getId()));
            subsidiaryDetailsMap.remove(found.getId());
            return true;
        }
        return false;
    }

    public SubsidiaryDetailDto.FinancialsTabDto getSubsidiaryFinancials(String id) {
        return getSubsidiaryDetail(id).getFinancials();
    }

    public SubsidiaryDetailDto.ShareholdingTabDto getSubsidiaryShareholding(String id) {
        return getSubsidiaryDetail(id).getShareholding();
    }

    public SubsidiaryDetailDto.MembersTabDto getSubsidiaryMembers(String id) {
        return getSubsidiaryDetail(id).getMembers();
    }

    public SubsidiaryDetailDto.TradingActivityTabDto getSubsidiaryTradingActivity(String id) {
        return getSubsidiaryDetail(id).getTradingActivity();
    }

    public SubsidiaryDetailDto.ComplianceTabDto getSubsidiaryCompliance(String id) {
        return getSubsidiaryDetail(id).getCompliance();
    }

    public SubsidiaryDetailDto.DocumentsTabDto getSubsidiaryDocuments(String id) {
        return getSubsidiaryDetail(id).getDocuments();
    }

    public SubsidiaryDetailDto.SettingsTabDto getSubsidiarySettings(String id) {
        return getSubsidiaryDetail(id).getSettings();
    }

    public SubsidiaryDetailDto addSubsidiaryActivity(String id, SubsidiaryDetailDto.SubsidiaryActivityDto activity) {
        SubsidiaryDetailDto detail = getSubsidiaryDetail(id);
        if (detail.getRecentActivities() != null) {
            List<SubsidiaryDetailDto.SubsidiaryActivityDto> list = new ArrayList<>(detail.getRecentActivities());
            list.add(0, activity);
            detail.setRecentActivities(list);
        }
        return detail;
    }

    // ==========================================
    // 4. GROUP SHARE CAPITAL PAGE
    // ==========================================
    public GroupShareCapitalPageDto getShareCapitalPage(String query, String sectorFilter) {
        List<KpiCardDto> kpis = List.of(
                KpiCardDto.builder().title("Total Authorized Capital").value("₹ 500.0 Cr").change("").comparisonText("").trend("neutral").icon("database").color("blue").build(),
                KpiCardDto.builder().title("Total Issued Capital").value("₹ 350.0 Cr").change("70.0% of authorized").comparisonText("").trend("positive").icon("wallet").color("orange").build(),
                KpiCardDto.builder().title("Total Paid-up Capital").value("₹ 324.5 Cr").change("92.7% of issued").comparisonText("").trend("positive").icon("trending-up").color("green").build(),
                KpiCardDto.builder().title("Total Shareholders").value("1,248").change("+12%").comparisonText("vs last year").trend("positive").icon("users").color("purple").build()
        );

        GroupShareCapitalPageDto.ShareCapitalStructureDto structure = GroupShareCapitalPageDto.ShareCapitalStructureDto.builder()
                .totalAuthorizedCapital("₹ 500.0 Cr")
                .slices(List.of(
                        GroupShareCapitalPageDto.CapitalStructureSliceDto.builder().company("Atirath Traders").percentage(32.0).color("#3b82f6").build(),
                        GroupShareCapitalPageDto.CapitalStructureSliceDto.builder().company("Atirath Holdings").percentage(24.0).color("#22c55e").build(),
                        GroupShareCapitalPageDto.CapitalStructureSliceDto.builder().company("Atirath Technologies").percentage(16.6).color("#f87171").build(),
                        GroupShareCapitalPageDto.CapitalStructureSliceDto.builder().company("Farm Vantara").percentage(12.0).color("#a855f7").build(),
                        GroupShareCapitalPageDto.CapitalStructureSliceDto.builder().company("Goeazymart").percentage(9.0).color("#f59e0b").build(),
                        GroupShareCapitalPageDto.CapitalStructureSliceDto.builder().company("Tri Energy Battery").percentage(7.0).color("#06b6d4").build()
                ))
                .build();

        GroupShareCapitalPageDto.CapitalComparisonChartDto comparison = GroupShareCapitalPageDto.CapitalComparisonChartDto.builder()
                .authorizedCapitalCr(500.0)
                .issuedCapitalCr(350.0)
                .paidUpCapitalCr(324.5)
                .build();

        List<GroupShareCapitalPageDto.SubsidiaryShareCapitalItemDto> filtered = shareCapitalItems.stream()
                .filter(item -> {
                    if (query != null && !query.isBlank()) {
                        String q = query.toLowerCase();
                        if (!item.getCompanyName().toLowerCase().contains(q) && !item.getSector().toLowerCase().contains(q)) {
                            return false;
                        }
                    }
                    if (sectorFilter != null && !sectorFilter.isBlank() && !sectorFilter.equalsIgnoreCase("All Sectors")) {
                        if (!item.getSector().equalsIgnoreCase(sectorFilter)) return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());

        List<GroupShareCapitalPageDto.KeyInsightItemDto> insights = List.of(
                GroupShareCapitalPageDto.KeyInsightItemDto.builder().metric("70.0%").label("Overall issued capital utilization").icon("wallet").theme("blue").build(),
                GroupShareCapitalPageDto.KeyInsightItemDto.builder().metric("92.7%").label("Overall paid-up capital ratio").icon("shield").theme("green").build(),
                GroupShareCapitalPageDto.KeyInsightItemDto.builder().metric("1,248").label("Total shareholders across group").icon("users").theme("purple").build(),
                GroupShareCapitalPageDto.KeyInsightItemDto.builder().metric("6").label("Active subsidiaries").icon("briefcase").theme("orange").build()
        );

        return GroupShareCapitalPageDto.builder()
                .adminUser(adminHeader)
                .pageTitle("Group Share Capital")
                .pageSubtitle("View and manage the authorized, issued and paid-up share capital across the group companies.")
                .dateFilter("Today, 16 Sep 2026")
                .kpis(kpis)
                .shareCapitalStructure(structure)
                .capitalComparisonChart(comparison)
                .shareCapitalDetails(filtered)
                .recentActivities(shareCapitalActivities)
                .keyInsights(insights)
                .build();
    }

    public Map<String, String> exportShareCapital() {
        return Map.of(
                "status", "READY",
                "fileName", "group_share_capital_report_2026-09-16.pdf",
                "downloadUrl", "https://atirathgroup.com/downloads/group-share-capital-16Sep2026.pdf"
        );
    }

    // ==========================================
    // 5. GROUP SHAREHOLDERS PAGE
    // ==========================================
    public GroupShareholdersPageDto getShareholdersPage(String query, String categoryFilter, String companyFilter) {
        return getShareholdersPage(query, categoryFilter, companyFilter, 1, 10);
    }

    public GroupShareholdersPageDto getShareholdersPage(String query, String categoryFilter, String companyFilter, Integer page, Integer size) {
        int pageNum = page != null && page > 0 ? page : 1;
        int pageSize = size != null && size > 0 ? size : 10;

        List<KpiCardDto> kpis = List.of(
                KpiCardDto.builder().title("Total Shareholders").value("1,248").change("+12% vs last year").comparisonText("").trend("positive").icon("users").color("purple").build(),
                KpiCardDto.builder().title("Individual Shareholders").value("892").change("71.5% of total").comparisonText("").trend("neutral").icon("user").color("blue").build(),
                KpiCardDto.builder().title("Institutional Shareholders").value("256").change("20.5% of total").comparisonText("").trend("neutral").icon("building").color("indigo").build(),
                KpiCardDto.builder().title("Promoter Shareholders").value("48").change("3.8% of total").comparisonText("").trend("neutral").icon("shield").color("green").build(),
                KpiCardDto.builder().title("Foreign Shareholders").value("52").change("4.2% of total").comparisonText("").trend("neutral").icon("globe").color("red").build()
        );

        GroupShareholdersPageDto.ShareholderCategoryDistributionDto catDist = GroupShareholdersPageDto.ShareholderCategoryDistributionDto.builder()
                .totalShareholders("1,248")
                .slices(List.of(
                        GroupShareholdersPageDto.CategorySliceDto.builder().category("Individual").percentage(71.5).count(892).color("#3b82f6").build(),
                        GroupShareholdersPageDto.CategorySliceDto.builder().category("Institutional").percentage(20.5).count(256).color("#f87171").build(),
                        GroupShareholdersPageDto.CategorySliceDto.builder().category("Promoter").percentage(3.8).count(48).color("#22c55e").build(),
                        GroupShareholdersPageDto.CategorySliceDto.builder().category("Foreign").percentage(4.2).count(52).color("#f59e0b").build()
                ))
                .build();

        List<GroupShareholdersPageDto.TopShareholderBarDto> topShareholders = List.of(
                GroupShareholdersPageDto.TopShareholderBarDto.builder().shareholderName("Atirath Holdings (Promoter)").category("Promoter").percentage(24.0).color("#3b82f6").build(),
                GroupShareholdersPageDto.TopShareholderBarDto.builder().shareholderName("Ramesh Kumar (Individual)").category("Individual").percentage(8.5).color("#2dd4bf").build(),
                GroupShareholdersPageDto.TopShareholderBarDto.builder().shareholderName("Global Renewables Ltd (Institutional)").category("Institutional").percentage(6.2).color("#a855f7").build(),
                GroupShareholdersPageDto.TopShareholderBarDto.builder().shareholderName("Priya Verma (Individual)").category("Individual").percentage(5.8).color("#fb923c").build(),
                GroupShareholdersPageDto.TopShareholderBarDto.builder().shareholderName("Sunrise Capital (Foreign)").category("Foreign").percentage(4.6).color("#ec4899").build(),
                GroupShareholdersPageDto.TopShareholderBarDto.builder().shareholderName("Vikram Shah (Individual)").category("Individual").percentage(3.9).color("#38bdf8").build(),
                GroupShareholdersPageDto.TopShareholderBarDto.builder().shareholderName("Sustainable Growth Fund (Institutional)").category("Institutional").percentage(3.4).color("#6366f1").build(),
                GroupShareholdersPageDto.TopShareholderBarDto.builder().shareholderName("Meera Reddy (Individual)").category("Individual").percentage(3.1).color("#34d399").build(),
                GroupShareholdersPageDto.TopShareholderBarDto.builder().shareholderName("Atirath Family Trust (Promoter)").category("Promoter").percentage(2.8).color("#fbbf24").build(),
                GroupShareholdersPageDto.TopShareholderBarDto.builder().shareholderName("Others").category("Various").percentage(41.7).color("#cbd5e1").build()
        );

        List<GroupShareholdersPageDto.ShareholderItemDto> filtered = shareholders.stream()
                .filter(s -> {
                    if (query != null && !query.isBlank()) {
                        String q = query.toLowerCase();
                        if (!s.getShareholderName().toLowerCase().contains(q) &&
                            !s.getCategory().toLowerCase().contains(q) &&
                            !s.getCountry().toLowerCase().contains(q)) {
                            return false;
                        }
                    }
                    if (categoryFilter != null && !categoryFilter.isBlank() && !categoryFilter.equalsIgnoreCase("All Categories")) {
                        if (!s.getCategory().equalsIgnoreCase(categoryFilter)) return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());

        int totalCount = filtered.size();
        int totalPages = (int) Math.ceil((double) 1248 / pageSize);

        return GroupShareholdersPageDto.builder()
                .adminUser(adminHeader)
                .pageTitle("Group Shareholders")
                .pageSubtitle("View and manage all shareholders across the group companies.")
                .dateFilter("Today, 16 Sep 2026")
                .kpis(kpis)
                .categoryDistribution(catDist)
                .topShareholders(topShareholders)
                .shareholders(filtered)
                .totalShareholders(1248)
                .currentPage(pageNum)
                .totalPages(totalPages > 0 ? totalPages : 125)
                .build();
    }

    public GroupShareholdersPageDto.ShareholderItemDto addShareholder(AddShareholderRequestDto req) {
        int nextNum = shareholderCounter.incrementAndGet();
        String initials = req.getShareholderName().length() >= 2 ?
                req.getShareholderName().substring(0, 2).toUpperCase() : "SH";

        GroupShareholdersPageDto.ShareholderItemDto item = GroupShareholdersPageDto.ShareholderItemDto.builder()
                .id("sh-" + nextNum)
                .number(nextNum)
                .shareholderName(req.getShareholderName())
                .initials(initials)
                .type(req.getType() != null ? req.getType() : "Individual")
                .category(req.getCategory() != null ? req.getCategory() : "Individual")
                .categoryTheme("blue")
                .country(req.getCountry() != null ? req.getCountry() : "India")
                .countryFlag("🇮🇳")
                .noOfShares(req.getNoOfShares() != null ? req.getNoOfShares() : "1,00,000")
                .shareholdingPercent(req.getShareholdingPercent() != null ? req.getShareholdingPercent() : 0.1)
                .totalValue(req.getTotalValue() != null ? req.getTotalValue() : "₹ 50.0 L")
                .kycStatus("Verified")
                .status("Active")
                .build();

        shareholders.add(0, item);
        return item;
    }

    public Map<String, String> exportShareholders() {
        return Map.of(
                "status", "READY",
                "fileName", "group_shareholders_report_2026-09-16.xlsx",
                "downloadUrl", "https://atirathgroup.com/downloads/shareholders-export-16Sep2026.xlsx"
        );
    }

    // ==========================================
    // 6. GROUP HOLDINGS PAGE
    // ==========================================
    public GroupHoldingsPageDto getGroupHoldingsPage(String query, String sectorFilter) {
        List<KpiCardDto> kpis = List.of(
                KpiCardDto.builder().title("Total Group Holdings Value").value("₹ 1,248.5 Cr").change("+18% vs last quarter").comparisonText("").trend("positive").icon("building").color("blue").build(),
                KpiCardDto.builder().title("Total Shares Held").value("41.7 Crore").change("+12% vs last quarter").comparisonText("").trend("positive").icon("pie-chart").color("green").build(),
                KpiCardDto.builder().title("Companies in Group").value("6").change("Across 6 sectors").comparisonText("").trend("neutral").icon("users").color("purple").build(),
                KpiCardDto.builder().title("Total Shareholders").value("1,248").change("+12% vs last year").comparisonText("").trend("positive").icon("user").color("blue").build()
        );

        List<GroupHoldingsPageDto.CompanyHoldingBarDto> companyBars = List.of(
                GroupHoldingsPageDto.CompanyHoldingBarDto.builder().company("Atirath Traders").valueCr(312.5).color("#3b82f6").build(),
                GroupHoldingsPageDto.CompanyHoldingBarDto.builder().company("Atirath Holdings").valueCr(248.0).color("#22c55e").build(),
                GroupHoldingsPageDto.CompanyHoldingBarDto.builder().company("Atirath Technologies").valueCr(186.4).color("#f87171").build(),
                GroupHoldingsPageDto.CompanyHoldingBarDto.builder().company("Farm Vantara").valueCr(142.6).color("#a855f7").build(),
                GroupHoldingsPageDto.CompanyHoldingBarDto.builder().company("Goeazymart").valueCr(98.5).color("#f59e0b").build(),
                GroupHoldingsPageDto.CompanyHoldingBarDto.builder().company("Tri Energy Battery").valueCr(76.5).color("#06b6d4").build()
        );

        GroupHoldingsPageDto.SectorHoldingsDistributionDto sectorDist = GroupHoldingsPageDto.SectorHoldingsDistributionDto.builder()
                .totalValue("₹ 1,248.5 Cr")
                .slices(List.of(
                        GroupHoldingsPageDto.SectorSliceDto.builder().sector("Trading").percentage(25.0).valueFormatted("₹ 312.5 Cr").color("#3b82f6").build(),
                        GroupHoldingsPageDto.SectorSliceDto.builder().sector("Renewable Energy").percentage(19.9).valueFormatted("₹ 248.0 Cr").color("#22c55e").build(),
                        GroupHoldingsPageDto.SectorSliceDto.builder().sector("Technology").percentage(14.9).valueFormatted("₹ 186.4 Cr").color("#f87171").build(),
                        GroupHoldingsPageDto.SectorSliceDto.builder().sector("Agriculture").percentage(11.4).valueFormatted("₹ 142.6 Cr").color("#a855f7").build(),
                        GroupHoldingsPageDto.SectorSliceDto.builder().sector("Retail").percentage(7.9).valueFormatted("₹ 98.5 Cr").color("#f59e0b").build(),
                        GroupHoldingsPageDto.SectorSliceDto.builder().sector("Energy Storage").percentage(6.1).valueFormatted("₹ 76.5 Cr").color("#06b6d4").build(),
                        GroupHoldingsPageDto.SectorSliceDto.builder().sector("Others").percentage(14.8).valueFormatted("₹ 184.0 Cr").color("#94a3b8").build()
                ))
                .build();

        List<GroupHoldingsPageDto.GroupHoldingItemDto> filtered = groupHoldings.stream()
                .filter(h -> {
                    if (query != null && !query.isBlank()) {
                        String q = query.toLowerCase();
                        if (!h.getCompanyName().toLowerCase().contains(q) && !h.getSector().toLowerCase().contains(q)) return false;
                    }
                    if (sectorFilter != null && !sectorFilter.isBlank() && !sectorFilter.equalsIgnoreCase("All Sectors")) {
                        if (!h.getSector().equalsIgnoreCase(sectorFilter)) return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());

        List<GroupHoldingsPageDto.HoldingActivityItemDto> activities = List.of(
                GroupHoldingsPageDto.HoldingActivityItemDto.builder().dateTime("16 Sep 2026, 11:20 AM").company("Atirath Traders").activity("Share Transfer").activityTheme("teal").details("50,000 shares transferred").performedBy("Admin User").build(),
                GroupHoldingsPageDto.HoldingActivityItemDto.builder().dateTime("15 Sep 2026, 04:15 PM").company("Atirath Holdings").activity("Share Allotment").activityTheme("blue").details("2,00,000 shares allotted").performedBy("Compliance Team").build(),
                GroupHoldingsPageDto.HoldingActivityItemDto.builder().dateTime("14 Sep 2026, 10:30 AM").company("Farm Vantara").activity("Share Buyback").activityTheme("orange").details("1,00,000 shares bought back").performedBy("Finance Team").build(),
                GroupHoldingsPageDto.HoldingActivityItemDto.builder().dateTime("12 Sep 2026, 05:45 PM").company("Goeazymart").activity("Share Pledge").activityTheme("pink").details("75,000 shares pledged").performedBy("Admin User").build(),
                GroupHoldingsPageDto.HoldingActivityItemDto.builder().dateTime("11 Sep 2026, 09:20 AM").company("Atirath Technologies").activity("Share Transfer").activityTheme("teal").details("25,000 shares transferred").performedBy("Operations Team").build()
        );

        List<GroupHoldingsPageDto.HoldingInsightItemDto> insights = List.of(
                GroupHoldingsPageDto.HoldingInsightItemDto.builder().title("Concentration Risk").description("Top 2 companies account for 44.9% of total holdings").icon("shield").theme("green").build(),
                GroupHoldingsPageDto.HoldingInsightItemDto.builder().title("Sector Diversification").description("Holdings across 6 different sectors").icon("layers").theme("blue").build(),
                GroupHoldingsPageDto.HoldingInsightItemDto.builder().title("Growth Trend").description("Total holdings value increased by 18% this quarter").icon("trending-up").theme("purple").build(),
                GroupHoldingsPageDto.HoldingInsightItemDto.builder().title("Liquidity Position").description("72.3% of holdings are liquid and tradable").icon("droplet").theme("yellow").build(),
                GroupHoldingsPageDto.HoldingInsightItemDto.builder().title("New Opportunities").description("Consider increasing exposure in Renewable Energy sector").icon("zap").theme("red").build()
        );

        return GroupHoldingsPageDto.builder()
                .adminUser(adminHeader)
                .pageTitle("Group Holdings")
                .pageSubtitle("View and analyze shareholding structure and holdings across all group companies.")
                .dateFilter("Today, 16 Sep 2026")
                .kpis(kpis)
                .holdingsByCompany(companyBars)
                .sectorHoldings(sectorDist)
                .holdingsDetails(filtered)
                .recentActivities(activities)
                .keyInsights(insights)
                .build();
    }

    public Map<String, String> exportHoldings() {
        return Map.of(
                "status", "READY",
                "fileName", "group_holdings_valuation_report_2026-09-16.xlsx",
                "downloadUrl", "https://atirathgroup.com/downloads/group-holdings-16Sep2026.xlsx"
        );
    }

    // ==========================================
    // 7. MARKET ACTIVITY (GROUP LEVEL) PAGE
    // ==========================================
    public MarketActivityGroupPageDto getMarketActivityPage(String query, String sectorFilter, String companyFilter) {
        return getMarketActivityPage(query, sectorFilter, companyFilter, 1, 6, "Today, 16 Sep 2026");
    }

    public MarketActivityGroupPageDto getMarketActivityPage(String query, String sectorFilter, String companyFilter, Integer page, Integer size, String dateRange) {
        int pageNum = page != null && page > 0 ? page : 1;
        int pageSize = size != null && size > 0 ? size : 6;
        String effectiveDate = (dateRange != null && !dateRange.isBlank()) ? dateRange : "Today, 16 Sep 2026";

        List<KpiCardDto> kpis = List.of(
                KpiCardDto.builder().title("Total Trade Volume").value("₹ 142.6 Cr").change("+18% vs last quarter").comparisonText("").trend("positive").icon("bar-chart").color("blue").build(),
                KpiCardDto.builder().title("Total Trades").value("1,248").change("+12% vs last quarter").comparisonText("").trend("positive").icon("wallet").color("orange").build(),
                KpiCardDto.builder().title("Active Members").value("842").change("+8% vs last quarter").comparisonText("").trend("positive").icon("users").color("purple").build(),
                KpiCardDto.builder().title("Companies Traded").value("6").change("Across all sectors").comparisonText("").trend("neutral").icon("building").color("purple").build(),
                KpiCardDto.builder().title("Avg. Trade Value").value("₹ 11.4 L").change("+5% vs last quarter").comparisonText("").trend("positive").icon("trending-up").color("green").build()
        );

        MarketActivityGroupPageDto.TradeVolumeTrendChartDto volumeTrend = MarketActivityGroupPageDto.TradeVolumeTrendChartDto.builder()
                .filterRange("Last 6 Months")
                .monthlyTrends(List.of(
                        new MarketActivityGroupPageDto.MonthlyTrendPointDto("Apr", 18.2, 180),
                        new MarketActivityGroupPageDto.MonthlyTrendPointDto("May", 22.5, 240),
                        new MarketActivityGroupPageDto.MonthlyTrendPointDto("Jun", 24.8, 290),
                        new MarketActivityGroupPageDto.MonthlyTrendPointDto("Jul", 31.6, 360),
                        new MarketActivityGroupPageDto.MonthlyTrendPointDto("Aug", 28.4, 310),
                        new MarketActivityGroupPageDto.MonthlyTrendPointDto("Sep", 36.2, 420)
                ))
                .build();

        MarketActivityGroupPageDto.SectorTradeDistributionDto sectorDist = MarketActivityGroupPageDto.SectorTradeDistributionDto.builder()
                .totalTradeValue("₹ 142.6 Cr")
                .slices(List.of(
                        MarketActivityGroupPageDto.SectorTradeSliceDto.builder().sector("Trading").percentage(33.2).valueFormatted("₹ 47.4 Cr").color("#3b82f6").build(),
                        MarketActivityGroupPageDto.SectorTradeSliceDto.builder().sector("Renewable Energy").percentage(21.5).valueFormatted("₹ 30.6 Cr").color("#22c55e").build(),
                        MarketActivityGroupPageDto.SectorTradeSliceDto.builder().sector("Technology").percentage(16.8).valueFormatted("₹ 23.9 Cr").color("#f87171").build(),
                        MarketActivityGroupPageDto.SectorTradeSliceDto.builder().sector("Agriculture").percentage(11.2).valueFormatted("₹ 16.0 Cr").color("#a855f7").build(),
                        MarketActivityGroupPageDto.SectorTradeSliceDto.builder().sector("Retail").percentage(8.4).valueFormatted("₹ 12.0 Cr").color("#f59e0b").build(),
                        MarketActivityGroupPageDto.SectorTradeSliceDto.builder().sector("Energy Storage").percentage(6.3).valueFormatted("₹ 9.0 Cr").color("#06b6d4").build(),
                        MarketActivityGroupPageDto.SectorTradeSliceDto.builder().sector("Others").percentage(2.6).valueFormatted("₹ 3.7 Cr").color("#94a3b8").build()
                ))
                .build();

        List<MarketActivityGroupPageDto.MarketActivityTradeDto> filtered = marketActivityTrades.stream()
                .filter(t -> {
                    if (query != null && !query.isBlank()) {
                        String q = query.toLowerCase();
                        if (!t.getCompany().toLowerCase().contains(q) &&
                            !t.getBuyer().toLowerCase().contains(q) &&
                            !t.getSeller().toLowerCase().contains(q)) {
                            return false;
                        }
                    }
                    if (companyFilter != null && !companyFilter.isBlank() && !companyFilter.equalsIgnoreCase("All Companies")) {
                        if (!t.getCompany().equalsIgnoreCase(companyFilter)) return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());

        List<MarketActivityGroupPageDto.TopTradedCompanyBarDto> topCompanies = List.of(
                MarketActivityGroupPageDto.TopTradedCompanyBarDto.builder().rank(1).company("Atirath Traders").initials("AT").tradeValue("47.4 Cr").valueCr(47.4).color("#3b82f6").build(),
                MarketActivityGroupPageDto.TopTradedCompanyBarDto.builder().rank(2).company("Atirath Holdings").initials("AH").tradeValue("30.6 Cr").valueCr(30.6).color("#22c55e").build(),
                MarketActivityGroupPageDto.TopTradedCompanyBarDto.builder().rank(3).company("Atirath Technologies").initials("AT").tradeValue("23.9 Cr").valueCr(23.9).color("#f87171").build(),
                MarketActivityGroupPageDto.TopTradedCompanyBarDto.builder().rank(4).company("Farm Vantara").initials("FV").tradeValue("16.0 Cr").valueCr(16.0).color("#a855f7").build(),
                MarketActivityGroupPageDto.TopTradedCompanyBarDto.builder().rank(5).company("Goeazymart").initials("GE").tradeValue("12.0 Cr").valueCr(12.0).color("#f59e0b").build()
        );

        List<MarketActivityGroupPageDto.MarketHighlightItemDto> highlights = List.of(
                MarketActivityGroupPageDto.MarketHighlightItemDto.builder().description("Trade volume increased by 18% compared to last quarter").icon("trending-up").theme("green").build(),
                MarketActivityGroupPageDto.MarketHighlightItemDto.builder().description("Atirath Traders accounts for 33.2% of total trade value").icon("pie-chart").theme("blue").build(),
                MarketActivityGroupPageDto.MarketHighlightItemDto.builder().description("1,248 total trades executed across 6 companies").icon("refresh-cw").theme("purple").build(),
                MarketActivityGroupPageDto.MarketHighlightItemDto.builder().description("Average settlement time: 1.2 days").icon("clock").theme("cyan").build(),
                MarketActivityGroupPageDto.MarketHighlightItemDto.builder().description("3 trades pending approval").icon("alert-triangle").theme("orange").build(),
                MarketActivityGroupPageDto.MarketHighlightItemDto.builder().description("No unusual market activity detected").icon("shield").theme("blue").build()
        );

        return MarketActivityGroupPageDto.builder()
                .adminUser(adminHeader)
                .pageTitle("Market Activity (Group Level)")
                .pageSubtitle("Monitor and analyze trading activity across all group companies in real-time.")
                .dateFilter(effectiveDate)
                .kpis(kpis)
                .tradeVolumeTrend(volumeTrend)
                .sectorTradeDistribution(sectorDist)
                .recentTrades(filtered)
                .totalTrades(1248)
                .currentPage(pageNum)
                .totalPages(125)
                .topTradedCompanies(topCompanies)
                .highlights(highlights)
                .build();
    }

    public Map<String, String> exportMarketActivity() {
        return Map.of(
                "status", "READY",
                "fileName", "group_market_activity_report_2026-09-16.csv",
                "downloadUrl", "https://atirathgroup.com/downloads/market-activity-16Sep2026.csv"
        );
    }

    // ==========================================
    // 8. GROUP TRADE MONITORING PAGE
    // ==========================================
    public GroupTradeMonitoringPageDto getTradeMonitoringPage(String query, String companyFilter, String sectorFilter, String typeFilter) {
        return getTradeMonitoringPage(query, companyFilter, sectorFilter, typeFilter, 1, 8, "01 Sep 2026 - 16 Sep 2026");
    }

    public GroupTradeMonitoringPageDto getTradeMonitoringPage(String query, String companyFilter, String sectorFilter, String typeFilter, Integer page, Integer size, String dateRange) {
        int pageNum = page != null && page > 0 ? page : 1;
        int pageSize = size != null && size > 0 ? size : 8;
        String effectiveDateRange = (dateRange != null && !dateRange.isBlank()) ? dateRange : "01 Sep 2026 - 16 Sep 2026";

        List<KpiCardDto> kpis = List.of(
                KpiCardDto.builder().title("Total Trades").value("1,248").change("+12% vs last quarter").comparisonText("").trend("positive").icon("refresh-cw").color("blue").build(),
                KpiCardDto.builder().title("Total Trade Value").value("₹ 142.6 Cr").change("+18% vs last quarter").comparisonText("").trend("positive").icon("rupee").color("orange").build(),
                KpiCardDto.builder().title("Active Traders").value("842").change("+8% vs last quarter").comparisonText("").trend("positive").icon("users").color("purple").build(),
                KpiCardDto.builder().title("Companies Involved").value("6").change("Across all sectors").comparisonText("").trend("neutral").icon("building").color("pink").build(),
                KpiCardDto.builder().title("Avg. Settlement Time").value("1.2 Days").change("-28% vs last quarter").comparisonText("").trend("positive").icon("clock").color("green").build()
        );

        GroupTradeMonitoringPageDto.TradeValueVolumeTrendDto trendChart = GroupTradeMonitoringPageDto.TradeValueVolumeTrendDto.builder()
                .filterRange("Last 6 Months")
                .monthlyTrends(List.of(
                        new GroupTradeMonitoringPageDto.MonthlyMonitoringPointDto("Apr", 18.2, 180),
                        new GroupTradeMonitoringPageDto.MonthlyMonitoringPointDto("May", 22.5, 240),
                        new GroupTradeMonitoringPageDto.MonthlyMonitoringPointDto("Jun", 24.8, 290),
                        new GroupTradeMonitoringPageDto.MonthlyMonitoringPointDto("Jul", 31.6, 360),
                        new GroupTradeMonitoringPageDto.MonthlyMonitoringPointDto("Aug", 28.4, 310),
                        new GroupTradeMonitoringPageDto.MonthlyMonitoringPointDto("Sep", 36.2, 420)
                ))
                .build();

        MarketActivityGroupPageDto.SectorTradeDistributionDto sectorDist = MarketActivityGroupPageDto.SectorTradeDistributionDto.builder()
                .totalTradeValue("₹ 142.6 Cr")
                .slices(List.of(
                        MarketActivityGroupPageDto.SectorTradeSliceDto.builder().sector("Trading").percentage(33.2).valueFormatted("₹ 47.4 Cr").color("#3b82f6").build(),
                        MarketActivityGroupPageDto.SectorTradeSliceDto.builder().sector("Renewable Energy").percentage(21.5).valueFormatted("₹ 30.6 Cr").color("#22c55e").build(),
                        MarketActivityGroupPageDto.SectorTradeSliceDto.builder().sector("Technology").percentage(16.8).valueFormatted("₹ 23.9 Cr").color("#f87171").build(),
                        MarketActivityGroupPageDto.SectorTradeSliceDto.builder().sector("Agriculture").percentage(11.2).valueFormatted("₹ 16.0 Cr").color("#a855f7").build(),
                        MarketActivityGroupPageDto.SectorTradeSliceDto.builder().sector("Retail").percentage(8.4).valueFormatted("₹ 12.0 Cr").color("#f59e0b").build(),
                        MarketActivityGroupPageDto.SectorTradeSliceDto.builder().sector("Energy Storage").percentage(6.3).valueFormatted("₹ 9.0 Cr").color("#06b6d4").build(),
                        MarketActivityGroupPageDto.SectorTradeSliceDto.builder().sector("Others").percentage(2.6).valueFormatted("₹ 3.7 Cr").color("#94a3b8").build()
                ))
                .build();

        List<GroupTradeMonitoringPageDto.TradeMonitoringItemDto> filtered = tradeMonitoringItems.stream()
                .filter(t -> {
                    if (query != null && !query.isBlank()) {
                        String q = query.toLowerCase();
                        if (!t.getTradeId().toLowerCase().contains(q) &&
                            !t.getBuyer().toLowerCase().contains(q) &&
                            !t.getSeller().toLowerCase().contains(q) &&
                            !t.getCompany().toLowerCase().contains(q)) {
                            return false;
                        }
                    }
                    if (companyFilter != null && !companyFilter.isBlank() && !companyFilter.equalsIgnoreCase("All Companies")) {
                        if (!t.getCompany().equalsIgnoreCase(companyFilter)) return false;
                    }
                    if (typeFilter != null && !typeFilter.isBlank() && !typeFilter.equalsIgnoreCase("All Trade Types")) {
                        if (!t.getType().equalsIgnoreCase(typeFilter)) return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());

        List<GroupTradeMonitoringPageDto.MonitoringInsightItemDto> insights = List.of(
                GroupTradeMonitoringPageDto.MonitoringInsightItemDto.builder().description("Trade volume increased by 18% compared to last quarter").icon("trending-up").theme("green").build(),
                GroupTradeMonitoringPageDto.MonitoringInsightItemDto.builder().description("No major market manipulation detected").icon("shield").theme("blue").build(),
                GroupTradeMonitoringPageDto.MonitoringInsightItemDto.builder().description("6 companies actively traded this month").icon("building").theme("purple").build(),
                GroupTradeMonitoringPageDto.MonitoringInsightItemDto.builder().description("Average settlement time improved by 28%").icon("clock").theme("orange").build(),
                GroupTradeMonitoringPageDto.MonitoringInsightItemDto.builder().description("3 unusual trade activities flagged for review").icon("alert-triangle").theme("red").build(),
                GroupTradeMonitoringPageDto.MonitoringInsightItemDto.builder().description("All trades are within regulatory limits").icon("check-circle").theme("blue").build()
        );

        return GroupTradeMonitoringPageDto.builder()
                .adminUser(adminHeader)
                .pageTitle("Group Trade Monitoring")
                .pageSubtitle("Monitor, track and analyze all trades happening across group companies in real-time.")
                .dateRange(effectiveDateRange)
                .kpis(kpis)
                .trendChart(trendChart)
                .sectorDistribution(sectorDist)
                .recentTrades(filtered)
                .totalTrades(1248)
                .currentPage(pageNum)
                .totalPages(156)
                .unusualActivities(unusualTradeActivities)
                .insights(insights)
                .build();
    }

    public Map<String, String> exportTradeMonitoring() {
        return Map.of(
                "status", "READY",
                "fileName", "group_trade_monitoring_report_01_to_16_Sep_2026.pdf",
                "downloadUrl", "https://atirathgroup.com/downloads/trade-monitoring-report-16Sep2026.pdf"
        );
    }

    // ==========================================
    // 9. ESCROW & SETTLEMENT (GROUP LEVEL) PAGE
    // ==========================================
    public EscrowSettlementGroupPageDto getEscrowSettlementPage(String query, String company, String tradeType, String status) {
        return getEscrowSettlementPage(query, company, tradeType, status, "Escrow Transactions", 1, 8, "01 Sep 2026 - 16 Sep 2026");
    }

    public EscrowSettlementGroupPageDto getEscrowSettlementPage(String query, String company, String tradeType, String status, String tab, Integer page, Integer size, String dateRange) {
        int pageNum = page != null && page > 0 ? page : 1;
        int pageSize = size != null && size > 0 ? size : 8;
        String effectiveTab = (tab != null && !tab.isBlank()) ? tab : "Escrow Transactions";
        String effectiveDateRange = (dateRange != null && !dateRange.isBlank()) ? dateRange : "01 Sep 2026 - 16 Sep 2026";

        List<KpiCardDto> kpis = List.of(
                KpiCardDto.builder().title("Total Escrow Balance").value("₹ 58.4 Cr").change("+12% vs last quarter").comparisonText("").trend("positive").icon("building").color("blue").build(),
                KpiCardDto.builder().title("Total Settled Amount").value("₹ 142.6 Cr").change("+18% vs last quarter").comparisonText("").trend("positive").icon("rupee").color("green").build(),
                KpiCardDto.builder().title("Pending Settlement").value("₹ 12.3 Cr").change("-6% vs last quarter").comparisonText("").trend("positive").icon("clock").color("orange").build(),
                KpiCardDto.builder().title("Total Transactions").value("1,248").change("+22% vs last quarter").comparisonText("").trend("positive").icon("file-text").color("purple").build(),
                KpiCardDto.builder().title("Dispute Amount").value("₹ 1.8 Cr").change("-32% vs last quarter").comparisonText("").trend("positive").icon("shield").color("blue").build()
        );

        EscrowSettlementGroupPageDto.EscrowTrendChartDto trend = EscrowSettlementGroupPageDto.EscrowTrendChartDto.builder()
                .filterRange("Last 6 Months")
                .monthlyData(List.of(
                        new EscrowSettlementGroupPageDto.MonthlyEscrowPointDto("Apr", 32.5),
                        new EscrowSettlementGroupPageDto.MonthlyEscrowPointDto("May", 38.2),
                        new EscrowSettlementGroupPageDto.MonthlyEscrowPointDto("Jun", 42.6),
                        new EscrowSettlementGroupPageDto.MonthlyEscrowPointDto("Jul", 48.1),
                        new EscrowSettlementGroupPageDto.MonthlyEscrowPointDto("Aug", 52.3),
                        new EscrowSettlementGroupPageDto.MonthlyEscrowPointDto("Sep", 58.4)
                )).build();

        EscrowSettlementGroupPageDto.SettlementStatusDonutDto statusDonut = EscrowSettlementGroupPageDto.SettlementStatusDonutDto.builder()
                .totalTrades("₹ 142.6 Cr")
                .slices(List.of(
                        EscrowSettlementGroupPageDto.SettlementStatusSliceDto.builder().status("Settled").percentage(72.4).amountFormatted("₹ 103.3 Cr").color("#22c55e").build(),
                        EscrowSettlementGroupPageDto.SettlementStatusSliceDto.builder().status("Pending").percentage(18.1).amountFormatted("₹ 25.8 Cr").color("#f59e0b").build(),
                        EscrowSettlementGroupPageDto.SettlementStatusSliceDto.builder().status("Under Review").percentage(6.2).amountFormatted("₹ 8.8 Cr").color("#3b82f6").build(),
                        EscrowSettlementGroupPageDto.SettlementStatusSliceDto.builder().status("Disputed").percentage(3.3).amountFormatted("₹ 4.7 Cr").color("#ef4444").build()
                )).build();

        List<EscrowSettlementGroupPageDto.EscrowTransactionItemDto> filtered = escrowTransactions.stream()
                .filter(e -> {
                    if (query != null && !query.isBlank()) {
                        String q = query.toLowerCase();
                        if (!e.getTradeId().toLowerCase().contains(q) &&
                            !e.getBuyer().toLowerCase().contains(q) &&
                            !e.getSeller().toLowerCase().contains(q) &&
                            !e.getCompany().toLowerCase().contains(q)) return false;
                    }
                    if (company != null && !company.isBlank() && !company.equalsIgnoreCase("All Companies")) {
                        if (!e.getCompany().equalsIgnoreCase(company)) return false;
                    }
                    if (status != null && !status.isBlank() && !status.equalsIgnoreCase("All Status")) {
                        if (!e.getStatus().equalsIgnoreCase(status)) return false;
                    }
                    return true;
                }).collect(Collectors.toList());

        List<EscrowSettlementGroupPageDto.PendingSettlementSummaryItemDto> summary = List.of(
                EscrowSettlementGroupPageDto.PendingSettlementSummaryItemDto.builder().company("Atirath Holdings").numberOfTrades(12).amount("25,80,000").build(),
                EscrowSettlementGroupPageDto.PendingSettlementSummaryItemDto.builder().company("Atirath Technologies").numberOfTrades(8).amount("18,40,000").build(),
                EscrowSettlementGroupPageDto.PendingSettlementSummaryItemDto.builder().company("Farm Vantara").numberOfTrades(6).amount("12,60,000").build(),
                EscrowSettlementGroupPageDto.PendingSettlementSummaryItemDto.builder().company("Goeazymart").numberOfTrades(5).amount("9,20,000").build(),
                EscrowSettlementGroupPageDto.PendingSettlementSummaryItemDto.builder().company("Tri Energy Battery").numberOfTrades(4).amount("8,10,000").build()
        );

        List<EscrowSettlementGroupPageDto.RecentSettlementActivityItemDto> recentSettlements = List.of(
                EscrowSettlementGroupPageDto.RecentSettlementActivityItemDto.builder().dateTime("16 Sep 2026, 11:20 AM").tradeId("ESC-100482").company("Atirath Traders").amount("1,49,90,000").status("Settled").build(),
                EscrowSettlementGroupPageDto.RecentSettlementActivityItemDto.builder().dateTime("16 Sep 2026, 09:45 AM").tradeId("ESC-100474").company("Farm Vantara").amount("75,60,000").status("Settled").build(),
                EscrowSettlementGroupPageDto.RecentSettlementActivityItemDto.builder().dateTime("15 Sep 2026, 05:30 PM").tradeId("ESC-100471").company("Atirath Holdings").amount("42,80,000").status("Settled").build(),
                EscrowSettlementGroupPageDto.RecentSettlementActivityItemDto.builder().dateTime("15 Sep 2026, 04:10 PM").tradeId("ESC-100468").company("Goeazymart").amount("36,50,000").status("Settled").build(),
                EscrowSettlementGroupPageDto.RecentSettlementActivityItemDto.builder().dateTime("14 Sep 2026, 03:25 PM").tradeId("ESC-100461").company("Atirath Technologies").amount("28,40,000").status("Settled").build()
        );

        List<EscrowSettlementGroupPageDto.SettlementInsightItemDto> insights = List.of(
                EscrowSettlementGroupPageDto.SettlementInsightItemDto.builder().description("72.4% of trades are settled within T+2 days").icon("clock").theme("orange").build(),
                EscrowSettlementGroupPageDto.SettlementInsightItemDto.builder().description("12.3 Cr pending settlement amount").icon("shield").theme("red").build(),
                EscrowSettlementGroupPageDto.SettlementInsightItemDto.builder().description("1.8 Cr under dispute (3.3% of total trades)").icon("alert-triangle").theme("red").build(),
                EscrowSettlementGroupPageDto.SettlementInsightItemDto.builder().description("Average settlement time: 1.2 days").icon("clock").theme("green").build(),
                EscrowSettlementGroupPageDto.SettlementInsightItemDto.builder().description("Settlement success rate: 96.7%").icon("check-circle").theme("green").build()
        );

        return EscrowSettlementGroupPageDto.builder()
                .adminUser(adminHeader)
                .pageTitle("Escrow & Settlement (Group Level)")
                .pageSubtitle("Manage escrow accounts, monitor settlements and track fund flows across all group companies.")
                .dateRange(effectiveDateRange)
                .kpis(kpis)
                .escrowTrend(trend)
                .settlementStatus(statusDonut)
                .tabs(List.of("Escrow Transactions", "Pending Settlements", "Dispute Management", "Escrow Accounts", "Settlement Reports"))
                .activeTab(effectiveTab)
                .transactions(filtered)
                .totalTransactions(1248)
                .currentPage(pageNum)
                .totalPages(156)
                .pendingSettlementSummary(summary)
                .recentSettlementActivity(recentSettlements)
                .keyInsights(insights)
                .build();
    }

    public EscrowSettlementGroupPageDto.EscrowTransactionItemDto addEscrowTransaction(AddEscrowRequestDto req) {
        int nextNum = escrowCounter.incrementAndGet();
        String tradeId = req.getTradeId() != null ? req.getTradeId() : "ESC-1004" + nextNum;

        EscrowSettlementGroupPageDto.EscrowTransactionItemDto item = EscrowSettlementGroupPageDto.EscrowTransactionItemDto.builder()
                .id("esc-" + nextNum)
                .number(nextNum)
                .tradeId(tradeId)
                .dateTime("16 Sep 2026, 12:00 PM")
                .buyer(req.getBuyer() != null ? req.getBuyer() : "New Buyer")
                .seller(req.getSeller() != null ? req.getSeller() : "New Seller")
                .company(req.getCompany() != null ? req.getCompany() : "Atirath Traders")
                .escrowAmount(req.getEscrowAmount() != null ? req.getEscrowAmount() : "50,00,000")
                .tradeValue(req.getTradeValue() != null ? req.getTradeValue() : "50,00,000")
                .settlementDate(req.getSettlementDate() != null ? req.getSettlementDate() : "19 Sep 2026")
                .status("Pending")
                .statusTheme("yellow")
                .build();

        escrowTransactions.add(0, item);
        return item;
    }

    public Map<String, String> exportEscrowSettlement() {
        return Map.of(
                "status", "READY",
                "fileName", "group_escrow_settlement_report_01_to_16_Sep_2026.pdf",
                "downloadUrl", "https://atirathgroup.com/downloads/escrow-settlement-16Sep2026.pdf"
        );
    }

    // ==========================================
    // 10. APPROVALS PAGE
    // ==========================================
    public ApprovalsPageDto getApprovalsPage(String query, String company, String approvalType, String status, String priority) {
        return getApprovalsPage(query, company, approvalType, status, priority, "All", 1, 10, "01 Sep 2026 - 16 Sep 2026");
    }

    public ApprovalsPageDto getApprovalsPage(String query, String company, String approvalType, String status, String priority, String tab, Integer page, Integer size, String dateRange) {
        int pageNum = page != null && page > 0 ? page : 1;
        int pageSize = size != null && size > 0 ? size : 10;
        String effectiveTab = (tab != null && !tab.isBlank()) ? tab : "All";
        String effectiveDateRange = (dateRange != null && !dateRange.isBlank()) ? dateRange : "01 Sep 2026 - 16 Sep 2026";

        List<KpiCardDto> kpis = List.of(
                KpiCardDto.builder().title("Total Requests").value("48").change("+12% vs last week").comparisonText("").trend("positive").icon("file-text").color("purple").build(),
                KpiCardDto.builder().title("Pending").value("21").change("+5% vs last week").comparisonText("").trend("neutral").icon("clock").color("orange").build(),
                KpiCardDto.builder().title("Approved").value("22").change("+37% vs last week").comparisonText("").trend("positive").icon("check-circle").color("green").build(),
                KpiCardDto.builder().title("Rejected").value("3").change("-25% vs last week").comparisonText("").trend("negative").icon("x-circle").color("red").build(),
                KpiCardDto.builder().title("Avg. Approval Time").value("1.8 Days").change("-28% vs last week").comparisonText("").trend("positive").icon("users").color("blue").build()
        );

        List<ApprovalsPageDto.TabCountDto> tabs = List.of(
                new ApprovalsPageDto.TabCountDto("All", "All Approvals", 48),
                new ApprovalsPageDto.TabCountDto("Pending", "Pending", 21),
                new ApprovalsPageDto.TabCountDto("UnderReview", "Under Review", 4),
                new ApprovalsPageDto.TabCountDto("Approved", "Approved", 22),
                new ApprovalsPageDto.TabCountDto("Rejected", "Rejected", 3)
        );

        List<ApprovalsPageDto.ApprovalItemDto> filtered = approvalsList.stream()
                .filter(a -> {
                    if (query != null && !query.isBlank()) {
                        String q = query.toLowerCase();
                        if (!a.getRequestId().toLowerCase().contains(q) &&
                            !a.getCompany().toLowerCase().contains(q) &&
                            !a.getRequestedBy().toLowerCase().contains(q) &&
                            !a.getApprovalType().toLowerCase().contains(q)) return false;
                    }
                    if (company != null && !company.isBlank() && !company.equalsIgnoreCase("All Companies")) {
                        if (!a.getCompany().equalsIgnoreCase(company)) return false;
                    }
                    if (approvalType != null && !approvalType.isBlank() && !approvalType.equalsIgnoreCase("All Approval Types")) {
                        if (!a.getApprovalType().equalsIgnoreCase(approvalType)) return false;
                    }
                    if (status != null && !status.isBlank() && !status.equalsIgnoreCase("All Status")) {
                        if (!a.getStatus().equalsIgnoreCase(status)) return false;
                    }
                    if (priority != null && !priority.isBlank() && !priority.equalsIgnoreCase("All Priority")) {
                        if (!a.getPriority().equalsIgnoreCase(priority)) return false;
                    }
                    if (tab != null && !tab.isBlank() && !tab.equalsIgnoreCase("All") && !tab.equalsIgnoreCase("All Approvals")) {
                        if (tab.equalsIgnoreCase("UnderReview") || tab.equalsIgnoreCase("Under Review")) {
                            if (!a.getStatus().equalsIgnoreCase("Under Review")) return false;
                        } else {
                            if (!a.getStatus().equalsIgnoreCase(tab)) return false;
                        }
                    }
                    return true;
                }).collect(Collectors.toList());

        ApprovalsPageDto.ApprovalsByTypeDonutDto byType = ApprovalsPageDto.ApprovalsByTypeDonutDto.builder()
                .totalRequests("48")
                .slices(List.of(
                        new ApprovalsPageDto.TypeSliceDto("Share Transfer", 10, 20.8, "#3b82f6"),
                        new ApprovalsPageDto.TypeSliceDto("Trade Settlement", 8, 16.7, "#22c55e"),
                        new ApprovalsPageDto.TypeSliceDto("Escrow Release", 7, 14.6, "#f87171"),
                        new ApprovalsPageDto.TypeSliceDto("New Investor", 6, 12.5, "#a855f7"),
                        new ApprovalsPageDto.TypeSliceDto("Capital Increase", 6, 12.5, "#f59e0b"),
                        new ApprovalsPageDto.TypeSliceDto("Dividend Payout", 4, 8.3, "#06b6d4"),
                        new ApprovalsPageDto.TypeSliceDto("Related Party Txn", 4, 8.3, "#ec4899"),
                        new ApprovalsPageDto.TypeSliceDto("Others", 3, 6.3, "#94a3b8")
                )).build();

        ApprovalsPageDto.ApprovalTrendChartDto trend = ApprovalsPageDto.ApprovalTrendChartDto.builder()
                .filterRange("Last 6 Months")
                .monthlyData(List.of(
                        new ApprovalsPageDto.MonthlyApprovalTrendDto("Apr", 14, 8, 2),
                        new ApprovalsPageDto.MonthlyApprovalTrendDto("May", 16, 12, 3),
                        new ApprovalsPageDto.MonthlyApprovalTrendDto("Jun", 18, 15, 2),
                        new ApprovalsPageDto.MonthlyApprovalTrendDto("Jul", 22, 18, 4),
                        new ApprovalsPageDto.MonthlyApprovalTrendDto("Aug", 20, 24, 3),
                        new ApprovalsPageDto.MonthlyApprovalTrendDto("Sep", 21, 22, 3)
                )).build();

        ApprovalDetailDto selected = approvalDetailsMap.getOrDefault("APR-2026-1042", approvalDetailsMap.values().iterator().next());

        return ApprovalsPageDto.builder()
                .adminUser(adminHeader)
                .pageTitle("Approvals")
                .pageSubtitle("Review and process pending approvals across all group companies.")
                .dateRange(effectiveDateRange)
                .kpis(kpis)
                .tabs(tabs)
                .activeTab(effectiveTab)
                .approvals(filtered)
                .totalRequests(48)
                .currentPage(pageNum)
                .totalPages(5)
                .selectedApprovalDetail(selected)
                .approvalsByType(byType)
                .approvalTrend(trend)
                .build();
    }

    public ApprovalDetailDto getApprovalDetail(String id) {
        return approvalDetailsMap.getOrDefault(id, approvalDetailsMap.get("APR-2026-1042"));
    }

    public ApprovalDetailDto addApprovalRequest(AddApprovalRequestDto req) {
        int nextNum = approvalCounter.incrementAndGet();
        String reqId = "APR-2026-" + (1042 + nextNum - 10);

        ApprovalsPageDto.ApprovalItemDto item = ApprovalsPageDto.ApprovalItemDto.builder()
                .id("apr-" + nextNum)
                .number(nextNum)
                .requestId(reqId)
                .dateTime("16 Sep 2026, 12:30 PM")
                .approvalType(req.getApprovalType() != null ? req.getApprovalType() : "General Approval")
                .company(req.getCompany() != null ? req.getCompany() : "Atirath Traders")
                .requestedBy(req.getRequestedBy() != null ? req.getRequestedBy() : "Admin User")
                .amountOrDetails(req.getAmountOrDetails() != null ? req.getAmountOrDetails() : "₹ 10,00,000")
                .priority(req.getPriority() != null ? req.getPriority() : "Medium")
                .priorityTheme("yellow")
                .status("Pending")
                .statusTheme("yellow")
                .build();

        approvalsList.add(0, item);

        ApprovalDetailDto detail = ApprovalDetailDto.builder()
                .requestId(reqId)
                .title(req.getApprovalType() + " Request")
                .requestedOn("Requested on 16 Sep 2026, 12:30 PM")
                .status("Pending")
                .statusTheme("yellow")
                .tabs(List.of("Details", "History", "Comments", "Related Docs"))
                .activeTab("Details")
                .approvalType(req.getApprovalType())
                .company(req.getCompany())
                .requestedBy(req.getRequestedBy())
                .dateTime("16 Sep 2026, 12:30 PM")
                .priority(req.getPriority())
                .description(req.getDescription() != null ? req.getDescription() : "Standard review request")
                .attachments(List.of(new ApprovalDetailDto.ApprovalAttachmentDto("Request_Doc.pdf", "1.5 MB", "https://atirathgroup.com/docs/sample.pdf")))
                .build();

        approvalDetailsMap.put(reqId, detail);
        return detail;
    }

    public ApprovalDetailDto approveRequest(String id) {
        ApprovalDetailDto detail = getApprovalDetail(id);
        detail.setStatus("Approved");
        detail.setStatusTheme("green");
        approvalsList.stream().filter(a -> a.getRequestId().equalsIgnoreCase(id)).findFirst().ifPresent(a -> {
            a.setStatus("Approved");
            a.setStatusTheme("green");
        });
        return detail;
    }

    public ApprovalDetailDto rejectRequest(String id) {
        ApprovalDetailDto detail = getApprovalDetail(id);
        detail.setStatus("Rejected");
        detail.setStatusTheme("red");
        approvalsList.stream().filter(a -> a.getRequestId().equalsIgnoreCase(id)).findFirst().ifPresent(a -> {
            a.setStatus("Rejected");
            a.setStatusTheme("red");
        });
        return detail;
    }

    // ==========================================
    // 11. EXTERNAL INVESTOR REQUESTS PAGE
    // ==========================================
    public ExternalInvestorRequestsPageDto getInvestorRequestsPage(String query, String sector, String investmentType, String status) {
        return getInvestorRequestsPage(query, sector, investmentType, status, "All", 1, 10, "01 Sep 2026 - 16 Sep 2026");
    }

    public ExternalInvestorRequestsPageDto getInvestorRequestsPage(String query, String sector, String investmentType, String status, String tab, Integer page, Integer size, String dateRange) {
        int pageNum = page != null && page > 0 ? page : 1;
        int pageSize = size != null && size > 0 ? size : 10;
        String effectiveTab = (tab != null && !tab.isBlank()) ? tab : "All";
        String effectiveDateRange = (dateRange != null && !dateRange.isBlank()) ? dateRange : "01 Sep 2026 - 16 Sep 2026";

        List<KpiCardDto> kpis = List.of(
                KpiCardDto.builder().title("Total Requests").value("36").change("+20% vs last quarter").comparisonText("").trend("positive").icon("users").color("blue").build(),
                KpiCardDto.builder().title("Pending Review").value("12").change("+33% vs last quarter").comparisonText("").trend("neutral").icon("clock").color("orange").build(),
                KpiCardDto.builder().title("Approved").value("15").change("+25% vs last quarter").comparisonText("").trend("positive").icon("check-circle").color("green").build(),
                KpiCardDto.builder().title("Rejected").value("5").change("-17% vs last quarter").comparisonText("").trend("negative").icon("x-circle").color("red").build(),
                KpiCardDto.builder().title("Total Investment Value").value("₹ 186.4 Cr").change("+42% vs last quarter").comparisonText("").trend("positive").icon("rupee").color("green").build()
        );

        List<ApprovalsPageDto.TabCountDto> tabs = List.of(
                new ApprovalsPageDto.TabCountDto("All", "All Requests", 36),
                new ApprovalsPageDto.TabCountDto("Pending", "Pending", 12),
                new ApprovalsPageDto.TabCountDto("UnderEvaluation", "Under Evaluation", 8),
                new ApprovalsPageDto.TabCountDto("Approved", "Approved", 15),
                new ApprovalsPageDto.TabCountDto("Rejected", "Rejected", 5)
        );

        List<ExternalInvestorRequestsPageDto.InvestorRequestItemDto> filtered = investorRequestsList.stream()
                .filter(r -> {
                    if (query != null && !query.isBlank()) {
                        String q = query.toLowerCase();
                        if (!r.getRequestId().toLowerCase().contains(q) &&
                            !r.getInvestorName().toLowerCase().contains(q) &&
                            !r.getCompany().toLowerCase().contains(q) &&
                            !r.getSector().toLowerCase().contains(q)) return false;
                    }
                    if (sector != null && !sector.isBlank() && !sector.equalsIgnoreCase("All Sectors")) {
                        if (!r.getSector().equalsIgnoreCase(sector)) return false;
                    }
                    if (status != null && !status.isBlank() && !status.equalsIgnoreCase("All Status")) {
                        if (!r.getStatus().equalsIgnoreCase(status)) return false;
                    }
                    if (tab != null && !tab.isBlank() && !tab.equalsIgnoreCase("All") && !tab.equalsIgnoreCase("All Requests")) {
                        if (tab.equalsIgnoreCase("UnderEvaluation") || tab.equalsIgnoreCase("Under Evaluation")) {
                            if (!r.getStatus().equalsIgnoreCase("Under Review")) return false;
                        } else {
                            if (!r.getStatus().equalsIgnoreCase(tab)) return false;
                        }
                    }
                    return true;
                }).collect(Collectors.toList());

        ExternalInvestorRequestsPageDto.InvestmentRequestsByCompanyDonutDto byCompany = ExternalInvestorRequestsPageDto.InvestmentRequestsByCompanyDonutDto.builder()
                .totalRequests("36")
                .slices(List.of(
                        new ExternalInvestorRequestsPageDto.CompanyRequestSliceDto("Farm Vantara", 8, 22.2, "#3b82f6"),
                        new ExternalInvestorRequestsPageDto.CompanyRequestSliceDto("Atirath Holdings", 7, 19.4, "#22c55e"),
                        new ExternalInvestorRequestsPageDto.CompanyRequestSliceDto("Goeazymart", 5, 13.9, "#f87171"),
                        new ExternalInvestorRequestsPageDto.CompanyRequestSliceDto("Atirath Technologies", 4, 11.1, "#a855f7"),
                        new ExternalInvestorRequestsPageDto.CompanyRequestSliceDto("Atirath Traders", 4, 11.1, "#f59e0b"),
                        new ExternalInvestorRequestsPageDto.CompanyRequestSliceDto("Tri Energy Battery", 3, 8.3, "#06b6d4"),
                        new ExternalInvestorRequestsPageDto.CompanyRequestSliceDto("NEU1 Smart Stay", 3, 8.3, "#ec4899"),
                        new ExternalInvestorRequestsPageDto.CompanyRequestSliceDto("Others", 2, 5.6, "#94a3b8")
                )).build();

        ApprovalsPageDto.ApprovalTrendChartDto trend = ApprovalsPageDto.ApprovalTrendChartDto.builder()
                .filterRange("Last 6 Months")
                .monthlyData(List.of(
                        new ApprovalsPageDto.MonthlyApprovalTrendDto("Apr", 4, 6, 2),
                        new ApprovalsPageDto.MonthlyApprovalTrendDto("May", 6, 8, 3),
                        new ApprovalsPageDto.MonthlyApprovalTrendDto("Jun", 8, 10, 2),
                        new ApprovalsPageDto.MonthlyApprovalTrendDto("Jul", 10, 12, 4),
                        new ApprovalsPageDto.MonthlyApprovalTrendDto("Aug", 11, 14, 3),
                        new ApprovalsPageDto.MonthlyApprovalTrendDto("Sep", 12, 15, 5)
                )).build();

        List<ExternalInvestorRequestsPageDto.TopInvestorTypeBarDto> topInvestorTypes = List.of(
                new ExternalInvestorRequestsPageDto.TopInvestorTypeBarDto("Individual (HNI)", 14, 38.9, "#3b82f6"),
                new ExternalInvestorRequestsPageDto.TopInvestorTypeBarDto("VC / PE Fund", 8, 22.2, "#22c55e"),
                new ExternalInvestorRequestsPageDto.TopInvestorTypeBarDto("Strategic Investor", 6, 16.7, "#f87171"),
                new ExternalInvestorRequestsPageDto.TopInvestorTypeBarDto("Corporate", 5, 13.9, "#f59e0b"),
                new ExternalInvestorRequestsPageDto.TopInvestorTypeBarDto("Family Office", 2, 5.6, "#a855f7"),
                new ExternalInvestorRequestsPageDto.TopInvestorTypeBarDto("Others", 1, 2.8, "#94a3b8")
        );

        InvestorRequestDetailDto selected = investorDetailsMap.getOrDefault("INV-2026-0036", investorDetailsMap.values().iterator().next());

        return ExternalInvestorRequestsPageDto.builder()
                .adminUser(adminHeader)
                .pageTitle("External Investor Requests")
                .pageSubtitle("Manage, review and process investment requests from external investors.")
                .dateRange(effectiveDateRange)
                .kpis(kpis)
                .tabs(tabs)
                .activeTab(effectiveTab)
                .requests(filtered)
                .totalRequests(36)
                .currentPage(pageNum)
                .totalPages(4)
                .selectedRequestDetail(selected)
                .requestsByCompany(byCompany)
                .statusTrend(trend)
                .topInvestorTypes(topInvestorTypes)
                .build();
    }

    public InvestorRequestDetailDto getInvestorRequestDetail(String id) {
        return investorDetailsMap.getOrDefault(id, investorDetailsMap.get("INV-2026-0036"));
    }

    public InvestorRequestDetailDto addInvestorRequest(AddInvestorRequestDto req) {
        int nextNum = investorCounter.incrementAndGet();
        String reqId = "INV-2026-00" + (36 + nextNum - 10);

        ExternalInvestorRequestsPageDto.InvestorRequestItemDto item = ExternalInvestorRequestsPageDto.InvestorRequestItemDto.builder()
                .id("inv-" + nextNum)
                .number(nextNum)
                .requestId(reqId)
                .dateTime("16 Sep 2026, 12:45 PM")
                .investorName(req.getInvestorName())
                .company(req.getCompany() != null ? req.getCompany() : "Farm Vantara")
                .sector(req.getSector() != null ? req.getSector() : "Agriculture")
                .investmentAmount(req.getProposedAmount() != null ? req.getProposedAmount() : "10,00,00,000")
                .status("Pending")
                .statusTheme("yellow")
                .build();

        investorRequestsList.add(0, item);

        InvestorRequestDetailDto detail = InvestorRequestDetailDto.builder()
                .requestId(reqId)
                .investorName(req.getInvestorName())
                .requestedOn("Requested on 16 Sep 2026, 12:45 PM")
                .status("Pending")
                .statusTheme("yellow")
                .tabs(List.of("Details", "Documents", "Comments", "History"))
                .activeTab("Details")
                .investorType(req.getInvestorType() != null ? req.getInvestorType() : "Individual (HNI)")
                .contactEmail(req.getContactEmail() != null ? req.getContactEmail() : "investor@example.com")
                .contactPhone(req.getContactPhone() != null ? req.getContactPhone() : "+91 99999 88888")
                .company(req.getCompany())
                .sector(req.getSector())
                .investmentType(req.getInvestmentType() != null ? req.getInvestmentType() : "Equity")
                .proposedAmount("₹ " + req.getProposedAmount())
                .proposedEquity(req.getProposedEquity() != null ? req.getProposedEquity() : "5%")
                .purpose(req.getPurpose() != null ? req.getPurpose() : "Expansion")
                .expectedRoi("18% (IRR)")
                .timeline("3 - 5 Years")
                .assignedTo("Finance Team")
                .description(req.getDescription() != null ? req.getDescription() : "Standard external investor proposal")
                .build();

        investorDetailsMap.put(reqId, detail);
        return detail;
    }

    public InvestorRequestDetailDto approveInvestorRequest(String id) {
        InvestorRequestDetailDto detail = getInvestorRequestDetail(id);
        detail.setStatus("Approved");
        detail.setStatusTheme("green");
        investorRequestsList.stream().filter(r -> r.getRequestId().equalsIgnoreCase(id)).findFirst().ifPresent(r -> {
            r.setStatus("Approved");
            r.setStatusTheme("green");
        });
        return detail;
    }

    public InvestorRequestDetailDto rejectInvestorRequest(String id) {
        InvestorRequestDetailDto detail = getInvestorRequestDetail(id);
        detail.setStatus("Rejected");
        detail.setStatusTheme("red");
        investorRequestsList.stream().filter(r -> r.getRequestId().equalsIgnoreCase(id)).findFirst().ifPresent(r -> {
            r.setStatus("Rejected");
            r.setStatusTheme("red");
        });
        return detail;
    }

    // ==========================================
    // 12. GROUP REPORTS PAGE
    // ==========================================
    public GroupReportsPageDto getReportsPage(String companyFilter, String dateRange) {
        List<KpiCardDto> kpis = List.of(
                KpiCardDto.builder().title("Total Group Companies").value("8").change("+14% vs last quarter").comparisonText("").trend("positive").icon("building").color("blue").build(),
                KpiCardDto.builder().title("Total Investment Value").value("₹ 186.4 Cr").change("+22% vs last quarter").comparisonText("").trend("positive").icon("rupee").color("green").build(),
                KpiCardDto.builder().title("Total Revenue (YTD)").value("₹ 124.8 Cr").change("+18% vs last quarter").comparisonText("").trend("positive").icon("bar-chart").color("orange").build(),
                KpiCardDto.builder().title("Total Profit (YTD)").value("₹ 18.6 Cr").change("+26% vs last quarter").comparisonText("").trend("positive").icon("pie-chart").color("purple").build(),
                KpiCardDto.builder().title("Total Employees").value("524").change("+12% vs last quarter").comparisonText("").trend("positive").icon("users").color("red").build()
        );

        GroupReportsPageDto.RevenueProfitInvestmentTrendDto trend = GroupReportsPageDto.RevenueProfitInvestmentTrendDto.builder()
                .filterRange("Last 6 Months")
                .monthlyTrends(List.of(
                        new GroupReportsPageDto.MonthlyReportPointDto("Apr", 18.2, 2.4, 8.5),
                        new GroupReportsPageDto.MonthlyReportPointDto("May", 21.4, 3.1, 12.0),
                        new GroupReportsPageDto.MonthlyReportPointDto("Jun", 26.8, 3.8, 15.2),
                        new GroupReportsPageDto.MonthlyReportPointDto("Jul", 32.6, 4.5, 18.0),
                        new GroupReportsPageDto.MonthlyReportPointDto("Aug", 38.1, 5.2, 22.4),
                        new GroupReportsPageDto.MonthlyReportPointDto("Sep", 45.3, 6.4, 26.8)
                )).build();

        GroupReportsPageDto.SegmentWiseInvestmentDonutDto segmentDonut = GroupReportsPageDto.SegmentWiseInvestmentDonutDto.builder()
                .totalInvestment("₹ 186.4 Cr")
                .slices(List.of(
                        new GroupReportsPageDto.SegmentSliceDto("Renewable Energy", 28.4, "₹ 52.9 Cr", "#22c55e"),
                        new GroupReportsPageDto.SegmentSliceDto("Agriculture", 21.0, "₹ 39.1 Cr", "#eab308"),
                        new GroupReportsPageDto.SegmentSliceDto("Technology", 16.8, "₹ 31.3 Cr", "#06b6d4"),
                        new GroupReportsPageDto.SegmentSliceDto("Retail / FMCG", 12.5, "₹ 23.3 Cr", "#3b82f6"),
                        new GroupReportsPageDto.SegmentSliceDto("Real Estate", 11.2, "₹ 20.9 Cr", "#a855f7"),
                        new GroupReportsPageDto.SegmentSliceDto("Manufacturing", 6.2, "₹ 11.6 Cr", "#ec4899"),
                        new GroupReportsPageDto.SegmentSliceDto("Others", 3.9, "₹ 7.3 Cr", "#94a3b8")
                )).build();

        List<GroupReportsPageDto.CompanyPerformanceReportItemDto> allPerformance = List.of(
                new GroupReportsPageDto.CompanyPerformanceReportItemDto(1, "Atirath Traders", "Import-Export", 32.0, 28.4, 4.6, 86, "↑ 18%", "Active"),
                new GroupReportsPageDto.CompanyPerformanceReportItemDto(2, "Atirath Holdings", "Renewable Energy", 52.9, 36.8, 6.4, 120, "↑ 24%", "Active"),
                new GroupReportsPageDto.CompanyPerformanceReportItemDto(3, "Atirath Technologies", "Technology", 31.3, 18.6, 3.2, 95, "↑ 32%", "Active"),
                new GroupReportsPageDto.CompanyPerformanceReportItemDto(4, "Farm Vantara", "Agriculture", 20.9, 12.4, 1.8, 78, "↑ 16%", "Active"),
                new GroupReportsPageDto.CompanyPerformanceReportItemDto(5, "Goeazymart", "Retail / FMCG", 23.3, 14.2, 2.1, 62, "↑ 20%", "Active"),
                new GroupReportsPageDto.CompanyPerformanceReportItemDto(6, "Tri Energy Battery", "Energy Storage", 11.6, 6.8, 0.9, 48, "↑ 15%", "Active"),
                new GroupReportsPageDto.CompanyPerformanceReportItemDto(7, "CS INTERIO", "Real Estate", 7.3, 4.6, 0.5, 28, "↑ 12%", "Active"),
                new GroupReportsPageDto.CompanyPerformanceReportItemDto(8, "Royal One Heating", "Manufacturing", 6.4, 3.0, 0.3, 7, "↑ 10%", "Active")
        );

        List<GroupReportsPageDto.CompanyPerformanceReportItemDto> companiesPerformance = allPerformance.stream()
                .filter(c -> {
                    if (companyFilter != null && !companyFilter.isBlank() && !companyFilter.equalsIgnoreCase("All Group Companies")) {
                        return c.getCompanyName().equalsIgnoreCase(companyFilter);
                    }
                    return true;
                }).collect(Collectors.toList());

        GroupReportsPageDto.RevenueContributionDonutDto revenueDonut = GroupReportsPageDto.RevenueContributionDonutDto.builder()
                .slices(List.of(
                        new GroupReportsPageDto.CompanyRevenueSliceDto("Atirath Holdings", 29.5, "₹ 36.8 Cr", "#22c55e"),
                        new GroupReportsPageDto.CompanyRevenueSliceDto("Atirath Traders", 22.8, "₹ 28.4 Cr", "#f59e0b"),
                        new GroupReportsPageDto.CompanyRevenueSliceDto("Atirath Technologies", 14.9, "₹ 18.6 Cr", "#3b82f6"),
                        new GroupReportsPageDto.CompanyRevenueSliceDto("Goeazymart", 11.4, "₹ 14.2 Cr", "#f87171"),
                        new GroupReportsPageDto.CompanyRevenueSliceDto("Farm Vantara", 9.9, "₹ 12.4 Cr", "#a855f7"),
                        new GroupReportsPageDto.CompanyRevenueSliceDto("Tri Energy Battery", 5.4, "₹ 6.8 Cr", "#06b6d4"),
                        new GroupReportsPageDto.CompanyRevenueSliceDto("CS INTERIO", 3.7, "₹ 4.6 Cr", "#8b5cf6"),
                        new GroupReportsPageDto.CompanyRevenueSliceDto("Royal One Heating", 2.4, "₹ 3.0 Cr", "#94a3b8")
                )).build();

        List<GroupReportsPageDto.KeyFinancialMetricItemDto> metrics = List.of(
                new GroupReportsPageDto.KeyFinancialMetricItemDto("Total Revenue", "124.8 Cr", "105.7 Cr", "+ 18%", "positive"),
                new GroupReportsPageDto.KeyFinancialMetricItemDto("Total Profit", "18.6 Cr", "14.7 Cr", "+ 26%", "positive"),
                new GroupReportsPageDto.KeyFinancialMetricItemDto("Total Assets", "342.1 Cr", "298.4 Cr", "+ 15%", "positive"),
                new GroupReportsPageDto.KeyFinancialMetricItemDto("Total Liabilities", "156.3 Cr", "142.1 Cr", "+ 10%", "positive"),
                new GroupReportsPageDto.KeyFinancialMetricItemDto("Net Worth", "185.8 Cr", "156.3 Cr", "+ 19%", "positive"),
                new GroupReportsPageDto.KeyFinancialMetricItemDto("EBITDA", "28.4 Cr", "22.1 Cr", "+ 29%", "positive")
        );

        List<GroupReportsPageDto.EmployeeDistributionBarDto> employees = List.of(
                new GroupReportsPageDto.EmployeeDistributionBarDto("Atirath Holdings", 120),
                new GroupReportsPageDto.EmployeeDistributionBarDto("Atirath Tech", 95),
                new GroupReportsPageDto.EmployeeDistributionBarDto("Atirath Traders", 86),
                new GroupReportsPageDto.EmployeeDistributionBarDto("Farm Vantara", 78),
                new GroupReportsPageDto.EmployeeDistributionBarDto("Goeazymart", 62),
                new GroupReportsPageDto.EmployeeDistributionBarDto("Tri Energy", 48),
                new GroupReportsPageDto.EmployeeDistributionBarDto("CS INTERIO", 28),
                new GroupReportsPageDto.EmployeeDistributionBarDto("Royal One", 7)
        );

        List<GroupReportsPageDto.TopPerformingCompanyProfitDto> topProfits = List.of(
                new GroupReportsPageDto.TopPerformingCompanyProfitDto(1, "Atirath Technologies", 3.2, "+ 34%"),
                new GroupReportsPageDto.TopPerformingCompanyProfitDto(2, "Atirath Holdings", 6.4, "+ 28%"),
                new GroupReportsPageDto.TopPerformingCompanyProfitDto(3, "Goeazymart", 2.1, "+ 25%"),
                new GroupReportsPageDto.TopPerformingCompanyProfitDto(4, "Atirath Traders", 4.6, "+ 22%"),
                new GroupReportsPageDto.TopPerformingCompanyProfitDto(5, "Farm Vantara", 1.8, "+ 18%")
        );

        return GroupReportsPageDto.builder()
                .adminUser(adminHeader)
                .pageTitle("Group Reports")
                .pageSubtitle("Consolidated view of all group companies, financials, operations and investment activities.")
                .dateRange(dateRange != null ? dateRange : "01 Apr 2026 - 16 Sep 2026")
                .selectedCompany(companyFilter != null ? companyFilter : "All Group Companies")
                .kpis(kpis)
                .financialTrend(trend)
                .segmentWiseInvestment(segmentDonut)
                .companyPerformance(companiesPerformance)
                .revenueContribution(revenueDonut)
                .keyFinancialMetrics(metrics)
                .employeeDistribution(employees)
                .topPerformingCompanies(topProfits)
                .build();
    }

    public Map<String, String> exportReports() {
        return Map.of(
                "status", "READY",
                "fileName", "consolidated_group_financial_and_operational_report_2026.pdf",
                "downloadUrl", "https://atirathgroup.com/downloads/consolidated-group-report-2026.pdf"
        );
    }

    // ==========================================
    // INTERNAL SEEDING HELPERS
    // ==========================================
    private void seedSubsidiaries() {
        subsidiaries.clear();
        subsidiaries.add(SubsidiarySummaryDto.builder()
                .id("sub-1").number(1).companyName("Atirath Traders India Pvt Ltd").initials("AT")
                .subIndustry("Import & Export Trading").sector("Trading").incorporationDate("12 Jan 2021")
                .cin("U51909TG2021PTC123456").headquarters("Hyderabad, TG").status("Active")
                .totalMembers(325).totalTradeValue("₹ 8.4 Cr").build());

        subsidiaries.add(SubsidiarySummaryDto.builder()
                .id("sub-2").number(2).companyName("Atirath Holdings India Limited").initials("AH")
                .subIndustry("Renewable Energy & CBG").sector("Renewable Energy").incorporationDate("18 Mar 2021")
                .cin("U40106TG2021PLC123457").headquarters("Hyderabad, TG").status("Active")
                .totalMembers(156).totalTradeValue("₹ 5.2 Cr").build());

        subsidiaries.add(SubsidiarySummaryDto.builder()
                .id("sub-3").number(3).companyName("Atirath Technologies Pvt Ltd").initials("AT")
                .subIndustry("IT & Digital Solutions").sector("Technology").incorporationDate("25 Jun 2021")
                .cin("U72200TG2021PTC123458").headquarters("Hyderabad, TG").status("Active")
                .totalMembers(128).totalTradeValue("₹ 3.6 Cr").build());

        subsidiaries.add(SubsidiarySummaryDto.builder()
                .id("sub-4").number(4).companyName("Farm Vantara AgriTech Pvt Ltd").initials("FV")
                .subIndustry("AgriTech & Marketplace").sector("Agriculture").incorporationDate("14 Sep 2021")
                .cin("U01100TG2021PTC123459").headquarters("Hyderabad, TG").status("Active")
                .totalMembers(98).totalTradeValue("₹ 2.8 Cr").build());

        subsidiaries.add(SubsidiarySummaryDto.builder()
                .id("sub-5").number(5).companyName("Goeazymart E-Commerce Pvt Ltd").initials("GE")
                .subIndustry("E-commerce & Retail").sector("Retail").incorporationDate("03 Jan 2022")
                .cin("U47912TG2022PTC123460").headquarters("Hyderabad, TG").status("Active")
                .totalMembers(85).totalTradeValue("₹ 2.1 Cr").build());

        subsidiaries.add(SubsidiarySummaryDto.builder()
                .id("sub-6").number(6).companyName("Tri Energy Battery Pvt Ltd").initials("TB")
                .subIndustry("Energy Storage Solutions").sector("Energy Storage").incorporationDate("22 Aug 2024")
                .cin("U29299TG2024PTC123461").headquarters("Hyderabad, TG").status("Under Setup")
                .totalMembers(50).totalTradeValue("₹ 1.9 Cr").build());
    }

    private void seedSubsidiaryDetails() {
        subsidiaryDetailsMap.clear();

        // 1. Atirath Traders India Pvt Ltd (Image 3 UI Ground Truth)
        List<KpiCardDto> sub1Kpis = List.of(
                KpiCardDto.builder().title("Total Members").value("325").change("+12%").comparisonText("vs last quarter").trend("positive").icon("users").color("blue").build(),
                KpiCardDto.builder().title("Total Trade Value").value("₹ 8.4 Cr").change("+18%").comparisonText("vs last quarter").trend("positive").icon("trending-up").color("green").build(),
                KpiCardDto.builder().title("Settlement Value").value("₹ 2.1 Cr").change("+16%").comparisonText("vs last quarter").trend("positive").icon("rupee").color("purple").build(),
                KpiCardDto.builder().title("Open Orders").value("48").change("+5%").comparisonText("vs last quarter").trend("positive").icon("file-text").color("orange").build(),
                KpiCardDto.builder().title("Pending Approvals").value("3").change("-25%").comparisonText("vs last quarter").trend("positive").icon("alert-triangle").color("red").build()
        );

        SubsidiaryDetailDto.CompanyInfoDto sub1Info = SubsidiaryDetailDto.CompanyInfoDto.builder()
                .companyName("Atirath Traders India Pvt Ltd")
                .cin("U51909TG2021PTC123456")
                .incorporationDate("12 Jan 2021")
                .sector("Trading")
                .industry("Import & Export Trading")
                .registeredAddress("Plot No. 123, Financial District, Hyderabad, Telangana - 500032")
                .headquarters("Hyderabad, TG")
                .pan("AAACT1234A")
                .gstNumber("36AAACT1234A1Z5")
                .status("Active")
                .build();

        SubsidiaryDetailDto.KeyMetricsDto sub1Metrics = SubsidiaryDetailDto.KeyMetricsDto.builder()
                .filterRange("Last 6 Months")
                .tradeVolume(SubsidiaryDetailDto.MetricChartDto.builder()
                        .title("Trade Volume (₹ in Crores)")
                        .currentValue("8.4")
                        .monthlyData(List.of(
                                new SubsidiaryDetailDto.MonthlyDataPointDto("Apr", 1.2),
                                new SubsidiaryDetailDto.MonthlyDataPointDto("May", 2.0),
                                new SubsidiaryDetailDto.MonthlyDataPointDto("Jun", 2.2),
                                new SubsidiaryDetailDto.MonthlyDataPointDto("Jul", 2.8),
                                new SubsidiaryDetailDto.MonthlyDataPointDto("Aug", 3.4),
                                new SubsidiaryDetailDto.MonthlyDataPointDto("Sep", 8.4)
                        )).build())
                .settlementValue(SubsidiaryDetailDto.MetricChartDto.builder()
                        .title("Settlement Value (₹ in Crores)")
                        .currentValue("2.1")
                        .monthlyData(List.of(
                                new SubsidiaryDetailDto.MonthlyDataPointDto("Apr", 0.4),
                                new SubsidiaryDetailDto.MonthlyDataPointDto("May", 0.8),
                                new SubsidiaryDetailDto.MonthlyDataPointDto("Jun", 0.9),
                                new SubsidiaryDetailDto.MonthlyDataPointDto("Jul", 1.2),
                                new SubsidiaryDetailDto.MonthlyDataPointDto("Aug", 1.6),
                                new SubsidiaryDetailDto.MonthlyDataPointDto("Sep", 2.1)
                        )).build())
                .activeMembers(SubsidiaryDetailDto.MetricChartDto.builder()
                        .title("Active Members")
                        .currentValue("325")
                        .monthlyData(List.of(
                                new SubsidiaryDetailDto.MonthlyDataPointDto("Apr", 180.0),
                                new SubsidiaryDetailDto.MonthlyDataPointDto("May", 210.0),
                                new SubsidiaryDetailDto.MonthlyDataPointDto("Jun", 245.0),
                                new SubsidiaryDetailDto.MonthlyDataPointDto("Jul", 270.0),
                                new SubsidiaryDetailDto.MonthlyDataPointDto("Aug", 295.0),
                                new SubsidiaryDetailDto.MonthlyDataPointDto("Sep", 325.0)
                        )).build())
                .newMembers(SubsidiaryDetailDto.MetricChartDto.builder()
                        .title("New Members")
                        .currentValue("28")
                        .monthlyData(List.of(
                                new SubsidiaryDetailDto.MonthlyDataPointDto("Apr", 15.0),
                                new SubsidiaryDetailDto.MonthlyDataPointDto("May", 18.0),
                                new SubsidiaryDetailDto.MonthlyDataPointDto("Jun", 22.0),
                                new SubsidiaryDetailDto.MonthlyDataPointDto("Jul", 24.0),
                                new SubsidiaryDetailDto.MonthlyDataPointDto("Aug", 25.0),
                                new SubsidiaryDetailDto.MonthlyDataPointDto("Sep", 28.0)
                        )).build())
                .build();

        List<SubsidiaryDetailDto.SubsidiaryActivityDto> sub1Activities = List.of(
                SubsidiaryDetailDto.SubsidiaryActivityDto.builder().dateTime("16 Sep 2026, 11:20 AM").activity("New trade order created").performedBy("Ramesh Kumar").status("Completed").build(),
                SubsidiaryDetailDto.SubsidiaryActivityDto.builder().dateTime("15 Sep 2026, 04:15 PM").activity("Settlement processed").performedBy("System").status("Completed").build(),
                SubsidiaryDetailDto.SubsidiaryActivityDto.builder().dateTime("14 Sep 2026, 10:30 AM").activity("New member added").performedBy("Farm Vantara").status("Completed").build(),
                SubsidiaryDetailDto.SubsidiaryActivityDto.builder().dateTime("12 Sep 2026, 05:45 PM").activity("Document updated").performedBy("Atirath Technologies").status("Completed").build(),
                SubsidiaryDetailDto.SubsidiaryActivityDto.builder().dateTime("11 Sep 2026, 09:20 AM").activity("KYC verified").performedBy("Compliance Team").status("Completed").build()
        );

        List<SubsidiaryDetailDto.ComplianceItemDto> sub1Compliance = List.of(
                SubsidiaryDetailDto.ComplianceItemDto.builder().title("ROC Filings").description("All filings up to date").status("Compliant").build(),
                SubsidiaryDetailDto.ComplianceItemDto.builder().title("GST Compliance").description("Returns filed up to date").status("Compliant").build(),
                SubsidiaryDetailDto.ComplianceItemDto.builder().title("Income Tax Filings").description("Latest return filed for FY 2024-25").status("Compliant").build(),
                SubsidiaryDetailDto.ComplianceItemDto.builder().title("KYC & AML").description("All director and member KYC verified").status("Compliant").build(),
                SubsidiaryDetailDto.ComplianceItemDto.builder().title("Regulatory Documents").description("All mandatory documents updated").status("Compliant").build()
        );

        SubsidiaryDetailDto sub1Detail = SubsidiaryDetailDto.builder()
                .adminUser(adminHeader)
                .id("sub-1")
                .companyName("Atirath Traders India Pvt Ltd")
                .initials("AT")
                .status("Active")
                .subIndustry("Import & Export Trading")
                .cin("U51909TG2021PTC123456")
                .incorporationDate("12 Jan 2021")
                .headquarters("Hyderabad, TG")
                .sector("Trading")
                .totalMembers(325)
                .totalTradeValue("₹ 8.4 Cr")
                .availableTabs(List.of("Overview", "Financials", "Shareholding", "Members", "Trading Activity", "Compliance", "Documents", "Settings"))
                .activeTab("Overview")
                .overviewKpis(sub1Kpis)
                .companyInfo(sub1Info)
                .keyMetrics(sub1Metrics)
                .recentActivities(sub1Activities)
                .complianceStatus(sub1Compliance)
                .financials(buildDefaultFinancials("Atirath Traders", "32.0 Cr", "6.4 Cr", "4.6 Cr", "14.4%"))
                .shareholding(buildDefaultShareholding("160.0 Cr", "112.0 Cr", "104.5 Cr", "10,45,00,000", "₹ 10"))
                .members(buildDefaultMembers(325, 310, 15, 45, 280))
                .tradingActivity(buildDefaultTradingActivity(420, "₹ 8.4 Cr", "₹ 4.8 Cr", "₹ 3.6 Cr", 48))
                .compliance(buildDefaultCompliance())
                .documents(buildDefaultDocuments("Atirath Traders India Pvt Ltd"))
                .settings(buildDefaultSettings("Atirath Traders", "traders@atirathgroup.com", "+91 98765 43210"))
                .build();

        subsidiaryDetailsMap.put("sub-1", sub1Detail);

        // Seed details for the remaining group subsidiaries
        subsidiaries.stream().filter(s -> !s.getId().equals("sub-1")).forEach(s -> {
            SubsidiaryDetailDto detail = createDefaultDetailFor(s);
            subsidiaryDetailsMap.put(s.getId(), detail);
        });
    }

    private SubsidiaryDetailDto createDefaultDetailFor(SubsidiarySummaryDto summary) {
        SubsidiaryDetailDto.CompanyInfoDto info = SubsidiaryDetailDto.CompanyInfoDto.builder()
                .companyName(summary.getCompanyName())
                .cin(summary.getCin())
                .incorporationDate(summary.getIncorporationDate())
                .sector(summary.getSector())
                .industry(summary.getSubIndustry())
                .registeredAddress("Plot No. " + (summary.getNumber() * 50) + ", Financial District, Hyderabad, Telangana - 500032")
                .headquarters(summary.getHeadquarters())
                .pan("AABCT" + summary.getNumber() + "456B")
                .gstNumber("36AABCT" + summary.getNumber() + "456B1Z8")
                .status(summary.getStatus())
                .build();

        List<KpiCardDto> kpis = List.of(
                KpiCardDto.builder().title("Total Members").value(String.valueOf(summary.getTotalMembers())).change("+8%").comparisonText("vs last quarter").trend("positive").icon("users").color("blue").build(),
                KpiCardDto.builder().title("Total Trade Value").value(summary.getTotalTradeValue()).change("+12%").comparisonText("vs last quarter").trend("positive").icon("trending-up").color("green").build(),
                KpiCardDto.builder().title("Settlement Value").value("₹ 1.2 Cr").change("+10%").comparisonText("vs last quarter").trend("positive").icon("rupee").color("purple").build(),
                KpiCardDto.builder().title("Open Orders").value("24").change("+3%").comparisonText("vs last quarter").trend("positive").icon("file-text").color("orange").build(),
                KpiCardDto.builder().title("Pending Approvals").value("1").change("-50%").comparisonText("vs last quarter").trend("positive").icon("alert-triangle").color("red").build()
        );

        SubsidiaryDetailDto.KeyMetricsDto metrics = SubsidiaryDetailDto.KeyMetricsDto.builder()
                .filterRange("Last 6 Months")
                .tradeVolume(SubsidiaryDetailDto.MetricChartDto.builder()
                        .title("Trade Volume (₹ in Crores)")
                        .currentValue(summary.getTotalTradeValue().replace("₹ ", "").replace(" Cr", ""))
                        .monthlyData(List.of(
                                new SubsidiaryDetailDto.MonthlyDataPointDto("Apr", 0.8),
                                new SubsidiaryDetailDto.MonthlyDataPointDto("May", 1.2),
                                new SubsidiaryDetailDto.MonthlyDataPointDto("Jun", 1.4),
                                new SubsidiaryDetailDto.MonthlyDataPointDto("Jul", 1.8),
                                new SubsidiaryDetailDto.MonthlyDataPointDto("Aug", 2.2),
                                new SubsidiaryDetailDto.MonthlyDataPointDto("Sep", 2.8)
                        )).build())
                .settlementValue(SubsidiaryDetailDto.MetricChartDto.builder()
                        .title("Settlement Value (₹ in Crores)")
                        .currentValue("1.2")
                        .monthlyData(List.of(
                                new SubsidiaryDetailDto.MonthlyDataPointDto("Apr", 0.3),
                                new SubsidiaryDetailDto.MonthlyDataPointDto("May", 0.5),
                                new SubsidiaryDetailDto.MonthlyDataPointDto("Jun", 0.6),
                                new SubsidiaryDetailDto.MonthlyDataPointDto("Jul", 0.8),
                                new SubsidiaryDetailDto.MonthlyDataPointDto("Aug", 1.0),
                                new SubsidiaryDetailDto.MonthlyDataPointDto("Sep", 1.2)
                        )).build())
                .activeMembers(SubsidiaryDetailDto.MetricChartDto.builder()
                        .title("Active Members")
                        .currentValue(String.valueOf(summary.getTotalMembers()))
                        .monthlyData(List.of(
                                new SubsidiaryDetailDto.MonthlyDataPointDto("Apr", (double) (summary.getTotalMembers() / 2)),
                                new SubsidiaryDetailDto.MonthlyDataPointDto("May", (double) (summary.getTotalMembers() * 6 / 10)),
                                new SubsidiaryDetailDto.MonthlyDataPointDto("Jun", (double) (summary.getTotalMembers() * 7 / 10)),
                                new SubsidiaryDetailDto.MonthlyDataPointDto("Jul", (double) (summary.getTotalMembers() * 8 / 10)),
                                new SubsidiaryDetailDto.MonthlyDataPointDto("Aug", (double) (summary.getTotalMembers() * 9 / 10)),
                                new SubsidiaryDetailDto.MonthlyDataPointDto("Sep", (double) summary.getTotalMembers())
                        )).build())
                .newMembers(SubsidiaryDetailDto.MetricChartDto.builder()
                        .title("New Members")
                        .currentValue("16")
                        .monthlyData(List.of(
                                new SubsidiaryDetailDto.MonthlyDataPointDto("Apr", 8.0),
                                new SubsidiaryDetailDto.MonthlyDataPointDto("May", 10.0),
                                new SubsidiaryDetailDto.MonthlyDataPointDto("Jun", 12.0),
                                new SubsidiaryDetailDto.MonthlyDataPointDto("Jul", 14.0),
                                new SubsidiaryDetailDto.MonthlyDataPointDto("Aug", 15.0),
                                new SubsidiaryDetailDto.MonthlyDataPointDto("Sep", 16.0)
                        )).build())
                .build();

        List<SubsidiaryDetailDto.SubsidiaryActivityDto> activities = List.of(
                SubsidiaryDetailDto.SubsidiaryActivityDto.builder().dateTime("16 Sep 2026, 10:30 AM").activity("Share transfer recorded").performedBy("Admin User").status("Completed").build(),
                SubsidiaryDetailDto.SubsidiaryActivityDto.builder().dateTime("15 Sep 2026, 03:00 PM").activity("Quarterly compliance validated").performedBy("Compliance Team").status("Completed").build(),
                SubsidiaryDetailDto.SubsidiaryActivityDto.builder().dateTime("14 Sep 2026, 11:15 AM").activity("New member onboarding").performedBy("Operations").status("Completed").build()
        );

        List<SubsidiaryDetailDto.ComplianceItemDto> compliance = List.of(
                SubsidiaryDetailDto.ComplianceItemDto.builder().title("ROC Filings").description("All filings up to date").status("Compliant").build(),
                SubsidiaryDetailDto.ComplianceItemDto.builder().title("GST Compliance").description("Returns filed up to date").status("Compliant").build(),
                SubsidiaryDetailDto.ComplianceItemDto.builder().title("Income Tax Filings").description("Latest return filed for FY 2024-25").status("Compliant").build(),
                SubsidiaryDetailDto.ComplianceItemDto.builder().title("KYC & AML").description("All director and member KYC verified").status("Compliant").build(),
                SubsidiaryDetailDto.ComplianceItemDto.builder().title("Regulatory Documents").description("All mandatory documents updated").status("Compliant").build()
        );

        return SubsidiaryDetailDto.builder()
                .adminUser(adminHeader)
                .id(summary.getId())
                .companyName(summary.getCompanyName())
                .initials(summary.getInitials())
                .status(summary.getStatus())
                .subIndustry(summary.getSubIndustry())
                .cin(summary.getCin())
                .incorporationDate(summary.getIncorporationDate())
                .headquarters(summary.getHeadquarters())
                .sector(summary.getSector())
                .totalMembers(summary.getTotalMembers())
                .totalTradeValue(summary.getTotalTradeValue())
                .availableTabs(List.of("Overview", "Financials", "Shareholding", "Members", "Trading Activity", "Compliance", "Documents", "Settings"))
                .activeTab("Overview")
                .overviewKpis(kpis)
                .companyInfo(info)
                .keyMetrics(metrics)
                .recentActivities(activities)
                .complianceStatus(compliance)
                .financials(buildDefaultFinancials(summary.getCompanyName(), "24.5 Cr", "5.2 Cr", "3.6 Cr", "14.7%"))
                .shareholding(buildDefaultShareholding("100.0 Cr", "75.0 Cr", "70.0 Cr", "7,00,00,000", "₹ 10"))
                .members(buildDefaultMembers(summary.getTotalMembers(), summary.getTotalMembers() * 9 / 10, summary.getTotalMembers() / 10, 20, summary.getTotalMembers() - 20))
                .tradingActivity(buildDefaultTradingActivity(250, summary.getTotalTradeValue(), "₹ 1.5 Cr", "₹ 1.0 Cr", 24))
                .compliance(buildDefaultCompliance())
                .documents(buildDefaultDocuments(summary.getCompanyName()))
                .settings(buildDefaultSettings(summary.getCompanyName(), "support@" + summary.getInitials().toLowerCase() + ".atirathgroup.com", "+91 98765 00000"))
                .build();
    }

    private SubsidiaryDetailDto.FinancialsTabDto buildDefaultFinancials(String company, String rev, String ebitda, String profit, String margin) {
        return SubsidiaryDetailDto.FinancialsTabDto.builder()
                .kpis(List.of(
                        KpiCardDto.builder().title("Revenue (YTD)").value("₹ " + rev).change("+18%").trend("positive").icon("bar-chart").color("blue").build(),
                        KpiCardDto.builder().title("EBITDA").value("₹ " + ebitda).change("+22%").trend("positive").icon("trending-up").color("green").build(),
                        KpiCardDto.builder().title("Net Profit").value("₹ " + profit).change("+15%").trend("positive").icon("rupee").color("purple").build(),
                        KpiCardDto.builder().title("Profit Margin").value(margin).change("+2.1%").trend("positive").icon("pie-chart").color("orange").build()
                ))
                .revenueTrend(SubsidiaryDetailDto.MetricChartDto.builder()
                        .title("Revenue Trend (₹ Cr)")
                        .currentValue(rev.replace(" Cr", ""))
                        .monthlyData(List.of(
                                new SubsidiaryDetailDto.MonthlyDataPointDto("Apr", 3.2),
                                new SubsidiaryDetailDto.MonthlyDataPointDto("May", 4.1),
                                new SubsidiaryDetailDto.MonthlyDataPointDto("Jun", 4.8),
                                new SubsidiaryDetailDto.MonthlyDataPointDto("Jul", 5.5),
                                new SubsidiaryDetailDto.MonthlyDataPointDto("Aug", 6.8),
                                new SubsidiaryDetailDto.MonthlyDataPointDto("Sep", 7.6)
                        )).build())
                .profitTrend(SubsidiaryDetailDto.MetricChartDto.builder()
                        .title("Net Profit Trend (₹ Cr)")
                        .currentValue(profit.replace(" Cr", ""))
                        .monthlyData(List.of(
                                new SubsidiaryDetailDto.MonthlyDataPointDto("Apr", 0.4),
                                new SubsidiaryDetailDto.MonthlyDataPointDto("May", 0.6),
                                new SubsidiaryDetailDto.MonthlyDataPointDto("Jun", 0.7),
                                new SubsidiaryDetailDto.MonthlyDataPointDto("Jul", 0.9),
                                new SubsidiaryDetailDto.MonthlyDataPointDto("Aug", 1.2),
                                new SubsidiaryDetailDto.MonthlyDataPointDto("Sep", 1.4)
                        )).build())
                .statements(List.of(
                        SubsidiaryDetailDto.FinancialStatementDto.builder().period("FY 2025-26 Q1").revenue("₹ 8.5 Cr").expenses("₹ 6.8 Cr").netProfit("₹ 1.7 Cr").profitMargin("20.0%").filingStatus("Audited").build(),
                        SubsidiaryDetailDto.FinancialStatementDto.builder().period("FY 2024-25 Full Year").revenue("₹ 28.4 Cr").expenses("₹ 23.8 Cr").netProfit("₹ 4.6 Cr").profitMargin("16.2%").filingStatus("Audited & Filed").build()
                ))
                .build();
    }

    private SubsidiaryDetailDto.ShareholdingTabDto buildDefaultShareholding(String auth, String issued, String paid, String shares, String face) {
        return SubsidiaryDetailDto.ShareholdingTabDto.builder()
                .authorizedCapital("₹ " + auth)
                .issuedCapital("₹ " + issued)
                .paidUpCapital("₹ " + paid)
                .totalShares(shares)
                .faceValue(face)
                .topShareholders(List.of(
                        SubsidiaryDetailDto.ShareholderItemDto.builder().shareholderName("Atirath Holdings India Limited").category("Promoter").shares("5,40,00,000").percentage(51.68).status("Active").build(),
                        SubsidiaryDetailDto.ShareholderItemDto.builder().shareholderName("Ramesh Kumar").category("Individual").shares("1,50,00,000").percentage(14.35).status("Active").build(),
                        SubsidiaryDetailDto.ShareholderItemDto.builder().shareholderName("Global Renewables Ltd").category("Institutional").shares("1,20,00,000").percentage(11.48).status("Active").build(),
                        SubsidiaryDetailDto.ShareholderItemDto.builder().shareholderName("Public & Others").category("Public").shares("2,35,00,000").percentage(22.49).status("Active").build()
                ))
                .categoryBreakdown(List.of(
                        SubsidiaryDetailDto.CategoryShareDto.builder().category("Promoter").percentage(51.68).color("#3b82f6").build(),
                        SubsidiaryDetailDto.CategoryShareDto.builder().category("Institutional").percentage(11.48).color("#22c55e").build(),
                        SubsidiaryDetailDto.CategoryShareDto.builder().category("Individual").percentage(14.35).color("#f59e0b").build(),
                        SubsidiaryDetailDto.CategoryShareDto.builder().category("Public / Others").percentage(22.49).color("#94a3b8").build()
                ))
                .build();
    }

    private SubsidiaryDetailDto.MembersTabDto buildDefaultMembers(int total, int active, int kyc, int corp, int indiv) {
        return SubsidiaryDetailDto.MembersTabDto.builder()
                .totalMembers(total)
                .activeMembers(active)
                .pendingKyc(kyc)
                .corporateMembers(corp)
                .individualMembers(indiv)
                .memberList(List.of(
                        SubsidiaryDetailDto.MemberItemDto.builder().id("MEM-001").memberName("Ramesh Kumar").type("Individual").email("ramesh.k@example.com").joinDate("15 Jan 2022").kycStatus("Verified").status("Active").build(),
                        SubsidiaryDetailDto.MemberItemDto.builder().id("MEM-002").memberName("Priya Verma").type("Individual").email("priya.v@example.com").joinDate("18 Feb 2022").kycStatus("Verified").status("Active").build(),
                        SubsidiaryDetailDto.MemberItemDto.builder().id("MEM-003").memberName("Global Renewables Ltd").type("Corporate").email("contact@globalrenewables.com").joinDate("10 May 2022").kycStatus("Verified").status("Active").build(),
                        SubsidiaryDetailDto.MemberItemDto.builder().id("MEM-004").memberName("Vikram Shah").type("Individual").email("vikram.s@example.com").joinDate("22 Aug 2022").kycStatus("Verified").status("Active").build(),
                        SubsidiaryDetailDto.MemberItemDto.builder().id("MEM-005").memberName("Sunil Reddy").type("Individual").email("sunil.r@example.com").joinDate("05 Nov 2022").kycStatus("Pending").status("Pending").build()
                ))
                .build();
    }

    private SubsidiaryDetailDto.TradingActivityTabDto buildDefaultTradingActivity(int trades, String vol, String buyVol, String sellVol, int open) {
        return SubsidiaryDetailDto.TradingActivityTabDto.builder()
                .totalTrades(trades)
                .tradeVolume(vol)
                .buyVolume(buyVol)
                .sellVolume(sellVol)
                .openOrdersCount(open)
                .recentTrades(List.of(
                        SubsidiaryDetailDto.SubsidiaryTradeItemDto.builder().tradeId("TRD-9021").dateTime("16 Sep 2026, 11:24 AM").orderType("Buy").quantity("10,000").price("₹ 245.60").totalValue("₹ 24,56,000").status("Executed").build(),
                        SubsidiaryDetailDto.SubsidiaryTradeItemDto.builder().tradeId("TRD-9020").dateTime("16 Sep 2026, 10:18 AM").orderType("Sell").quantity("5,000").price("₹ 180.30").totalValue("₹ 9,01,500").status("Executed").build(),
                        SubsidiaryDetailDto.SubsidiaryTradeItemDto.builder().tradeId("TRD-9019").dateTime("16 Sep 2026, 09:45 AM").orderType("Buy").quantity("8,000").price("₹ 98.75").totalValue("₹ 7,90,000").status("Executed").build(),
                        SubsidiaryDetailDto.SubsidiaryTradeItemDto.builder().tradeId("TRD-9018").dateTime("15 Sep 2026, 04:12 PM").orderType("Sell").quantity("12,000").price("₹ 76.40").totalValue("₹ 9,16,800").status("Executed").build()
                ))
                .build();
    }

    private SubsidiaryDetailDto.ComplianceTabDto buildDefaultCompliance() {
        return SubsidiaryDetailDto.ComplianceTabDto.builder()
                .overallStatus("Compliant")
                .lastAuditDate("15 Jul 2026")
                .nextFilingDueDate("30 Sep 2026")
                .items(List.of(
                        SubsidiaryDetailDto.ComplianceItemDto.builder().title("ROC Filings").description("All filings up to date").status("Compliant").build(),
                        SubsidiaryDetailDto.ComplianceItemDto.builder().title("GST Compliance").description("Returns filed up to date").status("Compliant").build(),
                        SubsidiaryDetailDto.ComplianceItemDto.builder().title("Income Tax Filings").description("Latest return filed for FY 2024-25").status("Compliant").build(),
                        SubsidiaryDetailDto.ComplianceItemDto.builder().title("KYC & AML").description("All director and member KYC verified").status("Compliant").build(),
                        SubsidiaryDetailDto.ComplianceItemDto.builder().title("Regulatory Documents").description("All mandatory documents updated").status("Compliant").build()
                ))
                .records(List.of(
                        SubsidiaryDetailDto.ComplianceRecordDto.builder().filingType("AOC-4 (Financial Statements)").dueDate("30 Oct 2025").submittedDate("25 Oct 2025").acknowledgementNo("ROC-HYD-99881").status("Approved").build(),
                        SubsidiaryDetailDto.ComplianceRecordDto.builder().filingType("MGT-7 (Annual Return)").dueDate("29 Nov 2025").submittedDate("20 Nov 2025").acknowledgementNo("ROC-HYD-99882").status("Approved").build(),
                        SubsidiaryDetailDto.ComplianceRecordDto.builder().filingType("GSTR-3B (August 2026)").dueDate("20 Sep 2026").submittedDate("18 Sep 2026").acknowledgementNo("GST-TG-123456").status("Approved").build()
                ))
                .build();
    }

    private SubsidiaryDetailDto.DocumentsTabDto buildDefaultDocuments(String company) {
        return SubsidiaryDetailDto.DocumentsTabDto.builder()
                .totalDocuments(8)
                .verifiedDocuments(8)
                .pendingVerification(0)
                .documentList(List.of(
                        SubsidiaryDetailDto.DocumentItemDto.builder().id("DOC-01").documentName("Certificate of Incorporation").category("Statutory").fileType("PDF").uploadDate("12 Jan 2021").status("Verified").downloadUrl("/api/v1/documents/cert-incorp.pdf").build(),
                        SubsidiaryDetailDto.DocumentItemDto.builder().id("DOC-02").documentName("Memorandum of Association (MOA)").category("Statutory").fileType("PDF").uploadDate("12 Jan 2021").status("Verified").downloadUrl("/api/v1/documents/moa.pdf").build(),
                        SubsidiaryDetailDto.DocumentItemDto.builder().id("DOC-03").documentName("Articles of Association (AOA)").category("Statutory").fileType("PDF").uploadDate("12 Jan 2021").status("Verified").downloadUrl("/api/v1/documents/aoa.pdf").build(),
                        SubsidiaryDetailDto.DocumentItemDto.builder().id("DOC-04").documentName("Company PAN Card").category("Tax").fileType("PDF").uploadDate("15 Jan 2021").status("Verified").downloadUrl("/api/v1/documents/pan.pdf").build(),
                        SubsidiaryDetailDto.DocumentItemDto.builder().id("DOC-05").documentName("GST Registration Certificate").category("Tax").fileType("PDF").uploadDate("20 Jan 2021").status("Verified").downloadUrl("/api/v1/documents/gst.pdf").build(),
                        SubsidiaryDetailDto.DocumentItemDto.builder().id("DOC-06").documentName("Audited Financial Statements FY 2024-25").category("Financial").fileType("PDF").uploadDate("15 Jul 2025").status("Verified").downloadUrl("/api/v1/documents/audit-2025.pdf").build()
                ))
                .build();
    }

    private SubsidiaryDetailDto.SettingsTabDto buildDefaultSettings(String company, String email, String phone) {
        return SubsidiaryDetailDto.SettingsTabDto.builder()
                .tradingStatus("Active")
                .minOrderQuantity("100")
                .maxOrderQuantity("50,000")
                .circuitBreakerPercentage("10%")
                .primaryContactName("Chief Compliance Officer")
                .primaryContactEmail(email)
                .primaryContactPhone(phone)
                .build();
    }

    private void seedShareCapital() {
        shareCapitalItems.clear();
        shareCapitalItems.add(GroupShareCapitalPageDto.SubsidiaryShareCapitalItemDto.builder()
                .id("sub-1").number(1).companyName("Atirath Traders India Pvt Ltd").initials("AT").sector("Trading")
                .authorizedCapitalCr(160.0).issuedCapitalCr(112.0).paidUpCapitalCr(104.5)
                .numberOfShares("10,45,00,000").faceValue(10).status("Active").build());

        shareCapitalItems.add(GroupShareCapitalPageDto.SubsidiaryShareCapitalItemDto.builder()
                .id("sub-2").number(2).companyName("Atirath Holdings India Limited").initials("AH").sector("Renewable Energy")
                .authorizedCapitalCr(120.0).issuedCapitalCr(84.0).paidUpCapitalCr(78.2)
                .numberOfShares("7,82,00,000").faceValue(10).status("Active").build());

        shareCapitalItems.add(GroupShareCapitalPageDto.SubsidiaryShareCapitalItemDto.builder()
                .id("sub-3").number(3).companyName("Atirath Technologies Pvt Ltd").initials("AT").sector("Technology")
                .authorizedCapitalCr(80.0).issuedCapitalCr(56.0).paidUpCapitalCr(50.4)
                .numberOfShares("5,04,00,000").faceValue(10).status("Active").build());

        shareCapitalItems.add(GroupShareCapitalPageDto.SubsidiaryShareCapitalItemDto.builder()
                .id("sub-4").number(4).companyName("Farm Vantara AgriTech Pvt Ltd").initials("FV").sector("Agriculture")
                .authorizedCapitalCr(60.0).issuedCapitalCr(42.0).paidUpCapitalCr(39.8)
                .numberOfShares("3,98,00,000").faceValue(10).status("Active").build());

        shareCapitalItems.add(GroupShareCapitalPageDto.SubsidiaryShareCapitalItemDto.builder()
                .id("sub-5").number(5).companyName("Goeazymart E-Commerce Pvt Ltd").initials("GE").sector("Retail")
                .authorizedCapitalCr(45.0).issuedCapitalCr(31.5).paidUpCapitalCr(28.4)
                .numberOfShares("2,84,00,000").faceValue(10).status("Active").build());

        shareCapitalItems.add(GroupShareCapitalPageDto.SubsidiaryShareCapitalItemDto.builder()
                .id("sub-6").number(6).companyName("Tri Energy Battery Pvt Ltd").initials("TB").sector("Energy Storage")
                .authorizedCapitalCr(35.0).issuedCapitalCr(24.5).paidUpCapitalCr(22.7)
                .numberOfShares("2,27,00,000").faceValue(10).status("Under Setup").build());

        shareCapitalActivities.clear();
        shareCapitalActivities.add(GroupShareCapitalPageDto.ShareCapitalActivityItemDto.builder()
                .dateTime("16 Sep 2026, 11:20 AM").company("Atirath Holdings").activity("Issued Capital").activityTheme("green")
                .description("Issued 5,00,000 shares").performedBy("Admin User").build());

        shareCapitalActivities.add(GroupShareCapitalPageDto.ShareCapitalActivityItemDto.builder()
                .dateTime("14 Sep 2026, 03:45 PM").company("Atirath Traders").activity("Share Allotment").activityTheme("teal")
                .description("Allotted 2,00,000 shares").performedBy("Compliance Team").build());

        shareCapitalActivities.add(GroupShareCapitalPageDto.ShareCapitalActivityItemDto.builder()
                .dateTime("12 Sep 2026, 10:30 AM").company("Farm Vantara").activity("Capital Increase").activityTheme("orange")
                .description("Increased authorized capital to ₹ 60 Cr").performedBy("Admin User").build());

        shareCapitalActivities.add(GroupShareCapitalPageDto.ShareCapitalActivityItemDto.builder()
                .dateTime("10 Sep 2026, 04:15 PM").company("Goeazymart").activity("Share Transfer").activityTheme("pink")
                .description("Transferred 50,000 shares").performedBy("Operations Team").build());

        shareCapitalActivities.add(GroupShareCapitalPageDto.ShareCapitalActivityItemDto.builder()
                .dateTime("08 Sep 2026, 11:05 AM").company("Atirath Technologies").activity("Share Buyback").activityTheme("purple")
                .description("Bought back 25,000 shares").performedBy("Finance Team").build());
    }

    private void seedShareholders() {
        shareholders.clear();
        shareholders.add(GroupShareholdersPageDto.ShareholderItemDto.builder()
                .id("sh-1").number(1).shareholderName("Atirath Holdings India Limited").initials("AH").type("Corporate")
                .category("Promoter").categoryTheme("orange").country("India").countryFlag("🇮🇳")
                .noOfShares("24,00,00,000").shareholdingPercent(24.0).totalValue("₹ 120.0 Cr").kycStatus("Verified").status("Active").build());

        shareholders.add(GroupShareholdersPageDto.ShareholderItemDto.builder()
                .id("sh-2").number(2).shareholderName("Ramesh Kumar").initials("RK").type("Individual")
                .category("Individual").categoryTheme("blue").country("India").countryFlag("🇮🇳")
                .noOfShares("8,50,00,000").shareholdingPercent(8.5).totalValue("₹ 42.5 Cr").kycStatus("Verified").status("Active").build());

        shareholders.add(GroupShareholdersPageDto.ShareholderItemDto.builder()
                .id("sh-3").number(3).shareholderName("Global Renewables Ltd").initials("GR").type("Corporate")
                .category("Institutional").categoryTheme("indigo").country("Singapore").countryFlag("🇸🇬")
                .noOfShares("6,20,00,000").shareholdingPercent(6.2).totalValue("₹ 31.0 Cr").kycStatus("Verified").status("Active").build());

        shareholders.add(GroupShareholdersPageDto.ShareholderItemDto.builder()
                .id("sh-4").number(4).shareholderName("Priya Verma").initials("PV").type("Individual")
                .category("Individual").categoryTheme("blue").country("India").countryFlag("🇮🇳")
                .noOfShares("5,80,00,000").shareholdingPercent(5.8).totalValue("₹ 29.0 Cr").kycStatus("Verified").status("Active").build());

        shareholders.add(GroupShareholdersPageDto.ShareholderItemDto.builder()
                .id("sh-5").number(5).shareholderName("Sunrise Capital").initials("SC").type("Corporate")
                .category("Foreign").categoryTheme("red").country("USA").countryFlag("🇺🇸")
                .noOfShares("4,60,00,000").shareholdingPercent(4.6).totalValue("₹ 23.0 Cr").kycStatus("Verified").status("Active").build());

        shareholders.add(GroupShareholdersPageDto.ShareholderItemDto.builder()
                .id("sh-6").number(6).shareholderName("Vikram Shah").initials("VS").type("Individual")
                .category("Individual").categoryTheme("blue").country("India").countryFlag("🇮🇳")
                .noOfShares("3,90,00,000").shareholdingPercent(3.9).totalValue("₹ 19.5 Cr").kycStatus("Verified").status("Active").build());

        shareholders.add(GroupShareholdersPageDto.ShareholderItemDto.builder()
                .id("sh-7").number(7).shareholderName("Sustainable Growth Fund").initials("SG").type("Corporate")
                .category("Institutional").categoryTheme("indigo").country("UK").countryFlag("🇬🇧")
                .noOfShares("3,40,00,000").shareholdingPercent(3.4).totalValue("₹ 17.0 Cr").kycStatus("Verified").status("Active").build());

        shareholders.add(GroupShareholdersPageDto.ShareholderItemDto.builder()
                .id("sh-8").number(8).shareholderName("Meera Reddy").initials("MR").type("Individual")
                .category("Individual").categoryTheme("blue").country("India").countryFlag("🇮🇳")
                .noOfShares("3,10,00,000").shareholdingPercent(3.1).totalValue("₹ 15.5 Cr").kycStatus("Verified").status("Active").build());

        shareholders.add(GroupShareholdersPageDto.ShareholderItemDto.builder()
                .id("sh-9").number(9).shareholderName("Atirath Family Trust").initials("AF").type("Trust")
                .category("Promoter").categoryTheme("orange").country("India").countryFlag("🇮🇳")
                .noOfShares("2,80,00,000").shareholdingPercent(2.8).totalValue("₹ 14.0 Cr").kycStatus("Verified").status("Active").build());

        shareholders.add(GroupShareholdersPageDto.ShareholderItemDto.builder()
                .id("sh-10").number(10).shareholderName("Others (374 shareholders)").initials("OT").type("Various")
                .category("Various").categoryTheme("grey").country("-").countryFlag("")
                .noOfShares("41,70,00,000").shareholdingPercent(41.7).totalValue("₹ 208.5 Cr").kycStatus("-").status("Active").build());
    }

    private void seedHoldings() {
        groupHoldings.clear();
        groupHoldings.add(GroupHoldingsPageDto.GroupHoldingItemDto.builder()
                .id("hld-1").number(1).companyName("Atirath Traders India Pvt Ltd").initials("AT").sector("Trading")
                .totalShares("10,45,00,000").shareholdingPercent(25.0).totalValue("₹ 312.5 Cr").lastValuation("₹ 299.80").change24h("+2.4%").changeTrend("positive").build());

        groupHoldings.add(GroupHoldingsPageDto.GroupHoldingItemDto.builder()
                .id("hld-2").number(2).companyName("Atirath Holdings India Limited").initials("AH").sector("Renewable Energy")
                .totalShares("7,82,00,000").shareholdingPercent(19.9).totalValue("₹ 248.0 Cr").lastValuation("₹ 317.40").change24h("+1.8%").changeTrend("positive").build());

        groupHoldings.add(GroupHoldingsPageDto.GroupHoldingItemDto.builder()
                .id("hld-3").number(3).companyName("Atirath Technologies Pvt Ltd").initials("AT").sector("Technology")
                .totalShares("5,04,00,000").shareholdingPercent(14.9).totalValue("₹ 186.4 Cr").lastValuation("₹ 369.80").change24h("-0.6%").changeTrend("negative").build());

        groupHoldings.add(GroupHoldingsPageDto.GroupHoldingItemDto.builder()
                .id("hld-4").number(4).companyName("Farm Vantara AgriTech Pvt Ltd").initials("FV").sector("Agriculture")
                .totalShares("3,98,00,000").shareholdingPercent(11.4).totalValue("₹ 142.6 Cr").lastValuation("₹ 358.20").change24h("+3.1%").changeTrend("positive").build());

        groupHoldings.add(GroupHoldingsPageDto.GroupHoldingItemDto.builder()
                .id("hld-5").number(5).companyName("Goeazymart E-Commerce Pvt Ltd").initials("GE").sector("Retail")
                .totalShares("2,84,00,000").shareholdingPercent(7.9).totalValue("₹ 98.5 Cr").lastValuation("₹ 347.10").change24h("+1.2%").changeTrend("positive").build());

        groupHoldings.add(GroupHoldingsPageDto.GroupHoldingItemDto.builder()
                .id("hld-6").number(6).companyName("Tri Energy Battery Pvt Ltd").initials("TB").sector("Energy Storage")
                .totalShares("2,27,00,000").shareholdingPercent(6.1).totalValue("₹ 76.5 Cr").lastValuation("₹ 336.80").change24h("+0.8%").changeTrend("positive").build());
    }

    private void seedMarketActivity() {
        marketActivityTrades.clear();
        marketActivityTrades.add(MarketActivityGroupPageDto.MarketActivityTradeDto.builder()
                .id("mat-1").number(1).dateTime("16 Sep 2026, 11:20 AM").company("Atirath Traders").companyInitials("AT")
                .type("Buy").security("Equity").quantity("50,000").price("299.80").tradeValue("1,49,90,000")
                .buyer("Ramesh Kumar").seller("Global Renewables").status("Settled").build());

        marketActivityTrades.add(MarketActivityGroupPageDto.MarketActivityTradeDto.builder()
                .id("mat-2").number(2).dateTime("16 Sep 2026, 10:35 AM").company("Atirath Holdings").companyInitials("AH")
                .type("Sell").security("Equity").quantity("25,000").price("317.40").tradeValue("79,35,000")
                .buyer("Sunrise Capital").seller("Priya Verma").status("Settled").build());

        marketActivityTrades.add(MarketActivityGroupPageDto.MarketActivityTradeDto.builder()
                .id("mat-3").number(3).dateTime("16 Sep 2026, 09:15 AM").company("Atirath Technologies").companyInitials("AT")
                .type("Buy").security("Equity").quantity("40,000").price("369.80").tradeValue("1,47,92,000")
                .buyer("Vikram Shah").seller("Meera Reddy").status("Pending").build());

        marketActivityTrades.add(MarketActivityGroupPageDto.MarketActivityTradeDto.builder()
                .id("mat-4").number(4).dateTime("15 Sep 2026, 04:45 PM").company("Farm Vantara").companyInitials("FV")
                .type("Sell").security("Equity").quantity("30,000").price("358.20").tradeValue("1,07,46,000")
                .buyer("Atirath Holdings").seller("Sustainable Growth").status("Settled").build());

        marketActivityTrades.add(MarketActivityGroupPageDto.MarketActivityTradeDto.builder()
                .id("mat-5").number(5).dateTime("15 Sep 2026, 03:20 PM").company("Goeazymart").companyInitials("GE")
                .type("Buy").security("Equity").quantity("60,000").price("347.10").tradeValue("2,08,26,000")
                .buyer("Priya Verma").seller("Atirath Traders").status("Settled").build());

        marketActivityTrades.add(MarketActivityGroupPageDto.MarketActivityTradeDto.builder()
                .id("mat-6").number(6).dateTime("15 Sep 2026, 11:05 AM").company("Tri Energy Battery").companyInitials("TB")
                .type("Sell").security("Equity").quantity("75,000").price("336.80").tradeValue("2,52,60,000")
                .buyer("Global Renewables").seller("Ramesh Kumar").status("Pending").build());
    }

    private void seedTradeMonitoring() {
        tradeMonitoringItems.clear();
        tradeMonitoringItems.add(GroupTradeMonitoringPageDto.TradeMonitoringItemDto.builder()
                .id("mon-1").number(1).tradeId("TRD-100482").dateTime("16 Sep 2026, 11:20 AM")
                .buyer("Ramesh Kumar").seller("Global Renewables").company("Atirath Traders").security("Equity")
                .quantity("50,000").price("299.80").tradeValue("1,49,90,000").type("Buy").status("Settled").settlementDate("18 Sep 2026").build());

        tradeMonitoringItems.add(GroupTradeMonitoringPageDto.TradeMonitoringItemDto.builder()
                .id("mon-2").number(2).tradeId("TRD-100481").dateTime("16 Sep 2026, 10:35 AM")
                .buyer("Priya Verma").seller("Sunrise Capital").company("Atirath Holdings").security("Equity")
                .quantity("25,000").price("317.40").tradeValue("79,35,000").type("Sell").status("Settled").settlementDate("18 Sep 2026").build());

        tradeMonitoringItems.add(GroupTradeMonitoringPageDto.TradeMonitoringItemDto.builder()
                .id("mon-3").number(3).tradeId("TRD-100480").dateTime("15 Sep 2026, 04:45 PM")
                .buyer("Vikram Shah").seller("Meera Reddy").company("Atirath Technologies").security("Equity")
                .quantity("40,000").price("369.80").tradeValue("1,47,92,000").type("Buy").status("Pending").settlementDate("17 Sep 2026").build());

        tradeMonitoringItems.add(GroupTradeMonitoringPageDto.TradeMonitoringItemDto.builder()
                .id("mon-4").number(4).tradeId("TRD-100479").dateTime("15 Sep 2026, 03:20 PM")
                .buyer("Sustain. Growth Fund").seller("Atirath Traders").company("Farm Vantara").security("Equity")
                .quantity("30,000").price("358.20").tradeValue("1,07,46,000").type("Sell").status("Settled").settlementDate("16 Sep 2026").build());

        tradeMonitoringItems.add(GroupTradeMonitoringPageDto.TradeMonitoringItemDto.builder()
                .id("mon-5").number(5).tradeId("TRD-100478").dateTime("15 Sep 2026, 11:05 AM")
                .buyer("Amit Singh").seller("Ramesh Kumar").company("Goeazymart").security("Equity")
                .quantity("60,000").price("347.10").tradeValue("2,08,26,000").type("Buy").status("Settled").settlementDate("16 Sep 2026").build());

        tradeMonitoringItems.add(GroupTradeMonitoringPageDto.TradeMonitoringItemDto.builder()
                .id("mon-6").number(6).tradeId("TRD-100477").dateTime("14 Sep 2026, 02:15 PM")
                .buyer("Global Renewables").seller("Tri Energy Battery").company("Tri Energy Battery").security("Equity")
                .quantity("75,000").price("336.80").tradeValue("2,52,60,000").type("Sell").status("Failed").settlementDate("-").build());

        tradeMonitoringItems.add(GroupTradeMonitoringPageDto.TradeMonitoringItemDto.builder()
                .id("mon-7").number(7).tradeId("TRD-100476").dateTime("14 Sep 2026, 11:40 AM")
                .buyer("Meera Reddy").seller("Priya Verma").company("Atirath Holdings").security("Equity")
                .quantity("20,000").price("315.60").tradeValue("63,12,000").type("Buy").status("Settled").settlementDate("15 Sep 2026").build());

        tradeMonitoringItems.add(GroupTradeMonitoringPageDto.TradeMonitoringItemDto.builder()
                .id("mon-8").number(8).tradeId("TRD-100475").dateTime("13 Sep 2026, 05:10 PM")
                .buyer("Vikram Shah").seller("Amit Singh").company("Atirath Traders").security("Equity")
                .quantity("15,000").price("302.40").tradeValue("45,36,000").type("Buy").status("Settled").settlementDate("15 Sep 2026").build());

        unusualTradeActivities.clear();
        unusualTradeActivities.add(GroupTradeMonitoringPageDto.UnusualTradeActivityItemDto.builder()
                .dateTime("16 Sep 2026, 10:35 AM").tradeId("TRD-100481").company("Atirath Holdings")
                .alertType("High Value Trade").alertTheme("yellow").details("Trade value ₹ 79.35 L (3x avg)")
                .status("Under Review").statusTheme("yellow").build());

        unusualTradeActivities.add(GroupTradeMonitoringPageDto.UnusualTradeActivityItemDto.builder()
                .dateTime("15 Sep 2026, 04:45 PM").tradeId("TRD-100480").company("Atirath Technologies")
                .alertType("Large Quantity").alertTheme("pink").details("Quantity 40,000 (4x avg)")
                .status("Investigating").statusTheme("orange").build());

        unusualTradeActivities.add(GroupTradeMonitoringPageDto.UnusualTradeActivityItemDto.builder()
                .dateTime("14 Sep 2026, 02:15 PM").tradeId("TRD-100477").company("Tri Energy Battery")
                .alertType("Price Variation").alertTheme("blue").details("Price 12% above market")
                .status("Resolved").statusTheme("blue").build());

        unusualTradeActivities.add(GroupTradeMonitoringPageDto.UnusualTradeActivityItemDto.builder()
                .dateTime("13 Sep 2026, 11:20 AM").tradeId("TRD-100472").company("Goeazymart")
                .alertType("Multiple Trades").alertTheme("red").details("5 trades in 10 minutes")
                .status("Under Review").statusTheme("yellow").build());
    }

    private void seedEscrowSettlement() {
        escrowTransactions.clear();
        escrowTransactions.add(EscrowSettlementGroupPageDto.EscrowTransactionItemDto.builder()
                .id("esc-1").number(1).tradeId("ESC-100482").dateTime("16 Sep 2026, 11:20 AM")
                .buyer("Ramesh Kumar").seller("Global Renewables").company("Atirath Traders")
                .escrowAmount("1,49,90,000").tradeValue("1,49,90,000").settlementDate("18 Sep 2026")
                .status("Settled").statusTheme("green").build());

        escrowTransactions.add(EscrowSettlementGroupPageDto.EscrowTransactionItemDto.builder()
                .id("esc-2").number(2).tradeId("ESC-100481").dateTime("16 Sep 2026, 10:35 AM")
                .buyer("Priya Verma").seller("Sunrise Capital").company("Atirath Holdings")
                .escrowAmount("79,35,000").tradeValue("79,35,000").settlementDate("18 Sep 2026")
                .status("Pending").statusTheme("yellow").build());

        escrowTransactions.add(EscrowSettlementGroupPageDto.EscrowTransactionItemDto.builder()
                .id("esc-3").number(3).tradeId("ESC-100480").dateTime("15 Sep 2026, 04:45 PM")
                .buyer("Vikram Shah").seller("Meera Reddy").company("Atirath Technologies")
                .escrowAmount("1,47,92,000").tradeValue("1,47,92,000").settlementDate("17 Sep 2026")
                .status("Under Review").statusTheme("blue").build());

        escrowTransactions.add(EscrowSettlementGroupPageDto.EscrowTransactionItemDto.builder()
                .id("esc-4").number(4).tradeId("ESC-100479").dateTime("15 Sep 2026, 03:20 PM")
                .buyer("Sustain Growth Fund").seller("Atirath Traders").company("Farm Vantara")
                .escrowAmount("1,07,46,000").tradeValue("1,07,46,000").settlementDate("16 Sep 2026")
                .status("Disputed").statusTheme("red").build());

        escrowTransactions.add(EscrowSettlementGroupPageDto.EscrowTransactionItemDto.builder()
                .id("esc-5").number(5).tradeId("ESC-100478").dateTime("15 Sep 2026, 11:05 AM")
                .buyer("Amit Singh").seller("Ramesh Kumar").company("Goeazymart")
                .escrowAmount("2,08,26,000").tradeValue("2,08,26,000").settlementDate("16 Sep 2026")
                .status("Settled").statusTheme("green").build());

        escrowTransactions.add(EscrowSettlementGroupPageDto.EscrowTransactionItemDto.builder()
                .id("esc-6").number(6).tradeId("ESC-100477").dateTime("14 Sep 2026, 02:15 PM")
                .buyer("Global Renewables").seller("Tri Energy Battery").company("Tri Energy Battery")
                .escrowAmount("2,52,60,000").tradeValue("2,52,60,000").settlementDate("15 Sep 2026")
                .status("Pending").statusTheme("yellow").build());

        escrowTransactions.add(EscrowSettlementGroupPageDto.EscrowTransactionItemDto.builder()
                .id("esc-7").number(7).tradeId("ESC-100476").dateTime("14 Sep 2026, 11:40 AM")
                .buyer("Meera Reddy").seller("Priya Verma").company("Atirath Holdings")
                .escrowAmount("63,12,000").tradeValue("63,12,000").settlementDate("15 Sep 2026")
                .status("Settled").statusTheme("green").build());

        escrowTransactions.add(EscrowSettlementGroupPageDto.EscrowTransactionItemDto.builder()
                .id("esc-8").number(8).tradeId("ESC-100475").dateTime("13 Sep 2026, 05:10 PM")
                .buyer("Vikram Shah").seller("Amit Singh").company("Atirath Traders")
                .escrowAmount("45,36,000").tradeValue("45,36,000").settlementDate("14 Sep 2026")
                .status("Under Review").statusTheme("blue").build());
    }

    private void seedApprovals() {
        approvalsList.clear();
        approvalsList.add(ApprovalsPageDto.ApprovalItemDto.builder()
                .id("apr-1").number(1).requestId("APR-2026-1042").dateTime("16 Sep 2026, 11:20 AM")
                .approvalType("Share Transfer").company("Atirath Traders").requestedBy("Ramesh Kumar")
                .amountOrDetails("50,000 shares").priority("High").priorityTheme("red").status("Pending").statusTheme("yellow").build());

        approvalsList.add(ApprovalsPageDto.ApprovalItemDto.builder()
                .id("apr-2").number(2).requestId("APR-2026-1041").dateTime("16 Sep 2026, 10:35 AM")
                .approvalType("Trade Settlement").company("Atirath Holdings").requestedBy("Priya Verma")
                .amountOrDetails("₹ 79,35,000").priority("High").priorityTheme("red").status("Under Review").statusTheme("blue").build());

        approvalsList.add(ApprovalsPageDto.ApprovalItemDto.builder()
                .id("apr-3").number(3).requestId("APR-2026-1040").dateTime("15 Sep 2026, 04:45 PM")
                .approvalType("Escrow Release").company("Atirath Technologies").requestedBy("Vikram Shah")
                .amountOrDetails("₹ 1,47,92,000").priority("Medium").priorityTheme("yellow").status("Pending").statusTheme("yellow").build());

        approvalsList.add(ApprovalsPageDto.ApprovalItemDto.builder()
                .id("apr-4").number(4).requestId("APR-2026-1039").dateTime("15 Sep 2026, 03:20 PM")
                .approvalType("New Investor").company("Farm Vantara").requestedBy("Sustain Growth Fund")
                .amountOrDetails("30,000 shares").priority("High").priorityTheme("red").status("Pending").statusTheme("yellow").build());

        approvalsList.add(ApprovalsPageDto.ApprovalItemDto.builder()
                .id("apr-5").number(5).requestId("APR-2026-1038").dateTime("15 Sep 2026, 11:05 AM")
                .approvalType("Capital Increase").company("Goeazymart").requestedBy("Amit Singh")
                .amountOrDetails("₹ 2,08,26,000").priority("Critical").priorityTheme("purple").status("Under Review").statusTheme("blue").build());

        approvalsList.add(ApprovalsPageDto.ApprovalItemDto.builder()
                .id("apr-6").number(6).requestId("APR-2026-1037").dateTime("14 Sep 2026, 02:15 PM")
                .approvalType("Dividend Payout").company("Tri Energy Battery").requestedBy("Global Renewables")
                .amountOrDetails("₹ 63,12,000").priority("Medium").priorityTheme("yellow").status("Approved").statusTheme("green").build());

        approvalsList.add(ApprovalsPageDto.ApprovalItemDto.builder()
                .id("apr-7").number(7).requestId("APR-2026-1036").dateTime("14 Sep 2026, 11:40 AM")
                .approvalType("Related Party Txn").company("Meera Reddy").requestedBy("Priya Verma")
                .amountOrDetails("₹ 45,36,000").priority("High").priorityTheme("red").status("Rejected").statusTheme("red").build());

        approvalsList.add(ApprovalsPageDto.ApprovalItemDto.builder()
                .id("apr-8").number(8).requestId("APR-2026-1035").dateTime("13 Sep 2026, 05:10 PM")
                .approvalType("Policy Update").company("Atirath Traders").requestedBy("Amit Singh")
                .amountOrDetails("Updated policy doc").priority("Low").priorityTheme("green").status("Approved").statusTheme("green").build());

        approvalsList.add(ApprovalsPageDto.ApprovalItemDto.builder()
                .id("apr-9").number(9).requestId("APR-2026-1034").dateTime("13 Sep 2026, 04:22 PM")
                .approvalType("Vendor Onboarding").company("Atirath Holdings").requestedBy("Ramesh Kumar")
                .amountOrDetails("New vendor - ABC Ltd").priority("Medium").priorityTheme("yellow").status("Pending").statusTheme("yellow").build());

        approvalsList.add(ApprovalsPageDto.ApprovalItemDto.builder()
                .id("apr-10").number(10).requestId("APR-2026-1033").dateTime("12 Sep 2026, 03:18 PM")
                .approvalType("System Access").company("Atirath Technologies").requestedBy("Vikram Shah")
                .amountOrDetails("Access for 5 users").priority("Low").priorityTheme("green").status("Approved").statusTheme("green").build());

        ApprovalDetailDto apr1042 = ApprovalDetailDto.builder()
                .requestId("APR-2026-1042")
                .title("Share Transfer Request")
                .requestedOn("Requested on 16 Sep 2026, 11:20 AM")
                .status("Pending")
                .statusTheme("yellow")
                .tabs(List.of("Details", "History", "Comments (2)", "Related Docs (3)"))
                .activeTab("Details")
                .approvalType("Share Transfer")
                .company("Atirath Traders India Pvt Ltd")
                .requestedBy("Ramesh Kumar (Individual)")
                .dateTime("16 Sep 2026, 11:20 AM")
                .shares("50,000 shares (0.5%)")
                .shareholder("Ramesh Kumar")
                .transferee("Global Renewables Ltd")
                .consideration("₹ 2,99,80,000 (₹ 299.80 per share)")
                .purpose("Secondary market transfer")
                .priority("High")
                .description("Requesting approval for transfer of 50,000 shares from Ramesh Kumar to Global Renewables Ltd at a price of ₹ 299.80 per share.")
                .attachments(List.of(
                        new ApprovalDetailDto.ApprovalAttachmentDto("Share Transfer Agreement.pdf", "1.2 MB", "https://atirathgroup.com/docs/share-transfer.pdf"),
                        new ApprovalDetailDto.ApprovalAttachmentDto("Valuation Report.pdf", "2.4 MB", "https://atirathgroup.com/docs/valuation.pdf"),
                        new ApprovalDetailDto.ApprovalAttachmentDto("KYC Documents.zip", "5.1 MB", "https://atirathgroup.com/docs/kyc.zip")
                ))
                .build();

        approvalDetailsMap.put("APR-2026-1042", apr1042);
    }

    private void seedInvestorRequests() {
        investorRequestsList.clear();
        investorRequestsList.add(ExternalInvestorRequestsPageDto.InvestorRequestItemDto.builder()
                .id("inv-1").number(1).requestId("INV-2026-0036").dateTime("16 Sep 2026, 11:20 AM")
                .investorName("Rajesh Mehta").company("Farm Vantara").sector("Agriculture")
                .investmentAmount("25,00,00,000").status("Pending").statusTheme("yellow").build());

        investorRequestsList.add(ExternalInvestorRequestsPageDto.InvestorRequestItemDto.builder()
                .id("inv-2").number(2).requestId("INV-2026-0035").dateTime("16 Sep 2026, 10:35 AM")
                .investorName("Global Green Fund").company("Atirath Holdings").sector("Renewable Energy")
                .investmentAmount("40,00,00,000").status("Under Review").statusTheme("blue").build());

        investorRequestsList.add(ExternalInvestorRequestsPageDto.InvestorRequestItemDto.builder()
                .id("inv-3").number(3).requestId("INV-2026-0034").dateTime("15 Sep 2026, 04:45 PM")
                .investorName("Sunrise Capital").company("Goeazymart").sector("FMCG / Retail")
                .investmentAmount("15,00,00,000").status("Approved").statusTheme("green").build());

        investorRequestsList.add(ExternalInvestorRequestsPageDto.InvestorRequestItemDto.builder()
                .id("inv-4").number(4).requestId("INV-2026-0033").dateTime("15 Sep 2026, 03:20 PM")
                .investorName("Vikram Shah").company("Atirath Technologies").sector("Technology")
                .investmentAmount("10,00,00,000").status("Pending").statusTheme("yellow").build());

        investorRequestsList.add(ExternalInvestorRequestsPageDto.InvestorRequestItemDto.builder()
                .id("inv-5").number(5).requestId("INV-2026-0032").dateTime("15 Sep 2026, 11:05 AM")
                .investorName("Meera Reddy").company("Atirath Traders").sector("Import-Export")
                .investmentAmount("8,00,00,000").status("Rejected").statusTheme("red").build());

        investorRequestsList.add(ExternalInvestorRequestsPageDto.InvestorRequestItemDto.builder()
                .id("inv-6").number(6).requestId("INV-2026-0031").dateTime("14 Sep 2026, 02:15 PM")
                .investorName("Horizon Ventures").company("Tri Energy Battery").sector("Energy Storage")
                .investmentAmount("30,00,00,000").status("Under Review").statusTheme("blue").build());

        investorRequestsList.add(ExternalInvestorRequestsPageDto.InvestorRequestItemDto.builder()
                .id("inv-7").number(7).requestId("INV-2026-0030").dateTime("14 Sep 2026, 11:40 AM")
                .investorName("Amit Singh").company("Renewable Energy").sector("Renewable Energy")
                .investmentAmount("12,00,00,000").status("Approved").statusTheme("green").build());

        investorRequestsList.add(ExternalInvestorRequestsPageDto.InvestorRequestItemDto.builder()
                .id("inv-8").number(8).requestId("INV-2026-0029").dateTime("13 Sep 2026, 05:10 PM")
                .investorName("Pearl Investments").company("NEU1 Smart Stay").sector("Hospitality")
                .investmentAmount("20,00,00,000").status("Pending").statusTheme("yellow").build());

        investorRequestsList.add(ExternalInvestorRequestsPageDto.InvestorRequestItemDto.builder()
                .id("inv-9").number(9).requestId("INV-2026-0028").dateTime("13 Sep 2026, 04:22 PM")
                .investorName("Karthik R").company("CS INTERIO").sector("Real Estate")
                .investmentAmount("5,00,00,000").status("Rejected").statusTheme("red").build());

        investorRequestsList.add(ExternalInvestorRequestsPageDto.InvestorRequestItemDto.builder()
                .id("inv-10").number(10).requestId("INV-2026-0027").dateTime("12 Sep 2026, 03:18 PM")
                .investorName("Aegis Global").company("Royal One Heating").sector("Manufacturing")
                .investmentAmount("21,00,00,000").status("Approved").statusTheme("green").build());

        InvestorRequestDetailDto inv0036 = InvestorRequestDetailDto.builder()
                .requestId("INV-2026-0036")
                .investorName("Rajesh Mehta")
                .requestedOn("Requested on 16 Sep 2026, 11:20 AM")
                .status("Pending")
                .statusTheme("yellow")
                .tabs(List.of("Details", "Documents (4)", "Comments (3)", "History"))
                .activeTab("Details")
                .investorType("Individual (HNI)")
                .contactEmail("rajesh.mehta@email.com")
                .contactPhone("+91 98765 43210")
                .company("Farm Vantara")
                .sector("Agriculture")
                .investmentType("Equity")
                .proposedAmount("₹ 25,00,00,000")
                .proposedEquity("5%")
                .purpose("Expansion & Working Capital")
                .expectedRoi("18% (IRR)")
                .timeline("3 - 5 Years")
                .assignedTo("Finance Team")
                .description("Interested in investing in Farm Vantara to support expansion across AP & Telangana.")
                .build();

        investorDetailsMap.put("INV-2026-0036", inv0036);
    }
}
