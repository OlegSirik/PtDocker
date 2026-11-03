package ru.pt.test.auth;

import io.restassured.response.Response;
import ru.pt.test.client.ApiClient;
import ru.pt.test.config.TestConfig;

import java.util.LinkedHashMap;
import java.util.Map;

public class AuthSession {

    private final ApiClient client;

    public AuthSession(ApiClient client) {
        this.client = client;
    }

    public String login(TestConfig.UserCreds user) {
        return login(
                client.effectiveTenant(),
                client.config().getClientId(),
                user.getLogin(),
                user.getPassword());
    }

    public String loginAs(String userKey) {
        return login(client.config().requireUser(userKey));
    }

    /**
     * Authenticate against {@code tenantCode}; restores previous tenant override after the login call.
     * Caller should set {@link ApiClient#setTenantOverride(String)} for subsequent API calls.
     */
    public String login(String tenantCode, String clientId, String userLogin, String password) {
        String prevTenant = client.getTenantOverride();
        try {
            client.setTenantOverride(tenantCode);
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("userLogin", userLogin);
            body.put("password", password);
            if (clientId != null && !clientId.isBlank()) {
                body.put("clientId", clientId);
            }
            Response r = client.post("/auth/login", body);
            if (r.statusCode() < 200 || r.statusCode() >= 300) {
                throw new IllegalStateException("Login failed for " + userLogin + " @ " + tenantCode
                        + ": HTTP " + r.statusCode() + " " + r.asString());
            }
            String token = r.jsonPath().getString("accessToken");
            if (token == null || token.isBlank()) {
                throw new IllegalStateException("Login response without accessToken: " + r.asString());
            }
            client.setBearerToken(token);
            // new session — acting account / API-key identity must be re-selected
            client.clearAccountId();
            client.clearApiKey();
            client.clearXClientId();
            return token;
        } finally {
            if (prevTenant != null) {
                client.setTenantOverride(prevTenant);
            } else {
                client.clearTenantOverride();
            }
        }
    }
}
