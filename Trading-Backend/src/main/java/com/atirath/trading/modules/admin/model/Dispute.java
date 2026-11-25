package com.atirath.trading.modules.admin.model;

import com.atirath.trading.modules.auth.model.AdminAccount;
import com.atirath.trading.modules.auth.model.UserAccount;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "disputes", indexes = {
        @Index(name = "idx_disputes_status", columnList = "status"),
        @Index(name = "idx_disputes_raised_by", columnList = "raised_by_user_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Dispute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dispute_id")
    private Long disputeId;

    @Column(name = "dispute_reference", nullable = false, unique = true, length = 50)
    private String disputeReference;

    @Column(name = "trade_id")
    private Long tradeId;

    @Column(name = "escrow_id")
    private Long escrowId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "raised_by_user_id", nullable = false)
    private UserAccount raisedByUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "against_user_id")
    private UserAccount againstUser;

    @Column(name = "dispute_category", nullable = false, length = 100)
    @Builder.Default
    private String disputeCategory = "TRADE_EXECUTION";

    @Column(name = "dispute_reason", nullable = false, columnDefinition = "TEXT")
    private String disputeReason;

    @Column(name = "disputed_amount", precision = 18, scale = 4)
    @Builder.Default
    private BigDecimal disputedAmount = BigDecimal.ZERO;

    @Column(name = "priority", nullable = false, length = 20)
    @Builder.Default
    private String priority = "MEDIUM";

    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private String status = "OPEN";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_admin_id")
    private AdminAccount assignedAdmin;

    @Column(name = "resolution_notes", columnDefinition = "TEXT")
    private String resolutionNotes;

    @Column(name = "resolved_at")
    private OffsetDateTime resolvedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
