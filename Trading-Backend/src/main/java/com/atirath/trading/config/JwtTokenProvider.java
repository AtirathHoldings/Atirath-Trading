package com.atirath.trading.config;

import com.atirath.trading.modules.auth.model.AdminAccount;
import com.atirath.trading.modules.auth.model.UserAccount;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final AppProperties appProperties;

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(appProperties.getJwt().getSecret());
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(UserAccount account) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", account.getUserId());
        claims.put("email", account.getEmail());
        claims.put("mobileNumber", account.getMobileNumber());
        claims.put("accountType", account.getAccountType() != null ? account.getAccountType().name() : null);
        claims.put("accountStatus", account.getAccountStatus() != null ? account.getAccountStatus().name() : null);

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + appProperties.getJwt().getAccessTokenExpirationMs());

        return Jwts.builder()
                .subject(account.getUserId().toString())
                .claims(claims)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    public String generateAdminAccessToken(AdminAccount admin) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", admin.getAdminId());
        claims.put("adminId", admin.getAdminId());
        claims.put("email", admin.getEmail());
        claims.put("displayName", admin.getAdminName());
        claims.put("role", "ADMIN");
        claims.put("isAdmin", true);
        claims.put("accountType", "ADMIN");
        claims.put("accountStatus", admin.getAdminStatus());

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + appProperties.getJwt().getAccessTokenExpirationMs());

        return Jwts.builder()
                .subject(admin.getAdminId().toString())
                .claims(claims)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    public String generateRefreshToken(UserAccount account) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + appProperties.getJwt().getRefreshTokenExpirationMs());

        return Jwts.builder()
                .subject(account.getUserId().toString())
                .claim("tokenType", "REFRESH")
                .id(UUID.randomUUID().toString())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    public String generateAdminRefreshToken(AdminAccount admin) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + appProperties.getJwt().getRefreshTokenExpirationMs());

        return Jwts.builder()
                .subject(admin.getAdminId().toString())
                .claim("tokenType", "REFRESH")
                .claim("role", "ADMIN")
                .claim("isAdmin", true)
                .claim("adminId", admin.getAdminId())
                .id(UUID.randomUUID().toString())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return Long.parseLong(claims.getSubject());
    }

    public Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        }
        return false;
    }

    public long getAccessTokenExpirationSeconds() {
        return appProperties.getJwt().getAccessTokenExpirationMs() / 1000;
    }

    public java.time.Instant getExpirationFromToken(String token) {
        Date exp = getClaims(token).getExpiration();
        return exp != null ? exp.toInstant() : null;
    }

    public boolean isRefreshToken(String token) {
        try {
            Claims claims = getClaims(token);
            return "REFRESH".equals(claims.get("tokenType", String.class));
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
