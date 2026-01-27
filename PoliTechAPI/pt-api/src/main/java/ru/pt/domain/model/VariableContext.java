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
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jayway.jsonpath.JsonPath;

public final class VariableContext implements Map<String, Object> {

    private static final Logger logger = LoggerFactory.getLogger(VariableContext.class);

    private final Object jsonDocument; // parsed once
    private final Map<String, PvVarDefinition> definitions;
    private final Map<String, Object> values = new ConcurrentHashMap<>();

    public VariableContext(String json, List<PvVarDefinition> vars) {
        logger.trace("Creating VariableContext with {} definitions", vars != null ? vars.size() : 0);
        this.jsonDocument = JsonPath.parse(json).json();
        this.definitions = new HashMap<>();
        if (vars != null) {
            vars.forEach(v -> {
                logger.trace("Registering definition: code={}, type={}, sourceType={}", 
                        v.getCode(), v.getType(), v.getSourceType());
                definitions.put(v.getCode(), v);
            });
        }
    }

    public Map<String, Object> getValues() {
        return values;
    }
    // ---------- Typed API ----------

    public BigDecimal getDecimal(String code) {
        Object v = get(code);
        BigDecimal result = safeToBigDecimal(v);
        logger.trace("getDecimal: code='{}', raw='{}', result='{}'", code, v, result);
        return result;
    }

    public BigDecimal safeToBigDecimal(Object value) {    
        logger.trace("safeToBigDecimal: input='{}' (type={})", value, value != null ? value.getClass().getName() : "null");

        if (value == null) {
            logger.trace("safeToBigDecimal: value is null, returning 0");
            return BigDecimal.ZERO;
        }
        
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        
        // Handle different number types
        if (value instanceof Number) {
            Number num = (Number) value;
            BigDecimal result = switch (num) {
                case Integer i -> BigDecimal.valueOf(i);
                case Long l -> BigDecimal.valueOf(l);
                case Double d -> BigDecimal.valueOf(d);
                case Float f -> BigDecimal.valueOf(f);
                case Short s -> BigDecimal.valueOf(s);
                case Byte b -> BigDecimal.valueOf(b);
                default -> BigDecimal.valueOf(num.doubleValue());
            };
            logger.trace("safeToBigDecimal: converted Number '{}' to '{}'", num, result);
            return result;
        }
        
        // Try to parse as string if it's a string representation of a number
        if (value instanceof String) {
            try {
                BigDecimal result = new BigDecimal((String) value);
                logger.trace("safeToBigDecimal: parsed String '{}' to '{}'", value, result);
                return result;
            } catch (NumberFormatException e) {
                logger.trace("safeToBigDecimal: cannot parse String '{}' to BigDecimal, returning 0", value);
                return BigDecimal.ZERO;
            }
        }
        
        return BigDecimal.ZERO;
    }


    public String getString(String code) {
        Object v = get(code);
        String result = v != null ? v.toString() : null;
        logger.trace("getString: code='{}', result='{}'", code, result);
        return result;
    }

    public PvVarDefinition getDefinition(String code) {
        return definitions.get(code);
    }

    public List<PvVarDefinition> getDefinitions() {
        return new ArrayList<>(definitions.values());
    }

    public void setValue(String key, Object value) {
        PvVarDefinition def = definitions.get(key);
        if (def == null) {
            logger.warn("setValue: Unknown variable '{}'", key);
            throw new IllegalArgumentException("Unknown variable: " + key);
        }
    
        if (def.getSourceType() == PvVarDefinition.VarSourceType.CONST && values.containsKey(key)) {
            logger.trace("setValue: ignoring update for CONST variable '{}'", key);
            return; // silently ignore, константа не обновляется
        }
    
        logger.trace("setValue: key='{}', value='{}'", key, value);
        values.put(key, value);
        //def.setValue(value);
    }
    // ---------- Lazy evaluation ----------

    @Override
    public Object get(Object key) {
        if (!(key instanceof String code)) return null;
        logger.trace("get: code='{}'", code);
        try {
            Object value = values.computeIfAbsent(code, this::evaluate);
            logger.trace("get: code='{}', resolvedValue='{}'", code, value);
            return value;
        } catch (Exception e) {
            logger.warn("get: error evaluating variable '{}': {}", code, e.getMessage());
            return ""; //"Error evaluating variable: " + code + " " + e.getMessage();
        }
    }

    private Object evaluate(String code) {
        logger.trace("evaluate: code='{}'", code);
        PvVarDefinition def = definitions.get(code);
        if (def == null) {
            logger.warn("evaluate: Unknown variable '{}'", code);
            throw new IllegalArgumentException("Unknown variable: " + code);
        }

        if (def.getSourceType() == PvVarDefinition.VarSourceType.MAGIC) {
            Object magic = ComputedVars.getMagicValue(this, code);
            logger.trace("evaluate: code='{}' (MAGIC) -> '{}'", code, magic);
            return magic;
        }

        if (def.getJsonPath() == null) {
            logger.trace("evaluate: code='{}' has null jsonPath, returning null (calc-only variable)", code);
            return null; // calc-only variable
        }

        Object raw = JsonPath.read(jsonDocument, def.getJsonPath());
        logger.trace("evaluate: code='{}', jsonPath='{}', raw='{}'", code, def.getJsonPath(), raw);
        Object ret =  convert(raw, def.getType(), def.getGroupFunctionName());
        logger.trace("evaluate: code='{}', converted='{}'", code, ret);
        return ret;
    }

