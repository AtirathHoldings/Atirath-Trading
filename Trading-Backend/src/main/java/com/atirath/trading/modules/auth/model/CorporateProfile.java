package com.atirath.trading.modules.auth.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "corporate_profiles", indexes = {
        @Index(name = "idx_corp_user_id", columnList = "user_id"),
        @Index(name = "idx_corp_reg_id", columnList = "registration_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CorporateProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "corporate_id")
    private Long corporateId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", nullable = false, unique = true)
    private UserAccount userAccount;

    @Column(name = "company_name", nullable = false, length = 200)
    private String companyName;

    @Column(name = "registration_id", nullable = false, unique = true, length = 30)
    private String registrationId;

    @Column(name = "contact_person_name", nullable = false, length = 150)
    private String contactPersonName;

    @Column(name = "industry", length = 150)
    private String industry;

    @Column(name = "incorporation_date")
    private LocalDate incorporationDate;

    @Column(name = "company_type", length = 50)
    private String companyType;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
