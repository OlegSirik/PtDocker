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
//    @Query("SELECT pr FROM ProductRoleEntity pr WHERE pr.accountEntity.id = :accountId ORDER BY pr.roleProductId, pr.roleAccountEntity.id")
//    List<ProductRoleEntity> findByAccountId(@Param("accountId") Long accountId);
/* 
    @Query(
        value = "WITH RECURSIVE category_levels AS ( " +
                "SELECT id, parent_id, 0 as lvl FROM acc_accounts WHERE id = :accountId " +
                "UNION ALL " +
                "SELECT c.id, c.parent_id, cl.lvl + 1 FROM acc_accounts c JOIN category_levels cl ON cl.parent_id = c.id " +
                ") " +
                "SELECT rl.* , pp.code roleProductCode, pp.lob lobCode, pp.code productCode, pp.name productName " +
                "FROM category_levels cl " +
                "JOIN acc_product_roles rl ON cl.id = rl.role_account_id " +
                "JOIN pt_products pp on pp.id = rl.role_product_id " +
                "ORDER BY rl.role_product_id, lvl ASC",
        nativeQuery = true
    )
*/
    @Query(
        value = "WITH account_path AS (" +
                "SELECT id_path FROM acc_accounts WHERE id = :accountId LIMIT 1" +
                ") " +
                "SELECT DISTINCT ON (t1.role_product_id) " +
                "t1.*, " +
                "t2.code roleProductCode, t2.lob lobCode, t2.code productCode, t2.name productName " +
                "FROM acc_product_roles t1 " +
                "JOIN account_path ap ON ap.id_path LIKE (t1.id_path || '%') " +
                "LEFT JOIN pt_products t2 ON t2.id = t1.role_product_id " +
                "ORDER BY t1.role_product_id, LENGTH(t1.id_path) DESC;",
        nativeQuery = true
    )
    List<Map<String, Object>> findAllProductRolesByAccountId(@Param("accountId") Long accountId);

    @Query("SELECT pr FROM ProductRoleEntity pr WHERE pr.accountEntity.id = :accountId AND pr.roleProductId = :productId")
    ProductRoleEntity findByAccountIdAndProductId(@Param("accountId") Long accountId, @Param("productId") Long productId);

    /*
    Поиск продуктовых прав для одного продукта
    Используется так же для определения права доступа к экаунту, чтобы 2 раза не гонять рекурсивный запрос 
    *
    @Query(
        value = "WITH RECURSIVE acc_tree AS ( " +
                "SELECT id, parent_id, 0 as lvl " +
                "FROM acc_accounts " +
                "WHERE id = :accountId " +
                "UNION ALL " +
                "SELECT parent.id, parent.parent_id, child.lvl + 1 " +
                "FROM acc_accounts parent " +
                "JOIN acc_tree child ON parent.id = child.parent_id " +
                "WHERE parent.parent_id IS NOT NULL " +
                ") " +
                "SELECT acc_tree.*, apr.*, pp.name as productName " +
                "FROM acc_tree " +
                "LEFT JOIN acc_product_roles apr ON apr.account_id = acc_tree.id AND apr.role_product_id = :productId " +
                "LEFT JOIN pt_products pp ON pp.id = apr.role_product_id",
        nativeQuery = true
    )
*/

    @Query(
        value = "SELECT t1.*, t2.name AS product_name " +
                "FROM acc_product_roles t1 " +
                "LEFT JOIN pt_products t2 ON t2.id = t1.role_product_id " +
                "WHERE t1.role_product_id = :productId " +
                "AND (SELECT id_path FROM acc_accounts WHERE id = :accountId) LIKE (t1.id_path || '%') " +
                "ORDER BY LENGTH(t1.id_path) DESC " +
                "LIMIT 1",
        nativeQuery = true
    )
    List<Map<String, Object>> findProductRoleForAccountId(@Param("accountId") Long accountId, @Param("productId") Long productId);
}
