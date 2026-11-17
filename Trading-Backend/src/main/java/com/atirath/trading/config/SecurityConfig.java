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
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(customAccessDeniedHandler)
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints: Registration, Login, Forgot Password, Web, Mobile, Swagger, Actuator
                        .requestMatchers(
                                "/api/v1/auth/register/**",
                                "/api/v1/auth/login",
                                "/api/v1/auth/forgot-password/**",
                                "/api/v1/auth/reset-password",
                                "/api/v1/auth/refresh-token",
                                "/api/v1/auth/logout",
                                "/api/v1/auth/web/register/**",
                                "/api/v1/auth/web/login",
                                "/api/v1/auth/web/forgot-password",
                                "/api/v1/auth/web/forgot-password/mobile",
                                "/api/v1/auth/web/reset-password",
                                "/api/v1/auth/web/refresh-token",
                                "/api/v1/auth/web/logout",
                                "/api/v1/auth/web/otp/**",
                                "/api/v1/auth/mobile/register/**",
                                "/api/v1/auth/mobile/login",
                                "/api/v1/auth/mobile/forgot-password",
                                "/api/v1/auth/mobile/refresh-token",
                                "/api/v1/auth/mobile/otp/**",
                                "/api/v1/auth/mobile/biometric/initiate",
                                "/api/v1/auth/mobile/biometric/verify",
                                "/h2-console/**",
                                "/api/v1/dashboard/**",
                                "/api/v1/actions/**",
                                "/api/v1/transactions/**",
                                "/api/v1/market/**",
                                "/api/v1/trade/**",
                                "/api/v1/negotiations/**",
                                "/api/v1/escrow/**",
                                "/api/v1/portfolio/**",
                                "/api/v1/notifications/**",
                                "/api/v1/external-requests/**",
                                "/api/v1/profile/**",
                                "/api/v1/admin/**",
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
                        // Any other endpoint requires JWT Bearer Authentication
                        .anyRequest().authenticated()
                );

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin", "Access-Control-Request-Method", "Access-Control-Request-Headers"));
        configuration.setExposedHeaders(Arrays.asList("Access-Control-Allow-Origin", "Access-Control-Allow-Credentials", "Authorization"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
