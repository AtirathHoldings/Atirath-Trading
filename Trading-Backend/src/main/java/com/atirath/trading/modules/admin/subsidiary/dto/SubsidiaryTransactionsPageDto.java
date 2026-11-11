package com.atirath.trading.modules.admin.subsidiary.dto;

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
public class SubsidiaryTransactionsPageDto {

    private AdminUserHeaderDto adminUser;
    private String pageTitle;
    private String pageSubtitle;
    private String selectedCompany;
    private String companyStatus;

    // 4 KPIs
    private List<KpiCardDto> kpis;

    // Tabs: All Transactions (156), Issuances (18), Transfers (76), Buybacks (6), Corporate Actions (12), Others (44)
    private List<String> tabs;
    private String activeTab;

    // Table: Transactions
    private List<CompanyTransactionItemDto> transactions;
    private int totalTransactions;
    private int currentPage;
    private int totalPages;

    // Right Drawer Detail
    private CompanyTransactionDetailDto selectedTransactionDetail;

    // Bottom Widget 1: Transactions by Type Donut
    private TransactionsByTypeDonutDto transactionsByType;

    // Bottom Widget 2: Monthly Transaction Value Bar Chart
    private List<MonthlyTransactionValueBarDto> monthlyTransactionValue;

    // Bottom Widget 3: Transaction Status Donut
    private TransactionStatusDonutDto transactionStatus;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompanyTransactionItemDto {
        private String id;
        private int number;
        private String transactionId;
        private String transactionType; // Share Issuance, Share Transfer, Share Buyback, Corporate Action, Others
        private String transactionTypeTheme; // green, blue, red, purple, gray
        private String date;
        private String shares;
        private String value;
        private String pricePerShare;
        private String counterparty;
        private String status; // Completed, Under Review, Rejected
        private String statusTheme; // green, orange, red
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransactionsByTypeDonutDto {
        private int totalTransactions;
        private List<TransactionTypeSliceDto> slices;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransactionTypeSliceDto {
        private String type;
        private int count;
        private double percentage;
        private String color;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyTransactionValueBarDto {
        private String month;
        private double valueCr;
        private String color;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransactionStatusDonutDto {
        private int totalTransactions;
        private List<TxnStatusSliceDto> slices;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TxnStatusSliceDto {
        private String status;
        private int count;
        private double percentage;
        private String color;
    }
}
