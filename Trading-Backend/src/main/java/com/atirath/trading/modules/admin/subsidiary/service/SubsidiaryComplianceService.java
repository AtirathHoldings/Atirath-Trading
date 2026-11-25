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
public class SubsidiaryComplianceService {

    private AdminUserHeaderDto adminUser;

    // 1. KYC Store
    private final List<SubsidiaryKycReviewPageDto.KycRequestItemDto> kycRequestsList = new CopyOnWriteArrayList<>();
    private final Map<String, KycDetailDto> kycDetailsMap = new ConcurrentHashMap<>();
    private final AtomicInteger kycCounter = new AtomicInteger(12);

    // 2. Compliance Alerts Store
    private final List<SubsidiaryComplianceAlertsPageDto.ComplianceAlertItemDto> complianceAlertsList = new CopyOnWriteArrayList<>();
    private final Map<String, ComplianceAlertDetailDto> alertDetailsMap = new ConcurrentHashMap<>();
    private final AtomicInteger alertCounter = new AtomicInteger(10);

    // 3. Transaction Monitoring Store
    private final List<SubsidiaryTransactionMonitoringPageDto.MonitoredTransactionItemDto> monitoredTransactionsList = new CopyOnWriteArrayList<>();
    private final Map<String, MonitoredTransactionDetailDto> monitoredTransactionDetailsMap = new ConcurrentHashMap<>();
    private final AtomicInteger monitoredTxnCounter = new AtomicInteger(10);

    // 4. Compliance Dashboard Bottom Lists
    private final List<SubsidiaryComplianceDashboardDto.RecentComplianceAlertDto> recentDashboardAlerts = new CopyOnWriteArrayList<>();
    private final List<SubsidiaryComplianceDashboardDto.RegulatoryFilingDueDto> regulatoryFilingsList = new CopyOnWriteArrayList<>();
    private final List<SubsidiaryComplianceDashboardDto.OpenComplianceCaseDto> openCasesList = new CopyOnWriteArrayList<>();
    private final List<SubsidiaryComplianceDashboardDto.PendingApprovalDto> pendingApprovalsList = new CopyOnWriteArrayList<>();

    // 5. Suspicious Transactions Store
    private final List<SuspiciousTransactionsPageDto.SuspiciousTransactionItemDto> suspiciousTransactionsList = new CopyOnWriteArrayList<>();
    private final Map<String, SuspiciousTransactionDetailDto> suspiciousTransactionDetailsMap = new ConcurrentHashMap<>();
    private final AtomicInteger suspiciousTxnCounter = new AtomicInteger(10);

    // 6. Restricted Users Store
    private final List<RestrictedUsersPageDto.RestrictedUserItemDto> restrictedUsersList = new CopyOnWriteArrayList<>();
    private final Map<String, RestrictedUserDetailDto> restrictedUserDetailsMap = new ConcurrentHashMap<>();
    private final AtomicInteger restrictedUserCounter = new AtomicInteger(10);

    // 7. Restricted Shares Store
    private final List<RestrictedSharesPageDto.RestrictedShareItemDto> restrictedSharesList = new CopyOnWriteArrayList<>();
    private final Map<String, RestrictedShareDetailDto> restrictedShareDetailsMap = new ConcurrentHashMap<>();
    private final AtomicInteger restrictedShareCounter = new AtomicInteger(10);

    // 8. Compliance Cases Store
    private final List<ComplianceCasesPageDto.ComplianceCaseItemDto> complianceCasesList = new CopyOnWriteArrayList<>();
    private final Map<String, ComplianceCaseDetailDto> complianceCaseDetailsMap = new ConcurrentHashMap<>();
    private final AtomicInteger complianceCaseCounter = new AtomicInteger(10);

    @PostConstruct
    public void init() {
        log.info("[SubsidiaryComplianceService] Initializing Compliance datasets matching UI screenshots...");

        adminUser = AdminUserHeaderDto.builder()
                .name("Admin User")
                .initials("AD")
                .role("Subsidiary Administration")
                .currentDate("01 Apr 2026 - 30 Sep 2026")
                .unreadNotifications(3)
                .build();

        seedComplianceDashboard();
        seedKycReview();
        seedComplianceAlerts();
        seedTransactionMonitoring();
        seedSuspiciousTransactions();
        seedRestrictedUsers();
        seedRestrictedShares();
        seedComplianceCases();

        log.info("[SubsidiaryComplianceService] Initialization completed. Ready for Compliance requests.");
    }

    // ==========================================
    // 1. COMPLIANCE DASHBOARD
    // ==========================================
    public SubsidiaryComplianceDashboardDto getComplianceDashboard(String company, String dateRange) {
        List<KpiCardDto> kpis = List.of(
                KpiCardDto.builder().title("Total Entities").value("5").change("+25% vs last quarter").comparisonText("").trend("positive").icon("shield").color("blue").build(),
                KpiCardDto.builder().title("KYC Verified").value("342").change("+18% vs last quarter").comparisonText("").trend("positive").icon("users").color("green").build(),
                KpiCardDto.builder().title("Open Compliance Cases").value("12").change("+50% vs last quarter").comparisonText("").trend("negative").icon("alert-triangle").color("red").build(),
                KpiCardDto.builder().title("Regulatory Filings Due").value("5").change("+25% vs last quarter").comparisonText("").trend("positive").icon("file-text").color("blue").build()
        );

        List<SubsidiaryComplianceDashboardDto.ComplianceStatusBySubsidiaryDto> subStatus = List.of(
                new SubsidiaryComplianceDashboardDto.ComplianceStatusBySubsidiaryDto("Atirath Holdings", 100, 50, 25, 25),
                new SubsidiaryComplianceDashboardDto.ComplianceStatusBySubsidiaryDto("Atirath Traders", 80, 50, 20, 10),
                new SubsidiaryComplianceDashboardDto.ComplianceStatusBySubsidiaryDto("Atirath Technologies", 60, 35, 15, 10),
                new SubsidiaryComplianceDashboardDto.ComplianceStatusBySubsidiaryDto("Farm Vantara", 40, 25, 10, 5),
                new SubsidiaryComplianceDashboardDto.ComplianceStatusBySubsidiaryDto("Others", 20, 10, 5, 5)
        );

        SubsidiaryComplianceDashboardDto.KycStatusDonutDto kycDonut = SubsidiaryComplianceDashboardDto.KycStatusDonutDto.builder()
                .totalInvestors(342)
                .slices(List.of(
                        new SubsidiaryComplianceDashboardDto.KycStatusSliceDto("Verified", 76.3, 261, "#22c55e"),
                        new SubsidiaryComplianceDashboardDto.KycStatusSliceDto("Under Review", 15.5, 53, "#f97316"),
                        new SubsidiaryComplianceDashboardDto.KycStatusSliceDto("Pending", 6.1, 21, "#f59e0b"),
                        new SubsidiaryComplianceDashboardDto.KycStatusSliceDto("Rejected", 2.1, 7, "#ef4444")
                )).build();

        SubsidiaryComplianceDashboardDto.ComplianceCasesDonutDto casesDonut = SubsidiaryComplianceDashboardDto.ComplianceCasesDonutDto.builder()
                .openCases(12)
                .slices(List.of(
                        new SubsidiaryComplianceDashboardDto.ComplianceCaseSliceDto("KYC Issues", 33.3, 4, "#3b82f6"),
                        new SubsidiaryComplianceDashboardDto.ComplianceCaseSliceDto("Transaction Alerts", 25.0, 3, "#f59e0b"),
                        new SubsidiaryComplianceDashboardDto.ComplianceCaseSliceDto("Restricted User", 16.7, 2, "#f97316"),
                        new SubsidiaryComplianceDashboardDto.ComplianceCaseSliceDto("Share Transfer", 16.7, 2, "#a855f7"),
                        new SubsidiaryComplianceDashboardDto.ComplianceCaseSliceDto("Others", 8.3, 1, "#06b6d4")
                )).build();

        return SubsidiaryComplianceDashboardDto.builder()
                .adminUser(adminUser)
                .pageTitle("Compliance Dashboard")
                .pageSubtitle("Monitor regulatory compliance, KYC status, transactions and risk alerts across all subsidiaries.")
                .selectedCompany(company != null ? company : "Atirath Holdings")
                .dateRange(dateRange != null ? dateRange : "01 Apr 2026 - 30 Sep 2026")
                .kpis(kpis)
                .complianceStatusBySubsidiary(subStatus)
                .kycStatus(kycDonut)
                .complianceCases(casesDonut)
                .recentComplianceAlerts(recentDashboardAlerts)
                .regulatoryFilingsDue(regulatoryFilingsList)
                .openComplianceCases(openCasesList)
                .pendingApprovals(pendingApprovalsList)
                .build();
    }

    // ==========================================
    // 2. KYC REVIEW
    // ==========================================
    public SubsidiaryKycReviewPageDto getKycReviewPage(String company, String tab, String query, String entityType, String status, String riskLevel) {
        List<KpiCardDto> kpis = List.of(
                KpiCardDto.builder().title("Total KYC Requests").value("342").change("+18% vs last quarter").comparisonText("").trend("positive").icon("file-text").color("blue").build(),
                KpiCardDto.builder().title("Verified").value("261").change("76.3%").comparisonText("").trend("positive").icon("check-circle").color("green").build(),
                KpiCardDto.builder().title("Under Review").value("53").change("15.5%").comparisonText("").trend("neutral").icon("clock").color("orange").build(),
                KpiCardDto.builder().title("Pending").value("21").change("6.1%").comparisonText("").trend("neutral").icon("hourglass").color("blue").build(),
                KpiCardDto.builder().title("Rejected").value("7").change("2.1%").comparisonText("").trend("negative").icon("x-circle").color("red").build()
        );

        List<SubsidiaryKycReviewPageDto.KycRequestItemDto> filtered = kycRequestsList.stream()
                .filter(k -> {
                    if (query != null && !query.isBlank()) {
                        String q = query.toLowerCase();
                        if (!k.getNameOrEntity().toLowerCase().contains(q) &&
                            !k.getPanOrCin().toLowerCase().contains(q) &&
                            !k.getRequestId().toLowerCase().contains(q)) return false;
                    }
                    if (tab != null && !tab.isBlank() && !tab.equalsIgnoreCase("All Requests")) {
                        if (tab.equalsIgnoreCase("Individual Investors") && !k.getEntityType().equalsIgnoreCase("Individual Investor")) return false;
                        if (tab.equalsIgnoreCase("Corporate Investors") && !k.getEntityType().equalsIgnoreCase("Corporate Investor")) return false;
                        if (tab.contains("Director") && !k.getEntityType().contains("Director")) return false;
                        if (tab.equalsIgnoreCase("Employees") && !k.getEntityType().equalsIgnoreCase("Employee")) return false;
                    }
                    if (entityType != null && !entityType.isBlank() && !entityType.equalsIgnoreCase("All Entity Types")) {
                        if (!k.getEntityType().equalsIgnoreCase(entityType)) return false;
                    }
                    if (status != null && !status.isBlank() && !status.equalsIgnoreCase("All Status")) {
                        if (!k.getStatus().equalsIgnoreCase(status)) return false;
                    }
                    if (riskLevel != null && !riskLevel.isBlank() && !riskLevel.equalsIgnoreCase("All Risk Levels")) {
                        if (!k.getRiskLevel().equalsIgnoreCase(riskLevel)) return false;
                    }
                    return true;
                }).collect(Collectors.toList());

        KycDetailDto selected = kycDetailsMap.getOrDefault("kyc-1", kycDetailsMap.values().iterator().next());

        return SubsidiaryKycReviewPageDto.builder()
                .adminUser(adminUser)
                .pageTitle("KYC Review")
                .pageSubtitle("Review and verify KYC documents for investors, shareholders and key stakeholders.")
                .selectedCompany(company != null ? company : "Atirath Holdings")
                .dateRange("01 Apr 2026 - 30 Sep 2026")
                .kpis(kpis)
                .tabs(List.of("All Requests", "Individual Investors", "Corporate Investors", "Directors / KMP", "Employees", "Others"))
                .activeTab(tab != null ? tab : "All Requests")
                .entityTypes(List.of("All Entity Types", "Individual Investor", "Corporate Investor", "Director / KMP", "Employee"))
                .statuses(List.of("All Status", "Verified", "Under Review", "Pending", "Rejected"))
                .riskLevels(List.of("All Risk Levels", "Low", "Medium", "High"))
                .requests(filtered)
                .totalRequests(342)
                .currentPage(1)
                .totalPages(29)
                .pageSize(12)
                .selectedKycDetail(selected)
                .build();
    }

    public KycDetailDto getKycDetail(String id) {
        return kycDetailsMap.getOrDefault(id, kycDetailsMap.get("kyc-1"));
    }

