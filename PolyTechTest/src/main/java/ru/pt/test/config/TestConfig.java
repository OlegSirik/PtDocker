package ru.pt.test.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TestConfig {

    private static final Pattern ENV_PATTERN =
            Pattern.compile("\\$\\{([A-Za-z0-9_]+)(?::([^}]*))?}");

    private String baseUrl = "http://localhost:8080";
    private String tenantCode = "demo";
    private String clientId = "sys";
    private Map<String, UserCreds> users = new HashMap<>();
    private Defaults defaults = new Defaults();
    private Load load = new Load();
    private AdminLifecycle adminLifecycle = new AdminLifecycle();
    private String reportDir = "reports";
    private List<String> tags = new ArrayList<>(List.of("regression"));
    private boolean failFast = false;
    private int connectTimeoutMs = 10_000;
    private int readTimeoutMs = 60_000;

    public static TestConfig load(Path externalConfig) throws IOException {
        ObjectMapper yaml = new ObjectMapper(new YAMLFactory());
        TestConfig cfg;
        try (InputStream in = TestConfig.class.getResourceAsStream("/application.yaml")) {
            if (in == null) {
                cfg = new TestConfig();
            } else {
                cfg = yaml.readValue(in, TestConfig.class);
            }
        }
        if (externalConfig != null && Files.isRegularFile(externalConfig)) {
            TestConfig override = yaml.readValue(Files.readString(externalConfig), TestConfig.class);
            cfg.merge(override);
        }
        cfg.resolveEnv();
        return cfg;
    }

    private void merge(TestConfig o) {
        if (o.baseUrl != null) baseUrl = o.baseUrl;
        if (o.tenantCode != null) tenantCode = o.tenantCode;
        if (o.clientId != null) clientId = o.clientId;
        if (o.users != null && !o.users.isEmpty()) users = o.users;
        if (o.defaults != null) defaults = o.defaults;
        if (o.load != null) load = o.load;
        if (o.adminLifecycle != null) adminLifecycle = o.adminLifecycle;
        if (o.reportDir != null) reportDir = o.reportDir;
        if (o.tags != null && !o.tags.isEmpty()) tags = o.tags;
        failFast = o.failFast;
        if (o.connectTimeoutMs > 0) connectTimeoutMs = o.connectTimeoutMs;
        if (o.readTimeoutMs > 0) readTimeoutMs = o.readTimeoutMs;
    }

    private void resolveEnv() {
        baseUrl = expand(baseUrl);
        tenantCode = expand(tenantCode);
        clientId = expand(clientId);
        reportDir = expand(reportDir);
        if (users != null) {
            for (UserCreds u : users.values()) {
                if (u == null) continue;
                u.setLogin(expand(u.getLogin()));
                u.setPassword(expand(u.getPassword()));
            }
        }
        if (defaults != null) {
            defaults.setProductCode(expand(defaults.getProductCode()));
            defaults.setPrintFormType(expand(defaults.getPrintFormType()));
        }
        List<String> expandedTags = new ArrayList<>();
        for (String t : tags) {
            expandedTags.add(expand(t));
        }
        tags = expandedTags;
    }

    private static String expand(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        Matcher m = ENV_PATTERN.matcher(value);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String key = m.group(1);
            String def = m.group(2) != null ? m.group(2) : "";
            String env = System.getenv(key);
            String repl = env != null ? env : def;
            m.appendReplacement(sb, Matcher.quoteReplacement(repl));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    public String apiRoot() {
        String base = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        return base + "/api/v1/" + tenantCode;
    }

    public UserCreds requireUser(String key) {
        UserCreds u = users != null ? users.get(key) : null;
        if (u == null || u.getLogin() == null || u.getLogin().isBlank()) {
            throw new IllegalStateException("User '" + key + "' is not configured");
        }
        if (u.getPassword() == null || u.getPassword().isBlank()) {
            throw new IllegalStateException("Password for user '" + key + "' is empty (set PT_* env)");
        }
        return u;
    }

    public boolean hasUserPassword(String key) {
        UserCreds u = users != null ? users.get(key) : null;
        return u != null
                && u.getLogin() != null && !u.getLogin().isBlank()
                && u.getPassword() != null && !u.getPassword().isBlank();
    }

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public String getTenantCode() { return tenantCode; }
    public void setTenantCode(String tenantCode) { this.tenantCode = tenantCode; }
    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }
    public Map<String, UserCreds> getUsers() { return users; }
    public void setUsers(Map<String, UserCreds> users) { this.users = users; }
    public Defaults getDefaults() { return defaults; }
    public void setDefaults(Defaults defaults) { this.defaults = defaults; }
    public Load getLoad() { return load; }
    public void setLoad(Load load) { this.load = load; }
    public AdminLifecycle getAdminLifecycle() { return adminLifecycle; }
    public void setAdminLifecycle(AdminLifecycle adminLifecycle) { this.adminLifecycle = adminLifecycle; }
    public String getReportDir() { return reportDir; }
    public void setReportDir(String reportDir) { this.reportDir = reportDir; }
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
    public boolean isFailFast() { return failFast; }
    public void setFailFast(boolean failFast) { this.failFast = failFast; }
    public int getConnectTimeoutMs() { return connectTimeoutMs; }
    public void setConnectTimeoutMs(int connectTimeoutMs) { this.connectTimeoutMs = connectTimeoutMs; }
    public int getReadTimeoutMs() { return readTimeoutMs; }
    public void setReadTimeoutMs(int readTimeoutMs) { this.readTimeoutMs = readTimeoutMs; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UserCreds {
        private String login;
        private String password;
        public String getLogin() { return login; }
        public void setLogin(String login) { this.login = login; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Defaults {
        private String productCode = "CARGO_GENERAL";
        private long versionNo = 1;
        private String printFormType = "POLICY";
        public String getProductCode() { return productCode; }
        public void setProductCode(String productCode) { this.productCode = productCode; }
        public long getVersionNo() { return versionNo; }
        public void setVersionNo(long versionNo) { this.versionNo = versionNo; }
        public String getPrintFormType() { return printFormType; }
        public void setPrintFormType(String printFormType) { this.printFormType = printFormType; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Load {
        /** Parallel workers for steady / e2e load. */
        private int threads = 20;
        /** Policy+print cycles per worker (used when totalRequests is 0). */
        private int iterationsPerThread = 5;
        /** Total policy+print requests for e2e / steady (preferred over iterationsPerThread). */
        private int totalRequests = 1000;
        /** Concurrency levels probed to find max error-free parallelism. */
        private List<Integer> concurrencyProbe = new ArrayList<>(List.of(1, 2, 4, 8));
        /** If false, print HTTP 404 is treated as non-error. */
        private boolean requirePrint = false;
        public int getThreads() { return threads; }
        public void setThreads(int threads) { this.threads = threads; }
        public int getIterationsPerThread() { return iterationsPerThread; }
        public void setIterationsPerThread(int iterationsPerThread) {
            this.iterationsPerThread = iterationsPerThread;
        }
        public int getTotalRequests() { return totalRequests; }
        public void setTotalRequests(int totalRequests) { this.totalRequests = totalRequests; }
        public List<Integer> getConcurrencyProbe() { return concurrencyProbe; }
        public void setConcurrencyProbe(List<Integer> concurrencyProbe) {
            this.concurrencyProbe = concurrencyProbe;
        }
        public boolean isRequirePrint() { return requirePrint; }
        public void setRequirePrint(boolean requirePrint) { this.requirePrint = requirePrint; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AdminLifecycle {
        /** Soft-delete the ephemeral tenant after the scenario (default true). */
        private boolean deleteTenant = true;
        public boolean isDeleteTenant() { return deleteTenant; }
        public void setDeleteTenant(boolean deleteTenant) { this.deleteTenant = deleteTenant; }
    }
}
