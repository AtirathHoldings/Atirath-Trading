package com.atirath.trading.modules.auth.repository;

import com.atirath.trading.modules.auth.model.AdminLoginAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdminLoginAttemptRepository extends JpaRepository<AdminLoginAttempt, Long> {

    List<AdminLoginAttempt> findByAdminAccount_AdminIdOrderByAttemptIdDesc(Long adminId);
}
