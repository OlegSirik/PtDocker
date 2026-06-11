package ru.pt.rules.service;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CelRuleEngineTest {

    private final CelRuleEngine engine = new CelRuleEngine();

    @Test
    void evaluate_ageLimit_passes() throws Exception {
        assertTrue(engine.evaluate("io_age <= 75", Map.of("io_age", 30)));
    }

    @Test
    void evaluate_magicAgeAsString_passes() throws Exception {
        assertTrue(engine.evaluate("io_age_issue <= 40", Map.of("io_age_issue", "25")));
    }

    @Test
    void evaluate_magicAgeAsString_fails() throws Exception {
        assertFalse(engine.evaluate("io_age_issue <= 40", Map.of("io_age_issue", "45")));
    }

    @Test
    void evaluate_ageLimit_fails() throws Exception {
        assertFalse(engine.evaluate("io_age <= 75", Map.of("io_age", 80)));
    }

    @Test
    void evaluate_missingVariableInMap_failsCompile() {
        org.junit.jupiter.api.Assertions.assertThrows(
                dev.cel.common.CelValidationException.class,
                () -> engine.evaluate("io_age <= 40", Map.of()));
    }

    @Test
    void extractVariableNames_ignoresKeywords() {
        var names = CelRuleEngine.extractVariableNames("io_age <= 75 && true");
        assertTrue(names.contains("io_age"));
        assertFalse(names.contains("true"));
    }

    @Test
    void extractVariableNames_ignoresStringLiteralsInList() {
        var names = CelRuleEngine.extractVariableNames("ph_age_issue < 40 && ph_gender in [\"M\"]");
        assertTrue(names.contains("ph_age_issue"));
        assertTrue(names.contains("ph_gender"));
        assertFalse(names.contains("M"));
    }

    @Test
    void validateExpression_nullCheck_compiles() throws Exception {
        engine.validateExpression("pl_policyNumber != null && pl_policyNumber != \"\"");
    }

    @Test
    void evaluate_nullInContext_detectedAsNull() throws Exception {
        Map<String, Object> vars = new HashMap<>();
        vars.put("pl_policyNumber", null);
        assertTrue(engine.evaluate("pl_policyNumber == null", vars));
        assertFalse(engine.evaluate("pl_policyNumber != null && pl_policyNumber != \"\"", vars));
    }

    @Test
    void evaluate_numHelper_magicAgeString() throws Exception {
        assertTrue(engine.evaluate("num(\"io_age_issue\") < 40", Map.of("io_age_issue", "25")));
        assertFalse(engine.evaluate("num(\"io_age_issue\") < 40", Map.of("io_age_issue", "45")));
    }

    @Test
    void evaluate_strHelper_genderInList() throws Exception {
        assertTrue(engine.evaluate(
                "str(\"ph_gender\") in [\"M\"]",
                Map.of("ph_gender", "M")));
    }

    @Test
    void evaluate_numHelper_nullKey() throws Exception {
        Map<String, Object> vars = new HashMap<>();
        vars.put("io_age_issue", null);
        assertTrue(engine.evaluate("num(\"io_age_issue\") == null", vars));
    }

    @Test
    void evaluate_optionalField_presentOrSkip() throws Exception {
        Map<String, Object> vars = new HashMap<>();
        vars.put("pl_policyNumber", null);
        assertTrue(engine.evaluate(
                "pl_policyNumber == null || pl_policyNumber == \"\" || pl_policyNumber != \"\"",
                vars));
    }

    @Test
    void evaluate_genderInList_passes() throws Exception {
        assertTrue(engine.evaluate(
                "ph_age_issue < 40 && ph_gender in [\"M\"]",
                Map.of("ph_age_issue", "25", "ph_gender", "M")));
    }
}
