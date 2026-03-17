package ru.pt.db.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.pt.api.dto.sales.QuoteDto;
import ru.pt.db.entity.PolicyIndexEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PolicyIndexRepository extends JpaRepository<PolicyIndexEntity, Long> {

    Optional<PolicyIndexEntity> findByPublicId(UUID publicId);

    Optional<PolicyIndexEntity> findPolicyIndexEntityByPolicyNumber(String policyNumber);

    List<PolicyIndexEntity> findAllByClientAccountIdAndUserAccountId(Long clientAccountId, Long userAccountId);

    Optional<PolicyIndexEntity> findByPolicyNumber(String policyNumber);

    Optional<PolicyIndexEntity> findByPaymentOrderId(String paymentOrderId);

    @Query(value = "SELECT a.id_path FROM acc_accounts a WHERE a.id = :accountId", nativeQuery = true)
    Optional<String> findAccountIdPath(@Param("accountId") Long accountId);

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
            p.payment_date,
            p.start_date,
            p.end_date,
            p.policy_status, 
            p.user_account_id,
            p.client_account_id,
            p.version_status,
            p.payment_order_id,
            p.ph_digest,
            p.io_digest,
            p.premium::text,
            p.agent_kv_percent::text,
            p.agent_kv_amount::text
        FROM policy_index p
        JOIN account_tree at ON p.user_account_id = at.id
        where ( p.policy_nr like '%'||:qstr||'%' or p.ph_digest like '%'||:qstr||'%'  )
        ORDER BY p.policy_nr
        """, nativeQuery = true)
    List<Object[]> findPoliciesByAccountIdRecursive(@Param("accountId") Long accountId, @Param("qstr") String qstr);

    @Query(value = """
        SELECT 
            count(*)                         AS sales_count,
            coalesce(sum(premium), 0)        AS total_sales,
            coalesce(sum(agent_kv_amount),0) AS agent_commission
        FROM policy_index
        WHERE start_date::date >= coalesce(:from, start_date::date)
          AND start_date::date <= coalesce(:to,   start_date::date)
          AND id_path LIKE :idPathPrefix
        """, nativeQuery = true)
    List<Object[]> getDashboardCardsAggregates(@Param("from") java.time.LocalDate from,
                                               @Param("to") java.time.LocalDate to,
                                               @Param("idPathPrefix") String idPathPrefix);

    @Query(value = """
        SELECT 
            product_code                    AS label,
            count(*)                        AS sales_count,
            coalesce(sum(premium), 0)       AS total_sales
        FROM policy_index
        WHERE start_date::date >= coalesce(:from, start_date::date)
          AND start_date::date <= coalesce(:to,   start_date::date)
          AND id_path LIKE :idPathPrefix
        GROUP BY product_code
        ORDER BY total_sales DESC
        """, nativeQuery = true)
    List<Object[]> getDashboardByProducts(@Param("from") java.time.LocalDate from,
                                          @Param("to") java.time.LocalDate to,
                                          @Param("idPathPrefix") String idPathPrefix);

    @Query(value = """
        SELECT 
            c.name                          AS label,
            count(*)                        AS sales_count,
            coalesce(sum(premium), 0)       AS total_sales
        FROM policy_index p
        JOIN acc_clients c ON p.client_account_id = c.id
        WHERE p.start_date::date >= coalesce(:from, p.start_date::date)
          AND p.start_date::date <= coalesce(:to,   p.start_date::date)
          AND p.id_path LIKE :idPathPrefix
        GROUP BY c.name
        ORDER BY total_sales DESC
        """, nativeQuery = true)
    List<Object[]> getDashboardByClients(@Param("from") java.time.LocalDate from,
                                         @Param("to") java.time.LocalDate to,
                                         @Param("idPathPrefix") String idPathPrefix);

    @Query(value = """
        SELECT 
            start_date::date                AS period,
            count(*)                        AS sales_count,
            coalesce(sum(premium), 0)       AS total_sales
        FROM policy_index
        WHERE start_date::date >= coalesce(:from, start_date::date)
          AND start_date::date <= coalesce(:to,   start_date::date)
          AND id_path LIKE :idPathPrefix
        GROUP BY start_date::date
        ORDER BY start_date::date
        """, nativeQuery = true)
    List<Object[]> getDailyChart(@Param("from") java.time.LocalDate from,
                                 @Param("to") java.time.LocalDate to,
                                 @Param("idPathPrefix") String idPathPrefix);

    @Query(value = """
        SELECT 
            date_trunc('week', start_date)::date AS period,
            count(*)                              AS sales_count,
            coalesce(sum(premium), 0)             AS total_sales
        FROM policy_index
        WHERE start_date::date >= coalesce(:from, start_date::date)
          AND start_date::date <= coalesce(:to,   start_date::date)
          AND id_path LIKE :idPathPrefix
        GROUP BY date_trunc('week', start_date)::date
        ORDER BY period
        """, nativeQuery = true)
    List<Object[]> getWeeklyChart(@Param("from") java.time.LocalDate from,
                                  @Param("to") java.time.LocalDate to,
                                  @Param("idPathPrefix") String idPathPrefix);

    @Query(value = """
        SELECT 
            date_trunc('month', start_date)::date AS period,
            count(*)                               AS sales_count,
            coalesce(sum(premium), 0)              AS total_sales
        FROM policy_index
        WHERE start_date::date >= coalesce(:from, start_date::date)
          AND start_date::date <= coalesce(:to,   start_date::date)
          AND id_path LIKE :idPathPrefix
        GROUP BY date_trunc('month', start_date)::date
        ORDER BY period
        """, nativeQuery = true)
    List<Object[]> getMonthlyChart(@Param("from") java.time.LocalDate from,
                                   @Param("to") java.time.LocalDate to,
                                   @Param("idPathPrefix") String idPathPrefix);

    @Query(value = """
        SELECT 
            date_trunc('year', start_date)::date AS period,
            count(*)                              AS sales_count,
            coalesce(sum(premium), 0)             AS total_sales
        FROM policy_index
        WHERE start_date::date >= coalesce(:from, start_date::date)
          AND start_date::date <= coalesce(:to,   start_date::date)
          AND id_path LIKE :idPathPrefix
        GROUP BY date_trunc('year', start_date)::date
        ORDER BY period
        """, nativeQuery = true)
    List<Object[]> getYearlyChart(@Param("from") java.time.LocalDate from,
                                  @Param("to") java.time.LocalDate to,
                                  @Param("idPathPrefix") String idPathPrefix);
}
