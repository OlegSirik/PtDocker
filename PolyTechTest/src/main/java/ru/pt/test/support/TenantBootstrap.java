package ru.pt.test.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.restassured.response.Response;
import ru.pt.test.assertj.ApiAssert;
import ru.pt.test.client.ApiClient;
import ru.pt.test.config.TestConfig;
import ru.pt.test.scenario.ScenarioContext;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Create {@code test_<ts>} tenant + user (login=password=code) with TNT_ADMIN + PRODUCT_ADMIN.
 */
public final class TenantBootstrap {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private TenantBootstrap() {
    }

    public static TenantEnv create(ScenarioContext ctx) throws Exception {
        if (!ctx.config().hasUserPassword("sys")) {
            throw new AssertionError("users.sys not configured (set PT_SYS_PASSWORD)");
        }
        TestConfig.UserCreds sys = ctx.config().requireUser("sys");
        ApiClient client = ctx.client();
        // tenant codes are lowercase-only
        String requestedCode = ("test_" + System.currentTimeMillis()).toLowerCase(Locale.ROOT);

        ctx.auth().login("sys", "sys", sys.getLogin(), sys.getPassword());
        client.setTenantOverride("sys");
        client.clearImpersonateTenant();

        ObjectNode tenantBody = MAPPER.createObjectNode();
        tenantBody.put("code", requestedCode);
        tenantBody.put("name", requestedCode);
        tenantBody.put("authType", "LOCAL_JWT");
        tenantBody.put("storageType", "DB");
        tenantBody.put("recordStatus", "ACTIVE");

        Response createTenant = client.post("/admin/tenants", tenantBody);
        ApiAssert.statusOneOf(createTenant, 200, 201);

        JsonNode created = MAPPER.readTree(createTenant.asString());
        String code = (created.hasNonNull("code") ? created.get("code").asText() : requestedCode)
                .toLowerCase(Locale.ROOT);
        String userLogin = code;
        String userPassword = code;
        System.out.println("E2E created tenant " + code);

        client.setTenantOverride("sys");
        client.setImpersonateTenant(code);

        Response clients = client.get("/admin/clients");
        ApiAssert.success(clients);
        JsonNode clientsArr = MAPPER.readTree(clients.asString());
        if (!clientsArr.isArray() || clientsArr.isEmpty()) {
            throw new AssertionError("Expected default client on new tenant");
        }
        long defaultClientId = clientsArr.get(0).path("id").asLong();
        Response clientGet = client.get("/admin/clients/" + defaultClientId);
        ApiAssert.success(clientGet);
        JsonNode defaultClient = MAPPER.readTree(clientGet.asString());
        Long clientAccountId = nullIfMissing(defaultClient, "clientAccountId");
        Long defaultAccountId = nullIfMissing(defaultClient, "defaultAccountId");

        Map<String, Object> loginReq = new LinkedHashMap<>();
        loginReq.put("userLogin", userLogin);
        loginReq.put("fullName", code);
        loginReq.put("position", "admin");
        loginReq.put("password", userPassword);
        ApiAssert.success(client.post("/admin/logins", loginReq));

        if (clientAccountId == null) {
            throw new AssertionError("clientAccountId missing; cannot grant roles");
        }
        grantRole(client, defaultClientId, userLogin, "TNT_ADMIN");
        grantRole(client, defaultClientId, userLogin, "PRODUCT_ADMIN");

        if (defaultAccountId != null) {
            Map<String, Object> bind = Map.of("userLogin", userLogin, "isDefault", true);
            Response bindR = client.post("/admin/accounts/" + defaultAccountId + "/logins", bind);
            if (bindR.statusCode() < 200 || bindR.statusCode() >= 300) {
                System.out.println("E2E WARN bind ACCOUNT login HTTP " + bindR.statusCode()
                        + " (continue with TNT/PRODUCT_ADMIN roles)");
            }
        }

        client.clearImpersonateTenant();
        ctx.auth().login(code, "sys", userLogin, userPassword);
        client.setTenantOverride(code);
        long tntAdminAccountId = ActingAccount.selectRole(client, "TNT_ADMIN");
        long productAdminAccountId = ActingAccount.resolveRoleId(client, "PRODUCT_ADMIN");
        // leave TNT_ADMIN active for product install / grants
        client.setAccountId(tntAdminAccountId);

        TenantEnv env = new TenantEnv(
                code, userLogin, userPassword, defaultClientId, clientAccountId, defaultAccountId,
                tntAdminAccountId, productAdminAccountId);
        ctx.put("e2e.tenant", env);
        return env;
    }

    public static void deleteIfConfigured(ScenarioContext ctx, TenantEnv env) {
        if (env == null) {
            return;
        }
        boolean delete = ctx.config().getAdminLifecycle() == null
                || ctx.config().getAdminLifecycle().isDeleteTenant();
        ApiClient client = ctx.client();
        if (!delete) {
            System.out.println("E2E keep tenant " + env.code());
            client.clearImpersonateTenant();
            client.clearTenantOverride();
            return;
        }
        try {
            // drop X-Client-ID / X-Api-Key left from sales identity — they break sys delete
            client.clearSalesIdentity();
            TestConfig.UserCreds sys = ctx.config().requireUser("sys");
            ctx.auth().login("sys", "sys", sys.getLogin(), sys.getPassword());
            client.setTenantOverride("sys");
            client.clearImpersonateTenant();
            client.clearXClientId();
            client.clearApiKey();
            Response del = client.delete("/admin/tenants/" + env.code());
            if (del.statusCode() >= 200 && del.statusCode() < 300) {
                System.out.println("E2E soft-deleted tenant " + env.code());
            } else {
                System.out.println("E2E WARN delete " + env.code() + ": HTTP " + del.statusCode()
                        + " " + del.asString());
            }
        } catch (Exception e) {
            System.out.println("E2E WARN delete " + env.code() + ": " + e.getMessage());
        } finally {
            client.clearSalesIdentity();
            client.clearImpersonateTenant();
            client.clearTenantOverride();
        }
    }

    private static void grantRole(ApiClient client, long clientId, String userLogin, String role) {
        Response r = client.post("/admin/clients/" + clientId + "/members",
                Map.of("role", role, "userLogin", userLogin));
        ApiAssert.success(r);
        System.out.println("E2E granted " + role + " to " + userLogin);
    }

    private static Long nullIfMissing(JsonNode node, String field) {
        if (node != null && node.has(field) && !node.get(field).isNull()) {
            return node.get(field).asLong();
        }
        return null;
    }
}
