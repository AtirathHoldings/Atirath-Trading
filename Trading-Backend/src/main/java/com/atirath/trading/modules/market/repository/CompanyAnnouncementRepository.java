package com.atirath.trading.modules.market.repository;

import com.atirath.trading.modules.market.model.CompanyAnnouncement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompanyAnnouncementRepository extends JpaRepository<CompanyAnnouncement, Long> {

    List<CompanyAnnouncement> findByCompany_CompanyIdAndStatusOrderByAnnouncementDateDesc(Long companyId, String status);

    List<CompanyAnnouncement> findByCompany_CompanyIdOrderByAnnouncementDateDesc(Long companyId);
}
