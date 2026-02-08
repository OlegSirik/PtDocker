package ru.pt.calculator.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.pt.api.dto.calculator.CoefficientColumn;
import ru.pt.api.dto.calculator.CoefficientDataRow;
import ru.pt.api.service.calculator.CoefficientService;
import ru.pt.calculator.entity.CoefficientDataEntity;
import ru.pt.calculator.repository.CoefficientDataRepository;
import ru.pt.domain.model.VariableContext;
import ru.pt.api.security.AuthenticatedUser;
import ru.pt.auth.security.SecurityContextHelper;
import ru.pt.api.dto.exception.BadRequestException;

import java.util.ArrayList;
import java.util.List;

@Component
public class CoefficientServiceImpl implements CoefficientService {

    private static final Logger logger = LoggerFactory.getLogger(CoefficientServiceImpl.class);

    private final CoefficientDataRepository repository;
    private final JdbcTemplate jdbcTemplate;
    private final SecurityContextHelper securityContextHelper;

    public CoefficientServiceImpl(CoefficientDataRepository repository, JdbcTemplate jdbcTemplate, SecurityContextHelper securityContextHelper) {
        this.repository = repository;
        this.jdbcTemplate = jdbcTemplate;
        this.securityContextHelper = securityContextHelper;
        }

    protected AuthenticatedUser getCurrentUser() {
        return securityContextHelper.getAuthenticatedUser()
                .orElseThrow(() -> new BadRequestException("Unable to get current user from context"));
    }

    protected Long getCurrentTenantId() {
        return getCurrentUser().getTenantId();
    }

    @Transactional
    public CoefficientDataEntity insert(Integer calculatorId, String code, CoefficientDataRow row) {
        logger.debug("Inserting coefficient data: calculatorId={}, code={}", calculatorId, code);
        CoefficientDataEntity entity = new CoefficientDataEntity();
        entity.setCalculatorId(calculatorId);
        entity.setCoefficientCode(code);
        entity.setTId(getCurrentTenantId());
        mapFromRow(entity, row);
        CoefficientDataEntity saved = repository.save(entity);
        logger.info("Coefficient data inserted: id={}", saved.getId());
        return saved;
    }

    @Transactional
    public CoefficientDataEntity update(Integer id, CoefficientDataRow row) {
        logger.debug("Updating coefficient data: id={}", id);
        CoefficientDataEntity entity = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Coefficient row not found: " + id));
        mapFromRow(entity, row);
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
    @Override
    public List<CoefficientDataRow> getTable(Integer calculatorId, String code) {
        logger.debug("Getting coefficient table: calculatorId={}, code={}", calculatorId, code);
        List<CoefficientDataEntity> rows = repository.findAllByCalcAndCode(getCurrentTenantId(), calculatorId, code);
        List<CoefficientDataRow> data = new ArrayList<>();
        for (CoefficientDataEntity e : rows) {
            data.add(mapToRow(e));
        }
        logger.trace("Retrieved {} coefficient rows", rows.size());
        return data;
    }

    @Transactional
    @Override
    public List<CoefficientDataRow> replaceTable(Integer calculatorId, String code, List<CoefficientDataRow> tableJson) {
        logger.info("Replacing coefficient table: calculatorId={}, code={}, rows={}", calculatorId, code, tableJson.size());
        repository.deleteAllByCalcAndCode(getCurrentTenantId(), calculatorId, code);
        logger.debug("Deleted existing coefficient data");
        
        for (CoefficientDataRow row : tableJson) {
            insert(calculatorId, code, row);
        }
        logger.info("Inserted {} new coefficient rows", tableJson.size());
        return getTable(calculatorId, code);
    }

    @Transactional(readOnly = true)
    @Override
    public String getCoefficientValue(Integer calculatorId,
                                      String coefficientCode,
                                      VariableContext values,
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

    @Transactional
    @Override
    public int copyCoefficient(Integer calculatorIdFrom, Integer calculatorIdTo, String coefficientCode) {
        logger.info("Copying coefficient: from={}, to={}, code={}", calculatorIdFrom, calculatorIdTo, coefficientCode);

        if (calculatorIdFrom == null || calculatorIdTo == null || coefficientCode == null) {
            throw new IllegalArgumentException("calculatorIdFrom, calculatorIdTo and coefficientCode are required");
        }

        Long tid = getCurrentTenantId();
        String sql = """
            insert into coefficient_data
                (tid, calculator_id, coefficient_code,
                 col0, col1, col2, col3, col4, col5, col6, col7, col8, col9, col10,
                 result_value)
            select
                ?, ?, ?,
                col0, col1, col2, col3, col4, col5, col6, col7, col8, col9, col10,
                result_value
            from coefficient_data
            where tid = ? and calculator_id = ? and coefficient_code = ?
            """;

        int inserted = jdbcTemplate.update(
            sql,
            tid,
            calculatorIdTo,
            coefficientCode,
            tid,
            calculatorIdFrom,
            coefficientCode
        );

        logger.info("Copied {} coefficient rows for code {}", inserted, coefficientCode);
        return inserted;
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

    private void mapFromRow(CoefficientDataEntity entity, CoefficientDataRow row) {
        List<String> condition = row.getConditionValue() != null ? row.getConditionValue() : List.of();
        entity.setCol0(getConditionValue(condition, 0));
        entity.setCol1(getConditionValue(condition, 1));
        entity.setCol2(getConditionValue(condition, 2));
        entity.setCol3(getConditionValue(condition, 3));
        entity.setCol4(getConditionValue(condition, 4));
        entity.setCol5(getConditionValue(condition, 5));
        entity.setCol6(getConditionValue(condition, 6));
        entity.setCol7(getConditionValue(condition, 7));
        entity.setCol8(getConditionValue(condition, 8));
        entity.setCol9(getConditionValue(condition, 9));
        entity.setCol10(getConditionValue(condition, 10));
        entity.setResultValue(row.getResultValue());
    }

    private CoefficientDataRow mapToRow(CoefficientDataEntity entity) {
        CoefficientDataRow row = new CoefficientDataRow();
        row.setId(entity.getId());
        List<String> cond = new ArrayList<>(11);
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
        row.setConditionValue(cond);
        row.setResultValue(entity.getResultValue());
        return row;
    }

    private String getConditionValue(List<String> condition, int index) {
        if (condition == null || index < 0 || index >= condition.size()) {
            return null;
        }
        return condition.get(index);
    }

}
