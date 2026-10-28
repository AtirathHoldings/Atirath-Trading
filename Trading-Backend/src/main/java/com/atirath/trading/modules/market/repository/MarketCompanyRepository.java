package com.atirath.trading.modules.market.repository;

import com.atirath.trading.modules.market.model.MarketCompany;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MarketCompanyRepository extends JpaRepository<MarketCompany, Long> {

    Optional<MarketCompany> findBySymbolIgnoreCase(String symbol);

    Optional<MarketCompany> findByCompanyCodeIgnoreCase(String companyCode);

    List<MarketCompany> findByStatusOrderByCompanyNameAsc(String status);

    List<MarketCompany> findBySectorIgnoreCaseAndStatusOrderByCompanyNameAsc(String sector, String status);

    @Query("SELECT DISTINCT m.sector FROM MarketCompany m WHERE m.status = 'ACTIVE' ORDER BY m.sector ASC")
    List<String> findAllActiveSectors();

    @Query("SELECT m FROM MarketCompany m WHERE m.status = :status AND " +
            "(LOWER(m.companyName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(m.symbol) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(m.sector) LIKE LOWER(CONCAT('%', :query, '%'))) " +
            "ORDER BY m.companyName ASC")
    List<MarketCompany> searchCompanies(@Param("query") String query, @Param("status") String status);
}
