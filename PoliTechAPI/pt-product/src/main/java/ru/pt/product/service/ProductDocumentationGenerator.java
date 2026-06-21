package ru.pt.product.service;

import org.springframework.stereotype.Component;
import ru.pt.api.dto.numbers.NumberGeneratorDescription;
import ru.pt.api.dto.product.LobCover;
import ru.pt.api.dto.product.LobModel;
import ru.pt.api.dto.product.PeriodRule;
import ru.pt.api.dto.product.ProductVersionModel;
import ru.pt.api.dto.product.PvCover;
import ru.pt.api.dto.product.PvDeductible;
import ru.pt.api.dto.product.PvPackage;
import ru.pt.api.dto.product.PvProductRules;
import ru.pt.api.dto.product.PvVar;
import ru.pt.api.dto.product.ValidatorRule;
import ru.pt.api.dto.rules.RuleDto;
import ru.pt.api.service.product.LobService;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class ProductDocumentationGenerator {

    private static final DateTimeFormatter GENERATED_AT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");

    private final LobService lobService;

    public ProductDocumentationGenerator(LobService lobService) {
        this.lobService = lobService;
    }

    public String generate(ProductVersionModel product, Long tenantId) {
        Map<String, LobCover> lobCovers = resolveLobCovers(tenantId, product.getLob());
        StringBuilder md = new StringBuilder();
        appendLine(md, "# " + safe(product.getName()));
        appendLine(md, "");
        appendLine(md, "_Сгенерировано: " + ZonedDateTime.now().format(GENERATED_AT) + "_");
        appendLine(md, "");
        appendGeneralInfo(md, product);
        appendPeriodRules(md, product);
        appendProductRules(md, product);
        appendValidators(md, "Проверки котировки (QUOTE)", product.getQuoteValidator());
        appendValidators(md, "Проверки сохранения (SAVE)", product.getSaveValidator());
        appendCelRules(md, product.getCelRules());
        appendSchema(md, product.getVars());
        appendPackages(md, product.getPackages(), lobCovers);
        appendNumberGenerator(md, product.getNumberGeneratorDescription());
        return md.toString().trim() + "\n";
    }

    private void appendGeneralInfo(StringBuilder md, ProductVersionModel product) {
        appendLine(md, "## Общая информация");
        appendLine(md, "");
        appendTableRow(md, "Код продукта", product.getCode());
        appendTableRow(md, "Линия бизнеса (LOB)", product.getLob());
        appendTableRow(md, "Версия", product.getVersionNo() != null ? String.valueOf(product.getVersionNo()) : null);
        appendTableRow(md, "Статус версии", product.getVersionStatus());
        appendTableRow(md, "Тип страхователя (phType)", product.getPhType());
        appendTableRow(md, "Тип объекта (ioType)", product.getIoType());
        appendLine(md, "");
    }

    private void appendPeriodRules(StringBuilder md, ProductVersionModel product) {
        appendLine(md, "## Сроки");
        appendLine(md, "");
        appendPeriodRule(md, "Период ожидания (waitingPeriod)", product.getWaitingPeriod());
        appendPeriodRule(md, "Срок полиса (policyTerm)", product.getPolicyTerm());
        appendLine(md, "");
    }

    private void appendPeriodRule(StringBuilder md, String title, PeriodRule rule) {
        appendLine(md, "### " + title);
        if (rule == null) {
            appendLine(md, "");
            appendLine(md, "_Не задано_");
            appendLine(md, "");
            return;
        }
        appendLine(md, "");
        appendTableRow(md, "Тип", rule.getValidatorType());
        appendTableRow(md, "Значение", rule.getValidatorValue());
        appendLine(md, "");
    }

    private void appendProductRules(StringBuilder md, ProductVersionModel product) {
        PvProductRules rules = product.getRules();
        if (rules == null) {
            return;
        }
        appendLine(md, "## Продуктовые настройки");
        appendLine(md, "");
        appendTableRow(md, "Застрахованный = страхователь", rules.isInsuredEqualsPolicyHolder() ? "да" : "нет");
        appendLine(md, "");
    }

    private void appendValidators(StringBuilder md, String title, List<ValidatorRule> validators) {
        appendLine(md, "## " + title);
        appendLine(md, "");
        if (validators == null || validators.isEmpty()) {
            appendLine(md, "_Нет правил_");
            appendLine(md, "");
            return;
        }
        appendLine(md, "| № | Левый ключ | Тип | Правый ключ | Значение | Текст ошибки |");
        appendLine(md, "|---|------------|-----|-------------|----------|--------------|");
        validators.stream()
                .sorted(Comparator.comparing(v -> v.getLineNr() != null ? v.getLineNr() : 0))
                .forEach(v -> appendLine(md, String.format(
                        "| %s | %s | %s | %s | %s | %s |",
                        cell(v.getLineNr()),
                        cell(v.getKeyLeft()),
                        cell(v.getRuleType()),
                        cell(v.getKeyRight()),
                        cell(v.getValueRight()),
                        cell(v.getErrorText()))));
        appendLine(md, "");
    }

    private void appendCelRules(StringBuilder md, List<RuleDto> rules) {
        appendLine(md, "## CEL-правила");
        appendLine(md, "");
        if (rules == null || rules.isEmpty()) {
            appendLine(md, "_Нет правил_");
            appendLine(md, "");
            return;
        }
        Map<String, List<RuleDto>> byType = rules.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(r -> r.getRuleType() != null ? r.getRuleType().name() : "OTHER"));
        byType.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    appendLine(md, "### " + entry.getKey());
                    appendLine(md, "");
                    for (RuleDto rule : entry.getValue()) {
                        appendLine(md, "#### " + safe(rule.getCode()) + " — " + safe(rule.getName()));
                        appendLine(md, "");
                        appendTableRow(md, "Приоритет", rule.getPriority() != null ? String.valueOf(rule.getPriority()) : null);
                        appendTableRow(md, "Статус", rule.getRecordStatus());
                        appendTableRow(md, "Scope", rule.getScopeType() != null ? rule.getScopeType().name() : null);
                        appendTableRow(md, "Scope code", rule.getScopeCode());
                        if (rule.getExpression() != null && !rule.getExpression().isBlank()) {
                            appendLine(md, "");
                            appendLine(md, "**Условие:**");
                            appendLine(md, "");
                            appendLine(md, "```");
                            appendLine(md, rule.getExpression().trim());
                            appendLine(md, "```");
                        }
                        if (rule.getMessage() != null && !rule.getMessage().isBlank()) {
                            appendLine(md, "");
                            appendTableRow(md, "Сообщение", rule.getMessage());
                        }
                        appendLine(md, "");
                    }
                });
    }

    private void appendSchema(StringBuilder md, List<PvVar> vars) {
        appendLine(md, "## Схема договора (переменные)");
        appendLine(md, "");
        if (vars == null || vars.isEmpty()) {
            appendLine(md, "_Нет переменных_");
            appendLine(md, "");
            return;
        }
        List<PvVar> active = vars.stream()
                .filter(v -> v != null && !v.getIsDeleted())
                .sorted(Comparator.comparing(v -> v.getVarNr() != null ? v.getVarNr() : ""))
                .toList();
        appendLine(md, "| Код | Наименование | Тип | Тип данных | Путь | CDM | Тарифный фактор |");
        appendLine(md, "|-----|--------------|-----|------------|------|-----|-----------------|");
        for (PvVar var : active) {
            appendLine(md, String.format(
                    "| %s | %s | %s | %s | %s | %s | %s |",
                    cell(var.getVarCode()),
                    cell(firstNonBlank(var.getVarName(), var.getName())),
                    cell(var.getVarType()),
                    cell(var.getVarDataType() != null ? var.getVarDataType().name() : null),
                    cell(var.getVarPath()),
                    cell(var.getVarCdm()),
                    var.getIsTarifFactor() ? "да" : "нет"));
        }
        appendLine(md, "");
    }

    private void appendPackages(StringBuilder md, List<PvPackage> packages, Map<String, LobCover> lobCovers) {
        appendLine(md, "## Пакеты и покрытия");
        appendLine(md, "");
        if (packages == null || packages.isEmpty()) {
            appendLine(md, "_Нет пакетов_");
            appendLine(md, "");
            return;
        }
        for (PvPackage pkg : packages) {
            appendLine(md, "### Пакет " + safe(pkg.getCode()) + " — " + safe(pkg.getName()));
            appendLine(md, "");
            appendTableRow(md, "ID калькулятора", pkg.getCalculatorId() != null ? String.valueOf(pkg.getCalculatorId()) : null);
            appendLine(md, "");
            List<PvCover> covers = pkg.getCovers() != null ? pkg.getCovers() : List.of();
            if (covers.isEmpty()) {
                appendLine(md, "_Нет покрытий_");
                appendLine(md, "");
                continue;
            }
            appendLine(md, "| Код | Название | Обязательность | Период исключения | Период страхования | Франшиза обязательна | Список рисков | Список франшиз |");
            appendLine(md, "|-----|----------|----------------|-------------------|--------------------|----------------------|---------------|----------------|");
            for (PvCover cover : covers) {
                LobCover lobCover = lobCovers.get(cover.getCode());
                appendLine(md, String.format(
                        "| %s | %s | %s | %s | %s | %s | %s | %s |",
                        cell(cover.getCode()),
                        cell(lobCover != null ? lobCover.getCoverName() : cover.getCode()),
                        bool(cover.getIsMandatory()),
                        cell(cover.getWaitingPeriod()),
                        cell(cover.getCoverageTerm()),
                        bool(cover.getIsDeductibleMandatory()),
                        cell(lobCover != null ? lobCover.getRisks() : null),
                        cell(formatDeductiblesList(cover.getDeductibles()))));
            }
            appendLine(md, "");
        }
    }

    private Map<String, LobCover> resolveLobCovers(Long tenantId, String lobCode) {
        if (tenantId == null || lobCode == null || lobCode.isBlank()) {
            return Map.of();
        }
        LobModel lob = lobService.getByCode(tenantId, lobCode);
        if (lob == null || lob.getMpCovers() == null) {
            return Map.of();
        }
        Map<String, LobCover> indexed = new HashMap<>();
        for (LobCover cover : lob.getMpCovers()) {
            if (cover != null && cover.getCoverCode() != null && !cover.getCoverCode().isBlank()) {
                indexed.putIfAbsent(cover.getCoverCode(), cover);
            }
        }
        return indexed;
    }

    private static String formatDeductiblesList(List<PvDeductible> deductibles) {
        if (deductibles == null || deductibles.isEmpty()) {
            return "";
        }
        return deductibles.stream()
                .filter(Objects::nonNull)
                .map(ProductDocumentationGenerator::formatDeductible)
                .filter(s -> !s.isBlank())
                .collect(Collectors.joining("; "));
    }

    private static String formatDeductible(PvDeductible deductible) {
        String text = deductible.getText();
        if (text != null && !text.isBlank()) {
            return text.trim();
        }
        if (deductible.getDeductible() != null) {
            return String.valueOf(deductible.getDeductible());
        }
        return "";
    }

    private void appendNumberGenerator(StringBuilder md, NumberGeneratorDescription generator) {
        appendLine(md, "## Генератор номера полиса");
        appendLine(md, "");
        if (generator == null) {
            appendLine(md, "_Не задан_");
            appendLine(md, "");
            return;
        }
        appendTableRow(md, "Маска", generator.getMask());
        appendTableRow(md, "Макс. значение", generator.getMaxValue() != null ? String.valueOf(generator.getMaxValue()) : null);
        appendTableRow(md, "Сброс", generator.getResetPolicy() != null ? generator.getResetPolicy().name() : null);
        appendTableRow(md, "XOR-маска", generator.getXorMask());
        appendLine(md, "");
    }

    private static void appendTableRow(StringBuilder md, String label, String value) {
        appendLine(md, "- **" + label + ":** " + (value != null && !value.isBlank() ? value : "—"));
    }

    private static void appendLine(StringBuilder md, String line) {
        md.append(line).append('\n');
    }

    private static String cell(Object value) {
        if (value == null) {
            return "";
        }
        return String.valueOf(value).replace("|", "\\|").replace("\n", " ");
    }

    private static String safe(String value) {
        return value != null ? value : "";
    }

    private static String bool(Boolean value) {
        if (value == null) {
            return "—";
        }
        return value ? "да" : "нет";
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }
}
