package com.atirath.trading.modules.negotiation.model;

import com.atirath.trading.modules.auth.model.UserAccount;
import com.atirath.trading.modules.market.model.MarketCompany;
import com.atirath.trading.modules.market.model.MarketOrder;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "negotiations")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Negotiation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "negotiation_id")
    private Long negotiationId;

    @Column(name = "negotiation_no", nullable = false, unique = true, length = 40)
    private String negotiationNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private MarketOrder order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private MarketCompany company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "initiator_user_id", nullable = false)
    private UserAccount initiatorUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "counterparty_user_id", nullable = false)
    private UserAccount counterpartyUser;

    @Column(name = "quantity", nullable = false)
    private Long quantity;

    @Column(name = "proposed_price", nullable = false, precision = 18, scale = 4)
    private BigDecimal proposedPrice;

    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private String status = "OPEN";

    @Column(name = "expires_at")
    private OffsetDateTime expiresAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
