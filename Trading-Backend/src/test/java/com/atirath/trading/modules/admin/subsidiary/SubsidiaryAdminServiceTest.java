package com.atirath.trading.modules.admin.subsidiary;

import com.atirath.trading.modules.admin.subsidiary.dto.*;
import com.atirath.trading.modules.admin.subsidiary.service.SubsidiaryAdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class SubsidiaryAdminServiceTest {

    private SubsidiaryAdminService subsidiaryAdminService;

    @BeforeEach
    void setUp() {
        subsidiaryAdminService = new SubsidiaryAdminService();
        subsidiaryAdminService.init();
    }

    // ==========================================
    // SCREEN 1: SUBSIDIARY DASHBOARD
    // ==========================================
    @Test
    @DisplayName("Screen 1: Verify Subsidiary Dashboard matches screen design")
    void testGetSubsidiaryDashboard() {
        SubsidiaryAdminDashboardDto dashboard = subsidiaryAdminService.getSubsidiaryDashboard("All Subsidiaries", "01 Sep 2026 - 16 Sep 2026");

        assertNotNull(dashboard);
        assertEquals("Subsidiary Dashboard", dashboard.getPageTitle());
        assertEquals("Overview of subsidiary operations, performance, compliance and key activities.", dashboard.getPageSubtitle());
        assertEquals("01 Sep 2026 - 16 Sep 2026", dashboard.getDateRange());
        assertEquals("All Subsidiaries", dashboard.getSelectedSubsidiary());

        // 5 KPI Cards
        assertNotNull(dashboard.getKpis());
        assertEquals(5, dashboard.getKpis().size());
        assertEquals("Total Subsidiaries", dashboard.getKpis().get(0).getTitle());
        assertEquals("8", dashboard.getKpis().get(0).getValue());
        assertEquals("+14%", dashboard.getKpis().get(0).getChange());
        assertEquals("Active Projects", dashboard.getKpis().get(1).getTitle());
        assertEquals("24", dashboard.getKpis().get(1).getValue());
        assertEquals("+26%", dashboard.getKpis().get(1).getChange());
        assertEquals("Total Employees", dashboard.getKpis().get(2).getTitle());
        assertEquals("524", dashboard.getKpis().get(2).getValue());
        assertEquals("+12%", dashboard.getKpis().get(2).getChange());
        assertEquals("Total Investment", dashboard.getKpis().get(3).getTitle());
        assertEquals("₹ 186.4 Cr", dashboard.getKpis().get(3).getValue());
        assertEquals("+22%", dashboard.getKpis().get(3).getChange());
        assertEquals("Total Revenue (YTD)", dashboard.getKpis().get(4).getTitle());
        assertEquals("₹ 124.8 Cr", dashboard.getKpis().get(4).getValue());
        assertEquals("+18%", dashboard.getKpis().get(4).getChange());

        // Performance Overview: 8 companies
        assertNotNull(dashboard.getPerformanceOverview());
        assertEquals(8, dashboard.getPerformanceOverview().size());

        SubsidiaryAdminDashboardDto.SubsidiaryPerformanceItemDto row1 = dashboard.getPerformanceOverview().get(0);
        assertEquals("Atirath Holdings", row1.getSubsidiaryName());
        assertEquals("Renewable Energy", row1.getSector());
        assertEquals(6, row1.getActiveProjects());
        assertEquals(120, row1.getEmployees());
        assertEquals(52.9, row1.getInvestmentCr());
        assertEquals(36.8, row1.getRevenueCr());
        assertEquals("Active", row1.getStatus());

        SubsidiaryAdminDashboardDto.SubsidiaryPerformanceItemDto row8 = dashboard.getPerformanceOverview().get(7);
        assertEquals("NEU1 Smart Stay", row8.getSubsidiaryName());
        assertEquals("Hospitality", row8.getSector());
        assertEquals("Planning", row8.getStatus());

        // Projects Status Donut: 24 total
        assertNotNull(dashboard.getProjectsStatus());
        assertEquals(24, dashboard.getProjectsStatus().getTotalProjects());
        assertEquals(5, dashboard.getProjectsStatus().getSlices().size());
        assertEquals("Open", dashboard.getProjectsStatus().getSlices().get(0).getStatus());
        assertEquals(6, dashboard.getProjectsStatus().getSlices().get(0).getCount());
        assertEquals(25.0, dashboard.getProjectsStatus().getSlices().get(0).getPercentage());
        assertEquals("In Progress", dashboard.getProjectsStatus().getSlices().get(1).getStatus());
        assertEquals(10, dashboard.getProjectsStatus().getSlices().get(1).getCount());
        assertEquals(41.7, dashboard.getProjectsStatus().getSlices().get(1).getPercentage());

        // Compliance Status: 4 categories
        assertNotNull(dashboard.getComplianceStatus());
        assertEquals(4, dashboard.getComplianceStatus().size());
        assertEquals("Statutory Compliance", dashboard.getComplianceStatus().get(0).getCategory());
        assertEquals(92, dashboard.getComplianceStatus().get(0).getPercentage());
        assertEquals("Environmental", dashboard.getComplianceStatus().get(1).getCategory());
        assertEquals(78, dashboard.getComplianceStatus().get(1).getPercentage());
        assertEquals("Financial Reporting", dashboard.getComplianceStatus().get(2).getCategory());
        assertEquals(85, dashboard.getComplianceStatus().get(2).getPercentage());
        assertEquals("Regulatory Filings", dashboard.getComplianceStatus().get(3).getCategory());
        assertEquals(88, dashboard.getComplianceStatus().get(3).getPercentage());

        // Investment vs Revenue Trend: 6 months
        assertNotNull(dashboard.getInvestmentVsRevenueTrend());
        assertEquals(6, dashboard.getInvestmentVsRevenueTrend().size());
        assertEquals("Apr", dashboard.getInvestmentVsRevenueTrend().get(0).getMonth());
        assertEquals(18.2, dashboard.getInvestmentVsRevenueTrend().get(0).getInvestment());
        assertEquals(12.0, dashboard.getInvestmentVsRevenueTrend().get(0).getRevenue());
        assertEquals("Sep", dashboard.getInvestmentVsRevenueTrend().get(5).getMonth());
        assertEquals(45.3, dashboard.getInvestmentVsRevenueTrend().get(5).getInvestment());

        // Employee Distribution Donut: 524 employees, 6 departments
        assertNotNull(dashboard.getEmployeeDistribution());
        assertEquals(524, dashboard.getEmployeeDistribution().getTotalEmployees());
        assertEquals(6, dashboard.getEmployeeDistribution().getSlices().size());
        assertEquals("Management", dashboard.getEmployeeDistribution().getSlices().get(0).getDepartment());
        assertEquals(48, dashboard.getEmployeeDistribution().getSlices().get(0).getCount());
        assertEquals(9.2, dashboard.getEmployeeDistribution().getSlices().get(0).getPercentage());
        assertEquals("Engineering", dashboard.getEmployeeDistribution().getSlices().get(1).getDepartment());
        assertEquals(186, dashboard.getEmployeeDistribution().getSlices().get(1).getCount());
        assertEquals(35.5, dashboard.getEmployeeDistribution().getSlices().get(1).getPercentage());

        // Recent Activities: 5 items
        assertNotNull(dashboard.getRecentActivities());
        assertEquals(5, dashboard.getRecentActivities().size());
        assertEquals("Farm Vantara", dashboard.getRecentActivities().get(0).getSubsidiary());
        assertEquals("New project created - AP Expansion", dashboard.getRecentActivities().get(0).getActivity());
        assertEquals("Ramesh Kumar", dashboard.getRecentActivities().get(0).getUser());

        // Upcoming Milestones: 5 items
        assertNotNull(dashboard.getUpcomingMilestones());
        assertEquals(5, dashboard.getUpcomingMilestones().size());
        assertEquals("Farm Vantara", dashboard.getUpcomingMilestones().get(0).getSubsidiary());
        assertEquals("Land Acquisition Approval", dashboard.getUpcomingMilestones().get(0).getMilestone());
        assertEquals("Due Soon", dashboard.getUpcomingMilestones().get(0).getStatus());
    }

    @Test
    @DisplayName("Screen 1: Verify Subsidiary Dashboard filtering by subsidiary name")
    void testGetSubsidiaryDashboardWithFilter() {
        SubsidiaryAdminDashboardDto filtered = subsidiaryAdminService.getSubsidiaryDashboard("Farm Vantara", "01 Sep 2026 - 16 Sep 2026");

        assertNotNull(filtered);
        assertEquals(1, filtered.getPerformanceOverview().size());
        assertEquals("Farm Vantara", filtered.getPerformanceOverview().get(0).getSubsidiaryName());
    }

    // ==========================================
    // SCREEN 2: SUBSIDIARY COMPANY PROFILE
    // ==========================================
    @Test
    @DisplayName("Screen 2: Verify Subsidiary Company Profile matches screen design")
    void testGetCompanyProfile() {
        SubsidiaryCompanyProfileDto profile = subsidiaryAdminService.getCompanyProfile("Atirath Holdings");

        assertNotNull(profile);
        assertEquals("Subsidiary Company Profile", profile.getPageTitle());
        assertEquals("Atirath Holdings", profile.getSelectedCompany());
        assertEquals("Active", profile.getStatus());

        // Hero Details
        assertNotNull(profile.getHero());
        assertEquals("Atirath Holdings India Limited", profile.getHero().getCompanyName());
        assertEquals("Active", profile.getHero().getStatus());
        assertEquals("U40109TG2020PLC138765", profile.getHero().getCin());
        assertEquals("15 Jan 2020", profile.getHero().getIncorporationDate());
        assertEquals("EPC company for CBG plants from Napier Grass, residues and organic waste.", profile.getHero().getDescription());
        assertEquals("Public Limited", profile.getHero().getCompanyType());
        assertEquals("Renewable Energy", profile.getHero().getIndustry());
        assertEquals("Hyderabad, Telangana", profile.getHero().getHeadOffice());
        assertEquals("Hyderabad, Telangana", profile.getHero().getRegisteredOffice());
        assertEquals("www.atirathholdings.in", profile.getHero().getWebsite());

        // 11 Tabs
        assertNotNull(profile.getTabs());
        assertEquals(11, profile.getTabs().size());
        assertEquals("Overview", profile.getActiveTab());

        // 5 Overview KPIs
        assertNotNull(profile.getKpis());
        assertEquals(5, profile.getKpis().size());
        assertEquals("Total Projects", profile.getKpis().get(0).getTitle());
        assertEquals("24", profile.getKpis().get(0).getValue());
        assertEquals("Total Plants", profile.getKpis().get(1).getTitle());
        assertEquals("5", profile.getKpis().get(1).getValue());
        assertEquals("Land Area (Acres)", profile.getKpis().get(2).getTitle());
        assertEquals("2,450", profile.getKpis().get(2).getValue());
        assertEquals("Total Employees", profile.getKpis().get(3).getTitle());
        assertEquals("524", profile.getKpis().get(3).getValue());
        assertEquals("Total Revenue (YTD)", profile.getKpis().get(4).getTitle());
        assertEquals("₹ 124.8 Cr", profile.getKpis().get(4).getValue());

        // Company Information Card
        assertNotNull(profile.getCompanyInformation());
        assertEquals("Atirath Holdings India Limited", profile.getCompanyInformation().getCompanyName());
        assertEquals("U40109TG2020PLC138765", profile.getCompanyInformation().getCin());
        assertEquals("AAECA1234D", profile.getCompanyInformation().getPan());
        assertEquals("36AAECA1234D1Z5", profile.getCompanyInformation().getGstin());
        assertEquals("15 Jan 2020", profile.getCompanyInformation().getIncorporationDate());
        assertEquals("Public Limited", profile.getCompanyInformation().getCompanyType());
        assertEquals("Renewable Energy", profile.getCompanyInformation().getIndustry());
        assertEquals("₹ 500.00 Cr", profile.getCompanyInformation().getAuthorisedCapital());
        assertEquals("₹ 320.50 Cr", profile.getCompanyInformation().getPaidUpCapital());
        assertEquals("info@atirathholdings.in", profile.getCompanyInformation().getEmail());
        assertEquals("+91 40 1234 5678", profile.getCompanyInformation().getPhone());

        // Shareholding Pattern Donut (5 slices, 100% total)
        assertNotNull(profile.getShareholdingPattern());
        assertEquals("100% Total Shares", profile.getShareholdingPattern().getTotalShares());
        assertEquals(5, profile.getShareholdingPattern().getSlices().size());
        assertEquals("Atirath Group (Promoter)", profile.getShareholdingPattern().getSlices().get(0).getCategory());
        assertEquals(62.5, profile.getShareholdingPattern().getSlices().get(0).getPercentage());
        assertEquals("Strategic Investors", profile.getShareholdingPattern().getSlices().get(1).getCategory());
        assertEquals(18.4, profile.getShareholdingPattern().getSlices().get(1).getPercentage());
        assertEquals("Institutional Investors", profile.getShareholdingPattern().getSlices().get(2).getCategory());
        assertEquals(12.1, profile.getShareholdingPattern().getSlices().get(2).getPercentage());
        assertEquals("Retail Investors", profile.getShareholdingPattern().getSlices().get(3).getCategory());
        assertEquals(5.0, profile.getShareholdingPattern().getSlices().get(3).getPercentage());
        assertEquals("Others", profile.getShareholdingPattern().getSlices().get(4).getCategory());
        assertEquals(2.0, profile.getShareholdingPattern().getSlices().get(4).getPercentage());

        // Key Management: 4 officers
        assertNotNull(profile.getKeyManagement());
        assertEquals(4, profile.getKeyManagement().size());
        assertEquals("Ramesh Kumar", profile.getKeyManagement().get(0).getName());
        assertEquals("Managing Director", profile.getKeyManagement().get(0).getDesignation());
        assertEquals("Priya Verma", profile.getKeyManagement().get(1).getName());
        assertEquals("Chief Financial Officer", profile.getKeyManagement().get(1).getDesignation());

        // Financial Summary: 4 years
        assertNotNull(profile.getFinancialSummary());
        assertEquals(4, profile.getFinancialSummary().size());
        assertEquals("2022", profile.getFinancialSummary().get(0).getYear());
        assertEquals(68.4, profile.getFinancialSummary().get(0).getRevenue());
        assertEquals("2025 (YTD)", profile.getFinancialSummary().get(3).getYear());
        assertEquals(124.8, profile.getFinancialSummary().get(3).getRevenue());

        // Recent Activities: 5 items
        assertNotNull(profile.getRecentActivities());
        assertEquals(5, profile.getRecentActivities().size());
        assertEquals("Company details updated", profile.getRecentActivities().get(0).getActivity());
    }

    @Test
    @DisplayName("Screen 2: Verify update subsidiary company profile")
    void testUpdateCompanyProfile() {
        UpdateSubsidiaryCompanyProfileDto updateReq = UpdateSubsidiaryCompanyProfileDto.builder()
                .companyName("Atirath Holdings India Ltd")
                .description("Updated premier EPC leader in bio-energy solutions")
                .paidUpCapital("₹ 330.00 Cr")
                .website("www.atirathholdings-updated.in")
                .build();

        SubsidiaryCompanyProfileDto updated = subsidiaryAdminService.updateCompanyProfile("Atirath Holdings", updateReq);

        assertNotNull(updated);
        assertEquals("Atirath Holdings India Ltd", updated.getHero().getCompanyName());
        assertEquals("Updated premier EPC leader in bio-energy solutions", updated.getHero().getDescription());
        assertEquals("₹ 330.00 Cr", updated.getCompanyInformation().getPaidUpCapital());
        assertEquals("www.atirathholdings-updated.in", updated.getHero().getWebsite());
    }

    // ==========================================
    // SCREEN 3: SHARE CLASSES
    // ==========================================
    @Test
    @DisplayName("Screen 3: Verify Share Classes Page matches screen design")
    void testGetShareClassesPage() {
        SubsidiaryShareClassesPageDto page = subsidiaryAdminService.getShareClassesPage("Atirath Holdings", null, null);

        assertNotNull(page);
        assertEquals("Share Classes", page.getPageTitle());
        assertEquals("Manage share classes, rights, voting, and distribution rules for the subsidiary.", page.getPageSubtitle());
        assertEquals("Atirath Holdings", page.getSelectedCompany());
        assertEquals("Active", page.getCompanyStatus());

        // 4 KPI Cards
        assertNotNull(page.getKpis());
        assertEquals(4, page.getKpis().size());
        assertEquals("Total Share Classes", page.getKpis().get(0).getTitle());
        assertEquals("4", page.getKpis().get(0).getValue());
        assertEquals("+0%", page.getKpis().get(0).getChange());
        assertEquals("Total Issued Shares", page.getKpis().get(1).getTitle());
        assertEquals("50,00,000", page.getKpis().get(1).getValue());
        assertEquals("+12%", page.getKpis().get(1).getChange());
        assertEquals("Total Equity Value", page.getKpis().get(2).getTitle());
        assertEquals("₹ 50.0 Cr", page.getKpis().get(2).getValue());
        assertEquals("+18%", page.getKpis().get(2).getChange());
        assertEquals("Investor Categories", page.getKpis().get(3).getTitle());
        assertEquals("3", page.getKpis().get(3).getValue());
        assertEquals("+50%", page.getKpis().get(3).getChange());

        // Tabs
        assertNotNull(page.getTabs());
        assertEquals(4, page.getTabs().size());
        assertEquals("All Share Classes (4)", page.getActiveTab());

        // 4 Share Classes Table Rows
        assertNotNull(page.getShareClasses());
        assertEquals(4, page.getShareClasses().size());

        SubsidiaryShareClassesPageDto.ShareClassItemDto sc1 = page.getShareClasses().get(0);
        assertEquals("Equity Shares (ES)", sc1.getClassName());
        assertEquals("Equity", sc1.getClassType());
        assertEquals(10.0, sc1.getFaceValue());
        assertEquals("30,00,000", sc1.getIssuedShares());
        assertEquals("1,00,00,000", sc1.getAuthorizedShares());
        assertEquals("1 vote / share", sc1.getVotingRights());
        assertEquals("Yes", sc1.getDividendRights());
        assertEquals("Active", sc1.getStatus());

        SubsidiaryShareClassesPageDto.ShareClassItemDto sc2 = page.getShareClasses().get(1);
        assertEquals("Preference Shares (PS)", sc2.getClassName());
        assertEquals("Preference", sc2.getClassType());
        assertEquals("Fixed (8%)", sc2.getDividendRights());

        SubsidiaryShareClassesPageDto.ShareClassItemDto sc3 = page.getShareClasses().get(2);
        assertEquals("Employee Stock Option (ESOP)", sc3.getClassName());
        assertEquals("Equity", sc3.getClassType());

        SubsidiaryShareClassesPageDto.ShareClassItemDto sc4 = page.getShareClasses().get(3);
        assertEquals("Series A Preference (A1)", sc4.getClassName());
        assertEquals("Cumulative (10%)", sc4.getDividendRights());

        // Selected Share Class Detail Drawer (Equity Shares (ES))
        assertNotNull(page.getSelectedShareClassDetail());
        assertEquals("Equity Shares (ES)", page.getSelectedShareClassDetail().getClassName());
        assertEquals("Common equity shares with full voting rights", page.getSelectedShareClassDetail().getSubheader());
        assertEquals("₹ 10", page.getSelectedShareClassDetail().getFaceValue());
        assertEquals("₹ 30.0 Cr", page.getSelectedShareClassDetail().getApproxValue());
        assertEquals("1 vote / share", page.getSelectedShareClassDetail().getVotingRights());
        assertEquals("Yes (As declared)", page.getSelectedShareClassDetail().getDividendRights());
        assertEquals("Restricted", page.getSelectedShareClassDetail().getTransferability());
        assertEquals("All Eligible Investors", page.getSelectedShareClassDetail().getInvestorCategory());

        // Share Class Distribution Donut (50,00,000 total shares)
        assertNotNull(page.getShareClassDistribution());
        assertEquals("50,00,000", page.getShareClassDistribution().getTotalShares());
        assertEquals(4, page.getShareClassDistribution().getSlices().size());
        assertEquals("Equity Shares (ES)", page.getShareClassDistribution().getSlices().get(0).getShareClass());
        assertEquals(60.0, page.getShareClassDistribution().getSlices().get(0).getPercentage());
        assertEquals("Preference Shares (PS)", page.getShareClassDistribution().getSlices().get(1).getShareClass());
        assertEquals(20.0, page.getShareClassDistribution().getSlices().get(1).getPercentage());

        // Authorized vs Issued Bars: 4 classes
        assertNotNull(page.getAuthorizedVsIssued());
        assertEquals(4, page.getAuthorizedVsIssued().size());
        assertEquals("Equity Shares (ES)", page.getAuthorizedVsIssued().get(0).getShareClass());
        assertEquals(30.0, page.getAuthorizedVsIssued().get(0).getIssuedLakhs());
        assertEquals(100.0, page.getAuthorizedVsIssued().get(0).getAuthorizedLakhs());

        // Investor Categories Donut: 4 categories
        assertNotNull(page.getInvestorCategories());
        assertEquals(4, page.getInvestorCategories().getSlices().size());
        assertEquals("Promoters", page.getInvestorCategories().getSlices().get(0).getCategory());
        assertEquals(62.5, page.getInvestorCategories().getSlices().get(0).getPercentage());
        assertEquals("Strategic Investors", page.getInvestorCategories().getSlices().get(1).getCategory());
        assertEquals(25.0, page.getInvestorCategories().getSlices().get(1).getPercentage());
    }

    @Test
    @DisplayName("Screen 3: Verify add new share class")
    void testAddShareClass() {
        AddShareClassRequestDto req = AddShareClassRequestDto.builder()
                .className("Class B Ordinary Shares")
                .classType("Equity")
                .faceValue(10.0)
                .issuedShares("10,00,000")
                .authorizedShares("25,00,000")
                .votingRights("1 vote / share")
                .dividendRights("Yes")
                .transferability("Free Transfer")
                .investorCategory("Qualified Institutional Buyers")
                .description("Class B ordinary shares for growth capital")
                .build();

        ShareClassDetailDto created = subsidiaryAdminService.addShareClass(req);

        assertNotNull(created);
        assertEquals("Class B Ordinary Shares", created.getClassName());
        assertEquals("Equity", created.getClassType());
        assertEquals("Active", created.getStatus());

        // Verify it appears in share classes page
        SubsidiaryShareClassesPageDto page = subsidiaryAdminService.getShareClassesPage("Atirath Holdings", null, null);
        assertTrue(page.getShareClasses().stream().anyMatch(sc -> sc.getClassName().equals("Class B Ordinary Shares")));
    }

    // ==========================================
    // SCREEN 4: SHARE ISSUANCE
    // ==========================================
    @Test
    @DisplayName("Screen 4: Verify Share Issuance Page matches screen design")
    void testGetShareIssuancePage() {
        SubsidiaryShareIssuancePageDto page = subsidiaryAdminService.getShareIssuancePage("Atirath Holdings", null, null);

        assertNotNull(page);
        assertEquals("Share Issuance", page.getPageTitle());
        assertEquals("Issue new shares, manage allotments, and track issuance history.", page.getPageSubtitle());
        assertEquals("Atirath Holdings", page.getSelectedCompany());
        assertEquals("Active", page.getCompanyStatus());

        // 4 KPI Cards
        assertNotNull(page.getKpis());
        assertEquals(4, page.getKpis().size());
        assertEquals("Total Shares Issued", page.getKpis().get(0).getTitle());
        assertEquals("50,00,000", page.getKpis().get(0).getValue());
        assertEquals("+12%", page.getKpis().get(0).getChange());
        assertEquals("Total Capital Raised", page.getKpis().get(1).getTitle());
        assertEquals("₹ 50.0 Cr", page.getKpis().get(1).getValue());
        assertEquals("+18%", page.getKpis().get(1).getChange());
        assertEquals("Total Investors", page.getKpis().get(2).getTitle());
        assertEquals("12", page.getKpis().get(2).getValue());
        assertEquals("+33%", page.getKpis().get(2).getChange());
        assertEquals("Pending Issuances", page.getKpis().get(3).getTitle());
        assertEquals("2", page.getKpis().get(3).getValue());
        assertEquals("-50%", page.getKpis().get(3).getChange());

        // Tabs
        assertNotNull(page.getTabs());
        assertEquals(3, page.getTabs().size());
        assertEquals("Issuance Requests (6)", page.getActiveTab());

        // Table Rows: 6 items (SI-2026-006 down to 001)
        assertNotNull(page.getRequests());
        assertEquals(6, page.getRequests().size());

        SubsidiaryShareIssuancePageDto.ShareIssuanceItemDto req1 = page.getRequests().get(0);
        assertEquals("SI-2026-006", req1.getRequestId());
        assertEquals("Equity Shares (ES)", req1.getShareClass());
        assertEquals("5,00,000", req1.getNoOfShares());
        assertEquals("10", req1.getFaceValue());
        assertEquals("100", req1.getIssuePrice());
        assertEquals("5,00,00,000", req1.getTotalAmount());
        assertEquals("Green Future Fund", req1.getInvestor());
        assertEquals("Pending Approval", req1.getStatus());
        assertEquals("16 Sep 2026", req1.getRequestedOn());

        SubsidiaryShareIssuancePageDto.ShareIssuanceItemDto req2 = page.getRequests().get(1);
        assertEquals("SI-2026-005", req2.getRequestId());
        assertEquals("Preference Shares (PS)", req2.getShareClass());
        assertEquals("Sunrise Ventures", req2.getInvestor());
        assertEquals("Under Review", req2.getStatus());

        SubsidiaryShareIssuancePageDto.ShareIssuanceItemDto req3 = page.getRequests().get(2);
        assertEquals("SI-2026-004", req3.getRequestId());
        assertEquals("ESOP (ESOP)", req3.getShareClass());
        assertEquals("Pending Documents", req3.getStatus());

        SubsidiaryShareIssuancePageDto.ShareIssuanceItemDto req4 = page.getRequests().get(3);
        assertEquals("SI-2026-003", req4.getRequestId());
        assertEquals("Approved", req4.getStatus());

        // Issuance Trend: 6 months
        assertNotNull(page.getIssuanceTrend());
        assertEquals(6, page.getIssuanceTrend().size());
        assertEquals("Apr", page.getIssuanceTrend().get(0).getMonth());

        // Share Class-wise Issuance Donut: 4 classes
        assertNotNull(page.getShareClassWiseIssuance());
        assertEquals("50,00,000", page.getShareClassWiseIssuance().getTotalShares());
        assertEquals(4, page.getShareClassWiseIssuance().getSlices().size());
        assertEquals("Equity Shares (ES)", page.getShareClassWiseIssuance().getSlices().get(0).getShareClass());
        assertEquals(60.0, page.getShareClassWiseIssuance().getSlices().get(0).getPercentage());

        // Recent Approvals: 3 rows
        assertNotNull(page.getRecentApprovals());
        assertEquals(3, page.getRecentApprovals().size());
        assertEquals("SI-2026-003", page.getRecentApprovals().get(0).getRequestId());
        assertEquals("Equity Shares (ES)", page.getRecentApprovals().get(0).getShareClass());
        assertEquals("8,00,000", page.getRecentApprovals().get(0).getNoOfShares());
        assertEquals("Global Energy Partners", page.getRecentApprovals().get(0).getInvestor());

        // Activity Log: 5 rows
        assertNotNull(page.getActivityLog());
        assertEquals(5, page.getActivityLog().size());
        assertTrue(page.getActivityLog().get(0).getActivity().contains("SI-2026-006"));
    }

    @Test
    @DisplayName("Screen 4: Verify submit share issuance request")
    void testSubmitShareIssuance() {
        SubmitShareIssuanceRequestDto newReq = SubmitShareIssuanceRequestDto.builder()
                .shareClass("Equity Shares (ES)")
                .numberOfShares("3,00,000")
                .faceValue(10.0)
                .issuePrice(110.0)
                .totalAmount(33000000.0)
                .investor("Apex Ventures India")
                .issueDate("16 Sep 2026")
                .allotmentType("Private Placement")
                .remarks("Expansion round tranche 2")
                .build();

        SubsidiaryShareIssuancePageDto.ShareIssuanceItemDto created = subsidiaryAdminService.submitShareIssuance(newReq);

        assertNotNull(created);
        assertEquals("SI-2026-007", created.getRequestId());
        assertEquals("Equity Shares (ES)", created.getShareClass());
        assertEquals("3,00,000", created.getNoOfShares());
        assertEquals("Apex Ventures India", created.getInvestor());
        assertEquals("Pending Approval", created.getStatus());

        // Verify it was prepended to the requests list
        SubsidiaryShareIssuancePageDto page = subsidiaryAdminService.getShareIssuancePage("Atirath Holdings", null, null);
        assertEquals("SI-2026-007", page.getRequests().get(0).getRequestId());
    }

    @Test
    @DisplayName("Screen 4: Verify approve and reject share issuance workflow")
    void testApproveAndRejectIssuance() {
        // Approve SI-2026-006
        SubsidiaryShareIssuancePageDto.ShareIssuanceItemDto approved = subsidiaryAdminService.approveIssuance("SI-2026-006");
        assertNotNull(approved);
        assertEquals("Approved", approved.getStatus());

        // Reject SI-2026-005
        SubsidiaryShareIssuancePageDto.ShareIssuanceItemDto rejected = subsidiaryAdminService.rejectIssuance("SI-2026-005");
        assertNotNull(rejected);
        assertEquals("Rejected", rejected.getStatus());
    }

    // ==========================================
    // SCREEN 5: SUBSIDIARY SHAREHOLDERS
    // ==========================================
    @Test
    @DisplayName("Screen 5: Verify Subsidiary Shareholders Page matches screen design")
    void testShareholdersPage() {
        SubsidiaryShareholdersPageDto page = subsidiaryAdminService.getSubsidiaryShareholdersPage("Atirath Holdings", null, null);

        assertNotNull(page);
        assertEquals("Shareholders", page.getPageTitle());
        assertEquals("View and manage all shareholders, their holdings, categories and related information.", page.getPageSubtitle());
        assertEquals("Atirath Holdings", page.getSelectedCompany());
        assertEquals("Active", page.getCompanyStatus());

        // 4 KPI Cards
        assertNotNull(page.getKpis());
        assertEquals(4, page.getKpis().size());
        assertEquals("Total Shareholders", page.getKpis().get(0).getTitle());
        assertEquals("12", page.getKpis().get(0).getValue());
        assertEquals("+20% vs last quarter", page.getKpis().get(0).getChange());
        assertEquals("Institutional Investors", page.getKpis().get(1).getTitle());
        assertEquals("5", page.getKpis().get(1).getValue());
        assertEquals("+25% vs last quarter", page.getKpis().get(1).getChange());
        assertEquals("Individual Investors", page.getKpis().get(2).getTitle());
        assertEquals("6", page.getKpis().get(2).getValue());
        assertEquals("+20% vs last quarter", page.getKpis().get(2).getChange());
        assertEquals("Foreign Investors", page.getKpis().get(3).getTitle());
        assertEquals("1", page.getKpis().get(3).getValue());
        assertEquals("+0% vs last quarter", page.getKpis().get(3).getChange());

        // Tabs: 4 tabs
        assertNotNull(page.getTabs());
        assertEquals(4, page.getTabs().size());
        assertEquals("All Shareholders (12)", page.getActiveTab());

        // 10 Shareholders in table (out of 12)
        assertNotNull(page.getShareholders());
        assertEquals(10, page.getShareholders().size());

        SubsidiaryShareholdersPageDto.ShareholderItemDto sh1 = page.getShareholders().get(0);
        assertEquals("Green Future Fund", sh1.getShareholderName());
        assertEquals("GF", sh1.getInitials());
        assertEquals("Institutional", sh1.getCategory());
        assertEquals("VC Fund", sh1.getType());
        assertEquals("12,00,000", sh1.getNoOfShares());
        assertEquals(24.0, sh1.getOwnershipPercentage());
        assertEquals(12.0, sh1.getInvestmentCr());
        assertEquals("Active", sh1.getStatus());

        SubsidiaryShareholdersPageDto.ShareholderItemDto sh2 = page.getShareholders().get(1);
        assertEquals("Sunrise Ventures", sh2.getShareholderName());
        assertEquals("PE Fund", sh2.getType());

        SubsidiaryShareholdersPageDto.ShareholderItemDto sh6 = page.getShareholders().get(5);
        assertEquals("Ramesh Kumar", sh6.getShareholderName());
        assertEquals("Promoter", sh6.getType());
        assertEquals("Individual", sh6.getCategory());

        SubsidiaryShareholdersPageDto.ShareholderItemDto sh10 = page.getShareholders().get(9);
        assertEquals("David Brown", sh10.getShareholderName());
        assertEquals("Foreign", sh10.getCategory());

        // Right Flyout: Shareholder Details (Green Future Fund)
        ShareholderDetailDto detail = subsidiaryAdminService.getShareholderDetail("sh-1");
        assertNotNull(detail);
        assertEquals("Green Future Fund", detail.getShareholderName());
        assertEquals("GF", detail.getInitials());
        assertEquals("Institutional", detail.getCategory());
        assertEquals("VC Fund", detail.getType());
        assertEquals("12,00,000", detail.getNoOfShares());
        assertEquals(24.0, detail.getOwnershipPercentage());
        assertEquals("₹ 12.0 Cr", detail.getInvestmentAmount());
        assertEquals("AAACG1234D", detail.getPan());
        assertEquals("India", detail.getCountry());
        assertEquals("Rahul Mehta", detail.getContactPerson());
        assertEquals("rahul@greenfuturefund.in", detail.getEmail());
        assertEquals("+91 98765 43210", detail.getPhone());
        assertEquals("Verified", detail.getKycStatus());
        assertEquals("10 Sep 2026", detail.getLastUpdated());

        // Bottom Widgets:
        // 1. Shareholding Pattern Donut (50,00,000 Total Shares)
        assertNotNull(page.getShareholdingPattern());
        assertEquals("50,00,000", page.getShareholdingPattern().getTotalShares());
        assertEquals(5, page.getShareholdingPattern().getSlices().size());
        assertEquals(24.0, page.getShareholdingPattern().getSlices().get(0).getPercentage());

        // 2. Shareholders by Category Bar Chart
        assertNotNull(page.getShareholdersByCategory());
        assertEquals(3, page.getShareholdersByCategory().size());
        assertEquals("Institutional", page.getShareholdersByCategory().get(0).getCategory());
        assertEquals(5, page.getShareholdersByCategory().get(0).getCount());
        assertEquals("Individual", page.getShareholdersByCategory().get(1).getCategory());
        assertEquals(6, page.getShareholdersByCategory().get(1).getCount());
        assertEquals("Foreign", page.getShareholdersByCategory().get(2).getCategory());
        assertEquals(1, page.getShareholdersByCategory().get(2).getCount());

        // 3. Top Shareholders Horizontal Bar Chart
        assertNotNull(page.getTopShareholders());
        assertEquals(5, page.getTopShareholders().size());
        assertEquals("Green Future Fund", page.getTopShareholders().get(0).getName());
        assertEquals(24.0, page.getTopShareholders().get(0).getPercentage());

        // Add Shareholder mutation
        AddSubsidiaryShareholderRequestDto addReq = AddSubsidiaryShareholderRequestDto.builder()
                .shareholderName("Nexus Horizon Fund")
                .category("Institutional")
                .type("Corporate")
                .noOfShares("1,50,000")
                .ownershipPercentage(3.0)
                .investmentCr(1.5)
                .pan("NEXUS1234F")
                .country("Singapore")
                .registeredAddress("Marina Bay Financial Centre, Singapore")
                .build();
        ShareholderDetailDto created = subsidiaryAdminService.addShareholder(addReq);
        assertNotNull(created);
        assertEquals("Nexus Horizon Fund", created.getShareholderName());
    }

    // ==========================================
    // SCREEN 6: CAP TABLE
    // ==========================================
    @Test
    @DisplayName("Screen 6: Verify Cap Table Page matches screen design")
    void testCapTablePage() {
        SubsidiaryCapTablePageDto page = subsidiaryAdminService.getCapTablePage("Atirath Holdings", null, null);

        assertNotNull(page);
        assertEquals("Cap Table", page.getPageTitle());
        assertEquals("Complete shareholding structure, ownership, dilution and investment history.", page.getPageSubtitle());
        assertEquals("Atirath Holdings", page.getSelectedCompany());
        assertEquals("Active", page.getCompanyStatus());

        // 4 KPI Cards
        assertNotNull(page.getKpis());
        assertEquals(4, page.getKpis().size());
        assertEquals("Total Shares", page.getKpis().get(0).getTitle());
        assertEquals("50,00,000", page.getKpis().get(0).getValue());
        assertEquals("+12% vs last quarter", page.getKpis().get(0).getChange());
        assertEquals("Total Equity Value", page.getKpis().get(1).getTitle());
        assertEquals("₹ 50.0 Cr", page.getKpis().get(1).getValue());
        assertEquals("+18% vs last quarter", page.getKpis().get(1).getChange());
        assertEquals("Total Shareholders", page.getKpis().get(2).getTitle());
        assertEquals("12", page.getKpis().get(2).getValue());
        assertEquals("+20% vs last quarter", page.getKpis().get(2).getChange());
        assertEquals("Share Classes", page.getKpis().get(3).getTitle());
        assertEquals("4", page.getKpis().get(3).getValue());
        assertEquals("+0% vs last quarter", page.getKpis().get(3).getChange());

        // 5 Tabs
        assertNotNull(page.getTabs());
        assertEquals(5, page.getTabs().size());
        assertEquals("Current Cap Table", page.getActiveTab());

        // 10 Cap Table Rows
        assertNotNull(page.getCapTableRows());
        assertEquals(10, page.getCapTableRows().size());

        SubsidiaryCapTablePageDto.CapTableRowDto row1 = page.getCapTableRows().get(0);
        assertEquals("Green Future Fund", row1.getShareholderName());
        assertEquals("Institutional", row1.getCategory());
        assertEquals("Equity (ES)", row1.getShareClass());
        assertEquals("12,00,000", row1.getNoOfShares());
        assertEquals(24.0, row1.getOwnershipPercentage());
        assertEquals(12.0, row1.getInvestedAmountCr());
        assertEquals(100.0, row1.getAvgCost());
        assertEquals("Active", row1.getStatus());

        SubsidiaryCapTablePageDto.CapTableRowDto row2 = page.getCapTableRows().get(1);
        assertEquals("Sunrise Ventures", row2.getShareholderName());
        assertEquals("Preference (PS)", row2.getShareClass());
        assertEquals(75.0, row2.getAvgCost());

        SubsidiaryCapTablePageDto.CapTableRowDto row4 = page.getCapTableRows().get(3);
        assertEquals("Nexus Capital", row4.getShareholderName());
        assertEquals("Series A (A1)", row4.getShareClass());
        assertEquals(120.0, row4.getAvgCost());

        SubsidiaryCapTablePageDto.CapTableRowDto row7 = page.getCapTableRows().get(6);
        assertEquals("Priya Verma", row7.getShareholderName());
        assertEquals("ESOP (ESOP)", row7.getShareClass());

        // Right Widget 1: Shareholding Summary Donut
        assertNotNull(page.getShareholdingSummary());
        assertEquals("50,00,000", page.getShareholdingSummary().getTotalShares());
        assertEquals(6, page.getShareholdingSummary().getSlices().size());

        // Right Widget 2: Key Information Card
        assertNotNull(page.getKeyInformation());
        assertEquals("₹ 500.00 Cr", page.getKeyInformation().getAuthorizedCapital());
        assertEquals("₹ 50.00 Cr", page.getKeyInformation().getIssuedCapital());
        assertEquals("₹ 32.50 Cr", page.getKeyInformation().getPaidUpCapital());
        assertEquals("50,00,000", page.getKeyInformation().getTotalShares());
        assertEquals("₹ 10", page.getKeyInformation().getFaceValue());
        assertEquals(4, page.getKeyInformation().getShareClasses());
        assertEquals(12, page.getKeyInformation().getShareholders());
        assertEquals("10 Sep 2026", page.getKeyInformation().getLastUpdated());

        // Bottom Widget 1: Ownership by Share Class Donut
        assertNotNull(page.getOwnershipByShareClass());
        assertEquals(4, page.getOwnershipByShareClass().getSlices().size());
        assertEquals("Equity Shares (ES)", page.getOwnershipByShareClass().getSlices().get(0).getShareClass());
        assertEquals(60.0, page.getOwnershipByShareClass().getSlices().get(0).getPercentage());

        // Bottom Widget 2: Ownership by Category Donut
        assertNotNull(page.getOwnershipByCategory());
        assertEquals(3, page.getOwnershipByCategory().getSlices().size());
        assertEquals("Institutional", page.getOwnershipByCategory().getSlices().get(0).getCategory());
        assertEquals(76.4, page.getOwnershipByCategory().getSlices().get(0).getPercentage());

        // Bottom Widget 3: Investment vs Ownership Point Chart
        assertNotNull(page.getInvestmentVsOwnership());
        assertEquals(6, page.getInvestmentVsOwnership().size());

        // Bottom Widget 4: Recent Transactions
        assertNotNull(page.getRecentTransactions());
        assertEquals(5, page.getRecentTransactions().size());
        assertEquals("10 Sep 2026", page.getRecentTransactions().get(0).getDate());
        assertEquals("Issuance", page.getRecentTransactions().get(0).getType());
        assertEquals("2,50,000", page.getRecentTransactions().get(0).getSharesFormatted());
        assertEquals(100.0, page.getRecentTransactions().get(0).getPrice());

        // Add Cap Table Transaction mutation
        AddCapTableTransactionRequestDto txReq = AddCapTableTransactionRequestDto.builder()
                .shareholderName("Green Future Fund")
                .shareClass("Equity Shares (ES)")
                .type("Issuance")
                .numberOfShares("5,00,000")
                .pricePerShare(100.0)
                .remarks("Seed round completion")
                .build();
        SubsidiaryCapTablePageDto.CapTableTransactionDto tx = subsidiaryAdminService.addCapTableTransaction(txReq);
        assertNotNull(tx);
        assertEquals("Issuance", tx.getType());

        // Export Cap Table
        Map<String, String> export = subsidiaryAdminService.exportCapTable("Atirath Holdings");
        assertNotNull(export);
        assertEquals("READY", export.get("status"));
    }

    // ==========================================
    // SCREEN 7: SHARE TRANSFER RULES
    // ==========================================
    @Test
    @DisplayName("Screen 7: Verify Share Transfer Rules Page matches screen design")
    void testShareTransferRulesPage() {
        SubsidiaryShareTransferRulesPageDto page = subsidiaryAdminService.getShareTransferRulesPage("Atirath Holdings", null, null);

        assertNotNull(page);
        assertEquals("Share Transfer Rules", page.getPageTitle());
        assertEquals("Define and manage rules for transfer of shares, approvals, restrictions and compliance checks.", page.getPageSubtitle());
        assertEquals("Atirath Holdings", page.getSelectedCompany());
        assertEquals("Active", page.getCompanyStatus());

        // 4 KPI Cards
        assertNotNull(page.getKpis());
        assertEquals(4, page.getKpis().size());
        assertEquals("Total Rules", page.getKpis().get(0).getTitle());
        assertEquals("8", page.getKpis().get(0).getValue());
        assertEquals("+14% vs last quarter", page.getKpis().get(0).getChange());
        assertEquals("Applicable Share Classes", page.getKpis().get(1).getTitle());
        assertEquals("4", page.getKpis().get(1).getValue());
        assertEquals("+0% vs last quarter", page.getKpis().get(1).getChange());
        assertEquals("Restricted Transfers", page.getKpis().get(2).getTitle());
        assertEquals("2", page.getKpis().get(2).getValue());
        assertEquals("-33% vs last quarter", page.getKpis().get(2).getChange());
        assertEquals("Auto Approved Transfers", page.getKpis().get(3).getTitle());
        assertEquals("3", page.getKpis().get(3).getValue());
        assertEquals("+50% vs last quarter", page.getKpis().get(3).getChange());

        // 5 Tabs
        assertNotNull(page.getTabs());
        assertEquals(5, page.getTabs().size());
        assertEquals("All Rules (8)", page.getActiveTab());

        // 8 Rules in table
        assertNotNull(page.getRules());
        assertEquals(8, page.getRules().size());

        SubsidiaryShareTransferRulesPageDto.ShareTransferRuleItemDto r1 = page.getRules().get(0);
        assertEquals("Board Approval for ES Transfer", r1.getRuleName());
        assertEquals("Approval Workflow", r1.getRuleType());
        assertEquals("Equity Shares (ES)", r1.getApplicableShareClasses());
        assertEquals("Transfer to external party", r1.getConditions());
        assertEquals("Yes (Board)", r1.getApprovalRequired());
        assertEquals("Active", r1.getStatus());

        SubsidiaryShareTransferRulesPageDto.ShareTransferRuleItemDto r2 = page.getRules().get(1);
        assertEquals("Pre-emption for Existing Shareholders", r2.getRuleName());
        assertEquals("Pre-emptive Right", r2.getRuleType());

        SubsidiaryShareTransferRulesPageDto.ShareTransferRuleItemDto r3 = page.getRules().get(2);
        assertEquals("Restricted Transfer (Lock-in)", r3.getRuleName());
        assertEquals("Restriction", r3.getRuleType());
        assertEquals("Preference Shares (PS)", r3.getApplicableShareClasses());
        assertEquals("No transfer for 2 years", r3.getConditions());

        SubsidiaryShareTransferRulesPageDto.ShareTransferRuleItemDto r6 = page.getRules().get(5);
        assertEquals("Right of First Refusal", r6.getRuleName());
        assertEquals("Inactive", r6.getStatus());

        // Right Flyout: Rule Details
        ShareTransferRuleDetailDto ruleDetail = subsidiaryAdminService.getShareTransferRuleDetail("rule-1");
        assertNotNull(ruleDetail);
        assertEquals("Board Approval for ES Transfer", ruleDetail.getRuleName());
        assertEquals("Approval Workflow", ruleDetail.getRuleType());
        assertEquals("Equity Shares (ES)", ruleDetail.getApplicableShareClasses());
        assertEquals("Transfer of equity shares to any external party requires prior approval from the Board of Directors.", ruleDetail.getDescription());
        assertEquals("Transfer to external party", ruleDetail.getConditions());
        assertEquals("Yes (Board)", ruleDetail.getApprovalRequired());
        assertEquals("01 Jan 2025", ruleDetail.getEffectiveFrom());
        assertEquals("Active", ruleDetail.getStatus());
        assertEquals("Amit Singh", ruleDetail.getCreatedBy());

        // Bottom Widgets:
        // 1. Rules by Type Donut
        assertNotNull(page.getRulesByType());
        assertEquals(8, page.getRulesByType().getTotalRules());
        assertEquals(4, page.getRulesByType().getSlices().size());

        // 2. Applicable Share Classes Bar Chart
        assertNotNull(page.getApplicableShareClasses());
        assertEquals(4, page.getApplicableShareClasses().size());
        assertEquals("Equity Shares (ES)", page.getApplicableShareClasses().get(0).getShareClass());
        assertEquals(4, page.getApplicableShareClasses().get(0).getRulesCount());

        // 3. Transfer Approval Status Donut
        assertNotNull(page.getTransferApprovalStatus());
        assertEquals(120, page.getTransferApprovalStatus().getTotalRequests());
        assertEquals(3, page.getTransferApprovalStatus().getSlices().size());
        assertEquals(70, page.getTransferApprovalStatus().getSlices().get(0).getCount());

        // Add Rule mutation
        AddShareTransferRuleRequestDto ruleReq = AddShareTransferRuleRequestDto.builder()
                .ruleName("ESOP Lock-in Period Rule")
                .ruleType("Restriction")
                .applicableShareClasses("Employee Stock Option (ESOP)")
                .approvalRequired("Board of Directors")
                .description("1 year mandatory cliff period before exercise.")
                .build();
        ShareTransferRuleDetailDto rule = subsidiaryAdminService.addShareTransferRule(ruleReq);
        assertNotNull(rule);
        assertEquals("ESOP Lock-in Period Rule", rule.getRuleName());
    }

    // ==========================================
    // SCREEN 8: TRADING RULES
    // ==========================================
    @Test
    @DisplayName("Screen 8: Verify Trading Rules Page matches screen design")
    void testTradingRulesPage() {
        SubsidiaryTradingRulesPageDto page = subsidiaryAdminService.getTradingRulesPage("Atirath Holdings", null, null);

        assertNotNull(page);
        assertEquals("Trading Rules", page.getPageTitle());
        assertEquals("Define and manage trading rules, order limits, trading windows, price bands and compliance requirements.", page.getPageSubtitle());
        assertEquals("Atirath Holdings", page.getSelectedCompany());
        assertEquals("Active", page.getCompanyStatus());

        // 4 KPI Cards
        assertNotNull(page.getKpis());
        assertEquals(4, page.getKpis().size());
        assertEquals("Total Trading Rules", page.getKpis().get(0).getTitle());
        assertEquals("10", page.getKpis().get(0).getValue());
        assertEquals("+25% vs last quarter", page.getKpis().get(0).getChange());
        assertEquals("Active Rules", page.getKpis().get(1).getTitle());
        assertEquals("8", page.getKpis().get(1).getValue());
        assertEquals("+14% vs last quarter", page.getKpis().get(1).getChange());
        assertEquals("Suspended Rules", page.getKpis().get(2).getTitle());
        assertEquals("1", page.getKpis().get(2).getValue());
        assertEquals("+0% vs last quarter", page.getKpis().get(2).getChange());
        assertEquals("Applicable Share Classes", page.getKpis().get(3).getTitle());
        assertEquals("4", page.getKpis().get(3).getValue());
        assertEquals("+0% vs last quarter", page.getKpis().get(3).getChange());

        // 6 Tabs
        assertNotNull(page.getTabs());
        assertEquals(6, page.getTabs().size());
        assertEquals("All Trading Rules (10)", page.getActiveTab());

        // 10 Trading Rules in table
        assertNotNull(page.getRules());
        assertEquals(10, page.getRules().size());

        SubsidiaryTradingRulesPageDto.TradingRuleItemDto tr1 = page.getRules().get(0);
        assertEquals("Trading Window (Quarterly)", tr1.getRuleName());
        assertEquals("Trading Window", tr1.getRuleType());
        assertEquals("All Classes", tr1.getApplicableShareClasses());
        assertEquals("Open: 1-25 of quarter", tr1.getParameters());
        assertEquals("Active", tr1.getStatus());
        assertEquals("01 Jan 2025", tr1.getEffectiveFrom());

        SubsidiaryTradingRulesPageDto.TradingRuleItemDto tr2 = page.getRules().get(1);
        assertEquals("Price Band Limit", tr2.getRuleName());
        assertEquals("Price Limit", tr2.getRuleType());
        assertEquals("Equity Shares (ES)", tr2.getApplicableShareClasses());
        assertEquals("±20% from reference price", tr2.getParameters());

        SubsidiaryTradingRulesPageDto.TradingRuleItemDto tr3 = page.getRules().get(2);
        assertEquals("Order Value Limit (Retail)", tr3.getRuleName());
        assertEquals("Preference Shares (PS)", tr3.getApplicableShareClasses());
        assertEquals("Max ₹ 10,00,000 per order", tr3.getParameters());

        SubsidiaryTradingRulesPageDto.TradingRuleItemDto tr4 = page.getRules().get(3);
        assertEquals("Order Quantity Limit", tr4.getRuleName());
        assertEquals("ES", tr4.getApplicableShareClasses());
        assertEquals("Max 1,00,000 shares", tr4.getParameters());

        SubsidiaryTradingRulesPageDto.TradingRuleItemDto tr9 = page.getRules().get(8);
        assertEquals("Volume Limit (Daily)", tr9.getRuleName());
        assertEquals("Suspended", tr9.getStatus());

        SubsidiaryTradingRulesPageDto.TradingRuleItemDto tr10 = page.getRules().get(9);
        assertEquals("Board Approval for Large Trades", tr10.getRuleName());
        assertEquals("ES, PS", tr10.getApplicableShareClasses());
        assertEquals("Trades above ₹ 50 Cr", tr10.getParameters());

        // Right Flyout: Rule Details
        TradingRuleDetailDto ruleDetail = subsidiaryAdminService.getTradingRuleDetail("trule-1");
        assertNotNull(ruleDetail);
        assertEquals("Trading Window (Quarterly)", ruleDetail.getRuleName());
        assertEquals("Trading Window", ruleDetail.getRuleType());
        assertEquals("Defines the trading window period for each quarter.", ruleDetail.getDescription());
        assertEquals("All Classes", ruleDetail.getApplicableShareClasses());
        assertEquals("1st day of quarter", ruleDetail.getWindowOpenDate());
        assertEquals("25th day of quarter", ruleDetail.getWindowCloseDate());
        assertEquals("Last 5 days of quarter", ruleDetail.getRestrictedPeriod());
        assertEquals("Trades not allowed during restricted period.", ruleDetail.getConditions());
        assertEquals("01 Jan 2025", ruleDetail.getEffectiveFrom());
        assertEquals("Active", ruleDetail.getStatus());
        assertEquals("Priya Verma", ruleDetail.getCreatedBy());

        // Bottom Widgets:
        // 1. Rules by Type Donut
        assertNotNull(page.getRulesByType());
        assertEquals(10, page.getRulesByType().getTotalRules());
        assertEquals(6, page.getRulesByType().getSlices().size());

        // 2. Rule Status Donut
        assertNotNull(page.getRuleStatus());
        assertEquals(10, page.getRuleStatus().getTotalRules());
        assertEquals(3, page.getRuleStatus().getSlices().size());
        assertEquals("Active", page.getRuleStatus().getSlices().get(0).getStatus());
        assertEquals(8, page.getRuleStatus().getSlices().get(0).getCount());

        // 3. Applicable Share Classes Bar Chart
        assertNotNull(page.getApplicableShareClasses());
        assertEquals(4, page.getApplicableShareClasses().size());
        assertEquals("Equity Shares (ES)", page.getApplicableShareClasses().get(0).getShareClass());
        assertEquals(5, page.getApplicableShareClasses().get(0).getRulesCount());

        // Status Toggle mutation
        TradingRuleDetailDto toggled = subsidiaryAdminService.toggleTradingRuleStatus("trule-1");
        assertNotNull(toggled);

        // Add Rule mutation
        AddTradingRuleRequestDto addRule = AddTradingRuleRequestDto.builder()
                .ruleName("Max Order Value Limit")
                .ruleType("Order Limit")
                .applicableShareClasses("All Classes")
                .parameters("Max ₹ 25,00,000 per single order")
                .description("Automated order ceiling")
                .build();
        TradingRuleDetailDto created = subsidiaryAdminService.addTradingRule(addRule);
        assertNotNull(created);
        assertEquals("Max Order Value Limit", created.getRuleName());
    }

    // ==========================================
    // SCREEN 9: COMPANY APPROVERS
    // ==========================================
    @Test
    @DisplayName("Screen 9: Verify Company Approvers Page matches screen design")
    void testCompanyApproversPage() {
        SubsidiaryApproversPageDto page = subsidiaryAdminService.getSubsidiaryApproversPage("Atirath Holdings", null, null);

        assertNotNull(page);
        assertEquals("Company Approvers", page.getPageTitle());
        assertEquals("Manage approval authorities for share issuances, transfers, corporate actions and other key transactions.", page.getPageSubtitle());
        assertEquals("Atirath Holdings", page.getSelectedCompany());
        assertEquals("Active", page.getCompanyStatus());

        // 4 KPI Cards
        assertNotNull(page.getKpis());
        assertEquals(4, page.getKpis().size());
        assertEquals("Total Approvers", page.getKpis().get(0).getTitle());
        assertEquals("8", page.getKpis().get(0).getValue());
        assertEquals("+14% vs last quarter", page.getKpis().get(0).getChange());
        assertEquals("Active Approvers", page.getKpis().get(1).getTitle());
        assertEquals("6", page.getKpis().get(1).getValue());
        assertEquals("+20% vs last quarter", page.getKpis().get(1).getChange());
        assertEquals("Inactive Approvers", page.getKpis().get(2).getTitle());
        assertEquals("2", page.getKpis().get(2).getValue());
        assertEquals("-33% vs last quarter", page.getKpis().get(2).getChange());
        assertEquals("Approval Workflows", page.getKpis().get(3).getTitle());
        assertEquals("5", page.getKpis().get(3).getValue());
        assertEquals("+25% vs last quarter", page.getKpis().get(3).getChange());

        // 4 Tabs
        assertNotNull(page.getTabs());
        assertEquals(4, page.getTabs().size());
        assertEquals("All Approvers (8)", page.getActiveTab());

        // 8 Table Approvers
        assertNotNull(page.getApprovers());
        assertEquals(8, page.getApprovers().size());

        SubsidiaryApproversPageDto.CompanyApproverItemDto a1 = page.getApprovers().get(0);
        assertEquals("Ramesh Kumar", a1.getApproverName());
        assertEquals("Managing Director", a1.getRoleOrDesignation());
        assertEquals("Board", a1.getDepartment());
        assertEquals("All Types", a1.getApprovalTypes());
        assertEquals("Unlimited", a1.getApprovalLimit());
        assertEquals("Active", a1.getStatus());

        SubsidiaryApproversPageDto.CompanyApproverItemDto a2 = page.getApprovers().get(1);
        assertEquals("Priya Verma", a2.getApproverName());
        assertEquals("Director - Finance", a2.getRoleOrDesignation());
        assertEquals("Finance", a2.getDepartment());
        assertEquals("Share Issuance, Buyback", a2.getApprovalTypes());
        assertEquals("50,00,00,000", a2.getApprovalLimit());

        SubsidiaryApproversPageDto.CompanyApproverItemDto a7 = page.getApprovers().get(6);
        assertEquals("Neha Patel", a7.getApproverName());
        assertEquals("Inactive", a7.getStatus());

        SubsidiaryApproversPageDto.CompanyApproverItemDto a8 = page.getApprovers().get(7);
        assertEquals("Lokesh Kumar", a8.getApproverName());
        assertEquals("Inactive", a8.getStatus());

        // Right Flyout: Approver Details (Ramesh Kumar)
        CompanyApproverDetailDto detail = subsidiaryAdminService.getCompanyApproverDetail("appr-1");
        assertNotNull(detail);
        assertEquals("Ramesh Kumar", detail.getFullName());
        assertEquals("Managing Director", detail.getDesignation());
        assertEquals("Board", detail.getDepartment());
        assertEquals("ramesh.kumar@atirathholdings.com", detail.getEmail());
        assertEquals("+91 98765 43210", detail.getPhone());
        assertEquals("Unlimited", detail.getApprovalLimit());
        assertEquals("All Types", detail.getApprovalTypes());
        assertEquals("01 Jan 2025", detail.getEffectiveFrom());
        assertEquals("Active", detail.getStatus());

        // Bottom Widgets:
        // 1. Approvers by Department Donut
        assertNotNull(page.getApproversByDepartment());
        assertEquals(8, page.getApproversByDepartment().getTotalApprovers());
        assertEquals(6, page.getApproversByDepartment().getSlices().size());

        // 2. Approvers by Approval Type Donut
        assertNotNull(page.getApproversByApprovalType());
        assertEquals(8, page.getApproversByApprovalType().getTotalApprovers());
        assertEquals(6, page.getApproversByApprovalType().getSlices().size());

        // 3. Approval Limit Distribution Bar Chart
        assertNotNull(page.getApprovalLimitDistribution());
        assertEquals(4, page.getApprovalLimitDistribution().size());
        assertEquals("< 1 Cr", page.getApprovalLimitDistribution().get(0).getRange());
        assertEquals(2, page.getApprovalLimitDistribution().get(0).getCount());

        // Toggle Status mutation
        CompanyApproverDetailDto toggled = subsidiaryAdminService.toggleCompanyApproverStatus("appr-1");
        assertNotNull(toggled);

        // Add Approver mutation
        AddCompanyApproverRequestDto addReq = AddCompanyApproverRequestDto.builder()
                .fullName("Rajesh Mehta")
                .designation("VP Operations")
                .department("Operations")
                .approvalLimit("₹ 50,00,000")
                .approvalTypes("Operations, Capex")
                .email("rajesh.mehta@atirathholdings.in")
                .phone("+91 98765 43210")
                .build();
        CompanyApproverDetailDto created = subsidiaryAdminService.addCompanyApprover(addReq);
        assertNotNull(created);
        assertEquals("Rajesh Mehta", created.getFullName());
    }

    // ==========================================
    // SCREEN 10: COMPANY DOCUMENTS
    // ==========================================
    @Test
    @DisplayName("Screen 10: Verify Company Documents Page matches screen design")
    void testCompanyDocumentsPage() {
        SubsidiaryDocumentsPageDto page = subsidiaryAdminService.getSubsidiaryDocumentsPage("Atirath Holdings", null, null);

        assertNotNull(page);
        assertEquals("Company Documents", page.getPageTitle());
        assertEquals("Manage all statutory, regulatory, corporate and operational documents.", page.getPageSubtitle());
        assertEquals("Atirath Holdings", page.getSelectedCompany());
        assertEquals("Active", page.getCompanyStatus());

        // 4 KPI Cards
        assertNotNull(page.getKpis());
        assertEquals(4, page.getKpis().size());
        assertEquals("Total Documents", page.getKpis().get(0).getTitle());
        assertEquals("24", page.getKpis().get(0).getValue());
        assertEquals("+33% vs last quarter", page.getKpis().get(0).getChange());
        assertEquals("Verified Documents", page.getKpis().get(1).getTitle());
        assertEquals("18", page.getKpis().get(1).getValue());
        assertEquals("+20% vs last quarter", page.getKpis().get(1).getChange());
        assertEquals("Expiring Soon", page.getKpis().get(2).getTitle());
        assertEquals("3", page.getKpis().get(2).getValue());
        assertEquals("+0% vs last quarter", page.getKpis().get(2).getChange());
        assertEquals("Document Categories", page.getKpis().get(3).getTitle());
        assertEquals("8", page.getKpis().get(3).getValue());
        assertEquals("+0% vs last quarter", page.getKpis().get(3).getChange());

        // 6 Tabs
        assertNotNull(page.getTabs());
        assertEquals(6, page.getTabs().size());
        assertEquals("All Documents (24)", page.getActiveTab());

        // 10 Table Documents (page 1 of 24)
        assertNotNull(page.getDocuments());
        assertEquals(10, page.getDocuments().size());

        SubsidiaryDocumentsPageDto.CompanyDocumentItemDto d1 = page.getDocuments().get(0);
        assertEquals("Certificate of Incorporation", d1.getDocumentName());
        assertEquals("Statutory", d1.getCategory());
        assertEquals("Incorporation", d1.getDocumentType());
        assertEquals("PDF", d1.getFileType());
        assertEquals("15 Jan 2024", d1.getUploadedOn());
        assertEquals("-", d1.getExpiryDate());
        assertEquals("Verified", d1.getStatus());

        SubsidiaryDocumentsPageDto.CompanyDocumentItemDto d5 = page.getDocuments().get(4);
        assertEquals("GST Registration Certificate", d5.getDocumentName());
        assertEquals("30 Jun 2027", d5.getExpiryDate());

        SubsidiaryDocumentsPageDto.CompanyDocumentItemDto d7 = page.getDocuments().get(6);
        assertEquals("Shareholder Agreement", d7.getDocumentName());
        assertEquals("DOCX", d7.getFileType());
        assertEquals("Active", d7.getStatus());

        SubsidiaryDocumentsPageDto.CompanyDocumentItemDto d9 = page.getDocuments().get(8);
        assertEquals("ISO 9001 Certificate", d9.getDocumentName());
        assertEquals("Expiring Soon", d9.getStatus());

        // Right Flyout: Document Details
        CompanyDocumentDetailDto detail = subsidiaryAdminService.getCompanyDocumentDetail("doc-1");
        assertNotNull(detail);
        assertEquals("Certificate of Incorporation", detail.getDocumentName());
        assertEquals("Statutory", detail.getCategory());
        assertEquals("Incorporation", detail.getDocumentType());
        assertEquals("COI_Atirath_Holdings.pdf", detail.getFileName());
        assertEquals("1.2 MB", detail.getFileSize());
        assertEquals("Verified", detail.getStatus());
        assertEquals("Ministry of Corporate Affairs", detail.getIssuedBy());

        // Bottom Widgets:
        // 1. Documents by Category Donut
        assertNotNull(page.getDocumentsByCategory());
        assertEquals(24, page.getDocumentsByCategory().getTotalDocuments());
        assertEquals(5, page.getDocumentsByCategory().getSlices().size());

        // 2. Document Status Donut
        assertNotNull(page.getDocumentStatus());
        assertEquals(24, page.getDocumentStatus().getTotalDocuments());
        assertEquals(4, page.getDocumentStatus().getSlices().size());
        assertEquals("Verified", page.getDocumentStatus().getSlices().get(0).getStatus());
        assertEquals(18, page.getDocumentStatus().getSlices().get(0).getCount());

        // 3. Expiry Timeline Bar Chart
        assertNotNull(page.getExpiryTimeline());
        assertEquals(4, page.getExpiryTimeline().size());
        assertEquals("< 3 Months", page.getExpiryTimeline().get(0).getRange());
        assertEquals(2, page.getExpiryTimeline().get(0).getCount());

        // Upload Document mutation
        UploadCompanyDocumentRequestDto docReq = UploadCompanyDocumentRequestDto.builder()
                .documentName("Board Resolution - Q2 2026")
                .category("Corporate")
                .issuedBy("Board of Directors")
                .fileSize("1.8 MB")
                .fileType("PDF")
                .build();
        CompanyDocumentDetailDto created = subsidiaryAdminService.uploadCompanyDocument(docReq);
        assertNotNull(created);
        assertEquals("Board Resolution - Q2 2026", created.getDocumentName());

        // Download document link
        Map<String, String> download = subsidiaryAdminService.downloadCompanyDocument("doc-1");
        assertNotNull(download);
        assertEquals("READY", download.get("status"));
    }

    // ==========================================
    // SCREEN 11: COMPANY TRANSACTIONS
    // ==========================================
    @Test
    @DisplayName("Screen 11: Verify Company Transactions Page matches screen design")
    void testCompanyTransactionsPage() {
        SubsidiaryTransactionsPageDto page = subsidiaryAdminService.getSubsidiaryTransactionsPage("Atirath Holdings", null, null);

        assertNotNull(page);
        assertEquals("Company Transactions", page.getPageTitle());
        assertEquals("Track and manage all share-related and corporate transactions.", page.getPageSubtitle());
        assertEquals("Atirath Holdings", page.getSelectedCompany());
        assertEquals("Active", page.getCompanyStatus());

        // 4 KPI Cards
        assertNotNull(page.getKpis());
        assertEquals(4, page.getKpis().size());
        assertEquals("Total Transactions", page.getKpis().get(0).getTitle());
        assertEquals("156", page.getKpis().get(0).getValue());
        assertEquals("+28% vs last quarter", page.getKpis().get(0).getChange());
        assertEquals("Total Transaction Value", page.getKpis().get(1).getTitle());
        assertEquals("₹ 125.80 Cr", page.getKpis().get(1).getValue());
        assertEquals("+32% vs last quarter", page.getKpis().get(1).getChange());
        assertEquals("Share Issuances", page.getKpis().get(2).getTitle());
        assertEquals("18", page.getKpis().get(2).getValue());
        assertEquals("+20% vs last quarter", page.getKpis().get(2).getChange());
        assertEquals("Share Transfers", page.getKpis().get(3).getTitle());
        assertEquals("76", page.getKpis().get(3).getValue());
        assertEquals("+12% vs last quarter", page.getKpis().get(3).getChange());

        // 6 Tabs
        assertNotNull(page.getTabs());
        assertEquals(6, page.getTabs().size());
        assertEquals("All Transactions (156)", page.getActiveTab());

        // 10 Table Transactions
        assertNotNull(page.getTransactions());
        assertEquals(10, page.getTransactions().size());

        SubsidiaryTransactionsPageDto.CompanyTransactionItemDto t1 = page.getTransactions().get(0);
        assertEquals("TXN-2025-001", t1.getTransactionId());
        assertEquals("Share Issuance", t1.getTransactionType());
        assertEquals("10 Sep 2026", t1.getDate());
        assertEquals("2,50,000", t1.getShares());
        assertEquals("25,00,00,000", t1.getValue());
        assertEquals("100", t1.getPricePerShare());
        assertEquals("Sunrise Ventures", t1.getCounterparty());
        assertEquals("Completed", t1.getStatus());

        SubsidiaryTransactionsPageDto.CompanyTransactionItemDto t3 = page.getTransactions().get(2);
        assertEquals("TXN-2025-003", t3.getTransactionId());
        assertEquals("Under Review", t3.getStatus());

        SubsidiaryTransactionsPageDto.CompanyTransactionItemDto t5 = page.getTransactions().get(4);
        assertEquals("TXN-2025-005", t5.getTransactionId());
        assertEquals("Corporate Action", t5.getTransactionType());
        assertEquals("Stock Split (1:2)", t5.getCounterparty());

        SubsidiaryTransactionsPageDto.CompanyTransactionItemDto t8 = page.getTransactions().get(7);
        assertEquals("TXN-2025-008", t8.getTransactionId());
        assertEquals("Rejected", t8.getStatus());

        // Right Flyout: Transaction Details
        CompanyTransactionDetailDto detail = subsidiaryAdminService.getCompanyTransactionDetail("txn-1");
        assertNotNull(detail);
        assertEquals("TXN-2025-001", detail.getTransactionId());
        assertEquals("Share Issuance", detail.getTransactionType());
        assertEquals("10 Sep 2026", detail.getTransactionDate());
        assertEquals("2,50,000", detail.getNumberOfShares());
        assertEquals("₹ 100", detail.getPricePerShare());
        assertEquals("₹ 25,00,00,000", detail.getTotalValue());
        assertEquals("Sunrise Ventures", detail.getCounterparty());
        assertEquals("Bank Transfer", detail.getPaymentMode());
        assertEquals("PAY/2026/0910/001", detail.getReferenceNo());
        assertEquals("Completed", detail.getStatus());
        assertEquals("Ramesh Kumar", detail.getApprovedBy());

        // Bottom Widgets:
        // 1. Transactions by Type Donut
        assertNotNull(page.getTransactionsByType());
        assertEquals(156, page.getTransactionsByType().getTotalTransactions());
        assertEquals(5, page.getTransactionsByType().getSlices().size());

        // 2. Monthly Transaction Value Bar Chart
        assertNotNull(page.getMonthlyTransactionValue());
        assertEquals(6, page.getMonthlyTransactionValue().size());
        assertEquals("Apr", page.getMonthlyTransactionValue().get(0).getMonth());
        assertEquals(12.5, page.getMonthlyTransactionValue().get(0).getValueCr());

        // 3. Transaction Status Donut
        assertNotNull(page.getTransactionStatus());
        assertEquals(156, page.getTransactionStatus().getTotalTransactions());
        assertEquals(5, page.getTransactionStatus().getSlices().size());
        assertEquals("Completed", page.getTransactionStatus().getSlices().get(0).getStatus());
        assertEquals(124, page.getTransactionStatus().getSlices().get(0).getCount());

        // Record Transaction mutation
        RecordCompanyTransactionRequestDto txReq = RecordCompanyTransactionRequestDto.builder()
                .transactionType("Share Issuance")
                .numberOfShares("5,00,000")
                .totalValue("₹ 5,00,00,000")
                .pricePerShare(100.0)
                .counterparty("Green Future Fund")
                .remarks("Settled transaction")
                .build();
        CompanyTransactionDetailDto recorded = subsidiaryAdminService.recordCompanyTransaction(txReq);
        assertNotNull(recorded);
        assertEquals("Green Future Fund", recorded.getCounterparty());
    }

    // ==========================================
    // SCREEN 12: COMPANY REPORTS
    // ==========================================
    @Test
    @DisplayName("Screen 12: Verify Company Reports Page matches screen design")
    void testCompanyReportsPage() {
        SubsidiaryReportsPageDto page = subsidiaryAdminService.getSubsidiaryReportsPage("Atirath Holdings", null, "01 Apr 2026 - 30 Sep 2026");

        assertNotNull(page);
        assertEquals("Company Reports", page.getPageTitle());
        assertEquals("View and generate comprehensive reports for Atirath Holdings.", page.getPageSubtitle());
        assertEquals("Atirath Holdings", page.getSelectedCompany());
        assertEquals("01 Apr 2026 - 30 Sep 2026", page.getDateRange());

        // 4 KPI Cards
        assertNotNull(page.getKpis());
        assertEquals(4, page.getKpis().size());
        assertEquals("Total Subsidiaries", page.getKpis().get(0).getTitle());
        assertEquals("5", page.getKpis().get(0).getValue());
        assertEquals("+25% vs last quarter", page.getKpis().get(0).getChange());
        assertEquals("Total Share Capital", page.getKpis().get(1).getTitle());
        assertEquals("₹ 500.00 Cr", page.getKpis().get(1).getValue());
        assertEquals("+18% vs last quarter", page.getKpis().get(1).getChange());
        assertEquals("Total Investments", page.getKpis().get(2).getTitle());
        assertEquals("₹ 320.00 Cr", page.getKpis().get(2).getValue());
        assertEquals("+32% vs last quarter", page.getKpis().get(2).getChange());
        assertEquals("Total Assets (Consolidated)", page.getKpis().get(3).getTitle());
        assertEquals("₹ 780.00 Cr", page.getKpis().get(3).getValue());
        assertEquals("+28% vs last quarter", page.getKpis().get(3).getChange());

        // 8 Tabs
        assertNotNull(page.getTabs());
        assertEquals(8, page.getTabs().size());
        assertEquals("Overview", page.getActiveTab());

        // Top Widgets:
        // 1. Subsidiary-wise Investment Bar Chart
        assertNotNull(page.getSubsidiaryWiseInvestment());
        assertEquals(5, page.getSubsidiaryWiseInvestment().size());
        assertEquals("Atirath Holdings", page.getSubsidiaryWiseInvestment().get(0).getSubsidiary());
        assertEquals(200.0, page.getSubsidiaryWiseInvestment().get(0).getInvestmentCr());

        // 2. Shareholding Distribution Donut
        assertNotNull(page.getShareholdingDistribution());
        assertEquals("500 Cr Total Share Capital", page.getShareholdingDistribution().getTotalShareCapital());
        assertEquals(4, page.getShareholdingDistribution().getSlices().size());
        assertEquals("Promoters", page.getShareholdingDistribution().getSlices().get(0).getHolderType());
        assertEquals(60.0, page.getShareholdingDistribution().getSlices().get(0).getPercentage());

        // 3. Company Performance Line Chart
        assertNotNull(page.getCompanyPerformance());
        assertEquals(6, page.getCompanyPerformance().size());
        assertEquals("Apr", page.getCompanyPerformance().get(0).getMonth());
        assertEquals(60.0, page.getCompanyPerformance().get(0).getRevenueCr());
        assertEquals(40.0, page.getCompanyPerformance().get(0).getEbitdaCr());

        // Subtabs: Available Reports, Scheduled Reports
        assertNotNull(page.getSubTabs());
        assertEquals(2, page.getSubTabs().size());
        assertEquals("Available Reports", page.getActiveSubTab());

        // 8 Available Reports in table
        assertNotNull(page.getAvailableReports());
        assertEquals(8, page.getAvailableReports().size());

        SubsidiaryReportsPageDto.ReportItemDto rep1 = page.getAvailableReports().get(0);
        assertEquals("Consolidated Financial Report", rep1.getReportName());
        assertEquals("Finance", rep1.getCategory());
        assertEquals("P&L, Balance Sheet, Cash Flow (Consolidated)", rep1.getDescription());
        assertEquals("25 Sep 2026, 10:30 AM", rep1.getLastGenerated());

        SubsidiaryReportsPageDto.ReportItemDto rep3 = page.getAvailableReports().get(2);
        assertEquals("Shareholding Report", rep3.getReportName());
        assertEquals("Shareholding", rep3.getCategory());

        SubsidiaryReportsPageDto.ReportItemDto rep8 = page.getAvailableReports().get(7);
        assertEquals("Employee & Department Report", rep8.getReportName());
        assertEquals("HR", rep8.getCategory());

        // Popular Reports: 5 items
        assertNotNull(page.getPopularReports());
        assertEquals(5, page.getPopularReports().size());
        assertEquals("Consolidated Financial Report", page.getPopularReports().get(0));

        // Generate Report mutation
        GenerateSubsidiaryReportRequestDto reportReq = GenerateSubsidiaryReportRequestDto.builder()
                .reportName("Q2 2026 Financial Audit Statement")
                .category("Financial")
                .dateRange("Q2 2026")
                .format("PDF")
                .build();
        SubsidiaryReportsPageDto.ReportItemDto report = subsidiaryAdminService.generateSubsidiaryReport(reportReq);
        assertNotNull(report);
        assertEquals("Q2 2026 Financial Audit Statement", report.getReportName());

        // Export All Reports bundle
        Map<String, String> export = subsidiaryAdminService.exportAllReports("Atirath Holdings");
        assertNotNull(export);
        assertEquals("READY", export.get("status"));
    }
}
