package com.atirath.trading;

import com.atirath.trading.modules.auth.dto.LoginRequest;
import com.atirath.trading.modules.auth.model.AdminAccount;
import com.atirath.trading.modules.auth.model.AdminLoginAttempt;
import com.atirath.trading.modules.auth.repository.AdminAccountRepository;
import com.atirath.trading.modules.auth.repository.AdminLoginAttemptRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminAuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AdminAccountRepository adminAccountRepository;

    @Autowired
    private AdminLoginAttemptRepository adminLoginAttemptRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String ADMIN_TEST_EMAIL = "admin.test@atirathgroup.com";
    private static final String ADMIN_TEST_PASSWORD = "AdminPassword@123";

    @BeforeEach
    void setUp() {
        if (!adminAccountRepository.existsByEmailIgnoreCase(ADMIN_TEST_EMAIL)) {
            AdminAccount admin = AdminAccount.builder()
                    .email(ADMIN_TEST_EMAIL)
                    .passwordHash(passwordEncoder.encode(ADMIN_TEST_PASSWORD))
                    .adminName("Test Admin Officer")
                    .adminStatus("ACTIVE")
                    .createdAt(OffsetDateTime.now())
                    .updatedAt(OffsetDateTime.now())
                    .build();
            adminAccountRepository.save(admin);
        }
    }

    @Test
    @DisplayName("Admin Login Success: records attempt in admin_login_attempts, updates last_login_at, and grants access to /parent/dashboard")
    void testAdminLoginSuccessAndAccessParentDashboard() throws Exception {
        LoginRequest loginRequest = LoginRequest.builder()
                .identifier(ADMIN_TEST_EMAIL)
                .password(ADMIN_TEST_PASSWORD)
                .build();

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/web/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.user.role").value("ADMIN"))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andReturn();

        JsonNode root = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String adminToken = root.path("data").path("accessToken").asText();
        assertNotNull(adminToken);

        // Verify admin_accounts.last_login_at was updated
        AdminAccount admin = adminAccountRepository.findByEmailIgnoreCase(ADMIN_TEST_EMAIL).orElseThrow();
        assertNotNull(admin.getLastLoginAt());

        // Verify admin_login_attempts has a successful record
        List<AdminLoginAttempt> attempts = adminLoginAttemptRepository.findByAdminAccount_AdminIdOrderByAttemptIdDesc(admin.getAdminId());
        assertFalse(attempts.isEmpty(), "Should have recorded login attempt in admin_login_attempts");
        assertTrue(attempts.get(0).getSuccess(), "Latest login attempt should be successful");

        // Verify Admin Token can access Parent Dashboard API
        mockMvc.perform(get("/api/v1/admin/parent/dashboard")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("Admin Login Failure: bad password records failure attempt in admin_login_attempts")
    void testAdminLoginFailureRecordsAttempt() throws Exception {
        LoginRequest loginRequest = LoginRequest.builder()
                .identifier(ADMIN_TEST_EMAIL)
                .password("WrongPassword@999")
                .build();

        mockMvc.perform(post("/api/v1/auth/web/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());

        AdminAccount admin = adminAccountRepository.findByEmailIgnoreCase(ADMIN_TEST_EMAIL).orElseThrow();
        List<AdminLoginAttempt> attempts = adminLoginAttemptRepository.findByAdminAccount_AdminIdOrderByAttemptIdDesc(admin.getAdminId());
        assertFalse(attempts.isEmpty());
        assertFalse(attempts.get(0).getSuccess(), "Failed login attempt must have success = false");
    }

    @Test
    @DisplayName("Regular User Login: still works normally and does NOT have ADMIN role")
    void testRegularUserLoginRemainsUnchanged() throws Exception {
        // Use standard demo account sairam.v@atirathgroup.com / Password@123
        LoginRequest userLogin = LoginRequest.builder()
                .identifier("sairam.v@atirathgroup.com")
                .password("Password@123")
                .build();

        MvcResult userResult = mockMvc.perform(post("/api/v1/auth/web/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userLogin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.user.role").value("MEMBER"))
                .andReturn();

        JsonNode root = objectMapper.readTree(userResult.getResponse().getContentAsString());
        String userToken = root.path("data").path("accessToken").asText();

        // Regular user should be forbidden from accessing admin endpoints
        mockMvc.perform(get("/api/v1/admin/parent/dashboard")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }
}
