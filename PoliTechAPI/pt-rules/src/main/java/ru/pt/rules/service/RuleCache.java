package ru.pt.rules.service;

import org.springframework.stereotype.Component;
import ru.pt.rules.entity.RuleEntity;
import ru.pt.rules.repository.RuleRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class RuleCache {

    private final RuleRepository ruleRepository;
    private volatile Map<Long, List<RuleEntity>> byTenant = new ConcurrentHashMap<>();

    public RuleCache(RuleRepository ruleRepository) {
        this.ruleRepository = ruleRepository;
    }

    public void reloadAll() {
        Map<Long, List<RuleEntity>> grouped = ruleRepository.findAll().stream()
                .filter(r -> "ACTIVE".equals(r.getRecordStatus()))
                .collect(Collectors.groupingBy(RuleEntity::getTid));
        byTenant = new ConcurrentHashMap<>(grouped);
    }

    public void reloadTenant(Long tid) {
        List<RuleEntity> rules = ruleRepository.findByTidAndRecordStatusOrderByPriorityAsc(tid, "ACTIVE");
        byTenant.put(tid, List.copyOf(rules));
    }

    public List<RuleEntity> findActive(Long tid, String ruleType, String scopeType, String scopeCode) {
        List<RuleEntity> all = byTenant.getOrDefault(tid, Collections.emptyList());
        List<RuleEntity> result = new ArrayList<>();
        for (RuleEntity rule : all) {
            if (!ruleType.equals(rule.getRuleType())) {
                continue;
            }
            if (!scopeType.equals(rule.getScopeType())) {
                continue;
            }
            if (!scopeCode.equals(rule.getScopeCode())) {
                continue;
            }
            result.add(rule);
        }
        return result;
    }
}
