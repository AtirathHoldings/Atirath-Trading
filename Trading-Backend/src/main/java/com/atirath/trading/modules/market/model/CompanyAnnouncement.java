package com.atirath.trading.modules.market.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "company_announcements")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyAnnouncement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "announcement_id")
    private Long announcementId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private MarketCompany company;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "announcement_type", length = 60)
    private String announcementType; // RESULT, DIVIDEND, BOARD, STRATEGIC, etc.

    @Column(name = "announcement_date", nullable = false)
    private LocalDate announcementDate;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id")
    private CompanyDocument document;

    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "PUBLISHED"; // PUBLISHED, ARCHIVED
}
