package ru.pt.test.scenario.packs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.Response;
import ru.pt.test.assertj.ApiAssert;
import ru.pt.test.scenario.Scenario;

import java.util.List;
import java.util.Map;
import java.util.Set;

public final class SalesScenarios {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private SalesScenarios() {
    }

    public static List<Scenario> all() {
        return List.of(
                Scenario.of("sales.quote", "POST /sales/quotes",
                        Set.of("smoke", "sales", "regression"), ctx -> {
                            ctx.auth().loginAs("admin");
                            // Body: fixtures/crg-quote.json (productCode from config.defaults)
                            String body = ctx.fixture("crg-quote.json");
                            String productCode = ctx.config().getDefaults().getProductCode();
                            if (productCode != null && !productCode.isBlank()) {
                                JsonNode node = MAPPER.readTree(body);
                                if (node.isObject()) {
                                    ((com.fasterxml.jackson.databind.node.ObjectNode) node)
                                            .put("productCode", productCode);
                                    body = MAPPER.writeValueAsString(node);
                                }
                            }
                            Response r = ctx.client().post("/sales/quotes", MAPPER.readTree(body));
                            ApiAssert.success(r);
                            ctx.put("lastQuote", r.asString());
                        }),
                Scenario.of("sales.policy", "POST /sales/policies then GET by number",
                        Set.of("smoke", "sales", "regression"), ctx -> {
                            ctx.auth().loginAs("admin");
                            String body = ctx.fixture("crg-policy.json");
                            Response save = ctx.client().post("/sales/policies", MAPPER.readTree(body));
                            ApiAssert.success(save);
                            String policyNumber = extractPolicyNumber(save);
                            ApiAssert.notBlank(policyNumber, "policyNumber");
                            ctx.put("policyNumber", policyNumber);

                            Response get = ctx.client().get("/sales/" + policyNumber);
                            ApiAssert.success(get);

                            Response list = ctx.client().get("/sales");
                            ApiAssert.success(list);
                        }),
                Scenario.of("sales.quotes-list", "GET /sales/quotes",
                        Set.of("sales", "regression"), ctx -> {
                            ctx.auth().loginAs("admin");
                            Response r = ctx.client().get("/sales/quotes", Map.of("qstr", ""));
                            ApiAssert.success(r);
                        }),
                Scenario.of("sales.print", "GET print form for policy (best-effort)",
                        Set.of("sales", "regression"), ctx -> {
                            ctx.auth().loginAs("admin");
                            String pn = ctx.get("policyNumber");
                            if (pn == null || pn.isBlank()) {
                                String body = ctx.fixture("crg-policy.json");
                                Response save = ctx.client().post("/sales/policies", MAPPER.readTree(body));
                                ApiAssert.success(save);
                                pn = extractPolicyNumber(save);
                                ctx.put("policyNumber", pn);
                            }
                            String pf = ctx.config().getDefaults().getPrintFormType();
                            Response r = ctx.client().getBytes(
                                    "/sales/policies/" + pn + "/printpf/" + pf);
                            // print may 404 if no file attached — accept 2xx or 404
                            ApiAssert.statusOneOf(r, 200, 404);
                        }),
                Scenario.of("sales.paid", "POST /sales/{nr}/paid stub",
                        Set.of("sales", "regression"), ctx -> {
                            ctx.auth().loginAs("admin");
                            String pn = ctx.get("policyNumber");
                            if (pn == null) {
                                throw new AssertionError("policyNumber missing; run sales.policy first or same suite");
                            }
                            Response r = ctx.client().post("/sales/" + pn + "/paid", Map.of());
                            ApiAssert.statusOneOf(r, 200, 201, 204, 404, 501);
                        })
        );
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
