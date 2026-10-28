package com.atirath.trading.modules.auth.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "identity_kyc_details", indexes = {
        @Index(name = "idx_kyc_user_id", columnList = "user_id"),
        @Index(name = "idx_kyc_pan", columnList = "pan_number")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IdentityKycDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "kyc_id")
    private Long kycId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", nullable = false, unique = true)
    private UserAccount userAccount;

    @Column(name = "pan_number", nullable = false, length = 10)
    private String panNumber;

    @Column(name = "aadhaar_number_encrypted", nullable = false, columnDefinition = "TEXT")
    private String aadhaarNumberEncrypted;

    @Column(name = "pan_full_name", nullable = false, length = 150)
    private String panFullName;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
