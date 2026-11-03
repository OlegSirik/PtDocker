package ru.pt.test.scenario.packs;

import io.restassured.response.Response;
import ru.pt.test.assertj.ApiAssert;
import ru.pt.test.scenario.Scenario;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class AuthScenarios {

    private AuthScenarios() {
    }

    public static List<Scenario> all() {
        return List.of(
                Scenario.of("auth.login", "POST /auth/login returns accessToken",
                        Set.of("smoke", "auth", "regression"), ctx -> {
                            ctx.auth().loginAs("admin");
                            ApiAssert.notBlank(ctx.client().getBearerToken(), "accessToken");
                        }),
                Scenario.of("auth.me", "GET /auth/me with Bearer",
                        Set.of("smoke", "auth", "regression"), ctx -> {
                            ctx.auth().loginAs("admin");
                            Response r = ctx.client().get("/auth/me");
                            ApiAssert.success(r);
                        }),
                Scenario.of("auth.context", "GET /auth/context with Bearer",
                        Set.of("auth", "regression"), ctx -> {
                            ctx.auth().loginAs("admin");
                            Response r = ctx.client().get("/auth/context");
                            ApiAssert.success(r);
                        }),
                Scenario.of("auth.unauthorized", "GET /auth/me without token → 401",
                        Set.of("auth", "regression"), ctx -> {
                            ctx.client().setBearerToken(null);
                            Response r = ctx.client().get("/auth/me");
                            ApiAssert.statusOneOf(r, 401, 403);
                        }),
                Scenario.of("auth.bad-password", "POST /auth/login bad password fails",
                        Set.of("auth", "regression"), ctx -> {
                            Map<String, Object> body = new LinkedHashMap<>();
                            body.put("userLogin", ctx.config().requireUser("admin").getLogin());
                            body.put("password", "__definitely_wrong__");
                            body.put("clientId", ctx.config().getClientId());
                            Response r = ctx.client().post("/auth/login", body);
                            if (r.statusCode() >= 200 && r.statusCode() < 300
                                    && r.jsonPath().getString("accessToken") != null
                                    && !r.jsonPath().getString("accessToken").isBlank()) {
                                throw new AssertionError("Expected login failure, got token");
                            }
                        })
        );
    }
}
