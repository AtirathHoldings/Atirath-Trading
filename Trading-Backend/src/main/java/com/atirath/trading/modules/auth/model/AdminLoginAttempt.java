package com.atirath.trading.modules.auth.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

/**
 * Entity mapping the admin_login_attempts database table.
 */
@Entity
@Table(name = "admin_login_attempts", indexes = {
        @Index(name = "idx_admin_login_attempts_admin_id", columnList = "admin_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminLoginAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attempt_id")
    private Long attemptId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id")
    private AdminAccount adminAccount;

    @CreationTimestamp
    @Column(name = "attempted_at", nullable = false, updatable = false)
    private OffsetDateTime attemptedAt;

    @Column(name = "success", nullable = false)
    @Builder.Default
    private Boolean success = false;

    @Column(name = "ip_address", columnDefinition = "inet")
    private String ipAddress;
}
