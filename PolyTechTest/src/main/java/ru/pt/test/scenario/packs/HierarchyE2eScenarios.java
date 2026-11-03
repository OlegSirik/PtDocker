package ru.pt.test.scenario.packs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.restassured.response.Response;
import ru.pt.test.assertj.ApiAssert;
import ru.pt.test.client.ApiClient;
import ru.pt.test.scenario.Scenario;
import ru.pt.test.scenario.ScenarioContext;
import ru.pt.test.support.ActingAccount;
import ru.pt.test.support.ProductInstaller;
import ru.pt.test.support.TenantBootstrap;
import ru.pt.test.support.TenantEnv;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Tenant + product + CLIENT/USER clients, account tree under sys, users, policies.
 * Run: {@code --tags=e2e-hierarchy}
 */
public final class HierarchyE2eScenarios {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final int POLICIES_PER_USER = 5;
    private static final String SYS_AUTH_CLIENT_ID = "sys";

    private HierarchyE2eScenarios() {
    }

    public static List<Scenario> all() {
        return List.of(
                Scenario.of("e2e.hierarchy",
                        "Tenant → product → CLIENT client → sys USER tree → policies + list counts",
                        Set.of("e2e-hierarchy"),
                        HierarchyE2eScenarios::run)
        );
    }

    private static void run(ScenarioContext ctx) throws Exception {
        TenantEnv env = null;
        try {
            System.out.println("HIER step 1: tenant + PRODUCT_ADMIN/TNT_ADMIN (lowercase code)");
            env = TenantBootstrap.create(ctx);
            if (!env.code().equals(env.code().toLowerCase(Locale.ROOT))) {
                throw new AssertionError("Tenant code must be lowercase: " + env.code());
            }

            System.out.println("HIER step 2: install CARGO_GENERAL (PROD+DEV)");
            long productId = ProductInstaller.installCargoGeneral(ctx, env);

            asTntAdmin(ctx, env);

            System.out.println("HIER step 3: separate client authLevel=CLIENT");
            runClientAuthLevelPhase(ctx, env, productId);

            System.out.println("HIER step 4: sys client authLevel=USER + account tree");
            runSysUserHierarchyPhase(ctx, env, productId);

            System.out.println("HIER hierarchy OK on tenant " + env.code());
        } finally {
            TenantBootstrap.deleteIfConfigured(ctx, env);
        }
    }

    private static void runClientAuthLevelPhase(ScenarioContext ctx, TenantEnv env, long productId)
            throws Exception {
        ApiClient admin = ctx.client();
        asTntAdmin(ctx, env);

        String authClientId = ("cli_client_" + System.currentTimeMillis()).toLowerCase(Locale.ROOT);
        ObjectNode create = MAPPER.createObjectNode();
        create.put("authClientId", authClientId);
        create.put("name", "Hierarchy CLIENT app");
        create.put("authLevel", "CLIENT");
        create.put("authType", "APIKEY");
        create.put("recordStatus", "ACTIVE");

        Response createdR = admin.post("/admin/clients", create);
        ApiAssert.success(createdR);
        JsonNode created = MAPPER.readTree(createdR.asString());
        long clientId = created.path("id").asLong();
        if (clientId == 0L) {
            throw new AssertionError("client create without id: " + createdR.asString());
        }

        JsonNode client = getClient(admin, clientId);
        Long clientAccountId = nullIfZero(client.path("clientAccountId").asLong());
        Long defaultAccountId = nullIfZero(client.path("defaultAccountId").asLong());
        if (clientAccountId == null) {
            throw new AssertionError("clientAccountId missing after create");
        }
        if (defaultAccountId == null) {
            defaultAccountId = createChild(admin, clientAccountId, "Default account", "ACCOUNT");
            ObjectNode upd = (ObjectNode) client.deepCopy();
            upd.put("defaultAccountId", defaultAccountId);
            ApiAssert.success(admin.put("/admin/clients/" + clientId, upd));
            client = getClient(admin, clientId);
            defaultAccountId = nullIfZero(client.path("defaultAccountId").asLong());
        }
        System.out.println("HIER CLIENT clientId=" + clientId
                + " defaultAccountId=" + defaultAccountId);

        grantProduct(admin, clientId, defaultAccountId, productId);

        String apiKey = "ak_" + UUID.randomUUID().toString().replace("-", "");
        ApiAssert.statusOneOf(
                admin.post("/admin/accounts/" + defaultAccountId + "/tokens", Map.of("token", apiKey)),
                200, 201);
        System.out.println("HIER CLIENT token created");

        createPolicyWithApiKey(ctx, env, authClientId, apiKey);
        System.out.println("HIER CLIENT policy via X-Api-Key OK");

        // switch to USER — authType null = inherit tenant LOCAL_JWT
        asTntAdmin(ctx, env);
        ObjectNode toUser = (ObjectNode) getClient(admin, clientId).deepCopy();
        toUser.put("authLevel", "USER");
        toUser.putNull("authType");
        ApiAssert.success(admin.put("/admin/clients/" + clientId, toUser));
        client = getClient(admin, clientId);
        defaultAccountId = nullIfZero(client.path("defaultAccountId").asLong());
        if (defaultAccountId == null) {
            defaultAccountId = createChild(admin, clientAccountId, "Default account", "ACCOUNT");
            ObjectNode upd = (ObjectNode) client.deepCopy();
            upd.put("defaultAccountId", defaultAccountId);
            ApiAssert.success(admin.put("/admin/clients/" + clientId, upd));
        }
        grantProduct(admin, clientId, defaultAccountId, productId);

        String defUser = ("cli_def_" + clientId).toLowerCase(Locale.ROOT);
        ensureLogin(admin, defUser, defUser);
        bindLogin(admin, defaultAccountId, defUser, true);
        System.out.println("HIER CLIENT→USER default login=" + defUser);

        createPolicyWithUserLogin(ctx, env, authClientId, defUser, defUser, defaultAccountId);
        System.out.println("HIER CLIENT→USER policy via JWT OK");
    }

