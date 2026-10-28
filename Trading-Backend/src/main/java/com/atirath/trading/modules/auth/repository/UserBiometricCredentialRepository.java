package com.atirath.trading.modules.auth.repository;

import com.atirath.trading.modules.auth.model.UserBiometricCredential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserBiometricCredentialRepository extends JpaRepository<UserBiometricCredential, Long> {

    Optional<UserBiometricCredential> findByUserAccount_UserIdAndDeviceId(Long userId, String deviceId);

    Optional<UserBiometricCredential> findByDeviceIdAndIsActiveTrue(String deviceId);

    Optional<UserBiometricCredential> findByUserAccount_UserIdAndDeviceIdAndIsActiveTrue(Long userId, String deviceId);

    List<UserBiometricCredential> findByUserAccount_UserId(Long userId);

    boolean existsByUserAccount_UserIdAndDeviceIdAndIsActiveTrue(Long userId, String deviceId);
}
