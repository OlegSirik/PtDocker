package ru.pt.test.scenario.packs;

import ru.pt.test.scenario.Scenario;
import ru.pt.test.support.TenantBootstrap;
import ru.pt.test.support.TenantEnv;

import java.util.List;
import java.util.Set;

/**
 * Create {@code test_<ts>} + admin user only. For full product+load use {@code --tags=e2e}.
 */
public final class AdminLifecycleScenarios {

    private AdminLifecycleScenarios() {
    }

    public static List<Scenario> all() {
        return List.of(
                Scenario.of("admin.tenant-lifecycle",
                        "SYS: create test_<ts> tenant + admin user → optional delete",
                        Set.of("admin-lifecycle"),
                        ctx -> {
                            TenantEnv env = null;
                            try {
                                env = TenantBootstrap.create(ctx);
                                System.out.println("ADMIN lifecycle OK: tenant=" + env.code());
                            } finally {
                                TenantBootstrap.deleteIfConfigured(ctx, env);
                            }
                        })
        );
    }
}
