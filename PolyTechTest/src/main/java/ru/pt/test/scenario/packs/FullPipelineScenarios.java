package ru.pt.test.scenario.packs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.restassured.response.Response;
import ru.pt.test.assertj.ApiAssert;
import ru.pt.test.client.ApiClient;
import ru.pt.test.config.TestConfig;
import ru.pt.test.scenario.Scenario;
import ru.pt.test.scenario.ScenarioContext;
import ru.pt.test.support.ActingAccount;
import ru.pt.test.support.LoadRunner;
import ru.pt.test.support.ProductInstaller;
import ru.pt.test.support.TenantBootstrap;
import ru.pt.test.support.TenantEnv;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * One pipeline: create tenant+user → install product → product/sales smoke → load N requests.
 * Run: {@code --tags=e2e}
 */
public final class FullPipelineScenarios {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private FullPipelineScenarios() {
    }

    public static List<Scenario> all() {
        return List.of(
                Scenario.of("e2e.full-pipeline",
                        "Tenant+user → product suite → load (1000 by default)",
                        Set.of("e2e"),
                        FullPipelineScenarios::run)
        );
    }

    private static void run(ScenarioContext ctx) throws Exception {
        TenantEnv env = null;
        try {
            System.out.println("E2E step 1/4: create tenant + admin user");
            env = TenantBootstrap.create(ctx);

            System.out.println("E2E step 2/4: install CARGO_GENERAL");
            long productId = ProductInstaller.installCargoGeneral(ctx, env);

            // re-login so JWT picks up product roles; sales/load as PRODUCT_ADMIN
            ctx.auth().login(env.code(), "sys", env.userLogin(), env.userPassword());
            ctx.client().setTenantOverride(env.code());
            ctx.client().clearImpersonateTenant();
            long productAdminAccountId = env.productAdminAccountId() != null
                    ? env.productAdminAccountId()
                    : ActingAccount.selectRole(ctx.client(), "PRODUCT_ADMIN");
            ctx.client().setAccountId(productAdminAccountId);
            System.out.println("AUTH  X-Account-Id=" + productAdminAccountId + " role=PRODUCT_ADMIN");
            env = new TenantEnv(
                    env.code(), env.userLogin(), env.userPassword(),
                    env.defaultClientId(), env.clientAccountId(), env.defaultAccountId(),
                    env.tntAdminAccountId(), productAdminAccountId);

            System.out.println("E2E step 3/4: product + sales suite (PRODUCT_ADMIN)");
            runProductSuite(ctx, productId);

            int total = resolveTotalRequests(ctx.config());
            int threads = Math.max(1, ctx.config().getLoad().getThreads());
            System.out.println("E2E step 4/4: load " + total + " requests, threads=" + threads);
            LoadRunner.runPolicyPrint(ctx, total, threads);

            System.out.println("E2E full pipeline OK on tenant " + env.code());
        } finally {
            TenantBootstrap.deleteIfConfigured(ctx, env);
        }
    }

    private static void runProductSuite(ScenarioContext ctx, long productId) throws Exception {
        ApiClient client = ctx.client();
        long ver = ctx.config().getDefaults().getVersionNo();

        step("admin.lobs", () -> ApiAssert.success(client.get("/admin/lobs")));
        step("admin.products", () -> ApiAssert.success(client.get("/admin/products")));

        Response verR = client.get("/admin/products/" + productId + "/versions/" + ver);
        ApiAssert.success(verR);
        step("admin.example_quote", () ->
                ApiAssert.statusOneOf(
                        client.get("/admin/products/" + productId + "/versions/" + ver + "/example_quote"),
                        200, 404));
        step("admin.example_save", () ->
                ApiAssert.statusOneOf(
                        client.get("/admin/products/" + productId + "/versions/" + ver + "/example_save"),
                        200, 404));

        String quoteBody = ctx.fixture("crg-quote.json");
        ObjectNode quote = (ObjectNode) MAPPER.readTree(quoteBody);
        quote.put("productCode", ctx.config().getDefaults().getProductCode());
        Response quoteR = client.post("/sales/quotes", quote);
        ApiAssert.success(quoteR);
        System.out.println("E2E   sales.quote OK");

        String policyBody = ctx.fixture("crg-policy.json");
        Response save = client.post("/sales/policies", MAPPER.readTree(policyBody));
        ApiAssert.success(save);
        String pn = extractPolicyNumber(save);
        ApiAssert.notBlank(pn, "policyNumber");
        ctx.put("policyNumber", pn);
        System.out.println("E2E   sales.policy OK " + pn);

        softOk("sales.get", client.get("/sales/" + pn));
        softOk("sales.list", client.get("/sales"));
        softOk("sales.quotes-list", client.get("/sales/quotes", Map.of("qstr", "")));

        String pf = ctx.config().getDefaults().getPrintFormType();
        Response print = client.getBytes("/sales/policies/" + pn + "/printpf/" + pf);
        softOk("sales.print", print);
    }

    private static void softOk(String name, Response r) {
        if (r.statusCode() >= 200 && r.statusCode() < 300) {
            System.out.println("E2E   " + name + " OK");
        } else {
            System.out.println("E2E   WARN " + name + " HTTP " + r.statusCode() + " (continue)");
        }
    }

    private static void step(String name, Step s) throws Exception {
        s.run();
        System.out.println("E2E   " + name + " OK");
    }

    @FunctionalInterface
    private interface Step {
        void run() throws Exception;
    }

    private static int resolveTotalRequests(TestConfig config) {
        TestConfig.Load load = config.getLoad();
        if (load.getTotalRequests() > 0) {
            return load.getTotalRequests();
        }
        return Math.max(1, load.getThreads()) * Math.max(1, load.getIterationsPerThread());
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
