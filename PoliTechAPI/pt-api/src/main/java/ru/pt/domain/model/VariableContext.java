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
import ru.pt.api.dto.product.PvVar;

import com.jayway.jsonpath.JsonPath;


public final class VariableContext implements Map<String, Object> {

    private final Object jsonDocument; // parsed once
    private final Map<String, PvVarDefinition> definitions;
    private final Map<String, Object> values = new ConcurrentHashMap<>();

    public VariableContext(String json, List<PvVarDefinition> vars) {
        this.jsonDocument = JsonPath.parse(json).json();
        this.definitions = new HashMap<>();
        vars.forEach(v -> definitions.put(v.getCode(), v));
    }

    public Map<String, Object> getValues() {
        return values;
    }
    // ---------- Typed API ----------

    public BigDecimal getDecimal(String code) {
        Object v = get(code);
        return safeToBigDecimal(v);
    }

    public BigDecimal safeToBigDecimal(Object value) {    
        
        if (value == null) {
            return BigDecimal.ZERO;
        }
        
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        
        // Handle different number types
        if (value instanceof Number) {
            Number num = (Number) value;
            return switch (num) {
                case Integer i -> BigDecimal.valueOf(i);
                case Long l -> BigDecimal.valueOf(l);
                case Double d -> BigDecimal.valueOf(d);
                case Float f -> BigDecimal.valueOf(f);
                case Short s -> BigDecimal.valueOf(s);
                case Byte b -> BigDecimal.valueOf(b);
                default -> BigDecimal.valueOf(num.doubleValue());
            };
        }
        
        // Try to parse as string if it's a string representation of a number
        if (value instanceof String) {
            try {
                return new BigDecimal((String) value);
            } catch (NumberFormatException e) {
                return BigDecimal.ZERO;
            }
        }
        
        return BigDecimal.ZERO;
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
        try {
            return values.computeIfAbsent(code, this::evaluate);
        } catch (Exception e) {
            return "Error evaluating variable: " + code + " " + e.getMessage();
        }
    }

    private Object evaluate(String code) {
        PvVarDefinition def = definitions.get(code);
        if (def == null) {
            throw new IllegalArgumentException("Unknown variable: " + code);
        }

        if (def.getSourceType() == PvVarDefinition.VarSourceType.MAGIC) {
            return new ComputedVars(this).getMagicValue(code);
        }

        if (def.getJsonPath() == null) {
            return null; // calc-only variable
        }

        String path = def.getJsonPath();
        String grp = def.getGroupFunctionName().getValue();
        Object raw = JsonPath.read(jsonDocument, def.getJsonPath());
        Object ret =  convert(raw, def.getType(), def.getGroupFunctionName());
        return ret;
    }

    private Object convert(Object raw, PvVarDefinition.Type type, PvVarDefinition.GroupFunctionName groupFunctionName) {
        if (raw == null) return null;

        if (raw instanceof List) {
            List<?> list = (List<?>) raw;
            
            if (groupFunctionName == null) {
                // If no group function specified, return based on type
                return switch (type) {
                    case STRING -> list.stream()
                        .map(Object::toString)
                        .collect(Collectors.joining(", "));
                    case NUMBER -> list.isEmpty() ? BigDecimal.ZERO : new BigDecimal(list.get(0).toString());
                };
            }
            
            return switch (groupFunctionName) {
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
        }

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

    public void putDefinition(PvVarDefinition def) {
        //if ( !definitions.containsKey(def.getCode() )) {
            definitions.put(def.getCode(), def);
        //}
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
        String sumInsuredVarCode = PvVar.varSumInsured(cover).getVarCode(); //"co_" + cover + "_sumInsured";
        this.values.put(sumInsuredVarCode, value);
    }
    public BigDecimal getCoverSumInsured(String cover) {
        if (cover == null) {
            return null;
        }
        String sumInsuredVarCode = PvVar.varSumInsured(cover).getVarCode(); //"co_" + cover + "_sumInsured";
        return (BigDecimal) this.values.get(sumInsuredVarCode);
    }

    public void setCoverPremium(String cover,BigDecimal value) {
        if (cover == null) {
            return;
        }
        String premiumVarCode = PvVar.varSumInsured(cover).getVarCode(); //"co_" + cover + "_premium";
        this.values.put(premiumVarCode, value);
    }
    public BigDecimal getCoverPremium(String cover) {
        if (cover == null) {
            return null;
        }
        String premiumVarCode = PvVar.varSumInsured(cover).getVarCode(); //"co_" + cover + "_premium";
        return (BigDecimal) this.values.get(premiumVarCode);
    }

    public void setCoverDeductibleNr(String cover,Integer value) {
        if (cover == null) {
            return;
        }
        String deductibleNrVarCode = PvVar.varDeductibleNr(cover).getVarCode(); //"co_" + cover + "_deductibleNr";
        this.values.put(deductibleNrVarCode, value.toString());
    }
    public Integer getCoverDeductibleNr(String cover) {
        if (cover == null) {
            return null;
        }
        String deductibleNrVarCode = PvVar.varDeductibleNr(cover).getVarCode(); //"co_" + cover + "_deductibleNr";
        return (Integer) this.values.get(deductibleNrVarCode);
    }

    public BigDecimal getCoverLimitMin(String cover) {
        if (cover == null) {
            return null;
        }
        String limitMinVarCode = PvVar.varLimitMin(cover).getVarCode(); //"co_" + cover + "_limitMin";
        return (BigDecimal) this.values.get(limitMinVarCode);
    }
    public BigDecimal getCoverLimitMax(String cover) {
        if (cover == null) {
            return null;
        }
        String limitMaxVarCode = PvVar.varLimitMax(cover).getVarCode(); //"co_" + cover + "_limitMin";
        return (BigDecimal)this.values.get(limitMaxVarCode);
    }

    public String getPackageNo() {
        return (String) this.get("io_packageCode");
    }
}
