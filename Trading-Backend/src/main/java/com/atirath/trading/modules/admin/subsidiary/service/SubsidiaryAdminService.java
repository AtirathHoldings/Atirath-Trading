package com.atirath.trading.modules.admin.subsidiary.service;

import com.atirath.trading.modules.admin.parent.dto.AdminUserHeaderDto;
import com.atirath.trading.modules.admin.parent.dto.KpiCardDto;
import com.atirath.trading.modules.admin.subsidiary.dto.*;
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
public class SubsidiaryAdminService {

    private AdminUserHeaderDto adminUser;

    // Performance Overview Store
    private final List<SubsidiaryAdminDashboardDto.SubsidiaryPerformanceItemDto> performanceItems = new CopyOnWriteArrayList<>();
    private final List<SubsidiaryAdminDashboardDto.SubsidiaryActivityItemDto> dashboardActivities = new CopyOnWriteArrayList<>();
    private final List<SubsidiaryAdminDashboardDto.SubsidiaryMilestoneItemDto> upcomingMilestones = new CopyOnWriteArrayList<>();

    // Company Profile Store
    private final Map<String, SubsidiaryCompanyProfileDto> profilesMap = new ConcurrentHashMap<>();

    // Share Classes Store
    private final List<SubsidiaryShareClassesPageDto.ShareClassItemDto> shareClassesList = new CopyOnWriteArrayList<>();
    private final Map<String, ShareClassDetailDto> shareClassDetailsMap = new ConcurrentHashMap<>();
    private final AtomicInteger shareClassCounter = new AtomicInteger(4);

    // Share Issuance Store
    private final List<SubsidiaryShareIssuancePageDto.ShareIssuanceItemDto> issuanceRequestsList = new CopyOnWriteArrayList<>();
    private final List<SubsidiaryShareIssuancePageDto.IssuanceApprovalDto> recentApprovalsList = new CopyOnWriteArrayList<>();
    private final List<SubsidiaryShareIssuancePageDto.IssuanceActivityLogDto> issuanceActivityLogList = new CopyOnWriteArrayList<>();
    private final AtomicInteger issuanceCounter = new AtomicInteger(6);

    // Shareholders Store
    private final List<SubsidiaryShareholdersPageDto.ShareholderItemDto> shareholdersList = new CopyOnWriteArrayList<>();
    private final Map<String, ShareholderDetailDto> shareholderDetailsMap = new ConcurrentHashMap<>();
    private final AtomicInteger shareholderCounter = new AtomicInteger(10);

    // Cap Table Store
    private final List<SubsidiaryCapTablePageDto.CapTableRowDto> capTableRowsList = new CopyOnWriteArrayList<>();
    private final List<SubsidiaryCapTablePageDto.CapTableTransactionDto> capTableTransactionsList = new CopyOnWriteArrayList<>();

    // Share Transfer Rules Store
    private final List<SubsidiaryShareTransferRulesPageDto.ShareTransferRuleItemDto> shareTransferRulesList = new CopyOnWriteArrayList<>();
    private final Map<String, ShareTransferRuleDetailDto> shareTransferRuleDetailsMap = new ConcurrentHashMap<>();
    private final AtomicInteger shareTransferRuleCounter = new AtomicInteger(8);

    // Trading Rules Store
    private final List<SubsidiaryTradingRulesPageDto.TradingRuleItemDto> tradingRulesList = new CopyOnWriteArrayList<>();
    private final Map<String, TradingRuleDetailDto> tradingRuleDetailsMap = new ConcurrentHashMap<>();
    private final AtomicInteger tradingRuleCounter = new AtomicInteger(10);

    // Company Approvers Store
    private final List<SubsidiaryApproversPageDto.CompanyApproverItemDto> companyApproversList = new CopyOnWriteArrayList<>();
    private final Map<String, CompanyApproverDetailDto> companyApproverDetailsMap = new ConcurrentHashMap<>();
    private final AtomicInteger approverCounter = new AtomicInteger(8);

    // Company Documents Store
    private final List<SubsidiaryDocumentsPageDto.CompanyDocumentItemDto> companyDocumentsList = new CopyOnWriteArrayList<>();
    private final Map<String, CompanyDocumentDetailDto> companyDocumentDetailsMap = new ConcurrentHashMap<>();
    private final AtomicInteger documentCounter = new AtomicInteger(10);

    // Company Transactions Store
    private final List<SubsidiaryTransactionsPageDto.CompanyTransactionItemDto> companyTransactionsList = new CopyOnWriteArrayList<>();
    private final Map<String, CompanyTransactionDetailDto> companyTransactionDetailsMap = new ConcurrentHashMap<>();
    private final AtomicInteger transactionCounter = new AtomicInteger(10);

    // Company Reports Store
    private final List<SubsidiaryReportsPageDto.ReportItemDto> availableReportsList = new CopyOnWriteArrayList<>();
    private final AtomicInteger reportCounter = new AtomicInteger(8);

    @PostConstruct
    public void init() {
        log.info("[SubsidiaryAdminService] Initializing Subsidiary Admin datasets matching UI designs...");

        adminUser = AdminUserHeaderDto.builder()
                .name("Admin User")
                .initials("AD")
                .role("Subsidiary Administration")
                .currentDate("01 Sep 2026 - 16 Sep 2026")
                .unreadNotifications(5)
                .build();

        seedDashboard();
        seedCompanyProfile();
        seedShareClasses();
        seedShareIssuance();
        seedShareholders();
        seedCapTable();
        seedShareTransferRules();
        seedTradingRules();
        seedCompanyApprovers();
        seedCompanyDocuments();
        seedCompanyTransactions();
        seedCompanyReports();

        log.info("[SubsidiaryAdminService] Initialization completed. Ready for Subsidiary Admin requests.");
    }

    // ==========================================
    // 1. SUBSIDIARY DASHBOARD
    // ==========================================
    public SubsidiaryAdminDashboardDto getSubsidiaryDashboard(String subsidiaryFilter, String dateRange) {
        List<KpiCardDto> kpis = List.of(
                KpiCardDto.builder().title("Total Subsidiaries").value("8").change("+14%").comparisonText("vs last quarter").trend("positive").icon("building").color("blue").build(),
                KpiCardDto.builder().title("Active Projects").value("24").change("+26%").comparisonText("vs last quarter").trend("positive").icon("pie-chart").color("green").build(),
                KpiCardDto.builder().title("Total Employees").value("524").change("+12%").comparisonText("vs last quarter").trend("positive").icon("users").color("pink").build(),
                KpiCardDto.builder().title("Total Investment").value("₹ 186.4 Cr").change("+22%").comparisonText("vs last quarter").trend("positive").icon("rupee").color("green").build(),
                KpiCardDto.builder().title("Total Revenue (YTD)").value("₹ 124.8 Cr").change("+18%").comparisonText("vs last quarter").trend("positive").icon("bar-chart").color("orange").build()
        );

        List<SubsidiaryAdminDashboardDto.SubsidiaryPerformanceItemDto> filteredPerformance = performanceItems.stream()
                .filter(p -> {
                    if (subsidiaryFilter != null && !subsidiaryFilter.isBlank() && !subsidiaryFilter.equalsIgnoreCase("All Subsidiaries")) {
                        return p.getSubsidiaryName().toLowerCase().contains(subsidiaryFilter.toLowerCase());
                    }
                    return true;
                }).collect(Collectors.toList());

        SubsidiaryAdminDashboardDto.ProjectsStatusDonutDto projectsStatus = SubsidiaryAdminDashboardDto.ProjectsStatusDonutDto.builder()
                .totalProjects(24)
                .slices(List.of(
                        new SubsidiaryAdminDashboardDto.ProjectStatusSliceDto("Open", 6, 25.0, "#3b82f6"),
                        new SubsidiaryAdminDashboardDto.ProjectStatusSliceDto("In Progress", 10, 41.7, "#22c55e"),
                        new SubsidiaryAdminDashboardDto.ProjectStatusSliceDto("Completed", 5, 20.8, "#f59e0b"),
                        new SubsidiaryAdminDashboardDto.ProjectStatusSliceDto("On Hold", 2, 8.3, "#a855f7"),
                        new SubsidiaryAdminDashboardDto.ProjectStatusSliceDto("Cancelled", 1, 4.2, "#ef4444")
                )).build();

        List<SubsidiaryAdminDashboardDto.ComplianceCategoryItemDto> compliance = List.of(
                new SubsidiaryAdminDashboardDto.ComplianceCategoryItemDto("Statutory Compliance", 92, "#22c55e"),
                new SubsidiaryAdminDashboardDto.ComplianceCategoryItemDto("Environmental", 78, "#3b82f6"),
                new SubsidiaryAdminDashboardDto.ComplianceCategoryItemDto("Financial Reporting", 85, "#f59e0b"),
                new SubsidiaryAdminDashboardDto.ComplianceCategoryItemDto("Regulatory Filings", 88, "#a855f7")
        );

        List<SubsidiaryAdminDashboardDto.MonthlyInvestmentRevenueDto> trend = List.of(
                new SubsidiaryAdminDashboardDto.MonthlyInvestmentRevenueDto("Apr", 18.2, 12.0),
                new SubsidiaryAdminDashboardDto.MonthlyInvestmentRevenueDto("May", 21.4, 15.2),
                new SubsidiaryAdminDashboardDto.MonthlyInvestmentRevenueDto("Jun", 26.8, 20.4),
                new SubsidiaryAdminDashboardDto.MonthlyInvestmentRevenueDto("Jul", 32.6, 24.8),
                new SubsidiaryAdminDashboardDto.MonthlyInvestmentRevenueDto("Aug", 38.1, 28.6),
                new SubsidiaryAdminDashboardDto.MonthlyInvestmentRevenueDto("Sep", 45.3, 32.4)
        );

        SubsidiaryAdminDashboardDto.EmployeeDistributionDonutDto empDist = SubsidiaryAdminDashboardDto.EmployeeDistributionDonutDto.builder()
                .totalEmployees(524)
                .slices(List.of(
                        new SubsidiaryAdminDashboardDto.EmployeeDepartmentSliceDto("Management", 48, 9.2, "#3b82f6"),
                        new SubsidiaryAdminDashboardDto.EmployeeDepartmentSliceDto("Engineering", 186, 35.5, "#22c55e"),
                        new SubsidiaryAdminDashboardDto.EmployeeDepartmentSliceDto("Operations", 132, 25.2, "#06b6d4"),
                        new SubsidiaryAdminDashboardDto.EmployeeDepartmentSliceDto("Sales & Marketing", 78, 14.9, "#a855f7"),
                        new SubsidiaryAdminDashboardDto.EmployeeDepartmentSliceDto("Finance", 42, 8.0, "#ef4444"),
                        new SubsidiaryAdminDashboardDto.EmployeeDepartmentSliceDto("HR & Admin", 38, 7.3, "#ec4899")
                )).build();

        return SubsidiaryAdminDashboardDto.builder()
                .adminUser(adminUser)
                .pageTitle("Subsidiary Dashboard")
                .pageSubtitle("Overview of subsidiary operations, performance, compliance and key activities.")
                .dateRange(dateRange != null ? dateRange : "01 Sep 2026 - 16 Sep 2026")
                .selectedSubsidiary(subsidiaryFilter != null ? subsidiaryFilter : "All Subsidiaries")
                .kpis(kpis)
                .performanceOverview(filteredPerformance)
                .projectsStatus(projectsStatus)
                .complianceStatus(compliance)
                .investmentVsRevenueTrend(trend)
                .employeeDistribution(empDist)
                .recentActivities(dashboardActivities)
                .upcomingMilestones(upcomingMilestones)
                .build();
    }

    // ==========================================
    // 2. SUBSIDIARY COMPANY PROFILE
    // ==========================================
    public SubsidiaryCompanyProfileDto getCompanyProfile(String companyName) {
        String key = (companyName != null && !companyName.isBlank()) ? companyName.toLowerCase() : "atirath holdings";
        if (profilesMap.containsKey(key)) {
            return profilesMap.get(key);
        }
        return profilesMap.values().iterator().next();
    }

    public SubsidiaryCompanyProfileDto updateCompanyProfile(String companyName, UpdateSubsidiaryCompanyProfileDto req) {
        SubsidiaryCompanyProfileDto profile = getCompanyProfile(companyName);
        if (req.getCompanyName() != null) {
            profile.getHero().setCompanyName(req.getCompanyName());
            profile.getCompanyInformation().setCompanyName(req.getCompanyName());
        }
        if (req.getDescription() != null) profile.getHero().setDescription(req.getDescription());
        if (req.getIndustry() != null) {
            profile.getHero().setIndustry(req.getIndustry());
            profile.getCompanyInformation().setIndustry(req.getIndustry());
        }
        if (req.getCompanyType() != null) {
            profile.getHero().setCompanyType(req.getCompanyType());
            profile.getCompanyInformation().setCompanyType(req.getCompanyType());
        }
        if (req.getPan() != null) profile.getCompanyInformation().setPan(req.getPan());
        if (req.getGstin() != null) profile.getCompanyInformation().setGstin(req.getGstin());
        if (req.getAuthorisedCapital() != null) profile.getCompanyInformation().setAuthorisedCapital(req.getAuthorisedCapital());
        if (req.getPaidUpCapital() != null) profile.getCompanyInformation().setPaidUpCapital(req.getPaidUpCapital());
        if (req.getRegisteredOffice() != null) {
            profile.getHero().setRegisteredOffice(req.getRegisteredOffice());
            profile.getCompanyInformation().setRegisteredOffice(req.getRegisteredOffice());
        }
        if (req.getHeadOffice() != null) {
            profile.getHero().setHeadOffice(req.getHeadOffice());
            profile.getCompanyInformation().setHeadOffice(req.getHeadOffice());
        }
        if (req.getWebsite() != null) {
            profile.getHero().setWebsite(req.getWebsite());
            profile.getCompanyInformation().setWebsite(req.getWebsite());
        }
        if (req.getEmail() != null) profile.getCompanyInformation().setEmail(req.getEmail());
        if (req.getPhone() != null) profile.getCompanyInformation().setPhone(req.getPhone());
        return profile;
    }

    // ==========================================
    // 3. SHARE CLASSES
    // ==========================================
    public SubsidiaryShareClassesPageDto getShareClassesPage(String company, String tab, String query) {
        List<KpiCardDto> kpis = List.of(
                KpiCardDto.builder().title("Total Share Classes").value("4").change("+0%").comparisonText("vs last quarter").trend("neutral").icon("layers").color("blue").build(),
                KpiCardDto.builder().title("Total Issued Shares").value("50,00,000").change("+12%").comparisonText("vs last quarter").trend("positive").icon("pie-chart").color("green").build(),
                KpiCardDto.builder().title("Total Equity Value").value("₹ 50.0 Cr").change("+18%").comparisonText("vs last quarter").trend("positive").icon("coins").color("orange").build(),
                KpiCardDto.builder().title("Investor Categories").value("3").change("+50%").comparisonText("vs last quarter").trend("positive").icon("users").color("purple").build()
        );

        List<SubsidiaryShareClassesPageDto.ShareClassItemDto> filtered = shareClassesList.stream()
                .filter(sc -> {
                    if (query != null && !query.isBlank()) {
                        String q = query.toLowerCase();
                        if (!sc.getClassName().toLowerCase().contains(q) &&
                            !sc.getClassType().toLowerCase().contains(q)) return false;
                    }
                    if (tab != null && !tab.isBlank() && !tab.equalsIgnoreCase("All") && !tab.startsWith("All Share Classes")) {
                        if (!sc.getClassType().equalsIgnoreCase(tab)) return false;
                    }
                    return true;
                }).collect(Collectors.toList());

        ShareClassDetailDto selected = shareClassDetailsMap.getOrDefault("sc-1", shareClassDetailsMap.values().iterator().next());

        SubsidiaryShareClassesPageDto.ShareClassDistributionDonutDto distribution = SubsidiaryShareClassesPageDto.ShareClassDistributionDonutDto.builder()
                .totalShares("50,00,000")
                .slices(List.of(
                        new SubsidiaryShareClassesPageDto.ShareClassDistributionSliceDto("Equity Shares (ES)", 60.0, "30,00,000", "#3b82f6"),
                        new SubsidiaryShareClassesPageDto.ShareClassDistributionSliceDto("Preference Shares (PS)", 20.0, "10,00,000", "#22c55e"),
                        new SubsidiaryShareClassesPageDto.ShareClassDistributionSliceDto("ESOP (ESOP)", 10.0, "5,00,000", "#a855f7"),
                        new SubsidiaryShareClassesPageDto.ShareClassDistributionSliceDto("Series A Preference (A1)", 10.0, "5,00,000", "#ec4899")
                )).build();

        List<SubsidiaryShareClassesPageDto.AuthorizedVsIssuedBarDto> authVsIssued = List.of(
                new SubsidiaryShareClassesPageDto.AuthorizedVsIssuedBarDto("Equity Shares (ES)", 30.0, 100.0),
                new SubsidiaryShareClassesPageDto.AuthorizedVsIssuedBarDto("Preference Shares (PS)", 10.0, 50.0),
                new SubsidiaryShareClassesPageDto.AuthorizedVsIssuedBarDto("ESOP", 5.0, 10.0),
                new SubsidiaryShareClassesPageDto.AuthorizedVsIssuedBarDto("Series A Preference (A1)", 5.0, 20.0)
        );

        SubsidiaryShareClassesPageDto.InvestorCategoriesDonutDto invCat = SubsidiaryShareClassesPageDto.InvestorCategoriesDonutDto.builder()
                .totalShares("50,00,000")
                .slices(List.of(
                        new SubsidiaryShareClassesPageDto.InvestorCategorySliceDto("Promoters", 62.5, "#3b82f6"),
                        new SubsidiaryShareClassesPageDto.InvestorCategorySliceDto("Strategic Investors", 25.0, "#22c55e"),
                        new SubsidiaryShareClassesPageDto.InvestorCategorySliceDto("Employees (ESOP)", 10.0, "#a855f7"),
                        new SubsidiaryShareClassesPageDto.InvestorCategorySliceDto("Others", 2.5, "#94a3b8")
                )).build();

        return SubsidiaryShareClassesPageDto.builder()
                .adminUser(adminUser)
                .pageTitle("Share Classes")
                .pageSubtitle("Manage share classes, rights, voting, and distribution rules for the subsidiary.")
                .selectedCompany(company != null ? company : "Atirath Holdings")
                .companyStatus("Active")
                .kpis(kpis)
                .tabs(List.of("All Share Classes (4)", "Equity (2)", "Preference (1)", "Other (1)"))
                .activeTab(tab != null ? tab : "All Share Classes (4)")
                .shareClasses(filtered)
                .totalClasses(4)
                .selectedShareClassDetail(selected)
                .shareClassDistribution(distribution)
                .authorizedVsIssued(authVsIssued)
                .investorCategories(invCat)
                .build();
    }

