package ru.pt.domain.model;

import java.math.BigDecimal;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.jayway.jsonpath.JsonPath;

public final class VariableContext implements Map<String, Object> {

    private final Object jsonDocument; // parsed once
    private final Map<String, PvVarDefinition> definitions;
    private final Map<String, Object> values = new HashMap<>();

    public VariableContext(String json, List<PvVarDefinition> vars) {
        this.jsonDocument = JsonPath.parse(json).json();
        this.definitions = new HashMap<>();
        vars.forEach(v -> definitions.put(v.getCode(), v));
    }

    // ---------- Typed API ----------

    public BigDecimal getDecimal(String code) {
        Object v = get(code);
        return v instanceof BigDecimal ? (BigDecimal) v : BigDecimal.ZERO;
    }

    public String getString(String code) {
        Object v = get(code);
        return v != null ? v.toString() : null;
    }

    public PvVarDefinition getDefinition(String code) {
        return definitions.get(code);
    }

    public List<PvVarDefinition> getDefinitions() {
        return new ArrayList<>(definitions.values());
    }

    public void setValue(String key, Object value) {
        PvVarDefinition def = definitions.get(key);
        if (def == null) throw new IllegalArgumentException("Unknown variable: " + key);
    
        if (def.getSourceType() == PvVarDefinition.VarSourceType.CONST && values.containsKey(key)) {
            return; // silently ignore, константа не обновляется
        }
    
        values.put(key, value);
        //def.setValue(value);
    }
    // ---------- Lazy evaluation ----------

    @Override
    public Object get(Object key) {
        if (!(key instanceof String code)) return null;

        Object object = new String("Error evaluating variable: " + code);
        try {
            object = values.computeIfAbsent(code, this::evaluate);
        } catch (Exception e) {
            //logger.error("Error evaluating variable: " + code, e);
            //return "Error evaluating variable: " + code;
        }
        return object;
    }

    private Object evaluate(String code) {
        PvVarDefinition def = definitions.get(code);
        if (def == null) {
            throw new IllegalArgumentException("Unknown variable: " + code);
        }

        if (def.getJsonPath() == null) {
            return null; // calc-only variable
        }

        Object raw = JsonPath.read(jsonDocument, def.getJsonPath());
        return convert(raw, def.getType());
    }

    private Object convert(Object raw, PvVarDefinition.Type type) {
        if (raw == null) return null;

        return switch (type) {
            case STRING -> raw.toString();
            case NUMBER -> new BigDecimal(raw.toString());
        };
    }

    // ---------- Runtime mutation ----------

    @Override
    public Object put(String key, Object value) {
        PvVarDefinition def = definitions.get(key);
        if (def == null) {
            throw new IllegalArgumentException("Unknown variable: " + key);
        }
        return values.put(key, value);
    }

    /* Только для калькулятора */
    public void putVar(String key, BigDecimal value) {
        PvVarDefinition def = definitions.get(key);
        if (def == null) {
            /* нет такой переменной */
           // def = this.definitions.put(key, new PvVarDefinition(key, null, PvVarDefinition.Type.NUMBER, PvVarDefinition.VarScope.CALCULATOR));
        }
        this.values.put(key, value);
    }
    public void cleanupTemporary() {
        definitions.entrySet().removeIf(e -> e.getValue().getScope() == PvVarDefinition.VarScope.CALCULATOR);
        values.keySet().removeIf(k -> !definitions.containsKey(k));
    }

    // ---------- Map view (delegated) ----------

    @Override public int size() { return definitions.size(); }
    @Override public boolean isEmpty() { return definitions.isEmpty(); }
    @Override public boolean containsKey(Object key) { return definitions.containsKey(key); }

    @Override
    public boolean containsValue(Object value) {
        for (String k : definitions.keySet()) {
            if (Objects.equals(get(k), value)) return true;
        }
        return false;
    }

    @Override
    public Object remove(Object key) {
        return values.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ?> m) {
        m.forEach(this::put);
    }

    @Override
    public void clear() {
        values.clear();
    }

    @Override
    public Set<String> keySet() {
        return Collections.unmodifiableSet(definitions.keySet());
    }

    @Override
    public Collection<Object> values() {
        List<Object> list = new ArrayList<>();
        definitions.keySet().forEach(k -> list.add(get(k)));
        return Collections.unmodifiableList(list);
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        Set<Entry<String, Object>> set = new HashSet<>();
        definitions.keySet().forEach(k ->
                set.add(new AbstractMap.SimpleEntry<>(k, get(k)))
        );
        return Collections.unmodifiableSet(set);
    }

    // ----------- Typed API for calculator --------------------------------------

    public void setCoverSumInsured(String cover,BigDecimal value) {
        if (cover == null) {
            return;
        }
        String sumInsuredVarCode = "co_" + cover + "_sumInsured";
        this.values.put(sumInsuredVarCode, value);
    }
    public BigDecimal getCoverSumInsured(String cover) {
        if (cover == null) {
            return null;
        }
        String sumInsuredVarCode = "co_" + cover + "_sumInsured";
        return (BigDecimal) this.values.get(sumInsuredVarCode);
    }

    public void setCoverPremium(String cover,BigDecimal value) {
        if (cover == null) {
            return;
        }
        String premiumVarCode = "co_" + cover + "_premium";
        this.values.put(premiumVarCode, value);
    }
    public BigDecimal getCoverPremium(String cover) {
        if (cover == null) {
            return null;
        }
        String premiumVarCode = "co_" + cover + "_premium";
        return (BigDecimal) this.values.get(premiumVarCode);
    }

    public void setCoverDeductibleNr(String cover,Integer value) {
        if (cover == null) {
            return;
        }
        String deductibleNrVarCode = "co_" + cover + "_deductibleNr";
        this.values.put(deductibleNrVarCode, value.toString());
    }
    public Integer getCoverDeductibleNr(String cover) {
        if (cover == null) {
            return null;
        }
        String deductibleNrVarCode = "co_" + cover + "_deductibleNr";
        return (Integer) this.values.get(deductibleNrVarCode);
    }

    public String getPackageNo() {
        return (String) this.values.get("pl_packageNo");
    }
}
