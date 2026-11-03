package ru.pt.test.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.Response;
import ru.pt.test.assertj.ApiAssert;
import ru.pt.test.client.ApiClient;

/**
 * Resolve acting account from {@code GET /auth/me} and set {@code X-Account-Id}.
 */
public final class ActingAccount {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private ActingAccount() {
    }

    /**
     * Call /auth/me, pick account with {@code role}, set on client. Returns account id.
     */
    public static long selectRole(ApiClient client, String role) throws Exception {
        long id = resolveRoleId(client, role);
        client.setAccountId(id);
        System.out.println("AUTH  X-Account-Id=" + id + " role=" + role);
        return id;
    }

    /**
     * Call /auth/me and return account id for {@code role} without changing the acting account.
     */
    public static long resolveRoleId(ApiClient client, String role) throws Exception {
        Long prev = client.getAccountId();
        try {
            client.clearAccountId();
            Response me = client.get("/auth/me");
            ApiAssert.success(me);
            JsonNode root = MAPPER.readTree(me.asString());
            JsonNode accounts = root.get("accounts");
            if (accounts == null || !accounts.isArray() || accounts.isEmpty()) {
                throw new AssertionError("/auth/me has no accounts[]: " + me.asString());
            }
            for (JsonNode a : accounts) {
                String r = a.path("role").asText("");
                if (role.equalsIgnoreCase(r)) {
                    long id = a.path("id").asLong();
                    if (id != 0L) {
                        return id;
                    }
                }
            }
            throw new AssertionError("Role " + role + " not in /auth/me accounts: " + accounts);
        } finally {
            if (prev != null) {
                client.setAccountId(prev);
            }
        }
    }
}
