package com.atirath.trading.modules.market.repository;

import com.atirath.trading.modules.market.model.MemberWatchlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberWatchlistRepository extends JpaRepository<MemberWatchlist, Long> {

    List<MemberWatchlist> findByUserAccount_UserIdOrderByCreatedAtDesc(Long userId);

    boolean existsByUserAccount_UserIdAndCompany_CompanyId(Long userId, Long companyId);

    Optional<MemberWatchlist> findByUserAccount_UserIdAndCompany_CompanyId(Long userId, Long companyId);

    void deleteByUserAccount_UserIdAndCompany_CompanyId(Long userId, Long companyId);
}
