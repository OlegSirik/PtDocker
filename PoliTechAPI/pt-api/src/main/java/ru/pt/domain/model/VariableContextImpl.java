package ru.pt.domain.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jayway.jsonpath.JsonPath;

import ru.pt.api.dto.product.ProductVersionModel;

public final class VariableContextImpl implements CalculatorContext {

    private static final Logger logger = LoggerFactory.getLogger(VariableContextImpl.class);

    /** ConcurrentHashMap не поддерживает null; sentinel по типу var из definitions. */
    private static final Object NULL_VALUE_NUMBER = new Object();
    private static final Object NULL_VALUE_STRING = new Object();

    private final Map<String, PvVarDefinition> definitions;
    private final Map<String, Object> values = new ConcurrentHashMap<>();

    public static Builder builder() {
        return new Builder();
    }

    private VariableContextImpl(Map<String, PvVarDefinition> definitions) {
        this.definitions = new HashMap<>(definitions);
        logger.trace("Creating VariableContext with {} definitions", this.definitions.size());
    }

    public static final class Builder {
        private String json;
        private ProductVersionModel productVersion;
        private List<PvVarDefinition> varDefinitions;

        public Builder json(String json) {
            this.json = json;
            return this;
        }

        public Builder productVersion(ProductVersionModel productVersion) {
            this.productVersion = productVersion;
            return this;
        }

        public Builder varDefinitions(List<PvVarDefinition> varDefinitions) {
            this.varDefinitions = varDefinitions;
            return this;
        }

        public VariableContextImpl build() {
            if (json == null) {
                throw new IllegalArgumentException("json is required");
            }
            Object jsonDocument = JsonPath.parse(json).json();
            List<PvVarDefinition> defs = resolveDefinitions();
            VariableContextImpl ctx = new VariableContextImpl(toDefinitionMap(defs));

            for (PvVarDefinition def : defs) {
                if (def.getSourceType() == PvVarDefinition.VarSourceType.MAGIC) {
                    continue;
                }
                Object value = ctx.resolveFromJson(jsonDocument, def);
                ctx.putValueInternal(def.getCode(), value);
                logger.trace("materialize: code='{}', value='{}'", def.getCode(), value);
            }
            ctx.calcEmptyMagic();

            return ctx;
        }

        private List<PvVarDefinition> resolveDefinitions() {
            if (varDefinitions != null) {
                return varDefinitions;
            }
            if (productVersion == null || productVersion.getVars() == null) {
                return List.of();
            }
            return productVersion.getVars().stream()
                    .filter(v -> !v.getIsDeleted())
                    .map(PvVarDefinition::fromPvVar)
                    .toList();
        }

        private static Map<String, PvVarDefinition> toDefinitionMap(List<PvVarDefinition> defs) {
            Map<String, PvVarDefinition> map = new HashMap<>();
            if (defs != null) {
                defs.forEach(def -> map.put(def.getCode(), def));
            }
            return map;
        }
    }

    public Map<String, Object> getValues() {
        Map<String, Object> decoded = new HashMap<>();
        values.forEach((k, v) -> decoded.put(k, decodeNullValue(v)));
        return decoded;
    }

    @Override
    public BigDecimal getDecimal(String code) {
        Object v = get(code);
        if (v == null) {
            return null;
        }
        return safeToBigDecimal(v);
    }

