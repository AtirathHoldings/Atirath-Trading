package com.atirath.trading.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Slf4j
@Component
@RequiredArgsConstructor
public class MarketRawWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;
    private final Set<WebSocketSession> sessions = new CopyOnWriteArraySet<>();
    // Track symbol subscriptions per session. If empty for a session, receives all symbols.
    private final Map<String, Set<String>> sessionSubscriptions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
        sessionSubscriptions.put(session.getId(), new CopyOnWriteArraySet<>());
        log.info("[WebSocket] New client connected: sessionId={}, remoteAddress={}", session.getId(), session.getRemoteAddress());

        // Send initial connection ACK
        Map<String, Object> welcome = new HashMap<>();
        welcome.put("type", "CONNECTED");
        welcome.put("sessionId", session.getId());
        welcome.put("message", "Connected to Atirath Real-Time Market Feed");
        welcome.put("supportedTypes", List.of("MARKET_TICK", "ORDER_BOOK", "TRADE", "SNAPSHOT"));
        welcome.put("timestamp", new Date());

        sendJson(session, welcome);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        String payload = message.getPayload();
        log.debug("[WebSocket] Received message from {}: {}", session.getId(), payload);

        try {
            JsonNode root = objectMapper.readTree(payload);
            String action = root.has("action") ? root.get("action").asText() : "";

            if ("ping".equalsIgnoreCase(action)) {
                Map<String, Object> pong = new HashMap<>();
                pong.put("type", "PONG");
                pong.put("timestamp", System.currentTimeMillis());
                sendJson(session, pong);
            } else if ("subscribe".equalsIgnoreCase(action)) {
                if (root.has("symbol")) {
                    String symbol = root.get("symbol").asText().trim().toUpperCase();
                    Set<String> subs = sessionSubscriptions.computeIfAbsent(session.getId(), k -> new CopyOnWriteArraySet<>());
                    subs.add(symbol);
                    // Also subscribe to canonical or alias
                    String alias = resolveSymbolAlias(symbol);
                    if (alias != null) subs.add(alias);
                    log.info("[WebSocket] Session {} subscribed to symbol: {} (alias: {})", session.getId(), symbol, alias);
                }
            } else if ("unsubscribe".equalsIgnoreCase(action)) {
                if (root.has("symbol")) {
                    String symbol = root.get("symbol").asText().trim().toUpperCase();
                    Set<String> subs = sessionSubscriptions.get(session.getId());
                    if (subs != null) {
                        subs.remove(symbol);
                        String alias = resolveSymbolAlias(symbol);
                        if (alias != null) subs.remove(alias);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("[WebSocket] Failed to parse message from {}: {}", session.getId(), e.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
        sessionSubscriptions.remove(session.getId());
        log.info("[WebSocket] Connection closed: sessionId={}, status={}", session.getId(), status);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.warn("[WebSocket] Transport error for sessionId={}: {}", session.getId(), exception.getMessage());
        try {
            session.close(CloseStatus.SERVER_ERROR);
        } catch (IOException ignored) {}
        sessions.remove(session);
        sessionSubscriptions.remove(session.getId());
    }

    public void broadcast(Object payload) {
        if (sessions.isEmpty()) return;
        try {
            String json = objectMapper.writeValueAsString(payload);
            TextMessage textMessage = new TextMessage(json);

            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    try {
                        synchronized (session) {
                            session.sendMessage(textMessage);
                        }
                    } catch (Exception e) {
                        log.warn("[WebSocket] Error sending message to session {}: {}", session.getId(), e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.error("[WebSocket] Failed to serialize broadcast payload", e);
        }
    }

    public void broadcastSymbolEvent(String symbol, Object payload) {
        if (sessions.isEmpty()) return;
        try {
            String json = objectMapper.writeValueAsString(payload);
            TextMessage textMessage = new TextMessage(json);
            String normSymbol = symbol != null ? symbol.toUpperCase() : "";

            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    Set<String> subs = sessionSubscriptions.get(session.getId());
                    // If no explicit symbol subscribed, send all. Otherwise filter by subscribed symbols.
                    if (subs == null || subs.isEmpty() || subs.contains(normSymbol)) {
                        try {
                            synchronized (session) {
                                session.sendMessage(textMessage);
                            }
                        } catch (Exception e) {
                            log.warn("[WebSocket] Error sending symbol event to session {}: {}", session.getId(), e.getMessage());
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("[WebSocket] Failed to serialize symbol event", e);
        }
    }

    public void sendJson(WebSocketSession session, Object payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            synchronized (session) {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(json));
                }
            }
        } catch (Exception e) {
            log.warn("[WebSocket] Error sending JSON to session {}: {}", session.getId(), e.getMessage());
        }
    }

    public int getActiveSessionCount() {
        return sessions.size();
    }

    private String resolveSymbolAlias(String sym) {
        if (sym == null) return null;
        return switch (sym) {
            case "AHIL" -> "ATG-H";
            case "ATG-H" -> "AHIL";
            case "ATTL" -> "ATG-T";
            case "ATG-T" -> "ATTL";
            case "FVTPL" -> "ATG-E";
            case "ATG-E" -> "FVTPL";
            case "SIEA" -> "ATG-F";
            case "ATG-F" -> "SIEA";
            case "GEML" -> "ATG-L";
            case "ATG-L" -> "GEML";
            case "TEBPL" -> "ATG-R";
            case "ATG-R" -> "TEBPL";
            default -> null;
        };
    }
}
