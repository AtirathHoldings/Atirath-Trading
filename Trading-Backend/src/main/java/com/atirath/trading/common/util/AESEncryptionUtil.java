package com.atirath.trading.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

@Slf4j
@Component
public class AESEncryptionUtil {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int TAG_LENGTH_BIT = 128;
    private static final int IV_LENGTH_BYTE = 12;
    private static final String SECRET_KEY_SEED = "AtirathTradingSecureEncryptionSeedKey2026";

    private final SecretKeySpec keySpec;

    public AESEncryptionUtil() {
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            byte[] key = sha.digest(SECRET_KEY_SEED.getBytes(StandardCharsets.UTF_8));
            this.keySpec = new SecretKeySpec(Arrays.copyOf(key, 32), "AES");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize AES encryption key", e);
        }
    }

    public String encrypt(String plaintext) {
        if (plaintext == null || plaintext.trim().isEmpty()) {
            return plaintext;
        }
        try {
            byte[] iv = new byte[IV_LENGTH_BYTE];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, parameterSpec);

            byte[] cipherText = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            byte[] combined = new byte[iv.length + cipherText.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(cipherText, 0, combined, iv.length, cipherText.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            log.error("Error encrypting sensitive data: {}", e.getMessage());
            throw new RuntimeException("Encryption failure", e);
        }
    }

    public String decrypt(String cipherTextBase64) {
        if (cipherTextBase64 == null || cipherTextBase64.trim().isEmpty()) {
            return cipherTextBase64;
        }
        try {
            byte[] combined = Base64.getDecoder().decode(cipherTextBase64);
            if (combined.length < IV_LENGTH_BYTE) {
                return cipherTextBase64;
            }

            byte[] iv = new byte[IV_LENGTH_BYTE];
            System.arraycopy(combined, 0, iv, 0, iv.length);

            byte[] cipherText = new byte[combined.length - iv.length];
            System.arraycopy(combined, iv.length, cipherText, 0, cipherText.length);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, parameterSpec);

            byte[] plainText = cipher.doFinal(cipherText);
            return new String(plainText, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.warn("Error decrypting data (may already be plaintext or corrupted): {}", e.getMessage());
            return cipherTextBase64;
        }
    }

    public static String maskAadhaar(String aadhaar) {
        if (aadhaar == null || aadhaar.length() < 4) {
            return "XXXX-XXXX-XXXX";
        }
        String clean = aadhaar.replaceAll("\\s|-", "");
        if (clean.length() >= 4) {
            String last4 = clean.substring(clean.length() - 4);
            return "XXXX-XXXX-" + last4;
        }
        return "XXXX-XXXX-XXXX";
    }

    public static String maskAccountNumber(String accountNo) {
        if (accountNo == null || accountNo.length() < 4) {
            return "XXXXXX";
        }
        String last4 = accountNo.substring(accountNo.length() - 4);
        return "XXXXXX" + last4;
    }
}
