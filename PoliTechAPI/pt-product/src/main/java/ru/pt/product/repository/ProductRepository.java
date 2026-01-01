package ru.pt.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.pt.product.entity.ProductEntity;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<ProductEntity, Integer> {

    @Query("select p from ProductEntity p where p.tId = :tId and p.id = :id and p.isDeleted = false")
    Optional<ProductEntity> findById(@Param("tId") Long tId, @Param("id") Integer id);

    @Query("select p from ProductEntity p where p.tId = :tId and p.code = :code and p.isDeleted = false")
    Optional<ProductEntity> findByCode(@Param("tId") Long tId, @Param("code") String code);

    @Query("select p.id as id, p.lob as lob, p.code as code, p.name as name, p.prodVersionNo as prodVersionNo, p.devVersionNo as devVersionNo from ProductEntity p where p.tId = :tId and p.isDeleted = false order by p.code")
    List<Object[]> listActiveSummaries(@Param("tId") Long tId);

    @Query(
        """
            select pr.roleProductId
            from ProductRoleEntity pr
            where pr.roleAccountEntity.id = ?1
        """
    )
    List<Integer> findProductIdEntityByAccountId(Long accountId);

}
