package com.atirath.trading.config;

import com.atirath.trading.common.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

/**
 * OWASP ASVS & API Security Client Integrity Filter.
 * Restricts all API access strictly to authorized clients:
 *  1. Official Flutter Mobile App (X-Client-App: ATIRATH_MOBILE)
 *  2. Official Web Portal (X-Client-App: ATIRATH_WEB or Whitelisted Browser Origin)
 *
 * Blocks direct browser address-bar URL navigation with 403 Forbidden.
 * Blocks unauthorized third-party scrapers, bots, and non-whitelisted cross-origin web requests.
 */
@Slf4j
@Component
public class ClientIntegrityFilter extends OncePerRequestFilter {

    public static final String HEADER_CLIENT_APP = "X-Client-App";
    public static final String HEADER_CLIENT_KEY = "X-Client-Key";

    public static final String CLIENT_MOBILE = "ATIRATH_MOBILE";
    public static final String CLIENT_WEB = "ATIRATH_WEB";
    public static final String SHARED_CLIENT_KEY = "atirath-sec-app-key-2026";

    private static final Set<String> ALLOWED_ORIGINS = Set.of(
            "http://localhost:5173",
            "http://127.0.0.1:5173",
            "http://localhost:5174",
            "http://127.0.0.1:5174",
            "http://localhost:5175",
            "http://127.0.0.1:5175",
            "http://localhost:3000",
            "http://127.0.0.1:3000",
            "http://localhost:3001",
            "http://127.0.0.1:3001",
            "http://localhost:4200",
            "http://127.0.0.1:4200",
            "http://localhost:8080",
            "http://127.0.0.1:8080",
            "http://localhost:8081",
            "http://127.0.0.1:8081",
            "https://trade.atirathgroup.com",
            "https://portal.atirathgroup.com"
    );

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String method = request.getMethod();
        if ("OPTIONS".equalsIgnoreCase(method)) {
            // CORS preflight must always pass unhindered
            return true;
        }

        String uri = request.getRequestURI();
        if (uri == null) return false;

        // Exempt documentation, monitoring, and WebSocket handshakes
        return uri.startsWith("/swagger-ui")
                || uri.startsWith("/v3/api-docs")
                || uri.startsWith("/api-docs")
                || uri.startsWith("/actuator")
                || uri.startsWith("/h2-console")
                || uri.startsWith("/ws-")
                || uri.startsWith("/topic")
                || uri.startsWith("/queue")
                || uri.startsWith("/app");
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        // 1. Block direct browser navigation (typing/pasting URL directly into browser address bar)
        if (isDirectBrowserNavigation(request)) {
            log.warn("Blocked direct browser URL navigation to {}", request.getRequestURI());
            writeForbiddenResponse(response,
                    "Direct browser URL access is restricted as per OWASP API security standards. Please access via the official Atirath Mobile App or Web Portal.");
            return;
        }

        // 2. Validate Client App and Key headers
        String clientApp = request.getHeader(HEADER_CLIENT_APP);
        String clientKey = request.getHeader(HEADER_CLIENT_KEY);
        String origin = request.getHeader("Origin");

        boolean isValidMobileClient = CLIENT_MOBILE.equalsIgnoreCase(clientApp)
                && SHARED_CLIENT_KEY.equals(clientKey);

        boolean isValidWebClient = (CLIENT_WEB.equalsIgnoreCase(clientApp) && SHARED_CLIENT_KEY.equals(clientKey))
                || isWhitelistedOrigin(origin);

        // Allow test environments / internal forwards if no client app header is explicitly declared
        boolean isInternalOrLocalTest = clientApp == null && origin == null && isLoopbackOrMock(request);

        if (isValidMobileClient || isValidWebClient || isInternalOrLocalTest) {
            filterChain.doFilter(request, response);
            return;
        }

        // Reject untrusted client
        log.warn("Blocked untrusted client request: uri={}, clientApp={}, origin={}",
                request.getRequestURI(), clientApp, origin);
        writeForbiddenResponse(response,
                "Access Denied: Untrusted client application. Requests are restricted to official Atirath Mobile App and Web Portal.");
    }

    private boolean isDirectBrowserNavigation(HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        String secFetchMode = request.getHeader("Sec-Fetch-Mode");
        String secFetchDest = request.getHeader("Sec-Fetch-Dest");

        // Browsers navigating directly to a URL send "navigate" or "document" or HTML accept headers
        return "navigate".equalsIgnoreCase(secFetchMode)
                || "document".equalsIgnoreCase(secFetchDest)
                || (accept != null && accept.contains("text/html") && !accept.contains("application/json"));
    }

    private boolean isWhitelistedOrigin(String origin) {
        if (origin == null || origin.isBlank()) return false;
        String cleanOrigin = origin.trim().toLowerCase();
        if (ALLOWED_ORIGINS.contains(cleanOrigin)) return true;
        if (cleanOrigin.startsWith("http://localhost:") || cleanOrigin.startsWith("http://127.0.0.1:")) return true;
        return cleanOrigin.endsWith(".atirathgroup.com");
    }

    private boolean isLoopbackOrMock(HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();
        return "127.0.0.1".equals(remoteAddr)
                || "0:0:0:0:0:0:0:1".equals(remoteAddr)
                || "localhost".equalsIgnoreCase(request.getServerName());
    }

    private void writeForbiddenResponse(HttpServletResponse response, String message) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        ApiResponse<Object> apiResponse = ApiResponse.error(message);
        response.getOutputStream().println(objectMapper.writeValueAsString(apiResponse));
    }
}
