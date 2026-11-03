package ru.pt.test.scenario.packs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.Response;
import ru.pt.test.client.ApiClient;
import ru.pt.test.config.TestConfig;
import ru.pt.test.scenario.Scenario;
import ru.pt.test.scenario.ScenarioContext;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Load pack: create policy + fetch print form under concurrency.
 * Not part of default {@code regression}; run with {@code --tags=load}.
 */
public final class LoadTestScenarios {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private LoadTestScenarios() {
    }

    public static List<Scenario> all() {
        return List.of(
                Scenario.of("load.policy-print",
                        "Parallel POST /sales/policies + GET print; avg latency",
                        Set.of("load"),
                        LoadTestScenarios::steadyLoad),
                Scenario.of("load.max-parallel",
                        "Probe concurrency levels; report max error-free parallel requests",
                        Set.of("load"),
                        LoadTestScenarios::findMaxParallel)
        );
    }

    private static void steadyLoad(ScenarioContext ctx) throws Exception {
        ctx.auth().loginAs("admin");
        TestConfig.Load load = ctx.config().getLoad();
        int threads = Math.max(1, load.getThreads());
        int total = load.getTotalRequests() > 0
                ? load.getTotalRequests()
                : threads * Math.max(1, load.getIterationsPerThread());
        ru.pt.test.support.LoadRunner.runPolicyPrint(ctx, total, threads);
    }

    private static void findMaxParallel(ScenarioContext ctx) throws Exception {
        ctx.auth().loginAs("admin");
        TestConfig.Load load = ctx.config().getLoad();
        List<Integer> levels = load.getConcurrencyProbe();
        if (levels == null || levels.isEmpty()) {
            levels = List.of(1, 2, 4, 8);
        }
        String token = ctx.client().getBearerToken();
        String body = policyBody(ctx);

        int maxOk = 0;
        StringBuilder detail = new StringBuilder();
        for (int level : levels) {
            if (level < 1) continue;
            AtomicInteger errors = new AtomicInteger();
            List<Long> latencies = new ArrayList<>();
            ExecutorService pool = Executors.newFixedThreadPool(level);
            try {
                List<Callable<Void>> tasks = new ArrayList<>();
                for (int i = 0; i < level; i++) {
                    tasks.add(() -> {
                        ApiClient worker = newWorker(ctx.config(), token);
                        long t0 = System.nanoTime();
                        try {
                            runOnce(worker, body, ctx.config());
                            synchronized (latencies) {
                                latencies.add((System.nanoTime() - t0) / 1_000_000L);
                            }
                        } catch (Exception e) {
                            errors.incrementAndGet();
                            synchronized (latencies) {
                                latencies.add((System.nanoTime() - t0) / 1_000_000L);
                            }
                        }
                        return null;
                    });
                }
                for (Future<Void> f : pool.invokeAll(tasks)) {
                    f.get();
                }
            } finally {
                pool.shutdown();
                pool.awaitTermination(5, TimeUnit.MINUTES);
            }
            double avg = latencies.stream().mapToLong(Long::longValue).average().orElse(0);
            String line = String.format(Locale.ROOT,
                    "parallel=%d errors=%d avgMs=%.1f", level, errors.get(), avg);
            detail.append(line).append('\n');
            System.out.println("LOAD  probe " + line);
            if (errors.get() == 0) {
                maxOk = level;
            } else {
                break;
            }
        }

        String summary = "maxParallelWithoutErrors=" + maxOk + "\n" + detail;
        writeLoadReport(ctx, "max-parallel", summary, List.of(), maxOk, 0);
        ctx.put("load.maxParallel", maxOk);
        System.out.println("LOAD  max parallel without errors: " + maxOk);

        if (maxOk < 1) {
            throw new AssertionError("Even single-threaded policy+print failed:\n" + detail);
        }
    }

    private static void runOnce(ApiClient worker, String policyJson, TestConfig config) throws Exception {
        Response save = worker.post("/sales/policies", MAPPER.readTree(policyJson));
        if (save.statusCode() < 200 || save.statusCode() >= 300) {
            throw new AssertionError("policy create HTTP " + save.statusCode() + " " + truncate(save.asString()));
        }
        String pn = extractPolicyNumber(save);
        if (pn == null || pn.isBlank()) {
            throw new AssertionError("policyNumber missing in create response");
        }
        String pf = config.getDefaults().getPrintFormType();
        Response print = worker.getBytes("/sales/policies/" + pn + "/printpf/" + pf);
        int sc = print.statusCode();
        if (sc >= 200 && sc < 300) {
            return;
        }
        if (!config.getLoad().isRequirePrint() && sc == 404) {
            return;
        }
        throw new AssertionError("print HTTP " + sc + " for " + pn);
    }

    private static ApiClient newWorker(TestConfig config, String token) {
        ApiClient c = new ApiClient(config);
        c.setBearerToken(token);
        return c;
    }

    private static String policyBody(ScenarioContext ctx) {
        return ctx.fixture("crg-policy.json");
    }

    private static long percentile(List<Long> values, int p) {
        if (values == null || values.isEmpty()) {
            return 0;
        }
        List<Long> sorted = new ArrayList<>(values);
        sorted.sort(Long::compareTo);
        int idx = (int) Math.ceil(p / 100.0 * sorted.size()) - 1;
        if (idx < 0) idx = 0;
        if (idx >= sorted.size()) idx = sorted.size() - 1;
        return sorted.get(idx);
    }

    private static void writeLoadReport(ScenarioContext ctx, String name, String summary,
                                       List<Long> latencies, int ok, int errors) throws Exception {
        Path dir = Path.of(ctx.config().getReportDir());
        Files.createDirectories(dir);
        Path file = dir.resolve("load-" + name + ".md");
        StringBuilder sb = new StringBuilder();
        sb.append("# Load: ").append(name).append("\n\n");
        sb.append("```\n").append(summary).append("\n```\n");
        if (latencies != null && !latencies.isEmpty()) {
            sb.append("\n- samples: ").append(latencies.size());
            sb.append("\n- ok: ").append(ok);
            sb.append("\n- errors: ").append(errors).append('\n');
        }
        Files.writeString(file, sb.toString(), StandardCharsets.UTF_8);
    }

    private static String extractPolicyNumber(Response r) {
        try {
            JsonNode n = MAPPER.readTree(r.asString());
            if (n.hasNonNull("policyNumber")) {
                return n.get("policyNumber").asText();
            }
            if (n.hasNonNull("number")) {
                return n.get("number").asText();
            }
            JsonNode policy = n.get("policy");
            if (policy != null && policy.hasNonNull("policyNumber")) {
                return policy.get("policyNumber").asText();
            }
        } catch (Exception ignored) {
        }
        return r.jsonPath().getString("policyNumber");
    }

    private static String truncate(String s) {
        if (s == null) return "";
        return s.length() > 300 ? s.substring(0, 300) + "…" : s;
    }
}
