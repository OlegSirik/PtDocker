package ru.pt.rules.service;

import com.google.common.collect.ImmutableList;
import com.google.protobuf.NullValue;
import dev.cel.common.CelAbstractSyntaxTree;
import dev.cel.common.CelFunctionDecl;
import dev.cel.common.CelOptions;
import dev.cel.common.CelOverloadDecl;
import dev.cel.common.CelValidationException;
import dev.cel.common.types.SimpleType;
import dev.cel.compiler.CelCompiler;
import dev.cel.compiler.CelCompilerFactory;
import dev.cel.parser.CelStandardMacro;
import dev.cel.runtime.CelEvaluationException;
import dev.cel.runtime.CelRuntime;
import dev.cel.runtime.CelRuntime.CelFunctionBinding;
import dev.cel.runtime.CelRuntimeBuilder;
import dev.cel.runtime.CelRuntimeFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.cel.common.CelFunctionDecl.newFunctionDeclaration;
import static dev.cel.common.CelOverloadDecl.newGlobalOverload;

@Component
public class CelRuleEngine {

    private static final String NUM_OVERLOAD = "pt_num_key";
    private static final String STR_OVERLOAD = "pt_str_key";

    private static final Pattern IDENTIFIER = Pattern.compile("\\b([a-zA-Z_][a-zA-Z0-9_]*)\\b");
    private static final Pattern STRING_LITERAL = Pattern.compile("\"([^\"\\\\]|\\\\.)*\"|'([^'\\\\]|\\\\.)*'");
    private static final Set<String> CEL_KEYWORDS = Set.of(
            "true", "false", "null", "in", "has", "num", "str");

    /**
     * Проверка синтаксиса при сохранении правила в админке (контекста полиса ещё нет).
     */
    public void validateExpression(String expression) throws CelValidationException {
        compile(expression, extractVariableNames(expression));
    }

    /**
     * Проверка CEL без проброса checked exception наружу модуля pt-rules.
     */
    public String validateExpressionOrError(String expression) {
        try {
            validateExpression(expression);
            return null;
        } catch (CelValidationException ex) {
            return ex.getMessage();
        }
    }

    /**
     * Runtime: контекст полиса в {@code variables}; в выражении — {@code num("varCode")}, {@code str("varCode")}.
     */
    public boolean evaluate(String expression, Map<String, Object> variables)
            throws CelValidationException, CelEvaluationException {
        Map<String, Object> ctx = variables != null ? variables : Map.of();
        Set<String> keys = ctx.keySet();
        CelAbstractSyntaxTree ast = compile(expression, keys);
        CelRuntime runtime = newRuntimeBuilder(ctx).build();
        CelRuntime.Program program = runtime.createProgram(ast);
        Object result = program.eval(toActivation(keys, ctx));
        return toBoolean(result);
    }

    private CelAbstractSyntaxTree compile(String expression, Set<String> variableNames)
            throws CelValidationException {
        var builder = newCompilerBuilder();
        for (String name : variableNames) {
            builder.addVar(name, SimpleType.DYN);
        }
        CelCompiler compiler = builder.build();
        return compiler.compile(expression).getAst();
    }

    private static dev.cel.compiler.CelCompilerBuilder newCompilerBuilder() {
        return CelCompilerFactory.standardCelCompilerBuilder()
                .setOptions(CelOptions.current().build())
                .setResultType(SimpleType.BOOL)
                .setStandardMacros(CelStandardMacro.STANDARD_MACROS)
                .addFunctionDeclarations(helperFunctionDecls());
    }

    private static CelRuntimeBuilder newRuntimeBuilder(Map<String, Object> ctx) {
        Map<String, Object> context = ctx != null ? ctx : Map.of();
        return CelRuntimeFactory.standardCelRuntimeBuilder()
                .setOptions(CelOptions.current().build())
                .addFunctionBindings(
                        CelFunctionBinding.from(
                                NUM_OVERLOAD,
                                ImmutableList.of(String.class),
                                args -> CelVariableHelpers.numForCel(context, (String) args[0])),
                        CelFunctionBinding.from(
                                STR_OVERLOAD,
                                ImmutableList.of(String.class),
                                args -> CelVariableHelpers.strForCel(context, (String) args[0])));
    }

    private static List<CelFunctionDecl> helperFunctionDecls() {
        return List.of(
                newFunctionDeclaration(
                        "num",
                        newGlobalOverload(NUM_OVERLOAD, SimpleType.DYN, ImmutableList.of(SimpleType.STRING))),
                newFunctionDeclaration(
                        "str",
                        newGlobalOverload(STR_OVERLOAD, SimpleType.STRING, ImmutableList.of(SimpleType.STRING))));
    }

    /**
     * CEL-Java: Java {@code null} в activation даёт CelUnknownSet.
     * Значение {@code null} в map → {@link NullValue#NULL_VALUE}.
     */
    private Map<String, Object> toActivation(Set<String> keys, Map<String, Object> variables) {
        Map<String, Object> activation = new HashMap<>();
        if (variables == null) {
            return activation;
        }
        for (String key : keys) {
            if (!variables.containsKey(key)) {
                continue;
            }
            Object value = variables.get(key);
            if (value == null) {
                activation.put(key, NullValue.NULL_VALUE);
            } else {
                activation.put(key, toCelValue(value));
            }
        }
        return activation;
    }

    private Object toCelValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal bd) {
            try {
                return bd.longValueExact();
            } catch (ArithmeticException ex) {
                return bd.doubleValue();
            }
        }
        if (value instanceof Number number) {
            if (value instanceof Double || value instanceof Float) {
                return number.doubleValue();
            }
            return number.longValue();
        }
        if (value instanceof String s) {
            return parseNumericString(s);
        }
        return value;
    }

    private Object parseNumericString(String s) {
        String trimmed = s.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        try {
            if (trimmed.contains(".") || trimmed.contains("e") || trimmed.contains("E")) {
                return Double.parseDouble(trimmed);
            }
            return Long.parseLong(trimmed);
        } catch (NumberFormatException ex) {
            return s;
        }
    }

    private boolean toBoolean(Object result) {
        if (result instanceof Boolean bool) {
            return bool;
        }
        String type = result != null ? result.getClass().getSimpleName() : "null";
        if (type.contains("Unknown")) {
            throw new IllegalStateException(
                    "CEL: переменная отсутствует в activation. "
                            + "Используйте num(\"varCode\") / str(\"varCode\") или проверку == null");
        }
        throw new IllegalStateException("CEL expression must return boolean, got: " + result);
    }

    static Set<String> extractVariableNames(String expression) {
        Set<String> names = new HashSet<>();
        if (expression == null || expression.isBlank()) {
            return names;
        }
        String withoutLiterals = STRING_LITERAL.matcher(expression).replaceAll(" ");
        Matcher matcher = IDENTIFIER.matcher(withoutLiterals);
        while (matcher.find()) {
            String token = matcher.group(1);
            if (!CEL_KEYWORDS.contains(token)) {
                names.add(token);
            }
        }
        return names;
    }
}
