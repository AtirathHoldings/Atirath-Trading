package com.atirath.trading.modules.market.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MobileStockDto {
    private Long companyId;
    private String symbol;
    private String shortCode;
    private String name;
    private String sector;
    private BigDecimal price;
    private BigDecimal change;
    private BigDecimal changePercent;
    private Boolean isGainer;
    private BigDecimal openPrice;
    private BigDecimal prevClose;
    private BigDecimal dayHigh;
    private BigDecimal dayLow;
    private BigDecimal high52;
    private BigDecimal low52;
    private BigDecimal marketCapCr;
    private BigDecimal turnoverCr;
    private String status;
    private String logoUrl;
    private BigDecimal peRatio;
    private BigDecimal eps;
    private BigDecimal dividendYield;
    private BigDecimal roe;
    private BigDecimal bookValue;
    private BigDecimal faceValue;
    private BigDecimal debtToEquity;
    private Long volume;
    private String description;
    private String ceo;
    private String headOffice;
    private Integer incorporatedYear;

    // Mini chart / Sparkline points for 1D, 1W, 1M, 1Y
    private List<BigDecimal> pricePoints1D;
    private List<BigDecimal> pricePoints1W;
    private List<BigDecimal> pricePoints1M;
    private List<BigDecimal> pricePoints1Y;

    // 5-depth live order book for trade_screen and order book tab
    private List<OrderBookLevelDto> bids;
    private List<OrderBookLevelDto> asks;

    // Fundamentals for company_details_screen
    private List<QuarterlyFinancialDto> quarterlyFinancials;
    private List<AnnualFinancialDto> annualFinancials;
    private List<ShareholdingDto> shareholding;

    // Company documents & disclosures
    private List<CompanyDocumentDto> documents;

    // Execution tape & Highlights
    private List<LiveTradeItemDto> recentTrades;
    private List<String> highlights;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderBookLevelDto {
        private BigDecimal price;
        private Long quantity;
        private Integer ordersCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuarterlyFinancialDto {
        private String quarter;
        private BigDecimal revenueCr;
        private BigDecimal netProfitCr;
        private BigDecimal ebitdaMargin;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnnualFinancialDto {
        private String financialYear;
        private BigDecimal revenueCr;
        private BigDecimal netProfitCr;
        private BigDecimal ebitdaMargin;
        private BigDecimal eps;
        private BigDecimal peRatio;
        private BigDecimal bookValue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShareholdingDto {
        private String category;
        private BigDecimal percentage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompanyDocumentDto {
        private Long documentId;
        private String documentName;
        private String category;
        private String periodDate;
        private String fileUrl;
        private Long fileSizeBytes;
        private String formattedFileSize;
        private String mimeType;
        private Boolean isVerified;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LiveTradeItemDto {
        private String time;
        private Boolean isBuy;
        private BigDecimal price;
        private Long quantity;
        private String tradeNo;
    }
}
