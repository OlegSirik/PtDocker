package ru.pt.rules.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.pt.api.dto.rules.RuleType;
import ru.pt.api.dto.rules.RuleValidationContext;
import ru.pt.api.service.rules.RuleValidationService;
import ru.pt.rules.entity.RuleEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class RuleValidationServiceImpl implements RuleValidationService {

    private static final Logger logger = LoggerFactory.getLogger(RuleValidationServiceImpl.class);

    private final RuleCache ruleCache;
    private final CelRuleEngine celRuleEngine;

    public RuleValidationServiceImpl(
            RuleCache ruleCache,
            CelRuleEngine celRuleEngine) {
        this.ruleCache = ruleCache;
        this.celRuleEngine = celRuleEngine;
    }

    @Override
    public List<String> processValidation(RuleType ruleType, RuleValidationContext context) {
        logger.info(
                "processValidation entered. ruleType={}, tid={}",
                ruleType,
                context != null ? context.getTid() : null);
        if (ruleType == null || !ruleType.isValidation()) {
            logger.debug("Skipping validation: ruleType is null or not a validation type ({})", ruleType);
            return List.of();
        }
        if (context == null || context.getTid() == null) {
            logger.warn("Skipping validation: context or tid is null. ruleType={}", ruleType);
            return List.of();
        }

        logger.debug(
                "Starting rule validation. ruleType={}, tid={}, productCode={}, lobCode={}, tenantCode={}, clientId={}, variableCount={}",
                ruleType,
                context.getTid(),
                context.getProductCode(),
                context.getLobCode(),
                context.getTenantCode(),
                context.getClientId(),
                context.getVariables() != null ? context.getVariables().size() : 0);

        List<String> messages = new ArrayList<>();
        Map<String, Object> variables = context.getVariables() != null
                ? context.getVariables()
                : Map.of();

        executeLayer(context, ruleType, "PRODUCT", context.getProductCode(), variables, messages);
        if (context.getLobCode() != null && !context.getLobCode().isBlank()) {
            executeLayer(context, ruleType, "LOB", context.getLobCode(), variables, messages);
        }
        if (context.getTenantCode() != null && !context.getTenantCode().isBlank()) {
            executeLayer(context, ruleType, "TENANT", context.getTenantCode(), variables, messages);
        }
        if (context.getClientId() != null && !context.getClientId().isBlank()) {
            executeLayer(context, ruleType, "CLIENT", context.getClientId(), variables, messages);
        }

        if (messages.isEmpty()) {
            logger.debug(
                    "Rule validation completed with no violations. ruleType={}, tid={}, productCode={}",
                    ruleType,
                    context.getTid(),
                    context.getProductCode());
        } else {
            logger.warn(
                    "Rule validation completed with violations. ruleType={}, tid={}, productCode={}, violationCount={}",
                    ruleType,
                    context.getTid(),
                    context.getProductCode(),
                    messages.size());
        }
        return messages;
    }

    @Override
    public void reloadCache() {
        logger.info("Reloading rules cache");
        ruleCache.reloadAll();
        logger.debug("Rules cache reload finished");
    }

    private void executeLayer(
            RuleValidationContext context,
            RuleType ruleType,
            String scopeType,
            String scopeCode,
            Map<String, Object> variables,
            List<String> messages) {
        if (scopeCode == null || scopeCode.isBlank()) {
            logger.debug(
                    "Skipping rule layer: scopeCode is blank. ruleType={}, scopeType={}, tid={}",
                    ruleType,
                    scopeType,
                    context.getTid());
            return;
        }

        List<RuleEntity> rules = ruleCache.findActive(
                context.getTid(),
                ruleType.name(),
                scopeType,
                scopeCode);

        logger.debug(
                "Executing rule layer. ruleType={}, scopeType={}, scopeCode={}, tid={}, ruleCount={}",
                ruleType,
                scopeType,
                scopeCode,
                context.getTid(),
                rules.size());

        for (RuleEntity rule : rules) {
            evaluateRule(rule, variables, messages);
        }
    }

    private void evaluateRule(RuleEntity rule, Map<String, Object> variables, List<String> messages) {
        logger.debug("Evaluating rule. code={}, priority={}", rule.getCode(), rule.getPriority());
        try {
            boolean ok = celRuleEngine.evaluate(rule.getExpression(), variables);
            if (!ok) {
                logger.warn(
                        "Rule violated. code={}, message={}",
                        rule.getCode(),
                        rule.getMessage());
                messages.add(rule.getMessage());
            } else {
                logger.debug("Rule passed. code={}", rule.getCode());
            }
        } catch (Exception ex) {
            logger.warn(
                    "CEL evaluation failed for rule {}: {}",
                    rule.getCode(),
                    ex.getMessage(),
                    ex);
            messages.add("[" + rule.getCode() + "] " + formatCelError(ex));
        }
    }

    private static String formatCelError(Exception ex) {
        String msg = ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName();
        if (msg.contains("invalid argument to has()")) {
            return "has() в CEL только для вложенных полей (msg.field), не для varCode верхнего уровня. "
                    + "Используйте: pl_policyNumber != null && pl_policyNumber != \"\"";
        }
        if (msg.contains("undeclared reference to 'has'")) {
            return "Макрос has() не включён — пересоберите backend; для varCode используйте сравнение с null/\"\"";
        }
        if (msg.contains("less_int64") || msg.contains("No matching overload")) {
            return "Ошибка сравнения: переменная должна быть числом (MAGIC-поля вроде io_age_issue — строка, движок приводит к числу)";
        }
        return "Ошибка выполнения правила: " + msg;
    }

}
