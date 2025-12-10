package ru.pt.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.pt.auth.entity.AccountEntity;
import ru.pt.auth.entity.AccountNodeType;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<AccountEntity, Long> {

    /**
     * Find account by ID with eager loading of children
     */
    @Query("SELECT a FROM AccountEntity a LEFT JOIN FETCH a.children WHERE a.id = :id")
    Optional<AccountEntity> findByIdWithChildren(Long id);

    /**
     * Find account by ID with eager loading of parent
     */
    @Query("SELECT a FROM AccountEntity a LEFT JOIN FETCH a.parent WHERE a.id = :id")
    Optional<AccountEntity> findByIdWithParent(Long id);

    /**
     * Find all root accounts (accounts without parent)
     */
    @Query("SELECT a FROM AccountEntity a WHERE a.parent.id IS NULL ORDER BY a.name")
    List<AccountEntity> findRootAccounts();

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

    /**
     * Find accounts by name containing the given string (case insensitive)
     */
    @Query("SELECT a FROM AccountEntity a WHERE LOWER(a.name) LIKE LOWER(CONCAT('%', :name, '%')) ORDER BY a.name")
    List<AccountEntity> findByNameContainingIgnoreCase(String name);

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


}