    public ShareClassDetailDto getShareClassDetail(String id) {
        return shareClassDetailsMap.getOrDefault(id, shareClassDetailsMap.get("sc-1"));
    }

    public ShareClassDetailDto addShareClass(AddShareClassRequestDto req) {
        int nextNum = shareClassCounter.incrementAndGet();
        String id = "sc-" + nextNum;

        SubsidiaryShareClassesPageDto.ShareClassItemDto item = SubsidiaryShareClassesPageDto.ShareClassItemDto.builder()
                .id(id)
                .number(nextNum)
                .className(req.getClassName())
                .classType(req.getClassType() != null ? req.getClassType() : "Equity")
                .classTypeTheme(req.getClassType() != null && req.getClassType().equalsIgnoreCase("Preference") ? "purple" : "blue")
                .faceValue(req.getFaceValue() > 0 ? req.getFaceValue() : 10.0)
                .issuedShares(req.getIssuedShares() != null ? req.getIssuedShares() : "5,00,000")
                .authorizedShares(req.getAuthorizedShares() != null ? req.getAuthorizedShares() : "20,00,000")
                .votingRights(req.getVotingRights() != null ? req.getVotingRights() : "1 vote / share")
                .dividendRights(req.getDividendRights() != null ? req.getDividendRights() : "Yes")
                .status("Active")
                .statusTheme("green")
                .build();

        shareClassesList.add(item);

        ShareClassDetailDto detail = ShareClassDetailDto.builder()
                .id(id)
                .className(req.getClassName())
                .subheader("New share class allotment with specified voting rights")
                .status("Active")
                .statusTheme("green")
                .tabs(List.of("Details", "Rights", "Terms", "Holders", "History"))
                .activeTab("Details")
                .classType(req.getClassType())
                .faceValue("₹ " + (req.getFaceValue() > 0 ? (int)req.getFaceValue() : 10))
                .issuedShares(req.getIssuedShares())
                .authorizedShares(req.getAuthorizedShares())
                .approxValue("₹ 5.0 Cr")
                .votingRights(req.getVotingRights())
                .dividendRights(req.getDividendRights())
                .transferability(req.getTransferability() != null ? req.getTransferability() : "Restricted")
                .investorCategory(req.getInvestorCategory() != null ? req.getInvestorCategory() : "All Eligible Investors")
                .createdOn("16 Sep 2026")
                .lastUpdated("16 Sep 2026")
                .description(req.getDescription() != null ? req.getDescription() : "Newly registered subsidiary share class.")
                .build();

        shareClassDetailsMap.put(id, detail);
        return detail;
    }

    // ==========================================
    // 4. SHARE ISSUANCE
    // ==========================================
    public SubsidiaryShareIssuancePageDto getShareIssuancePage(String company, String tab, String query) {
        List<KpiCardDto> kpis = List.of(
                KpiCardDto.builder().title("Total Shares Issued").value("50,00,000").change("+12%").comparisonText("vs last quarter").trend("positive").icon("file-text").color("blue").build(),
                KpiCardDto.builder().title("Total Capital Raised").value("₹ 50.0 Cr").change("+18%").comparisonText("vs last quarter").trend("positive").icon("rupee").color("green").build(),
                KpiCardDto.builder().title("Total Investors").value("12").change("+33%").comparisonText("vs last quarter").trend("positive").icon("users").color("pink").build(),
                KpiCardDto.builder().title("Pending Issuances").value("2").change("-50%").comparisonText("vs last quarter").trend("positive").icon("check-circle").color("purple").build()
        );

        List<SubsidiaryShareIssuancePageDto.ShareIssuanceItemDto> filtered = issuanceRequestsList.stream()
                .filter(i -> {
                    if (query != null && !query.isBlank()) {
                        String q = query.toLowerCase();
                        if (!i.getRequestId().toLowerCase().contains(q) &&
                            !i.getInvestor().toLowerCase().contains(q) &&
                            !i.getShareClass().toLowerCase().contains(q)) return false;
                    }
                    if (tab != null && !tab.isBlank() && !tab.startsWith("All")) {
                        if (tab.equalsIgnoreCase("Approved") || tab.startsWith("Approved Issuances")) {
                            if (!i.getStatus().equalsIgnoreCase("Approved") && !i.getStatus().equalsIgnoreCase("Issued")) return false;
                        } else if (tab.equalsIgnoreCase("Requests") || tab.startsWith("Issuance Requests")) {
                            if (i.getStatus().equalsIgnoreCase("Approved") || i.getStatus().equalsIgnoreCase("Issued")) return false;
                        }
                    }
                    return true;
                }).collect(Collectors.toList());

        List<SubsidiaryShareIssuancePageDto.MonthlyIssuancePointDto> trend = List.of(
                new SubsidiaryShareIssuancePageDto.MonthlyIssuancePointDto("Apr", 5.0, 4.0),
                new SubsidiaryShareIssuancePageDto.MonthlyIssuancePointDto("May", 8.0, 6.4),
                new SubsidiaryShareIssuancePageDto.MonthlyIssuancePointDto("Jun", 12.0, 9.6),
                new SubsidiaryShareIssuancePageDto.MonthlyIssuancePointDto("Jul", 15.0, 12.0),
                new SubsidiaryShareIssuancePageDto.MonthlyIssuancePointDto("Aug", 18.0, 14.4),
                new SubsidiaryShareIssuancePageDto.MonthlyIssuancePointDto("Sep", 22.0, 17.6)
        );

        SubsidiaryShareIssuancePageDto.ShareClassWiseIssuanceDonutDto classDonut = SubsidiaryShareIssuancePageDto.ShareClassWiseIssuanceDonutDto.builder()
                .totalShares("50,00,000")
                .slices(List.of(
                        new SubsidiaryShareIssuancePageDto.ShareClassIssuanceSliceDto("Equity Shares (ES)", 60.0, "30,00,000", "#3b82f6"),
                        new SubsidiaryShareIssuancePageDto.ShareClassIssuanceSliceDto("Preference Shares (PS)", 20.0, "10,00,000", "#22c55e"),
                        new SubsidiaryShareIssuancePageDto.ShareClassIssuanceSliceDto("ESOP (ESOP)", 10.0, "5,00,000", "#a855f7"),
                        new SubsidiaryShareIssuancePageDto.ShareClassIssuanceSliceDto("Series A Preference (A1)", 10.0, "5,00,000", "#ec4899")
                )).build();

        return SubsidiaryShareIssuancePageDto.builder()
                .adminUser(adminUser)
                .pageTitle("Share Issuance")
                .pageSubtitle("Issue new shares, manage allotments, and track issuance history.")
                .selectedCompany(company != null ? company : "Atirath Holdings")
                .companyStatus("Active")
                .kpis(kpis)
                .tabs(List.of("Issuance Requests (6)", "Approved Issuances (10)", "All Issuances (16)"))
                .activeTab(tab != null ? tab : "Issuance Requests (6)")
                .requests(filtered)
                .totalRequests(16)
                .currentPage(1)
                .totalPages(2)
                .issuanceTrend(trend)
                .shareClassWiseIssuance(classDonut)
                .recentApprovals(recentApprovalsList)
                .activityLog(issuanceActivityLogList)
                .build();
    }

    public SubsidiaryShareIssuancePageDto.ShareIssuanceItemDto submitShareIssuance(SubmitShareIssuanceRequestDto req) {
        int nextNum = issuanceCounter.incrementAndGet();
        String reqId = "SI-2026-00" + nextNum;

        double totalAmt = req.getTotalAmount() > 0 ? req.getTotalAmount() :
                (req.getFaceValue() * 500000);

        SubsidiaryShareIssuancePageDto.ShareIssuanceItemDto item = SubsidiaryShareIssuancePageDto.ShareIssuanceItemDto.builder()
                .id("si-" + nextNum)
                .number(nextNum)
                .requestId(reqId)
                .shareClass(req.getShareClass() != null ? req.getShareClass() : "Equity Shares (ES)")
                .noOfShares(req.getNumberOfShares() != null ? req.getNumberOfShares() : "5,00,000")
                .faceValue(String.valueOf((int) (req.getFaceValue() > 0 ? req.getFaceValue() : 10)))
                .issuePrice(String.valueOf((int) (req.getIssuePrice() > 0 ? req.getIssuePrice() : 100)))
                .totalAmount(String.format("%,.0f", totalAmt))
                .investor(req.getInvestor() != null ? req.getInvestor() : "New Investor Fund")
                .status("Pending Approval")
                .statusTheme("orange")
                .requestedOn("16 Sep 2026")
                .build();

        issuanceRequestsList.add(0, item);

        issuanceActivityLogList.add(0, new SubsidiaryShareIssuancePageDto.IssuanceActivityLogDto(
                "16 Sep 2026, 12:40 PM",
                "New issuance request created (" + reqId + ")",
                "Admin User"
        ));

        return item;
    }

    public SubsidiaryShareIssuancePageDto.ShareIssuanceItemDto approveIssuance(String requestId) {
        for (SubsidiaryShareIssuancePageDto.ShareIssuanceItemDto item : issuanceRequestsList) {
            if (item.getRequestId().equalsIgnoreCase(requestId)) {
                item.setStatus("Approved");
                item.setStatusTheme("green");
                recentApprovalsList.add(0, new SubsidiaryShareIssuancePageDto.IssuanceApprovalDto(
                        recentApprovalsList.size() + 1,
                        item.getRequestId(),
                        item.getShareClass(),
                        item.getNoOfShares(),
                        item.getInvestor(),
                        "16 Sep 2026",
                        "Admin User"
                ));
                return item;
            }
        }
        return issuanceRequestsList.get(0);
    }

    public SubsidiaryShareIssuancePageDto.ShareIssuanceItemDto rejectIssuance(String requestId) {
        for (SubsidiaryShareIssuancePageDto.ShareIssuanceItemDto item : issuanceRequestsList) {
            if (item.getRequestId().equalsIgnoreCase(requestId)) {
                item.setStatus("Rejected");
                item.setStatusTheme("red");
                return item;
            }
        }
        return issuanceRequestsList.get(0);
    }

    // ==========================================
    // SEEDING HELPERS
    // ==========================================
    private void seedDashboard() {
        performanceItems.clear();
        performanceItems.add(new SubsidiaryAdminDashboardDto.SubsidiaryPerformanceItemDto(1, "Atirath Holdings", "Renewable Energy", 6, 120, 52.9, 36.8, "Active", "green"));
        performanceItems.add(new SubsidiaryAdminDashboardDto.SubsidiaryPerformanceItemDto(2, "Atirath Technologies", "Technology", 5, 95, 31.3, 18.6, "Active", "green"));
        performanceItems.add(new SubsidiaryAdminDashboardDto.SubsidiaryPerformanceItemDto(3, "Farm Vantara", "Agriculture", 4, 78, 20.9, 12.4, "Active", "green"));
        performanceItems.add(new SubsidiaryAdminDashboardDto.SubsidiaryPerformanceItemDto(4, "Goeazymart", "Retail / FMCG", 3, 62, 23.3, 14.2, "Active", "green"));
        performanceItems.add(new SubsidiaryAdminDashboardDto.SubsidiaryPerformanceItemDto(5, "Tri Energy Battery", "Energy Storage", 2, 48, 11.6, 6.8, "Active", "green"));
        performanceItems.add(new SubsidiaryAdminDashboardDto.SubsidiaryPerformanceItemDto(6, "CS INTERIO", "Real Estate", 2, 28, 7.3, 4.6, "Active", "green"));
        performanceItems.add(new SubsidiaryAdminDashboardDto.SubsidiaryPerformanceItemDto(7, "Royal One Heating", "Manufacturing", 1, 7, 6.4, 3.0, "Active", "green"));
        performanceItems.add(new SubsidiaryAdminDashboardDto.SubsidiaryPerformanceItemDto(8, "NEU1 Smart Stay", "Hospitality", 1, 12, 5.1, 2.8, "Planning", "orange"));

        dashboardActivities.clear();
        dashboardActivities.add(new SubsidiaryAdminDashboardDto.SubsidiaryActivityItemDto("16 Sep 2026, 11:30 AM", "Farm Vantara", "New project created - AP Expansion", "Ramesh Kumar"));
        dashboardActivities.add(new SubsidiaryAdminDashboardDto.SubsidiaryActivityItemDto("16 Sep 2026, 10:45 AM", "Atirath Holdings", "Investment updated (₹ 5 Cr)", "Priya Verma"));
        dashboardActivities.add(new SubsidiaryAdminDashboardDto.SubsidiaryActivityItemDto("15 Sep 2026, 04:20 PM", "Goeazymart", "Compliance document submitted", "Amit Singh"));
        dashboardActivities.add(new SubsidiaryAdminDashboardDto.SubsidiaryActivityItemDto("15 Sep 2026, 03:15 PM", "Atirath Technologies", "New employee onboarded", "Meera Reddy"));
        dashboardActivities.add(new SubsidiaryAdminDashboardDto.SubsidiaryActivityItemDto("14 Sep 2026, 12:10 PM", "Tri Energy Battery", "Project milestone completed", "Karthik R"));

        upcomingMilestones.clear();
        upcomingMilestones.add(new SubsidiaryAdminDashboardDto.SubsidiaryMilestoneItemDto("20 Sep 2026", "Farm Vantara", "Land Acquisition Approval", "Due Soon", "orange"));
        upcomingMilestones.add(new SubsidiaryAdminDashboardDto.SubsidiaryMilestoneItemDto("25 Sep 2026", "Atirath Holdings", "CBG Plant - Phase 1 Completion", "On Track", "green"));
        upcomingMilestones.add(new SubsidiaryAdminDashboardDto.SubsidiaryMilestoneItemDto("28 Sep 2026", "Goeazymart", "Retail Platform Launch", "On Track", "green"));
        upcomingMilestones.add(new SubsidiaryAdminDashboardDto.SubsidiaryMilestoneItemDto("30 Sep 2026", "Atirath Technologies", "Product Release v2.0", "At Risk", "red"));
        upcomingMilestones.add(new SubsidiaryAdminDashboardDto.SubsidiaryMilestoneItemDto("05 Oct 2026", "CS INTERIO", "Site Construction Start", "On Track", "green"));
    }

