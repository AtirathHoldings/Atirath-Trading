package com.atirath.trading.modules.market.repository;

import com.atirath.trading.modules.market.model.CompanyQuarterlyFinancial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompanyQuarterlyFinancialRepository extends JpaRepository<CompanyQuarterlyFinancial, Long> {

    List<CompanyQuarterlyFinancial> findByCompany_CompanyIdOrderByFinancialYearDescPeriodLabelDesc(Long companyId);
}
