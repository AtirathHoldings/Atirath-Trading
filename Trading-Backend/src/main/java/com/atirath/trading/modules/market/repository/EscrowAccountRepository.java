package com.atirath.trading.modules.market.repository;

import com.atirath.trading.modules.market.model.EscrowAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EscrowAccountRepository extends JpaRepository<EscrowAccount, Long> {
    Optional<EscrowAccount> findByUserAccount_UserId(Long userId);
    Optional<EscrowAccount> findByAccountReference(String accountReference);
}
