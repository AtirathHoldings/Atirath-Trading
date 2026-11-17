package com.atirath.trading.modules.market.repository;

import com.atirath.trading.modules.market.model.EscrowAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EscrowAccountRepository extends JpaRepository<EscrowAccount, Long> {
    List<EscrowAccount> findByUserAccount_UserIdOrderByEscrowAccountIdDesc(Long userId);

    default Optional<EscrowAccount> findByUserAccount_UserId(Long userId) {
        List<EscrowAccount> list = findByUserAccount_UserIdOrderByEscrowAccountIdDesc(userId);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    Optional<EscrowAccount> findByAccountReference(String accountReference);
}