    public KycDetailDto createKycRequest(CreateKycRequestDto req) {
        int nextNum = kycCounter.incrementAndGet();
        String id = "kyc-" + nextNum;
        String reqId = "KYC-2026-" + String.format("%03d", nextNum);
        String initials = req.getFullName().length() >= 2 ? req.getFullName().substring(0, 2).toUpperCase() : "KY";

        String rLevel = req.getRiskLevel() != null ? req.getRiskLevel() : "Low";
        String rTheme = rLevel.equalsIgnoreCase("High") ? "red" : (rLevel.equalsIgnoreCase("Medium") ? "orange" : "green");

        SubsidiaryKycReviewPageDto.KycRequestItemDto item = SubsidiaryKycReviewPageDto.KycRequestItemDto.builder()
                .id(id)
                .number(nextNum)
                .requestId(reqId)
                .nameOrEntity(req.getFullName())
                .entityType(req.getEntityType() != null ? req.getEntityType() : "Individual Investor")
                .panOrCin(req.getPanOrCin() != null ? req.getPanOrCin() : "AAZPK" + nextNum + "F")
                .requestDate("29 Sep 2026")
                .status("Pending")
                .statusTheme("blue")
                .riskLevel(rLevel)
                .riskTheme(rTheme)
                .build();

        kycRequestsList.add(0, item);

        KycDetailDto detail = KycDetailDto.builder()
                .id(id)
                .requestId(reqId)
                .requestDate("29 Sep 2026")
                .fullName(req.getFullName())
                .initials(initials)
                .entityType(req.getEntityType() != null ? req.getEntityType() : "Individual Investor")
                .status("Pending")
                .statusTheme("blue")
                .tabs(List.of("Details", "Documents", "Verification", "Remarks", "History"))
                .activeTab("Details")
                .dateOfBirth(req.getDateOfBirth() != null ? req.getDateOfBirth() : "15 Aug 1990")
                .email(req.getEmail() != null ? req.getEmail() : "investor" + nextNum + "@example.com")
                .phone(req.getPhone() != null ? req.getPhone() : "+91 98765 00000")
                .address(req.getAddress() != null ? req.getAddress() : "Jubilee Hills, Hyderabad, Telangana - 500033")
                .pan(req.getPanOrCin() != null ? req.getPanOrCin() : "AAZPK" + nextNum + "F")
                .aadhaar(req.getAadhaar() != null ? req.getAadhaar() : "1234 5678 9012")
                .nationality("Indian")
                .occupation(req.getOccupation() != null ? req.getOccupation() : "Professional")
                .sourceOfFunds(req.getSourceOfFunds() != null ? req.getSourceOfFunds() : "Investment Income")
                .riskLevel(rLevel)
                .riskTheme(rTheme)
                .pepStatus("No")
                .sanctionsCheck("Clear")
                .adverseMedia("No issues found")
                .documents(List.of(
                        new KycDetailDto.KycDocumentDto("PAN Card", req.getPanOrCin(), "PAN_Front.pdf", "Verified", "29 Sep 2026"),
                        new KycDetailDto.KycDocumentDto("Aadhaar Card", "XXXX-XXXX-9012", "Aadhaar.pdf", "Verified", "29 Sep 2026")
                ))
                .build();

        kycDetailsMap.put(id, detail);
        return detail;
    }

    public KycDetailDto updateKycStatus(String id, String newStatus) {
        KycDetailDto detail = getKycDetail(id);
        String theme = newStatus.equalsIgnoreCase("Verified") ? "green" :
                (newStatus.equalsIgnoreCase("Rejected") ? "red" :
                (newStatus.equalsIgnoreCase("Under Review") ? "orange" : "blue"));

        detail.setStatus(newStatus);
        detail.setStatusTheme(theme);

        kycRequestsList.stream().filter(k -> k.getId().equalsIgnoreCase(id)).findFirst().ifPresent(k -> {
            k.setStatus(newStatus);
            k.setStatusTheme(theme);
        });

        return detail;
    }

    // ==========================================
    // 3. COMPLIANCE ALERTS
    // ==========================================
    public SubsidiaryComplianceAlertsPageDto getComplianceAlertsPage(String company, String tab, String query, String riskLevel, String status) {
        List<KpiCardDto> kpis = List.of(
                KpiCardDto.builder().title("Total Alerts").value("36").change("+28% vs last quarter").comparisonText("").trend("positive").icon("alert-circle").color("red").build(),
                KpiCardDto.builder().title("High Risk Alerts").value("8").change("+60% vs last quarter").comparisonText("").trend("negative").icon("alert-triangle").color("red").build(),
                KpiCardDto.builder().title("Medium Risk Alerts").value("14").change("-12% vs last quarter").comparisonText("").trend("positive").icon("alert-octagon").color("orange").build(),
                KpiCardDto.builder().title("Low Risk Alerts").value("14").change("+5% vs last quarter").comparisonText("").trend("positive").icon("info").color("blue").build()
        );

        List<SubsidiaryComplianceAlertsPageDto.ComplianceAlertItemDto> filtered = complianceAlertsList.stream()
                .filter(a -> {
                    if (query != null && !query.isBlank()) {
                        String q = query.toLowerCase();
                        if (!a.getAlertId().toLowerCase().contains(q) &&
                            !a.getEntity().toLowerCase().contains(q) &&
                            !a.getDescription().toLowerCase().contains(q)) return false;
                    }
                    if (tab != null && !tab.isBlank() && !tab.equalsIgnoreCase("All Alerts")) {
                        if (tab.equalsIgnoreCase("Open Alerts") && !a.getStatus().equalsIgnoreCase("Open")) return false;
                        if (tab.equalsIgnoreCase("In Progress") && !a.getStatus().equalsIgnoreCase("In Progress") && !a.getStatus().equalsIgnoreCase("Investigating")) return false;
                        if (tab.equalsIgnoreCase("Resolved") && !a.getStatus().equalsIgnoreCase("Resolved")) return false;
                        if (tab.equalsIgnoreCase("Dismissed") && !a.getStatus().equalsIgnoreCase("Dismissed")) return false;
                    }
                    if (riskLevel != null && !riskLevel.isBlank() && !riskLevel.equalsIgnoreCase("All Risk Levels")) {
                        if (!a.getRiskLevel().equalsIgnoreCase(riskLevel)) return false;
                    }
                    if (status != null && !status.isBlank() && !status.equalsIgnoreCase("All Status")) {
                        if (!a.getStatus().equalsIgnoreCase(status)) return false;
                    }
                    return true;
                }).collect(Collectors.toList());

        ComplianceAlertDetailDto selected = alertDetailsMap.getOrDefault("al-1", alertDetailsMap.values().iterator().next());

        return SubsidiaryComplianceAlertsPageDto.builder()
                .adminUser(adminUser)
                .pageTitle("Compliance Alerts")
                .pageSubtitle("Monitor and manage compliance alerts across all subsidiaries.")
                .selectedCompany(company != null ? company : "Atirath Holdings")
                .dateRange("01 Apr 2026 - 30 Sep 2026")
                .kpis(kpis)
                .tabs(List.of("All Alerts", "Open Alerts", "In Progress", "Resolved", "Dismissed"))
                .activeTab(tab != null ? tab : "All Alerts")
                .entities(List.of("All Entities", "Atirath Holdings", "Atirath Traders", "Atirath Technologies", "Farm Vantara", "Sunrise Ventures", "Green Future Fund", "Others"))
                .alertTypes(List.of("All Alert Types", "Large Share Transfer", "Investor KYC Expiry", "Restricted User Activity", "Suspicious Transaction", "Document Missing", "PEP Match Found", "Repeated Failed Login", "Sanctions Screening"))
                .riskLevels(List.of("All Risk Levels", "High", "Medium", "Low"))
                .statuses(List.of("All Status", "Open", "In Progress", "Investigating", "Resolved", "Dismissed"))
                .alerts(filtered)
                .totalAlerts(36)
                .currentPage(1)
                .totalPages(4)
                .pageSize(10)
                .selectedAlertDetail(selected)
                .build();
    }

    public ComplianceAlertDetailDto getComplianceAlertDetail(String id) {
        return alertDetailsMap.getOrDefault(id, alertDetailsMap.get("al-1"));
    }

    public ComplianceAlertDetailDto createComplianceAlert(CreateComplianceAlertRequestDto req) {
        int nextNum = alertCounter.incrementAndGet();
        String id = "al-" + nextNum;
        String alertId = "AL-2026-" + String.format("%03d", nextNum);

        String rLevel = req.getRiskLevel() != null ? req.getRiskLevel() : "Medium";
        String rTheme = rLevel.equalsIgnoreCase("High") ? "red" : (rLevel.equalsIgnoreCase("Low") ? "green" : "orange");

        SubsidiaryComplianceAlertsPageDto.ComplianceAlertItemDto item = SubsidiaryComplianceAlertsPageDto.ComplianceAlertItemDto.builder()
                .id(id)
                .number(nextNum)
                .alertId(alertId)
                .dateTime("29 Sep 2026, 02:30 PM")
                .entity(req.getEntity() != null ? req.getEntity() : "Atirath Traders")
                .alertType(req.getAlertType() != null ? req.getAlertType() : "Suspicious Transaction")
                .riskLevel(rLevel)
                .riskTheme(rTheme)
                .status("Open")
                .statusTheme("red")
                .description(req.getDescription() != null ? req.getDescription() : "Automated rule violation detected.")
                .build();

        complianceAlertsList.add(0, item);

        ComplianceAlertDetailDto detail = ComplianceAlertDetailDto.builder()
                .id(id)
                .alertId(alertId)
                .alertType(req.getAlertType() != null ? req.getAlertType() : "Suspicious Transaction")
                .status("Open")
                .statusTheme("red")
                .detectedOn("29 Sep 2026, 02:30 PM")
                .tabs(List.of("Details", "Investors", "Related Transactions", "Activity Log"))
                .activeTab("Details")
                .riskLevel(rLevel)
                .riskTheme(rTheme)
                .entity(req.getEntity() != null ? req.getEntity() : "Atirath Traders")
                .detectionSource(req.getDetectionSource() != null ? req.getDetectionSource() : "Transaction Monitoring")
                .description(req.getDescription() != null ? req.getDescription() : "Unusual high-frequency share transfer detected.")
                .investor(req.getInvestor() != null ? req.getInvestor() : "Rakesh Mehta")
                .fromParty(req.getFromParty() != null ? req.getFromParty() : "Sunrise Ventures Pvt Ltd")
                .toParty(req.getToParty() != null ? req.getToParty() : "Global Agri Fund")
                .shares("50,000")
                .amount(req.getAmount() != null ? req.getAmount() : "₹ 50,00,000")
                .transactionDate("29 Sep 2026")
                .referenceNo("TXN-2026-" + nextNum)
                .riskIndicators(List.of(
                        "Transaction amount higher than normal account velocity",
                        "Requires enhanced screening due diligence"
                ))
                .build();

        alertDetailsMap.put(id, detail);
        return detail;
    }

    public ComplianceAlertDetailDto dismissComplianceAlert(String id) {
        ComplianceAlertDetailDto detail = getComplianceAlertDetail(id);
        detail.setStatus("Dismissed");
        detail.setStatusTheme("gray");

        complianceAlertsList.stream().filter(a -> a.getId().equalsIgnoreCase(id)).findFirst().ifPresent(a -> {
            a.setStatus("Dismissed");
            a.setStatusTheme("gray");
        });
        return detail;
    }

    public ComplianceAlertDetailDto updateAlertStatus(String id, String newStatus) {
        ComplianceAlertDetailDto detail = getComplianceAlertDetail(id);
        String theme = newStatus.equalsIgnoreCase("Resolved") ? "green" :
                (newStatus.equalsIgnoreCase("Dismissed") ? "gray" :
                (newStatus.equalsIgnoreCase("In Progress") || newStatus.equalsIgnoreCase("Investigating") ? "blue" : "red"));

        detail.setStatus(newStatus);
        detail.setStatusTheme(theme);

        complianceAlertsList.stream().filter(a -> a.getId().equalsIgnoreCase(id)).findFirst().ifPresent(a -> {
            a.setStatus(newStatus);
            a.setStatusTheme(theme);
        });
        return detail;
    }