    private void seedCompanyProfile() {
        profilesMap.clear();

        SubsidiaryCompanyProfileDto.CompanyHeroDto hero = SubsidiaryCompanyProfileDto.CompanyHeroDto.builder()
                .companyName("Atirath Holdings India Limited")
                .status("Active")
                .cin("U40109TG2020PLC138765")
                .incorporationDate("15 Jan 2020")
                .description("EPC company for CBG plants from Napier Grass, residues and organic waste.")
                .companyType("Public Limited")
                .industry("Renewable Energy")
                .headOffice("Hyderabad, Telangana")
                .registeredOffice("Hyderabad, Telangana")
                .website("www.atirathholdings.in")
                .imageUrl("https://images.unsplash.com/photo-1486406146926-c627a92ad1ab?w=800&auto=format&fit=crop&q=60")
                .build();

        List<KpiCardDto> kpis = List.of(
                KpiCardDto.builder().title("Total Projects").value("24").change("+26% vs last quarter").comparisonText("").trend("positive").icon("bar-chart").color("blue").build(),
                KpiCardDto.builder().title("Total Plants").value("5").change("+25% vs last quarter").comparisonText("").trend("positive").icon("factory").color("green").build(),
                KpiCardDto.builder().title("Land Area (Acres)").value("2,450").change("+12% vs last quarter").comparisonText("").trend("positive").icon("map").color("yellow").build(),
                KpiCardDto.builder().title("Total Employees").value("524").change("+18% vs last quarter").comparisonText("").trend("positive").icon("users").color("purple").build(),
                KpiCardDto.builder().title("Total Revenue (YTD)").value("₹ 124.8 Cr").change("+22% vs last quarter").comparisonText("").trend("positive").icon("rupee").color("red").build()
        );

        SubsidiaryCompanyProfileDto.CompanyFullInformationDto info = SubsidiaryCompanyProfileDto.CompanyFullInformationDto.builder()
                .companyName("Atirath Holdings India Limited")
                .cin("U40109TG2020PLC138765")
                .pan("AAECA1234D")
                .gstin("36AAECA1234D1Z5")
                .incorporationDate("15 Jan 2020")
                .companyType("Public Limited")
                .industry("Renewable Energy")
                .authorisedCapital("₹ 500.00 Cr")
                .paidUpCapital("₹ 320.50 Cr")
                .registeredOffice("Plot No. 12, Financial District, Hyderabad, Telangana - 500032")
                .headOffice("Atirath Towers, Gachibowli, Hyderabad, Telangana - 500032")
                .website("www.atirathholdings.in")
                .email("info@atirathholdings.in")
                .phone("+91 40 1234 5678")
                .build();

        SubsidiaryCompanyProfileDto.ShareholdingPatternDto shareholding = SubsidiaryCompanyProfileDto.ShareholdingPatternDto.builder()
                .totalShares("100% Total Shares")
                .slices(List.of(
                        new SubsidiaryCompanyProfileDto.ShareholdingSliceDto("Atirath Group (Promoter)", 62.5, "#3b82f6"),
                        new SubsidiaryCompanyProfileDto.ShareholdingSliceDto("Strategic Investors", 18.4, "#06b6d4"),
                        new SubsidiaryCompanyProfileDto.ShareholdingSliceDto("Institutional Investors", 12.1, "#22c55e"),
                        new SubsidiaryCompanyProfileDto.ShareholdingSliceDto("Retail Investors", 5.0, "#f59e0b"),
                        new SubsidiaryCompanyProfileDto.ShareholdingSliceDto("Others", 2.0, "#a855f7")
                )).build();

        List<SubsidiaryCompanyProfileDto.ManagementPersonDto> management = List.of(
                new SubsidiaryCompanyProfileDto.ManagementPersonDto("Ramesh Kumar", "RK", "Managing Director", "ramesh.kumar@atirathholdings.in"),
                new SubsidiaryCompanyProfileDto.ManagementPersonDto("Priya Verma", "PV", "Chief Financial Officer", "priya.verma@atirathholdings.in"),
                new SubsidiaryCompanyProfileDto.ManagementPersonDto("Amit Singh", "AS", "Chief Operating Officer", "amit.singh@atirathholdings.in"),
                new SubsidiaryCompanyProfileDto.ManagementPersonDto("Meera Reddy", "MR", "Company Secretary", "meera.reddy@atirathholdings.in")
        );

        List<SubsidiaryCompanyProfileDto.YearlyFinancialDto> financials = List.of(
                new SubsidiaryCompanyProfileDto.YearlyFinancialDto("2022", 68.4, 32.1, 18.5),
                new SubsidiaryCompanyProfileDto.YearlyFinancialDto("2023", 92.1, 45.3, 28.2),
                new SubsidiaryCompanyProfileDto.YearlyFinancialDto("2024", 112.6, 56.4, 38.6),
                new SubsidiaryCompanyProfileDto.YearlyFinancialDto("2025 (YTD)", 124.8, 64.2, 45.3)
        );

        List<SubsidiaryCompanyProfileDto.ProfileActivityItemDto> profileActivities = List.of(
                new SubsidiaryCompanyProfileDto.ProfileActivityItemDto("16 Sep 2026, 11:30 AM", "Company details updated", "Ramesh Kumar"),
                new SubsidiaryCompanyProfileDto.ProfileActivityItemDto("15 Sep 2026, 04:20 PM", "Financial statement uploaded", "Priya Verma"),
                new SubsidiaryCompanyProfileDto.ProfileActivityItemDto("14 Sep 2026, 02:15 PM", "New project added", "Amit Singh"),
                new SubsidiaryCompanyProfileDto.ProfileActivityItemDto("13 Sep 2026, 10:45 AM", "Shareholding details updated", "Meera Reddy"),
                new SubsidiaryCompanyProfileDto.ProfileActivityItemDto("12 Sep 2026, 03:10 PM", "Compliance document added", "Karthik R")
        );

        SubsidiaryCompanyProfileDto profile = SubsidiaryCompanyProfileDto.builder()
                .adminUser(adminUser)
                .pageTitle("Subsidiary Company Profile")
                .pageSubtitle("View and manage complete details, operations, financials and compliance for the subsidiary.")
                .selectedCompany("Atirath Holdings")
                .status("Active")
                .hero(hero)
                .tabs(List.of("Overview", "Key Details", "Financials", "Shareholding", "Projects", "Plants", "Land Assets", "Employees", "Compliance", "Documents", "Activity Log"))
                .activeTab("Overview")
                .kpis(kpis)
                .companyInformation(info)
                .shareholdingPattern(shareholding)
                .keyManagement(management)
                .financialSummary(financials)
                .recentActivities(profileActivities)
                .build();

        profilesMap.put("atirath holdings", profile);
    }

    private void seedShareClasses() {
        shareClassesList.clear();

        shareClassesList.add(SubsidiaryShareClassesPageDto.ShareClassItemDto.builder()
                .id("sc-1").number(1).className("Equity Shares (ES)").classType("Equity").classTypeTheme("blue")
                .faceValue(10.0).issuedShares("30,00,000").authorizedShares("1,00,00,000").votingRights("1 vote / share")
                .dividendRights("Yes").status("Active").statusTheme("green").build());

        shareClassesList.add(SubsidiaryShareClassesPageDto.ShareClassItemDto.builder()
                .id("sc-2").number(2).className("Preference Shares (PS)").classType("Preference").classTypeTheme("purple")
                .faceValue(10.0).issuedShares("10,00,000").authorizedShares("50,00,000").votingRights("No")
                .dividendRights("Fixed (8%)").status("Active").statusTheme("green").build());

        shareClassesList.add(SubsidiaryShareClassesPageDto.ShareClassItemDto.builder()
                .id("sc-3").number(3).className("Employee Stock Option (ESOP)").classType("Equity").classTypeTheme("blue")
                .faceValue(10.0).issuedShares("5,00,000").authorizedShares("10,00,000").votingRights("1 vote / share")
                .dividendRights("No").status("Active").statusTheme("green").build());

        shareClassesList.add(SubsidiaryShareClassesPageDto.ShareClassItemDto.builder()
                .id("sc-4").number(4).className("Series A Preference (A1)").classType("Preference").classTypeTheme("purple")
                .faceValue(10.0).issuedShares("5,00,000").authorizedShares("20,00,000").votingRights("No")
                .dividendRights("Cumulative (10%)").status("Active").statusTheme("green").build());

        ShareClassDetailDto sc1Detail = ShareClassDetailDto.builder()
                .id("sc-1")
                .className("Equity Shares (ES)")
                .subheader("Common equity shares with full voting rights")
                .status("Active")
                .statusTheme("green")
                .tabs(List.of("Details", "Rights", "Terms", "Holders (3)", "History"))
                .activeTab("Details")
                .classType("Equity")
                .faceValue("₹ 10")
                .issuedShares("30,00,000")
                .authorizedShares("1,00,00,000")
                .approxValue("₹ 30.0 Cr")
                .votingRights("1 vote / share")
                .dividendRights("Yes (As declared)")
                .transferability("Restricted")
                .investorCategory("All Eligible Investors")
                .createdOn("15 Jan 2020")
                .lastUpdated("12 Sep 2026")
                .description("Ordinary equity shares with full voting rights. Eligible for dividends as per board resolution. Transfer subject to approval.")
                .build();

        shareClassDetailsMap.put("sc-1", sc1Detail);
    }

    private void seedShareIssuance() {
        issuanceRequestsList.clear();

        issuanceRequestsList.add(SubsidiaryShareIssuancePageDto.ShareIssuanceItemDto.builder()
                .id("si-1").number(1).requestId("SI-2026-006").shareClass("Equity Shares (ES)")
                .noOfShares("5,00,000").faceValue("10").issuePrice("100").totalAmount("5,00,00,000")
                .investor("Green Future Fund").status("Pending Approval").statusTheme("orange")
                .requestedOn("16 Sep 2026").build());

        issuanceRequestsList.add(SubsidiaryShareIssuancePageDto.ShareIssuanceItemDto.builder()
                .id("si-2").number(2).requestId("SI-2026-005").shareClass("Preference Shares (PS)")
                .noOfShares("10,00,000").faceValue("10").issuePrice("75").totalAmount("7,50,00,000")
                .investor("Sunrise Ventures").status("Under Review").statusTheme("blue")
                .requestedOn("14 Sep 2026").build());

        issuanceRequestsList.add(SubsidiaryShareIssuancePageDto.ShareIssuanceItemDto.builder()
                .id("si-3").number(3).requestId("SI-2026-004").shareClass("ESOP (ESOP)")
                .noOfShares("2,00,000").faceValue("10").issuePrice("50").totalAmount("1,00,00,000")
                .investor("Atirath Holdings").status("Pending Documents").statusTheme("pink")
                .requestedOn("12 Sep 2026").build());

        issuanceRequestsList.add(SubsidiaryShareIssuancePageDto.ShareIssuanceItemDto.builder()
                .id("si-4").number(4).requestId("SI-2026-003").shareClass("Equity Shares (ES)")
                .noOfShares("8,00,000").faceValue("10").issuePrice("90").totalAmount("7,20,00,000")
                .investor("Global Energy Partners").status("Approved").statusTheme("green")
                .requestedOn("10 Sep 2026").build());

        issuanceRequestsList.add(SubsidiaryShareIssuancePageDto.ShareIssuanceItemDto.builder()
                .id("si-5").number(5).requestId("SI-2026-002").shareClass("Series A Preference (A1)")
                .noOfShares("5,00,000").faceValue("10").issuePrice("120").totalAmount("6,00,00,000")
                .investor("Nexus Capital").status("Issued").statusTheme("teal")
                .requestedOn("05 Sep 2026").build());

        issuanceRequestsList.add(SubsidiaryShareIssuancePageDto.ShareIssuanceItemDto.builder()
                .id("si-6").number(6).requestId("SI-2026-001").shareClass("Equity Shares (ES)")
                .noOfShares("20,00,000").faceValue("10").issuePrice("80").totalAmount("16,00,00,000")
                .investor("Strategic Investors").status("Issued").statusTheme("teal")
                .requestedOn("01 Sep 2026").build());

        recentApprovalsList.clear();
        recentApprovalsList.add(new SubsidiaryShareIssuancePageDto.IssuanceApprovalDto(1, "SI-2026-003", "Equity Shares (ES)", "8,00,000", "Global Energy Partners", "10 Sep 2026", "Ramesh Kumar"));
        recentApprovalsList.add(new SubsidiaryShareIssuancePageDto.IssuanceApprovalDto(2, "SI-2026-002", "Series A Preference (A1)", "5,00,000", "Nexus Capital", "05 Sep 2026", "Priya Verma"));
        recentApprovalsList.add(new SubsidiaryShareIssuancePageDto.IssuanceApprovalDto(3, "SI-2026-001", "Equity Shares (ES)", "20,00,000", "Strategic Investors", "01 Sep 2026", "Amit Singh"));

        issuanceActivityLogList.clear();
        issuanceActivityLogList.add(new SubsidiaryShareIssuancePageDto.IssuanceActivityLogDto("16 Sep 2026, 11:30 AM", "New issuance request created (SI-2026-006)", "Ramesh Kumar"));
        issuanceActivityLogList.add(new SubsidiaryShareIssuancePageDto.IssuanceActivityLogDto("14 Sep 2026, 04:20 PM", "Documents uploaded for SI-2026-005", "Priya Verma"));
        issuanceActivityLogList.add(new SubsidiaryShareIssuancePageDto.IssuanceActivityLogDto("12 Sep 2026, 02:15 PM", "Issuance request updated", "Amit Singh"));
        issuanceActivityLogList.add(new SubsidiaryShareIssuancePageDto.IssuanceActivityLogDto("10 Sep 2026, 10:45 AM", "Request approved (SI-2026-003)", "Meera Reddy"));
        issuanceActivityLogList.add(new SubsidiaryShareIssuancePageDto.IssuanceActivityLogDto("05 Sep 2026, 03:10 PM", "Shares allotted (SI-2026-002)", "Karthik R"));
    }

    // ==========================================
    // 5. SUBSIDIARY SHAREHOLDERS
    // ==========================================
    public SubsidiaryShareholdersPageDto getSubsidiaryShareholdersPage(String company, String tab, String query) {
        List<KpiCardDto> kpis = List.of(
                KpiCardDto.builder().title("Total Shareholders").value("12").change("+20% vs last quarter").comparisonText("").trend("positive").icon("users").color("blue").build(),
                KpiCardDto.builder().title("Institutional Investors").value("5").change("+25% vs last quarter").comparisonText("").trend("positive").icon("building").color("green").build(),
                KpiCardDto.builder().title("Individual Investors").value("6").change("+20% vs last quarter").comparisonText("").trend("positive").icon("briefcase").color("purple").build(),
                KpiCardDto.builder().title("Foreign Investors").value("1").change("+0% vs last quarter").comparisonText("").trend("neutral").icon("pie-chart").color("orange").build()
        );

        List<SubsidiaryShareholdersPageDto.ShareholderItemDto> filtered = shareholdersList.stream()
                .filter(s -> {
                    if (query != null && !query.isBlank()) {
                        String q = query.toLowerCase();
                        if (!s.getShareholderName().toLowerCase().contains(q) &&
                            !s.getType().toLowerCase().contains(q) &&
                            !s.getCategory().toLowerCase().contains(q)) return false;
                    }
                    if (tab != null && !tab.isBlank() && !tab.startsWith("All")) {
                        if (tab.equalsIgnoreCase("Institutional") && !s.getCategory().equalsIgnoreCase("Institutional")) return false;
                        if (tab.equalsIgnoreCase("Individual") && !s.getCategory().equalsIgnoreCase("Individual")) return false;
                        if (tab.equalsIgnoreCase("Foreign") && !s.getCategory().equalsIgnoreCase("Foreign")) return false;
                    }
                    return true;
                }).collect(Collectors.toList());

        ShareholderDetailDto selected = shareholderDetailsMap.getOrDefault("sh-1", shareholderDetailsMap.values().iterator().next());

        SubsidiaryShareholdersPageDto.ShareholdingPatternDonutDto pattern = SubsidiaryShareholdersPageDto.ShareholdingPatternDonutDto.builder()
                .totalShares("50,00,000")
                .slices(List.of(
                        new SubsidiaryShareholdersPageDto.ShareholderSliceDto("Green Future Fund", 24.0, "#3b82f6"),
                        new SubsidiaryShareholdersPageDto.ShareholderSliceDto("Sunrise Ventures", 16.0, "#22c55e"),
                        new SubsidiaryShareholdersPageDto.ShareholderSliceDto("Global Energy Partners", 14.4, "#a855f7"),
                        new SubsidiaryShareholdersPageDto.ShareholderSliceDto("Nexus Capital", 10.0, "#f59e0b"),
                        new SubsidiaryShareholdersPageDto.ShareholderSliceDto("Others", 35.6, "#94a3b8")
                )).build();

        List<SubsidiaryShareholdersPageDto.CategoryCountBarDto> catBars = List.of(
                new SubsidiaryShareholdersPageDto.CategoryCountBarDto("Institutional", 5, "#3b82f6"),
                new SubsidiaryShareholdersPageDto.CategoryCountBarDto("Individual", 6, "#a855f7"),
                new SubsidiaryShareholdersPageDto.CategoryCountBarDto("Foreign", 1, "#f59e0b")
        );

        List<SubsidiaryShareholdersPageDto.TopShareholderBarDto> topBars = List.of(
                new SubsidiaryShareholdersPageDto.TopShareholderBarDto("Green Future Fund", 24.0, "#3b82f6"),
                new SubsidiaryShareholdersPageDto.TopShareholderBarDto("Sunrise Ventures", 16.0, "#22c55e"),
                new SubsidiaryShareholdersPageDto.TopShareholderBarDto("Global Energy Partners", 14.4, "#a855f7"),
                new SubsidiaryShareholdersPageDto.TopShareholderBarDto("Nexus Capital", 10.0, "#f59e0b"),
                new SubsidiaryShareholdersPageDto.TopShareholderBarDto("Strategic Investors", 8.0, "#06b6d4")
        );

        return SubsidiaryShareholdersPageDto.builder()
                .adminUser(adminUser)
                .pageTitle("Shareholders")
                .pageSubtitle("View and manage all shareholders, their holdings, categories and related information.")
                .selectedCompany(company != null ? company : "Atirath Holdings")
                .companyStatus("Active")
                .kpis(kpis)
                .tabs(List.of("All Shareholders (12)", "Institutional (5)", "Individual (6)", "Foreign (1)"))
                .activeTab(tab != null ? tab : "All Shareholders (12)")
                .shareholders(filtered)
                .totalShareholders(12)
                .currentPage(1)
                .totalPages(2)
                .selectedShareholderDetail(selected)
                .shareholdingPattern(pattern)
                .shareholdersByCategory(catBars)
                .topShareholders(topBars)
                .build();
    }

