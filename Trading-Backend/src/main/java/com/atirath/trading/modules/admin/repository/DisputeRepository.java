package com.atirath.trading.modules.admin.repository;

import com.atirath.trading.modules.admin.model.Dispute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DisputeRepository extends JpaRepository<Dispute, Long> {
    Optional<Dispute> findByDisputeReference(String disputeReference);
    List<Dispute> findByStatusOrderByCreatedAtDesc(String status);
    List<Dispute> findAllByOrderByCreatedAtDesc();
}