    // ==========================================
    // 4. TRANSACTION MONITORING
    // ==========================================
    public SubsidiaryTransactionMonitoringPageDto getTransactionMonitoringPage(String company, String tab, String query, String transactionType, String riskLevel, String status) {
        List<KpiCardDto> kpis = List.of(
                KpiCardDto.builder().title("Total Transactions").value("1,248").change("+18% vs last quarter").comparisonText("").trend("positive").icon("repeat").color("blue").build(),
                KpiCardDto.builder().title("Flagged Transactions").value("32").change("+45% vs last quarter").comparisonText("").trend("negative").icon("alert-circle").color("red").build(),
                KpiCardDto.builder().title("Under Review").value("18").change("+28% vs last quarter").comparisonText("").trend("neutral").icon("clock").color("orange").build(),
                KpiCardDto.builder().title("Cleared Transactions").value("1,198").change("+16% vs last quarter").comparisonText("").trend("positive").icon("check-circle").color("green").build()
        );

        List<SubsidiaryTransactionMonitoringPageDto.MonthlyVolumeTrendDto> trend = List.of(
                new SubsidiaryTransactionMonitoringPageDto.MonthlyVolumeTrendDto("Apr", 120, 12),
                new SubsidiaryTransactionMonitoringPageDto.MonthlyVolumeTrendDto("May", 150, 18),
                new SubsidiaryTransactionMonitoringPageDto.MonthlyVolumeTrendDto("Jun", 180, 25),
                new SubsidiaryTransactionMonitoringPageDto.MonthlyVolumeTrendDto("Jul", 165, 22),
                new SubsidiaryTransactionMonitoringPageDto.MonthlyVolumeTrendDto("Aug", 140, 16),
                new SubsidiaryTransactionMonitoringPageDto.MonthlyVolumeTrendDto("Sep", 170, 20)
        );

        SubsidiaryTransactionMonitoringPageDto.TransactionTypesDonutDto typesDonut = SubsidiaryTransactionMonitoringPageDto.TransactionTypesDonutDto.builder()
                .totalTransactions(1248)
                .slices(List.of(
                        new SubsidiaryTransactionMonitoringPageDto.TransactionTypeSliceDto("Share Transfer", 40.0, 499, "#3b82f6"),
                        new SubsidiaryTransactionMonitoringPageDto.TransactionTypeSliceDto("Share Issuance", 20.0, 250, "#22c55e"),
                        new SubsidiaryTransactionMonitoringPageDto.TransactionTypeSliceDto("Share Buyback", 15.0, 187, "#f59e0b"),
                        new SubsidiaryTransactionMonitoringPageDto.TransactionTypeSliceDto("Pledge / Encumbrance", 10.0, 125, "#a855f7"),
                        new SubsidiaryTransactionMonitoringPageDto.TransactionTypeSliceDto("Corporate Action", 8.0, 100, "#06b6d4"),
                        new SubsidiaryTransactionMonitoringPageDto.TransactionTypeSliceDto("Others", 7.0, 87, "#94a3b8")
                )).build();

        List<SubsidiaryTransactionMonitoringPageDto.MonitoredTransactionItemDto> filtered = monitoredTransactionsList.stream()
                .filter(t -> {
                    if (query != null && !query.isBlank()) {
                        String q = query.toLowerCase();
                        if (!t.getTransactionId().toLowerCase().contains(q) &&
                            !t.getEntity().toLowerCase().contains(q) &&
                            !t.getInvestorOrShareholder().toLowerCase().contains(q)) return false;
                    }
                    if (tab != null && !tab.isBlank() && !tab.equalsIgnoreCase("All Transactions")) {
                        if (tab.equalsIgnoreCase("Flagged") && !t.getStatus().equalsIgnoreCase("Flagged")) return false;
                        if (tab.equalsIgnoreCase("Under Review") && !t.getStatus().equalsIgnoreCase("Under Review")) return false;
                        if (tab.equalsIgnoreCase("Cleared") && !t.getStatus().equalsIgnoreCase("Cleared")) return false;
                        if (tab.equalsIgnoreCase("High Risk") && !t.getRiskLevel().equalsIgnoreCase("High")) return false;
                    }
                    if (transactionType != null && !transactionType.isBlank() && !transactionType.equalsIgnoreCase("All Transaction Types")) {
                        if (!t.getType().equalsIgnoreCase(transactionType)) return false;
                    }
                    if (riskLevel != null && !riskLevel.isBlank() && !riskLevel.equalsIgnoreCase("All Risk Levels")) {
                        if (!t.getRiskLevel().equalsIgnoreCase(riskLevel)) return false;
                    }
                    if (status != null && !status.isBlank() && !status.equalsIgnoreCase("All Status")) {
                        if (!t.getStatus().equalsIgnoreCase(status)) return false;
                    }
                    return true;
                }).collect(Collectors.toList());

        MonitoredTransactionDetailDto selected = monitoredTransactionDetailsMap.getOrDefault("mtxn-1", monitoredTransactionDetailsMap.values().iterator().next());

        return SubsidiaryTransactionMonitoringPageDto.builder()
                .adminUser(adminUser)
                .pageTitle("Transaction Monitoring")
                .pageSubtitle("Monitor and analyze share transactions for unusual or suspicious activity.")
                .selectedCompany(company != null ? company : "Atirath Holdings")
                .dateRange("01 Apr 2026 - 30 Sep 2026")
                .kpis(kpis)
                .transactionVolumeTrend(trend)
                .transactionTypes(typesDonut)
                .tabs(List.of("All Transactions", "Flagged", "Under Review", "Cleared", "High Risk", "Search & Screening"))
                .activeTab(tab != null ? tab : "All Transactions")
                .entities(List.of("All Entities", "Atirath Holdings", "Atirath Traders", "Atirath Technologies", "Farm Vantara", "Sunrise Ventures", "Others"))
                .transactionTypesList(List.of("All Transaction Types", "Share Transfer", "Share Issuance", "Share Buyback", "Pledge Creation", "Corporate Action"))
                .riskLevels(List.of("All Risk Levels", "High", "Medium", "Low"))
                .statuses(List.of("All Status", "Flagged", "Under Review", "Cleared"))
                .transactions(filtered)
                .totalTransactions(1248)
                .currentPage(1)
                .totalPages(125)
                .pageSize(10)
                .selectedTransactionDetail(selected)
                .build();
    }

    public MonitoredTransactionDetailDto getMonitoredTransactionDetail(String id) {
        return monitoredTransactionDetailsMap.getOrDefault(id, monitoredTransactionDetailsMap.get("mtxn-1"));
    }

    public Map<String, Object> runScreening(RunTransactionScreeningRequestDto req) {
        return Map.of(
                "status", "COMPLETED",
                "screenedTransactionsCount", 1248,
                "newFlaggedCount", 2,
                "sanctionsMatches", 1,
                "message", "Transaction screening completed successfully against latest watchlists."
        );
    }

    public MonitoredTransactionDetailDto clearTransaction(String id) {
        MonitoredTransactionDetailDto detail = getMonitoredTransactionDetail(id);
        detail.setStatus("Cleared");
        detail.setStatusTheme("green");

        monitoredTransactionsList.stream().filter(t -> t.getId().equalsIgnoreCase(id)).findFirst().ifPresent(t -> {
            t.setStatus("Cleared");
            t.setStatusTheme("green");
        });
        return detail;
    }

    // ==========================================
    // 5. SUSPICIOUS TRANSACTIONS
    // ==========================================
    public SuspiciousTransactionsPageDto getSuspiciousTransactionsPage(String company, String tab, String query, String riskLevel, String alertType, String status) {
        List<KpiCardDto> kpis = List.of(
                KpiCardDto.builder().title("Total Flagged").value("32").change("+45% vs last quarter").comparisonText("").trend("negative").icon("alert-triangle").color("red").build(),
                KpiCardDto.builder().title("Under Investigation").value("14").change("+27% vs last quarter").comparisonText("").trend("neutral").icon("search").color("orange").build(),
                KpiCardDto.builder().title("Resolved").value("12").change("+33% vs last quarter").comparisonText("").trend("positive").icon("check-circle").color("green").build(),
                KpiCardDto.builder().title("Closed (No Issue)").value("6").change("-14% vs last quarter").comparisonText("").trend("positive").icon("minus-circle").color("gray").build()
        );

        List<SuspiciousTransactionsPageDto.SuspiciousTransactionsTrendDto> trend = List.of(
                new SuspiciousTransactionsPageDto.SuspiciousTransactionsTrendDto("Apr", 12, 6, 4, 2),
                new SuspiciousTransactionsPageDto.SuspiciousTransactionsTrendDto("May", 18, 9, 6, 3),
                new SuspiciousTransactionsPageDto.SuspiciousTransactionsTrendDto("Jun", 22, 11, 7, 4),
                new SuspiciousTransactionsPageDto.SuspiciousTransactionsTrendDto("Jul", 20, 10, 6, 4),
                new SuspiciousTransactionsPageDto.SuspiciousTransactionsTrendDto("Aug", 16, 8, 5, 3),
                new SuspiciousTransactionsPageDto.SuspiciousTransactionsTrendDto("Sep", 14, 7, 4, 3)
        );

        SuspiciousTransactionsPageDto.AlertTypeDistributionDonutDto alertDist = SuspiciousTransactionsPageDto.AlertTypeDistributionDonutDto.builder()
                .totalFlaggedTransactions(32)
                .slices(List.of(
                        new SuspiciousTransactionsPageDto.AlertTypeSliceDto("Unusual Trading Pattern", 31.3, 10, "#3b82f6"),
                        new SuspiciousTransactionsPageDto.AlertTypeSliceDto("Price Manipulation", 21.9, 7, "#22c55e"),
                        new SuspiciousTransactionsPageDto.AlertTypeSliceDto("Layering", 15.6, 5, "#f59e0b"),
                        new SuspiciousTransactionsPageDto.AlertTypeSliceDto("Insider Trading", 12.5, 4, "#a855f7"),
                        new SuspiciousTransactionsPageDto.AlertTypeSliceDto("Wash Trading", 9.4, 3, "#06b6d4"),
                        new SuspiciousTransactionsPageDto.AlertTypeSliceDto("Other", 9.4, 3, "#ec4899")
                )).build();

        List<SuspiciousTransactionsPageDto.SuspiciousTransactionItemDto> filtered = suspiciousTransactionsList.stream()
                .filter(s -> {
                    if (query != null && !query.isBlank()) {
                        String q = query.toLowerCase();
                        if (!s.getTransactionId().toLowerCase().contains(q) &&
                            !s.getEntity().toLowerCase().contains(q) &&
                            !s.getInvestorOrShareholder().toLowerCase().contains(q)) return false;
                    }
                    if (tab != null && !tab.isBlank() && !tab.equalsIgnoreCase("All")) {
                        if (tab.equalsIgnoreCase("Under Investigation") && !s.getStatus().equalsIgnoreCase("Under Investigation")) return false;
                        if (tab.equalsIgnoreCase("Pending Information") && !s.getStatus().equalsIgnoreCase("Pending Info")) return false;
                        if (tab.equalsIgnoreCase("Resolved") && !s.getStatus().equalsIgnoreCase("Resolved")) return false;
                        if (tab.equalsIgnoreCase("Closed (No Issue)") && !s.getStatus().equalsIgnoreCase("Closed (No Issue)")) return false;
                    }
                    if (riskLevel != null && !riskLevel.isBlank() && !riskLevel.equalsIgnoreCase("All Risk Levels")) {
                        if (!s.getRiskLevel().equalsIgnoreCase(riskLevel)) return false;
                    }
                    if (alertType != null && !alertType.isBlank() && !alertType.equalsIgnoreCase("All Alert Types")) {
                        if (!s.getAlertType().equalsIgnoreCase(alertType)) return false;
                    }
                    if (status != null && !status.isBlank() && !status.equalsIgnoreCase("All Status")) {
                        if (!s.getStatus().equalsIgnoreCase(status)) return false;
                    }
                    return true;
                }).collect(Collectors.toList());

        SuspiciousTransactionDetailDto selected = suspiciousTransactionDetailsMap.getOrDefault("stx-1", suspiciousTransactionDetailsMap.values().iterator().next());

        return SuspiciousTransactionsPageDto.builder()
                .adminUser(adminUser)
                .pageTitle("Suspicious Transactions")
                .pageSubtitle("Identify, review and investigate potentially suspicious share transactions.")
                .selectedCompany(company != null ? company : "Atirath Holdings")
                .dateRange("01 Apr 2026 - 30 Sep 2026")
                .kpis(kpis)
                .trend(trend)
                .alertTypeDistribution(alertDist)
                .tabs(List.of("All", "Under Investigation", "Pending Information", "Resolved", "Closed (No Issue)"))
                .activeTab(tab != null ? tab : "All")
                .entities(List.of("All Entities", "Atirath Holdings", "Atirath Traders", "Atirath Technologies", "Farm Vantara", "Sunrise Ventures", "Others"))
                .riskLevels(List.of("All Risk Levels", "High", "Medium", "Low"))
                .alertTypes(List.of("All Alert Types", "Unusual Trading Pattern", "Price Manipulation", "Layering", "Insider Trading", "Wash Trading"))
                .statuses(List.of("All Status", "Under Investigation", "Pending Info", "Resolved", "Closed (No Issue)"))
                .transactions(filtered)
                .totalTransactions(32)
                .currentPage(1)
                .totalPages(4)
                .pageSize(10)
                .selectedTransactionDetail(selected)
                .build();
    }

    public SuspiciousTransactionDetailDto getSuspiciousTransactionDetail(String id) {
        return suspiciousTransactionDetailsMap.getOrDefault(id, suspiciousTransactionDetailsMap.get("stx-1"));
    }

    public SuspiciousTransactionDetailDto flagSuspiciousTransaction(FlagSuspiciousTransactionRequestDto req) {
        int nextNum = suspiciousTxnCounter.incrementAndGet();
        String id = "stx-" + nextNum;
        String txnId = "STX-2026-" + String.format("%03d", nextNum);

        String rLevel = req.getRiskLevel() != null ? req.getRiskLevel() : "High";
        String rTheme = rLevel.equalsIgnoreCase("High") ? "red" : (rLevel.equalsIgnoreCase("Medium") ? "orange" : "green");

        SuspiciousTransactionsPageDto.SuspiciousTransactionItemDto item = SuspiciousTransactionsPageDto.SuspiciousTransactionItemDto.builder()
                .id(id)
                .number(nextNum)
                .transactionId(txnId)
                .dateTime("29 Sep 2026, 03:00 PM")
                .entity(req.getEntity() != null ? req.getEntity() : "Atirath Traders")
                .investorOrShareholder(req.getInvestorOrShareholder() != null ? req.getInvestorOrShareholder() : "New Investor")
                .alertType(req.getAlertType() != null ? req.getAlertType() : "Unusual Trading Pattern")
                .riskLevel(rLevel)
                .riskTheme(rTheme)
                .status("Under Investigation")
                .statusTheme("orange")
                .build();

        suspiciousTransactionsList.add(0, item);

        SuspiciousTransactionDetailDto detail = SuspiciousTransactionDetailDto.builder()
                .id(id)
                .transactionId(txnId)
                .alertType(req.getAlertType() != null ? req.getAlertType() : "Unusual Trading Pattern")
                .dateTime("29 Sep 2026, 03:00 PM")
                .status("Under Investigation")
                .statusTheme("orange")
                .riskLevel(rLevel)
                .riskTheme(rTheme)
                .tabs(List.of("Details", "Alerts", "Investigation", "Related", "History"))
                .activeTab("Details")
                .transactionType(req.getTransactionType() != null ? req.getTransactionType() : "Share Transfer")
                .entity(req.getEntity() != null ? req.getEntity() : "Atirath Traders")
                .investorOrShareholder(req.getInvestorOrShareholder() != null ? req.getInvestorOrShareholder() : "New Investor")
                .fromParty(req.getFromParty() != null ? req.getFromParty() : "Primary Account")
                .toParty(req.getToParty() != null ? req.getToParty() : "Counterparty Account")
                .noOfShares(req.getNoOfShares() != null ? req.getNoOfShares() : "1,00,000")
                .amount(req.getAmount() != null ? req.getAmount() : "₹ 1,00,00,000")
                .pricePerShare("100")
                .transactionDate("29 Sep 2026, 03:00 PM")
                .detectionSource("Manual / Admin Flagging")
                .detectionRule("Admin marked as high risk audit")
                .detectedOn("29 Sep 2026, 03:05 PM")
                .confidenceScore("95%")
                .additionalNotes(req.getNotes() != null ? req.getNotes() : "Flagged for comprehensive compliance investigation.")
                .build();

        suspiciousTransactionDetailsMap.put(id, detail);
        return detail;
    }

