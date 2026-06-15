package ru.pt.api.service.rules;

import ru.pt.api.dto.rules.RuleDto;
import ru.pt.api.dto.rules.RuleScopeType;
import ru.pt.api.dto.rules.RuleType;

import java.util.List;

public interface RuleManagementService {

    List<RuleDto> list(Long tid, RuleType ruleType, RuleScopeType scopeType, String scopeCode, String recordStatus);

    /**
     * ACTIVE rules for product context: PRODUCT+productCode, LOB+lobCode, TENANT+tenantCode.
     */
    List<RuleDto> listForProduct(Long tid, String tenantCode, String productCode, String lobCode);

    RuleDto getById(Long tid, Long id);

    RuleDto create(Long tid, RuleDto dto);

    RuleDto update(Long tid, Long id, RuleDto dto);

    void delete(Long tid, Long id);

    void reloadCache();
}
