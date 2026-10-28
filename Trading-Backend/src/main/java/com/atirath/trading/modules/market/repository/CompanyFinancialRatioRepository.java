package com.atirath.trading.modules.market.repository;

import com.atirath.trading.modules.market.model.CompanyFinancialRatio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyFinancialRatioRepository extends JpaRepository<CompanyFinancialRatio, Long> {

    List<CompanyFinancialRatio> findByCompany_CompanyIdOrderByFinancialYearDesc(Long companyId);

    Optional<CompanyFinancialRatio> findTopByCompany_CompanyIdOrderByFinancialYearDesc(Long companyId);
}
