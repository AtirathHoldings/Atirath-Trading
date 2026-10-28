package com.atirath.trading.modules.market.repository;

import com.atirath.trading.modules.market.model.CompanyDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompanyDocumentRepository extends JpaRepository<CompanyDocument, Long> {

    List<CompanyDocument> findByCompany_CompanyIdOrderByPeriodDateDesc(Long companyId);

    List<CompanyDocument> findByCompany_CompanyIdAndCategoryOrderByPeriodDateDesc(Long companyId, String category);
}
