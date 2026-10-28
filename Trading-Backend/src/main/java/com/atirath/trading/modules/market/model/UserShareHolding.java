package com.atirath.trading.modules.market.model;

import com.atirath.trading.modules.auth.model.UserAccount;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "user_share_holdings", uniqueConstraints = {
        @UniqueConstraint(name = "uq_user_company_holding", columnNames = {"user_id", "company_id"})
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserShareHolding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "holding_id")
    private Long holdingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount userAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private MarketCompany company;

    @Column(name = "total_shares", nullable = false)
    @Builder.Default
    private Long totalShares = 0L;

    @Column(name = "locked_shares", nullable = false)
    @Builder.Default
    private Long lockedShares = 0L;

    @Column(name = "average_buy_price", precision = 18, scale = 4)
    @Builder.Default
    private BigDecimal averageBuyPrice = BigDecimal.ZERO;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public Long getAvailableShares() {
        long total = totalShares != null ? totalShares : 0L;
        long locked = lockedShares != null ? lockedShares : 0L;
        return Math.max(0L, total - locked);
    }
}
