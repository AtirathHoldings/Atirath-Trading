package com.atirath.trading.modules.market.repository;

import com.atirath.trading.modules.market.model.Escrow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EscrowRepository extends JpaRepository<Escrow, Long> {
    Optional<Escrow> findByEscrowReference(String escrowReference);
    Optional<Escrow> findByTrade_TradeId(Long tradeId);

    @org.springframework.data.jpa.repository.Query("SELECT e FROM Escrow e WHERE e.buyerUser.userId = :userId OR e.sellerUser.userId = :userId ORDER BY e.createdAt DESC")
    java.util.List<Escrow> findUserEscrows(@org.springframework.data.repository.query.Param("userId") Long userId);
}