    public ShareholderDetailDto getShareholderDetail(String id) {
        return shareholderDetailsMap.getOrDefault(id, shareholderDetailsMap.get("sh-1"));
    }

    public ShareholderDetailDto addShareholder(AddSubsidiaryShareholderRequestDto req) {
        int nextNum = shareholderCounter.incrementAndGet();
        String id = "sh-" + nextNum;
        String initials = req.getShareholderName().length() >= 2 ?
                req.getShareholderName().substring(0, 2).toUpperCase() : "SH";

        SubsidiaryShareholdersPageDto.ShareholderItemDto item = SubsidiaryShareholdersPageDto.ShareholderItemDto.builder()
                .id(id)
                .number(nextNum)
                .shareholderName(req.getShareholderName())
                .initials(initials)
                .category(req.getCategory() != null ? req.getCategory() : "Individual")
                .categoryTheme(req.getCategory() != null && req.getCategory().equalsIgnoreCase("Institutional") ? "blue" : "purple")
                .type(req.getType() != null ? req.getType() : "Individual")
                .noOfShares(req.getNoOfShares() != null ? req.getNoOfShares() : "1,00,000")
                .ownershipPercentage(req.getOwnershipPercentage() > 0 ? req.getOwnershipPercentage() : 2.0)
                .investmentCr(req.getInvestmentCr() > 0 ? req.getInvestmentCr() : 1.0)
                .status("Active")
                .statusTheme("green")
                .build();

        shareholdersList.add(item);

        ShareholderDetailDto detail = ShareholderDetailDto.builder()
                .id(id)
                .shareholderName(req.getShareholderName())
                .initials(initials)
                .status("Active")
                .statusTheme("green")
                .categoryBadge(req.getCategory() != null ? req.getCategory() + " Investor" : "Individual Investor")
                .typeBadge(req.getType() != null ? req.getType() : "Individual")
                .tabs(List.of("Overview", "Holdings", "Documents", "History"))
                .activeTab("Overview")
                .category(req.getCategory() != null ? req.getCategory() : "Individual")
                .type(req.getType() != null ? req.getType() : "Individual")
                .noOfShares(req.getNoOfShares())
                .ownershipPercentage(req.getOwnershipPercentage())
                .investmentAmount("₹ " + req.getInvestmentCr() + " Cr")
                .pan(req.getPan() != null ? req.getPan() : "ABCDE1234F")
                .country(req.getCountry() != null ? req.getCountry() : "India")
                .registeredAddress(req.getRegisteredAddress() != null ? req.getRegisteredAddress() : "Hyderabad, Telangana")
                .contactPerson(req.getContactPerson() != null ? req.getContactPerson() : req.getShareholderName())
                .email(req.getEmail() != null ? req.getEmail() : "shareholder@example.com")
                .phone(req.getPhone() != null ? req.getPhone() : "+91 99999 88888")
                .kycStatus("Verified")
                .kycStatusTheme("green")
                .lastUpdated("16 Sep 2026")
                .build();

        shareholderDetailsMap.put(id, detail);
        return detail;
    }

    // ==========================================
    // 6. CAP TABLE
    // ==========================================
    public SubsidiaryCapTablePageDto getCapTablePage(String company, String tab, String query) {
        List<KpiCardDto> kpis = List.of(
                KpiCardDto.builder().title("Total Shares").value("50,00,000").change("+12% vs last quarter").comparisonText("").trend("positive").icon("pie-chart").color("blue").build(),
                KpiCardDto.builder().title("Total Equity Value").value("₹ 50.0 Cr").change("+18% vs last quarter").comparisonText("").trend("positive").icon("rupee").color("green").build(),
                KpiCardDto.builder().title("Total Shareholders").value("12").change("+20% vs last quarter").comparisonText("").trend("positive").icon("users").color("pink").build(),
                KpiCardDto.builder().title("Share Classes").value("4").change("+0% vs last quarter").comparisonText("").trend("neutral").icon("layers").color("blue").build()
        );

        List<SubsidiaryCapTablePageDto.CapTableRowDto> filtered = capTableRowsList.stream()
                .filter(r -> {
                    if (query != null && !query.isBlank()) {
                        String q = query.toLowerCase();
                        if (!r.getShareholderName().toLowerCase().contains(q) &&
                            !r.getShareClass().toLowerCase().contains(q)) return false;
                    }
                    return true;
                }).collect(Collectors.toList());

        SubsidiaryCapTablePageDto.CapTableSummaryDonutDto summary = SubsidiaryCapTablePageDto.CapTableSummaryDonutDto.builder()
                .totalShares("50,00,000")
                .slices(List.of(
                        new SubsidiaryCapTablePageDto.CapTableSummarySliceDto("Green Future Fund", 24.0, "#3b82f6"),
                        new SubsidiaryCapTablePageDto.CapTableSummarySliceDto("Sunrise Ventures", 20.0, "#22c55e"),
                        new SubsidiaryCapTablePageDto.CapTableSummarySliceDto("Global Energy Partners", 14.4, "#a855f7"),
                        new SubsidiaryCapTablePageDto.CapTableSummarySliceDto("Nexus Capital", 10.0, "#f59e0b"),
                        new SubsidiaryCapTablePageDto.CapTableSummarySliceDto("Strategic Investors", 8.0, "#06b6d4"),
                        new SubsidiaryCapTablePageDto.CapTableSummarySliceDto("Others", 23.6, "#94a3b8")
                )).build();

        SubsidiaryCapTablePageDto.CapTableKeyInfoDto keyInfo = SubsidiaryCapTablePageDto.CapTableKeyInfoDto.builder()
                .authorizedCapital("₹ 500.00 Cr")
                .issuedCapital("₹ 50.00 Cr")
                .paidUpCapital("₹ 32.50 Cr")
                .totalShares("50,00,000")
                .faceValue("₹ 10")
                .shareClasses(4)
                .shareholders(12)
                .lastUpdated("10 Sep 2026")
                .build();

        SubsidiaryCapTablePageDto.CapTableOwnershipShareClassDonutDto byClass = SubsidiaryCapTablePageDto.CapTableOwnershipShareClassDonutDto.builder()
                .totalShares("50,00,000")
                .slices(List.of(
                        new SubsidiaryCapTablePageDto.OwnershipShareClassSliceDto("Equity Shares (ES)", 60.0, "#3b82f6"),
                        new SubsidiaryCapTablePageDto.OwnershipShareClassSliceDto("Preference Shares (PS)", 20.0, "#22c55e"),
                        new SubsidiaryCapTablePageDto.OwnershipShareClassSliceDto("ESOP (ESOP)", 10.0, "#a855f7"),
                        new SubsidiaryCapTablePageDto.OwnershipShareClassSliceDto("Series A Preference (A1)", 10.0, "#ec4899")
                )).build();

        SubsidiaryCapTablePageDto.CapTableOwnershipCategoryDonutDto byCat = SubsidiaryCapTablePageDto.CapTableOwnershipCategoryDonutDto.builder()
                .totalShares("50,00,000")
                .slices(List.of(
                        new SubsidiaryCapTablePageDto.OwnershipCategorySliceDto("Institutional", 76.4, "#3b82f6"),
                        new SubsidiaryCapTablePageDto.OwnershipCategorySliceDto("Individual", 20.6, "#a855f7"),
                        new SubsidiaryCapTablePageDto.OwnershipCategorySliceDto("Foreign", 3.0, "#f59e0b")
                )).build();

        List<SubsidiaryCapTablePageDto.InvestmentVsOwnershipPointDto> invVsOwn = List.of(
                new SubsidiaryCapTablePageDto.InvestmentVsOwnershipPointDto("GF Fund", 12.0, 24.0),
                new SubsidiaryCapTablePageDto.InvestmentVsOwnershipPointDto("Sunrise", 7.5, 20.0),
                new SubsidiaryCapTablePageDto.InvestmentVsOwnershipPointDto("Global", 7.2, 14.4),
                new SubsidiaryCapTablePageDto.InvestmentVsOwnershipPointDto("Nexus", 5.0, 10.0),
                new SubsidiaryCapTablePageDto.InvestmentVsOwnershipPointDto("Strategic", 4.0, 8.0),
                new SubsidiaryCapTablePageDto.InvestmentVsOwnershipPointDto("Others", 14.3, 23.6)
        );

        return SubsidiaryCapTablePageDto.builder()
                .adminUser(adminUser)
                .pageTitle("Cap Table")
                .pageSubtitle("Complete shareholding structure, ownership, dilution and investment history.")
                .selectedCompany(company != null ? company : "Atirath Holdings")
                .companyStatus("Active")
                .kpis(kpis)
                .tabs(List.of("Current Cap Table", "Fully Diluted", "Investment Rounds", "Dilution Analysis", "Historical Cap Table"))
                .activeTab(tab != null ? tab : "Current Cap Table")
                .capTableRows(filtered)
                .totalShareholders(12)
                .currentPage(1)
                .totalPages(2)
                .shareholdingSummary(summary)
                .keyInformation(keyInfo)
                .ownershipByShareClass(byClass)
                .ownershipByCategory(byCat)
                .investmentVsOwnership(invVsOwn)
                .recentTransactions(capTableTransactionsList)
                .build();
    }

    public SubsidiaryCapTablePageDto.CapTableTransactionDto addCapTableTransaction(AddCapTableTransactionRequestDto req) {
        SubsidiaryCapTablePageDto.CapTableTransactionDto tx = SubsidiaryCapTablePageDto.CapTableTransactionDto.builder()
                .date(req.getTransactionDate() != null ? req.getTransactionDate() : "16 Sep 2026")
                .type(req.getType() != null ? req.getType() : "Issuance")
                .typeTheme(req.getType() != null && req.getType().equalsIgnoreCase("Transfer") ? "orange" : "green")
                .sharesFormatted(req.getNumberOfShares() != null ? req.getNumberOfShares() + " shares" : "1,00,000 shares")
                .price(req.getPricePerShare() > 0 ? req.getPricePerShare() : 100.0)
                .build();

        capTableTransactionsList.add(0, tx);
        return tx;
    }

    public Map<String, String> exportCapTable(String company) {
        return Map.of(
                "status", "READY",
                "fileName", "atirath_holdings_cap_table_statement_2026.xlsx",
                "downloadUrl", "https://atirathgroup.com/downloads/cap-table-statement-2026.xlsx"
        );
    }

    // ==========================================
    // 7. SHARE TRANSFER RULES
    // ==========================================
    public SubsidiaryShareTransferRulesPageDto getShareTransferRulesPage(String company, String tab, String query) {
        List<KpiCardDto> kpis = List.of(
                KpiCardDto.builder().title("Total Rules").value("8").change("+14% vs last quarter").comparisonText("").trend("positive").icon("file-text").color("blue").build(),
                KpiCardDto.builder().title("Applicable Share Classes").value("4").change("+0% vs last quarter").comparisonText("").trend("neutral").icon("layers").color("purple").build(),
                KpiCardDto.builder().title("Restricted Transfers").value("2").change("-33% vs last quarter").comparisonText("").trend("positive").icon("alert-triangle").color("red").build(),
                KpiCardDto.builder().title("Auto Approved Transfers").value("3").change("+50% vs last quarter").comparisonText("").trend("positive").icon("check-circle").color("blue").build()
        );

        List<SubsidiaryShareTransferRulesPageDto.ShareTransferRuleItemDto> filtered = shareTransferRulesList.stream()
                .filter(r -> {
                    if (query != null && !query.isBlank()) {
                        String q = query.toLowerCase();
                        if (!r.getRuleName().toLowerCase().contains(q) &&
                            !r.getRuleType().toLowerCase().contains(q)) return false;
                    }
                    if (tab != null && !tab.isBlank() && !tab.startsWith("All")) {
                        if (tab.contains("Restrictions") && !r.getRuleType().equalsIgnoreCase("Restriction")) return false;
                        if (tab.contains("Approval") && !r.getRuleType().equalsIgnoreCase("Approval Workflow")) return false;
                        if (tab.contains("Pre-emptive") && !r.getRuleType().equalsIgnoreCase("Pre-emptive Right")) return false;
                    }
                    return true;
                }).collect(Collectors.toList());

        ShareTransferRuleDetailDto selected = shareTransferRuleDetailsMap.getOrDefault("rule-1", shareTransferRuleDetailsMap.values().iterator().next());

        SubsidiaryShareTransferRulesPageDto.RulesByTypeDonutDto byType = SubsidiaryShareTransferRulesPageDto.RulesByTypeDonutDto.builder()
                .totalRules(8)
                .slices(List.of(
                        new SubsidiaryShareTransferRulesPageDto.RuleTypeSliceDto("Approval Workflow", 2, 25.0, "#3b82f6"),
                        new SubsidiaryShareTransferRulesPageDto.RuleTypeSliceDto("Restriction", 2, 25.0, "#ef4444"),
                        new SubsidiaryShareTransferRulesPageDto.RuleTypeSliceDto("Pre-emptive Right", 2, 25.0, "#a855f7"),
                        new SubsidiaryShareTransferRulesPageDto.RuleTypeSliceDto("General Rule", 2, 25.0, "#22c55e")
                )).build();

        List<SubsidiaryShareTransferRulesPageDto.ApplicableClassBarDto> appClasses = List.of(
                new SubsidiaryShareTransferRulesPageDto.ApplicableClassBarDto("Equity Shares (ES)", 4, "#3b82f6"),
                new SubsidiaryShareTransferRulesPageDto.ApplicableClassBarDto("Preference Shares (PS)", 2, "#22c55e"),
                new SubsidiaryShareTransferRulesPageDto.ApplicableClassBarDto("ESOP (ESOP)", 2, "#a855f7"),
                new SubsidiaryShareTransferRulesPageDto.ApplicableClassBarDto("Series A Preference (A1)", 1, "#f59e0b")
        );

        SubsidiaryShareTransferRulesPageDto.TransferApprovalStatusDonutDto appStatus = SubsidiaryShareTransferRulesPageDto.TransferApprovalStatusDonutDto.builder()
                .totalRequests(120)
                .slices(List.of(
                        new SubsidiaryShareTransferRulesPageDto.ApprovalStatusSliceDto("Approved", 70, 58.3, "#22c55e"),
                        new SubsidiaryShareTransferRulesPageDto.ApprovalStatusSliceDto("Pending", 30, 25.0, "#f59e0b"),
                        new SubsidiaryShareTransferRulesPageDto.ApprovalStatusSliceDto("Rejected", 20, 16.7, "#ef4444")
                )).build();

        return SubsidiaryShareTransferRulesPageDto.builder()
                .adminUser(adminUser)
                .pageTitle("Share Transfer Rules")
                .pageSubtitle("Define and manage rules for transfer of shares, approvals, restrictions and compliance checks.")
                .selectedCompany(company != null ? company : "Atirath Holdings")
                .companyStatus("Active")
                .kpis(kpis)
                .tabs(List.of("All Rules (8)", "Transfer Restrictions (3)", "Approval Workflow (2)", "Pre-emptive Rights (1)", "Other Rules (2)"))
                .activeTab(tab != null ? tab : "All Rules (8)")
                .rules(filtered)
                .totalRules(8)
                .selectedRuleDetail(selected)
                .rulesByType(byType)
                .applicableShareClasses(appClasses)
                .transferApprovalStatus(appStatus)
                .build();
    }

    public ShareTransferRuleDetailDto getShareTransferRuleDetail(String id) {
        return shareTransferRuleDetailsMap.getOrDefault(id, shareTransferRuleDetailsMap.get("rule-1"));
    }

