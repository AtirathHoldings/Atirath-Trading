package com.atirath.trading.modules.market.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "market_order_matches")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketOrderMatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "match_id")
    private Long matchId;

    @Column(name = "buy_order_id", nullable = false)
    private Long buyOrderId;

    @Column(name = "sell_order_id", nullable = false)
    private Long sellOrderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private MarketCompany company;

    @Column(name = "matched_quantity", nullable = false)
    private Long matchedQuantity;

    @Column(name = "matched_price", nullable = false, precision = 18, scale = 4)
    private BigDecimal matchedPrice;

    @Column(name = "match_status", nullable = false, length = 30)
    private String matchStatus; // MATCHED, PARTIAL, CANCELLED

    @CreationTimestamp
    @Column(name = "matched_at", nullable = false)
    private OffsetDateTime matchedAt;
}
