package com.atirath.trading.modules.auth.service;

import com.atirath.trading.common.DeviceLimitExceededException;
import com.atirath.trading.modules.auth.dto.device.ActiveDeviceSessionDto;
import com.atirath.trading.modules.auth.dto.device.DeviceLimitExceededDto;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Enforces a strict limit of 2 active devices per user (combined across Web and Mobile).
 * When a 3rd device attempts login, returns 409 Conflict with the 2 active devices
 * and enables selective logout with immediate token revocation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserDeviceSessionManager {

    private static final int MAX_ACTIVE_DEVICES = 2;
    private final TokenBlacklistService tokenBlacklistService;

    // UserId -> (DeviceId -> ActiveDeviceSession)
    private final Map<Long, Map<String, StoredDeviceSession>> userSessions = new ConcurrentHashMap<>();

    // ResolutionToken -> PendingResolution
    private final Map<String, PendingResolution> pendingResolutions = new ConcurrentHashMap<>();

    @Data
    @Builder
    public static class StoredDeviceSession {
        private String sessionId;
        private String deviceId;
        private String deviceName;
        private String os;
        private String deviceType; // WEB or MOBILE
        private String ipAddress;
        private String location;
        private Instant loginTime;
        private Instant lastActive;
        private String accessToken;
        private String refreshToken;
    }

    @Data
    @Builder
    public static class PendingResolution {
        private String resolutionToken;
        private Long userId;
        private String identifier;
        private String password;
        private String pendingDeviceId;
        private String pendingDeviceName;
        private String pendingDeviceType;
        private String ipAddress;
        private Instant expiresAt;
    }

    /**
     * Evaluates whether a login attempt is allowed or exceeds the 2-device limit.
     * Throws DeviceLimitExceededException if user already has 2 active devices
     * and this is a new/different device.
     */
    public void evaluateDeviceLimit(Long userId,
                                    String clientDeviceId,
                                    String clientDeviceName,
                                    String clientDeviceType,
                                    String ipAddress,
                                    String userAgent,
                                    String identifier,
                                    String password) {

        Map<String, StoredDeviceSession> sessions = getOrInitSessions(userId);

        String effDeviceId = (clientDeviceId != null && !clientDeviceId.isBlank())
                ? clientDeviceId.trim()
                : generateFallbackDeviceId(clientDeviceType, ipAddress, userAgent);

        // If this device is already logged in, allow re-authentication/session update
        if (sessions.containsKey(effDeviceId)) {
            log.info("[DeviceLimit] Device {} is already logged in for user {}. Allowing login.", effDeviceId, userId);
            return;
        }

        // If active devices count is strictly less than limit (0 or 1), allow
        if (sessions.size() < MAX_ACTIVE_DEVICES) {
            log.info("[DeviceLimit] User {} currently has {} active devices. Below limit of {}. Allowing login.",
                    userId, sessions.size(), MAX_ACTIVE_DEVICES);
            return;
        }

        // Limit of 2 devices is reached and this is a NEW device!
        log.warn("[DeviceLimit] User {} exceeded device limit ({} active devices). Triggering kickout selection.",
                userId, sessions.size());

        List<ActiveDeviceSessionDto> activeList = new ArrayList<>();
        for (StoredDeviceSession s : sessions.values()) {
            activeList.add(mapToDto(s, false));
        }

        String resolutionToken = "RES-" + UUID.randomUUID().toString();
        PendingResolution pending = PendingResolution.builder()
                .resolutionToken(resolutionToken)
                .userId(userId)
                .identifier(identifier)
                .password(password)
                .pendingDeviceId(effDeviceId)
                .pendingDeviceName(clientDeviceName)
                .pendingDeviceType(clientDeviceType)
                .ipAddress(ipAddress)
                .expiresAt(Instant.now().plusSeconds(300)) // 5 mins validity
                .build();
        pendingResolutions.put(resolutionToken, pending);

        DeviceLimitExceededDto dto = DeviceLimitExceededDto.builder()
                .errorCode("DEVICE_LIMIT_EXCEEDED")
                .maxAllowed(MAX_ACTIVE_DEVICES)
                .resolutionToken(resolutionToken)
                .message("Active device limit reached. Maximum 2 active devices allowed across Web and Mobile. Please select an existing device to log out.")
                .activeDevices(activeList)
                .build();

        throw new DeviceLimitExceededException(dto);
    }

    /**
     * Registers or updates a device session upon successful login.
     */
    public void registerDeviceSession(Long userId,
                                      String clientDeviceId,
                                      String clientDeviceName,
                                      String clientDeviceType,
                                      String ipAddress,
                                      String userAgent,
                                      String accessToken,
                                      String refreshToken) {

        Map<String, StoredDeviceSession> sessions = getOrInitSessions(userId);

        String effDeviceId = (clientDeviceId != null && !clientDeviceId.isBlank())
                ? clientDeviceId.trim()
                : generateFallbackDeviceId(clientDeviceType, ipAddress, userAgent);

        String effDeviceName = (clientDeviceName != null && !clientDeviceName.isBlank())
                ? clientDeviceName
                : resolveDeviceName(clientDeviceType, userAgent);

        String os = resolveOsAndLocation(clientDeviceType, userAgent, ipAddress);

        StoredDeviceSession session = StoredDeviceSession.builder()
                .sessionId(UUID.randomUUID().toString())
                .deviceId(effDeviceId)
                .deviceName(effDeviceName)
                .deviceType(clientDeviceType != null ? clientDeviceType.toUpperCase() : "WEB")
                .os(os)
                .ipAddress(ipAddress != null ? ipAddress : "103.21.124.5")
                .location("Mumbai, Maharashtra")
                .loginTime(Instant.now())
                .lastActive(Instant.now())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();

        // Enforce max 2: if adding this exceeds 2, remove oldest session
        if (!sessions.containsKey(effDeviceId) && sessions.size() >= MAX_ACTIVE_DEVICES) {
            String oldestId = sessions.values().stream()
                    .min(Comparator.comparing(StoredDeviceSession::getLoginTime))
                    .map(StoredDeviceSession::getDeviceId)
                    .orElse(null);
            if (oldestId != null) {
                disconnectDevice(userId, oldestId);
            }
        }

        sessions.put(effDeviceId, session);
        log.info("[DeviceLimit] Registered session for user {} on device {} (Total: {}/{})",
                userId, effDeviceId, sessions.size(), MAX_ACTIVE_DEVICES);
    }

    /**
     * Disconnects a specific device: invalidates its JWT tokens immediately and removes its session.
     */
    public boolean disconnectDevice(Long userId, String deviceId) {
        if (userId == null || deviceId == null || deviceId.isBlank()) return false;
        Map<String, StoredDeviceSession> sessions = userSessions.get(userId);
        if (sessions == null) return false;

        String targetId = deviceId.trim();
        StoredDeviceSession removed = sessions.remove(targetId);
        if (removed == null) {
            // Case-insensitive fallback lookup
            for (Map.Entry<String, StoredDeviceSession> entry : sessions.entrySet()) {
                if (entry.getKey().equalsIgnoreCase(targetId)) {
                    removed = sessions.remove(entry.getKey());
                    break;
                }
            }
        }

        if (removed != null) {
            // Revoke its access token and refresh token in token blacklist
            if (removed.getAccessToken() != null && !removed.getAccessToken().isBlank()) {
                tokenBlacklistService.revokeToken(removed.getAccessToken(), Instant.now().plusSeconds(86400));
            }
            if (removed.getRefreshToken() != null && !removed.getRefreshToken().isBlank()) {
                tokenBlacklistService.revokeToken(removed.getRefreshToken(), Instant.now().plusSeconds(604800));
            }
            log.info("[DeviceLimit] Disconnected device {} for user {}. Tokens blacklisted for auto-logout.",
                    deviceId, userId);
            return true;
        }
        return false;
    }

    /**
     * Resolves pending resolution and disconnects the requested device so the new login can complete.
     */
    public PendingResolution consumeResolution(String resolutionToken, String deviceIdToDisconnect) {
        if (resolutionToken == null) return null;
        PendingResolution pending = pendingResolutions.remove(resolutionToken);
        if (pending == null || Instant.now().isAfter(pending.getExpiresAt())) {
            return null;
        }

        // Disconnect the requested device
        disconnectDevice(pending.getUserId(), deviceIdToDisconnect);
        return pending;
    }

    /**
     * Returns list of active devices for a user (at most 2).
     */
    public List<ActiveDeviceSessionDto> getActiveDevicesForUser(Long userId, String currentDeviceId) {
        Map<String, StoredDeviceSession> sessions = getOrInitSessions(userId);
        List<ActiveDeviceSessionDto> list = new ArrayList<>();
        for (StoredDeviceSession s : sessions.values()) {
            boolean isCurr = currentDeviceId != null && currentDeviceId.equals(s.getDeviceId());
            list.add(mapToDto(s, isCurr));
        }
        return list;
    }

    /**
     * Disconnects all devices for this user except current.
     */
    public void disconnectAllOtherDevices(Long userId, String currentDeviceId) {
        Map<String, StoredDeviceSession> sessions = userSessions.get(userId);
        if (sessions == null) return;

        List<String> toRemove = new ArrayList<>();
        for (String devId : sessions.keySet()) {
            if (currentDeviceId == null || !currentDeviceId.equals(devId)) {
                toRemove.add(devId);
            }
        }
        for (String id : toRemove) {
            disconnectDevice(userId, id);
        }
    }

    // =========================================================================
    // INTERNAL HELPERS
    // =========================================================================

    public void clearSessionsForUser(Long userId) {
        if (userId != null) {
            userSessions.remove(userId);
        }
    }

    private Map<String, StoredDeviceSession> getOrInitSessions(Long userId) {
        return userSessions.computeIfAbsent(userId, k -> new ConcurrentHashMap<>());
    }

    private ActiveDeviceSessionDto mapToDto(StoredDeviceSession s, boolean isCurrent) {
        return ActiveDeviceSessionDto.builder()
                .deviceId(s.getDeviceId())
                .name(s.getDeviceName())
                .os(s.getOs())
                .deviceType(s.getDeviceType())
                .lastActive(isCurrent ? "Now — Current Device" : "Active")
                .location(s.getLocation())
                .ipAddress(s.getIpAddress())
                .isCurrent(isCurrent)
                .loginTime(s.getLoginTime())
                .build();
    }

    private String generateFallbackDeviceId(String deviceType, String ipAddress, String userAgent) {
        String base = (deviceType != null ? deviceType : "DEV") + "-" + (ipAddress != null ? ipAddress : "127.0.0.1");
        return "dev-" + Math.abs(base.hashCode() % 1000000);
    }

    private String resolveDeviceName(String deviceType, String userAgent) {
        if ("MOBILE".equalsIgnoreCase(deviceType)) {
            return "Mobile App Client";
        }
        if (userAgent != null) {
            if (userAgent.contains("Chrome")) return "Chrome on Windows";
            if (userAgent.contains("Safari")) return "Safari on macOS";
            if (userAgent.contains("Firefox")) return "Firefox Browser";
        }
        return "Web Browser Client";
    }

    private String resolveOsAndLocation(String deviceType, String userAgent, String ip) {
        String os = "Web Browser";
        if (userAgent != null) {
            if (userAgent.contains("Windows")) os = "Windows 11";
            else if (userAgent.contains("Macintosh") || userAgent.contains("Mac OS")) os = "macOS 15.1";
            else if (userAgent.contains("Android")) os = "Android 14";
            else if (userAgent.contains("iPhone") || userAgent.contains("iPad")) os = "iOS 18.2";
        } else if ("MOBILE".equalsIgnoreCase(deviceType)) {
            os = "Mobile OS";
        }
        return os + " · Mumbai, Maharashtra";
    }
}
