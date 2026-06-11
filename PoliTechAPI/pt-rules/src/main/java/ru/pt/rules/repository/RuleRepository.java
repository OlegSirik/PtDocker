package ru.pt.rules.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.pt.rules.entity.RuleEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface RuleRepository extends JpaRepository<RuleEntity, Long> {

    List<RuleEntity> findByTidAndRecordStatusOrderByPriorityAsc(Long tid, String recordStatus);

    List<RuleEntity> findByTidAndRuleTypeAndScopeTypeAndScopeCodeAndRecordStatusOrderByPriorityAsc(
            Long tid, String ruleType, String scopeType, String scopeCode, String recordStatus);

    Optional<RuleEntity> findByTidAndId(Long tid, Long id);

    Optional<RuleEntity> findByTidAndCodeAndRecordStatus(Long tid, String code, String recordStatus);

    boolean existsByTidAndCodeAndRecordStatusAndIdNot(Long tid, String code, String recordStatus, Long id);
}
