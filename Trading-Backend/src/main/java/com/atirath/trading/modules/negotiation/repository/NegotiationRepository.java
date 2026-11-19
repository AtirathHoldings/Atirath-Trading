package com.atirath.trading.modules.negotiation.repository;

import com.atirath.trading.modules.negotiation.model.Negotiation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NegotiationRepository extends JpaRepository<Negotiation, Long> {

    Optional<Negotiation> findByNegotiationNo(String negotiationNo);

    @Query("SELECT n FROM Negotiation n WHERE n.initiatorUser.userId = :userId OR n.counterpartyUser.userId = :userId ORDER BY n.updatedAt DESC, n.createdAt DESC")
    List<Negotiation> findAllForUser(@Param("userId") Long userId);

    @Query("SELECT n FROM Negotiation n WHERE (n.initiatorUser.userId = :userId OR n.counterpartyUser.userId = :userId) AND (:status IS NULL OR UPPER(n.status) = UPPER(:status)) ORDER BY n.updatedAt DESC")
    Page<Negotiation> findForUserWithStatus(@Param("userId") Long userId, @Param("status") String status, Pageable pageable);

    long countByInitiatorUser_UserIdOrCounterpartyUser_UserId(Long initiatorId, Long counterpartyId);

    @Query("SELECT COUNT(n) FROM Negotiation n WHERE (n.initiatorUser.userId = :userId OR n.counterpartyUser.userId = :userId) AND UPPER(n.status) = UPPER(:status)")
    long countForUserByStatus(@Param("userId") Long userId, @Param("status") String status);
}
