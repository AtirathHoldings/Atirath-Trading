package com.atirath.trading.modules.market.repository;

import com.atirath.trading.modules.market.model.CompanyShareholding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompanyShareholdingRepository extends JpaRepository<CompanyShareholding, Long> {

    List<CompanyShareholding> findByCompany_CompanyIdOrderByPercentageDesc(Long companyId);
}
