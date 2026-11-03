package ru.pt.test.scenario.packs;

import io.restassured.response.Response;
import ru.pt.test.assertj.ApiAssert;
import ru.pt.test.scenario.Scenario;

import java.util.List;
import java.util.Set;

public final class DashboardLkScenarios {

    private DashboardLkScenarios() {
    }

    public static List<Scenario> all() {
        return List.of(
                Scenario.of("dashboard.smoke", "GET /dashboard/cards",
                        Set.of("smoke", "regression"), ctx -> {
                            ctx.auth().loginAs("admin");
                            // Root GET /dashboard is not mapped; cards/chart are.
                            Response r = ctx.client().get("/dashboard/cards");
                            ApiAssert.success(r);
                        }),
                Scenario.of("lk.products-for-quote", "GET /lk/products-for-quote",
                        Set.of("sales", "regression"), ctx -> {
                            ctx.auth().loginAs("admin");
                            Response r = ctx.client().get("/lk/products-for-quote");
                            ApiAssert.statusOneOf(r, 200, 404);
                        })
        );
    }
}
