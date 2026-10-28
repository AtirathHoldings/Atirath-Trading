package com.atirath.trading.modules.market.model;

import com.atirath.trading.modules.auth.model.UserAccount;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "market_orders")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "order_no", nullable = false, unique = true, length = 40)
    private String orderNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount userAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private MarketCompany company;

    @Column(name = "order_side", nullable = false, length = 10)
    private String orderSide; // BUY, SELL

    @Column(name = "order_type", nullable = false, length = 20)
    private String orderType; // MARKET, LIMIT, STOP_LOSS

    @Column(name = "quantity", nullable = false)
    private Long quantity;

    @Column(name = "price_per_share", precision = 18, scale = 4)
    private BigDecimal pricePerShare;

    @Column(name = "order_value", nullable = false, precision = 20, scale = 2)
    @Builder.Default
    private BigDecimal orderValue = BigDecimal.ZERO;

    @Column(name = "validity", nullable = false, length = 20)
    @Builder.Default
    private String validity = "DAY_ORDER";

    @Column(name = "stop_loss_enabled", nullable = false)
    @Builder.Default
    private Boolean stopLossEnabled = false;

    @Column(name = "stop_price", precision = 18, scale = 4)
    private BigDecimal stopPrice;

    @Column(name = "trigger_type", length = 20)
    private String triggerType;

    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private String status = "PENDING";

    @Column(name = "order_source", length = 30)
    private String orderSource;

    @CreationTimestamp
    @Column(name = "placed_at", nullable = false, updatable = false)
    private OffsetDateTime placedAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
