package com.atirath.trading.modules.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Audit service for recording admin login attempts in dedicated new transaction (REQUIRES_NEW)
 * so that failed attempts are committed even if login throws InvalidCredentialsException.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminAuditService {

    private final JdbcTemplate jdbcTemplate;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordAttempt(Long adminId, boolean success, String ipAddress) {
        String sanitizedIp = sanitizeIp(ipAddress);
        try {
            jdbcTemplate.update(
                    "INSERT INTO admin_login_attempts (admin_id, attempted_at, success, ip_address) VALUES (?, NOW(), ?, cast(? as inet))",
                    adminId, success, sanitizedIp
            );
        } catch (Exception ex) {
            log.warn("Failed to record admin login attempt with INET cast: {}. Retrying without IP.", ex.getMessage());
            try {
                jdbcTemplate.update(
                        "INSERT INTO admin_login_attempts (admin_id, attempted_at, success, ip_address) VALUES (?, NOW(), ?, NULL)",
                        adminId, success
                );
            } catch (Exception e) {
                log.error("Could not record admin login attempt: {}", e.getMessage());
            }
        }
    }

    private String sanitizeIp(String ip) {
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            return null;
        }
        String clean = ip.trim();
        if (clean.contains(":") || clean.matches("^\\d{1,3}(\\.\\d{1,3}){3}$")) {
            return clean;
        }
        return null;
    }
}
