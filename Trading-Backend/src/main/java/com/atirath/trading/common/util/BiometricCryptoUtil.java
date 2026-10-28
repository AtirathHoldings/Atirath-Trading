package com.atirath.trading.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Slf4j
@Component
public class BiometricCryptoUtil {

    /**
     * Verifies cryptographic signature of a challenge using the stored public key.
     * Supports both ECDSA (P-256) and RSA public keys.
     */
    public boolean verifySignature(String publicKeyBase64, String challenge, String signatureBase64) {
        if (publicKeyBase64 == null || challenge == null || signatureBase64 == null) {
            return false;
        }

        try {
            byte[] keyBytes = cleanAndDecodeBase64(publicKeyBase64);
            byte[] sigBytes = cleanAndDecodeBase64(signatureBase64);
            byte[] dataBytes = challenge.getBytes(StandardCharsets.UTF_8);

            PublicKey publicKey = parsePublicKey(keyBytes);

            // Try ECDSA first (default for iOS Secure Enclave & modern Android Keystore)
            if (tryVerify("SHA256withECDSA", publicKey, dataBytes, sigBytes)) {
                return true;
            }

            // Try RSA next
            if (tryVerify("SHA256withRSA", publicKey, dataBytes, sigBytes)) {
                return true;
            }

            // Try alternative ECDSA algorithms (e.g. raw SHA256 / NONEwithECDSA)
            if (tryVerify("NONEwithECDSA", publicKey, dataBytes, sigBytes)) {
                return true;
            }

            log.warn("Biometric signature verification failed for provided public key algorithms");
            return false;
        } catch (Exception e) {
            log.error("Error during biometric signature verification: {}", e.getMessage());
            return false;
        }
    }

    private boolean tryVerify(String algorithm, PublicKey publicKey, byte[] data, byte[] signature) {
        try {
            Signature sig = Signature.getInstance(algorithm);
            sig.initVerify(publicKey);
            sig.update(data);
            return sig.verify(signature);
        } catch (Exception e) {
            // Alg mismatch or format mismatch, fall through to next algorithm
            return false;
        }
    }

    private PublicKey parsePublicKey(byte[] keyBytes) throws Exception {
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        try {
            KeyFactory ecFactory = KeyFactory.getInstance("EC");
            return ecFactory.generatePublic(spec);
        } catch (Exception e) {
            KeyFactory rsaFactory = KeyFactory.getInstance("RSA");
            return rsaFactory.generatePublic(spec);
        }
    }

    private byte[] cleanAndDecodeBase64(String input) {
        String clean = input.trim()
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s+", "");
        return Base64.getDecoder().decode(clean);
    }
}
