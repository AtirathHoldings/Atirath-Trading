package com.atirath.trading.modules.market.repository;

import com.atirath.trading.modules.market.model.ShareLock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShareLockRepository extends JpaRepository<ShareLock, Long> {

    List<ShareLock> findByUserAccount_UserIdAndCompany_CompanyIdAndLockStatus(Long userId, Long companyId, String lockStatus);

    Optional<ShareLock> findByOrder_OrderId(Long orderId);

    @Query("SELECT COALESCE(SUM(s.lockedQuantity), 0) FROM ShareLock s " +
           "WHERE s.userAccount.userId = :userId AND s.company.companyId = :companyId AND s.lockStatus = 'LOCKED'")
    Long calculateTotalLockedShares(@Param("userId") Long userId, @Param("companyId") Long companyId);
}
