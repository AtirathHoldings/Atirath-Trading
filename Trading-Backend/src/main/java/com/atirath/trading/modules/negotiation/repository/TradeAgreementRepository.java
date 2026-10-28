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
}
