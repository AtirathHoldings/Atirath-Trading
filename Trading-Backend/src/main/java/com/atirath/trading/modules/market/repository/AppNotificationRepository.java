package com.atirath.trading.modules.market.repository;

import com.atirath.trading.modules.market.model.AppNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppNotificationRepository extends JpaRepository<AppNotification, Long> {
    List<AppNotification> findByUserAccount_UserIdOrderByCreatedAtDesc(Long userId);
    long countByUserAccount_UserIdAndStatus(Long userId, String status);
}
