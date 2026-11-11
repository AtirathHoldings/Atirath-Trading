package com.atirath.trading.modules.auth.service;

import com.atirath.trading.common.util.HashUtil;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenBlacklistService {

    private final Map<String, Instant> revokedTokens = new ConcurrentHashMap<>();

    /**
     * Marks a JWT access or refresh token as revoked until its natural expiration time.
     */
    public void revokeToken(String token, Instant expiresAt) {
        if (token == null || token.isBlank()) {
            return;
        }
        pruneExpiredIfNeeded();
        Instant expiry = expiresAt != null ? expiresAt : Instant.now().plusSeconds(3600);
        revokedTokens.put(HashUtil.sha256(token.trim()), expiry);
    }

    /**
     * Returns true if the token has been explicitly revoked (e.g. on logout) and has not yet expired.
     */
    public boolean isBlacklisted(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        String key = HashUtil.sha256(token.trim());
        Instant expiry = revokedTokens.get(key);
        if (expiry == null) {
            return false;
        }
        if (Instant.now().isAfter(expiry)) {
            revokedTokens.remove(key);
            return false;
        }
        return true;
    }

    private void pruneExpiredIfNeeded() {
        if (revokedTokens.size() > 1000) {
            Instant now = Instant.now();
            revokedTokens.entrySet().removeIf(entry -> now.isAfter(entry.getValue()));
        }
    }
}

