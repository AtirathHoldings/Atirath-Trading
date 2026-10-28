package com.atirath.trading.modules.market.repository;

import com.atirath.trading.modules.market.model.TradeOrderDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TradeOrderDocumentRepository extends JpaRepository<TradeOrderDocument, Long> {
    List<TradeOrderDocument> findByOrder_OrderId(Long orderId);
}
