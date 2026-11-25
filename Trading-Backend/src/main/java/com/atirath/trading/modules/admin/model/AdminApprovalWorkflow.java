package com.atirath.trading.modules.admin.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "admin_approval_workflows", indexes = {
        @Index(name = "idx_approval_workflows_module", columnList = "module")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminApprovalWorkflow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "workflow_id")
    private Long workflowId;

    @Column(name = "workflow_code", nullable = false, unique = true, length = 50)
    private String workflowCode;

    @Column(name = "workflow_name", nullable = false, length = 150)
    private String workflowName;

    @Column(name = "module", nullable = false, length = 50)
    private String module;

    @Column(name = "trigger_condition", length = 255)
    private String triggerCondition;

    @Column(name = "required_role", nullable = false, length = 50)
    @Builder.Default
    private String requiredRole = "ROLE_ADMIN";

    @Column(name = "min_approvals", nullable = false)
    @Builder.Default
    private Integer minApprovals = 1;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
