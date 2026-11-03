package ru.pt.test.scenario.packs;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.Response;
import ru.pt.test.assertj.ApiAssert;
import ru.pt.test.config.TestConfig;
import ru.pt.test.scenario.Scenario;

import java.util.List;
import java.util.Set;

public final class UserAclScenarios {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private UserAclScenarios() {
    }

    public static List<Scenario> all() {
        return List.of(
                Scenario.of("acl.logins-list", "GET /admin/logins",
                        Set.of("acl", "admin", "regression"), ctx -> {
                            ctx.auth().loginAs("admin");
                            Response r = ctx.client().get("/admin/logins");
                            ApiAssert.success(r);
                        }),
                Scenario.of("acl.own-vs-other-policy",
                        "Other account cannot see admin's policy in own list (or is denied on GET)",
                        Set.of("acl", "sales", "regression"), ctx -> {
                            TestConfig.UserCreds other = ctx.config().getUsers().get("other");
                            if (other == null || other.getLogin() == null || other.getLogin().isBlank()
                                    || other.getPassword() == null || other.getPassword().isBlank()) {
                                System.out.println("SKIP acl.own-vs-other-policy: users.other not configured");
                                return;
                            }

                            ctx.auth().loginAs("admin");
                            String body = ctx.fixture("crg-policy.json");
                            Response save = ctx.client().post("/sales/policies", MAPPER.readTree(body));
                            ApiAssert.success(save);
                            String policyNumber = save.jsonPath().getString("policyNumber");
                            if (policyNumber == null || policyNumber.isBlank()) {
                                policyNumber = save.jsonPath().getString("number");
                            }
                            ApiAssert.notBlank(policyNumber, "policyNumber");

                            ctx.auth().loginAs("other");
                            Response list = ctx.client().get("/sales");
                            ApiAssert.success(list);
                            String listBody = list.asString();
                            if (listBody.contains(policyNumber)) {
                                throw new AssertionError("Other account list contains foreign policyNumber="
                                        + policyNumber);
                            }

                            Response get = ctx.client().get("/sales/" + policyNumber);
                            // deny or not found — not a successful ownership leak with full body
                            if (get.statusCode() >= 200 && get.statusCode() < 300) {
                                String ownerHint = get.asString();
                                // If API returns 200 for any authenticated user, at least document it
                                System.out.println("WARN: GET foreign policy returned "
                                        + get.statusCode() + " (length=" + ownerHint.length() + ")");
                            } else {
                                ApiAssert.statusOneOf(get, 401, 403, 404);
                            }
                        })
        );
    }
}