    public SuspiciousTransactionDetailDto updateSuspiciousTransactionStatus(String id, String newStatus) {
        SuspiciousTransactionDetailDto detail = getSuspiciousTransactionDetail(id);
        String theme = newStatus.equalsIgnoreCase("Resolved") ? "green" :
                (newStatus.equalsIgnoreCase("Closed (No Issue)") ? "gray" :
                (newStatus.equalsIgnoreCase("Pending Info") ? "blue" : "orange"));

        detail.setStatus(newStatus);
        detail.setStatusTheme(theme);

        suspiciousTransactionsList.stream().filter(s -> s.getId().equalsIgnoreCase(id)).findFirst().ifPresent(s -> {
            s.setStatus(newStatus);
            s.setStatusTheme(theme);
        });
        return detail;
    }

    // ==========================================
    // 6. RESTRICTED USERS
    // ==========================================
    public RestrictedUsersPageDto getRestrictedUsersPage(String company, String tab, String query, String entity, String restrictionType, String status) {
        List<KpiCardDto> kpis = List.of(
                KpiCardDto.builder().title("Total Restricted Users").value("18").change("+50% vs last quarter").comparisonText("").trend("negative").icon("user-x").color("red").build(),
                KpiCardDto.builder().title("Temporary Restrictions").value("12").change("+33% vs last quarter").comparisonText("").trend("neutral").icon("clock").color("orange").build(),
                KpiCardDto.builder().title("Permanent Restrictions").value("6").change("+200% vs last quarter").comparisonText("").trend("negative").icon("slash").color("red").build(),
                KpiCardDto.builder().title("Reinstated Users").value("4").change("+100% vs last quarter").comparisonText("").trend("positive").icon("user-check").color("green").build()
        );

        List<RestrictedUsersPageDto.RestrictedUserItemDto> filtered = restrictedUsersList.stream()
                .filter(u -> {
                    if (query != null && !query.isBlank()) {
                        String q = query.toLowerCase();
                        if (!u.getName().toLowerCase().contains(q) &&
                            !u.getUserId().toLowerCase().contains(q) &&
                            !u.getEntity().toLowerCase().contains(q)) return false;
                    }
                    if (tab != null && !tab.isBlank() && !tab.equalsIgnoreCase("All Users")) {
                        if (tab.equalsIgnoreCase("Temporary Restriction") && !u.getRestrictionType().contains("Temporary")) return false;
                        if (tab.equalsIgnoreCase("Permanent Restriction") && !u.getRestrictionType().contains("Permanent")) return false;
                        if (tab.equalsIgnoreCase("Reinstated Users") && !u.getStatus().equalsIgnoreCase("Reinstated")) return false;
                    }
                    if (entity != null && !entity.isBlank() && !entity.equalsIgnoreCase("All Entities")) {
                        if (!u.getEntity().equalsIgnoreCase(entity)) return false;
                    }
                    if (restrictionType != null && !restrictionType.isBlank() && !restrictionType.equalsIgnoreCase("All Restriction Types")) {
                        if (!u.getRestrictionType().equalsIgnoreCase(restrictionType)) return false;
                    }
                    if (status != null && !status.isBlank() && !status.equalsIgnoreCase("All Status")) {
                        if (!u.getStatus().equalsIgnoreCase(status)) return false;
                    }
                    return true;
                }).collect(Collectors.toList());

        RestrictedUserDetailDto selected = restrictedUserDetailsMap.getOrDefault("ru-1", restrictedUserDetailsMap.values().iterator().next());

        return RestrictedUsersPageDto.builder()
                .adminUser(adminUser)
                .pageTitle("Restricted Users")
                .pageSubtitle("Manage users restricted from trading or corporate actions across all subsidiaries.")
                .selectedCompany(company != null ? company : "Atirath Holdings")
                .dateRange("01 Apr 2026 - 30 Sep 2026")
                .kpis(kpis)
                .tabs(List.of("All Users", "Temporary Restriction", "Permanent Restriction", "Reinstated Users"))
                .activeTab(tab != null ? tab : "All Users")
                .entities(List.of("All Entities", "Atirath Holdings", "Atirath Traders", "Atirath Technologies", "Farm Vantara", "Sunrise Ventures", "Green Future Fund", "Others"))
                .restrictionTypes(List.of("All Restriction Types", "Trading Restriction", "Corporate Action Restriction", "System Access Restriction", "Temporary Restriction", "Permanent Restriction"))
                .statuses(List.of("All Status", "Active", "Under Review", "Reinstated"))
                .users(filtered)
                .totalUsers(18)
                .currentPage(1)
                .totalPages(2)
                .pageSize(10)
                .selectedUserDetail(selected)
                .build();
    }

    public RestrictedUserDetailDto getRestrictedUserDetail(String id) {
        return restrictedUserDetailsMap.getOrDefault(id, restrictedUserDetailsMap.get("ru-1"));
    }

    public RestrictedUserDetailDto restrictUser(RestrictUserRequestDto req) {
        int nextNum = restrictedUserCounter.incrementAndGet();
        String id = "ru-" + nextNum;
        String uid = "US-2026-" + String.format("%03d", nextNum);
        String initials = req.getFullName().length() >= 2 ? req.getFullName().substring(0, 2).toUpperCase() : "US";

        RestrictedUsersPageDto.RestrictedUserItemDto item = RestrictedUsersPageDto.RestrictedUserItemDto.builder()
                .id(id)
                .number(nextNum)
                .userId(uid)
                .name(req.getFullName())
                .entity(req.getEntity() != null ? req.getEntity() : "Atirath Traders")
                .role(req.getRole() != null ? req.getRole() : "Investor")
                .restrictionType(req.getRestrictionType() != null ? req.getRestrictionType() : "Trading Restriction")
                .restrictionTypeTheme("red")
                .reason(req.getReason() != null ? req.getReason() : "Compliance policy restriction")
                .restrictedOn("29 Sep 2026")
                .status("Active")
                .statusTheme("red")
                .build();

        restrictedUsersList.add(0, item);

        RestrictedUserDetailDto detail = RestrictedUserDetailDto.builder()
                .id(id)
                .userId(uid)
                .fullName(req.getFullName())
                .initials(initials)
                .role(req.getRole() != null ? req.getRole() : "Investor")
                .entity(req.getEntity() != null ? req.getEntity() : "Atirath Traders")
                .status("Active")
                .statusTheme("red")
                .restrictedOnDate("29 Sep 2026")
                .tabs(List.of("Details", "Restriction History", "Related Transactions", "Notes"))
                .activeTab("Details")
                .email(req.getEmail() != null ? req.getEmail() : "user" + nextNum + "@example.com")
                .phone(req.getPhone() != null ? req.getPhone() : "+91 98765 11111")
                .restrictionType(req.getRestrictionType() != null ? req.getRestrictionType() : "Trading Restriction")
                .restrictionTypeTheme("red")
                .restrictedOn("29 Sep 2026, 03:30 PM")
                .restrictedBy("Admin User")
                .reason(req.getReason() != null ? req.getReason() : "Compliance policy violation")
                .duration(req.getDuration() != null ? req.getDuration() : "6 months")
                .notes(req.getNotes() != null ? req.getNotes() : "Restricted pending formal inquiry.")
                .kycStatus("Verified")
                .pepStatus("No")
                .sanctionsCheck("Clear")
                .build();

        restrictedUserDetailsMap.put(id, detail);
        return detail;
    }

    public RestrictedUserDetailDto liftUserRestriction(String id) {
        RestrictedUserDetailDto detail = getRestrictedUserDetail(id);
        detail.setStatus("Reinstated");
        detail.setStatusTheme("green");

        restrictedUsersList.stream().filter(u -> u.getId().equalsIgnoreCase(id)).findFirst().ifPresent(u -> {
            u.setStatus("Reinstated");
            u.setStatusTheme("green");
        });
        return detail;
    }

    // ==========================================
    // 7. RESTRICTED SHARES
    // ==========================================
    public RestrictedSharesPageDto getRestrictedSharesPage(String company, String tab, String query, String entity, String shareClass, String restrictionType, String status) {
        List<KpiCardDto> kpis = List.of(
                KpiCardDto.builder().title("Total Restricted Shares").value("12,00,000").change("+20% vs last quarter").comparisonText("").trend("positive").icon("lock").color("red").build(),
                KpiCardDto.builder().title("Temporary Restrictions").value("7,50,000").change("+15% vs last quarter").comparisonText("").trend("neutral").icon("clock").color("orange").build(),
                KpiCardDto.builder().title("Permanent Restrictions").value("4,50,000").change("+50% vs last quarter").comparisonText("").trend("negative").icon("slash").color("red").build(),
                KpiCardDto.builder().title("Restriction Expiring in 30 Days").value("1,20,000").change("-25% vs last quarter").comparisonText("").trend("positive").icon("pie-chart").color("green").build()
        );

        RestrictedSharesPageDto.RestrictedSharesByTypeDonutDto byType = RestrictedSharesPageDto.RestrictedSharesByTypeDonutDto.builder()
                .totalRestrictedShares("12,00,000 Restricted Shares")
                .slices(List.of(
                        new RestrictedSharesPageDto.RestrictedShareTypeSliceDto("Lock-in", 33.3, "4,00,000", "#ef4444"),
                        new RestrictedSharesPageDto.RestrictedShareTypeSliceDto("Regulatory Hold", 25.0, "3,00,000", "#eab308"),
                        new RestrictedSharesPageDto.RestrictedShareTypeSliceDto("Promoter Lock-in", 16.7, "2,00,000", "#f97316"),
                        new RestrictedSharesPageDto.RestrictedShareTypeSliceDto("Employee ESOP", 12.5, "1,50,000", "#3b82f6"),
                        new RestrictedSharesPageDto.RestrictedShareTypeSliceDto("Litigation Hold", 8.3, "1,00,000", "#a855f7"),
                        new RestrictedSharesPageDto.RestrictedShareTypeSliceDto("Others", 4.2, "50,000", "#22c55e")
                )).build();

        List<RestrictedSharesPageDto.RestrictionExpiryBarDto> timeline = List.of(
                new RestrictedSharesPageDto.RestrictionExpiryBarDto("Oct 2026", 45000, 15000),
                new RestrictedSharesPageDto.RestrictionExpiryBarDto("Nov 2026", 75000, 20000),
                new RestrictedSharesPageDto.RestrictionExpiryBarDto("Dec 2026", 115000, 30000),
                new RestrictedSharesPageDto.RestrictionExpiryBarDto("Jan 2027", 55000, 15000),
                new RestrictedSharesPageDto.RestrictionExpiryBarDto("Feb 2027", 110000, 40000),
                new RestrictedSharesPageDto.RestrictionExpiryBarDto("Mar 2027", 65000, 20000)
        );

        List<RestrictedSharesPageDto.RestrictedShareItemDto> filtered = restrictedSharesList.stream()
                .filter(s -> {
                    if (query != null && !query.isBlank()) {
                        String q = query.toLowerCase();
                        if (!s.getShareholder().toLowerCase().contains(q) &&
                            !s.getShareId().toLowerCase().contains(q) &&
                            !s.getEntity().toLowerCase().contains(q)) return false;
                    }
                    if (tab != null && !tab.isBlank() && !tab.equalsIgnoreCase("All Restricted Shares")) {
                        if (tab.equalsIgnoreCase("Temporary Restrictions") && !s.getRestrictionType().contains("Lock-in") && !s.getRestrictionType().contains("Hold")) return false;
                        if (tab.equalsIgnoreCase("Permanent Restrictions") && !s.getRestrictionType().contains("Litigation") && !s.getRestrictionType().contains("Court")) return false;
                    }
                    if (entity != null && !entity.isBlank() && !entity.equalsIgnoreCase("All Entities")) {
                        if (!s.getEntity().equalsIgnoreCase(entity)) return false;
                    }
                    if (shareClass != null && !shareClass.isBlank() && !shareClass.equalsIgnoreCase("All Share Classes")) {
                        if (!s.getShareClass().equalsIgnoreCase(shareClass)) return false;
                    }
                    if (restrictionType != null && !restrictionType.isBlank() && !restrictionType.equalsIgnoreCase("All Restriction Types")) {
                        if (!s.getRestrictionType().equalsIgnoreCase(restrictionType)) return false;
                    }
                    if (status != null && !status.isBlank() && !status.equalsIgnoreCase("All Status")) {
                        if (!s.getStatus().equalsIgnoreCase(status)) return false;
                    }
                    return true;
                }).collect(Collectors.toList());

        RestrictedShareDetailDto selected = restrictedShareDetailsMap.getOrDefault("rs-1", restrictedShareDetailsMap.values().iterator().next());

        return RestrictedSharesPageDto.builder()
                .adminUser(adminUser)
                .pageTitle("Restricted Shares")
                .pageSubtitle("Manage and monitor shares subject to trading restrictions across all subsidiaries.")
                .selectedCompany(company != null ? company : "Atirath Holdings")
                .dateRange("01 Apr 2026 - 30 Sep 2026")
                .kpis(kpis)
                .tabs(List.of("All Restricted Shares", "Temporary Restrictions", "Permanent Restrictions", "Expiring Soon"))
                .activeTab(tab != null ? tab : "All Restricted Shares")
                .entities(List.of("All Entities", "Atirath Holdings", "Atirath Traders", "Atirath Technologies", "Farm Vantara", "Sunrise Ventures", "Others"))
                .shareClasses(List.of("All Share Classes", "Equity - A", "Equity - B", "Equity - C", "Preference"))
                .restrictionTypes(List.of("All Restriction Types", "Lock-in", "Regulatory Hold", "Promoter Lock-in", "Litigation Hold", "Employee ESOP", "Court Order"))
                .statuses(List.of("All Status", "Active", "Under Review", "Pending"))
                .shares(filtered)
                .totalRestrictedSharesCount(24)
                .currentPage(1)
                .totalPages(3)
                .pageSize(10)
                .restrictedSharesByType(byType)
                .restrictionExpiryTimeline(timeline)
                .selectedShareDetail(selected)
                .build();
    }

