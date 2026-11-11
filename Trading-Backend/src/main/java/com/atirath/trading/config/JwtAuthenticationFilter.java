package com.atirath.trading.config;

import com.atirath.trading.common.ApiResponse;
import com.atirath.trading.modules.auth.model.IdentityKycDetail;
import com.atirath.trading.modules.auth.repository.IdentityKycDetailRepository;
import com.atirath.trading.modules.auth.service.CustomUserDetailsService;
import com.atirath.trading.modules.auth.service.TokenBlacklistService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Period;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;
    private final TokenBlacklistService tokenBlacklistService;
    private final IdentityKycDetailRepository identityKycDetailRepository;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt)
                    && !tokenBlacklistService.isBlacklisted(jwt)
                    && jwtTokenProvider.validateToken(jwt)
                    && !jwtTokenProvider.isRefreshToken(jwt)) {
                Long userId = jwtTokenProvider.getUserIdFromToken(jwt);

                if (isTradingMutationRequest(request) && isUnder18User(userId)) {
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    ApiResponse<Object> apiResponse = ApiResponse.error(
                            "Age restriction: You must be at least 18 years old to buy or sell shares.");
                    response.getOutputStream().println(objectMapper.writeValueAsString(apiResponse));
                    return;
                }

                UserDetails userDetails = customUserDetailsService.loadUserById(userId);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            log.error("Could not set user authentication in security context: {}", ex.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private boolean isTradingMutationRequest(HttpServletRequest request) {
        String method = request.getMethod();
        if (!"POST".equalsIgnoreCase(method) && !"PUT".equalsIgnoreCase(method)) {
            return false;
        }
        String uri = request.getRequestURI();
        if (uri.endsWith("/cancel")) {
            return false;
        }
        return uri.startsWith("/api/v1/trade/orders")
                || uri.startsWith("/api/v1/trade/mobile/orders")
                || uri.equals("/api/v1/trade/mobile/preview");
    }

    private boolean isUnder18User(Long userId) {
        if (userId == null) {
            return false;
        }
        Optional<IdentityKycDetail> kycOpt = identityKycDetailRepository.findByUserAccount_UserId(userId);
        if (kycOpt.isEmpty() || kycOpt.get().getDateOfBirth() == null) {
            return false;
        }
        LocalDate dob = kycOpt.get().getDateOfBirth();
        return Period.between(dob, LocalDate.now()).getYears() < 18;
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
