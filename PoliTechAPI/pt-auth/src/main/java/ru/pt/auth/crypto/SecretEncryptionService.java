package ru.pt.auth.crypto;

import org.springframework.stereotype.Service;
import ru.pt.api.dto.exception.InternalServerErrorException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class SecretEncryptionService {

    private static final String VERSION_PREFIX = "v1:";
    private static final int GCM_TAG_BITS = 128;
    private static final int NONCE_BYTES = 12;

    private final SecretKey secretKey;
    private final SecureRandom secureRandom = new SecureRandom();

    public SecretEncryptionService(AppSecretsProperties properties) {
        String masterKeyB64 = properties.getMasterKey();
        if (masterKeyB64 == null || masterKeyB64.isBlank()) {
            throw new IllegalStateException(
                    "APP_SECRETS_MASTER_KEY is not configured (app.secrets.master-key)");
        }
        byte[] keyBytes;
        try {
            keyBytes = Base64.getDecoder().decode(masterKeyB64.trim());
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException("APP_SECRETS_MASTER_KEY must be valid base64", ex);
        }
        if (keyBytes.length != 32) {
            throw new IllegalStateException("APP_SECRETS_MASTER_KEY must decode to 32 bytes");
        }
        this.secretKey = new SecretKeySpec(keyBytes, "AES");
    }

    public String encrypt(String plaintext) {
        if (plaintext == null) {
            return null;
        }
        try {
            byte[] nonce = new byte[NONCE_BYTES];
            secureRandom.nextBytes(nonce);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_BITS, nonce));
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            byte[] payload = new byte[nonce.length + ciphertext.length];
            System.arraycopy(nonce, 0, payload, 0, nonce.length);
            System.arraycopy(ciphertext, 0, payload, nonce.length, ciphertext.length);

            return VERSION_PREFIX + Base64.getEncoder().encodeToString(payload);
        } catch (Exception ex) {
            throw new InternalServerErrorException("Failed to encrypt secret", ex);
        }
    }

    public String decrypt(String encrypted) {
        if (encrypted == null || encrypted.isBlank()) {
            return null;
        }
        if (!encrypted.startsWith(VERSION_PREFIX)) {
            throw new InternalServerErrorException("Unsupported secret encryption format");
        }
        try {
            byte[] payload = Base64.getDecoder().decode(encrypted.substring(VERSION_PREFIX.length()));
            if (payload.length <= NONCE_BYTES) {
                throw new InternalServerErrorException("Invalid encrypted secret payload");
            }
            byte[] nonce = new byte[NONCE_BYTES];
            byte[] ciphertext = new byte[payload.length - NONCE_BYTES];
            System.arraycopy(payload, 0, nonce, 0, NONCE_BYTES);
            System.arraycopy(payload, NONCE_BYTES, ciphertext, 0, ciphertext.length);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_BITS, nonce));
            byte[] plaintext = cipher.doFinal(ciphertext);
            return new String(plaintext, StandardCharsets.UTF_8);
        } catch (InternalServerErrorException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new InternalServerErrorException("Failed to decrypt secret", ex);
        }
    }
}