    public ShareTransferRuleDetailDto addShareTransferRule(AddShareTransferRuleRequestDto req) {
        int nextNum = shareTransferRuleCounter.incrementAndGet();
        String id = "rule-" + nextNum;

        SubsidiaryShareTransferRulesPageDto.ShareTransferRuleItemDto item = SubsidiaryShareTransferRulesPageDto.ShareTransferRuleItemDto.builder()
                .id(id)
                .number(nextNum)
                .ruleName(req.getRuleName())
                .ruleType(req.getRuleType() != null ? req.getRuleType() : "General Rule")
                .ruleTypeTheme("blue")
                .applicableShareClasses(req.getApplicableShareClasses() != null ? req.getApplicableShareClasses() : "All Classes")
                .conditions(req.getConditions() != null ? req.getConditions() : "Standard transfer")
                .approvalRequired(req.getApprovalRequired() != null ? req.getApprovalRequired() : "Yes (Board)")
                .status("Active")
                .statusTheme("green")
                .build();

        shareTransferRulesList.add(item);

        ShareTransferRuleDetailDto detail = ShareTransferRuleDetailDto.builder()
                .id(id)
                .ruleName(req.getRuleName())
                .status("Active")
                .statusTheme("green")
                .ruleTypeBadge(req.getRuleType())
                .tabs(List.of("Details", "Conditions", "Approval Flow", "History"))
                .activeTab("Details")
                .ruleType(req.getRuleType())
                .applicableShareClasses(req.getApplicableShareClasses())
                .description(req.getDescription() != null ? req.getDescription() : "Newly established share transfer governance rule.")
                .conditions(req.getConditions())
                .approvalRequired(req.getApprovalRequired())
                .effectiveFrom(req.getEffectiveFrom() != null ? req.getEffectiveFrom() : "01 Oct 2026")
                .createdBy("Admin User")
                .createdOn("16 Sep 2026, 01:00 PM")
                .lastUpdated("16 Sep 2026, 01:00 PM")
                .build();

        shareTransferRuleDetailsMap.put(id, detail);
        return detail;
    }

    // ==========================================
    // 8. TRADING RULES
    // ==========================================
    public SubsidiaryTradingRulesPageDto getTradingRulesPage(String company, String tab, String query) {
        List<KpiCardDto> kpis = List.of(
                KpiCardDto.builder().title("Total Trading Rules").value("10").change("+25% vs last quarter").comparisonText("").trend("positive").icon("bar-chart").color("blue").build(),
                KpiCardDto.builder().title("Active Rules").value("8").change("+14% vs last quarter").comparisonText("").trend("positive").icon("clock").color("green").build(),
                KpiCardDto.builder().title("Suspended Rules").value("1").change("+0% vs last quarter").comparisonText("").trend("neutral").icon("pause-circle").color("red").build(),
                KpiCardDto.builder().title("Applicable Share Classes").value("4").change("+0% vs last quarter").comparisonText("").trend("neutral").icon("layers").color("purple").build()
        );

        List<SubsidiaryTradingRulesPageDto.TradingRuleItemDto> filtered = tradingRulesList.stream()
                .filter(r -> {
                    if (query != null && !query.isBlank()) {
                        String q = query.toLowerCase();
                        if (!r.getRuleName().toLowerCase().contains(q) &&
                            !r.getRuleType().toLowerCase().contains(q)) return false;
                    }
                    if (tab != null && !tab.isBlank() && !tab.startsWith("All")) {
                        if (tab.contains("Windows") && !r.getRuleType().equalsIgnoreCase("Trading Window")) return false;
                        if (tab.contains("Price") && !r.getRuleType().equalsIgnoreCase("Price Limit")) return false;
                        if (tab.contains("Order") && !r.getRuleType().equalsIgnoreCase("Order Limit")) return false;
                        if (tab.contains("Eligibility") && !r.getRuleType().equalsIgnoreCase("Eligibility Rule")) return false;
                    }
                    return true;
                }).collect(Collectors.toList());

        TradingRuleDetailDto selected = tradingRuleDetailsMap.getOrDefault("trule-1", tradingRuleDetailsMap.values().iterator().next());

        SubsidiaryTradingRulesPageDto.TradingRulesByTypeDonutDto byType = SubsidiaryTradingRulesPageDto.TradingRulesByTypeDonutDto.builder()
                .totalRules(10)
                .slices(List.of(
                        new SubsidiaryTradingRulesPageDto.TradingRuleTypeSliceDto("Trading Window", 3, 30.0, "#3b82f6"),
                        new SubsidiaryTradingRulesPageDto.TradingRuleTypeSliceDto("Price Limit", 2, 20.0, "#a855f7"),
                        new SubsidiaryTradingRulesPageDto.TradingRuleTypeSliceDto("Order Limit", 2, 20.0, "#22c55e"),
                        new SubsidiaryTradingRulesPageDto.TradingRuleTypeSliceDto("Eligibility Rule", 1, 10.0, "#f59e0b"),
                        new SubsidiaryTradingRulesPageDto.TradingRuleTypeSliceDto("Compliance Rule", 1, 10.0, "#ec4899"),
                        new SubsidiaryTradingRulesPageDto.TradingRuleTypeSliceDto("Approval Rule", 1, 10.0, "#06b6d4")
                )).build();

        SubsidiaryTradingRulesPageDto.TradingRuleStatusDonutDto statusDonut = SubsidiaryTradingRulesPageDto.TradingRuleStatusDonutDto.builder()
                .totalRules(10)
                .slices(List.of(
                        new SubsidiaryTradingRulesPageDto.TradingRuleStatusSliceDto("Active", 8, 80.0, "#22c55e"),
                        new SubsidiaryTradingRulesPageDto.TradingRuleStatusSliceDto("Suspended", 1, 10.0, "#ef4444"),
                        new SubsidiaryTradingRulesPageDto.TradingRuleStatusSliceDto("Inactive", 1, 10.0, "#94a3b8")
                )).build();

        List<SubsidiaryTradingRulesPageDto.TradingApplicableClassBarDto> appClasses = List.of(
                new SubsidiaryTradingRulesPageDto.TradingApplicableClassBarDto("Equity Shares (ES)", 5, "#3b82f6"),
                new SubsidiaryTradingRulesPageDto.TradingApplicableClassBarDto("Preference Shares (PS)", 4, "#22c55e"),
                new SubsidiaryTradingRulesPageDto.TradingApplicableClassBarDto("ESOP (ESOP)", 2, "#a855f7"),
                new SubsidiaryTradingRulesPageDto.TradingApplicableClassBarDto("Series A (A1)", 1, "#f59e0b")
        );

        return SubsidiaryTradingRulesPageDto.builder()
                .adminUser(adminUser)
                .pageTitle("Trading Rules")
                .pageSubtitle("Define and manage trading rules, order limits, trading windows, price bands and compliance requirements.")
                .selectedCompany(company != null ? company : "Atirath Holdings")
                .companyStatus("Active")
                .kpis(kpis)
                .tabs(List.of("All Trading Rules (10)", "Trading Windows (3)", "Price Limits (2)", "Order Limits (2)", "Eligibility Rules (2)", "Other Rules (1)"))
                .activeTab(tab != null ? tab : "All Trading Rules (10)")
                .rules(filtered)
                .totalRules(10)
                .currentPage(1)
                .totalPages(1)
                .selectedRuleDetail(selected)
                .rulesByType(byType)
                .ruleStatus(statusDonut)
                .applicableShareClasses(appClasses)
                .build();
    }

    public TradingRuleDetailDto getTradingRuleDetail(String id) {
        return tradingRuleDetailsMap.getOrDefault(id, tradingRuleDetailsMap.get("trule-1"));
    }

    public TradingRuleDetailDto addTradingRule(AddTradingRuleRequestDto req) {
        int nextNum = tradingRuleCounter.incrementAndGet();
        String id = "trule-" + nextNum;

        SubsidiaryTradingRulesPageDto.TradingRuleItemDto item = SubsidiaryTradingRulesPageDto.TradingRuleItemDto.builder()
                .id(id)
                .number(nextNum)
                .ruleName(req.getRuleName())
                .ruleType(req.getRuleType() != null ? req.getRuleType() : "Trading Window")
                .ruleTypeTheme("blue")
                .applicableShareClasses(req.getApplicableShareClasses() != null ? req.getApplicableShareClasses() : "All Classes")
                .parameters(req.getParameters() != null ? req.getParameters() : "Configured limit")
                .status("Active")
                .statusTheme("green")
                .effectiveFrom(req.getEffectiveFrom() != null ? req.getEffectiveFrom() : "01 Oct 2026")
                .build();

        tradingRulesList.add(item);

        TradingRuleDetailDto detail = TradingRuleDetailDto.builder()
                .id(id)
                .ruleName(req.getRuleName())
                .status("Active")
                .statusTheme("green")
                .ruleTypeBadge(req.getRuleType())
                .tabs(List.of("Details", "Applicable Classes", "Exceptions", "History"))
                .activeTab("Details")
                .ruleType(req.getRuleType())
                .description(req.getDescription() != null ? req.getDescription() : "Standard trading regulatory policy.")
                .applicableShareClasses(req.getApplicableShareClasses())
                .windowOpenDate("1st day of period")
                .windowCloseDate("25th day of period")
                .restrictedPeriod("Last 5 days of period")
                .conditions(req.getConditions() != null ? req.getConditions() : "Automated compliance enforcement")
                .effectiveFrom(req.getEffectiveFrom() != null ? req.getEffectiveFrom() : "01 Oct 2026")
                .createdBy("Admin User")
                .createdOn("16 Sep 2026, 01:15 PM")
                .lastUpdated("16 Sep 2026, 01:15 PM")
                .build();

        tradingRuleDetailsMap.put(id, detail);
        return detail;
    }

    public TradingRuleDetailDto toggleTradingRuleStatus(String id) {
        TradingRuleDetailDto detail = getTradingRuleDetail(id);
        String newStatus = detail.getStatus().equalsIgnoreCase("Active") ? "Suspended" : "Active";
        String newTheme = newStatus.equalsIgnoreCase("Active") ? "green" : "red";
        detail.setStatus(newStatus);
        detail.setStatusTheme(newTheme);
        tradingRulesList.stream().filter(r -> r.getId().equalsIgnoreCase(id)).findFirst().ifPresent(r -> {
            r.setStatus(newStatus);
            r.setStatusTheme(newTheme);
        });
        return detail;
    }

    // ==========================================
    // SEEDING HELPERS 5 - 8
    // ==========================================
    private void seedShareholders() {
        shareholdersList.clear();

        shareholdersList.add(new SubsidiaryShareholdersPageDto.ShareholderItemDto("sh-1", 1, "Green Future Fund", "GF", "Institutional", "blue", "VC Fund", "12,00,000", 24.0, 12.0, "Active", "green"));
        shareholdersList.add(new SubsidiaryShareholdersPageDto.ShareholderItemDto("sh-2", 2, "Sunrise Ventures", "SV", "Institutional", "blue", "PE Fund", "8,00,000", 16.0, 8.0, "Active", "green"));
        shareholdersList.add(new SubsidiaryShareholdersPageDto.ShareholderItemDto("sh-3", 3, "Global Energy Partners", "GE", "Institutional", "blue", "Strategic", "7,20,000", 14.4, 7.2, "Active", "green"));
        shareholdersList.add(new SubsidiaryShareholdersPageDto.ShareholderItemDto("sh-4", 4, "Nexus Capital", "NC", "Institutional", "blue", "PE Fund", "5,00,000", 10.0, 5.0, "Active", "green"));
        shareholdersList.add(new SubsidiaryShareholdersPageDto.ShareholderItemDto("sh-5", 5, "Strategic Investors", "SI", "Institutional", "blue", "Corporate", "4,00,000", 8.0, 4.0, "Active", "green"));
        shareholdersList.add(new SubsidiaryShareholdersPageDto.ShareholderItemDto("sh-6", 6, "Ramesh Kumar", "RK", "Individual", "purple", "Promoter", "3,00,000", 6.0, 3.0, "Active", "green"));
        shareholdersList.add(new SubsidiaryShareholdersPageDto.ShareholderItemDto("sh-7", 7, "Priya Verma", "PV", "Individual", "purple", "Individual", "2,50,000", 5.0, 2.5, "Active", "green"));
        shareholdersList.add(new SubsidiaryShareholdersPageDto.ShareholderItemDto("sh-8", 8, "Amit Singh", "AS", "Individual", "purple", "Individual", "2,00,000", 4.0, 2.0, "Active", "green"));
        shareholdersList.add(new SubsidiaryShareholdersPageDto.ShareholderItemDto("sh-9", 9, "Meera Reddy", "MR", "Individual", "purple", "Individual", "1,50,000", 3.0, 1.5, "Active", "green"));
        shareholdersList.add(new SubsidiaryShareholdersPageDto.ShareholderItemDto("sh-10", 10, "David Brown", "DB", "Foreign", "orange", "Individual", "1,30,000", 2.6, 1.3, "Active", "green"));

        ShareholderDetailDto sh1Detail = ShareholderDetailDto.builder()
                .id("sh-1")
                .shareholderName("Green Future Fund")
                .initials("GF")
                .status("Active")
                .statusTheme("green")
                .categoryBadge("Institutional Investor")
                .typeBadge("VC Fund")
                .tabs(List.of("Overview", "Holdings", "Documents", "History"))
                .activeTab("Overview")
                .category("Institutional")
                .type("VC Fund")
                .noOfShares("12,00,000")
                .ownershipPercentage(24.0)
                .investmentAmount("₹ 12.0 Cr")
                .pan("AAACG1234D")
                .country("India")
                .registeredAddress("Unit 501, Capital Tower, Bandra Kurla Complex, Mumbai, Maharashtra - 400051")
                .contactPerson("Rahul Mehta")
                .email("rahul@greenfuturefund.in")
                .phone("+91 98765 43210")
                .kycStatus("Verified")
                .kycStatusTheme("green")
                .lastUpdated("10 Sep 2026")
                .build();

        shareholderDetailsMap.put("sh-1", sh1Detail);
    }

    private void seedCapTable() {
        capTableRowsList.clear();

        capTableRowsList.add(new SubsidiaryCapTablePageDto.CapTableRowDto("ct-1", 1, "Green Future Fund", "GF", "Institutional", "blue", "Equity (ES)", "12,00,000", 24.0, 12.0, 100.0, "Active", "green"));
        capTableRowsList.add(new SubsidiaryCapTablePageDto.CapTableRowDto("ct-2", 2, "Sunrise Ventures", "SV", "Institutional", "blue", "Preference (PS)", "10,00,000", 20.0, 7.5, 75.0, "Active", "green"));
        capTableRowsList.add(new SubsidiaryCapTablePageDto.CapTableRowDto("ct-3", 3, "Global Energy Partners", "GE", "Institutional", "blue", "Equity (ES)", "7,20,000", 14.4, 7.2, 100.0, "Active", "green"));
        capTableRowsList.add(new SubsidiaryCapTablePageDto.CapTableRowDto("ct-4", 4, "Nexus Capital", "NC", "Institutional", "blue", "Series A (A1)", "5,00,000", 10.0, 5.0, 120.0, "Active", "green"));
        capTableRowsList.add(new SubsidiaryCapTablePageDto.CapTableRowDto("ct-5", 5, "Strategic Investors", "SI", "Institutional", "blue", "Equity (ES)", "4,00,000", 8.0, 4.0, 100.0, "Active", "green"));
        capTableRowsList.add(new SubsidiaryCapTablePageDto.CapTableRowDto("ct-6", 6, "Ramesh Kumar", "RK", "Individual", "purple", "Equity (ES)", "3,00,000", 6.0, 3.0, 100.0, "Active", "green"));
        capTableRowsList.add(new SubsidiaryCapTablePageDto.CapTableRowDto("ct-7", 7, "Priya Verma", "PV", "Individual", "purple", "ESOP (ESOP)", "2,50,000", 5.0, 2.5, 100.0, "Active", "green"));
        capTableRowsList.add(new SubsidiaryCapTablePageDto.CapTableRowDto("ct-8", 8, "Amit Singh", "AS", "Individual", "purple", "Equity (ES)", "2,00,000", 4.0, 2.0, 100.0, "Active", "green"));
        capTableRowsList.add(new SubsidiaryCapTablePageDto.CapTableRowDto("ct-9", 9, "Meera Reddy", "MR", "Individual", "purple", "Equity (ES)", "1,50,000", 3.0, 1.5, 100.0, "Active", "green"));
        capTableRowsList.add(new SubsidiaryCapTablePageDto.CapTableRowDto("ct-10", 10, "David Brown", "DB", "Foreign", "orange", "Equity (ES)", "1,30,000", 2.6, 1.3, 100.0, "Active", "green"));

        capTableTransactionsList.clear();
        capTableTransactionsList.add(new SubsidiaryCapTablePageDto.CapTableTransactionDto("10 Sep 2026", "Issuance", "green", "2,50,000", 100.0));
        capTableTransactionsList.add(new SubsidiaryCapTablePageDto.CapTableTransactionDto("05 Sep 2026", "Transfer", "orange", "50,000", 100.0));
        capTableTransactionsList.add(new SubsidiaryCapTablePageDto.CapTableTransactionDto("01 Sep 2026", "Issuance", "green", "1,00,000", 75.0));
        capTableTransactionsList.add(new SubsidiaryCapTablePageDto.CapTableTransactionDto("25 Aug 2026", "Transfer", "orange", "30,000", 100.0));
        capTableTransactionsList.add(new SubsidiaryCapTablePageDto.CapTableTransactionDto("20 Aug 2026", "Issuance", "green", "5,00,000", 120.0));
    }

