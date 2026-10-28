package com.atirath.trading.modules.market.model;

import com.atirath.trading.modules.auth.model.UserAccount;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "share_transfers")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShareTransfer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transfer_id")
    private Long transferId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_user_id", nullable = false)
    private UserAccount fromUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_user_id", nullable = false)
    private UserAccount toUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trade_id")
    private MarketTrade trade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private MarketCompany company;

    @Column(name = "quantity", nullable = false)
    private Long quantity;

    @Column(name = "transfer_status", nullable = false, length = 30)
    @Builder.Default
    private String transferStatus = "PENDING"; // PENDING, COMPLETED, CANCELLED

    @CreationTimestamp
    @Column(name = "transferred_at")
    private OffsetDateTime transferredAt;
}