    /**
     * Hierarchy under default tenant client {@code sys} (authLevel=USER).
     */
    private static void runSysUserHierarchyPhase(ScenarioContext ctx, TenantEnv env, long productId)
            throws Exception {
        ApiClient admin = ctx.client();
        asTntAdmin(ctx, env);

        long clientId = env.defaultClientId();
        JsonNode client = getClient(admin, clientId);
        String authClientId = client.hasNonNull("authClientId")
                ? client.get("authClientId").asText()
                : SYS_AUTH_CLIENT_ID;
        if (!SYS_AUTH_CLIENT_ID.equalsIgnoreCase(authClientId)) {
            System.out.println("HIER WARN expected sys client, got authClientId=" + authClientId);
        }
        authClientId = authClientId.toLowerCase(Locale.ROOT);

        ObjectNode ensureUser = (ObjectNode) client.deepCopy();
        ensureUser.put("authLevel", "USER");
        ensureUser.putNull("authType");
        ApiAssert.success(admin.put("/admin/clients/" + clientId, ensureUser));
        client = getClient(admin, clientId);
        System.out.println("HIER sys clientId=" + clientId
                + " authLevel=" + client.path("authLevel").asText()
                + " authClientId=" + authClientId);

        long clientAccountId = client.path("clientAccountId").asLong();
        if (clientAccountId == 0L && env.clientAccountId() != null) {
            clientAccountId = env.clientAccountId();
        }
        if (clientAccountId == 0L) {
            throw new AssertionError("sys clientAccountId missing");
        }
        Long defaultAccountId = nullIfZero(client.path("defaultAccountId").asLong());
        if (defaultAccountId == null) {
            defaultAccountId = env.defaultAccountId();
        }
        grantProduct(admin, clientId, defaultAccountId, productId);

        ensureLogin(admin, "user_admin1", "user_admin1");
        Response mem = admin.post("/admin/clients/" + clientId + "/members",
                Map.of("role", "GROUP_ADMIN", "userLogin", "user_admin1"));
        ApiAssert.success(mem);
        System.out.println("HIER sys GROUP_ADMIN user_admin1");

        long account1 = createChild(admin, clientAccountId, "account1", "ACCOUNT");
        long account2 = createChild(admin, clientAccountId, "account2", "ACCOUNT");
        long account3 = createChild(admin, clientAccountId, "account3", "ACCOUNT");
        long group1 = createChild(admin, clientAccountId, "group1", "GROUP");
        long account11 = createChild(admin, group1, "account1.1", "ACCOUNT");
        long account21 = createChild(admin, group1, "account2.1", "ACCOUNT");
        long account31 = createChild(admin, group1, "account3.1", "ACCOUNT");

        // user_admin1 linked to every group (as GROUP_ADMIN) and every account
        linkGroupAdmin(admin, group1, "user_admin1");
        List<Long> allAccounts = List.of(
                account1, account2, account3, account11, account21, account31);
        for (long accId : allAccounts) {
            linkAccountMember(admin, accId, "user_admin1");
        }
        if (defaultAccountId != null) {
            linkAccountMember(admin, defaultAccountId, "user_admin1");
        }
        System.out.println("HIER user_admin1 linked to group1 + all accounts");

        record Acc(String label, long id) {
        }
        List<Acc> accounts = List.of(
                new Acc("1", account1),
                new Acc("2", account2),
                new Acc("3", account3),
                new Acc("1.1", account11),
                new Acc("2.1", account21),
                new Acc("3.1", account31)
        );

        List<String[]> users = new ArrayList<>();
        for (Acc a : accounts) {
            grantAccountProduct(admin, a.id(), productId);
            for (int u = 1; u <= 2; u++) {
                String login = ("user" + a.label() + "_" + u).toLowerCase(Locale.ROOT);
                ensureLogin(admin, login, login);
                bindLogin(admin, a.id(), login, true);
                users.add(new String[]{login, login, String.valueOf(a.id()), a.label()});
                System.out.println("HIER user " + login + " → account " + a.label() + " id=" + a.id());
            }
        }

        for (String[] u : users) {
            String login = u[0];
            String password = u[1];
            long accountId = Long.parseLong(u[2]);
            for (int i = 0; i < POLICIES_PER_USER; i++) {
                createPolicyWithUserLogin(ctx, env, authClientId, login, password, accountId);
            }
            asUser(ctx, env, authClientId, login, password, accountId);
            Response list = ctx.client().get("/sales");
            ApiAssert.success(list);
            JsonNode arr = MAPPER.readTree(list.asString());
            int count = arr.isArray() ? arr.size() : -1;
            System.out.println("HIER sales.count user=" + login + " account=" + u[3]
                    + " count=" + count);
            if (count < POLICIES_PER_USER) {
                throw new AssertionError("Expected at least " + POLICIES_PER_USER
                        + " policies for " + login + ", got " + count);
            }
        }
    }

