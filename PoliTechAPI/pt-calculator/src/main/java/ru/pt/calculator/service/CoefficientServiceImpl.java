package ru.pt.calculator.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.pt.api.dto.calculator.CoefficientColumn;
import ru.pt.api.service.calculator.CoefficientService;
import ru.pt.calculator.entity.CoefficientDataEntity;
import ru.pt.calculator.repository.CoefficientDataRepository;
import ru.pt.auth.security.SecurityContextHelper;
import ru.pt.auth.security.UserDetailsImpl;
import ru.pt.api.dto.exception.BadRequestException;

import java.util.List;
import java.util.Map;

@Component
public class CoefficientServiceImpl implements CoefficientService {

    private static final Logger logger = LoggerFactory.getLogger(CoefficientServiceImpl.class);

    private final CoefficientDataRepository repository;
    private final ObjectMapper objectMapper;
    private final JdbcTemplate jdbcTemplate;
    private final SecurityContextHelper securityContextHelper;

    public CoefficientServiceImpl(CoefficientDataRepository repository, ObjectMapper objectMapper, JdbcTemplate jdbcTemplate, SecurityContextHelper securityContextHelper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
        this.jdbcTemplate = jdbcTemplate;
        this.securityContextHelper = securityContextHelper;
        }

    protected UserDetailsImpl getCurrentUser() {
        return securityContextHelper.getCurrentUser()
                .orElseThrow(() -> new BadRequestException("Unable to get current user from context"));
    }

    protected Long getCurrentTenantId() {
        return getCurrentUser().getTenantId();
    }

    @Transactional
    public CoefficientDataEntity insert(Integer calculatorId, String code, JsonNode coefficientDataJson) {
        logger.debug("Inserting coefficient data: calculatorId={}, code={}", calculatorId, code);
        CoefficientDataEntity entity = new CoefficientDataEntity();
        entity.setCalculatorId(calculatorId);
        entity.setCoefficientCode(code);
        entity.setTId(getCurrentTenantId());
        mapFromJson(entity, coefficientDataJson);
        CoefficientDataEntity saved = repository.save(entity);
        logger.info("Coefficient data inserted: id={}", saved.getId());
        return saved;
    }

    @Transactional
    public CoefficientDataEntity update(Integer id, JsonNode coefficientDataJson) {
        logger.debug("Updating coefficient data: id={}", id);
        CoefficientDataEntity entity = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Coefficient row not found: " + id));
        mapFromJson(entity, coefficientDataJson);
        CoefficientDataEntity updated = repository.save(entity);
        logger.info("Coefficient data updated: id={}", id);
        return updated;
    }

