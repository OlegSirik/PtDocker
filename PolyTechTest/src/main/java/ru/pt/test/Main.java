package ru.pt.test;

import ru.pt.test.auth.AuthSession;
import ru.pt.test.client.ApiClient;
import ru.pt.test.config.TestConfig;
import ru.pt.test.report.ReportWriter;
import ru.pt.test.scenario.Scenario;
import ru.pt.test.scenario.ScenarioContext;
import ru.pt.test.scenario.ScenarioRegistry;
import ru.pt.test.scenario.ScenarioResult;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class Main {

    public static void main(String[] args) throws Exception {
        Path configPath = null;
        String reportDirOverride = null;
        List<String> tagsOverride = null;
        Boolean failFastOverride = null;

        for (int i = 0; i < args.length; i++) {
            String a = args[i];
            if ("--config".equals(a) && i + 1 < args.length) {
                configPath = Path.of(args[++i]);
            } else if (a.startsWith("--config=")) {
                configPath = Path.of(a.substring("--config=".length()));
            } else if ("--report-dir".equals(a) && i + 1 < args.length) {
                reportDirOverride = args[++i];
            } else if (a.startsWith("--report-dir=")) {
                reportDirOverride = a.substring("--report-dir=".length());
            } else if ("--tags".equals(a) && i + 1 < args.length) {
                tagsOverride = splitTags(args[++i]);
            } else if (a.startsWith("--tags=")) {
                tagsOverride = splitTags(a.substring("--tags=".length()));
            } else if ("--fail-fast".equals(a)) {
                failFastOverride = true;
            } else if ("--help".equals(a) || "-h".equals(a)) {
                printHelp();
                return;
            }
        }

        TestConfig config = TestConfig.load(configPath);
        if (reportDirOverride != null) {
            config.setReportDir(reportDirOverride);
        }
        if (tagsOverride != null) {
            config.setTags(tagsOverride);
        }
        if (failFastOverride != null) {
            config.setFailFast(failFastOverride);
        }

        System.out.println("PolyTechTest → " + config.apiRoot());
        System.out.println("Tags: " + config.getTags());

        ApiClient client = new ApiClient(config);
        AuthSession auth = new AuthSession(client);
        ScenarioContext ctx = new ScenarioContext(config, client, auth);

        List<Scenario> scenarios = ScenarioRegistry.select(config.getTags());
        if (scenarios.isEmpty()) {
            System.err.println("No scenarios matched tags: " + config.getTags());
            System.exit(2);
            return;
        }

        List<ScenarioResult> results = new ArrayList<>();
        boolean failed = false;
        for (Scenario s : scenarios) {
            long t0 = System.currentTimeMillis();
            Integer http = null;
            try {
                System.out.print("RUN  " + s.id() + " … ");
                s.run(ctx);
                long ms = System.currentTimeMillis() - t0;
                results.add(new ScenarioResult(s.id(), s.title(), true, ms, http, null));
                System.out.println("PASS (" + ms + " ms)");
            } catch (Throwable e) {
                long ms = System.currentTimeMillis() - t0;
                String msg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
                results.add(new ScenarioResult(s.id(), s.title(), false, ms, http, msg));
                System.out.println("FAIL (" + ms + " ms): " + msg);
                failed = true;
                if (config.isFailFast()) {
                    break;
                }
            }
        }

        Path report = new ReportWriter(Path.of(config.getReportDir())).write(results);
        long pass = results.stream().filter(ScenarioResult::passed).count();
        System.out.println();
        System.out.println("Done: " + pass + "/" + results.size() + " passed");
        System.out.println("Report: " + report.toAbsolutePath());

        System.exit(failed ? 1 : 0);
    }

    private static List<String> splitTags(String raw) {
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    private static void printHelp() {
        System.out.println("""
                PolyTechTest — REST regression for PoliTechAPI

                Usage:
                  java -jar polytech-test.jar [--config=path] [--tags=smoke,sales] [--report-dir=reports] [--fail-fast]

                Env:
                  PT_DEMO_PASSWORD   password for users.admin
                  PT_OTHER_LOGIN / PT_OTHER_PASSWORD   optional second account for ACL tests
                  PT_SYS_PASSWORD    password for users.sys (sys_admin) — admin-lifecycle

                Tags (examples):
                  --tags=regression
                  --tags=e2e
                  --tags=e2e-hierarchy
                  --tags=load
                  --tags=admin-lifecycle
                """);
    }
}