    private static void asTntAdmin(ScenarioContext ctx, TenantEnv env) throws Exception {
        ApiClient c = ctx.client();
        c.clearSalesIdentity();
        c.clearApiKey();
        c.clearXClientId();
        ctx.auth().login(env.code(), SYS_AUTH_CLIENT_ID, env.userLogin(), env.userPassword());
        c.setTenantOverride(env.code());
        c.clearImpersonateTenant();
        if (env.tntAdminAccountId() != null) {
            c.setAccountId(env.tntAdminAccountId());
        } else {
            ActingAccount.selectRole(c, "TNT_ADMIN");
        }
    }

    private static void asUser(ScenarioContext ctx, TenantEnv env, String authClientId,
                               String login, String password, Long accountId) {
        ApiClient c = ctx.client();
        c.clearSalesIdentity();
        ctx.auth().login(env.code(), authClientId, login, password);
        c.setTenantOverride(env.code());
        c.clearImpersonateTenant();
        c.setXClientId(authClientId);
        if (accountId != null) {
            c.setAccountId(accountId);
        }
    }

    private static void createPolicyWithApiKey(ScenarioContext ctx, TenantEnv env,
                                               String authClientId, String apiKey) throws Exception {
        ApiClient c = ctx.client();
        c.clearSalesIdentity();
        c.setTenantOverride(env.code());
        c.setXClientId(authClientId);
        c.setApiKey(apiKey);
        postPolicy(ctx);
        asTntAdmin(ctx, env);
    }

    private static void createPolicyWithUserLogin(ScenarioContext ctx, TenantEnv env,
                                                  String authClientId, String login, String password,
                                                  Long accountId) throws Exception {
        asUser(ctx, env, authClientId, login, password, accountId);
        postPolicy(ctx);
        asTntAdmin(ctx, env);
    }

