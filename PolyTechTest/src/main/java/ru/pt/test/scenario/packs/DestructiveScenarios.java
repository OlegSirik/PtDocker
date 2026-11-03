package ru.pt.test.scenario.packs;

import ru.pt.test.scenario.Scenario;

import java.util.List;
import java.util.Set;

/**
 * Opt-in destructive admin mutations. Enabled only with tag {@code destructive}.
 */
public final class DestructiveScenarios {

    private DestructiveScenarios() {
    }

    public static List<Scenario> all() {
        return List.of(
                Scenario.of("destructive.placeholder",
                        "Placeholder: create tenant / wipe schema — enable explicitly",
                        Set.of("destructive"), ctx -> {
                            System.out.println("destructive.placeholder: no-op (wire real mutations carefully)");
                        })
        );
    }
}
