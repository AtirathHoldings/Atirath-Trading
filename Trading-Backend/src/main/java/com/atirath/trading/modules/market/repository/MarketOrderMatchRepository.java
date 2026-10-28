package com.atirath.trading.modules.market.repository;

import com.atirath.trading.modules.market.model.MarketOrderMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MarketOrderMatchRepository extends JpaRepository<MarketOrderMatch, Long> {

    List<MarketOrderMatch> findByCompany_CompanyIdOrderByMatchedAtDesc(Long companyId);

    List<MarketOrderMatch> findTop20ByOrderByMatchedAtDesc();
}
