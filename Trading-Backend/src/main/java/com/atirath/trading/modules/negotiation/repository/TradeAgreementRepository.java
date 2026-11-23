package com.atirath.trading.modules.negotiation.repository;

import com.atirath.trading.modules.negotiation.model.TradeAgreement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TradeAgreementRepository extends JpaRepository<TradeAgreement, Long> {

    Optional<TradeAgreement> findByAgreementNo(String agreementNo);

    Optional<TradeAgreement> findByNegotiation_NegotiationNo(String negotiationNo);

    Optional<TradeAgreement> findByNegotiation_NegotiationId(Long negotiationId);

    @org.springframework.data.jpa.repository.Query("SELECT t FROM TradeAgreement t LEFT JOIN FETCH t.company LEFT JOIN FETCH t.buyerUser LEFT JOIN FETCH t.sellerUser WHERE t.agreementId = :id")
    java.util.Optional<TradeAgreement> findWithDetailsById(@org.springframework.data.repository.query.Param("id") Long id);

    @org.springframework.data.jpa.repository.Query("SELECT t FROM TradeAgreement t LEFT JOIN FETCH t.company LEFT JOIN FETCH t.buyerUser LEFT JOIN FETCH t.sellerUser WHERE (t.buyerUser.userId = :userId OR t.sellerUser.userId = :userId) AND UPPER(t.agreementStatus) = 'CONFIRMED' ORDER BY t.agreementId DESC")
    java.util.List<TradeAgreement> findUserConfirmedAgreements(@org.springframework.data.repository.query.Param("userId") Long userId);
}