    private void seedShareTransferRules() {
        shareTransferRulesList.clear();

        shareTransferRulesList.add(new SubsidiaryShareTransferRulesPageDto.ShareTransferRuleItemDto("rule-1", 1, "Board Approval for ES Transfer", "Approval Workflow", "blue", "Equity Shares (ES)", "Transfer to external party", "Yes (Board)", "Active", "green"));
        shareTransferRulesList.add(new SubsidiaryShareTransferRulesPageDto.ShareTransferRuleItemDto("rule-2", 2, "Pre-emption for Existing Shareholders", "Pre-emptive Right", "purple", "All Classes", "Offer to existing shareholders", "Yes", "Active", "green"));
        shareTransferRulesList.add(new SubsidiaryShareTransferRulesPageDto.ShareTransferRuleItemDto("rule-3", 3, "Restricted Transfer (Lock-in)", "Restriction", "red", "Preference Shares (PS)", "No transfer for 2 years", "N/A", "Active", "green"));
        shareTransferRulesList.add(new SubsidiaryShareTransferRulesPageDto.ShareTransferRuleItemDto("rule-4", 4, "Employee Transfer Approval", "Approval Workflow", "blue", "ESOP (ESOP)", "Transfer between employees", "Yes (HR + Board)", "Active", "green"));
        shareTransferRulesList.add(new SubsidiaryShareTransferRulesPageDto.ShareTransferRuleItemDto("rule-5", 5, "Open Transfer for Series A", "General Rule", "green", "Series A Preference (A1)", "Free transfer allowed", "No", "Active", "green"));
        shareTransferRulesList.add(new SubsidiaryShareTransferRulesPageDto.ShareTransferRuleItemDto("rule-6", 6, "Right of First Refusal", "Pre-emptive Right", "purple", "All Classes", "Offer to company first", "Yes", "Inactive", "red"));
        shareTransferRulesList.add(new SubsidiaryShareTransferRulesPageDto.ShareTransferRuleItemDto("rule-7", 7, "Restricted Transfer to Competitor", "Restriction", "red", "All Classes", "No transfer to competitors", "N/A", "Active", "green"));
        shareTransferRulesList.add(new SubsidiaryShareTransferRulesPageDto.ShareTransferRuleItemDto("rule-8", 8, "Auto Approval for Internal Transfer", "General Rule", "green", "Equity Shares (ES)", "Transfer within group", "No", "Active", "green"));

        ShareTransferRuleDetailDto rule1Detail = ShareTransferRuleDetailDto.builder()
                .id("rule-1")
                .ruleName("Board Approval for ES Transfer")
                .status("Active")
                .statusTheme("green")
                .ruleTypeBadge("Approval Workflow")
                .tabs(List.of("Details", "Conditions", "Approval Flow", "History"))
                .activeTab("Details")
                .ruleType("Approval Workflow")
                .applicableShareClasses("Equity Shares (ES)")
                .description("Transfer of equity shares to any external party requires prior approval from the Board of Directors.")
                .conditions("Transfer to external party")
                .approvalRequired("Yes (Board)")
                .effectiveFrom("01 Jan 2025")
                .createdBy("Amit Singh")
                .createdOn("15 Jan 2025, 10:30 AM")
                .lastUpdated("10 Sep 2026, 04:20 PM")
                .build();

        shareTransferRuleDetailsMap.put("rule-1", rule1Detail);
    }

    private void seedTradingRules() {
        tradingRulesList.clear();

        tradingRulesList.add(new SubsidiaryTradingRulesPageDto.TradingRuleItemDto("trule-1", 1, "Trading Window (Quarterly)", "Trading Window", "blue", "All Classes", "Open: 1-25 of quarter", "Active", "green", "01 Jan 2025"));
        tradingRulesList.add(new SubsidiaryTradingRulesPageDto.TradingRuleItemDto("trule-2", 2, "Price Band Limit", "Price Limit", "purple", "Equity Shares (ES)", "±20% from reference price", "Active", "green", "01 Jan 2025"));
        tradingRulesList.add(new SubsidiaryTradingRulesPageDto.TradingRuleItemDto("trule-3", 3, "Order Value Limit (Retail)", "Order Limit", "green", "Preference Shares (PS)", "Max ₹10,00,000 per order", "Active", "green", "01 Jan 2025"));
        tradingRulesList.add(new SubsidiaryTradingRulesPageDto.TradingRuleItemDto("trule-4", 4, "Order Quantity Limit", "Order Limit", "green", "ES, PS", "Max 1,00,000 shares", "Active", "green", "01 Jan 2025"));
        tradingRulesList.add(new SubsidiaryTradingRulesPageDto.TradingRuleItemDto("trule-5", 5, "Eligible Investors", "Eligibility Rule", "orange", "ES, PS, ESOP", "Only approved investors", "Active", "green", "01 Jan 2025"));
        tradingRulesList.add(new SubsidiaryTradingRulesPageDto.TradingRuleItemDto("trule-6", 6, "Insider Trading Restriction", "Compliance Rule", "pink", "All Classes", "Restricted for insiders", "Active", "green", "01 Jan 2025"));
        tradingRulesList.add(new SubsidiaryTradingRulesPageDto.TradingRuleItemDto("trule-7", 7, "Minimum Holding Period", "Other Rule", "blue", "Series A (A1)", "No sell within 6 months", "Active", "green", "01 Jan 2025"));
        tradingRulesList.add(new SubsidiaryTradingRulesPageDto.TradingRuleItemDto("trule-8", 8, "KYC Completion", "Eligibility Rule", "orange", "All Classes", "KYC mandatory before trade", "Active", "green", "01 Jan 2025"));
        tradingRulesList.add(new SubsidiaryTradingRulesPageDto.TradingRuleItemDto("trule-9", 9, "Volume Limit (Daily)", "Order Limit", "green", "ES", "Max 5% of daily volume", "Suspended", "red", "01 Jan 2025"));
        tradingRulesList.add(new SubsidiaryTradingRulesPageDto.TradingRuleItemDto("trule-10", 10, "Board Approval for Large Trades", "Approval Rule", "blue", "ES, PS", "Trades above ₹50 Cr", "Active", "green", "01 Jan 2025"));

        TradingRuleDetailDto trule1Detail = TradingRuleDetailDto.builder()
                .id("trule-1")
                .ruleName("Trading Window (Quarterly)")
                .status("Active")
                .statusTheme("green")
                .ruleTypeBadge("Trading Window")
                .tabs(List.of("Details", "Applicable Classes", "Exceptions", "History"))
                .activeTab("Details")
                .ruleType("Trading Window")
                .description("Defines the trading window period for each quarter.")
                .applicableShareClasses("All Classes")
                .windowOpenDate("1st day of quarter")
                .windowCloseDate("25th day of quarter")
                .restrictedPeriod("Last 5 days of quarter")
                .conditions("Trades not allowed during restricted period.")
                .effectiveFrom("01 Jan 2025")
                .createdBy("Priya Verma")
                .createdOn("10 Dec 2024, 11:30 AM")
                .lastUpdated("15 Sep 2026, 04:20 PM")
                .build();

        tradingRuleDetailsMap.put("trule-1", trule1Detail);
    }

    // ==========================================
    // 9. COMPANY APPROVERS
    // ==========================================
    public SubsidiaryApproversPageDto getSubsidiaryApproversPage(String company, String tab, String query) {
        List<KpiCardDto> kpis = List.of(
                KpiCardDto.builder().title("Total Approvers").value("8").change("+14% vs last quarter").comparisonText("").trend("positive").icon("users").color("blue").build(),
                KpiCardDto.builder().title("Active Approvers").value("6").change("+20% vs last quarter").comparisonText("").trend("positive").icon("shield").color("green").build(),
                KpiCardDto.builder().title("Inactive Approvers").value("2").change("-33% vs last quarter").comparisonText("").trend("negative").icon("pause-circle").color("red").build(),
                KpiCardDto.builder().title("Approval Workflows").value("5").change("+25% vs last quarter").comparisonText("").trend("positive").icon("file-text").color("blue").build()
        );

        List<SubsidiaryApproversPageDto.CompanyApproverItemDto> filtered = companyApproversList.stream()
                .filter(a -> {
                    if (query != null && !query.isBlank()) {
                        String q = query.toLowerCase();
                        if (!a.getApproverName().toLowerCase().contains(q) &&
                            !a.getRoleOrDesignation().toLowerCase().contains(q) &&
                            !a.getDepartment().toLowerCase().contains(q)) return false;
                    }
                    if (tab != null && !tab.isBlank() && !tab.startsWith("All")) {
                        if (tab.equalsIgnoreCase("Inactive") && !a.getStatus().equalsIgnoreCase("Inactive")) return false;
                        if (tab.contains("Department") && a.getStatus().equalsIgnoreCase("Inactive")) return false;
                    }
                    return true;
                }).collect(Collectors.toList());

        CompanyApproverDetailDto selected = companyApproverDetailsMap.getOrDefault("appr-1", companyApproverDetailsMap.values().iterator().next());

        SubsidiaryApproversPageDto.ApproversByDepartmentDonutDto byDept = SubsidiaryApproversPageDto.ApproversByDepartmentDonutDto.builder()
                .totalApprovers(8)
                .slices(List.of(
                        new SubsidiaryApproversPageDto.DepartmentApproverSliceDto("Board", 2, 25.0, "#3b82f6"),
                        new SubsidiaryApproversPageDto.DepartmentApproverSliceDto("Finance", 2, 25.0, "#22c55e"),
                        new SubsidiaryApproversPageDto.DepartmentApproverSliceDto("Legal", 1, 12.5, "#a855f7"),
                        new SubsidiaryApproversPageDto.DepartmentApproverSliceDto("Operations", 1, 12.5, "#06b6d4"),
                        new SubsidiaryApproversPageDto.DepartmentApproverSliceDto("Compliance", 1, 12.5, "#f59e0b"),
                        new SubsidiaryApproversPageDto.DepartmentApproverSliceDto("Strategy", 1, 12.5, "#ec4899")
                )).build();

        SubsidiaryApproversPageDto.ApproversByApprovalTypeDonutDto byType = SubsidiaryApproversPageDto.ApproversByApprovalTypeDonutDto.builder()
                .totalApprovers(8)
                .slices(List.of(
                        new SubsidiaryApproversPageDto.ApprovalTypeSliceDto("Share Issuance", 2, 25.0, "#3b82f6"),
                        new SubsidiaryApproversPageDto.ApprovalTypeSliceDto("Share Transfer", 2, 25.0, "#22c55e"),
                        new SubsidiaryApproversPageDto.ApprovalTypeSliceDto("ESOP", 1, 12.5, "#a855f7"),
                        new SubsidiaryApproversPageDto.ApprovalTypeSliceDto("Strategic Issuance", 1, 12.5, "#06b6d4"),
                        new SubsidiaryApproversPageDto.ApprovalTypeSliceDto("Routine Transfer", 1, 12.5, "#f59e0b"),
                        new SubsidiaryApproversPageDto.ApprovalTypeSliceDto("Others", 1, 12.5, "#ec4899")
                )).build();

        List<SubsidiaryApproversPageDto.ApprovalLimitBarDto> limitBars = List.of(
                new SubsidiaryApproversPageDto.ApprovalLimitBarDto("< 1 Cr", 2, "#3b82f6"),
                new SubsidiaryApproversPageDto.ApprovalLimitBarDto("1 - 10 Cr", 2, "#3b82f6"),
                new SubsidiaryApproversPageDto.ApprovalLimitBarDto("10 - 50 Cr", 1, "#3b82f6"),
                new SubsidiaryApproversPageDto.ApprovalLimitBarDto("> 50 Cr", 3, "#3b82f6")
        );

        return SubsidiaryApproversPageDto.builder()
                .adminUser(adminUser)
                .pageTitle("Company Approvers")
                .pageSubtitle("Manage approval authorities for share issuances, transfers, corporate actions and other key transactions.")
                .selectedCompany(company != null ? company : "Atirath Holdings")
                .companyStatus("Active")
                .kpis(kpis)
                .tabs(List.of("All Approvers (8)", "By Department (5)", "By Approval Type (5)", "Inactive (2)"))
                .activeTab(tab != null ? tab : "All Approvers (8)")
                .approvers(filtered)
                .totalApprovers(8)
                .selectedApproverDetail(selected)
                .approversByDepartment(byDept)
                .approversByApprovalType(byType)
                .approvalLimitDistribution(limitBars)
                .build();
    }

    public CompanyApproverDetailDto getCompanyApproverDetail(String id) {
        return companyApproverDetailsMap.getOrDefault(id, companyApproverDetailsMap.get("appr-1"));
    }

    public CompanyApproverDetailDto addCompanyApprover(AddCompanyApproverRequestDto req) {
        int nextNum = approverCounter.incrementAndGet();
        String id = "appr-" + nextNum;
        String initials = req.getFullName().length() >= 2 ?
                req.getFullName().substring(0, 2).toUpperCase() : "AP";

        SubsidiaryApproversPageDto.CompanyApproverItemDto item = SubsidiaryApproversPageDto.CompanyApproverItemDto.builder()
                .id(id)
                .number(nextNum)
                .approverName(req.getFullName())
                .initials(initials)
                .roleOrDesignation(req.getDesignation() != null ? req.getDesignation() : "Officer")
                .department(req.getDepartment() != null ? req.getDepartment() : "Operations")
                .approvalTypes(req.getApprovalTypes() != null ? req.getApprovalTypes() : "General Approval")
                .approvalLimit(req.getApprovalLimit() != null ? req.getApprovalLimit() : "1,00,00,000")
                .status("Active")
                .statusTheme("green")
                .build();

        companyApproversList.add(item);

        CompanyApproverDetailDto detail = CompanyApproverDetailDto.builder()
                .id(id)
                .approverName(req.getFullName())
                .initials(initials)
                .designation(req.getDesignation())
                .department(req.getDepartment())
                .status("Active")
                .statusTheme("green")
                .tabs(List.of("Details", "Approval Limits", "Approval Types", "History"))
                .activeTab("Details")
                .fullName(req.getFullName())
                .email(req.getEmail() != null ? req.getEmail() : "approver@example.com")
                .phone(req.getPhone() != null ? req.getPhone() : "+91 99999 88888")
                .approvalLimit(req.getApprovalLimit() != null ? req.getApprovalLimit() : "1,00,00,000")
                .approvalTypes(req.getApprovalTypes() != null ? req.getApprovalTypes() : "General Approval")
                .effectiveFrom(req.getEffectiveFrom() != null ? req.getEffectiveFrom() : "01 Oct 2026")
                .createdBy("Admin User")
                .createdOn("16 Sep 2026, 02:00 PM")
                .lastUpdated("16 Sep 2026, 02:00 PM")
                .remarks(req.getRemarks() != null ? req.getRemarks() : "Newly appointed corporate approver.")
                .build();

        companyApproverDetailsMap.put(id, detail);
        return detail;
    }

    public CompanyApproverDetailDto toggleCompanyApproverStatus(String id) {
        CompanyApproverDetailDto detail = getCompanyApproverDetail(id);
        String newStatus = detail.getStatus().equalsIgnoreCase("Active") ? "Inactive" : "Active";
        String newTheme = newStatus.equalsIgnoreCase("Active") ? "green" : "red";
        detail.setStatus(newStatus);
        detail.setStatusTheme(newTheme);
        companyApproversList.stream().filter(a -> a.getId().equalsIgnoreCase(id)).findFirst().ifPresent(a -> {
            a.setStatus(newStatus);
            a.setStatusTheme(newTheme);
        });
        return detail;
    }

