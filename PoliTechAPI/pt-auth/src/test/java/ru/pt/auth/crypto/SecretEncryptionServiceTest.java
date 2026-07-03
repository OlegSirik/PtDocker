package ru.pt.auth.crypto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecretEncryptionServiceTest {

    private static final String DEV_MASTER_KEY_B64 =
            "MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=";

    @Test
    void roundTrip_encryptDecrypt() {
        AppSecretsProperties properties = new AppSecretsProperties();
        properties.setMasterKey(DEV_MASTER_KEY_B64);
        SecretEncryptionService service = new SecretEncryptionService(properties);

        String plaintext = "{\"apiKey\":\"sk-secret-value\"}";
        String encrypted = service.encrypt(plaintext);

        assertTrue(encrypted.startsWith("v1:"));
        assertNotEquals(plaintext, encrypted);
        assertEquals(plaintext, service.decrypt(encrypted));
    }
}
