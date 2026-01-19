package ru.pt.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.pt.auth.entity.AccountEntity;
import ru.pt.auth.entity.AccountNodeType;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<AccountEntity, Long> {


    @Query("SELECT a FROM AccountEntity a WHERE a.tenantEntity.code = :tenantCode AND a.nodeType = 'TENANT'")
    Optional<AccountEntity> findTenantAccount(@Param("tenantCode") String tenantCode);

    @Query("SELECT a FROM AccountEntity a WHERE a.tenantEntity.code = :tenantCode AND a.clientEntity.clientId = :authClientId AND a.nodeType = 'CLIENT'")
    Optional<AccountEntity> findClientAccount(@Param("tenantCode") String tenantCode, @Param("authClientId") String authClientId);

    @Query("SELECT a FROM AccountEntity a WHERE a.tenantEntity.code = :tenantCode AND a.clientEntity.Id = :clientId AND a.nodeType = 'CLIENT'")
    Optional<AccountEntity> findClientAccountById(@Param("tenantCode") String tenantCode, @Param("clientId") Long clientId);

    @Query("SELECT a FROM AccountEntity a WHERE a.tenantEntity.code = :tenantCode AND a.clientEntity.clientId = :authClientId AND a.nodeType = :nodeType")
    Optional<AccountEntity> findAdminAccount(@Param("tenantCode") String tenantCode, @Param("authClientId") String authClientId, @Param("nodeType") AccountNodeType nodeType);

    @Query("SELECT a FROM AccountEntity a WHERE a.tenantEntity.code = :tenantCode AND a.id = :id")
    Optional<AccountEntity> findByTenantCodeAndId(@Param("tenantCode") String tenantCode, @Param("id") Long id);

    /**
     * Find all child accounts of a specific parent
     */
    @Query("SELECT a FROM AccountEntity a WHERE a.parent.id = :parentId ORDER BY a.name")
    List<AccountEntity> findByParentId(Long parentId);

    /**
     * Find accounts by node type
     */
    @Query("SELECT a FROM AccountEntity a WHERE a.nodeType = :nodeType ORDER BY a.name")
    List<AccountEntity> findByNodeType(AccountNodeType nodeType);

    @Query("SELECT a FROM AccountEntity a WHERE a.tenantEntity.code = :tenantCode AND a.clientEntity.id = :clientId AND a.id = :accountId")
    Optional<AccountEntity> findByTenantCodeAndClientIdAndId(@Param("tenantCode") String tenantCode, @Param("clientId") Long clientId, @Param("accountId") Long accountId);

    /**
     * Get next account ID from sequence
     */
    @Query(value = "SELECT nextval('account_seq')", nativeQuery = true)
    Long getNextAccountId();

    /**
     * Find accounts by tenant and client
     */
    @Query("SELECT a FROM AccountEntity a WHERE a.tenantEntity.code = :tenantCode AND a.clientEntity.id = :clientId ORDER BY a.name")
    List<AccountEntity> findByTenantCodeAndClientId(String tenantCode, Long clientId);

    /**
     * Find accounts by client id and type = client !!Must be 1 record
     */
    @Query("SELECT a FROM AccountEntity a WHERE a.clientEntity.id = :clientId AND a.nodeType = 'CLIENT'")
    Optional<AccountEntity> findCliensAccountByClientId(Long clientId);

    /**
     * Find tenant account by tenant id
     */
    //@Query("SELECT a FROM AccountEntity a WHERE a.tenantEntity.id = :tenantId and a.nodeType = 'TENANT' and a.parent.id is null")
    @Query(value = "SELECT * FROM acc_accounts WHERE tid = :tenantId and parent_id is null and node_type = 'TENANT'", nativeQuery = true)
    Optional<AccountEntity> findByTenantId(Long tenantId);

    @Query(
        value = "WITH RECURSIVE path_cte AS ( " +
                "SELECT a.*, 1 AS level " +
                "FROM acc_accounts a " +
                "WHERE a.id = :accountId " +
                "UNION ALL " +
                "SELECT t.*, c.level + 1 " +
                "FROM acc_accounts t " +
                "INNER JOIN path_cte c ON t.id = c.parent_id " +
                "WHERE c.parent_id IS NOT NULL " +
                ") " +
                "SELECT * " +
                "FROM path_cte " +
                "ORDER BY level DESC",
        nativeQuery = true
    )
    List<AccountEntity> findPathByAccountId(Long accountId);

    List<AccountEntity> findAllByParentId(Long parentId);

    /**
     * Find accounts by tenant code, client id, and user login.
     * Joins through AccountLoginEntity to access userLogin field.
     */
    @Query("SELECT DISTINCT a FROM AccountEntity a JOIN a.accountLoginEntities al WHERE a.tenantEntity.code = :tenantCode AND a.clientEntity.id = :clientId AND al.userLogin = :userLogin")
    List<AccountEntity> findByTenantCodeAndClientIdAndUserLogin(@Param("tenantCode") String tenantCode, @Param("clientId") Long clientId, @Param("userLogin") String userLogin);

    @Query(value = """
            WITH RECURSIVE ancestor_path AS (
                SELECT id, parent_id
                FROM accounts
                WHERE id = :resourceAccountId

                UNION ALL

                SELECT a.id, a.parent_id
                FROM accounts a
                INNER JOIN ancestor_path ap ON a.id = ap.parent_id
            )
            SELECT COUNT(*) > 0
            FROM ancestor_path
            WHERE id = :userAccountId
            """, nativeQuery = true)
    boolean iCanSeeResource(@Param("userAccountId") Long userAccountId,
                            @Param("resourceAccountId") Long resourceAccountId);

}
