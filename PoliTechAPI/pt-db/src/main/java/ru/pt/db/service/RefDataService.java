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
public class RefDataService  implements ReferenceDataService{

    private final JdbcTemplate jdbcTemplate;

    // varCode -> (mdCode -> mdName)
    private final Map<String, Map<String, String>> cache = new ConcurrentHashMap<>();

    public RefDataService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void loadCache() {
        jdbcTemplate.query(
            "select ref_Code, md_Code, md_Name from pt_refdata",
            rs -> {
                String varCode = rs.getString("ref_Code");
                String mdCode  = rs.getString("md_Code");
                String mdName  = rs.getString("md_Name");

                cache
                    .computeIfAbsent(varCode, k -> new HashMap<>())
                    .put(mdCode, mdName);
            }
        );
    }

    @Override
    public List<String> getAllRefs() {
        if (cache == null || cache.isEmpty()) {
            return Collections.emptyList();
        }
        // Возвращаем копию списка для безопасности
        return new ArrayList<>(cache.keySet());
    }

    @Override
    public String getName(String attributeCode, String code) {
        Map<String, String> byAttr = cache.get(attributeCode);
        if (byAttr == null) {
            return null; // or code
        }
        return byAttr.get(code); // or byAttr.getOrDefault(code, code)
    }

    
/*         
Country
AddressType
DeviceType
IdType
FamilyStType
SexType
Product
*/
    
    
}