    private BigDecimal safeToBigDecimal(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal bd) {
            return bd;
        }
        if (value instanceof Number num) {
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
        if (value instanceof String s) {
            try {
                return new BigDecimal(s);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    @Override
    public String getString(String code) {
        Object v = get(code);
        return v != null ? v.toString() : null;
    }

    @Override
    public PvVarDefinition getDefinition(String code) {
        return definitions.get(code);
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

    public List<PvVarDefinition> getDefinitions() {
        return new ArrayList<>(definitions.values());
    }

    @Override
    public void calcEmptyMagic() {
        definitions.values().stream()
                .filter(def -> def.getSourceType() == PvVarDefinition.VarSourceType.MAGIC)
                .forEach(def -> {
                    Object value = ComputedVars.getMagicValue(this, def.getCode());
                    putValueInternal(def.getCode(), value);
                });
    }

    @Override
    public Object get(Object key) {
        if (!(key instanceof String code)) {
            return null;
        }
        Object cached = values.get(code);
        if (cached == null && !values.containsKey(code)) {
            PvVarDefinition def = definitions.get(code);
            if (def != null && def.getSourceType() == PvVarDefinition.VarSourceType.MAGIC) {
                Object magic = ComputedVars.getMagicValue(this, code);
                putValueInternal(code, magic);
                return magic;
            }
            return null;
        }
        return decodeNullValue(cached);
    }

    private Object resolveFromJson(Object jsonDocument, PvVarDefinition def) {
        if (def.getJsonPath() == null) {
            return null;
        }
        try {
            Object raw = JsonPath.read(jsonDocument, def.getJsonPath());
            return convert(raw, def.getType(), def.getGroupFunctionName());
        } catch (Exception e) {
            logger.trace("resolveFromJson: code='{}', path='{}', error={}",
                    def.getCode(), def.getJsonPath(), e.getMessage());
            return null;
        }
    }

    private Object convert(Object raw, PvVarDefinition.Type type, PvVarDefinition.GroupFunctionName groupFunctionName) {
        if (raw == null) {
            return null;
        }

        if (raw instanceof List<?> list) {
            if (groupFunctionName == null) {
                return switch (type) {
                    case STRING -> list.stream()
                            .map(Object::toString)
                            .collect(Collectors.joining(", "));
                    case NUMBER -> list.isEmpty() ? BigDecimal.ZERO : new BigDecimal(list.get(0).toString());
                };
            }

            return switch (groupFunctionName) {
                case COUNT -> BigDecimal.valueOf(list.size());
                case SUM -> list.stream()
                        .map(this::safeToBigDecimal)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
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
                case MIN -> list.stream()
                        .map(this::safeToBigDecimal)
                        .min(BigDecimal::compareTo)
                        .orElse(BigDecimal.ZERO);
                case MAX -> list.stream()
                        .map(this::safeToBigDecimal)
                        .max(BigDecimal::compareTo)
                        .orElse(BigDecimal.ZERO);
                case FIRST -> list.isEmpty() ? null : list.get(0);
                case HZ -> switch (type) {
                    case STRING -> list.stream()
                            .map(Object::toString)
                            .collect(Collectors.toList());
                    case NUMBER -> list.isEmpty() ? BigDecimal.ZERO : new BigDecimal(list.get(0).toString());
                };
                default -> null;
            };
        }

        return switch (type) {
            case STRING -> raw.toString();
            case NUMBER -> new BigDecimal(raw.toString());
        };
    }

    @Override
    public Object put(String key, Object value) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Variable key is null or blank");
        }
        PvVarDefinition def = definitions.get(key);
        if (def == null) {
            throw new IllegalArgumentException("Unknown variable: " + key);
        }
        return putValueInternal(key, value);
    }

    private Object putValueInternal(String key, Object value) {
        PvVarDefinition def = definitions.get(key);
        Object previous = values.put(key, encodeNullValue(value, def));
        return decodeNullValue(previous);
    }

    private Object encodeNullValue(Object value, PvVarDefinition def) {
        if (value != null) {
            return value;
        }
        if (def != null && def.getType() == PvVarDefinition.Type.NUMBER) {
            return NULL_VALUE_NUMBER;
        }
        return NULL_VALUE_STRING;
    }

    private Object decodeNullValue(Object value) {
        if (value == NULL_VALUE_NUMBER || value == NULL_VALUE_STRING) {
            return null;
        }
        return value;
    }
}
