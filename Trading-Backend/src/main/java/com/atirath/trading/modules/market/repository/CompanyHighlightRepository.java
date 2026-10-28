package com.atirath.trading.modules.market.repository;

import com.atirath.trading.modules.market.model.CompanyHighlight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompanyHighlightRepository extends JpaRepository<CompanyHighlight, Long> {

    List<CompanyHighlight> findByCompany_CompanyIdAndIsActiveTrueOrderByDisplayOrderAsc(Long companyId);

    List<CompanyHighlight> findByCompany_CompanyIdOrderByDisplayOrderAsc(Long companyId);
}
