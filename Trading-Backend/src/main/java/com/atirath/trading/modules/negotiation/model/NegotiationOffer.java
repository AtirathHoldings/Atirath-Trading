package com.atirath.trading.modules.negotiation.model;

import com.atirath.trading.modules.auth.model.UserAccount;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "negotiation_offers")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NegotiationOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "offer_id")
    private Long offerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "negotiation_id", nullable = false)
    private Negotiation negotiation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "offered_by_user_id", nullable = false)
    private UserAccount offeredByUser;

    @Column(name = "quantity", nullable = false)
    private Long quantity;

    @Column(name = "offered_price", nullable = false, precision = 18, scale = 4)
    private BigDecimal offeredPrice;

    @Column(name = "offer_message", columnDefinition = "TEXT")
    private String offerMessage;

    @Column(name = "offer_status", nullable = false, length = 30)
    @Builder.Default
    private String offerStatus = "PENDING";

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
