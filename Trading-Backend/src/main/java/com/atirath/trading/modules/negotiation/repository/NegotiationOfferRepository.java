package com.atirath.trading.modules.negotiation.repository;

import com.atirath.trading.modules.negotiation.model.NegotiationOffer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NegotiationOfferRepository extends JpaRepository<NegotiationOffer, Long> {

    List<NegotiationOffer> findByNegotiation_NegotiationIdOrderByCreatedAtAsc(Long negotiationId);

    List<NegotiationOffer> findByNegotiation_NegotiationNoOrderByCreatedAtAsc(String negotiationNo);
}
