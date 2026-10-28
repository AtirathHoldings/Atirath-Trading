package com.atirath.trading.modules.market.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "company_leadership")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyLeadership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "leadership_id")
    private Long leadershipId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private MarketCompany company;

    @Column(name = "person_name", nullable = false, length = 150)
    private String personName;

    @Column(name = "designation", nullable = false, length = 150)
    private String designation;

    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private Integer displayOrder = 1;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}
