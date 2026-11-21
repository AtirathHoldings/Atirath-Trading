package com.atirath.trading.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final ClientIntegrityFilter clientIntegrityFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)
                        .httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true).maxAgeInSeconds(31536000))
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(customAccessDeniedHandler)
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints: Registration, Login, Forgot Password, OTP, Biometric, WebSockets, Documentation
                        .requestMatchers(
                                "/api/v1/auth/register/**",
                                "/api/v1/auth/login",
                                "/api/v1/auth/devices/**",
                                "/api/v1/auth/forgot-password/**",
                                "/api/v1/auth/reset-password",
                                "/api/v1/auth/refresh-token",
                                "/api/v1/auth/logout",
                                "/api/v1/auth/web/register/**",
                                "/api/v1/auth/web/login",
                                "/api/v1/auth/web/devices/**",
                                "/api/v1/auth/web/forgot-password/**",
                                "/api/v1/auth/web/reset-password",
                                "/api/v1/auth/web/refresh-token",
                                "/api/v1/auth/web/logout",
                                "/api/v1/auth/web/otp/**",
                                "/api/v1/auth/mobile/register/**",
                                "/api/v1/auth/mobile/login",
                                "/api/v1/auth/mobile/devices/**",
                                "/api/v1/auth/mobile/forgot-password/**",
                                "/api/v1/auth/mobile/refresh-token",
                                "/api/v1/auth/mobile/otp/**",
                                "/api/v1/auth/mobile/biometric/**",
                                "/ws-trading/**",
                                "/ws-trading-raw/**",
                                "/ws-market/**",
                                "/ws-trading-live/**",
                                "/topic/**",
                                "/queue/**",
                                "/app/**",
                                "/api-docs/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/actuator/**"
                        ).permitAll()
                        // Admin-only endpoints
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        // Any other endpoint (Portfolio, Trade, Market, Negotiations, Escrow, Profile, Notifications) requires JWT Bearer Authentication
                        .anyRequest().authenticated()
                );

        http.addFilterBefore(clientIntegrityFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Strict OWASP CORS whitelist: Only authorized local dev & production origins
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:5173",
                "http://127.0.0.1:5173",
                "http://localhost:3000",
                "http://127.0.0.1:3000",
                "http://localhost:8080",
                "http://127.0.0.1:8080",
                "https://trade.atirathgroup.com",
                "https://portal.atirathgroup.com"
        ));
        configuration.setAllowedOriginPatterns(List.of("https://*.atirathgroup.com"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Accept",
                "Origin",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers",
                "X-Client-App",
                "X-Client-Key",
                "X-Device-Id",
                "X-Device-Name",
                "X-Device-Type",
                "*"
        ));
        configuration.setExposedHeaders(Arrays.asList("Access-Control-Allow-Origin", "Access-Control-Allow-Credentials", "Authorization"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
