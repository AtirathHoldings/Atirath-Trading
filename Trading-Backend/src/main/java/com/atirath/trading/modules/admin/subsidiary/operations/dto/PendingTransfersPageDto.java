package com.atirath.trading.modules.admin.subsidiary.operations.dto;

import com.atirath.trading.modules.admin.parent.dto.AdminUserHeaderDto;
import com.atirath.trading.modules.admin.parent.dto.KpiCardDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PendingTransfersPageDto {

    private AdminUserHeaderDto adminUser;
    private String pageTitle;
    private String pageSubtitle;
    private String selectedCompany;
    private String dateRange;

    // 5 Top KPI Cards
    private List<KpiCardDto> kpis;

    // Tabs
    private List<String> tabs;
    private String activeTab;

    // Filter selections
    private List<String> companies;
    private List<String> shareClasses;
    private List<String> transferTypes;
    private List<String> statuses;

    // Table items
    private List<PendingTransferItemDto> transfers;
    private int totalTransfers;
    private int currentPage;
    private int totalPages;
    private int pageSize;

    // Bottom Chart 1: Transfers by Status Donut
    private TransfersStatusDonutDto transfersByStatus;

    // Bottom Chart 2: Transfers Trend (Monthly Stacked Bar)
    private List<TransfersTrendMonthDto> transfersTrend;

    // Selected Drawer item
    private TransferDetailDto selectedTransferDetail;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransfersStatusDonutDto {
        private int totalTransfers;
        private List<TransferStatusSliceDto> slices;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransferStatusSliceDto {
        private String label;
        private double percentage;
        private int count;
        private String color;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransfersTrendMonthDto {
        private String month;
        private int approval;
        private int verification;
        private int settlement;
        private int overdue;
        private int rejected;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PendingTransferItemDto {
        private String id;
        private int number;
        private String transferId;
        private String transferDate;
        private String fromParty;
        private String toParty;
        private String shares;
        private String shareClass;
        private String type; // Transfer, Gift, Inheritance
        private String typeTheme; // gray, blue, orange
        private String status; // Approval, Verification, Settlement, Overdue
        private String statusTheme; // orange, blue, green, red
        private String settlementDate;
    }
}
