package ru.pt.test.scenario.packs;

import io.restassured.response.Response;
import ru.pt.test.assertj.ApiAssert;
import ru.pt.test.scenario.Scenario;

import java.util.List;
import java.util.Set;

public final class FilesPaymentsScenarios {

    private FilesPaymentsScenarios() {
    }

    public static List<Scenario> all() {
        return List.of(
                Scenario.of("files.get-missing", "GET /admin/files/{id} for missing id → 404",
                        Set.of("admin", "regression"), ctx -> {
                            ctx.auth().loginAs("admin");
                            // Collection GET is not supported; only GET /{fileId}
                            Response r = ctx.client().get("/admin/files/0");
                            ApiAssert.statusOneOf(r, 404, 400);
                        }),
                Scenario.of("payments.installments", "GET /payments/installments?policyId=",
                        Set.of("admin", "regression"), ctx -> {
                            ctx.auth().loginAs("admin");
                            // policyId is required; unknown id → empty/404/400, not 500
                            Response r = ctx.client().get("/payments/installments",
                                    java.util.Map.of("policyId", 0L));
                            ApiAssert.statusOneOf(r, 200, 400, 404);
                        }),
                Scenario.of("policy-forms.list", "GET /admin/policy-forms",
                        Set.of("admin", "regression"), ctx -> {
                            ctx.auth().loginAs("admin");
                            Response r = ctx.client().get("/admin/policy-forms");
                            ApiAssert.statusOneOf(r, 200, 404);
                        }),
                Scenario.of("commissions.list", "GET /admin/commissions/configurations?accountId=",
                        Set.of("admin", "regression"), ctx -> {
                            ctx.auth().loginAs("admin");
                            Response ctxR = ctx.client().get("/auth/context");
                            ApiAssert.success(ctxR);
                            Long accountId = ctxR.jsonPath().getLong("accountId");
                            if (accountId == null || accountId == 0L) {
                                accountId = ctxR.jsonPath().getLong("actingAccountId");
                            }
                            if (accountId == null || accountId == 0L) {
                                throw new AssertionError("accountId missing in /auth/context");
                            }
                            Response r = ctx.client().get("/admin/commissions/configurations",
                                    java.util.Map.of("accountId", accountId));
                            ApiAssert.success(r);
                        })
        );
    }
}
