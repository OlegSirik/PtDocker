package ru.pt.auth.crypto;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.secrets")
public class AppSecretsProperties {

    /**
     * Base64-encoded 32-byte AES-256 key. Env: APP_SECRETS_MASTER_KEY.
     */
    private String masterKey;

    public String getMasterKey() {
        return masterKey;
    }

    public void setMasterKey(String masterKey) {
        this.masterKey = masterKey;
    }
}
