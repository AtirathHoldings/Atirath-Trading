package com.atirath.trading.modules.market.model;

import com.atirath.trading.modules.auth.model.UserAccount;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "share_locks")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShareLock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "share_lock_id")
    private Long shareLockId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount userAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private MarketCompany company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private MarketOrder order;

    @Column(name = "locked_quantity", nullable = false)
    private Long lockedQuantity;

    @Column(name = "lock_status", nullable = false, length = 20)
    @Builder.Default
    private String lockStatus = "LOCKED"; // LOCKED, RELEASED, SETTLED

    @CreationTimestamp
    @Column(name = "locked_at", nullable = false, updatable = false)
    private OffsetDateTime lockedAt;

    @Column(name = "released_at")
    private OffsetDateTime releasedAt;
}
