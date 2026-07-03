package ru.pt.files.template;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ru.pt.api.service.db.ReferenceDataService;
import ru.pt.domain.model.PvVarDefinition;
import ru.pt.domain.model.VariableContext;

@Service
@RequiredArgsConstructor
public final class TextDocumentView {

    private static final Logger LOGGER = LoggerFactory.getLogger(TextDocumentView.class);
    private final ReferenceDataService referenceDataService;
    private final Map<String, Function<Object, String>> filters = new HashMap<>();

    @PostConstruct
    void initFilters() {
        registerDefaultFilters();
        referenceDataService.getAllRefs().forEach(this::addRefFilter);
    }

    public String get(VariableContext ctx, String key) {
        PvVarDefinition def = ctx.getDefinition(key);
        if (def == null) {
            LOGGER.trace("No definition found for key: {}", key);
            return key + " Not Found";
        }
        if (def.getSourceType() != PvVarDefinition.VarSourceType.TEXT) {
            return ctx.getString(key);
        }

        String template = def.getTemplate();
        return render(ctx, template);
    }

    private void registerDefaultFilters() {
        filters.put("upper", obj -> obj.toString().toUpperCase());
        filters.put("lower", obj -> obj.toString().toLowerCase());
        filters.put("capitalize", obj -> {
            String str = obj.toString();
            return str.substring(0, 1).toUpperCase() + str.substring(1);
        });
        filters.put("currency", obj -> {
            if (obj instanceof Number) {
                return String.format("%,.2f руб.", ((Number) obj).doubleValue());
            }
            return obj.toString();
        });
        filters.put("date", obj -> {
            if (obj instanceof Date) {
                return new SimpleDateFormat("dd.MM.yyyy").format((Date) obj);
            }
            return obj.toString();
        });
        filters.put("dd.MM.yyyy", obj -> formatDate(obj, "dd.MM.yyyy"));
        filters.put("dd MMMM yyyy", obj -> formatDate(obj, "dd MMMM yyyy"));
        filters.put("money", TextDocumentView::formatMoney);
    }

    private void addRefFilter(String attributeCode) {
        filters.put(attributeCode, obj -> iAmRefFilter(attributeCode, obj.toString()));
    }

    private String iAmRefFilter(String attributeCode, String code) {
        return referenceDataService.getName(attributeCode, code);
    }

    public void addFilter(String name, Function<Object, String> filter) {
        filters.put(name, filter);
    }

    public String render(VariableContext ctx, String template) {
        LOGGER.trace("Render template with length: {}", template != null ? template.length() : 0);
        String result = template;

        result = processConditions(ctx, result);
        result = processVariables(ctx, result);

        return result;
    }

    private String processConditions(VariableContext ctx, String input) {
        Pattern pattern = Pattern.compile("\\{\\{#if\\s+([^}]+)\\}\\}(.*?)\\{\\{/if\\}\\}",
                Pattern.DOTALL);
        Matcher matcher = pattern.matcher(input);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String condition = matcher.group(1).trim();
            String content = matcher.group(2);

            boolean shouldInclude = evaluateCondition(ctx, condition);

            if (shouldInclude) {
                matcher.appendReplacement(result, content);
            } else {
                matcher.appendReplacement(result, "");
            }
        }

        matcher.appendTail(result);
        return result.toString();
    }

    private String processVariables(VariableContext ctx, String input) {
        Pattern pattern = Pattern.compile("\\{\\{([^|{}]+)(?:\\|([^}]+))?\\}\\}");
        Matcher matcher = pattern.matcher(input);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String varName = matcher.group(1).trim();
            String filterName = matcher.group(2);

            Object value = get(ctx, varName);
            String replacement = value != null ? value.toString() : "";

            if (filterName != null) {
                Function<Object, String> filter = filters.get(filterName.trim());
                if (filter != null) {
                    replacement = filter.apply(value != null ? value : "");
                }
            }

            if (replacement == null) {
                replacement = "";
            }
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }

        matcher.appendTail(result);
        return result.toString();
    }

    private boolean evaluateCondition(VariableContext ctx, String condition) {
        if (condition.startsWith("!")) {
            return !evaluateCondition(ctx, condition.substring(1));
        }

        Object value = ctx.get(condition);

        if (value == null) {
            return false;
        }

        if (value instanceof Boolean) {
            return (Boolean) value;
        }

        if (value instanceof String) {
            return !((String) value).isEmpty();
        }

        if (value instanceof Collection) {
            return !((Collection<?>) value).isEmpty();
        }

        return true;
    }

    private String formatDate(Object obj, String format) {
        if (obj == null) {
            return "";
        }

        if (obj instanceof String stringObj) {
            if (stringObj.length() < 10) {
                return "";
            }
            stringObj = stringObj.substring(0, 10);
            if (stringObj.length() != 10) {
                return "";
            }

            try {
                LocalDate date = LocalDate.parse(stringObj, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                return date.format(DateTimeFormatter.ofPattern(format, Locale.forLanguageTag("ru")));
            } catch (DateTimeParseException e) {
                return "";
            }
        }

        return "";
    }

    public static String formatMoney(Object number) {
        BigDecimal value;

        if (number == null) {
            value = BigDecimal.ZERO;
        } else if (number instanceof BigDecimal) {
            value = (BigDecimal) number;
        } else if (number instanceof Number) {
            value = BigDecimal.valueOf(((Number) number).doubleValue());
        } else if (number instanceof String) {
            try {
                String str = ((String) number).replaceAll("[\\s,]", "");
                value = new BigDecimal(str);
            } catch (Exception e) {
                value = BigDecimal.ZERO;
            }
        } else {
            value = BigDecimal.ZERO;
        }

        value = value.setScale(2, RoundingMode.HALF_UP);

        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        symbols.setGroupingSeparator(' ');
        symbols.setDecimalSeparator('.');

        DecimalFormat formatter = new DecimalFormat("#,##0.00", symbols);
        return formatter.format(value);
    }
}
