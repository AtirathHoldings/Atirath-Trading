package com.atirath.trading.modules.market.repository;

import com.atirath.trading.modules.market.model.MarketTrade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface MarketTradeRepository extends JpaRepository<MarketTrade, Long> {

    List<MarketTrade> findTop50ByOrderByExecutedAtDesc();

    @org.springframework.data.jpa.repository.Query("SELECT t FROM MarketTrade t WHERE t.buyerUser.userId = :userId OR t.sellerUser.userId = :userId ORDER BY t.executedAt DESC")
    List<MarketTrade> findUserTrades(@org.springframework.data.repository.query.Param("userId") Long userId);

    List<MarketTrade> findTop50ByCompany_CompanyIdOrderByExecutedAtDesc(Long companyId);

    long countByStatus(String status);

    @Query("SELECT COALESCE(SUM(t.tradeValue), 0) FROM MarketTrade t WHERE t.status = 'EXECUTED'")
    BigDecimal calculateTotalTradedValue();

    @Query("SELECT COALESCE(SUM(t.tradeValue), 0) FROM MarketTrade t WHERE t.company.companyId = :companyId AND t.status = 'EXECUTED'")
    BigDecimal calculateTotalTradedValueForCompany(@Param("companyId") Long companyId);

    @Query("SELECT COUNT(t) FROM MarketTrade t WHERE t.executedAt >= :since AND t.status = 'EXECUTED'")
    long countTradesSince(@Param("since") OffsetDateTime since);
}
