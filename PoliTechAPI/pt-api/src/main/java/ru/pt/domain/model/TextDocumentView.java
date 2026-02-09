package ru.pt.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.function.Function;
import java.util.regex.*;
//import ru.pt.domain.model.VariableContext;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import ru.pt.api.service.db.ReferenceDataService;

@Service
@RequiredArgsConstructor
public final class TextDocumentView implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(TextDocumentView.class);
    private final ReferenceDataService referenceDataService;
    //private final Map<String, Object> context = new HashMap<>();
    private final Map<String, Function<Object, String>> filters = new HashMap<>();

    /**
     * Initialize filters after ReferenceDataService is injected.
     * Called by Spring after bean construction.
     */
    @Override
    public void afterPropertiesSet() {
        registerDefaultFilters();
        List<String> list = referenceDataService.getAllRefs();
        list.forEach(ref -> addRefFilter(ref));
    }

    public String get(VariableContext ctx, String key){
        PvVarDefinition def = ctx.getDefinition(key);
        if ( def == null ) {
            LOGGER.trace("No definition found for key: {}", key);
            return key + " Not Found";
        }
        if ( def.getSourceType() != PvVarDefinition.VarSourceType.TEXT ) { return ctx.getString(key); }

        String template = def.getTemplate();
        String result = this.render(ctx, template);
        return result;
    }

    public String getOrDefault(VariableContext ctx, String key, String ifNull){
        PvVarDefinition def = ctx.getDefinition(key);
        if ( def == null ) { return this.render(ctx, ifNull);}
        
        if ( def.getSourceType() != PvVarDefinition.VarSourceType.TEXT ) { return ctx.getString(key); }
        
        return "";
    }

    
    /**
     * Регистрация стандартных фильтров
     */
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
        filters.put("dd.MM.yyyy", obj -> { return formatDate(obj, "dd.MM.yyyy");});
        filters.put("dd MMMM yyyy", obj -> { return formatDate(obj, "dd MMMM yyyy");});

        filters.put("money", obj -> { return formatMoney(obj);});

    }
        
    private void addRefFilter(String attributeCode) {
        List<String> list = referenceDataService.getAllRefs();
        if (!list.contains(attributeCode)) { return;}

        filters.put( attributeCode, obj -> {
            return iAmRefFilter(attributeCode, obj.toString());
        });
    }

    private String iAmRefFilter(String attributeCode, String code) {
        return referenceDataService.getName(attributeCode, code);
    }
    
    /**
     * Добавление фильтра
     */
    public void addFilter(String name, Function<Object, String> filter) {
        filters.put(name, filter);
    }
    
    /**
     * Рендеринг шаблона
     */
    public String render(VariableContext ctx, String template) {
        LOGGER.trace("Render template with length: {}", template != null ? template.length() : 0);
        String result = template;
        
        // 1. Обработка условий
        result = processConditions(ctx, result);
        
        // 2. Обработка циклов
        //result = processLoops(ctx, result);
        
        // 3. Замена переменных
        result = processVariables(ctx, result);
        
        return result;
    }
    
    /**
     * Обработка условий: {{#if var}}...{{/if}}
     */
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
    
    /**
     * Обработка циклов: {{#each list}}...{{/each}}
     */
    /* 
    private String processLoops(String input) {
        Pattern pattern = Pattern.compile("\\{\\{#each\\s+([^}]+)\\}\\}(.*?)\\{\\{/each\\}\\}", 
            Pattern.DOTALL);
        Matcher matcher = pattern.matcher(input);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String listName = matcher.group(1).trim();
            String loopBody = matcher.group(2);
            
            Object listObj = ctx.get(listName);
            StringBuilder loopResult = new StringBuilder();
            
            if (listObj instanceof Iterable) {
                int index = 0;
                for (Object item : (Iterable<?>) listObj) {
                    // Создаем контекст для элемента цикла
                    Map<String, Object> itemContext = new HashMap<>(context);
                    itemContext.put("this", item);
                    itemContext.put("index", index);
                    itemContext.put("first", index == 0);
                    itemContext.put("last", false); // установим позже
                    
                    // Рендерим тело цикла с контекстом элемента
                    String itemResult = new AdvancedTemplate(loopBody)
                        .setAll(itemContext)
                        .processVariables();
                    
                    loopResult.append(itemResult);
                    index++;
                }
            }
            
            matcher.appendReplacement(result, loopResult.toString());
        }
        
        matcher.appendTail(result);
        return result.toString();
    }
    */
    /**
     * Обработка переменных: {{var}} или {{var|filter}}
     */
    private String processVariables(VariableContext ctx, String input) {
        Pattern pattern = Pattern.compile("\\{\\{([^|{}]+)(?:\\|([^}]+))?\\}\\}");
        Matcher matcher = pattern.matcher(input);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String varName = matcher.group(1).trim();
            String filterName = matcher.group(2);
            
            //Object value = getValueFromContext(ctx, varName);
            Object value = get(ctx, varName);
            String replacement = value != null ? value.toString() : "";
            
            // Применяем фильтр если есть
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
    
    /**
     * Получение значения из контекста (поддержка вложенных свойств)
     */
    private Object getValueFromContext(VariableContext ctx, String path) {
        //String[] parts = path.split("\\.");
        //Object current = ctx;
        
        //for (String part : parts) {
            return ctx.get(path);
        //    if (current == null) return null;
        //}
        
        // return current;
    }
    
    /**
     * Вычисление условия
     */
    private boolean evaluateCondition(VariableContext ctx, String condition) {
        if (condition.startsWith("!")) {
            return !evaluateCondition(ctx, condition.substring(1));
        }
        
        Object value = getValueFromContext(ctx, condition);
        
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
    
    /* 
    private AdvancedTemplate setAll(Map<String, Object> vars) {
        context.putAll(vars);
        return this;
    }
        */

// Добавить форматер для дат для отчетов
    private String formatDate(Object obj, String format) {
        if (obj == null) return "";
            
        if (obj instanceof String) {
            String stringObj = (String) obj;
            if ( stringObj.length() < 10 ) { return "";}
            stringObj = stringObj.substring(0,10);
            if ( stringObj.length() != 10) { return ""; };

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
        
        // Округляем до 2 знаков после запятой
        value = value.setScale(2, RoundingMode.HALF_UP);
        
        // Форматируем
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        symbols.setGroupingSeparator(' ');
        symbols.setDecimalSeparator('.');
        
        DecimalFormat formatter = new DecimalFormat("#,##0.00", symbols);
        return formatter.format(value);
    }
}
 

