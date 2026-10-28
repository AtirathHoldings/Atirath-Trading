package com.atirath.trading.modules.auth.repository;

import com.atirath.trading.modules.auth.model.CorporateProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CorporateProfileRepository extends JpaRepository<CorporateProfile, Long> {

    Optional<CorporateProfile> findByUserAccount_UserId(Long userId);

    boolean existsByRegistrationId(String registrationId);
}
