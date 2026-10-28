package com.atirath.trading.modules.market.repository;

import com.atirath.trading.modules.market.model.CompanyLeadership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompanyLeadershipRepository extends JpaRepository<CompanyLeadership, Long> {

    List<CompanyLeadership> findByCompany_CompanyIdAndIsActiveTrueOrderByDisplayOrderAsc(Long companyId);

    List<CompanyLeadership> findByCompany_CompanyIdOrderByDisplayOrderAsc(Long companyId);
}
