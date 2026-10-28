package com.atirath.trading.modules.notification.repository;

import com.atirath.trading.modules.notification.model.MemberNotificationPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberNotificationPreferenceRepository extends JpaRepository<MemberNotificationPreference, Long> {
    List<MemberNotificationPreference> findByUserAccount_UserId(Long userId);
    Optional<MemberNotificationPreference> findByUserAccount_UserIdAndModeOfNotification(Long userId, String modeOfNotification);
}
