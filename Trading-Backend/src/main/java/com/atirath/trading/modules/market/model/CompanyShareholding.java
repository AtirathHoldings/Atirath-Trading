package com.atirath.trading.modules.market.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "company_shareholding")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyShareholding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "shareholding_id")
    private Long shareholdingId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private MarketCompany company;

    @Column(name = "holder_category", nullable = false, length = 80)
    private String holderCategory; // PROMOTERS, GROUP_COMPANIES, EMPLOYEES, OTHERS

    @Column(name = "shares_held", nullable = false)
    private Long sharesHeld;

    @Column(name = "percentage", nullable = false, precision = 7, scale = 4)
    private BigDecimal percentage;

    @Column(name = "as_of_date")
    private LocalDate asOfDate;
}
