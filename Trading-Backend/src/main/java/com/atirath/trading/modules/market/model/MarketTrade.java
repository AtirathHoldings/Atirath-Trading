package com.atirath.trading.modules.market.model;

import com.atirath.trading.modules.auth.model.UserAccount;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "market_trades")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketTrade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trade_id")
    private Long tradeId;

    @Column(name = "trade_no", nullable = false, unique = true, length = 40)
    private String tradeNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id")
    private MarketOrderMatch match;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private MarketCompany company;

    @Column(name = "buy_order_id", nullable = false)
    private Long buyOrderId;

    @Column(name = "sell_order_id", nullable = false)
    private Long sellOrderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_user_id", nullable = false)
    private UserAccount buyerUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_user_id", nullable = false)
    private UserAccount sellerUser;

    @Column(name = "quantity", nullable = false)
    private Long quantity;

    @Column(name = "trade_price", nullable = false, precision = 18, scale = 4)
    private BigDecimal tradePrice;

    @Column(name = "trade_value", nullable = false, precision = 20, scale = 2)
    private BigDecimal tradeValue;

    @Column(name = "executed_at", nullable = false)
    private OffsetDateTime executedAt;

    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "EXECUTED"; // EXECUTED, REVERSED
}
