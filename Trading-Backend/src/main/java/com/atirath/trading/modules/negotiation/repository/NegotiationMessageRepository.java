package com.atirath.trading.modules.negotiation.repository;

import com.atirath.trading.modules.negotiation.model.NegotiationMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NegotiationMessageRepository extends JpaRepository<NegotiationMessage, Long> {

    List<NegotiationMessage> findByNegotiation_NegotiationIdOrderBySentAtAsc(Long negotiationId);

    List<NegotiationMessage> findByNegotiation_NegotiationNoOrderBySentAtAsc(String negotiationNo);
}
