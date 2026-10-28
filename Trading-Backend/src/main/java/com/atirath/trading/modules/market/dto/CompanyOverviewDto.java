package com.atirath.trading.modules.market.dto;

import com.atirath.trading.modules.market.model.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyOverviewDto {

    private Long companyId;
    private String companyCode;
    private String companyName;
    private String symbol;
    private String sector;
    private String description;
    private String status;
    private LocalDate foundedDate;
    private String headquarters;
    private String websiteUrl;
    private String logoUrl;
    private Long issuedShares;
    private Long availableShares;

    // Price snapshot
    private BigDecimal currentPrice;
    private BigDecimal priceChange;
    private BigDecimal priceChangePercentage;
    private BigDecimal high52Week;
    private BigDecimal low52Week;
    private Long volume24h;
    private Boolean isWatchlisted;

    // Sub-modules
    private List<LeadershipItemDto> leadership;
    private List<HighlightItemDto> highlights;
    private List<ShareholdingItemDto> shareholding;
    private FinancialRatioItemDto latestRatios;
    private List<AnnualFinancialItemDto> annualFinancials;
    private List<QuarterlyFinancialItemDto> quarterlyFinancials;
    private List<AnnouncementItemDto> recentAnnouncements;
    private List<DocumentItemDto> documents;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LeadershipItemDto {
        private Long leadershipId;
        private String personName;
        private String designation;
        private Integer displayOrder;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HighlightItemDto {
        private Long highlightId;
        private String highlightText;
        private Integer displayOrder;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShareholdingItemDto {
        private Long shareholdingId;
        private String holderCategory;
        private Long sharesHeld;
        private BigDecimal percentage;
        private LocalDate asOfDate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FinancialRatioItemDto {
        private Long ratioId;
        private String financialYear;
        private BigDecimal eps;
        private BigDecimal peRatio;
        private BigDecimal bookValue;
        private BigDecimal priceToBook;
        private BigDecimal debtToEquity;
        private BigDecimal currentRatio;
        private BigDecimal roe;
        private BigDecimal roce;
        private BigDecimal dividendYield;
        private BigDecimal netProfitMargin;
        private BigDecimal ebitdaMargin;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnnualFinancialItemDto {
        private Long financialId;
        private String financialYear;
        private LocalDate periodEndDate;
        private BigDecimal revenue;
        private BigDecimal ebitda;
        private BigDecimal ebitdaMargin;
        private BigDecimal netProfit;
        private BigDecimal netProfitMargin;
        private BigDecimal totalAssets;
        private BigDecimal totalEquity;
        private BigDecimal debtToEquity;
        private BigDecimal currentRatio;
        private BigDecimal roe;
        private BigDecimal roce;
        private BigDecimal eps;
        private BigDecimal peRatio;
        private BigDecimal bookValue;
        private BigDecimal priceToBook;
        private BigDecimal dividendYield;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuarterlyFinancialItemDto {
        private Long financialId;
        private String financialYear;
        private String periodLabel;
        private LocalDate periodEndDate;
        private BigDecimal revenue;
        private BigDecimal ebitda;
        private BigDecimal ebitdaMargin;
        private BigDecimal netProfit;
        private BigDecimal netProfitMargin;
        private BigDecimal totalAssets;
        private BigDecimal totalEquity;
        private BigDecimal debtToEquity;
        private BigDecimal currentRatio;
        private BigDecimal roe;
        private BigDecimal roce;
        private BigDecimal eps;
        private BigDecimal peRatio;
        private BigDecimal bookValue;
        private BigDecimal priceToBook;
        private BigDecimal dividendYield;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnnouncementItemDto {
        private Long announcementId;
        private String title;
        private String description;
        private String announcementType;
        private LocalDate announcementDate;
        private Long documentId;
        private String status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DocumentItemDto {
        private Long documentId;
        private String documentName;
        private String category;
        private LocalDate periodDate;
        private String fileUrl;
        private Long fileSizeBytes;
        private String mimeType;
        private Boolean isVerified;
        private OffsetDateTime createdAt;
    }
}
