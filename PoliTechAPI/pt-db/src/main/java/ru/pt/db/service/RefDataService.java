package ru.pt.db.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import ru.pt.api.service.db.ReferenceDataService;

@Component
public class RefDataService implements ReferenceDataService {

    private static final long DEFAULT_TID = 1L;

    private final JdbcTemplate jdbcTemplate;

    // tid -> varCode -> (mdCode -> mdName)
    private final Map<Long, Map<String, Map<String, String>>> cache = new ConcurrentHashMap<>();

    public RefDataService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void loadCache() {
        reloadCache();
    }

    public void reloadCache() {
        Map<Long, Map<String, Map<String, String>>> next = new ConcurrentHashMap<>();
        jdbcTemplate.query(
                "select tid, ref_code, md_code, md_name from pt_refdata",
                rs -> {
                    Long tid = rs.getLong("tid");
                    String varCode = rs.getString("ref_code");
                    String mdCode = rs.getString("md_code");
                    String mdName = rs.getString("md_name");

                    next.computeIfAbsent(tid, k -> new ConcurrentHashMap<>())
                            .computeIfAbsent(varCode, k -> new HashMap<>())
                            .put(mdCode, mdName);
                });
        cache.clear();
        cache.putAll(next);
    }

    @Override
    public List<String> getAllRefs() {
        return getAllRefs(DEFAULT_TID);
    }

    public List<String> getAllRefs(Long tid) {
        Map<String, Map<String, String>> byTenant = cache.get(tid);
        if (byTenant == null || byTenant.isEmpty()) {
            return Collections.emptyList();
        }
        return new ArrayList<>(byTenant.keySet());
    }

    @Override
    public String getName(String attributeCode, String code) {
        return getName(DEFAULT_TID, attributeCode, code);
    }

    public String getName(Long tid, String attributeCode, String code) {
        Map<String, String> byAttr = getTenantDict(tid, attributeCode);
        if (byAttr == null) {
            return null;
        }
        return byAttr.get(code);
    }

    @Override
    public Map<String, String> getRefData(String attributeCode) {
        return getRefData(DEFAULT_TID, attributeCode);
    }

    @Override
    public Map<String, String> getRefData(Long tid, String attributeCode) {
        Map<String, String> byAttr = getTenantDict(tid, attributeCode);
        if (byAttr == null) {
            return Collections.emptyMap();
        }
        return new HashMap<>(byAttr);
    }

    @Override
    public Map<String, String> getRefData(String attributeCode, List<String> filter) {
        return getRefData(DEFAULT_TID, attributeCode, filter);
    }

    @Override
    public Map<String, String> getRefData(Long tid, String attributeCode, List<String> filter) {
        if (filter == null || filter.isEmpty()) {
            return getRefData(tid, attributeCode);
        }
        Map<String, String> byAttr = getTenantDict(tid, attributeCode);
        if (byAttr == null) {
            return Collections.emptyMap();
        }
        Map<String, String> result = new HashMap<>();
        for (String key : filter) {
            if (byAttr.containsKey(key)) {
                result.put(key, byAttr.get(key));
            }
        }
        return result;
    }

    private Map<String, String> getTenantDict(Long tid, String attributeCode) {
        Map<String, Map<String, String>> byTenant = cache.get(tid);
        if (byTenant == null) {
            return null;
        }
        return byTenant.get(attributeCode);
    }
}
