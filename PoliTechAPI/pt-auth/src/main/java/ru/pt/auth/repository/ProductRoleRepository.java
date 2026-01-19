package ru.pt.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.pt.auth.entity.ProductRoleEntity;

import java.util.List;
import java.util.Map;


public interface ProductRoleRepository extends JpaRepository<ProductRoleEntity, Long> {

    /**
     * Find all product roles for a specific account
     */
    @Query("SELECT pr FROM ProductRoleEntity pr WHERE pr.accountEntity.id = :accountId ORDER BY pr.roleProductId, pr.roleAccountEntity.id")
    List<ProductRoleEntity> findByAccountId(@Param("accountId") Long accountId);

    @Query(
        value = "WITH RECURSIVE category_levels AS ( " +
                "SELECT id, parent_id, 0 as lvl FROM acc_accounts WHERE id = :accountId " +
                "UNION ALL " +
                "SELECT c.id, c.parent_id, cl.lvl + 1 FROM acc_accounts c JOIN category_levels cl ON cl.parent_id = c.id " +
                ") " +
                "SELECT rl.* , pp.code roleProductCode, pp.lob lobCode, pp.code productCode, pp.name productName " +
                "FROM category_levels cl " +
                "JOIN acc_product_roles rl ON cl.id = rl.role_account_id AND rl.account_id = :accountId " +
                "JOIN pt_products pp on pp.id = rl.role_product_id " +
                "ORDER BY rl.role_product_id, lvl ASC",
        nativeQuery = true
    )
    List<Map<String, Object>> findAllProductRolesByAccountId(@Param("accountId") Long accountId);

}
