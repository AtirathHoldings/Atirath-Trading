package com.atirath.trading.modules.market.repository;

import com.atirath.trading.modules.market.model.UserShareHolding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserShareHoldingRepository extends JpaRepository<UserShareHolding, Long> {
    List<UserShareHolding> findByUserAccount_UserId(Long userId);
    List<UserShareHolding> findByUserAccount_UserIdAndCompany_CompanyIdOrderByHoldingIdDesc(Long userId, Long companyId);

    default Optional<UserShareHolding> findByUserAccount_UserIdAndCompany_CompanyId(Long userId, Long companyId) {
        List<UserShareHolding> list = findByUserAccount_UserIdAndCompany_CompanyIdOrderByHoldingIdDesc(userId, companyId);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }
    Optional<UserShareHolding> findByUserAccount_UserIdAndCompany_SymbolIgnoreCase(Long userId, String symbol);
}
