package com.atirath.trading.modules.admin.repository;

import com.atirath.trading.modules.admin.model.AdminAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdminAuditLogRepository extends JpaRepository<AdminAuditLog, Long> {
    List<AdminAuditLog> findByModuleNameOrderByCreatedAtDesc(String moduleName);
    List<AdminAuditLog> findAllByOrderByCreatedAtDesc();
}