    // ==========================================
    // 10. COMPANY DOCUMENTS
    // ==========================================
    public SubsidiaryDocumentsPageDto getSubsidiaryDocumentsPage(String company, String tab, String query) {
        List<KpiCardDto> kpis = List.of(
                KpiCardDto.builder().title("Total Documents").value("24").change("+33% vs last quarter").comparisonText("").trend("positive").icon("file-text").color("blue").build(),
                KpiCardDto.builder().title("Verified Documents").value("18").change("+20% vs last quarter").comparisonText("").trend("positive").icon("check-circle").color("green").build(),
                KpiCardDto.builder().title("Expiring Soon").value("3").change("+0% vs last quarter").comparisonText("").trend("neutral").icon("alert-triangle").color("red").build(),
                KpiCardDto.builder().title("Document Categories").value("8").change("+0% vs last quarter").comparisonText("").trend("neutral").icon("folder").color("purple").build()
        );

        List<SubsidiaryDocumentsPageDto.CompanyDocumentItemDto> filtered = companyDocumentsList.stream()
                .filter(d -> {
                    if (query != null && !query.isBlank()) {
                        String q = query.toLowerCase();
                        if (!d.getDocumentName().toLowerCase().contains(q) &&
                            !d.getCategory().toLowerCase().contains(q) &&
                            !d.getDocumentType().toLowerCase().contains(q)) return false;
                    }
                    if (tab != null && !tab.isBlank() && !tab.startsWith("All")) {
                        if (tab.equalsIgnoreCase("Statutory") && !d.getCategory().equalsIgnoreCase("Statutory")) return false;
                        if (tab.equalsIgnoreCase("Regulatory") && !d.getCategory().equalsIgnoreCase("Regulatory")) return false;
                        if (tab.equalsIgnoreCase("Corporate") && !d.getCategory().equalsIgnoreCase("Corporate")) return false;
                        if (tab.equalsIgnoreCase("Financial") && !d.getCategory().equalsIgnoreCase("Financial")) return false;
                        if (tab.equalsIgnoreCase("Others") && !d.getCategory().equalsIgnoreCase("Others")) return false;
                    }
                    return true;
                }).collect(Collectors.toList());

        CompanyDocumentDetailDto selected = companyDocumentDetailsMap.getOrDefault("doc-1", companyDocumentDetailsMap.values().iterator().next());

        SubsidiaryDocumentsPageDto.DocumentsByCategoryDonutDto byCat = SubsidiaryDocumentsPageDto.DocumentsByCategoryDonutDto.builder()
                .totalDocuments(24)
                .slices(List.of(
                        new SubsidiaryDocumentsPageDto.DocumentCategorySliceDto("Statutory", 8, 33.3, "#3b82f6"),
                        new SubsidiaryDocumentsPageDto.DocumentCategorySliceDto("Corporate", 6, 25.0, "#22c55e"),
                        new SubsidiaryDocumentsPageDto.DocumentCategorySliceDto("Regulatory", 5, 20.8, "#f59e0b"),
                        new SubsidiaryDocumentsPageDto.DocumentCategorySliceDto("Financial", 3, 12.5, "#06b6d4"),
                        new SubsidiaryDocumentsPageDto.DocumentCategorySliceDto("Others", 2, 8.3, "#a855f7")
                )).build();

        SubsidiaryDocumentsPageDto.DocumentStatusDonutDto statusDonut = SubsidiaryDocumentsPageDto.DocumentStatusDonutDto.builder()
                .totalDocuments(24)
                .slices(List.of(
                        new SubsidiaryDocumentsPageDto.DocStatusSliceDto("Verified", 18, 75.0, "#22c55e"),
                        new SubsidiaryDocumentsPageDto.DocStatusSliceDto("Active", 4, 16.7, "#3b82f6"),
                        new SubsidiaryDocumentsPageDto.DocStatusSliceDto("Expiring Soon", 2, 8.3, "#f59e0b"),
                        new SubsidiaryDocumentsPageDto.DocStatusSliceDto("Expired", 0, 0.0, "#ef4444")
                )).build();

        List<SubsidiaryDocumentsPageDto.ExpiryTimelineBarDto> expiryBars = List.of(
                new SubsidiaryDocumentsPageDto.ExpiryTimelineBarDto("< 3 Months", 2, "#f59e0b"),
                new SubsidiaryDocumentsPageDto.ExpiryTimelineBarDto("3 - 6 Months", 3, "#f97316"),
                new SubsidiaryDocumentsPageDto.ExpiryTimelineBarDto("6 - 12 Months", 5, "#3b82f6"),
                new SubsidiaryDocumentsPageDto.ExpiryTimelineBarDto("> 1 Year", 0, "#94a3b8")
        );

        return SubsidiaryDocumentsPageDto.builder()
                .adminUser(adminUser)
                .pageTitle("Company Documents")
                .pageSubtitle("Manage all statutory, regulatory, corporate and operational documents.")
                .selectedCompany(company != null ? company : "Atirath Holdings")
                .companyStatus("Active")
                .kpis(kpis)
                .tabs(List.of("All Documents (24)", "Statutory (8)", "Regulatory (5)", "Corporate (6)", "Financial (3)", "Others (2)"))
                .activeTab(tab != null ? tab : "All Documents (24)")
                .documents(filtered)
                .totalDocuments(24)
                .currentPage(1)
                .totalPages(3)
                .selectedDocumentDetail(selected)
                .documentsByCategory(byCat)
                .documentStatus(statusDonut)
                .expiryTimeline(expiryBars)
                .build();
    }

    public CompanyDocumentDetailDto getCompanyDocumentDetail(String id) {
        return companyDocumentDetailsMap.getOrDefault(id, companyDocumentDetailsMap.get("doc-1"));
    }

    public CompanyDocumentDetailDto uploadCompanyDocument(UploadCompanyDocumentRequestDto req) {
        int nextNum = documentCounter.incrementAndGet();
        String id = "doc-" + nextNum;

        SubsidiaryDocumentsPageDto.CompanyDocumentItemDto item = SubsidiaryDocumentsPageDto.CompanyDocumentItemDto.builder()
                .id(id)
                .number(nextNum)
                .documentName(req.getDocumentName())
                .category(req.getCategory() != null ? req.getCategory() : "Corporate")
                .categoryTheme("blue")
                .documentType(req.getDocumentType() != null ? req.getDocumentType() : "Agreement")
                .fileType(req.getFileType() != null ? req.getFileType() : "PDF")
                .uploadedOn("16 Sep 2026")
                .expiryDate(req.getExpiryDate() != null ? req.getExpiryDate() : "-")
                .status("Active")
                .statusTheme("green")
                .build();

        companyDocumentsList.add(0, item);

        CompanyDocumentDetailDto detail = CompanyDocumentDetailDto.builder()
                .id(id)
                .documentName(req.getDocumentName())
                .fileType(req.getFileType() != null ? req.getFileType() : "PDF")
                .status("Active")
                .statusTheme("green")
                .categoryBadge(req.getCategory())
                .documentTypeBadge(req.getDocumentType())
                .tabs(List.of("Details", "Version History", "Access Control", "Activity"))
                .activeTab("Details")
                .category(req.getCategory())
                .documentType(req.getDocumentType())
                .fileName(req.getFileName() != null ? req.getFileName() : req.getDocumentName().replace(" ", "_") + ".pdf")
                .fileSize(req.getFileSize() != null ? req.getFileSize() : "1.5 MB")
                .uploadedOn("16 Sep 2026, 02:15 PM")
                .uploadedBy("Admin User")
                .expiryDate(req.getExpiryDate() != null ? req.getExpiryDate() : "-")
                .documentNumber(req.getDocumentNumber() != null ? req.getDocumentNumber() : "DOC-" + nextNum)
                .issuedBy(req.getIssuedBy() != null ? req.getIssuedBy() : "Atirath Holdings Ltd")
                .description(req.getDescription() != null ? req.getDescription() : "Standard official document.")
                .downloadUrl("https://atirathgroup.com/docs/" + id + ".pdf")
                .build();

        companyDocumentDetailsMap.put(id, detail);
        return detail;
    }

    public Map<String, String> downloadCompanyDocument(String id) {
        CompanyDocumentDetailDto doc = getCompanyDocumentDetail(id);
        return Map.of(
                "status", "READY",
                "documentId", id,
                "fileName", doc.getFileName(),
                "downloadUrl", doc.getDownloadUrl() != null ? doc.getDownloadUrl() : "https://atirathgroup.com/docs/" + id + ".pdf"
        );
    }

    // ==========================================
    // 11. COMPANY TRANSACTIONS
    // ==========================================
    public SubsidiaryTransactionsPageDto getSubsidiaryTransactionsPage(String company, String tab, String query) {
        List<KpiCardDto> kpis = List.of(
                KpiCardDto.builder().title("Total Transactions").value("156").change("+28% vs last quarter").comparisonText("").trend("positive").icon("repeat").color("blue").build(),
                KpiCardDto.builder().title("Total Transaction Value").value("₹ 125.80 Cr").change("+32% vs last quarter").comparisonText("").trend("positive").icon("rupee").color("green").build(),
                KpiCardDto.builder().title("Share Issuances").value("18").change("+20% vs last quarter").comparisonText("").trend("positive").icon("coins").color("orange").build(),
                KpiCardDto.builder().title("Share Transfers").value("76").change("+12% vs last quarter").comparisonText("").trend("positive").icon("layers").color("blue").build()
        );

        List<SubsidiaryTransactionsPageDto.CompanyTransactionItemDto> filtered = companyTransactionsList.stream()
                .filter(t -> {
                    if (query != null && !query.isBlank()) {
                        String q = query.toLowerCase();
                        if (!t.getTransactionId().toLowerCase().contains(q) &&
                            !t.getTransactionType().toLowerCase().contains(q) &&
                            !t.getCounterparty().toLowerCase().contains(q)) return false;
                    }
                    if (tab != null && !tab.isBlank() && !tab.startsWith("All")) {
                        if (tab.equalsIgnoreCase("Issuances") && !t.getTransactionType().equalsIgnoreCase("Share Issuance")) return false;
                        if (tab.equalsIgnoreCase("Transfers") && !t.getTransactionType().equalsIgnoreCase("Share Transfer")) return false;
                        if (tab.equalsIgnoreCase("Buybacks") && !t.getTransactionType().equalsIgnoreCase("Share Buyback")) return false;
                        if (tab.contains("Corporate") && !t.getTransactionType().equalsIgnoreCase("Corporate Action")) return false;
                    }
                    return true;
                }).collect(Collectors.toList());

        CompanyTransactionDetailDto selected = companyTransactionDetailsMap.getOrDefault("txn-1", companyTransactionDetailsMap.values().iterator().next());

        SubsidiaryTransactionsPageDto.TransactionsByTypeDonutDto byType = SubsidiaryTransactionsPageDto.TransactionsByTypeDonutDto.builder()
                .totalTransactions(156)
                .slices(List.of(
                        new SubsidiaryTransactionsPageDto.TransactionTypeSliceDto("Share Issuance", 18, 11.5, "#3b82f6"),
                        new SubsidiaryTransactionsPageDto.TransactionTypeSliceDto("Share Transfer", 76, 48.7, "#22c55e"),
                        new SubsidiaryTransactionsPageDto.TransactionTypeSliceDto("Share Buyback", 6, 3.8, "#ef4444"),
                        new SubsidiaryTransactionsPageDto.TransactionTypeSliceDto("Corporate Actions", 12, 7.7, "#a855f7"),
                        new SubsidiaryTransactionsPageDto.TransactionTypeSliceDto("Others", 44, 28.2, "#94a3b8")
                )).build();

        List<SubsidiaryTransactionsPageDto.MonthlyTransactionValueBarDto> monthlyVal = List.of(
                new SubsidiaryTransactionsPageDto.MonthlyTransactionValueBarDto("Apr", 12.5, "#3b82f6"),
                new SubsidiaryTransactionsPageDto.MonthlyTransactionValueBarDto("May", 18.2, "#3b82f6"),
                new SubsidiaryTransactionsPageDto.MonthlyTransactionValueBarDto("Jun", 22.1, "#3b82f6"),
                new SubsidiaryTransactionsPageDto.MonthlyTransactionValueBarDto("Jul", 16.8, "#3b82f6"),
                new SubsidiaryTransactionsPageDto.MonthlyTransactionValueBarDto("Aug", 25.0, "#3b82f6"),
                new SubsidiaryTransactionsPageDto.MonthlyTransactionValueBarDto("Sep", 31.2, "#3b82f6")
        );

        SubsidiaryTransactionsPageDto.TransactionStatusDonutDto statusDonut = SubsidiaryTransactionsPageDto.TransactionStatusDonutDto.builder()
                .totalTransactions(156)
                .slices(List.of(
                        new SubsidiaryTransactionsPageDto.TxnStatusSliceDto("Completed", 124, 79.5, "#22c55e"),
                        new SubsidiaryTransactionsPageDto.TxnStatusSliceDto("Under Review", 12, 7.7, "#f59e0b"),
                        new SubsidiaryTransactionsPageDto.TxnStatusSliceDto("Pending", 8, 5.1, "#3b82f6"),
                        new SubsidiaryTransactionsPageDto.TxnStatusSliceDto("Rejected", 9, 5.8, "#ef4444"),
                        new SubsidiaryTransactionsPageDto.TxnStatusSliceDto("Cancelled", 3, 1.9, "#94a3b8")
                )).build();

        return SubsidiaryTransactionsPageDto.builder()
                .adminUser(adminUser)
                .pageTitle("Company Transactions")
                .pageSubtitle("Track and manage all share-related and corporate transactions.")
                .selectedCompany(company != null ? company : "Atirath Holdings")
                .companyStatus("Active")
                .kpis(kpis)
                .tabs(List.of("All Transactions (156)", "Issuances (18)", "Transfers (76)", "Buybacks (6)", "Corporate Actions (12)", "Others (44)"))
                .activeTab(tab != null ? tab : "All Transactions (156)")
                .transactions(filtered)
                .totalTransactions(156)
                .currentPage(1)
                .totalPages(16)
                .selectedTransactionDetail(selected)
                .transactionsByType(byType)
                .monthlyTransactionValue(monthlyVal)
                .transactionStatus(statusDonut)
                .build();
    }

    public CompanyTransactionDetailDto getCompanyTransactionDetail(String id) {
        return companyTransactionDetailsMap.getOrDefault(id, companyTransactionDetailsMap.get("txn-1"));
    }

    public CompanyTransactionDetailDto recordCompanyTransaction(RecordCompanyTransactionRequestDto req) {
        int nextNum = transactionCounter.incrementAndGet();
        String id = "txn-" + nextNum;
        String txnId = "TXN-2025-" + String.format("%03d", nextNum);

        SubsidiaryTransactionsPageDto.CompanyTransactionItemDto item = SubsidiaryTransactionsPageDto.CompanyTransactionItemDto.builder()
                .id(id)
                .number(nextNum)
                .transactionId(txnId)
                .transactionType(req.getTransactionType() != null ? req.getTransactionType() : "Share Issuance")
                .transactionTypeTheme("green")
                .date("16 Sep 2026")
                .shares(req.getNumberOfShares() != null ? req.getNumberOfShares() : "1,00,000")
                .value(req.getTotalValue() != null ? req.getTotalValue() : "1,00,00,000")
                .pricePerShare(req.getPricePerShare() > 0 ? String.valueOf((int)req.getPricePerShare()) : "100")
                .counterparty(req.getCounterparty() != null ? req.getCounterparty() : "Strategic Investor")
                .status("Completed")
                .statusTheme("green")
                .build();

        companyTransactionsList.add(0, item);

        CompanyTransactionDetailDto detail = CompanyTransactionDetailDto.builder()
                .id(id)
                .transactionId(txnId)
                .transactionType(req.getTransactionType())
                .status("Completed")
                .statusTheme("green")
                .tabs(List.of("Details", "Documents", "Approvals", "History"))
                .activeTab("Details")
                .transactionDate(req.getTransactionDate() != null ? req.getTransactionDate() : "16 Sep 2026")
                .numberOfShares(req.getNumberOfShares())
                .pricePerShare("₹ " + (req.getPricePerShare() > 0 ? (int)req.getPricePerShare() : 100))
                .totalValue("₹ " + req.getTotalValue())
                .counterparty(req.getCounterparty())
                .paymentMode(req.getPaymentMode() != null ? req.getPaymentMode() : "Bank Transfer")
                .referenceNo(req.getReferenceNo() != null ? req.getReferenceNo() : "PAY/2026/" + nextNum)
                .approvedBy("Ramesh Kumar")
                .approvedOn("16 Sep 2026, 02:30 PM")
                .remarks(req.getRemarks() != null ? req.getRemarks() : "Standard transaction recorded successfully.")
                .build();

        companyTransactionDetailsMap.put(id, detail);
        return detail;
    }

    // ==========================================
    // 12. COMPANY REPORTS
    // ==========================================
    public SubsidiaryReportsPageDto getSubsidiaryReportsPage(String company, String tab, String dateRange) {
        List<KpiCardDto> kpis = List.of(
                KpiCardDto.builder().title("Total Subsidiaries").value("5").change("+25% vs last quarter").comparisonText("").trend("positive").icon("building").color("blue").build(),
                KpiCardDto.builder().title("Total Share Capital").value("₹ 500.00 Cr").change("+18% vs last quarter").comparisonText("").trend("positive").icon("rupee").color("green").build(),
                KpiCardDto.builder().title("Total Investments").value("₹ 320.00 Cr").change("+32% vs last quarter").comparisonText("").trend("positive").icon("trending-up").color("purple").build(),
                KpiCardDto.builder().title("Total Assets (Consolidated)").value("₹ 780.00 Cr").change("+28% vs last quarter").comparisonText("").trend("positive").icon("pie-chart").color("orange").build()
        );

        List<SubsidiaryReportsPageDto.SubsidiaryInvestmentBarDto> subInvestments = List.of(
                new SubsidiaryReportsPageDto.SubsidiaryInvestmentBarDto("Atirath Holdings", 200.0, "#3b82f6"),
                new SubsidiaryReportsPageDto.SubsidiaryInvestmentBarDto("Atirath Traders", 120.0, "#22c55e"),
                new SubsidiaryReportsPageDto.SubsidiaryInvestmentBarDto("Atirath Technologies", 80.0, "#f59e0b"),
                new SubsidiaryReportsPageDto.SubsidiaryInvestmentBarDto("Farm Vantara", 60.0, "#a855f7"),
                new SubsidiaryReportsPageDto.SubsidiaryInvestmentBarDto("Others", 40.0, "#06b6d4")
        );

        SubsidiaryReportsPageDto.ShareholdingDistributionDonutDto shareDist = SubsidiaryReportsPageDto.ShareholdingDistributionDonutDto.builder()
                .totalShareCapital("500 Cr Total Share Capital")
                .slices(List.of(
                        new SubsidiaryReportsPageDto.ShareholdingDistSliceDto("Promoters", 60.0, "#3b82f6"),
                        new SubsidiaryReportsPageDto.ShareholdingDistSliceDto("Strategic Investors", 20.0, "#22c55e"),
                        new SubsidiaryReportsPageDto.ShareholdingDistSliceDto("Financial Investors", 15.0, "#f59e0b"),
                        new SubsidiaryReportsPageDto.ShareholdingDistSliceDto("Others", 5.0, "#a855f7")
                )).build();

        List<SubsidiaryReportsPageDto.PerformancePointDto> performance = List.of(
                new SubsidiaryReportsPageDto.PerformancePointDto("Apr", 60.0, 40.0),
                new SubsidiaryReportsPageDto.PerformancePointDto("May", 75.0, 50.0),
                new SubsidiaryReportsPageDto.PerformancePointDto("Jun", 95.0, 65.0),
                new SubsidiaryReportsPageDto.PerformancePointDto("Jul", 120.0, 80.0),
                new SubsidiaryReportsPageDto.PerformancePointDto("Aug", 140.0, 95.0),
                new SubsidiaryReportsPageDto.PerformancePointDto("Sep", 165.0, 115.0)
        );

        List<String> popular = List.of(
                "Consolidated Financial Report",
                "Shareholding Report",
                "Subsidiary Performance Report",
                "Transaction Summary Report",
                "Project Progress Report"
        );

        return SubsidiaryReportsPageDto.builder()
                .adminUser(adminUser)
                .pageTitle("Company Reports")
                .pageSubtitle("View and generate comprehensive reports for Atirath Holdings.")
                .selectedCompany(company != null ? company : "Atirath Holdings")
                .dateRange(dateRange != null ? dateRange : "01 Apr 2026 - 30 Sep 2026")
                .kpis(kpis)
                .tabs(List.of("Overview", "Finance", "Shareholding", "Transactions", "Projects", "Plants", "Employees", "Audit & Compliance"))
                .activeTab(tab != null ? tab : "Overview")
                .subsidiaryWiseInvestment(subInvestments)
                .shareholdingDistribution(shareDist)
                .companyPerformance(performance)
                .subTabs(List.of("Available Reports", "Scheduled Reports"))
                .activeSubTab("Available Reports")
                .availableReports(availableReportsList)
                .totalReports(8)
                .popularReports(popular)
                .build();
    }