    public RestrictedShareDetailDto getRestrictedShareDetail(String id) {
        return restrictedShareDetailsMap.getOrDefault(id, restrictedShareDetailsMap.get("rs-1"));
    }

    public RestrictedShareDetailDto restrictShares(RestrictSharesRequestDto req) {
        int nextNum = restrictedShareCounter.incrementAndGet();
        String id = "rs-" + nextNum;
        String sid = "RS-2026-" + String.format("%03d", nextNum);

        RestrictedSharesPageDto.RestrictedShareItemDto item = RestrictedSharesPageDto.RestrictedShareItemDto.builder()
                .id(id)
                .number(nextNum)
                .shareId(sid)
                .shareholder(req.getShareholder())
                .entity(req.getEntity() != null ? req.getEntity() : "Atirath Traders")
                .shareClass(req.getShareClass() != null ? req.getShareClass() : "Equity - A")
                .noOfShares(req.getNoOfShares() != null ? req.getNoOfShares() : "50,000")
                .restrictionType(req.getRestrictionType() != null ? req.getRestrictionType() : "Lock-in")
                .restrictionTypeTheme("pink")
                .fromDate(req.getFromDate() != null ? req.getFromDate() : "01 Oct 2026")
                .toDate(req.getToDate() != null ? req.getToDate() : "31 Dec 2028")
                .status("Active")
                .statusTheme("red")
                .build();

        restrictedSharesList.add(0, item);

        RestrictedShareDetailDto detail = RestrictedShareDetailDto.builder()
                .id(id)
                .shareId(sid)
                .restrictionTypeTitle(req.getRestrictionType() != null ? req.getRestrictionType() + " Restriction" : "Lock-in Restriction")
                .status("Active")
                .statusTheme("red")
                .createdInfo("Created on 29 Sep 2026, 03:45 PM")
                .tabs(List.of("Details", "History", "Related Transactions", "Notes"))
                .activeTab("Details")
                .entity(req.getEntity() != null ? req.getEntity() : "Atirath Traders")
                .shareClass(req.getShareClass() != null ? req.getShareClass() : "Equity - A")
                .noOfShares(req.getNoOfShares() != null ? req.getNoOfShares() : "50,000")
                .faceValue("10")
                .certificateNo(req.getCertificateNo() != null ? req.getCertificateNo() : "AT-EC-" + nextNum)
                .restrictionType(req.getRestrictionType() != null ? req.getRestrictionType() : "Lock-in")
                .restrictionTypeTheme("red")
                .fromDate(req.getFromDate() != null ? req.getFromDate() : "01 Oct 2026")
                .toDate(req.getToDate() != null ? req.getToDate() : "31 Dec 2028")
                .reason(req.getReason() != null ? req.getReason() : "Standard statutory lock-in")
                .applicableTo(req.getApplicableTo() != null ? req.getApplicableTo() : "Trading, Transfer, Pledge")
                .createdBy("Admin User")
                .createdOn("29 Sep 2026, 03:45 PM")
                .shareholderName(req.getShareholder())
                .shareholderType("Individual Investor")
                .pan("AAZPK" + nextNum + "F")
                .build();

        restrictedShareDetailsMap.put(id, detail);
        return detail;
    }

    public RestrictedShareDetailDto liftShareRestriction(String id) {
        RestrictedShareDetailDto detail = getRestrictedShareDetail(id);
        detail.setStatus("Lifted");
        detail.setStatusTheme("green");

        restrictedSharesList.stream().filter(s -> s.getId().equalsIgnoreCase(id)).findFirst().ifPresent(s -> {
            s.setStatus("Lifted");
            s.setStatusTheme("green");
        });
        return detail;
    }

    // ==========================================
    // 8. COMPLIANCE CASES
    // ==========================================
    public ComplianceCasesPageDto getComplianceCasesPage(String company, String tab, String query, String entity, String caseType, String riskLevel, String status) {
        List<KpiCardDto> kpis = List.of(
                KpiCardDto.builder().title("Total Cases").value("24").change("+33% vs last quarter").comparisonText("").trend("positive").icon("folder").color("orange").build(),
                KpiCardDto.builder().title("Open Cases").value("12").change("+50% vs last quarter").comparisonText("").trend("negative").icon("alert-triangle").color("red").build(),
                KpiCardDto.builder().title("In Progress Cases").value("6").change("+20% vs last quarter").comparisonText("").trend("neutral").icon("clock").color("orange").build(),
                KpiCardDto.builder().title("Resolved Cases").value("6").change("-25% vs last quarter").comparisonText("").trend("positive").icon("check-circle").color("green").build()
        );

        ComplianceCasesPageDto.CasesByTypeDonutDto byType = ComplianceCasesPageDto.CasesByTypeDonutDto.builder()
                .totalCases(24)
                .slices(List.of(
                        new ComplianceCasesPageDto.CaseTypeSliceDto("Market Abuse", 25.0, 6, "#3b82f6"),
                        new ComplianceCasesPageDto.CaseTypeSliceDto("KYC/AML", 16.7, 4, "#06b6d4"),
                        new ComplianceCasesPageDto.CaseTypeSliceDto("Regulatory Breach", 16.7, 4, "#f59e0b"),
                        new ComplianceCasesPageDto.CaseTypeSliceDto("Insider Trading", 12.5, 3, "#ec4899"),
                        new ComplianceCasesPageDto.CaseTypeSliceDto("Fraud", 12.5, 3, "#ef4444"),
                        new ComplianceCasesPageDto.CaseTypeSliceDto("Others", 16.7, 4, "#eab308")
                )).build();

        List<ComplianceCasesPageDto.CaseStatusTrendDto> trend = List.of(
                new ComplianceCasesPageDto.CaseStatusTrendDto("Apr", 4, 2, 2, 3, 1),
                new ComplianceCasesPageDto.CaseStatusTrendDto("May", 5, 3, 2, 4, 1),
                new ComplianceCasesPageDto.CaseStatusTrendDto("Jun", 7, 4, 3, 5, 2),
                new ComplianceCasesPageDto.CaseStatusTrendDto("Jul", 6, 3, 2, 5, 2),
                new ComplianceCasesPageDto.CaseStatusTrendDto("Aug", 8, 4, 3, 6, 2),
                new ComplianceCasesPageDto.CaseStatusTrendDto("Sep", 12, 6, 4, 6, 3)
        );

        List<ComplianceCasesPageDto.ComplianceCaseItemDto> filtered = complianceCasesList.stream()
                .filter(c -> {
                    if (query != null && !query.isBlank()) {
                        String q = query.toLowerCase();
                        if (!c.getTitle().toLowerCase().contains(q) &&
                            !c.getCaseId().toLowerCase().contains(q) &&
                            !c.getEntity().toLowerCase().contains(q)) return false;
                    }
                    if (tab != null && !tab.isBlank() && !tab.equalsIgnoreCase("All Cases")) {
                        if (tab.equalsIgnoreCase("Open") && !c.getStatus().equalsIgnoreCase("Open")) return false;
                        if (tab.equalsIgnoreCase("In Progress") && !c.getStatus().equalsIgnoreCase("In Progress")) return false;
                        if (tab.equalsIgnoreCase("Under Review") && !c.getStatus().equalsIgnoreCase("Under Review")) return false;
                        if (tab.equalsIgnoreCase("Resolved") && !c.getStatus().equalsIgnoreCase("Resolved")) return false;
                        if (tab.equalsIgnoreCase("Closed") && !c.getStatus().equalsIgnoreCase("Closed")) return false;
                    }
                    if (entity != null && !entity.isBlank() && !entity.equalsIgnoreCase("All Entities")) {
                        if (!c.getEntity().equalsIgnoreCase(entity)) return false;
                    }
                    if (caseType != null && !caseType.isBlank() && !caseType.equalsIgnoreCase("All Case Types")) {
                        if (!c.getCaseType().equalsIgnoreCase(caseType)) return false;
                    }
                    if (riskLevel != null && !riskLevel.isBlank() && !riskLevel.equalsIgnoreCase("All Risk Levels")) {
                        if (!c.getRiskLevel().equalsIgnoreCase(riskLevel)) return false;
                    }
                    if (status != null && !status.isBlank() && !status.equalsIgnoreCase("All Status")) {
                        if (!c.getStatus().equalsIgnoreCase(status)) return false;
                    }
                    return true;
                }).collect(Collectors.toList());

        ComplianceCaseDetailDto selected = complianceCaseDetailsMap.getOrDefault("cc-1", complianceCaseDetailsMap.values().iterator().next());

        return ComplianceCasesPageDto.builder()
                .adminUser(adminUser)
                .pageTitle("Compliance Cases")
                .pageSubtitle("Manage and track compliance cases across all subsidiaries.")
                .selectedCompany(company != null ? company : "Atirath Holdings")
                .dateRange("01 Apr 2026 - 30 Sep 2026")
                .kpis(kpis)
                .tabs(List.of("All Cases", "Open", "In Progress", "Under Review", "Resolved", "Closed"))
                .activeTab(tab != null ? tab : "All Cases")
                .entities(List.of("All Entities", "Atirath Holdings", "Atirath Traders", "Atirath Technologies", "Farm Vantara", "Sunrise Ventures", "Green Future Fund", "Others"))
                .caseTypes(List.of("All Case Types", "Market Abuse", "KYC/AML", "Market Manipulation", "Policy Violation", "Sanctions", "Regulatory Breach", "Insider Trading", "Ownership Violation", "Fraud", "Financial Irregularity"))
                .riskLevels(List.of("All Risk Levels", "Critical", "High", "Medium", "Low"))
                .statuses(List.of("All Status", "Open", "In Progress", "Under Review", "Resolved", "Closed"))
                .cases(filtered)
                .totalCases(24)
                .currentPage(1)
                .totalPages(3)
                .pageSize(10)
                .casesByType(byType)
                .caseStatusTrend(trend)
                .selectedCaseDetail(selected)
                .build();
    }

    public ComplianceCaseDetailDto getComplianceCaseDetail(String id) {
        return complianceCaseDetailsMap.getOrDefault(id, complianceCaseDetailsMap.get("cc-1"));
    }

