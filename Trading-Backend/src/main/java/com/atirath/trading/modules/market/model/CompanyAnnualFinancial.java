package com.atirath.trading.modules.market.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "company_annual_financials", uniqueConstraints = {
        @UniqueConstraint(name = "uq_annual_fin_company_year", columnNames = {"company_id", "financial_year"})
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyAnnualFinancial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "financial_id")
    private Long financialId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private MarketCompany company;

    @Column(name = "financial_year", nullable = false, length = 20)
    private String financialYear;

    @Column(name = "period_end_date")
    private LocalDate periodEndDate;

    @Column(name = "revenue", precision = 20, scale = 2)
    private BigDecimal revenue;

    @Column(name = "ebitda", precision = 20, scale = 2)
    private BigDecimal ebitda;

    @Column(name = "ebitda_margin", precision = 10, scale = 4)
    private BigDecimal ebitdaMargin;

    @Column(name = "net_profit", precision = 20, scale = 2)
    private BigDecimal netProfit;

    @Column(name = "net_profit_margin", precision = 10, scale = 4)
    private BigDecimal netProfitMargin;

    @Column(name = "total_assets", precision = 20, scale = 2)
    private BigDecimal totalAssets;

    @Column(name = "total_equity", precision = 20, scale = 2)
    private BigDecimal totalEquity;

    @Column(name = "debt_to_equity", precision = 10, scale = 4)
    private BigDecimal debtToEquity;

    @Column(name = "current_ratio", precision = 10, scale = 4)
    private BigDecimal currentRatio;

    @Column(name = "roe", precision = 10, scale = 4)
    private BigDecimal roe;

    @Column(name = "roce", precision = 10, scale = 4)
    private BigDecimal roce;

    @Column(name = "eps", precision = 18, scale = 4)
    private BigDecimal eps;

    @Column(name = "pe_ratio", precision = 10, scale = 4)
    private BigDecimal peRatio;

    @Column(name = "book_value", precision = 18, scale = 4)
    private BigDecimal bookValue;

    @Column(name = "price_to_book", precision = 10, scale = 4)
    private BigDecimal priceToBook;

    @Column(name = "dividend_yield", precision = 10, scale = 4)
    private BigDecimal dividendYield;
}
