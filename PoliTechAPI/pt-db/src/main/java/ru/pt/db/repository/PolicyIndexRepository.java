package ru.pt.db.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.pt.api.dto.sales.QuoteDto;
import ru.pt.db.entity.PolicyIndexEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PolicyIndexRepository extends JpaRepository<PolicyIndexEntity, UUID> {

    Optional<PolicyIndexEntity> findPolicyIndexEntityByPolicyNumber(String policyNumber);

    List<PolicyIndexEntity> findAllByClientAccountIdAndUserAccountId(Long clientAccountId, Long userAccountId);

    Optional<PolicyIndexEntity> findByPolicyNumber(String policyNumber);

    Optional<PolicyIndexEntity> findByPaymentOrderId(String paymentOrderId);

    @Query(value = """
        WITH RECURSIVE account_tree AS (
            SELECT id FROM acc_accounts WHERE id = :accountId
            UNION ALL
            SELECT a.id FROM acc_accounts a
            JOIN account_tree at ON a.parent_id = at.id
        )
        SELECT 
            p.id,
            p.draft_id,
            p.policy_nr,
            p.product_code,
            p.create_date,
            p.issue_date,
            p.issue_timezone,
            p.payment_date,
            p.start_date,
            p.end_date,
            p.policy_status,
            p.user_account_id,
            p.client_account_id,
            p.version_status,
            p.payment_order_id
        FROM policy_index p
        JOIN account_tree at ON p.user_account_id = at.id
        ORDER BY p.policy_nr
        """, nativeQuery = true)
    List<Object[]> findPoliciesByAccountIdRecursive(@Param("accountId") Long accountId);
}