    public ComplianceCaseDetailDto createComplianceCase(CreateComplianceCaseRequestDto req) {
        int nextNum = complianceCaseCounter.incrementAndGet();
        String id = "cc-" + nextNum;
        String cid = "CC-2026-" + String.format("%03d", nextNum);

        String rLevel = req.getRiskLevel() != null ? req.getRiskLevel() : "High";
        String rTheme = rLevel.equalsIgnoreCase("Critical") ? "red" : (rLevel.equalsIgnoreCase("High") ? "red" : (rLevel.equalsIgnoreCase("Medium") ? "orange" : "green"));

        ComplianceCasesPageDto.ComplianceCaseItemDto item = ComplianceCasesPageDto.ComplianceCaseItemDto.builder()
                .id(id)
                .number(nextNum)
                .caseId(cid)
                .title(req.getTitle())
                .entity(req.getEntity() != null ? req.getEntity() : "Atirath Traders")
                .caseType(req.getCaseType() != null ? req.getCaseType() : "Market Abuse")
                .riskLevel(rLevel)
                .riskTheme(rTheme)
                .status("Open")
                .statusTheme("red")
                .assignedTo(req.getAssignedTo() != null ? req.getAssignedTo() : "Rakesh Kumar")
                .createdOn("29 Sep 2026")
                .dueDate(req.getDueDate() != null ? req.getDueDate() : "06 Oct 2026")
                .build();

        complianceCasesList.add(0, item);

        ComplianceCaseDetailDto detail = ComplianceCaseDetailDto.builder()
                .id(id)
                .caseId(cid)
                .title(req.getTitle())
                .status("Open")
                .statusTheme("red")
                .createdInfo("Created On 29 Sep 2026, 04:00 PM")
                .tabs(List.of("Details", "Investigation", "Related Data", "Activity Log"))
                .activeTab("Details")
                .entity(req.getEntity() != null ? req.getEntity() : "Atirath Traders")
                .caseType(req.getCaseType() != null ? req.getCaseType() : "Market Abuse")
                .riskLevel(rLevel)
                .riskTheme(rTheme)
                .assignedTo(req.getAssignedTo() != null ? req.getAssignedTo() : "Rakesh Kumar")
                .createdOn("29 Sep 2026, 04:00 PM")
                .dueDate(req.getDueDate() != null ? req.getDueDate() : "06 Oct 2026")
                .priority(req.getPriority() != null ? req.getPriority() : "High")
                .priorityTheme("red")
                .source("Automated Monitoring")
                .description(req.getDescription() != null ? req.getDescription() : "Multiple compliance indicators flagged for investigation.")
                .investigationOwner(req.getAssignedTo() != null ? req.getAssignedTo() : "Rakesh Kumar")
                .startDate("29 Sep 2026")
                .targetCompletion(req.getDueDate() != null ? req.getDueDate() : "06 Oct 2026")
                .currentStage("Initial Review")
                .findings("Preliminary findings require detailed trade analysis.")
                .riskIndicators(List.of(
                        "Transaction volume 3x higher than average",
                        "Related accounts involved in transactions",
                        "Elevated risk score observed"
                ))
                .build();

        complianceCaseDetailsMap.put(id, detail);
        return detail;
    }

    public ComplianceCaseDetailDto updateComplianceCaseStatus(String id, String newStatus) {
        ComplianceCaseDetailDto detail = getComplianceCaseDetail(id);
        String theme = newStatus.equalsIgnoreCase("Resolved") ? "green" :
                (newStatus.equalsIgnoreCase("Closed") ? "gray" :
                (newStatus.equalsIgnoreCase("In Progress") ? "blue" :
                (newStatus.equalsIgnoreCase("Under Review") ? "orange" : "red")));

        detail.setStatus(newStatus);
        detail.setStatusTheme(theme);

        complianceCasesList.stream().filter(c -> c.getId().equalsIgnoreCase(id)).findFirst().ifPresent(c -> {
            c.setStatus(newStatus);
            c.setStatusTheme(theme);
        });
        return detail;
    }

    // ==========================================
    // SEEDING HELPERS
    // ==========================================
    private void seedComplianceDashboard() {
        recentDashboardAlerts.clear();
        recentDashboardAlerts.add(new SubsidiaryComplianceDashboardDto.RecentComplianceAlertDto("rda-1", 1, "29 Sep 2026, 11:20 AM", "Atirath Traders", "Unusual Transaction", "High", "red", "Large fund transfer detected", "Open", "red"));
        recentDashboardAlerts.add(new SubsidiaryComplianceDashboardDto.RecentComplianceAlertDto("rda-2", 2, "28 Sep 2026, 04:15 PM", "Farm Vantara", "KYC Expiry", "Medium", "orange", "Investor KYC due for renewal", "Under Review", "orange"));
        recentDashboardAlerts.add(new SubsidiaryComplianceDashboardDto.RecentComplianceAlertDto("rda-3", 3, "27 Sep 2026, 10:30 AM", "Atirath Holdings", "Restricted User", "High", "red", "User flagged for trading restriction", "Open", "red"));
        recentDashboardAlerts.add(new SubsidiaryComplianceDashboardDto.RecentComplianceAlertDto("rda-4", 4, "25 Sep 2026, 02:45 PM", "Atirath Technologies", "Suspicious Transaction", "High", "red", "Abnormal trading pattern", "Investigating", "blue"));
        recentDashboardAlerts.add(new SubsidiaryComplianceDashboardDto.RecentComplianceAlertDto("rda-5", 5, "24 Sep 2026, 11:10 AM", "Others", "Share Transfer", "Low", "green", "Transfer requires approval", "Resolved", "green"));

        regulatoryFilingsList.clear();
        regulatoryFilingsList.add(new SubsidiaryComplianceDashboardDto.RegulatoryFilingDueDto("rfd-1", 1, "Annual Return", "Atirath Holdings", "15 Oct 2026", "Pending", "red"));
        regulatoryFilingsList.add(new SubsidiaryComplianceDashboardDto.RegulatoryFilingDueDto("rfd-2", 2, "Financial Statements", "Atirath Traders", "31 Oct 2026", "Pending", "red"));
        regulatoryFilingsList.add(new SubsidiaryComplianceDashboardDto.RegulatoryFilingDueDto("rfd-3", 3, "Shareholding Report", "Farm Vantara", "15 Nov 2026", "In Progress", "blue"));
        regulatoryFilingsList.add(new SubsidiaryComplianceDashboardDto.RegulatoryFilingDueDto("rfd-4", 4, "KYC Update", "Atirath Technologies", "30 Nov 2026", "Pending", "red"));
        regulatoryFilingsList.add(new SubsidiaryComplianceDashboardDto.RegulatoryFilingDueDto("rfd-5", 5, "Compliance Certificate", "Others", "15 Dec 2026", "Not Started", "gray"));

        openCasesList.clear();
        openCasesList.add(new SubsidiaryComplianceDashboardDto.OpenComplianceCaseDto("occ-1", 1, "CC-2026-001", "Atirath Traders", "Unusual Transaction", "Suresh Kumar", "29 Sep 2026", "Open", "red", "High", "red"));
        openCasesList.add(new SubsidiaryComplianceDashboardDto.OpenComplianceCaseDto("occ-2", 2, "CC-2026-002", "Farm Vantara", "KYC Verification", "Priya Mehta", "28 Sep 2026", "Under Review", "orange", "Medium", "orange"));
        openCasesList.add(new SubsidiaryComplianceDashboardDto.OpenComplianceCaseDto("occ-3", 3, "CC-2026-003", "Atirath Holdings", "Restricted User", "Rajesh Nair", "27 Sep 2026", "Open", "red", "High", "red"));
        openCasesList.add(new SubsidiaryComplianceDashboardDto.OpenComplianceCaseDto("occ-4", 4, "CC-2026-004", "Atirath Technologies", "Suspicious Transaction", "Suresh Kumar", "25 Sep 2026", "Investigating", "blue", "High", "red"));
        openCasesList.add(new SubsidiaryComplianceDashboardDto.OpenComplianceCaseDto("occ-5", 5, "CC-2026-005", "Others", "Share Transfer", "Priya Mehta", "24 Sep 2026", "Resolved", "green", "Low", "green"));

        pendingApprovalsList.clear();
        pendingApprovalsList.add(new SubsidiaryComplianceDashboardDto.PendingApprovalDto("pa-1", 1, "KYC Approval", "Atirath Traders", "29 Sep 2026", "Pending", "red"));
        pendingApprovalsList.add(new SubsidiaryComplianceDashboardDto.PendingApprovalDto("pa-2", 2, "Share Transfer", "Farm Vantara", "28 Sep 2026", "Pending", "red"));
        pendingApprovalsList.add(new SubsidiaryComplianceDashboardDto.PendingApprovalDto("pa-3", 3, "User Unblock", "Atirath Holdings", "26 Sep 2026", "Pending", "red"));
        pendingApprovalsList.add(new SubsidiaryComplianceDashboardDto.PendingApprovalDto("pa-4", 4, "Transaction Review", "Atirath Technologies", "25 Sep 2026", "Under Review", "orange"));
        pendingApprovalsList.add(new SubsidiaryComplianceDashboardDto.PendingApprovalDto("pa-5", 5, "Document Approval", "Others", "24 Sep 2026", "Pending", "red"));
    }

    private void seedKycReview() {
        kycRequestsList.clear();
        kycRequestsList.add(new SubsidiaryKycReviewPageDto.KycRequestItemDto("kyc-1", 1, "KYC-2026-001", "Rajesh Kumar", "Individual Investor", "AAZPK1234F", "29 Sep 2026", "Verified", "green", "Low", "green"));
        kycRequestsList.add(new SubsidiaryKycReviewPageDto.KycRequestItemDto("kyc-2", 2, "KYC-2026-002", "Sunrise Ventures Pvt Ltd", "Corporate Investor", "U74999TS2026PTC123456", "28 Sep 2026", "Under Review", "orange", "Medium", "orange"));
        kycRequestsList.add(new SubsidiaryKycReviewPageDto.KycRequestItemDto("kyc-3", 3, "KYC-2026-003", "Priya Mehta", "Individual Investor", "AALPM5678K", "27 Sep 2026", "Pending", "blue", "High", "red"));
        kycRequestsList.add(new SubsidiaryKycReviewPageDto.KycRequestItemDto("kyc-4", 4, "KYC-2026-004", "Global Agri Fund", "Corporate Investor", "U67100MH2026PLC654321", "26 Sep 2026", "Verified", "green", "Low", "green"));
        kycRequestsList.add(new SubsidiaryKycReviewPageDto.KycRequestItemDto("kyc-5", 5, "KYC-2026-005", "Suresh Kumar", "Director / KMP", "ABXPS8765L", "25 Sep 2026", "Rejected", "red", "High", "red"));
        kycRequestsList.add(new SubsidiaryKycReviewPageDto.KycRequestItemDto("kyc-6", 6, "KYC-2026-006", "Atirath Technologies", "Corporate Investor", "U72200TG2026PTC112233", "24 Sep 2026", "Verified", "green", "Low", "green"));
        kycRequestsList.add(new SubsidiaryKycReviewPageDto.KycRequestItemDto("kyc-7", 7, "KYC-2026-007", "Ananya Reddy", "Individual Investor", "CHXPR2345M", "23 Sep 2026", "Under Review", "orange", "Medium", "orange"));
        kycRequestsList.add(new SubsidiaryKycReviewPageDto.KycRequestItemDto("kyc-8", 8, "KYC-2026-008", "Neha Sharma", "Employee", "BBNPS3456D", "22 Sep 2026", "Verified", "green", "Low", "green"));
        kycRequestsList.add(new SubsidiaryKycReviewPageDto.KycRequestItemDto("kyc-9", 9, "KYC-2026-009", "Green Future Fund", "Corporate Investor", "U67100KA2026PTC778899", "21 Sep 2026", "Pending", "blue", "High", "red"));
        kycRequestsList.add(new SubsidiaryKycReviewPageDto.KycRequestItemDto("kyc-10", 10, "KYC-2026-010", "Vikram Rao", "Individual Investor", "AAZVR9988P", "20 Sep 2026", "Verified", "green", "Low", "green"));
        kycRequestsList.add(new SubsidiaryKycReviewPageDto.KycRequestItemDto("kyc-11", 11, "KYC-2026-011", "Farm Vantara Pvt Ltd", "Corporate Investor", "U74999AP2026PTC445566", "19 Sep 2026", "Under Review", "orange", "Medium", "orange"));
        kycRequestsList.add(new SubsidiaryKycReviewPageDto.KycRequestItemDto("kyc-12", 12, "KYC-2026-012", "Lakshmi Narayan", "Individual Investor", "AANPL1122Q", "18 Sep 2026", "Pending", "blue", "Medium", "orange"));

        KycDetailDto kyc1 = KycDetailDto.builder()
                .id("kyc-1")
                .requestId("KYC-2026-001")
                .requestDate("29 Sep 2026")
                .fullName("Rajesh Kumar")
                .initials("RK")
                .entityType("Individual Investor")
                .status("Verified")
                .statusTheme("green")
                .tabs(List.of("Details", "Documents", "Verification", "Remarks", "History"))
                .activeTab("Details")
                .dateOfBirth("12 Mar 1995")
                .email("rajesh.kumar@gmail.com")
                .phone("+91 98765 43210")
                .address("12-3-456, Banjara Hills, Hyderabad, Telangana - 500034")
                .pan("AAZPK1234F")
                .aadhaar("9876 5432 1098")
                .nationality("Indian")
                .occupation("Business")
                .sourceOfFunds("Business Income")
                .riskLevel("Low")
                .riskTheme("green")
                .pepStatus("No")
                .sanctionsCheck("Clear")
                .adverseMedia("No issues found")
                .documents(List.of(
                        new KycDetailDto.KycDocumentDto("PAN Card", "AAZPK1234F", "PAN_Rajesh_Kumar.pdf", "Verified", "29 Sep 2026, 11:30 AM"),
                        new KycDetailDto.KycDocumentDto("Aadhaar Card", "XXXX-XXXX-1098", "Aadhaar_Rajesh.pdf", "Verified", "29 Sep 2026, 11:32 AM"),
                        new KycDetailDto.KycDocumentDto("Bank Statement", "HDFC Bank - 5421", "Bank_Statement_3M.pdf", "Verified", "29 Sep 2026, 11:35 AM")
                ))
                .build();

        kycDetailsMap.put("kyc-1", kyc1);
    }

