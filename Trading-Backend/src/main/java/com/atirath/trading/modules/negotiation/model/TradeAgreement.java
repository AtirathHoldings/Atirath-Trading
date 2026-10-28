package com.atirath.trading.modules.negotiation.model;

import com.atirath.trading.modules.auth.model.UserAccount;
import com.atirath.trading.modules.market.model.MarketCompany;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "trade_agreements")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeAgreement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "agreement_id")
    private Long agreementId;

    @Column(name = "agreement_no", nullable = false, unique = true, length = 40)
    private String agreementNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "negotiation_id", nullable = false)
    private Negotiation negotiation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_user_id", nullable = false)
    private UserAccount buyerUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_user_id", nullable = false)
    private UserAccount sellerUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private MarketCompany company;

    @Column(name = "agreed_quantity", nullable = false)
    private Long agreedQuantity;

    @Column(name = "agreed_price", nullable = false, precision = 18, scale = 4)
    private BigDecimal agreedPrice;

    @Column(name = "agreement_status", nullable = false, length = 30)
    @Builder.Default
    private String agreementStatus = "DRAFT";

    @Column(name = "buyer_accepted_at")
    private OffsetDateTime buyerAcceptedAt;

    @Column(name = "seller_accepted_at")
    private OffsetDateTime sellerAcceptedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
