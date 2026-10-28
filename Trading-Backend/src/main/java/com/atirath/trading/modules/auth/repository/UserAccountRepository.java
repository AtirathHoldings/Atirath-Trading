package com.atirath.trading.modules.auth.repository;

import com.atirath.trading.modules.auth.model.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

    Optional<UserAccount> findByEmailIgnoreCase(String email);

    Optional<UserAccount> findByMobileNumber(String mobileNumber);

    @Query("SELECT u FROM UserAccount u WHERE LOWER(u.email) = LOWER(:identifier) OR u.mobileNumber = :identifier")
    Optional<UserAccount> findByEmailOrMobile(@Param("identifier") String identifier);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByMobileNumber(String mobileNumber);
}
