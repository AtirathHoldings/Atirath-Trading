package com.atirath.trading.modules.market.repository;

import com.atirath.trading.modules.market.model.OrderEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderEventRepository extends JpaRepository<OrderEvent, Long> {
    List<OrderEvent> findByOrder_OrderIdOrderByCreatedAtAsc(Long orderId);
}