    @Transactional
    public void delete(Integer id) {
        logger.info("Deleting coefficient data: id={}", id);
        repository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public ArrayNode getTable(Integer calculatorId, String code) {
        logger.debug("Getting coefficient table: calculatorId={}, code={}", calculatorId, code);
        List<CoefficientDataEntity> rows = repository.findAllByCalcAndCode(getCurrentTenantId(), calculatorId, code);
        ArrayNode data = objectMapper.createArrayNode();
        for (CoefficientDataEntity e : rows) {
            data.add(mapToJson(e));
        }
        logger.trace("Retrieved {} coefficient rows", rows.size());
        return data;
    }

    @Transactional
    public ArrayNode replaceTable(Integer calculatorId, String code, ArrayNode tableJson) {
        logger.info("Replacing coefficient table: calculatorId={}, code={}, rows={}", calculatorId, code, tableJson.size());
        repository.deleteAllByCalcAndCode(getCurrentTenantId(), calculatorId, code);
        logger.debug("Deleted existing coefficient data");
        
        //ArrayNode data = (tableJson.has("data") && tableJson.get("data").isArray()) ? (ArrayNode) tableJson.get("data") : objectMapper.createArrayNode();
        for (JsonNode row : tableJson) {
            insert(calculatorId, code, row);
        }
        logger.info("Inserted {} new coefficient rows", tableJson.size());
        return getTable(calculatorId, code);
    }

    @Transactional(readOnly = true)
    public String getCoefficientValue(Integer calculatorId,
                                      String coefficientCode,
                                      Map<String, Object> values,
                                      List<CoefficientColumn> columns) {
        logger.debug("Getting coefficient value: calculatorId={}, coefficientCode={}", calculatorId, coefficientCode);
        
        if (calculatorId == null || coefficientCode == null || columns == null) {
            logger.warn("Invalid parameters: calculatorId={}, coefficientCode={}, columns={}", 
                    calculatorId, coefficientCode, columns != null ? "present" : "null");
            return null;
        }

        StringBuilder sql = new StringBuilder("select result_value from coefficient_data where calculator_id = ");
        sql.append(calculatorId.toString());
        sql.append(" and coefficient_code = ");
        sql.append("'").append(coefficientCode).append("'");

        StringBuilder orderBy = new StringBuilder();
        java.util.List<Object> params = new java.util.ArrayList<>();
        params.add(calculatorId.toString());
        params.add(coefficientCode);

        for (CoefficientColumn col : columns) {
            if (col == null) continue;
            String varCode = col.getVarCode();
            String nr = (col.getNr() - 1) + "";  // TODO
            String op = col.getConditionOperator();
            String sortOrder = col.getSortOrder();
            String varDataType = col.getVarDataType();

            if (varCode == null || nr == null || op == null) return null;
            if (!nr.matches("1?0|[0-9]")) return null; // only 0..10

            String varValue = values != null ? values.get(varCode).toString() : null;
            if (varValue == null) return null;

            String operator = normalizeOperator(op);
            if (operator == null) return null;

            if (varDataType.equals("NUMBER")) {
                sql.append(" AND to_number(col").append(nr).append(",'9999999999.99') ").append(operator).append(varValue);
            } else {
                sql.append(" AND col").append(nr).append(" ").append(operator).append("'").append(varValue).append("'");
            }
            params.add(varValue);

            String ord = normalizeOrder(sortOrder);
            if (ord != null) {
                if (orderBy.length() == 0) orderBy.append(" order by ");
                else orderBy.append(", ");
                if (varDataType.equals("NUMBER")) {
                    orderBy.append("to_number(col").append(nr).append(",'9999999999.99') ");
                } else {
                    orderBy.append("col").append(nr).append(" ");
                }
                orderBy.append(" ").append(ord);
            }
        }

        if (orderBy.length() > 0) sql.append(orderBy);
        sql.append(" limit 1");
        String sqlS = sql.toString();
        
        logger.trace("Executing coefficient query: {}", sqlS);

        try {
            Double result = jdbcTemplate.query(sqlS, rs -> rs.next() ? rs.getDouble(1) : null);
            logger.debug("Coefficient value retrieved: {}={}", coefficientCode, result);
            return result == null ? null : String.valueOf(result);
        } catch (Exception e) {
            logger.error("Failed to query coefficient: {}", e.getMessage(), e);
            return null;
        }
    }

    private String normalizeOperator(String op) {
        if (op == null) return null;
        String s = op.trim().toUpperCase();
        return switch (s) {
            case "=", ">", "<", ">=", "<=", "<>" -> s;
            case "LIKE" -> "LIKE";
            default -> null;
        };
    }

    private String normalizeOrder(String order) {
        if (order == null) return null;
        String s = order.trim().toUpperCase();
        return switch (s) {
            case "ASC", "DESC" -> s;
            default -> null;
        };
    }

    private void mapFromJson(CoefficientDataEntity entity, JsonNode json) {
        ArrayNode condition = (json.has("conditionValue") && json.get("conditionValue").isArray()) ? (ArrayNode) json.get("conditionValue") : objectMapper.createArrayNode();
        entity.setCol0(condition.size() > 0 ? condition.get(0).asText(null) : null);
        entity.setCol1(condition.size() > 1 ? condition.get(1).asText(null) : null);
        entity.setCol2(condition.size() > 2 ? condition.get(2).asText(null) : null);
        entity.setCol3(condition.size() > 3 ? condition.get(3).asText(null) : null);
        entity.setCol4(condition.size() > 4 ? condition.get(4).asText(null) : null);
        entity.setCol5(condition.size() > 5 ? condition.get(5).asText(null) : null);
        entity.setCol6(condition.size() > 6 ? condition.get(6).asText(null) : null);
        entity.setCol7(condition.size() > 7 ? condition.get(7).asText(null) : null);
        entity.setCol8(condition.size() > 8 ? condition.get(8).asText(null) : null);
        entity.setCol9(condition.size() > 9 ? condition.get(9).asText(null) : null);
        entity.setCol10(condition.size() > 10 ? condition.get(10).asText(null) : null);
        if (json.has("resultValue") && !json.get("resultValue").isNull()) {
            entity.setResultValue(json.get("resultValue").asDouble());
        } else {
            entity.setResultValue(null);
        }
    }

    private ObjectNode mapToJson(CoefficientDataEntity entity) {
        ObjectNode row = objectMapper.createObjectNode();
        row.put("id", entity.getId());
        ArrayNode cond = objectMapper.createArrayNode();
        cond.add(entity.getCol0());
        cond.add(entity.getCol1());
        cond.add(entity.getCol2());
        cond.add(entity.getCol3());
        cond.add(entity.getCol4());
        cond.add(entity.getCol5());
        cond.add(entity.getCol6());
        cond.add(entity.getCol7());
        cond.add(entity.getCol8());
        cond.add(entity.getCol9());
        cond.add(entity.getCol10());
        row.set("conditionValue", cond);
        if (entity.getResultValue() != null) row.put("resultValue", entity.getResultValue());
        return row;
    }

}
