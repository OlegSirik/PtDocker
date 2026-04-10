package ru.pt.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.pt.product.entity.ProductEntity;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<ProductEntity, Long> {

    @Query("select p from ProductEntity p where p.tId = :tId and p.id = :id and p.recordStatus = 'ACTIVE'")
    Optional<ProductEntity> findById(@Param("tId") Long tId, @Param("id") Long id);

    @Query("select p from ProductEntity p where p.tId = :tId and p.code = :code and p.recordStatus = 'ACTIVE'")
    Optional<ProductEntity> findByCode(@Param("tId") Long tId, @Param("code") String code);

    @Query("""
            select p.id, p.lob, p.code, p.name, p.prodVersionNo, p.devVersionNo, p.insCompanyId
            from ProductEntity p
            where p.tId = :tId and p.recordStatus = 'ACTIVE'
              and (:insComp is null or p.insCompanyId = :insComp)
            order by p.code
            """)
    List<Object[]> listActiveSummaries(@Param("tId") Long tId, @Param("insComp") Long insComp);

    @Query(
        """
            select pr.roleProductId
            from ProductRoleEntity pr
            where pr.roleAccountEntity.id = ?1
        """
    )
    List<Long> findProductIdEntityByAccountId(Long accountId);

}
