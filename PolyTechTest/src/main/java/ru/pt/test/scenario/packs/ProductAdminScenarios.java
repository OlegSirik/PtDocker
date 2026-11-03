package ru.pt.test.scenario.packs;

import io.restassured.response.Response;
import ru.pt.test.assertj.ApiAssert;
import ru.pt.test.scenario.Scenario;

import java.util.List;
import java.util.Set;

public final class ProductAdminScenarios {

    private ProductAdminScenarios() {
    }

    public static List<Scenario> all() {
        return List.of(
                Scenario.of("admin.lobs-list", "GET /admin/lobs",
                        Set.of("admin", "regression"), ctx -> {
                            ctx.auth().loginAs("admin");
                            Response r = ctx.client().get("/admin/lobs");
                            ApiAssert.success(r);
                        }),
                Scenario.of("admin.products-list", "GET /admin/products",
                        Set.of("smoke", "admin", "regression"), ctx -> {
                            ctx.auth().loginAs("admin");
                            Response r = ctx.client().get("/admin/products");
                            ApiAssert.success(r);
                        }),
                Scenario.of("admin.product-version", "GET product version + example_quote/save",
                        Set.of("admin", "regression"), ctx -> {
                            ctx.auth().loginAs("admin");
                            Response list = ctx.client().get("/admin/products");
                            ApiAssert.success(list);
                            // Try configured product via code search in list, else first id
                            Long productId = list.jsonPath().getLong("[0].id");
                            if (productId == null || productId == 0L) {
                                productId = list.jsonPath().getLong("content[0].id");
                            }
                            if (productId == null || productId == 0L) {
                                throw new AssertionError("No products in tenant");
                            }
                            long ver = ctx.config().getDefaults().getVersionNo();
                            Response verR = ctx.client().get("/admin/products/" + productId + "/versions/" + ver);
                            ApiAssert.statusOneOf(verR, 200, 404);
                            if (verR.statusCode() == 200) {
                                Response eq = ctx.client().get(
                                        "/admin/products/" + productId + "/versions/" + ver + "/example_quote");
                                ApiAssert.statusOneOf(eq, 200, 404);
                                Response es = ctx.client().get(
                                        "/admin/products/" + productId + "/versions/" + ver + "/example_save");
                                ApiAssert.statusOneOf(es, 200, 404);
                            }
                            ctx.put("productId", productId);
                        }),
                Scenario.of("admin.ins-companies", "GET /admin/insCompanies",
                        Set.of("admin", "regression"), ctx -> {
                            ctx.auth().loginAs("admin");
                            Response r = ctx.client().get("/admin/insCompanies");
                            ApiAssert.success(r);
                        })
        );
    }
}