    private void seedComplianceAlerts() {
        complianceAlertsList.clear();
        complianceAlertsList.add(new SubsidiaryComplianceAlertsPageDto.ComplianceAlertItemDto("al-1", 1, "AL-2026-001", "29 Sep 2026, 11:20 AM", "Atirath Traders", "Large Share Transfer", "High", "red", "Open", "red", "Unusual large share transfer detected (₹ 5.2 Cr)"));
        complianceAlertsList.add(new SubsidiaryComplianceAlertsPageDto.ComplianceAlertItemDto("al-2", 2, "AL-2026-002", "28 Sep 2026, 04:15 PM", "Farm Vantara", "Investor KYC Expiry", "Medium", "orange", "In Progress", "blue", "Investor KYC due for renewal in 7 days"));
        complianceAlertsList.add(new SubsidiaryComplianceAlertsPageDto.ComplianceAlertItemDto("al-3", 3, "AL-2026-003", "27 Sep 2026, 10:30 AM", "Atirath Holdings", "Restricted User Activity", "High", "red", "Open", "red", "User attempted trade beyond assigned limits"));
        complianceAlertsList.add(new SubsidiaryComplianceAlertsPageDto.ComplianceAlertItemDto("al-4", 4, "AL-2026-004", "26 Sep 2026, 02:45 PM", "Atirath Technologies", "Suspicious Transaction", "High", "red", "Investigating", "blue", "Abnormal trading pattern detected"));
        complianceAlertsList.add(new SubsidiaryComplianceAlertsPageDto.ComplianceAlertItemDto("al-5", 5, "AL-2026-005", "25 Sep 2026, 11:10 AM", "Others", "Document Missing", "Low", "green", "Open", "red", "Annual compliance certificate not submitted"));
        complianceAlertsList.add(new SubsidiaryComplianceAlertsPageDto.ComplianceAlertItemDto("al-6", 6, "AL-2026-006", "24 Sep 2026, 03:20 PM", "Sunrise Ventures", "PEP Match Found", "High", "red", "In Progress", "blue", "Investor flagged as PEP match"));
        complianceAlertsList.add(new SubsidiaryComplianceAlertsPageDto.ComplianceAlertItemDto("al-7", 7, "AL-2026-007", "23 Sep 2026, 09:30 AM", "Green Future Fund", "Repeated Failed Login", "Medium", "orange", "Open", "red", "Multiple failed login attempts detected"));
        complianceAlertsList.add(new SubsidiaryComplianceAlertsPageDto.ComplianceAlertItemDto("al-8", 8, "AL-2026-008", "22 Sep 2026, 05:15 PM", "Farm Vantara", "Sanctions Screening", "High", "red", "Open", "red", "Potential sanctions list match found"));
        complianceAlertsList.add(new SubsidiaryComplianceAlertsPageDto.ComplianceAlertItemDto("al-9", 9, "AL-2026-009", "21 Sep 2026, 01:40 PM", "Atirath Traders", "Unusual Dividend Activity", "Medium", "orange", "Resolved", "green", "Dividend payout higher than historical average"));
        complianceAlertsList.add(new SubsidiaryComplianceAlertsPageDto.ComplianceAlertItemDto("al-10", 10, "AL-2026-010", "20 Sep 2026, 10:25 AM", "Atirath Holdings", "Beneficial Owner Change", "Low", "green", "Dismissed", "gray", "Change in beneficial ownership detected"));

        ComplianceAlertDetailDto al1 = ComplianceAlertDetailDto.builder()
                .id("al-1")
                .alertId("AL-2026-001")
                .alertType("Large Share Transfer")
                .status("Open")
                .statusTheme("red")
                .detectedOn("29 Sep 2026, 11:20 AM")
                .tabs(List.of("Details", "Investors", "Related Transactions", "Activity Log"))
                .activeTab("Details")
                .riskLevel("High")
                .riskTheme("red")
                .entity("Atirath Traders")
                .detectionSource("Transaction Monitoring")
                .description("Unusual large share transfer detected (₹ 5.2 Cr) which is 3x higher than average transaction size.")
                .investor("Rakesh Mehta")
                .fromParty("Sunrise Ventures Pvt Ltd")
                .toParty("Global Agri Fund")
                .shares("5,20,000")
                .amount("₹ 5,20,00,000")
                .transactionDate("28 Sep 2026")
                .referenceNo("TXN-2026-4587")
                .riskIndicators(List.of(
                        "Transaction amount significantly higher than average",
                        "Investor flagged for enhanced due diligence",
                        "Transfer outside usual trading pattern"
                ))
                .build();

        alertDetailsMap.put("al-1", al1);
    }

    private void seedTransactionMonitoring() {
        monitoredTransactionsList.clear();
        monitoredTransactionsList.add(new SubsidiaryTransactionMonitoringPageDto.MonitoredTransactionItemDto("mtxn-1", 1, "TXN-2026-001", "29 Sep 2026, 11:20 AM", "Atirath Traders", "Share Transfer", "Rajesh Kumar", "5,20,00,000", "High", "red", "Flagged", "red"));
        monitoredTransactionsList.add(new SubsidiaryTransactionMonitoringPageDto.MonitoredTransactionItemDto("mtxn-2", 2, "TXN-2026-002", "28 Sep 2026, 04:15 PM", "Farm Vantara", "Share Issuance", "Global Agri Fund", "10,00,00,000", "Medium", "orange", "Under Review", "orange"));
        monitoredTransactionsList.add(new SubsidiaryTransactionMonitoringPageDto.MonitoredTransactionItemDto("mtxn-3", 3, "TXN-2026-003", "27 Sep 2026, 10:30 AM", "Atirath Holdings", "Pledge Creation", "Priya Mehta", "2,50,00,000", "High", "red", "Flagged", "red"));
        monitoredTransactionsList.add(new SubsidiaryTransactionMonitoringPageDto.MonitoredTransactionItemDto("mtxn-4", 4, "TXN-2026-004", "26 Sep 2026, 02:45 PM", "Atirath Technologies", "Share Transfer", "Suresh Kumar", "1,75,00,000", "Low", "green", "Cleared", "green"));
        monitoredTransactionsList.add(new SubsidiaryTransactionMonitoringPageDto.MonitoredTransactionItemDto("mtxn-5", 5, "TXN-2026-005", "25 Sep 2026, 11:10 AM", "Others", "Share Buyback", "Green Future Fund", "3,00,00,000", "Medium", "orange", "Under Review", "orange"));
        monitoredTransactionsList.add(new SubsidiaryTransactionMonitoringPageDto.MonitoredTransactionItemDto("mtxn-6", 6, "TXN-2026-006", "24 Sep 2026, 03:20 PM", "Sunrise Ventures", "Share Transfer", "Neha Sharma", "4,80,00,000", "High", "red", "Flagged", "red"));
        monitoredTransactionsList.add(new SubsidiaryTransactionMonitoringPageDto.MonitoredTransactionItemDto("mtxn-7", 7, "TXN-2026-007", "23 Sep 2026, 09:30 AM", "Farm Vantara", "Corporate Action", "Vikram Rao", "1,20,00,000", "Low", "green", "Cleared", "green"));
        monitoredTransactionsList.add(new SubsidiaryTransactionMonitoringPageDto.MonitoredTransactionItemDto("mtxn-8", 8, "TXN-2026-008", "22 Sep 2026, 05:15 PM", "Atirath Traders", "Share Transfer", "Ananya Reddy", "6,50,00,000", "High", "red", "Under Review", "orange"));
        monitoredTransactionsList.add(new SubsidiaryTransactionMonitoringPageDto.MonitoredTransactionItemDto("mtxn-9", 9, "TXN-2026-009", "21 Sep 2026, 01:40 PM", "Atirath Holdings", "Share Issuance", "Lakshmi Narayan", "8,75,00,000", "Medium", "orange", "Cleared", "green"));
        monitoredTransactionsList.add(new SubsidiaryTransactionMonitoringPageDto.MonitoredTransactionItemDto("mtxn-10", 10, "TXN-2026-010", "20 Sep 2026, 10:25 AM", "Atirath Technologies", "Share Transfer", "Rakesh Mehta", "2,10,00,000", "Low", "green", "Cleared", "green"));

        MonitoredTransactionDetailDto mtxn1 = MonitoredTransactionDetailDto.builder()
                .id("mtxn-1")
                .transactionId("TXN-2026-001")
                .type("Share Transfer")
                .status("Flagged")
                .statusTheme("red")
                .dateTime("29 Sep 2026, 11:20 AM")
                .tabs(List.of("Details", "Screening Results", "Related Alerts", "History"))
                .activeTab("Details")
                .entity("Atirath Traders")
                .investorOrShareholder("Rajesh Kumar")
                .fromParty("Rajesh Kumar")
                .toParty("Green Future Fund")
                .noOfShares("5,20,000")
                .amount("₹ 5,20,00,000")
                .pricePerShare("100")
                .transactionDate("29 Sep 2026, 11:20 AM")
                .riskLevel("High")
                .riskTheme("red")
                .pepCheck("Clear")
                .sanctionsCheck("Match Found")
                .adverseMedia("Clear")
                .shareholdingLimit("Exceeds Limit")
                .unusualPattern("Detected")
                .riskNotes(List.of(
                        "Transaction amount significantly higher than average",
                        "Investor appears in sanctions watchlist",
                        "Transfer outside usual trading pattern",
                        "Requires enhanced due diligence"
                ))
                .build();

        monitoredTransactionDetailsMap.put("mtxn-1", mtxn1);
    }

    private void seedSuspiciousTransactions() {
        suspiciousTransactionsList.clear();
        suspiciousTransactionsList.add(new SuspiciousTransactionsPageDto.SuspiciousTransactionItemDto("stx-1", 1, "STX-2026-001", "29 Sep 2026, 11:20 AM", "Atirath Traders", "Rajesh Kumar", "Unusual Trading Pattern", "High", "red", "Under Investigation", "orange"));
        suspiciousTransactionsList.add(new SuspiciousTransactionsPageDto.SuspiciousTransactionItemDto("stx-2", 2, "STX-2026-002", "28 Sep 2026, 04:15 PM", "Farm Vantara", "Global Agri Fund", "Price Manipulation", "High", "red", "Pending Info", "blue"));
        suspiciousTransactionsList.add(new SuspiciousTransactionsPageDto.SuspiciousTransactionItemDto("stx-3", 3, "STX-2026-003", "27 Sep 2026, 10:30 AM", "Atirath Holdings", "Priya Mehta", "Layering", "Medium", "orange", "Under Investigation", "orange"));
        suspiciousTransactionsList.add(new SuspiciousTransactionsPageDto.SuspiciousTransactionItemDto("stx-4", 4, "STX-2026-004", "26 Sep 2026, 02:45 PM", "Atirath Technologies", "Suresh Kumar", "Insider Trading", "High", "red", "Resolved", "green"));
        suspiciousTransactionsList.add(new SuspiciousTransactionsPageDto.SuspiciousTransactionItemDto("stx-5", 5, "STX-2026-005", "25 Sep 2026, 11:10 AM", "Others", "Green Future Fund", "Wash Trading", "Medium", "orange", "Under Investigation", "orange"));
        suspiciousTransactionsList.add(new SuspiciousTransactionsPageDto.SuspiciousTransactionItemDto("stx-6", 6, "STX-2026-006", "24 Sep 2026, 03:20 PM", "Sunrise Ventures", "Neha Sharma", "Unusual Trading Pattern", "High", "red", "Pending Info", "blue"));
        suspiciousTransactionsList.add(new SuspiciousTransactionsPageDto.SuspiciousTransactionItemDto("stx-7", 7, "STX-2026-007", "23 Sep 2026, 09:30 AM", "Farm Vantara", "Vikram Rao", "Price Manipulation", "Medium", "orange", "Closed (No Issue)", "gray"));
        suspiciousTransactionsList.add(new SuspiciousTransactionsPageDto.SuspiciousTransactionItemDto("stx-8", 8, "STX-2026-008", "22 Sep 2026, 05:15 PM", "Atirath Traders", "Ananya Reddy", "Layering", "High", "red", "Under Investigation", "orange"));
        suspiciousTransactionsList.add(new SuspiciousTransactionsPageDto.SuspiciousTransactionItemDto("stx-9", 9, "STX-2026-009", "21 Sep 2026, 01:40 PM", "Atirath Holdings", "Lakshmi Narayan", "Insider Trading", "Low", "green", "Resolved", "green"));
        suspiciousTransactionsList.add(new SuspiciousTransactionsPageDto.SuspiciousTransactionItemDto("stx-10", 10, "STX-2026-010", "20 Sep 2026, 10:25 AM", "Atirath Technologies", "Rakesh Mehta", "Wash Trading", "Medium", "orange", "Pending Info", "blue"));

        SuspiciousTransactionDetailDto stx1 = SuspiciousTransactionDetailDto.builder()
                .id("stx-1")
                .transactionId("STX-2026-001")
                .alertType("Unusual Trading Pattern")
                .dateTime("29 Sep 2026, 11:20 AM")
                .status("Under Investigation")
                .statusTheme("orange")
                .riskLevel("High")
                .riskTheme("red")
                .tabs(List.of("Details", "Alerts", "Investigation", "Related", "History"))
                .activeTab("Details")
                .transactionType("Share Transfer")
                .entity("Atirath Traders")
                .investorOrShareholder("Rajesh Kumar")
                .fromParty("Rajesh Kumar")
                .toParty("Green Future Fund")
                .noOfShares("5,20,000")
                .amount("₹ 5,20,00,000")
                .pricePerShare("100")
                .transactionDate("29 Sep 2026, 11:20 AM")
                .detectionSource("Automated Monitoring")
                .detectionRule("Transaction size > 5x average")
                .detectedOn("29 Sep 2026, 11:25 AM")
                .confidenceScore("92%")
                .additionalNotes("Transaction size is significantly higher than the 3-month average for this investor. Requires detailed review of source of funds and trading intent.")
                .build();

        suspiciousTransactionDetailsMap.put("stx-1", stx1);
    }

