package ru.pt.api.service.rules;

import ru.pt.api.dto.rules.RuleType;
import ru.pt.api.dto.rules.RuleValidationContext;

import java.util.List;

public interface RuleValidationService {

    List<String> processValidation(RuleType ruleType, RuleValidationContext context);

    void reloadCache();
}
