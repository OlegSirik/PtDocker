package ru.pt.test.scenario;

import java.util.Set;

@FunctionalInterface
public interface Scenario {
    void run(ScenarioContext ctx) throws Exception;

    default String id() {
        return getClass().getSimpleName();
    }

    default String title() {
        return id();
    }

    default Set<String> tags() {
        return Set.of("regression");
    }

    static Scenario of(String id, String title, Set<String> tags, Scenario body) {
        return new Scenario() {
            @Override
            public void run(ScenarioContext ctx) throws Exception {
                body.run(ctx);
            }

            @Override
            public String id() {
                return id;
            }

            @Override
            public String title() {
                return title;
            }

            @Override
            public Set<String> tags() {
                return tags;
            }
        };
    }
}