    private Object convert(Object raw, PvVarDefinition.Type type, PvVarDefinition.GroupFunctionName groupFunctionName) {
        logger.trace("convert: raw='{}' (type={}), varType={}, groupFunction={}", 
                raw, raw != null ? raw.getClass().getName() : "null", type, groupFunctionName);
        if (raw == null) return null;

        if (raw instanceof List) {
            List<?> list = (List<?>) raw;
            logger.trace("convert: list size={}, firstElement={}", list.size(), list.isEmpty() ? null : list.get(0));
            
            if (groupFunctionName == null) {
                // If no group function specified, return based on type
                Object result = switch (type) {
                    case STRING -> list.stream()
                        .map(Object::toString)
                        .collect(Collectors.joining(", "));
                    case NUMBER -> list.isEmpty() ? BigDecimal.ZERO : new BigDecimal(list.get(0).toString());
                };
                logger.trace("convert: result without groupFunction='{}'", result);
                return result;
            }
            
            Object grouped = switch (groupFunctionName) {
                case COUNT -> BigDecimal.valueOf(list.size());
                case SUM -> {
                    BigDecimal sum = list.stream()
                        .map(this::safeToBigDecimal)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                    yield sum;
                }
                case AVG -> {
                    List<Double> numbers = list.stream()
                        .filter(Number.class::isInstance)
                        .map(n -> ((Number) n).doubleValue())
                        .toList();
                    if (numbers.isEmpty()) {
                        yield BigDecimal.ZERO;
                    }
                    double avg = numbers.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
                    yield BigDecimal.valueOf(avg);
                }
                case MIN -> {
                    Optional<BigDecimal> min = list.stream()
                        .map(this::safeToBigDecimal)
                        .min(BigDecimal::compareTo);
                    yield min.orElse(BigDecimal.ZERO);
                }
                case MAX -> {
                    Optional<BigDecimal> max = list.stream()
                        .map(this::safeToBigDecimal)
                        .max(BigDecimal::compareTo);
                    yield max.orElse(BigDecimal.ZERO);
                }
                case FIRST -> {
                    yield list.isEmpty() ? null : list.get(0);
                }
                case HZ -> {
                    yield switch (type) {
                        case STRING -> list.stream()
                            .map(Object::toString)
                            .collect(Collectors.toList());
                        case NUMBER -> list.isEmpty() ? BigDecimal.ZERO : new BigDecimal(list.get(0).toString());
                    };    
                }
                default -> null;
            };
            logger.trace("convert: result with groupFunction='{}'", grouped);
            return grouped;
        }

        Object scalarResult = switch (type) {
            case STRING -> raw.toString();
            case NUMBER -> new BigDecimal(raw.toString());
        };
        logger.trace("convert: scalar result='{}'", scalarResult);
        return scalarResult;

    }

    // ---------- Runtime mutation ----------

    @Override
    public Object put(String key, Object value) {
        PvVarDefinition def = definitions.get(key);
        if (def == null) {
            logger.warn("put: Unknown variable '{}'", key);
            throw new IllegalArgumentException("Unknown variable: " + key);
        }
        logger.trace("put: key='{}', value='{}'", key, value);
        return values.put(key, value);
    }

    public void putDefinition(PvVarDefinition def) {
        if (def == null) {
            logger.warn("putDefinition: null definition ignored");
            return;
        }
        logger.trace("putDefinition: code='{}', type='{}', sourceType='{}'", 
                def.getCode(), def.getType(), def.getSourceType());
        definitions.put(def.getCode(), def);
    }

    /* Только для калькулятора */
    public void putVar(String key, BigDecimal value) {
        PvVarDefinition def = definitions.get(key);
        if (def == null) {
            /* нет такой переменной */
            logger.trace("putVar: definition for '{}' not found, storing value anyway", key);
           // def = this.definitions.put(key, new PvVarDefinition(key, null, PvVarDefinition.Type.NUMBER, PvVarDefinition.VarScope.CALCULATOR));
        }
        logger.trace("putVar: key='{}', value='{}'", key, value);
        this.values.put(key, value);
    }
    public void cleanupTemporary() {
        logger.trace("cleanupTemporary: before cleanup definitions.size={}, values.size={}", 
                definitions.size(), values.size());
        definitions.entrySet().removeIf(e -> e.getValue().getScope() == PvVarDefinition.VarScope.CALCULATOR);
        values.keySet().removeIf(k -> !definitions.containsKey(k));
        logger.trace("cleanupTemporary: after cleanup definitions.size={}, values.size={}", 
                definitions.size(), values.size());
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

}
