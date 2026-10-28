package com.atirath.trading.modules.auth.repository;

import com.atirath.trading.modules.auth.model.BankDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BankDetailRepository extends JpaRepository<BankDetail, Long> {

    Optional<BankDetail> findByUserAccount_UserId(Long userId);
}
