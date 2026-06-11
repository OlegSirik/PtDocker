package ru.pt.rules.service;

import dev.cel.common.CelValidationException;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.pt.api.dto.exception.BadRequestException;
import ru.pt.api.dto.exception.NotFoundException;
import ru.pt.api.dto.rules.RuleDto;
import ru.pt.api.dto.rules.RuleScopeType;
import ru.pt.api.dto.rules.RuleType;
import ru.pt.api.service.rules.RuleManagementService;
import ru.pt.rules.entity.RuleEntity;
import ru.pt.rules.repository.RuleRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class RuleManagementServiceImpl implements RuleManagementService {

    private final RuleRepository ruleRepository;
    private final RuleCache ruleCache;
    private final CelRuleEngine celRuleEngine;

    public RuleManagementServiceImpl(
            RuleRepository ruleRepository,
            RuleCache ruleCache,
            CelRuleEngine celRuleEngine) {
        this.ruleRepository = ruleRepository;
        this.ruleCache = ruleCache;
        this.celRuleEngine = celRuleEngine;
    }

    @PostConstruct
    void initCache() {
        ruleCache.reloadAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RuleDto> list(Long tid, RuleType ruleType, RuleScopeType scopeType, String scopeCode, String recordStatus) {
        List<RuleEntity> entities = ruleRepository.findByTidAndRecordStatusOrderByPriorityAsc(
                tid, recordStatus != null ? recordStatus : "ACTIVE");
        List<RuleDto> result = new ArrayList<>();
        for (RuleEntity entity : entities) {
            if (ruleType != null && !ruleType.name().equals(entity.getRuleType())) {
                continue;
            }
            if (scopeType != null && !scopeType.name().equals(entity.getScopeType())) {
                continue;
            }
            if (scopeCode != null && !scopeCode.equals(entity.getScopeCode())) {
                continue;
            }
            result.add(RuleMapper.toDto(entity));
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public RuleDto getById(Long tid, Long id) {
        RuleEntity entity = ruleRepository.findByTidAndId(tid, id)
                .orElseThrow(() -> new NotFoundException("Rule not found: " + id));
        return RuleMapper.toDto(entity);
    }

    @Override
    @Transactional
    public RuleDto create(Long tid, RuleDto dto) {
        validateDto(dto);
        if (ruleRepository.findByTidAndCodeAndRecordStatus(tid, dto.getCode(), "ACTIVE").isPresent()) {
            throw new BadRequestException("Active rule with code already exists: " + dto.getCode());
        }
        RuleEntity entity = new RuleEntity();
        entity.setTid(tid);
        RuleMapper.applyDto(entity, dto);
        entity.setRecordStatus("ACTIVE");
        validateCel(entity.getExpression());
        RuleEntity saved = ruleRepository.save(entity);
        ruleCache.reloadTenant(tid);
        return RuleMapper.toDto(saved);
    }

    @Override
    @Transactional
    public RuleDto update(Long tid, Long id, RuleDto dto) {
        validateDto(dto);
        RuleEntity entity = ruleRepository.findByTidAndId(tid, id)
                .orElseThrow(() -> new NotFoundException("Rule not found: " + id));
        if (ruleRepository.existsByTidAndCodeAndRecordStatusAndIdNot(tid, dto.getCode(), "ACTIVE", id)) {
            throw new BadRequestException("Active rule with code already exists: " + dto.getCode());
        }
        RuleMapper.applyDto(entity, dto);
        validateCel(entity.getExpression());
        RuleEntity saved = ruleRepository.save(entity);
        ruleCache.reloadTenant(tid);
        return RuleMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void delete(Long tid, Long id) {
        RuleEntity entity = ruleRepository.findByTidAndId(tid, id)
                .orElseThrow(() -> new NotFoundException("Rule not found: " + id));
        entity.setRecordStatus("INACTIVE");
        ruleRepository.save(entity);
        ruleCache.reloadTenant(tid);
    }

    @Override
    public void reloadCache() {
        ruleCache.reloadAll();
    }

    private void validateDto(RuleDto dto) {
        if (dto.getCode() == null || dto.getCode().isBlank()) {
            throw new BadRequestException("Rule code is required");
        }
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new BadRequestException("Rule name is required");
        }
        if (dto.getScopeType() == null) {
            throw new BadRequestException("scopeType is required");
        }
        if (dto.getScopeCode() == null || dto.getScopeCode().isBlank()) {
            throw new BadRequestException("scopeCode is required");
        }
        if (dto.getRuleType() == null) {
            throw new BadRequestException("ruleType is required");
        }
        if (dto.getExpression() == null || dto.getExpression().isBlank()) {
            throw new BadRequestException("expression is required");
        }
        if (dto.getMessage() == null || dto.getMessage().isBlank()) {
            throw new BadRequestException("message is required");
        }
    }

    private void validateCel(String expression) {
        try {
            celRuleEngine.validateExpression(expression);
        } catch (CelValidationException ex) {
            String msg = ex.getMessage() != null ? ex.getMessage() : "CEL validation failed";
            if (msg.contains("invalid argument to has()")) {
                throw new BadRequestException(
                        "Invalid CEL: has() только для вложенных полей (например msg.field), "
                                + "не для varCode (pl_policyNumber). "
                                + "Пример: pl_policyNumber != null && pl_policyNumber != \"\"");
            }
            throw new BadRequestException("Invalid CEL expression: " + msg);
        }
    }
}
