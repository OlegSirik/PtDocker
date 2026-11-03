package ru.pt.test.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.Response;
import ru.pt.test.client.ApiClient;
import ru.pt.test.config.TestConfig;
import ru.pt.test.scenario.ScenarioContext;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public final class LoadRunner {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private LoadRunner() {
    }

    /**
     * Run {@code totalRequests} policy+print cycles across {@code threads} workers
     * on the current tenant (uses {@link ApiClient#effectiveTenant()}).
     */
    public static void runPolicyPrint(ScenarioContext ctx, int totalRequests, int threads) throws Exception {
        if (totalRequests < 1) {
            throw new IllegalArgumentException("totalRequests must be >= 1");
        }
        threads = Math.max(1, threads);
        String token = ctx.client().getBearerToken();
        String tenant = ctx.client().effectiveTenant();
        Long accountId = ctx.client().getAccountId();
        String body = ctx.fixture("crg-policy.json");
        TestConfig config = ctx.config();

        int base = totalRequests / threads;
        int rem = totalRequests % threads;

        List<Long> latencies = new ArrayList<>();
        AtomicInteger errors = new AtomicInteger();
        AtomicInteger ok = new AtomicInteger();

        ExecutorService pool = Executors.newFixedThreadPool(threads);
        try {
            List<Callable<Void>> tasks = new ArrayList<>();
            for (int t = 0; t < threads; t++) {
                int iters = base + (t < rem ? 1 : 0);
                if (iters == 0) {
                    continue;
                }
                final int n = iters;
                tasks.add(() -> {
                    ApiClient worker = new ApiClient(config);
                    worker.setBearerToken(token);
                    worker.setTenantOverride(tenant);
                    if (accountId != null) {
                        worker.setAccountId(accountId);
                    }
                    for (int i = 0; i < n; i++) {
                        long t0 = System.nanoTime();
                        try {
                            runOnce(worker, body, config);
                            ok.incrementAndGet();
                        } catch (Exception e) {
                            errors.incrementAndGet();
                        }
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
            pool.awaitTermination(30, TimeUnit.MINUTES);
        }

        int total = ok.get() + errors.get();
        double avg = latencies.stream().mapToLong(Long::longValue).average().orElse(0);
        long p95 = percentile(latencies, 95);
        String summary = String.format(Locale.ROOT,
                "tenant=%s threads=%d total=%d ok=%d errors=%d avgMs=%.1f p95Ms=%d",
                tenant, threads, total, ok.get(), errors.get(), avg, p95);
        System.out.println("LOAD  " + summary);
        writeReport(ctx, summary, latencies, ok.get(), errors.get());
        ctx.put("load.summary", summary);

        if (errors.get() > 0) {
            throw new AssertionError("Load had errors: " + summary);
        }
    }

    private static void runOnce(ApiClient worker, String policyJson, TestConfig config) throws Exception {
        Response save = worker.post("/sales/policies", MAPPER.readTree(policyJson));
        if (save.statusCode() < 200 || save.statusCode() >= 300) {
            throw new AssertionError("policy HTTP " + save.statusCode() + " " + truncate(save.asString()));
        }
        String pn = extractPolicyNumber(save);
        if (pn == null || pn.isBlank()) {
            throw new AssertionError("policyNumber missing");
        }
        String pf = config.getDefaults().getPrintFormType();
        Response print = worker.getBytes("/sales/policies/" + pn + "/printpf/" + pf);
        int sc = print.statusCode();
        if (sc >= 200 && sc < 300) {
            return;
        }
        // Print is best-effort unless requirePrint=true (404/500 e.g. duplicate nr / no form)
        if (!config.getLoad().isRequirePrint()) {
            return;
        }
        throw new AssertionError("print HTTP " + sc + " for " + pn);
    }

    private static long percentile(List<Long> values, int p) {
        if (values == null || values.isEmpty()) {
            return 0;
        }
        List<Long> sorted = new ArrayList<>(values);
        sorted.sort(Long::compareTo);
        int idx = (int) Math.ceil(p / 100.0 * sorted.size()) - 1;
        idx = Math.max(0, Math.min(idx, sorted.size() - 1));
        return sorted.get(idx);
    }

    private static void writeReport(ScenarioContext ctx, String summary, List<Long> latencies, int ok, int errors)
            throws Exception {
        Path dir = Path.of(ctx.config().getReportDir());
        Files.createDirectories(dir);
        StringBuilder sb = new StringBuilder();
        sb.append("# Load e2e\n\n```\n").append(summary).append("\n```\n");
        sb.append("\n- samples: ").append(latencies.size());
        sb.append("\n- ok: ").append(ok);
        sb.append("\n- errors: ").append(errors).append('\n');
        Files.writeString(dir.resolve("load-e2e.md"), sb.toString(), StandardCharsets.UTF_8);
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
