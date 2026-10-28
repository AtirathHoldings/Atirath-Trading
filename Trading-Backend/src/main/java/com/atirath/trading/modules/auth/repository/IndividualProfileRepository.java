package com.atirath.trading.modules.auth.repository;

import com.atirath.trading.modules.auth.model.IndividualProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IndividualProfileRepository extends JpaRepository<IndividualProfile, Long> {

    Optional<IndividualProfile> findByUserAccount_UserId(Long userId);
}
