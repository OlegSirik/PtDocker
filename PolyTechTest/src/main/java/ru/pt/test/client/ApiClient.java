package ru.pt.test.client;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import ru.pt.test.config.TestConfig;

import java.util.Map;

public class ApiClient {

    private final TestConfig config;
    private volatile String bearerToken;
    private volatile String tenantOverride;
    private volatile String impersonateTenant;
    /** Acting account for multi-role logins ({@code X-Account-Id}). */
    private volatile Long accountId;
    /** Client auth client id ({@code X-Client-ID}) — switches strategy to client authType. */
    private volatile String xClientId;
    /** API key for clients with authType=APIKEY ({@code X-Api-Key}). */
    private volatile String apiKey;

    public ApiClient(TestConfig config) {
        this.config = config;
        RestAssured.config = RestAssuredConfig.config()
                .httpClient(HttpClientConfig.httpClientConfig()
                        .setParam("http.connection.timeout", config.getConnectTimeoutMs())
                        .setParam("http.socket.timeout", config.getReadTimeoutMs()));
    }

    public void setBearerToken(String token) {
        this.bearerToken = token;
    }

    public String getBearerToken() {
        return bearerToken;
    }

    public void setTenantOverride(String tenantCode) {
        this.tenantOverride = tenantCode;
    }

    public String getTenantOverride() {
        return tenantOverride;
    }

    public void clearTenantOverride() {
        this.tenantOverride = null;
    }

    public void setImpersonateTenant(String tenantCode) {
        this.impersonateTenant = tenantCode;
    }

    public void clearImpersonateTenant() {
        this.impersonateTenant = null;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void clearAccountId() {
        this.accountId = null;
    }

    public void setXClientId(String xClientId) {
        this.xClientId = xClientId;
    }

    public void clearXClientId() {
        this.xClientId = null;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public void clearApiKey() {
        this.apiKey = null;
    }

    /** Drop JWT / API-key / account / client headers used for sales identity. */
    public void clearSalesIdentity() {
        this.bearerToken = null;
        this.apiKey = null;
        this.accountId = null;
        this.xClientId = null;
    }

    public TestConfig config() {
        return config;
    }

    public String effectiveTenant() {
        return tenantOverride != null && !tenantOverride.isBlank()
                ? tenantOverride
                : config.getTenantCode();
    }

    public String apiRoot() {
        String base = config.getBaseUrl().endsWith("/")
                ? config.getBaseUrl().substring(0, config.getBaseUrl().length() - 1)
                : config.getBaseUrl();
        return base + "/api/v1/" + effectiveTenant();
    }

    public RequestSpecification spec() {
        RequestSpecBuilder b = new RequestSpecBuilder()
                .setBaseUri(apiRoot())
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON);
        applyAuthHeaders(b);
        return b.build();
    }

    private void applyAuthHeaders(RequestSpecBuilder b) {
        String key = apiKey;
        if (key != null && !key.isBlank()) {
            b.addHeader("X-Api-Key", key);
        } else {
            String token = bearerToken;
            if (token != null && !token.isBlank()) {
                b.addHeader("Authorization", "Bearer " + token);
            }
        }
        String cid = xClientId;
        if (cid != null && !cid.isBlank()) {
            b.addHeader("X-Client-ID", cid);
        }
        String imp = impersonateTenant;
        if (imp != null && !imp.isBlank()) {
            b.addHeader("X-Imp-Tenant", imp);
        }
        Long acc = accountId;
        if (acc != null) {
            b.addHeader("X-Account-Id", String.valueOf(acc));
        }
    }

    public Response get(String path) {
        return RestAssured.given().spec(spec()).when().get(path);
    }

    public Response get(String path, Map<String, ?> query) {
        return RestAssured.given().spec(spec()).queryParams(query).when().get(path);
    }

    public Response post(String path, Object body) {
        var req = RestAssured.given().spec(spec());
        if (body != null) {
            req = req.body(body);
        }
        return req.when().post(path);
    }

    public Response put(String path, Object body) {
        var req = RestAssured.given().spec(spec());
        if (body != null) {
            req = req.body(body);
        }
        return req.when().put(path);
    }

    public Response patch(String path, Object body) {
        var req = RestAssured.given().spec(spec());
        if (body != null) {
            req = req.body(body);
        }
        return req.when().patch(path);
    }

    public Response delete(String path) {
        return RestAssured.given().spec(spec()).when().delete(path);
    }

    public Response getBytes(String path) {
        RequestSpecBuilder b = new RequestSpecBuilder()
                .setBaseUri(apiRoot());
        applyAuthHeaders(b);
        return RestAssured.given().spec(b.build()).when().get(path);
    }
}
