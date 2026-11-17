package com.atirath.trading.modules.market.repository;

import com.atirath.trading.modules.market.model.MarketOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MarketOrderRepository extends JpaRepository<MarketOrder, Long> {

    Optional<MarketOrder> findByOrderNo(String orderNo);

    List<MarketOrder> findByUserAccount_UserIdOrderByPlacedAtDesc(Long userId);

    List<MarketOrder> findByCompany_CompanyIdOrderByPlacedAtDesc(Long companyId);

    List<MarketOrder> findByCompany_CompanyIdAndStatusOrderByPlacedAtDesc(Long companyId, String status);

    List<MarketOrder> findByCompany_CompanyIdAndStatusInOrderByPlacedAtDesc(Long companyId, List<String> statuses);

    long countByOrderSideIgnoreCaseAndStatusIn(String orderSide, List<String> statuses);
}
