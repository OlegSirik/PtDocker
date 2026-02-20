package ru.pt.db.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.pt.db.entity.PolicyEntity;

public interface PolicyRepository extends JpaRepository<PolicyEntity, Long> {

    @Query(value = "SELECT nextval('policy_seq')", nativeQuery = true)
    Long getNextPolicySeqValue();
}