    private void seedRestrictedUsers() {
        restrictedUsersList.clear();
        restrictedUsersList.add(new RestrictedUsersPageDto.RestrictedUserItemDto("ru-1", 1, "US-2026-001", "Rakesh Kumar", "Atirath Traders", "Investor", "Trading Restriction", "red", "Insider trading suspicion", "29 Sep 2026", "Active", "red"));
        restrictedUsersList.add(new RestrictedUsersPageDto.RestrictedUserItemDto("ru-2", 2, "US-2026-002", "Priya Mehta", "Farm Vantara", "Shareholder", "Corporate Action Restriction", "orange", "KYC verification pending", "28 Sep 2026", "Active", "red"));
        restrictedUsersList.add(new RestrictedUsersPageDto.RestrictedUserItemDto("ru-3", 3, "US-2026-003", "Suresh Kumar", "Atirath Holdings", "Director", "Trading Restriction", "red", "Regulatory alert", "27 Sep 2026", "Active", "red"));
        restrictedUsersList.add(new RestrictedUsersPageDto.RestrictedUserItemDto("ru-4", 4, "US-2026-004", "Ananya Reddy", "Atirath Technologies", "Employee", "System Access Restriction", "blue", "Policy violation", "26 Sep 2026", "Active", "red"));
        restrictedUsersList.add(new RestrictedUsersPageDto.RestrictedUserItemDto("ru-5", 5, "US-2026-005", "Vikram Rao", "Others", "Investor", "Trading Restriction", "red", "Unusual trading pattern", "25 Sep 2026", "Active", "red"));
        restrictedUsersList.add(new RestrictedUsersPageDto.RestrictedUserItemDto("ru-6", 6, "US-2026-006", "Neha Sharma", "Sunrise Ventures", "Shareholder", "Corporate Action Restriction", "orange", "Sanctions watchlist match", "24 Sep 2026", "Under Review", "orange"));
        restrictedUsersList.add(new RestrictedUsersPageDto.RestrictedUserItemDto("ru-7", 7, "US-2026-007", "Lakshmi Narayan", "Atirath Traders", "Investor", "Temporary Restriction", "blue", "PEP match", "23 Sep 2026", "Active", "red"));
        restrictedUsersList.add(new RestrictedUsersPageDto.RestrictedUserItemDto("ru-8", 8, "US-2026-008", "Rajesh Mehta", "Green Future Fund", "Shareholder", "Permanent Restriction", "red", "Fraudulent activity", "22 Sep 2026", "Active", "red"));
        restrictedUsersList.add(new RestrictedUsersPageDto.RestrictedUserItemDto("ru-9", 9, "US-2026-009", "Sunil Verma", "Farm Vantara", "Employee", "System Access Restriction", "blue", "Multiple failed login attempts", "21 Sep 2026", "Reinstated", "green"));
        restrictedUsersList.add(new RestrictedUsersPageDto.RestrictedUserItemDto("ru-10", 10, "US-2026-010", "Kavya Iyer", "Atirath Holdings", "Director", "Trading Restriction", "red", "Information leakage", "20 Sep 2026", "Active", "red"));

        RestrictedUserDetailDto ru1 = RestrictedUserDetailDto.builder()
                .id("ru-1")
                .userId("US-2026-001")
                .fullName("Rakesh Kumar")
                .initials("RK")
                .role("Investor")
                .entity("Atirath Traders")
                .status("Active")
                .statusTheme("red")
                .restrictedOnDate("29 Sep 2026")
                .tabs(List.of("Details", "Restriction History", "Related Transactions", "Notes"))
                .activeTab("Details")
                .email("rakeshkumar@gmail.com")
                .phone("+91 98765 43210")
                .restrictionType("Trading Restriction")
                .restrictionTypeTheme("red")
                .restrictedOn("29 Sep 2026, 11:20 AM")
                .restrictedBy("Admin User")
                .reason("Insider trading suspicion")
                .duration("6 months (till 29 Mar 2027)")
                .notes("Flagged for unusual trading activity beyond assigned limits.")
                .kycStatus("Verified")
                .pepStatus("No")
                .sanctionsCheck("Clear")
                .build();

        restrictedUserDetailsMap.put("ru-1", ru1);
    }

    private void seedRestrictedShares() {
        restrictedSharesList.clear();
        restrictedSharesList.add(new RestrictedSharesPageDto.RestrictedShareItemDto("rs-1", 1, "RS-2026-001", "Rakesh Kumar", "Atirath Traders", "Equity - A", "2,00,000", "Lock-in", "pink", "01 Jan 2026", "31 Dec 2028", "Active", "red"));
        restrictedSharesList.add(new RestrictedSharesPageDto.RestrictedShareItemDto("rs-2", 2, "RS-2026-002", "Global Agri Fund", "Farm Vantara", "Equity - B", "1,50,000", "Regulatory Hold", "yellow", "15 Mar 2026", "15 Mar 2027", "Active", "red"));
        restrictedSharesList.add(new RestrictedSharesPageDto.RestrictedShareItemDto("rs-3", 3, "RS-2026-003", "Priya Mehta", "Atirath Holdings", "Preference", "1,00,000", "Promoter Lock-in", "orange", "10 Feb 2026", "10 Feb 2028", "Active", "red"));
        restrictedSharesList.add(new RestrictedSharesPageDto.RestrictedShareItemDto("rs-4", 4, "RS-2026-004", "Green Future Fund", "Atirath Technologies", "Equity - A", "75,000", "Litigation Hold", "red", "01 Aug 2026", "Until Resolved", "Active", "red"));
        restrictedSharesList.add(new RestrictedSharesPageDto.RestrictedShareItemDto("rs-5", 5, "RS-2026-005", "Sunrise Ventures", "Farm Vantara", "Equity - B", "50,000", "Employee ESOP", "blue", "01 Apr 2026", "01 Apr 2029", "Active", "red"));
        restrictedSharesList.add(new RestrictedSharesPageDto.RestrictedShareItemDto("rs-6", 6, "RS-2026-006", "Suresh Kumar", "Atirath Holdings", "Equity - A", "1,25,000", "Regulatory Hold", "yellow", "20 Jun 2026", "20 Jun 2027", "Under Review", "orange"));
        restrictedSharesList.add(new RestrictedSharesPageDto.RestrictedShareItemDto("rs-7", 7, "RS-2026-007", "Vikram Rao", "Others", "Equity - C", "80,000", "Promoter Lock-in", "orange", "15 May 2026", "15 May 2028", "Active", "red"));
        restrictedSharesList.add(new RestrictedSharesPageDto.RestrictedShareItemDto("rs-8", 8, "RS-2026-008", "Neha Sharma", "Sunrise Ventures", "Preference", "60,000", "Court Order", "pink", "01 Jul 2026", "Until Resolved", "Active", "red"));
        restrictedSharesList.add(new RestrictedSharesPageDto.RestrictedShareItemDto("rs-9", 9, "RS-2026-009", "Lakshmi Narayan", "Atirath Traders", "Equity - A", "90,000", "Employee ESOP", "blue", "01 Apr 2026", "01 Apr 2029", "Pending", "blue"));
        restrictedSharesList.add(new RestrictedSharesPageDto.RestrictedShareItemDto("rs-10", 10, "RS-2026-010", "Ananya Reddy", "Atirath Technologies", "Equity - B", "70,000", "Regulatory Hold", "yellow", "25 Sep 2026", "25 Sep 2027", "Active", "red"));

        RestrictedShareDetailDto rs1 = RestrictedShareDetailDto.builder()
                .id("rs-1")
                .shareId("RS-2026-001")
                .restrictionTypeTitle("Lock-in Restriction")
                .status("Active")
                .statusTheme("red")
                .createdInfo("Created on 01 Jan 2026, 10:30 AM")
                .tabs(List.of("Details", "History", "Related Transactions", "Notes"))
                .activeTab("Details")
                .entity("Atirath Traders")
                .shareClass("Equity - A")
                .noOfShares("2,00,000")
                .faceValue("10")
                .certificateNo("AT-EC-0001")
                .restrictionType("Lock-in")
                .restrictionTypeTheme("red")
                .fromDate("01 Jan 2026")
                .toDate("31 Dec 2028")
                .reason("Promoter lock-in as per shareholders agreement")
                .applicableTo("Trading, Transfer, Pledge")
                .createdBy("Admin User")
                .createdOn("01 Jan 2026, 10:30 AM")
                .shareholderName("Rakesh Kumar")
                .shareholderType("Individual Investor")
                .pan("AAZPK1234F")
                .build();

        restrictedShareDetailsMap.put("rs-1", rs1);
    }

    private void seedComplianceCases() {
        complianceCasesList.clear();
        complianceCasesList.add(new ComplianceCasesPageDto.ComplianceCaseItemDto("cc-1", 1, "CC-2026-001", "Unusual trading pattern in share transfers", "Atirath Traders", "Market Abuse", "High", "red", "Open", "red", "Rakesh Kumar", "29 Sep 2026", "06 Oct 2026"));
        complianceCasesList.add(new ComplianceCasesPageDto.ComplianceCaseItemDto("cc-2", 2, "CC-2026-002", "KYC verification pending for investor", "Farm Vantara", "KYC/AML", "Medium", "orange", "In Progress", "blue", "Priya Mehta", "28 Sep 2026", "05 Oct 2026"));
        complianceCasesList.add(new ComplianceCasesPageDto.ComplianceCaseItemDto("cc-3", 3, "CC-2026-003", "Possible price manipulation", "Atirath Holdings", "Market Manipulation", "High", "red", "Under Review", "orange", "Suresh Kumar", "27 Sep 2026", "04 Oct 2026"));
        complianceCasesList.add(new ComplianceCasesPageDto.ComplianceCaseItemDto("cc-4", 4, "CC-2026-004", "Restricted user trading activity", "Atirath Technologies", "Policy Violation", "High", "red", "Open", "red", "Ananya Reddy", "26 Sep 2026", "03 Oct 2026"));
        complianceCasesList.add(new ComplianceCasesPageDto.ComplianceCaseItemDto("cc-5", 5, "CC-2026-005", "Sanctions list match", "Green Future Fund", "Sanctions", "Critical", "red", "In Progress", "blue", "Vikram Rao", "25 Sep 2026", "02 Oct 2026"));
        complianceCasesList.add(new ComplianceCasesPageDto.ComplianceCaseItemDto("cc-6", 6, "CC-2026-006", "Share transfer without approval", "Others", "Regulatory Breach", "High", "red", "Open", "red", "Neha Sharma", "24 Sep 2026", "01 Oct 2026"));
        complianceCasesList.add(new ComplianceCasesPageDto.ComplianceCaseItemDto("cc-7", 7, "CC-2026-007", "Insider trading suspicion", "Sunrise Ventures", "Insider Trading", "High", "red", "Under Review", "orange", "Lakshmi Narayan", "23 Sep 2026", "30 Sep 2026"));
        complianceCasesList.add(new ComplianceCasesPageDto.ComplianceCaseItemDto("cc-8", 8, "CC-2026-008", "Ownership limit breach", "Farm Vantara", "Ownership Violation", "Medium", "orange", "In Progress", "blue", "Rajesh Mehta", "22 Sep 2026", "29 Sep 2026"));
        complianceCasesList.add(new ComplianceCasesPageDto.ComplianceCaseItemDto("cc-9", 9, "CC-2026-009", "Document forgery suspicion", "Atirath Traders", "Fraud", "Critical", "red", "Open", "red", "Sunil Verma", "21 Sep 2026", "28 Sep 2026"));
        complianceCasesList.add(new ComplianceCasesPageDto.ComplianceCaseItemDto("cc-10", 10, "CC-2026-010", "Unusual dividend payout", "Atirath Holdings", "Financial Irregularity", "Low", "green", "Resolved", "green", "Kavya Iyer", "20 Sep 2026", "27 Sep 2026"));

        ComplianceCaseDetailDto cc1 = ComplianceCaseDetailDto.builder()
                .id("cc-1")
                .caseId("CC-2026-001")
                .title("Unusual trading pattern in share transfers")
                .status("Open")
                .statusTheme("red")
                .createdInfo("Created On 29 Sep 2026, 11:20 AM")
                .tabs(List.of("Details", "Investigation", "Related Data", "Activity Log"))
                .activeTab("Details")
                .entity("Atirath Traders")
                .caseType("Market Abuse")
                .riskLevel("High")
                .riskTheme("red")
                .assignedTo("Rakesh Kumar")
                .createdOn("29 Sep 2026, 11:20 AM")
                .dueDate("06 Oct 2026")
                .priority("High")
                .priorityTheme("red")
                .source("Automated Monitoring")
                .description("Multiple share transfers detected between related accounts at unusual frequency and volume.")
                .investigationOwner("Rakesh Kumar")
                .startDate("29 Sep 2026")
                .targetCompletion("06 Oct 2026")
                .currentStage("Initial Review")
                .findings("Preliminary indicators suggest potential market manipulation.")
                .riskIndicators(List.of(
                        "Transaction volume 3x higher than average",
                        "Multiple transfers within short time interval",
                        "Related accounts involved",
                        "Price impact observed after transfers"
                ))
                .build();

        complianceCaseDetailsMap.put("cc-1", cc1);
    }
}
