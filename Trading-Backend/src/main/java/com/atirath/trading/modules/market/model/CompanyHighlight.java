package com.atirath.trading.modules.market.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "company_highlights")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyHighlight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "highlight_id")
    private Long highlightId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private MarketCompany company;

    @Column(name = "highlight_text", nullable = false, columnDefinition = "TEXT")
    private String highlightText;

    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private Integer displayOrder = 1;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}
