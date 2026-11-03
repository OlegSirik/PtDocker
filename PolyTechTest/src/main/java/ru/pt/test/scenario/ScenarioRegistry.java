package ru.pt.test.scenario;

import ru.pt.test.scenario.packs.AdminLifecycleScenarios;
import ru.pt.test.scenario.packs.AuthScenarios;
import ru.pt.test.scenario.packs.DashboardLkScenarios;
import ru.pt.test.scenario.packs.DestructiveScenarios;
import ru.pt.test.scenario.packs.FilesPaymentsScenarios;
import ru.pt.test.scenario.packs.FullPipelineScenarios;
import ru.pt.test.scenario.packs.HierarchyE2eScenarios;
import ru.pt.test.scenario.packs.LoadTestScenarios;
import ru.pt.test.scenario.packs.ProductAdminScenarios;
import ru.pt.test.scenario.packs.SalesScenarios;
import ru.pt.test.scenario.packs.SchemaRefdictScenarios;
import ru.pt.test.scenario.packs.UserAclScenarios;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public final class ScenarioRegistry {

    private ScenarioRegistry() {
    }

    public static List<Scenario> all() {
        List<Scenario> list = new ArrayList<>();
        list.addAll(AuthScenarios.all());
        list.addAll(SalesScenarios.all());
        list.addAll(UserAclScenarios.all());
        list.addAll(ProductAdminScenarios.all());
        list.addAll(SchemaRefdictScenarios.all());
        list.addAll(FilesPaymentsScenarios.all());
        list.addAll(DashboardLkScenarios.all());
        list.addAll(LoadTestScenarios.all());
        list.addAll(AdminLifecycleScenarios.all());
        list.addAll(FullPipelineScenarios.all());
        list.addAll(HierarchyE2eScenarios.all());
        list.addAll(DestructiveScenarios.all());
        return list;
    }

    public static List<Scenario> select(List<String> tags) {
        Set<String> wanted = tags == null || tags.isEmpty()
                ? Set.of("regression")
                : tags.stream().map(t -> t.toLowerCase(Locale.ROOT)).collect(Collectors.toCollection(LinkedHashSet::new));
        boolean includeDestructive = wanted.contains("destructive");
        return all().stream()
                .filter(s -> {
                    Set<String> st = s.tags().stream()
                            .map(t -> t.toLowerCase(Locale.ROOT))
                            .collect(Collectors.toSet());
                    if (st.contains("destructive") && !includeDestructive) {
                        return false;
                    }
                    if (wanted.contains("all")) {
                        // load / admin-lifecycle / e2e* are opt-in only
                        if (st.contains("load") || st.contains("admin-lifecycle")
                                || st.contains("e2e") || st.contains("e2e-hierarchy")) {
                            return false;
                        }
                        return !st.contains("destructive") || includeDestructive;
                    }
                    for (String t : st) {
                        if (wanted.contains(t)) {
                            return true;
                        }
                    }
                    return false;
                })
                .toList();
    }
}
