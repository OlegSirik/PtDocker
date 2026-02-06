package ru.pt.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.pt.auth.entity.CommissionRateEntity;

import java.util.List;
import java.util.Optional;

public interface CommissionRateRepository extends JpaRepository<CommissionRateEntity, Long> {

    @Query("SELECT c FROM CommissionRateEntity c WHERE c.tenant.id = :tid AND c.account.id = :accountId AND c.productId = :productId AND c.isDeleted = false")
    Optional<CommissionRateEntity> findByAccountAndProduct(
            @Param("tid") Long tid,
            @Param("accountId") Long accountId,
            @Param("productId") Integer productId);

    @Query("SELECT c FROM CommissionRateEntity c WHERE c.tenant.id = :tid AND c.account.id = :accountId AND c.productId = :productId AND c.action = :action AND c.isDeleted = false")
    Optional<CommissionRateEntity> findByAccountProductAndAction(
            @Param("tid") Long tid,
            @Param("accountId") Long accountId,
            @Param("productId") Integer productId,
            @Param("action") String action);

    @Query("SELECT c FROM CommissionRateEntity c WHERE c.tenant.id = :tid AND c.account.id = :accountId AND c.isDeleted = false")
    List<CommissionRateEntity> findByAccountAndTenant(
            @Param("tid") Long tid,
            @Param("accountId") Long accountId);

    @Query("SELECT c FROM CommissionRateEntity c WHERE c.tenant.id = :tid AND c.id = :id and c.isDeleted = false")
    Optional<CommissionRateEntity> findByIdAndTenant(@Param("tid") Long tid, @Param("id") Long id);

    @Query("SELECT c FROM CommissionRateEntity c WHERE c.tenant.id = :tid AND c.id = :id")
    Optional<CommissionRateEntity> findByIdAndTenantIncludeDeleted(@Param("tid") Long tid, @Param("id") Long id);

    /**
     * Finds commission configurations for the given tenant/account/product/action,
     * taking into account the account hierarchy.
     *
     * <p>Logic:
     * <ul>
     *   <li>Start from the given accountId</li>
     *   <li>Walk up the {@code acc_accounts.parent_id} chain using a recursive CTE</li>
     *   <li>Return all commission rows found for any account in this path (leaf first)</li>
     * </ul>
     */
    @Query(value = """
        WITH RECURSIVE acc_tree AS (
            SELECT a.id, a.parent_id, 0 AS depth
            FROM acc_accounts a
            WHERE a.id = :accountId

            UNION ALL

            SELECT a.id, a.parent_id, t.depth + 1
            FROM acc_accounts a
            JOIN acc_tree t ON a.id = t.parent_id
        )
        SELECT c.*
        FROM acc_commission_rates c
        JOIN acc_tree t ON c.account_id = t.id
        WHERE c.tid = :tid
          AND c.is_deleted = FALSE
          AND (:productId IS NULL OR c.product_id = :productId)
          AND (:action IS NULL OR c.action = :action)
        ORDER BY t.depth
        """, nativeQuery = true)
    List<CommissionRateEntity> findConfigurations(
            @Param("tid") Long tid,
            @Param("accountId") Long accountId,
            @Param("productId") Integer productId,
            @Param("action") String action);
}