    private static void postPolicy(ScenarioContext ctx) throws Exception {
        String body = ctx.fixture("crg-policy.json");
        Response save = ctx.client().post("/sales/policies", MAPPER.readTree(body));
        ApiAssert.success(save);
        String pn = extractPolicyNumber(save);
        ApiAssert.notBlank(pn, "policyNumber");
        System.out.println("HIER   policy " + pn);
    }

    private static JsonNode getClient(ApiClient admin, long clientId) throws Exception {
        Response r = admin.get("/admin/clients/" + clientId);
        ApiAssert.success(r);
        return MAPPER.readTree(r.asString());
    }

    private static long createChild(ApiClient admin, long parentId, String name, String nodeType)
            throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name", name);
        body.put("nodeType", nodeType);
        Response r = admin.post("/admin/accounts/" + parentId + "/children", body);
        ApiAssert.statusOneOf(r, 200, 201);
        long id = MAPPER.readTree(r.asString()).path("id").asLong();
        if (id == 0L) {
            throw new AssertionError("child create without id: " + r.asString());
        }
        System.out.println("HIER   " + nodeType + " " + name + " id=" + id);
        return id;
    }

    private static void linkGroupAdmin(ApiClient admin, long groupId, String userLogin) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("role", "GROUP_ADMIN");
        body.put("userLogin", userLogin);
        Response r = admin.post("/admin/accounts/" + groupId + "/members", body);
        ApiAssert.success(r);
    }

    private static void linkAccountMember(ApiClient admin, long accountId, String userLogin) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("userLogin", userLogin);
        Response r = admin.post("/admin/accounts/" + accountId + "/members", body);
        if (r.statusCode() >= 200 && r.statusCode() < 300) {
            return;
        }
        // fallback: account logins binding
        bindLogin(admin, accountId, userLogin, false);
    }

    private static void grantProduct(ApiClient admin, long clientId, Long accountId, long productId) {
        ObjectNode grant = productGrant(productId);
        ApiAssert.statusOneOf(admin.post("/admin/clients/" + clientId + "/products", grant), 200, 201);
        if (accountId != null) {
            grantAccountProduct(admin, accountId, productId);
        }
    }

    private static void grantAccountProduct(ApiClient admin, long accountId, long productId) {
        ApiAssert.success(admin.post("/admin/accounts/" + accountId + "/products", productGrant(productId)));
    }

    private static ObjectNode productGrant(long productId) {
        ObjectNode grant = MAPPER.createObjectNode();
        grant.put("roleProductId", productId);
        grant.put("canRead", true);
        grant.put("canQuote", true);
        grant.put("canPolicy", true);
        grant.put("canAddendum", true);
        grant.put("canCancel", true);
        grant.put("canProlongate", true);
        return grant;
    }

    private static void ensureLogin(ApiClient admin, String login, String password) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("userLogin", login);
        body.put("fullName", login);
        body.put("position", "user");
        body.put("password", password);
        Response r = admin.post("/admin/logins", body);
        if (r.statusCode() >= 200 && r.statusCode() < 300) {
            return;
        }
        System.out.println("HIER   login " + login + " HTTP " + r.statusCode() + " (reuse if exists)");
    }

    private static void bindLogin(ApiClient admin, long accountId, String userLogin, boolean isDefault) {
        Map<String, Object> bind = new LinkedHashMap<>();
        bind.put("userLogin", userLogin);
        bind.put("isDefault", isDefault);
        Response r = admin.post("/admin/accounts/" + accountId + "/logins", bind);
        ApiAssert.statusOneOf(r, 200, 201);
    }

    private static Long nullIfZero(long v) {
        return v == 0L ? null : v;
    }

    private static String extractPolicyNumber(Response r) {
        try {
            JsonNode n = MAPPER.readTree(r.asString());
            if (n.hasNonNull("policyNumber")) {
                return n.get("policyNumber").asText();
            }
            if (n.hasNonNull("number")) {
                return n.get("number").asText();
            }
            JsonNode policy = n.get("policy");
            if (policy != null && policy.hasNonNull("policyNumber")) {
                return policy.get("policyNumber").asText();
            }
        } catch (Exception ignored) {
        }
        return r.jsonPath().getString("policyNumber");
    }
}
