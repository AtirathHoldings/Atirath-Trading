package com.atirath.trading.modules.admin.parent;

import com.atirath.trading.modules.admin.parent.dto.*;
import com.atirath.trading.modules.admin.parent.service.ParentCompanyAdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ParentCompanyAdminServiceTest {

    private ParentCompanyAdminService adminService;

    @BeforeEach
    void setUp() {
        adminService = new ParentCompanyAdminService();
        adminService.init();
    }

    @Test
    @DisplayName("Screen 1: Verify Parent Company Dashboard matches screen design")
    void testGetDashboard() {
        ParentDashboardDto dashboard = adminService.getDashboard();

        assertNotNull(dashboard);
        assertEquals("Parent Company Dashboard", dashboard.getPageTitle());
        assertEquals("Today, 16 Sep 2026", dashboard.getDateFilter());

        // 5 Top KPI Cards
        assertEquals(5, dashboard.getKpis().size());
        assertEquals("Total Subsidiaries", dashboard.getKpis().get(0).getTitle());
        assertEquals("6", dashboard.getKpis().get(0).getValue());
        assertEquals("Total Members", dashboard.getKpis().get(1).getTitle());
        assertEquals("842", dashboard.getKpis().get(1).getValue());
        assertEquals("Total Trade Volume", dashboard.getKpis().get(2).getTitle());
        assertEquals("₹ 24.8 Cr", dashboard.getKpis().get(2).getValue());
        assertEquals("Total Settlement Value", dashboard.getKpis().get(3).getTitle());
        assertEquals("₹ 23.6 Cr", dashboard.getKpis().get(3).getValue());
        assertEquals("Escrow Balance", dashboard.getKpis().get(4).getTitle());
        assertEquals("₹ 4.2 Cr", dashboard.getKpis().get(4).getValue());

        // Group Company Performance: 6 companies
        assertEquals(6, dashboard.getGroupCompanyPerformance().size());
        assertEquals("Atirath Traders", dashboard.getGroupCompanyPerformance().get(0).getCompany());
        assertEquals(8.4, dashboard.getGroupCompanyPerformance().get(0).getVolumeCr());

        // Trade Activity Trend: 7 days
        assertEquals(7, dashboard.getTradeActivityTrends().size());

        // Market Overview
        assertEquals("₹ 24.8 Cr", dashboard.getMarketOverview().getTotalVolume());
        assertEquals(6, dashboard.getMarketOverview().getSlices().size());

        // Top Traded Securities: 5 rows
        assertEquals(5, dashboard.getTopTradedSecurities().size());
        assertEquals("ATIPL", dashboard.getTopTradedSecurities().get(0).getSecurity());
        assertEquals("Atirath Holdings", dashboard.getTopTradedSecurities().get(0).getCompany());

        // Pending Approvals: 5 categories
        assertEquals(5, dashboard.getPendingApprovals().size());

        // Recent Trade Activities: 5 rows
        assertEquals(5, dashboard.getRecentTradeActivities().size());

        // Recent Member & Investor Activity: 5 rows
        assertEquals(5, dashboard.getRecentMemberActivities().size());
    }

    @Test
    @DisplayName("Screen 2: Verify Subsidiary Companies Page matches screen design")
    void testGetSubsidiariesPage() {
        SubsidiariesPageDto page = adminService.getSubsidiariesPage(null, null, null);

        assertNotNull(page);
        assertEquals("Subsidiary Companies", page.getPageTitle());

        // 5 KPI Cards
        assertEquals(5, page.getKpis().size());
        assertEquals("6", page.getKpis().get(0).getValue()); // Total Subsidiaries
        assertEquals("5", page.getKpis().get(1).getValue()); // Active
        assertEquals("1", page.getKpis().get(2).getValue()); // Under Setup
        assertEquals("0", page.getKpis().get(3).getValue()); // Inactive
        assertEquals("842", page.getKpis().get(4).getValue()); // Total Employees

        // 6 Subsidiary companies
        assertEquals(6, page.getSubsidiaries().size());
        assertEquals("Atirath Traders India Pvt Ltd", page.getSubsidiaries().get(0).getCompanyName());
        assertEquals("Trading", page.getSubsidiaries().get(0).getSector());
        assertEquals("Active", page.getSubsidiaries().get(0).getStatus());
        assertEquals(325, page.getSubsidiaries().get(0).getTotalMembers());
        assertEquals("₹ 8.4 Cr", page.getSubsidiaries().get(0).getTotalTradeValue());

        assertEquals("Tri Energy Battery Pvt Ltd", page.getSubsidiaries().get(5).getCompanyName());
        assertEquals("Under Setup", page.getSubsidiaries().get(5).getStatus());

        // Sector Distribution
        assertEquals("₹ 24.8 Cr", page.getSectorDistribution().getTotalTradeValue());
        assertEquals(6, page.getSectorDistribution().getSlices().size());

        // Recent Updates
        assertEquals(5, page.getRecentUpdates().size());
    }

    @Test
    @DisplayName("Screen 3: Verify Subsidiary Company Details matches screen design")
    void testGetSubsidiaryDetail() {
        // Query by id "sub-1"
        SubsidiaryDetailDto detail = adminService.getSubsidiaryDetail("sub-1");

        assertNotNull(detail);
        assertEquals("Atirath Traders India Pvt Ltd", detail.getCompanyName());
        assertEquals("AT", detail.getInitials());
        assertEquals("Active", detail.getStatus());
        assertEquals("Import & Export Trading", detail.getSubIndustry());
        assertEquals("U51909TG2021PTC123456", detail.getCin());
        assertEquals("12 Jan 2021", detail.getIncorporationDate());
        assertEquals("Hyderabad, TG", detail.getHeadquarters());
        assertEquals("Trading", detail.getSector());
        assertEquals(325, detail.getTotalMembers());
        assertEquals("₹ 8.4 Cr", detail.getTotalTradeValue());

        // Available Tabs (8 tabs)
        assertEquals(8, detail.getAvailableTabs().size());
        assertTrue(detail.getAvailableTabs().contains("Overview"));
        assertTrue(detail.getAvailableTabs().contains("Financials"));
        assertTrue(detail.getAvailableTabs().contains("Shareholding"));
        assertTrue(detail.getAvailableTabs().contains("Members"));
        assertTrue(detail.getAvailableTabs().contains("Trading Activity"));
        assertTrue(detail.getAvailableTabs().contains("Compliance"));
        assertTrue(detail.getAvailableTabs().contains("Documents"));
        assertTrue(detail.getAvailableTabs().contains("Settings"));

        // Overview KPIs (5 cards)
        assertEquals(5, detail.getOverviewKpis().size());
        assertEquals("325", detail.getOverviewKpis().get(0).getValue());
        assertEquals("₹ 8.4 Cr", detail.getOverviewKpis().get(1).getValue());
        assertEquals("₹ 2.1 Cr", detail.getOverviewKpis().get(2).getValue());
        assertEquals("48", detail.getOverviewKpis().get(3).getValue());
        assertEquals("3", detail.getOverviewKpis().get(4).getValue());

        // Company Information
        assertNotNull(detail.getCompanyInfo());
        assertEquals("Atirath Traders India Pvt Ltd", detail.getCompanyInfo().getCompanyName());
        assertEquals("U51909TG2021PTC123456", detail.getCompanyInfo().getCin());
        assertEquals("12 Jan 2021", detail.getCompanyInfo().getIncorporationDate());
        assertEquals("Trading", detail.getCompanyInfo().getSector());
        assertEquals("Import & Export Trading", detail.getCompanyInfo().getIndustry());
        assertEquals("Plot No. 123, Financial District, Hyderabad, Telangana - 500032", detail.getCompanyInfo().getRegisteredAddress());
        assertEquals("Hyderabad, TG", detail.getCompanyInfo().getHeadquarters());
        assertEquals("AAACT1234A", detail.getCompanyInfo().getPan());
        assertEquals("36AAACT1234A1Z5", detail.getCompanyInfo().getGstNumber());
        assertEquals("Active", detail.getCompanyInfo().getStatus());

        // Key Metrics: 4 mini bar charts
        assertNotNull(detail.getKeyMetrics());
        assertEquals("Last 6 Months", detail.getKeyMetrics().getFilterRange());
        assertEquals("8.4", detail.getKeyMetrics().getTradeVolume().getCurrentValue());
        assertEquals("2.1", detail.getKeyMetrics().getSettlementValue().getCurrentValue());
        assertEquals("325", detail.getKeyMetrics().getActiveMembers().getCurrentValue());
        assertEquals("28", detail.getKeyMetrics().getNewMembers().getCurrentValue());

        // Recent Activities: 5 items
        assertEquals(5, detail.getRecentActivities().size());
        assertEquals("New trade order created", detail.getRecentActivities().get(0).getActivity());
        assertEquals("Ramesh Kumar", detail.getRecentActivities().get(0).getPerformedBy());

        // Compliance Status: 5 items all Compliant
        assertEquals(5, detail.getComplianceStatus().size());
        assertEquals("Compliant", detail.getComplianceStatus().get(0).getStatus());

        // Additional tabs populated
        assertNotNull(detail.getFinancials());
        assertNotNull(detail.getShareholding());
        assertNotNull(detail.getMembers());
        assertNotNull(detail.getTradingActivity());
        assertNotNull(detail.getCompliance());
        assertNotNull(detail.getDocuments());
        assertNotNull(detail.getSettings());
    }

    @Test
    @DisplayName("Verify flexible identifier resolution (sub-1, 1, CIN, slug)")
    void testFlexibleIdentifierResolution() {
        // By numeric string "1"
        SubsidiaryDetailDto byNum = adminService.getSubsidiaryDetail("1");
        assertNotNull(byNum);
        assertEquals("Atirath Traders India Pvt Ltd", byNum.getCompanyName());

        // By CIN
        SubsidiaryDetailDto byCin = adminService.getSubsidiaryDetail("U51909TG2021PTC123456");
        assertNotNull(byCin);
        assertEquals("Atirath Traders India Pvt Ltd", byCin.getCompanyName());

        // By company name / slug
        SubsidiaryDetailDto bySlug = adminService.getSubsidiaryDetail("atirath-traders");
        assertNotNull(bySlug);
        assertEquals("Atirath Traders India Pvt Ltd", bySlug.getCompanyName());

        // By sub-6 (Under setup company)
        SubsidiaryDetailDto sub6 = adminService.getSubsidiaryDetail("sub-6");
        assertNotNull(sub6);
        assertEquals("Tri Energy Battery Pvt Ltd", sub6.getCompanyName());
        assertEquals("Under Setup", sub6.getStatus());
    }

    @Test
    @DisplayName("Screen 4: Verify Group Share Capital matches screen design")
    void testGetShareCapitalPage() {
        GroupShareCapitalPageDto page = adminService.getShareCapitalPage(null, null);

        assertNotNull(page);
        assertEquals("Group Share Capital", page.getPageTitle());
        assertEquals("Today, 16 Sep 2026", page.getDateFilter());

        // 4 KPI Cards
        assertEquals(4, page.getKpis().size());
        assertEquals("Total Authorized Capital", page.getKpis().get(0).getTitle());
        assertEquals("₹ 500.0 Cr", page.getKpis().get(0).getValue());
        assertEquals("Total Issued Capital", page.getKpis().get(1).getTitle());
        assertEquals("₹ 350.0 Cr", page.getKpis().get(1).getValue());
        assertEquals("70.0% of authorized", page.getKpis().get(1).getChange());
        assertEquals("Total Paid-up Capital", page.getKpis().get(2).getTitle());
        assertEquals("₹ 324.5 Cr", page.getKpis().get(2).getValue());
        assertEquals("92.7% of issued", page.getKpis().get(2).getChange());
        assertEquals("Total Shareholders", page.getKpis().get(3).getTitle());
        assertEquals("1,248", page.getKpis().get(3).getValue());

        // Share Capital Structure (Group) Donut Chart: 6 slices
        assertNotNull(page.getShareCapitalStructure());
        assertEquals("₹ 500.0 Cr", page.getShareCapitalStructure().getTotalAuthorizedCapital());
        assertEquals(6, page.getShareCapitalStructure().getSlices().size());
        assertEquals("Atirath Traders", page.getShareCapitalStructure().getSlices().get(0).getCompany());
        assertEquals(32.0, page.getShareCapitalStructure().getSlices().get(0).getPercentage());
        assertEquals("Atirath Holdings", page.getShareCapitalStructure().getSlices().get(1).getCompany());
        assertEquals(24.0, page.getShareCapitalStructure().getSlices().get(1).getPercentage());

        // Authorized vs Issued vs Paid-up Capital Bar Chart
        assertNotNull(page.getCapitalComparisonChart());
        assertEquals(500.0, page.getCapitalComparisonChart().getAuthorizedCapitalCr());
        assertEquals(350.0, page.getCapitalComparisonChart().getIssuedCapitalCr());
        assertEquals(324.5, page.getCapitalComparisonChart().getPaidUpCapitalCr());

        // Share Capital Details by Subsidiary: 6 items
        assertEquals(6, page.getShareCapitalDetails().size());
        assertEquals("Atirath Traders India Pvt Ltd", page.getShareCapitalDetails().get(0).getCompanyName());
        assertEquals("Trading", page.getShareCapitalDetails().get(0).getSector());
        assertEquals(160.0, page.getShareCapitalDetails().get(0).getAuthorizedCapitalCr());
        assertEquals(112.0, page.getShareCapitalDetails().get(0).getIssuedCapitalCr());
        assertEquals(104.5, page.getShareCapitalDetails().get(0).getPaidUpCapitalCr());
        assertEquals("10,45,00,000", page.getShareCapitalDetails().get(0).getNumberOfShares());
        assertEquals(10, page.getShareCapitalDetails().get(0).getFaceValue());
        assertEquals("Active", page.getShareCapitalDetails().get(0).getStatus());

        assertEquals("Tri Energy Battery Pvt Ltd", page.getShareCapitalDetails().get(5).getCompanyName());
        assertEquals("Under Setup", page.getShareCapitalDetails().get(5).getStatus());

        // Recent Share Capital Activities: 5 items
        assertEquals(5, page.getRecentActivities().size());
        assertEquals("Atirath Holdings", page.getRecentActivities().get(0).getCompany());
        assertEquals("Issued Capital", page.getRecentActivities().get(0).getActivity());

        // Key Insights: 4 items
        assertEquals(4, page.getKeyInsights().size());
        assertEquals("70.0%", page.getKeyInsights().get(0).getMetric());
        assertEquals("Overall issued capital utilization", page.getKeyInsights().get(0).getLabel());

        // Test filter by query
        GroupShareCapitalPageDto filteredByQuery = adminService.getShareCapitalPage("Vantara", null);
        assertEquals(1, filteredByQuery.getShareCapitalDetails().size());
        assertEquals("Farm Vantara AgriTech Pvt Ltd", filteredByQuery.getShareCapitalDetails().get(0).getCompanyName());

        // Test filter by sector
        GroupShareCapitalPageDto filteredBySector = adminService.getShareCapitalPage(null, "Retail");
        assertEquals(1, filteredBySector.getShareCapitalDetails().size());
        assertEquals("Goeazymart E-Commerce Pvt Ltd", filteredBySector.getShareCapitalDetails().get(0).getCompanyName());
    }

    @Test
    @DisplayName("Screen 5: Verify Group Shareholders matches screen design with pagination and category breakdown")
    void testGetShareholdersPage() {
        GroupShareholdersPageDto page = adminService.getShareholdersPage(null, null, null, 1, 10);

        assertNotNull(page);
        assertEquals("Group Shareholders", page.getPageTitle());
        assertEquals("Today, 16 Sep 2026", page.getDateFilter());
        assertEquals(1248, page.getTotalShareholders());
        assertEquals(1, page.getCurrentPage());
        assertEquals(125, page.getTotalPages());

        // 5 KPI Cards
        assertEquals(5, page.getKpis().size());
        assertEquals("Total Shareholders", page.getKpis().get(0).getTitle());
        assertEquals("1,248", page.getKpis().get(0).getValue());
        assertEquals("Individual Shareholders", page.getKpis().get(1).getTitle());
        assertEquals("892", page.getKpis().get(1).getValue());
        assertEquals("71.5% of total", page.getKpis().get(1).getChange());
        assertEquals("Institutional Shareholders", page.getKpis().get(2).getTitle());
        assertEquals("256", page.getKpis().get(2).getValue());
        assertEquals("20.5% of total", page.getKpis().get(2).getChange());
        assertEquals("Promoter Shareholders", page.getKpis().get(3).getTitle());
        assertEquals("48", page.getKpis().get(3).getValue());
        assertEquals("3.8% of total", page.getKpis().get(3).getChange());
        assertEquals("Foreign Shareholders", page.getKpis().get(4).getTitle());
        assertEquals("52", page.getKpis().get(4).getValue());
        assertEquals("4.2% of total", page.getKpis().get(4).getChange());

        // Shareholder Category Distribution Donut Chart
        assertNotNull(page.getCategoryDistribution());
        assertEquals("1,248", page.getCategoryDistribution().getTotalShareholders());
        assertEquals(4, page.getCategoryDistribution().getSlices().size());
        assertEquals("Individual", page.getCategoryDistribution().getSlices().get(0).getCategory());
        assertEquals(71.5, page.getCategoryDistribution().getSlices().get(0).getPercentage());
        assertEquals(892, page.getCategoryDistribution().getSlices().get(0).getCount());

        // Top Shareholders by Shareholding Bar Chart (Top 10)
        assertEquals(10, page.getTopShareholders().size());
        assertEquals("Atirath Holdings (Promoter)", page.getTopShareholders().get(0).getShareholderName());
        assertEquals(24.0, page.getTopShareholders().get(0).getPercentage());
        assertEquals("Ramesh Kumar (Individual)", page.getTopShareholders().get(1).getShareholderName());
        assertEquals(8.5, page.getTopShareholders().get(1).getPercentage());
        assertEquals("Global Renewables Ltd (Institutional)", page.getTopShareholders().get(2).getShareholderName());
        assertEquals(6.2, page.getTopShareholders().get(2).getPercentage());

        // Shareholder List: 10 rows matching exact screen entries
        assertEquals(10, page.getShareholders().size());
        GroupShareholdersPageDto.ShareholderItemDto first = page.getShareholders().get(0);
        assertEquals("Atirath Holdings India Limited", first.getShareholderName());
        assertEquals("AH", first.getInitials());
        assertEquals("Corporate", first.getType());
        assertEquals("Promoter", first.getCategory());
        assertEquals("India", first.getCountry());
        assertEquals("24,00,00,000", first.getNoOfShares());
        assertEquals(24.0, first.getShareholdingPercent());
        assertEquals("₹ 120.0 Cr", first.getTotalValue());
        assertEquals("Verified", first.getKycStatus());
        assertEquals("Active", first.getStatus());

        // Check Individual shareholder
        GroupShareholdersPageDto.ShareholderItemDto second = page.getShareholders().get(1);
        assertEquals("Ramesh Kumar", second.getShareholderName());
        assertEquals("Individual", second.getType());
        assertEquals("Individual", second.getCategory());
        assertEquals("8,50,00,000", second.getNoOfShares());
        assertEquals(8.5, second.getShareholdingPercent());
        assertEquals("₹ 42.5 Cr", second.getTotalValue());

        // Check Foreign shareholder
        GroupShareholdersPageDto.ShareholderItemDto fifth = page.getShareholders().get(4);
        assertEquals("Sunrise Capital", fifth.getShareholderName());
        assertEquals("Foreign", fifth.getCategory());
        assertEquals("USA", fifth.getCountry());

        // Test category filter
        GroupShareholdersPageDto filteredCat = adminService.getShareholdersPage(null, "Promoter", null, 1, 10);
        assertEquals(2, filteredCat.getShareholders().size());

        // Test addShareholder
        AddShareholderRequestDto newShareholder = AddShareholderRequestDto.builder()
                .shareholderName("Aravind Swamy")
                .type("Individual")
                .category("Individual")
                .country("India")
                .noOfShares("10,00,000")
                .shareholdingPercent(0.1)
                .totalValue("₹ 50.0 L")
                .build();
        GroupShareholdersPageDto.ShareholderItemDto created = adminService.addShareholder(newShareholder);
        assertNotNull(created);
        assertEquals("Aravind Swamy", created.getShareholderName());
        assertEquals("AR", created.getInitials());
    }

    @Test
    @DisplayName("Screen 6: Verify Group Holdings matches screen design with valuations and sector breakdown")
    void testGetGroupHoldingsPage() {
        GroupHoldingsPageDto page = adminService.getGroupHoldingsPage(null, null);

        assertNotNull(page);
        assertEquals("Group Holdings", page.getPageTitle());
        assertEquals("Today, 16 Sep 2026", page.getDateFilter());

        // 4 KPI Cards
        assertEquals(4, page.getKpis().size());
        assertEquals("Total Group Holdings Value", page.getKpis().get(0).getTitle());
        assertEquals("₹ 1,248.5 Cr", page.getKpis().get(0).getValue());
        assertEquals("+18% vs last quarter", page.getKpis().get(0).getChange());
        assertEquals("Total Shares Held", page.getKpis().get(1).getTitle());
        assertEquals("41.7 Crore", page.getKpis().get(1).getValue());
        assertEquals("+12% vs last quarter", page.getKpis().get(1).getChange());
        assertEquals("Companies in Group", page.getKpis().get(2).getTitle());
        assertEquals("6", page.getKpis().get(2).getValue());
        assertEquals("Total Shareholders", page.getKpis().get(3).getTitle());
        assertEquals("1,248", page.getKpis().get(3).getValue());

        // Holdings by Company Bar Chart: 6 companies
        assertEquals(6, page.getHoldingsByCompany().size());
        assertEquals("Atirath Traders", page.getHoldingsByCompany().get(0).getCompany());
        assertEquals(312.5, page.getHoldingsByCompany().get(0).getValueCr());
        assertEquals("Atirath Holdings", page.getHoldingsByCompany().get(1).getCompany());
        assertEquals(248.0, page.getHoldingsByCompany().get(1).getValueCr());
        assertEquals("Atirath Technologies", page.getHoldingsByCompany().get(2).getCompany());
        assertEquals(186.4, page.getHoldingsByCompany().get(2).getValueCr());

        // Sector-wise Holdings Value Donut Chart: 7 slices
        assertNotNull(page.getSectorHoldings());
        assertEquals("₹ 1,248.5 Cr", page.getSectorHoldings().getTotalValue());
        assertEquals(7, page.getSectorHoldings().getSlices().size());
        assertEquals("Trading", page.getSectorHoldings().getSlices().get(0).getSector());
        assertEquals(25.0, page.getSectorHoldings().getSlices().get(0).getPercentage());
        assertEquals("₹ 312.5 Cr", page.getSectorHoldings().getSlices().get(0).getValueFormatted());

        assertEquals("Renewable Energy", page.getSectorHoldings().getSlices().get(1).getSector());
        assertEquals(19.9, page.getSectorHoldings().getSlices().get(1).getPercentage());
        assertEquals("₹ 248.0 Cr", page.getSectorHoldings().getSlices().get(1).getValueFormatted());

        // Group Holdings Details: 6 rows
        assertEquals(6, page.getHoldingsDetails().size());
        GroupHoldingsPageDto.GroupHoldingItemDto first = page.getHoldingsDetails().get(0);
        assertEquals("Atirath Traders India Pvt Ltd", first.getCompanyName());
        assertEquals("Trading", first.getSector());
        assertEquals("10,45,00,000", first.getTotalShares());
        assertEquals(25.0, first.getShareholdingPercent());
        assertEquals("₹ 312.5 Cr", first.getTotalValue());
        assertEquals("₹ 299.80", first.getLastValuation());
        assertEquals("+2.4%", first.getChange24h());

        GroupHoldingsPageDto.GroupHoldingItemDto third = page.getHoldingsDetails().get(2);
        assertEquals("Atirath Technologies Pvt Ltd", third.getCompanyName());
        assertEquals("-0.6%", third.getChange24h());

        // Recent Holdings Activity: 5 items
        assertEquals(5, page.getRecentActivities().size());
        assertEquals("Atirath Traders", page.getRecentActivities().get(0).getCompany());
        assertEquals("Share Transfer", page.getRecentActivities().get(0).getActivity());

        // Key Insights: 5 items
        assertEquals(5, page.getKeyInsights().size());
        assertEquals("Concentration Risk", page.getKeyInsights().get(0).getTitle());
        assertEquals("Sector Diversification", page.getKeyInsights().get(1).getTitle());
        assertEquals("Growth Trend", page.getKeyInsights().get(2).getTitle());
        assertEquals("Liquidity Position", page.getKeyInsights().get(3).getTitle());
        assertEquals("New Opportunities", page.getKeyInsights().get(4).getTitle());

        // Filter by sector
        GroupHoldingsPageDto filteredSector = adminService.getGroupHoldingsPage(null, "Technology");
        assertEquals(1, filteredSector.getHoldingsDetails().size());
        assertEquals("Atirath Technologies Pvt Ltd", filteredSector.getHoldingsDetails().get(0).getCompanyName());
    }

    @Test
    @DisplayName("Screen 7: Verify Market Activity (Group Level) matches screen design")
    void testGetMarketActivityPage() {
        MarketActivityGroupPageDto page = adminService.getMarketActivityPage(null, null, null, 1, 6, "Today, 16 Sep 2026");

        assertNotNull(page);
        assertEquals("Market Activity (Group Level)", page.getPageTitle());
        assertEquals("Today, 16 Sep 2026", page.getDateFilter());

        // 5 KPI Cards
        assertEquals(5, page.getKpis().size());
        assertEquals("Total Trade Volume", page.getKpis().get(0).getTitle());
        assertEquals("₹ 142.6 Cr", page.getKpis().get(0).getValue());
        assertEquals("+18% vs last quarter", page.getKpis().get(0).getChange());
        assertEquals("Total Trades", page.getKpis().get(1).getTitle());
        assertEquals("1,248", page.getKpis().get(1).getValue());
        assertEquals("+12% vs last quarter", page.getKpis().get(1).getChange());
        assertEquals("Active Members", page.getKpis().get(2).getTitle());
        assertEquals("842", page.getKpis().get(2).getValue());
        assertEquals("+8% vs last quarter", page.getKpis().get(2).getChange());
        assertEquals("Companies Traded", page.getKpis().get(3).getTitle());
        assertEquals("6", page.getKpis().get(3).getValue());
        assertEquals("Avg. Trade Value", page.getKpis().get(4).getTitle());
        assertEquals("₹ 11.4 L", page.getKpis().get(4).getValue());
        assertEquals("+5% vs last quarter", page.getKpis().get(4).getChange());

        // Trade Volume Trend: 6 months
        assertNotNull(page.getTradeVolumeTrend());
        assertEquals("Last 6 Months", page.getTradeVolumeTrend().getFilterRange());
        assertEquals(6, page.getTradeVolumeTrend().getMonthlyTrends().size());
        assertEquals("Apr", page.getTradeVolumeTrend().getMonthlyTrends().get(0).getMonth());
        assertEquals(18.2, page.getTradeVolumeTrend().getMonthlyTrends().get(0).getTradeVolumeCr());
        assertEquals("Sep", page.getTradeVolumeTrend().getMonthlyTrends().get(5).getMonth());
        assertEquals(36.2, page.getTradeVolumeTrend().getMonthlyTrends().get(5).getTradeVolumeCr());

        // Sector-wise Trade Distribution Donut: 7 slices
        assertNotNull(page.getSectorTradeDistribution());
        assertEquals("₹ 142.6 Cr", page.getSectorTradeDistribution().getTotalTradeValue());
        assertEquals(7, page.getSectorTradeDistribution().getSlices().size());
        assertEquals("Trading", page.getSectorTradeDistribution().getSlices().get(0).getSector());
        assertEquals(33.2, page.getSectorTradeDistribution().getSlices().get(0).getPercentage());
        assertEquals("₹ 47.4 Cr", page.getSectorTradeDistribution().getSlices().get(0).getValueFormatted());

        assertEquals("Renewable Energy", page.getSectorTradeDistribution().getSlices().get(1).getSector());
        assertEquals(21.5, page.getSectorTradeDistribution().getSlices().get(1).getPercentage());
        assertEquals("₹ 30.6 Cr", page.getSectorTradeDistribution().getSlices().get(1).getValueFormatted());

        // Recent Market Activity Table: 6 trades matching Image 1
        assertEquals(6, page.getRecentTrades().size());
        MarketActivityGroupPageDto.MarketActivityTradeDto trade1 = page.getRecentTrades().get(0);
        assertEquals("Atirath Traders", trade1.getCompany());
        assertEquals("AT", trade1.getCompanyInitials());
        assertEquals("Buy", trade1.getType());
        assertEquals("Equity", trade1.getSecurity());
        assertEquals("50,000", trade1.getQuantity());
        assertEquals("299.80", trade1.getPrice());
        assertEquals("1,49,90,000", trade1.getTradeValue());
        assertEquals("Ramesh Kumar", trade1.getBuyer());
        assertEquals("Global Renewables", trade1.getSeller());
        assertEquals("Settled", trade1.getStatus());

        MarketActivityGroupPageDto.MarketActivityTradeDto trade2 = page.getRecentTrades().get(1);
        assertEquals("Atirath Holdings", trade2.getCompany());
        assertEquals("Sell", trade2.getType());
        assertEquals("Settled", trade2.getStatus());

        MarketActivityGroupPageDto.MarketActivityTradeDto trade3 = page.getRecentTrades().get(2);
        assertEquals("Atirath Technologies", trade3.getCompany());
        assertEquals("Buy", trade3.getType());
        assertEquals("Pending", trade3.getStatus());

        // Pagination
        assertEquals(1248, page.getTotalTrades());
        assertEquals(1, page.getCurrentPage());
        assertEquals(125, page.getTotalPages());

        // Top Traded Companies (By Trade Value): 5 companies
        assertEquals(5, page.getTopTradedCompanies().size());
        assertEquals("Atirath Traders", page.getTopTradedCompanies().get(0).getCompany());
        assertEquals("47.4 Cr", page.getTopTradedCompanies().get(0).getTradeValue());
        assertEquals("Atirath Holdings", page.getTopTradedCompanies().get(1).getCompany());
        assertEquals("30.6 Cr", page.getTopTradedCompanies().get(1).getTradeValue());
        assertEquals("Atirath Technologies", page.getTopTradedCompanies().get(2).getCompany());
        assertEquals("23.9 Cr", page.getTopTradedCompanies().get(2).getTradeValue());
        assertEquals("Farm Vantara", page.getTopTradedCompanies().get(3).getCompany());
        assertEquals("16.0 Cr", page.getTopTradedCompanies().get(3).getTradeValue());
        assertEquals("#a855f7", page.getTopTradedCompanies().get(3).getColor());
        assertEquals("Goeazymart", page.getTopTradedCompanies().get(4).getCompany());
        assertEquals("12.0 Cr", page.getTopTradedCompanies().get(4).getTradeValue());

        // Market Activity Highlights: 6 items
        assertEquals(6, page.getHighlights().size());
        assertEquals("Trade volume increased by 18% compared to last quarter", page.getHighlights().get(0).getDescription());
        assertEquals("No unusual market activity detected", page.getHighlights().get(5).getDescription());

        // Filter by company
        MarketActivityGroupPageDto filtered = adminService.getMarketActivityPage(null, null, "Farm Vantara", 1, 6, null);
        assertEquals(1, filtered.getRecentTrades().size());
        assertEquals("Farm Vantara", filtered.getRecentTrades().get(0).getCompany());
    }

    @Test
    @DisplayName("Screen 8: Verify Group Trade Monitoring matches screen design")
    void testGetTradeMonitoringPage() {
        GroupTradeMonitoringPageDto page = adminService.getTradeMonitoringPage(null, null, null, null, 1, 8, "01 Sep 2026 - 16 Sep 2026");

        assertNotNull(page);
        assertEquals("Group Trade Monitoring", page.getPageTitle());
        assertEquals("01 Sep 2026 - 16 Sep 2026", page.getDateRange());

        // 5 KPI Cards
        assertEquals(5, page.getKpis().size());
        assertEquals("Total Trades", page.getKpis().get(0).getTitle());
        assertEquals("1,248", page.getKpis().get(0).getValue());
        assertEquals("+12% vs last quarter", page.getKpis().get(0).getChange());
        assertEquals("Total Trade Value", page.getKpis().get(1).getTitle());
        assertEquals("₹ 142.6 Cr", page.getKpis().get(1).getValue());
        assertEquals("+18% vs last quarter", page.getKpis().get(1).getChange());
        assertEquals("Active Traders", page.getKpis().get(2).getTitle());
        assertEquals("842", page.getKpis().get(2).getValue());
        assertEquals("+8% vs last quarter", page.getKpis().get(2).getChange());
        assertEquals("Companies Involved", page.getKpis().get(3).getTitle());
        assertEquals("6", page.getKpis().get(3).getValue());
        assertEquals("Avg. Settlement Time", page.getKpis().get(4).getTitle());
        assertEquals("1.2 Days", page.getKpis().get(4).getValue());
        assertEquals("-28% vs last quarter", page.getKpis().get(4).getChange());

        // Trade Value & Volume Trend: 6 months
        assertNotNull(page.getTrendChart());
        assertEquals("Last 6 Months", page.getTrendChart().getFilterRange());
        assertEquals(6, page.getTrendChart().getMonthlyTrends().size());

        // Sector Distribution Donut: 7 slices
        assertNotNull(page.getSectorDistribution());
        assertEquals("₹ 142.6 Cr", page.getSectorDistribution().getTotalTradeValue());
        assertEquals(7, page.getSectorDistribution().getSlices().size());

        // Recent Trades Table: 8 rows matching Image 2
        assertEquals(8, page.getRecentTrades().size());
        GroupTradeMonitoringPageDto.TradeMonitoringItemDto row1 = page.getRecentTrades().get(0);
        assertEquals("TRD-100482", row1.getTradeId());
        assertEquals("16 Sep 2026, 11:20 AM", row1.getDateTime());
        assertEquals("Ramesh Kumar", row1.getBuyer());
        assertEquals("Global Renewables", row1.getSeller());
        assertEquals("Atirath Traders", row1.getCompany());
        assertEquals("Equity", row1.getSecurity());
        assertEquals("50,000", row1.getQuantity());
        assertEquals("299.80", row1.getPrice());
        assertEquals("1,49,90,000", row1.getTradeValue());
        assertEquals("Buy", row1.getType());
        assertEquals("Settled", row1.getStatus());
        assertEquals("18 Sep 2026", row1.getSettlementDate());

        // Check Failed trade
        GroupTradeMonitoringPageDto.TradeMonitoringItemDto row6 = page.getRecentTrades().get(5);
        assertEquals("TRD-100477", row6.getTradeId());
        assertEquals("Failed", row6.getStatus());
        assertEquals("-", row6.getSettlementDate());

        // Check Row 8 Buy
        GroupTradeMonitoringPageDto.TradeMonitoringItemDto row8 = page.getRecentTrades().get(7);
        assertEquals("TRD-100475", row8.getTradeId());
        assertEquals("Buy", row8.getType());
        assertEquals("Settled", row8.getStatus());

        // Pagination
        assertEquals(1248, page.getTotalTrades());
        assertEquals(1, page.getCurrentPage());
        assertEquals(156, page.getTotalPages());

        // Unusual Trade Activity: 4 items
        assertEquals(4, page.getUnusualActivities().size());
        assertEquals("TRD-100481", page.getUnusualActivities().get(0).getTradeId());
        assertEquals("High Value Trade", page.getUnusualActivities().get(0).getAlertType());
        assertEquals("Under Review", page.getUnusualActivities().get(0).getStatus());

        assertEquals("TRD-100480", page.getUnusualActivities().get(1).getTradeId());
        assertEquals("Large Quantity", page.getUnusualActivities().get(1).getAlertType());
        assertEquals("Investigating", page.getUnusualActivities().get(1).getStatus());

        // Monitoring Insights: 6 items
        assertEquals(6, page.getInsights().size());
        assertEquals("Trade volume increased by 18% compared to last quarter", page.getInsights().get(0).getDescription());
        assertEquals("All trades are within regulatory limits", page.getInsights().get(5).getDescription());

        // Filter by type
        GroupTradeMonitoringPageDto filteredType = adminService.getTradeMonitoringPage(null, null, null, "Sell", 1, 8, null);
        assertTrue(filteredType.getRecentTrades().stream().allMatch(t -> t.getType().equalsIgnoreCase("Sell")));
    }

    @Test
    @DisplayName("Screen 9: Verify Escrow & Settlement (Group Level) matches screen design")
    void testGetEscrowSettlementPage() {
        EscrowSettlementGroupPageDto page = adminService.getEscrowSettlementPage(null, null, null, null, "Escrow Transactions", 1, 8, "01 Sep 2026 - 16 Sep 2026");

        assertNotNull(page);
        assertEquals("Escrow & Settlement (Group Level)", page.getPageTitle());
        assertEquals("01 Sep 2026 - 16 Sep 2026", page.getDateRange());

        // 5 KPI Cards
        assertEquals(5, page.getKpis().size());
        assertEquals("Total Escrow Balance", page.getKpis().get(0).getTitle());
        assertEquals("₹ 58.4 Cr", page.getKpis().get(0).getValue());
        assertEquals("+12% vs last quarter", page.getKpis().get(0).getChange());
        assertEquals("Total Settled Amount", page.getKpis().get(1).getTitle());
        assertEquals("₹ 142.6 Cr", page.getKpis().get(1).getValue());
        assertEquals("+18% vs last quarter", page.getKpis().get(1).getChange());
        assertEquals("Pending Settlement", page.getKpis().get(2).getTitle());
        assertEquals("₹ 12.3 Cr", page.getKpis().get(2).getValue());
        assertEquals("-6% vs last quarter", page.getKpis().get(2).getChange());
        assertEquals("Total Transactions", page.getKpis().get(3).getTitle());
        assertEquals("1,248", page.getKpis().get(3).getValue());
        assertEquals("+22% vs last quarter", page.getKpis().get(3).getChange());
        assertEquals("Dispute Amount", page.getKpis().get(4).getTitle());
        assertEquals("₹ 1.8 Cr", page.getKpis().get(4).getValue());
        assertEquals("-32% vs last quarter", page.getKpis().get(4).getChange());

        // Escrow Balance Trend: 6 months
        assertNotNull(page.getEscrowTrend());
        assertEquals("Last 6 Months", page.getEscrowTrend().getFilterRange());
        assertEquals(6, page.getEscrowTrend().getMonthlyData().size());
        assertEquals("Apr", page.getEscrowTrend().getMonthlyData().get(0).getMonth());
        assertEquals(32.5, page.getEscrowTrend().getMonthlyData().get(0).getAmountCr());
        assertEquals("Sep", page.getEscrowTrend().getMonthlyData().get(5).getMonth());
        assertEquals(58.4, page.getEscrowTrend().getMonthlyData().get(5).getAmountCr());

        // Settlement Status Donut: 4 slices
        assertNotNull(page.getSettlementStatus());
        assertEquals("₹ 142.6 Cr", page.getSettlementStatus().getTotalTrades());
        assertEquals(4, page.getSettlementStatus().getSlices().size());
        assertEquals("Settled", page.getSettlementStatus().getSlices().get(0).getStatus());
        assertEquals(72.4, page.getSettlementStatus().getSlices().get(0).getPercentage());
        assertEquals("₹ 103.3 Cr", page.getSettlementStatus().getSlices().get(0).getAmountFormatted());

        assertEquals("Pending", page.getSettlementStatus().getSlices().get(1).getStatus());
        assertEquals(18.1, page.getSettlementStatus().getSlices().get(1).getPercentage());
        assertEquals("₹ 25.8 Cr", page.getSettlementStatus().getSlices().get(1).getAmountFormatted());

        assertEquals("Under Review", page.getSettlementStatus().getSlices().get(2).getStatus());
        assertEquals(6.2, page.getSettlementStatus().getSlices().get(2).getPercentage());
        assertEquals("₹ 8.8 Cr", page.getSettlementStatus().getSlices().get(2).getAmountFormatted());

        assertEquals("Disputed", page.getSettlementStatus().getSlices().get(3).getStatus());
        assertEquals(3.3, page.getSettlementStatus().getSlices().get(3).getPercentage());
        assertEquals("₹ 4.7 Cr", page.getSettlementStatus().getSlices().get(3).getAmountFormatted());

        // 5 Tabs
        assertEquals(5, page.getTabs().size());
        assertTrue(page.getTabs().contains("Escrow Transactions"));
        assertTrue(page.getTabs().contains("Pending Settlements"));
        assertTrue(page.getTabs().contains("Dispute Management"));
        assertTrue(page.getTabs().contains("Escrow Accounts"));
        assertTrue(page.getTabs().contains("Settlement Reports"));
        assertEquals("Escrow Transactions", page.getActiveTab());

        // Escrow Transactions Table: 8 items matching Image 3
        assertEquals(8, page.getTransactions().size());
        EscrowSettlementGroupPageDto.EscrowTransactionItemDto tx1 = page.getTransactions().get(0);
        assertEquals("ESC-100482", tx1.getTradeId());
        assertEquals("16 Sep 2026, 11:20 AM", tx1.getDateTime());
        assertEquals("Ramesh Kumar", tx1.getBuyer());
        assertEquals("Global Renewables", tx1.getSeller());
        assertEquals("Atirath Traders", tx1.getCompany());
        assertEquals("1,49,90,000", tx1.getEscrowAmount());
        assertEquals("1,49,90,000", tx1.getTradeValue());
        assertEquals("18 Sep 2026", tx1.getSettlementDate());
        assertEquals("Settled", tx1.getStatus());

        // Disputed transaction
        EscrowSettlementGroupPageDto.EscrowTransactionItemDto tx4 = page.getTransactions().get(3);
        assertEquals("ESC-100479", tx4.getTradeId());
        assertEquals("Disputed", tx4.getStatus());

        // Pagination
        assertEquals(1248, page.getTotalTransactions());
        assertEquals(1, page.getCurrentPage());
        assertEquals(156, page.getTotalPages());

        // Pending Settlement Summary: 5 companies
        assertEquals(5, page.getPendingSettlementSummary().size());
        assertEquals("Atirath Holdings", page.getPendingSettlementSummary().get(0).getCompany());
        assertEquals(12, page.getPendingSettlementSummary().get(0).getNumberOfTrades());
        assertEquals("25,80,000", page.getPendingSettlementSummary().get(0).getAmount());

        // Recent Settlement Activity: 5 items
        assertEquals(5, page.getRecentSettlementActivity().size());
        assertEquals("ESC-100482", page.getRecentSettlementActivity().get(0).getTradeId());
        assertEquals("Atirath Traders", page.getRecentSettlementActivity().get(0).getCompany());
        assertEquals("1,49,90,000", page.getRecentSettlementActivity().get(0).getAmount());
        assertEquals("Settled", page.getRecentSettlementActivity().get(0).getStatus());

        // Key Insights: 5 items
        assertEquals(5, page.getKeyInsights().size());
        assertEquals("72.4% of trades are settled within T+2 days", page.getKeyInsights().get(0).getDescription());
        assertEquals("Settlement success rate: 96.7%", page.getKeyInsights().get(4).getDescription());

        // Test addEscrowTransaction
        AddEscrowRequestDto req = AddEscrowRequestDto.builder()
                .buyer("Karthik Raja")
                .seller("Atirath Traders")
                .company("Atirath Traders")
                .escrowAmount("50,00,000")
                .settlementDate("20 Sep 2026")
                .build();
        EscrowSettlementGroupPageDto.EscrowTransactionItemDto created = adminService.addEscrowTransaction(req);
        assertNotNull(created);
        assertEquals("Karthik Raja", created.getBuyer());
        assertEquals("Pending", created.getStatus());
    }

    @Test
    @DisplayName("Screen 10: Verify Approvals matches screen design with tabs, table, and details flyout")
    void testGetApprovalsPage() {
        ApprovalsPageDto page = adminService.getApprovalsPage(null, null, null, null, null, "All", 1, 10, "01 Sep 2026 - 16 Sep 2026");

        assertNotNull(page);
        assertEquals("Approvals", page.getPageTitle());
        assertEquals("01 Sep 2026 - 16 Sep 2026", page.getDateRange());

        // 5 KPI Cards
        assertEquals(5, page.getKpis().size());
        assertEquals("Total Requests", page.getKpis().get(0).getTitle());
        assertEquals("48", page.getKpis().get(0).getValue());
        assertEquals("+12% vs last week", page.getKpis().get(0).getChange());
        assertEquals("Pending", page.getKpis().get(1).getTitle());
        assertEquals("21", page.getKpis().get(1).getValue());
        assertEquals("+5% vs last week", page.getKpis().get(1).getChange());
        assertEquals("Approved", page.getKpis().get(2).getTitle());
        assertEquals("22", page.getKpis().get(2).getValue());
        assertEquals("+37% vs last week", page.getKpis().get(2).getChange());
        assertEquals("Rejected", page.getKpis().get(3).getTitle());
        assertEquals("3", page.getKpis().get(3).getValue());
        assertEquals("-25% vs last week", page.getKpis().get(3).getChange());
        assertEquals("Avg. Approval Time", page.getKpis().get(4).getTitle());
        assertEquals("1.8 Days", page.getKpis().get(4).getValue());
        assertEquals("-28% vs last week", page.getKpis().get(4).getChange());

        // 5 Tabs
        assertEquals(5, page.getTabs().size());
        assertEquals(48, page.getTabs().get(0).getCount());
        assertEquals(21, page.getTabs().get(1).getCount());
        assertEquals(4, page.getTabs().get(2).getCount());
        assertEquals(22, page.getTabs().get(3).getCount());
        assertEquals(3, page.getTabs().get(4).getCount());

        // Approvals Table: 10 items matching Image 1
        assertEquals(10, page.getApprovals().size());
        ApprovalsPageDto.ApprovalItemDto app1 = page.getApprovals().get(0);
        assertEquals("APR-2026-1042", app1.getRequestId());
        assertEquals("16 Sep 2026, 11:20 AM", app1.getDateTime());
        assertEquals("Share Transfer", app1.getApprovalType());
        assertEquals("Atirath Traders", app1.getCompany());
        assertEquals("Ramesh Kumar", app1.getRequestedBy());
        assertEquals("50,000 shares", app1.getAmountOrDetails());
        assertEquals("High", app1.getPriority());
        assertEquals("Pending", app1.getStatus());

        ApprovalsPageDto.ApprovalItemDto app2 = page.getApprovals().get(1);
        assertEquals("APR-2026-1041", app2.getRequestId());
        assertEquals("Trade Settlement", app2.getApprovalType());
        assertEquals("Under Review", app2.getStatus());

        ApprovalsPageDto.ApprovalItemDto app3 = page.getApprovals().get(2);
        assertEquals("APR-2026-1040", app3.getRequestId());
        assertEquals("Pending", app3.getStatus());

        // Flyout Details
        assertNotNull(page.getSelectedApprovalDetail());
        assertEquals("APR-2026-1042", page.getSelectedApprovalDetail().getRequestId());
        assertEquals("Share Transfer Request", page.getSelectedApprovalDetail().getTitle());
        assertEquals("Pending", page.getSelectedApprovalDetail().getStatus());
        assertEquals("50,000 shares (0.5%)", page.getSelectedApprovalDetail().getShares());
        assertEquals(3, page.getSelectedApprovalDetail().getAttachments().size());

        // Donut: Approvals by Type (8 slices)
        assertNotNull(page.getApprovalsByType());
        assertEquals("48", page.getApprovalsByType().getTotalRequests());
        assertEquals(8, page.getApprovalsByType().getSlices().size());
        assertEquals("Share Transfer", page.getApprovalsByType().getSlices().get(0).getType());
        assertEquals(10, page.getApprovalsByType().getSlices().get(0).getCount());
        assertEquals(20.8, page.getApprovalsByType().getSlices().get(0).getPercentage());

        // Approval Trend Chart: 6 months
        assertNotNull(page.getApprovalTrend());
        assertEquals(6, page.getApprovalTrend().getMonthlyData().size());
        assertEquals("Apr", page.getApprovalTrend().getMonthlyData().get(0).getMonth());
        assertEquals(14, page.getApprovalTrend().getMonthlyData().get(0).getPending());
        assertEquals(8, page.getApprovalTrend().getMonthlyData().get(0).getApproved());
        assertEquals(2, page.getApprovalTrend().getMonthlyData().get(0).getRejected());

        // Test Approve / Reject
        ApprovalDetailDto approved = adminService.approveRequest("APR-2026-1042");
        assertEquals("Approved", approved.getStatus());

        ApprovalDetailDto rejected = adminService.rejectRequest("APR-2026-1041");
        assertEquals("Rejected", rejected.getStatus());
    }

    @Test
    @DisplayName("Screen 11: Verify External Investor Requests matches screen design")
    void testGetInvestorRequestsPage() {
        ExternalInvestorRequestsPageDto page = adminService.getInvestorRequestsPage(null, null, null, null, "All", 1, 10, "01 Sep 2026 - 16 Sep 2026");

        assertNotNull(page);
        assertEquals("External Investor Requests", page.getPageTitle());
        assertEquals("01 Sep 2026 - 16 Sep 2026", page.getDateRange());

        // 5 KPI Cards
        assertEquals(5, page.getKpis().size());
        assertEquals("Total Requests", page.getKpis().get(0).getTitle());
        assertEquals("36", page.getKpis().get(0).getValue());
        assertEquals("+20% vs last quarter", page.getKpis().get(0).getChange());
        assertEquals("Pending Review", page.getKpis().get(1).getTitle());
        assertEquals("12", page.getKpis().get(1).getValue());
        assertEquals("+33% vs last quarter", page.getKpis().get(1).getChange());
        assertEquals("Approved", page.getKpis().get(2).getTitle());
        assertEquals("15", page.getKpis().get(2).getValue());
        assertEquals("+25% vs last quarter", page.getKpis().get(2).getChange());
        assertEquals("Rejected", page.getKpis().get(3).getTitle());
        assertEquals("5", page.getKpis().get(3).getValue());
        assertEquals("-17% vs last quarter", page.getKpis().get(3).getChange());
        assertEquals("Total Investment Value", page.getKpis().get(4).getTitle());
        assertEquals("₹ 186.4 Cr", page.getKpis().get(4).getValue());
        assertEquals("+42% vs last quarter", page.getKpis().get(4).getChange());

        // 5 Tabs
        assertEquals(5, page.getTabs().size());
        assertEquals(36, page.getTabs().get(0).getCount());
        assertEquals(12, page.getTabs().get(1).getCount());
        assertEquals(8, page.getTabs().get(2).getCount());
        assertEquals(15, page.getTabs().get(3).getCount());
        assertEquals(5, page.getTabs().get(4).getCount());

        // Investor Requests Table: 10 items matching Image 2
        assertEquals(10, page.getRequests().size());
        ExternalInvestorRequestsPageDto.InvestorRequestItemDto req1 = page.getRequests().get(0);
        assertEquals("INV-2026-0036", req1.getRequestId());
        assertEquals("Rajesh Mehta", req1.getInvestorName());
        assertEquals("Farm Vantara", req1.getCompany());
        assertEquals("Agriculture", req1.getSector());
        assertEquals("25,00,00,000", req1.getInvestmentAmount());
        assertEquals("Pending", req1.getStatus());

        ExternalInvestorRequestsPageDto.InvestorRequestItemDto req2 = page.getRequests().get(1);
        assertEquals("INV-2026-0035", req2.getRequestId());
        assertEquals("Global Green Fund", req2.getInvestorName());
        assertEquals("Under Review", req2.getStatus());

        // Flyout Details
        assertNotNull(page.getSelectedRequestDetail());
        assertEquals("INV-2026-0036", page.getSelectedRequestDetail().getRequestId());
        assertEquals("Rajesh Mehta", page.getSelectedRequestDetail().getInvestorName());
        assertEquals("₹ 25,00,00,000", page.getSelectedRequestDetail().getProposedAmount());
        assertEquals("5%", page.getSelectedRequestDetail().getProposedEquity());
        assertEquals("18% (IRR)", page.getSelectedRequestDetail().getExpectedRoi());

        // Investment Requests by Company Donut: 8 slices
        assertNotNull(page.getRequestsByCompany());
        assertEquals("36", page.getRequestsByCompany().getTotalRequests());
        assertEquals(8, page.getRequestsByCompany().getSlices().size());
        assertEquals("Farm Vantara", page.getRequestsByCompany().getSlices().get(0).getCompany());
        assertEquals(8, page.getRequestsByCompany().getSlices().get(0).getCount());
        assertEquals(22.2, page.getRequestsByCompany().getSlices().get(0).getPercentage());

        // Top Investor Types: 6 items
        assertEquals(6, page.getTopInvestorTypes().size());
        assertEquals("Individual (HNI)", page.getTopInvestorTypes().get(0).getInvestorType());
        assertEquals(14, page.getTopInvestorTypes().get(0).getCount());
        assertEquals(38.9, page.getTopInvestorTypes().get(0).getPercentage());

        // Test Approve / Reject
        InvestorRequestDetailDto approved = adminService.approveInvestorRequest("INV-2026-0036");
        assertEquals("Approved", approved.getStatus());

        InvestorRequestDetailDto rejected = adminService.rejectInvestorRequest("INV-2026-0035");
        assertEquals("Rejected", rejected.getStatus());
    }

    @Test
    @DisplayName("Screen 12: Verify Group Reports matches screen design with charts, tables, and metrics")
    void testGetGroupReportsPage() {
        GroupReportsPageDto page = adminService.getReportsPage(null, "01 Apr 2026 - 16 Sep 2026");

        assertNotNull(page);
        assertEquals("Group Reports", page.getPageTitle());
        assertEquals("01 Apr 2026 - 16 Sep 2026", page.getDateRange());
        assertEquals("All Group Companies", page.getSelectedCompany());

        // 5 KPI Cards
        assertEquals(5, page.getKpis().size());
        assertEquals("Total Group Companies", page.getKpis().get(0).getTitle());
        assertEquals("8", page.getKpis().get(0).getValue());
        assertEquals("+14% vs last quarter", page.getKpis().get(0).getChange());
        assertEquals("Total Investment Value", page.getKpis().get(1).getTitle());
        assertEquals("₹ 186.4 Cr", page.getKpis().get(1).getValue());
        assertEquals("+22% vs last quarter", page.getKpis().get(1).getChange());
        assertEquals("Total Revenue (YTD)", page.getKpis().get(2).getTitle());
        assertEquals("₹ 124.8 Cr", page.getKpis().get(2).getValue());
        assertEquals("+18% vs last quarter", page.getKpis().get(2).getChange());
        assertEquals("Total Profit (YTD)", page.getKpis().get(3).getTitle());
        assertEquals("₹ 18.6 Cr", page.getKpis().get(3).getValue());
        assertEquals("+26% vs last quarter", page.getKpis().get(3).getChange());
        assertEquals("Total Employees", page.getKpis().get(4).getTitle());
        assertEquals("524", page.getKpis().get(4).getValue());
        assertEquals("+12% vs last quarter", page.getKpis().get(4).getChange());

        // Revenue, Profit & Investment Trend: 6 months
        assertNotNull(page.getFinancialTrend());
        assertEquals("Last 6 Months", page.getFinancialTrend().getFilterRange());
        assertEquals(6, page.getFinancialTrend().getMonthlyTrends().size());
        assertEquals("Apr", page.getFinancialTrend().getMonthlyTrends().get(0).getMonth());
        assertEquals(18.2, page.getFinancialTrend().getMonthlyTrends().get(0).getRevenueCr());
        assertEquals(2.4, page.getFinancialTrend().getMonthlyTrends().get(0).getProfitCr());
        assertEquals(8.5, page.getFinancialTrend().getMonthlyTrends().get(0).getInvestmentCr());

        assertEquals("Sep", page.getFinancialTrend().getMonthlyTrends().get(5).getMonth());
        assertEquals(45.3, page.getFinancialTrend().getMonthlyTrends().get(5).getRevenueCr());
        assertEquals(6.4, page.getFinancialTrend().getMonthlyTrends().get(5).getProfitCr());
        assertEquals(26.8, page.getFinancialTrend().getMonthlyTrends().get(5).getInvestmentCr());

        // Segment-wise Investment Distribution: 7 slices
        assertNotNull(page.getSegmentWiseInvestment());
        assertEquals("₹ 186.4 Cr", page.getSegmentWiseInvestment().getTotalInvestment());
        assertEquals(7, page.getSegmentWiseInvestment().getSlices().size());
        assertEquals("Renewable Energy", page.getSegmentWiseInvestment().getSlices().get(0).getSegment());
        assertEquals(28.4, page.getSegmentWiseInvestment().getSlices().get(0).getPercentage());
        assertEquals("₹ 52.9 Cr", page.getSegmentWiseInvestment().getSlices().get(0).getValueFormatted());

        // Group Companies Performance Table: 8 companies
        assertEquals(8, page.getCompanyPerformance().size());
        GroupReportsPageDto.CompanyPerformanceReportItemDto comp1 = page.getCompanyPerformance().get(0);
        assertEquals("Atirath Traders", comp1.getCompanyName());
        assertEquals("Import-Export", comp1.getSector());
        assertEquals(32.0, comp1.getInvestmentCr());
        assertEquals(28.4, comp1.getRevenueCr());
        assertEquals(4.6, comp1.getProfitCr());
        assertEquals(86, comp1.getEmployees());
        assertEquals("Active", comp1.getStatus());

        // Revenue Contribution by Company Donut: 8 slices
        assertNotNull(page.getRevenueContribution());
        assertEquals(8, page.getRevenueContribution().getSlices().size());
        assertEquals("Atirath Holdings", page.getRevenueContribution().getSlices().get(0).getCompany());
        assertEquals(29.5, page.getRevenueContribution().getSlices().get(0).getPercentage());
        assertEquals("₹ 36.8 Cr", page.getRevenueContribution().getSlices().get(0).getValueFormatted());

        // Key Financial Metrics: 6 metrics
        assertEquals(6, page.getKeyFinancialMetrics().size());
        assertEquals("Total Revenue", page.getKeyFinancialMetrics().get(0).getMetric());
        assertEquals("124.8 Cr", page.getKeyFinancialMetrics().get(0).getCurrentPeriod());
        assertEquals("105.7 Cr", page.getKeyFinancialMetrics().get(0).getPreviousPeriod());

        // Employee Distribution: 8 companies totaling 524
        assertEquals(8, page.getEmployeeDistribution().size());
        int totalEmps = page.getEmployeeDistribution().stream().mapToInt(GroupReportsPageDto.EmployeeDistributionBarDto::getCount).sum();
        assertEquals(524, totalEmps);

        // Top Performing Companies (Profit Growth): 5 companies
        assertEquals(5, page.getTopPerformingCompanies().size());
        assertEquals("Atirath Technologies", page.getTopPerformingCompanies().get(0).getCompany());
        assertEquals(3.2, page.getTopPerformingCompanies().get(0).getProfitCr());
        assertEquals("+ 34%", page.getTopPerformingCompanies().get(0).getGrowthYoY());

        // Filter by company
        GroupReportsPageDto filteredCompany = adminService.getReportsPage("Atirath Traders", null);
        assertEquals(1, filteredCompany.getCompanyPerformance().size());
        assertEquals("Atirath Traders", filteredCompany.getCompanyPerformance().get(0).getCompanyName());
    }
}
