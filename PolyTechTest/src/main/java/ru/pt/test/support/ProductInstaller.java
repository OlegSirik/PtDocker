package ru.pt.test.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.restassured.response.Response;
import ru.pt.test.assertj.ApiAssert;
import ru.pt.test.client.ApiClient;
import ru.pt.test.scenario.ScenarioContext;

import java.util.Map;

/**
 * Install CRG / CARGO_GENERAL into current tenant from fixtures (bootstrap copies).
 */
public final class ProductInstaller {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private ProductInstaller() {
    }

    public static long installCargoGeneral(ScenarioContext ctx, TenantEnv env) throws Exception {
        ApiClient client = ctx.client();
        // install + grants need TNT_ADMIN (ACCOUNT_PRODUCT MANAGE); sales use PRODUCT_ADMIN later
        client.setTenantOverride(env.code());
        client.clearImpersonateTenant();
        if (env.tntAdminAccountId() != null) {
            client.setAccountId(env.tntAdminAccountId());
        } else {
            ActingAccount.selectRole(client, "TNT_ADMIN");
        }
        System.out.println("E2E install under TNT_ADMIN X-Account-Id=" + client.getAccountId());

        ObjectNode lob = (ObjectNode) MAPPER.readTree(ctx.fixture("business-line_CRG.json"));
        lob.remove("id");
        stripIds(lob.get("mpVars"));
        Response lobR = client.post("/admin/lobs", lob);
        ApiAssert.success(lobR);
        System.out.println("E2E LOB CRG created");

        ObjectNode company = MAPPER.createObjectNode();
        company.put("code", "TEST_IC");
        company.put("name", "Test Insurance Co");
        company.put("status", "active");
        company.put("shortName", "TestIC");
        company.put("fullName", "Test Insurance Company");
        company.put("contractText", "Test Insurance Company");
        Response icR = client.post("/admin/insCompanies", company);
        ApiAssert.success(icR);
        long insCompanyId = MAPPER.readTree(icR.asString()).path("id").asLong();
        if (insCompanyId == 0L) {
            throw new AssertionError("insCompany create without id: " + icR.asString());
        }
        System.out.println("E2E insCompany id=" + insCompanyId);

        ObjectNode product = (ObjectNode) MAPPER.readTree(ctx.fixture("product_CARGO_GENERAL.json"));
        product.remove("id");
        // DEV first — only DEV versions are updatable (need insCompanyId)
        product.put("versionStatus", "DEV");
        product.put("insCompanyId", insCompanyId);
        if (!product.has("versionNo")) {
            product.put("versionNo", 1);
        }
        stripIds(product.get("vars"));
        Response prodR = client.post("/admin/products", product);
        ApiAssert.success(prodR);
        JsonNode created = MAPPER.readTree(prodR.asString());
        long productId = created.path("id").asLong();
        if (productId == 0L) {
            productId = created.path("productId").asLong();
        }
        if (productId == 0L) {
            throw new AssertionError("product create without id: " + prodR.asString());
        }
        long versionNo = created.has("versionNo") ? created.path("versionNo").asLong() : 1L;
        System.out.println("E2E product CARGO_GENERAL id=" + productId);

        ObjectNode update = (ObjectNode) created.deepCopy();
        update.put("insCompanyId", insCompanyId);
        update.put("id", productId);
        update.put("versionStatus", "DEV");
        Response updR = client.put("/admin/products/" + productId + "/versions/" + versionNo, update);
        ApiAssert.success(updR);

        String pkg = "0";
        if (created.has("packages") && created.get("packages").isArray() && !created.get("packages").isEmpty()) {
            JsonNode p0 = created.get("packages").get(0);
            if (p0.hasNonNull("code")) {
                pkg = p0.get("code").asText();
            }
        }

        installCalculator(client, ctx, productId, versionNo, pkg);
        System.out.println("E2E calculator on DEV v" + versionNo + " package " + pkg);

        // PROD for ACCOUNT / GROUP_ADMIN / API-key (dataScope=PROD)
        Response pub = client.post(
                "/admin/products/" + productId + "/versions/" + versionNo + "/cmd/publish", null);
        ApiAssert.success(pub);
        System.out.println("E2E product published → PROD v" + versionNo);

        // New DEV for PRODUCT_ADMIN (dataScope=DEV); calculators copied from PROD
        Response newDev = client.post(
                "/admin/products/" + productId + "/versions/" + versionNo + "/cmd/create", null);
        ApiAssert.success(newDev);
        long devVersionNo = MAPPER.readTree(newDev.asString()).path("versionNo").asLong();
        if (devVersionNo == 0L) {
            devVersionNo = versionNo + 1;
        }
        System.out.println("E2E product DEV v" + devVersionNo + " + PROD v" + versionNo
                + " (PRODUCT_ADMIN→DEV, ACCOUNT→PROD)");

        ObjectNode grant = MAPPER.createObjectNode();
        grant.put("roleProductId", productId);
        grant.put("canRead", true);
        grant.put("canQuote", true);
        grant.put("canPolicy", true);
        grant.put("canAddendum", true);
        grant.put("canCancel", true);
        grant.put("canProlongate", true);
        ApiAssert.statusOneOf(
                client.post("/admin/clients/" + env.defaultClientId() + "/products", grant),
                200, 201);
        if (env.defaultAccountId() != null) {
            ApiAssert.success(client.post("/admin/accounts/" + env.defaultAccountId() + "/products", grant));
        }
        System.out.println("E2E product grants applied");

        ctx.put("e2e.productId", productId);
        ctx.put("e2e.prodVersionNo", versionNo);
        ctx.put("e2e.devVersionNo", devVersionNo);
        return productId;
    }

    private static void installCalculator(ApiClient client, ScenarioContext ctx,
                                          long productId, long versionNo, String pkg) throws Exception {
        Response calcCreate = client.post(
                "/admin/calculators/products/" + productId + "/versions/" + versionNo + "/packages/" + pkg,
                Map.of());
        ApiAssert.statusOneOf(calcCreate, 200, 201);

        ObjectNode calc = (ObjectNode) MAPPER.readTree(ctx.fixture("calculator__1_0.json"));
        calc.remove("id");
        calc.put("productId", productId);
        calc.put("productCode", "CARGO_GENERAL");
        calc.put("versionNo", versionNo);
        calc.put("packageNo", pkg);
        Response calcPut = client.put(
                "/admin/calculators/products/" + productId + "/versions/" + versionNo + "/packages/" + pkg,
                calc);
        ApiAssert.success(calcPut);
    }

    private static void stripIds(JsonNode arr) {
        if (arr == null || !arr.isArray()) {
            return;
        }
        for (JsonNode n : arr) {
            if (n instanceof ObjectNode o) {
                o.remove("id");
                o.putNull("parent_id");
            }
        }
    }
}
