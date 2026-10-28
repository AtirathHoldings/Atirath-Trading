package com.atirath.trading.modules.market.repository;

import com.atirath.trading.modules.market.model.MarketPriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MarketPriceHistoryRepository extends JpaRepository<MarketPriceHistory, Long> {

    List<MarketPriceHistory> findByCompany_CompanyIdAndIntervalTypeOrderByPriceTimeAsc(
            Long companyId, String intervalType);

    List<MarketPriceHistory> findByCompany_CompanyIdAndIntervalTypeAndPriceTimeBetweenOrderByPriceTimeAsc(
            Long companyId, String intervalType, OffsetDateTime from, OffsetDateTime to);

    Optional<MarketPriceHistory> findTopByCompany_CompanyIdOrderByPriceTimeDesc(Long companyId);

    @Query("SELECT p FROM MarketPriceHistory p WHERE p.company.companyId = :companyId " +
           "ORDER BY p.priceTime DESC LIMIT 2")
    List<MarketPriceHistory> findLatestTwoPrices(@Param("companyId") Long companyId);
}
