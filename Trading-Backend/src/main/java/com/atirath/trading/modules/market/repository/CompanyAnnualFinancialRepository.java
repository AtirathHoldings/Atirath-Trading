package com.atirath.trading.modules.market.repository;

import com.atirath.trading.modules.market.model.CompanyAnnualFinancial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyAnnualFinancialRepository extends JpaRepository<CompanyAnnualFinancial, Long> {

    List<CompanyAnnualFinancial> findByCompany_CompanyIdOrderByFinancialYearDesc(Long companyId);

    Optional<CompanyAnnualFinancial> findByCompany_CompanyIdAndFinancialYear(Long companyId, String financialYear);
}
