package com.atirath.trading;

import com.atirath.trading.common.AccountStatus;
import com.atirath.trading.common.AccountType;
import com.atirath.trading.common.DocumentType;
import com.atirath.trading.common.util.AESEncryptionUtil;
import com.atirath.trading.config.JwtTokenProvider;
import com.atirath.trading.modules.auth.model.*;
import com.atirath.trading.modules.auth.repository.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProfileSettingsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private IndividualProfileRepository individualProfileRepository;

    @Autowired
    private IdentityKycDetailRepository identityKycDetailRepository;

    @Autowired
    private BankDetailRepository bankDetailRepository;

    @Autowired
    private AccountDocumentRepository accountDocumentRepository;

    @Autowired
    private UserCredentialRepository userCredentialRepository;

    @Autowired
    private UserBiometricCredentialRepository userBiometricCredentialRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AESEncryptionUtil aesEncryptionUtil;

    @Autowired
    private EntityManager entityManager;

    private UserAccount seedMember(long suffix, String name, String pan) {
        UserAccount user = userAccountRepository.save(UserAccount.builder()
                .accountType(AccountType.INDIVIDUAL)
                .email("profile.user." + suffix + "@test.com")
                .mobileNumber("8" + String.format("%09d", suffix % 1_000_000_000L))
                .referralCode("ATG" + (suffix % 100000))
                .accountStatus(AccountStatus.ACTIVE)
                .build());

        individualProfileRepository.save(IndividualProfile.builder()
                .userAccount(user)
                .fullName(name)
                .build());

        identityKycDetailRepository.save(IdentityKycDetail.builder()
                .userAccount(user)
                .panNumber(pan)
                .panFullName(name)
                .aadhaarNumberEncrypted(aesEncryptionUtil.encrypt("987654321234"))
                .dateOfBirth(LocalDate.of(1994, 5, 18))
                .build());

        bankDetailRepository.save(BankDetail.builder()
                .userAccount(user)
                .bankName("Kotak Mahindra Bank")
                .accountHolderName(name)
                .accountNumberEncrypted(aesEncryptionUtil.encrypt("112233445566"))
                .ifscCode("KKBK0000958")
                .branchName("Madhapur")
                .build());

        accountDocumentRepository.save(AccountDocument.builder()
                .userAccount(user)
                .documentType(DocumentType.IDENTITY_PROOF)
                .documentName("PAN Verification Card")
                .filePath("/uploads/pan_card_" + suffix + ".pdf")
                .mimeType("application/pdf")
                .fileSizeBytes(524288L)
                .build());

        userCredentialRepository.save(UserCredential.builder()
                .userAccount(user)
                .passwordHash(passwordEncoder.encode("SecretPass#123"))
                .passwordChangedAt(OffsetDateTime.now().minusDays(10))
                .build());

        userBiometricCredentialRepository.save(UserBiometricCredential.builder()
                .userAccount(user)
                .deviceId("device-ios-" + suffix)
                .deviceName("iPhone 15 Pro")
                .publicKey("pub-key-" + suffix)
                .isActive(true)
                .enrolledAt(OffsetDateTime.now().minusDays(5))
                .lastUsedAt(OffsetDateTime.now().minusHours(2))
                .build());

        entityManager.flush();
        entityManager.clear();
        return user;
    }

    @Test
    @DisplayName("Profile & Settings endpoints reject unauthenticated access with 401")
    void shouldRejectUnauthenticatedAccess() throws Exception {
        mockMvc.perform(get("/api/v1/profile")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/profile/kyc")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/profile/bank-accounts")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/profile/security")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/profile/devices")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/profile/documents")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/profile/settings")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Profile hydrates real DB data for authenticated member without Rohit Sharma mock data and persists updates")
    void shouldHydrateAndUpdateProfileFromDb() throws Exception {
        long suffix = System.nanoTime() % 1_000_000_000L;
        String pan = "ABCDE" + String.format("%04d", suffix % 10000) + "K";
        UserAccount user = seedMember(suffix, "Kiran Reddy", pan);
        String token = jwtTokenProvider.generateAccessToken(user);

        mockMvc.perform(get("/api/v1/profile").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fullName").value("Kiran Reddy"))
                .andExpect(jsonPath("$.data.email").value(user.getEmail()))
                .andExpect(jsonPath("$.data.personalInfo.panNumber").value(pan))
                .andExpect(jsonPath("$.data.fullName", not("Rohit Sharma")));

        mockMvc.perform(put("/api/v1/profile/personal")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"Kiran V Reddy\",\"occupation\":\"Managing Partner\",\"linkedInProfile\":\"https://linkedin.com/in/kiranreddy\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fullName").value("Kiran V Reddy"))
                .andExpect(jsonPath("$.data.occupation").value("Managing Partner"));

        mockMvc.perform(put("/api/v1/profile/address")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentAddress\":\"Jubilee Hills, Hyderabad - 500033\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.currentAddress").value("Jubilee Hills, Hyderabad - 500033"));

        mockMvc.perform(get("/api/v1/profile").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fullName").value("Kiran V Reddy"))
                .andExpect(jsonPath("$.data.addressInfo.currentAddress").value("Jubilee Hills, Hyderabad - 500033"));
    }

    @Test
    @DisplayName("KYC, Bank Accounts, Security password check, Devices, Documents & Settings operate on real per-user state")
    void shouldHandleKycBankSecurityDevicesDocumentsAndSettings() throws Exception {
        long suffix = (System.nanoTime() + 17) % 1_000_000_000L;
        String pan = "PQRSX" + String.format("%04d", suffix % 10000) + "M";
        UserAccount user = seedMember(suffix, "Meera Nair", pan);
        String token = jwtTokenProvider.generateAccessToken(user);

        // KYC & upload
        mockMvc.perform(get("/api/v1/profile/kyc").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fullName").value("Meera Nair"))
                .andExpect(jsonPath("$.data.documents", hasSize(5)));

        mockMvc.perform(post("/api/v1/profile/kyc/documents/5/upload")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fileUrl\":\"/uploads/self_declaration_meera.pdf\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("Under Review"));

        // Bank accounts
        MvcResult bankAddRes = mockMvc.perform(post("/api/v1/profile/bank-accounts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bankName\":\"HDFC Bank\",\"accountHolderName\":\"Meera Nair\",\"accountNumber\":\"50100998877665\",\"ifscCode\":\"HDFC0001234\",\"setAsPrimary\":false}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.bankName").value("HDFC Bank"))
                .andReturn();

        mockMvc.perform(get("/api/v1/profile/bank-accounts").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.bankAccounts", hasSize(2)))
                .andExpect(jsonPath("$.data.bankAccounts[0].bankName").value("Kotak Mahindra Bank"));

        // Wrong current password rejected
        mockMvc.perform(post("/api/v1/profile/security/change-password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\":\"WrongPass\",\"newPassword\":\"NewPass#789\",\"confirmPassword\":\"NewPass#789\"}"))
                .andExpect(status().isBadRequest());

        // Valid password change succeeds & updates hash in DB
        mockMvc.perform(post("/api/v1/profile/security/change-password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\":\"SecretPass#123\",\"newPassword\":\"NewPass#789\",\"confirmPassword\":\"NewPass#789\"}"))
                .andExpect(status().isOk());

        UserCredential updatedCred = userCredentialRepository.findByUserAccount_UserId(user.getUserId()).orElseThrow();
        assertTrue(passwordEncoder.matches("NewPass#789", updatedCred.getPasswordHash()));

        // Devices & revoke DB biometric credential
        UserBiometricCredential bio = userBiometricCredentialRepository.findByUserAccount_UserId(user.getUserId()).get(0);
        mockMvc.perform(get("/api/v1/profile/devices").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.devices[*].name", hasItem("iPhone 15 Pro")));

        mockMvc.perform(delete("/api/v1/profile/devices/bio-" + bio.getCredentialId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        UserBiometricCredential reloadedBio = userBiometricCredentialRepository.findById(bio.getCredentialId()).orElseThrow();
        assertFalse(reloadedBio.getIsActive());

        // Documents & upload
        mockMvc.perform(post("/api/v1/profile/documents/upload")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Board Resolution\",\"category\":\"Other\",\"fileName\":\"resolution.pdf\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("Board Resolution"));

        mockMvc.perform(get("/api/v1/profile/documents").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.documents[*].name", hasItem("Board Resolution")));

        // Settings
        mockMvc.perform(put("/api/v1/profile/settings/appearance")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"selectedTheme\":\"Dark\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.selectedTheme").value("Dark"));

        mockMvc.perform(get("/api/v1/profile/settings/export")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.downloadUrl").value("/api/v1/portfolio/download/report"));
    }
}