    public SubsidiaryReportsPageDto.ReportItemDto generateSubsidiaryReport(GenerateSubsidiaryReportRequestDto req) {
        int nextNum = reportCounter.incrementAndGet();
        String id = "rep-" + nextNum;

        SubsidiaryReportsPageDto.ReportItemDto item = SubsidiaryReportsPageDto.ReportItemDto.builder()
                .id(id)
                .number(nextNum)
                .reportName(req.getReportName())
                .category(req.getCategory() != null ? req.getCategory() : "Finance")
                .description("Custom generated report: " + req.getReportName())
                .lastGenerated("16 Sep 2026, 02:45 PM")
                .formats(List.of("PDF", "Excel"))
                .downloadPdfUrl("https://atirathgroup.com/reports/" + id + ".pdf")
                .downloadExcelUrl("https://atirathgroup.com/reports/" + id + ".xlsx")
                .build();

        availableReportsList.add(0, item);
        return item;
    }

    public Map<String, String> exportAllReports(String company) {
        return Map.of(
                "status", "READY",
                "fileName", "atirath_holdings_complete_reports_bundle_2026.zip",
                "downloadUrl", "https://atirathgroup.com/downloads/all-reports-bundle.zip"
        );
    }

    // ==========================================
    // SEEDING HELPERS 9 - 12
    // ==========================================
    private void seedCompanyApprovers() {
        companyApproversList.clear();

        companyApproversList.add(new SubsidiaryApproversPageDto.CompanyApproverItemDto("appr-1", 1, "Ramesh Kumar", "RB", "Managing Director", "Board", "All Types", "Unlimited", "Active", "green"));
        companyApproversList.add(new SubsidiaryApproversPageDto.CompanyApproverItemDto("appr-2", 2, "Priya Verma", "PV", "Director - Finance", "Finance", "Share Issuance, Buyback", "50,00,00,000", "Active", "green"));
        companyApproversList.add(new SubsidiaryApproversPageDto.CompanyApproverItemDto("appr-3", 3, "Amit Singh", "AS", "Director - Legal", "Legal", "Share Transfer, Compliance", "25,00,00,000", "Active", "green"));
        companyApproversList.add(new SubsidiaryApproversPageDto.CompanyApproverItemDto("appr-4", 4, "Meera Reddy", "MR", "Chief Operating Officer", "Operations", "ESOP, Employee Transfer", "10,00,00,000", "Active", "green"));
        companyApproversList.add(new SubsidiaryApproversPageDto.CompanyApproverItemDto("appr-5", 5, "David Brown", "DB", "Independent Director", "Board", "Strategic Issuance, M&A", "Unlimited", "Active", "green"));
        companyApproversList.add(new SubsidiaryApproversPageDto.CompanyApproverItemDto("appr-6", 6, "Suresh K", "SK", "Company Secretary", "Compliance", "All Types (Compliance)", "5,00,00,000", "Active", "green"));
        companyApproversList.add(new SubsidiaryApproversPageDto.CompanyApproverItemDto("appr-7", 7, "Neha Patel", "NP", "VP - Investments", "Strategy", "Series A, B Issuance", "20,00,00,000", "Inactive", "red"));
        companyApproversList.add(new SubsidiaryApproversPageDto.CompanyApproverItemDto("appr-8", 8, "Lokesh Kumar", "LK", "Finance Manager", "Finance", "Routine Transfers", "1,00,00,000", "Inactive", "red"));

        CompanyApproverDetailDto appr1 = CompanyApproverDetailDto.builder()
                .id("appr-1")
                .approverName("Ramesh Kumar")
                .initials("RB")
                .designation("Managing Director")
                .department("Board")
                .status("Active")
                .statusTheme("green")
                .tabs(List.of("Details", "Approval Limits", "Approval Types", "History"))
                .activeTab("Details")
                .fullName("Ramesh Kumar")
                .email("ramesh.kumar@atirathholdings.com")
                .phone("+91 98765 43210")
                .approvalLimit("Unlimited")
                .approvalTypes("All Types")
                .effectiveFrom("01 Jan 2025")
                .createdBy("Admin User")
                .createdOn("10 Dec 2024, 11:30 AM")
                .lastUpdated("15 Sep 2026, 04:20 PM")
                .remarks("Primary approver for all major corporate actions.")
                .build();

        companyApproverDetailsMap.put("appr-1", appr1);
    }

    private void seedCompanyDocuments() {
        companyDocumentsList.clear();

        companyDocumentsList.add(new SubsidiaryDocumentsPageDto.CompanyDocumentItemDto("doc-1", 1, "Certificate of Incorporation", "Statutory", "purple", "Incorporation", "PDF", "15 Jan 2024", "-", "Verified", "green"));
        companyDocumentsList.add(new SubsidiaryDocumentsPageDto.CompanyDocumentItemDto("doc-2", 2, "Memorandum of Association", "Statutory", "purple", "Governing Document", "PDF", "15 Jan 2024", "-", "Verified", "green"));
        companyDocumentsList.add(new SubsidiaryDocumentsPageDto.CompanyDocumentItemDto("doc-3", 3, "Articles of Association", "Statutory", "purple", "Governing Document", "PDF", "15 Jan 2024", "-", "Verified", "green"));
        companyDocumentsList.add(new SubsidiaryDocumentsPageDto.CompanyDocumentItemDto("doc-4", 4, "PAN Card", "Statutory", "purple", "Tax Document", "PDF", "16 Jan 2024", "-", "Verified", "green"));
        companyDocumentsList.add(new SubsidiaryDocumentsPageDto.CompanyDocumentItemDto("doc-5", 5, "GST Registration Certificate", "Statutory", "purple", "Tax Document", "PDF", "16 Jan 2024", "30 Jun 2027", "Verified", "green"));
        companyDocumentsList.add(new SubsidiaryDocumentsPageDto.CompanyDocumentItemDto("doc-6", 6, "Board Resolution - ES Issue", "Corporate", "blue", "Board Resolution", "PDF", "05 Mar 2025", "-", "Active", "green"));
        companyDocumentsList.add(new SubsidiaryDocumentsPageDto.CompanyDocumentItemDto("doc-7", 7, "Shareholder Agreement", "Corporate", "blue", "Agreement", "DOCX", "10 Feb 2025", "-", "Active", "green"));
        companyDocumentsList.add(new SubsidiaryDocumentsPageDto.CompanyDocumentItemDto("doc-8", 8, "Latest Audited Financials (FY 24-25)", "Financial", "green", "Financial Statement", "PDF", "30 Sep 2025", "-", "Verified", "green"));
        companyDocumentsList.add(new SubsidiaryDocumentsPageDto.CompanyDocumentItemDto("doc-9", 9, "ISO 9001 Certificate", "Regulatory", "orange", "Certification", "PDF", "01 Mar 2024", "28 Feb 2026", "Expiring Soon", "orange"));
        companyDocumentsList.add(new SubsidiaryDocumentsPageDto.CompanyDocumentItemDto("doc-10", 10, "Environmental Compliance", "Regulatory", "orange", "Compliance", "PDF", "12 Jun 2024", "12 Jun 2026", "Active", "green"));

        CompanyDocumentDetailDto doc1 = CompanyDocumentDetailDto.builder()
                .id("doc-1")
                .documentName("Certificate of Incorporation")
                .fileType("PDF")
                .status("Verified")
                .statusTheme("green")
                .categoryBadge("Statutory")
                .documentTypeBadge("Incorporation")
                .tabs(List.of("Details", "Version History", "Access Control", "Activity"))
                .activeTab("Details")
                .category("Statutory")
                .documentType("Incorporation")
                .fileName("COI_Atirath_Holdings.pdf")
                .fileSize("1.2 MB")
                .uploadedOn("15 Jan 2024, 10:30 AM")
                .uploadedBy("Admin User")
                .expiryDate("-")
                .documentNumber("CIN: U40100TG2024PLC123456")
                .issuedBy("Ministry of Corporate Affairs")
                .description("Certificate of Incorporation for Atirath Holdings India Limited.")
                .downloadUrl("https://atirathgroup.com/docs/coi.pdf")
                .build();

        companyDocumentDetailsMap.put("doc-1", doc1);
    }

    private void seedCompanyTransactions() {
        companyTransactionsList.clear();

        companyTransactionsList.add(new SubsidiaryTransactionsPageDto.CompanyTransactionItemDto("txn-1", 1, "TXN-2025-001", "Share Issuance", "green", "10 Sep 2026", "2,50,000", "25,00,00,000", "100", "Sunrise Ventures", "Completed", "green"));
        companyTransactionsList.add(new SubsidiaryTransactionsPageDto.CompanyTransactionItemDto("txn-2", 2, "TXN-2025-002", "Share Transfer", "blue", "05 Sep 2026", "50,000", "50,00,000", "100", "Amit Singh", "Completed", "green"));
        companyTransactionsList.add(new SubsidiaryTransactionsPageDto.CompanyTransactionItemDto("txn-3", 3, "TXN-2025-003", "Share Transfer", "blue", "01 Sep 2026", "1,00,000", "1,00,00,000", "100", "Nexus Capital", "Under Review", "orange"));
        companyTransactionsList.add(new SubsidiaryTransactionsPageDto.CompanyTransactionItemDto("txn-4", 4, "TXN-2025-004", "Share Buyback", "red", "25 Aug 2026", "30,000", "30,00,000", "100", "David Brown", "Completed", "green"));
        companyTransactionsList.add(new SubsidiaryTransactionsPageDto.CompanyTransactionItemDto("txn-5", 5, "TXN-2025-005", "Corporate Action", "purple", "20 Aug 2026", "-", "-", "-", "Stock Split (1:2)", "Completed", "green"));
        companyTransactionsList.add(new SubsidiaryTransactionsPageDto.CompanyTransactionItemDto("txn-6", 6, "TXN-2025-006", "Share Transfer", "blue", "18 Aug 2026", "75,000", "75,00,000", "100", "Meera Reddy", "Completed", "green"));
        companyTransactionsList.add(new SubsidiaryTransactionsPageDto.CompanyTransactionItemDto("txn-7", 7, "TXN-2025-007", "Share Issuance", "green", "12 Aug 2026", "5,00,00,000", "50,00,00,000", "100", "Strategic Investors", "Completed", "green"));
        companyTransactionsList.add(new SubsidiaryTransactionsPageDto.CompanyTransactionItemDto("txn-8", 8, "TXN-2025-008", "Share Transfer", "blue", "05 Aug 2026", "20,000", "20,00,000", "100", "Ramesh Kumar", "Rejected", "red"));
        companyTransactionsList.add(new SubsidiaryTransactionsPageDto.CompanyTransactionItemDto("txn-9", 9, "TXN-2025-009", "Share Issuance", "green", "28 Jul 2026", "1,00,000", "10,00,00,000", "100", "Global Energy Partners", "Completed", "green"));
        companyTransactionsList.add(new SubsidiaryTransactionsPageDto.CompanyTransactionItemDto("txn-10", 10, "TXN-2025-010", "Others", "gray", "15 Jul 2026", "-", "5,00,000", "-", "Registration Fee", "Completed", "green"));

        CompanyTransactionDetailDto txn1 = CompanyTransactionDetailDto.builder()
                .id("txn-1")
                .transactionId("TXN-2025-001")
                .transactionType("Share Issuance")
                .status("Completed")
                .statusTheme("green")
                .tabs(List.of("Details", "Documents (3)", "Approvals (4)", "History"))
                .activeTab("Details")
                .transactionDate("10 Sep 2026")
                .numberOfShares("2,50,000")
                .pricePerShare("₹ 100")
                .totalValue("₹ 25,00,00,000")
                .counterparty("Sunrise Ventures")
                .paymentMode("Bank Transfer")
                .referenceNo("PAY/2026/0910/001")
                .approvedBy("Ramesh Kumar")
                .approvedOn("10 Sep 2026, 04:20 PM")
                .remarks("Issuance of equity shares to Sunrise Ventures as per board resolution dated 05 Sep 2026.")
                .build();

        companyTransactionDetailsMap.put("txn-1", txn1);
    }

    private void seedCompanyReports() {
        availableReportsList.clear();

        availableReportsList.add(new SubsidiaryReportsPageDto.ReportItemDto("rep-1", 1, "Consolidated Financial Report", "Finance", "P&L, Balance Sheet, Cash Flow (Consolidated)", "25 Sep 2026, 10:30 AM", List.of("PDF", "Excel"), "https://atirathgroup.com/rep1.pdf", "https://atirathgroup.com/rep1.xlsx"));
        availableReportsList.add(new SubsidiaryReportsPageDto.ReportItemDto("rep-2", 2, "Subsidiary Performance Report", "Finance", "Financial performance of all subsidiaries", "22 Sep 2026, 04:15 PM", List.of("PDF", "Excel"), "https://atirathgroup.com/rep2.pdf", "https://atirathgroup.com/rep2.xlsx"));
        availableReportsList.add(new SubsidiaryReportsPageDto.ReportItemDto("rep-3", 3, "Shareholding Report", "Shareholding", "Current shareholding structure", "20 Sep 2026, 11:20 AM", List.of("PDF", "Excel"), "https://atirathgroup.com/rep3.pdf", "https://atirathgroup.com/rep3.xlsx"));
        availableReportsList.add(new SubsidiaryReportsPageDto.ReportItemDto("rep-4", 4, "Capital Raising Report", "Transactions", "Share issuance and fund raising details", "18 Sep 2026, 03:45 PM", List.of("PDF", "Excel"), "https://atirathgroup.com/rep4.pdf", "https://atirathgroup.com/rep4.xlsx"));
        availableReportsList.add(new SubsidiaryReportsPageDto.ReportItemDto("rep-5", 5, "Transaction Summary Report", "Transactions", "Issuances, transfers, buybacks, corporate actions", "15 Sep 2026, 12:10 PM", List.of("PDF", "Excel"), "https://atirathgroup.com/rep5.pdf", "https://atirathgroup.com/rep5.xlsx"));
        availableReportsList.add(new SubsidiaryReportsPageDto.ReportItemDto("rep-6", 6, "Project Progress Report", "Projects", "Project-wise status and milestones", "12 Sep 2026, 09:30 AM", List.of("PDF", "Excel"), "https://atirathgroup.com/rep6.pdf", "https://atirathgroup.com/rep6.xlsx"));
        availableReportsList.add(new SubsidiaryReportsPageDto.ReportItemDto("rep-7", 7, "Plant Operations Report", "Plants", "Operational status and production details", "10 Sep 2026, 05:20 PM", List.of("PDF", "Excel"), "https://atirathgroup.com/rep7.pdf", "https://atirathgroup.com/rep7.xlsx"));
        availableReportsList.add(new SubsidiaryReportsPageDto.ReportItemDto("rep-8", 8, "Employee & Department Report", "HR", "Employee strength and department-wise details", "08 Sep 2026, 11:00 AM", List.of("PDF", "Excel"), "https://atirathgroup.com/rep8.pdf", "https://atirathgroup.com/rep8.xlsx"));
    }
}


