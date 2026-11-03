package ru.pt.test.scenario.packs;

import io.restassured.response.Response;
import ru.pt.test.assertj.ApiAssert;
import ru.pt.test.scenario.Scenario;

import java.util.List;
import java.util.Set;

public final class SchemaRefdictScenarios {

    private SchemaRefdictScenarios() {
    }

    public static List<Scenario> all() {
        return List.of(
                Scenario.of("schema.models", "GET /admin/schema/models",
                        Set.of("admin", "regression"), ctx -> {
                            ctx.auth().loginAs("admin");
                            Response r = ctx.client().get("/admin/schema/models");
                            ApiAssert.success(r);
                        }),
                Scenario.of("schema.attributes", "GET INSURANCE_CONTRACT attributes + metadata",
                        Set.of("admin", "regression"), ctx -> {
                            ctx.auth().loginAs("admin");
                            Response attrs = ctx.client().get("/admin/schema/INSURANCE_CONTRACT/attributes");
                            ApiAssert.success(attrs);
                            Response meta = ctx.client().get(
                                    "/admin/schema/INSURANCE_CONTRACT/attributes/metadata");
                            ApiAssert.success(meta);
                        }),
                Scenario.of("schema.export", "GET schema export (read-only)",
                        Set.of("admin", "regression"), ctx -> {
                            ctx.auth().loginAs("admin");
                            Response r = ctx.client().get("/admin/schema/INSURANCE_CONTRACT/export");
                            ApiAssert.statusOneOf(r, 200, 404);
                        }),
                Scenario.of("refdicts.list", "GET /admin/refdicts",
                        Set.of("admin", "regression"), ctx -> {
                            ctx.auth().loginAs("admin");
                            Response r = ctx.client().get("/admin/refdicts");
                            ApiAssert.success(r);
                        }),
                Scenario.of("refdicts.currency", "GET /admin/refdicts/currencyCode/items (if present)",
                        Set.of("admin", "regression"), ctx -> {
                            ctx.auth().loginAs("admin");
                            Response r = ctx.client().get("/admin/refdicts/currencyCode/items");
                            ApiAssert.statusOneOf(r, 200, 404);
                        }),
                Scenario.of("rules.list", "GET /admin/rules",
                        Set.of("admin", "regression"), ctx -> {
                            ctx.auth().loginAs("admin");
                            Response r = ctx.client().get("/admin/rules");
                            ApiAssert.success(r);
                        })
        );
    }
}
