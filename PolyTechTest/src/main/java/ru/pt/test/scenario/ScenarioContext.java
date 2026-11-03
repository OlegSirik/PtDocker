package ru.pt.test.scenario;

import ru.pt.test.auth.AuthSession;
import ru.pt.test.client.ApiClient;
import ru.pt.test.config.TestConfig;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class ScenarioContext {

    private final TestConfig config;
    private final ApiClient client;
    private final AuthSession auth;
    private final Map<String, Object> bag = new HashMap<>();

    public ScenarioContext(TestConfig config, ApiClient client, AuthSession auth) {
        this.config = config;
        this.client = client;
        this.auth = auth;
    }

    public TestConfig config() {
        return config;
    }

    public ApiClient client() {
        return client;
    }

    public AuthSession auth() {
        return auth;
    }

    public void put(String key, Object value) {
        bag.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) bag.get(key);
    }

    public String fixture(String classpathName) {
        String path = "/fixtures/" + classpathName;
        try (InputStream in = ScenarioContext.class.getResourceAsStream(path)) {
            if (in == null) {
                throw new IllegalStateException("Fixture not found: " + path);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot read fixture " + path, e);
        }
    }
}
