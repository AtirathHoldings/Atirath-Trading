package com.atirath.trading.modules.auth.repository;

import com.atirath.trading.modules.auth.model.IdentityKycDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IdentityKycDetailRepository extends JpaRepository<IdentityKycDetail, Long> {

    Optional<IdentityKycDetail> findByUserAccount_UserId(Long userId);

    boolean existsByPanNumber(String panNumber);
}
