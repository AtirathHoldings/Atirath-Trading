package com.atirath.trading.modules.admin.repository;

import com.atirath.trading.modules.admin.model.AdminApprovalWorkflow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdminApprovalWorkflowRepository extends JpaRepository<AdminApprovalWorkflow, Long> {
    Optional<AdminApprovalWorkflow> findByWorkflowCode(String workflowCode);
    List<AdminApprovalWorkflow> findByModuleAndIsActiveTrue(String module);
    List<AdminApprovalWorkflow> findAllByOrderByCreatedAtDesc();
}
