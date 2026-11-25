package com.atirath.trading.modules.admin.subsidiary;

import com.atirath.trading.modules.admin.subsidiary.dto.*;
import com.atirath.trading.modules.admin.subsidiary.service.SubsidiaryComplianceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SubsidiaryComplianceServiceTest {

    private SubsidiaryComplianceService service;

    @BeforeEach
    void setUp() {
        service = new SubsidiaryComplianceService();
        service.init();
    }

    // ==========================================
    // SCREEN 1: COMPLIANCE DASHBOARD
    // ==========================================
    @Test
    @DisplayName("Screen 1: Compliance Dashboard should match KPIs, subsidiary charts, donuts, and tables")
    void testComplianceDashboard() {
        SubsidiaryComplianceDashboardDto dto = service.getComplianceDashboard("Atirath Holdings", "01 Apr 2026 - 30 Sep 2026");

        assertNotNull(dto);
        assertEquals("Compliance Dashboard", dto.getPageTitle());
        assertEquals("Atirath Holdings", dto.getSelectedCompany());
        assertEquals("01 Apr 2026 - 30 Sep 2026", dto.getDateRange());

        // 4 KPIs
        assertEquals(4, dto.getKpis().size());
        assertEquals("5", dto.getKpis().get(0).getValue());
        assertEquals("+25% vs last quarter", dto.getKpis().get(0).getChange());
        assertEquals("342", dto.getKpis().get(1).getValue());
        assertEquals("+18% vs last quarter", dto.getKpis().get(1).getChange());
        assertEquals("12", dto.getKpis().get(2).getValue());
        assertEquals("+50% vs last quarter", dto.getKpis().get(2).getChange());
        assertEquals("5", dto.getKpis().get(3).getValue());
        assertEquals("+25% vs last quarter", dto.getKpis().get(3).getChange());

        // Compliance Status by Subsidiary (5 items)
        assertEquals(5, dto.getComplianceStatusBySubsidiary().size());
        assertEquals("Atirath Holdings", dto.getComplianceStatusBySubsidiary().get(0).getEntityName());
        assertEquals(100, dto.getComplianceStatusBySubsidiary().get(0).getTotal());
        assertEquals(80, dto.getComplianceStatusBySubsidiary().get(1).getTotal());
        assertEquals(60, dto.getComplianceStatusBySubsidiary().get(2).getTotal());
        assertEquals(40, dto.getComplianceStatusBySubsidiary().get(3).getTotal());
        assertEquals(20, dto.getComplianceStatusBySubsidiary().get(4).getTotal());

        // KYC Status Donut (342 total, 4 slices)
        assertNotNull(dto.getKycStatus());
        assertEquals(342, dto.getKycStatus().getTotalInvestors());
        assertEquals(4, dto.getKycStatus().getSlices().size());
        assertEquals("Verified", dto.getKycStatus().getSlices().get(0).getLabel());
        assertEquals(76.3, dto.getKycStatus().getSlices().get(0).getPercentage());
        assertEquals(261, dto.getKycStatus().getSlices().get(0).getCount());

        // Compliance Cases Donut (12 cases, 5 slices)
        assertNotNull(dto.getComplianceCases());
        assertEquals(12, dto.getComplianceCases().getOpenCases());
        assertEquals(5, dto.getComplianceCases().getSlices().size());
        assertEquals("KYC Issues", dto.getComplianceCases().getSlices().get(0).getLabel());
        assertEquals(33.3, dto.getComplianceCases().getSlices().get(0).getPercentage());
        assertEquals(4, dto.getComplianceCases().getSlices().get(0).getCount());

        // 4 Bottom Tables
        assertEquals(5, dto.getRecentComplianceAlerts().size());
        assertEquals("Atirath Traders", dto.getRecentComplianceAlerts().get(0).getEntity());
        assertEquals("Unusual Transaction", dto.getRecentComplianceAlerts().get(0).getType());

        assertEquals(5, dto.getRegulatoryFilingsDue().size());
        assertEquals("Annual Return", dto.getRegulatoryFilingsDue().get(0).getFilingType());

        assertEquals(5, dto.getOpenComplianceCases().size());
        assertEquals("CC-2026-001", dto.getOpenComplianceCases().get(0).getCaseId());

        assertEquals(5, dto.getPendingApprovals().size());
        assertEquals("KYC Approval", dto.getPendingApprovals().get(0).getRequestType());
    }

    // ==========================================
    // SCREEN 2: KYC REVIEW
    // ==========================================
    @Test
    @DisplayName("Screen 2: KYC Review page should match 5 KPIs, tabs, 12 table rows, drawer, and mutations")
    void testKycReviewPage() {
        SubsidiaryKycReviewPageDto dto = service.getKycReviewPage("Atirath Holdings", "All Requests", null, null, null, null);

        assertNotNull(dto);
        assertEquals("KYC Review", dto.getPageTitle());
        assertEquals(342, dto.getTotalRequests());
        assertEquals(29, dto.getTotalPages());

        // 5 KPIs
        assertEquals(5, dto.getKpis().size());
        assertEquals("342", dto.getKpis().get(0).getValue());
        assertEquals("261", dto.getKpis().get(1).getValue());
        assertEquals("53", dto.getKpis().get(2).getValue());
        assertEquals("21", dto.getKpis().get(3).getValue());
        assertEquals("7", dto.getKpis().get(4).getValue());

        // 6 Tabs
        assertEquals(6, dto.getTabs().size());
        assertTrue(dto.getTabs().contains("All Requests"));
        assertTrue(dto.getTabs().contains("Individual Investors"));
        assertTrue(dto.getTabs().contains("Corporate Investors"));

        // 12 Table items
        assertEquals(12, dto.getRequests().size());
        SubsidiaryKycReviewPageDto.KycRequestItemDto row1 = dto.getRequests().get(0);
        assertEquals("KYC-2026-001", row1.getRequestId());
        assertEquals("Rajesh Kumar", row1.getNameOrEntity());
        assertEquals("Individual Investor", row1.getEntityType());
        assertEquals("AAZPK1234F", row1.getPanOrCin());
        assertEquals("29 Sep 2026", row1.getRequestDate());
        assertEquals("Verified", row1.getStatus());
        assertEquals("Low", row1.getRiskLevel());

        SubsidiaryKycReviewPageDto.KycRequestItemDto row5 = dto.getRequests().get(4);
        assertEquals("KYC-2026-005", row5.getRequestId());
        assertEquals("Suresh Kumar", row5.getNameOrEntity());
        assertEquals("Director / KMP", row5.getEntityType());
        assertEquals("ABXPS8765L", row5.getPanOrCin());
        assertEquals("Rejected", row5.getStatus());
        assertEquals("High", row5.getRiskLevel());

        // Selected KYC Drawer (Rajesh Kumar)
        KycDetailDto drawer = dto.getSelectedKycDetail();
        assertNotNull(drawer);
        assertEquals("KYC-2026-001", drawer.getRequestId());
        assertEquals("Rajesh Kumar", drawer.getFullName());
        assertEquals("12 Mar 1995", drawer.getDateOfBirth());
        assertEquals("AAZPK1234F", drawer.getPan());
        assertEquals("9876 5432 1098", drawer.getAadhaar());
        assertEquals("Business", drawer.getOccupation());
        assertEquals("No", drawer.getPepStatus());
        assertEquals("Clear", drawer.getSanctionsCheck());
        assertEquals("No issues found", drawer.getAdverseMedia());

        // Mutation: create KYC
        CreateKycRequestDto createReq = CreateKycRequestDto.builder()
                .fullName("Sunil Gavaskar")
                .entityType("Individual Investor")
                .panOrCin("AAZPG9999K")
                .riskLevel("Low")
                .build();
        KycDetailDto created = service.createKycRequest(createReq);
        assertNotNull(created);
        assertEquals("Sunil Gavaskar", created.getFullName());
        assertEquals("Pending", created.getStatus());

        // Mutation: update status
        KycDetailDto updated = service.updateKycStatus("kyc-1", "Under Review");
        assertEquals("Under Review", updated.getStatus());
    }

    // ==========================================
    // SCREEN 3: COMPLIANCE ALERTS
    // ==========================================
    @Test
    @DisplayName("Screen 3: Compliance Alerts page should match KPIs, tabs, table, drawer, and mutations")
    void testComplianceAlertsPage() {
        SubsidiaryComplianceAlertsPageDto dto = service.getComplianceAlertsPage("Atirath Holdings", "All Alerts", null, null, null);

        assertNotNull(dto);
        assertEquals("Compliance Alerts", dto.getPageTitle());
        assertEquals(36, dto.getTotalAlerts());
        assertEquals(4, dto.getTotalPages());

        // 4 KPIs
        assertEquals(4, dto.getKpis().size());
        assertEquals("36", dto.getKpis().get(0).getValue());
        assertEquals("+28% vs last quarter", dto.getKpis().get(0).getChange());
        assertEquals("8", dto.getKpis().get(1).getValue());
        assertEquals("+60% vs last quarter", dto.getKpis().get(1).getChange());
        assertEquals("14", dto.getKpis().get(2).getValue());
        assertEquals("-12% vs last quarter", dto.getKpis().get(2).getChange());

        // 5 Tabs
        assertEquals(5, dto.getTabs().size());
        assertTrue(dto.getTabs().contains("All Alerts"));
        assertTrue(dto.getTabs().contains("Open Alerts"));
        assertTrue(dto.getTabs().contains("In Progress"));

        // 10 Alerts
        assertEquals(10, dto.getAlerts().size());
        SubsidiaryComplianceAlertsPageDto.ComplianceAlertItemDto row1 = dto.getAlerts().get(0);
        assertEquals("AL-2026-001", row1.getAlertId());
        assertEquals("29 Sep 2026, 11:20 AM", row1.getDateTime());
        assertEquals("Atirath Traders", row1.getEntity());
        assertEquals("Large Share Transfer", row1.getAlertType());
        assertEquals("High", row1.getRiskLevel());
        assertEquals("Open", row1.getStatus());

        // Drawer
        ComplianceAlertDetailDto drawer = dto.getSelectedAlertDetail();
        assertNotNull(drawer);
        assertEquals("AL-2026-001", drawer.getAlertId());
        assertEquals("Large Share Transfer", drawer.getAlertType());
        assertEquals("High", drawer.getRiskLevel());
        assertEquals("Rakesh Mehta", drawer.getInvestor());
        assertEquals("Sunrise Ventures Pvt Ltd", drawer.getFromParty());
        assertEquals("Global Agri Fund", drawer.getToParty());
        assertEquals("5,20,000", drawer.getShares());
        assertEquals("₹ 5,20,00,000", drawer.getAmount());
        assertEquals("TXN-2026-4587", drawer.getReferenceNo());

        // Mutation: create alert
        CreateComplianceAlertRequestDto newAlert = CreateComplianceAlertRequestDto.builder()
                .entity("Atirath Technologies")
                .alertType("Suspicious Transaction")
                .riskLevel("High")
                .description("Unusual trading volume")
                .build();
        ComplianceAlertDetailDto created = service.createComplianceAlert(newAlert);
        assertNotNull(created);
        assertEquals("Atirath Technologies", created.getEntity());

        // Mutation: dismiss alert
        ComplianceAlertDetailDto dismissed = service.dismissComplianceAlert("al-1");
        assertEquals("Dismissed", dismissed.getStatus());

        // Mutation: update status
        ComplianceAlertDetailDto resolved = service.updateAlertStatus("al-1", "Resolved");
        assertEquals("Resolved", resolved.getStatus());
    }

    // ==========================================
    // SCREEN 4: TRANSACTION MONITORING
    // ==========================================
    @Test
    @DisplayName("Screen 4: Transaction Monitoring page should match KPIs, trend, donut, 10 table rows, drawer, and mutations")
    void testTransactionMonitoringPage() {
        SubsidiaryTransactionMonitoringPageDto dto = service.getTransactionMonitoringPage("Atirath Holdings", "All Transactions", null, null, null, null);

        assertNotNull(dto);
        assertEquals("Transaction Monitoring", dto.getPageTitle());
        assertEquals(1248, dto.getTotalTransactions());
        assertEquals(125, dto.getTotalPages());

        // 4 KPIs
        assertEquals(4, dto.getKpis().size());
        assertEquals("1,248", dto.getKpis().get(0).getValue());
        assertEquals("+18% vs last quarter", dto.getKpis().get(0).getChange());
        assertEquals("32", dto.getKpis().get(1).getValue());
        assertEquals("+45% vs last quarter", dto.getKpis().get(1).getChange());
        assertEquals("18", dto.getKpis().get(2).getValue());
        assertEquals("+28% vs last quarter", dto.getKpis().get(2).getChange());
        assertEquals("1,198", dto.getKpis().get(3).getValue());
        assertEquals("+16% vs last quarter", dto.getKpis().get(3).getChange());

        // Trend (6 months)
        assertEquals(6, dto.getTransactionVolumeTrend().size());
        assertEquals("Apr", dto.getTransactionVolumeTrend().get(0).getMonth());
        assertEquals(120, dto.getTransactionVolumeTrend().get(0).getTotalTransactions());
        assertEquals(12, dto.getTransactionVolumeTrend().get(0).getFlaggedTransactions());
        assertEquals("Sep", dto.getTransactionVolumeTrend().get(5).getMonth());
        assertEquals(170, dto.getTransactionVolumeTrend().get(5).getTotalTransactions());
        assertEquals(20, dto.getTransactionVolumeTrend().get(5).getFlaggedTransactions());

        // Donut (1,248 total, 6 slices)
        assertNotNull(dto.getTransactionTypes());
        assertEquals(1248, dto.getTransactionTypes().getTotalTransactions());
        assertEquals(6, dto.getTransactionTypes().getSlices().size());
        assertEquals("Share Transfer", dto.getTransactionTypes().getSlices().get(0).getLabel());
        assertEquals(40.0, dto.getTransactionTypes().getSlices().get(0).getPercentage());
        assertEquals(499, dto.getTransactionTypes().getSlices().get(0).getCount());

        // 10 Table rows
        assertEquals(10, dto.getTransactions().size());
        SubsidiaryTransactionMonitoringPageDto.MonitoredTransactionItemDto row1 = dto.getTransactions().get(0);
        assertEquals("TXN-2026-001", row1.getTransactionId());
        assertEquals("Atirath Traders", row1.getEntity());
        assertEquals("Share Transfer", row1.getType());
        assertEquals("Rajesh Kumar", row1.getInvestorOrShareholder());
        assertEquals("5,20,00,000", row1.getAmount());
        assertEquals("High", row1.getRiskLevel());
        assertEquals("Flagged", row1.getStatus());

        SubsidiaryTransactionMonitoringPageDto.MonitoredTransactionItemDto row5 = dto.getTransactions().get(4);
        assertEquals("TXN-2026-005", row5.getTransactionId());
        assertEquals("Medium", row5.getRiskLevel());
        assertEquals("Under Review", row5.getStatus());

        // Drawer
        MonitoredTransactionDetailDto drawer = dto.getSelectedTransactionDetail();
        assertNotNull(drawer);
        assertEquals("TXN-2026-001", drawer.getTransactionId());
        assertEquals("Rajesh Kumar", drawer.getFromParty());
        assertEquals("Green Future Fund", drawer.getToParty());
        assertEquals("5,20,000", drawer.getNoOfShares());
        assertEquals("₹ 5,20,00,000", drawer.getAmount());
        assertEquals("100", drawer.getPricePerShare());
        assertEquals("Match Found", drawer.getSanctionsCheck());
        assertEquals("Exceeds Limit", drawer.getShareholdingLimit());
        assertEquals("Detected", drawer.getUnusualPattern());

        // Mutation: run screening
        Map<String, Object> screening = service.runScreening(new RunTransactionScreeningRequestDto());
        assertNotNull(screening);
        assertEquals("COMPLETED", screening.get("status"));
        assertEquals(1248, screening.get("screenedTransactionsCount"));

        // Mutation: clear transaction
        MonitoredTransactionDetailDto cleared = service.clearTransaction("mtxn-1");
        assertEquals("Cleared", cleared.getStatus());
    }

    // ==========================================
    // SCREEN 5: SUSPICIOUS TRANSACTIONS
    // ==========================================
    @Test
    @DisplayName("Screen 5: Suspicious Transactions page should match 4 KPIs, tabs, charts, 10 table rows, drawer, and mutations")
    void testSuspiciousTransactionsPage() {
        SuspiciousTransactionsPageDto dto = service.getSuspiciousTransactionsPage("Atirath Holdings", "All", null, null, null, null);

        assertNotNull(dto);
        assertEquals("Suspicious Transactions", dto.getPageTitle());
        assertEquals(32, dto.getTotalTransactions());
        assertEquals(4, dto.getTotalPages());

        // 4 KPIs
        assertEquals(4, dto.getKpis().size());
        assertEquals("32", dto.getKpis().get(0).getValue());
        assertEquals("+45% vs last quarter", dto.getKpis().get(0).getChange());
        assertEquals("14", dto.getKpis().get(1).getValue());
        assertEquals("+27% vs last quarter", dto.getKpis().get(1).getChange());
        assertEquals("12", dto.getKpis().get(2).getValue());
        assertEquals("+33% vs last quarter", dto.getKpis().get(2).getChange());
        assertEquals("6", dto.getKpis().get(3).getValue());
        assertEquals("-14% vs last quarter", dto.getKpis().get(3).getChange());

        // Tabs
        assertEquals(5, dto.getTabs().size());
        assertTrue(dto.getTabs().contains("All"));
        assertTrue(dto.getTabs().contains("Under Investigation"));
        assertTrue(dto.getTabs().contains("Pending Information"));

        // Trend chart (6 months)
        assertEquals(6, dto.getTrend().size());
        assertEquals("Apr", dto.getTrend().get(0).getMonth());
        assertEquals(12, dto.getTrend().get(0).getTotal());
        assertEquals("Sep", dto.getTrend().get(5).getMonth());
        assertEquals(14, dto.getTrend().get(5).getTotal());

        // Donut
        assertNotNull(dto.getAlertTypeDistribution());
        assertEquals(32, dto.getAlertTypeDistribution().getTotalFlaggedTransactions());
        assertEquals(6, dto.getAlertTypeDistribution().getSlices().size());
        assertEquals("Unusual Trading Pattern", dto.getAlertTypeDistribution().getSlices().get(0).getLabel());
        assertEquals(31.3, dto.getAlertTypeDistribution().getSlices().get(0).getPercentage());
        assertEquals(10, dto.getAlertTypeDistribution().getSlices().get(0).getCount());

        // 10 Table rows
        assertEquals(10, dto.getTransactions().size());
        SuspiciousTransactionsPageDto.SuspiciousTransactionItemDto row1 = dto.getTransactions().get(0);
        assertEquals("STX-2026-001", row1.getTransactionId());
        assertEquals("29 Sep 2026, 11:20 AM", row1.getDateTime());
        assertEquals("Atirath Traders", row1.getEntity());
        assertEquals("Rajesh Kumar", row1.getInvestorOrShareholder());
        assertEquals("Unusual Trading Pattern", row1.getAlertType());
        assertEquals("High", row1.getRiskLevel());
        assertEquals("Under Investigation", row1.getStatus());

        // Drawer
        SuspiciousTransactionDetailDto drawer = dto.getSelectedTransactionDetail();
        assertNotNull(drawer);
        assertEquals("STX-2026-001", drawer.getTransactionId());
        assertEquals("Share Transfer", drawer.getTransactionType());
        assertEquals("Rajesh Kumar", drawer.getFromParty());
        assertEquals("Green Future Fund", drawer.getToParty());
        assertEquals("5,20,000", drawer.getNoOfShares());
        assertEquals("₹ 5,20,00,000", drawer.getAmount());
        assertEquals("100", drawer.getPricePerShare());
        assertEquals("92%", drawer.getConfidenceScore());

        // Mutation: flag suspicious transaction
        FlagSuspiciousTransactionRequestDto flagReq = FlagSuspiciousTransactionRequestDto.builder()
                .entity("Atirath Traders")
                .investorOrShareholder("Sunil Gavaskar")
                .alertType("Layering")
                .riskLevel("High")
                .build();
        SuspiciousTransactionDetailDto created = service.flagSuspiciousTransaction(flagReq);
        assertNotNull(created);
        assertEquals("Sunil Gavaskar", created.getInvestorOrShareholder());

        // Mutation: update status
        SuspiciousTransactionDetailDto resolved = service.updateSuspiciousTransactionStatus("stx-1", "Resolved");
        assertEquals("Resolved", resolved.getStatus());
    }

    // ==========================================
    // SCREEN 6: RESTRICTED USERS
    // ==========================================
    @Test
    @DisplayName("Screen 6: Restricted Users page should match 4 KPIs, tabs, 10 table rows, drawer, and mutations")
    void testRestrictedUsersPage() {
        RestrictedUsersPageDto dto = service.getRestrictedUsersPage("Atirath Holdings", "All Users", null, null, null, null);

        assertNotNull(dto);
        assertEquals("Restricted Users", dto.getPageTitle());
        assertEquals(18, dto.getTotalUsers());
        assertEquals(2, dto.getTotalPages());

        // 4 KPIs
        assertEquals(4, dto.getKpis().size());
        assertEquals("18", dto.getKpis().get(0).getValue());
        assertEquals("+50% vs last quarter", dto.getKpis().get(0).getChange());
        assertEquals("12", dto.getKpis().get(1).getValue());
        assertEquals("+33% vs last quarter", dto.getKpis().get(1).getChange());
        assertEquals("6", dto.getKpis().get(2).getValue());
        assertEquals("+200% vs last quarter", dto.getKpis().get(2).getChange());
        assertEquals("4", dto.getKpis().get(3).getValue());
        assertEquals("+100% vs last quarter", dto.getKpis().get(3).getChange());

        // Tabs
        assertEquals(4, dto.getTabs().size());
        assertTrue(dto.getTabs().contains("All Users"));
        assertTrue(dto.getTabs().contains("Temporary Restriction"));
        assertTrue(dto.getTabs().contains("Permanent Restriction"));
        assertTrue(dto.getTabs().contains("Reinstated Users"));

        // 10 Table rows
        assertEquals(10, dto.getUsers().size());
        RestrictedUsersPageDto.RestrictedUserItemDto row1 = dto.getUsers().get(0);
        assertEquals("US-2026-001", row1.getUserId());
        assertEquals("Rakesh Kumar", row1.getName());
        assertEquals("Atirath Traders", row1.getEntity());
        assertEquals("Investor", row1.getRole());
        assertEquals("Trading Restriction", row1.getRestrictionType());
        assertEquals("Insider trading suspicion", row1.getReason());
        assertEquals("Active", row1.getStatus());

        // Drawer
        RestrictedUserDetailDto drawer = dto.getSelectedUserDetail();
        assertNotNull(drawer);
        assertEquals("US-2026-001", drawer.getUserId());
        assertEquals("Rakesh Kumar", drawer.getFullName());
        assertEquals("rakeshkumar@gmail.com", drawer.getEmail());
        assertEquals("+91 98765 43210", drawer.getPhone());
        assertEquals("Trading Restriction", drawer.getRestrictionType());
        assertEquals("6 months (till 29 Mar 2027)", drawer.getDuration());
        assertEquals("Verified", drawer.getKycStatus());
        assertEquals("No", drawer.getPepStatus());
        assertEquals("Clear", drawer.getSanctionsCheck());

        // Mutation: restrict user
        RestrictUserRequestDto restrictReq = RestrictUserRequestDto.builder()
                .fullName("Kapil Dev")
                .entity("Farm Vantara")
                .role("Investor")
                .restrictionType("Trading Restriction")
                .reason("Compliance violation")
                .build();
        RestrictedUserDetailDto created = service.restrictUser(restrictReq);
        assertNotNull(created);
        assertEquals("Kapil Dev", created.getFullName());

        // Mutation: lift restriction
        RestrictedUserDetailDto reinstated = service.liftUserRestriction("ru-1");
        assertEquals("Reinstated", reinstated.getStatus());
    }

    // ==========================================
    // SCREEN 7: RESTRICTED SHARES
    // ==========================================
    @Test
    @DisplayName("Screen 7: Restricted Shares page should match 4 KPIs, donut, timeline, 10 table rows, drawer, and mutations")
    void testRestrictedSharesPage() {
        RestrictedSharesPageDto dto = service.getRestrictedSharesPage("Atirath Holdings", "All Shares", null, null, null, null, null);

        assertNotNull(dto);
        assertEquals("Restricted Shares", dto.getPageTitle());
        assertEquals(24, dto.getTotalRestrictedSharesCount());
        assertEquals(3, dto.getTotalPages());

        // 4 KPIs
        assertEquals(4, dto.getKpis().size());
        assertEquals("12,00,000", dto.getKpis().get(0).getValue());
        assertEquals("+20% vs last quarter", dto.getKpis().get(0).getChange());
        assertEquals("7,50,000", dto.getKpis().get(1).getValue());
        assertEquals("+15% vs last quarter", dto.getKpis().get(1).getChange());
        assertEquals("4,50,000", dto.getKpis().get(2).getValue());
        assertEquals("+50% vs last quarter", dto.getKpis().get(2).getChange());
        assertEquals("1,20,000", dto.getKpis().get(3).getValue());
        assertEquals("-25% vs last quarter", dto.getKpis().get(3).getChange());

        // Donut
        assertNotNull(dto.getRestrictedSharesByType());
        assertEquals("12,00,000 Restricted Shares", dto.getRestrictedSharesByType().getTotalRestrictedShares());
        assertEquals(6, dto.getRestrictedSharesByType().getSlices().size());
        assertEquals("Lock-in", dto.getRestrictedSharesByType().getSlices().get(0).getLabel());
        assertEquals(33.3, dto.getRestrictedSharesByType().getSlices().get(0).getPercentage());
        assertEquals("4,00,000", dto.getRestrictedSharesByType().getSlices().get(0).getCount());

        // Timeline
        assertEquals(6, dto.getRestrictionExpiryTimeline().size());
        assertEquals("Oct 2026", dto.getRestrictionExpiryTimeline().get(0).getMonth());

        // 10 Table rows
        assertEquals(10, dto.getShares().size());
        RestrictedSharesPageDto.RestrictedShareItemDto row1 = dto.getShares().get(0);
        assertEquals("RS-2026-001", row1.getShareId());
        assertEquals("Rakesh Kumar", row1.getShareholder());
        assertEquals("Atirath Traders", row1.getEntity());
        assertEquals("Equity - A", row1.getShareClass());
        assertEquals("2,00,000", row1.getNoOfShares());
        assertEquals("Lock-in", row1.getRestrictionType());
        assertEquals("01 Jan 2026", row1.getFromDate());
        assertEquals("31 Dec 2028", row1.getToDate());
        assertEquals("Active", row1.getStatus());

        // Drawer
        RestrictedShareDetailDto drawer = dto.getSelectedShareDetail();
        assertNotNull(drawer);
        assertEquals("RS-2026-001", drawer.getShareId());
        assertEquals("Lock-in Restriction", drawer.getRestrictionTypeTitle());
        assertEquals("2,00,000", drawer.getNoOfShares());
        assertEquals("10", drawer.getFaceValue());
        assertEquals("AT-EC-0001", drawer.getCertificateNo());
        assertEquals("Rakesh Kumar", drawer.getShareholderName());
        assertEquals("AAZPK1234F", drawer.getPan());

        // Mutation: restrict shares
        RestrictSharesRequestDto restrictReq = RestrictSharesRequestDto.builder()
                .shareholder("Sachin Tendulkar")
                .entity("Atirath Holdings")
                .shareClass("Equity - A")
                .noOfShares("1,00,000")
                .restrictionType("Lock-in")
                .build();
        RestrictedShareDetailDto created = service.restrictShares(restrictReq);
        assertNotNull(created);
        assertEquals("Sachin Tendulkar", created.getShareholderName());

        // Mutation: lift restriction
        RestrictedShareDetailDto lifted = service.liftShareRestriction("rs-1");
        assertEquals("Lifted", lifted.getStatus());
    }

    // ==========================================
    // SCREEN 8: COMPLIANCE CASES
    // ==========================================
    @Test
    @DisplayName("Screen 8: Compliance Cases page should match 4 KPIs, donut, trend, 10 table rows, drawer, and mutations")
    void testComplianceCasesPage() {
        ComplianceCasesPageDto dto = service.getComplianceCasesPage("Atirath Holdings", "All Cases", null, null, null, null, null);

        assertNotNull(dto);
        assertEquals("Compliance Cases", dto.getPageTitle());
        assertEquals(24, dto.getTotalCases());
        assertEquals(3, dto.getTotalPages());

        // 4 KPIs
        assertEquals(4, dto.getKpis().size());
        assertEquals("24", dto.getKpis().get(0).getValue());
        assertEquals("+33% vs last quarter", dto.getKpis().get(0).getChange());
        assertEquals("12", dto.getKpis().get(1).getValue());
        assertEquals("+50% vs last quarter", dto.getKpis().get(1).getChange());
        assertEquals("6", dto.getKpis().get(2).getValue());
        assertEquals("+20% vs last quarter", dto.getKpis().get(2).getChange());
        assertEquals("6", dto.getKpis().get(3).getValue());
        assertEquals("-25% vs last quarter", dto.getKpis().get(3).getChange());

        // Tabs
        assertEquals(6, dto.getTabs().size());
        assertTrue(dto.getTabs().contains("All Cases"));
        assertTrue(dto.getTabs().contains("Open"));
        assertTrue(dto.getTabs().contains("In Progress"));

        // Donut
        assertNotNull(dto.getCasesByType());
        assertEquals(24, dto.getCasesByType().getTotalCases());
        assertEquals(6, dto.getCasesByType().getSlices().size());
        assertEquals("Market Abuse", dto.getCasesByType().getSlices().get(0).getLabel());
        assertEquals(25.0, dto.getCasesByType().getSlices().get(0).getPercentage());
        assertEquals(6, dto.getCasesByType().getSlices().get(0).getCount());

        // Trend
        assertEquals(6, dto.getCaseStatusTrend().size());
        assertEquals("Apr", dto.getCaseStatusTrend().get(0).getMonth());

        // 10 Table rows
        assertEquals(10, dto.getCases().size());
        ComplianceCasesPageDto.ComplianceCaseItemDto row1 = dto.getCases().get(0);
        assertEquals("CC-2026-001", row1.getCaseId());
        assertEquals("Unusual trading pattern in share transfers", row1.getTitle());
        assertEquals("Atirath Traders", row1.getEntity());
        assertEquals("Market Abuse", row1.getCaseType());
        assertEquals("High", row1.getRiskLevel());
        assertEquals("Open", row1.getStatus());
        assertEquals("Rakesh Kumar", row1.getAssignedTo());

        ComplianceCasesPageDto.ComplianceCaseItemDto row6 = dto.getCases().get(5);
        assertEquals("CC-2026-006", row6.getCaseId());
        assertEquals("High", row6.getRiskLevel());

        // Drawer
        ComplianceCaseDetailDto drawer = dto.getSelectedCaseDetail();
        assertNotNull(drawer);
        assertEquals("CC-2026-001", drawer.getCaseId());
        assertEquals("Unusual trading pattern in share transfers", drawer.getTitle());
        assertEquals("Market Abuse", drawer.getCaseType());
        assertEquals("High", drawer.getRiskLevel());
        assertEquals("Open", drawer.getStatus());
        assertEquals("Rakesh Kumar", drawer.getAssignedTo());
        assertEquals("Initial Review", drawer.getCurrentStage());

        // Mutation: create compliance case
        CreateComplianceCaseRequestDto caseReq = CreateComplianceCaseRequestDto.builder()
                .title("New Forgery Inquiry")
                .entity("Atirath Traders")
                .caseType("Fraud")
                .riskLevel("Critical")
                .assignedTo("Priya Mehta")
                .build();
        ComplianceCaseDetailDto created = service.createComplianceCase(caseReq);
        assertNotNull(created);
        assertEquals("New Forgery Inquiry", created.getTitle());

        // Mutation: update status
        ComplianceCaseDetailDto closed = service.updateComplianceCaseStatus("cc-1", "Closed");
        assertEquals("Closed", closed.getStatus());
    }
}
