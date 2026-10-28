package com.atirath.trading.modules.market.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "market_companies")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketCompany {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "company_id")
    private Long companyId;

    @Column(name = "company_code", nullable = false, unique = true, length = 30)
    private String companyCode;

    @Column(name = "company_name", nullable = false, length = 200)
    private String companyName;

    @Column(name = "symbol", nullable = false, unique = true, length = 30)
    private String symbol;

    @Column(name = "sector", nullable = false, length = 100)
    private String sector;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "ACTIVE";

    @Column(name = "founded_date")
    private LocalDate foundedDate;

    @Column(name = "headquarters", length = 200)
    private String headquarters;

    @Column(name = "website_url", columnDefinition = "TEXT")
    private String websiteUrl;

    @Column(name = "logo_url", columnDefinition = "TEXT")
    private String logoUrl;

    @Column(name = "issued_shares", nullable = false)
    @Builder.Default
    private Long issuedShares = 0L;

    @Column(name = "available_shares", nullable = false)
    @Builder.Default
    private Long availableShares = 0L;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
