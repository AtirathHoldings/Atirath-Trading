package com.atirath.trading.modules.market.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "market_price_history")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketPriceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "price_history_id")
    private Long priceHistoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private MarketCompany company;

    @Column(name = "interval_type", nullable = false, length = 20)
    private String intervalType;

    @Column(name = "price_time", nullable = false)
    private OffsetDateTime priceTime;

    @Column(name = "open_price", nullable = false, precision = 18, scale = 4)
    private BigDecimal openPrice;

    @Column(name = "high_price", nullable = false, precision = 18, scale = 4)
    private BigDecimal highPrice;

    @Column(name = "low_price", nullable = false, precision = 18, scale = 4)
    private BigDecimal lowPrice;

    @Column(name = "close_price", nullable = false, precision = 18, scale = 4)
    private BigDecimal closePrice;

    @Column(name = "volume_shares", nullable = false)
    @Builder.Default
    private Long volumeShares = 0L;

    @Column(name = "turnover_value", nullable = false, precision = 20, scale = 2)
    @Builder.Default
    private BigDecimal turnoverValue = BigDecimal.ZERO;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
