package ru.pt.domain.model;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.regex.*;
//import ru.pt.domain.model.VariableContext;


public final class TextDocumentView {

    
    //private final Map<String, Object> context = new HashMap<>();
    private Map<String, Function<Object, String>> filters = new HashMap<>();
    private final VariableContext ctx;

    public TextDocumentView (VariableContext ctx) {
        this.ctx = ctx;
        registerDefaultFilters();
    }

    public String get(String key){
        PvVarDefinition def = ctx.getDefinition(key);
        if ( def == null ) { return key + " Not Found"; }
        if ( def.getSourceType() != PvVarDefinition.VarSourceType.TEXT ) { return ctx.getString(key); }

        String template = def.getTemplate();
        String result = this.render(template);
        return result;
    }

    public String getOrDefault(String key, String ifNull){
        PvVarDefinition def = ctx.getDefinition(key);
        if ( def == null ) { return this.render(ifNull);}
        
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
    public String render(String template) {
        String result = template;
        
        // 1. Обработка условий
        result = processConditions(result);
        
        // 2. Обработка циклов
        //result = processLoops(result);
        
        // 3. Замена переменных
        result = processVariables(result);
        
        return result;
    }
    
    /**
     * Обработка условий: {{#if var}}...{{/if}}
     */
    private String processConditions(String input) {
        Pattern pattern = Pattern.compile("\\{\\{#if\\s+([^}]+)\\}\\}(.*?)\\{\\{/if\\}\\}", 
            Pattern.DOTALL);
        Matcher matcher = pattern.matcher(input);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String condition = matcher.group(1).trim();
            String content = matcher.group(2);
            
            boolean shouldInclude = evaluateCondition(condition);
            
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
    private String processVariables(String input) {
        Pattern pattern = Pattern.compile("\\{\\{([^|{}]+)(?:\\|([^}]+))?\\}\\}");
        Matcher matcher = pattern.matcher(input);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String varName = matcher.group(1).trim();
            String filterName = matcher.group(2);
            
            Object value = getValueFromContext(varName);
            String replacement = value != null ? value.toString() : "";
            
            // Применяем фильтр если есть
            if (filterName != null) {
                Function<Object, String> filter = filters.get(filterName.trim());
                if (filter != null) {
                    replacement = filter.apply(value != null ? value : "");
                }
            }
            
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        
        matcher.appendTail(result);
        return result.toString();
    }
    
    /**
     * Получение значения из контекста (поддержка вложенных свойств)
     */
    private Object getValueFromContext(String path) {
        String[] parts = path.split("\\.");
        Object current = ctx;
        
        for (String part : parts) {
            if (current instanceof Map) {
                current = ((Map<?, ?>) current).get(part);
            } else {
                return null;
            }
            if (current == null) return null;
        }
        
        return current;
    }
    
    /**
     * Вычисление условия
     */
    private boolean evaluateCondition(String condition) {
        if (condition.startsWith("!")) {
            return !evaluateCondition(condition.substring(1));
        }
        
        Object value = getValueFromContext(condition);
        
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
}
 

