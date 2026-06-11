package ru.pt.rules.service;

import ru.pt.api.dto.rules.RuleDto;
import ru.pt.api.dto.rules.RuleScopeType;
import ru.pt.api.dto.rules.RuleType;
import ru.pt.rules.entity.RuleEntity;

final class RuleMapper {

    private RuleMapper() {
    }

    static RuleDto toDto(RuleEntity entity) {
        RuleDto dto = new RuleDto();
        dto.setId(entity.getId());
        dto.setCode(entity.getCode());
        dto.setName(entity.getName());
        dto.setScopeType(RuleScopeType.valueOf(entity.getScopeType()));
        dto.setScopeCode(entity.getScopeCode());
        dto.setRuleType(RuleType.valueOf(entity.getRuleType()));
        dto.setPriority(entity.getPriority());
        dto.setRecordStatus(entity.getRecordStatus());
        dto.setExpressionLanguage(entity.getExpressionLanguage());
        dto.setExpression(entity.getExpression());
        dto.setMessage(entity.getMessage());
        dto.setLlmText(entity.getLlmText());
        return dto;
    }

    static void applyDto(RuleEntity entity, RuleDto dto) {
        entity.setCode(dto.getCode());
        entity.setName(dto.getName());
        entity.setScopeType(dto.getScopeType().name());
        entity.setScopeCode(dto.getScopeCode());
        entity.setRuleType(dto.getRuleType().name());
        entity.setPriority(dto.getPriority() != null ? dto.getPriority() : 100);
        entity.setRecordStatus(dto.getRecordStatus() != null ? dto.getRecordStatus() : "ACTIVE");
        entity.setExpressionLanguage(
                dto.getExpressionLanguage() != null ? dto.getExpressionLanguage() : "CEL");
        entity.setExpression(dto.getExpression());
        entity.setMessage(dto.getMessage());
        entity.setLlmText(dto.getLlmText());
    }
}
